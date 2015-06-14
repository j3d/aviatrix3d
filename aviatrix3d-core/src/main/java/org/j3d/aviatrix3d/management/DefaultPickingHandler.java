/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2006
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
import java.util.Collection;
import java.util.Locale;
import java.util.ArrayList;

import org.j3d.maths.vector.*;
import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.picking.*;

import org.j3d.aviatrix3d.rendering.BoundingVolume;

import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;
import org.j3d.util.MatrixUtils;

/**
 * The default internal implementation of the pick handling system.
 * tests.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>unknownPickTypeMsg: Error message when an unknown pick type is requested</li>
 * <li>unknownSortTypeMsg: Error message when an unknown sort type is requested</li>
 * <li>unknownProxyTypeMsg: Error message for an unknown proxy geometry type</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.15 $
 */
class DefaultPickingHandler
    implements PickingManager
{
    /** Message when the PickRequest doesn't have one of the required types */
    private static final String NO_PICK_TYPE_PROP =
        "org.j3d.aviatrix3d.management.DefaultPickingHandler.unknownPickTypeMsg";

    /** Message when the PickRequest doesn't have one of the required types */
    private static final String NO_SORT_TYPE_PROP =
        "org.j3d.aviatrix3d.management.DefaultPickingHandler.unknownSortTypeMsg";

    /** Message when the proxy geometry is not a known picking type */
    private static final String UNKNOWN_PROXY_TYPE_PROP =
        "org.j3d.aviatrix3d.management.DefaultPickingHandler.unknownProxyTypeMsg";

    /** The initial size of the children list */
    private static final int LIST_START_SIZE = 32;

    /** The increment size of the list if it gets overflowed */
    private static final int LIST_INCREMENT = 8;

    /** Handler for batch picking implementation. This class is big enough! */
    private DefaultBatchPickingHandler batchPicker;

    /** Path from the root of the scene graph to the current place */
    private PickTarget[] pickPath;

    /** Path of transforms from the root of the scene to the current place */
    private Matrix4d[] transformPath;

    /** Flags to say with of the transforms are currently valid */
    private boolean[] validTransform;

    /** Last index of valid items on the path */
    private int lastPathIndex;

    /** Distance for the closest object found to date */
    private float closestDistance;

    /** The working vector for start location or direction */
    private float[] start;

    /** The working vector for finish location or direction */
    private float[] end;

    /** Place to fetch the intersection point data for the line picking */
    private float[] vertexPickData;

    /** The matrix to set everything in and then invert it. */
    private Matrix4d vworldMatrix;

    /** The matrix containing the local inverted form. */
    private Matrix4d invertedMatrix;

    /** Working vector for interacting with the world matrix */
    private Vector4d wkVec;

    /** Working vector for interacting with the world matrix */
    private Vector3d wkNormal;

    /** Temp value for holding the frustum planes */
    private Vector4d[] frustumPlanes;

    /** Matrix utility code for doing inversions */
    private MatrixUtils matrixUtils;

    /** The pick instructions used to fetch custom pick structures */
    private PickInstructions pickInstructions;

    /** Error reporter instance to use */
    private ErrorReporter errorReporter;

    /**
     * Initialise a new instance of the pick handler.
     */
    DefaultPickingHandler()
    {
        start = new float[4];
        end = new float[4];
        start[3] = 1;
        end[3] = 1;

        vertexPickData = new float[3];
        pickPath = new PickTarget[LIST_START_SIZE];
        transformPath = new Matrix4d[LIST_START_SIZE];
        validTransform = new boolean[LIST_START_SIZE];

        for(int i = 0; i < LIST_START_SIZE; i++)
            transformPath[i] = new Matrix4d();

        vworldMatrix = new Matrix4d();
        invertedMatrix = new Matrix4d();
        wkVec = new Vector4d();
        wkVec.w = 1;

        wkNormal = new Vector3d();

        frustumPlanes = new Vector4d[]
        {
            new Vector4d(), new Vector4d(), new Vector4d(),
            new Vector4d(), new Vector4d(), new Vector4d()
        };

        matrixUtils = new MatrixUtils();
        pickInstructions = new PickInstructions();
        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    //---------------------------------------------------------------
    // Methods defined by PickingManager
    //---------------------------------------------------------------

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the node's internals can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    @Override
    public void setErrorReporter(ErrorReporter reporter)
    {
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter = reporter;

        if(batchPicker != null)
        {
            batchPicker.setErrorReporter(errorReporter);
        }
    }

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given set of requests.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     * @param numRequests The number of valid pick requests to process
     * @throws NotPickableException This object has been marked as non pickable,
     *   but you decided to try to call the method anyway
     */
    @Override
    public synchronized void pickBatch(PickTarget root,
                                       PickRequest[] req,
                                       int numRequests)
        throws NotPickableException
    {
        // Weed out the common cases first.
        if(numRequests == 0 || root == null || req == null || req.length == 0)
            return;

        if(numRequests == 1 || req.length == 1)
            pickSingle(root, req[0]);
        else
        {
            if(batchPicker == null)
            {
                batchPicker = new DefaultBatchPickingHandler();
                batchPicker.setErrorReporter(errorReporter);
            }

            batchPicker.processPick(root, req, numRequests);
        }
    }

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given set of requests.
     *
     * @param root The root point to start the pick processing from
     * @param req The details of the pick to be made
     * @throws NotPickableException This object has been marked as non pickable,
     *   but you decided to try to call the method anyway
     */
    @Override
    public synchronized void pickSingle(PickTarget root, PickRequest req)
        throws NotPickableException
    {
        if(req == null)
        {
            return;
        }

        req.pickCount = 0;

        if(root == null || !root.checkPickMask(req.pickType))
        {
            return;
        }

        switch(req.pickSortType)
        {
            case PickRequest.SORT_ALL:
            case PickRequest.SORT_ORDERED:
                if(!(req.foundPaths instanceof Collection))
                {
                    req.foundPaths = new ArrayList<SceneGraphPath>();
                }

                break;

            case PickRequest.SORT_ANY:
            case PickRequest.SORT_CLOSEST:
                if(!(req.foundPaths instanceof SceneGraphPath))
                {
                    req.foundPaths = new SceneGraphPath();
                }
                break;

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(NO_SORT_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

                Object[] msg_args = { req.pickSortType };
                Format[] fmts = { n_fmt };
                MessageFormat msg_fmt = new MessageFormat(msg_pattern, lcl);
                msg_fmt.setFormats(fmts);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
                return;
        }

        switch(req.pickGeometryType)
        {
            case PickRequest.PICK_POINT:
                pickPoint(root, req);
                break;

            case PickRequest.PICK_RAY:
                pickRay(root, req);
                break;

            case PickRequest.PICK_LINE_SEGMENT:
                pickLineSegment(root, req);
                break;

            case PickRequest.PICK_CYLINDER:
            case PickRequest.PICK_CYLINDER_SEGMENT:
                pickCylinder(root, req);
                break;

            case PickRequest.PICK_CONE:
            case PickRequest.PICK_CONE_SEGMENT:
                pickCone(root, req);
                break;

            case PickRequest.PICK_BOX:
                pickBox(root, req);
                break;

            case PickRequest.PICK_FRUSTUM:
                pickFrustum(root, req);
                break;

            case PickRequest.PICK_SPHERE:
                pickSphere(root, req);
                break;

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(NO_PICK_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

                Object[] msg_args = { req.pickGeometryType };
                Format[] fmts = { n_fmt };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                msg_fmt.setFormats(fmts);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
        }

        for(int i = 0; i < pickPath.length; i++)
        {
            pickPath[i] = null;
        }
    }

    //---------------------------------------------------------------
    // Misc Internal methods
    //---------------------------------------------------------------

    // ----------------------- Point Picking ------------------------------

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given ray. Return the first
     * found.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     */
    private void pickPoint(PickTarget root, PickRequest req)
    {
        switch(req.pickSortType)
        {
            case PickRequest.SORT_ALL:
            case PickRequest.SORT_ORDERED:
                pickAllPoint(root, req);
                break;

            case PickRequest.SORT_ANY:
            case PickRequest.SORT_CLOSEST:
                pickSinglePoint(root, req);
                break;
        }
    }

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given ray. Return the first
     * found.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     */
    private void pickSinglePoint(PickTarget root, PickRequest req)
    {
        if(!root.checkPickMask(req.pickType))
        {
            return;
        }

        BoundingVolume bounds = root.getPickableBounds();

        if(bounds == null || !bounds.checkIntersectionPoint(req.origin))
        {
            return;
        }

        PickTarget target_node = root;

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
            {
                target_node = (PickTarget) geom;
            }
            else
            {
                return;
            }
        }

        start[0] = req.origin[0];
        start[1] = req.origin[1];
        start[2] = req.origin[2];
        start[3] = 1;

        // reset the transform at the top of the stack
        if(target_node instanceof TransformPickTarget)
        {
            TransformPickTarget tg = (TransformPickTarget)target_node;
            tg.getTransform(transformPath[lastPathIndex]);
            tg.getInverseTransform(invertedMatrix);
            transform(invertedMatrix, start);

            validTransform[lastPathIndex] = true;
        }
        else
        {
            transformPath[lastPathIndex].setIdentity();
            validTransform[lastPathIndex] = false;
        }

        pickPath[lastPathIndex] = target_node;
        lastPathIndex++;

        boolean found = false;
        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    PickTarget[] kids = g.getPickableChildren();

                    for(int i = 0; i < num_kids && !found; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickSinglePoint(kids[i], req);
                        found = req.pickCount != 0;
                    }
                }
                break;

            case PickTarget.LEAF_PICK_TYPE:
                updatePathAfterSuccess((LeafPickTarget)root, req);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                target_node = ((SinglePickTarget)target_node).getPickableChild();

                if(target_node != null)
                {
                    pickSinglePoint(target_node, req);
                }
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                if(pickCustom((CustomPickTarget)target_node, req))
                {
                    // reset the transform at the top of the stack based on our local
                    if(pickInstructions.hasTransform)
                    {
                        transformPath[lastPathIndex - 1].set(pickInstructions.localTransform);

                        matrixUtils.inverse(transformPath[lastPathIndex - 1], invertedMatrix);
                        transform(invertedMatrix, start);
                    }

                    // Make sure to clone the array locally because if we are recursing the global
                    // list will be overwritten each time we go down a level
                    num_kids = pickInstructions.numChildren;
                    PickTarget[] kids = pickInstructions.children.clone();

                    for(int i = 0; i < num_kids && !found; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickSinglePoint(kids[i], req);
                        found = req.pickCount != 0;
                    }
                }
                break;
        }

        lastPathIndex--;
    }

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given ray. Return the first
     * found.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     */
    private void pickAllPoint(PickTarget root, PickRequest req)
    {
        if(!root.checkPickMask(req.pickType))
        {
            return;
        }

        BoundingVolume bounds = root.getPickableBounds();

        if(bounds == null || !bounds.checkIntersectionPoint(req.origin))
        {
            return;
        }

        PickTarget target_node = root;

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
            {
                target_node = (PickTarget) geom;
            }
            else
            {
                return;
            }
        }

        start[0] = req.origin[0];
        start[1] = req.origin[1];
        start[2] = req.origin[2];
        start[3] = 1;

        // reset the transform at the top of the stack
        if(target_node instanceof TransformPickTarget)
        {
            TransformPickTarget tg = (TransformPickTarget)target_node;
            tg.getTransform(transformPath[lastPathIndex]);
            tg.getInverseTransform(invertedMatrix);
            transform(invertedMatrix, start);

            validTransform[lastPathIndex] = true;
        }
        else
        {
            transformPath[lastPathIndex].setIdentity();
            validTransform[lastPathIndex] = false;
        }

        pickPath[lastPathIndex] = target_node;
        lastPathIndex++;

        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    PickTarget[] kids = g.getPickableChildren();

                    for(int i = 0; i < num_kids; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickAllPoint(kids[i], req);
                    }
                }
                break;

            case PickTarget.LEAF_PICK_TYPE:
                updatePathAfterSuccess((LeafPickTarget) root, req);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                target_node = ((SinglePickTarget)target_node).getPickableChild();

                if(target_node != null)
                {
                    pickAllPoint(target_node, req);
                }
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                if(pickCustom((CustomPickTarget)target_node, req))
                {
                    // reset the transform at the top of the stack based on our local
                    if(pickInstructions.hasTransform)
                    {
                        transformPath[lastPathIndex - 1].set(pickInstructions.localTransform);

                        matrixUtils.inverse(transformPath[lastPathIndex - 1], invertedMatrix);
                        transform(invertedMatrix, start);
                    }

                    // Make sure to clone the array locally because if we are recursing the global
                    // list will be overwritten each time we go down a level
                    num_kids = pickInstructions.numChildren;
                    PickTarget[] kids = pickInstructions.children.clone();

                    for(int i = 0; i < num_kids; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickAllPoint(kids[i], req);
                    }
                }
                break;
        }

        lastPathIndex--;
    }

    // ----------------------- Line Picking ------------------------------

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given ray. Return the first
     * found.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     */
    private void pickLineSegment(PickTarget root, PickRequest req)
    {
        switch(req.pickSortType)
        {
            case PickRequest.SORT_ALL:
            case PickRequest.SORT_ORDERED:
                pickAllLineSegment(root, req);
                break;

            case PickRequest.SORT_ANY:
            case PickRequest.SORT_CLOSEST:
                pickSingleLineSegment(root, req);
                break;
        }
    }

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given ray. Return the first
     * found.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     */
    private void pickSingleLineSegment(PickTarget root, PickRequest req)
    {
        if(!root.checkPickMask(req.pickType))
        {
            return;
        }

        PickTarget target_node = root;

        BoundingVolume bounds = root.getPickableBounds();

        if(bounds == null || !bounds.checkIntersectionSegment(req.origin, req.destination))
        {
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
            {
                target_node = (PickTarget) geom;
            }
            else
            {
                return;
            }
        }

        start[0] = req.origin[0];
        start[1] = req.origin[1];
        start[2] = req.origin[2];
        start[3] = 1;

        end[0] = req.destination[0];
        end[1] = req.destination[1];
        end[2] = req.destination[2];
        end[3] = 1;

        // reset the transform at the top of the stack
        if(target_node instanceof TransformPickTarget)
        {
            TransformPickTarget tg = (TransformPickTarget)target_node;
            tg.getTransform(transformPath[lastPathIndex]);
            tg.getInverseTransform(invertedMatrix);
            transform(invertedMatrix, start);
            transform(invertedMatrix, end);

            validTransform[lastPathIndex] = true;
        }
        else
        {
            transformPath[lastPathIndex].setIdentity();
            validTransform[lastPathIndex] = false;
        }

        pickPath[lastPathIndex] = target_node;
        lastPathIndex++;

        boolean found = false;
        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    PickTarget[] kids = g.getPickableChildren();

                    for(int i = 0; i < num_kids && !found; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickSingleLineSegment(kids[i], req);
                        found = req.pickCount != 0;
                    }
                }
                break;

            case PickTarget.LEAF_PICK_TYPE:
                updatePathAfterSuccess((LeafPickTarget) root, req);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                target_node = ((SinglePickTarget)target_node).getPickableChild();

                if(target_node != null)
                {
                    pickSingleLineSegment(target_node, req);
                }
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                if(pickCustom((CustomPickTarget)target_node, req))
                {
                    // reset the transform at the top of the stack based on our local
                    if(pickInstructions.hasTransform)
                    {
                        transformPath[lastPathIndex - 1].set(pickInstructions.localTransform);

                        matrixUtils.inverse(transformPath[lastPathIndex - 1], invertedMatrix);
                        transform(invertedMatrix, start);
                    }

                    // Make sure to clone the array locally because if we are recursing the global
                    // list will be overwritten each time we go down a level
                    num_kids = pickInstructions.numChildren;
                    PickTarget[] kids = pickInstructions.children.clone();

                    for(int i = 0; i < num_kids && !found; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickSingleLineSegment(kids[i], req);
                        found = req.pickCount != 0;
                    }
                }
                break;
        }

        lastPathIndex--;
    }

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given ray. Return the first
     * found.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     */
    private void pickAllLineSegment(PickTarget root, PickRequest req)
    {
        if(!root.checkPickMask(req.pickType))
        {
            return;
        }

        PickTarget target_node = root;

        BoundingVolume bounds = root.getPickableBounds();

        if(bounds == null || !bounds.checkIntersectionSegment(req.origin, req.destination))
        {
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
            {
                target_node = (PickTarget) geom;
            }
            else
            {
                return;
            }
        }

        start[0] = req.origin[0];
        start[1] = req.origin[1];
        start[2] = req.origin[2];
        start[3] = 1;

        end[0] = req.destination[0];
        end[1] = req.destination[1];
        end[2] = req.destination[2];
        end[3] = 1;

        // reset the transform at the top of the stack
        if(target_node instanceof TransformPickTarget)
        {
            TransformPickTarget tg = (TransformPickTarget)target_node;
            tg.getTransform(transformPath[lastPathIndex]);
            tg.getInverseTransform(invertedMatrix);
            transform(invertedMatrix, start);
            transform(invertedMatrix, end);

            validTransform[lastPathIndex] = true;
        }
        else
        {
            transformPath[lastPathIndex].setIdentity();
            validTransform[lastPathIndex] = false;
        }

        pickPath[lastPathIndex] = target_node;
        lastPathIndex++;

        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    PickTarget[] kids = g.getPickableChildren();

                    for(int i = 0; i < num_kids; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickAllLineSegment(kids[i], req);
                    }
                }
                break;

            case PickTarget.LEAF_PICK_TYPE:
                updatePathAfterSuccess((LeafPickTarget) root, req);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                target_node = ((SinglePickTarget)target_node).getPickableChild();

                if(target_node != null)
                {
                    pickAllLineSegment(target_node, req);
                }
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                if(pickCustom((CustomPickTarget)target_node, req))
                {
                    // reset the transform at the top of the stack based on our local
                    if(pickInstructions.hasTransform)
                    {
                        transformPath[lastPathIndex - 1].set(pickInstructions.localTransform);

                        matrixUtils.inverse(transformPath[lastPathIndex - 1], invertedMatrix);
                        transform(invertedMatrix, start);
                    }

                    // Make sure to clone the array locally because if we are recursing the global
                    // list will be overwritten each time we go down a level
                    num_kids = pickInstructions.numChildren;
                    PickTarget[] kids = pickInstructions.children.clone();

                    for(int i = 0; i < num_kids; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickAllLineSegment(kids[i], req);
                    }
                }
                break;
        }

        lastPathIndex--;
    }

    // ----------------------- Ray Picking ------------------------------

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given ray. Return the first
     * found.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     */
    private void pickRay(PickTarget root, PickRequest req)
    {
        switch(req.pickSortType)
        {
            case PickRequest.SORT_ALL:
            case PickRequest.SORT_ORDERED:
                pickAllRay(root, req);
                break;

            case PickRequest.SORT_ANY:
            case PickRequest.SORT_CLOSEST:
                pickSingleRay(root, req);
                break;
        }
    }

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given ray. Return the first
     * found.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     */
    private void pickSingleRay(PickTarget root, PickRequest req)
    {
        if(!root.checkPickMask(req.pickType))
        {
            return;
        }

        PickTarget target_node = root;

        BoundingVolume bounds = root.getPickableBounds();

        if(bounds == null || !bounds.checkIntersectionRay(req.origin, req.destination))
        {
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
            {
                target_node = (PickTarget) geom;
            }
            else
            {
                return;
            }
        }

        start[0] = req.origin[0];
        start[1] = req.origin[1];
        start[2] = req.origin[2];
        start[3] = 1;

        end[0] = req.destination[0];
        end[1] = req.destination[1];
        end[2] = req.destination[2];
        end[3] = 0;

        // reset the transform at the top of the stack
        if(target_node instanceof TransformPickTarget)
        {
            TransformPickTarget tg = (TransformPickTarget)target_node;
            tg.getTransform(transformPath[lastPathIndex]);
            tg.getInverseTransform(invertedMatrix);
            transform(invertedMatrix, start);
            transformNormal(invertedMatrix, end);

            validTransform[lastPathIndex] = true;
        }
        else
        {
            transformPath[lastPathIndex].setIdentity();
            validTransform[lastPathIndex] = false;
        }

        pickPath[lastPathIndex] = target_node;
        lastPathIndex++;

        boolean found = false;
        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    PickTarget[] kids = g.getPickableChildren();

                    for(int i = 0; i < num_kids && !found; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickSingleRay(kids[i], req);
                        found = req.pickCount != 0;
                    }
                }
                break;

            case PickTarget.LEAF_PICK_TYPE:
                updatePathAfterSuccess((LeafPickTarget) root, req);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                target_node = ((SinglePickTarget)target_node).getPickableChild();

                if(target_node != null)
                {
                    pickSingleRay(target_node, req);
                }
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                if(pickCustom((CustomPickTarget)target_node, req))
                {
                    // reset the transform at the top of the stack based on our local
                    if(pickInstructions.hasTransform)
                    {
                        transformPath[lastPathIndex - 1].set(pickInstructions.localTransform);

                        matrixUtils.inverse(transformPath[lastPathIndex - 1], invertedMatrix);
                        transform(invertedMatrix, start);
                    }

                    // Make sure to clone the array locally because if we are recursing the global
                    // list will be overwritten each time we go down a level
                    num_kids = pickInstructions.numChildren;
                    PickTarget[] kids = pickInstructions.children.clone();

                    for(int i = 0; i < num_kids && !found; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickSingleRay(kids[i], req);
                        found = req.pickCount != 0;
                    }
                }
                break;
        }

        lastPathIndex--;
    }

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given ray. Return the first
     * found.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     */
    private void pickAllRay(PickTarget root, PickRequest req)
    {
        if(!root.checkPickMask(req.pickType))
        {
            return;
        }

        PickTarget target_node = root;

        BoundingVolume bounds = root.getPickableBounds();

        if(bounds == null || !bounds.checkIntersectionRay(req.origin, req.destination))
        {
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
            {
                target_node = (PickTarget) geom;
            }
            else
            {
                return;
            }
        }

        start[0] = req.origin[0];
        start[1] = req.origin[1];
        start[2] = req.origin[2];
        start[3] = 1;

        end[0] = req.destination[0];
        end[1] = req.destination[1];
        end[2] = req.destination[2];
        end[3] = 0;

        // reset the transform at the top of the stack
        if(target_node instanceof TransformPickTarget)
        {
            TransformPickTarget tg = (TransformPickTarget)target_node;
            tg.getTransform(transformPath[lastPathIndex]);
            tg.getInverseTransform(invertedMatrix);
            transform(invertedMatrix, start);
            transformNormal(invertedMatrix, end);

            validTransform[lastPathIndex] = true;
        }
        else
        {
            transformPath[lastPathIndex].setIdentity();
            validTransform[lastPathIndex] = false;
        }

        pickPath[lastPathIndex] = target_node;
        lastPathIndex++;

        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    PickTarget[] kids = g.getPickableChildren();

                    for(int i = 0; i < num_kids; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickAllRay(kids[i], req);
                    }
                }
                break;

            case PickTarget.LEAF_PICK_TYPE:
                updatePathAfterSuccess((LeafPickTarget) root, req);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                target_node = ((SinglePickTarget)target_node).getPickableChild();

                if(target_node != null)
                {
                    pickAllRay(target_node, req);
                }
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                if(pickCustom((CustomPickTarget)target_node, req))
                {
                    // reset the transform at the top of the stack based on our local
                    if(pickInstructions.hasTransform)
                    {
                        transformPath[lastPathIndex - 1].set(pickInstructions.localTransform);

                        matrixUtils.inverse(transformPath[lastPathIndex - 1], invertedMatrix);
                        transform(invertedMatrix, start);
                    }

                    // Make sure to clone the array locally because if we are recursing the global
                    // list will be overwritten each time we go down a level
                    num_kids = pickInstructions.numChildren;
                    PickTarget[] kids = pickInstructions.children.clone();

                    for(int i = 0; i < num_kids; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickAllRay(kids[i], req);
                    }
                }
                break;
        }

        lastPathIndex--;
    }

    // ----------------------- Cylinder Picking ------------------------------

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given ray. Return the first
     * found.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     */
    private void pickCylinder(PickTarget root, PickRequest req)
    {
        switch(req.pickSortType)
        {
            case PickRequest.SORT_ALL:
            case PickRequest.SORT_ORDERED:
                pickAllCylinder(root, req);
                break;

            case PickRequest.SORT_ANY:
            case PickRequest.SORT_CLOSEST:
                pickSingleCylinder(root, req);
                break;
        }
    }

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given ray. Return the first
     * found.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     */
    private void pickSingleCylinder(PickTarget root, PickRequest req)
    {
        if(!root.checkPickMask(req.pickType))
        {
            return;
        }

        PickTarget target_node = root;

        BoundingVolume bounds = root.getPickableBounds();

        float x = req.origin[0] - req.destination[0];
        float y = req.origin[1] - req.destination[1];
        float z = req.origin[2] - req.destination[2];

        float height = (float)Math.sqrt(x * x + y * y + z * z);

        if(height == 0)
        {
            return;
        }

        // Start will be the center and end will be the axis vector
        start[0] = (req.origin[0] + req.destination[0]) * 0.5f;
        start[1] = (req.origin[1] + req.destination[0]) * 0.5f;
        start[2] = (req.origin[2] + req.destination[0]) * 0.5f;
        start[3] = 1;

        end[0] = x;
        end[1] = y;
        end[2] = z;
        end[3] = 1;

        float radius = req.additionalData;
        if(bounds == null || !bounds.checkIntersectionCylinder(start, end, radius, height))
        {
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
            {
                target_node = (PickTarget) geom;
            }
            else
            {
                return;
            }
        }

        // reset the transform at the top of the stack
        if(target_node instanceof TransformPickTarget)
        {
            TransformPickTarget tg = (TransformPickTarget)target_node;
            tg.getTransform(transformPath[lastPathIndex]);
            tg.getInverseTransform(invertedMatrix);
            transform(invertedMatrix, start);
            transformNormal(invertedMatrix, end);

            // need to scale the radius and height as well.
            float scale = (float)matrixUtils.getUniformScale(invertedMatrix);
            radius *= scale;
            height *= scale;

            validTransform[lastPathIndex] = true;
        }
        else
        {
            transformPath[lastPathIndex].setIdentity();
            validTransform[lastPathIndex] = false;
        }

        pickPath[lastPathIndex] = target_node;
        lastPathIndex++;

        boolean found = false;
        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    PickTarget[] kids = g.getPickableChildren();

                    for(int i = 0; i < num_kids && !found; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickSingleCylinder(kids[i], req);
                        found = req.pickCount != 0;
                    }
                }
                break;

            case PickTarget.LEAF_PICK_TYPE:
                updatePathAfterSuccess((LeafPickTarget) root, req);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                target_node = ((SinglePickTarget)target_node).getPickableChild();

                if(target_node != null)
                {
                    pickSingleCylinder(target_node, req);
                }
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                if(pickCustom((CustomPickTarget)target_node, req))
                {
                    // reset the transform at the top of the stack based on our local
                    if(pickInstructions.hasTransform)
                    {
                        transformPath[lastPathIndex - 1].set(pickInstructions.localTransform);

                        matrixUtils.inverse(transformPath[lastPathIndex - 1], invertedMatrix);
                        transform(invertedMatrix, start);
                    }

                    // Make sure to clone the array locally because if we are recursing the global
                    // list will be overwritten each time we go down a level
                    num_kids = pickInstructions.numChildren;
                    PickTarget[] kids = pickInstructions.children.clone();

                    for(int i = 0; i < num_kids && !found; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickSingleCylinder(kids[i], req);
                        found = req.pickCount != 0;
                    }
                }
                break;
        }

        lastPathIndex--;
    }

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given ray. Return the first
     * found.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     */
    private void pickAllCylinder(PickTarget root, PickRequest req)
    {
        if(!root.checkPickMask(req.pickType))
        {
            return;
        }

        PickTarget target_node = root;

        BoundingVolume bounds = root.getPickableBounds();

        float x = req.origin[0] - req.destination[0];
        float y = req.origin[1] - req.destination[1];
        float z = req.origin[2] - req.destination[2];

        float height = (float)Math.sqrt(x * x + y * y + z * z);

        if(height == 0)
        {
            return;
        }

        // Start will be the center and end will be the axis vector
        start[0] = (req.origin[0] + req.destination[0]) * 0.5f;
        start[1] = (req.origin[1] + req.destination[0]) * 0.5f;
        start[2] = (req.origin[2] + req.destination[0]) * 0.5f;
        start[3] = 1;

        end[0] = x;
        end[1] = y;
        end[2] = z;
        end[3] = 1;

        float radius = req.additionalData;

        if(bounds == null || !bounds.checkIntersectionCylinder(start, end, radius, height))
        {
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
            {
                target_node = (PickTarget) geom;
            }
            else
            {
                return;
            }
        }

        // reset the transform at the top of the stack
        if(target_node instanceof TransformPickTarget)
        {
            TransformPickTarget tg = (TransformPickTarget)target_node;
            tg.getTransform(transformPath[lastPathIndex]);
            tg.getInverseTransform(invertedMatrix);
            transform(invertedMatrix, start);
            transformNormal(invertedMatrix, end);

            // need to scale the radius and height as well.
            float scale = (float)matrixUtils.getUniformScale(invertedMatrix);
            radius *= scale;
            height *= scale;

            validTransform[lastPathIndex] = true;
        }
        else
        {
            transformPath[lastPathIndex].setIdentity();
            validTransform[lastPathIndex] = false;
        }

        pickPath[lastPathIndex] = target_node;
        lastPathIndex++;

        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    PickTarget[] kids = g.getPickableChildren();

                    for(int i = 0; i < num_kids; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickAllCylinder(kids[i], req);
                    }
                }
                break;

            case PickTarget.LEAF_PICK_TYPE:
                updatePathAfterSuccess((LeafPickTarget) root, req);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                target_node = ((SinglePickTarget)target_node).getPickableChild();

                if(target_node != null)
                {
                    pickAllCylinder(target_node, req);
                }
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                if(pickCustom((CustomPickTarget)target_node, req))
                {
                    // reset the transform at the top of the stack based on our local
                    if(pickInstructions.hasTransform)
                    {
                        transformPath[lastPathIndex - 1].set(pickInstructions.localTransform);

                        matrixUtils.inverse(transformPath[lastPathIndex - 1], invertedMatrix);
                        transform(invertedMatrix, start);
                    }

                    // Make sure to clone the array locally because if we are recursing the global
                    // list will be overwritten each time we go down a level
                    num_kids = pickInstructions.numChildren;
                    PickTarget[] kids = pickInstructions.children.clone();

                    for(int i = 0; i < num_kids; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickAllCylinder(kids[i], req);
                    }
                }
                break;
        }

        lastPathIndex--;
    }

    // ----------------------- Cone Picking ------------------------------

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given ray. Return the first
     * found.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     */
    private void pickCone(PickTarget root, PickRequest req)
    {
        switch(req.pickSortType)
        {
            case PickRequest.SORT_ALL:
            case PickRequest.SORT_ORDERED:
                pickAllCone(root, req);
                break;

            case PickRequest.SORT_ANY:
            case PickRequest.SORT_CLOSEST:
                pickSingleCone(root, req);
                break;
        }
    }

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given ray. Return the first
     * found.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     */
    private void pickSingleCone(PickTarget root, PickRequest req)
    {
        if(!root.checkPickMask(req.pickType))
        {
            return;
        }

        PickTarget target_node = root;

        BoundingVolume bounds = root.getPickableBounds();

        if(bounds == null || !bounds.checkIntersectionCone(req.origin, req.destination, req.additionalData))
        {
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
            {
                target_node = (PickTarget) geom;
            }
            else
            {
                return;
            }
        }

        start[0] = req.origin[0];
        start[1] = req.origin[1];
        start[2] = req.origin[2];
        start[3] = 1;

        end[0] = req.destination[0];
        end[1] = req.destination[1];
        end[2] = req.destination[2];
        end[3] = 0;

        // reset the transform at the top of the stack
        if(target_node instanceof TransformPickTarget)
        {
            TransformPickTarget tg = (TransformPickTarget)target_node;
            tg.getTransform(transformPath[lastPathIndex]);
            tg.getInverseTransform(invertedMatrix);
            transform(invertedMatrix, start);
            transformNormal(invertedMatrix, end);

            validTransform[lastPathIndex] = true;
        }
        else
        {
            transformPath[lastPathIndex].setIdentity();
            validTransform[lastPathIndex] = false;
        }

        pickPath[lastPathIndex] = target_node;
        lastPathIndex++;

        boolean found = false;
        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    PickTarget[] kids = g.getPickableChildren();

                    for(int i = 0; i < num_kids && !found; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickSingleCone(kids[i], req);
                        found = req.pickCount != 0;
                    }
                }
                break;

            case PickTarget.LEAF_PICK_TYPE:
                updatePathAfterSuccess((LeafPickTarget) root, req);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                target_node = ((SinglePickTarget)target_node).getPickableChild();

                if(target_node != null)
                {
                    pickSingleCone(target_node, req);
                }
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                if(pickCustom((CustomPickTarget)target_node, req))
                {
                    // reset the transform at the top of the stack based on our local
                    if(pickInstructions.hasTransform)
                    {
                        transformPath[lastPathIndex - 1].set(pickInstructions.localTransform);

                        matrixUtils.inverse(transformPath[lastPathIndex - 1], invertedMatrix);
                        transform(invertedMatrix, start);
                    }

                    // Make sure to clone the array locally because if we are recursing the global
                    // list will be overwritten each time we go down a level
                    num_kids = pickInstructions.numChildren;
                    PickTarget[] kids = pickInstructions.children.clone();

                    for(int i = 0; i < num_kids && !found; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickSingleCone(kids[i], req);
                        found = req.pickCount != 0;
                    }
                }
                break;
        }

        lastPathIndex--;
    }

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given ray. Return the first
     * found.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     */
    private void pickAllCone(PickTarget root, PickRequest req)
    {
        if(!root.checkPickMask(req.pickType))
        {
            return;
        }

        PickTarget target_node = root;

        BoundingVolume bounds = root.getPickableBounds();

        if(bounds == null || !bounds.checkIntersectionCone(req.origin, req.destination, req.additionalData))
        {
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
            {
                target_node = (PickTarget) geom;
            }
            else
            {
                return;
            }
        }

        start[0] = req.origin[0];
        start[1] = req.origin[1];
        start[2] = req.origin[2];
        start[3] = 1;

        end[0] = req.destination[0];
        end[1] = req.destination[1];
        end[2] = req.destination[2];
        end[3] = 0;

        // reset the transform at the top of the stack
        if(target_node instanceof TransformPickTarget)
        {
            TransformPickTarget tg = (TransformPickTarget)target_node;
            tg.getTransform(transformPath[lastPathIndex]);
            tg.getInverseTransform(invertedMatrix);
            transform(invertedMatrix, start);
            transformNormal(invertedMatrix, end);

            validTransform[lastPathIndex] = true;
        }
        else
        {
            transformPath[lastPathIndex].setIdentity();
            validTransform[lastPathIndex] = false;
        }

        pickPath[lastPathIndex] = target_node;
        lastPathIndex++;

        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    PickTarget[] kids = g.getPickableChildren();

                    for(int i = 0; i < num_kids; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickAllCone(kids[i], req);
                    }
                }
                break;

            case PickTarget.LEAF_PICK_TYPE:
                updatePathAfterSuccess((LeafPickTarget) root, req);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                target_node = ((SinglePickTarget)target_node).getPickableChild();

                if(target_node != null)
                {
                    pickAllCone(target_node, req);
                }
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                if(pickCustom((CustomPickTarget)target_node, req))
                {
                    // reset the transform at the top of the stack based on our local
                    if(pickInstructions.hasTransform)
                    {
                        transformPath[lastPathIndex - 1].set(pickInstructions.localTransform);

                        matrixUtils.inverse(transformPath[lastPathIndex - 1], invertedMatrix);
                        transform(invertedMatrix, start);
                    }

                    // Make sure to clone the array locally because if we are recursing the global
                    // list will be overwritten each time we go down a level
                    num_kids = pickInstructions.numChildren;
                    PickTarget[] kids = pickInstructions.children.clone();

                    for(int i = 0; i < num_kids; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickAllCone(kids[i], req);
                    }
                }
                break;
        }

        lastPathIndex--;
    }

    // ----------------------- Box Picking -------------------------------

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given ray. Return the first
     * found.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     */
    private void pickBox(PickTarget root, PickRequest req)
    {
        switch(req.pickSortType)
        {
            case PickRequest.SORT_ALL:
            case PickRequest.SORT_ORDERED:
                pickAllBox(root, req);
                break;

            case PickRequest.SORT_ANY:
            case PickRequest.SORT_CLOSEST:
                pickSingleBox(root, req);
                break;
        }
    }

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given ray. Return the first
     * found.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     */
    private void pickSingleBox(PickTarget root, PickRequest req)
    {
        if(!root.checkPickMask(req.pickType))
        {
            return;
        }

        PickTarget target_node = root;

        BoundingVolume bounds = root.getPickableBounds();

        if(bounds == null || !bounds.checkIntersectionBox(req.origin, req.destination))
        {
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
            {
                target_node = (PickTarget) geom;
            }
            else
            {
                return;
            }
        }

        start[0] = req.origin[0];
        start[1] = req.origin[1];
        start[2] = req.origin[2];
        start[3] = 1;

        end[0] = req.destination[0];
        end[1] = req.destination[1];
        end[2] = req.destination[2];
        end[3] = 0;

        // reset the transform at the top of the stack
        if(target_node instanceof TransformPickTarget)
        {
            TransformPickTarget tg = (TransformPickTarget)target_node;
            tg.getTransform(transformPath[lastPathIndex]);
            tg.getInverseTransform(invertedMatrix);
            transform(invertedMatrix, start);
            transform(invertedMatrix, end);
            fixExtents(start, end);

            validTransform[lastPathIndex] = true;
        }
        else
        {
            transformPath[lastPathIndex].setIdentity();
            validTransform[lastPathIndex] = false;
        }

        pickPath[lastPathIndex] = target_node;
        lastPathIndex++;

        boolean found = false;
        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    PickTarget[] kids = g.getPickableChildren();

                    for(int i = 0; i < num_kids && !found; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickSingleBox(kids[i], req);
                        found = req.pickCount != 0;
                    }
                }
                break;

            case PickTarget.LEAF_PICK_TYPE:
                updatePathAfterSuccess((LeafPickTarget) root, req);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                target_node = ((SinglePickTarget)target_node).getPickableChild();

                if(target_node != null)
                {
                    pickSingleBox(target_node, req);
                }
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                if(pickCustom((CustomPickTarget)target_node, req))
                {
                    // reset the transform at the top of the stack based on our local
                    if(pickInstructions.hasTransform)
                    {
                        transformPath[lastPathIndex - 1].set(pickInstructions.localTransform);

                        matrixUtils.inverse(transformPath[lastPathIndex - 1], invertedMatrix);
                        transform(invertedMatrix, start);
                        transform(invertedMatrix, end);
                        fixExtents(start, end);
                    }

                    // Make sure to clone the array locally because if we are recursing the global
                    // list will be overwritten each time we go down a level
                    num_kids = pickInstructions.numChildren;
                    PickTarget[] kids = pickInstructions.children.clone();

                    for(int i = 0; i < num_kids && !found; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickSingleBox(kids[i], req);
                        found = req.pickCount != 0;
                    }
                }
                break;
        }

        lastPathIndex--;
    }

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given ray. Return the first
     * found.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     */
    private void pickAllBox(PickTarget root, PickRequest req)
    {
        if(!root.checkPickMask(req.pickType))
        {
            return;
        }

        PickTarget target_node = root;

        BoundingVolume bounds = root.getPickableBounds();

        if(bounds == null || !bounds.checkIntersectionBox(req.origin, req.destination))
        {
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
            {
                target_node = (PickTarget) geom;
            }
            else
            {
                return;
            }
        }

        start[0] = req.origin[0];
        start[1] = req.origin[1];
        start[2] = req.origin[2];
        start[3] = 1;

        end[0] = req.destination[0];
        end[1] = req.destination[1];
        end[2] = req.destination[2];
        end[3] = 0;

        // reset the transform at the top of the stack
        if(target_node instanceof TransformPickTarget)
        {
            TransformPickTarget tg = (TransformPickTarget)target_node;
            tg.getTransform(transformPath[lastPathIndex]);
            tg.getInverseTransform(invertedMatrix);
            transform(invertedMatrix, start);
            transformNormal(invertedMatrix, end);
            fixExtents(start, end);

            validTransform[lastPathIndex] = true;
        }
        else
        {
            transformPath[lastPathIndex].setIdentity();
            validTransform[lastPathIndex] = false;
        }

        pickPath[lastPathIndex] = target_node;
        lastPathIndex++;

        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    PickTarget[] kids = g.getPickableChildren();

                    for(int i = 0; i < num_kids; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickAllBox(kids[i], req);
                    }
                }
                break;

            case PickTarget.LEAF_PICK_TYPE:
                updatePathAfterSuccess((LeafPickTarget) root, req);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                target_node = ((SinglePickTarget)target_node).getPickableChild();

                if(target_node != null)
                {
                    pickAllBox(target_node, req);
                }
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                if(pickCustom((CustomPickTarget)target_node, req))
                {
                    // reset the transform at the top of the stack based on our local
                    if(pickInstructions.hasTransform)
                    {
                        transformPath[lastPathIndex - 1].set(pickInstructions.localTransform);

                        matrixUtils.inverse(transformPath[lastPathIndex - 1], invertedMatrix);
                        transform(invertedMatrix, start);
                        transform(invertedMatrix, end);
                        fixExtents(start, end);
                    }

                    // Make sure to clone the array locally because if we are recursing the global
                    // list will be overwritten each time we go down a level
                    num_kids = pickInstructions.numChildren;
                    PickTarget[] kids = pickInstructions.children.clone();

                    for(int i = 0; i < num_kids; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickAllBox(kids[i], req);
                    }
                }
                break;
        }

        lastPathIndex--;
    }

    // ----------------------- Frustum Picking ------------------------------

    // Implementation Notes:
    // Unlike the other forms of picking, it is more efficient for us to
    // provide the v-world to local transformation to the picked object rather
    // than the other way around. This saves on a lot of matrix-vector
    // multiplications. Because of this, the transformPath is used differently.
    // Instead of just holding the local transformation, what this is doing is
    // premultiplying the matrix for the world transform as we walk the stack.
    //
    // Also, the frustum planes are just referenced as the global variables
    // rather than using local parameter copies. Need to monitor if this causes
    // any multithreaded issues or not.

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given ray. Return the first
     * found.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     */
    private void pickFrustum(PickTarget root, PickRequest req)
    {
        frustumPlanes[0].x = req.origin[0];
        frustumPlanes[0].y = req.origin[1];
        frustumPlanes[0].z = req.origin[2];
        frustumPlanes[0].w = req.origin[3];

        frustumPlanes[1].x = req.origin[4];
        frustumPlanes[1].y = req.origin[5];
        frustumPlanes[1].z = req.origin[6];
        frustumPlanes[1].w = req.origin[7];

        frustumPlanes[2].x = req.origin[8];
        frustumPlanes[2].y = req.origin[9];
        frustumPlanes[2].z = req.origin[10];
        frustumPlanes[2].w = req.origin[11];

        frustumPlanes[3].x = req.origin[12];
        frustumPlanes[3].y = req.origin[13];
        frustumPlanes[3].z = req.origin[14];
        frustumPlanes[3].w = req.origin[15];

        frustumPlanes[4].x = req.origin[16];
        frustumPlanes[4].y = req.origin[17];
        frustumPlanes[4].z = req.origin[18];
        frustumPlanes[4].w = req.origin[19];

        frustumPlanes[5].x = req.origin[20];
        frustumPlanes[5].y = req.origin[21];
        frustumPlanes[5].z = req.origin[22];
        frustumPlanes[5].w = req.origin[23];

        switch(req.pickSortType)
        {
            case PickRequest.SORT_ALL:
            case PickRequest.SORT_ORDERED:
                pickAllFrustum(root, req);
                break;

            case PickRequest.SORT_ANY:
            case PickRequest.SORT_CLOSEST:
                pickSingleFrustum(root, req);
                break;
        }
    }

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given ray. Return the first
     * found.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     */
    private void pickSingleFrustum(PickTarget root, PickRequest req)
    {
        if(!root.checkPickMask(req.pickType))
        {
            return;
        }

        PickTarget target_node = root;

        BoundingVolume bounds = root.getPickableBounds();

        if(bounds == null ||
            bounds.checkIntersectionFrustum(frustumPlanes, transformPath[lastPathIndex-1]) != BoundingVolume.FRUSTUM_ALLOUT)
        {
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
            {
                target_node = (PickTarget) geom;
            }
            else
            {
                return;
            }
        }

        // reset the transform at the top of the stack
        if(target_node instanceof TransformPickTarget)
        {
            TransformPickTarget tg = (TransformPickTarget)target_node;
            tg.getTransform(transformPath[lastPathIndex]);
            tg.getInverseTransform(invertedMatrix);
            transform(invertedMatrix, start);
            transform(invertedMatrix, end);
            fixExtents(start, end);

            validTransform[lastPathIndex] = true;
        }
        else
        {
            transformPath[lastPathIndex].setIdentity();
            validTransform[lastPathIndex] = false;
        }

        pickPath[lastPathIndex] = target_node;
        lastPathIndex++;

        boolean found = false;
        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    PickTarget[] kids = g.getPickableChildren();

                    for(int i = 0; i < num_kids && !found; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickSingleFrustum(kids[i], req);
                        found = req.pickCount != 0;
                    }
                }
                break;

            case PickTarget.LEAF_PICK_TYPE:
                updatePathAfterSuccess((LeafPickTarget) root, req);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                target_node = ((SinglePickTarget)target_node).getPickableChild();

                if(target_node != null)
                {
                    pickSingleFrustum(target_node, req);
                }
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                if(pickCustom((CustomPickTarget)target_node, req))
                {
                    // reset the transform at the top of the stack based on our local
                    if(pickInstructions.hasTransform)
                    {
                        transformPath[lastPathIndex - 1].set(pickInstructions.localTransform);

                        matrixUtils.inverse(transformPath[lastPathIndex - 1], invertedMatrix);
                        transform(invertedMatrix, start);
                        transform(invertedMatrix, end);
                        fixExtents(start, end);
                    }

                    // Make sure to clone the array locally because if we are recursing the global
                    // list will be overwritten each time we go down a level
                    num_kids = pickInstructions.numChildren;
                    PickTarget[] kids = pickInstructions.children.clone();

                    for(int i = 0; i < num_kids && !found; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickSingleFrustum(kids[i], req);
                        found = req.pickCount != 0;
                    }
                }
                break;
        }

        lastPathIndex--;
    }

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given ray. Return the first
     * found.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     */
    private void pickAllFrustum(PickTarget root, PickRequest req)
    {
        if(!root.checkPickMask(req.pickType))
        {
            return;
        }

        PickTarget target_node = root;

        BoundingVolume bounds = root.getPickableBounds();

        if(bounds == null ||
           bounds.checkIntersectionFrustum(frustumPlanes, transformPath[lastPathIndex-1]) != BoundingVolume.FRUSTUM_ALLOUT)
        {
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
            {
                target_node = (PickTarget) geom;
            }
            else
            {
                return;
            }
        }


        // reset the transform at the top of the stack
        if(target_node instanceof TransformPickTarget)
        {
            TransformPickTarget tg = (TransformPickTarget)target_node;
            tg.getTransform(transformPath[lastPathIndex]);
            tg.getInverseTransform(invertedMatrix);
            transform(invertedMatrix, start);
            transformNormal(invertedMatrix, end);
            fixExtents(start, end);

            validTransform[lastPathIndex] = true;
        }
        else
        {
            transformPath[lastPathIndex].setIdentity();
            validTransform[lastPathIndex] = false;
        }

        pickPath[lastPathIndex] = target_node;
        lastPathIndex++;

        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    PickTarget[] kids = g.getPickableChildren();

                    for(int i = 0; i < num_kids; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickAllFrustum(kids[i], req);
                    }
                }
                break;

            case PickTarget.LEAF_PICK_TYPE:
                updatePathAfterSuccess((LeafPickTarget) root, req);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                target_node = ((SinglePickTarget)target_node).getPickableChild();

                if(target_node != null)
                {
                    pickAllFrustum(target_node, req);
                }
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                if(pickCustom((CustomPickTarget)target_node, req))
                {
                    // reset the transform at the top of the stack based on our local
                    if(pickInstructions.hasTransform)
                    {
                        transformPath[lastPathIndex - 1].set(pickInstructions.localTransform);

                        matrixUtils.inverse(transformPath[lastPathIndex - 1], invertedMatrix);
                        transform(invertedMatrix, start);
                        transform(invertedMatrix, end);
                        fixExtents(start, end);
                    }

                    // Make sure to clone the array locally because if we are recursing the global
                    // list will be overwritten each time we go down a level
                    num_kids = pickInstructions.numChildren;
                    PickTarget[] kids = pickInstructions.children.clone();

                    for(int i = 0; i < num_kids; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickAllFrustum(kids[i], req);
                    }
                }
                break;
        }

        lastPathIndex--;
    }

    // ----------------------- Sphere Picking -------------------------------

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given ray. Return the first
     * found.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     */
    private void pickSphere(PickTarget root, PickRequest req)
    {
        switch(req.pickSortType)
        {
            case PickRequest.SORT_ALL:
            case PickRequest.SORT_ORDERED:
                pickAllSphere(root, req);
                break;

            case PickRequest.SORT_ANY:
            case PickRequest.SORT_CLOSEST:
                pickSingleSphere(root, req);
                break;
        }
    }

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given ray. Return the first
     * found.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     */
    private void pickSingleSphere(PickTarget root, PickRequest req)
    {
        if(!root.checkPickMask(req.pickType))
        {
            return;
        }

        PickTarget target_node = root;

        BoundingVolume bounds = root.getPickableBounds();

        if(bounds == null || !bounds.checkIntersectionSphere(req.origin, req.additionalData))
        {
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
            {
                target_node = (PickTarget) geom;
            }
            else
            {
                return;
            }
        }

        start[0] = req.origin[0];
        start[1] = req.origin[1];
        start[2] = req.origin[2];
        start[3] = 1;

        float radius = req.additionalData;

        // reset the transform at the top of the stack
        if(target_node instanceof TransformPickTarget)
        {
            TransformPickTarget tg = (TransformPickTarget)target_node;
            tg.getTransform(transformPath[lastPathIndex]);
            tg.getInverseTransform(invertedMatrix);
            transform(invertedMatrix, start);

            float scale = (float)matrixUtils.getUniformScale(invertedMatrix);
            radius *= scale;

            validTransform[lastPathIndex] = true;
        }
        else
        {
            transformPath[lastPathIndex].setIdentity();
            validTransform[lastPathIndex] = false;
        }

        pickPath[lastPathIndex] = target_node;
        lastPathIndex++;

        boolean found = false;

        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    PickTarget[] kids = g.getPickableChildren();

                    for(int i = 0; i < num_kids && !found; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickSingleSphere(kids[i], req);
                        found = req.pickCount != 0;
                    }
                }
                break;

            case PickTarget.LEAF_PICK_TYPE:
                updatePathAfterSuccess((LeafPickTarget) root, req);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                target_node = ((SinglePickTarget)target_node).getPickableChild();

                if(target_node != null)
                {
                    pickSingleSphere(target_node, req);
                }
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                if(pickCustom((CustomPickTarget)target_node, req))
                {
                    // reset the transform at the top of the stack based on our local
                    if(pickInstructions.hasTransform)
                    {
                        transformPath[lastPathIndex - 1].set(pickInstructions.localTransform);

                        matrixUtils.inverse(transformPath[lastPathIndex - 1], invertedMatrix);
                        transform(invertedMatrix, start);

                        float scale = (float)matrixUtils.getUniformScale(invertedMatrix);
                        radius *= scale;
                    }

                    // Make sure to clone the array locally because if we are recursing the global
                    // list will be overwritten each time we go down a level
                    num_kids = pickInstructions.numChildren;
                    PickTarget[] kids = pickInstructions.children.clone();

                    for(int i = 0; i < num_kids && !found; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickSingleSphere(kids[i], req);
                        found = req.pickCount != 0;
                    }
                }
                break;
        }

        lastPathIndex--;
    }

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given ray. Return the first
     * found.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     */
    private void pickAllSphere(PickTarget root, PickRequest req)
    {
        if(!root.checkPickMask(req.pickType))
        {
            return;
        }

        PickTarget target_node = root;

        BoundingVolume bounds = root.getPickableBounds();

        if(bounds == null || !bounds.checkIntersectionSphere(req.origin, req.additionalData))
        {
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
            {
                target_node = (PickTarget) geom;
            }
            else
            {
                return;
            }
        }

        start[0] = req.origin[0];
        start[1] = req.origin[1];
        start[2] = req.origin[2];
        start[3] = 1;

        float radius = req.additionalData;

        // reset the transform at the top of the stack
        resizePath();
        if(target_node instanceof TransformPickTarget)
        {
            TransformPickTarget tg = (TransformPickTarget)target_node;
            tg.getTransform(transformPath[lastPathIndex]);
            tg.getInverseTransform(invertedMatrix);
            transform(invertedMatrix, start);

            float scale = (float)matrixUtils.getUniformScale(invertedMatrix);
            radius *= scale;

            validTransform[lastPathIndex] = true;
        }
        else
        {
            transformPath[lastPathIndex].setIdentity();
            validTransform[lastPathIndex] = false;
        }

        pickPath[lastPathIndex] = target_node;
        lastPathIndex++;

        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    PickTarget[] kids = g.getPickableChildren();

                    for(int i = 0; i < num_kids; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickAllSphere(kids[i], req);
                    }
                }
                break;

            case PickTarget.LEAF_PICK_TYPE:
                updatePathAfterSuccess((LeafPickTarget) root, req);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                target_node = ((SinglePickTarget)target_node).getPickableChild();

                if(target_node != null)
                {
                    pickAllSphere(target_node, req);
                }
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                if(pickCustom((CustomPickTarget)target_node, req))
                {
                    // reset the transform at the top of the stack based on our local
                    if(pickInstructions.hasTransform)
                    {
                        transformPath[lastPathIndex - 1].set(pickInstructions.localTransform);

                        matrixUtils.inverse(transformPath[lastPathIndex - 1], invertedMatrix);
                        transform(invertedMatrix, start);

                        float scale = (float)matrixUtils.getUniformScale(invertedMatrix);
                        radius *= scale;
                    }

                    // Make sure to clone the array locally because if we are recursing the global
                    // list will be overwritten each time we go down a level
                    num_kids = pickInstructions.numChildren;
                    PickTarget[] kids = pickInstructions.children.clone();

                    for(int i = 0; i < num_kids; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        pickAllSphere(kids[i], req);
                    }
                }
                break;
        }

        lastPathIndex--;
    }

    /**
     * Process a custom pick. All this does is call the appropriate custom picking
     * method and then return. The values are left in the class variable
     * pickInstructions for the individual methods to process.
     *
     * @param node The target node to test against
     * @param request The current picking request
     * @return true if this target has valid children returned
     */
    private boolean pickCustom(CustomPickTarget node, PickRequest request)
    {
        if(!node.checkPickMask(request.pickType))
            return false;

        // Since we need the transform from the root to here, build it now.
        buildVWorldTransform();

        node.pickChildren(pickInstructions, vworldMatrix, request);

        return pickInstructions.numChildren != 0;
    }

    /**
     * Resize the list if needed. Marked as final in order to encourage the
     * compiler to inline the code for faster execution
     */
    private void resizePath()
    {
        if(lastPathIndex == pickPath.length)
        {
            int old_size = pickPath.length;
            int new_size = old_size + LIST_INCREMENT;

            PickTarget[] tmp_nodes = new PickTarget[new_size];

            System.arraycopy(pickPath, 0, tmp_nodes, 0, old_size);

            pickPath = tmp_nodes;

            Matrix4d[] tmp_tx = new Matrix4d[new_size];
            System.arraycopy(transformPath, 0, tmp_tx, 0, old_size);
            transformPath = tmp_tx;

            for(int i = old_size; i < new_size; i++)
                transformPath[i] = new Matrix4d();

            boolean[] tmp_flags = new boolean[new_size];
            System.arraycopy(validTransform, 0, tmp_flags, 0, old_size);
            validTransform = tmp_flags;
        }
    }

    /**
     * Transform the location vector by the matrix.
     *
     * @param mat The matrix to do the transformation with
     * @param vec The vector to be changed
     */
    private void transform(Matrix4d mat, float[] vec)
    {
        wkVec.set(vec[0], vec[1], vec[2], vec[3]);
        mat.transform(wkVec, wkVec);
        vec[0] = (float)wkVec.x;
        vec[1] = (float)wkVec.y;
        vec[2] = (float)wkVec.z;
        vec[3] = (float)wkVec.w;
    }

    /**
     * Transform the normal vector by the matrix.
     *
     * @param mat The matrix to do the transformation with
     * @param vec The vector to be changed
     */
    private void transformNormal(Matrix4d mat, float[] vec)
    {
        wkNormal.set(vec[0], vec[1], vec[2]);
        mat.transformNormal(wkNormal, wkNormal);
        vec[0] = (float)wkNormal.x;
        vec[1] = (float)wkNormal.y;
        vec[2] = (float)wkNormal.z;
    }

    /**
     * Walk the transform stack and compute the total transform to get from
     * the world root to here. The forward matrix is placed in vworldMatrix
     * and the inverse is placed in invertedMatrix. Both are class-scope
     * variables.
     */
    private void buildVWorldTransform()
    {
        vworldMatrix.set(transformPath[0]);

        for(int i = 1; i < lastPathIndex; i++)
        {
            if(!validTransform[i])
                continue;

            vworldMatrix.mul(vworldMatrix, transformPath[i]);
        }

        matrixUtils.inverse(vworldMatrix, invertedMatrix);
    }

    /**
     * Common method to update or replace the path information in the current
     * pick request after a pick has succeeded.
     *
     * @param req The request object to fill in with the global details
     */
    private void updatePathAfterSuccess(LeafPickTarget geom, PickRequest req)
    {
        resizePath();

        // Don't need these here because they will have been set before this method
        // was called. We just need to update the matrix and set the stack into the
        // path and we're done.
        //pickPath[l    astPathIndex] = geom;
        //validTransform[lastPathIndex] = false;

        if(req.generateVWorldMatrix)
        {
            buildVWorldTransform();
        }
        else
        {
            vworldMatrix.setIdentity();
            invertedMatrix.setIdentity();
        }

        if(req.foundPaths instanceof SceneGraphPath)
        {
            SceneGraphPath path = (SceneGraphPath) req.foundPaths;
            path.updatePath(pickPath,
                            lastPathIndex + 1,
                            vworldMatrix,
                            invertedMatrix);
        }
        else
        {
            SceneGraphPath path = new SceneGraphPath();
            path.updatePath(pickPath,
                            lastPathIndex + 1,
                            vworldMatrix,
                            invertedMatrix);

            ((Collection<SceneGraphPath>)req.foundPaths).add(path);
        }

        req.pickCount++;
    }

	/**
	 * Ensure that bounding box extents are properly ordered
	 * after they have been transformed
	 *
	 * @param minExtent The minimum bound
	 * @param maxExtent The maximum bound
	 */
	private void fixExtents(float[] minExtent, float[] maxExtent)
    {
		for (int i = 0; i < 3; i++)
        {
			if(minExtent[i] > maxExtent[i])
            {
				float tmp = minExtent[i];
				minExtent[i] = maxExtent[i];
				maxExtent[i] = tmp;
			}
		}
	}
}
