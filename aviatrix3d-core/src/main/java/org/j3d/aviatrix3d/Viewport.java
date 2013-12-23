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
 * Abstract representation of a viewport on the drawable surface.
 * <p>
 *
 * A viewport defines the amount of screen to cover in pixel dimensions.
 * Viewports may be used to segregate the rendered surface into multiple
 * separate entities. An example of this is the 4-view CAD application,
 * where each viewport has a different viewpoint looking into a shared
 * scene graph structure.
 * <p>
 *
 * Viewports define their coordinate system in the window-system interface:
 * The x,y position is the lower left corner, with height going up the screen
 * and width heading to the right.
 * <p>
 *
 * Viewports cannot be shared amongst multiple parent layers.
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public abstract class Viewport extends SceneGraphObject
{
    /** The viewport is a simple type, having single scene defined */
    public static final int SIMPLE = 0;

    /** The viewport is a composite type, having many local layers defined */
    public static final int COMPOSITE = 1;

    /**
     * The viewport is a multipass type, allowing multiple rendering passes
     * for a single layer.
     */
    public static final int MULTIPASS = 2;

    /**
     * The viewport is a 2D type, allowing only flat projections and
     * automatically calculated view environment information.
     */
    public static final int FLAT = 3;

    /** The viewport type constant */
    protected final int viewportType;

    /** The lower left X position of the viewpoint, in pixels */
    protected int viewX;

    /** The lower left Y position of the viewpoint, in pixels */
    protected int viewY;

    /** The width of the viewport in pixels */
    protected int viewWidth;

    /** The width of the viewport in pixels */
    protected int viewHeight;

    /** Check to see if we have a valid viewport */
    private boolean valid;

    /**
     * Create a default instance of this viewport. All values are set to zero.
     *
     * @param type The type constant for this layer
     */
    protected Viewport(int type)
    {
        viewportType = type;
        valid = false;
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the dimensions of the viewport in pixels. Coordinates are defined in
     * the space of the parent component that is being rendered to.
     *
     * @param x The lower left x coordinate for the view
     * @param y The lower left y coordinate for the view
     * @param width The width of the viewport in pixels
     * @param height The height of the viewport in pixels
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void setDimensions(int x, int y, int width, int height)
        throws InvalidWriteTimingException
    {
        if(isLive() && (updateHandler != null) &&
            !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(getAppUpdateWriteTimingMessage());

        viewX = x;
        viewY = y;
        viewWidth = width;
        viewHeight = height;

        valid = (width > 0) && (height > 0) &&
                (x + width > 0) && (y + height > 0);
    }

    /**
     * Get the dimensions of the viewport, copied into the user-provided array.
     * The array must be at least 4 units in length, and the values are copied
     * in the order x, y, width, height.
     *
     * @param dim The array to copy the values into
     */
    public void getDimensions(int[] dim)
    {
        dim[0] = viewX;
        dim[1] = viewY;
        dim[2] = viewWidth;
        dim[3] = viewHeight;
    }

    /**
     * Convenience method to fetch the starting X position.
     *
     * @return The lower left X coordinate, in pixels
     */
    public int getX()
    {
        return viewX;
    }

    /**
     * Convenience method to fetch the starting Y position.
     *
     * @return The lower left Y coordinate, in pixels
     */
    public int getY()
    {
        return viewY;
    }

    /**
     * Convenience method to fetch the width of the viewport.
     *
     * @return The width amount, in pixels
     */
    public int getWidth()
    {
        return viewWidth;
    }

    /**
     * Convenience method to fetch the height of the viewport.
     *
     * @return The height amount, in pixels
     */
    public int getHeight()
    {
        return viewHeight;
    }

    /**
     * Get the type that this layer represents
     *
     * @return A layer type descriptor
     */
    public int getType()
    {
        return viewportType;
    }

    /**
     * Check to see if this is a valid viewport. A valid viewport requires
     * that the width and height are both greater than zero and that X + width
     * and Y + height are positive.
     *
     * @return true if the viewport represents a valid screen space
     */
    public boolean isValid()
    {
        return valid;
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
