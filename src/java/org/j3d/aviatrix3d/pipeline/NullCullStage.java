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

package org.j3d.aviatrix3d.pipeline;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.CullStage;
import org.j3d.aviatrix3d.CulledGeometryReceiver;
import org.j3d.aviatrix3d.Node;

/**
 * Handles the scenegraph maintenance and culling operations.
 * <p>
 *
 * The culling phase generates a list of nodes to render.
 * A future optimization will sort the render list by OGL state.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class NullCullStage implements CullStage
{
    /** Handler for the output */
    private CulledGeometryReceiver receiver;

    /**
     * Create a basic instance of this class.
     */
    public NullCullStage()
    {
    }

    /**
     * Update and cull the scenegraph. This generates an ordered list
     * of nodes to render. It will not return until the culling is complete.
     */
    public void cull(Node node)
    {
    }

    /**
     * Register a reciever for the output of the sorter. If the value is null,
     * it will clear the currently set receiver.
     *
     * @param sgr The receiver instance to add or null
     */
    public void setCulledGeometryReceiver(CulledGeometryReceiver sgr)
    {
        receiver = sgr;
    }
}