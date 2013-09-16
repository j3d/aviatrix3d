/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.DrawableSurface;
import org.j3d.aviatrix3d.SortedGeometryReceiver;
import org.j3d.aviatrix3d.Node;

/**
 * Handles the output of the geometry sorter.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class SortToSingleDrawListener implements SortedGeometryReceiver
{
    /** The surface that the sorter passes the output to */
    private DrawableSurface surface;

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
     * @param nodes The list of nodes in sorted order
     * @param numValid The number of valid items in the array
     */
    public void sortedOutput(Node[] nodes, int numValid)
    {
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Set the sorter instance to be used as the target. Passing a value of
     * null will clear the current registered instance.
     *
     * @param s The sorter instance to use or null
     */
    public void setSurface(DrawableSurface ds)
    {
        surface = ds;
    }
}