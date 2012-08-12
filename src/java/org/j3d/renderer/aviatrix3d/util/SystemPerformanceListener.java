/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.renderer.aviatrix3d.util;

// External Imports
// None

// Local imports
// None

/**
 * A listener for monitoring system performance of the application.
 * <p>
 *
 * A user of this interface can use the information to control its own
 * behaviour based on how the current user's system is performing. For
 * example, if the frame rate is bogging down, the listener implementation
 * could drop to a lighter weight shader implementation of lighting.
 * <p>
 *
 * The listener will be called during the scene update cycle, so you
 * will be safe to make AV3D calls to register for bounds updates etc
 * when this is called.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface SystemPerformanceListener
{

    /**
     * Notification of a performance downgrade is required in the system.
     * This listener should attempt to reduce it's performance demands now if
     * it is able to and return true. If not able to reduce it the return
     * false.
     *
     * @return True if the performance demands were decreased
     */
    public boolean downgradePerformance();

    /**
     * Notification of a performance upgrade is required by the system. This
     * listener is free to increase performance demands of the system. If it
     * does upgrade, return true, otherwise return false.
     *
     * @return True if the performance demands were increased
     */
    public boolean upgradePerformance();
}
