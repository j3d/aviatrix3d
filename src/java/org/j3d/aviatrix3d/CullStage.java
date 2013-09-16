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
 * Handles the scenegraph maintenance and culling operations.
 * <p>
 *
 * The update phase will update transforms and recalculate bounding boxes.
 * The culling phase generates a list of nodes to render.
 * A future optimization will sort the render list by OGL state.
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */

public interface CullStage
{
    /**
     * Update and cull the scenegraph.  This generates an ordered list
     * of nodes to render.
     */
    public void cull(Node node);

    /**
     * Get the list of nodes to render.
     *
     * @return The list of nodes in render order
     */
    public Node[] getRenderList();

    /**
     * Get the renderOps list.
     *
     * @return The list of operations to perform for a node
     */
    public int[] getRenderOp();

    /**
     * Get the size of the render list.
     */
    public int getRenderListSize();
}