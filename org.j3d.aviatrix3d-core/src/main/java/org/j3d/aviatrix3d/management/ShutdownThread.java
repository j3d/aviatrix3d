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

package org.j3d.aviatrix3d.management;

// External imports
// None

// Local imports
// None

/**
 * Helper class used to provide the system shutdown hook thread.
 * <p>
 *
 * Implementation just takes the system manager and calls the shutdown()
 * method directly on it when the system is shutting down.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class ShutdownThread extends Thread
{
    /** The render manager that is going to be the target for the callback */
    private RenderManager renderManager;

    /**
     * Construct a new action that will start the given runnable as a new
     * thread.
     */
    ShutdownThread(RenderManager mgr)
    {
        super("Aviatrix3D Shutdown handler");

        renderManager = mgr;
    }

    //---------------------------------------------------------------
    // Methods defined by Thread
    //---------------------------------------------------------------

    /**
     * Tell render to start or stop management. If currently running, it
     * will wait until all the pipelines have completed their current cycle
     * and will then halt.
     */
    public void run()
    {
        renderManager.shutdown();
    }
}
