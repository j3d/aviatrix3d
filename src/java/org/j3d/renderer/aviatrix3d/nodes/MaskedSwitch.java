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

package org.j3d.renderer.aviatrix3d.nodes;

// External imports
import javax.vecmath.Matrix4f;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.rendering.BoundingVolume;
import org.j3d.aviatrix3d.rendering.Cullable;

/**
 * Special grouping node that allows the selection of only a mask of
 * children to be rendered.
 * <p>
 *
 * If the mask has less entries then the children list the unspecified
 * children will not be displayed.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.10 $
 */
public class MaskedSwitch extends Group
{
    /** The children to display */
    private boolean[] mask;

    /** The number of renderable children */
    private int numRenderedChild;

    /**
     * The default constructor.  Nothing will be shown.
     */
    public MaskedSwitch()
    {
        mask = new boolean[0];
    }

    /**
     * Constructor with mask specified.
     */
    public MaskedSwitch(boolean[] mask)
    {
        this.mask = mask;
    }

    //----------------------------------------------------------
    // Methods defined by GroupCullable
    //----------------------------------------------------------

    /**
     * Get the list of children that are valid to be rendered according to
     * the rules of the grouping node.
     *
     * @return an array of nodes
     */
    public Cullable[] getCullableChildren()
    {
        return cullList;
    }

    /**
     * Returns the number of valid renderable children to process. If there are
     * no valid renderable children return -1.
     *
     * @return A number greater than or equal to zero or -1
     */
    public int numCullableChildren()
    {
        return numRenderedChild;
    }

    //----------------------------------------------------------
    // Methods defined by Group
    //----------------------------------------------------------

    /**
     * Replaces the child node at the specified index in this group
     * node's list of children with the specified child.
     *
     * @param newChild The child node to use
     * @param idx The index to replace.  Must be greater than 0 and less then numChildren
     * @throws IndexOutOfBoundsException When the idx is invalid
     */
    public void setChild(Node newChild, int idx)
        throws InvalidWriteTimingException
    {
        super.setChild(newChild, idx);

        if(idx < mask.length -1 && mask[idx])
            rebuildRenderedChild();
    }

    /**
     * Remove the child at the specified index from the group.
     *
     * @param idx The index of the child to remove
     * @throws IndexOfBoundsException When the idx is invalid
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void removeChild(int idx)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());


        if(idx < mask.length -1 && mask[idx])
            rebuildRenderedChild();

        super.removeChild(idx);
    }

    /**
     * Removes all children from the group.
     *
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void removeAllChildren()
        throws InvalidWriteTimingException
    {
        super.removeAllChildren();

        // Resize array to lower rendering cost
        numRenderedChild = 0;
        if(cullList.length > 1)
            cullList = new Cullable[1];

        cullList[0] = null;
    }

    //----------------------------------------------------------
    // Methods defined by Node
    //----------------------------------------------------------

    /**
     * Internal method to recalculate the implicit bounds of this Node. By
     * default the bounds are a point sphere, so derived classes should
     * override this method with something better.
     */
    protected void recomputeBounds()
    {
        if(mask.length == 0)
        {
            bounds = INVALID_BOUNDS;
            return;
        }

        int len = Math.min(mask.length, childList.length);

        BoundingVolume bds = null;
        float min_x = Float.POSITIVE_INFINITY;
        float min_y = Float.POSITIVE_INFINITY;
        float min_z = Float.POSITIVE_INFINITY;

        float max_x = Float.NEGATIVE_INFINITY;
        float max_y = Float.NEGATIVE_INFINITY;
        float max_z = Float.NEGATIVE_INFINITY;

        for(int i=0; i < len; i++)
        {
            if(childList[i] == null)
                continue;

            bds = childList[i].getBounds();

            if(bds instanceof BoundingVoid)
                continue;

            bds.getExtents(wkVec1, wkVec2);

            if(wkVec1[0] < min_x)
                min_x = wkVec1[0];

            if(wkVec1[1] < min_y)
                min_y = wkVec1[1];

            if(wkVec1[2] < min_z)
                min_z = wkVec1[2];

            if(wkVec2[0] > max_x)
                max_x = wkVec2[0];

            if(wkVec2[1] > max_y)
                max_y = wkVec2[1];

            if(wkVec2[2] > max_z)
                max_z = wkVec2[2];

        }

        if(bds == null)
        {
            bounds = INVALID_BOUNDS;
            return;
        }

        bds.getExtents(wkVec1, wkVec2);

        if(bounds == null || bounds instanceof BoundingVoid)
            bounds = new BoundingBox();

        BoundingBox bbox = (BoundingBox)bounds;
        bbox.setMinimum(min_x, min_y, min_z);
        bbox.setMaximum(max_x, max_y, max_z);
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Set the mask to change which objects are to be visible of all the
     * children. If the length of the mask is longer than the number of
     * children, any extras are ignored.
     *
     * @param mask The new mask
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void setMask(boolean[] mask)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        if(mask == null)
            this.mask = new boolean[0];
        else
        {
            if(this.mask.length != mask.length)
                this.mask = new boolean[mask.length];

            System.arraycopy(mask, 0, this.mask, 0, mask.length);
        }

        rebuildRenderedChild();
    }

    /**
     * Get the current mask. The array passed in should be at least
     * {@link #getMaskLength()} long.
     *
     * @param mask The array to copy the values into
     */
    public void getMask(boolean[] mask)
    {
        System.arraycopy(this.mask, 0, mask, 0, this.mask.length);
    }

    /**
     * Request the current length of the mask set.
     *
     * @return A number greater than or equal to zero
     */
    public int getMaskLength()
    {
        return mask.length;
    }

    /**
     * Rebuild the renderedChild list.  Rebuilt each time to lower
     * later rendering costs.
     */
    private void rebuildRenderedChild()
    {
        int len = mask.length;
        int num_visible = 0;

        for(int i = 0;i < len; i++)
        {
            if(mask[i])
                num_visible++;
        }

        len = Math.min(len, childList.length);

        if(num_visible == 0)
        {
            numRenderedChild = 0;
            cullList = new Cullable[1];
            cullList[0] = null;

            return;
        }
        else if(cullList.length != num_visible)
        {
            cullList = new Cullable[num_visible];
        }

        numRenderedChild = Math.min(num_visible, len);

        int idx = 0;
        for(int i = 0;i < len; i++)
        {
            if(mask[i] && (childList[i] != null) &&
               (childList[i] instanceof Cullable))
                cullList[idx++] = (Cullable)childList[i];
        }
    }
}
