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
 * Special grouping node that allows the selection of only a single
 * child to be rendered.
 * <p>
 * If the node that is the selected index is removed, then the
 * selectedChild is automatically to be invalid. The user must reset
 * the selected child index in order for rendering to continue.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class SwitchGroup extends Group
{
    /** Index to the next place to add items in the nodeList */
    private int selectedChild;

    /** The child that is currently selected for rendering */
    private Node[] renderedChild;

    /**
     * The default constructor
     */
    public SwitchGroup()
    {
        selectedChild = -1;
        renderedChild = new Node[1];
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
        super.setChild(newChild, idx);

        if(idx == selectedChild)
            renderedChild[0] = newChild;
    }

    /**
     * Remove the child at the specified index from the group.
     *
     * @param idx The index of the child to remove
     * @throws IndexOfBoundsException When the idx is invalid
     */
    public void removeChild(int idx)
    {
        if(idx == selectedChild)
        {
            selectedChild = -1;
            renderedChild[0] = null;
        }

        super.removeChild(idx);
    }

    /**
     * Get the list of children that are valid to be rendered according to
     * the rules of the grouping node.
     *
     * @return an array of nodes
     */
    public Node[] getRenderableChild()
    {
        return renderedChild;
    }

    /**
     * Returns the number of valid renderable children to process.If there are
     * no valid renderable children return -1.
     *
     * @return A number greater than or equal to zero
     */
    public int numRenderableChildren()
    {
        return selectedChild;
    }

    /**
     * Removes all children from the group.
     */
    public void removeAllChildren()
    {
        super.removeAllChildren();

        renderedChild[0] = null;
        selectedChild = -1;
    }

    //---------------------------------------------------------------
    // Misc local methods
    //---------------------------------------------------------------

    /**
     * Set the selected child to be rendered to the given index. If the index
     * is invalid, then an exception is issued. Using a value of -1 means
     * that no child will be rendered.
     *
     * @param idx The index of the child to now be rendered
     */
    public void setActiveChild(int idx)
    {
        if(idx > lastList)
            throw new IllegalArgumentException("Index is not valid");

        selectedChild = idx < 0 ? -1 : idx;
        renderedChild[0] = (idx >= 0) ? childList[idx] : null;
    }

    /**
     * Get the currently selected active child of this switch node. If none is
     * active, -1 will be returned.
     *
     * @param The valid child index or -1
     */
    public int getActiveChild()
    {
        return selectedChild;
    }
}