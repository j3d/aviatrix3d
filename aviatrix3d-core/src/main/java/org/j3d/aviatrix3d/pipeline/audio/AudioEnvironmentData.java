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

package org.j3d.aviatrix3d.pipeline.audio;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.pipeline.RenderEnvironmentData;

/**
 * Data holder class used to pass the current environment data along the audio
 * rendering pipeline.
 * <p>
 *
 * End-user code should never be making use of this class unless the end user is
 * implementing a customised rendering pipeline. This class is used as a simple
 * internal collection of the per-frame renderable data that is passed along
 * each stage of the pipeline.
 *
 * @author Alan Hudson
 * @version $Revision: 2.1 $
 */
public class AudioEnvironmentData extends RenderEnvironmentData
{
    /**
     * Create a new instance of this class. The variables are initialized
     * to their default values and arrays constructed.
     */
    public AudioEnvironmentData()
    {
    }
}
