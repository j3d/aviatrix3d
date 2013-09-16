/*****************************************************************************
 *                  Yumetech, Inc Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d.pipeline.graphics;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.rendering.EffectRenderable;
import org.j3d.aviatrix3d.pipeline.CullOutputDetails;

/**
 * Class for passing the detailed graphics rendering information through
 * the pipeline.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 3.2 $
 */
public class GraphicsCullOutputDetails extends CullOutputDetails
{
    /** Array of lights that effect this node. If none, it may be null */
    public VisualDetails[] lights;

    /** Number of valid lights in the array */
    public int numLights;

    /** Array of clip planes that effect this node. If none, it may be null */
    public VisualDetails[] clipPlanes;

    /** Number of valid lights in the array */
    public int numClipPlanes;

    /** If a locally declared fog is set, use it */
    public EffectRenderable localFog;

    /**
     * Construct a default instance of the output details. None of the arrays
     * are instantiated to start with.
     */
    public GraphicsCullOutputDetails()
    {
    }
}
