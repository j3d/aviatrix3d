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
import java.util.HashMap;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLDrawable;
import net.java.games.jogl.GLU;

// Local imports
// None

/**
 * Describes the texture properties of an object.
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public abstract class Texture extends NodeComponent
{
    /** MipMapMode constants - No Mip Map*/
    public static final int MODE_BASE_LEVEL = 0;

    /** MipMapMode constants - Use Mip Maps */
    public static final int MODE_MULTI_LEVEL_MIPMAP = 1;

    // Boundary Modes
    public static final int BM_WRAP = GL.GL_REPEAT;
    public static final int BM_CLAMP = GL.GL_CLAMP;
    public static final int BM_CLAMP_TO_EDGE = GL.GL_CLAMP_TO_EDGE;
    public static final int BM_CLAMP_TO_BOUNDARY = GL.GL_CLAMP_TO_BORDER;
    //public static final int BM_MIRRORED_REPEAT = GL.GL_MIRRORED_REPEAT;

    // Maxification Filter Techniques
    public static final int MAGFILTER_FASTEST = 0;
    public static final int MAGFILTER_NICEST = 1;
    public static final int MAGFILTER_BASE_LEVEL_POINT = 2;
    public static final int MAGFILTER_BASE_LEVEL_LINEAR = 3;
    public static final int MAGFILTER_LINEAR_DETAIL = 4;
    public static final int MAGFILTER_LINEAR_DETAIL_RGB = 5;
    public static final int MAGFILTER_LINEAR_DETAIL_ALPHA = 6;

    // Minification Filter Techniques
    public static final int MINFILTER_FASTEST = 0;
    public static final int MINFILTER_NICEST = 1;
    public static final int MINFILTER_BASE_LEVEL_POINT = 2;
    public static final int MINFILTER_BASE_LEVEL_LINEAR = 3;
    public static final int MINFILTER_MULTI_LEVEL_POINT = 4;
    public static final int MINFILTER_MULTI_LEVEL_LINEAR = 5;

    // Anistropic Mode
    public static final int ANISOTROPIC_MODE_NONE = 0;
    public static final int ANISOTROPIC_MODE_SINGLE = 1;

    // Format values
    public static final int FORMAT_RGB = GL.GL_RGB;
    public static final int FORMAT_RGBA = GL.GL_RGBA;

    /** The images defining this texture */
    protected ImageComponent[] images;

    /** The Anisotropic Filtering Mode */
    protected int anisotropicMode;

    /** The Anisotropic Filtering Degree */
    protected float anisotropicDegree;

    /** The magnification filter */
    protected int magFilter;

    /** The minification filter */
    protected int minFilter;

    /** The boundary mode S value */
    protected int boundaryModeS;

    /** The boundary mode S value */
    protected int boundaryModeT;

    /** Has any attribute changed */
    protected boolean changed;

    /**
     * Constructs a texture with default values.
     */
    public Texture()
    {
    }

    /**
     * Set the images for this texture
     *
     * @param images The image data
     */
    public void setImages(ImageComponent[] images)
    {
        this.images = images;
    }

    /**
     * Set the aniostropic filtering mode.
     *
     * @param mode The new mode.
     */
    public void setAnisotropicFilterMode(int mode)
    {
        anisotropicMode = mode;
        changed = true;
    }

    /**
     * Set the anisotropic filtering degree.  Values greater
     * then the hardware supports will be clamped.
     *
     * @param degree The filtering degree.  1.0 is the default.
     */
    public void setAnisotropicFilterDegree(float degree)
    {
        anisotropicDegree = degree;
        // TODO: Need to clamp to hardware max
        changed = true;
    }

    /**
     * Set the magnification filtering mode.
     *
     * @param mode The new mode.
     */
    public void setMagFilter(int mode)
    {
        magFilter = mode;
        changed = true;
    }

    /**
     * Set the magnification filtering mode.
     *
     * @param mode The new mode.
     */
    public void setMinFilter(int mode)
    {
        minFilter = mode;
        changed = true;
    }

    /**
     * Set the boundary handling for the S parameter.
     *
     * @param mode The new mode.
     */
    public void setBoundaryModeS(int mode)
    {
        boundaryModeS = mode;
        changed = true;
    }

    /**
     * Set the boundary handling for the T parameter.
     *
     * @param mode The new mode.
     */
    public void setBoundaryModeT(int mode)
    {
        boundaryModeT = mode;
        changed = true;
    }
}