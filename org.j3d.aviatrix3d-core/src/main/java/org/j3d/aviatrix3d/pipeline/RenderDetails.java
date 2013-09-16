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

package org.j3d.aviatrix3d.pipeline;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.rendering.Renderable;

/**
 * Class for passing the detailed rendering information through the pipeline.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 2.2 $
 */
public class RenderDetails
{
    /**
     * The lead node instance to be rendered. The object type it needs to
     * be cast to depends on the accompanying rendering operation.
     */
    public Renderable renderable;

    /** An arbitrary, unique identifier for the operations that need it */
    public int id;

    /**
     * A per-RenderOp defined set of data that is passed from the sorter to
     * the rendering device. See the individual instruction for more
     * information.
     */
    public Object instructions;

    /**
     * Construct a default instance with nothing initialised.
     */
    public RenderDetails()
    {
    }

    /**
     * Remove any currently set references that may be here.
     */
    public void clear()
    {
        renderable = null;
        instructions = null;
    }
}
