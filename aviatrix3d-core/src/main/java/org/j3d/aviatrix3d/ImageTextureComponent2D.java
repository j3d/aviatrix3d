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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.HashMap;

import org.j3d.util.I18nManager;

// Local imports
// None

/**
 * Wraps a 2D image and turns it into a texture source.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>renderedImageSptMsg: Error message when there was a user provided a
 *     RenderedImage as the data source.</li>
 * <li>unsupportedFormatMsg: User has given a source image format that we
 *     haven't created a transformation to yet</li>
 * </ul>
 *
 * @author Alan Hudson
 * @version $Revision: 1.26 $
 */
public class ImageTextureComponent2D extends TextureComponent2D
{
    /** Can't handle RenderedImage2D for an image source */
    private static final String RI_SOURCE_SUPPORT_PROP =
        "org.j3d.aviatrix3d.ImageTextureComponent2D.renderedImageSptMsg";

    /** Unsupported image type to convert to textures */
    private static final String UNSUPPORTED_TYPE_PROP =
        "org.j3d.aviatrix3d.ImageTextureComponent2D.unsupportedFormatMsg";

    /** Temp for fetching pixels from the input image during conversion */
    private int[] pixelTmp;

    /** The image data */
    private RenderedImage[] images;

    /**
     * Constructs an image with default values.
     */
    public ImageTextureComponent2D()
    {
        this(0, 0, 0, null);
    }

    /**
     * Constructs an image with default values.
     *
     * @param yUp Change the image aroud the Y axis if needed
     */
    public ImageTextureComponent2D(boolean yUp)
    {
        this(0, 0, 0, null);

        invertY = !yUp;
    }

    /**
     * Constructs an Image2D using the specified format, width, height and
     * rendered image. If the srcImage[0] is an instance of BufferedImage,
     * the format passed is ignored and the image directly used.
     *
     * @param format The image format.  RGB, RGBA currently
     * @param width The width of the image
     * @param height The height of the image
     * @param srcImage The image data
     */
    public ImageTextureComponent2D(int format,
                                   int width,
                                   int height,
                                   RenderedImage srcImage)
    {
        super(1);

        this.width = width;
        this.height = height;
        this.format = format;

        images = new RenderedImage[1];
        images[0] = srcImage;
    }

    /**
     * Constructs an Image2D using the specified format, width, height and
     * rendered image. If the srcImage[0] is an instance of BufferedImage,
     * the format passed is ignored and the image directly used.
     *
     * @param format The image format.  RGB, RGBA currently
     * @param width The width of the image
     * @param height The height of the image
     * @param srcImage The image data
     * @param yUp Change the image aroud the Y axis if needed
     */
    public ImageTextureComponent2D(int format,
                                   int width,
                                   int height,
                                   RenderedImage srcImage,
                                   boolean yUp)
    {
        this(format, width, height, srcImage);

        invertY = !yUp;
    }

    /**
     * Constructs an Image2D using the specified format and
     * rendered image.
     *
     * @param format The image format RGB, RGBA currently
     * @param srcImage The image data
     */
    public ImageTextureComponent2D(int format, RenderedImage srcImage)
    {
        super(1);

        BufferedImage bi;

        this.format = format;

        images = new RenderedImage[1];
        images[0] = srcImage;

        if(srcImage instanceof BufferedImage)
        {
            bi = (BufferedImage)srcImage;
            width = bi.getWidth(null);
            height = bi.getHeight(null);
        }
        else
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(RI_SOURCE_SUPPORT_PROP);
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Constructs an Image2D using the specified format and
     * an array of rendered images.  Any images past the first are assumed
     * to be mipmap levels.
     *
     * @param format The image format.  RGB, RGBA currently
     * @param srcImages The image data.  They must all be of the same type
     */
    public ImageTextureComponent2D(int format, RenderedImage[] srcImages)
    {
        super(srcImages.length);

        BufferedImage bi;

        this.format = format;

        images = new RenderedImage[numLevels];

        for(int i=0; i < numLevels; i++)
            images[i] = srcImages[i];

        // Get the width from the first one
        if(srcImages[0] instanceof BufferedImage)
        {
            bi = (BufferedImage)srcImages[0];
            width = bi.getWidth(null);
            height = bi.getHeight(null);
        }
        else
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(RI_SOURCE_SUPPORT_PROP);
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Constructs an Image2D using the specified format and
     * rendered image.
     *
     * @param format The image format.  RGB, RGBA currently
     * @param srcImage The image data
     * @param yUp Change the image aroud the Y axis if needed
     */
    public ImageTextureComponent2D(int format,
                                   RenderedImage srcImage,
                                   boolean yUp)
    {
        this(format, srcImage);

        invertY = !yUp;
    }

    /**
     * Constructs an Image2D using the specified format and
     * rendered images.
     *
     * @param format The image format.  RGB, RGBA currently
     * @param srcImages The image data
     * @param yUp Change the image aroud the Y axis if needed
     */
    public ImageTextureComponent2D(int format,
                                   RenderedImage[] srcImages,
                                   boolean yUp)
    {
        this(format, srcImages);

        invertY = !yUp;
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Update a sub-section of the image data with the new pixel values.
     *
     * @param srcX The starting X offset in the existing image space
     * @param srcY The starting Y offset in the existing image space
     * @param destX The starting X offset in the existing image space
     * @param destY The starting Y offset in the existing image space
     * @param width The width of the section to replace
     * @param height The height of the section to replace
     * @param img The image to take data from
     * @param level The mipmap level to update
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void updateSubImage(int srcX,
                               int srcY,
                               int destX,
                               int destY,
                               int width,
                               int height,
                               int level,
                               RenderedImage img)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(img instanceof BufferedImage)
            convertForSubImage((BufferedImage)img, srcX, srcY, width, height);

        sendTextureUpdate(destX,
                          destY,
                          0,
                          width,
                          height,
                          1,
                          level,
                          copyBuffer);
    }

    /**
     * Clear local data stored in this node.  Only data needed for
     * OpenGL calls will be retained;
     */
    public void clearLocalData()
    {
        int len = images.length;

        for(int i=0; i < len; i++)
        {
            // Insure all openGL structures are created
            if(data[i] == null)
                data[i] = convertImage(i);

            if(images[i] instanceof BufferedImage)
                ((BufferedImage)images[i]).flush();
        }

        images = null;
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
        return convertImage((BufferedImage)images[level]);
    }

    /**
     * Convenience method to convert a buffered image into a NIO array of the
     * corresponding type. Images typically need to be swapped when doing this
     * by the Y axis is in the opposite direction to the one used by OpenGL.
     *
     * @param img The source image to convert
     * @return an appropriate array type - either IntBuffer or ByteBuffer
     */
    private ByteBuffer convertImage(BufferedImage img)
    {
        ByteBuffer ret_val = null;

        int height = img.getHeight(null);
        int width = img.getWidth(null);
        ColorModel cm = img.getColorModel();

        if((pixelTmp == null) || (pixelTmp.length < width))
            pixelTmp = new int[width];

        // OpenGL only likes RGBA, not ARGB. Flip the order where necessary.
        // Also, some cards don't like dealing with BGR textures, so
        // automatically flip the bytes around the RGB for those that have it
        // reversed.
        switch(img.getType())
        {
            // All these types are 4byte per pixels. Use the getRGB method of
            // BufferedImage to deal with the fetch and auto colour conversion.
            // That call will automaticall swap the bytes around, so we just
            // need send the values to the ByteBuffer in the appropriate order
            case BufferedImage.TYPE_3BYTE_BGR:
            case BufferedImage.TYPE_INT_RGB:
            case BufferedImage.TYPE_USHORT_555_RGB:
            case BufferedImage.TYPE_USHORT_565_RGB:
                format = FORMAT_RGB;

                ret_val = ByteBuffer.allocateDirect(width * height * 3);
                ret_val.order(ByteOrder.nativeOrder());

                if(invertY)
                {
                    int y = height - 1;

                    for(int i = 0; i < height; i++)
                    {
                        img.getRGB(0, y, width, 1, pixelTmp, 0, width);

                        for(int j = 0; j < width; j++)
                        {
                            int tmp = pixelTmp[j];

                            ret_val.put((byte)((tmp >> 16) & 0xFF));
                            ret_val.put((byte)((tmp >> 8) & 0xFF));
                            ret_val.put((byte)(tmp & 0xFF));
                        }
                        y--;
                    }
                }
                else
                {
                    for(int i = 0; i < height; i++)
                    {
                        img.getRGB(0, i, width, 1, pixelTmp, 0, width);

                        for(int j = 0; j < width; j++)
                        {
                            int tmp = pixelTmp[j];

                            ret_val.put((byte)((tmp >> 16) & 0xFF));
                            ret_val.put((byte)((tmp >> 8) & 0xFF));
                            ret_val.put((byte)(tmp & 0xFF));
                        }
                    }
                }
                break;

            case BufferedImage.TYPE_4BYTE_ABGR:
            case BufferedImage.TYPE_INT_ARGB:
                // OpenGL wants RGBA, so swap the byte order around as part of
                // the conversion process.

                format = FORMAT_RGBA;

                ret_val = ByteBuffer.allocateDirect(width * height * 4);
                ret_val.order(ByteOrder.nativeOrder());

                if(invertY)
                {
                    int y = height - 1;

                    for(int i = 0; i < height; i++)
                    {
                        img.getRGB(0, y, width, 1, pixelTmp, 0, width);

                        for(int j = 0; j < width; j++)
                        {
                            int tmp = pixelTmp[j];
                            ret_val.put((byte)((tmp >> 16) & 0xFF));
                            ret_val.put((byte)((tmp >> 8) & 0xFF));
                            ret_val.put((byte)(tmp & 0xFF));
                            ret_val.put((byte)((tmp >> 24) & 0xFF));
                        }

                        y--;
                    }
                }
                else
                {
                    for(int i = 0; i < height; i++)
                    {
                        img.getRGB(0, i, width, 1, pixelTmp, 0, width);

                        for(int j = 0; j < width; j++)
                        {
                            int tmp = pixelTmp[j];
                            ret_val.put((byte)((tmp >> 16) & 0xFF));
                            ret_val.put((byte)((tmp >> 8) & 0xFF));
                            ret_val.put((byte)(tmp & 0xFF));
                            ret_val.put((byte)((tmp >> 24) & 0xFF));
                        }
                    }
                }
                break;

            case BufferedImage.TYPE_BYTE_INDEXED:
            case BufferedImage.TYPE_CUSTOM:
            case BufferedImage.TYPE_BYTE_BINARY:
                // Force the format change.
                int num_comp = cm.getNumComponents();
                boolean has_alpha = cm.hasAlpha();

                switch(num_comp)
                {
                    case 1:
                        // format = FORMAT_RGB;
                        // Leave this as the user selected format for either
                        // alpha or gray maps
                        ret_val = ByteBuffer.allocateDirect(width * height);
                        ret_val.order(ByteOrder.nativeOrder());

                        if(invertY)
                        {
                            int y = height - 1;

                            for(int i = 0; i < height; i++)
                            {
                                img.getRGB(0, y, width, 1, pixelTmp, 0, width);

                                for(int j = 0; j < width; j++)
                                {
                                    int tmp = pixelTmp[j];

                                    ret_val.put((byte)(tmp & 0x000000FF));
                                }

                                y--;
                            }
                        }
                        else
                        {
                            for(int i = 0; i < height; i++)
                            {
                                img.getRGB(0, i, width, 1, pixelTmp, 0, width);
                                for(int j = 0; j < width; j++)
                                {
                                    int tmp = pixelTmp[j];

                                    ret_val.put((byte)(tmp & 0x000000FF));
                                }
                            }
                        }
                        break;

                    case 2:
                        format = FORMAT_INTENSITY_ALPHA;
                        ret_val = ByteBuffer.allocateDirect(width * height * 2);
                        ret_val.order(ByteOrder.nativeOrder());

                        if(invertY)
                        {
                            int y = height - 1;

                            for(int i = 0; i < height; i++)
                            {
                                img.getRGB(0, y, width, 1, pixelTmp, 0, width);

                                for(int j = 0; j < width; j++)
                                {
                                    int tmp = pixelTmp[j];

                                    ret_val.put((byte)((tmp >> 8) & 0xFF));
                                    ret_val.put((byte)(tmp & 0xFF));
                                }

                                y--;
                            }
                        }
                        else
                        {
                            for(int i = 0; i < height; i++)
                            {
                                img.getRGB(0, i, width, 1, pixelTmp, 0, width);
                                for(int j = 0; j < width; j++)
                                {
                                    int tmp = pixelTmp[j];

                                    ret_val.put((byte)((tmp >> 8) & 0xFF));
                                    ret_val.put((byte)(tmp & 0xFF));
                                }
                            }
                        }
                        break;

                    case 3:
                        format = FORMAT_RGB;
                        ret_val = ByteBuffer.allocateDirect(width * height * 3);
                        ret_val.order(ByteOrder.nativeOrder());

                        if(invertY)
                        {
                            int y = height - 1;

                            for(int i = 0; i < height; i++)
                            {
                                img.getRGB(0, y, width, 1, pixelTmp, 0, width);

                                for(int j = 0; j < width; j++)
                                {
                                    int tmp = pixelTmp[j];

                                    byte r = (byte)((tmp >> 16) & 0xFF);
                                    byte g = (byte)((tmp >> 8) & 0xFF);
                                    byte b = (byte)(tmp & 0xFF);

                                    ret_val.put(r);
                                    ret_val.put(g);
                                    ret_val.put(b);
                                }

                                y--;
                            }
                        }
                        else
                        {
                            for(int i = 0; i < height; i++)
                            {
                                img.getRGB(0, i, width, 1, pixelTmp, 0, width);
                                for(int j = 0; j < width; j++)
                                {
                                    int tmp = pixelTmp[j];

                                    ret_val.put((byte)((tmp >> 16) & 0xFF));
                                    ret_val.put((byte)((tmp >> 8) & 0xFF));
                                    ret_val.put((byte)(tmp & 0xFF));
                                }
                            }
                        }
                        break;

                    case 4:
                        format = FORMAT_RGBA;
                        ret_val = ByteBuffer.allocateDirect(width * height * 4);
                        ret_val.order(ByteOrder.nativeOrder());

                        if(invertY)
                        {
                            int y = height - 1;

                            for(int i = 0; i < height; i++)
                            {
                                img.getRGB(0, y, width, 1, pixelTmp, 0, width);

                                for(int j = 0; j < width; j++)
                                {
                                    int tmp = pixelTmp[j];

                                    ret_val.put((byte)((tmp >> 16) & 0xFF));
                                    ret_val.put((byte)((tmp >> 8) & 0xFF));
                                    ret_val.put((byte)(tmp & 0xFF));
                                    ret_val.put((byte)((tmp >> 24) & 0xFF));
                                }

                                y--;
                            }
                        }
                        else
                        {
                            for(int i = 0; i < height; i++)
                            {
                                img.getRGB(0, i, width, 1, pixelTmp, 0, width);

                                for(int j = 0; j < width; j++)
                                {
                                    int tmp = pixelTmp[j];

                                    ret_val.put((byte)((tmp >> 16) & 0xFF));
                                    ret_val.put((byte)((tmp >> 8) & 0xFF));
                                    ret_val.put((byte)(tmp & 0xFF));
                                    ret_val.put((byte)((tmp >> 24) & 0xFF));

                                }
                            }
                        }

                        break;
                }
                break;

            case BufferedImage.TYPE_BYTE_GRAY:
                java.awt.image.Raster data = img.getData();
                DataBuffer db = data.getDataBuffer();

                // The color conversion is doing weird things here, try using the
                // the buffer directly if we can
                if (db instanceof DataBufferByte)
                {
                    byte[] vals = ((DataBufferByte)db).getData();
                    ret_val = ByteBuffer.allocateDirect(width * height);
                    ret_val.order(ByteOrder.nativeOrder());

                    if (invertY)
                    {
                        int y = height - 1;
                        for(int i = 0; i < height; i++)
                        {
                            for(int j = 0; j < width; j++)
                            {
                                ret_val.put(vals[y * width + j]);
                            }

                            y--;
                        }
                    }
                    else
                    {
                        ret_val.put(vals);
                    }
                    return ret_val;
                }

                // format = FORMAT_RGB;
                // Leave this as the user selected format for either
                // alpha or gray maps
                ret_val = ByteBuffer.allocateDirect(width * height);
                ret_val.order(ByteOrder.nativeOrder());

                if(invertY)
                {
                    int y = height - 1;

                    for(int i = 0; i < height; i++)
                    {
                        img.getRGB(0, y, width, 1, pixelTmp, 0, width);

                        for(int j = 0; j < width; j++)
                        {
                            int tmp = pixelTmp[j];

                            ret_val.put((byte)(tmp & 0x000000FF));
                        }

                        y--;
                    }
                }
                else
                {
                    for(int i = 0; i < height; i++)
                    {
                        img.getRGB(0, i, width, 1, pixelTmp, 0, width);
                        for(int j = 0; j < width; j++)
                        {
                            int tmp = pixelTmp[j];

                            ret_val.put((byte)(tmp & 0x000000FF));
                        }
                    }
                }
                break;

            case BufferedImage.TYPE_USHORT_GRAY:
            case BufferedImage.TYPE_INT_ARGB_PRE:

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNSUPPORTED_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

                Object[] msg_args = { new Integer(img.getType()) };
                Format[] fmts = { n_fmt };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                msg_fmt.setFormats(fmts);
                String msg = msg_fmt.format(msg_args);
        }

        ret_val.rewind();

        return ret_val;
    }

    /**
     * Alternate method to convert a buffered image into a byte[] used for the
     * sub-image updates. Images typically need to be swapped when doing this
     * by the Y axis is in the opposite direction to the one used by OpenGL.
     * The results will be placed in copyBuffer.
     *
     * @param img The source image to convert
     * @param startX The starting X position of the image
     * @param startY The starting Y position of the image
     * @param width The width of the section to replace
     * @param height The height of the section to replace
     * @return an appropriate array type - either IntBuffer or ByteBuffer
     */
    private void convertForSubImage(BufferedImage img,
                                    int startX,
                                    int startY,
                                    int width,
                                    int height)
    {
        int img_height = img.getHeight(null);
        ColorModel cm = img.getColorModel();
        int pos = 0;

        if((pixelTmp == null) || (pixelTmp.length < width))
            pixelTmp = new int[width];

        // OpenGL only likes RGBA, not ARGB. Flip the order where necessary.
        // Also, some cards don't like dealing with BGR textures, so
        // automatically flip the bytes around the RGB for those that have it
        // reversed.
        switch(img.getType())
        {
            // All these types are 4byte per pixels. Use the getRGB method of
            // BufferedImage to deal with the fetch and auto colour conversion.
            // That call will automaticall swap the bytes around, so we just
            // need send the values to the ByteBuffer in the appropriate order
            case BufferedImage.TYPE_3BYTE_BGR:
            case BufferedImage.TYPE_INT_RGB:
            case BufferedImage.TYPE_USHORT_555_RGB:
            case BufferedImage.TYPE_USHORT_565_RGB:
                format = FORMAT_RGB;

                checkCopyBufferSize(width * height * 3);

                if(invertY)
                {
                    int y = img_height - 1 - startY;

                    for(int i = 0; i < height; i++)
                    {
                        img.getRGB(startX, y, width, 1, pixelTmp, 0, width);

                        for(int j = 0; j < width; j++)
                        {
                            int tmp = pixelTmp[j];

                            copyBuffer[pos++] = (byte)((tmp >> 16) & 0xFF);
                            copyBuffer[pos++] = (byte)((tmp >> 8) & 0xFF);
                            copyBuffer[pos++] = (byte)(tmp & 0xFF);
                        }
                        y--;
                    }
                }
                else
                {
                    for(int i = startY; i < startY + height; i++)
                    {
                        img.getRGB(startX, i, width, 1, pixelTmp, 0, width);

                        for(int j = 0; j < width; j++)
                        {
                            int tmp = pixelTmp[j];

                            copyBuffer[pos++] = (byte)((tmp >> 16) & 0xFF);
                            copyBuffer[pos++] = (byte)((tmp >> 8) & 0xFF);
                            copyBuffer[pos++] = (byte)(tmp & 0xFF);
                        }
                    }
                }
                break;

            case BufferedImage.TYPE_4BYTE_ABGR:
            case BufferedImage.TYPE_INT_ARGB:
                // OpenGL wants RGBA, so swap the byte order around as part of
                // the conversion process.

                format = FORMAT_RGBA;

                checkCopyBufferSize(width * height * 4);

                if(invertY)
                {
                    int y = img_height - 1 - startY;

                    for(int i = 0; i < height; i++)
                    {
                        img.getRGB(startX, y, width, 1, pixelTmp, 0, width);

                        for(int j = 0; j < width; j++)
                        {
                            int tmp = pixelTmp[j];
                            copyBuffer[pos++] = (byte)((tmp >> 16) & 0xFF);
                            copyBuffer[pos++] = (byte)((tmp >> 8) & 0xFF);
                            copyBuffer[pos++] = (byte)(tmp & 0xFF);
                            copyBuffer[pos++] = (byte)((tmp >> 24) & 0xFF);
                        }

                        y--;
                    }
                }
                else
                {
                    for(int i = startY; i < startY + height; i++)
                    {
                        img.getRGB(startX, i, width, 1, pixelTmp, 0, width);

                        for(int j = 0; j < width; j++)
                        {
                            int tmp = pixelTmp[j];
                            copyBuffer[pos++] = (byte)((tmp >> 16) & 0xFF);
                            copyBuffer[pos++] = (byte)((tmp >> 8) & 0xFF);
                            copyBuffer[pos++] = (byte)(tmp & 0xFF);
                            copyBuffer[pos++] = (byte)((tmp >> 24) & 0xFF);
                        }
                    }
                }
                break;

            case BufferedImage.TYPE_BYTE_INDEXED:
            case BufferedImage.TYPE_BYTE_BINARY:
            case BufferedImage.TYPE_CUSTOM:
                // Force the format change.
                int num_comp = cm.getNumComponents();
                boolean has_alpha = cm.hasAlpha();

                switch(num_comp)
                {
                    case 1:
                        // format = FORMAT_RGB;
                        // Leave this as the user selected format for either
                        // alpha or gray maps
                        checkCopyBufferSize(width * height);

                        if(invertY)
                        {
                            int y = img_height - 1 - startY;

                            for(int i = 0; i < height; i++)
                            {
                                img.getRGB(startX, y, width, 1, pixelTmp, 0, width);

                                for(int j = 0; j < width; j++)
                                {
                                    int tmp = pixelTmp[j];

                                    copyBuffer[pos++] = (byte)(tmp & 0x000000FF);
                                }

                                y--;
                            }
                        }
                        else
                        {
                            for(int i = startY; i < startY + height; i++)
                            {
                                img.getRGB(startX, i, width, 1, pixelTmp, 0, width);
                                for(int j = 0; j < width; j++)
                                {
                                    int tmp = pixelTmp[j];

                                    copyBuffer[pos++] = (byte)(tmp & 0x000000FF);
                                }
                            }
                        }
                        break;

                    case 2:
                        format = FORMAT_INTENSITY_ALPHA;
                        checkCopyBufferSize(width * height * 2);

                        if(invertY)
                        {
                            int y = img_height - 1 - startY;

                            for(int i = 0; i < height; i++)
                            {
                                img.getRGB(startX, y, width, 1, pixelTmp, 0, width);

                                for(int j = 0; j < width; j++)
                                {
                                    int tmp = pixelTmp[j];

                                    copyBuffer[pos++] = (byte)((tmp >> 8) & 0xFF);
                                    copyBuffer[pos++] = (byte)(tmp & 0xFF);
                                }

                                y--;
                            }
                        }
                        else
                        {
                            for(int i = startY; i < startY + height; i++)
                            {
                                img.getRGB(startX, i, width, 1, pixelTmp, 0, width);
                                for(int j = 0; j < width; j++)
                                {
                                    int tmp = pixelTmp[j];

                                    copyBuffer[pos++] = (byte)((tmp >> 8) & 0xFF);
                                    copyBuffer[pos++] = (byte)(tmp & 0xFF);
                                }
                            }
                        }
                        break;

                    case 3:
                        format = FORMAT_RGB;
                        checkCopyBufferSize(width * height * 3);

                        if(invertY)
                        {
                            int y = img_height - 1 - startY;

                            for(int i = 0; i < height; i++)
                            {
                                img.getRGB(startX, y, width, 1, pixelTmp, 0, width);

                                for(int j = 0; j < width; j++)
                                {
                                    int tmp = pixelTmp[j];

                                    byte r = (byte)((tmp >> 16) & 0xFF);
                                    byte g = (byte)((tmp >> 8) & 0xFF);
                                    byte b = (byte)(tmp & 0xFF);

                                    copyBuffer[pos++] = r;
                                    copyBuffer[pos++] = g;
                                    copyBuffer[pos++] = b;
                                }

                                y--;
                            }
                        }
                        else
                        {
                            for(int i = startY; i < startY + height; i++)
                            {
                                img.getRGB(startX, i, width, 1, pixelTmp, 0, width);
                                for(int j = 0; j < width; j++)
                                {
                                    int tmp = pixelTmp[j];

                                    copyBuffer[pos++] = (byte)((tmp >> 16) & 0xFF);
                                    copyBuffer[pos++] = (byte)((tmp >> 8) & 0xFF);
                                    copyBuffer[pos++] = (byte)(tmp & 0xFF);
                                }
                            }
                        }
                        break;

                    case 4:
                        format = FORMAT_RGBA;
                        checkCopyBufferSize(width * height * 4);

                        if(invertY)
                        {
                            int y = img_height - 1 - startY;

                            for(int i = 0; i < height; i++)
                            {
                                img.getRGB(startX, y, width, 1, pixelTmp, 0, width);

                                for(int j = 0; j < width; j++)
                                {
                                    int tmp = pixelTmp[j];

                                    copyBuffer[pos++] = (byte)((tmp >> 16) & 0xFF);
                                    copyBuffer[pos++] = (byte)((tmp >> 8) & 0xFF);
                                    copyBuffer[pos++] = (byte)(tmp & 0xFF);
                                    copyBuffer[pos++] = (byte)((tmp >> 24) & 0xFF);
                                }

                                y--;
                            }
                        }
                        else
                        {
                            for(int i = startY; i < startY + height; i++)
                            {
                                img.getRGB(startX, i, width, 1, pixelTmp, 0, width);

                                for(int j = 0; j < width; j++)
                                {
                                    int tmp = pixelTmp[j];

                                    copyBuffer[pos++] = (byte)((tmp >> 16) & 0xFF);
                                    copyBuffer[pos++] = (byte)((tmp >> 8) & 0xFF);
                                    copyBuffer[pos++] = (byte)(tmp & 0xFF);
                                    copyBuffer[pos++] = (byte)((tmp >> 24) & 0xFF);

                                }
                            }
                        }

                        break;
                }
                break;

            case BufferedImage.TYPE_BYTE_GRAY:
                // format = FORMAT_RGB;
                // Leave this as the user selected format for either
                // alpha or gray maps
                checkCopyBufferSize(width * height);

                if(invertY)
                {
                    int y = img_height - 1 - startY;

                    for(int i = 0; i < height; i++)
                    {
                        img.getRGB(startX, y, width, 1, pixelTmp, 0, width);

                        for(int j = 0; j < width; j++)
                        {
                            int tmp = pixelTmp[j];

                            copyBuffer[pos++] = (byte)(tmp & 0x000000FF);
                        }

                        y--;
                    }
                }
                else
                {
                    for(int i = startY; i < startY + height; i++)
                    {
                        img.getRGB(startX, i, width, 1, pixelTmp, 0, width);
                        for(int j = 0; j < width; j++)
                        {
                            int tmp = pixelTmp[j];

                            copyBuffer[pos++] = (byte)(tmp & 0x000000FF);
                        }
                    }
                }
                break;

            case BufferedImage.TYPE_USHORT_GRAY:
            case BufferedImage.TYPE_INT_ARGB_PRE:

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNSUPPORTED_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

                Object[] msg_args = { new Integer(img.getType()) };
                Format[] fmts = { n_fmt };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                msg_fmt.setFormats(fmts);
                String msg = msg_fmt.format(msg_args);
        }
    }
}
