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

import java.awt.image.Raster;

// Local imports
// None

/**
 * Wraps an image that gets used in textures.
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class ImageComponent extends NodeComponent
{
    /** Specifies the image is in RGB format */
    public static final int FORMAT_RGB = 0;

    /** Specifies the image is in RGBA format */
    public static final int FORMAT_RGBA = 1;

    /** Specifies the data is in byte format */
    public static final int TYPE_BYTE = DataBuffer.TYPE_BYTE;

    /** Specifies the data is in int format */
    public static final int TYPE_INT = DataBuffer.TYPE_INT;

    /** The width */
    protected int width;

    /** The height */
    protected int height;

    /** The format */
    protected int format;

    /** The image data */
    protected RenderedImage image;

    /** The size of the data buffer */
    protected int size;

    /** The type of the data */
    protected int type;

    /** Buffer to hold the data */
    protected Object data;

    /**
     * Constructs an image with default values.
     */
    public ImageComponent()
    {
    }

    /**
     * Get the width of this image.
     *
     * @return the width.
     */
    public int getWidth()
    {
        return width;
    }

    /**
     * Get the height of this image.
     *
     * @return the height.
     */
    public int getHeight()
    {
        return height;
    }

    /**
     * Get the format of this image.
     *
     * @return the format.
     */
    public int getFormat()
    {
        return format;
    }

    /**
     * Get the size of the data.
     *
     * @return the data size
     */
    protected int getDataSize()
    {
        if(data == null)
            fillData();

        return size;
    }

    /**
     * Get the underlying data object.  This will be
     * an array of the underlying data type.
     *
     * @return A reference to the data.
     */
    protected Object getData()
    {
        if (data == null)
            fillData();

        return data;
    }

    /**
     * Clear the storage used for this object.
     */
    protected void clearData()
    {
        data = null;
    }

    /**
     * Get the data type of the image.
     *
     * @return The data type.
     */
    protected int getDataType()
    {
        if (data == null)
            fillData();

        return type;
    }

    /**
     * Fill in the data array.
     */
    protected void fillData()
    {
        if (image == null)
            return;

        Raster raster = image.getData();

        type = raster.getTransferType();
        int num_el = raster.getNumDataElements();
        int num_band = raster.getNumBands();
        int img_bytes = width * height * num_el * num_band;

        switch(type)
        {
            case DataBuffer.TYPE_BYTE :
                data = new byte[img_bytes];
                raster.getDataElements(0, 0, width, height,data);
                break;

            case DataBuffer.TYPE_INT:
                // Seems only byte arrays work for gl4Java so convert, yuck!
                int[] tmpdata = new int[img_bytes];
                data = new byte[img_bytes * 3];
                raster.getDataElements(0, 0, width, height, tmpdata);
                int aPixel;
                int offset = 0;
                int len = tmpdata.length;

                byte[] bdata = ((byte[])data);
                //offset = bdata.length - 1;

                for(int i=0; i < len; i++) {
                    aPixel = tmpdata[i];
                    bdata[offset++] = (byte) (aPixel >> 16);
                    bdata[offset++] = (byte) (aPixel >> 8);
                    bdata[offset++] = (byte) (aPixel >> 0);
                }
/*
                data = new int[img_bytes];
                raster.getDataElements(0,0,width,height,data);
*/
                break;

            default:
                System.out.println("Unhandled transfertype in ImageComponent");
        }

        image = null;
    }
}
