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

import org.j3d.aviatrix3d.rendering.BoundingVolume;


/**
 * Abstracted verson of the Group node designed for extension by 3rd-party node
 * implementors.
 * <p>
 *
 * Group nodes have exactly one parent and an arbitrary number of children.
 * The children are rendered in an unspecified order. Null children
 * are allowed but no operation is performed on a null child.
 * <p>
 * Lights are automatically scoped and culled to the parent group.
 * Picking is enabled by default.
 * <p>
 *
 * <h3>Implementors notes</h3>
 *
 * This class only provides the grouping structure management. It does not
 * label itself as part of the culling or picking systems. It is up to the drive
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>invalidChildIndexMsg: Error message when the user provides an index
 *     for a child that is < 0 or > the number of children.</li>
 * <li>pickTimingMsg: Error message when attempting to pick outside the app
 *     update observer callback.</li>
 * <li>notPickableMsg: Error message when the user has set the pickmask to
 *     zero and then requested a pick directly on this object.</li>
 * </ul>
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.6 $
 */
public abstract class BaseGroup extends BaseNode
    implements TransformHierarchy
{
    /** Message for the index provided being out of range */
    private static final String CHILD_IDX_ERR_PROP =
        "org.j3d.aviatrix3d.BaseGroup.invalidChildIndexMsg";

    /** The initial size of the children list */
    private static final int LIST_START_SIZE = 5;

    /** The increment size of the list if it gets overflowed */
    private static final int LIST_INCREMENT = 5;

    /** The list of children nodes */
    protected Node[] childList;

    /** Index to the next place to add items in the nodeList */
    protected int lastChild;

    /** Number of currently known dirty child bounds */
    protected int dirtyBoundsCount;

    /** Temp array for fetching bounds from the children. */
    protected float[] wkVec1;

    /** Temp array for fetching bounds from the children. */
    protected float[] wkVec2;


    /**
     * The default constructor
     */
    protected BaseGroup()
    {
        childList = new Node[LIST_START_SIZE];
        lastChild = 0;

        dirtyBoundsCount = 0;

        wkVec1 = new float[3];
        wkVec2 = new float[3];
    }

    //----------------------------------------------------------
    // Methods defined by Node
    //----------------------------------------------------------

    /**
     * Set the bounds to the given explicit value. When set, auto computation
     * of the bounds of this node is turned off. A value of null can be used
     * to clear the current explicit bounds and return to auto computation.
     *
     * @param b The new bounds to use or null to clear
     */
    public void setBounds(BoundingVolume b)
    {
        super.setBounds(b);

        // Have to reset
        dirtyBoundsCount = 0;
    }

    /**
     * Mark this node as having dirty bounds due to one of it's children having
     * their bounds changed.
     */
    protected void markBoundsDirty()
    {
        // Sanity check to make sure we can't have more things marked dirty
        // than the number of children we have
        if(dirtyBoundsCount < lastChild)
            dirtyBoundsCount++;

        // Only notify the parents that the bounds need to be updated if there
        // are implicit bounds being set.
        if(parent != null && implicitBounds && dirtyBoundsCount == 1)
        {
            parent.markBoundsDirty();
        }
    }

    /**
     * Update this node's bounds and then call the parent to update it's
     * bounds. Used to propogate bounds changes from the leaves of the tree
     * to the root. A node implementation may decide when and where to tell
     * the parent(s)s that updates are ready.
     */
    protected void updateBounds()
    {
        if(dirtyBoundsCount > 1)
        {
            dirtyBoundsCount--;
            return;
        }

        if(!implicitBounds)
            return;

        // Need to set this up to set a point source bounds if there are no
        // children.
        if(lastChild == 0)
            bounds = INVALID_BOUNDS;
        else
            recomputeBounds();

        dirtyBoundsCount = 0;

        if(parent != null)
            parent.updateBounds();
    }

    /**
     * Internal method to recalculate the implicit bounds of this Node. By
     * default the bounds are a point sphere, so derived classes should
     * override this method with something better.
     */
    protected void recomputeBounds()
    {
        if(!implicitBounds)
            return;

        if(lastChild == 0)
        {
            bounds = INVALID_BOUNDS;
            return;
        }

        BoundingVolume bds = null;
        int start_child = 0;

        // Go looking for a non-null, non-void starting bounds
        for( ; start_child < lastChild; start_child++)
        {
            if(childList[start_child] == null)
                continue;

            bds = childList[start_child].getBounds();

            if(!(bds instanceof BoundingVoid))
                break;
        }

        if(start_child == lastChild)
        {
            bounds = INVALID_BOUNDS;
            return;
        }

        bds.getExtents(wkVec1, wkVec2);

        float min_x = wkVec1[0];
        float min_y = wkVec1[1];
        float min_z = wkVec1[2];

        float max_x = wkVec2[0];
        float max_y = wkVec2[1];
        float max_z = wkVec2[2];

        // So we're down to the last update here. Time to actually
        // update the bounds based on the now-updated bounds of the
        // remaining children.
        for(int i = start_child; i < lastChild; i++)
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

        if((bounds instanceof BoundingVoid) || (bounds == null))
            bounds = new BoundingBox();

        BoundingBox bbox = (BoundingBox)bounds;
        bbox.setMinimum(min_x, min_y, min_z);
        bbox.setMaximum(max_x, max_y, max_z);
    }

    /**
     * Request a recomputation of the bounds of this object. If this object is
     * not currently live, you can request a recompute of the bounds to get the
     * most current values. If this node is currently live, then the request is
     * ignored.
     * <p>
     * This will recurse down the children asking all of them to recompute the
     * bounds. If a child is found to be during this process, that branch will
     * not update, and thus the value used will be the last updated (ie from the
     * previous frame it was processed).
     */
    public void requestBoundsUpdate()
    {
        if(alive || (lastChild == 0) || !implicitBounds)
            return;

        for(int i = 0; i < lastChild; i++)
            childList[i].requestBoundsUpdate();

        // Clear this as we are no longer dirty.
        recomputeBounds();

        dirtyBoundsCount = 0;
    }

    //----------------------------------------------------------
    // Methods defined by SceneGraphObject
    //----------------------------------------------------------

    /**
     * Check to see if this node is the same reference as the passed node that
     * is a parent of this node. This is the downwards check to ensure that
     * there is no cyclic scene graph structures at the point where someone
     * adds a node to the scenegraph. When the reference and this are the
     * same, an exception is generated. Since each class may have different
     * lists of child node setups, this should be overriden by any class that
     * can take children, and have the call passed along to the children.
     *
     * @param parent The reference to check against this class
     * @throws CyclicSceneGraphStructureException Equal parent and child
     */
    protected void checkForCyclicChild(SceneGraphObject parent)
        throws CyclicSceneGraphStructureException
    {
        if(parent == this)
            throw new CyclicSceneGraphStructureException();

        for(int i = 0; i < lastChild; i++) {
            if(childList[i] != null)
                childList[i].checkForCyclicChild(parent);
        }
    }

    /**
     * Set the scenegraph update handler for this node.  It will notify
     * all its children of the value. A null value will clear the current
     * handler.
     *
     * @param handler The instance to use as a handler
     */
    protected void setUpdateHandler(NodeUpdateHandler handler)
    {
        super.setUpdateHandler(handler);

        for(int i=0; i < lastChild; i++)
        {
            if(childList[i] != null)
                childList[i].setUpdateHandler(handler);
        }
    }

    /**
     * Notification that this object is live now.
     */
    protected void setLive(boolean state)
    {
        // Ignore stuff that doesn't change the state
        if(state == alive)
            return;

        for(int i = 0; i < lastChild; i++)
        {
            if(childList[i] != null)
                if(childList[i] instanceof MultiParentNode)
                    ((MultiParentNode)childList[i]).setLive(this, alive);
                else
                    childList[i].setLive(state);
        }

        // Call this after, that way the bounds are recalculated here with
        // the correct bounds of all the children set up.
        super.setLive(state);

        dirtyBoundsCount = 0;
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Appends the specified child node to this group node's list of children
     *
     * @param newChild The child to add
     * @throws AlreadyParentedException There is a valid parent already set
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds callback method
     */
    public void addChild(Node newChild)
        throws AlreadyParentedException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        resizeList();
        childList[lastChild] = newChild;

        lastChild++;

        if(newChild != null)
        {
            newChild.setParent(this);

            if(newChild.isLive() != alive)
            {
                if(newChild instanceof MultiParentNode)
                    ((MultiParentNode)newChild).setLive(this, alive);
                else
                   newChild.setLive(alive);
            }

            newChild.setUpdateHandler(updateHandler);
        }
    }

    /**
     * Replaces the child node at the specified index in this group
     * node's list of children with the specified child.
     *
     * @param newChild The child node to use
     * @param idx The index to replace.  Must be greater than 0 and less then numChildren
     * @throws IndexOutOfBoundsException When the idx is invalid
     * @throws AlreadyParentedException There is a valid parent already set
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds callback method
     */
    public void setChild(Node newChild, int idx)
        throws AlreadyParentedException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        if(idx > lastChild)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(CHILD_IDX_ERR_PROP);
            throw new IndexOutOfBoundsException(msg);
        }

        if(childList[idx] != null)
        {
            if(childList[idx] instanceof MultiParentNode)
                ((MultiParentNode)childList[idx]).setLive(this, false);
            else
               childList[idx].setLive(false);

            childList[idx].removeParent(this);
        }

        childList[idx] = newChild;

        if(newChild != null)
        {
            newChild.setParent(this);

            if(newChild.isLive() != alive)
            {
                if(newChild instanceof MultiParentNode)
                    ((MultiParentNode)newChild).setLive(this, alive);
                else
                   newChild.setLive(alive);
            }

            newChild.setUpdateHandler(updateHandler);
        }
    }

    /**
     * Remove the child at the specified index from the group.
     *
     * @param idx The index of the child to remove
     * @throws IndexOutOfBoundsException When the idx is invalid
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds callback method
     */
    public void removeChild(int idx)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        if(idx > lastChild)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(CHILD_IDX_ERR_PROP);
            throw new IndexOutOfBoundsException(msg);
        }

        Node old_child = childList[idx];
        lastChild--;

        System.arraycopy(childList, idx + 1, childList, idx, lastChild - idx);

        if(old_child != null)
        {
            old_child.removeParent(this);

            if(old_child instanceof MultiParentNode)
                ((MultiParentNode)old_child).setLive(this, false);
            else
               old_child.setLive(false);
        }
    }

    /**
     * Retrieves the child node at the specified index in this group node's
     * list of children.
     *
     * @return The node
     * @throws IndexOutOfBoundsException If the idx is invalid
     */
    public Node getChild(int idx)
    {
        if(idx < 0 || idx > lastChild)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(CHILD_IDX_ERR_PROP);
            throw new IndexOutOfBoundsException(msg);
        }

        return childList[idx];
    }

    /**
     * Return an array containing all of this group's children.  This
     * structure is the nodes internal representation.  Check the numChildren
     * call to determine how many entries are valid.
     *
     * @return An array of nodes
     */
    public Node[] getAllChildren()
    {
        return childList;
    }

    /**
     * Returns the number of children this group contains.
     *
     * @return The number of children
     */
    public int numChildren()
    {
        return lastChild;
    }

    /**
     * Retrieves the index of the specified child node in this group node's
     * list of children.
     *
     * @param child The child to find
     * @return the index of the child or -1 if not found
     */
    public int indexOfChild(Node child)
    {
        for(int i=0; i < lastChild; i++)
        {
            if(child == childList[i])
                return i;
        }

        return -1;
    }

    /**
     * Removes the specified child from the group.  If the child does not
     * exist its silently ignored.
     *
     * @param child The child to remove
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds callback method
     */
    public void removeChild(Node child)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        int idx = indexOfChild(child);

        if(idx == -1)
            return;

        removeChild(idx);
    }

    /**
     * Removes all children from the group.
     *
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds callback method
     */
    public void removeAllChildren()
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        // Remove all references to allow garbage collection
        for(int i = 0; i < lastChild; i++)
        {
            if(childList[i] != null)
            {
                // TODO setLive false was on the single removeChild, should be here?
                childList[i].removeParent(this);

                if(childList[i] instanceof MultiParentNode)
                    ((MultiParentNode)childList[i]).setLive(this, false);
                else
                   childList[i].setLive(false);

                childList[i] = null;
            }
        }

        lastChild = 0;
    }

    /**
     * Resize the list if needed. Marked as final in order to encourage the
     * compiler to inline the code for faster execution
     */
    private final void resizeList()
    {
        if((lastChild + 1) == childList.length)
        {
            int old_size = childList.length;
            int new_size = old_size + LIST_INCREMENT;

            Node[] tmp_nodes = new Node[new_size];

            System.arraycopy(childList, 0, tmp_nodes, 0, old_size);

            childList = tmp_nodes;
        }
    }
}
