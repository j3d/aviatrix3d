/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004-2005
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
// None

/**
 * Class for passing the detailed rendering information for a single surface.
 * from the sorter through to the renderable surface.
 * <p>
 *
 * Since pBuffers cannot be shared across different GL contexts with the
 * construct that JOGL is is using, when two parent textures reference a
 * single child, there will be two copies of this class, with two
 * different parentSource references, but only one copyOf set.
 *
 * @author Justin Couch
 * @version $Revision: 2.0 $
 */
public class RenderInstructions
{
    /** The initial size of the children list */
    protected static final int LIST_START_SIZE = 496;

    /**
     * Reference to the real instance of this class that contains the
     * renderList and renderOps you should read from. If null, this is the
     * original specification.
     */
    public RenderInstructions copyOf;

    /** Operation to perform on each node */
    public RenderOp[] renderOps;

    /** The number of valid items in the array */
    public int numValid;

    /**
     * Construct a new instance of this class with the arrays initialised
     * to a default size.
     */
    public RenderInstructions()
    {
        renderOps = new RenderOp[LIST_START_SIZE];
    }

}
