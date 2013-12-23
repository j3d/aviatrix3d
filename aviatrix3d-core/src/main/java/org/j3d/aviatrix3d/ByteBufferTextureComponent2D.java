/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2007
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

// Local imports
// None

/**
 * A Texture component that uses raw byte buffer data to be interpreted
 * by the format IDs passed in.
 *
 * @author Rex Melton
 * @version $Revision: 2.2 $
 */
public class ByteBufferTextureComponent2D extends TextureComponent2D
{
    /**
     * Constructs an image with default values.
     */
    public ByteBufferTextureComponent2D()
    {
        this(0, 0, 0, ByteBuffer.allocateDirect( 0 ));
    }

    /**
     * Constructs a TextureComponent2D using the specified format,
     * width, height and ByteBuffer image.
     *
     * @param format The image format.
     * @param width The width of the image
     * @param height The height of the image
     * @param src The image data
     */
    public ByteBufferTextureComponent2D(
        int format,
        int width,
        int height,
        ByteBuffer src )
    {
        super(1);

        this.width = width;
        this.height = height;
        this.format = format;

        data[0] = ( src == null ) ? ByteBuffer.allocateDirect( 0 ) : src;
    }

    /**
     * Constructs a TextureComponent2D using the specified format,
     * width, height and ByteBuffer images. Any images past the first
     * are assumed to be mipmap levels.
     *
     * @param format The image format.  RGB, RGBA currently
     * @param width The width of the image
     * @param height The height of the image
     * @param src The image data
     */
    public ByteBufferTextureComponent2D(
        int format,
        int width,
        int height,
        ByteBuffer[] src )
    {
        super(src.length);

        this.width = width;
        this.height = height;
        this.format = format;

        int numLevels = src.length;

        for(int i = 0; i < numLevels; i++)
        {
            data[i] = ( src[i] == null ) ? ByteBuffer.allocateDirect( 0 ) : src[i];
        }
    }

    //----------------------------------------------------------
    // Methods defined in TextureComponent
    //----------------------------------------------------------

    /**
     * Ignored. Clear local data stored in this node.  Only data needed for
     * OpenGL calls will be retained;
     */
    @Override
    public void clearLocalData( )
    {
    }

    /**
     * Return the ByteBuffer representation of the image.
     *
     * @param level The image level required
     * @return a ByteBuffer
     */
    @Override
    protected ByteBuffer convertImage(int level)
    {
        return data[level];
    }
}
