/*****************************************************************************
 *                   Yumetech, Inc Copyright (c) 2004 - 2006
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
// None

/**
 * A general purpose container class to allow end users to extend the basic
 * geometry capabilities, while still providing a way to manage scene graph
 * state such as liveness calls and update handlers.
 * <p>
 *
 * A lot of the state calls of children can only be called by extending the
 * class and calling protected methods. When a child scenegraph object is
 * defined for a collection of children outside of the base aviatrix3d package
 * there is no way for passing in liveness state due to the protected nature
 * of the required methods. This class provides methods that allow a derived
 * class to call back into the package and set those states.
 * <p>
 *
 * <b>Implementation Note</b>
 * <p>
 *
 * Since this is a base set of nodes, no interface with the rendering pipeline
 * has been implemented. It is up to the person extending this class to
 * implement an appropriate interface that extends from
 * {@link org.j3d.aviatrix3d.rendering.Cullable} so that the rendering
 * infrastructure may process any or all children of this set.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>invalidChildIndexMsg: Error message when the user provides an index
 *     for a child that is < 0 or > the number of children.</li>
 * </ul>
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 2.5 $
 */
public abstract class ObjectSet extends SceneGraphObject
{
    /** Message for the index provided being out of range */
    private static final String CHILD_IDX_ERR_PROP =
        "org.j3d.aviatrix3d.ObjectSet.invalidChildIndexMsg";

    /** The initial size of the children list */
    private static final int LIST_START_SIZE = 5;

    /** The increment size of the list if it gets overflowed */
    private static final int LIST_INCREMENT = 5;

    /** The list of children nodes */
    protected SceneGraphObject[] childList;

    /** Index to the next place to add items in the nodeList */
    protected int lastChild;

    /**
     * The default constructor
     */
    protected ObjectSet()
    {
        childList = new SceneGraphObject[LIST_START_SIZE];
        lastChild = 0;
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
    @Override
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
    @Override
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
    @Override
    protected void setLive(boolean state)
    {
        // Ignore stuff that doesn't change the state
        if(state == alive)
            return;

        for(int i = 0; i < lastChild; i++)
        {
            if(childList[i] != null)
                childList[i].setLive(state);
        }

        // Call this after, that way the bounds are recalculated here with
        // the correct bounds of all the children set up.
        super.setLive(state);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Appends the specified child node to this group node's list of children
     *
     * @param newChild The child to add
     * @throws AlreadyParentedException There is a valid parent already set
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds changed callback method
     */
    public void addChild(SceneGraphObject newChild)
        throws AlreadyParentedException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        resizeList();
        childList[lastChild++] = newChild;

        if(newChild != null)
        {
            if(newChild.isLive() != alive)
                newChild.setLive(alive);

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
     *   of the NodeUpdateListener bounds changed callback method
     */
    public void setChild(SceneGraphObject newChild, int idx)
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
            childList[idx].setLive(false);
        }

        childList[idx] = newChild;

        if(newChild != null)
        {
            if(newChild.isLive() != alive)
                newChild.setLive(alive);

            newChild.setUpdateHandler(updateHandler);
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

        if(childList[idx] != null)
        {
            childList[idx].setLive(false);
        }

        System.arraycopy(childList, idx+1, childList, idx, lastChild - idx);

        lastChild--;
    }

    /**
     * Retrieves the child node at the specified index in this group node's
     * list of children.
     *
     * @return The node
     * @throws IndexOutOfBoundsException If the idx is invalid
     */
    public SceneGraphObject getChild(int idx)
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
     * Return an array containing all of this groups children.  This
     * structure is the nodes internal representation.  Check the numChildren
     * call to determine how many entries are valid.
     *
     * @return An array of nodes
     */
    public SceneGraphObject[] getAllChildren()
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
    public int indexOfChild(SceneGraphObject child)
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
     *   of the NodeUpdateListener bounds changed callback method
     */
    public void removeChild(SceneGraphObject child)
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
     *   of the NodeUpdateListener bounds changed callback method
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

            SceneGraphObject[] tmp_nodes = new SceneGraphObject[new_size];

            System.arraycopy(childList, 0, tmp_nodes, 0, old_size);

            childList = tmp_nodes;
        }
    }
}
