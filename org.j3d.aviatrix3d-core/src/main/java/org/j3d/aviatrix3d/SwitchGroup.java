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

package org.j3d.aviatrix3d;

// External imports
import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.picking.PickTarget;

import org.j3d.aviatrix3d.rendering.BoundingVolume;
import org.j3d.aviatrix3d.rendering.Cullable;

/**
 * Special grouping node that allows the selection of only a single
 * child to be rendered.
 * <p>
 * If the node that is the selected index is removed, then the
 * selectedChild is automatically to be invalid. The user must reset
 * the selected child index in order for rendering to continue.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>invalidIndexRangeMsg: Error message when the switch index is
 *     too big for the amount of data provided.</li>
 * <li>nullShapeMsg: </li>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.21 $
 */
public class SwitchGroup extends Group
{
    /** Error message when the switch index is too big */
    private static final String INDEX_RANGE_PROP =
        "org.j3d.aviatrix3d.SwitchGroup.invalidIndexRangeMsg";

    /** Index to the next place to add items in the nodeList */
    private int selectedChild;

    /** The child that is currently selected for rendering */
    private PickTarget[] pickChild;

    /**
     * The default constructor
     */
    public SwitchGroup()
    {
        selectedChild = -1;
        pickChild = new PickTarget[1];
    }

    //---------------------------------------------------------------
    // Methods defined by Group
    //---------------------------------------------------------------

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

        if(idx == selectedChild && (newChild instanceof Cullable)) {
            cullList[0] = (Cullable) newChild;
        } else {
            cullList[0] = null;
        }
    }

    /**
     * Remove the child at the specified index from the group.
     *
     * @param idx The index of the child to remove
     * @throws IndexOutOfBoundsException When the idx is invalid
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void removeChild(int idx)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        if(idx == selectedChild)
        {
            selectedChild = -1;
            cullList[0] = null;
        }

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

        cullList[0] = null;
        selectedChild = -1;
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
        return selectedChild < 0 ? 0 : 1;
    }


    //---------------------------------------------------------------
    // Methods defined by Node
    //---------------------------------------------------------------

    /**
     * Internal method to recalculate the implicit bounds of this Node. By
     * default the bounds are a point sphere, so derived classes should
     * override this method with something better.
     */
    protected void recomputeBounds()
    {
        if((selectedChild == -1) || (childList[selectedChild] == null))
        {
            bounds = INVALID_BOUNDS;
            return;
        }

        BoundingVolume bds = childList[selectedChild].getBounds();

        if(bds instanceof BoundingVoid)
        {
            bounds = INVALID_BOUNDS;
            return;
        }

        bds.getExtents(wkVec1, wkVec2);

        if(bounds == null || bounds instanceof BoundingVoid)
            bounds = new BoundingBox();

        BoundingBox bbox = (BoundingBox)bounds;
        bbox.setMinimum(wkVec1);
        bbox.setMaximum(wkVec2);
    }

    //---------------------------------------------------------------
    // Methods defined by GroupPickTarget
    //---------------------------------------------------------------

    /**
     * Returns the number of valid pickable child targets to process. If there
     * are no valid children return -1.
     *
     * @return A number greater than or equal to zero or -1
     */
    public int numPickableChildren()
    {
        return (pickChild[0] != null) ? 1 : 0;
    }

    /**
     * Return an array containing all of this group's pickable children.  This
     * structure is the nodes internal representation.  Check the
     * {@link #numPickableChildren()} call for how many valid objects are part
     * of this array. If there are none, this may return either a null or a
     * valid array, depending on the implementation.
     * <p>
     *
     * The list may contain null values.
     *
     * @return An array of pick targets
     */
    public PickTarget[] getPickableChildren()
    {
        return pickChild;
    }

    /**
     * Return the pickable target instance at the given index. If there is
     * nothing at the given index, null will be returned.
     *
     * @param idx The index of the child to get
     * @return The target object at the given index.
     */
    public PickTarget getPickableChild(int idx)
    {
        return pickChild[0];
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Set the selected child to be rendered to the given index. If the index
     * is invalid, then an exception is issued. Using a value of -1 means
     * that no child will be rendered.
     *
     * @param idx The index of the child to now be rendered
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds changed callback method
     */
    public void setActiveChild(int idx)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        if(idx > lastChild)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(INDEX_RANGE_PROP) + idx;
            throw new IllegalArgumentException(msg);
        }

        selectedChild = idx < 0 ? -1 : idx;

        if (idx >= 0 && childList[idx] instanceof Cullable) {
            cullList[0] = (Cullable) childList[idx];
        } else {
            cullList[0] = null;
        }

        if(cullList[0] instanceof PickTarget)
            pickChild[0] = (PickTarget)cullList[0];
        else
            pickChild[0] = null;
    }

    /**
     * Get the currently selected active child of this switch node. If none is
     * active, -1 will be returned.
     *
     * @return The valid child index or -1
     */
    public int getActiveChild()
    {
        return selectedChild;
    }
}
