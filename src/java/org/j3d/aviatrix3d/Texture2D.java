/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003
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
import java.util.HashMap;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLU;

// Local imports
// None

/**
 * Describes the 2D texture properties of an object.
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class Texture2D extends Texture
{
    /** The texture format */
    private int format;

    /** The width */
    private int width;

    /** The height */
    private int height;

    /** The mipMapMode */
    private int mipMapMode;

    /**
     * Constructs a texture with default values.
     */
    public Texture2D()
    {
        changed = false;
    }

    /**
     * Constructs a texture an empty texture of the given format and size.
     */
    public Texture2D(int mipMapMode, int format, int width, int height)
    {
        changed = false;
        this.format = format;
        this.width = width;
        this.height = height;
        this.mipMapMode = mipMapMode;
    }

    /**
     * Constructs a 2D texture with the specified images.  The number of images
     * should be 1 for no mip maps, or all mipmap levels should be provided.
     *
     * @param images The image defining this texture.
     */
    public Texture2D(ImageComponent2D[] images)
    {
        this.images = images;

        changed = true;
    }

    /**
     * Set the images for this texture
     *
     * @param images The image data
     */
     public void setImages(ImageComponent2D[] images)
     {
        super.setImages(images);

        changed = true;
     }

    /**
     * Set the format for this texture.
     *
     * @param format The format.
     */
    public void setFormat(int format)
    {
        this.format = format;
    }

    /**
     * Get the format for this texture.
     *
     * @return The format.
     */
    public int getFormat()
    {
        return format;
    }

    /**
     * Issue ogl commands needed for this component
     *
     * @param gld The drawable for reseting the state
     */
    public void renderState(GL gl, GLU glu)
    {
        if(images == null || images.length < 1)
            return;

        gl.glEnable(GL.GL_TEXTURE_2D);

        if(changed)
        {
            gl.glTexParameteri(GL.GL_TEXTURE_2D,
                               GL.GL_TEXTURE_WRAP_S,
                               boundaryModeS);

            gl.glTexParameteri(GL.GL_TEXTURE_2D,
                               GL.GL_TEXTURE_WRAP_T,
                               boundaryModeT);

            int numImages = images.length;
            int mode = 0;
            switch(magFilter) {
                case MAGFILTER_FASTEST:
                case MAGFILTER_BASE_LEVEL_POINT:
                    mode = GL.GL_NEAREST;
                    break;

                case MAGFILTER_NICEST:
                case MAGFILTER_BASE_LEVEL_LINEAR:
                    mode = GL.GL_LINEAR;
                    break;

                default: System.out.println("Unknown mode in MagFilter: " + magFilter);
            }

            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, mode);

            switch(minFilter)
            {
                case MINFILTER_FASTEST:
                case MINFILTER_BASE_LEVEL_POINT:
                    mode = GL.GL_NEAREST;
                    break;
                case MINFILTER_BASE_LEVEL_LINEAR:
                    mode = GL.GL_LINEAR;
                case MINFILTER_MULTI_LEVEL_LINEAR:
                    mode = GL.GL_LINEAR_MIPMAP_LINEAR;
                    break;
                case MINFILTER_MULTI_LEVEL_POINT:
                    mode = GL.GL_NEAREST_MIPMAP_NEAREST;
                    break;
                case MINFILTER_NICEST:
                    if (numImages > 1)
                        mode = GL.GL_LINEAR_MIPMAP_LINEAR;
                    else
                        mode = GL.GL_LINEAR;

                    break;
                default: System.out.println("Unknown mode in MinFilter: " + minFilter);
            }

            gl.glTexParameteri(GL.GL_TEXTURE_2D,
                               GL.GL_TEXTURE_MIN_FILTER,
                               mode);

            if (anisotropicMode != ANISOTROPIC_MODE_NONE)
            {
                // float[] val = new float[1];

                //gl.glGetFloatv(GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, val);
                gl.glTexParameterf(GL.GL_TEXTURE_2D,
                                   GL.GL_TEXTURE_MAX_ANISOTROPY_EXT,
                                   anisotropicDegree);
            }

            Object data;

            for(int i=0; i < numImages; i++)
            {
                data = images[i].getData();

                int width = images[i].getWidth();
                int height = images[i].getHeight();

//                System.out.println("wid: " + width + " height: " + height + " data: " + data);
                switch(images[0].getDataType())
                {
                    case ImageComponent2D.TYPE_INT:
                        gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
                        gl.glTexImage2D(GL.GL_TEXTURE_2D,
                                        i,
                                        GL.GL_RGB,
                                        width,
                                        height,
                                        0,
                                        GL.GL_RGB,
                                        GL.GL_UNSIGNED_BYTE,
                                        (byte[])data);

                        break;

                    case ImageComponent2D.TYPE_BYTE:
                        System.out.println("Byte");
                        gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT,1);
                        gl.glTexImage2D(GL.GL_TEXTURE_2D, i, GL.GL_RGBA, width, height, 0,
                            GL.GL_RGBA, GL.GL_UNSIGNED_BYTE,(byte[])data);
                        break;
                }
            }

            changed = false;
        }
    }

    /**
     * Restore all openGL state to the given drawable
     *
     * @param gld The drawable for reseting the state
     */
    public void restoreState(GL gl, GLU glu)
    {
        if (images == null || images.length < 1)
            return;

        gl.glDisable(GL.GL_TEXTURE_2D);
    }
}