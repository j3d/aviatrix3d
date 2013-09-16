/*****************************************************************************
 *                    Yumetech, Inc Copyright (c) 2004 - 2007
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
import org.j3d.aviatrix3d.pipeline.OutputDevice;
import org.j3d.aviatrix3d.pipeline.RenderableRequestData;

/**
 * Interface representing the output device for an audio pipeline..
 * <p>
 * Conceptually an AudioOutputDevice might run all speakers or just part of
 * the total soundscape.
 *
 * @author Alan Hudson
 * @version $Revision: 2.2 $
 */
public interface AudioOutputDevice extends OutputDevice
{
    /**
     * Update the list of items to be rendered to the current list. Draw them
     * at the next oppourtunity.
     *
     * @param otherData data to be processed before the rendering
     * @param commands The list of drawable surfaces to render
     */
    public void setDrawableObjects(RenderableRequestData otherData,
                                   AudioInstructions commands);
}
