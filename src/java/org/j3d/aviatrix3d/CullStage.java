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
import gl4java.GLFunc;
import gl4java.GLEnum;

import gl4java.drawable.GLDrawable;

/**
 * Handles the scenegraph maintenance and culling operations.
 * <p>
 *
 * The update phase will update transforms and recalculate bounding boxes.
 * The culling phase generates a list of nodes to render.
 * A future optimization will sort the render list by OGL state.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */

public class CullStage
{
    /** The initial size of the children list */
    private static final int LIST_START_SIZE = 200;

    /** The increment size of the list if it gets overflowed */
    private static final int LIST_INCREMENT = 200;

    /** The list of nodes to render */
    private Node[] renderList;
    private int renderOp[];
    private int lastRender;

    public CullStage()
    {
        renderList = new Node[LIST_START_SIZE];
        renderOp = new int[LIST_START_SIZE];
        lastRender = 0;
    }

    /**
     * Update and cull the scenegraph.  This generates an ordered list
     * of nodes to render.
     */
    public void cull(Node node)
    {
        lastRender=0;
        updateTraverse(node, null);
        cullTraverse(node);
    }

    /**
     * Traverse the scenegraph and update the transforms and bounding boxes.
     *
     * @param node The node to update
     * @param trans The nodes new transformation
     */
    private void updateTraverse(Node node, Matrix4f trans)
    {
        int len;
        Node[] kids;

        // Need to stop updating all transforms every frame!

        node.updateTransform(trans);
        if (node instanceof Group)
        {
            trans = node.getTransform();
            kids = ((Group)node).getAllChildren();
            len = ((Group)node).numChildren();
            for(int i=0; i < len; i++)
            {
                if (kids[i] == null)
                    continue;

                updateTraverse(kids[i], trans);
            }
        }
        node.updateBounds();
    }

    /**
     * Traverse the scenegraph and cull nodes that aren't visible.
     *
     * @param node The node to start from
     */
    private void cullTraverse(Node node)
    {
        int len;
        Node[] kids;

        resizeList();
        renderList[lastRender] = node;
        renderOp[lastRender++] = Draw.RENDER;

        // Convert to getPrimaryType()
        if(node instanceof Group)
        {
            kids = ((Group)node).getAllChildren();
            len = ((Group)node).numChildren();
            for(int i=0; i < len; i++)
            {
                if (kids[i] == null) continue;
                cullTraverse(kids[i]);
            }
        }

        renderList[lastRender] = node;
        renderOp[lastRender++] = Draw.POSTRENDER;
    }

    /**
     * Get the list of nodes to render.
     *
     * @return The list of nodes in render order
     */
    public Node[] getRenderList()
    {
        return renderList;
    }

    /**
     * Get the renderOps list.
     *
     * @return The list of operations to perform for a node
     */
    public int[] getRenderOp()
    {
        return renderOp;
    }

    /**
     * Get the size of the render list.
     */
    public int getRenderListSize()
    {
        return lastRender;
    }

    //---------------------------------------------------------------
    // Misc Internal methods
    //---------------------------------------------------------------

    /**
     * Resize the list if needed. Marked as final in order to encourage the
     * compiler to inline the code for faster execution.
     *
     * Resize by 2 since we will always job by that amount
     */
    private final void resizeList()
    {

        if((lastRender + 2) >= renderList.length -1)
        {
            int old_size = renderList.length;
            int new_size = old_size + LIST_INCREMENT;

            Node[] tmp_nodes = new Node[new_size];
            int[] tmp_ops = new int[new_size];

            System.arraycopy(renderList, 0, tmp_nodes, 0, old_size);
            System.arraycopy(renderOp, 0, tmp_ops, 0, old_size);

            renderList = tmp_nodes;
            renderOp = tmp_ops;
        }
    }
}