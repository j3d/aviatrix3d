/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2009
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d.rendering;

// External imports
// None

// Local imports
// None

/**
 * Abstracted renderable representation of objects that can appear at places
 * other than leaf nodes and effect the children of the current cullable
 * object.
 * <p>
 *
 * <b>State Management</b>
 * <p>
 *
 * Effects can be enabled. If they are enabled they effect all geometry from
 * their parent group and down the scene graph. When it is disabled, it will
 * have no effect. The rendering process will ignore the group.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 2.1 $
 */
public interface CascadeRenderable extends Renderable
{
    /**
     * Get the current enabled state of the renderable. A value of true means
     * that this effect is current and can be used to render objects in the
     * world.
     *
     * @return True if this effect is active and can effect the world
     */
    public boolean isEnabled();
}
