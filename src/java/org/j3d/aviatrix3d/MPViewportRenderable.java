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
import javax.media.opengl.GL;

// Local imports
import org.j3d.aviatrix3d.rendering.ViewportRenderable;

/**
 * Implementation of the multipass viewport renderable object.
 * <p>
 *
 * The viewport can be modified in an individual pass to achieve unusual
 * effects.
 *
 * @author Justin Couch
 * @version $Revision: 2.1 $
 */
class MPViewportRenderable implements ViewportRenderable
{
    /** X component of the top left position of the viewport */
    private int left;

    /** Y component of the top left position of the viewport */
    private int top;

    /** The width of the viewport in pixels */
    private int width;

    /** The height of the viewport in pixels */
    private int height;

    MPViewportRenderable()
    {
    }

    //---------------------------------------------------------------
    // Methods defined by ViewportRenderable
    //---------------------------------------------------------------

    /**
     * Render the viewport changes now
     *
     * @param gl The GL context to render with
     */
    public void render(GL gl)
    {
        gl.glViewport(left, top, width, height);
    }

    //---------------------------------------------------------------
    // Methods defined by Comparable
    //---------------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param o The objec to be compared
     * @return -1, 0 or 1 depending on order
     * @throws ClassCastException The specified object's type prevents it from
     *    being compared to this Object
     */
    public int compareTo(Object o)
        throws ClassCastException
    {
        MPViewportRenderable app = (MPViewportRenderable)o;
        return compareTo(app);
    }

    //---------------------------------------------------------------
    // Methods defined by Object
    //---------------------------------------------------------------

    /**
     * Compare this object for equality to the given object. This provides a
     * local interface-driven access to the normal {@link java.lang.Object}
     * version of equals, without needing to cast.
     *
     * @param o The object to be compared
     * @return True if these represent the same values
     */
    public boolean equals(Object o)
    {
        if(!(o instanceof MPViewportRenderable))
            return false;

        MPViewportRenderable mvp = (MPViewportRenderable)o;

        return (mvp.top == top) && (mvp.left == left) &&
               (mvp.width == width) && (mvp.height == height);
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Set the viewport dimensions. All values in pixels
     *
     * @param x The left position of the location
     * @param y The top position of the location
     * @param w The width of the viewport
     * @param h The left position of the location
     */
    void setDimensions(int x, int y, int w, int h)
    {
        top = y;
        left = x;
        width = w;
        height = h;
    }

    /**
     * Get the viewport dimensions.
     *
     * @param vals The array to copy dimensions in to
     */
    void getDimensions(float[] vals)
    {
        vals[0] = left;
        vals[1] = top;
        vals[2] = width;
        vals[3] = height;
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param app The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    int compareTo(MPViewportRenderable app)
    {
        if(app == null)
            return 1;

        if(app == this)
            return 0;

        if(top != app.top)
            return top > app.top ? 1 : -1;

        if(left != app.left)
            return left > app.left ? 1 : -1;

        if(width != app.width)
            return width > app.width ? 1 : -1;

        if(height != app.height)
            return height > app.height ? 1 : -1;

        return 0;
    }

}
