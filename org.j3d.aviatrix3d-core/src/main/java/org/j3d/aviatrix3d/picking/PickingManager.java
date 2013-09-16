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

package org.j3d.aviatrix3d.picking;

// External imports
import java.util.ArrayList;

// Local imports
import org.j3d.util.ErrorReporter;

/**
 * A interface that describes an internal implemention of pick intersection
 * tests.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface PickingManager
{
    /**
     * Register an error reporter with the engine so that any errors generated
     * by the node's internals can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter);

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given set of requests.
     *
     * @param root The root point to start the pick processing from
     * @param req The list of picks to be made, starting at this object
     * @param numRequests The number of valid pick requests to process
     * @throws NotPickableException This object has been marked as non pickable,
     *   but you decided to try to call the method anyway
     */
    public void pickBatch(PickTarget root, PickRequest[] req, int numRequests)
        throws NotPickableException;

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given single request.
     *
     * @param root The root point to start the pick processing from
     * @param req The details of the pick to be made
     * @throws NotPickableException This object has been marked as non pickable,
     *   but you decided to try to call the method anyway
     */
    public void pickSingle(PickTarget root, PickRequest req)
        throws NotPickableException;
}
