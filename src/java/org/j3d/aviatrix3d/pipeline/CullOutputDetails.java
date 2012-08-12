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
import javax.vecmath.Matrix4f;

// Local imports
import org.j3d.aviatrix3d.rendering.BoundingVolume;
import org.j3d.aviatrix3d.rendering.Renderable;

/**
 * Class for passing the detailed rendering information through the pipeline.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 2.2 $
 */
public class CullOutputDetails
{
    /** The node instance to be rendered. */
    public Renderable renderable;

    /** The transform from the root of the scene graph to here */
    public Matrix4f transform;

    /** Passed along additional rendering information for the object */
    public Object customData;

	/** The bounds of the cullable */
	public BoundingVolume cullableBounds;
	
    /**
     * Construct a default instance with just the transform initialised to the
     * zero matrix.
     */
    public CullOutputDetails()
    {
        transform = new Matrix4f();
    }
}
