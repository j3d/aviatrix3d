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
 * An cullable that represents the end of the rendering traversal and results in
 * something that can be rendered now.
 * <p>
 *
 * This object transforms the cullable object into a renderable object. The
 * renderable can then be tested for other properties once it is decided that
 * this cullable is acceptable.
 * <p>
 *
 * For the purposes of fast rendering traversal, leaf cullables have a constant
 * that is returned describing their basic function in the scene graph.
 *
 * @author Justin Couch
 * @version $Revision: 2.4 $
 */
public interface LeafCullable extends Cullable
{
    /** This is a fog-type cullable */
    public static final int FOG_CULLABLE = 1;

    /** This is a light-type cullable */
    public static final int LIGHT_CULLABLE = 2;

    /** This is a clip plane-type cullable */
    public static final int CLIP_CULLABLE = 3;

    /** This is a any cullable that provides visuals for rendering */
    public static final int GEOMETRY_CULLABLE = 4;

    /** This is a any cullable that provides audio source */
    public static final int AUDIO_CULLABLE = 5;

    /**
     * This is a any cullable that provides items that override other properties
     * as you traverse the scene graph.
     */
    public static final int OVERRIDE_CULLABLE = 6;

    /**
     * Get the type that this cullable represents.
     *
     * @return One of the _CULLABLE constants
     */
    public int getCullableType();

    /**
     * Get the currently set bounds for this object. If no explicit bounds have
     * been set, then an implicit set of bounds is returned based on the
     * current scene graph state.
     *
     * @return The current bounds of this object
     */
    public BoundingVolume getBounds();

    /**
     * Get the child renderable of this object.
     *
     * @return an array of nodes
     */
    public Renderable getRenderable();
}
