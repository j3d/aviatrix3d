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

import com.jogamp.opengl.GL2;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.picking.*;

/**
 * Raster object that represents a single drawn object using individual bits.
 * <p>
 *
 * Bitmaps describe what should be seen on screen, but not the colour. Colour
 * information is derived from the Pixmap's appearance information. A bitmap
 * also has a local offset field. A user may set explicit bounds for this
 * object, but in doing so may or may not truncate the bits themselves.
 * <p>
 *
 * When providing byte data, each bit represents the state of a single pixel
 * as either on or off. The bitmap width does not need to be a power of 2 or
 * eight to handle this. An image that is 2 pixels high and 10 pixels wide
 * would be represented by 4 bytes. The first byte is the first 8 bits of the
 * input for the first row. The second byte uses the first 2 bits for the
 * remaining part of the first row. The other 6 bits are ignored. The third byte
 * starts the next row, and finally the first 2 bits of the the fourth byte
 * provide the last two pixels of the second row.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>negWidthMsg: Error message when the stipple array is < 1024.</li>
 * <li>negHeightMsg: Error message when the cull type requested is invalid.</li>
 * <li>bitArraySizeMsg: Error message when the draw type requested is invalid.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 2.6 $
 */
public class BitmapRaster extends Raster
{
    /** Message when the width is less than or equal to zero */
    private static final String NEG_WIDTH_PROP =
        "org.j3d.aviatrix3d.BitmapRaster.negWidthMsg";

    /** Message when the height is less than or equal to zero */
    private static final String NEG_HEIGHT_PROP =
        "org.j3d.aviatrix3d.BitmapRaster.negHeightMsg";

    /** Error message when the provided bit array is too small */
    private static final String BIT_ARRAY_SIZE_PROP =
        "org.j3d.aviatrix3d.BitmapRaster.bitArraySizeMsg";

    /** Buffer for bitmap pixel data */
    private ByteBuffer pixelBuffer;

    /** The origin coordinate to use with the bitmap */
    private float[] origin;

    /** The width of the bitmap in pixels */
    private int pixelWidth;

    /** The height of the bitmap in pixels */
    private int pixelHeight;

    /** The number of bytes needed for the width */
    private int byteWidth;

    /**
     * Create a new empty raster with the given width and height.
     *
     * @param width The width of the raster in pixels
     * @param height The height of the raster in pixels
     * @throws IllegalArgumentException The width or height are not positive
     */
    public BitmapRaster(int width, int height)
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

        origin = new float[2];
        pixelWidth = width;
        pixelHeight = height;

        byteWidth = width / 8;

        if((width % 8) != 0)
            byteWidth++;

        pixelBuffer = ByteBuffer.allocateDirect(pixelHeight * byteWidth);
    }

    //---------------------------------------------------------------
    // Methods defined by GeometryRenderable
    //---------------------------------------------------------------

    @Override
    public void render(GL2 gl)
    {
        gl.glBitmap(pixelWidth,
                    pixelHeight,
                    origin[0],
                    origin[1],
                    0,
                    0,
                    pixelBuffer);
    }

    //---------------------------------------------------------------
    // Methods defined by LeafPickTarget
    //---------------------------------------------------------------

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

    @Override
    protected void recomputeBounds()
    {
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

    @Override
    public int compareTo(Object o)
        throws ClassCastException
    {
        BitmapRaster app = (BitmapRaster)o;
        return compareTo(app);
    }

    //---------------------------------------------------------------
    // Methods defined by Object
    //---------------------------------------------------------------

    @Override
    public boolean equals(Object o)
    {
        return o instanceof BitmapRaster && equals((BitmapRaster) o);
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
     * @param bitmask The bits to use for the raster or null to clear
     * @throws IllegalArgumentException The number of bytes is not sufficient to
     *   fulfill the previously set width and height of the bitmap
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data callback method
     */
    public void setBits(byte[] bitmask)
        throws InvalidWriteTimingException, IllegalArgumentException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(bitmask == null)
        {
            pixelBuffer.clear();
            return;
        }

        if(bitmask.length < pixelHeight * byteWidth)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(BIT_ARRAY_SIZE_PROP);
            throw new IllegalArgumentException(msg);
        }

        pixelBuffer.put(bitmask, 0, pixelHeight * byteWidth);
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
     * @param bitmask The bits to use for the raster or null to clear
     * @param width The width of the raster in pixels
     * @param height The height of the raster in pixels
     * @throws IllegalArgumentException The width or height are not positive
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds callback method
     */
    public void setBits(byte[] bitmask, int width, int height)
        throws InvalidWriteTimingException, IllegalArgumentException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

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


        if(bitmask == null)
        {
            pixelBuffer.clear();
            return;
        }

        int b_width = width / 8;

        if((width % 8) != 0)
            b_width++;

        if(bitmask.length < height * b_width)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(BIT_ARRAY_SIZE_PROP);
            throw new IllegalArgumentException(msg);
        }

        byteWidth = b_width;
        pixelWidth = width;
        pixelHeight = height;

        pixelBuffer = ByteBuffer.allocateDirect(pixelHeight * byteWidth);
        pixelBuffer.put(bitmask, 0, pixelHeight * byteWidth);
        pixelBuffer.rewind();
    }

    /**
     * Retrieve the vertices that are currently set. The array must be at
     * least as long as the valid vertex count, times 3. If none are set
     * currently or have been cleared, the provided array is untouched.
     *
     * @param bitmask The array to copy the bit mask values into
     * @throws ArrayIndexOutOfBoundsException The provided array is too short
     */
    public void getBits(byte[] bitmask)
    {
        pixelBuffer.get(bitmask, 0, pixelHeight * byteWidth);
    }

    /**
     * Set the origin offset of the bitmap to be used when rendering. The
     * origin allows an offset to be defined relative to the raster position.
     * Raster position is generated from the parent pixel transforms, and this
     * allows for bitmaps to be offset from that.
     *
     * @param x The X coordinate of the origin
     * @param y The Y coordinate of the origin
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds callback method
     */
    public void setOrigin(float x, float y)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        origin[0] = x;
        origin[1] = y;
    }

    /**
     * Get the current origin of the bitmap. The default origin is (0,0).
     *
     * @param origin An array of length 2 to copy the values into
     */
    public void getOrigin(float[] origin)
    {
        origin[0] = this.origin[0];
        origin[1] = this.origin[1];
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param br The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(BitmapRaster br)
    {
        if(br == null)
            return 1;

        if(br == this)
            return 0;

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param br The shape instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(BitmapRaster br)
    {
        return (br == this);
    }
}
