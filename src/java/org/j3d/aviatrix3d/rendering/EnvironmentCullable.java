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

package org.j3d.aviatrix3d.rendering;

// External imports
// None

// Local imports
// None

/**
 * Cullable describing a node that is used for the environmental effects
 * such as viewpoints and backgrounds.
 * <p>
 *
 * This describes a leaf type of cullable that is used to represent
 * environmental effects, that are provided as part of the view environment
 * of a scene. One particular requirement is that this cullable has to be
 * traversed in reverse to find the transformation from the root of the
 * scene down to it - hence the reason for the getCullableParent() method.
 * Tracing parenting is also important to work out if we have an orphaned
 * end point that has no access to the root of the scene graph - thus
 * knowing whether we should ignore it or not.
 *
 * @author Justin Couch
 * @version $Revision: 2.1 $
 */
public interface EnvironmentCullable extends Cullable
{
    /**
     * Get the parent cullable of this instance.
     *
     * @return The parent instance
     */
    public Cullable getCullableParent();

    /**
     * Get the renderable that represents the environment node rendering.
     *
     * @return The renderable responsible for this node
     */
    public Renderable getRenderable();
}
