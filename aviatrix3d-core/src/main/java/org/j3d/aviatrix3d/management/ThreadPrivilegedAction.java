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
import java.security.AccessController;
import java.security.PrivilegedAction;

// Local imports
// None

/**
 * Helper class used to start a new thread in the context of a privileged
 * action block of code.
 * <p>
 *
 * Common to all the thread manager implementations so that the renderer can
 * create and start new threads when inside a security sand box, such as an
 * applet or JWS.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class ThreadPrivilegedAction implements PrivilegedAction
{
    /** The thread that we want to start */
    private Runnable targetThread;

    /** The completed, started thread. */
    private Thread runtimeThread;

    /**
     * Construct a new action that will start the given runnable as a new
     * thread.
     */
    ThreadPrivilegedAction(Runnable r)
    {
        if(r == null)
        {
            // No I18n here because this should be caught during development.
            throw new IllegalArgumentException("No runnable is defined");
        }

        targetThread = r;

        AccessController.doPrivileged(this);
    }

    //---------------------------------------------------------------
    // Methods defined by PrivilegedAction
    //---------------------------------------------------------------

    @Override
    public Object run()
    {
        runtimeThread = new Thread(targetThread, "AV3D Runtime");
        runtimeThread.start();
        return null;
    }

    //---------------------------------------------------------------
    // Loca Methods
    //---------------------------------------------------------------

    /**
     * Get the contained, started thread.
     *
     * @return The new thread instance
     */
    Thread getThread()
    {
        return runtimeThread;
    }
}
