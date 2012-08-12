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
import java.awt.image.*;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;

// Local imports
// None

/**
 * A texture component that wraps a 3D image described as a collection of
 * bytes.
 * <p>
 *
 * <b>Byte array format</b><br/>
 * Images are presented in terms of bytes. Per pixel format is described by the
 * <code>format</code> parameter that can be found on the various method calls.
 * Bytes are laid down in a single contiguous array for 3D images in the format
 * of width, height, depth. That is, all the bytes for the first row of the
 * first image, then the second row of the first image, etc until the first
 * depth layer is complete (depth 0), then the second image starts etc.
 * Unless the video hardware supports non-power-of-2 textures, the width and
 * height must be powers of 2 and the depth must be an even number.
 *
 * @author Justin Couch
 * @version $Revision: 1.15 $
 */
public class ByteTextureComponent3D extends TextureComponent3D
{
    /** The image data [level][pixels] */
    private byte[][] pixels;

    /**
     * Constructs an image with default values.
     */
    public ByteTextureComponent3D()
    {
        this(0, 0, 0, 0, null);
    }

    /**
     * Constructs an image with default values.
     *
     * @param yUp Change the image aroud the Y axis if needed
     */
    public ByteTextureComponent3D(boolean yUp)
    {
        this(0, 0, 0, 0, null);

        invertY = !yUp;
    }

    /**
     * Constructs an 3D texture component using the specified format, width,
     * height and a single (BASE_LEVEL) mip-map image.
     *
     * @param format The image format.  RGB, RGBA currently
     * @param width The width of the image
     * @param height The height of the image
     * @param srcPixels The image data. See class header for format info.
     * @throws IllegalArgumentException Formats don't match
     */
    public ByteTextureComponent3D(int format,
                                   int width,
                                   int height,
                                   int depth,
                                   byte[] srcPixels)
    {
        super(1);

        this.width = width;
        this.height = height;
        this.format = format;
        this.depth = depth;

        int size = srcPixels == null ? 0 : srcPixels.length;
        pixels = new byte[1][size];

        System.arraycopy(srcPixels, 0, pixels[0], 0, size);
    }

    /**
     * Constructs an 3D texture component using the specified format, width,
     * height and one or more mip-map levels.
     *
     * @param format The image format.  RGB, RGBA currently
     * @param width The width of the image
     * @param height The height of the image
     * @param srcPixels The image data
     * @throws IllegalArgumentException Formats don't match
     */
    public ByteTextureComponent3D(int format,
                                   int width,
                                   int height,
                                   int depth,
                                   byte[][] srcPixels,
                                   int numLevels)
    {
        super(numLevels);

        this.width = width;
        this.height = height;
        this.format = format;
        this.depth = depth;

        pixels = new byte[numLevels][];

        for(int i = 0; i < numLevels; i++)
        {
            int size = srcPixels[i] == null ? 0 : srcPixels[i].length;
            pixels[i] = new byte[size];
            System.arraycopy(srcPixels[i], 0, pixels[i], 0, size);
        }
    }

    /**
     * Constructs an Image3D using the specified format, width, height and
     * rendered image.
     *
     * @param format The image format.  RGB, RGBA currently
     * @param width The width of the image
     * @param height The height of the image
     * @param srcPixels The image data
     * @param yUp Change the image aroud the Y axis if needed
     * @throws IllegalArgumentException Formats don't match
     */
    public ByteTextureComponent3D(int format,
                                   int width,
                                   int height,
                                   int depth,
                                   byte[][] srcPixels,
                                   int numLevels,
                                   boolean yUp)
    {
        this(format, width, height, depth, srcPixels, numLevels);

        invertY = !yUp;
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Update a sub-section of the image data with the new pixel values. Not
     * implemented yet.
     *
     * @param destX The starting X offset in the existing image space
     * @param destY The starting Y offset in the existing image space
     * @param destZ The starting Z offset in the existing image space
     * @param width The width of the section to replace
     * @param height The height of the section to replace
     * @param depth The height of the section to replace
     * @param level The mipmap level to update
     * @param img The image to take data from
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void updateSubImage(int destX,
                               int destY,
                               int destZ,
                               int width,
                               int height,
                               int depth,
                               int level,
                               byte[] img)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        sendTextureUpdate(destX, destY, destZ, width, height, depth, level, img);
    }

    /**
     * Get the current depth of the image component.
     *
     * @return A value >= 0
     */
    public int getDepth()
    {
        return depth;
    }

    /**
     * Clear local data stored in this node.  Only data needed for
     * OpenGL calls will be retained;
     */
    public void clearLocalData()
    {
// Not implemented yet.
    }

    /**
     * Convenience method to convert a buffered image into a NIO array of the
     * corresponding type. Images typically need to be swapped when doing this
     * by the Y axis is in the opposite direction to the one used by OpenGL.
     *
     * @param level Which image level needs to be converted
     * @return an appropriate array type - either IntBuffer or ByteBuffer
     */
    protected ByteBuffer convertImage(int level)
    {
        ByteBuffer ret_val = null;

        ret_val = ByteBuffer.allocateDirect(pixels[level].length);
        ret_val.order(ByteOrder.nativeOrder());
        ret_val.put(pixels[level]);

        return ret_val;
    }
}
