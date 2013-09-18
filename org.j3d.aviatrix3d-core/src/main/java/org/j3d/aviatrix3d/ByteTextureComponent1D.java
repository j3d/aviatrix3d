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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

// Local imports
// None

/**
 * A Texture component that uses raw byte data to be interpreted by the
 * format IDs passed in.
 *
 * @author Alan Hudson
 * @version $Revision: 1.13 $
 */
public class ByteTextureComponent1D extends TextureComponent1D
{
    /** The image data */
    private byte[][] pixels;

    /**
     * Constructs an image with default values.
     */
    public ByteTextureComponent1D()
    {
        this(0, 0, null);
    }

    /**
     * Constructs an image with default values.
     *
     * @param yUp Change the image aroud the Y axis if needed
     */
    public ByteTextureComponent1D(boolean yUp)
    {
        this(0, 0, null);

        invertY = !yUp;
    }

    /**
     * Constructs an Image1D using the specified format, width, height and
     * rendered image. If the srcImage[0] is an instance of BufferedImage,
     * the format passed is ignored and the image directly used.
     *
     * @param format The image format.  RGB, RGBA currently
     * @param width The width of the image
     * @param srcPixels The image data
     */
    public ByteTextureComponent1D(int format,
                                  int width,
                                  byte[] srcPixels)
    {
        super(1);

        this.width = width;
        this.format = format;

        int size = srcPixels == null ? 0 : srcPixels.length;
        pixels = new byte[1][size];

        System.arraycopy(srcPixels, 0, pixels[0], 0, size);
    }

    /**
     * Constructs an Image1D using the specified format, width, height and
     * rendered image. If the srcImage[0] is an instance of BufferedImage,
     * the format passed is ignored and the image directly used.
     *
     * @param format The image format.  RGB, RGBA currently
     * @param width The width of the image
     * @param srcPixels The image data
     * @param numLevels The number of mip-map levels to generate
     */
    public ByteTextureComponent1D(int format,
                                  int width,
                                  byte[][] srcPixels,
                                  int numLevels)
    {
        super(numLevels);

        this.width = width;
        this.format = format;

        pixels = new byte[numLevels][];

        for(int i = 0; i < numLevels; i++)
        {
            int size = srcPixels[i] == null ? 0 : srcPixels[i].length;
            pixels[i] = new byte[size];
            System.arraycopy(srcPixels[i], 0, pixels[i], 0, size);
        }
    }

    /**
     * Constructs an Image1D using the specified format, width, height and
     * rendered image. If the srcImage[0] is an instance of BufferedImage,
     * the format passed is ignored and the image directly used.
     *
     * @param format The image format.  RGB, RGBA currently
     * @param width The width of the image
     * @param srcPixels The image data
     * @param yUp Change the image aroud the Y axis if needed
     */
    public ByteTextureComponent1D(int format,
                                  int width,
                                  byte[] srcPixels,
                                  boolean yUp)
    {
        this(format, width, srcPixels);

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
     * @param width The width of the section to replace
     * @param level The mipmap level to update
     * @param img The image to take data from
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void updateSubImage(int destX,
                               int width,
                               int level,
                               byte[] img)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        sendTextureUpdate(destX, 0, 0, width, 1, 1, level, img);
    }

    /**
     * Clear local data stored in this node.  Only data needed for
     * OpenGL calls will be retained;
     */
    @Override
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
    @Override
    protected ByteBuffer convertImage(int level)
    {
        ByteBuffer ret_val = null;

        ret_val = ByteBuffer.allocateDirect(pixels[level].length);
        ret_val.order(ByteOrder.nativeOrder());
        ret_val.put(pixels[level]);

        return ret_val;
    }
}
