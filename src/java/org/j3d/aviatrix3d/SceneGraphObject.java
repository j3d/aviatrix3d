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
 * The SceneGraphObject is a common superclass for all scene graph objects.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class SceneGraphObject
{
    /** The scene this node belongs to */
    protected NodeUpdateHandler updateHandler;

    /**
     * Set the scenegraph update handler for this node.  It will notify
     * all its children of the value. A null value will clear the current
     * handler.
     *
     * @param handler The instance to use as a handler
     */
    public void setUpdateHandler(NodeUpdateHandler handler)
    {
        updateHandler = handler;
    }

    /**
     * Notify the node that you have updates to the node that might alter
     * its bounds.
     *
     * @param l The change requestor
     */
    public void boundsChanged(NodeUpdateListener l)
    {
        // Ignore till live
        if(updateHandler == null)
            return;

        updateHandler.boundsChanged(l);
    }

    /**
     * Notify the node that you have updates to the node that will not
     * alter its bounds.
     *
     * @param l The change requestor
     */
    public void dataChanged(NodeUpdateListener l)
    {
        // Ignore till live
        if(updateHandler == null)
            return;

        updateHandler.dataChanged(l);
    }
}