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

package org.j3d.aviatrix3d;

// External imports
import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.picking.*;

import org.j3d.aviatrix3d.rendering.BoundingVolume;
import org.j3d.aviatrix3d.rendering.Cullable;
import org.j3d.aviatrix3d.rendering.GroupCullable;
import org.j3d.aviatrix3d.rendering.SingleCullable;


/**
 * Marker interface used to allow group nodes to work out when a child is
 * multiparented.
 * <p>
 *
 * For live state checks you need to call the methods in this class so that
 * we know which parent was calling the update. This is needed so that the
 * multiparented objects can do a check through all the parents to find out
 * whether it should still be live, while eliminating the caller from the
 * check. We need to eliminate the caller because at the point the setLive()
 * method is called the parent will not have yet changed over its state to
 * the final state, due to needing the children updated first for the bounds
 * update.
 *
 * @author Justin Couch
 * @version $Revision: 2.1 $
 */
public interface MultiParentNode
{
    /**
     * Overloaded version of the notification that this object's liveness state
     * has changed. We overload with the caller so that for shared
     *
     * @param caller The node calling us with the state changes
     * @param state true if this should be marked as live now
     */
    public void setLive(Node caller, boolean state);
}
