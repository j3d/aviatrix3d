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
import java.nio.ByteOrder;

// Local imports
// None

/**
 * A Texture component that uses raw byte buffer data to be interpreted
 * by the format IDs passed in.
 *
 * @author Rex Melton
 * @version $Revision: 2.3 $
 */
public class ByteBufferTextureComponent3D extends TextureComponent3D
{
    /** The image data */
    private ByteBuffer[] image_data;

    /**
     * Constructs a TextureComponent with default values.
     */
    public ByteBufferTextureComponent3D()
    {
        this(0, 0, 0, new ByteBuffer[0]);
    }

    /**
     * Constructs a TextureComponent3D using the specified format,
     * width, height and ByteBuffer images.
     *
     * @param format The image format.
     * @param width The width of the image
     * @param height The height of the image
     * @param src The image data
     */
    public ByteBufferTextureComponent3D(
        int format,
        int width,
        int height,
        ByteBuffer[] src )
    {
        super(1);

        this.width = width;
        this.height = height;
        this.depth = src.length;
        this.format = format;

        image_data = src;
    }

    //----------------------------------------------------------
    // Methods defined in TextureComponent
    //----------------------------------------------------------

    /**
     * Ignored. Clear local data stored in this node.  Only data needed for
     * OpenGL calls will be retained;
     */
    @Override
    public void clearLocalData()
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
        int num_comp = bytesPerPixel( );

        data[0] = ByteBuffer.allocateDirect(width * height * depth * num_comp);
        data[0].order(ByteOrder.nativeOrder());

        for(int i = 0; i < depth; i++)
        {
            image_data[i].rewind( );
            data[0].put( image_data[i] );
        }

        return data[0];
    }
}
