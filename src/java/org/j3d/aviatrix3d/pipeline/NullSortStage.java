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
import org.j3d.aviatrix3d.SortStage;
import org.j3d.aviatrix3d.SortedGeometryReceiver;
import org.j3d.aviatrix3d.Node;

/**
 * Implementation of the sort stage that does nothing.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class NullSortStage implements SortStage
{
    /** Receiver for the output of this stage */
    private SortedGeometryReceiver receiver;

    /**
     * Create an empty sorting stage
     */
    public NullSortStage()
    {
    }

    /**
     * Sort the listing of nodes in the given array. Do not return until the
     * sort has been completed.
     */
    public void sort(Node[] nodes, int numNodes)
    {
    }

    /**
     * Register a reciever for the output of the sorter. If the value is null,
     * it will clear the currently set receiver.
     *
     * @param sgr The receiver instance to add or null
     */
    public void setSortedGeometryReceiver(SortedGeometryReceiver sgr)
    {
        receiver = sgr;
    }
}