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

// Standard imports
// None

// Application specific imports
// None

/**
 * A listener interface for notification that its safe to update a nodes
 * representation in the Scene Graph.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public interface NodeUpdateListener
{
    /**
     * Notification that its safe to update the node now.
     */
    public void updateNode();
}