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
import java.text.Format;
import java.text.MessageFormat;
import java.util.Locale;

import java.nio.ByteBuffer;

import javax.media.opengl.GL;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.iutil.TextureUpdateData;

/**
 * Describes the 3D (volume) texture properties of an object.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>3dSourceMsg: Message when the user doesn't provide a 3D texture
 *     source.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.36 $
 */
public class Texture3D extends Texture
{
    /** Message when setting the active view if it contains a shared parent */
    private static final String TEXT_3D_SRC_PROP =
        "org.j3d.aviatrix3d.Texture3D.3dSourceMsg";

    /** The boundary mode R value */
    private int boundaryModeR;

    /** The height of the main texture. */
    private int height;

    /** The depth of the main texture (the first image in the component */
    private int depth;

    /** The boundary mode S value */
    private int boundaryModeT;

    /**
     * Constructs a texture with default values.
     */
    public Texture3D()
    {
        super(GL.GL_TEXTURE_3D, 1);
        height = -1;
        depth = -1;

        init();
    }

    /**
     * Constructs a texture using just a single image, thus setting it initially
     * as MODE_BASE_LEVEL.
     */
    public Texture3D(int format, TextureComponent3D singleImage)
    {
        super(GL.GL_TEXTURE_3D, 1);

        this.format = format;
        sources = new TextureSource[1];
        sources[0] = singleImage;

        width = singleImage.getWidth();
        height = singleImage.getHeight();
        depth = singleImage.getDepth();
        numSources = 1;

        init();
    }

    //---------------------------------------------------------------
    // Methods defined by Texture
    //---------------------------------------------------------------

    /**
     * Set a new collection of sources for this texture to use.
     *
     * @param mipMapMode Flag stating the type of texture mode to use
     * @param format Image format to use for grayscale sources
     * @param texSources The source data to use, single for base level
     * @param num The valid number of sources to use from the array
     * @throws IllegalArgumentException Source must be 3D texture data
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void setSources(int mipMapMode,
                           int format,
                           TextureSource[] texSources,
                           int num)
        throws InvalidWriteTimingException
    {
        if(num > 0)
        {
            for(int i = 0; i < num; i++)
            {
                if((texSources[i] != null) &&
                   !(texSources[i] instanceof TextureComponent3D))
                {
                    I18nManager intl_mgr = I18nManager.getManager();
                    Locale lcl = intl_mgr.getFoundLocale();
                    String msg_pattern = intl_mgr.getString(TEXT_3D_SRC_PROP);

                    Object[] msg_args = { texSources[i].getClass().getName() };
                    MessageFormat msg_fmt =
                        new MessageFormat(msg_pattern, lcl);
                    String msg = msg_fmt.format(msg_args);

                    throw new IllegalArgumentException(msg);
                }
            }
        }

        super.setSources(mipMapMode, format, texSources, num);

        // Check to see if the source is a multipass source or not.
        if(num > 0)
        {
            depth = ((TextureComponent3D)sources[0]).getDepth();
            height = ((TextureComponent3D)sources[0]).getHeight();
        }

        stateChanged.setAll(true);
    }

    //---------------------------------------------------------------
    // Methods defined by Comparable
    //---------------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object. Derived instances
     * should override this to add texture-specific extensions.
     *
     * @param tex The texture instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(Texture tex)
    {
        int res = super.compareTo(tex);
        if(res != 0)
            return res;

        Texture3D t3d = (Texture3D)tex;

        if(height != t3d.height)
            return height < t3d.height ? -1 : 1;

        if(depth != t3d.depth)
            return depth < t3d.depth ? -1 : 1;

        if(boundaryModeS != t3d.boundaryModeS)
            return boundaryModeS < t3d.boundaryModeS ? -1 : 1;

        if(boundaryModeT != t3d.boundaryModeT)
            return boundaryModeT < t3d.boundaryModeT ? -1 : 1;

        if(boundaryModeR != t3d.boundaryModeR)
            return boundaryModeR < t3d.boundaryModeR ? -1 : 1;

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param tex The texture instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(Texture tex)
    {
        if(!super.equals(tex))
            return false;

        if(!(tex instanceof Texture3D))
            return false;

        Texture3D t3d = (Texture3D)tex;

        if((height != t3d.height) ||
           (depth != t3d.depth) ||
           (boundaryModeS != t3d.boundaryModeS) ||
           (boundaryModeT != t3d.boundaryModeT) ||
           (boundaryModeR != t3d.boundaryModeR))
            return false;

        return true;
    }

    //---------------------------------------------------------------
    // Methods defined by ObjectRenderable
    //---------------------------------------------------------------

    /**
     * Issue ogl commands needed for this component
     *
     * @param gl The gl context to draw with
     */
    public void render(GL gl)
    {
        if(numSources == 0)
            return;

        Integer t_id = textureIdMap.get(gl);
        if(t_id == null)
        {
            int[] tex_id_tmp = new int[1];
            gl.glGenTextures(1, tex_id_tmp, 0);
            textureIdMap.put(gl, new Integer(tex_id_tmp[0]));

            gl.glBindTexture(textureType, tex_id_tmp[0]);

            // Set the flag so that we update later in the method
            imageChanged.put(gl, true);
            stateChanged.put(gl, true);
            updateManagers[0].addContext(gl);
        }
        else
        {
            gl.glBindTexture(textureType, t_id.intValue());
        }

        if(stateChanged.getState(gl))
        {
            stateChanged.put(gl, false);

            gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
            gl.glTexParameteri(GL.GL_TEXTURE_3D,
                               GL.GL_TEXTURE_WRAP_S,
                               boundaryModeS);

            gl.glTexParameteri(GL.GL_TEXTURE_3D,
                               GL.GL_TEXTURE_WRAP_T,
                               boundaryModeT);

            gl.glTexParameteri(GL.GL_TEXTURE_3D,
                               GL.GL_TEXTURE_WRAP_R,
                               boundaryModeR);

            gl.glTexParameteri(GL.GL_TEXTURE_2D,
                               GL.GL_GENERATE_MIPMAP,
                               generateMipMap);

            gl.glHint(GL.GL_GENERATE_MIPMAP_HINT, generateMipMapHint);


            int mode = 0;
            switch(magFilter)
            {
                case MAGFILTER_FASTEST:
                case MAGFILTER_BASE_LEVEL_POINT:
                    mode = GL.GL_NEAREST;
                    break;

                case MAGFILTER_NICEST:
                case MAGFILTER_BASE_LEVEL_LINEAR:
                    mode = GL.GL_LINEAR;
                    break;
            }

            gl.glTexParameteri(GL.GL_TEXTURE_3D, GL.GL_TEXTURE_MAG_FILTER, mode);

            switch(minFilter)
            {
                case MINFILTER_FASTEST:
                case MINFILTER_BASE_LEVEL_POINT:
                    mode = GL.GL_NEAREST;
                    break;

                case MINFILTER_BASE_LEVEL_LINEAR:
                    mode = GL.GL_LINEAR;
                    break;

                case MINFILTER_MULTI_LEVEL_LINEAR:
                    mode = GL.GL_LINEAR_MIPMAP_LINEAR;
                    break;

                case MINFILTER_MULTI_LEVEL_POINT:
                    mode = GL.GL_NEAREST_MIPMAP_NEAREST;
                    break;

                case MINFILTER_NICEST:
                    mode = (numSources > 1) ? GL.GL_LINEAR_MIPMAP_LINEAR : GL.GL_LINEAR;
                    break;
            }

            gl.glTexParameteri(GL.GL_TEXTURE_3D,
                               GL.GL_TEXTURE_MIN_FILTER,
                               mode);

            if(anisotropicMode != ANISOTROPIC_MODE_NONE)
            {
                gl.glTexParameterf(GL.GL_TEXTURE_3D,
                                   GL.GL_TEXTURE_MAX_ANISOTROPY_EXT,
                                   anisotropicDegree);
            }

            if(priority >= 0)
            {
                gl.glTexParameterf(GL.GL_TEXTURE_3D,
                                   GL.GL_TEXTURE_PRIORITY,
                                   priority);
            }

            if(borderColor != null)
            {
                gl.glTexParameterfv(GL.GL_TEXTURE_3D,
                                    GL.GL_TEXTURE_BORDER_COLOR,
                                    borderColor,
                                    0);
            }

            if(format == FORMAT_DEPTH_COMPONENT)
            {
                gl.glTexParameterf(GL.GL_TEXTURE_3D,
                                   GL.GL_DEPTH_TEXTURE_MODE,
                                   depthComponentMode);

                gl.glTexParameterf(GL.GL_TEXTURE_3D,
                                   GL.GL_TEXTURE_COMPARE_MODE,
                                   compareMode);

                gl.glTexParameterf(GL.GL_TEXTURE_3D,
                                   GL.GL_TEXTURE_COMPARE_FUNC,
                                   compareFunction);
            }
        }

        if(imageChanged.getState(gl))
        {
            imageChanged.put(gl, false);

            TextureComponent3D img = (TextureComponent3D)sources[0];
            int width = img.getWidth();
            int height = img.getHeight();
            int depth = img.getDepth();

            int num_levels =
                (mipMapMode == MODE_BASE_LEVEL) ? 1 : img.getNumLevels();

            for(int i = 0; i < num_levels; i++)
            {
                ByteBuffer pixels = img.getData(i);
                pixels.rewind();
                int comp_format = img.getFormat(i);
                int int_format = GL.GL_RGB;
                int ext_format = GL.GL_RGB;

                switch(comp_format)
                {
                    case TextureComponent.FORMAT_RGB:
                        int_format = GL.GL_RGB;
                        ext_format = GL.GL_RGB;
                        break;

                    case TextureComponent.FORMAT_RGBA:
                        int_format = GL.GL_RGBA;
                        ext_format = GL.GL_RGBA;
                        break;

                    case TextureComponent.FORMAT_BGR:
                        int_format = GL.GL_BGR;
                        ext_format = GL.GL_BGR;
                        break;

                    case TextureComponent.FORMAT_BGRA:
                        int_format = GL.GL_BGRA;
                        ext_format = GL.GL_BGRA;
                        break;

                    case TextureComponent.FORMAT_INTENSITY_ALPHA:
                        int_format = GL.GL_LUMINANCE_ALPHA;
                        ext_format = GL.GL_LUMINANCE_ALPHA;
                        break;

                    case TextureComponent.FORMAT_SINGLE_COMPONENT:
                        switch(format)
                        {
                            case FORMAT_INTENSITY:
                                int_format = GL.GL_INTENSITY;
                                ext_format = GL.GL_LUMINANCE;
                                break;

                            case FORMAT_LUMINANCE:
                                int_format = GL.GL_LUMINANCE;
                                ext_format = GL.GL_LUMINANCE;
                                break;

                            case FORMAT_ALPHA:
                                int_format = GL.GL_ALPHA;
                                ext_format = GL.GL_ALPHA;
                        }
                        break;
                }

                gl.glTexImage3D(GL.GL_TEXTURE_3D,
                                i,
                                int_format,
                                width,
                                height,
                                depth,
                                0,
                                ext_format,
                                GL.GL_UNSIGNED_BYTE,
                                pixels);

                pixels.clear();
                pixels = null;
    // TODO: Do we want this?  We lose caching but it saves one copy of the texture
    //sources[0].clearData(i);

                if(width > 1)
                    width = width >> 1;

                if(height > 1)
                    height = height >> 1;

                if(depth > 1)
                    depth = depth >> 1;
            }
        }

        // Any updates? Do those now
        int num_updates = updateManagers[0].getNumUpdatesPending(gl);

        if(num_updates != 0)
        {
            TextureUpdateData[] tud = updateManagers[0].getUpdatesAndClear(gl);

            for(int i = 0; i < num_updates; i++)
            {
                tud[i].pixels.rewind();
                gl.glTexSubImage3D(GL.GL_TEXTURE_3D,
                                   tud[i].level,
                                   tud[i].x,
                                   tud[i].y,
                                   tud[i].z,
                                   tud[i].width,
                                   tud[i].height,
                                   tud[i].depth,
                                   tud[i].format,
                                   GL.GL_UNSIGNED_BYTE,
                                   tud[i].pixels);
            }
        }
    }

    /**
     * Restore all openGL state to the given drawable
     *
     * @param gl The gl context to draw with
     */
    public void postRender(GL gl)
    {
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Set the boundary handling for the T parameter.
     *
     * @param mode The new mode.
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setBoundaryModeR(int mode)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        boundaryModeR = mode;
        stateChanged.setAll(true);
    }

    /**
     * Get the current boundary handling for the R parameter.
     *
     * @return The current mode.
     */
    public int getBoundaryModeR()
    {
        return boundaryModeR;
    }

   /**
     * Set the boundary handling for the T parameter.
     *
     * @param mode The new mode.
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setBoundaryModeT(int mode)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        boundaryModeT = mode;
        stateChanged.setAll(true);
    }

    /**
     * Get the current boundary handling for the T parameter.
     *
     * @return The current mode.
     */
    public int getBoundaryModeT()
    {
        return boundaryModeT;
    }

    /**
     * Get the depth of the texture in pixels. If no image is set, this returns
     * -1.
     *
     * @return a number >= -1
     */
    public int getDepth()
    {
        return depth;
    }

    /**
     * Get the height of the texture in pixels. If no image is set, this returns
     * -1.
     *
     * @return a number >= -1
     */
    public int getHeight()
    {
        return height;
    }

    /**
     * Internal common initialisation method called during the constructor.
     * Should be called after any image setup is complete.
     */
    private void init()
    {
        boundaryModeT = BM_CLAMP;
        boundaryModeR = BM_CLAMP;

        if(numSources == 0)
            return;

        int num_mipmaps =
            (mipMapMode == MODE_BASE_LEVEL) ? 1 : sources[0].getNumLevels();

        for(int i = 0; i < numSources; i++)
        {
            int comp_format = sources[i].getFormat(0);
            int tex_format = GL.GL_RGB;

            switch(comp_format)
            {
                case TextureComponent.FORMAT_RGB:
                    tex_format = GL.GL_RGB;
                    break;

                case TextureComponent.FORMAT_RGBA:
                    tex_format = GL.GL_RGBA;
                    break;

                case TextureComponent.FORMAT_BGR:
                    tex_format = GL.GL_BGR;
                    break;

                case TextureComponent.FORMAT_BGRA:
                    tex_format = GL.GL_BGRA;
                    break;

                case TextureComponent.FORMAT_INTENSITY_ALPHA:
                    tex_format = GL.GL_LUMINANCE_ALPHA;
                    break;

                case TextureComponent.FORMAT_SINGLE_COMPONENT:
                    switch(format)
                    {
                        case FORMAT_INTENSITY:
                            tex_format = GL.GL_INTENSITY;
                            break;

                        case FORMAT_LUMINANCE:
                            tex_format = GL.GL_LUMINANCE;
                            break;

                        case FORMAT_ALPHA:
                            tex_format = GL.GL_ALPHA;
                    }
                    break;

                default:
            }

            updateManagers[i].setTextureFormat(tex_format);
           ((TextureComponent)sources[i]).addUpdateListener(updateManagers[i]);
        }
    }
}
