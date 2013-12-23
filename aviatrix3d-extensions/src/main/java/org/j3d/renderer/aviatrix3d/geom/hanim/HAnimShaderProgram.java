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

package org.j3d.renderer.aviatrix3d.geom.hanim;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.NodeUpdateHandler;
import org.j3d.aviatrix3d.ShaderProgram;

/**
 * Custom ShaderProgram implementation for dealing with the needs of hardware shaded
 * HAnim characters.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
class HAnimShaderProgram extends ShaderProgram
{
    /**
     * Constructs a shader with nothing set. When used, it will have no effect.
     */
    HAnimShaderProgram()
    {
    }

    //---------------------------------------------------------------
    // Methods defined by SceneGraphObject
    //---------------------------------------------------------------

    /**
     * Set the scenegraph update handler for this node.  It will notify
     * all its children of the value. A null value will clear the current
     * handler.
     *
     * @param handler The instance to use as a handler
     */
    protected void setUpdateHandler(NodeUpdateHandler handler)
    {
        super.setUpdateHandler(handler);
    }

    /**
     * Notification that this object is live now. Overridden to make sure that
     * the live state of the nodes represents the same state as the parent
     * scene graph.
     *
     * @param state true if this should be marked as live now
     */
    protected void setLive(boolean state)
    {
        super.setLive(state);
    }
}
