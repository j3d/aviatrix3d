/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004-2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d.management;

// External imports
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ArrayList;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.picking.*;

import org.j3d.aviatrix3d.rendering.BoundingVolume;

import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;
import org.j3d.util.MatrixUtils;

/**
 * The default internal implementation of the pick handling system
 * that handles batch picking requests.
 * <p>
 *
 * Used for processing batch requests. The difference between this and mainline
 * picking is that instead of recursing down the scene graph with a single
 * request, each level is processed for all requests that are still valid before
 * descending to the next level. This allows for a lot of optimisation where
 * only a single traversal of the tree is needed for all pick requests.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>unknownPickTypeMsg: Error message when an unknown pick type is requested</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
class DefaultBatchPickingHandler
{
    /** Message when the PickRequest doesn't have one of the required types */
    private static final String NO_PICK_TYPE_PROP =
        "org.j3d.aviatrix3d.management.DefaultBatchPickingHandler.unknownPickTypeMsg";

    /** The initial size of the children list */
    private static final int LIST_START_SIZE = 32;

    /** The increment size of the list if it gets overflowed */
    private static final int LIST_INCREMENT = 8;

    /**
     * Current active depth flags for deciding whether to continue down this
     * stent path or it has already been terminated due to non-intersection
     * at this point or some higher level.
     */
    private int[] activePicks;

    /** Path from the root of the scene graph to the stent place */
    private PickTarget[] pickPath;

    /** Path of transforms from the root of the scene to the stent place */
    private Matrix4f[] transformPath;

    /** Flags to say with of the transforms are stently valid */
    private boolean[] validTransform;

    /** Last index of valid items on the path */
    private int lastPathIndex;

    /** Distance for the closest object found to date */
    private float closestDistance;

    /**
     * The working vector for start and finish locations or direction. The
     * first dimension corresponds to the activePicks index ie Which of the
     * requests in the batch are we stently looking at. The second dimension
     * is the pick depth down the transform heirarchy. This will correspond to
     * the depth down the scene graph we're at (lastPathIndex). The third
     * dimension is the temporary values for the stent depth.
     */
    private float[][][] start;
    private float[][][] end;
    private float[][][] extraData;

    /** Place to fetch the intersection point data for the line picking */
    private float[] vertexPickData;

    /** The matrix to set everything in and then invert it. */
    private Matrix4f vworldMatrix;

    /** The matrix containing the local inverted form. */
    private Matrix4f invertedMatrix;

    /** Working vector for interacting with the world matrix */
    private Vector4f wkVec;

    /** Working vector for interacting with the world matrix */
    private Vector3f wkNormal;

    /** Temp value for holding the frustum planes */
    private Vector4f[][] frustumPlanes;

    /** Matrix utility code for doing inversions */
    private MatrixUtils matrixUtils;

    /** Error reporter instance to use */
    private ErrorReporter errorReporter;

    /**
     * Initialise a new instance of the pick handler.
     */
    public DefaultBatchPickingHandler()
    {
        vertexPickData = new float[3];
        pickPath = new PickTarget[LIST_START_SIZE];
        transformPath = new Matrix4f[LIST_START_SIZE];
        validTransform = new boolean[LIST_START_SIZE];

        for(int i = 0; i < LIST_START_SIZE; i++)
            transformPath[i] = new Matrix4f();

        vworldMatrix = new Matrix4f();
        invertedMatrix = new Matrix4f();
        wkVec = new Vector4f();
        wkVec.w = 1;

        wkNormal = new Vector3f();

        // Only assign activePicks because the first time through the call to
        // here we're going to reallocate everything anyway in resizeForBatch.
        activePicks = new int[0];

        matrixUtils = new MatrixUtils();
        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the node's internals can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    void setErrorReporter(ErrorReporter reporter)
    {
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter = reporter;
    }

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given set of requests. Assumes
     * there has been some earlier processing to weed out the situations where
     * calling this class would be useless.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     * @param numRequests The number of valid pick requests to process
     * @throws NotPickableException This object has been marked as non pickable,
     *   but you decided to try to call the method anyway
     */
    void processPick(PickTarget root, PickRequest[] req, int numRequests)
        throws NotPickableException
    {
        resizeForBatch(numRequests);
        lastPathIndex = 0;
        transformPath[0].setIdentity();
        boolean keep_going = false;

        BoundingVolume bounds = root.getPickableBounds();

        if(bounds == null)
        {
            for(int i = 0; i < numRequests; i++)
                req[i].pickCount = 0;

            return;
        }

        for(int i = 0; i < numRequests; i++)
        {
            activePicks[i] = 0;
            req[i].pickCount = 0;

            // Sort out the basic scene graph path info first before descending
            // into the quagmire.
            switch(req[i].pickSortType)
            {
                case PickRequest.SORT_ALL:
                case PickRequest.SORT_ORDERED:
                    if(!(req[i].foundPaths instanceof ArrayList))
                        req[i].foundPaths = new ArrayList();
                    break;

                case PickRequest.SORT_ANY:
                case PickRequest.SORT_CLOSEST:
                    if(!(req[i].foundPaths instanceof SceneGraphPath))
                        req[i].foundPaths = new SceneGraphPath();
                    break;
            }

            float[] st = start[i][0];
            float[] ed = end[i][0];
            float[] data = extraData[i][0];

            switch(req[i].pickGeometryType)
            {
                case PickRequest.PICK_POINT:
                    if(bounds.checkIntersectionPoint(req[i].origin))
                    {
                        st[0] = req[i].origin[0];
                        st[1] = req[i].origin[1];
                        st[2] = req[i].origin[2];

                        activePicks[i]++;
                        keep_going = true;
                    }
                    break;

                case PickRequest.PICK_RAY:
                    if(bounds.checkIntersectionRay(req[i].origin,
                                                   req[i].destination))
                    {
                        st[0] = req[i].origin[0];
                        st[1] = req[i].origin[1];
                        st[2] = req[i].origin[2];

                        ed[0] = req[i].destination[0];
                        ed[1] = req[i].destination[1];
                        ed[2] = req[i].destination[2];

                        activePicks[i]++;
                        keep_going = true;
                    }
                    break;

                case PickRequest.PICK_LINE_SEGMENT:
                    if(bounds.checkIntersectionSegment(req[i].origin,
                                                       req[i].destination))
                    {
                        st[0] = req[i].origin[0];
                        st[1] = req[i].origin[1];
                        st[2] = req[i].origin[2];

                        ed[0] = req[i].destination[0];
                        ed[1] = req[i].destination[1];
                        ed[2] = req[i].destination[2];

                        activePicks[i]++;
                        keep_going = true;
                    }
                    break;

                case PickRequest.PICK_CYLINDER:
                case PickRequest.PICK_CYLINDER_SEGMENT:
                    float x = req[i].origin[0] - req[i].destination[0];
                    float y = req[i].origin[1] - req[i].destination[1];
                    float z = req[i].origin[2] - req[i].destination[2];

                    float height = (float)Math.sqrt(x * x + y * y + z * z);

                    if(height == 0)
                        return;

                    // Start will be the center and end will be the axis vector
                    st[0] = (req[i].origin[0] + req[i].destination[0]) * 0.5f;
                    st[1] = (req[i].origin[1] + req[i].destination[0]) * 0.5f;
                    st[2] = (req[i].origin[2] + req[i].destination[0]) * 0.5f;

                    ed[0] = x;
                    ed[1] = y;
                    ed[2] = z;

                    float radius = req[i].additionalData;

                    if(bounds.checkIntersectionCylinder(st, ed, radius, height))
                    {
                        data[0] = radius;
                        data[1] = height;

                        activePicks[i]++;
                        keep_going = true;
                    }
                    break;

                case PickRequest.PICK_CONE:
                case PickRequest.PICK_CONE_SEGMENT:
                    if(bounds.checkIntersectionCone(req[i].origin,
                                                    req[i].destination,
                                                    req[i].additionalData))
                    {
                        st[0] = req[i].origin[0];
                        st[1] = req[i].origin[1];
                        st[2] = req[i].origin[2];

                        ed[0] = req[i].destination[0];
                        ed[1] = req[i].destination[1];
                        ed[2] = req[i].destination[2];

                        data[0] = req[i].additionalData;

                        activePicks[i]++;
                        keep_going = true;
                    }
                    break;

                case PickRequest.PICK_BOX:
                    if(bounds.checkIntersectionBox(req[i].origin,
                                                   req[i].destination))
                    {
                        st[0] = req[i].origin[0];
                        st[1] = req[i].origin[1];
                        st[2] = req[i].origin[2];

                        ed[0] = req[i].destination[0];
                        ed[1] = req[i].destination[1];
                        ed[2] = req[i].destination[2];

                        activePicks[i]++;
                        keep_going = true;
                    }
                    break;

                case PickRequest.PICK_FRUSTUM:
                    frustumPlanes[i][0].x = req[i].origin[0];
                    frustumPlanes[i][0].y = req[i].origin[1];
                    frustumPlanes[i][0].z = req[i].origin[2];
                    frustumPlanes[i][0].w = req[i].origin[3];

                    frustumPlanes[i][1].x = req[i].origin[4];
                    frustumPlanes[i][1].y = req[i].origin[5];
                    frustumPlanes[i][1].z = req[i].origin[6];
                    frustumPlanes[i][1].w = req[i].origin[7];

                    frustumPlanes[i][2].x = req[i].origin[8];
                    frustumPlanes[i][2].y = req[i].origin[9];
                    frustumPlanes[i][2].z = req[i].origin[10];
                    frustumPlanes[i][2].w = req[i].origin[11];

                    frustumPlanes[i][3].x = req[i].origin[12];
                    frustumPlanes[i][3].y = req[i].origin[13];
                    frustumPlanes[i][3].z = req[i].origin[14];
                    frustumPlanes[i][3].w = req[i].origin[15];

                    frustumPlanes[i][4].x = req[i].origin[16];
                    frustumPlanes[i][4].y = req[i].origin[17];
                    frustumPlanes[i][4].z = req[i].origin[18];
                    frustumPlanes[i][4].w = req[i].origin[19];

                    frustumPlanes[i][5].x = req[i].origin[20];
                    frustumPlanes[i][5].y = req[i].origin[21];
                    frustumPlanes[i][5].z = req[i].origin[22];
                    frustumPlanes[i][5].w = req[i].origin[23];

                    if(bounds.checkIntersectionFrustum(frustumPlanes[i],
                                                       transformPath[lastPathIndex])
                       == BoundingVolume.FRUSTUM_ALLOUT)
                    {
                        activePicks[i]++;
                        keep_going = true;
                    }
                    break;

                case PickRequest.PICK_SPHERE:
                    if(bounds.checkIntersectionSphere(req[i].origin,
                                                      req[i].additionalData))
                    {
                        st[0] = req[i].origin[0];
                        st[1] = req[i].origin[1];
                        st[2] = req[i].origin[2];

                        data[0] = req[i].additionalData;

                        activePicks[i]++;
                        keep_going = true;
                    }
                    break;

                default:
					I18nManager intl_mgr = I18nManager.getManager();
					String msg_pattern = intl_mgr.getString(NO_PICK_TYPE_PROP);

					Locale lcl = intl_mgr.getFoundLocale();

					NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

					Object[] msg_args = { new Integer(req[i].pickGeometryType) };
					Format[] fmts = { n_fmt };
					MessageFormat msg_fmt =
						new MessageFormat(msg_pattern, lcl);
					msg_fmt.setFormats(fmts);
					String msg = msg_fmt.format(msg_args);
					errorReporter.warningReport(msg, null);
            }
        }

        if(keep_going)
            descendTree(root, req, numRequests);
    }

    /**
     * Descend to the next level of the scene graph tree.
     *
     * @param parent The parent node that is to be descended into
     * @param req The list of picks to be made, starting at this object
     * @param numRequests The number of valid pick requests to process
     * @throws NotPickableException This object has been marked as non pickable,
     *   but you decided to try to call the method anyway
     */
    private void descendTree(PickTarget parent, PickRequest[] req, int numRequests)
    {
        BoundingVolume bounds = parent.getPickableBounds();
        PickTarget target_node = parent;
        int num_kids = 0;

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
                target_node = (PickTarget)geom;
            else
                return;
        }

        // Early bug out if needed.

        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                num_kids = g.numPickableChildren();

                if(num_kids == 0)
                    return;

                break;

            case PickTarget.SINGLE_PICK_TYPE:
                    descendSingleTree((SinglePickTarget)target_node,
                                      req,
                                      numRequests);
                    return;

            case PickTarget.LEAF_PICK_TYPE:
                testLeaf((LeafPickTarget)target_node, req, numRequests);
                return;
        }

        // If the parent is a transform, we need to take the current values
        // and transform them across to the new items.
        if(target_node instanceof TransformPickTarget)
        {
            Matrix4f tx = transformPath[lastPathIndex];
            ((TransformPickTarget)target_node).getTransform(tx);
            ((TransformPickTarget)target_node).getInverseTransform(invertedMatrix);

            float scale = invertedMatrix.getScale();

            // multiply the transform matrix through to the current level.
            tx.mul(transformPath[lastPathIndex - 1], tx);
            validTransform[lastPathIndex] = true;

            for(int i = 0; i < numRequests; i++)
            {
                if(activePicks[i] < lastPathIndex)
                    continue;

                switch(req[i].pickGeometryType)
                {
                    // do nothing else
                    case PickRequest.PICK_POINT:
                        transform(invertedMatrix,
                                  start[i][lastPathIndex],
                                  start[i][lastPathIndex + 1]);
                        break;

                    case PickRequest.PICK_RAY:
                        transform(invertedMatrix,
                                  start[i][lastPathIndex],
                                  start[i][lastPathIndex + 1]);
                        transformNormal(invertedMatrix,
                                        end[i][lastPathIndex],
                                        end[i][lastPathIndex + 1]);
                        break;

                    case PickRequest.PICK_LINE_SEGMENT:
                        transform(invertedMatrix,
                                  start[i][lastPathIndex],
                                  start[i][lastPathIndex + 1]);
                        transform(invertedMatrix,
                                  end[i][lastPathIndex],
                                  end[i][lastPathIndex + 1]);
                        break;

                    case PickRequest.PICK_CYLINDER:
                    case PickRequest.PICK_CYLINDER_SEGMENT:
                        transform(invertedMatrix,
                                  start[i][lastPathIndex],
                                  start[i][lastPathIndex + 1]);
                        transformNormal(invertedMatrix,
                                        end[i][lastPathIndex],
                                        end[i][lastPathIndex + 1]);

                        extraData[i][lastPathIndex + 1][0] =
                            scale * extraData[i][lastPathIndex][0];
                        extraData[i][lastPathIndex + 1][1] =
                            scale * extraData[i][lastPathIndex][1];
                        break;

                    case PickRequest.PICK_CONE:
                    case PickRequest.PICK_CONE_SEGMENT:
                        transform(invertedMatrix,
                                  start[i][lastPathIndex],
                                  start[i][lastPathIndex + 1]);
                        transformNormal(invertedMatrix,
                                        end[i][lastPathIndex],
                                        end[i][lastPathIndex + 1]);

                        extraData[i][lastPathIndex + 1][0] =
                            scale * extraData[i][lastPathIndex][0];
                        break;

                    case PickRequest.PICK_BOX:
                        transform(invertedMatrix,
                                  start[i][lastPathIndex],
                                  start[i][lastPathIndex + 1]);
                        transform(invertedMatrix,
                                  end[i][lastPathIndex],
                                  end[i][lastPathIndex + 1]);
                        break;

                    case PickRequest.PICK_FRUSTUM:
                        // do nothing here for frustum picking
                        break;

                    case PickRequest.PICK_SPHERE:
                        transform(invertedMatrix,
                                  start[i][lastPathIndex],
                                  start[i][lastPathIndex + 1]);

                        extraData[i][lastPathIndex + 1][0] =
                            scale * extraData[i][lastPathIndex][0];
                        break;

                    default:
                }
            }
        }
        else
        {
            // don't bother setting to identity, just mark it as non-valid.
            // Assignment is a waste of CPU cycles. Then copy over the details
            // from the level above.
            validTransform[lastPathIndex] = false;
            if(lastPathIndex == 0)
                transformPath[lastPathIndex].setIdentity();
            else
                transformPath[lastPathIndex].set(transformPath[lastPathIndex - 1]);

            for(int i = 0; i < numRequests; i++)
            {
                if(activePicks[i] < lastPathIndex)
                    continue;

                start[i][lastPathIndex + 1][0] = start[i][lastPathIndex][0];
                start[i][lastPathIndex + 1][1] = start[i][lastPathIndex][1];
                start[i][lastPathIndex + 1][2] = start[i][lastPathIndex][2];

                end[i][lastPathIndex + 1][0] = end[i][lastPathIndex][0];
                end[i][lastPathIndex + 1][1] = end[i][lastPathIndex][1];
                end[i][lastPathIndex + 1][2] = end[i][lastPathIndex][2];

                extraData[i][lastPathIndex + 1][0] = extraData[i][lastPathIndex][0];
                extraData[i][lastPathIndex + 1][1] = extraData[i][lastPathIndex][1];
            }
        }

        GroupPickTarget g = (GroupPickTarget)target_node;
        PickTarget[] kids = g.getPickableChildren();
        boolean keep_going;

        pickPath[lastPathIndex] = g;
        lastPathIndex++;

        for(int i = 0; i < num_kids; i++)
        {
            keep_going = false;

            if(kids[i] == null)
                continue;

            bounds = kids[i].getPickableBounds();

            for(int j = 0; j < numRequests; j++)
            {
                if(activePicks[j] < lastPathIndex)
                    continue;

                switch(req[j].pickGeometryType)
                {
                    // do nothing else
                    case PickRequest.PICK_POINT:
                        if(bounds.checkIntersectionPoint(start[j][lastPathIndex]))
                        {
                            keep_going = true;
                            activePicks[j]++;
                        }
                        break;

                    case PickRequest.PICK_RAY:
                        if(bounds.checkIntersectionRay(start[j][lastPathIndex],
                                                       end[j][lastPathIndex]))
                        {
                            keep_going = true;
                            activePicks[j]++;
                        }
                        break;

                    case PickRequest.PICK_LINE_SEGMENT:
                        if(bounds.checkIntersectionSegment(start[j][lastPathIndex],
                                                           end[j][lastPathIndex]))
                        {
                            keep_going = true;
                            activePicks[j]++;
                        }
                        break;

                    case PickRequest.PICK_CYLINDER:
                    case PickRequest.PICK_CYLINDER_SEGMENT:
                        if(bounds.checkIntersectionCylinder(start[j][lastPathIndex],
                                                            end[j][lastPathIndex],
                                                            extraData[j][lastPathIndex][0],
                                                            extraData[j][lastPathIndex][1]))
                        {
                            keep_going = true;
                            activePicks[j]++;
                        }
                        break;

                    case PickRequest.PICK_CONE:
                    case PickRequest.PICK_CONE_SEGMENT:
                        if(bounds.checkIntersectionCone(start[j][lastPathIndex],
                                                        end[j][lastPathIndex],
                                                        extraData[j][lastPathIndex][0]))
                        {
                            keep_going = true;
                            activePicks[j]++;
                        }
                        break;

                    case PickRequest.PICK_BOX:
                        if(bounds.checkIntersectionBox(start[j][lastPathIndex],
                                                       end[j][lastPathIndex]))
                        {
                            keep_going = true;
                                activePicks[j]++;
                        }
                        break;

                    case PickRequest.PICK_FRUSTUM:
                        if(bounds.checkIntersectionFrustum(frustumPlanes[j],
                                                           transformPath[lastPathIndex])
                           == BoundingVolume.FRUSTUM_ALLOUT)
                        {
                            keep_going = true;
                            activePicks[j]++;
                        }
                        break;

                    case PickRequest.PICK_SPHERE:
                        if(bounds.checkIntersectionSphere(start[j][lastPathIndex],
                                                         extraData[j][lastPathIndex][0]))
                        {
                            keep_going = true;
                            activePicks[j]++;
                        }
                        break;

                    default:
                }
            }

            if(keep_going)
            {
                descendTree(kids[i], req, numRequests);

                // decrement matching picks
                for(int j = 0; j < numRequests; j++)
                {
                    if(activePicks[j] > lastPathIndex)
                        activePicks[j]--;
                }
            }
        }

        lastPathIndex--;
        pickPath[lastPathIndex] = null;
    }

    /**
     * We've hit a shared node. Descend down it's children until we find a real
     * node instance again.
     *
     * @param parent The shared node that is to be descended
     * @param req The list of picks to be made, starting at this object
     * @param numRequests The number of valid pick requests to process
     * @throws NotPickableException This object has been marked as non pickable,
     *   but you decided to try to call the method anyway
     */
    private void descendSingleTree(SinglePickTarget parent,
                                   PickRequest[] req,
                                   int numRequests)
    {
        boolean keep_going = false;

        for(int i = 0; i < numRequests; i++)
        {
            if(parent.checkPickMask(req[i].pickType))
            {
                keep_going = true;
                break;
            }
        }

        if(!keep_going)
            return;

        resizePath();
        validTransform[lastPathIndex] = false;
        pickPath[lastPathIndex] = parent;
        lastPathIndex++;

        PickTarget child = parent.getPickableChild();

        switch(child.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                descendTree((GroupPickTarget)child, req, numRequests);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                descendSingleTree((SinglePickTarget)child, req, numRequests);
                break;

            case PickTarget.LEAF_PICK_TYPE:
                testLeaf((LeafPickTarget)child, req, numRequests);
                break;
        }

        lastPathIndex--;
        pickPath[lastPathIndex] = null;
    }

    /**
     * Do the final checks on a shape to see if it can be intersected by the
     * geometry.
     *
     * @param target The shape that is to be tested
     * @param req The list of picks to be made, starting at this object
     * @param numRequests The number of valid pick requests to process
     * @throws NotPickableException This object has been marked as non pickable,
     *   but you decided to try to call the method anyway
     */
    private void testLeaf(LeafPickTarget target,
                          PickRequest[] req,
                          int numRequests)
    {
        boolean intersect;
        BoundingVolume bounds = target.getPickableBounds();

        for(int i = 0; i < numRequests; i++)
        {
            // Only process this if we have an intersection for the parent,
            // there is the right pick type to match what we're looking for,
            // or we are not in pick_any mode with a match already.
            if((activePicks[i] < lastPathIndex) ||
               !target.checkPickMask(req[i].pickType) ||
                ((req[i].pickSortType == PickRequest.SORT_ANY) &&
                 (req[i].pickCount == 1)))
                continue;

            intersect = false;

            switch(req[i].pickGeometryType)
            {
                // do nothing else
                case PickRequest.PICK_POINT:
                    intersect =
                        bounds.checkIntersectionPoint(start[i][lastPathIndex]);
                    break;

                case PickRequest.PICK_RAY:
                    if(bounds.checkIntersectionRay(start[i][lastPathIndex],
                                                   end[i][lastPathIndex]))
                    {
                        if(req[i].useGeometry)
                        {
                            boolean sort =
                                req[i].pickSortType == PickRequest.SORT_CLOSEST ||
                                req[i].pickSortType == PickRequest.SORT_ORDERED;


                            intersect = target.pickLineRay(start[i][lastPathIndex],
                                                           end[i][lastPathIndex],
                                                           sort,
                                                           vertexPickData,
                                                           0);

                            // TODO:
                            // Need to have something here that checks distance
                            // and updates only when required.
                            intersect = true;
                        }
                        else
                            intersect = true;
                    }
                    break;

                case PickRequest.PICK_LINE_SEGMENT:
                    if(bounds.checkIntersectionSegment(start[i][lastPathIndex],
                                                       end[i][lastPathIndex]))
                    {
                        if(req[i].useGeometry)
                        {
                            boolean sort =
                                req[i].pickSortType == PickRequest.SORT_CLOSEST ||
                                req[i].pickSortType == PickRequest.SORT_ORDERED;


                            intersect = target.pickLineSegment(start[i][lastPathIndex],
                                                               end[i][lastPathIndex],
                                                               sort,
                                                               vertexPickData,
                                                               0);
                            // TODO:
                            // Need to have something here that checks distance
                            // and updates only when required.
                            intersect = true;
                        }
                        else
                            intersect = true;
                    }
                    break;

                case PickRequest.PICK_CYLINDER:
                case PickRequest.PICK_CYLINDER_SEGMENT:
                    intersect =
                        bounds.checkIntersectionCylinder(start[i][lastPathIndex],
                                                         end[i][lastPathIndex],
                                                         extraData[i][lastPathIndex][0],
                                                         extraData[i][lastPathIndex][1]);
                    break;

                case PickRequest.PICK_CONE:
                case PickRequest.PICK_CONE_SEGMENT:
                    intersect =
                        bounds.checkIntersectionCone(start[i][lastPathIndex],
                                                     end[i][lastPathIndex],
                                                     extraData[i][lastPathIndex][0]);
                    break;

                case PickRequest.PICK_BOX:
                    intersect =
                        bounds.checkIntersectionBox(start[i][lastPathIndex],
                                                    end[i][lastPathIndex]);
                    break;

                case PickRequest.PICK_FRUSTUM:
                    intersect =
                        (bounds.checkIntersectionFrustum(frustumPlanes[i],
                                                         transformPath[lastPathIndex])
                           == BoundingVolume.FRUSTUM_ALLOUT);
                    break;

                case PickRequest.PICK_SPHERE:
                    intersect =
                        bounds.checkIntersectionSphere(start[i][lastPathIndex],
                                                       extraData[i][lastPathIndex][0]);
                    break;

                default:
            }

            // TODO:
            // Optimise this heavily. We should only need to calculate all the
            // vworld transforms etc just once per call to this method, not for
            // every iteration of the loop. Need some flags at the top ofthe
            // method to indicate if these have already been calculated.
            if(intersect)
            {
                resizePath();
                pickPath[lastPathIndex] = target;
                validTransform[lastPathIndex] = false;
                lastPathIndex++;

                SceneGraphPath p = null;

                switch(req[i].pickSortType)
                {
                    case PickRequest.SORT_ALL:
                    case PickRequest.SORT_ORDERED:
                        ArrayList<SceneGraphPath> paths =
                        	(ArrayList<SceneGraphPath>)req[i].foundPaths;
                        if(req[i].pickCount >= paths.size())
                        {
                            p = new SceneGraphPath();
                            paths.add(p);
                        }
                        else
                            p = (SceneGraphPath)paths.get(req[i].pickCount);

                        break;

                    case PickRequest.SORT_ANY:
                    case PickRequest.SORT_CLOSEST:
                        p = (SceneGraphPath)req[i].foundPaths;
                        break;
                }

                // Note that the transform path is already pre-multiplied
                // through here. Just and invert is all that needs to be done.
                if(req[i].generateVWorldMatrix)
                    matrixUtils.inverse(transformPath[lastPathIndex - 1],
                                        vworldMatrix);
                else
                    vworldMatrix.setIdentity();

                p.updatePath(pickPath,
                             lastPathIndex,
                             transformPath[lastPathIndex - 1],
                             vworldMatrix);

                req[i].pickCount++;
                lastPathIndex--;
            }
        }
    }

    /**
     * Resize the list if needed. Marked as final in order to encourage the
     * compiler to inline the code for faster execution
     */
    private final void resizePath()
    {
        if(lastPathIndex == pickPath.length)
        {
            int old_size = pickPath.length;
            int new_size = old_size + LIST_INCREMENT;

            PickTarget[] tmp_nodes = new PickTarget[new_size];

            System.arraycopy(pickPath, 0, tmp_nodes, 0, old_size);

            pickPath = tmp_nodes;

            Matrix4f[] tmp_tx = new Matrix4f[new_size];
            System.arraycopy(transformPath, 0, tmp_tx, 0, old_size);
            transformPath = tmp_tx;

            for(int i = old_size; i < new_size; i++)
                transformPath[i] = new Matrix4f();

            boolean[] tmp_flags = new boolean[new_size];
            System.arraycopy(validTransform, 0, tmp_flags, 0, old_size);
            validTransform = tmp_flags;
        }
    }

    /**
     * Resize all the top level arrays that depend on the number of items in
     * the batch picking request. Only called at the start of the request.
     *
     * @param numBatch The number of valid items in the request
     */
    private void resizeForBatch(int numBatch)
    {
        if(numBatch <= activePicks.length)
            return;

        // Just wipe out the old ones with new values. No point copying over
        // as this should happen vary rarely - probably only once in the
        // lifecycle of the application.
        activePicks = new int[numBatch];

        int depth = lastPathIndex == 0 ? LIST_START_SIZE : lastPathIndex;
        start = new float[numBatch][depth][3];
        end = new float[numBatch][depth][3];
        extraData = new float[numBatch][depth][2];

        frustumPlanes = new Vector4f[numBatch][6];

        for(int i = 0; i < numBatch; i++)
        {
            frustumPlanes[i][0] = new Vector4f();
            frustumPlanes[i][1] = new Vector4f();
            frustumPlanes[i][2] = new Vector4f();
            frustumPlanes[i][3] = new Vector4f();
            frustumPlanes[i][4] = new Vector4f();
            frustumPlanes[i][5] = new Vector4f();
        }
    }

    /**
     * Transform the location vector by the matrix. Location includes the
     * translation offset.
     *
     * @param mat The matrix to do the transformation with
     * @param vec The vector to be changed
     * @param out The vector to put the updated values in
     */
    private void transform(Matrix4f mat, float[] vec, float[] out)
    {
        float x = vec[0];
        float y = vec[1];
        float z = vec[2];

        out[0] = x * mat.m00 + y * mat.m01 + z * mat.m02 + mat.m03;
        out[1] = x * mat.m10 + y * mat.m11 + z * mat.m12 + mat.m13;
        out[2] = x * mat.m20 + y * mat.m21 + z * mat.m22 + mat.m23;
    }

    /**
     * Transform the normal vector by the matrix. The normal vector does not
     * include the translation offset.
     *
     * @param mat The matrix to do the transformation with
     * @param vec The vector to be changed
     * @param out The vector to put the updated values in
     */
    private void transformNormal(Matrix4f mat, float[] vec, float[] out)
    {
        float x = vec[0];
        float y = vec[1];
        float z = vec[2];

        out[0] = x * mat.m00 + y * mat.m01 + z * mat.m02;
        out[1] = x * mat.m10 + y * mat.m11 + z * mat.m12;
        out[2] = x * mat.m20 + y * mat.m21 + z * mat.m22;
    }
}
