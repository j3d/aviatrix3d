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

// External imports
// None

// Local imports
// None

/**
 * Representation of a path of nodes through the scene graph.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class SceneGraphPath
{
    /** Error message when the initial size is too small */
    private static final String TOO_SMALL_MSG = "The initial size is < 1";

    /** Error message based on the leaf node not being present */
    private static final String NON_LEAF_MSG = "The terminating node is " +
        "not an instance of Leaf";

    /** The number of valid items in the path */
    private int numItems;

    /** The nodes in the path */
    private Node[] items;

    /** The local to root transformation matrix */
    private float[] txMatrix;

    /**
     * Create a new path instance with a pre-allocated set of storage
     * space internally.
     *
     * @param initialSize number of items to create
     * @throws IllegalArgumentException if the number is < 1
     */
    public SceneGraphPath(int initialSize)
    {
        if(initialSize < 1)
            throw new IllegalArgumentException(TOO_SMALL_MSG);

        numItems = 0;
        items = new Node[initialSize];
        txMatrix = new float[16];
    }

    /**
     * Set the scene graph path to the new value. Invalidates the current
     * matrix and another will need to be calculated. The final item in the
     * path must be an instance of a leaf node. If not, an exception is
     * generated.
     *
     * @param nodes The list of nodes to copy
     * @param num The number of nodes to copy from the array
     * @throws IllegalArgumentException The last node is not a Leaf
     */
    public void updatePath(Node[] nodes, int num)
        throws IllegalArgumentException
    {
        if(!(nodes[num - 1] instanceof Leaf))
            throw new IllegalArgumentException(NON_LEAF_MSG);
    }

    /**
     * Get the leaf node at the end of the path.
     */
    public Leaf getTerminalNode()
    {
        return (Leaf)items[numItems - 1];
    }

    /**
     * Get the raw list of path items from the internal array. The array may
     * be longer than the number of valid nodes, so make sure to get the
     * length as well.
     */
    public Node[] getNodes()
    {
        return items;
    }

    /**
     * Get the number of valid items in the path.
     *
     * @return The number of valid items.
     */
    public int getNodeCount()
    {
        return numItems;
    }
}