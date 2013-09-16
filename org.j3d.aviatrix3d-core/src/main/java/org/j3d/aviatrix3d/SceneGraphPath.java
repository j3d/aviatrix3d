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
import org.j3d.maths.vector.Matrix4d;
import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.picking.PickTarget;

/**
 * Representation of a path of nodes through the scene graph.
 * <p>
 *
 * A path consists of at least a root node and a leaf node, representing the
 * two ends of the path. The root and leaf may be the same node instance. The
 * terminating node must always be an instance of {@link Leaf}.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>emptyPathMsg: Error message when the user tries to construct an empty path with
 *      no nodes in it</li>
 * <li>leafTerminatorMsg: Error message when the leaf node is not an instance of Leaf</li>
 * <li>invalidNodeIndexMsg: Error message when the getNode() call is given a either a
  *    negative index or too large index.</li>
 * @author Justin Couch
 * @version $Revision: 1.11 $
 */
public class SceneGraphPath
{
    /** Error message when the initial size is too small */
    private static final String TOO_SMALL_PROP =
        "org.j3d.aviatrix3d.SceneGraphPath.emptyPathMsg";

    /** Error message based on the leaf node not being present */
    private static final String NON_LEAF_PROP =
        "org.j3d.aviatrix3d.SceneGraphPath.leafTerminatorMsg";

    /** Error message when getNode() index is out of range */
    private static final String NODE_INDEX_PROP =
        "org.j3d.aviatrix3d.SceneGraphPath.invalidNodeIndexMsg";

    /** The default depth of the path to handle */
    private static final int DEFAULT_DEPTH = 32;

    /** The number of valid items in the path */
    private int numItems;

    /** The nodes in the path */
    private Node[] items;

    /** The root to local transformation matrix */
    private double[] txMatrix;

    /** The local to root transformation matrix */
    private double[] invTxMatrix;

    /**
     * Create a new path instance with a pre-allocated set of storage
     * space internally.
     */
    public SceneGraphPath()
    {
        this(DEFAULT_DEPTH);
    }

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
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(TOO_SMALL_PROP);
            throw new IllegalArgumentException(msg);
        }

        numItems = 0;
        items = new Node[initialSize];
        txMatrix = new double[16];
        invTxMatrix = new double[16];
    }

    /**
     * Create a new path with the given nodes from the array as the base path
     * definition.
     *
     * @param nodes The list of nodes to copy
     * @param num The number of nodes to copy from the array
     * @param mat The transformation matrix from the root to the local node
     * @param iMat The transformation matrix from the local node to the root
     * @throws IllegalArgumentException The last node is not a Leaf
     */
    public SceneGraphPath(Node[] nodes, int num, Matrix4d mat, Matrix4d iMat)
        throws IllegalArgumentException
    {
        this(num);

        updatePath(nodes, num, mat, iMat);
    }

    /**
     * Set the scene graph path to the new value. Invalidates the current
     * matrix and another will need to be calculated. The final item in the
     * path must be an instance of a leaf node. If not, an exception is
     * generated.
     *
     * @param nodes The list of nodes to copy
     * @param num The number of nodes to copy from the array
     * @param mat The transformation matrix from the root to the local node
     * @param iMat The transformation matrix from the local node to the root
     */
    public void updatePath(Node[] nodes, int num, Matrix4d mat, Matrix4d iMat)
    {
        if(items.length < num)
            items = new Node[num];
        else
        {
            // Null out anything over the existing values
            for(int i = numItems; i < items.length; i++)
                items[i] = null;
        }

        System.arraycopy(nodes, 0, items, 0, num);
        numItems = num;

        txMatrix[0]  = mat.m00;
        txMatrix[1]  = mat.m01;
        txMatrix[2]  = mat.m02;
        txMatrix[3]  = mat.m03;

        txMatrix[4]  = mat.m10;
        txMatrix[5]  = mat.m11;
        txMatrix[6]  = mat.m12;
        txMatrix[7]  = mat.m13;

        txMatrix[8]  = mat.m20;
        txMatrix[9]  = mat.m21;
        txMatrix[10] = mat.m22;
        txMatrix[11] = mat.m23;

        txMatrix[12] = mat.m30;
        txMatrix[13] = mat.m31;
        txMatrix[14] = mat.m32;
        txMatrix[15] = mat.m33;

        invTxMatrix[0]  = iMat.m00;
        invTxMatrix[1]  = iMat.m01;
        invTxMatrix[2]  = iMat.m02;
        invTxMatrix[3]  = iMat.m03;

        invTxMatrix[4]  = iMat.m10;
        invTxMatrix[5]  = iMat.m11;
        invTxMatrix[6]  = iMat.m12;
        invTxMatrix[7]  = iMat.m13;

        invTxMatrix[8]  = iMat.m20;
        invTxMatrix[9]  = iMat.m21;
        invTxMatrix[10] = iMat.m22;
        invTxMatrix[11] = iMat.m23;

        invTxMatrix[12] = iMat.m30;
        invTxMatrix[13] = iMat.m31;
        invTxMatrix[14] = iMat.m32;
        invTxMatrix[15] = iMat.m33;
    }

    /**
     * Set the scene graph path to the new value from a set of {@link PickTarget}
     * instances. Invalidates the current
     * matrix and another will need to be calculated. The final item in the
     * path must be an instance of a leaf node. If not, an exception is
     * generated.
     *
     * @param picks The list of pick targets to copy
     * @param num The number of nodes to copy from the array
     * @param mat The transformation matrix from the root to the local node
     * @param iMat The transformation matrix from the local node to the root
     */
    public void updatePath(PickTarget[] picks, int num, Matrix4d mat, Matrix4d iMat)
    {
        if(items.length < num)
            items = new Node[num];
        else
        {
            // Null out anything over the existing values
            for(int i = numItems; i < items.length; i++)
                items[i] = null;
        }

        int out = 0;
        for(int i = 0; i < num; i++)
        {
            if(picks[i] instanceof Node)
                items[out++] = (Node)picks[i];
        }

        numItems = out;

        txMatrix[0]  = mat.m00;
        txMatrix[1]  = mat.m01;
        txMatrix[2]  = mat.m02;
        txMatrix[3]  = mat.m03;

        txMatrix[4]  = mat.m10;
        txMatrix[5]  = mat.m11;
        txMatrix[6]  = mat.m12;
        txMatrix[7]  = mat.m13;

        txMatrix[8]  = mat.m20;
        txMatrix[9]  = mat.m21;
        txMatrix[10] = mat.m22;
        txMatrix[11] = mat.m23;

        txMatrix[12] = mat.m30;
        txMatrix[13] = mat.m31;
        txMatrix[14] = mat.m32;
        txMatrix[15] = mat.m33;

        invTxMatrix[0]  = iMat.m00;
        invTxMatrix[1]  = iMat.m01;
        invTxMatrix[2]  = iMat.m02;
        invTxMatrix[3]  = iMat.m03;

        invTxMatrix[4]  = iMat.m10;
        invTxMatrix[5]  = iMat.m11;
        invTxMatrix[6]  = iMat.m12;
        invTxMatrix[7]  = iMat.m13;

        invTxMatrix[8]  = iMat.m20;
        invTxMatrix[9]  = iMat.m21;
        invTxMatrix[10] = iMat.m22;
        invTxMatrix[11] = iMat.m23;

        invTxMatrix[12] = iMat.m30;
        invTxMatrix[13] = iMat.m31;
        invTxMatrix[14] = iMat.m32;
        invTxMatrix[15] = iMat.m33;
    }

    /**
     * Get the leaf node at the end of the path.
     */
    public Node getTerminalNode()
    {
        return items[numItems - 1];
    }

    /**
     * Get a single node at the given index position. If the index is greater
     * than the number of nodes found in the path, then generate an exception.
     *
     * @param pos The index of the node in the path
     * @return The node instance at that position
     * @throws ArrayIndexOutOfBoundsException The index was out of range
     */
    public Node getNode(int pos)
        throws ArrayIndexOutOfBoundsException
    {
        if((pos < 0) || (pos >= numItems))
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NODE_INDEX_PROP);
            throw new ArrayIndexOutOfBoundsException(msg);
        }

        return items[pos];
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

    /**
     * Get the transformation matrix from the root of the path the to the
     * terminal node. If no matrix has been set, this will return the
     * zero matrix.
     *
     * @param mat The matrix to copy the values into
     */
    public void getTransform(Matrix4d mat)
    {
        mat.m00 = txMatrix[0];
        mat.m01 = txMatrix[1];
        mat.m02 = txMatrix[2];
        mat.m03 = txMatrix[3];

        mat.m10 = txMatrix[4];
        mat.m11 = txMatrix[5];
        mat.m12 = txMatrix[6];
        mat.m13 = txMatrix[7];

        mat.m20 = txMatrix[8];
        mat.m21 = txMatrix[9];
        mat.m22 = txMatrix[10];
        mat.m23 = txMatrix[11];

        mat.m30 = txMatrix[12];
        mat.m31 = txMatrix[13];
        mat.m32 = txMatrix[14];
        mat.m33 = txMatrix[15];
    }

    /**
     * Get the transformation matrix from the terminal node to the root
     * of the path. If no matrix has been set, this will return the
     * zero matrix.
     *
     * @param mat The matrix to copy the values into
     */
    public void getInverseTransform(Matrix4d mat)
    {
        mat.m00 = invTxMatrix[0];
        mat.m01 = invTxMatrix[1];
        mat.m02 = invTxMatrix[2];
        mat.m03 = invTxMatrix[3];

        mat.m10 = invTxMatrix[4];
        mat.m11 = invTxMatrix[5];
        mat.m12 = invTxMatrix[6];
        mat.m13 = invTxMatrix[7];

        mat.m20 = invTxMatrix[8];
        mat.m21 = invTxMatrix[9];
        mat.m22 = invTxMatrix[10];
        mat.m23 = invTxMatrix[11];

        mat.m30 = invTxMatrix[12];
        mat.m31 = invTxMatrix[13];
        mat.m32 = invTxMatrix[14];
        mat.m33 = invTxMatrix[15];
    }
}
