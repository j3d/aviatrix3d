/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
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

// Standard imports
//import org.web3d.vecmath.Matrix4f;
import javax.vecmath.Matrix4f;

// Application specific imports

/**
 * The Group node object is a generic container object.
 * Group nodes have exactly one parent and an arbitrary number of children.
 * The children are rendered in an unspecified order. Null children
 * are allowed but no operation is performed on a null child.
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class Group extends Node
{
    /** Message for the index provided being out of range */
    private static final String CHILD_IDX_ERR =
        "Index provided > last valid index";

    /** The initial size of the children list */
    private static final int LIST_START_SIZE = 5;

    /** The increment size of the list if it gets overflowed */
    private static final int LIST_INCREMENT = 5;

    /** The list of children nodes */
    protected Node[] childList;

    /** Index to the next place to add items in the nodeList */
    protected int lastList;

    /**
     * The default constructor
     */
    public Group()
    {
        childList = new Node[LIST_START_SIZE];
        lastList = 0;

        transform = new Matrix4f();
        transform.setIdentity();
    }

    /**
     * Appends the specified child node to this group node's list of children
     *
     * @param newChild The child to add
     */
    public void addChild(Node newChild)
    {
        resizeList();
        childList[lastList++] = newChild;

        if(newChild != null)
        {
            newChild.setParent(this);
            newChild.setUpdateHandler(updateHandler);
        }
    }

    /**
     * Replaces the child node at the specified index in this group
     * node's list of children with the specified child.
     *
     * @param newNode The child node to use
     * @param idx The index to replace.  Must be greater than 0 and less then numChildren
     * @throws IndexOutOfBoundsException When the idx is invalid
     */
    public void setChild(Node newChild, int idx)
    {
        if(idx > lastList)
            throw new IndexOutOfBoundsException(CHILD_IDX_ERR);

        childList[idx] = newChild;

        if (newChild != null)
        {
            // What if this object has multiple parents? Bug here....
            newChild.setParent(this);
            newChild.setUpdateHandler(updateHandler);
        }
    }

    /**
     * Remove the child at the specified index from the group.
     *
     * @param idx The index of the child to remove
     * @throws IndexOfBoundsException When the idx is invalid
     */
    public void removeChild(int idx)
    {
        if(idx > lastList)
            throw new IndexOutOfBoundsException(CHILD_IDX_ERR);

        System.arraycopy(childList, idx+1, childList, idx, lastList - idx);
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
        if(idx < lastList)
            throw new IndexOutOfBoundsException(CHILD_IDX_ERR);

        return childList[idx];
    }

    /**
     * Get the list of children that are valid to be rendered according to
     * the rules of the grouping node.
     *
     * @return an array of nodes
     */
    public Node[] getRenderableChild()
    {
        return childList;
    }

    /**
     * Returns the number of valid renderable children to process. If there are
     * no valid renderable children return -1.
     *
     * @return A number greater than or equal to zero or -1
     */
    public int numRenderableChildren()
    {
        return lastList + 1;
    }

    /**
     * Return an array containing all of this groups children.  This
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
        return lastList + 1;
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
        for(int i=0; i < lastList; i++)
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
     */
    public void removeChild(Node child)
    {
        int idx = indexOfChild(child);
        if(idx == -1)
            return;

        removeChild(idx);
    }

    /**
     * Removes all children from the group.
     */
    public void removeAllChildren()
    {
        // Remove all references to allow garbage collection
        for(int i=0; i < lastList; i++)
            childList[i] = null;

        lastList = 0;
    }

    /**
     * Set the scenegraph update handler for this node.  It will notify
     * all its children of the value. A null value will clear the current
     * handler.
     *
     * @param handler The instance to use as a handler
     */
    public void setUpdateHandler(NodeUpdateHandler handler)
    {
        super.setUpdateHandler(handler);

        for(int i=0; i < lastList; i++)
        {
            childList[i].setUpdateHandler(handler);
        }
    }

    //---------------------------------------------------------------
    // Misc Internal methods
    //---------------------------------------------------------------

    /**
     * Resize the list if needed. Marked as final in order to encourage the
     * compiler to inline the code for faster execution
     */
    private final void resizeList()
    {

        if((lastList + 1) == childList.length)
        {
            int old_size = childList.length;
            int new_size = old_size + LIST_INCREMENT;

            Node[] tmp_nodes = new Node[new_size];

            System.arraycopy(childList, 0, tmp_nodes, 0, old_size);

            childList = tmp_nodes;
        }
    }
}