/*****************************************************************************
 *                   Yumetech, Inc Copyright (c) 2004 - 2007
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
// None

/**
 * Handles the output of the geometry sorter.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 3.3 $
 */
public class SortToSingleDrawListener implements SortedGeometryReceiver
{
    /** The surface that the sorter passes the output to */
    private GraphicsOutputDevice device;

    /**
     * Create a new default instance of this class with nothing set.
     */
    public SortToSingleDrawListener()
    {
    }

    //---------------------------------------------------------------
    // Methods defined by SortedGeometryReceiver
    //---------------------------------------------------------------

    /**
     * Here's the sorted output list of nodes.
     *
     * @param otherData Other graphics request data
     * @param profilingData Timing and load data
     * @param commands The list of drawable surfaces to render
     * @param numValid The number of valid items in the array
     */
    public void sortedOutput(GraphicsRequestData otherData,
                             GraphicsProfilingData profilingData,
                             GraphicsInstructions[] commands,
                             int numValid)
    {
        if(device != null)
            device.setDrawableObjects(otherData, commands, numValid);
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Set the surfaces instance to be used as the target. Passing a value of
     * null will clear the current registered instance.
     *
     * @param device The surface instance to use or null
     */
    public void setGraphicsOutputDevice(GraphicsOutputDevice device)
    {
        this.device = device;
    }
}
