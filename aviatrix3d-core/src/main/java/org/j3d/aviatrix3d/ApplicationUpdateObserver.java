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

package org.j3d.aviatrix3d;

// External imports
// None

// Local imports
// None

/**
 * Observer of the rendering system that is informed when it is safe to update
 * the scene graph from application code.
 * <p>
 *
 * The observer is used to synchronise the user-level application with the
 * scene graph and it's update cycle. The observer signals the point in time
 * when it is a valid and safe time to make changes to the scene graph. During
 * the time of this callbacks, the user code may register that it needs to
 * change parts of the scene graph, but cannot make the changes. Direct changes
 * are made during the callbacks from the {@link NodeUpdateListener} methods.
 * <p>
 *
 * Applications may opt to run their own threads independent of the clocking
 * provided by this interface, however, they must buffer their changes and make
 * them in response to the callbacks.
 *
 * @author Justin Couch
 * @version $Revision: 2.1 $
 */
public interface ApplicationUpdateObserver
{
    /**
     * Notification that now is a good time to synchronise application code
     * with scene graph.
     */
    public void updateSceneGraph();

    /**
     * Notification that the AV3D internal shutdown handler has detected a
     * system-wide shutdown. The aviatrix code has already terminated rendering
     * at the point this method is called, only the user's system code needs to
     * terminate before exiting here.
     */
    public void appShutdown();
}
