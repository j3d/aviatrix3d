/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
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

// Standard imports
import java.awt.image.*;

import java.util.HashMap;

// Application specific imports
import gl4java.GLFunc;
import gl4java.GLContext;
import gl4java.GLEnum;
import gl4java.drawable.GLDrawable;

/**
 * Wraps a 2D image.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class ImageComponent2D extends ImageComponent
{

    /**
     * Constructs an image with default values.
     */
    public ImageComponent2D()
    {
        this(0, 0, 0, null);
    }

    /**
     * Constructs an Image2D using the specified format, width, height and
     * rendered image.
     *
     * @param format The image format.  RGB, RGBA currently
     * @param width The width of the image
     * @param height The height of the image
     * @param image The image data
     */
    public ImageComponent2D(int format,
                            int width,
                            int height,
                            RenderedImage image)
    {
        this.image = image;
        this.width = width;
        this.height = height;
        this.format = format;

        fillData();
    }

    /**
     * Constructs an Image2D using the specified format and
     * rendered image.
     *
     * @param format The image format.  RGB, RGBA currently
     * @param image The image data
     */
    public ImageComponent2D(int format, RenderedImage image)
    {
        BufferedImage bi;

        this.format = format;
        this.image = image;

        if(image instanceof BufferedImage)
        {
            bi = (BufferedImage) image;
            width = image.getWidth();
            height = image.getHeight();
        }
        else
        {
            System.out.println("Can't handle RenderedImage in Image2D");
            return;
        }

        fillData();
    }
}