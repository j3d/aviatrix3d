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
 * @version $Revision: 3.2 $
 */
public interface SortedGeometryReceiver
{
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
                             int numValid);
}
