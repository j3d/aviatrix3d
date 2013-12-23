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


// Local imports
// None

/**
 * Wraps a 3D image.
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class ImageComponent3D extends ImageComponent
{
    /** The depth */
    private int depth;

    /**
     * Constructs an image with default values.
     */
    public ImageComponent3D()
    {
        this(0, 0, 0, 0, null);
    }

    /**
     * Constructs an Image3D using the specified format, width, height and
     * rendered image.
     *
     * @param format The image format.  RGB, RGBA currently
     * @param width The width of the image
     * @param height The height of the image
     * @param image The image data
     */
    public ImageComponent3D(int format,
                            int width,
                            int height,
                            int depth,
                            RenderedImage image)
    {
        this.image = image;
        this.width = width;
        this.height = height;
        this.format = format;
        this.depth = depth;

        fillData();
    }

    /**
     * Constructs an Image2D using the specified format and
     * rendered image.
     *
     * @param format The image format.  RGB, RGBA currently
     * @param image The image data
     */
    public ImageComponent3D(int format, RenderedImage image)
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
            System.out.println("Can't handle RenderedImage in Image3D");
            return;
        }

        fillData();
    }
}
