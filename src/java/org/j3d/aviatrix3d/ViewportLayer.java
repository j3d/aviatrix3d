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
 * An abstract layer definition for per-viewport layer rendering.
 * <p>
 *
 * A layer is a composite of objects that are applied in a sequential manner
 * to the given surface, within one specific viewport. Between each layer
 * the depth buffer is cleared and a new rendering is applied directly over
 * the top of the previous. Colour buffers or other buffers are not cleared.
 * <p>
 *
 * <b>Implementation Notes</b>
 * <p>
 *
 * Even though this class is almost identical to the {@link Layer} class, a
 * separate heirarchy is defined so that users cannot accidently use viewport
 * layers at the root of the scene graph, and vice versa.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public abstract class ViewportLayer extends SceneGraphObject
{
    /** The layer is a simple type, having single scene defined */
    public static final int SIMPLE = 0;

    /** The layer is a 2D type, having only 2D rendering used */
    public static final int FLAT = 1;

    /** The layer contains multipass rendering */
    public static final int MULTIPASS = 2;


    /** The layer type constant */
    protected final int layerType;

    /** The lower left X position of the viewpoint, in pixels */
    protected int viewX;

    /** The lower left Y position of the viewpoint, in pixels */
    protected int viewY;

    /** The width of the viewport in pixels */
    protected int viewWidth;

    /** The width of the viewport in pixels */
    protected int viewHeight;

    /**
     * Construct a new layer of the given type. One of the standard types may
     * be used, or a custom type.
     *
     * @param type The type constant for this layer
     */
    protected ViewportLayer(int type)
    {
        layerType = type;
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the viewport dimensions from the parent viewport. These dimensions
     * are pushed down through the scene to the viewport.
     *
     * @param x The lower left x coordinate for the view
     * @param y The lower left y coordinate for the view
     * @param width The width of the viewport in pixels
     * @param height The height of the viewport in pixels
     */
    void setViewportDimensions(int x, int y, int width, int height)
    {
        viewX = x;
        viewY = y;
        viewWidth = width;
        viewHeight = height;
    }

    /**
     * Get the type that this layer represents
     *
     * @return A layer type descriptor
     */
    public int getType()
    {
        return layerType;
    }

    /**
     * Internal check to see if this scene has an update handler registered.
     * If it does, that is a sign that it currently has a parent registered for
     * it.
     */
    boolean hasParent()
    {
        return updateHandler != null;
    }
}
