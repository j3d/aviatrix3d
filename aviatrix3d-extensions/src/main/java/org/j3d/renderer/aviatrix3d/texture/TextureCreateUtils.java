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

package org.j3d.renderer.aviatrix3d.texture;

// External imports
import java.awt.image.*;

import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;
import org.j3d.util.ImageUtils;
import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.ImageTextureComponent2D;
import org.j3d.aviatrix3d.TextureComponent;
import org.j3d.aviatrix3d.TextureComponent2D;
import org.j3d.aviatrix3d.Texture;
import org.j3d.aviatrix3d.Texture2D;

/**
 * <p>
 * Convenience class with a collection of useful utility methods taking an
 * image and turning it into a Aviatrix3D texture object.
 * </p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>invalidImageTypeMsg: Error message when given a non-standard image type</li>
 * <li>cantRescaleRIMsg: Error message when a non-Buffered image is provided
 *     for rescaling</li>
 * <li>resizeInfoMsg: Info message detailing final size picked</li>
 * </ul>
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.8 $
 */
public class TextureCreateUtils
{
    /** Message when the image type provided is invalid */
    private static final String INVALID_IMAGE_TYPE_PROP =
        "org.j3d.renderer.aviatrix3d.texture.TextureCreateUtils.invalidImageTypeMsg";

    /** Message when the attempting to rescale a non buffered image */
    private static final String RI_RESCALE_PROP =
        "org.j3d.renderer.aviatrix3d.texture.TextureCreateUtils.cantRescaleRIMsg";

	/** Info message about resizing the image */
	private static final String RESIZE_INFO_MSG =
        "org.j3d.renderer.aviatrix3d.texture.TextureCreateUtils.resizeInfoMsg";

    /** Internal convenience value of Math.log(2) */
    private static final double LOG_2 = Math.log(2);

    /** Error reporter used to send out messages */
    private ErrorReporter errorReporter;

    /**
     * Default constructor.
     */
    public TextureCreateUtils()
    {
        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the node's internals can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter)
    {
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter = reporter;
    }

    /**
     * Given the image, create a texture object from it, resizing the image to
     * up to a power of 2 if needed. The texture created is a basic
     * non-repeating texture, with no filtering information set.
     *
     * @param img The source image to work with
     * @return A texture object to hold the image with
     */
    public Texture2D createTexture2D(RenderedImage img)
    {
        int width = img.getWidth();
        int height = img.getHeight();

        width = nearestPowerTwo(width, true);
        height = nearestPowerTwo(height, true);

        RenderedImage base_img = scaleTexture(img, width, height);
        TextureComponent2D[] comp = { create2DTextureComponent(base_img) };
        Texture2D ret_val = new Texture2D();

        ret_val.setSources(Texture2D.MODE_BASE_LEVEL,
                           getTextureFormat(comp[0]),
                           comp,
                           1);

        return ret_val;
    }

    /**
     * From the image component format, generate the appropriate texture
     * format.
     *
     * @param comp The image component to get the value from
     * @return The appropriate corresponding texture format value
     */
    public int getTextureFormat(TextureComponent comp)
    {
        int ret_val = Texture.FORMAT_RGBA;

        switch(comp.getFormat(0))
        {
            case TextureComponent.FORMAT_SINGLE_COMPONENT:
                // could also be alpha, but we'll punt for now. We really need
                // the user to pass in this information. Need to think of a
                // good way of doing this.
                ret_val = Texture.FORMAT_INTENSITY;
                break;

            case TextureComponent.FORMAT_INTENSITY_ALPHA:
                ret_val = Texture.FORMAT_INTENSITY_ALPHA;
                break;

            case TextureComponent.FORMAT_RGB:
            case TextureComponent.FORMAT_BGR:
                ret_val = Texture.FORMAT_RGB;
                break;

            case TextureComponent.FORMAT_RGBA:
            case TextureComponent.FORMAT_BGRA:
                ret_val = Texture.FORMAT_RGBA;
                break;
        }

        return ret_val;
    }

    /**
     * Scale a texture to a new size. Generally used to scale a texture to a
     * power of 2.
     *
     * @param ri The texture to scale
     * @param newWidth The new width
     * @param newHeight The new height
     */
    public RenderedImage scaleTexture(RenderedImage ri,
                                      int newWidth,
                                      int newHeight)
    {
        int width = ri.getWidth();
        int height = ri.getHeight();

        if(width == newWidth && height == newHeight)
            return ri;

		if(!(ri instanceof BufferedImage))
		{
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(RI_RESCALE_PROP);

            errorReporter.warningReport(msg, null);
		}

        float xScale = (float)newWidth / (float)width;
        float yScale = (float)newHeight / (float)height;

        AffineTransform at = AffineTransform.getScaleInstance(xScale, yScale);
        AffineTransformOp atop =
            new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        RenderedImage ret_val = ri;

		BufferedImage buffImage = (BufferedImage)ri;
		ColorModel cm = buffImage.getColorModel();

		if(cm.hasAlpha())
		{
			ret_val = atop.filter((BufferedImage)ri, null);
		}
		else
		{
			ret_val = atop.filter((BufferedImage)ri, null);
		}

        return ret_val;
    }

    /**
     * Load the image component from the given object type. All images are
     * loaded by-reference. This does not automatically register the component
     * with the internal datastructures. That is the responsibility of the
     * caller.
     *
     * @param content The object that was loaded and needs to be converted
     * @return An TextureComponent instance with byRef true and yUp false
     */
    public TextureComponent2D create2DTextureComponent(Object content)
    {
        if(!(content instanceof ImageProducer) &&
           !(content instanceof BufferedImage) &&
           !(content instanceof Image))
		{
			I18nManager intl_mgr = I18nManager.getManager();
            Locale lcl = intl_mgr.getFoundLocale();
			String msg_pattern = intl_mgr.getString(INVALID_IMAGE_TYPE_PROP);

			Object[] msg_args = { content.getClass().getName() };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
			String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
		}

        BufferedImage image = null;

        if(content instanceof ImageProducer)
            image = ImageUtils.createBufferedImage((ImageProducer)content);
        else if(content instanceof BufferedImage)
            image = (BufferedImage)content;
        else
            image = ImageUtils.createBufferedImage((Image)content);

        int img_width = image.getWidth(null);
        int img_height = image.getHeight(null);

        int tex_width = nearestPowerTwo(img_width, true);
        int tex_height = nearestPowerTwo(img_height, true);

        if(tex_width != img_width || tex_height != img_height)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(RESIZE_INFO_MSG);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args =
            {
				new Integer(tex_width),
				new Integer(tex_height)
		    };

            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            errorReporter.messageReport(msg);

            image = (BufferedImage)scaleTexture(image, tex_width, tex_height);
        }

        ColorModel cm = image.getColorModel();
        boolean alpha = cm.hasAlpha();

        int format = TextureComponent.FORMAT_RGBA;

        switch(image.getType())
        {
            case BufferedImage.TYPE_3BYTE_BGR:
            case BufferedImage.TYPE_BYTE_BINARY:
            case BufferedImage.TYPE_INT_BGR:
            case BufferedImage.TYPE_INT_RGB:
            case BufferedImage.TYPE_USHORT_555_RGB:
            case BufferedImage.TYPE_USHORT_565_RGB:
                format = TextureComponent.FORMAT_RGB;
                break;

            case BufferedImage.TYPE_CUSTOM:
                // no idea what this should be, so default to RGBA
            case BufferedImage.TYPE_INT_ARGB:
            case BufferedImage.TYPE_INT_ARGB_PRE:
            case BufferedImage.TYPE_4BYTE_ABGR:
            case BufferedImage.TYPE_4BYTE_ABGR_PRE:
                format = TextureComponent.FORMAT_RGBA;
                break;

            case BufferedImage.TYPE_BYTE_GRAY:
            case BufferedImage.TYPE_USHORT_GRAY:
                format = TextureComponent.FORMAT_SINGLE_COMPONENT;
                break;

            case BufferedImage.TYPE_BYTE_INDEXED:
                if(alpha)
                    format = TextureComponent.FORMAT_RGBA;
                else
                    format = TextureComponent.FORMAT_RGB;
                break;
        }

        TextureComponent2D ret_val =
            new ImageTextureComponent2D(format,
                                        tex_width,
                                        tex_height,
                                        image);


        return ret_val;
    }

    /**
     * Determine the nearest power of two value for a given argument.
     * This function uses the formal ln(x) / ln(2) = log2(x)
     *
     * @param val The initial size
     * @param scaleUp true to scale the value up, false for down
     * @return The power-of-two-ized value
     */
    public int nearestPowerTwo(int val, boolean scaleUp)
    {
        int log;

        if(scaleUp)
            log = (int) Math.ceil(Math.log(val) / LOG_2);
        else
            log = (int) Math.floor(Math.log(val) / LOG_2);

        return (int)Math.pow(2, log);
    }

    /**
     * Convert an image that is a greyscale heightmap into a normal map, for
     * use in DOT3 bump mapping. The input image must be one of the forms of
     * grayscale image. The output image, if provided must be exactly the same
     * dimensions as the input image and must be TYPE_INT_RGB. If it does not
     * fit this description, a new instance will be created and used as the
     * return value. If it does fit, then it will written to and returned as the
     * return value.
     *
     * @param bumpImage The source image to take the heights from
     * @param destImage An image to write the normal map to or null if creating new
     * @return An image representing the new map
     */
    public BufferedImage createNormalMap(BufferedImage bumpImage,
                                         BufferedImage destImage)
    {
        BufferedImage ret_val = null;
        int width = bumpImage.getWidth();
        int height = bumpImage.getHeight();

        if((destImage == null) ||
           (destImage.getHeight() != height) ||
           (destImage.getWidth() != width) ||
           (destImage.getType() != BufferedImage.TYPE_INT_RGB))
        {
            ret_val = new BufferedImage(width,
                                        height,
                                        BufferedImage.TYPE_INT_RGB);
        }

        // first turn the image into a set of floating point values [0,1]
        Raster src_data = bumpImage.getData();

        float[][] src_pixels = new float[height][width];
        int idx = 0;
        for(int y = 0; y < height; y++)
        {
            for(int x = 0; x < width; x++)
                src_pixels[y][x] = src_data.getSample(x, y, 0) * 0.0039215f;
        }

        // Now create normals by looking at the differences between the
        // heights of this pixel and yt's neighbours.
        for(int y = 0; y < height; y++)
        {
            for(int x = 0; x < width; x++)
            {
                // Y Sobel filter
                float dY = -src_pixels[(y + 1) % height][(x - 1 + width) % width];
                dY += src_pixels[(y+1) % height][x % width] * -2.0f;
                dY += -src_pixels[(y+1) % height][(x+1) % width];
                dY += src_pixels[(y-1+height) % height][(x-1+width) % width];
                dY += src_pixels[(y-1+height) % height][x % width] * 2;
                dY += src_pixels[(y-1+height) % height][(x+1) % width];

                // X Sobel filter
                float dX = -src_pixels[(y-1+height) % height][(x-1+width) % width];
                dX += src_pixels[y % height][(x-1+width) % width] * -2.0f;
                dX += -src_pixels[(y+1) % height][(x-1+width) % width];
                dX += src_pixels[(y-1+height) % height][(x+1) % width];
                dX += src_pixels[y % height][(x+1) % width] * 2.0f;
                dX += src_pixels[(y+1) % height][(x+1) % width];

                // Cross Product of components of gradient reduces to
                // -dX, -dY, 1

                // Normalize
                float d = 1.0f / (float)Math.sqrt(dX*dX + dY*dY + 1);
                dX = -dX * d;
                dY = -dY * d;
                float dZ = d;

                // convert them all to RGB with yn a single ynt
                int rgb = ((int)((dX + 1) / 2 * 255) << 16) |
                          ((int)((dY + 1) / 2 * 255) << 8) |
                          ((int)((dZ + 1) / 2 * 255));

                ret_val.setRGB(x, y, rgb);
            }
        }

        return ret_val;
    }
}
