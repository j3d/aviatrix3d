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

package org.j3d.aviatrix3d.pipeline;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.CulledGeometryReceiver;
import org.j3d.aviatrix3d.Node;
import org.j3d.aviatrix3d.SortStage;

/**
 * Adapter class that maps the output of a cull stage to a single sorter.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class CullToSingleSortListener implements CulledGeometryReceiver
{
    /** The sorter to hand the nodes to */
    private SortStage sorter;

    /**
     * Create a new default instance of this class with nothing set.
     */
    public CullToSingleSortListener()
    {
    }

    //---------------------------------------------------------------
    // Methods defined by CulledGeometryReceiver
    //---------------------------------------------------------------

    /**
     * Here's the sorted output list of nodes.
     *
     * @param nodes The list of nodes in sorted order
     * @param numValid The number of valid items in the array
     */
    public void culledOutput(Node[] nodes, int numValid)
    {
        if(sorter != null)
            sorter.sort(nodes, numValid);
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
    public void setSorter(SortStage s)
    {
        sorter = s;
    }
}