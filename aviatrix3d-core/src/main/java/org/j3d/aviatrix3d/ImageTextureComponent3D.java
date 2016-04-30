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
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.HashMap;

import org.j3d.util.I18nManager;

// Local imports
// None

/**
 * A texture component that wraps a 3D image.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>mixedImageFormatsMsg: Error message when the user provides a set of
 *     images with varying formats</li>
 * <li>renderedImageSptMsg: Error message when there was a user provided a
 *     RenderedImage as the data source.</li>
 * <li>unsupportedFormatMsg: User has given a source image format that we
 *     haven't created a transformation to yet</li>
 * </ul>
 *
 * @author Alan Hudson
 * @version $Revision: 1.20 $
 */
public class ImageTextureComponent3D extends TextureComponent3D
{
    /** Message when attempting to pick the object at the wrong time */
    private static final String MATCH_FORMAT_PROP =
        "org.j3d.aviatrix3d.ImageTextureComponent3D.mixedImageFormatsMsg";

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
    public ImageTextureComponent3D()
    {
        this(0, 0, 0, 0, null);
    }

    /**
     * Constructs an image with default values.
     *
     * @param yUp Change the image aroud the Y axis if needed
     */
    public ImageTextureComponent3D(boolean yUp)
    {
        this(0, 0, 0, 0, null);

        invertY = !yUp;
    }

    /**
     * Constructs an Image3D using the specified format, width, height and
     * rendered image.
     *
     * @param format The image format.  RGB, RGBA currently
     * @param width The width of the image
     * @param height The height of the image
     * @param images The image data
     * @throws IllegalArgumentException Formats don't match
     */
    public ImageTextureComponent3D(int format,
                                   int width,
                                   int height,
                                   int depth,
                                   RenderedImage[] images)
    {
        super(1);

        this.images = images;
        this.width = width;
        this.height = height;
        this.format = format;
        this.depth = depth;

        checkFormats();
    }

    /**
     * Constructs an Image3D using the specified format, width, height and
     * rendered image.
     *
     * @param format The image format.  RGB, RGBA currently
     * @param width The width of the image
     * @param height The height of the image
     * @param images The image data
     * @param yUp Change the image aroud the Y axis if needed
     * @throws IllegalArgumentException Formats don't match
     */
    public ImageTextureComponent3D(int format,
                                   int width,
                                   int height,
                                   int depth,
                                   RenderedImage[] images,
                                   boolean yUp)
    {
        this(format, width, height, depth, images);

        invertY = !yUp;
    }

    /**
     * Constructs an Image3D using the specified format and
     * rendered image.
     *
     * @param format The image format.  RGB, RGBA currently
     * @param images The image data
     * @throws IllegalArgumentException Formats don't match
     */
    public ImageTextureComponent3D(int format, RenderedImage[] images)
    {
        super(1);

        this.format = format;
        this.images = images;

        checkFormats();

        if(images[0] instanceof BufferedImage)
        {
            BufferedImage bi = (BufferedImage)images[0];
            width = images[0].getWidth();
            height = images[0].getHeight();
            depth = images.length;
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
     * @param images The image data
     * @param yUp Change the image aroud the Y axis if needed
     * @throws IllegalArgumentException Formats don't match
     */
    public ImageTextureComponent3D(int format,
                                   RenderedImage[] images,
                                   boolean yUp)
    {
        this(format, images);

        invertY = !yUp;
    }

    //----------------------------------------------------------
    // Methods defined by TextureComponent
    //----------------------------------------------------------

    @Override
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

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Update a sub-section of the image data with the new pixel values. Since
     * an image is a 2D object, we can only replace one piece of the depth at
     * a time. Updating multiple pieces of the image should use the alternate
     * form of this method.
     *
     * @param srcX The starting X offset in the existing image space
     * @param srcY The starting Y offset in the existing image space
     * @param destX The starting X offset in the existing image space
     * @param destY The starting Y offset in the existing image space
     * @param destZ The starting Z offset in the existing image space
     * @param width The width of the section to replace
     * @param height The height of the section to replace
     * @param level The mipmap level to update
     * @param img The image to take data from
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void updateSubImage(int srcX,
                               int srcY,
                               int destX,
                               int destY,
                               int destZ,
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
        {
            BufferedImage bi = (BufferedImage)img;
            convertForSubImage(bi, srcX, srcY, width, height, 1, 0);

            sendTextureUpdate(destX,
                              destY,
                              destZ,
                              width,
                              height,
                              1,
                              level,
                              copyBuffer);
        }
    }

    /**
     * Update a sub-section of the image data with the new pixel values. The
     * depth should be no greater than the number of images in the provided
     * source array.
     *
     * @param srcX The starting X offset in the existing image space
     * @param srcY The starting Y offset in the existing image space
     * @param destX The starting X offset in the existing image space
     * @param destY The starting Y offset in the existing image space
     * @param width The width of the section to replace
     * @param height The height of the section to replace
     * @param depth The height of the section to replace
     * @param level The mipmap level to update
     * @param img The image to take data from
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void updateSubImage(int srcX,
                               int srcY,
                               int destX,
                               int destY,
                               int destZ,
                               int width,
                               int height,
                               int depth,
                               int level,
                               RenderedImage[] img)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        int offset = 0;
        for(int i = 0; i < depth; i++)
        {
            if(img[i] instanceof BufferedImage)
            {
                BufferedImage bi = (BufferedImage)img[i];
                offset = convertForSubImage(bi,
                                            srcX,
                                            srcY,
                                            width,
                                            height,
                                            depth,
                                            offset);
            }
        }

        sendTextureUpdate(destX,
                          destY,
                          destZ,
                          width,
                          height,
                          depth,
                          level,
                          copyBuffer);
    }

    /**
     * Check that all the provided images have an identical image format. Start
     * with the first image and if anything differs from that, barf.
     *
     * @throws IllegalArgumentException Formats don't match
     */
    private void checkFormats() throws IllegalArgumentException
    {
        if(images == null || images.length == 0)
            return;

        int base_format = ((BufferedImage)images[0]).getType();

        for(int i = 1; i < images.length; i++)
            if(base_format != ((BufferedImage)images[i]).getType())
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(MATCH_FORMAT_PROP);
                throw new IllegalArgumentException(msg);
            }
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
        return convertImage(images);
    }

    /**
     * Convenience method to convert a buffered image into a NIO array of the
     * corresponding type. Images typically need to be swapped when doing this
     * by the Y axis is in the opposite direction to the one used by OpenGL.
     *
     * @param srcImages The list of images to convert
     * @return an appropriate array type - either IntBuffer or ByteBuffer
     */
    private ByteBuffer convertImage(RenderedImage[] srcImages)
    {
        if(!(srcImages[0] instanceof BufferedImage))
            return null;

        ByteBuffer ret_val = null;

        int num_comp = 0;
        boolean need_int_buf = false;
        BufferedImage img_0 = (BufferedImage)srcImages[0];

        switch(img_0.getType())
        {
            case BufferedImage.TYPE_3BYTE_BGR:
            case BufferedImage.TYPE_INT_RGB:
            case BufferedImage.TYPE_USHORT_555_RGB:
            case BufferedImage.TYPE_USHORT_565_RGB:
                num_comp = 3;
                break;

            case BufferedImage.TYPE_4BYTE_ABGR:
            case BufferedImage.TYPE_INT_ARGB:
                num_comp = 4;
                need_int_buf = true;
                break;

            case BufferedImage.TYPE_BYTE_INDEXED:
            case BufferedImage.TYPE_BYTE_BINARY:
                ColorModel cm = img_0.getColorModel();
                num_comp = cm.getNumComponents();

                if(num_comp == 4)
                    need_int_buf = true;
                break;

            case BufferedImage.TYPE_BYTE_GRAY:
                num_comp = 1;
                break;

            case BufferedImage.TYPE_USHORT_GRAY:
            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNSUPPORTED_TYPE_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

                Object[] msg_args = { new Integer(img_0.getType()) };
                Format[] fmts = { n_fmt };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                msg_fmt.setFormats(fmts);
                String msg = msg_fmt.format(msg_args);
        }

        ret_val = ByteBuffer.allocateDirect(width * height * depth * num_comp);
        ret_val.order(ByteOrder.nativeOrder());

        if(need_int_buf)
        {
            IntBuffer i_buf = ret_val.asIntBuffer();

            for(int i = 0; i < srcImages.length; i++)
                convertImage(i_buf, (BufferedImage)srcImages[i]);
        }
        else
        {
            for(int i = 0; i < srcImages.length; i++)
                convertImage(ret_val, (BufferedImage)srcImages[i]);
        }

        ret_val.rewind();

        return ret_val;
    }

    /**
     * Convenience method to convert a buffered image into a NIO array of the
     * corresponding type. Images typically need to be swapped when doing this
     * by the Y axis is in the opposite direction to the one used by OpenGL.
     *
     * @param img The image to convert
     * @param buffer The buffer to put the converted pixels into
     * @return an appropriate array type - either IntBuffer or ByteBuffer
     */
    private void convertImage(ByteBuffer buffer, BufferedImage img)
    {
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
            case BufferedImage.TYPE_3BYTE_BGR:
            case BufferedImage.TYPE_INT_RGB:
            case BufferedImage.TYPE_USHORT_555_RGB:
            case BufferedImage.TYPE_USHORT_565_RGB:

                format = FORMAT_RGB;

                if(invertY)
                {
                    int y = height - 1;

                    for(int i = 0; i < height; i++)
                    {
                        img.getRGB(0, y, width, 1, pixelTmp, 0, width);

                        for(int j = 0; j < width; j++)
                        {
                            int tmp = pixelTmp[j];

                            buffer.put((byte)((tmp >> 16) & 0xFF));
                            buffer.put((byte)((tmp >> 8) & 0xFF));
                            buffer.put((byte)(tmp & 0xFF));
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

                            buffer.put((byte)((tmp >> 16) & 0xFF));
                            buffer.put((byte)((tmp >> 8) & 0xFF));
                            buffer.put((byte)(tmp & 0xFF));
                        }
                    }
                }
                break;

            case BufferedImage.TYPE_4BYTE_ABGR:
            case BufferedImage.TYPE_INT_ARGB:
                // should not get here.
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
                        if(invertY)
                        {
                            int y = height - 1;

                            for(int i = 0; i < height; i++)
                            {
                                img.getRGB(0, y, width, 1, pixelTmp, 0, width);

                                for(int j = 0; j < width; j++)
                                {
                                    int tmp = pixelTmp[j];

                                    buffer.put((byte)(tmp & 0x000000FF));
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

                                    buffer.put((byte)(tmp & 0x000000FF));
                                }
                            }
                        }
                        break;

                    case 2:
                        format = FORMAT_INTENSITY_ALPHA;

                        if(invertY)
                        {
                            int y = height - 1;

                            for(int i = 0; i < height; i++)
                            {
                                img.getRGB(0, y, width, 1, pixelTmp, 0, width);

                                for(int j = 0; j < width; j++)
                                {
                                    int tmp = pixelTmp[j];

                                    buffer.put((byte)((tmp >> 8) & 0xFF));
                                    buffer.put((byte)(tmp & 0xFF));
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

                                    buffer.put((byte)((tmp >> 8) & 0xFF));
                                    buffer.put((byte)(tmp & 0xFF));
                                }
                            }
                        }
                        break;

                    case 3:
                        format = FORMAT_RGB;

                        if(invertY)
                        {
                            int y = height - 1;

                            for(int i = 0; i < height; i++)
                            {
                                img.getRGB(0, y, width, 1, pixelTmp, 0, width);

                                for(int j = 0; j < width; j++)
                                {
                                    int tmp = pixelTmp[j];

                                    buffer.put((byte)((tmp >> 16) & 0xFF));
                                    buffer.put((byte)((tmp >> 8) & 0xFF));
                                    buffer.put((byte)(tmp & 0xFF));
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

                                    buffer.put((byte)((tmp >> 16) & 0xFF));
                                    buffer.put((byte)((tmp >> 8) & 0xFF));
                                    buffer.put((byte)(tmp & 0xFF));
                                }
                            }
                        }
                        break;

                    case 4:
                        // should not get here.
                }
                break;

            case BufferedImage.TYPE_BYTE_GRAY:
                // format = FORMAT_RGB;
                // Leave this as the user selected format for either
                // alpha or gray maps
                if(invertY)
                {
                    int y = height - 1;

                    for(int i = 0; i < height; i++)
                    {
                        img.getRGB(0, y, width, 1, pixelTmp, 0, width);

                        for(int j = 0; j < width; j++)
                        {
                            int tmp = pixelTmp[j];

                            buffer.put((byte)(tmp & 0x000000FF));
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

                            buffer.put((byte)(tmp & 0x000000FF));
                        }
                    }
                }
                break;

            case BufferedImage.TYPE_USHORT_GRAY:
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

    /**
     * Convenience method to convert a buffered image into a NIO array of the
     * corresponding type. Images typically need to be swapped when doing this
     * by the Y axis is in the opposite direction to the one used by OpenGL.
     *
     * @param img The image to convert
     * @param buffer The buffer to put the converted pixels into
     */
    private void convertImage(IntBuffer buffer, BufferedImage img)
    {
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
            case BufferedImage.TYPE_3BYTE_BGR:
            case BufferedImage.TYPE_INT_RGB:
            case BufferedImage.TYPE_USHORT_555_RGB:
            case BufferedImage.TYPE_USHORT_565_RGB:
                break;

            case BufferedImage.TYPE_4BYTE_ABGR:
            case BufferedImage.TYPE_INT_ARGB:
                // OpenGL wants RGBA, so swap the byte order around as part of
                // the conversion process.

                format = FORMAT_RGBA;

                if(invertY)
                {
                    int y = height - 1;

                    for(int i = 0; i < height; i++)
                    {
                        img.getRGB(0, y, width, 1, pixelTmp, 0, width);

                        for(int j = 0; j < width; j++)
                        {
                            int alpha = (pixelTmp[j] >> 24) & 0xFF;
                            int rgb = (pixelTmp[j] << 8);

                            buffer.put(rgb | alpha);
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
                            int alpha = (pixelTmp[j] >> 24) & 0xFF;
                            int rgb = (pixelTmp[j] << 8);

                            buffer.put(rgb | alpha);
                        }
                    }
                }
                break;

            case BufferedImage.TYPE_BYTE_INDEXED:
            case BufferedImage.TYPE_BYTE_BINARY:
            case BufferedImage.TYPE_CUSTOM:
                format = FORMAT_RGBA;

                if(invertY)
                {
                    int y = height - 1;

                    for(int i = 0; i < height; i++)
                    {
                        img.getRGB(0, y, width, 1, pixelTmp, 0, width);

                        for(int j = 0; j < width; j++)
                        {
                            int alpha = (pixelTmp[j] >> 24) & 0xFF;
                            int rgb = (pixelTmp[j] << 8);

                            buffer.put(rgb | alpha);
                        }

                        y--;
                    }
                }
                else
                {
                    for(int i = 0; i < height; i++)
                    {
                        img.getRGB(i, 0, width, 1, pixelTmp, 0, width);

                        for(int j = 0; j < width; j++)
                        {
                            int alpha = (pixelTmp[j] >> 24) & 0xFF;
                            int rgb = (pixelTmp[j] << 8);

                            buffer.put(rgb | alpha);
                        }
                    }
                }
                break;

            case BufferedImage.TYPE_BYTE_GRAY:
            case BufferedImage.TYPE_USHORT_GRAY:
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
     * @param depth The depth of the section to replace
     * @param offset The offset into the copyBuffer to start placing bytes
     * @return The final offset after placing all these bytes
     */
    private int convertForSubImage(BufferedImage img,
                                    int startX,
                                    int startY,
                                    int width,
                                    int height,
                                    int depth,
                                    int offset)
    {
        int img_height = img.getHeight(null);
        ColorModel cm = img.getColorModel();
        int pos = offset;

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

                if(offset == 0)
                    checkCopyBufferSize(width * height * depth * 3);

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

                if(offset == 0)
                    checkCopyBufferSize(width * height * depth * 4);

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
                        if(offset == 0)
                            checkCopyBufferSize(width * height * depth);

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
                        if(offset == 0)
                            checkCopyBufferSize(width * height * depth * 2);

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

                        if(offset == 0)
                            checkCopyBufferSize(width * height * depth * 3);

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
                        if(offset == 0)
                            checkCopyBufferSize(width * height * depth * 4);

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
                if(offset == 0)
                    checkCopyBufferSize(width * height * depth);

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

        return pos;
    }
}
