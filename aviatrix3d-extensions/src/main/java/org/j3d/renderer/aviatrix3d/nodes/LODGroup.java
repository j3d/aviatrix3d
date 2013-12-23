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
import org.j3d.aviatrix3d.picking.*;
import org.j3d.aviatrix3d.rendering.*;

/**
 * A grouping node structure that controls which children are being rendered
 * based on distance from the user.
 * <p>
 *
 * Each child of LODGroup should have a value. The value can be interpreted as
 * measure of the the maximum distance between the child and the eye point for
 * the child to be visible. If distanceMode is true the value is interpreted as
 * the distance, if false (the default) the value is the size of characteristic
 * details in the child. The distance is then computed automatically for when
 * the characteristic details should become visible. The children should be
 * added in a most detailed to least detailed order, with monothonically
 * increasing values.
 * <p>
 *
 * When in distance mode, a center offset can be applied. This allows you to
 * have the LOD offset from the center of the geometry, if needed. If this is
 * the default value, then the center of the combined bounds of all children is
 * used to determine the range.
 * <p>
 *
 * When setting up the node, you need to have one less range value than the
 * number of children currently supplied. If not, then beyond the last set
 * range value, we will automatically choose the last child as the one to be
 * displayed.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>invalidChildIndexMsg: Error message when the user provides an index
 *     for a child that is < 0 or > the number of children.</li>
 * <li>pickTimingMsg: Error message when attempting to pick outside the app
 *     update observer callback.</li>
 * <li>notPickableMsg: Error message when the user has set the pickmask to
 *     zero and then requested a pick directly on this object.</li>
 * <li>incRangeMsg: When the range value is set, a non-increasing value was
 *     found in the list</li>
 * </ul>
 *
 * @author Rune Aasgaard, (c) SINTEF, Justin Couch
 * @version 1.0
 */
public class LODGroup extends BaseGroup
    implements CustomCullable, CustomPickTarget, PickableObject
{
    /** Message for the index provided being out of range */
    private static final String CHILD_IDX_ERR_PROP =
        "org.j3d.renderer.aviatrix3d.nodes.LODGroup.invalidChildIndexMsg";

    /** Message when attempting to pick the object at the wrong time */
    private static final String PICK_TIMING_PROP =
        "org.j3d.renderer.aviatrix3d.nodes.LODGroup.pickTimingMsg";

    /**
     * You are attempting to pick a node you have marked as not-pickable, yet
     * called the pick method on this class anyway.
     */
    private static final String PICKABLE_FALSE_PROP =
        "org.j3d.renderer.aviatrix3d.nodes.LODGroup.notPickableMsg";


    /** Message when the range values are not monotonically increasing. */
    private static final String VALUE_ORDER_PROP =
        "org.j3d.renderer.aviatrix3d.nodes.LODGroup.incRangeMsg";

    /**
     * Should the values be interpreted as node - eye distance or
     * size of smallest visible feature
     */
    private final boolean distanceMode;

    /** The array of child values used for LOD computation */
    private float[] valueArr;

    /**
     * If this is in distance mode, this array is assigned and contains each
     * value in valueArr squared.
     */
    private float[] rangeSquared;

    /** The center that we are operating from when in distance mode */
    private float[] center;

    /** Temp variable for calculating the LOD handling */
    private float[] cTmp;

    /** Flag indicating if this object is pickable currently */
    private int pickFlags;

    /**
     * The child that was selected as being active. See class docs for more
     * info
     */
    private int activeChild;

    /** Utility class for processing pick requests */
    private PickingUtils pickUtils;

    /**
     * Create a new LOD implementation that uses distance as the determining
     * visual factor.
     */
    public LODGroup()
    {
        this(true);
    }

    /**
     * Construct a LOD group that is selectable whether is uses distance or
     * smallest visual cue.
     *
     * @param distanceMode true if this should use user distance, false for
     *     visual size
     */
    public LODGroup(boolean distanceMode)
    {
        this.distanceMode = distanceMode;

        pickFlags = 0xFFFFFFFF;
        valueArr = new float[childList.length];

        for(int i = 0; i < childList.length; ++i)
            valueArr[i] = Float.MAX_VALUE;

        cTmp = new float[3];
        center = new float[3];

        if(distanceMode)
        {
            rangeSquared = new float[childList.length];

            for(int i = 0; i < childList.length; ++i)
                rangeSquared[i] = Float.MAX_VALUE;
        }

        pickUtils = new PickingUtils();
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
    @Override
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

        double camera_x = viewTransform.m03 - vworldTx.m03 - center[0];
        double camera_y = viewTransform.m13 - vworldTx.m13 - center[1];
        double camera_z = viewTransform.m23 - vworldTx.m23 - center[2];

        int render_child = -1;
        if(distanceMode)
        {
            double d_squared = (camera_x * camera_x) + (camera_y * camera_y) +
                               (camera_z * camera_z);

            for(int i = 0; i < lastChild; i++)
            {
                if(d_squared <= rangeSquared[i])
                {
                    render_child = i;
                    break;
                }
            }
        }
        else
        {
            angularRes = (float)Math.toRadians(angularRes);

            for (int i = 0; i < lastChild; ++i)
            {
                BoundingVolume ch_b = childList[i].getBounds();
                if(ch_b == null || ch_b instanceof BoundingVoid)
                    continue;

                ch_b.getCenter(cTmp);
                float radius = 0;
                if (ch_b instanceof BoundingSphere)
                    radius = ((BoundingSphere)ch_b).getRadius();
                else
                {
                    bounds.getExtents(wkVec1, wkVec2);
                    wkVec1[0] = cTmp[0] - wkVec1[0];
                    wkVec1[1] = cTmp[1] - wkVec1[1];
                    wkVec1[2] = cTmp[2] - wkVec1[2];
                    wkVec2[0] -= cTmp[0];
                    wkVec2[1] -= cTmp[1];
                    wkVec2[2] -= cTmp[2];
                    if(wkVec1[0] < wkVec2[0])
                        wkVec1[0] = wkVec2[0];

                    if(wkVec1[1] < wkVec2[1])
                        wkVec1[1] = wkVec2[1];

                    if(wkVec1[2] < wkVec2[2])
                        wkVec1[2] = wkVec2[2];

                    radius = (float)Math.sqrt(wkVec1[0] * wkVec1[0]+
                                              wkVec1[1] * wkVec1[1]+
                                              wkVec1[2] * wkVec1[2]);
                }

                cTmp[0] -= camera_x;
                cTmp[1] -= camera_y;
                cTmp[2] -= camera_z;

                float dist = (float)Math.sqrt(cTmp[0] * cTmp[0] +
                                              cTmp[1] * cTmp[1] +
                                              cTmp[2] * cTmp[2]) - radius;

                if(dist * angularRes <= valueArr[i])
                {
                    render_child = i;
                    break;
                }
            }
        }

        output.hasTransform = false;

        if(render_child >= 0)
        {
            if(output.children == null || output.children.length < 1)
                output.resizeChildren(1);

            output.numChildren = 1;
            output.children[0] = (Cullable)childList[render_child];
        }
        else
            output.numChildren = 0;
    }

    //---------------------------------------------------------------
    // Methods defined by CustomPickTarget
    //---------------------------------------------------------------

    /**
     * This node is being subjected to picking, so process the provided data
     * and return the instructions on the list of available children and any
     * transformation information to the system.
     * <p>
     *
     * @param output Fill in the results of the picking evaluation here
     * @param vworldTx The transformation from the root of the scene to
     *    this node according to the current traversal path
     * @param request The picking request made of this object
     */
    public void pickChildren(PickInstructions output,
                             Matrix4d vworldTx,
                             PickRequest request)
    {
        output.hasTransform = false;

        // NOTE:
        // Not entirely certain that the request.origin[] is the correct
        // thing to use in all cases.
        double camera_x = request.origin[0] - vworldTx.m03 - center[0];
        double camera_y = request.origin[1] - vworldTx.m13 - center[1];
        double camera_z = request.origin[2] - vworldTx.m23 - center[2];

        if(distanceMode)
        {
            int render_child = -1;
            double d_squared = (camera_x * camera_x) + (camera_y * camera_y) +
                               (camera_z * camera_z);

            for(int i = 0; i < lastChild; i++)
            {
                if(d_squared <= rangeSquared[i])
                {
                    render_child = i;
                    break;
                }
            }

            if((render_child != -1) &&
               (childList[render_child] instanceof PickTarget))
            {
                output.resizeChildren(1);
                output.numChildren = 1;
                output.children[0] = (PickTarget)childList[render_child];
            }
        }
        else
        {
            // If we are in screen resolution mode,
            // use the highest resolution available child
            PickTarget child = null;
            for(int i = 0; child == null && i < lastChild; i++)
            {
                if(childList[i] instanceof PickTarget)
                    child = (PickTarget)childList[i];
            }

            if(child != null)
            {
                output.resizeChildren(1);
                output.numChildren = 1;
                output.children[0] = child;
            }
        }
    }

    //---------------------------------------------------------------
    // Methods defined by PickTarget
    //---------------------------------------------------------------

    /**
     * Return the type constant that represents the type of pick target this
     * is. Used to provided optimised picking implementations.
     *
     * @return One of the _PICK_TYPE constants
     */
    @Override
    public final int getPickTargetType()
    {
        return PickTarget.CUSTOM_PICK_TYPE;
    }

    /**
     * Check the given pick mask against the node's internal pick mask
     * representation. If there is a match in one or more bitfields then this
     * will return true, allowing picking to continue to process for this
     * target.
     *
     * @param mask The bit mask to check against
     * @return true if the mask has an overlapping set of bitfields
     */
    @Override
    public boolean checkPickMask(int mask)
    {
        return ((pickFlags & mask) != 0);
    }

    /**
     * Get the bounds of this picking target so that testing can be performed
     * on the object.
     *
     * @return A representation of the volume representing the pickable objects
     */
    @Override
    public BoundingVolume getPickableBounds()
    {
        return bounds;
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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

    //-------------------------------------------------
    // Methods defined by Group
    //-------------------------------------------------

    /**
     * Appends the specified child node to this group node's list of children
     *
     * @param newChild The child to add
     * @throws AlreadyParentedException There is a valid parent already set
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds changed callback method
     */
    @Override
    public void addChild(Node newChild)
        throws AlreadyParentedException, InvalidWriteTimingException
    {
        super.addChild(newChild);

        if(childList.length > valueArr.length)
        {
            float[] tmp_fa = new float[childList.length];
            for(int i = 0; i < lastChild; ++i)
                tmp_fa[i] = valueArr[i];

            for(int i = lastChild; i < tmp_fa.length; ++i)
                tmp_fa[i] = Float.MAX_VALUE;

            valueArr = tmp_fa;

            if(distanceMode)
            {
                tmp_fa = new float[childList.length];
                System.arraycopy(rangeSquared, 0, tmp_fa, 0, lastChild - 1);

                for(int i = lastChild; i < tmp_fa.length; ++i)
                    tmp_fa[i] = Float.MAX_VALUE;

                rangeSquared = tmp_fa;
            }
        }
    }

    /**
     * Remove the child at the specified index from the group.
     *
     * @param idx The index of the child to remove
     * @throws IndexOutOfBoundsException When the idx is invalid
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds changed callback method
     */
    @Override
    public void removeChild(int idx)
        throws InvalidWriteTimingException
    {
        super.removeChild(idx);
        for(int i = idx; i < lastChild; ++i)
            valueArr[i] = valueArr[i + 1];

        if(distanceMode)
        {
            for(int i = idx; i < lastChild; ++i)
                rangeSquared[i] = rangeSquared[i + 1];
        }

        valueArr[lastChild] = Float.MAX_VALUE;
    }

    /**
     * Removes all children from the group.
     *
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    @Override
    public void removeAllChildren()
        throws InvalidWriteTimingException
    {
        super.removeAllChildren();

        for(int i = lastChild; i < valueArr.length; ++i)
          valueArr[i] = Float.MAX_VALUE;

        if(distanceMode)
        {
            for(int i = lastChild; i < valueArr.length; ++i)
               rangeSquared[i] = Float.MAX_VALUE;
        }
    }

    //-------------------------------------------------
    // Local methods
    //-------------------------------------------------

    /**
     * Get the range set at the given child index.
     *
     * @param idx The range index to get the value from
     * @throws IndexOutOfBoundsException The index is negative or past the
     *   last child
     */
    public float getRange(int idx)
    {
        if(idx < 0 || idx > lastChild)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(CHILD_IDX_ERR_PROP);
            throw new IndexOutOfBoundsException(msg);
        }

        return valueArr[idx];
    }

    /**
     * Set the range at the given child index. The value at this index is
     * required to be monotonically increasing relative to the previous and next
     * index.
     *
     * @param idx The range index to get the value from
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data callback method
     * @throws IndexOutOfBoundsException The index is negative or past the
     *   last child
     * @throws IllegalArgumentException The value is not monotonically
     *   increasing
     */
    public void setRange(int idx, float value)
        throws InvalidWriteTimingException,
               IndexOutOfBoundsException,
               IllegalArgumentException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(idx < 0 || idx > lastChild)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(CHILD_IDX_ERR_PROP);
            throw new IndexOutOfBoundsException(msg);
        }

        if((idx + 1 < lastChild && valueArr[idx + 1] <= value) ||
            (idx - 1 >= 0 && valueArr[idx - 1] >= value))
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(CHILD_IDX_ERR_PROP);
            throw new IllegalArgumentException(msg);
        }

        valueArr[idx] = value;

        if(distanceMode)
            rangeSquared[idx] = value * value;
    }

    /**
     * Get the current center location. If this LOD is not using distance mode, the
     * request is ignored and the given array returns unchanged.
     *
     * @param center The array to copy the center value into
     */
    public void getCenter(float[] center)
    {
        if(distanceMode)
        {
            center[0] = this.center[0];
            center[1] = this.center[1];
            center[2] = this.center[2];
        }
    }

    /**
     * Set the center that the LOD would use to determine distance from when using
     * distance mode. This request is ignored when the mode is not working on distance.
     *
     * @param center The value of the center to use
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data callback method
     */
    public void setCenter(float[] center)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(distanceMode)
        {
            this.center[0] = center[0];
            this.center[1] = center[1];
            this.center[2] = center[2];
        }
    }

    /**
     * Find out which of the two modes are being used by this LOD.
     */
    public boolean isDistanceMode()
    {
        return distanceMode;
    }
}
