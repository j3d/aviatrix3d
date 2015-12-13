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
import java.nio.ByteBuffer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

// Local imports
import org.j3d.aviatrix3d.iutil.TextureUpdateData;

/**
 * Describes the 1D texture properties of an object.
 *
 * @author Justin Couch
 * @version $Revision: 1.19 $
 */
public class Texture1D extends Texture
{
    /**
     * Constructs a texture with default values.
     */
    public Texture1D()
    {
        super(GL2.GL_TEXTURE_1D, 1);

        init();
    }

    /**
     * Constructs a texture using just a single image, thus setting it initially
     * as MODE_BASE_LEVEL.
     */
    public Texture1D(int format, TextureComponent1D singleImage)
    {
        super(GL2.GL_TEXTURE_1D, 1);

        this.format = format;
        sources = new TextureSource[1];
        sources[0] = singleImage;

        width = singleImage.getWidth();
        numSources = 1;

        init();
    }

    //---------------------------------------------------------------
    // Methods defined by Texture
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
    @Override
    public int compareTo(Texture tex)
    {
        int res = super.compareTo(tex);
        if(res != 0)
            return res;

        Texture1D t1d = (Texture1D)tex;

        if(boundaryModeS != t1d.boundaryModeS)
            return boundaryModeS < t1d.boundaryModeS ? -1 : 1;

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param tex The texture instance to be compared
     * @return true if the objects represent identical values
     */
    @Override
    public boolean equals(Texture tex)
    {
        if(!super.equals(tex))
            return false;

        if(!(tex instanceof Texture1D))
            return false;

        Texture1D t1d = (Texture1D)tex;

        return (boundaryModeS == t1d.boundaryModeS);
    }

    //---------------------------------------------------------------
    // Methods defined by ObjectRenderable
    //---------------------------------------------------------------

    /**
     * Issue ogl commands needed for this component
     *
     * @param gl The gl context to draw with
     */
    @Override
    public void render(GL2 gl)
    {
        if(numSources == 0)
            return;

        Integer t_id = (Integer)textureIdMap.get(gl);
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
            gl.glTexParameteri(GL2.GL_TEXTURE_1D,
                               GL.GL_TEXTURE_WRAP_S,
                               boundaryModeS);

            gl.glTexParameteri(GL.GL_TEXTURE_2D,
                               GL2.GL_GENERATE_MIPMAP,
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

            gl.glTexParameteri(GL2.GL_TEXTURE_1D,
                               GL.GL_TEXTURE_MAG_FILTER,
                               mode);

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

            gl.glTexParameteri(GL2.GL_TEXTURE_1D,
                               GL.GL_TEXTURE_MIN_FILTER,
                               mode);

            if(anisotropicMode != ANISOTROPIC_MODE_NONE)
            {
                gl.glTexParameterf(GL2.GL_TEXTURE_1D,
                                   GL.GL_TEXTURE_MAX_ANISOTROPY_EXT,
                                   anisotropicDegree);
            }

            if(priority >= 0)
            {
                gl.glTexParameterf(GL2.GL_TEXTURE_1D,
                                   GL2.GL_TEXTURE_PRIORITY,
                                   priority);
            }

            if(borderColor != null)
            {
                gl.glTexParameterfv(GL2.GL_TEXTURE_1D,
                                    GL2.GL_TEXTURE_BORDER_COLOR,
                                    borderColor,
                                    0);
            }

            if(format == FORMAT_DEPTH_COMPONENT)
            {
                gl.glTexParameterf(GL2.GL_TEXTURE_1D,
                                   GL2.GL_DEPTH_TEXTURE_MODE,
                                   depthComponentMode);

                gl.glTexParameterf(GL2.GL_TEXTURE_1D,
                                   GL2.GL_TEXTURE_COMPARE_MODE,
                                   compareMode);

                gl.glTexParameterf(GL2.GL_TEXTURE_1D,
                                   GL2.GL_TEXTURE_COMPARE_FUNC,
                                   compareFunction);
            }
        }

        if(imageChanged.getState(gl))
        {
            imageChanged.put(gl, false);

            int img_count = (mipMapMode == MODE_BASE_LEVEL) ? 1 : numSources;
            int width = sources[0].getWidth();

            for(int i = 0; i < img_count; i++)
            {
                ByteBuffer pixels = ((TextureComponent)sources[0]).getData(i);
                pixels.rewind();
                int comp_format = sources[0].getFormat(i);
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
                        int_format = GL2.GL_BGR;
                        ext_format = GL2.GL_BGR;
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
                                int_format = GL2.GL_INTENSITY;
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

                    default:
                }

                gl.glTexImage1D(GL2.GL_TEXTURE_1D,
                                i,
                                int_format,
                                width,
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
                gl.glTexSubImage1D(GL2.GL_TEXTURE_1D,
                                   tud[i].level,
                                   tud[i].x,
                                   tud[i].width,
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
    @Override
    public void postRender(GL2 gl)
    {
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Internal common initialisation method called during the constructor.
     * Should be called after any image setup is complete.
     */
    private void init()
    {
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
                    tex_format = GL2.GL_BGR;
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
                            tex_format = GL2.GL_INTENSITY;
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
