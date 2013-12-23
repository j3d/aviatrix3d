/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2005
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
 * Renderable object that applies some visual effect rather than geometry.
 * <p>
 *
 * This interface is typically combined with other renderable types to augment
 * the geometry based on the specific object needs. This is used to control
 * what happens to the object during the rendering pass to know how to treat this
 * renderable. The other interfaces are used during the drawing pass to know
 * how to paint the object on screen.
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
 * Effects can have two different ranges of effects - local and global. A local
 * effect means that this node will be scoped to effect only children of the
 * parent group that this effect is a child of (if it is a child of a SharedNode
 * or anything that has only a single child, effectively nothing happens). A
 * global effect will scope to the entire scene using the transformations from
 * the root of the scene graph down to this node. This may be useful if you are
 * creating something like a lamp where the light needs to work with all the
 * scene's content, but is still transformed by the geometry to the light on the
 * end of the stalk.
 * <p>
 *
 * <b>Scoping Rules</b>
 * <p>
 *
 * Because effects can be very expensive to render, and real effects don't
 * effect everything, effects are typically limited in their rendering by scoping
 * rules. The default scoping of effects is to only modify anything that is under
 * the effect's parent group. Any object in a scenegraph path outside that
 * parent group is not effected by this effect instance. Even if the effect is
 * part of a shared scene graph, only the shared sections of the scenegraph are
 * effected, not all of the world.
 * <p>
 *
 * The default scoping rules can be modified through the use of several
 * attributes of this class. They may be global, scoped to the parent grouping
 * structure and/or limited to some specific set of bounds. A global effect will
 * effect everything in the scenegraph for this layer. It's position is
 * determined from the transformation heirarchy to this node (or multiples if
 * it is part of a shared scene graph). A bounds scoping allows the user to
 * provide an instance of a {@link BoundingVolume} object that defines how the
 * light is to effect geometry. The bounding volume is defined in the local
 * coordinate space of this effect. Any geometry that is potentially effected
 * by this effect then has its bounds checked for intersection with this
 * effect's bounds. If any part of the bounds are found to intersect, then the
 * effect is applied to that geometry. Note that this does not imply that only
 * the geometry that intersects with the effect's bounds has that effect applied
 * using appearance attributes (eg enabling lighting in a Material node) - all
 * the geometry will be effected if any part of the two bounds intersect.
 * <p>
 *
 * Bounds and global scope can be used together. If the effect is set to global
 * scope, and a bounds is set, then the rendering will check to see if each item
 * of geometry intersects it's bounds with the bounds of the global effect. If
 * there is no interesection, the effect is not applied to that geometry. If
 * intersection occurs, the object is rendered using the global effect.
 * <p>
 *
 * If the bounds of the effect are set to
 * {@link org.j3d.aviatrix3d.BoundingVoid}, it is treated as being off,
 * regardless of any other setting.
 * <p>
 *
 * All effects are scoped to be local to the scene that they're included in.
 * Effects do not bleed across layers or viewports.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 2.6 $
 */
public interface EffectRenderable extends CascadeRenderable
{
    /**
     * Get the current setting of the global-only flag.
     *
     * @return true if for global use only, false otherwise
     */
    public boolean isGlobalOnly();

    /**
     * Get the current bounding volume that this light effects. If the light is
     * to effect everything for infinite distance, then this will return null.
     *
     * @return A bounding volume if there is to be bounds, null for none.
     */
    public BoundingVolume getEffectBounds();
}
