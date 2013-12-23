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
 * A Node class is the base class for all renderable nodes in the SceneGraph.
 * <p>
 *
 * Unless otherwise overridden, all nodes are only single parented and have a
 * bounds object. By default the bounds are void (ie not even a point in space)
 * and do not contribute this node to the scene graph's bounds.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>hasParentMsg: Error message when the (internal) caller tries to
 *     call setParent() when this class already has a parent.</li>
 * </ul>
 *
 * @author Alan Hudson
 * @version $Revision: 1.34 $
 */
public abstract class Node extends SceneGraphObject
{

    /** Message for when the node is currently owned */
    private static final String CURRENT_PARENT_PROP =
        "org.j3d.aviatrix3d.Node.hasParentMsg";

    /** Sharable version of the null bounds object for those that need it */
    protected static final BoundingVoid INVALID_BOUNDS = new BoundingVoid();

    /** The parent of this node */
    protected Node parent;

    /** Bounding volume set by the user */
    protected BoundingVolume bounds;

    /** Was the bounds automatically calculated? */
    protected boolean implicitBounds;

    /** Update handler for the external code. Not created until needed. */
    private InternalUpdater internalUpdater;

    /**
     * Internal implementation of the InternalNodeUpdateListener. Done as an
     * inner class to hide the calls from public consumption.
     */
    private class InternalUpdater
        implements InternalNodeUpdateListener
    {

        /**
         * Notify this node to update it's bounds now and propogate those new
         * bounds to their parent(s).
         */
        public void updateBoundsAndNotify()
        {
            updateBounds();
        }
    }

    /**
     * Construct a new instance of this node, with implicit bounds calculation.
     */
    protected Node()
    {
        implicitBounds = true;
        internalUpdater = new InternalUpdater();
    }

    //----------------------------------------------------------
    // Methods defined by SceneGraphObject
    //----------------------------------------------------------

    /**
     * Check to see if this node is the same reference as the passed node.
     * This is the upwards check to ensure that there is no cyclic scene graph
     * structures at the point where someone adds a node to the scenegraph.
     * When the reference and this are the same, an exception is generated.
     * If not, then the code will find the parent of this class and invoke
     * this same method on the parent.
     *
     * @param child The reference to check against this class
     * @throws CyclicSceneGraphStructureException Equal parent and child
     */
    @Override
    protected void checkForCyclicParent(SceneGraphObject child)
        throws CyclicSceneGraphStructureException
    {
        super.checkForCyclicParent(child);

        if(parent != null)
            parent.checkForCyclicParent(child);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Specify this nodes parent. Should not be called directly by external
     * callers. Setting a value of null will clear the existing parent. Bit
     * broken right now as it doesn't handle multiple-parents like needed in
     * a proper scene graph. If the node has a parent already, an exception
     * is generated. Note that this method is ignored if the derived type is
     * SharedGroup.
     *
     * @param p The new parent instance to call or null
     * @throws AlreadyParentedException There is a valid parent already set
     */
    protected void setParent(Node p)
        throws AlreadyParentedException
    {
        if(p != null && parent != null)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(CURRENT_PARENT_PROP);
            throw new AlreadyParentedException(msg);
        }

        parent = p;
    }

    /**
     * Remove a parent from this node. An alternate way to remove a parent
     * from the list.
     *
     * @param p The new parent instance to remove from the list
     */
    protected void removeParent(Node p)
    {
        parent = null;
    }

    /**
     * Get the current parent of this node. If no parent is set, return
     * null. Also, note that the behaviour of this method is to always
     * return null if the derived type of this class is SharedGroup.
     *
     * @return The current parent instance of the node
     */
    public Node getParent()
    {
        return parent;
    }

    /**
     * Notify the node that you have updates to the node that might alter
     * its bounds.
     *
     * @param l The change requestor
     * @throws InvalidListenerSetTimingException If called when the node is not live or
     *   if called during one of the bounds/data changed callbacks
     */
    public void boundsChanged(NodeUpdateListener l)
        throws InvalidListenerSetTimingException
    {
        if(!isLive())
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(LISTENER_SET_TIMING_ERR_PROP);
            throw new InvalidListenerSetTimingException(msg);
        }

        // Do nothing if not live
        if(updateHandler == null)
            return;

        if(updateHandler.boundsChanged(l, this, internalUpdater))
            markBoundsDirty();
    }

    /**
     * Set the bounds to the given explicit value. When set, auto computation
     * of the bounds of this node is turned off. A value of null can be used
     * to clear the current explicit bounds and return to auto computation.
     *
     * @param b The new bounds to use or null to clear
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds changed callback method
     */
    public void setBounds(BoundingVolume b)
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        bounds = b;
        implicitBounds = (bounds == null);
    }

    /**
     * Get the currently set bounds for this object. If no explicit bounds have
     * been set, then an implicit set of bounds is returned based on the
     * current scene graph state.
     *
     * @return The current bounds of this object
     */
    public BoundingVolume getBounds()
    {
        // need to check and set a new instance here if needed.
        if(implicitBounds && bounds == null)
            recomputeBounds();

        return bounds;
    }

    /**
     * Update this node's bounds. Used to propogate bounds changes from the
     * leaves of the tree to the root. A node implementation may decide when and
     * where to tell the parent(s)s that updates are ready, though typically it
     * is done in an overridden version of this method.
     */
    protected void updateBounds()
    {
    }

    /**
     * Update this node's parent bounds now. Used to propogate bounds changes
     * from the current level to the parent when needed. Typically used by
     * classes that extend the core nodes that do not automatically have access
     * to the updateBounds() method of any contained geometry, or of their own
     * parent.
     */
    protected void updateParentBounds()
    {
        if(parent != null)
            parent.updateBounds();
    }

    /**
     * Internal method to recalculate the implicit bounds of this Node. By
     * default the bounds are a BoundingVoid, so derived classes should
     * override this method with something better. It does not mark this node
     * as being dirty with it's parent.
     */
    protected void recomputeBounds()
    {
        if(bounds == null)
            bounds = INVALID_BOUNDS;
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
     * <p>
     * The default implementation in this class does nothing. A derived class
     * should override and implement as needed.
     */
    public void requestBoundsUpdate()
    {
    }

    /**
     * Mark this node as having dirty bounds due to one of it's children having
     * their bounds changed. Default implementation will just call the parent
     * node and inform it that its bounds are dirty too.
     */
    protected void markBoundsDirty()
    {
        if(implicitBounds && (parent != null))
            parent.markBoundsDirty();
    }
}
