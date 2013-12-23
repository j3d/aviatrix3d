/*****************************************************************************
 *                     SINTEF Copyright (c) 2004-2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.renderer.aviatrix3d.nodes;

// External imports
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector4d;
import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.rendering.*;
import org.j3d.aviatrix3d.picking.*;

/**
 * A class that automatically orients its children towards the camera location.
 * <p>
 *
 * This class is design to operate in a shared scene graph structure. It works
 * with the Cullable interface to make sure that regardless of the traversal
 * path, it will have the children pointing towards the camera location. This
 * makes it safe to share between layers as well as normal scene graph usage.
 * Correctness is ensured regardless of culling traversal.
 * <p>
 * Bounds of this object is represented as a spherical object that is based on the
 * largest dimension of all the children combined.
 * <p>
 *
 * In order to use this node effectively, you will need to use the
 * {@link org.j3d.aviatrix3d.pipeline.graphics.FrustumCullStage}  to process the
 * children. It uses custom culling routines internally and that is the only cull
 * stage that will do something useful with ths node.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>pickTimingMsg: Error message when attempting to pick outside the app
 *     update observer callback.</li>
 * <li>notPickableMsg: Error message when the user has set the pickmask to
 *     zero and then requested a pick directly on this object.</li>
 * </ul>
 *
 * @author Rune Aasgaard, (c) SINTEF, Justin Couch
 * @version $Revision: 1.8 $
 */
public class Billboard extends BaseGroup
    implements CustomCullable, CustomPickTarget, PickableObject
{
    /** Message when attempting to pick the object at the wrong time */
    private static final String PICK_TIMING_PROP =
        "org.j3d.aviatrix3d.Group.pickTimingMsg";

    /**
     * You are attempting to pick a node you have marked as not-pickable, yet
     * called the pick method on this class anyway.
     */
    private static final String PICKABLE_FALSE_PROP =
        "org.j3d.aviatrix3d.Group.notPickableMsg";


    /** Sharable version of the null bounds object for those that need it */
    private static final BoundingVoid INVALID_BOUNDS = new BoundingVoid();

    /** Point or cylinder mode? */
    private final boolean pointMode;

    /** Flag indicating if this object is pickable currently */
    private int pickFlags;

    /** Working vector needed for bounds calculation */
    private float[] wkVec1;

    /** Working vector needed for bounds calculation */
    private float[] wkVec2;

    /**
     * Construct a default billboard that uses point mode for the default axis.
     */
    public Billboard()
    {
        this(true);
    }

    /**
     * Create a new billboard with the option of rotating around a point in space or
     * any nominal axis.
     *
     * @param pointMode true if this should be rotating around a point in space
     */
    public Billboard(boolean pointMode)
    {
        this.pointMode = pointMode;
        pickFlags = 0xFFFFFFFF;
        bounds = null;
        wkVec1 = new float[3];
        wkVec2 = new float[3];
    }

    //-------------------------------------------------
    // Methods defined by CustomCullable
    //-------------------------------------------------

    /**
     * Check this node for children to traverse. The angular resolution is
     * defined as Field Of View (in radians) / viewport width in pixels.
     *
     * @param output Fill in the child information here
     * @param vworldTx The transformation from the root of the scene to
     *    this node according to the current traversal path
     * @param viewTransform The transformation from the root of the scene
     *    graph to the active viewpoint
     * @param frustumPlanes Listing of frustum planes in the order: right,
     *    left, bottom, top, far, near
     * @param angularRes Angular resolution of the screen, or 0 if not
     *    calculable from the available data.
     */
    public void cullChildren(CullInstructions output,
                             Matrix4d vworldTx,
                             Matrix4d viewTransform,
                             Vector4d[] frustumPlanes,
                             float angularRes)
    {
        if(BoundingVolume.FRUSTUM_ALLOUT ==
           bounds.checkIntersectionFrustum(frustumPlanes, vworldTx))
        {
            output.hasTransform = false;
            output.numChildren = 0;
            return;
        }

        // All positions are transformed to the world coordinate
        // system to avoid matrix inversions

        // Compute camera - object vector
        double camera_x = viewTransform.m03 - vworldTx.m03;
        double camera_y = viewTransform.m13 - vworldTx.m13;
        double camera_z = viewTransform.m23 - vworldTx.m23;
        double d = Math.sqrt(camera_x*camera_x+camera_y*camera_y+camera_z*camera_z);
        camera_x /= d;
        camera_y /= d;
        camera_z /= d;

        // Transformed up vector (0,1,0)
        double up_x = vworldTx.m01;
        double up_y = vworldTx.m11;
        double up_z = vworldTx.m21;
        d = Math.sqrt(up_x*up_x+up_y*up_y+up_z*up_z);
        up_x /= d;
        up_y /= d;
        up_z /= d;

        double siny = camera_x*up_x+camera_y*up_y+camera_z*up_z;
        double cosy = 1 - siny*siny;
        double sin = 0;
        double cos = 1;
        if(cosy < 1e-5)
        {
            cosy = 0;
            siny = 1;
        }
        else
        {
            cosy = Math.sqrt(cosy);

            // Project camera into x-z plane
            camera_x -= up_x*siny;
            camera_y -= up_y*siny;
            camera_z -= up_z*siny;

            // Transformed front vector (0,0,1)
            double front_x = vworldTx.m02;
            double front_y = vworldTx.m12;
            double front_z = vworldTx.m22;

            // Front dot camera, normalized
            cos = camera_x*front_x + camera_y*front_y + camera_z*front_z;
            cos /= cosy*Math.sqrt(front_x*front_x+front_y*front_y+front_z*front_z);

            // Transformed right vector (1,0,0)
            double right_x = vworldTx.m00;
            double right_y = vworldTx.m10;
            double right_z = vworldTx.m20;

            // Right dot camera, normalized
            sin = camera_x*right_x + camera_y*right_y + camera_z*right_z;
            sin /= cosy*Math.sqrt(right_x*right_x+right_y*right_y+right_z*right_z);
        }

        // Set matrix values
        if(pointMode)
        {
            output.localTransform.m00 =  (float)cos;
            output.localTransform.m01 = -(float)(sin*siny);
            output.localTransform.m02 =  (float)(sin*cosy);

            output.localTransform.m10 =   0;
            output.localTransform.m11 =  (float)cosy;
            output.localTransform.m12 =  (float)siny;

            output.localTransform.m20 = -(float)sin;
            output.localTransform.m21 = -(float)(cos*siny);
            output.localTransform.m22 =  (float)(cos*cosy);

            output.localTransform.m03 = 0;
            output.localTransform.m13 = 0;
            output.localTransform.m23 = 0;
            output.localTransform.m30 = 0;
            output.localTransform.m31 = 0;
            output.localTransform.m32 = 0;
            output.localTransform.m33 = 1;
        }
        else
        {
            output.localTransform.m00 = output.localTransform.m22 = (float)cos;
            output.localTransform.m02 =  (float)sin;
            output.localTransform.m20 = -(float)sin;
            output.localTransform.m11 = output.localTransform.m33 = 1;
            output.localTransform.m01 = output.localTransform.m10 = 0;
            output.localTransform.m03 = output.localTransform.m30 = 0;
            output.localTransform.m12 = output.localTransform.m21 = 0;
            output.localTransform.m13 = output.localTransform.m31 = 0;
            output.localTransform.m23 = output.localTransform.m32 = 0;
        }

        // And the rest of the output data...
        output.hasTransform = true;
        if(output.children.length <lastChild)
            output.resizeChildren(lastChild);
        output.numChildren = 0;

        for(int i=0; i<lastChild; ++i)
        {
            if(childList[i] instanceof Cullable)
                output.children[output.numChildren++] = (Cullable)childList[i];
        }
    }

    //---------------------------------------------------------------
    // Methods defined by CustomPickTarget
    //---------------------------------------------------------------

    /**
     * This node is being subjected to picking, so process the provided data and return
     * the instructions on the list of available children and any transformation
     * information to the system.
     *
     * @param output Fill in the picking results here
     * @param vworldTx The transformation from the root of the scene to this node
     *   according to the current traversal path
     * @param req The details of the picking request that are to be processed
     */
    public void pickChildren(PickInstructions output,
                             Matrix4d vworldTx,
                             PickRequest req)
    {
        // NOTE:
        // Not entirely certain that the request.origin[] is the correct
        // thing to use in all cases.
        computeBbTx(output.localTransform, vworldTx,
                req.origin[0], req.origin[1], req.origin[2]);

        output.hasTransform = true;
        output.resizeChildren(lastChild);
        output.numChildren = 0;

        for(int i = 0; i < lastChild; ++i)
        {
            if(childList[i] instanceof PickTarget)
                output.children[output.numChildren++] = (PickTarget)childList[i];
        }
    }

    //---------------------------------------------------------------
    // Methods defined by PickTarget
    //---------------------------------------------------------------

    /**
     * Check the given pick mask against the node's internal pick mask representation.
     * If there is a match in one or more bitfields then this will return true,
     * allowing picking to continue to process for this target.
     *
     * @param mask The bit mask to check against
     * @return True if the mask has an overlapping set of bitfields
     */
    public boolean checkPickMask(int mask)
    {
        return ((pickFlags & mask) != 0);
    }

    /**
     * Get the bounds of this picking target so that testing can be performed on the
     *  object.
     *
     * @return The bounding volume defining the pickable target space
     */
    public BoundingVolume getPickableBounds()
    {
        return getBounds();
    }

    /**
     * Return the type constant that represents the type of pick target this
     * is. Used to provided optimised picking implementations.
     *
     * @return The CUSTOM_PICK_TYPE constant
     */
    public int getPickTargetType()
    {
        return PickTarget.CUSTOM_PICK_TYPE;
    }

    //---------------------------------------------------------------
    // Methods defined by PickableObject
    //---------------------------------------------------------------

    /**
     * Set the node as being pickable currently using the given bit mask.
     * A mask of 0 will completely disable picking.
     *
     * @param state A bit mask of available options to pick for
     */
    public void setPickMask(int state)
    {
        pickFlags = state;
    }

    /**
     * Get the current pickable state mask of this object. A value of zero
     * means it is completely unpickable.
     *
     * @return A bit mask of available options to pick for
     */
    public int getPickMask()
    {
        return pickFlags;
    }

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given set of requests.
     *
     * @param reqs The list of picks to be made, starting at this object
     * @param numRequests The number of valid pick requests to process
     * @throws NotPickableException This object has been marked as non pickable,
     *   but you decided to try to call the method anyway
     * @throws InvalidPickTimingException An attempt was made to pick outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void pickBatch(PickRequest[] reqs, int numRequests)
        throws NotPickableException, InvalidPickTimingException
    {
        if(updateHandler == null || !updateHandler.isPickingPermitted())
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(PICK_TIMING_PROP);
            throw new InvalidPickTimingException(msg);
        }

        if(pickFlags == 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(PICKABLE_FALSE_PROP);
            throw new NotPickableException(msg);
        }

        PickingManager picker = updateHandler.getPickingManager();

        picker.pickBatch(this, reqs, numRequests);
    }

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given set of requests.
     *
     * @param req The details of the pick to be made
     * @throws NotPickableException This object has been marked as non pickable,
     *   but you decided to try to call the method anyway
     * @throws InvalidPickTimingException An attempt was made to pick outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void pickSingle(PickRequest req)
        throws NotPickableException, InvalidPickTimingException
    {
        if(updateHandler == null || !updateHandler.isPickingPermitted())
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(PICK_TIMING_PROP);
            throw new InvalidPickTimingException(msg);
        }

        if(pickFlags == 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(PICKABLE_FALSE_PROP);
            throw new NotPickableException(msg);
        }

        PickingManager picker = updateHandler.getPickingManager();

        picker.pickSingle(this, req);
    }

    //-------------------------------------------------------------------------
    // Methods defined by Node
    //-------------------------------------------------------------------------

    /**
     * Implementation of recompute bounds.
     * Expands the boundingbox to compensate for rotation of the children
     */
    protected void recomputeBounds()
    {
        if(!implicitBounds)
            return;

        super.recomputeBounds();
        if(bounds == INVALID_BOUNDS)
            return;

        bounds.getExtents(wkVec1, wkVec2);
        float x = (wkVec1[0] + wkVec2[0])/2;
        float y = (wkVec1[1] + wkVec2[1])/2;
        float z = (wkVec1[2] + wkVec2[2])/2;
        float dx = x - wkVec1[0];
        float dy = y - wkVec1[1];
        float dz = z - wkVec1[2];

        if(pointMode)
            dx = dy = dz = (float)Math.sqrt(dx*dx+dy*dy+dz*dz);
        else
            dx = dz = (float)Math.sqrt(dx*dx+dz*dz);

        BoundingBox bbox = (BoundingBox)bounds;
        bbox.setMinimum(x-dx, y-dy, z-dz);
        bbox.setMaximum(x+dx, y+dy, z+dz);
    }

    //-------------------------------------------------------------------------
    // Local Methods
    //-------------------------------------------------------------------------

    /**
     * Check to see whether this billboard is operating as a point in space or along
     * an axis.
     *
     * @return true if this is rotating around a point in space
     */
    public boolean isPointMode()
    {
        return pointMode;
    }

    /**
     * Set the axis of rotation used when the billboard is not operating in point
     * mode. If the billboard is in point mode, the values set by this method are
     * ignored. Setting an axis with all three components equal to zero is an error
     * if this is not in point mode. In point mode, no checks are performed.
     *
     * @param axis An array containing the 3 axis coordinates
     */
    public void setAxisOfRotation(float[] axis)
    {
    }

    /**
     * Fetch the currently set axis of rotation.
     *
     * @param axis An array of at least length 3 to copy the rotation axis into
     */
    public void getAxisOfRotation(float[] axis)
    {
    }

    /**
     * Compute the BillBoard transform, turn towards camera
     *
     * @param bbTx Billboard transform, fill inn here
     * @param vworldTx World transform
     * @param cameraX Camera coordinate
     * @param cameraY Camera coordinate
     * @param cameraZ Camera coordinate
     */
    private void computeBbTx(Matrix4d bbTx,
                             Matrix4d vworldTx,
                             double cameraX,
                             double cameraY,
                             double cameraZ)
    {
               // All positions are transformed to the world coordinate
        // system to avoid matrix inversions

        // Compute camera - object vector
        double c_x = cameraX - vworldTx.m03;
        double c_y = cameraY - vworldTx.m13;
        double c_z = cameraZ - vworldTx.m23;
        double d = Math.sqrt(c_x * c_x + c_y * c_y + c_z * c_z);

        if(d != 0)
        {
            cameraX /= d;
            cameraY /= d;
            cameraZ /= d;
        }

        // Transformed up vector (0,1,0)
        double up_x = vworldTx.m01;
        double up_y = vworldTx.m11;
        double up_z = vworldTx.m21;
        d = Math.sqrt(up_x * up_x + up_y * up_y + up_z * up_z);
        up_x /= d;
        up_y /= d;
        up_z /= d;

        double siny = c_x * up_x + c_y * up_y + c_z * up_z;
        double cosy = 1 - siny * siny;
        double sin = 0;
        double cos = 1;

        if(cosy < 1e-5)
        {
            cosy = 0;
            siny = 1;
        }
        else
        {
            cosy = Math.sqrt(cosy);

            // Project camera into x-z plane
            c_x -= up_x * siny;
            c_y -= up_y * siny;
            c_z -= up_z * siny;

            // Transformed front vector (0,0,1)
            double front_x = vworldTx.m02;
            double front_y = vworldTx.m12;
            double front_z = vworldTx.m22;

            // Front dot camera, normalized
            cos = c_x * front_x + c_y * front_y + c_z * front_z;
            cos /= cosy * Math.sqrt(front_x * front_x +
                                    front_y * front_y +
                                    front_z * front_z);

            // Transformed right vector (1,0,0)
            double right_x = vworldTx.m00;
            double right_y = vworldTx.m10;
            double right_z = vworldTx.m20;

            // Right dot camera, normalized
            sin = c_x * right_x + c_y * right_y + c_z * right_z;

            sin /= cosy * Math.sqrt(right_x * right_x +
                                    right_y * right_y +
                                    right_z * right_z);
        }

        // Set matrix values
        if(pointMode)
        {
            bbTx.m00 =  (float)cos;
            bbTx.m01 = -(float)(sin*siny);
            bbTx.m02 =  (float)(sin*cosy);

            bbTx.m10 =   0;
            bbTx.m11 =  (float)cosy;
            bbTx.m12 =  (float)siny;

            bbTx.m20 = -(float)sin;
            bbTx.m21 = -(float)(cos*siny);
            bbTx.m22 =  (float)(cos*cosy);

            bbTx.m03 = 0;
            bbTx.m13 = 0;
            bbTx.m23 = 0;
            bbTx.m30 = 0;
            bbTx.m31 = 0;
            bbTx.m32 = 0;
            bbTx.m33 = 1;
        }
        else
        {
            bbTx.m00 = bbTx.m22 = (float)cos;
            bbTx.m02 =  (float)sin;
            bbTx.m20 = -(float)sin;
            bbTx.m11 = bbTx.m33 = 1;
            bbTx.m01 = bbTx.m10 = 0;
            bbTx.m03 = bbTx.m30 = 0;
            bbTx.m12 = bbTx.m21 = 0;
            bbTx.m13 = bbTx.m31 = 0;
            bbTx.m23 = bbTx.m32 = 0;
        }
    }
}
