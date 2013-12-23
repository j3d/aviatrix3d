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
import java.nio.ByteBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.picking.*;

/**
 * Raster object that represents a coloured image taken from a Java AWT
 * Image object.
 * <p>
 *
 * The image raster can contain 1 to 4 colour components in.
 *
 * @author Justin Couch
 * @version $Revision: 2.7 $
 */
public class ImageRaster extends Raster
{
    /** Specifies the source is in RGB format */
    public static final int FORMAT_RGB = 1;

    /** Specifies the source is in RGBA format */
    public static final int FORMAT_RGBA = 2;

    /** Specifies the source is in Windows BGR format */
    public static final int FORMAT_BGR = 3;

    /** Specifies the source is in Windows BGRA format */
    public static final int FORMAT_BGRA = 4;

    /** Specifies the source is in 2-component Intensity-Alpha format */
    public static final int FORMAT_INTENSITY_ALPHA = 5;

    /** Specifies the source is in 1-component Intensity (greyscale) format */
    public static final int FORMAT_INTENSITY = 6;

    /** Message when the width is less than or equal to zero */
    private static final String NEG_WIDTH_PROP =
        "org.j3d.aviatrix3d.ImageRaster.negWidthMsg";

    /** Message when the height is less than or equal to zero */
    private static final String NEG_HEIGHT_PROP =
        "org.j3d.aviatrix3d.ImageRaster.negHeightMsg";

    /** Error message when the provided bit array is too small */
    private static final String BIT_ARRAY_SIZE_PROP =
        "org.j3d.aviatrix3d.ImageRaster.bitArraySizeMsg";

    /** Error message when the format type supplied is not known */
    private static final String INVALID_FORMAT_PROP =
        "org.j3d.aviatrix3d.ImageRaster.invalidPixelFormatMsg";


    /** Buffer for bitmap pixel data */
    private ByteBuffer pixelBuffer;

    /** The format supplied by the user */
    private int pixelFormat;

    /** Number of bytes per pixel */
    private int bytesPerPixel;

    /** The OpenGL format of the pixels for glDrawPixels */
    private int glPixelFormat;

    /** Local reference to the pixel data */
    private byte[] pixels;

    /** The width of the bitmap in pixels */
    private int pixelWidth;

    /** The height of the bitmap in pixels */
    private int pixelHeight;

    /**
     * Create a new empty raster with the given width and height.
     *
     * @param width The width of the raster in pixels
     * @param height The height of the raster in pixels
     * @param format One of the FORMAT_ values describing the byte format
     * @throws IllegalArgumentException The width or height are not positive
     *    or an invalid pixel format
     */
    public ImageRaster(int width, int height, int format)
    {
        if(width <= 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NEG_WIDTH_PROP) + width;
            throw new IllegalArgumentException(msg);
        }

        if(height <= 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NEG_HEIGHT_PROP) + height;
            throw new IllegalArgumentException(msg);
        }

        int pixel_size = getBytesPerPixel(format);

        if(pixel_size == -1)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(INVALID_FORMAT_PROP);
            throw new IllegalArgumentException(msg);
        }

        glPixelFormat = getPixelFormat(format);

        pixelWidth = width;
        pixelHeight = height;
        pixelFormat = format;
        bytesPerPixel = pixel_size;

        pixelBuffer =
            ByteBuffer.allocateDirect(height * width * bytesPerPixel);
    }

    //---------------------------------------------------------------
    // Methods defined by GeometryRenderable
    //---------------------------------------------------------------

    /**
     * Render the geometry now.
     *
     * @param gl The GL context to render with
     */
    @Override
    public void render(GL2 gl)
    {
        gl.glDrawPixels(pixelWidth,
                        pixelHeight,
                        glPixelFormat,
                        GL.GL_UNSIGNED_BYTE,
                        pixelBuffer);
    }

    //---------------------------------------------------------------
    // Methods defined by LeafPickTarget
    //---------------------------------------------------------------

    /**
     * Check for all intersections against this geometry using a line segment and
     * return the exact distance away of the closest picking point. Default
     * implementation always returns false indicating that nothing was found.
     * Derived classes should override and provide a real implementation.
     *
     * @param start The start point of the segment
     * @param end The end point of the segment
     * @param findAny True if it only has to find a single intersection and can
     *   exit as soon as it finds the first intersection. False if it must find
     *   the closest polygon
     * @param dataOut An array to put the data in for the intersection. Exact
     *   format is described by the flags
     * @param dataOutFlags A set of derived-class specific flags describing what
     *   data should be included in the output array
     * @return True if an intersection was found according to the input request
     * @throws NotPickableException This object has been marked as non pickable,
     *   but you decided to try to call the method anyway
     */
    @Override
    public boolean pickLineSegment(float[] start,
                                   float[] end,
                                   boolean findAny,
                                   float[] dataOut,
                                   int dataOutFlags)
        throws NotPickableException
    {
        super.pickLineSegment(start, end, findAny, dataOut, dataOutFlags);

        return false;
    }

    /**
     * Check for all intersections against this geometry using a line ray and
     * return the exact distance away of the closest picking point. Default
     * implementation always returns false indicating that nothing was found.
     * Derived classes should override and provide a real implementation.
     *
     * @param origin The start point of the ray
     * @param direction The direction vector of the ray
     * @param findAny True if it only has to find a single intersection and can
     *   exit as soon as it finds the first intersection. False if it must find
     *   the closest polygon
     * @param dataOut An array to put the data in for the intersection. Exact
     *   format is described by the flags
     * @param dataOutFlags A set of derived-class specific flags describing what
     *   data should be included in the output array
     * @return True if an intersection was found according to the input request
     * @throws NotPickableException This object has been marked as non pickable,
     *   but you decided to try to call the method anyway
     */
    @Override
    public boolean pickLineRay(float[] origin,
                               float[] direction,
                               boolean findAny,
                               float[] dataOut,
                               int dataOutFlags)
        throws NotPickableException
    {
        super.pickLineRay(origin, direction, findAny, dataOut, dataOutFlags);

        return false;
    }

    //----------------------------------------------------------
    // Methods defined by Raster
    //----------------------------------------------------------

    /**
     * Update this node's bounds and then call the parent to update it's
     * bounds. Used to propogate bounds changes from the leaves of the tree
     * to the root. A node implementation may decide when and where to tell
     * the parent(s)s that updates are ready.
     */
    @Override
    protected void updateBounds()
    {
        if(pixels != null)
            super.updateBounds();
    }

    /**
     * Internal method to recalculate the implicit bounds of this Node. By
     * default the bounds are a point sphere, so derived classes should
     * override this method with something better.
     */
    @Override
    protected void recomputeBounds()
    {
        if(pixels != null)
            return;

        BoundingBox bbox = (BoundingBox)bounds;

        if(bounds == null)
        {
            bbox = new BoundingBox();
            bounds = bbox;
        }

        bbox.setMinimum(0, 0, 0);
        bbox.setMaximum(pixelWidth, pixelHeight, 0);
    }

    //----------------------------------------------------------
    // Methods defined by SceneGraphObject
    //----------------------------------------------------------

    /**
     * Notification that this object is live now. Overridden to make sure that
     * the live state of the nodes represents the same state as the parent
     * scene graph.
     *
     * @param state true if this should be marked as live now
     */
    @Override
    protected void setLive(boolean state)
    {
        // Ignore stuff that doesn't change the state
        if(state == alive)
            return;

        boolean old_state = alive;

        super.setLive(state);

        if(!old_state && state)
            recomputeBounds();
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
    @Override
    public int compareTo(Object o)
        throws ClassCastException
    {
        ImageRaster app = (ImageRaster)o;
        return compareTo(app);
    }

    //---------------------------------------------------------------
    // Methods defined by Object
    //---------------------------------------------------------------

    /**
     * Compare this object for equality to the given object.
     *
     * @param o The object to be compared
     * @return True if these represent the same values
     */
    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof ImageRaster))
            return false;
        else
            return equals((ImageRaster)o);
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Get the height of this bitmap.
     *
     * @return the height.
     */
    public int getHeight()
    {
        return pixelHeight;
    }

    /**
     * Get the width of this bitmap.
     *
     * @return the width.
     */
    public int getWidth()
    {
        return pixelWidth;
    }

    /**
     * Set the pixel data for the bitmap contents. Each byte represents 8
     * pixels of the image. See class documentation for more information. A
     * null reference can be used to clear the bitmap and not have anything
     * appear on screen.
     *
     * @param pixels The bytes to use for the raster or null to clear
     * @throws IllegalArgumentException The number of bytes is not sufficient to
     *   fulfill the previously set width and height of the bitmap
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data callback method
     */
    public void setBits(byte[] pixels)
        throws InvalidWriteTimingException, IllegalArgumentException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(pixels == null)
        {
            pixelBuffer.clear();
            return;
        }

        if(pixels.length < pixelHeight * pixelWidth * bytesPerPixel)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(BIT_ARRAY_SIZE_PROP);
            throw new IllegalArgumentException(msg);
        }

        pixelBuffer.put(pixels, 0, pixelHeight * pixelWidth * bytesPerPixel);
        pixelBuffer.rewind();
    }

    /**
     * Set the pixel data for the bitmap contents and change the size of the
     * raster at the same time. Each byte represents 8 pixels of the image.
     * See class documentation for more information. A null reference can be
     * used to clear the bitmap and not have anything appear on screen. Because
     * this is also changing the bounds of the image, it needs to be called
     * during the bounds callback, not the data callback like the other
     * setBits method.
     *
     * @param pixels The bytes to use for the raster or null to clear
     * @param width The width of the raster in pixels
     * @param height The height of the raster in pixels
     * @param format One of the FORMAT_ values describing the byte format
     * @throws IllegalArgumentException The width or height are not positive
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds callback method
     */
    public void setBits(byte[] pixels, int width, int height, int format)
        throws InvalidWriteTimingException, IllegalArgumentException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        if(width <= 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NEG_WIDTH_PROP) + width;
            throw new IllegalArgumentException(msg);
        }

        if(height <= 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NEG_HEIGHT_PROP) + height;
            throw new IllegalArgumentException(msg);
        }

        int pixel_size = getBytesPerPixel(format);

        if(pixel_size == -1)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(INVALID_FORMAT_PROP);
            throw new IllegalArgumentException(msg);
        }

        if(pixels == null)
        {
            pixelBuffer.clear();
            return;
        }

        glPixelFormat = getPixelFormat(format);

        pixelWidth = width;
        pixelHeight = height;
        pixelFormat = format;
        bytesPerPixel = pixel_size;

        pixelBuffer =
            ByteBuffer.allocateDirect(width * height * bytesPerPixel);
        pixelBuffer.put(pixels, 0, width * height * bytesPerPixel);
        pixelBuffer.rewind();
    }

    /**
     * Retrieve the vertices that are currently set. The array must be at
     * least as long as the valid vertex count, times 3. If none are set
     * currently or have been cleared, the provided array is untouched.
     *
     * @param bitmask The array to copy the image byte values into
     * @throws ArrayIndexOutOfBoundsException The provided array is too short
     */
    public void getBits(byte[] bitmask)
    {
        System.arraycopy(pixels,
                         0,
                         bitmask,
                         0,
                         pixelHeight * pixelWidth * bytesPerPixel);
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param ir The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(ImageRaster ir)
    {
        if(ir == null)
            return 1;

        if(ir == this)
            return 0;

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param ir The image instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(ImageRaster ir)
    {
        return (ir == this);
    }

    /**
     * Convenience method that looks at the user provided image format and
     * returns the number of bytes per pixel
     *
     * @return A value between 1 and 4 or -1 if an unknown format supplied
     */
    private int getBytesPerPixel(int format)
    {
        int ret_val = -1;

        switch(format)
        {
            case FORMAT_RGB:
                ret_val = 3;
                break;

            case FORMAT_RGBA:
                ret_val = 4;
                break;

            case FORMAT_BGR:
                ret_val = 3;
                break;

            case FORMAT_BGRA:
                ret_val = 4;
                break;

            case FORMAT_INTENSITY_ALPHA:
                ret_val = 2;
                break;

            case FORMAT_INTENSITY:
                ret_val = 1;
        }

        return ret_val;
    }

    /**
     * Convenience method that looks at the user provided image format and
     * returns the OpenGL format constant best suited.
     *
     * @return One of the GL_* constants
     */
    private int getPixelFormat(int format)
    {
        int ret_val = -1;

        switch(format)
        {
            case FORMAT_RGB:
                ret_val = GL.GL_RGB;
                break;

            case FORMAT_RGBA:
                ret_val = GL.GL_RGBA;
                break;

            case FORMAT_BGR:
                ret_val = GL2.GL_BGR;
                break;

            case FORMAT_BGRA:
                ret_val = GL.GL_BGRA;
                break;

            case FORMAT_INTENSITY_ALPHA:
                ret_val = GL.GL_LUMINANCE_ALPHA;
                break;

            case FORMAT_INTENSITY:
                ret_val = GL.GL_LUMINANCE;
        }

        return ret_val;
    }

}
