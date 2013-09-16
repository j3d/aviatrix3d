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
// None

// Local imports
import org.j3d.aviatrix3d.pipeline.RenderableRequestData;

/**
 * Handles the output of the audio sorter.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 2.1 $
 */
public class AudioSortToSingleDeviceListener implements SortedAudioReceiver
{
    /** The surface that the sorter passes the output to */
    private AudioOutputDevice device;

    /**
     * Create a new default instance of this class with nothing set.
     */
    public AudioSortToSingleDeviceListener()
    {
    }

    //---------------------------------------------------------------
    // Methods defined by SortedGeometryReceiver
    //---------------------------------------------------------------

    /**
     * Here's the sorted output list of nodes.
     *
     * @param otherData data to be processed before the rendering
     * @param commands The list of drawable surfaces to render
     */
    public void sortedOutput(RenderableRequestData otherData,
                             AudioInstructions commands)
    {
        if(device != null)
            device.setDrawableObjects(otherData, commands);
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Set the device instance to be used as the target. Passing a value of
     * null will clear the current registered instance.
     *
     * @param ad The surface instance to use or null
     */
    public void setDevice(AudioOutputDevice ad)
    {
        device = ad;
    }
}
