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

package org.j3d.aviatrix3d.pipeline.audio;

// External imports
import org.j3d.maths.vector.Matrix4d;

// Local imports
import org.j3d.aviatrix3d.pipeline.RenderDetails;

/**
 * Class for passing the detailed rendering information through the pipeline.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 2.0 $
 */
public class AudioDetails extends RenderDetails
{
    /** The transform from the root of the scene graph to here */
    public Matrix4d transform;

    /**
     * Construct a default instance with just the transform initialised
     */
    public AudioDetails()
    {
        transform = new Matrix4d();
    }
}
