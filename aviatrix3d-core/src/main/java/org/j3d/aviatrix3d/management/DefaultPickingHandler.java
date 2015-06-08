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
        if(root == null || req == null)
            return;

        if(!root.checkPickMask(req.pickType))
        {
            req.pickCount = 0;
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
        SceneGraphPath output_path = null;

        if(req.foundPaths instanceof SceneGraphPath)
            output_path = (SceneGraphPath)req.foundPaths;
        else
        {
            output_path = new SceneGraphPath();
            req.foundPaths = output_path;
        }

        boolean found = false;

        PickTarget target_node = root;

        BoundingVolume bounds = root.getPickableBounds();

        if(bounds == null ||
           !bounds.checkIntersectionPoint(req.origin))
        {
            req.pickCount = 0;
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
                target_node = (PickTarget)geom;
            else
            {
                req.pickCount = 0;
                return;
            }
        }

        start[0] = req.origin[0];
        start[1] = req.origin[1];
        start[2] = req.origin[2];
        start[3] = 1;

        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    // reset the transform at the top of the stack
                    if(target_node instanceof TransformPickTarget)
                    {
                        TransformPickTarget tg = (TransformPickTarget)target_node;
                        tg.getTransform(transformPath[0]);
                        tg.getInverseTransform(invertedMatrix);
                        transform(invertedMatrix, start);
                    }
                    else
                        transformPath[0].setIdentity();

                    validTransform[0] = true;
                    pickPath[0] = g;
                    PickTarget[] kids = g.getPickableChildren();

                    found = pickSinglePointFromList(kids, num_kids, req, start, output_path);
                }
                break;

            case PickTarget.LEAF_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found = pickSinglePoint((LeafPickTarget)target_node,
                                        req,
                                        start,
                                        output_path,
                                        req.generateVWorldMatrix);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found = pickSinglePoint((SinglePickTarget)target_node,
                                        req,
                                        start,
                                        output_path,
                                        req.generateVWorldMatrix);
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                if(pickCustom((CustomPickTarget)target_node, req))
                {
                    num_kids = pickInstructions.numChildren;

                    // reset the transform at the top of the stack
                    if(pickInstructions.hasTransform)
                    {
                        transformPath[0].set(pickInstructions.localTransform);

                        matrixUtils.inverse(transformPath[0], invertedMatrix);
                        transform(invertedMatrix, start);
                    }
                    else
                        transformPath[0].setIdentity();

                    validTransform[0] = true;
                    pickPath[0] = target_node;

                    // Make sure to clone the array locally
                    PickTarget[] kids = pickInstructions.children.clone();

                    found = pickSinglePointFromList(kids, num_kids, req, start, output_path);
                }
                break;
        }

        req.pickCount = found ? 1 : 0;
    }

    /**
     * Pick from a list of kids a single response.
     *
     * @param targets The array holding target pick objects
     * @param numTargets The number of valid items in the targets array
     * @param req flags to compare against for picking
     * @param loc The location of the point to pick with in local coords
     * @param path The place to put the results in
     * @return true if an intersection was found
     */
    private boolean pickSinglePointFromList(PickTarget[] targets,
                                            int numTargets,
                                            PickRequest req,
                                            float[] loc,
                                            SceneGraphPath path)
    {
        boolean found = false;

        // now that the setup is done, walk down the tree.
        for(int i = 0; i < numTargets && !found; i++)
        {
            if(targets[i] == null)
                continue;

            lastPathIndex = 1;

            switch(targets[i].getPickTargetType())
            {
                case PickTarget.GROUP_PICK_TYPE:
                    found = pickSinglePoint((GroupPickTarget)targets[i],
                                            req,
                                            loc,
                                            path,
                                            req.generateVWorldMatrix);
                    break;

                case PickTarget.SINGLE_PICK_TYPE:
                    found = pickSinglePoint((SinglePickTarget)targets[i],
                                            req,
                                            loc,
                                            path,
                                            req.generateVWorldMatrix);
                    break;

                case PickTarget.LEAF_PICK_TYPE:
                    found = pickSinglePoint((LeafPickTarget)targets[i],
                                            req,
                                            loc,
                                            path,
                                            req.generateVWorldMatrix);
                    break;

                case PickTarget.CUSTOM_PICK_TYPE:
                    found = pickSinglePoint((CustomPickTarget)targets[i],
                                            req,
                                            loc,
                                            path,
                                            req.generateVWorldMatrix);
                    break;
            }
        }

        return found;
    }

    /**
     * Recurse a single-child node tree looking for
     * intersections with a single point. Shared nodes are different to normal
     * because we don't bother doing bounds tests as the bounds will be
     * identical to their child. Skip the test and just head straight to the
     * child node to test against.
     *
     * @param root The shared node to test against
     * @param req flags to compare against for picking
     * @param loc The location of the point to pick with in local coords
     * @param path The place to put the results in
     * @param needTransform true if we should calc vworld information
     */
    private boolean pickSinglePoint(SinglePickTarget root,
                                    PickRequest req,
                                    float[] loc,
                                    SceneGraphPath path,
                                    boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return false;

        resizePath();
        validTransform[lastPathIndex] = false;
        pickPath[lastPathIndex] = root;
        lastPathIndex++;

        PickTarget child = root.getPickableChild();
        if(child == null)
            return false;

        boolean found = false;

        switch(child.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                found = pickSinglePoint((GroupPickTarget)child,
                                        req,
                                        loc,
                                        path,
                                        needTransform);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                found = pickSinglePoint((SinglePickTarget)child,
                                        req,
                                        loc,
                                        path,
                                        needTransform);
                break;

            case PickTarget.LEAF_PICK_TYPE:
                found = pickSinglePoint((LeafPickTarget)child,
                                        req,
                                        loc,
                                        path,
                                        needTransform);
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                found = pickSinglePoint((CustomPickTarget)child,
                                        req,
                                        loc,
                                        path,
                                        needTransform);
        }

        lastPathIndex--;
        pickPath[lastPathIndex] = null;
        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param req flags to compare against for picking
     * @param path A place to set the results in
     * @param needTransform True if the local to v-world transform needs
     *    calculating
     */
    private boolean pickSinglePoint(GroupPickTarget root,
                                    PickRequest req,
                                    float[] loc,
                                    SceneGraphPath path,
                                    boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return false;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionPoint(loc))
            return false;

        // Bounding check. If it has it, recurse on the bounding geometry
        // rather than the real geometry.
        if(bounds instanceof BoundingGeometry)
        {
            if(pickSinglePointFromBoundsGeometry((BoundingGeometry)bounds,
                                                 req,
                                                 loc,
                                                 path,
                                                 needTransform))
            {
                return true;
            }
        }

        boolean found = false;
        int num_kids = root.numPickableChildren();

        if(num_kids != 0)
        {
            float old_x = loc[0];
            float old_y = loc[1];
            float old_z = loc[2];

            if(root instanceof TransformPickTarget)
            {
                Matrix4d tx = transformPath[lastPathIndex];

                TransformPickTarget tg = (TransformPickTarget)root;
                tg.getTransform(tx);
                tg.getInverseTransform(invertedMatrix);
                transform(invertedMatrix, loc);

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = root.getPickableChildren();

            for(int i = 0; i < num_kids && !found; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found = pickSinglePoint((GroupPickTarget)kids[i],
                                                req,
                                                loc,
                                                path,
                                                needTransform);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found = pickSinglePoint((SinglePickTarget)kids[i],
                                                req,
                                                loc,
                                                path,
                                                needTransform);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found = pickSinglePoint((LeafPickTarget)kids[i],
                                                req,
                                                loc,
                                                path,
                                                needTransform);
                        break;

                    case PickTarget.CUSTOM_PICK_TYPE:
                        found = pickSinglePoint((CustomPickTarget)kids[i],
                                                req,
                                                loc,
                                                path,
                                                needTransform);
                        break;
                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            loc[0] = old_x;
            loc[1] = old_y;
            loc[2] = old_z;
        }

        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point from a
     * custom pickable.
     *
     * @param req flags to compare against for picking
     * @param path A place to set the results in
     * @param needTransform True if the local to v-world transform needs
     *    calculating
     */
    private boolean pickSinglePoint(CustomPickTarget root,
                                    PickRequest req,
                                    float[] loc,
                                    SceneGraphPath path,
                                    boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return false;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionPoint(loc))
            return false;

        // Bounding check. If it has it, recurse on the bounding geometry
        // rather than the real geometry.
        if(bounds instanceof BoundingGeometry)
        {
            if(pickSinglePointFromBoundsGeometry((BoundingGeometry)bounds,
                                                 req,
                                                 loc,
                                                 path,
                                                 needTransform))
            {
                return true;
            }
        }

        boolean found = false;

        if(pickCustom(root, req))
        {
            int num_kids = pickInstructions.numChildren;

            float old_x = loc[0];
            float old_y = loc[1];
            float old_z = loc[2];

            // reset the transform at the top of the stack
            if(pickInstructions.hasTransform)
            {
                transformPath[lastPathIndex].set(pickInstructions.localTransform);

                matrixUtils.inverse(transformPath[lastPathIndex], invertedMatrix);
                transform(invertedMatrix, start);
                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;

            // Make sure to clone the array locally
            PickTarget[] kids = pickInstructions.children.clone();

            // now that the setup is done, walk down the tree.
            for(int i = 0; i < num_kids && !found; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found = pickSinglePoint((GroupPickTarget)kids[i],
                                                req,
                                                start,
                                                path,
                                                req.generateVWorldMatrix);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found = pickSinglePoint((SinglePickTarget)kids[i],
                                                req,
                                                start,
                                                path,
                                                req.generateVWorldMatrix);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found = pickSinglePoint((LeafPickTarget)kids[i],
                                                req,
                                                start,
                                                path,
                                                req.generateVWorldMatrix);
                        break;

                    case PickTarget.CUSTOM_PICK_TYPE:
                        found = pickSinglePoint((CustomPickTarget)kids[i],
                                                req,
                                                start,
                                                path,
                                                req.generateVWorldMatrix);
                        break;
                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            loc[0] = old_x;
            loc[1] = old_y;
            loc[2] = old_z;
        }

        return found;
    }

    /**
     * Convenience method to check if a bounding geometry single pick will
     * find an intersection.
     *
     * @param bounds The original bounds as geometry
     * @param req flags to compare against for picking
     * @param path A place to set the results in
     * @param needTransform True if the local to v-world transform needs
     *    calculating
     * @return
     */
    private boolean pickSinglePointFromBoundsGeometry(BoundingGeometry bounds,
                                                      PickRequest req,
                                                      float[] loc,
                                                      SceneGraphPath path,
                                                      boolean needTransform)
    {
        Node target_node = bounds.getProxyGeometry();

        if(target_node instanceof GroupPickTarget)
        {
            return pickSinglePoint((GroupPickTarget)target_node,
                                   req,
                                   loc,
                                   path,
                                   needTransform);
        }
        else if(target_node instanceof SinglePickTarget)
        {
            return pickSinglePoint((SinglePickTarget)target_node,
                                   req,
                                   loc,
                                   path,
                                   needTransform);
        }
        else if(target_node instanceof LeafPickTarget)
        {
            return pickSinglePoint((LeafPickTarget)target_node,
                                   req,
                                   loc,
                                   path,
                                   needTransform);
        }
        else if(target_node instanceof CustomPickTarget)
        {
            return pickSinglePoint((CustomPickTarget)target_node,
                                   req,
                                   loc,
                                   path,
                                   needTransform);
        }
        else if(target_node != null)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            Object[] msg_args = { target_node.getClass().getName() };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            String msg = msg_fmt.format(msg_args);
            errorReporter.warningReport(msg, null);
        }

        return false;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param req flags to compare against for picking
     * @param path A place to set the results in
     * @param needTransform True if the local to v-world transform needs
     *    calculating
     */
    private boolean pickSinglePoint(LeafPickTarget leaf,
                                    PickRequest req,
                                    float[] loc,
                                    SceneGraphPath path,
                                    boolean needTransform)
    {
        if(!leaf.checkPickMask(req.pickType))
            return false;

        boolean found = false;
        BoundingVolume bounds = leaf.getPickableBounds();

        if(bounds.checkIntersectionPoint(loc))
        {
            resizePath();
            pickPath[lastPathIndex] = leaf;
            validTransform[lastPathIndex] = false;
            lastPathIndex++;

            if(needTransform)
                buildVWorldTransform();
            else
                vworldMatrix.setIdentity();

            path.updatePath(pickPath,
                            lastPathIndex,
                            vworldMatrix,
                            invertedMatrix);

            found = true;
            lastPathIndex--;
        }

        return found;
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
        int found = 0;
        ArrayList<SceneGraphPath> output_list = null;

        if(req.foundPaths instanceof ArrayList)
        {
            output_list = (ArrayList<SceneGraphPath>)req.foundPaths;
            output_list.clear();
        }
        else
        {
            output_list = new ArrayList<>();
            req.foundPaths = output_list;
        }

        PickTarget target_node = root;
        BoundingVolume bounds = target_node.getPickableBounds();

        if(bounds == null || !bounds.checkIntersectionPoint(req.origin))
        {
            req.pickCount = 0;
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
                target_node = (PickTarget)geom;
            else
            {
                req.pickCount = 0;
                return;
            }
        }

        start[0] = req.origin[0];
        start[1] = req.origin[1];
        start[2] = req.origin[2];
        start[3] = 1;

        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    // reset the transform at the top of the stack
                    if(root instanceof TransformPickTarget)
                    {
                        Matrix4d tx = transformPath[0];

                        TransformPickTarget tg = (TransformPickTarget)root;
                        tg.getTransform(tx);
                        tg.getInverseTransform(invertedMatrix);
                        transform(invertedMatrix, start);
                    }
                    else
                        transformPath[0].setIdentity();

                    validTransform[0] = true;
                    pickPath[0] = g;
                    PickTarget[] kids = g.getPickableChildren();

                    // now that the setup is done, walk down the tree.
                    for(int i = 0; i < num_kids; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        lastPathIndex = 1;

                        switch(kids[i].getPickTargetType())
                        {
                            case PickTarget.GROUP_PICK_TYPE:
                                found += pickAllPoint((GroupPickTarget)kids[i],
                                                      req,
                                                      start,
                                                      output_list,
                                                      found,
                                                      req.generateVWorldMatrix);
                                break;

                            case PickTarget.SINGLE_PICK_TYPE:
                                found += pickAllPoint((SinglePickTarget)kids[i],
                                                      req,
                                                      start,
                                                      output_list,
                                                      found,
                                                      req.generateVWorldMatrix);
                                break;

                            case PickTarget.LEAF_PICK_TYPE:
                                found += pickAllPoint((LeafPickTarget)kids[i],
                                                      req,
                                                      start,
                                                      output_list,
                                                      found,
                                                      req.generateVWorldMatrix);
                                break;

                            case PickTarget.CUSTOM_PICK_TYPE:
                                found += pickAllPoint((CustomPickTarget)kids[i],
                                                      req,
                                                      start,
                                                      output_list,
                                                      found,
                                                      req.generateVWorldMatrix);

                                break;
                        }
                    }

                    req.pickCount = found;
                }
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found += pickAllPoint((SinglePickTarget)target_node,
                    req,
                    start,
                    output_list,
                    0,
                    req.generateVWorldMatrix);

                req.pickCount = found;
                break;

            case PickTarget.LEAF_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found += pickAllPoint((LeafPickTarget)target_node,
                                      req,
                                      start,
                                      output_list,
                                      0,
                                      req.generateVWorldMatrix);

                req.pickCount = found;
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                found += pickAllPoint((CustomPickTarget)target_node,
                                      req,
                                      start,
                                      output_list,
                                      0,
                                      req.generateVWorldMatrix);

                req.pickCount = found;
                break;
        }
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param req flags to compare against for picking
     */
    private int pickAllPoint(GroupPickTarget root,
                             PickRequest req,
                             float[] loc,
                             ArrayList<SceneGraphPath> paths,
                             int currentPath,
                             boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return 0;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionPoint(loc))
            return 0;


        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickAllPoint((GroupPickTarget)target_node,
                                    req,
                                    loc,
                                    paths,
                                    currentPath,
                                    needTransform);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickAllPoint((SinglePickTarget)target_node,
                                    req,
                                    loc,
                                    paths,
                                    currentPath,
                                    needTransform);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickAllPoint((LeafPickTarget)target_node,
                                    req,
                                    loc,
                                    paths,
                                    currentPath,
                                    needTransform);
            }
            else if(target_node instanceof CustomPickTarget)
            {
                return pickAllPoint((CustomPickTarget)target_node,
                                    req,
                                    loc,
                                    paths,
                                    currentPath,
                                    needTransform);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
            }
        }

        int found = 0;
        int num_kids = root.numPickableChildren();

        if(num_kids != 0)
        {
            float old_x = loc[0];
            float old_y = loc[1];
            float old_z = loc[2];

            // reset the transform at the top of the stack
            if(root instanceof TransformPickTarget)
            {
                Matrix4d tx = transformPath[lastPathIndex];

                TransformPickTarget tg = (TransformPickTarget)root;
                tg.getTransform(tx);
                tg.getInverseTransform(invertedMatrix);
                transform(invertedMatrix, loc);

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = root.getPickableChildren();

            for(int i = 0; i < num_kids; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found += pickAllPoint((GroupPickTarget)kids[i],
                                              req,
                                              loc,
                                              paths,
                                              currentPath + found,
                                              needTransform);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found += pickAllPoint((SinglePickTarget)kids[i],
                                              req,
                                              loc,
                                              paths,
                                              currentPath + found,
                                              needTransform);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found += pickAllPoint((LeafPickTarget)kids[i],
                                              req,
                                              loc,
                                              paths,
                                              currentPath + found,
                                              needTransform);
                        break;

                    case PickTarget.CUSTOM_PICK_TYPE:
                        found += pickAllPoint((CustomPickTarget)kids[i],
                                              req,
                                              loc,
                                              paths,
                                              currentPath + found,
                                              needTransform);
                        break;
                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            loc[0] = old_x;
            loc[1] = old_y;
            loc[2] = old_z;
        }
        return found;
    }

    /**
     * Recurse a single-child shared node tree looking for
     * intersections with a single point. Shared nodes are different to normal
     * because we don't bother doing bounds tests as the bounds will be
     * identical to their child. Skip the test and just head straight to the
     * child node to test against.
     *
     * @param root The shared node to test against
     * @param req flags to compare against for picking
     * @param loc The location of the point to pick with in local coords
     * @param paths The place to put the results in
     * @param needTransform true if we should calc vworld information
     */
    private int pickAllPoint(SinglePickTarget root,
                             PickRequest req,
                             float[] loc,
                             ArrayList<SceneGraphPath> paths,
                             int currentPath,
                             boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return 0;

        resizePath();
        validTransform[lastPathIndex] = false;
        pickPath[lastPathIndex] = root;
        lastPathIndex++;

        PickTarget child = root.getPickableChild();
        if(child == null)
            return 0;

        int found = 0;

        switch(child.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                found = pickAllPoint((GroupPickTarget)child,
                                     req,
                                     loc,
                                     paths,
                                     currentPath,
                                     needTransform);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                found = pickAllPoint((SinglePickTarget)child,
                                     req,
                                     loc,
                                     paths,
                                     currentPath,
                                     needTransform);
                break;

            case PickTarget.LEAF_PICK_TYPE:
                found = pickAllPoint((LeafPickTarget)child,
                                     req,
                                     loc,
                                     paths,
                                     currentPath,
                                     needTransform);
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                found = pickAllPoint((CustomPickTarget)child,
                                     req,
                                     loc,
                                     paths,
                                     currentPath,
                                     needTransform);
                break;
        }

        lastPathIndex--;
        pickPath[lastPathIndex] = null;
        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param req flags to compare against for picking
     */
    private int pickAllPoint(CustomPickTarget root,
                             PickRequest req,
                             float[] loc,
                             ArrayList<SceneGraphPath> paths,
                             int currentPath,
                             boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return 0;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionPoint(loc))
            return 0;


        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickAllPoint((GroupPickTarget)target_node,
                                    req,
                                    loc,
                                    paths,
                                    currentPath,
                                    needTransform);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickAllPoint((SinglePickTarget)target_node,
                                    req,
                                    loc,
                                    paths,
                                    currentPath,
                                    needTransform);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickAllPoint((LeafPickTarget)target_node,
                                    req,
                                    loc,
                                    paths,
                                    currentPath,
                                    needTransform);
            }
            else if(target_node instanceof CustomPickTarget)
            {
                return pickAllPoint((CustomPickTarget)target_node,
                                    req,
                                    loc,
                                    paths,
                                    currentPath,
                                    needTransform);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        int found = 0;
        if(pickCustom(root, req))
        {
            int num_kids = pickInstructions.numChildren;
            float old_x = loc[0];
            float old_y = loc[1];
            float old_z = loc[2];

            // reset the transform at the top of the stack
            if(pickInstructions.hasTransform)
            {
                transformPath[lastPathIndex].set(pickInstructions.localTransform);

                matrixUtils.inverse(pickInstructions.localTransform,
                                    invertedMatrix);
                transform(invertedMatrix, loc);

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = pickInstructions.children.clone();

            for(int i = 0; i < num_kids; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found += pickAllPoint((GroupPickTarget)kids[i],
                                              req,
                                              loc,
                                              paths,
                                              currentPath + found,
                                              needTransform);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found += pickAllPoint((SinglePickTarget)kids[i],
                                              req,
                                              loc,
                                              paths,
                                              currentPath + found,
                                              needTransform);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found += pickAllPoint((LeafPickTarget)kids[i],
                                              req,
                                              loc,
                                              paths,
                                              currentPath + found,
                                              needTransform);
                        break;

                    case PickTarget.CUSTOM_PICK_TYPE:
                        found += pickAllPoint((CustomPickTarget)kids[i],
                                              req,
                                              loc,
                                              paths,
                                              currentPath + found,
                                              needTransform);
                        break;
                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            loc[0] = old_x;
            loc[1] = old_y;
            loc[2] = old_z;
        }
        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param req flags to compare against for picking
     * @param paths A place to set the results in
     * @param needTransform True if the local to v-world transform needs
     *    calculating
     */
    private int pickAllPoint(LeafPickTarget geom,
                             PickRequest req,
                             float[] loc,
                             ArrayList<SceneGraphPath> paths,
                             int currentPath,
                             boolean needTransform)
    {
        if(!geom.checkPickMask(req.pickType))
            return 0;

        int found = 0;
        BoundingVolume bounds = geom.getPickableBounds();

        if(bounds.checkIntersectionPoint(loc))
        {
            resizePath();
            pickPath[lastPathIndex] = geom;
            validTransform[lastPathIndex] = false;
            lastPathIndex++;

            SceneGraphPath p;

            if(currentPath >= paths.size())
            {
                p = new SceneGraphPath();
                paths.add(p);
            }
            else
                p = paths.get(currentPath);

            if(needTransform)
                buildVWorldTransform();
            else
                vworldMatrix.setIdentity();


            p.updatePath(pickPath,
                         lastPathIndex,
                         vworldMatrix,
                         invertedMatrix);

            found = 1;
            lastPathIndex--;
        }

        return found;
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
                pickSingleLineSegment(root, req);
                break;

            case PickRequest.SORT_CLOSEST:
                pickSingleLineSegmentSorted(root, req);
                break;

            default:
				I18nManager intl_mgr = I18nManager.getManager();
				String msg_pattern = intl_mgr.getString(NO_SORT_TYPE_PROP);

				Locale lcl = intl_mgr.getFoundLocale();

				NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

				Object[] msg_args = { req.pickSortType };
				Format[] fmts = { n_fmt };
				MessageFormat msg_fmt =
					new MessageFormat(msg_pattern, lcl);
				msg_fmt.setFormats(fmts);
				String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
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
        SceneGraphPath output_path = null;

        if(req.foundPaths instanceof SceneGraphPath)
            output_path = (SceneGraphPath)req.foundPaths;
        else
        {
            output_path = new SceneGraphPath();
            req.foundPaths = output_path;
        }

        boolean found = false;

        PickTarget target_node = root;
        BoundingVolume bounds = target_node.getPickableBounds();

        if(bounds == null ||
           !bounds.checkIntersectionSegment(req.origin, req.destination))
        {
            req.pickCount = 0;
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
                target_node = (PickTarget)geom;
            else
            {
                req.pickCount = 0;
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

        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    // reset the transform at the top of the stack
                    if(root instanceof TransformPickTarget)
                    {
                        Matrix4d tx = transformPath[0];

                        TransformPickTarget tg = (TransformPickTarget)root;
                        tg.getTransform(tx);
                        tg.getInverseTransform(invertedMatrix);
                        transform(invertedMatrix, start);
                        transform(invertedMatrix, end);
                    }
                    else
                        transformPath[0].setIdentity();

                    validTransform[0] = true;
                    pickPath[0] = g;
                    PickTarget[] kids = g.getPickableChildren();

                    // now that the setup is done, walk down the tree.
                    for(int i = 0; i < num_kids && !found; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        lastPathIndex = 1;

                        switch(kids[i].getPickTargetType())
                        {
                            case PickTarget.GROUP_PICK_TYPE:
                                found = pickSingleLineSegment((GroupPickTarget)kids[i],
                                                              req,
                                                              start,
                                                              end,
                                                              output_path,
                                                              req.generateVWorldMatrix,
                                                              req.useGeometry);
                                break;

                            case PickTarget.SINGLE_PICK_TYPE:
                                found = pickSingleLineSegment((SinglePickTarget)kids[i],
                                                              req,
                                                              start,
                                                              end,
                                                              output_path,
                                                              req.generateVWorldMatrix,
                                                              req.useGeometry);
                                break;

                            case PickTarget.LEAF_PICK_TYPE:
                                found = pickSingleLineSegment((LeafPickTarget)kids[i],
                                                              req,
                                                              start,
                                                              end,
                                                              output_path,
                                                              req.generateVWorldMatrix,
                                                              req.useGeometry,
                                                              false);
                                break;

                            case PickTarget.CUSTOM_PICK_TYPE:
                                found = pickSingleLineSegment((CustomPickTarget)kids[i],
                                                              req,
                                                              start,
                                                              end,
                                                              output_path,
                                                              req.generateVWorldMatrix,
                                                              req.useGeometry);
                                break;
                        }
                    }
                }

                break;

            case PickTarget.SINGLE_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found = pickSingleLineSegment((SinglePickTarget)target_node,
                                              req,
                                              start,
                                              end,
                                              output_path,
                                              req.generateVWorldMatrix,
                                              req.useGeometry);
                break;

            case PickTarget.LEAF_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found = pickSingleLineSegment((LeafPickTarget)target_node,
                                              req,
                                              start,
                                              end,
                                              output_path,
                                              req.generateVWorldMatrix,
                                              req.useGeometry,
                                              false);
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                found = pickSingleLineSegment((CustomPickTarget)target_node,
                                              req,
                                              start,
                                              end,
                                              output_path,
                                              req.generateVWorldMatrix,
                                              req.useGeometry);
        }

        req.pickCount = found ? 1 : 0;
    }

    /**
     * Recurse the tree looking for intersections with a single line segment.
     *
     * @param root The now that is acting as the local root to work through
     * @param req flags to compare against for picking
     * @param p1 The first point of the segment
     * @param p2 The second point of the segment
     * @param path A place to set the results in
     * @param needTransform True if the local to v-world transform needs
     *    calculating
     * @param useGeom true if this should check the results based on geometry
     *    rather than just bounds
     */
    private boolean pickSingleLineSegment(GroupPickTarget root,
                                          PickRequest req,
                                          float[] p1,
                                          float[] p2,
                                          SceneGraphPath path,
                                          boolean needTransform,
                                          boolean useGeom)
    {
        if(!root.checkPickMask(req.pickType))
            return false;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionSegment(p1, p2))
            return false;

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickSingleLineSegment((GroupPickTarget)target_node,
                                             req,
                                             p1,
                                             p2,
                                             path,
                                             needTransform,
                                             useGeom);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickSingleLineSegment((SinglePickTarget)target_node,
                                             req,
                                             p1,
                                             p2,
                                             path,
                                             needTransform,
                                             useGeom);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickSingleLineSegment((LeafPickTarget)target_node,
                                             req,
                                             p1,
                                             p2,
                                             path,
                                             needTransform,
                                             useGeom,
                                             false);
            }
            else if(target_node instanceof CustomPickTarget)
            {
                return pickSingleLineSegment((CustomPickTarget)target_node,
                                             req,
                                             p1,
                                             p2,
                                             path,
                                             needTransform,
                                             useGeom);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        boolean found = false;
        int num_kids = root.numPickableChildren();

        if(num_kids != 0)
        {
            float p1_x = p1[0];
            float p1_y = p1[1];
            float p1_z = p1[2];

            float p2_x = p2[0];
            float p2_y = p2[1];
            float p2_z = p2[2];

            // reset the transform at the top of the stack
            if(root instanceof TransformPickTarget)
            {
                Matrix4d tx = transformPath[lastPathIndex];

                TransformPickTarget tg = (TransformPickTarget)root;
                tg.getTransform(tx);
                tg.getInverseTransform(invertedMatrix);
                transform(invertedMatrix, p1);
                transform(invertedMatrix, p2);

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = root.getPickableChildren();

            for(int i = 0; i < num_kids && !found; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found = pickSingleLineSegment((GroupPickTarget)kids[i],
                                                      req,
                                                      p1,
                                                      p2,
                                                      path,
                                                      needTransform,
                                                      useGeom);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found = pickSingleLineSegment((SinglePickTarget)kids[i],
                                                      req,
                                                      p1,
                                                      p2,
                                                      path,
                                                      needTransform,
                                                      useGeom);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found = pickSingleLineSegment((LeafPickTarget)kids[i],
                                                      req,
                                                      p1,
                                                      p2,
                                                      path,
                                                      needTransform,
                                                      useGeom,
                                                      false);
                        break;

                    case PickTarget.CUSTOM_PICK_TYPE:
                        found = pickSingleLineSegment((CustomPickTarget)kids[i],
                                                      req,
                                                      p1,
                                                      p2,
                                                      path,
                                                      needTransform,
                                                      useGeom);
                        break;
                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            p1[0] = p1_x;
            p1[1] = p1_y;
            p1[2] = p1_z;

            p2[0] = p2_x;
            p2[1] = p2_y;
            p2[2] = p2_z;
        }

        return found;
    }

    /**
     * Recurse a single-child shared node tree looking for
     * intersections with a single point. Shared nodes are different to normal
     * because we don't bother doing bounds tests as the bounds will be
     * identical to their child. Skip the test and just head straight to the
     * child node to test against.
     *
     * @param root The shared node to test against
     * @param req flags to compare against for picking
     * @param p1 The first point of the segment
     * @param p2 The second point of the segment
     * @param path The place to put the results in
     * @param needTransform true if we should calc vworld information
     * @param useGeom true if this should check the results based on geometry
     *    rather than just bounds
     */
    private boolean pickSingleLineSegment(SinglePickTarget root,
                                          PickRequest req,
                                          float[] p1,
                                          float[] p2,
                                          SceneGraphPath path,
                                          boolean needTransform,
                                          boolean useGeom)
    {
        if(!root.checkPickMask(req.pickType))
            return false;

        resizePath();
        validTransform[lastPathIndex] = false;
        pickPath[lastPathIndex] = root;
        lastPathIndex++;

        PickTarget child = root.getPickableChild();
        if(child == null)
            return false;

        boolean found = false;

        switch(child.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                found = pickSingleLineSegment((GroupPickTarget)child,
                                              req,
                                              p1,
                                              p2,
                                              path,
                                              needTransform,
                                              useGeom);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                found = pickSingleLineSegment((SinglePickTarget)child,
                                              req,
                                              p1,
                                              p2,
                                              path,
                                              needTransform,
                                              useGeom);
                break;

            case PickTarget.LEAF_PICK_TYPE:
                found = pickSingleLineSegment((LeafPickTarget)child,
                                              req,
                                              p1,
                                              p2,
                                              path,
                                              needTransform,
                                              useGeom,
                                              false);
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                found = pickSingleLineSegment((CustomPickTarget)child,
                                              req,
                                              p1,
                                              p2,
                                              path,
                                              needTransform,
                                              useGeom);
                break;
        }

        lastPathIndex--;
        pickPath[lastPathIndex] = null;
        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single line segment.
     *
     * @param root The now that is acting as the local root to work through
     * @param req flags to compare against for picking
     * @param p1 The first point of the segment
     * @param p2 The second point of the segment
     * @param path A place to set the results in
     * @param needTransform True if the local to v-world transform needs
     *    calculating
     * @param useGeom true if this should check the results based on geometry
     *    rather than just bounds
     */
    private boolean pickSingleLineSegment(CustomPickTarget root,
                                          PickRequest req,
                                          float[] p1,
                                          float[] p2,
                                          SceneGraphPath path,
                                          boolean needTransform,
                                          boolean useGeom)
    {
        if(!root.checkPickMask(req.pickType))
            return false;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionSegment(p1, p2))
            return false;

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickSingleLineSegment((GroupPickTarget)target_node,
                                             req,
                                             p1,
                                             p2,
                                             path,
                                             needTransform,
                                             useGeom);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickSingleLineSegment((SinglePickTarget)target_node,
                                             req,
                                             p1,
                                             p2,
                                             path,
                                             needTransform,
                                             useGeom);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickSingleLineSegment((LeafPickTarget)target_node,
                                             req,
                                             p1,
                                             p2,
                                             path,
                                             needTransform,
                                             useGeom,
                                             false);
            }
            else if(target_node instanceof CustomPickTarget)
            {
                return pickSingleLineSegment((CustomPickTarget)target_node,
                                             req,
                                             p1,
                                             p2,
                                             path,
                                             needTransform,
                                             useGeom);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        boolean found = false;
        if(pickCustom(root, req))
        {
            int num_kids = pickInstructions.numChildren;
            float p1_x = p1[0];
            float p1_y = p1[1];
            float p1_z = p1[2];

            float p2_x = p2[0];
            float p2_y = p2[1];
            float p2_z = p2[2];

            // reset the transform at the top of the stack
            if(pickInstructions.hasTransform)
            {
                transformPath[lastPathIndex].set(pickInstructions.localTransform);

                matrixUtils.inverse(pickInstructions.localTransform,
                                    invertedMatrix);
                transform(invertedMatrix, p1);
                transform(invertedMatrix, p2);

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = pickInstructions.children.clone();

            for(int i = 0; i < num_kids && !found; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found = pickSingleLineSegment((GroupPickTarget)kids[i],
                                                      req,
                                                      p1,
                                                      p2,
                                                      path,
                                                      needTransform,
                                                      useGeom);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found = pickSingleLineSegment((SinglePickTarget)kids[i],
                                                      req,
                                                      p1,
                                                      p2,
                                                      path,
                                                      needTransform,
                                                      useGeom);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found = pickSingleLineSegment((LeafPickTarget)kids[i],
                                                      req,
                                                      p1,
                                                      p2,
                                                      path,
                                                      needTransform,
                                                      useGeom,
                                                      false);
                        break;

                    case PickTarget.CUSTOM_PICK_TYPE:
                        found = pickSingleLineSegment((CustomPickTarget)kids[i],
                                                      req,
                                                      p1,
                                                      p2,
                                                      path,
                                                      needTransform,
                                                      useGeom);
                        break;
                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            p1[0] = p1_x;
            p1[1] = p1_y;
            p1[2] = p1_z;

            p2[0] = p2_x;
            p2[1] = p2_y;
            p2[2] = p2_z;
        }

        return found;
    }

    /**
     * Look at a specific Shape instance for intersections with a single line
     * segment.
     *
     * @param geom The geom we are picking against
     * @param req flags to compare against for picking
     * @param p1 The first point of the segment
     * @param p2 The second point of the segment
     * @param path A place to set the results in
     * @param needTransform True if the local to v-world transform needs
     *    calculating
     * @param useGeom true if this should check the results based on geometry
     *    rather than just bounds
     * @param needClosest true if every triangle should be searched to find
     *    the real one that is closest. Only used if useGeom is true
     */
    private boolean pickSingleLineSegment(LeafPickTarget geom,
                                          PickRequest req,
                                          float[] p1,
                                          float[] p2,
                                          SceneGraphPath path,
                                          boolean needTransform,
                                          boolean useGeom,
                                          boolean needClosest)
    {
        if(!geom.checkPickMask(req.pickType))
            return false;

        boolean found = false;
        BoundingVolume bounds = geom.getPickableBounds();

        if(bounds.checkIntersectionSegment(p1, p2))
        {
            if(useGeom)
            {
                if(geom.pickLineSegment(p1, p2, needClosest, vertexPickData, 0))
                {
                    resizePath();
                    pickPath[lastPathIndex] = geom;
                    validTransform[lastPathIndex] = false;
                    lastPathIndex++;

                    if(needTransform)
                        buildVWorldTransform();
                    else
                        vworldMatrix.setIdentity();

                    path.updatePath(pickPath,
                                    lastPathIndex,
                                    vworldMatrix,
                                    invertedMatrix);

                    found = true;
                    lastPathIndex--;
                }
            }
            else
            {
                resizePath();
                pickPath[lastPathIndex] = geom;
                validTransform[lastPathIndex] = false;
                lastPathIndex++;

                if(needTransform)
                    buildVWorldTransform();
                else
                    vworldMatrix.setIdentity();

                path.updatePath(pickPath,
                                lastPathIndex,
                                vworldMatrix,
                                invertedMatrix);

                found = true;
                lastPathIndex--;
            }
        }

        return found;
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
        int found = 0;
        ArrayList<SceneGraphPath> output_list = null;

        if(req.foundPaths instanceof ArrayList)
            output_list = (ArrayList<SceneGraphPath>)req.foundPaths;
        else
        {
            output_list = new ArrayList<>();
            req.foundPaths = output_list;
        }

        PickTarget target_node = root;
        BoundingVolume bounds = target_node.getPickableBounds();

        if(bounds == null ||
           !bounds.checkIntersectionSegment(req.origin, req.destination))
        {
            req.pickCount = 0;
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
                target_node = (PickTarget)geom;
            else
            {
                req.pickCount = 0;
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

        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    // reset the transform at the top of the stack
                    if(root instanceof TransformPickTarget)
                    {
                        Matrix4d tx = transformPath[0];

                        TransformPickTarget tg = (TransformPickTarget)root;
                        tg.getTransform(tx);
                        tg.getInverseTransform(invertedMatrix);
                        transform(invertedMatrix, start);
                        transform(invertedMatrix, end);
                    }
                    else
                        transformPath[0].setIdentity();

                    validTransform[0] = true;
                    pickPath[0] = g;
                    PickTarget[] kids = g.getPickableChildren();

                    // now that the setup is done, walk down the tree.
                    for(int i = 0; i < num_kids; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        lastPathIndex = 1;

                        switch(kids[i].getPickTargetType())
                        {
                            case PickTarget.GROUP_PICK_TYPE:
                                found += pickAllLineSegment((GroupPickTarget)kids[i],
                                                            req,
                                                            start,
                                                            end,
                                                            output_list,
                                                            found,
                                                            req.generateVWorldMatrix,
                                                            req.useGeometry);
                                break;

                            case PickTarget.SINGLE_PICK_TYPE:
                                found += pickAllLineSegment((SinglePickTarget)kids[i],
                                                            req,
                                                            start,
                                                            end,
                                                            output_list,
                                                            found,
                                                            req.generateVWorldMatrix,
                                                            req.useGeometry);
                                break;

                            case PickTarget.LEAF_PICK_TYPE:
                                found += pickAllLineSegment((LeafPickTarget)kids[i],
                                                            req,
                                                            start,
                                                            end,
                                                            output_list,
                                                            found,
                                                            req.generateVWorldMatrix,
                                                            req.useGeometry,
                                                            false);
                                break;

                            case PickTarget.CUSTOM_PICK_TYPE:
                                found += pickAllLineSegment((CustomPickTarget)kids[i],
                                                            req,
                                                            start,
                                                            end,
                                                            output_list,
                                                            found,
                                                            req.generateVWorldMatrix,
                                                            req.useGeometry);
                                break;
                        }
                    }

                }
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found += pickAllLineSegment((SinglePickTarget)target_node,
                                            req,
                                            start,
                                            end,
                                            output_list,
                                            0,
                                            req.generateVWorldMatrix,
                                            req.useGeometry);
                break;

            case PickTarget.LEAF_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found += pickAllLineSegment((LeafPickTarget)target_node,
                                            req,
                                            start,
                                            end,
                                            output_list,
                                            0,
                                            req.generateVWorldMatrix,
                                            req.useGeometry,
                                            false);
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                found += pickAllLineSegment((CustomPickTarget)target_node,
                                            req,
                                            start,
                                            end,
                                            output_list,
                                            found,
                                            req.generateVWorldMatrix,
                                            req.useGeometry);
        }

        req.pickCount = found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param root The now that is acting as the local root to work through
     * @param req flags to compare against for picking
     * @param p1 The first point of the segment
     * @param p2 The second point of the segment
     * @param paths Array of paths place to set the results in
     * @param currentPath Active index in the paths array
     * @param needTransform True if the local to v-world transform needs
     *    calculating
     * @param useGeom true if this should check the results based on geometry
     *    rather than just bounds
     */
    private int pickAllLineSegment(GroupPickTarget root,
                                   PickRequest req,
                                   float[] p1,
                                   float[] p2,
                                   ArrayList<SceneGraphPath> paths,
                                   int currentPath,
                                   boolean needTransform,
                                   boolean useGeom)
    {
        if(!root.checkPickMask(req.pickType))
            return 0;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionSegment(p1, p2))
            return 0;

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickAllLineSegment((GroupPickTarget)target_node,
                                          req,
                                          p1,
                                          p2,
                                          paths,
                                          currentPath,
                                          needTransform,
                                          useGeom);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickAllLineSegment((SinglePickTarget)target_node,
                                          req,
                                          p1,
                                          p2,
                                          paths,
                                          currentPath,
                                          needTransform,
                                          useGeom);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickAllLineSegment((LeafPickTarget)target_node,
                                          req,
                                          p1,
                                          p2,
                                          paths,
                                          currentPath,
                                          needTransform,
                                          useGeom,
                                          false);
            }
            else if(target_node instanceof CustomPickTarget)
            {
                return pickAllLineSegment((CustomPickTarget)target_node,
                                          req,
                                          p1,
                                          p2,
                                          paths,
                                          currentPath,
                                          needTransform,
                                          useGeom);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        int found = 0;
        int num_kids = root.numPickableChildren();

        if(num_kids != 0)
        {
            float p1_x = p1[0];
            float p1_y = p1[1];
            float p1_z = p1[2];

            float p2_x = p2[0];
            float p2_y = p2[1];
            float p2_z = p2[2];

            // reset the transform at the top of the stack
            if(root instanceof TransformPickTarget)
            {
                Matrix4d tx = transformPath[lastPathIndex];

                TransformPickTarget tg = (TransformPickTarget)root;
                tg.getTransform(tx);
                tg.getInverseTransform(invertedMatrix);
                transform(invertedMatrix, p1);
                transform(invertedMatrix, p2);

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = root.getPickableChildren();

            for(int i = 0; i < num_kids; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found += pickAllLineSegment((GroupPickTarget)kids[i],
                                                    req,
                                                    p1,
                                                    p2,
                                                    paths,
                                                    currentPath + found,
                                                    needTransform,
                                                    useGeom);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found += pickAllLineSegment((SinglePickTarget)kids[i],
                                                    req,
                                                    p1,
                                                    p2,
                                                    paths,
                                                    currentPath + found,
                                                    needTransform,
                                                    useGeom);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found += pickAllLineSegment((LeafPickTarget)kids[i],
                                                    req,
                                                    p1,
                                                    p2,
                                                    paths,
                                                    currentPath + found,
                                                    needTransform,
                                                    useGeom,
                                                    false);
                        break;

                    case PickTarget.CUSTOM_PICK_TYPE:
                        found += pickAllLineSegment((CustomPickTarget)kids[i],
                                                    req,
                                                    p1,
                                                    p2,
                                                    paths,
                                                    currentPath + found,
                                                    needTransform,
                                                    useGeom);
                        break;
                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            p1[0] = p1_x;
            p1[1] = p1_y;
            p1[2] = p1_z;

            p2[0] = p2_x;
            p2[1] = p2_y;
            p2[2] = p2_z;
        }

        return found;
    }

    /**
     * Recurse a single-child shared node tree looking for
     * intersections with a single point. Shared nodes are different to normal
     * because we don't bother doing bounds tests as the bounds will be
     * identical to their child. Skip the test and just head straight to the
     * child node to test against.
     *
     * @param root The shared node to test against
     * @param req flags to compare against for picking
     * @param p1 The first point of the segment
     * @param p2 The second point of the segment
     * @param paths Array of paths place to set the results in
     * @param currentPath Active index in the paths array
     * @param needTransform true if we should calc vworld information
     * @param useGeom true if this should check the results based on geometry
     *    rather than just bounds
     */
    private int pickAllLineSegment(SinglePickTarget root,
                                   PickRequest req,
                                   float[] p1,
                                   float[] p2,
                                   ArrayList<SceneGraphPath> paths,
                                   int currentPath,
                                   boolean needTransform,
                                   boolean useGeom)
    {
        if(!root.checkPickMask(req.pickType))
            return 0;

        resizePath();
        validTransform[lastPathIndex] = false;
        pickPath[lastPathIndex] = root;
        lastPathIndex++;

        PickTarget child = root.getPickableChild();
        if(child == null)
            return 0;

        int found = 0;

        switch(child.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                found = pickAllLineSegment((GroupPickTarget)child,
                                           req,
                                           p1,
                                           p2,
                                           paths,
                                           currentPath,
                                           needTransform,
                                           useGeom);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                found = pickAllLineSegment((SinglePickTarget)child,
                                           req,
                                           p1,
                                           p2,
                                           paths,
                                           currentPath,
                                           needTransform,
                                           useGeom);
                break;

            case PickTarget.LEAF_PICK_TYPE:
                found = pickAllLineSegment((LeafPickTarget)child,
                                           req,
                                           p1,
                                           p2,
                                           paths,
                                           currentPath,
                                           needTransform,
                                           useGeom,
                                           false);
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                found = pickAllLineSegment((CustomPickTarget)child,
                                           req,
                                           p1,
                                           p2,
                                           paths,
                                           currentPath,
                                           needTransform,
                                           useGeom);
                break;
        }

        lastPathIndex--;
        pickPath[lastPathIndex] = null;
        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param root The now that is acting as the local root to work through
     * @param req flags to compare against for picking
     * @param p1 The first point of the segment
     * @param p2 The second point of the segment
     * @param paths Array of paths place to set the results in
     * @param currentPath Active index in the paths array
     * @param needTransform True if the local to v-world transform needs
     *    calculating
     * @param useGeom true if this should check the results based on geometry
     *    rather than just bounds
     */
    private int pickAllLineSegment(CustomPickTarget root,
                                   PickRequest req,
                                   float[] p1,
                                   float[] p2,
                                   ArrayList<SceneGraphPath> paths,
                                   int currentPath,
                                   boolean needTransform,
                                   boolean useGeom)
    {
        if(!root.checkPickMask(req.pickType))
            return 0;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionSegment(p1, p2))
            return 0;

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickAllLineSegment((GroupPickTarget)target_node,
                                          req,
                                          p1,
                                          p2,
                                          paths,
                                          currentPath,
                                          needTransform,
                                          useGeom);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickAllLineSegment((SinglePickTarget)target_node,
                                          req,
                                          p1,
                                          p2,
                                          paths,
                                          currentPath,
                                          needTransform,
                                          useGeom);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickAllLineSegment((LeafPickTarget)target_node,
                                          req,
                                          p1,
                                          p2,
                                          paths,
                                          currentPath,
                                          needTransform,
                                          useGeom,
                                          false);
            }
            else if(target_node instanceof CustomPickTarget)
            {
                return pickAllLineSegment((CustomPickTarget)target_node,
                                          req,
                                          p1,
                                          p2,
                                          paths,
                                          currentPath,
                                          needTransform,
                                          useGeom);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        int found = 0;

        if(pickCustom(root, req))
        {
            int num_kids = pickInstructions.numChildren;

            float p1_x = p1[0];
            float p1_y = p1[1];
            float p1_z = p1[2];

            float p2_x = p2[0];
            float p2_y = p2[1];
            float p2_z = p2[2];

            // reset the transform at the top of the stack
            if(pickInstructions.hasTransform)
            {
                transformPath[lastPathIndex].set(pickInstructions.localTransform);

                matrixUtils.inverse(pickInstructions.localTransform,
                                    invertedMatrix);
                transform(invertedMatrix, p1);
                transform(invertedMatrix, p2);

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = pickInstructions.children.clone();

            for(int i = 0; i < num_kids; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found += pickAllLineSegment((GroupPickTarget)kids[i],
                                                    req,
                                                    p1,
                                                    p2,
                                                    paths,
                                                    currentPath + found,
                                                    needTransform,
                                                    useGeom);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found += pickAllLineSegment((SinglePickTarget)kids[i],
                                                    req,
                                                    p1,
                                                    p2,
                                                    paths,
                                                    currentPath + found,
                                                    needTransform,
                                                    useGeom);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found += pickAllLineSegment((LeafPickTarget)kids[i],
                                                    req,
                                                    p1,
                                                    p2,
                                                    paths,
                                                    currentPath + found,
                                                    needTransform,
                                                    useGeom,
                                                    false);
                        break;

                    case PickTarget.CUSTOM_PICK_TYPE:
                        found += pickAllLineSegment((CustomPickTarget)kids[i],
                                                    req,
                                                    p1,
                                                    p2,
                                                    paths,
                                                    currentPath + found,
                                                    needTransform,
                                                    useGeom);
                        break;
                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            p1[0] = p1_x;
            p1[1] = p1_y;
            p1[2] = p1_z;

            p2[0] = p2_x;
            p2[1] = p2_y;
            p2[2] = p2_z;
        }

        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param req flags to compare against for picking
     * @param paths A place to set the results in
     * @param needTransform True if the local to v-world transform needs
     *    calculating
     */
    private int pickAllLineSegment(LeafPickTarget geom,
                                   PickRequest req,
                                   float[] p1,
                                   float[] p2,
                                   ArrayList<SceneGraphPath> paths,
                                   int currentPath,
                                   boolean needTransform,
                                   boolean useGeom,
                                   boolean needClosest)
    {
        if(!geom.checkPickMask(req.pickType))
            return 0;

        int found = 0;
        BoundingVolume bounds = geom.getPickableBounds();

        if(bounds.checkIntersectionSegment(p1, p2))
        {
            if(useGeom)
            {
                if(geom.pickLineSegment(p1, p2, needClosest, vertexPickData, 0))
                {
                    resizePath();
                    pickPath[lastPathIndex] = geom;
                    validTransform[lastPathIndex] = false;
                    lastPathIndex++;

                    SceneGraphPath p;

                    if(currentPath >= paths.size())
                    {
                        p = new SceneGraphPath();
                        paths.add(p);
                    }
                    else
                        p = paths.get(currentPath);

                    if(needTransform)
                        buildVWorldTransform();
                    else
                        vworldMatrix.setIdentity();


                    p.updatePath(pickPath,
                                 lastPathIndex,
                                 vworldMatrix,
                                 invertedMatrix);

                    found = 1;
                    lastPathIndex--;
                }
            }
            else
            {
                resizePath();
                pickPath[lastPathIndex] = geom;
                validTransform[lastPathIndex] = false;
                lastPathIndex++;

                SceneGraphPath p;

                if(currentPath >= paths.size())
                {
                    p = new SceneGraphPath();
                    paths.add(p);
                }
                else
                    p = paths.get(currentPath);

                if(needTransform)
                    buildVWorldTransform();
                else
                    vworldMatrix.setIdentity();


                p.updatePath(pickPath,
                             lastPathIndex,
                             vworldMatrix,
                             invertedMatrix);

                found = 1;
                lastPathIndex--;
            }
        }

        return found;
    }

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given ray. Return the first
     * found.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     */
    private void pickSingleLineSegmentSorted(PickTarget root, PickRequest req)
    {
        SceneGraphPath output_path = null;

        if(req.foundPaths instanceof SceneGraphPath)
            output_path = (SceneGraphPath)req.foundPaths;
        else
        {
            output_path = new SceneGraphPath();
            req.foundPaths = output_path;
        }

        boolean found = false;

        PickTarget target_node = root;
        BoundingVolume bounds = target_node.getPickableBounds();

        if(bounds == null ||
           !bounds.checkIntersectionSegment(req.origin, req.destination))
        {
            req.pickCount = 0;
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
                target_node = (PickTarget)geom;
            else
            {
                req.pickCount = 0;
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

        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    // reset the transform at the top of the stack
                    if(root instanceof TransformPickTarget)
                    {
                        Matrix4d tx = transformPath[0];

                        TransformPickTarget tg = (TransformPickTarget)root;
                        tg.getTransform(tx);
                        tg.getInverseTransform(invertedMatrix);
                        transform(invertedMatrix, start);
                        transform(invertedMatrix, end);
                    }
                    else
                        transformPath[0].setIdentity();

                    validTransform[0] = true;
                    pickPath[0] = g;
                    PickTarget[] kids = g.getPickableChildren();

                    // now that the setup is done, walk down the tree.
                    for(int i = 0; i < num_kids && !found; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        lastPathIndex = 1;

                        switch(kids[i].getPickTargetType())
                        {
                            case PickTarget.GROUP_PICK_TYPE:
                                found = pickSingleLineSegmentSorted((GroupPickTarget)kids[i],
                                                                    req,
                                                                    start,
                                                                    end,
                                                                    output_path,
                                                                    req.generateVWorldMatrix,
                                                                    req.useGeometry);
                                break;

                            case PickTarget.SINGLE_PICK_TYPE:
                                found = pickSingleLineSegmentSorted((SinglePickTarget)kids[i],
                                                                    req,
                                                                    start,
                                                                    end,
                                                                    output_path,
                                                                    req.generateVWorldMatrix,
                                                                    req.useGeometry);
                                break;

                            case PickTarget.LEAF_PICK_TYPE:
                                found = pickSingleLineSegment((LeafPickTarget)kids[i],
                                                              req,
                                                              start,
                                                              end,
                                                              output_path,
                                                              req.generateVWorldMatrix,
                                                              req.useGeometry,
                                                              true);
                                break;

                            case PickTarget.CUSTOM_PICK_TYPE:
                                found = pickSingleLineSegmentSorted((CustomPickTarget)kids[i],
                                                                    req,
                                                                    start,
                                                                    end,
                                                                    output_path,
                                                                    req.generateVWorldMatrix,
                                                                    req.useGeometry);
                                break;
                        }
                    }
                }
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found = pickSingleLineSegmentSorted((SinglePickTarget)target_node,
                                                    req,
                                                    start,
                                                    end,
                                                    output_path,
                                                    req.generateVWorldMatrix,
                                                    req.useGeometry);

                break;

            case PickTarget.LEAF_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found = pickSingleLineSegment((LeafPickTarget)target_node,
                                               req,
                                               start,
                                               end,
                                               output_path,
                                               req.generateVWorldMatrix,
                                               req.useGeometry,
                                               true);
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                found = pickSingleLineSegmentSorted((CustomPickTarget)target_node,
                                                    req,
                                                    start,
                                                    end,
                                                    output_path,
                                                    req.generateVWorldMatrix,
                                                    req.useGeometry);
                break;
        }

        req.pickCount = found ? 1 : 0;
    }

    /**
     * Recurse a single-child shared node tree looking for
     * intersections with a single point. Shared nodes are different to normal
     * because we don't bother doing bounds tests as the bounds will be
     * identical to their child. Skip the test and just head straight to the
     * child node to test against.
     *
     * @param root The shared node to test against
     * @param req flags to compare against for picking
     * @param p1 The first point of the segment
     * @param p2 The second point of the segment
     * @param path The place to put the results in
     * @param needTransform true if we should calc vworld information
     * @param useGeom true if this should check the results based on geometry
     *    rather than just bounds
     */
    private boolean pickSingleLineSegmentSorted(SinglePickTarget root,
                                                PickRequest req,
                                                float[] p1,
                                                float[] p2,
                                                SceneGraphPath path,
                                                boolean needTransform,
                                                boolean useGeom)
    {
        if(!root.checkPickMask(req.pickType))
            return false;

        resizePath();
        validTransform[lastPathIndex] = false;
        pickPath[lastPathIndex] = root;
        lastPathIndex++;

        PickTarget child = root.getPickableChild();
        if(child == null)
            return false;

        boolean found = false;

        switch(child.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                found = pickSingleLineSegmentSorted((GroupPickTarget)child,
                                                    req,
                                                    p1,
                                                    p2,
                                                    path,
                                                    needTransform,
                                                    useGeom);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                found = pickSingleLineSegmentSorted((SinglePickTarget)child,
                                                    req,
                                                    p1,
                                                    p2,
                                                    path,
                                                    needTransform,
                                                    useGeom);
                break;

            case PickTarget.LEAF_PICK_TYPE:
                found = pickSingleLineSegment((LeafPickTarget)child,
                                              req,
                                              p1,
                                              p2,
                                              path,
                                              needTransform,
                                              useGeom,
                                              true);
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                found = pickSingleLineSegmentSorted((CustomPickTarget)child,
                                                    req,
                                                    p1,
                                                    p2,
                                                    path,
                                                    needTransform,
                                                    useGeom);
                break;

        }

        lastPathIndex--;
        pickPath[lastPathIndex] = null;
        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param root The now that is acting as the local root to work through
     * @param req flags to compare against for picking
     * @param p1 The first point of the segment
     * @param p2 The second point of the segment
     * @param path A place to set the results in
     * @param needTransform True if the local to v-world transform needs
     *    calculating
     * @param useGeom true if this should check the results based on geometry
     *    rather than just bounds
     */
    private boolean pickSingleLineSegmentSorted(GroupPickTarget root,
                                                PickRequest req,
                                                float[] p1,
                                                float[] p2,
                                                SceneGraphPath path,
                                                boolean needTransform,
                                                boolean useGeom)
    {
        if(!root.checkPickMask(req.pickType))
            return false;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionSegment(p1, p2))
            return false;

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickSingleLineSegmentSorted((GroupPickTarget)target_node,
                                                  req,
                                                  p1,
                                                  p2,
                                                  path,
                                                  needTransform,
                                                  useGeom);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickSingleLineSegmentSorted((SinglePickTarget)target_node,
                                                   req,
                                                   p1,
                                                   p2,
                                                   path,
                                                   needTransform,
                                                   useGeom);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickSingleLineSegment((LeafPickTarget)target_node,
                                             req,
                                             p1,
                                             p2,
                                             path,
                                             needTransform,
                                             useGeom,
                                             true);
            }
            else if(target_node instanceof CustomPickTarget)
            {
                return pickSingleLineSegmentSorted((CustomPickTarget)target_node,
                                                   req,
                                                   p1,
                                                   p2,
                                                   path,
                                                   needTransform,
                                                   useGeom);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        boolean found = false;
        int num_kids = root.numPickableChildren();

        if(num_kids != 0)
        {
            float p1_x = p1[0];
            float p1_y = p1[1];
            float p1_z = p1[2];

            float p2_x = p2[0];
            float p2_y = p2[1];
            float p2_z = p2[2];

            // reset the transform at the top of the stack
            if(root instanceof TransformPickTarget)
            {
                Matrix4d tx = transformPath[lastPathIndex];

                TransformPickTarget tg = (TransformPickTarget)root;
                tg.getTransform(tx);
                tg.getInverseTransform(invertedMatrix);
                transform(invertedMatrix, p1);
                transform(invertedMatrix, p2);

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = root.getPickableChildren();

            for(int i = 0; i < num_kids && !found; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found = pickSingleLineSegmentSorted((GroupPickTarget)kids[i],
                                                            req,
                                                            p1,
                                                            p2,
                                                            path,
                                                            needTransform,
                                                            useGeom);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found = pickSingleLineSegmentSorted((SinglePickTarget)kids[i],
                                                            req,
                                                            p1,
                                                            p2,
                                                            path,
                                                            needTransform,
                                                            useGeom);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found = pickSingleLineSegment((LeafPickTarget)kids[i],
                                                      req,
                                                      p1,
                                                      p2,
                                                      path,
                                                      needTransform,
                                                      useGeom,
                                                      true);
                        break;

                    case PickTarget.CUSTOM_PICK_TYPE:
                        found = pickSingleLineSegmentSorted((CustomPickTarget)kids[i],
                                                            req,
                                                            p1,
                                                            p2,
                                                            path,
                                                            needTransform,
                                                            useGeom);
                        break;

                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            p1[0] = p1_x;
            p1[1] = p1_y;
            p1[2] = p1_z;

            p2[0] = p2_x;
            p2[1] = p2_y;
            p2[2] = p2_z;
        }

        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param root The now that is acting as the local root to work through
     * @param req flags to compare against for picking
     * @param p1 The first point of the segment
     * @param p2 The second point of the segment
     * @param path A place to set the results in
     * @param needTransform True if the local to v-world transform needs
     *    calculating
     * @param useGeom true if this should check the results based on geometry
     *    rather than just bounds
     */
    private boolean pickSingleLineSegmentSorted(CustomPickTarget root,
                                                PickRequest req,
                                                float[] p1,
                                                float[] p2,
                                                SceneGraphPath path,
                                                boolean needTransform,
                                                boolean useGeom)
    {
        if(!root.checkPickMask(req.pickType))
            return false;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionSegment(p1, p2))
            return false;

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickSingleLineSegmentSorted((GroupPickTarget)target_node,
                                                  req,
                                                  p1,
                                                  p2,
                                                  path,
                                                  needTransform,
                                                  useGeom);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickSingleLineSegmentSorted((SinglePickTarget)target_node,
                                                   req,
                                                   p1,
                                                   p2,
                                                   path,
                                                   needTransform,
                                                   useGeom);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickSingleLineSegment((LeafPickTarget)target_node,
                                             req,
                                             p1,
                                             p2,
                                             path,
                                             needTransform,
                                             useGeom,
                                             true);
            }
            else if(target_node instanceof CustomPickTarget)
            {
                return pickSingleLineSegmentSorted((CustomPickTarget)target_node,
                                                   req,
                                                   p1,
                                                   p2,
                                                   path,
                                                   needTransform,
                                                   useGeom);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        boolean found = false;
        if(pickCustom(root, req))
        {
            int num_kids = pickInstructions.numChildren;
            float p1_x = p1[0];
            float p1_y = p1[1];
            float p1_z = p1[2];

            float p2_x = p2[0];
            float p2_y = p2[1];
            float p2_z = p2[2];

            // reset the transform at the top of the stack
            if(pickInstructions.hasTransform)
            {
                transformPath[lastPathIndex].set(pickInstructions.localTransform);

                matrixUtils.inverse(pickInstructions.localTransform,
                                    invertedMatrix);
                transform(invertedMatrix, p1);
                transform(invertedMatrix, p2);

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = pickInstructions.children.clone();

            for(int i = 0; i < num_kids && !found; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found = pickSingleLineSegmentSorted((GroupPickTarget)kids[i],
                                                            req,
                                                            p1,
                                                            p2,
                                                            path,
                                                            needTransform,
                                                            useGeom);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found = pickSingleLineSegmentSorted((SinglePickTarget)kids[i],
                                                            req,
                                                            p1,
                                                            p2,
                                                            path,
                                                            needTransform,
                                                            useGeom);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found = pickSingleLineSegment((LeafPickTarget)kids[i],
                                                      req,
                                                      p1,
                                                      p2,
                                                      path,
                                                      needTransform,
                                                      useGeom,
                                                      true);
                        break;

                    case PickTarget.CUSTOM_PICK_TYPE:
                        found = pickSingleLineSegmentSorted((CustomPickTarget)kids[i],
                                                            req,
                                                            p1,
                                                            p2,
                                                            path,
                                                            needTransform,
                                                            useGeom);
                        break;

                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            p1[0] = p1_x;
            p1[1] = p1_y;
            p1[2] = p1_z;

            p2[0] = p2_x;
            p2[1] = p2_y;
            p2[2] = p2_z;
        }

        return found;
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

            default:
				I18nManager intl_mgr = I18nManager.getManager();
				String msg_pattern = intl_mgr.getString(NO_SORT_TYPE_PROP);

				Locale lcl = intl_mgr.getFoundLocale();

				NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

				Object[] msg_args = { req.pickSortType };
				Format[] fmts = { n_fmt };
				MessageFormat msg_fmt =
					new MessageFormat(msg_pattern, lcl);
				msg_fmt.setFormats(fmts);
				String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
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
        SceneGraphPath output_path = null;

        if(req.foundPaths instanceof SceneGraphPath)
            output_path = (SceneGraphPath)req.foundPaths;
        else
        {
            output_path = new SceneGraphPath();
            req.foundPaths = output_path;
        }

        boolean found = false;

        PickTarget target_node = root;
        BoundingVolume bounds = target_node.getPickableBounds();

        if(bounds == null ||
           !bounds.checkIntersectionRay(req.origin, req.destination))
        {
            req.pickCount = 0;
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
                target_node = (PickTarget)geom;
            else
            {
                req.pickCount = 0;
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

        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    // reset the transform at the top of the stack
                    if(root instanceof TransformPickTarget)
                    {
                        Matrix4d tx = transformPath[lastPathIndex];

                        TransformPickTarget tg = (TransformPickTarget)root;
                        tg.getTransform(tx);
                        tg.getInverseTransform(invertedMatrix);
                        transform(invertedMatrix, start);
                        transformNormal(invertedMatrix, end);
                    }
                    else
                        transformPath[0].setIdentity();

                    validTransform[0] = true;
                    pickPath[0] = g;
                    PickTarget[] kids = g.getPickableChildren();

                    // now that the setup is done, walk down the tree.
                    for(int i = 0; i < num_kids && !found; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        lastPathIndex = 1;

                        switch(kids[i].getPickTargetType())
                        {
                            case PickTarget.GROUP_PICK_TYPE:
                                found = pickSingleRay((GroupPickTarget)kids[i],
                                                      req,
                                                      start,
                                                      end,
                                                      output_path,
                                                      req.generateVWorldMatrix,
                                                      req.useGeometry);
                                break;

                            case PickTarget.SINGLE_PICK_TYPE:
                                found = pickSingleRay((SinglePickTarget)kids[i],
                                                      req,
                                                      start,
                                                      end,
                                                      output_path,
                                                      req.generateVWorldMatrix,
                                                      req.useGeometry);
                                break;

                            case PickTarget.LEAF_PICK_TYPE:
                                found = pickSingleRay((LeafPickTarget)kids[i],
                                                      req,
                                                      start,
                                                      end,
                                                      output_path,
                                                      req.generateVWorldMatrix,
                                                      req.useGeometry,
                                                      false);
                                break;

                            case PickTarget.CUSTOM_PICK_TYPE:
                                found = pickSingleRay((CustomPickTarget)kids[i],
                                                      req,
                                                      start,
                                                      end,
                                                      output_path,
                                                      req.generateVWorldMatrix,
                                                      req.useGeometry);
                                break;
                        }
                    }
                }
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found = pickSingleRay((SinglePickTarget)target_node,
                                      req,
                                      start,
                                      end,
                                      output_path,
                                      req.generateVWorldMatrix,
                                      req.useGeometry);
                break;

            case PickTarget.LEAF_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found = pickSingleRay((LeafPickTarget)target_node,
                                      req,
                                      start,
                                      end,
                                      output_path,
                                      req.generateVWorldMatrix,
                                      req.useGeometry,
                                      false);
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                found = pickSingleRay((CustomPickTarget)target_node,
                                      req,
                                      start,
                                      end,
                                      output_path,
                                      req.generateVWorldMatrix,
                                      req.useGeometry);
                break;
        }

        req.pickCount = found ? 1 : 0;
    }

    /**
     * Recurse a single-child shared node tree looking for
     * intersections with a single point. Shared nodes are different to normal
     * because we don't bother doing bounds tests as the bounds will be
     * identical to their child. Skip the test and just head straight to the
     * child node to test against.
     *
     * @param root The shared node to test against
     * @param req flags to compare against for picking
     * @param p1 The first point of the segment
     * @param p2 The second point of the segment
     * @param path The place to put the results in
     * @param needTransform true if we should calc vworld information
     * @param useGeom true if this should check the results based on geometry
     *    rather than just bounds
     */
    private boolean pickSingleRay(SinglePickTarget root,
                                  PickRequest req,
                                  float[] p1,
                                  float[] p2,
                                  SceneGraphPath path,
                                  boolean needTransform,
                                  boolean useGeom)
    {
        if(!root.checkPickMask(req.pickType))
            return false;

        PickTarget child = root.getPickableChild();

        if(child == null)
            return false;

        resizePath();
        validTransform[lastPathIndex] = false;
        pickPath[lastPathIndex] = root;
        lastPathIndex++;

        boolean found = false;

        switch(child.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                found = pickSingleRay((GroupPickTarget)child,
                                      req,
                                      p1,
                                      p2,
                                      path,
                                      needTransform,
                                      useGeom);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                found = pickSingleRay((SinglePickTarget)child,
                                      req,
                                      p1,
                                      p2,
                                      path,
                                      needTransform,
                                      useGeom);
                break;

            case PickTarget.LEAF_PICK_TYPE:
                found = pickSingleRay((LeafPickTarget)child,
                                      req,
                                      p1,
                                      p2,
                                      path,
                                      needTransform,
                                      useGeom,
                                      true);
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                found = pickSingleRay((CustomPickTarget)child,
                                      req,
                                      p1,
                                      p2,
                                      path,
                                      needTransform,
                                      useGeom);
                break;

        }

        lastPathIndex--;
        pickPath[lastPathIndex] = null;
        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single line segment.
     *
     * @param root The now that is acting as the local root to work through
     * @param req flags to compare against for picking
     * @param p1 The first point of the segment
     * @param p2 The second point of the segment
     * @param path A place to set the results in
     * @param needTransform True if the local to v-world transform needs
     *    calculating
     * @param useGeom true if this should check the results based on geometry
     *    rather than just bounds
     */
    private boolean pickSingleRay(GroupPickTarget root,
                                  PickRequest req,
                                  float[] p1,
                                  float[] p2,
                                  SceneGraphPath path,
                                  boolean needTransform,
                                  boolean useGeom)
    {
        if(!root.checkPickMask(req.pickType))
            return false;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionRay(p1, p2))
            return false;

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickSingleRay((GroupPickTarget)target_node,
                                      req,
                                      p1,
                                      p2,
                                      path,
                                      needTransform,
                                      useGeom);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickSingleRay((SinglePickTarget)target_node,
                                     req,
                                     p1,
                                     p2,
                                     path,
                                     needTransform,
                                     useGeom);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickSingleRay((LeafPickTarget)target_node,
                                     req,
                                     p1,
                                     p2,
                                     path,
                                     needTransform,
                                     useGeom,
                                     false);
            }
            else if(target_node instanceof CustomPickTarget)
            {
                return pickSingleRay((CustomPickTarget)target_node,
                                     req,
                                     p1,
                                     p2,
                                     path,
                                     needTransform,
                                     useGeom);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        boolean found = false;
        int num_kids = root.numPickableChildren();

        if(num_kids != 0)
        {
            float p1_x = p1[0];
            float p1_y = p1[1];
            float p1_z = p1[2];

            float p2_x = p2[0];
            float p2_y = p2[1];
            float p2_z = p2[2];

            // reset the transform at the top of the stack
            if(root instanceof TransformPickTarget)
            {
                Matrix4d tx = transformPath[lastPathIndex];

                TransformPickTarget tg = (TransformPickTarget)root;
                tg.getTransform(tx);
                tg.getInverseTransform(invertedMatrix);
                transform(invertedMatrix, p1);
                transformNormal(invertedMatrix, p2);

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = root.getPickableChildren();

            for(int i = 0; i < num_kids && !found; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found = pickSingleRay((GroupPickTarget)kids[i],
                                              req,
                                              p1,
                                              p2,
                                              path,
                                              needTransform,
                                              useGeom);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found = pickSingleRay((SinglePickTarget)kids[i],
                                              req,
                                              p1,
                                              p2,
                                              path,
                                              needTransform,
                                              useGeom);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found = pickSingleRay((LeafPickTarget)kids[i],
                                              req,
                                              p1,
                                              p2,
                                              path,
                                              needTransform,
                                              useGeom,
                                              false);
                        break;

                    case PickTarget.CUSTOM_PICK_TYPE:
                        found = pickSingleRay((CustomPickTarget)kids[i],
                                              req,
                                              p1,
                                              p2,
                                              path,
                                              needTransform,
                                              useGeom);
                        break;

                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            p1[0] = p1_x;
            p1[1] = p1_y;
            p1[2] = p1_z;

            p2[0] = p2_x;
            p2[1] = p2_y;
            p2[2] = p2_z;
        }

        return found;
    }

    /**
     * Look at a specific Shape instance for intersections with a single line
     * segment.
     *
     * @param geom The geom we are picking against
     * @param req flags to compare against for picking
     * @param p1 The first point of the segment
     * @param p2 The second point of the segment
     * @param path A place to set the results in
     * @param needTransform True if the local to v-world transform needs
     *    calculating
     * @param useGeom true if this should check the results based on geometry
     *    rather than just bounds
     * @param needClosest true if every triangle should be searched to find
     *    the real one that is closest. Only used if useGeom is true
     */
    private boolean pickSingleRay(LeafPickTarget geom,
                                  PickRequest req,
                                  float[] p1,
                                  float[] p2,
                                  SceneGraphPath path,
                                  boolean needTransform,
                                  boolean useGeom,
                                  boolean needClosest)
    {
        if(!geom.checkPickMask(req.pickType))
            return false;

        boolean found = false;
        BoundingVolume bounds = geom.getPickableBounds();

        if(bounds.checkIntersectionRay(p1, p2))
        {
            if(useGeom)
            {
                if(geom.pickLineRay(p1, p2, needClosest, vertexPickData, 0))
                {
                    resizePath();
                    pickPath[lastPathIndex] = geom;
                    validTransform[lastPathIndex] = false;
                    lastPathIndex++;

                    if(needTransform)
                        buildVWorldTransform();
                    else
                        vworldMatrix.setIdentity();

                    path.updatePath(pickPath,
                        lastPathIndex,
                        vworldMatrix,
                        invertedMatrix);

                    found = true;
                    lastPathIndex--;
                }
            }
            else
            {
                resizePath();
                pickPath[lastPathIndex] = geom;
                validTransform[lastPathIndex] = false;
                lastPathIndex++;

                if(needTransform)
                    buildVWorldTransform();
                else
                    vworldMatrix.setIdentity();

                path.updatePath(pickPath,
                                lastPathIndex,
                                vworldMatrix,
                                invertedMatrix);

                found = true;
                lastPathIndex--;
            }
        }

        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single line segment.
     *
     * @param root The now that is acting as the local root to work through
     * @param req flags to compare against for picking
     * @param p1 The first point of the segment
     * @param p2 The second point of the segment
     * @param path A place to set the results in
     * @param needTransform True if the local to v-world transform needs
     *    calculating
     * @param useGeom true if this should check the results based on geometry
     *    rather than just bounds
     */
    private boolean pickSingleRay(CustomPickTarget root,
                                  PickRequest req,
                                  float[] p1,
                                  float[] p2,
                                  SceneGraphPath path,
                                  boolean needTransform,
                                  boolean useGeom)
    {
        if(!root.checkPickMask(req.pickType))
            return false;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionRay(p1, p2))
            return false;

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickSingleRay((GroupPickTarget)target_node,
                                      req,
                                      p1,
                                      p2,
                                      path,
                                      needTransform,
                                      useGeom);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickSingleRay((SinglePickTarget)target_node,
                                     req,
                                     p1,
                                     p2,
                                     path,
                                     needTransform,
                                     useGeom);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickSingleRay((LeafPickTarget)target_node,
                                     req,
                                     p1,
                                     p2,
                                     path,
                                     needTransform,
                                     useGeom,
                                     false);
            }
            else if(target_node instanceof CustomPickTarget)
            {
                return pickSingleRay((CustomPickTarget)target_node,
                                     req,
                                     p1,
                                     p2,
                                     path,
                                     needTransform,
                                     useGeom);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        boolean found = false;
        if(pickCustom(root, req))
        {
            int num_kids = pickInstructions.numChildren;

            float p1_x = p1[0];
            float p1_y = p1[1];
            float p1_z = p1[2];

            float p2_x = p2[0];
            float p2_y = p2[1];
            float p2_z = p2[2];

            // reset the transform at the top of the stack
            if(pickInstructions.hasTransform)
            {
                transformPath[lastPathIndex].set(pickInstructions.localTransform);

                matrixUtils.inverse(pickInstructions.localTransform,
                                    invertedMatrix);
                transform(invertedMatrix, p1);
                transformNormal(invertedMatrix, p2);

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = pickInstructions.children.clone();

            for(int i = 0; i < num_kids && !found; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found = pickSingleRay((GroupPickTarget)kids[i],
                                              req,
                                              p1,
                                              p2,
                                              path,
                                              needTransform,
                                              useGeom);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found = pickSingleRay((SinglePickTarget)kids[i],
                                              req,
                                              p1,
                                              p2,
                                              path,
                                              needTransform,
                                              useGeom);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found = pickSingleRay((LeafPickTarget)kids[i],
                                              req,
                                              p1,
                                              p2,
                                              path,
                                              needTransform,
                                              useGeom,
                                              false);
                        break;

                    case PickTarget.CUSTOM_PICK_TYPE:
                        found = pickSingleRay((CustomPickTarget)kids[i],
                                              req,
                                              p1,
                                              p2,
                                              path,
                                              needTransform,
                                              useGeom);
                        break;

                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            p1[0] = p1_x;
            p1[1] = p1_y;
            p1[2] = p1_z;

            p2[0] = p2_x;
            p2[1] = p2_y;
            p2[2] = p2_z;
        }

        return found;
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
        int found = 0;
        ArrayList<SceneGraphPath> output_list = null;

        if(req.foundPaths instanceof ArrayList)
            output_list = (ArrayList<SceneGraphPath>)req.foundPaths;
        else
        {
            output_list = new ArrayList<>();
            req.foundPaths = output_list;
        }

        PickTarget target_node = root;
        BoundingVolume bounds = target_node.getPickableBounds();

        if(bounds == null ||
           !bounds.checkIntersectionRay(req.origin, req.destination))
        {
            req.pickCount = 0;
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
                target_node = (PickTarget)geom;
            else
            {
                req.pickCount = 0;
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

        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    // reset the transform at the top of the stack
                    if(root instanceof TransformPickTarget)
                    {
                        Matrix4d tx = transformPath[0];

                        TransformPickTarget tg = (TransformPickTarget)root;
                        tg.getTransform(tx);
                        tg.getInverseTransform(invertedMatrix);
                        transform(invertedMatrix, start);
                        transformNormal(invertedMatrix, end);
                    }
                    else
                        transformPath[0].setIdentity();

                    validTransform[0] = true;
                    pickPath[0] = g;
                    PickTarget[] kids = g.getPickableChildren();

                    // now that the setup is done, walk down the tree.
                    for(int i = 0; i < num_kids; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        lastPathIndex = 1;

                        switch(kids[i].getPickTargetType())
                        {
                            case PickTarget.GROUP_PICK_TYPE:
                                found += pickAllRay((GroupPickTarget)kids[i],
                                                    req,
                                                    start,
                                                    end,
                                                    output_list,
                                                    found,
                                                    req.generateVWorldMatrix,
                                                    req.useGeometry);
                                break;

                            case PickTarget.SINGLE_PICK_TYPE:
                                found += pickAllRay((SinglePickTarget)kids[i],
                                                    req,
                                                    start,
                                                    end,
                                                    output_list,
                                                    found,
                                                    req.generateVWorldMatrix,
                                                    req.useGeometry);
                                break;

                            case PickTarget.LEAF_PICK_TYPE:
                                found += pickAllRay((LeafPickTarget)kids[i],
                                                    req,
                                                    start,
                                                    end,
                                                    output_list,
                                                    found,
                                                    req.generateVWorldMatrix,
                                                    req.useGeometry,
                                                    false);
                                break;

                            case PickTarget.CUSTOM_PICK_TYPE:
                                found += pickAllRay((CustomPickTarget)kids[i],
                                                    req,
                                                    start,
                                                    end,
                                                    output_list,
                                                    found,
                                                    req.generateVWorldMatrix,
                                                    req.useGeometry);
                                break;
                        }
                    }
                }
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found += pickAllRay((SinglePickTarget)target_node,
                                    req,
                                    start,
                                    end,
                                    output_list,
                                    0,
                                    req.generateVWorldMatrix,
                                    req.useGeometry);
                break;

            case PickTarget.LEAF_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found += pickAllRay((LeafPickTarget)target_node,
                                    req,
                                    start,
                                    end,
                                    output_list,
                                    0,
                                    req.generateVWorldMatrix,
                                    req.useGeometry,
                                    false);
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                found += pickAllRay((CustomPickTarget)target_node,
                                    req,
                                    start,
                                    end,
                                    output_list,
                                    found,
                                    req.generateVWorldMatrix,
                                    req.useGeometry);
        }

        req.pickCount = found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param root The now that is acting as the local root to work through
     * @param req flags to compare against for picking
     * @param p1 The first point of the segment
     * @param p2 The second point of the segment
     * @param paths Array of paths place to set the results in
     * @param currentPath Active index in the paths array
     * @param needTransform True if the local to v-world transform needs
     *    calculating
     * @param useGeom true if this should check the results based on geometry
     *    rather than just bounds
     */
    private int pickAllRay(GroupPickTarget root,
                           PickRequest req,
                           float[] p1,
                           float[] p2,
                           ArrayList<SceneGraphPath> paths,
                           int currentPath,
                           boolean needTransform,
                           boolean useGeom)
    {
        if(!root.checkPickMask(req.pickType))
            return 0;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionRay(p1, p2))
            return 0;

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickAllRay((GroupPickTarget)target_node,
                                  req,
                                  p1,
                                  p2,
                                  paths,
                                  currentPath,
                                  needTransform,
                                  useGeom);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickAllRay((SinglePickTarget)target_node,
                                  req,
                                  p1,
                                  p2,
                                  paths,
                                  currentPath,
                                  needTransform,
                                  useGeom);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickAllRay((LeafPickTarget)target_node,
                                  req,
                                  p1,
                                  p2,
                                  paths,
                                  currentPath,
                                  needTransform,
                                  useGeom,
                                  false);
            }
            else if(target_node instanceof CustomPickTarget)
            {
                return pickAllRay((CustomPickTarget)target_node,
                                  req,
                                  p1,
                                  p2,
                                  paths,
                                  currentPath,
                                  needTransform,
                                  useGeom);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        int found = 0;
        int num_kids = root.numPickableChildren();

        if(num_kids != 0)
        {
            float p1_x = p1[0];
            float p1_y = p1[1];
            float p1_z = p1[2];

            float p2_x = p2[0];
            float p2_y = p2[1];
            float p2_z = p2[2];

            // reset the transform at the top of the stack
            resizePath();
            if(root instanceof TransformPickTarget)
            {
                Matrix4d tx = transformPath[lastPathIndex];

                TransformPickTarget tg = (TransformPickTarget)root;
                tg.getTransform(tx);
                tg.getInverseTransform(invertedMatrix);
                transform(invertedMatrix, p1);
                transformNormal(invertedMatrix, p2);

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = root.getPickableChildren();

            for(int i = 0; i < num_kids; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found += pickAllRay((GroupPickTarget)kids[i],
                                            req,
                                            p1,
                                            p2,
                                            paths,
                                            currentPath + found,
                                            needTransform,
                                            useGeom);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found += pickAllRay((SinglePickTarget)kids[i],
                                            req,
                                            p1,
                                            p2,
                                            paths,
                                            currentPath + found,
                                            needTransform,
                                            useGeom);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found += pickAllRay((LeafPickTarget)kids[i],
                                            req,
                                            p1,
                                            p2,
                                            paths,
                                            currentPath + found,
                                            needTransform,
                                            useGeom,
                                            false);
                        break;

                    case PickTarget.CUSTOM_PICK_TYPE:
                        found += pickAllRay((CustomPickTarget)kids[i],
                                            req,
                                            p1,
                                            p2,
                                            paths,
                                            currentPath + found,
                                            needTransform,
                                            useGeom);
                        break;
                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            p1[0] = p1_x;
            p1[1] = p1_y;
            p1[2] = p1_z;

            p2[0] = p2_x;
            p2[1] = p2_y;
            p2[2] = p2_z;
        }

        return found;
    }

    /**
     * Recurse a single-child shared node tree looking for
     * intersections with a single point. Shared nodes are different to normal
     * because we don't bother doing bounds tests as the bounds will be
     * identical to their child. Skip the test and just head straight to the
     * child node to test against.
     *
     * @param root The shared node to test against
     * @param req flags to compare against for picking
     * @param p1 The first point of the segment
     * @param p2 The second point of the segment
     * @param paths Array of paths place to set the results in
     * @param currentPath Active index in the paths array
     * @param needTransform true if we should calc vworld information
     * @param useGeom true if this should check the results based on geometry
     *    rather than just bounds
     */
    private int pickAllRay(SinglePickTarget root,
                           PickRequest req,
                           float[] p1,
                           float[] p2,
                           ArrayList<SceneGraphPath> paths,
                           int currentPath,
                           boolean needTransform,
                           boolean useGeom)
    {
        if(!root.checkPickMask(req.pickType))
            return 0;

        PickTarget child = root.getPickableChild();

        if(child == null)
            return 0;

        resizePath();
        validTransform[lastPathIndex] = false;
        pickPath[lastPathIndex] = root;
        lastPathIndex++;

        int found = 0;

        switch(child.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                found = pickAllRay((GroupPickTarget)child,
                                   req,
                                   p1,
                                   p2,
                                   paths,
                                   currentPath,
                                   needTransform,
                                   useGeom);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                found = pickAllRay((SinglePickTarget)child,
                                   req,
                                   p1,
                                   p2,
                                   paths,
                                   currentPath,
                                   needTransform,
                                   useGeom);
                break;

            case PickTarget.LEAF_PICK_TYPE:
                found = pickAllRay((LeafPickTarget)child,
                                   req,
                                   p1,
                                   p2,
                                   paths,
                                   currentPath,
                                   needTransform,
                                   useGeom,
                                   false);
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                found = pickAllRay((CustomPickTarget)child,
                                   req,
                                   p1,
                                   p2,
                                   paths,
                                   currentPath,
                                   needTransform,
                                   useGeom);
                break;
        }

        lastPathIndex--;
        pickPath[lastPathIndex] = null;
        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param root The now that is acting as the local root to work through
     * @param req flags to compare against for picking
     * @param p1 The first point of the segment
     * @param p2 The second point of the segment
     * @param paths Array of paths place to set the results in
     * @param currentPath Active index in the paths array
     * @param needTransform True if the local to v-world transform needs
     *    calculating
     * @param useGeom true if this should check the results based on geometry
     *    rather than just bounds
     */
    private int pickAllRay(CustomPickTarget root,
                           PickRequest req,
                           float[] p1,
                           float[] p2,
                           ArrayList<SceneGraphPath> paths,
                           int currentPath,
                           boolean needTransform,
                           boolean useGeom)
    {
        if(!root.checkPickMask(req.pickType))
            return 0;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionRay(p1, p2))
            return 0;

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickAllRay((GroupPickTarget)target_node,
                                  req,
                                  p1,
                                  p2,
                                  paths,
                                  currentPath,
                                  needTransform,
                                  useGeom);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickAllRay((SinglePickTarget)target_node,
                                  req,
                                  p1,
                                  p2,
                                  paths,
                                  currentPath,
                                  needTransform,
                                  useGeom);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickAllRay((LeafPickTarget)target_node,
                                  req,
                                  p1,
                                  p2,
                                  paths,
                                  currentPath,
                                  needTransform,
                                  useGeom,
                                  false);
            }
            else if(target_node instanceof CustomPickTarget)
            {
                return pickAllRay((CustomPickTarget)target_node,
                                  req,
                                  p1,
                                  p2,
                                  paths,
                                  currentPath,
                                  needTransform,
                                  useGeom);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        int found = 0;
        if(pickCustom(root, req))
        {
            int num_kids = pickInstructions.numChildren;
            float p1_x = p1[0];
            float p1_y = p1[1];
            float p1_z = p1[2];

            float p2_x = p2[0];
            float p2_y = p2[1];
            float p2_z = p2[2];

            // reset the transform at the top of the stack
            resizePath();
            if(pickInstructions.hasTransform)
            {
                transformPath[lastPathIndex].set(pickInstructions.localTransform);

                matrixUtils.inverse(pickInstructions.localTransform,
                                    invertedMatrix);
                transform(invertedMatrix, p1);
                transformNormal(invertedMatrix, p2);

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = pickInstructions.children.clone();

            for(int i = 0; i < num_kids; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found += pickAllRay((GroupPickTarget)kids[i],
                                            req,
                                            p1,
                                            p2,
                                            paths,
                                            currentPath + found,
                                            needTransform,
                                            useGeom);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found += pickAllRay((SinglePickTarget)kids[i],
                                            req,
                                            p1,
                                            p2,
                                            paths,
                                            currentPath + found,
                                            needTransform,
                                            useGeom);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found += pickAllRay((LeafPickTarget)kids[i],
                                            req,
                                            p1,
                                            p2,
                                            paths,
                                            currentPath + found,
                                            needTransform,
                                            useGeom,
                                            false);
                        break;
                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            p1[0] = p1_x;
            p1[1] = p1_y;
            p1[2] = p1_z;

            p2[0] = p2_x;
            p2[1] = p2_y;
            p2[2] = p2_z;
        }

        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param p1 The first point of the segment
     * @param p2 The second point of the segment
     * @param req flags to compare against for picking
     * @param paths A place to set the results in
     * @param needTransform True if the local to v-world transform needs
     *    calculating
     * @param useGeom true if this should check the results based on geometry
     *    rather than just bounds
     */
    private int pickAllRay(LeafPickTarget geom,
                           PickRequest req,
                           float[] p1,
                           float[] p2,
                           ArrayList<SceneGraphPath> paths,
                           int currentPath,
                           boolean needTransform,
                           boolean useGeom,
                           boolean needClosest)
    {
        if(!geom.checkPickMask(req.pickType))
            return 0;

        int found = 0;
        BoundingVolume bounds = geom.getPickableBounds();

        if(bounds.checkIntersectionRay(p1, p2))
        {
            if(useGeom)
            {
                if(geom.pickLineRay(p1, p2, needClosest, vertexPickData, 0))
                {
                    resizePath();
                    pickPath[lastPathIndex] = geom;
                    validTransform[lastPathIndex] = false;
                    lastPathIndex++;
                    SceneGraphPath p;

                    if(currentPath >= paths.size())
                    {
                        p = new SceneGraphPath();
                        paths.add(p);
                    }
                    else
                        p = (SceneGraphPath)paths.get(currentPath);

                    if(needTransform)
                        buildVWorldTransform();
                    else
                        vworldMatrix.setIdentity();


                    p.updatePath(pickPath,
                                 lastPathIndex,
                                 vworldMatrix,
                                 invertedMatrix);

                    found = 1;
                    lastPathIndex--;
                }
            }
            else
            {
                resizePath();
                pickPath[lastPathIndex] = geom;
                validTransform[lastPathIndex] = false;
                lastPathIndex++;

                SceneGraphPath p;

                if(currentPath >= paths.size())
                {
                    p = new SceneGraphPath();
                    paths.add(p);
                }
                else
                    p = (SceneGraphPath)paths.get(currentPath);

                if(needTransform)
                    buildVWorldTransform();
                else
                    vworldMatrix.setIdentity();

                p.updatePath(pickPath,
                             lastPathIndex,
                             vworldMatrix,
                             invertedMatrix);

                found = 1;
                lastPathIndex--;
            }
        }

        return found;
    }

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given ray. Return the first
     * found.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     */
    private void pickSingleRaySorted(PickTarget root, PickRequest req)
    {
        SceneGraphPath output_path = null;

        if(req.foundPaths instanceof SceneGraphPath)
            output_path = (SceneGraphPath)req.foundPaths;
        else
        {
            output_path = new SceneGraphPath();
            req.foundPaths = output_path;
        }

        boolean found = false;

        PickTarget target_node = root;
        BoundingVolume bounds = target_node.getPickableBounds();

        if(bounds == null ||
           !bounds.checkIntersectionRay(req.origin, req.destination))
        {
            req.pickCount = 0;
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
                target_node = (PickTarget)geom;
            else
            {
                req.pickCount = 0;
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

        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    // reset the transform at the top of the stack
                    if(root instanceof TransformPickTarget)
                    {
                        Matrix4d tx = transformPath[0];

                        TransformPickTarget tg = (TransformPickTarget)root;
                        tg.getTransform(tx);
                        tg.getInverseTransform(invertedMatrix);
                        transform(invertedMatrix, start);
                        transform(invertedMatrix, end);
                    }
                    else
                        transformPath[0].setIdentity();

                    validTransform[0] = true;
                    pickPath[0] = g;
                    PickTarget[] kids = g.getPickableChildren();

                    // now that the setup is done, walk down the tree.
                    for(int i = 0; i < num_kids && !found; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        lastPathIndex = 1;

                        switch(kids[i].getPickTargetType())
                        {
                            case PickTarget.GROUP_PICK_TYPE:
                                found = pickSingleRaySorted((GroupPickTarget)kids[i],
                                                            req,
                                                            start,
                                                            end,
                                                            output_path,
                                                            req.generateVWorldMatrix,
                                                            req.useGeometry);
                                break;

                            case PickTarget.SINGLE_PICK_TYPE:
                                found = pickSingleRaySorted((SinglePickTarget)kids[i],
                                                            req,
                                                            start,
                                                            end,
                                                            output_path,
                                                            req.generateVWorldMatrix,
                                                            req.useGeometry);
                                break;

                            case PickTarget.LEAF_PICK_TYPE:
                                found = pickSingleRay((LeafPickTarget)kids[i],
                                                      req,
                                                      start,
                                                      end,
                                                      output_path,
                                                      req.generateVWorldMatrix,
                                                      req.useGeometry,
                                                      true);
                                break;

                            case PickTarget.CUSTOM_PICK_TYPE:
                                found = pickSingleRaySorted((CustomPickTarget)kids[i],
                                                            req,
                                                            start,
                                                            end,
                                                            output_path,
                                                            req.generateVWorldMatrix,
                                                            req.useGeometry);
                                break;
                        }
                    }
                }
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found = pickSingleRaySorted((SinglePickTarget)target_node,
                                            req,
                                            start,
                                            end,
                                            output_path,
                                            req.generateVWorldMatrix,
                                            req.useGeometry);
                break;

            case PickTarget.LEAF_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found = pickSingleRay((LeafPickTarget)target_node,
                                      req,
                                      start,
                                      end,
                                      output_path,
                                      req.generateVWorldMatrix,
                                      req.useGeometry,
                                      false);
                break;
        }

        req.pickCount = found ? 1 : 0;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param root The now that is acting as the local root to work through
     * @param req flags to compare against for picking
     * @param p1 The first point of the segment
     * @param p2 The second point of the segment
     * @param path A place to set the results in
     * @param needTransform True if the local to v-world transform needs
     *    calculating
     * @param useGeom true if this should check the results based on geometry
     *    rather than just bounds
     */
    private boolean pickSingleRaySorted(GroupPickTarget root,
                                        PickRequest req,
                                        float[] p1,
                                        float[] p2,
                                        SceneGraphPath path,
                                        boolean needTransform,
                                        boolean useGeom)
    {
        if(!root.checkPickMask(req.pickType))
            return false;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionRay(p1, p2))
            return false;

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickSingleRaySorted((GroupPickTarget)target_node,
                                           req,
                                           p1,
                                           p2,
                                           path,
                                           needTransform,
                                           useGeom);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickSingleRaySorted((SinglePickTarget)target_node,
                                           req,
                                           p1,
                                           p2,
                                           path,
                                           needTransform,
                                           useGeom);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickSingleRay((LeafPickTarget)target_node,
                                     req,
                                     p1,
                                     p2,
                                     path,
                                     needTransform,
                                     useGeom,
                                     true);
            }
            else if(target_node instanceof CustomPickTarget)
            {
                return pickSingleRaySorted((CustomPickTarget)target_node,
                                           req,
                                           p1,
                                           p2,
                                           path,
                                           needTransform,
                                           useGeom);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        boolean found = false;
        int num_kids = root.numPickableChildren();

        if(num_kids != 0)
        {
            float p1_x = p1[0];
            float p1_y = p1[1];
            float p1_z = p1[2];

            float p2_x = p2[0];
            float p2_y = p2[1];
            float p2_z = p2[2];

            // reset the transform at the top of the stack
            if(root instanceof TransformPickTarget)
            {
                Matrix4d tx = transformPath[lastPathIndex];

                TransformPickTarget tg = (TransformPickTarget)root;
                tg.getTransform(tx);
                tg.getInverseTransform(invertedMatrix);
                transform(invertedMatrix, p1);
                transform(invertedMatrix, p2);

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = root.getPickableChildren();

            for(int i = 0; i < num_kids && !found; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found = pickSingleRaySorted((GroupPickTarget)kids[i],
                                                    req,
                                                    p1,
                                                    p2,
                                                    path,
                                                    needTransform,
                                                    useGeom);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found = pickSingleRaySorted((SinglePickTarget)kids[i],
                                                    req,
                                                    p1,
                                                    p2,
                                                    path,
                                                    needTransform,
                                                    useGeom);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found = pickSingleRay((LeafPickTarget)kids[i],
                                              req,
                                              p1,
                                              p2,
                                              path,
                                              needTransform,
                                              useGeom,
                                              true);
                        break;

                    case PickTarget.CUSTOM_PICK_TYPE:
                        found = pickSingleRaySorted((CustomPickTarget)kids[i],
                                                    req,
                                                    p1,
                                                    p2,
                                                    path,
                                                    needTransform,
                                                    useGeom);
                        break;

                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            p1[0] = p1_x;
            p1[1] = p1_y;
            p1[2] = p1_z;

            p2[0] = p2_x;
            p2[1] = p2_y;
            p2[2] = p2_z;
        }

        return found;
    }

    /**
     * Recurse a single-child shared node tree looking for
     * intersections with a single point. Shared nodes are different to normal
     * because we don't bother doing bounds tests as the bounds will be
     * identical to their child. Skip the test and just head straight to the
     * child node to test against.
     *
     * @param root The shared node to test against
     * @param req flags to compare against for picking
     * @param p1 The first point of the segment
     * @param p2 The second point of the segment
     * @param path The place to put the results in
     * @param needTransform true if we should calc vworld information
     * @param useGeom true if this should check the results based on geometry
     *    rather than just bounds
     */
    private boolean pickSingleRaySorted(SinglePickTarget root,
                                        PickRequest req,
                                        float[] p1,
                                        float[] p2,
                                        SceneGraphPath path,
                                        boolean needTransform,
                                        boolean useGeom)
    {
        if(!root.checkPickMask(req.pickType))
            return false;

        resizePath();
        validTransform[lastPathIndex] = false;
        pickPath[lastPathIndex] = root;
        lastPathIndex++;

        PickTarget child = root.getPickableChild();
        if(child == null)
            return false;

        boolean found = false;

        switch(child.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                found = pickSingleRaySorted((GroupPickTarget)child,
                                            req,
                                            p1,
                                            p2,
                                            path,
                                            needTransform,
                                            useGeom);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                found = pickSingleRaySorted((SinglePickTarget)child,
                                            req,
                                            p1,
                                            p2,
                                            path,
                                            needTransform,
                                            useGeom);
                break;

            case PickTarget.LEAF_PICK_TYPE:
                found = pickSingleRay((LeafPickTarget)child,
                                      req,
                                      p1,
                                      p2,
                                      path,
                                      needTransform,
                                      useGeom,
                                      true);

                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                found = pickSingleRaySorted((CustomPickTarget)child,
                                            req,
                                            p1,
                                            p2,
                                            path,
                                            needTransform,
                                            useGeom);
                break;

        }

        lastPathIndex--;
        pickPath[lastPathIndex] = null;
        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param root The now that is acting as the local root to work through
     * @param req flags to compare against for picking
     * @param p1 The first point of the segment
     * @param p2 The second point of the segment
     * @param path A place to set the results in
     * @param needTransform True if the local to v-world transform needs
     *    calculating
     * @param useGeom true if this should check the results based on geometry
     *    rather than just bounds
     */
    private boolean pickSingleRaySorted(CustomPickTarget root,
                                        PickRequest req,
                                        float[] p1,
                                        float[] p2,
                                        SceneGraphPath path,
                                        boolean needTransform,
                                        boolean useGeom)
    {
        if(!root.checkPickMask(req.pickType))
            return false;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionRay(p1, p2))
            return false;

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickSingleRaySorted((GroupPickTarget)target_node,
                                           req,
                                           p1,
                                           p2,
                                           path,
                                           needTransform,
                                           useGeom);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickSingleRaySorted((SinglePickTarget)target_node,
                                           req,
                                           p1,
                                           p2,
                                           path,
                                           needTransform,
                                           useGeom);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickSingleRay((LeafPickTarget)target_node,
                                     req,
                                     p1,
                                     p2,
                                     path,
                                     needTransform,
                                     useGeom,
                                     true);
            }
            else if(target_node instanceof CustomPickTarget)
            {
                return pickSingleRaySorted((CustomPickTarget)target_node,
                                           req,
                                           p1,
                                           p2,
                                           path,
                                           needTransform,
                                           useGeom);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        boolean found = false;
        if(pickCustom(root, req))
        {
            int num_kids = pickInstructions.numChildren;

            float p1_x = p1[0];
            float p1_y = p1[1];
            float p1_z = p1[2];

            float p2_x = p2[0];
            float p2_y = p2[1];
            float p2_z = p2[2];

            // reset the transform at the top of the stack
            if(pickInstructions.hasTransform)
            {
                transformPath[lastPathIndex].set(pickInstructions.localTransform);

                matrixUtils.inverse(pickInstructions.localTransform,
                                    invertedMatrix);
                transform(invertedMatrix, p1);
                transform(invertedMatrix, p2);

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = pickInstructions.children.clone();

            for(int i = 0; i < num_kids && !found; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found = pickSingleRaySorted((GroupPickTarget)kids[i],
                                                    req,
                                                    p1,
                                                    p2,
                                                    path,
                                                    needTransform,
                                                    useGeom);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found = pickSingleRaySorted((SinglePickTarget)kids[i],
                                                    req,
                                                    p1,
                                                    p2,
                                                    path,
                                                    needTransform,
                                                    useGeom);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found = pickSingleRay((LeafPickTarget)kids[i],
                                              req,
                                              p1,
                                              p2,
                                              path,
                                              needTransform,
                                              useGeom,
                                              true);
                        break;

                    case PickTarget.CUSTOM_PICK_TYPE:
                        found = pickSingleRaySorted((CustomPickTarget)kids[i],
                                                    req,
                                                    p1,
                                                    p2,
                                                    path,
                                                    needTransform,
                                                    useGeom);
                        break;

                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            p1[0] = p1_x;
            p1[1] = p1_y;
            p1[2] = p1_z;

            p2[0] = p2_x;
            p2[1] = p2_y;
            p2[2] = p2_z;
        }

        return found;
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

            default:
				I18nManager intl_mgr = I18nManager.getManager();
				String msg_pattern = intl_mgr.getString(NO_SORT_TYPE_PROP);

				Locale lcl = intl_mgr.getFoundLocale();

				NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

				Object[] msg_args = { req.pickSortType };
				Format[] fmts = { n_fmt };
				MessageFormat msg_fmt =
					new MessageFormat(msg_pattern, lcl);
				msg_fmt.setFormats(fmts);
				String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
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
        SceneGraphPath output_path = null;

        if(req.foundPaths instanceof SceneGraphPath)
            output_path = (SceneGraphPath)req.foundPaths;
        else
        {
            output_path = new SceneGraphPath();
            req.foundPaths = output_path;
        }

        boolean found = false;

        PickTarget target_node = root;
        BoundingVolume bounds = target_node.getPickableBounds();

        float x = req.origin[0] - req.destination[0];
        float y = req.origin[1] - req.destination[1];
        float z = req.origin[2] - req.destination[2];

        float height = (float)Math.sqrt(x * x + y * y + z * z);

        if(height == 0)
        {
            req.pickCount = 0;
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

        if(bounds == null ||
           !bounds.checkIntersectionCylinder(start, end, radius, height))
        {
            req.pickCount = 0;
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
                target_node = (PickTarget)geom;
            else
            {
                req.pickCount = 0;
                return;
            }
        }

        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    // reset the transform at the top of the stack
                    if(root instanceof TransformPickTarget)
                    {
                        Matrix4d tx = transformPath[0];

                        TransformPickTarget tg = (TransformPickTarget)root;
                        tg.getTransform(tx);
                        tg.getInverseTransform(invertedMatrix);
                        transform(invertedMatrix, start);
                        transformNormal(invertedMatrix, end);

                        // need to scale the radius and height as well.
                        float scale = (float)matrixUtils.getUniformScale(invertedMatrix);
                        radius *= scale;
                        height *= scale;
                    }
                    else
                        transformPath[0].setIdentity();

                    validTransform[0] = true;
                    pickPath[0] = g;
                    PickTarget[] kids = g.getPickableChildren();

                    // now that the setup is done, walk down the tree.
                    for(int i = 0; i < num_kids && !found; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        lastPathIndex = 1;

                        switch(kids[i].getPickTargetType())
                        {
                            case PickTarget.GROUP_PICK_TYPE:
                                found = pickSingleCylinder((GroupPickTarget)kids[i],
                                                           req,
                                                           start,
                                                           end,
                                                           radius,
                                                           height,
                                                           output_path,
                                                           req.generateVWorldMatrix);
                                break;

                            case PickTarget.SINGLE_PICK_TYPE:
                                found = pickSingleCylinder((SinglePickTarget)kids[i],
                                                           req,
                                                           start,
                                                           end,
                                                           radius,
                                                           height,
                                                           output_path,
                                                           req.generateVWorldMatrix);
                                break;

                            case PickTarget.LEAF_PICK_TYPE:
                                found = pickSingleCylinder((LeafPickTarget)kids[i],
                                                           req,
                                                           start,
                                                           end,
                                                           radius,
                                                           height,
                                                           output_path,
                                                           req.generateVWorldMatrix);
                                break;

                            case PickTarget.CUSTOM_PICK_TYPE:
                                found = pickSingleCylinder((CustomPickTarget)kids[i],
                                                           req,
                                                           start,
                                                           end,
                                                           radius,
                                                           height,
                                                           output_path,
                                                           req.generateVWorldMatrix);
                                break;
                        }
                    }
                }
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found = pickSingleCylinder((SinglePickTarget)target_node,
                                           req,
                                           start,
                                           end,
                                           radius,
                                           height,
                                           output_path,
                                           req.generateVWorldMatrix);
                break;

            case PickTarget.LEAF_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found = pickSingleCylinder((LeafPickTarget)target_node,
                                           req,
                                           start,
                                           end,
                                           radius,
                                           height,
                                           output_path,
                                           req.generateVWorldMatrix);
                break;
        }

        req.pickCount = found ? 1 : 0;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param root The group node to test against and recurse into
     * @param req flags to compare against for picking
     * @param center The center of the axis of the cylinder to pick against
     * @param axis Vector describing the axis of the cylinder to pick against
     * @param radius The height of the cylinder to pick against
     * @param height The height of the cylinder to pick against
     * @param path The place to put the results in
     * @param needTransform true if we should calc vworld information
     */
    private boolean pickSingleCylinder(GroupPickTarget root,
                                       PickRequest req,
                                       float[] center,
                                       float[] axis,
                                       float radius,
                                       float height,
                                       SceneGraphPath path,
                                       boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return false;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionCylinder(center, axis, radius, height))
            return false;

        // Bounding check. If it has it, recurse on the bounding geometry
        // rather than the real geometry.
        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickSingleCylinder((GroupPickTarget)target_node,
                                          req,
                                          center,
                                          axis,
                                          radius,
                                          height,
                                          path,
                                          needTransform);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickSingleCylinder((SinglePickTarget)target_node,
                                          req,
                                          center,
                                          axis,
                                          radius,
                                          height,
                                          path,
                                          needTransform);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickSingleCylinder((LeafPickTarget)target_node,
                                          req,
                                          center,
                                          axis,
                                          radius,
                                          height,
                                          path,
                                          needTransform);
            }
            else if(target_node instanceof CustomPickTarget)
            {
                return pickSingleCylinder((CustomPickTarget)target_node,
                                          req,
                                          center,
                                          axis,
                                          radius,
                                          height,
                                          path,
                                          needTransform);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        boolean found = false;
        int num_kids = root.numPickableChildren();

        if(num_kids != 0)
        {
            float center_x = center[0];
            float center_y = center[1];
            float center_z = center[2];

            float axis_x = axis[0];
            float axis_y = axis[1];
            float axis_z = axis[2];

            // reset the transform at the top of the stack
            if(root instanceof TransformPickTarget)
            {
                Matrix4d tx = transformPath[lastPathIndex];

                TransformPickTarget tg = (TransformPickTarget)root;
                tg.getTransform(tx);
                tg.getInverseTransform(invertedMatrix);
                transform(invertedMatrix, center);
                transformNormal(invertedMatrix, axis);

                // need to scale the radius and height as well.
                float scale = (float)matrixUtils.getUniformScale(invertedMatrix);
                radius *= scale;
                height *= scale;

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = root.getPickableChildren();

            for(int i = 0; i < num_kids && !found; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found = pickSingleCylinder((GroupPickTarget)kids[i],
                                                   req,
                                                   center,
                                                   axis,
                                                   radius,
                                                   height,
                                                   path,
                                                   needTransform);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found = pickSingleCylinder((SinglePickTarget)kids[i],
                                                   req,
                                                   center,
                                                   axis,
                                                   radius,
                                                   height,
                                                   path,
                                                   needTransform);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found = pickSingleCylinder((LeafPickTarget)kids[i],
                                                   req,
                                                   center,
                                                   axis,
                                                   radius,
                                                   height,
                                                   path,
                                                   needTransform);
                        break;
                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            center[0] = center_x;
            center[1] = center_y;
            center[2] = center_z;

            axis[0] = axis_x;
            axis[1] = axis_y;
            axis[2] = axis_z;
        }

        return found;
    }

    /**
     * Recurse a single-child shared node tree looking for
     * intersections with a single point. Shared nodes are different to normal
     * because we don't bother doing bounds tests as the bounds will be
     * identical to their child. Skip the test and just head straight to the
     * child node to test against.
     *
     * @param root The shared node to test against
     * @param req flags to compare against for picking
     * @param path The place to put the results in
     * @param needTransform true if we should calc vworld information
     */
    private boolean pickSingleCylinder(SinglePickTarget root,
                                       PickRequest req,
                                       float[] center,
                                       float[] axis,
                                       float radius,
                                       float height,
                                       SceneGraphPath path,
                                       boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return false;

        resizePath();
        validTransform[lastPathIndex] = false;
        pickPath[lastPathIndex] = root;
        lastPathIndex++;

        PickTarget child = root.getPickableChild();
        if(child == null)
            return false;

        boolean found = false;

        switch(child.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                found = pickSingleCylinder((GroupPickTarget)child,
                                           req,
                                           center,
                                           axis,
                                           radius,
                                           height,
                                           path,
                                           needTransform);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                found = pickSingleCylinder((SinglePickTarget)child,
                                           req,
                                           center,
                                           axis,
                                           radius,
                                           height,
                                           path,
                                           needTransform);
                break;

            case PickTarget.LEAF_PICK_TYPE:
                found = pickSingleCylinder((LeafPickTarget)child,
                                           req,
                                           center,
                                           axis,
                                           radius,
                                           height,
                                           path,
                                           needTransform);

                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                found = pickSingleCylinder((CustomPickTarget)child,
                                           req,
                                           center,
                                           axis,
                                           radius,
                                           height,
                                           path,
                                           needTransform);
                break;

        }

        lastPathIndex--;
        pickPath[lastPathIndex] = null;
        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param root The group node to test against and recurse into
     * @param req flags to compare against for picking
     * @param center The center of the axis of the cylinder to pick against
     * @param axis Vector describing the axis of the cylinder to pick against
     * @param radius The height of the cylinder to pick against
     * @param height The height of the cylinder to pick against
     * @param path The place to put the results in
     * @param needTransform true if we should calc vworld information
     */
    private boolean pickSingleCylinder(CustomPickTarget root,
                                       PickRequest req,
                                       float[] center,
                                       float[] axis,
                                       float radius,
                                       float height,
                                       SceneGraphPath path,
                                       boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return false;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionCylinder(center, axis, radius, height))
            return false;

        // Bounding check. If it has it, recurse on the bounding geometry
        // rather than the real geometry.
        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickSingleCylinder((GroupPickTarget)target_node,
                                          req,
                                          center,
                                          axis,
                                          radius,
                                          height,
                                          path,
                                          needTransform);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickSingleCylinder((SinglePickTarget)target_node,
                                          req,
                                          center,
                                          axis,
                                          radius,
                                          height,
                                          path,
                                          needTransform);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickSingleCylinder((LeafPickTarget)target_node,
                                          req,
                                          center,
                                          axis,
                                          radius,
                                          height,
                                          path,
                                          needTransform);
            }
            else if(target_node instanceof CustomPickTarget)
            {
                return pickSingleCylinder((CustomPickTarget)target_node,
                                          req,
                                          center,
                                          axis,
                                          radius,
                                          height,
                                          path,
                                          needTransform);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        boolean found = false;

        if(pickCustom(root, req))
        {
            int num_kids = pickInstructions.numChildren;

            float center_x = center[0];
            float center_y = center[1];
            float center_z = center[2];

            float axis_x = axis[0];
            float axis_y = axis[1];
            float axis_z = axis[2];

            // reset the transform at the top of the stack
            if(pickInstructions.hasTransform)
            {
                transformPath[lastPathIndex].set(pickInstructions.localTransform);

                matrixUtils.inverse(pickInstructions.localTransform,
                                    invertedMatrix);
                transform(invertedMatrix, center);
                transformNormal(invertedMatrix, axis);

                // need to scale the radius and height as well.
                float scale = (float)matrixUtils.getUniformScale(invertedMatrix);
                radius *= scale;
                height *= scale;

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = pickInstructions.children.clone();

            for(int i = 0; i < num_kids && !found; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found = pickSingleCylinder((GroupPickTarget)kids[i],
                                                   req,
                                                   center,
                                                   axis,
                                                   radius,
                                                   height,
                                                   path,
                                                   needTransform);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found = pickSingleCylinder((SinglePickTarget)kids[i],
                                                   req,
                                                   center,
                                                   axis,
                                                   radius,
                                                   height,
                                                   path,
                                                   needTransform);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found = pickSingleCylinder((LeafPickTarget)kids[i],
                                                   req,
                                                   center,
                                                   axis,
                                                   radius,
                                                   height,
                                                   path,
                                                   needTransform);
                        break;
                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            center[0] = center_x;
            center[1] = center_y;
            center[2] = center_z;

            axis[0] = axis_x;
            axis[1] = axis_y;
            axis[2] = axis_z;
        }

        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param geom The geom node to test against
     * @param req flags to compare against for picking
     * @param center The center of the axis of the cylinder to pick against
     * @param axis Vector describing the axis of the cylinder to pick against
     * @param radius The height of the cylinder to pick against
     * @param height The height of the cylinder to pick against
     * @param path A place to set the results in
     * @param needTransform True if the local to v-world transform needs
     *    calculating
     */
    private boolean pickSingleCylinder(LeafPickTarget geom,
                                       PickRequest req,
                                       float[] center,
                                       float[] axis,
                                       float radius,
                                       float height,
                                       SceneGraphPath path,
                                       boolean needTransform)
    {
        if(!geom.checkPickMask(req.pickType))
            return false;

        boolean found = false;
        BoundingVolume bounds = geom.getPickableBounds();

        if(bounds.checkIntersectionCylinder(center, axis, radius, height))
        {
            resizePath();
            pickPath[lastPathIndex] = geom;
            validTransform[lastPathIndex] = false;
            lastPathIndex++;

            if(needTransform)
                buildVWorldTransform();
            else
                vworldMatrix.setIdentity();

            path.updatePath(pickPath,
                            lastPathIndex,
                            vworldMatrix,
                            invertedMatrix);

            found = true;
            lastPathIndex--;
        }

        return found;
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
        int found = 0;
        ArrayList<SceneGraphPath> output_list = null;

        if(req.foundPaths instanceof ArrayList)
        {
            output_list = (ArrayList<SceneGraphPath>) req.foundPaths;
        }
        else
        {
            output_list = new ArrayList<>();
            req.foundPaths = output_list;
        }

        PickTarget target_node = root;
        BoundingVolume bounds = target_node.getPickableBounds();

        float x = req.origin[0] - req.destination[0];
        float y = req.origin[1] - req.destination[1];
        float z = req.origin[2] - req.destination[2];

        float height = (float)Math.sqrt(x * x + y * y + z * z);

        if(height == 0)
        {
            req.pickCount = 0;
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

        if(bounds == null ||
           !bounds.checkIntersectionCylinder(start, end, radius, height))
        {
            req.pickCount = 0;
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
                target_node = (PickTarget)geom;
            else
            {
                req.pickCount = 0;
                return;
            }
        }

        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {

                    // reset the transform at the top of the stack
                    if(root instanceof TransformPickTarget)
                    {
                        Matrix4d tx = transformPath[0];

                        TransformPickTarget tg = (TransformPickTarget)root;
                        tg.getTransform(tx);
                        tg.getInverseTransform(invertedMatrix);
                        transform(invertedMatrix, start);
                        transformNormal(invertedMatrix, end);

                        // need to scale the radius and height as well.
                        float scale = (float)matrixUtils.getUniformScale(invertedMatrix);
                        radius *= scale;
                        height *= scale;
                    }
                    else
                        transformPath[0].setIdentity();

                    validTransform[0] = true;
                    pickPath[0] = g;
                    PickTarget[] kids = g.getPickableChildren();

                    // now that the setup is done, walk down the tree.
                    for(int i = 0; i < num_kids; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        lastPathIndex = 1;

                        switch(kids[i].getPickTargetType())
                        {
                            case PickTarget.GROUP_PICK_TYPE:
                                found += pickAllCylinder((GroupPickTarget)kids[i],
                                                         req,
                                                         start,
                                                         end,
                                                         radius,
                                                         height,
                                                         output_list,
                                                         found,
                                                         req.generateVWorldMatrix);
                                break;

                            case PickTarget.SINGLE_PICK_TYPE:
                                found += pickAllCylinder((SinglePickTarget)kids[i],
                                                         req,
                                                         start,
                                                         end,
                                                         radius,
                                                         height,
                                                         output_list,
                                                         found,
                                                         req.generateVWorldMatrix);
                                break;

                            case PickTarget.LEAF_PICK_TYPE:
                                found += pickAllCylinder((LeafPickTarget)kids[i],
                                                         req,
                                                         start,
                                                         end,
                                                         radius,
                                                         height,
                                                         output_list,
                                                         found,
                                                         req.generateVWorldMatrix);
                                break;

                            case PickTarget.CUSTOM_PICK_TYPE:
                                found += pickAllCylinder((CustomPickTarget)kids[i],
                                                         req,
                                                         start,
                                                         end,
                                                         radius,
                                                         height,
                                                         output_list,
                                                         found,
                                                         req.generateVWorldMatrix);
                                break;
                        }
                    }

                    req.pickCount = found;
                }
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found += pickAllCylinder((SinglePickTarget)target_node,
                                         req,
                                         start,
                                         end,
                                         radius,
                                         height,
                                         output_list,
                                         0,
                                         req.generateVWorldMatrix);

                req.pickCount = found;
                break;

            case PickTarget.LEAF_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found += pickAllCylinder((LeafPickTarget)target_node,
                                         req,
                                         start,
                                         end,
                                         radius,
                                         height,
                                         output_list,
                                         0,
                                         req.generateVWorldMatrix);

                req.pickCount = found;
                break;
        }
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param root The group node to test against and descend into
     * @param req flags to compare against for picking
     * @param center The center of the axis of the cylinder to pick against
     * @param axis Vector describing the axis of the cylinder to pick against
     * @param radius The height of the cylinder to pick against
     * @param height The height of the cylinder to pick against
     * @param needTransform true if we should calc vworld information
     */
    private int pickAllCylinder(GroupPickTarget root,
                                PickRequest req,
                                float[] center,
                                float[] axis,
                                float radius,
                                float height,
                                ArrayList<SceneGraphPath> paths,
                                int currentPath,
                                boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return 0;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionCylinder(center, axis, radius, height))
            return 0;

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickAllCylinder((GroupPickTarget)target_node,
                                       req,
                                       center,
                                       axis,
                                       radius,
                                       height,
                                       paths,
                                       currentPath,
                                       needTransform);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickAllCylinder((SinglePickTarget)target_node,
                                       req,
                                       center,
                                       axis,
                                       radius,
                                       height,
                                       paths,
                                       currentPath,
                                       needTransform);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickAllCylinder((LeafPickTarget)target_node,
                                       req,
                                       center,
                                       axis,
                                       radius,
                                       height,
                                       paths,
                                       currentPath,
                                       needTransform);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        int found = 0;
        int num_kids = root.numPickableChildren();

        if(num_kids != 0)
        {
            float center_x = center[0];
            float center_y = center[1];
            float center_z = center[2];

            float axis_x = axis[0];
            float axis_y = axis[1];
            float axis_z = axis[2];

            // reset the transform at the top of the stack
            if(root instanceof TransformPickTarget)
            {
                Matrix4d tx = transformPath[lastPathIndex];

                TransformPickTarget tg = (TransformPickTarget)root;
                tg.getTransform(tx);
                tg.getInverseTransform(invertedMatrix);
                transform(invertedMatrix, center);
                transformNormal(invertedMatrix, axis);

                // need to scale the radius and height as well.
                float scale = (float)matrixUtils.getUniformScale(invertedMatrix);
                radius *= scale;
                height *= scale;

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = root.getPickableChildren();

            for(int i = 0; i < num_kids; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found += pickAllCylinder((GroupPickTarget)kids[i],
                                                 req,
                                                 center,
                                                 axis,
                                                 radius,
                                                 height,
                                                 paths,
                                                 currentPath + found,
                                                 needTransform);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found += pickAllCylinder((SinglePickTarget)kids[i],
                                                 req,
                                                 center,
                                                 axis,
                                                 radius,
                                                 height,
                                                 paths,
                                                 currentPath + found,
                                                 needTransform);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found += pickAllCylinder((LeafPickTarget)kids[i],
                                                 req,
                                                 center,
                                                 axis,
                                                 radius,
                                                 height,
                                                 paths,
                                                 currentPath + found,
                                                 needTransform);
                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            center[0] = center_x;
            center[1] = center_y;
            center[2] = center_z;

            axis[0] = axis_x;
            axis[1] = axis_y;
            axis[2] = axis_z;
        }

        return found;
    }

    /**
     * Recurse a single-child shared node tree looking for
     * intersections with a single point. Shared nodes are different to normal
     * because we don't bother doing bounds tests as the bounds will be
     * identical to their child. Skip the test and just head straight to the
     * child node to test against.
     *
     * @param root The shared node to test against
     * @param req flags to compare against for picking
     * @param center The center of the axis of the cylinder to pick against
     * @param axis Vector describing the axis of the cylinder to pick against
     * @param radius The height of the cylinder to pick against
     * @param height The height of the cylinder to pick against
     * @param needTransform true if we should calc vworld information
     */
    private int pickAllCylinder(SinglePickTarget root,
                                PickRequest req,
                                float[] center,
                                float[] axis,
                                float radius,
                                float height,
                                ArrayList<SceneGraphPath> paths,
                                int currentPath,
                                boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return 0;

        resizePath();
        validTransform[lastPathIndex] = false;
        pickPath[lastPathIndex] = root;
        lastPathIndex++;

        PickTarget child = root.getPickableChild();
        if(child == null)
            return 0;

        int found = 0;

        switch(child.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                found = pickAllCylinder((GroupPickTarget)child,
                                        req,
                                        center,
                                        axis,
                                        radius,
                                        height,
                                        paths,
                                        currentPath,
                                        needTransform);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                found = pickAllCylinder((SinglePickTarget)child,
                                        req,
                                        center,
                                        axis,
                                        radius,
                                        height,
                                        paths,
                                        currentPath,
                                        needTransform);
                break;

            case PickTarget.LEAF_PICK_TYPE:
                found = pickAllCylinder((LeafPickTarget)child,
                                        req,
                                        center,
                                        axis,
                                        radius,
                                        height,
                                        paths,
                                        currentPath,
                                        needTransform);
                break;
        }

        lastPathIndex--;
        pickPath[lastPathIndex] = null;
        return found;
    }

    /**
     * Recurse the tree looking for intersections with anything that fits into the cylinder.
     *
     * @param root The group node to test against and descend into
     * @param req flags to compare against for picking
     * @param center The center of the axis of the cylinder to pick against
     * @param axis Vector describing the axis of the cylinder to pick against
     * @param radius The height of the cylinder to pick against
     * @param height The height of the cylinder to pick against
     * @param paths The place to put the results in
     * @param needTransform true if we should calc vworld information
     */
    private int pickAllCylinder(CustomPickTarget root,
                                PickRequest req,
                                float[] center,
                                float[] axis,
                                float radius,
                                float height,
                                ArrayList<SceneGraphPath> paths,
                                int currentPath,
                                boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return 0;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionCylinder(center, axis, radius, height))
            return 0;

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickAllCylinder((GroupPickTarget)target_node,
                                       req,
                                       center,
                                       axis,
                                       radius,
                                       height,
                                       paths,
                                       currentPath,
                                       needTransform);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickAllCylinder((SinglePickTarget)target_node,
                                       req,
                                       center,
                                       axis,
                                       radius,
                                       height,
                                       paths,
                                       currentPath,
                                       needTransform);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickAllCylinder((LeafPickTarget)target_node,
                                       req,
                                       center,
                                       axis,
                                       radius,
                                       height,
                                       paths,
                                       currentPath,
                                       needTransform);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        int found = 0;
        if(pickCustom(root, req))
        {
            int num_kids = pickInstructions.numChildren;

            float center_x = center[0];
            float center_y = center[1];
            float center_z = center[2];

            float axis_x = axis[0];
            float axis_y = axis[1];
            float axis_z = axis[2];

            // reset the transform at the top of the stack
            if(pickInstructions.hasTransform)
            {
                transformPath[lastPathIndex].set(pickInstructions.localTransform);

                matrixUtils.inverse(pickInstructions.localTransform,
                                    invertedMatrix);
                transform(invertedMatrix, center);
                transformNormal(invertedMatrix, axis);

                // need to scale the radius and height as well.
                float scale = (float)matrixUtils.getUniformScale(invertedMatrix);
                radius *= scale;
                height *= scale;

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = pickInstructions.children.clone();

            for(int i = 0; i < num_kids; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found += pickAllCylinder((GroupPickTarget)kids[i],
                                                 req,
                                                 center,
                                                 axis,
                                                 radius,
                                                 height,
                                                 paths,
                                                 currentPath + found,
                                                 needTransform);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found += pickAllCylinder((SinglePickTarget)kids[i],
                                                 req,
                                                 center,
                                                 axis,
                                                 radius,
                                                 height,
                                                 paths,
                                                 currentPath + found,
                                                 needTransform);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found += pickAllCylinder((LeafPickTarget)kids[i],
                                                 req,
                                                 center,
                                                 axis,
                                                 radius,
                                                 height,
                                                 paths,
                                                 currentPath + found,
                                                 needTransform);
                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            center[0] = center_x;
            center[1] = center_y;
            center[2] = center_z;

            axis[0] = axis_x;
            axis[1] = axis_y;
            axis[2] = axis_z;
        }

        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param geom The geom node to test against
     * @param req flags to compare against for picking
     * @param paths A place to set the results in
     * @param needTransform True if the minal to v-world transform needs
     *    calculating
     */
    private int pickAllCylinder(LeafPickTarget geom,
                                PickRequest req,
                                float[] center,
                                float[] axis,
                                float radius,
                                float height,
                                ArrayList<SceneGraphPath> paths,
                                int currentPath,
                                boolean needTransform)
    {
        if(!geom.checkPickMask(req.pickType))
            return 0;

        int found = 0;
        BoundingVolume bounds = geom.getPickableBounds();

        if(bounds.checkIntersectionCylinder(center, axis, radius, height))
        {
            resizePath();
            pickPath[lastPathIndex] = geom;
            validTransform[lastPathIndex] = false;
            lastPathIndex++;

            SceneGraphPath p;

            if(currentPath >= paths.size())
            {
                p = new SceneGraphPath();
                paths.add(p);
            }
            else
                p = paths.get(currentPath);

            if(needTransform)
                buildVWorldTransform();
            else
                vworldMatrix.setIdentity();


            p.updatePath(pickPath,
                         lastPathIndex,
                         vworldMatrix,
                         invertedMatrix);

            found = 1;
            lastPathIndex--;
        }

        return found;
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

            default:
				I18nManager intl_mgr = I18nManager.getManager();
				String msg_pattern = intl_mgr.getString(NO_SORT_TYPE_PROP);

				Locale lcl = intl_mgr.getFoundLocale();

				NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

				Object[] msg_args = { req.pickSortType };
				Format[] fmts = { n_fmt };
				MessageFormat msg_fmt =
					new MessageFormat(msg_pattern, lcl);
				msg_fmt.setFormats(fmts);
				String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
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
        SceneGraphPath output_path = null;

        if(req.foundPaths instanceof SceneGraphPath)
            output_path = (SceneGraphPath)req.foundPaths;
        else
        {
            output_path = new SceneGraphPath();
            req.foundPaths = output_path;
        }

        boolean found = false;

        PickTarget target_node = root;
        BoundingVolume bounds = target_node.getPickableBounds();
        float angle = req.additionalData;

        if(bounds == null ||
           !bounds.checkIntersectionCone(req.origin,
            req.destination,
            req.additionalData))
        {
            req.pickCount = 0;
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
                target_node = (PickTarget)geom;
            else
            {
                req.pickCount = 0;
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

        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    // reset the transform at the top of the stack
                    if(root instanceof TransformPickTarget)
                    {
                        Matrix4d tx = transformPath[0];

                        TransformPickTarget tg = (TransformPickTarget)root;
                        tg.getTransform(tx);
                        tg.getInverseTransform(invertedMatrix);
                        transform(invertedMatrix, start);
                        transformNormal(invertedMatrix, end);

                        // Angle does not need to be changed.
                    }
                    else
                        transformPath[0].setIdentity();

                    validTransform[0] = true;
                    pickPath[0] = g;
                    PickTarget[] kids = g.getPickableChildren();

                    // now that the setup is done, walk down the tree.
                    for(int i = 0; i < num_kids && !found; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        lastPathIndex = 1;

                        switch(kids[i].getPickTargetType())
                        {
                            case PickTarget.GROUP_PICK_TYPE:
                                found = pickSingleCone((GroupPickTarget)kids[i],
                                                       req,
                                                       start,
                                                       end,
                                                       angle,
                                                       output_path,
                                                       req.generateVWorldMatrix);
                                break;

                            case PickTarget.SINGLE_PICK_TYPE:
                                found = pickSingleCone((SinglePickTarget)kids[i],
                                                       req,
                                                       start,
                                                       end,
                                                       angle,
                                                       output_path,
                                                       req.generateVWorldMatrix);
                                break;

                            case PickTarget.LEAF_PICK_TYPE:
                                found = pickSingleCone((LeafPickTarget)kids[i],
                                                       req,
                                                       start,
                                                       end,
                                                       angle,
                                                       output_path,
                                                       req.generateVWorldMatrix);
                                break;

                            case PickTarget.CUSTOM_PICK_TYPE:
                                found = pickSingleCone((CustomPickTarget)kids[i],
                                                       req,
                                                       start,
                                                       end,
                                                       angle,
                                                       output_path,
                                                       req.generateVWorldMatrix);
                                break;
                        }
                    }
                }
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found = pickSingleCone((SinglePickTarget)target_node,
                                       req,
                                       start,
                                       end,
                                       angle,
                                       output_path,
                                       req.generateVWorldMatrix);
                break;

            case PickTarget.LEAF_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found = pickSingleCone((LeafPickTarget)target_node,
                                       req,
                                       start,
                                       end,
                                       angle,
                                       output_path,
                                       req.generateVWorldMatrix);
                break;
        }

        req.pickCount = found ? 1 : 0;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param root The group node to test against and recurse into
     * @param req flags to compare against for picking
     * @param vertex The verteximum extents of the box in local coordinate space
     * @param axis The axisimum extents of the box in local coordinate space
     * @param path The place to put the results in
     * @param needTransform true if we should calc vworld information
     */
    private boolean pickSingleCone(GroupPickTarget root,
                                   PickRequest req,
                                   float[] vertex,
                                   float[] axis,
                                   float angle,
                                   SceneGraphPath path,
                                   boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return false;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionCone(vertex, axis, angle))
            return false;

        // Bounding check. If it has it, recurse on the bounding geometry
        // rather than the real geometry.
        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickSingleCone((GroupPickTarget)target_node,
                                      req,
                                      vertex,
                                      axis,
                                      angle,
                                      path,
                                      needTransform);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickSingleCone((SinglePickTarget)target_node,
                                      req,
                                      vertex,
                                      axis,
                                      angle,
                                      path,
                                      needTransform);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickSingleCone((LeafPickTarget)target_node,
                                      req,
                                      vertex,
                                      axis,
                                      angle,
                                      path,
                                      needTransform);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        boolean found = false;
        int num_kids = root.numPickableChildren();

        if(num_kids != 0)
        {
            float vertex_x = vertex[0];
            float vertex_y = vertex[1];
            float vertex_z = vertex[2];

            float axis_x = axis[0];
            float axis_y = axis[1];
            float axis_z = axis[2];

            // reset the transform at the top of the stack
            if(root instanceof TransformPickTarget)
            {
                Matrix4d tx = transformPath[lastPathIndex];

                TransformPickTarget tg = (TransformPickTarget)root;
                tg.getTransform(tx);
                tg.getInverseTransform(invertedMatrix);
                transform(invertedMatrix, vertex);
                transformNormal(invertedMatrix, axis);

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = root.getPickableChildren();

            for(int i = 0; i < num_kids && !found; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found = pickSingleCone((GroupPickTarget)kids[i],
                                               req,
                                               vertex,
                                               axis,
                                               angle,
                                               path,
                                               needTransform);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found = pickSingleCone((SinglePickTarget)kids[i],
                                               req,
                                               vertex,
                                               axis,
                                               angle,
                                               path,
                                               needTransform);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found = pickSingleCone((LeafPickTarget)kids[i],
                                               req,
                                               vertex,
                                               axis,
                                               angle,
                                               path,
                                               needTransform);
                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            vertex[0] = vertex_x;
            vertex[1] = vertex_y;
            vertex[2] = vertex_z;

            axis[0] = axis_x;
            axis[1] = axis_y;
            axis[2] = axis_z;
        }

        return found;
    }

    /**
     * Recurse a single-child shared node tree looking for
     * intersections with a single point. Shared nodes are different to normal
     * because we don't bother doing bounds tests as the bounds will be
     * identical to their child. Skip the test and just head straight to the
     * child node to test against.
     *
     * @param root The shared node to test against
     * @param req flags to compare against for picking
     * @param vertex The vertex position of the cone in local coordinate space
     * @param axis The axis vector of the code in local coordinate space
     * @param path The place to put the results in
     * @param needTransform true if we should calc vworld information
     */
    private boolean pickSingleCone(SinglePickTarget root,
                                   PickRequest req,
                                   float[] vertex,
                                   float[] axis,
                                   float angle,
                                   SceneGraphPath path,
                                   boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return false;

        resizePath();
        validTransform[lastPathIndex] = false;
        pickPath[lastPathIndex] = root;
        lastPathIndex++;

        PickTarget child = root.getPickableChild();
        if(child == null)
            return false;

        boolean found = false;

        switch(child.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                found = pickSingleCone((GroupPickTarget)child,
                                       req,
                                       vertex,
                                       axis,
                                       angle,
                                       path,
                                       needTransform);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                found = pickSingleCone((SinglePickTarget)child,
                                       req,
                                       vertex,
                                       axis,
                                       angle,
                                       path,
                                       needTransform);
                break;

            case PickTarget.LEAF_PICK_TYPE:
                found = pickSingleCone((LeafPickTarget)child,
                                       req,
                                       vertex,
                                       axis,
                                       angle,
                                       path,
                                       needTransform);
        }

        lastPathIndex--;
        pickPath[lastPathIndex] = null;
        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param root The group node to test against and recurse into
     * @param req flags to compare against for picking
     * @param vertex The vertex position of the cone in local coordinate space
     * @param axis The axis vector of the code in local coordinate space
     * @param path The place to put the results in
     * @param needTransform true if we should calc vworld information
     */
    private boolean pickSingleCone(CustomPickTarget root,
                                   PickRequest req,
                                   float[] vertex,
                                   float[] axis,
                                   float angle,
                                   SceneGraphPath path,
                                   boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return false;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionCone(vertex, axis, angle))
            return false;

        // Bounding check. If it has it, recurse on the bounding geometry
        // rather than the real geometry.
        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickSingleCone((GroupPickTarget)target_node,
                                      req,
                                      vertex,
                                      axis,
                                      angle,
                                      path,
                                      needTransform);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickSingleCone((SinglePickTarget)target_node,
                                      req,
                                      vertex,
                                      axis,
                                      angle,
                                      path,
                                      needTransform);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickSingleCone((LeafPickTarget)target_node,
                                      req,
                                      vertex,
                                      axis,
                                      angle,
                                      path,
                                      needTransform);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        boolean found = false;
        if(pickCustom(root, req))
        {
            int num_kids = pickInstructions.numChildren;

            float vertex_x = vertex[0];
            float vertex_y = vertex[1];
            float vertex_z = vertex[2];

            float axis_x = axis[0];
            float axis_y = axis[1];
            float axis_z = axis[2];

            // reset the transform at the top of the stack
            if(pickInstructions.hasTransform)
            {
                transformPath[lastPathIndex].set(pickInstructions.localTransform);

                matrixUtils.inverse(pickInstructions.localTransform,
                                    invertedMatrix);
                transform(invertedMatrix, vertex);
                transformNormal(invertedMatrix, axis);

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = pickInstructions.children.clone();

            for(int i = 0; i < num_kids && !found; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found = pickSingleCone((GroupPickTarget)kids[i],
                                               req,
                                               vertex,
                                               axis,
                                               angle,
                                               path,
                                               needTransform);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found = pickSingleCone((SinglePickTarget)kids[i],
                                               req,
                                               vertex,
                                               axis,
                                               angle,
                                               path,
                                               needTransform);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found = pickSingleCone((LeafPickTarget)kids[i],
                                               req,
                                               vertex,
                                               axis,
                                               angle,
                                               path,
                                               needTransform);
                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            vertex[0] = vertex_x;
            vertex[1] = vertex_y;
            vertex[2] = vertex_z;

            axis[0] = axis_x;
            axis[1] = axis_y;
            axis[2] = axis_z;
        }

        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param geom The geom node to test against
     * @param req flags to compare against for picking
     * @param vertex The vertex position of the cone in local coordinate space
     * @param axis The axis vector of the code in local coordinate space
     * @param path A place to set the results in
     * @param needTransform True if the local to v-world transform needs
     *    calculating
     */
    private boolean pickSingleCone(LeafPickTarget geom,
                                   PickRequest req,
                                   float[] vertex,
                                   float[] axis,
                                   float angle,
                                   SceneGraphPath path,
                                   boolean needTransform)
    {
        if(!geom.checkPickMask(req.pickType))
            return false;

        boolean found = false;
        BoundingVolume bounds = geom.getPickableBounds();

        if(bounds.checkIntersectionCone(vertex, axis, angle))
        {
            resizePath();
            pickPath[lastPathIndex] = geom;
            validTransform[lastPathIndex] = false;
            lastPathIndex++;

            if(needTransform)
                buildVWorldTransform();
            else
                vworldMatrix.setIdentity();

            path.updatePath(pickPath,
                            lastPathIndex,
                            vworldMatrix,
                            invertedMatrix);

            found = true;
            lastPathIndex--;
        }

        return found;
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
        int found = 0;
        ArrayList<SceneGraphPath> output_list = null;

        if(req.foundPaths instanceof ArrayList)
            output_list = (ArrayList<SceneGraphPath>)req.foundPaths;
        else
        {
            output_list = new ArrayList<SceneGraphPath>();
            req.foundPaths = output_list;
        }

        PickTarget target_node = root;
        BoundingVolume bounds = target_node.getPickableBounds();
        float angle = req.additionalData;

        if(bounds == null ||
           !bounds.checkIntersectionCone(req.origin,
                                         req.destination,
                                         req.additionalData))
        {
            req.pickCount = 0;
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
                target_node = (PickTarget)geom;
            else
            {
                req.pickCount = 0;
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

        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    // reset the transform at the top of the stack
                    if(root instanceof TransformPickTarget)
                    {
                        Matrix4d tx = transformPath[0];

                        TransformPickTarget tg = (TransformPickTarget)root;
                        tg.getTransform(tx);
                        tg.getInverseTransform(invertedMatrix);
                        transform(invertedMatrix, start);
                        transformNormal(invertedMatrix, end);

                        // keep angle unmodified.
                    }
                    else
                        transformPath[0].setIdentity();

                    validTransform[0] = true;
                    pickPath[0] = g;
                    PickTarget[] kids = g.getPickableChildren();

                    // now that the setup is done, walk down the tree.
                    for(int i = 0; i < num_kids; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        lastPathIndex = 1;

                        switch(kids[i].getPickTargetType())
                        {
                            case PickTarget.GROUP_PICK_TYPE:
                                found += pickAllCone((GroupPickTarget)kids[i],
                                                     req,
                                                     start,
                                                     end,
                                                     angle,
                                                     output_list,
                                                     found,
                                                     req.generateVWorldMatrix);
                                break;

                            case PickTarget.SINGLE_PICK_TYPE:
                                found += pickAllCone((SinglePickTarget)kids[i],
                                                     req,
                                                     start,
                                                     end,
                                                     angle,
                                                     output_list,
                                                     found,
                                                     req.generateVWorldMatrix);
                                break;

                            case PickTarget.LEAF_PICK_TYPE:
                                found += pickAllCone((LeafPickTarget)kids[i],
                                                     req,
                                                     start,
                                                     end,
                                                     angle,
                                                     output_list,
                                                     found,
                                                     req.generateVWorldMatrix);
                                break;

                            case PickTarget.CUSTOM_PICK_TYPE:
                                found += pickAllCone((CustomPickTarget)kids[i],
                                                     req,
                                                     start,
                                                     end,
                                                     angle,
                                                     output_list,
                                                     found,
                                                     req.generateVWorldMatrix);
                                break;
                        }
                    }

                    req.pickCount = found;
                }
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found += pickAllCone((SinglePickTarget)target_node,
                                     req,
                                     start,
                                     end,
                                     angle,
                                     output_list,
                                     0,
                                     req.generateVWorldMatrix);

                req.pickCount = found;
                break;

            case PickTarget.LEAF_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found += pickAllCone((LeafPickTarget)target_node,
                                     req,
                                     start,
                                     end,
                                     angle,
                                     output_list,
                                     0,
                                     req.generateVWorldMatrix);

                req.pickCount = found;
                break;
        }
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param root The group node to test against and descend into
     * @param req flags to compare against for picking
     * @param vertex The vertex position of the cone in local coordinate space
     * @param axis The axis vector of the code in local coordinate space
     * @param paths The place to put the results in
     * @param needTransform true if we should calc vworld information
     */
    private int pickAllCone(GroupPickTarget root,
                            PickRequest req,
                            float[] vertex,
                            float[] axis,
                            float angle,
                            ArrayList<SceneGraphPath> paths,
                            int currentPath,
                            boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return 0;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionCone(vertex, axis, angle))
            return 0;

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickAllCone((GroupPickTarget)target_node,
                                   req,
                                   vertex,
                                   axis,
                                   angle,
                                   paths,
                                   currentPath,
                                   needTransform);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickAllCone((SinglePickTarget)target_node,
                                   req,
                                   vertex,
                                   axis,
                                   angle,
                                   paths,
                                   currentPath,
                                   needTransform);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickAllCone((LeafPickTarget)target_node,
                                   req,
                                   vertex,
                                   axis,
                                   angle,
                                   paths,
                                   currentPath,
                                   needTransform);
            }
            else if(target_node instanceof CustomPickTarget)
            {
                return pickAllCone((CustomPickTarget)target_node,
                                   req,
                                   vertex,
                                   axis,
                                   angle,
                                   paths,
                                   currentPath,
                                   needTransform);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        int found = 0;
        int num_kids = root.numPickableChildren();

        if(num_kids != 0)
        {
            float vertex_x = vertex[0];
            float vertex_y = vertex[1];
            float vertex_z = vertex[2];

            float axis_x = axis[0];
            float axis_y = axis[1];
            float axis_z = axis[2];

            // reset the transform at the top of the stack
            if(root instanceof TransformPickTarget)
            {
                Matrix4d tx = transformPath[lastPathIndex];

                TransformPickTarget tg = (TransformPickTarget)root;
                tg.getTransform(tx);
                tg.getInverseTransform(invertedMatrix);
                transform(invertedMatrix, vertex);
                transformNormal(invertedMatrix, axis);

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = root.getPickableChildren();

            for(int i = 0; i < num_kids; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found += pickAllCone((GroupPickTarget)kids[i],
                                             req,
                                             vertex,
                                             axis,
                                             angle,
                                             paths,
                                             currentPath + found,
                                             needTransform);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found += pickAllCone((SinglePickTarget)kids[i],
                                             req,
                                             vertex,
                                             axis,
                                             angle,
                                             paths,
                                             currentPath + found,
                                             needTransform);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found += pickAllCone((LeafPickTarget)kids[i],
                                             req,
                                             vertex,
                                             axis,
                                             angle,
                                             paths,
                                             currentPath + found,
                                             needTransform);
                        break;

                    case PickTarget.CUSTOM_PICK_TYPE:
                        found += pickAllCone((CustomPickTarget)kids[i],
                                             req,
                                             vertex,
                                             axis,
                                             angle,
                                             paths,
                                             currentPath + found,
                                             needTransform);
                        break;

                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            vertex[0] = vertex_x;
            vertex[1] = vertex_y;
            vertex[2] = vertex_z;

            axis[0] = axis_x;
            axis[1] = axis_y;
            axis[2] = axis_z;
        }

        return found;
    }

    /**
     * Recurse a single-child shared node tree looking for
     * intersections with a single point. Shared nodes are different to normal
     * because we don't bother doing bounds tests as the bounds will be
     * identical to their child. Skip the test and just head straight to the
     * child node to test against.
     *
     * @param root The shared node to test against
     * @param req flags to compare against for picking
     * @param vertex The vertex position of the cone in local coordinate space
     * @param axis The axis vector of the code in local coordinate space
     * @param paths The place to put the results in
     * @param needTransform true if we should calc vworld information
     */
    private int pickAllCone(SinglePickTarget root,
                            PickRequest req,
                            float[] vertex,
                            float[] axis,
                            float angle,
                            ArrayList<SceneGraphPath> paths,
                            int currentPath,
                            boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return 0;

        resizePath();
        validTransform[lastPathIndex] = false;
        pickPath[lastPathIndex] = root;
        lastPathIndex++;

        PickTarget child = root.getPickableChild();
        if(child != null)
            return 0;

        int found = 0;

        switch(child.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                found = pickAllCone((GroupPickTarget)child,
                                    req,
                                    vertex,
                                    axis,
                                    angle,
                                    paths,
                                    currentPath,
                                    needTransform);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                found = pickAllCone((SinglePickTarget)child,
                                    req,
                                    vertex,
                                    axis,
                                    angle,
                                    paths,
                                    currentPath,
                                    needTransform);
                break;

            case PickTarget.LEAF_PICK_TYPE:
                found = pickAllCone((LeafPickTarget)child,
                                    req,
                                    vertex,
                                    axis,
                                    angle,
                                    paths,
                                    currentPath,
                                    needTransform);
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                found = pickAllCone((CustomPickTarget)child,
                                    req,
                                    vertex,
                                    axis,
                                    angle,
                                    paths,
                                    currentPath,
                                    needTransform);
                break;

        }

        lastPathIndex--;
        pickPath[lastPathIndex] = null;
        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param root The group node to test against and descend into
     * @param req flags to compare against for picking
     * @param vertex The vertex position of the cone in local coordinate space
     * @param axis The axis vector of the code in local coordinate space
     * @param paths The place to put the results in
     * @param needTransform true if we should calc vworld information
     */
    private int pickAllCone(CustomPickTarget root,
                            PickRequest req,
                            float[] vertex,
                            float[] axis,
                            float angle,
                            ArrayList<SceneGraphPath> paths,
                            int currentPath,
                            boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return 0;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionCone(vertex, axis, angle))
            return 0;

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickAllCone((GroupPickTarget)target_node,
                                   req,
                                   vertex,
                                   axis,
                                   angle,
                                   paths,
                                   currentPath,
                                   needTransform);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickAllCone((SinglePickTarget)target_node,
                                   req,
                                   vertex,
                                   axis,
                                   angle,
                                   paths,
                                   currentPath,
                                   needTransform);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickAllCone((LeafPickTarget)target_node,
                                   req,
                                   vertex,
                                   axis,
                                   angle,
                                   paths,
                                   currentPath,
                                   needTransform);
            }
            else if(target_node instanceof CustomPickTarget)
            {
                return pickAllCone((CustomPickTarget)target_node,
                                   req,
                                   vertex,
                                   axis,
                                   angle,
                                   paths,
                                   currentPath,
                                   needTransform);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        int found = 0;
        if(pickCustom(root, req))
        {
            int num_kids = pickInstructions.numChildren;

            float vertex_x = vertex[0];
            float vertex_y = vertex[1];
            float vertex_z = vertex[2];

            float axis_x = axis[0];
            float axis_y = axis[1];
            float axis_z = axis[2];

            // reset the transform at the top of the stack
            if(pickInstructions.hasTransform)
            {
                transformPath[lastPathIndex].set(pickInstructions.localTransform);

                matrixUtils.inverse(pickInstructions.localTransform,
                                    invertedMatrix);
                transform(invertedMatrix, vertex);
                transformNormal(invertedMatrix, axis);

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = pickInstructions.children.clone();

            for(int i = 0; i < num_kids; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found += pickAllCone((GroupPickTarget)kids[i],
                                             req,
                                             vertex,
                                             axis,
                                             angle,
                                             paths,
                                             currentPath + found,
                                             needTransform);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found += pickAllCone((SinglePickTarget)kids[i],
                                             req,
                                             vertex,
                                             axis,
                                             angle,
                                             paths,
                                             currentPath + found,
                                             needTransform);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found += pickAllCone((LeafPickTarget)kids[i],
                                             req,
                                             vertex,
                                             axis,
                                             angle,
                                             paths,
                                             currentPath + found,
                                             needTransform);
                        break;

                    case PickTarget.CUSTOM_PICK_TYPE:
                        found += pickAllCone((CustomPickTarget)kids[i],
                                             req,
                                             vertex,
                                             axis,
                                             angle,
                                             paths,
                                             currentPath + found,
                                             needTransform);
                        break;

                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            vertex[0] = vertex_x;
            vertex[1] = vertex_y;
            vertex[2] = vertex_z;

            axis[0] = axis_x;
            axis[1] = axis_y;
            axis[2] = axis_z;
        }

        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param geom The geom node to test against
     * @param req flags to compare against for picking
     * @param vertex The verteximum extents of the box in local coordinate space
     * @param axis The axisimum extents of the box in local coordinate space
     * @param paths A place to set the results in
     * @param needTransform True if the vertexal to v-world transform needs
     *    calculating
     */
    private int pickAllCone(LeafPickTarget geom,
                            PickRequest req,
                            float[] vertex,
                            float[] axis,
                            float angle,
                            ArrayList<SceneGraphPath> paths,
                            int currentPath,
                            boolean needTransform)
    {
        if(!geom.checkPickMask(req.pickType))
            return 0;

        int found = 0;
        BoundingVolume bounds = geom.getPickableBounds();

        if(bounds.checkIntersectionCone(vertex, axis, angle))
        {
            resizePath();
            pickPath[lastPathIndex] = geom;
            validTransform[lastPathIndex] = false;
            lastPathIndex++;

            SceneGraphPath p;

            if(currentPath >= paths.size())
            {
                p = new SceneGraphPath();
                paths.add(p);
            }
            else
                p = (SceneGraphPath)paths.get(currentPath);

            if(needTransform)
                buildVWorldTransform();
            else
                vworldMatrix.setIdentity();


            p.updatePath(pickPath,
                         lastPathIndex,
                         vworldMatrix,
                         invertedMatrix);

            found = 1;
            lastPathIndex--;
        }

        return found;
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

            default:
				I18nManager intl_mgr = I18nManager.getManager();
				String msg_pattern = intl_mgr.getString(NO_SORT_TYPE_PROP);

				Locale lcl = intl_mgr.getFoundLocale();

				NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

				Object[] msg_args = { req.pickSortType };
				Format[] fmts = { n_fmt };
				MessageFormat msg_fmt =
					new MessageFormat(msg_pattern, lcl);
				msg_fmt.setFormats(fmts);
				String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
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
        SceneGraphPath output_path = null;

        if(req.foundPaths instanceof SceneGraphPath)
            output_path = (SceneGraphPath)req.foundPaths;
        else
        {
            output_path = new SceneGraphPath();
            req.foundPaths = output_path;
        }

        boolean found = false;

        PickTarget target_node = root;
        BoundingVolume bounds = target_node.getPickableBounds();

        if(bounds == null ||
           !bounds.checkIntersectionBox(req.origin, req.destination))
        {
            req.pickCount = 0;
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
                target_node = (PickTarget)geom;
            else
            {
                req.pickCount = 0;
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

        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    // reset the transform at the top of the stack
                    if(root instanceof TransformPickTarget)
                    {
                        Matrix4d tx = transformPath[0];

                        TransformPickTarget tg = (TransformPickTarget)root;
                        tg.getTransform(tx);
                        tg.getInverseTransform(invertedMatrix);
                        transform(invertedMatrix, start);
                        transform(invertedMatrix, end);
						checkExtents(start, end);
                    }
                    else
                        transformPath[0].setIdentity();

                    validTransform[0] = true;
                    pickPath[0] = g;
                    PickTarget[] kids = g.getPickableChildren();

                    // now that the setup is done, walk down the tree.
                    for(int i = 0; i < num_kids && !found; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        lastPathIndex = 1;

                        switch(kids[i].getPickTargetType())
                        {
                            case PickTarget.GROUP_PICK_TYPE:
                                found = pickSingleBox((GroupPickTarget)kids[i],
                                                      req,
                                                      start,
                                                      end,
                                                      output_path,
                                                      req.generateVWorldMatrix);
                                break;

                            case PickTarget.SINGLE_PICK_TYPE:
                                found = pickSingleBox((SinglePickTarget)kids[i],
                                                      req,
                                                      start,
                                                      end,
                                                      output_path,
                                                      req.generateVWorldMatrix);
                                break;

                            case PickTarget.LEAF_PICK_TYPE:
                                found = pickSingleBox((LeafPickTarget)kids[i],
                                                      req,
                                                      start,
                                                      end,
                                                      output_path,
                                                      req.generateVWorldMatrix);
                                break;

                            case PickTarget.CUSTOM_PICK_TYPE:
                                found = pickSingleBox((CustomPickTarget)kids[i],
                                                      req,
                                                      start,
                                                      end,
                                                      output_path,
                                                      req.generateVWorldMatrix);
                                break;
                        }
                    }
                }
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found = pickSingleBox((SinglePickTarget)target_node,
                                      req,
                                      start,
                                      end,
                                      output_path,
                                      req.generateVWorldMatrix);
                break;

            case PickTarget.LEAF_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found = pickSingleBox((LeafPickTarget)target_node,
                                      req,
                                      start,
                                      end,
                                      output_path,
                                      req.generateVWorldMatrix);

                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                found = pickSingleBox((CustomPickTarget)target_node,
                                      req,
                                      start,
                                      end,
                                      output_path,
                                      req.generateVWorldMatrix);
        }

        req.pickCount = found ? 1 : 0;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param root The group node to test against and recurse into
     * @param req flags to compare against for picking
     * @param min The minimum extents of the box in local coordinate space
     * @param max The maximum extents of the box in local coordinate space
     * @param path The place to put the results in
     * @param needTransform true if we should calc vworld information
     */
    private boolean pickSingleBox(GroupPickTarget root,
                                  PickRequest req,
                                  float[] min,
                                  float[] max,
                                  SceneGraphPath path,
                                  boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return false;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionBox(min, max))
            return false;

        // Bounding check. If it has it, recurse on the bounding geometry
        // rather than the real geometry.
        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickSingleBox((GroupPickTarget)target_node,
                                     req,
                                     min,
                                     max,
                                     path,
                                     needTransform);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickSingleBox((SinglePickTarget)target_node,
                                     req,
                                     min,
                                     max,
                                     path,
                                     needTransform);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickSingleBox((LeafPickTarget)target_node,
                                     req,
                                     min,
                                     max,
                                     path,
                                     needTransform);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        boolean found = false;
        int num_kids = root.numPickableChildren();

        if(num_kids != 0)
        {
            float min_x = min[0];
            float min_y = min[1];
            float min_z = min[2];

            float max_x = max[0];
            float max_y = max[1];
            float max_z = max[2];

            // reset the transform at the top of the stack
            if(root instanceof TransformPickTarget)
            {
                Matrix4d tx = transformPath[lastPathIndex];

                TransformPickTarget tg = (TransformPickTarget)root;
                tg.getTransform(tx);
                tg.getInverseTransform(invertedMatrix);
                transform(invertedMatrix, min);
                transform(invertedMatrix, max);
				checkExtents(min, max);

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = root.getPickableChildren();

            for(int i = 0; i < num_kids && !found; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found = pickSingleBox((GroupPickTarget)kids[i],
                                              req,
                                              min,
                                              max,
                                              path,
                                              needTransform);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found = pickSingleBox((SinglePickTarget)kids[i],
                                              req,
                                              min,
                                              max,
                                              path,
                                              needTransform);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found = pickSingleBox((LeafPickTarget)kids[i],
                                              req,
                                              min,
                                              max,
                                              path,
                                              needTransform);
                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            min[0] = min_x;
            min[1] = min_y;
            min[2] = min_z;

            max[0] = max_x;
            max[1] = max_y;
            max[2] = max_z;
        }

        return found;
    }

    /**
     * Recurse a single-child shared node tree looking for
     * intersections with a single point. Shared nodes are different to normal
     * because we don't bother doing bounds tests as the bounds will be
     * identical to their child. Skip the test and just head straight to the
     * child node to test against.
     *
     * @param root The shared node to test against
     * @param req flags to compare against for picking
     * @param min The minimum extents of the box in local coordinate space
     * @param max The maximum extents of the box in local coordinate space
     * @param path The place to put the results in
     * @param needTransform true if we should calc vworld information
     */
    private boolean pickSingleBox(SinglePickTarget root,
                                  PickRequest req,
                                  float[] min,
                                  float[] max,
                                  SceneGraphPath path,
                                  boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return false;

        resizePath();
        validTransform[lastPathIndex] = false;
        pickPath[lastPathIndex] = root;
        lastPathIndex++;

        PickTarget child = root.getPickableChild();
        boolean found = false;

        switch(child.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                found = pickSingleBox((GroupPickTarget)child,
                                      req,
                                      min,
                                      max,
                                      path,
                                      needTransform);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                found = pickSingleBox((SinglePickTarget)child,
                                      req,
                                      min,
                                      max,
                                      path,
                                      needTransform);
                break;

            case PickTarget.LEAF_PICK_TYPE:
                found = pickSingleBox((LeafPickTarget)child,
                                      req,
                                      min,
                                      max,
                                      path,
                                      needTransform);
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                found = pickSingleBox((CustomPickTarget)child,
                                      req,
                                      min,
                                      max,
                                      path,
                                      needTransform);
                break;
        }

        lastPathIndex--;
        pickPath[lastPathIndex] = null;
        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param root The group node to test against and recurse into
     * @param req flags to compare against for picking
     * @param min The minimum extents of the box in local coordinate space
     * @param max The maximum extents of the box in local coordinate space
     * @param path The place to put the results in
     * @param needTransform true if we should calc vworld information
     */
    private boolean pickSingleBox(CustomPickTarget root,
                                  PickRequest req,
                                  float[] min,
                                  float[] max,
                                  SceneGraphPath path,
                                  boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return false;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionBox(min, max))
            return false;

        // Bounding check. If it has it, recurse on the bounding geometry
        // rather than the real geometry.
        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickSingleBox((GroupPickTarget)target_node,
                                     req,
                                     min,
                                     max,
                                     path,
                                     needTransform);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickSingleBox((SinglePickTarget)target_node,
                                     req,
                                     min,
                                     max,
                                     path,
                                     needTransform);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickSingleBox((LeafPickTarget)target_node,
                                     req,
                                     min,
                                     max,
                                     path,
                                     needTransform);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        boolean found = false;

        if(pickCustom(root, req))
        {
            int num_kids = pickInstructions.numChildren;

            float min_x = min[0];
            float min_y = min[1];
            float min_z = min[2];

            float max_x = max[0];
            float max_y = max[1];
            float max_z = max[2];

            // reset the transform at the top of the stack
            if(pickInstructions.hasTransform)
            {
                transformPath[lastPathIndex].set(pickInstructions.localTransform);

                matrixUtils.inverse(pickInstructions.localTransform,
                                    invertedMatrix);
                transform(invertedMatrix, min);
                transform(invertedMatrix, max);
				checkExtents(min, max);

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = pickInstructions.children.clone();

            for(int i = 0; i < num_kids && !found; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found = pickSingleBox((GroupPickTarget)kids[i],
                                              req,
                                              min,
                                              max,
                                              path,
                                              needTransform);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found = pickSingleBox((SinglePickTarget)kids[i],
                                              req,
                                              min,
                                              max,
                                              path,
                                              needTransform);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found = pickSingleBox((LeafPickTarget)kids[i],
                                              req,
                                              min,
                                              max,
                                              path,
                                              needTransform);
                        break;

                    case PickTarget.CUSTOM_PICK_TYPE:
                        found = pickSingleBox((CustomPickTarget)kids[i],
                                              req,
                                              min,
                                              max,
                                              path,
                                              needTransform);
                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            min[0] = min_x;
            min[1] = min_y;
            min[2] = min_z;

            max[0] = max_x;
            max[1] = max_y;
            max[2] = max_z;
        }

        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param geom The geom node to test against
     * @param req flags to compare against for picking
     * @param min The minimum extents of the box in local coordinate space
     * @param max The maximum extents of the box in local coordinate space
     * @param path A place to set the results in
     * @param needTransform True if the local to v-world transform needs
     *    calculating
     */
    private boolean pickSingleBox(LeafPickTarget geom,
                                  PickRequest req,
                                  float[] min,
                                  float[] max,
                                  SceneGraphPath path,
                                  boolean needTransform)
    {
        if(!geom.checkPickMask(req.pickType))
            return false;

        boolean found = false;
        BoundingVolume bounds = geom.getPickableBounds();

        if(bounds.checkIntersectionBox(min, max))
        {
            resizePath();
            pickPath[lastPathIndex] = geom;
            validTransform[lastPathIndex] = false;
            lastPathIndex++;

            if(needTransform)
                buildVWorldTransform();
            else
                vworldMatrix.setIdentity();

            path.updatePath(pickPath,
                            lastPathIndex,
                            vworldMatrix,
                            invertedMatrix);

            found = true;
            lastPathIndex--;
        }

        return found;
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
        int found = 0;
        ArrayList<SceneGraphPath> output_list = null;

        if(req.foundPaths instanceof ArrayList)
            output_list = (ArrayList<SceneGraphPath>)req.foundPaths;
        else
        {
            output_list = new ArrayList<SceneGraphPath>();
            req.foundPaths = output_list;
        }

        PickTarget target_node = root;
        BoundingVolume bounds = target_node.getPickableBounds();

        if(bounds == null ||
           !bounds.checkIntersectionBox(req.origin, req.destination))
        {
            req.pickCount = 0;
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
                target_node = (PickTarget)geom;
            else
            {
                req.pickCount = 0;
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

        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    // reset the transform at the top of the stack
                    if(root instanceof TransformPickTarget)
                    {
                        Matrix4d tx = transformPath[0];

                        TransformPickTarget tg = (TransformPickTarget)root;
                        tg.getTransform(tx);
                        tg.getInverseTransform(invertedMatrix);
                        transform(invertedMatrix, start);
                        transform(invertedMatrix, end);
						checkExtents(start, end);
                    }
                    else
                        transformPath[0].setIdentity();

                    validTransform[0] = true;
                    pickPath[0] = g;
                    PickTarget[] kids = g.getPickableChildren();

                    // now that the setup is done, walk down the tree.
                    for(int i = 0; i < num_kids; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        lastPathIndex = 1;

                        switch(kids[i].getPickTargetType())
                        {
                            case PickTarget.GROUP_PICK_TYPE:
                                found += pickAllBox((GroupPickTarget)kids[i],
                                                    req,
                                                    start,
                                                    end,
                                                    output_list,
                                                    found,
                                                    req.generateVWorldMatrix);
                                break;

                            case PickTarget.SINGLE_PICK_TYPE:
                                found += pickAllBox((SinglePickTarget)kids[i],
                                                    req,
                                                    start,
                                                    end,
                                                    output_list,
                                                    found,
                                                    req.generateVWorldMatrix);
                                break;

                            case PickTarget.LEAF_PICK_TYPE:
                                found += pickAllBox((LeafPickTarget)kids[i],
                                                    req,
                                                    start,
                                                    end,
                                                    output_list,
                                                    found,
                                                    req.generateVWorldMatrix);
                                break;

                            case PickTarget.CUSTOM_PICK_TYPE:
                                found += pickAllBox((CustomPickTarget)kids[i],
                                                    req,
                                                    start,
                                                    end,
                                                    output_list,
                                                    found,
                                                    req.generateVWorldMatrix);
                                break;
                        }
                    }

                    req.pickCount = found;
                }
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found += pickAllBox((SinglePickTarget)target_node,
                                    req,
                                    start,
                                    end,
                                    output_list,
                                    0,
                                    req.generateVWorldMatrix);

                req.pickCount = found;
                break;

            case PickTarget.LEAF_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found += pickAllBox((LeafPickTarget)target_node,
                                    req,
                                    start,
                                    end,
                                    output_list,
                                    0,
                                    req.generateVWorldMatrix);

                req.pickCount = found;
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                found += pickAllBox((CustomPickTarget)target_node,
                                    req,
                                    start,
                                    end,
                                    output_list,
                                    found,
                                    req.generateVWorldMatrix);
                req.pickCount = found;
        }
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param root The group node to test against and descend into
     * @param req flags to compare against for picking
     * @param min The minimum extents of the box in local coordinate space
     * @param max The maximum extents of the box in local coordinate space
     * @param paths The place to put the results in
     * @param needTransform true if we should calc vworld information
     */
    private int pickAllBox(GroupPickTarget root,
                           PickRequest req,
                           float[] min,
                           float[] max,
                           ArrayList<SceneGraphPath> paths,
                           int currentPath,
                           boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return 0;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionBox(min, max))
            return 0;

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickAllBox((GroupPickTarget)target_node,
                                  req,
                                  min,
                                  max,
                                  paths,
                                  currentPath,
                                  needTransform);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickAllBox((SinglePickTarget)target_node,
                                  req,
                                  min,
                                  max,
                                  paths,
                                  currentPath,
                                  needTransform);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickAllBox((LeafPickTarget)target_node,
                                  req,
                                  min,
                                  max,
                                  paths,
                                  currentPath,
                                  needTransform);
            }
            else if(target_node instanceof CustomPickTarget)
            {
                return pickAllBox((CustomPickTarget)target_node,
                                  req,
                                  min,
                                  max,
                                  paths,
                                  currentPath,
                                  needTransform);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        int found = 0;
        int num_kids = root.numPickableChildren();

        if(num_kids != 0)
        {
            float min_x = min[0];
            float min_y = min[1];
            float min_z = min[2];

            float max_x = max[0];
            float max_y = max[1];
            float max_z = max[2];

            // reset the transform at the top of the stack
            if(root instanceof TransformPickTarget)
            {
                Matrix4d tx = transformPath[lastPathIndex];

                TransformPickTarget tg = (TransformPickTarget)root;
                tg.getTransform(tx);
                tg.getInverseTransform(invertedMatrix);
                transform(invertedMatrix, min);
                transform(invertedMatrix, max);
				checkExtents(min, max);

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = root.getPickableChildren();

            for(int i = 0; i < num_kids; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found += pickAllBox((GroupPickTarget)kids[i],
                                            req,
                                            min,
                                            max,
                                            paths,
                                            currentPath + found,
                                            needTransform);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found += pickAllBox((SinglePickTarget)kids[i],
                                            req,
                                            min,
                                            max,
                                            paths,
                                            currentPath + found,
                                            needTransform);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found += pickAllBox((LeafPickTarget)kids[i],
                                            req,
                                            min,
                                            max,
                                            paths,
                                            currentPath + found,
                                            needTransform);
                        break;

                    case PickTarget.CUSTOM_PICK_TYPE:
                        found += pickAllBox((CustomPickTarget)kids[i],
                                            req,
                                            min,
                                            max,
                                            paths,
                                            currentPath + found,
                                            needTransform);
                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            min[0] = min_x;
            min[1] = min_y;
            min[2] = min_z;

            max[0] = max_x;
            max[1] = max_y;
            max[2] = max_z;
        }

        return found;
    }

    /**
     * Recurse a single-child shared node tree looking for
     * intersections with a single point. Shared nodes are different to normal
     * because we don't bother doing bounds tests as the bounds will be
     * identical to their child. Skip the test and just head straight to the
     * child node to test against.
     *
     * @param root The shared node to test against
     * @param req flags to compare against for picking
     * @param min The minimum extents of the box in local coordinate space
     * @param max The maximum extents of the box in local coordinate space
     * @param paths The place to put the results in
     * @param needTransform true if we should calc vworld information
     */
    private int pickAllBox(SinglePickTarget root,
                           PickRequest req,
                           float[] min,
                           float[] max,
                           ArrayList<SceneGraphPath> paths,
                           int currentPath,
                           boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return 0;

        resizePath();
        validTransform[lastPathIndex] = false;
        pickPath[lastPathIndex] = root;
        lastPathIndex++;

        PickTarget child = root.getPickableChild();
        if(child == null)
            return 0;

        int found = 0;

        switch(child.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                found = pickAllBox((GroupPickTarget)child,
                                   req,
                                   min,
                                   max,
                                   paths,
                                   currentPath,
                                   needTransform);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                found = pickAllBox((SinglePickTarget)child,
                                   req,
                                   min,
                                   max,
                                   paths,
                                   currentPath,
                                   needTransform);
                break;

            case PickTarget.LEAF_PICK_TYPE:
                found = pickAllBox((LeafPickTarget)child,
                                   req,
                                   min,
                                   max,
                                   paths,
                                   currentPath,
                                   needTransform);
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                found = pickAllBox((CustomPickTarget)child,
                                   req,
                                   min,
                                   max,
                                   paths,
                                   currentPath,
                                   needTransform);
        }

        lastPathIndex--;
        pickPath[lastPathIndex] = null;
        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param root The group node to test against and descend into
     * @param req flags to compare against for picking
     * @param min The minimum extents of the box in local coordinate space
     * @param max The maximum extents of the box in local coordinate space
     * @param paths The place to put the results in
     * @param needTransform true if we should calc vworld information
     */
    private int pickAllBox(CustomPickTarget root,
                           PickRequest req,
                           float[] min,
                           float[] max,
                           ArrayList<SceneGraphPath> paths,
                           int currentPath,
                           boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return 0;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionBox(min, max))
            return 0;

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickAllBox((GroupPickTarget)target_node,
                                  req,
                                  min,
                                  max,
                                  paths,
                                  currentPath,
                                  needTransform);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickAllBox((SinglePickTarget)target_node,
                                  req,
                                  min,
                                  max,
                                  paths,
                                  currentPath,
                                  needTransform);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickAllBox((LeafPickTarget)target_node,
                                  req,
                                  min,
                                  max,
                                  paths,
                                  currentPath,
                                  needTransform);
            }
            else if(target_node instanceof CustomPickTarget)
            {
                return pickAllBox((CustomPickTarget)target_node,
                                  req,
                                  min,
                                  max,
                                  paths,
                                  currentPath,
                                  needTransform);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        int found = 0;
        if(pickCustom(root, req))
        {
            int num_kids = pickInstructions.numChildren;

            float min_x = min[0];
            float min_y = min[1];
            float min_z = min[2];

            float max_x = max[0];
            float max_y = max[1];
            float max_z = max[2];

            // reset the transform at the top of the stack
            if(root instanceof TransformPickTarget)
            {
                transformPath[lastPathIndex].set(pickInstructions.localTransform);

                matrixUtils.inverse(pickInstructions.localTransform,
                                    invertedMatrix);
                transform(invertedMatrix, min);
                transform(invertedMatrix, max);
				checkExtents(min, max);

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = pickInstructions.children.clone();

            for(int i = 0; i < num_kids; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found += pickAllBox((GroupPickTarget)kids[i],
                                            req,
                                            min,
                                            max,
                                            paths,
                                            currentPath + found,
                                            needTransform);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found += pickAllBox((SinglePickTarget)kids[i],
                                            req,
                                            min,
                                            max,
                                            paths,
                                            currentPath + found,
                                            needTransform);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found += pickAllBox((LeafPickTarget)kids[i],
                                            req,
                                            min,
                                            max,
                                            paths,
                                            currentPath + found,
                                            needTransform);
                        break;

                    case PickTarget.CUSTOM_PICK_TYPE:
                        found += pickAllBox((CustomPickTarget)kids[i],
                                            req,
                                            min,
                                            max,
                                            paths,
                                            currentPath + found,
                                            needTransform);
                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            min[0] = min_x;
            min[1] = min_y;
            min[2] = min_z;

            max[0] = max_x;
            max[1] = max_y;
            max[2] = max_z;
        }

        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param geom The geom node to test against
     * @param req flags to compare against for picking
     * @param min The minimum extents of the box in local coordinate space
     * @param max The maximum extents of the box in local coordinate space
     * @param paths A place to set the results in
     * @param needTransform True if the minal to v-world transform needs
     *    calculating
     */
    private int pickAllBox(LeafPickTarget geom,
                           PickRequest req,
                           float[] min,
                           float[] max,
                           ArrayList<SceneGraphPath> paths,
                           int currentPath,
                           boolean needTransform)
    {
        if(!geom.checkPickMask(req.pickType))
            return 0;

        int found = 0;
        BoundingVolume bounds = geom.getPickableBounds();

        if(bounds.checkIntersectionBox(min, max))
        {
            resizePath();
            pickPath[lastPathIndex] = geom;
            validTransform[lastPathIndex] = false;
            lastPathIndex++;

            SceneGraphPath p;

            if(currentPath >= paths.size())
            {
                p = new SceneGraphPath();
                paths.add(p);
            }
            else
                p = (SceneGraphPath)paths.get(currentPath);

            if(needTransform)
                buildVWorldTransform();
            else
                vworldMatrix.setIdentity();

            p.updatePath(pickPath,
                         lastPathIndex,
                         vworldMatrix,
                         invertedMatrix);

            found = 1;
            lastPathIndex--;
        }

        return found;
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
     * @return The number of intersections found
     */
    private void pickFrustum(PickTarget root, PickRequest req)
    {
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

            default:
				I18nManager intl_mgr = I18nManager.getManager();
				String msg_pattern = intl_mgr.getString(NO_SORT_TYPE_PROP);

				Locale lcl = intl_mgr.getFoundLocale();

				NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

				Object[] msg_args = { req.pickSortType };
				Format[] fmts = { n_fmt };
				MessageFormat msg_fmt =
					new MessageFormat(msg_pattern, lcl);
				msg_fmt.setFormats(fmts);
				String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
        }
    }

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given ray. Return the first
     * found.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     * @return The number of intersections found
     */
    private void pickSingleFrustum(PickTarget root, PickRequest req)
    {
        SceneGraphPath output_path = null;

        if(req.foundPaths instanceof SceneGraphPath)
            output_path = (SceneGraphPath)req.foundPaths;
        else
        {
            output_path = new SceneGraphPath();
            req.foundPaths = output_path;
        }

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

        boolean found = false;

        PickTarget target_node = root;
        BoundingVolume bounds = target_node.getPickableBounds();
        transformPath[0].setIdentity();

        if(bounds.checkIntersectionFrustum(frustumPlanes,
            transformPath[0]) ==
            BoundingVolume.FRUSTUM_ALLOUT)
        {
            req.pickCount = 0;
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
                target_node = (PickTarget)geom;
            else
            {
                req.pickCount = 0;
                return;
            }
        }

        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    // reset the transform at the top of the stack
                    if(root instanceof TransformPickTarget)
                    {
                        Matrix4d tx = transformPath[lastPathIndex];

                        TransformPickTarget tg = (TransformPickTarget)root;
                        tg.getTransform(tx);
                    }

                    validTransform[0] = true;
                    pickPath[0] = g;
                    PickTarget[] kids = g.getPickableChildren();

                    // now that the setup is done, walk down the tree.
                    for(int i = 0; i < num_kids && !found; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        lastPathIndex = 1;

                        switch(kids[i].getPickTargetType())
                        {
                            case PickTarget.GROUP_PICK_TYPE:
                                found = pickSingleFrustum((GroupPickTarget)kids[i],
                                                          req,
                                                          output_path,
                                                          req.generateVWorldMatrix);
                                break;

                            case PickTarget.SINGLE_PICK_TYPE:
                                found = pickSingleFrustum((SinglePickTarget)kids[i],
                                                          req,
                                                          output_path,
                                                          req.generateVWorldMatrix);
                                break;

                            case PickTarget.LEAF_PICK_TYPE:
                                found = pickSingleFrustum((LeafPickTarget)kids[i],
                                                          req,
                                                          output_path,
                                                          req.generateVWorldMatrix);
                                break;

                            case PickTarget.CUSTOM_PICK_TYPE:
                                found = pickSingleFrustum((CustomPickTarget)kids[i],
                                                          req,
                                                          output_path,
                                                          req.generateVWorldMatrix);
                                break;
                        }
                    }
                }
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found = pickSingleFrustum((SinglePickTarget)target_node,
                                          req,
                                          output_path,
                                          req.generateVWorldMatrix);
                break;

            case PickTarget.LEAF_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found = pickSingleFrustum((LeafPickTarget)target_node,
                                          req,
                                          output_path,
                                          req.generateVWorldMatrix);
                break;
        }

        req.pickCount = found ? 1 : 0;
    }

    /**
     * Recurse a single-child shared node tree looking for
     * intersections with a frustum. Shared nodes are different to normal
     * because we don't bother doing bounds tests as the bounds will be
     * identical to their child. Skip the test and just head straight to the
     * child node to test against.
     *
     * @param root The shared node to test against
     * @param req flags to compare against for picking
     * @param path The place to put the results in
     * @param needTransform true if we should calc vworld information
     */
    private boolean pickSingleFrustum(SinglePickTarget root,
                                      PickRequest req,
                                      SceneGraphPath path,
                                      boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return false;

        resizePath();
        validTransform[lastPathIndex] = false;
        pickPath[lastPathIndex] = root;

        Matrix4d tx = transformPath[lastPathIndex - 1];
        transformPath[lastPathIndex].set(tx);

        lastPathIndex++;

        PickTarget child = root.getPickableChild();
        if(child == null)
            return false;

        boolean found = false;

        switch(child.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                found = pickSingleFrustum((GroupPickTarget)child,
                                          req,
                                          path,
                                          needTransform);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                found = pickSingleFrustum((SinglePickTarget)child,
                                          req,
                                          path,
                                          needTransform);
                break;

            case PickTarget.LEAF_PICK_TYPE:
                found = pickSingleFrustum((LeafPickTarget)child,
                                          req,
                                          path,
                                          needTransform);
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                found = pickSingleFrustum((CustomPickTarget)child,
                                          req,
                                          path,
                                          needTransform);
                break;
        }

        lastPathIndex--;
        pickPath[lastPathIndex] = null;
        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param root The group to test against
     * @param req flags to compare against for picking
     * @param path The place to put the results in
     * @param needTransform true if we should calc vworld information
     */
    private boolean pickSingleFrustum(GroupPickTarget root,
                                      PickRequest req,
                                      SceneGraphPath path,
                                      boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return false;

        BoundingVolume bounds = root.getPickableBounds();

        if(bounds.checkIntersectionFrustum(frustumPlanes,
            transformPath[lastPathIndex]) ==
            BoundingVolume.FRUSTUM_ALLOUT)
            return false;

        // Bounding check. If it has it, recurse on the bounding geometry
        // rather than the real geometry.
        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickSingleFrustum((GroupPickTarget)target_node,
                                         req,
                                         path,
                                         needTransform);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickSingleFrustum((SinglePickTarget)target_node,
                                         req,
                                         path,
                                         needTransform);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickSingleFrustum((LeafPickTarget)target_node,
                                         req,
                                         path,
                                         needTransform);
            }
            else if(target_node instanceof CustomPickTarget)
            {
                return pickSingleFrustum((CustomPickTarget)target_node,
                                         req,
                                         path,
                                         needTransform);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        boolean found = false;
        int num_kids = root.numPickableChildren();

        if(num_kids != 0)
        {
            // reset the transform at the top of the stack
            if(root instanceof TransformPickTarget)
            {
                Matrix4d tx = transformPath[lastPathIndex];

                TransformPickTarget tg = (TransformPickTarget)root;
                tg.getTransform(tx);
                tx.mul(transformPath[lastPathIndex - 1], tx);

                validTransform[lastPathIndex] = true;
            }
            else
            {
                Matrix4d tx = transformPath[lastPathIndex - 1];
                transformPath[lastPathIndex].set(tx);
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = root.getPickableChildren();

            for(int i = 0; i < num_kids && !found; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found = pickSingleFrustum((GroupPickTarget)kids[i],
                                                  req,
                                                  path,
                                                  needTransform);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found = pickSingleFrustum((SinglePickTarget)kids[i],
                                                  req,
                                                  path,
                                                  needTransform);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found = pickSingleFrustum((LeafPickTarget)kids[i],
                                                  req,
                                                  path,
                                                  needTransform);
                        break;

                    case PickTarget.CUSTOM_PICK_TYPE:
                        found = pickSingleFrustum((CustomPickTarget)kids[i],
                                                  req,
                                                  path,
                                                  needTransform);
                        break;
                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;
        }

        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param root The group to test against
     * @param req flags to compare against for picking
     * @param path The place to put the results in
     * @param needTransform true if we should calc vworld information
     */
    private boolean pickSingleFrustum(CustomPickTarget root,
                                      PickRequest req,
                                      SceneGraphPath path,
                                      boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return false;

        BoundingVolume bounds = root.getPickableBounds();

        if(bounds.checkIntersectionFrustum(frustumPlanes,
                                           transformPath[lastPathIndex]) ==
            BoundingVolume.FRUSTUM_ALLOUT)
            return false;

        // Bounding check. If it has it, recurse on the bounding geometry
        // rather than the real geometry.
        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickSingleFrustum((GroupPickTarget)target_node,
                                         req,
                                         path,
                                         needTransform);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickSingleFrustum((SinglePickTarget)target_node,
                                         req,
                                         path,
                                         needTransform);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickSingleFrustum((LeafPickTarget)target_node,
                                         req,
                                         path,
                                         needTransform);
            }
            else if(target_node instanceof CustomPickTarget)
            {
                return pickSingleFrustum((CustomPickTarget)target_node,
                                         req,
                                         path,
                                         needTransform);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        boolean found = false;
        if(pickCustom(root, req))
        {
            int num_kids = pickInstructions.numChildren;

            // reset the transform at the top of the stack
            if(pickInstructions.hasTransform)
            {
                Matrix4d tx = transformPath[lastPathIndex];

                tx.set(pickInstructions.localTransform);
                tx.mul(transformPath[lastPathIndex - 1], tx);

                validTransform[lastPathIndex] = true;
            }
            else
            {
                Matrix4d tx = transformPath[lastPathIndex - 1];
                transformPath[lastPathIndex].set(tx);
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = pickInstructions.children.clone();

            for(int i = 0; i < num_kids && !found; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found = pickSingleFrustum((GroupPickTarget)kids[i],
                                                  req,
                                                  path,
                                                  needTransform);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found = pickSingleFrustum((SinglePickTarget)kids[i],
                                                  req,
                                                  path,
                                                  needTransform);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found = pickSingleFrustum((LeafPickTarget)kids[i],
                                                  req,
                                                  path,
                                                  needTransform);
                        break;

                    case PickTarget.CUSTOM_PICK_TYPE:
                        found = pickSingleFrustum((CustomPickTarget)kids[i],
                                                  req,
                                                  path,
                                                  needTransform);
                        break;
                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;
        }

        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param path A place to set the results in
     * @param needTransform True if the local to v-world transform needs
     *    calculating
     */
    private boolean pickSingleFrustum(LeafPickTarget geom,
                                      PickRequest req,
                                      SceneGraphPath path,
                                      boolean needTransform)
    {
        if(!geom.checkPickMask(req.pickType))
            return false;

        boolean found = false;
        BoundingVolume bounds = geom.getPickableBounds();

        if(bounds.checkIntersectionFrustum(frustumPlanes,
            transformPath[lastPathIndex-1]) !=
            BoundingVolume.FRUSTUM_ALLOUT)
        {
            resizePath();
            pickPath[lastPathIndex] = geom;
            validTransform[lastPathIndex] = false;
            lastPathIndex++;

            // Note that the transform path is already pre-multiplied through
            // here. Just and invert is all that needs to be done.
            if(needTransform)
                matrixUtils.inverse(transformPath[lastPathIndex - 2],
                vworldMatrix);
            else
                vworldMatrix.setIdentity();

            path.updatePath(pickPath,
                            lastPathIndex,
                            transformPath[lastPathIndex - 2],
                            vworldMatrix);

            found = true;
            lastPathIndex--;
        }

        return found;
    }

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given ray. Return the first
     * found.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     * @return The number of intersections found
     */
    private void pickAllFrustum(PickTarget root, PickRequest req)
    {
        int found = 0;
        ArrayList<SceneGraphPath> output_list = null;

        if(req.foundPaths instanceof ArrayList)
            output_list = (ArrayList<SceneGraphPath>)req.foundPaths;
        else
        {
            output_list = new ArrayList<SceneGraphPath>();
            req.foundPaths = output_list;
        }

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

        PickTarget target_node = root;
        BoundingVolume bounds = target_node.getPickableBounds();
        transformPath[0].setIdentity();

        if(bounds.checkIntersectionFrustum(frustumPlanes,
            transformPath[0]) ==
            BoundingVolume.FRUSTUM_ALLOUT)
        {
            req.pickCount = 0;
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
                target_node = (PickTarget)geom;
            else
            {
                req.pickCount = 0;
                return;
            }
        }

        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    // reset the transform at the top of the stack
                    if(root instanceof TransformPickTarget)
                    {
                        Matrix4d tx = transformPath[0];

                        TransformPickTarget tg = (TransformPickTarget)root;
                        tg.getTransform(tx);
                    }

                    validTransform[0] = true;
                    pickPath[0] = g;
                    PickTarget[] kids = g.getPickableChildren();

                    // now that the setup is done, walk down the tree.
                    for(int i = 0; i < num_kids; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        lastPathIndex = 1;

                        switch(kids[i].getPickTargetType())
                        {
                            case PickTarget.GROUP_PICK_TYPE:
                                found += pickAllFrustum((GroupPickTarget)kids[i],
                                                        req,
                                                        output_list,
                                                        found,
                                                        req.generateVWorldMatrix);
                                break;

                            case PickTarget.SINGLE_PICK_TYPE:
                                found += pickAllFrustum((SinglePickTarget)kids[i],
                                                        req,
                                                        output_list,
                                                        found,
                                                        req.generateVWorldMatrix);
                                break;

                            case PickTarget.LEAF_PICK_TYPE:
                                found += pickAllFrustum((LeafPickTarget)kids[i],
                                                        req,
                                                        output_list,
                                                        found,
                                                        req.generateVWorldMatrix);
                                break;

                            case PickTarget.CUSTOM_PICK_TYPE:
                                found = pickAllFrustum((CustomPickTarget)kids[i],
                                                       req,
                                                       output_list,
                                                       found,
                                                       req.generateVWorldMatrix);
                                break;
                        }
                    }

                    req.pickCount = found;
                }
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found += pickAllFrustum((SinglePickTarget)target_node,
                                        req,
                                        output_list,
                                        0,
                                        req.generateVWorldMatrix);
                req.pickCount = found;
                break;

            case PickTarget.LEAF_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found += pickAllFrustum((LeafPickTarget)target_node,
                                        req,
                                        output_list,
                                        0,
                                        req.generateVWorldMatrix);

                req.pickCount = found;
                break;
        }
    }

    /**
     * Recurse a single-child shared node tree looking for
     * intersections with a frustum. Shared nodes are different to normal
     * because we don't bother doing bounds tests as the bounds will be
     * identical to their child. Skip the test and just head straight to the
     * child node to test against.
     *
     * @param root The shared node to test against
     * @param req flags to compare against for picking
     * @param paths Array of paths place to set the results in
     * @param currentPath Active index in the paths array
     * @param needTransform true if we should calc vworld information
     */
    private int pickAllFrustum(SinglePickTarget root,
                               PickRequest req,
                               ArrayList<SceneGraphPath> paths,
                               int currentPath,
                               boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return 0;

        resizePath();
        validTransform[lastPathIndex] = false;
        pickPath[lastPathIndex] = root;

        Matrix4d tx = transformPath[lastPathIndex - 1];
        transformPath[lastPathIndex].set(tx);

        lastPathIndex++;

        PickTarget child = root.getPickableChild();
        if(child == null)
            return 0;

        int found = 0;

        switch(child.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                found = pickAllFrustum((GroupPickTarget)child,
                                       req,
                                       paths,
                                       currentPath,
                                       needTransform);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                found = pickAllFrustum((SinglePickTarget)child,
                                       req,
                                       paths,
                                       currentPath,
                                       needTransform);
                break;

            case PickTarget.LEAF_PICK_TYPE:
                found = pickAllFrustum((LeafPickTarget)child,
                                       req,
                                       paths,
                                       currentPath,
                                       needTransform);
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                found = pickAllFrustum((CustomPickTarget)child,
                                       req,
                                       paths,
                                       currentPath,
                                       needTransform);
                break;
        }

        lastPathIndex--;
        pickPath[lastPathIndex] = null;
        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param root The shared node to test against
     * @param req flags to compare against for picking
     * @param paths The place to put the results in
     * @param needTransform true if we should calc vworld information
     */
    private int pickAllFrustum(GroupPickTarget root,
                               PickRequest req,
                               ArrayList<SceneGraphPath> paths,
                               int currentPath,
                               boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return 0;

        BoundingVolume bounds = root.getPickableBounds();

        if(bounds.checkIntersectionFrustum(frustumPlanes,
            transformPath[lastPathIndex-1]) ==
            BoundingVolume.FRUSTUM_ALLOUT)
        {
            return 0;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickAllFrustum((GroupPickTarget)target_node,
                                      req,
                                      paths,
                                      currentPath,
                                      needTransform);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickAllFrustum((SinglePickTarget)target_node,
                                      req,
                                      paths,
                                      currentPath,
                                      needTransform);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickAllFrustum((LeafPickTarget)target_node,
                                      req,
                                      paths,
                                      currentPath,
                                      needTransform);
            }
            else if(target_node instanceof CustomPickTarget)
            {
                return pickAllFrustum((CustomPickTarget)target_node,
                                      req,
                                      paths,
                                      currentPath,
                                      needTransform);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        int found = 0;
        int num_kids = root.numPickableChildren();

        if(num_kids != 0)
        {
            resizePath();
            // reset the transform at the top of the stack
            if(root instanceof TransformPickTarget)
            {
                Matrix4d tx = transformPath[lastPathIndex];

                TransformPickTarget tg = (TransformPickTarget)root;
                tg.getTransform(tx);
                tx.mul(transformPath[lastPathIndex - 1], tx);
                validTransform[lastPathIndex] = true;
            }
            else
            {
                Matrix4d tx = transformPath[lastPathIndex - 1];
                transformPath[lastPathIndex].set(tx);
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;

            lastPathIndex++;
            PickTarget[] kids = root.getPickableChildren();

            for(int i = 0; i < num_kids; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found += pickAllFrustum((GroupPickTarget)kids[i],
                                                req,
                                                paths,
                                                currentPath + found,
                                                needTransform);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found += pickAllFrustum((SinglePickTarget)kids[i],
                                                req,
                                                paths,
                                                currentPath + found,
                                                needTransform);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found += pickAllFrustum((LeafPickTarget)kids[i],
                                                req,
                                                paths,
                                                currentPath + found,
                                                needTransform);
                        break;

                    case PickTarget.CUSTOM_PICK_TYPE:
                        found += pickAllFrustum((CustomPickTarget)kids[i],
                                                req,
                                                paths,
                                                currentPath + found,
                                                needTransform);
                        break;
                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;
        }

        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param root The shared node to test against
     * @param req flags to compare against for picking
     * @param paths The place to put the results in
     * @param needTransform true if we should calc vworld information
     */
    private int pickAllFrustum(CustomPickTarget root,
                               PickRequest req,
                               ArrayList<SceneGraphPath> paths,
                               int currentPath,
                               boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return 0;

        BoundingVolume bounds = root.getPickableBounds();

        if(bounds.checkIntersectionFrustum(frustumPlanes,
            transformPath[lastPathIndex-1]) ==
            BoundingVolume.FRUSTUM_ALLOUT)
        {
            return 0;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickAllFrustum((GroupPickTarget)target_node,
                                      req,
                                      paths,
                                      currentPath,
                                      needTransform);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickAllFrustum((SinglePickTarget)target_node,
                                      req,
                                      paths,
                                      currentPath,
                                      needTransform);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickAllFrustum((LeafPickTarget)target_node,
                                      req,
                                      paths,
                                      currentPath,
                                      needTransform);
            }
            else if(target_node instanceof CustomPickTarget)
            {
                return pickAllFrustum((CustomPickTarget)target_node,
                                      req,
                                      paths,
                                      currentPath,
                                      needTransform);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        int found = 0;
        if(pickCustom(root, req))
        {
            int num_kids = pickInstructions.numChildren;

            // reset the transform at the top of the stack
            if(pickInstructions.hasTransform)
            {
                Matrix4d tx = transformPath[lastPathIndex];
                tx.set(pickInstructions.localTransform);
                tx.mul(transformPath[lastPathIndex - 1], tx);
                validTransform[lastPathIndex] = true;
            }
            else
            {
                Matrix4d tx = transformPath[lastPathIndex - 1];
                transformPath[lastPathIndex].set(tx);
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;

            lastPathIndex++;
            PickTarget[] kids = pickInstructions.children.clone();

            for(int i = 0; i < num_kids; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found += pickAllFrustum((GroupPickTarget)kids[i],
                                                req,
                                                paths,
                                                currentPath + found,
                                                needTransform);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found += pickAllFrustum((SinglePickTarget)kids[i],
                                                req,
                                                paths,
                                                currentPath + found,
                                                needTransform);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found += pickAllFrustum((LeafPickTarget)kids[i],
                                                req,
                                                paths,
                                                currentPath + found,
                                                needTransform);
                        break;

                    case PickTarget.CUSTOM_PICK_TYPE:
                        found += pickAllFrustum((CustomPickTarget)kids[i],
                                                req,
                                                paths,
                                                currentPath + found,
                                                needTransform);
                        break;
                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;
        }

        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param req flags to compare against for picking
     * @param paths Array of paths place to set the results in
     * @param currentPath Active index in the paths array
     * @param needTransform True if the local to v-world transform needs
     *    calculating
     */
    private int pickAllFrustum(LeafPickTarget geom,
                               PickRequest req,
                               ArrayList<SceneGraphPath> paths,
                               int currentPath,
                               boolean needTransform)
    {
        if(!geom.checkPickMask(req.pickType))
            return 0;

        int found = 0;
        BoundingVolume bounds = geom.getPickableBounds();

        if(bounds.checkIntersectionFrustum(frustumPlanes,
            transformPath[lastPathIndex-1]) !=
            BoundingVolume.FRUSTUM_ALLOUT)
        {
            resizePath();
            pickPath[lastPathIndex] = geom;
            validTransform[lastPathIndex] = false;
            lastPathIndex++;

            SceneGraphPath p;

            if(currentPath >= paths.size())
            {
                p = new SceneGraphPath();
                paths.add(p);
            }
            else
                p = (SceneGraphPath)paths.get(currentPath);

            // Note that the transform path is already pre-multiplied through
            // here. Just and invert is all that needs to be done.
            if(needTransform)
                matrixUtils.inverse(transformPath[lastPathIndex - 2],
                vworldMatrix);
            else
                vworldMatrix.setIdentity();

            p.updatePath(pickPath,
                         lastPathIndex,
                         transformPath[lastPathIndex - 2],
                         vworldMatrix);

            found = 1;
            lastPathIndex--;
        }

        return found;
    }

    // ----------------------- Sphere Picking -------------------------------

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given ray. Return the first
     * found.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     * @return The number of intersections found
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

            default:
				I18nManager intl_mgr = I18nManager.getManager();
				String msg_pattern = intl_mgr.getString(NO_SORT_TYPE_PROP);

				Locale lcl = intl_mgr.getFoundLocale();

				NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

				Object[] msg_args = { req.pickSortType };
				Format[] fmts = { n_fmt };
				MessageFormat msg_fmt =
					new MessageFormat(msg_pattern, lcl);
				msg_fmt.setFormats(fmts);
				String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
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
        SceneGraphPath output_path = null;

        if(req.foundPaths instanceof SceneGraphPath)
            output_path = (SceneGraphPath)req.foundPaths;
        else
        {
            output_path = new SceneGraphPath();
            req.foundPaths = output_path;
        }

        boolean found = false;

        PickTarget target_node = root;
        BoundingVolume bounds = target_node.getPickableBounds();

        if(bounds == null ||
           !bounds.checkIntersectionSphere(req.origin, req.additionalData))
        {
            req.pickCount = 0;
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
                target_node = (PickTarget)geom;
            else
            {
                req.pickCount = 0;
                return;
            }
        }

        start[0] = req.origin[0];
        start[1] = req.origin[1];
        start[2] = req.origin[2];
        start[3] = 1;

        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    float radius = req.additionalData;

                    // reset the transform at the top of the stack
                    if(root instanceof TransformPickTarget)
                    {
                        Matrix4d tx = transformPath[0];

                        TransformPickTarget tg = (TransformPickTarget)root;
                        tg.getTransform(tx);
                        tg.getInverseTransform(invertedMatrix);
                        transform(invertedMatrix, start);
                        float scale = (float)matrixUtils.getUniformScale(invertedMatrix);
                        radius *= scale;
                    }
                    else
                        transformPath[0].setIdentity();

                    validTransform[0] = true;
                    pickPath[0] = g;
                    PickTarget[] kids = g.getPickableChildren();

                    // now that the setup is done, walk down the tree.
                    for(int i = 0; i < num_kids && !found; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        lastPathIndex = 1;

                        switch(kids[i].getPickTargetType())
                        {
                            case PickTarget.GROUP_PICK_TYPE:
                                found = pickSingleSphere((GroupPickTarget)kids[i],
                                                         req,
                                                         start,
                                                         radius,
                                                         output_path,
                                                         req.generateVWorldMatrix);
                                break;

                            case PickTarget.SINGLE_PICK_TYPE:
                                found = pickSingleSphere((SinglePickTarget)kids[i],
                                                         req,
                                                         start,
                                                         radius,
                                                         output_path,
                                                         req.generateVWorldMatrix);
                                break;

                            case PickTarget.LEAF_PICK_TYPE:
                                found = pickSingleSphere((LeafPickTarget)kids[i],
                                                         req,
                                                         start,
                                                         radius,
                                                         output_path,
                                                         req.generateVWorldMatrix);
                                 break;

                            case PickTarget.CUSTOM_PICK_TYPE:
                                found = pickSingleSphere((CustomPickTarget)kids[i],
                                                         req,
                                                         start,
                                                         radius,
                                                         output_path,
                                                         req.generateVWorldMatrix);
                                break;

                        }
                    }
                }
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found = pickSingleSphere((SinglePickTarget)target_node,
                                         req,
                                         start,
                                         req.additionalData,
                                         output_path,
                                         req.generateVWorldMatrix);
                break;

            case PickTarget.LEAF_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found = pickSingleSphere((LeafPickTarget)target_node,
                                         req,
                                         start,
                                         req.additionalData,
                                         output_path,
                                         req.generateVWorldMatrix);
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                found = pickSingleSphere((CustomPickTarget)target_node,
                                         req,
                                         start,
                                         req.additionalData,
                                         output_path,
                                         req.generateVWorldMatrix);
        }

        req.pickCount = found ? 1 : 0;
    }

    /**
     * Recurse a single-child shared node tree looking for
     * intersections with a single point. Shared nodes are different to normal
     * because we don't bother doing bounds tests as the bounds will be
     * identical to their child. Skip the test and just head straight to the
     * child node to test against.
     *
     * @param root The shared node to test against
     * @param req flags to compare against for picking
     * @param min The minimum extents of the box in local coordinate space
     * @param radius The radius of the sphere
     * @param path The place to put the results in
     * @param needTransform true if we should calc vworld information
     */
    private boolean pickSingleSphere(SinglePickTarget root,
                                     PickRequest req,
                                     float[] min,
                                     float radius,
                                     SceneGraphPath path,
                                     boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return false;

        resizePath();
        validTransform[lastPathIndex] = false;
        pickPath[lastPathIndex] = root;
        lastPathIndex++;

        PickTarget child = root.getPickableChild();
        if(child == null)
            return false;

        boolean found = false;

        switch(child.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                found = pickSingleSphere((GroupPickTarget)child,
                                         req,
                                         min,
                                         radius,
                                         path,
                                         needTransform);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                found = pickSingleSphere((SinglePickTarget)child,
                                         req,
                                         min,
                                         radius,
                                         path,
                                         needTransform);
                break;

            case PickTarget.LEAF_PICK_TYPE:
                found = pickSingleSphere((LeafPickTarget)child,
                                         req,
                                         min,
                                         radius,
                                         path,
                                         needTransform);
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                found = pickSingleSphere((CustomPickTarget)child,
                                         req,
                                         min,
                                         radius,
                                         path,
                                         needTransform);

        }

        lastPathIndex--;
        pickPath[lastPathIndex] = null;
        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param root The group node to test against and recurse into
     * @param req flags to compare against for picking
     * @param min The minimum extents of the box in local coordinate space
     * @param radius The radius of the sphere
     * @param path The place to put the results in
     * @param needTransform true if we should calc vworld information
     */
    private boolean pickSingleSphere(GroupPickTarget root,
                                     PickRequest req,
                                     float[] min,
                                     float radius,
                                     SceneGraphPath path,
                                     boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return false;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionSphere(min, radius))
            return false;

        // Bounding check. If it has it, recurse on the bounding geometry
        // rather than the real geometry.
        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickSingleSphere((GroupPickTarget)target_node,
                                        req,
                                        min,
                                        radius,
                                        path,
                                        needTransform);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickSingleSphere((SinglePickTarget)target_node,
                                        req,
                                        min,
                                        radius,
                                        path,
                                        needTransform);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickSingleSphere((LeafPickTarget)target_node,
                                        req,
                                        min,
                                        radius,
                                        path,
                                        needTransform);
            }
            else if(target_node instanceof CustomPickTarget)
            {
                return pickSingleSphere((CustomPickTarget)target_node,
                                        req,
                                        min,
                                        radius,
                                        path,
                                        needTransform);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        boolean found = false;
        int num_kids = root.numPickableChildren();

        if(num_kids != 0)
        {
            float min_x = min[0];
            float min_y = min[1];
            float min_z = min[2];

            float old_radius = radius;

            // reset the transform at the top of the stack
            if(root instanceof TransformPickTarget)
            {
                Matrix4d tx = transformPath[lastPathIndex];

                TransformPickTarget tg = (TransformPickTarget)root;
                tg.getTransform(tx);
                tg.getInverseTransform(invertedMatrix);
                matrixUtils.inverse(tx, invertedMatrix);
                transform(invertedMatrix, min);

                float scale = (float)matrixUtils.getUniformScale(invertedMatrix);
                radius *= scale;

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = root.getPickableChildren();

            for(int i = 0; i < num_kids && !found; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found = pickSingleSphere((GroupPickTarget)kids[i],
                                                 req,
                                                 min,
                                                 radius,
                                                 path,
                                                 needTransform);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found = pickSingleSphere((SinglePickTarget)kids[i],
                                                 req,
                                                 min,
                                                 radius,
                                                 path,
                                                 needTransform);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found = pickSingleSphere((LeafPickTarget)kids[i],
                                                 req,
                                                 min,
                                                 radius,
                                                 path,
                                                 needTransform);
                        break;

                    case PickTarget.CUSTOM_PICK_TYPE:
                        found = pickSingleSphere((CustomPickTarget)kids[i],
                                                 req,
                                                 min,
                                                 radius,
                                                 path,
                                                 needTransform);
                        break;

                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            min[0] = min_x;
            min[1] = min_y;
            min[2] = min_z;

            radius = old_radius;
        }

        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param root The group node to test against and recurse into
     * @param req flags to compare against for picking
     * @param min The minimum extents of the box in local coordinate space
     * @param radius The radius of the sphere
     * @param path The place to put the results in
     * @param needTransform true if we should calc vworld information
     */
    private boolean pickSingleSphere(CustomPickTarget root,
                                     PickRequest req,
                                     float[] min,
                                     float radius,
                                     SceneGraphPath path,
                                     boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return false;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionSphere(min, radius))
            return false;

        // Bounding check. If it has it, recurse on the bounding geometry
        // rather than the real geometry.
        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickSingleSphere((GroupPickTarget)target_node,
                                        req,
                                        min,
                                        radius,
                                        path,
                                        needTransform);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickSingleSphere((SinglePickTarget)target_node,
                                        req,
                                        min,
                                        radius,
                                        path,
                                        needTransform);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickSingleSphere((LeafPickTarget)target_node,
                                        req,
                                        min,
                                        radius,
                                        path,
                                        needTransform);
            }
            else if(target_node instanceof CustomPickTarget)
            {
                return pickSingleSphere((CustomPickTarget)target_node,
                                        req,
                                        min,
                                        radius,
                                        path,
                                        needTransform);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        boolean found = false;
        if(pickCustom(root, req))
        {
            int num_kids = pickInstructions.numChildren;

            float min_x = min[0];
            float min_y = min[1];
            float min_z = min[2];

            float old_radius = radius;

            // reset the transform at the top of the stack
            if(pickInstructions.hasTransform)
            {
                transformPath[lastPathIndex].set(pickInstructions.localTransform);

                matrixUtils.inverse(pickInstructions.localTransform,
                                    invertedMatrix);
                transform(invertedMatrix, min);

                float scale = (float)matrixUtils.getUniformScale(invertedMatrix);
                radius *= scale;

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = pickInstructions.children.clone();

            for(int i = 0; i < num_kids && !found; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found = pickSingleSphere((GroupPickTarget)kids[i],
                                                 req,
                                                 min,
                                                 radius,
                                                 path,
                                                 needTransform);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found = pickSingleSphere((SinglePickTarget)kids[i],
                                                 req,
                                                 min,
                                                 radius,
                                                 path,
                                                 needTransform);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found = pickSingleSphere((LeafPickTarget)kids[i],
                                                 req,
                                                 min,
                                                 radius,
                                                 path,
                                                 needTransform);
                        break;

                    case PickTarget.CUSTOM_PICK_TYPE:
                        found = pickSingleSphere((CustomPickTarget)kids[i],
                                                 req,
                                                 min,
                                                 radius,
                                                 path,
                                                 needTransform);
                        break;

                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            min[0] = min_x;
            min[1] = min_y;
            min[2] = min_z;

            radius = old_radius;
        }

        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param geom The geom node to test against
     * @param req flags to compare against for picking
     * @param min The minimum extents of the box in local coordinate space
     * @param radius The radius of the sphere
     * @param path A place to set the results in
     * @param needTransform True if the local to v-world transform needs
     *    calculating
     */
    private boolean pickSingleSphere(LeafPickTarget geom,
                                     PickRequest req,
                                     float[] min,
                                     float radius,
                                     SceneGraphPath path,
                                     boolean needTransform)
    {
        if(!geom.checkPickMask(req.pickType))
            return false;

        boolean found = false;
        BoundingVolume bounds = geom.getPickableBounds();

        if(bounds.checkIntersectionSphere(min, radius))
        {
            resizePath();
            pickPath[lastPathIndex] = geom;
            validTransform[lastPathIndex] = false;
            lastPathIndex++;

            if(needTransform)
                buildVWorldTransform();
            else
                vworldMatrix.setIdentity();

            path.updatePath(pickPath,
                            lastPathIndex,
                            vworldMatrix,
                            invertedMatrix);

            lastPathIndex--;
            found = true;
        }

        return found;
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
        int found = 0;
        ArrayList<SceneGraphPath> output_list = null;

        if(req.foundPaths instanceof ArrayList)
            output_list = (ArrayList<SceneGraphPath>)req.foundPaths;
        else
        {
            output_list = new ArrayList<>();
            req.foundPaths = output_list;
        }

        PickTarget target_node = root;
        BoundingVolume bounds = target_node.getPickableBounds();

        if(bounds == null ||
           !bounds.checkIntersectionSphere(req.origin, req.additionalData))
        {
            req.pickCount = 0;
            return;
        }

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node geom = bg.getProxyGeometry();

            if(geom instanceof PickTarget)
                target_node = (PickTarget)geom;
            else
            {
                req.pickCount = 0;
                return;
            }
        }

        start[0] = req.origin[0];
        start[1] = req.origin[1];
        start[2] = req.origin[2];
        start[3] = 1;

        switch(target_node.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                GroupPickTarget g = (GroupPickTarget)target_node;
                int num_kids = g.numPickableChildren();

                if(num_kids != 0)
                {
                    float radius = req.additionalData;

                    // reset the transform at the top of the stack
                    if(root instanceof TransformPickTarget)
                    {
                        Matrix4d tx = transformPath[0];

                        TransformPickTarget tg = (TransformPickTarget)root;
                        tg.getTransform(tx);
                        tg.getInverseTransform(invertedMatrix);
                        transform(invertedMatrix, start);
                        float scale = (float)matrixUtils.getUniformScale(invertedMatrix);
                        radius *= scale;
                    }
                    else
                        transformPath[0].setIdentity();

                    validTransform[0] = true;
                    pickPath[0] = g;
                    PickTarget[] kids = g.getPickableChildren();

                    // now that the setup is done, walk down the tree.
                    for(int i = 0; i < num_kids; i++)
                    {
                        if(kids[i] == null)
                            continue;

                        lastPathIndex = 1;

                        switch(kids[i].getPickTargetType())
                        {
                            case PickTarget.GROUP_PICK_TYPE:
                                found += pickAllSphere((GroupPickTarget)kids[i],
                                                       req,
                                                       start,
                                                       radius,
                                                       output_list,
                                                       found,
                                                       req.generateVWorldMatrix);
                                break;

                            case PickTarget.SINGLE_PICK_TYPE:
                                found += pickAllSphere((SinglePickTarget)kids[i],
                                                       req,
                                                       start,
                                                       radius,
                                                       output_list,
                                                       found,
                                                       req.generateVWorldMatrix);
                                break;

                            case PickTarget.LEAF_PICK_TYPE:
                                found += pickAllSphere((LeafPickTarget)kids[i],
                                                       req,
                                                       start,
                                                       radius,
                                                       output_list,
                                                       found,
                                                       req.generateVWorldMatrix);
                                break;

                            case PickTarget.CUSTOM_PICK_TYPE:
                                found += pickAllSphere((CustomPickTarget)kids[i],
                                                       req,
                                                       start,
                                                       radius,
                                                       output_list,
                                                       found,
                                                       req.generateVWorldMatrix);
                                break;

                        }
                    }

                    req.pickCount = found;
                }
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found += pickAllSphere((SinglePickTarget)target_node,
                                       req,
                                       start,
                                       req.additionalData,
                                       output_list,
                                       0,
                                       req.generateVWorldMatrix);

                req.pickCount = found;
                break;

            case PickTarget.LEAF_PICK_TYPE:
                transformPath[0].setIdentity();
                validTransform[0] = true;
                pickPath[0] = target_node;
                lastPathIndex = 1;

                found += pickAllSphere((LeafPickTarget)target_node,
                                       req,
                                       start,
                                       req.additionalData,
                                       output_list,
                                       0,
                                       req.generateVWorldMatrix);

                req.pickCount = found;
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                found += pickAllSphere((CustomPickTarget)target_node,
                                       req,
                                       start,
                                       req.additionalData,
                                       output_list,
                                       found,
                                       req.generateVWorldMatrix);
                req.pickCount = found;
        }
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param root The group node to test against and descend into
     * @param req flags to compare against for picking
     * @param min The minimum extents of the box in local coordinate space
     * @param radius The radius of the sphere
     * @param paths The place to put the results in
     * @param needTransform true if we should calc vworld information
     */
    private int pickAllSphere(GroupPickTarget root,
                              PickRequest req,
                              float[] min,
                              float radius,
                              ArrayList<SceneGraphPath> paths,
                              int currentPath,
                              boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return 0;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionSphere(min, radius))
            return 0;

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickAllSphere((GroupPickTarget)target_node,
                                     req,
                                     min,
                                     radius,
                                     paths,
                                     currentPath,
                                     needTransform);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickAllSphere((SinglePickTarget)target_node,
                                     req,
                                     min,
                                     radius,
                                     paths,
                                     currentPath,
                                     needTransform);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickAllSphere((LeafPickTarget)target_node,
                                     req,
                                     min,
                                     radius,
                                     paths,
                                     currentPath,
                                     needTransform);
            }
            else if(target_node instanceof CustomPickTarget)
            {
                return pickAllSphere((CustomPickTarget)target_node,
                                     req,
                                     min,
                                     radius,
                                     paths,
                                     currentPath,
                                     needTransform);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        int found = 0;
        int num_kids = root.numPickableChildren();

        if(num_kids != 0)
        {
            float min_x = min[0];
            float min_y = min[1];
            float min_z = min[2];

            float old_radius = radius;

            // reset the transform at the top of the stack
            if(root instanceof TransformPickTarget)
            {
                Matrix4d tx = transformPath[lastPathIndex];

                TransformPickTarget tg = (TransformPickTarget)root;
                tg.getTransform(tx);
                tg.getInverseTransform(invertedMatrix);
                transform(invertedMatrix, min);
                float scale = (float)matrixUtils.getUniformScale(invertedMatrix);
                radius *= scale;

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = root.getPickableChildren();

            for(int i = 0; i < num_kids; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found += pickAllSphere((GroupPickTarget)kids[i],
                                               req,
                                               min,
                                               radius,
                                               paths,
                                               currentPath + found,
                                               needTransform);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found += pickAllSphere((SinglePickTarget)kids[i],
                                               req,
                                               min,
                                               radius,
                                               paths,
                                               currentPath + found,
                                               needTransform);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found += pickAllSphere((LeafPickTarget)kids[i],
                                               req,
                                               min,
                                               radius,
                                               paths,
                                               currentPath + found,
                                               needTransform);
                        break;

                    case PickTarget.CUSTOM_PICK_TYPE:
                        found += pickAllSphere((CustomPickTarget)kids[i],
                                               req,
                                               min,
                                               radius,
                                               paths,
                                               currentPath + found,
                                               needTransform);
                        break;
                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            min[0] = min_x;
            min[1] = min_y;
            min[2] = min_z;

            radius = old_radius;
        }

        return found;
    }

    /**
     * Recurse a single-child shared node tree looking for
     * intersections with a single point. Shared nodes are different to normal
     * because we don't bother doing bounds tests as the bounds will be
     * identical to their child. Skip the test and just head straight to the
     * child node to test against.
     *
     * @param root The shared node to test against
     * @param req flags to compare against for picking
     * @param min req minimum extents of the box in local coordinate space
     * @param radius The radius of the sphere
     * @param paths The place to put the results in
     * @param needTransform true if we should calc vworld information
     */
    private int pickAllSphere(SinglePickTarget root,
                              PickRequest req,
                              float[] min,
                              float radius,
                              ArrayList<SceneGraphPath> paths,
                              int currentPath,
                              boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return 0;

        resizePath();
        validTransform[lastPathIndex] = false;
        pickPath[lastPathIndex] = root;
        lastPathIndex++;

        PickTarget child = root.getPickableChild();
        if(child == null)
            return 0;

        int found = 0;

        switch(child.getPickTargetType())
        {
            case PickTarget.GROUP_PICK_TYPE:
                found = pickAllSphere((GroupPickTarget)child,
                                      req,
                                      min,
                                      radius,
                                      paths,
                                      currentPath,
                                      needTransform);
                break;

            case PickTarget.SINGLE_PICK_TYPE:
                found = pickAllSphere((SinglePickTarget)child,
                                      req,
                                      min,
                                      radius,
                                      paths,
                                      currentPath,
                                      needTransform);
                break;

            case PickTarget.LEAF_PICK_TYPE:
                found = pickAllSphere((LeafPickTarget)child,
                                      req,
                                      min,
                                      radius,
                                      paths,
                                      currentPath,
                                      needTransform);
                break;

            case PickTarget.CUSTOM_PICK_TYPE:
                found = pickAllSphere((CustomPickTarget)child,
                                      req,
                                      min,
                                      radius,
                                      paths,
                                      currentPath,
                                      needTransform);
                break;

        }

        lastPathIndex--;
        pickPath[lastPathIndex] = null;
        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param root The group node to test against and descend into
     * @param req flags to compare against for picking
     * @param min The minimum extents of the box in local coordinate space
     * @param radius The radius of the sphere
     * @param paths The place to put the results in
     * @param needTransform true if we should calc vworld information
     */
    private int pickAllSphere(CustomPickTarget root,
                              PickRequest req,
                              float[] min,
                              float radius,
                              ArrayList<SceneGraphPath> paths,
                              int currentPath,
                              boolean needTransform)
    {
        if(!root.checkPickMask(req.pickType))
            return 0;

        BoundingVolume bounds = root.getPickableBounds();

        if(!bounds.checkIntersectionSphere(min, radius))
            return 0;

        if(bounds instanceof BoundingGeometry)
        {
            BoundingGeometry bg = (BoundingGeometry)bounds;
            Node target_node = bg.getProxyGeometry();

            if(target_node instanceof GroupPickTarget)
            {
                return pickAllSphere((GroupPickTarget)target_node,
                                     req,
                                     min,
                                     radius,
                                     paths,
                                     currentPath,
                                     needTransform);
            }
            else if(target_node instanceof SinglePickTarget)
            {
                return pickAllSphere((SinglePickTarget)target_node,
                                     req,
                                     min,
                                     radius,
                                     paths,
                                     currentPath,
                                     needTransform);
            }
            else if(target_node instanceof LeafPickTarget)
            {
                return pickAllSphere((LeafPickTarget)target_node,
                                     req,
                                     min,
                                     radius,
                                     paths,
                                     currentPath,
                                     needTransform);
            }
            else if(target_node instanceof CustomPickTarget)
            {
                return pickAllSphere((CustomPickTarget)target_node,
                                     req,
                                     min,
                                     radius,
                                     paths,
                                     currentPath,
                                     needTransform);
            }
            else if(target_node != null)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROXY_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                Object[] msg_args = { target_node.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);
                errorReporter.warningReport(msg, null);
            }
        }

        int found = 0;

        if(pickCustom(root, req))
        {
            int num_kids = pickInstructions.numChildren;

            float min_x = min[0];
            float min_y = min[1];
            float min_z = min[2];

            float old_radius = radius;

            // reset the transform at the top of the stack
            if(pickInstructions.hasTransform)
            {
                transformPath[lastPathIndex].set(pickInstructions.localTransform);

                matrixUtils.inverse(pickInstructions.localTransform,
                                    invertedMatrix);
                transform(invertedMatrix, min);
                float scale = (float)matrixUtils.getUniformScale(invertedMatrix);
                radius *= scale;

                validTransform[lastPathIndex] = true;
            }
            else
            {
                // don't bother setting to identity, just mark it as non-valid.
                // Assignment is a waste of CPU cycles.
                validTransform[lastPathIndex] = false;
            }

            pickPath[lastPathIndex] = root;
            lastPathIndex++;
            PickTarget[] kids = pickInstructions.children.clone();

            for(int i = 0; i < num_kids; i++)
            {
                if(kids[i] == null)
                    continue;

                switch(kids[i].getPickTargetType())
                {
                    case PickTarget.GROUP_PICK_TYPE:
                        found += pickAllSphere((GroupPickTarget)kids[i],
                                               req,
                                               min,
                                               radius,
                                               paths,
                                               currentPath + found,
                                               needTransform);
                        break;

                    case PickTarget.SINGLE_PICK_TYPE:
                        found += pickAllSphere((SinglePickTarget)kids[i],
                                               req,
                                               min,
                                               radius,
                                               paths,
                                               currentPath + found,
                                               needTransform);
                        break;

                    case PickTarget.LEAF_PICK_TYPE:
                        found += pickAllSphere((LeafPickTarget)kids[i],
                                               req,
                                               min,
                                               radius,
                                               paths,
                                               currentPath + found,
                                               needTransform);
                        break;

                    case PickTarget.CUSTOM_PICK_TYPE:
                        found += pickAllSphere((CustomPickTarget)kids[i],
                                               req,
                                               min,
                                               radius,
                                               paths,
                                               currentPath + found,
                                               needTransform);
                        break;
                }
            }

            lastPathIndex--;
            pickPath[lastPathIndex] = null;

            min[0] = min_x;
            min[1] = min_y;
            min[2] = min_z;

            radius = old_radius;
        }

        return found;
    }

    /**
     * Recurse the tree looking for intersections with a single point.
     *
     * @param geom The geom node to test against
     * @param req flags to compare against for picking
     * @param min The minimum extents of the box in local coordinate space
     * @param radius The radius of the sphere
     * @param paths A place to set the results in
     * @param needTransform True if the minal to v-world transform needs
     *    calculating
     */
    private int pickAllSphere(LeafPickTarget geom,
                              PickRequest req,
                              float[] min,
                              float radius,
                              ArrayList<SceneGraphPath> paths,
                              int currentPath,
                              boolean needTransform)
    {
        if(!geom.checkPickMask(req.pickType))
            return 0;

        int found = 0;
        BoundingVolume bounds = geom.getPickableBounds();

        if(bounds.checkIntersectionSphere(min, radius))
        {
            resizePath();
            pickPath[lastPathIndex] = geom;
            validTransform[lastPathIndex] = false;
            lastPathIndex++;

            SceneGraphPath p;

            if(currentPath >= paths.size())
            {
                p = new SceneGraphPath();
                paths.add(p);
            }
            else
                p = paths.get(currentPath);

            if(needTransform)
                buildVWorldTransform();
            else
                vworldMatrix.setIdentity();


            p.updatePath(pickPath,
                         lastPathIndex,
                         vworldMatrix,
                         invertedMatrix);

            lastPathIndex--;
            found = 1;
        }

        return found;
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
        int last_valid = 0;
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
	 * Ensure that bounding box extents are properly ordered
	 * after they have been transformed
	 *
	 * @param min_ext The minimum bound
	 * @param max_ext The maximum bound
	 */
	private void checkExtents(float[] min_ext, float[] max_ext) {
		for (int i = 0; i < 3; i++) {
			if (min_ext[i] > max_ext[i]) {
				float tmp = min_ext[i];
				min_ext[i] = max_ext[i];
				max_ext[i] = tmp;
			}
		}
	}
}
