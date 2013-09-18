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
import org.j3d.aviatrix3d.picking.*;

import org.j3d.aviatrix3d.rendering.BoundingVolume;
import org.j3d.aviatrix3d.rendering.Cullable;
import org.j3d.aviatrix3d.rendering.SingleCullable;

/**
 * A node that can have multiple parents, thus allowing a graph
 * structure to the scene graph.
 * <p>
 *
 * Normal nodes cannot have more than one parent, so this class provides
 * the ability to have more than one. In doing so, it overrides the normal
 * methods provided by Node to provide the shared functionality. It provides
 * a compliment to the SharedGroup for parts of the scene graph where you
 * want to share a common piece, but really don't need the grouping
 * functionality.
 * <p>
 *
 * Using this node in preference to SharedGroup has several performance
 * benefits. For example, when performing picking, the picking implementation
 * can just ignore this node altogether as it knows the bounds are identical
 * to it's child.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>invalidParentTypeMsg: Error message when the user tries to set this
 *     in something that is not part of the transform heirarchy.</li>
 * <li>pickTimingMsg: Error message when attempting to pick outside the app
 *     update observer callback.</li>
 * <li>notPickableMsg: Error message when the user has set the pickmask to
 *     zero and then requested a pick directly on this object.</li>
 * <li>nullArrayParentMsg: Error message when the (internal) caller tries to
 *     call getParents() with a null array reference.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.30 $
 */
public class SharedNode extends Node
    implements MultiParentNode,
               PickableObject,
               SinglePickTarget,
               SingleCullable
{
    /** Message when attempting to pick the object at the wrong time */
    private static final String PICK_TIMING_PROP =
        "org.j3d.aviatrix3d.SharedNode.pickTimingMsg";

    /**
     * You are attempting to pick a node you have marked as not-pickable, yet
     * called the pick method on this class anyway.
     */
    private static final String PICKABLE_FALSE_PROP =
        "org.j3d.aviatrix3d.SharedNode.notPickableMsg";

    /** Message when the setParent does not receive a group */
    private static final String NOT_GROUP_PROP =
        "org.j3d.aviatrix3d.SharedNode.invalidParentTypeMsg";

    /** Message for getParent(null) case error */
    private static final String ARRAY_PARENT_NULL_PROP =
        "org.j3d.aviatrix3d.SharedNode.nullArrayParentMsg";

    /** The initial size of the children list */
    private static final int LIST_START_SIZE = 5;

    /** The increment size of the list if it gets overflowed */
    private static final int LIST_INCREMENT = 5;

    /** Listing of all the parents of this node */
    private Node[] parentList;

    /** Index to the next place to add items in the nodeList */
    private int lastParentList;

    /** The child node of this one */
    private Node sharedChild;

    /** Flag indicating if this object is pickable currently */
    private int pickFlags;

    /**
     * The default constructor
     */
    public SharedNode()
    {
        parentList = new Node[LIST_START_SIZE];
        lastParentList = 0;

        pickFlags = 0xFFFFFFFF;
    }

    //---------------------------------------------------------------
    // Methods defined by SingleCullable
    //---------------------------------------------------------------

    /**
     * Get the child renderable of this object.
     *
     * @return an array of nodes
     */
    @Override
    public Cullable getCullableChild()
    {
        if(sharedChild instanceof Cullable)
            return (Cullable)sharedChild;
        else
            return null;
    }

    /**
     * Check to see if this cullable is mulitparented. If it is, then that
     * will cause problems for code that needs to know information like the
     * transformation to the root of the scene graph.
     *
     * @return true if there are multiple parents
     */
    @Override
    public boolean hasMultipleParents()
    {
        return true;
    }

    /**
     * Get the parent cullable of this instance. If this node has multiple
     * direct parents, then this should return null.
     *
     * @return The parent instance or null if none
     */
    @Override
    public Cullable getCullableParent()
    {
        return null;
    }

    //---------------------------------------------------------------
    // Methods defined by MultiParentNode
    //---------------------------------------------------------------

    /**
     * Overloaded version of the notification that this object's liveness state
     * has changed. We overload with the caller so that for shared
     *
     * @param caller The node calling us with the state changes
     * @param state true if this should be marked as live now
     */
    @Override
    public void setLive(Node caller, boolean state)
    {
        // Ignore stuff that doesn't change the state
        if(state == alive)
            return;

        if(!alive)
        {
            // If we are not alive, that implies that state == true because
            // that is already implied by the first if(state == alive) test
            if(sharedChild != null)
            {
                if(sharedChild instanceof MultiParentNode)
                    ((MultiParentNode)sharedChild).setLive(this, true);
                else
                    sharedChild.setLive(true);
            }

            super.setLive(true);
        }
        else
        {
            // we are alive, but we're being told to not be. Check all the
            // parents to see if they think we're all being told to die
            boolean still_live = false;

            for(int i = 0; i < lastParentList && !still_live; i++)
            {
                if(parentList[i] != caller)
                    still_live = parentList[i].isLive();
            }

            if(!still_live)
            {
                if(sharedChild instanceof MultiParentNode)
                    ((MultiParentNode)sharedChild).setLive(this, false);
                else
                    sharedChild.setLive(false);

                super.setLive(false);
            }
        }
    }

    //---------------------------------------------------------------
    // Methods defined by Node
    //---------------------------------------------------------------

    /**
     * Mark this node as having dirty bounds due to one of it's children having
     * their bounds changed.
     */
    @Override
    protected void markBoundsDirty()
    {
        if(implicitBounds)
        {
            for(int i = 0; i < lastParentList; i++)
                parentList[i].markBoundsDirty();
        }
    }

    /**
     * Internal method to recalculate the implicit bounds of this Node. By
     * default the bounds are a point sphere, so derived classes should
     * override this method with something better.
     */
    @Override
    protected void recomputeBounds()
    {
        if(!alive || !implicitBounds)
            return;

        if(sharedChild == null)
            bounds = INVALID_BOUNDS;
        else
        {
            // Just use the child's bounds!
            bounds = sharedChild.getBounds();
        }
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
    @Override
    public void requestBoundsUpdate()
    {
        if(alive || !implicitBounds)
            return;

        sharedChild.requestBoundsUpdate();
        recomputeBounds();
    }

    /**
     * Update this node's bounds and then call the parent to update it's
     * bounds. Used to propogate bounds changes from the leaves of the tree
     * to the root. A node implementation may decide when and where to tell
     * the parent(s)s that updates are ready.
     */
    @Override
    protected void updateBounds()
    {
        if(!implicitBounds)
            return;

        recomputeBounds();

        for(int i = 0; i < lastParentList; i++)
            parentList[i].updateBounds();
    }

    //---------------------------------------------------------------
    // Methods defined by SceneGraphObject
    //---------------------------------------------------------------

    /**
     * Specify this nodes parent, overridden to provide behaviour that appends
     * the node to the list rather than replacing it. The parent must be a group
     * node in this case.
     *
     * @param p The new parent instance to add to the list
     * @throws AlreadyParentedException There is a valid parent already set
     * @throws InvalidNodeTypeException Not a group node
     * @throws CyclicSceneGraphStructureException Equal parent and child causing
     *   a cycle in the scene graph structure
     */
    @Override
    protected void setParent(Node p)
        throws AlreadyParentedException,
               InvalidNodeTypeException,
               CyclicSceneGraphStructureException
    {
        if((p != null) && !(p instanceof TransformHierarchy))
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NOT_GROUP_PROP);
            throw new InvalidNodeTypeException(msg);
        }

        // Check to see that this parent isn't already in the list
        for(int i = 0; i < lastParentList; i++)
            if(parentList[i] == p)
                return;

        resizeList();
        parentList[lastParentList++] = p;
    }

    /**
     * Remove a parent from this shared group. Since setParent() cannot be
     * used to remove a parent from the graph, you'll need to use this method
     * to remove the parent.
     *
     * @param p The new parent instance to remove from the list
     */
    @Override
    protected void removeParent(Node p)
    {
        // find the location, move everything down one
        for(int i = 0; i < lastParentList; i++)
        {
            if(parentList[i] == p)
            {
                int move_size = lastParentList - i;
                if(move_size != 0)
                    System.arraycopy(parentList,
                                     i,
                                     parentList,
                                     i + 1,
                                     move_size);
                lastParentList--;
                break;
            }
        }
    }

    /**
     * Overridden to always return the current first parent in the list.
     *
     * @return parent[0] if there are any
     */
    @Override
    public Node getParent()
    {
        return parentList[0];
    }

    /**
     * Notification that this object is live now.
     *
     * @param state true if this should be marked as live now
     */
    @Override
    protected void setLive(boolean state)
    {
        throw new IllegalStateException("This method should never be called. Use setLive(Node, boolean)");
    }

    /**
     * Set the scenegraph update handler for this node.  It will notify
     * all its children of the value. A null value will clear the current
     * handler.
     *
     * @param handler The instance to use as a handler
     */
    @Override
    protected void setUpdateHandler(NodeUpdateHandler handler)
    {
        if(handler == updateHandler)
            return;

        boolean set_handler = true;

        if(handler == null)
        {
            // check that we don't have a valid handler in any one of the parents
            // that we would be trashing.
            for(int i = 0; i < lastParentList && set_handler; i++)
            {
                if(parentList[i] != null)
                    set_handler = (parentList[i].updateHandler == null);
            }
        }

        if(set_handler)
        {
            super.setUpdateHandler(handler);

            if(sharedChild != null)
                sharedChild.setUpdateHandler(handler);
        }
    }

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
        if(child == this)
            throw new CyclicSceneGraphStructureException();

        for(int i = 0; i < lastParentList; i++)
            parentList[i].checkForCyclicParent(child);
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

    //---------------------------------------------------------------
    // Methods defined by SinglePickTarget
    //---------------------------------------------------------------

    /**
     * Return the child that is pickable of from this target. If there is none
     * then return null.
     *
     * @return The child pickable object or null
     */
    @Override
    public PickTarget getPickableChild()
    {
        return (sharedChild instanceof PickTarget) ?
               (PickTarget)sharedChild : null;
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
        return SINGLE_PICK_TYPE;
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
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Request the number of parents this node currently contains
     *
     * @return a positive number
     */
    public int numParents()
    {
        return lastParentList;
    }

    /**
     * Get the listing of the number of parents that this node currently has.
     * The provided array must be at least big enough to copy all the values
     * into it.
     *
     * @param parents An array to copy the parent listing into
     */
    public void getParents(Node[] parents)
    {
        if(parents == null)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(ARRAY_PARENT_NULL_PROP);
            throw new NullPointerException(msg);
        }

        System.arraycopy(parentList, 0, parents, 0, lastParentList);
    }

    /**
     * Set the child to be the new value. If the existing child is set,
     * it is replaced by this current child. Setting a value of null will
     * remove the old one.
     *
     * @param child The new instance to set or null
     * @throws CyclicSceneGraphStructureException Equal parent and child causing
     *   a cycle in the scene graph structure
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds callback method
     */
    public void setChild(Node child)
        throws CyclicSceneGraphStructureException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        if(sharedChild != null)
        {
            sharedChild.setLive(false);
            sharedChild.removeParent(this);
        }

        sharedChild = child;

        if(sharedChild != null)
        {
            sharedChild.setParent(this);
            sharedChild.setLive(alive);
            sharedChild.setUpdateHandler(updateHandler);
        }
        else
        {
            bounds = INVALID_BOUNDS;
        }
    }

    /**
     * Get the currently set child of this node. If there is none set, the
     * return null.
     *
     * @return The current child or null
     */
    public Node getChild()
    {
        return sharedChild;
    }

    /**
     * Resize the list if needed. Marked as final in order to encourage the
     * compiler to inline the code for faster execution
     */
    private final void resizeList()
    {
        if((lastParentList + 1) == parentList.length)
        {
            int old_size = parentList.length;
            int new_size = old_size + LIST_INCREMENT;

            Node[] tmp_nodes = new Node[new_size];

            System.arraycopy(parentList, 0, tmp_nodes, 0, old_size);

            parentList = tmp_nodes;
        }
    }
}
