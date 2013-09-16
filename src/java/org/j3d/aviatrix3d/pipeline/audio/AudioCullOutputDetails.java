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

package org.j3d.aviatrix3d.pipeline.audio;

// External imports
import javax.vecmath.Matrix4f;

// Local imports
import org.j3d.aviatrix3d.pipeline.CullOutputDetails;

/**
 * Class for passing the detailed audio rendering information through the
 * pipeline.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 2.1 $
 */
public class AudioCullOutputDetails extends CullOutputDetails
{
    /**
     * Construct a default instance with just the transform initialised to the
     * zero matrix.
     */
    public AudioCullOutputDetails()
    {
    }
}
