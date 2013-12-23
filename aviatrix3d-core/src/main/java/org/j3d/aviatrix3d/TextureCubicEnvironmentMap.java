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

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

// Local imports
import org.j3d.aviatrix3d.iutil.TextureUpdateData;

/**
 * A specialist object that renders a cubic environment map from pre-built
 * sources.
 * <p>
 *
 * All textures must be square in size. If they are not square, then an error
 * is generated.
 * <p>
 *
 * This implementation does not handle dynamic cubic environment mapping, nor
 * mipmaps.
 *
 * @author Justin Couch
 * @version $Revision: 1.28 $
 */
public class TextureCubicEnvironmentMap extends Texture
    implements MultipassTextureDestination
{
    /** The texture belongs to the positive X axis */
    public static final int POSITIVE_X = 0;

    /** The texture belongs to the negative X axis */
    public static final int NEGATIVE_X = 1;

    /** The texture belongs to the positive Y axis */
    public static final int POSITIVE_Y = 2;

    /** The texture belongs to the negative Y axis */
    public static final int NEGATIVE_Y = 3;

    /** The texture belongs to the positive Z axis */
    public static final int POSITIVE_Z = 4;

    /** The texture belongs to the negative Z axis */
    public static final int NEGATIVE_Z = 5;

    /** Internal array indexing these constants to GL constants */
    private static final int[] TEXTURE_TARGETS =
    {
        GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X,
        GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
        GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Y,
        GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,
        GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Z,
        GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z
    };

    /** The height of the main texture. */
    protected int height;

    /** The boundary mode S value */
    protected int boundaryModeT;

    /** The buffer that multipass source should read from */
    protected int[] mpReadBuffer;

    /**
     * The offset for the multipass copy update per-mipmap.
     * [imageNum][mipmaplevel][x,y]
     */
    protected int[][][] mpOffsets;

    /** The current number of mp sources defined */
    protected int mpNumSources;

    /** The indices of the known MP sources */
    protected int[] mpSourceIndices;

    /**
     * Constructs a texture with default values.
     */
    public TextureCubicEnvironmentMap()
    {
        super(GL.GL_TEXTURE_CUBE_MAP, 6);

        sources = new TextureSource[6];
        height = -1;

        init();
    }

    /**
     * Constructs a texture with the given list of sources. The provided array
     * must be at least 6 items long, even if some are null. The order taken
     * for the sources must follow the constants defined for the image IDs.
     * MipMaps are not handled in this case.
     */
    public TextureCubicEnvironmentMap(TextureComponent2D[] srcImages)
    {
        super(GL.GL_TEXTURE_CUBE_MAP, 6);

        // TODO:
        // Need to set this properly and make sure that the sources are
        // square.
        sources = new TextureSource[6];
        height = -1;

        if(srcImages != null)
        {
            height = srcImages[0].getHeight();
            this.sources[0] = srcImages[0];
            this.sources[1] = srcImages[1];
            this.sources[2] = srcImages[2];
            this.sources[3] = srcImages[3];
            this.sources[4] = srcImages[4];
            this.sources[5] = srcImages[5];
            numSources = 6;
        }

        init();
    }

    //---------------------------------------------------------------
    // Methods defined by MultipassTextureDestination
    //---------------------------------------------------------------

    /**
     * Check to see how many multipass texture sources are actually set for.
     * this instance of the destination.
     *
     * @return The number of defined sources >= 0
     */
    @Override
    public int numMultipassSources()
    {
        return mpNumSources;
    }

    /**
     * Fetch all of the currently specified multipass texture sources in this
     * instance of the class. The values are copied into the arrays, along with
     * their corresponding image index and level for mipmapping. The array must
     * be at least as long as {@link #numMultipassSources()}.
     *
     * @param sources An array to copy the current sources into
     * @param images The indices of each of the source images
     */
    @Override
    public void getMultipassSources(MultipassTextureSource[] sources,
                                    int[] images)
    {
        if(mpNumSources == 0)
            return;

        for(int i = 0; i < mpNumSources; i++)
        {
            images[i] = mpSourceIndices[i];
            sources[i] = (MultipassTextureSource)sources[mpSourceIndices[i]];
        }
    }

    /**
     * The multipass source has completed rendering and the implemented class
     * should now copy the image data across now.
     *
     * @param gl The gl context to draw with
     *     * @param x The x offset in pixels to start the copy from
     * @param y The y offset in pixels to start the copy from
     * @param width The width in pixels of the texture that was rendered
     * @param height The height in pixels of the texture that was rendered
     * @param imgNum The index of the texture source to copy to
     * @param level The mipmap level that this corresponds to
     */
    @Override
    public void updateMultipassSource(GL2 gl,
                                      int x,
                                      int y,
                                      int width,
                                      int height,
                                      int imgNum,
                                      int level)
    {
        gl.glReadBuffer(mpReadBuffer[imgNum]);

        // Not handling mipmaps right now.
        Integer t_id = textureIdMap.get(gl);
        if(t_id == null)
        {
            int[] tex_id_tmp = new int[1];
            gl.glGenTextures(1, tex_id_tmp, 0);
            textureIdMap.put(gl, new Integer(tex_id_tmp[0]));

            gl.glBindTexture(textureType, tex_id_tmp[0]);

            int comp_format = sources[imgNum].getFormat(level);
            int int_format = GL.GL_RGB;

            switch(comp_format)
            {
                case TextureComponent.FORMAT_RGB:
                    int_format = GL.GL_RGB;
                    break;

                case TextureComponent.FORMAT_RGBA:
                    int_format = GL.GL_RGBA;
                    break;

                case TextureComponent.FORMAT_BGR:
                    int_format = GL2.GL_BGR;
                    break;

                case TextureComponent.FORMAT_BGRA:
                    int_format = GL.GL_BGRA;
                    break;


                case TextureComponent.FORMAT_INTENSITY_ALPHA:
                    int_format = GL.GL_LUMINANCE_ALPHA;
                    break;

                case TextureComponent.FORMAT_SINGLE_COMPONENT:
                    switch(format)
                    {
                        case FORMAT_INTENSITY:
                            int_format = GL.GL_LUMINANCE;
                            break;

                        case FORMAT_ALPHA:
                            int_format = GL.GL_ALPHA;
                    }
                    break;

                default:
            }

            gl.glCopyTexImage2D(TEXTURE_TARGETS[imgNum],
                                level,
                                int_format,
                                x,
                                y,
                                width,
                                height,
                                0);
        }
        else
        {
            gl.glBindTexture(textureType, t_id.intValue());
            gl.glCopyTexSubImage2D(TEXTURE_TARGETS[imgNum],
                                   level,
                                   mpOffsets[imgNum][level][0],
                                   mpOffsets[imgNum][level][1],
                                   x,
                                   y,
                                   width,
                                   height);
        }
    }

    /**
     * Set the buffer that this texture should read it's input from during the
     * update callback. For 2D textures the image number is ignored.
     *
     * @param imgNum The index of the image that this offset applies to
     * @param buffer The identifier of the buffer to read from
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    @Override
    public void setReadBuffer(int imgNum, int buffer)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        mpReadBuffer[imgNum] = buffer;
    }

    /**
     * Get the current read buffer that is being used. For 2D textures the
     * image number is ignored.
     *
     * @param imgNum The index of the image that this offset applies to
     * @return One of the buffer indicies.
     */
    @Override
    public int getReadBuffer(int imgNum)
    {
        return mpReadBuffer[imgNum];
    }

    /**
     * Set the offsets in this texture to use for update the sub image
     * update values. For 2D textures the image number is ignored.
     *
     * @param imgNum The index of the image that this offset applies to
     * @param xoffset The x offset in pixels to start the copy at
     * @param yoffset The y offset in pixels to start the copy at
     * @param level The mipmap level that this corresponds to
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    @Override
    public void setCopyOffset(int imgNum, int level, int xoffset, int yoffset)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        mpOffsets[imgNum][level][0] = xoffset;
        mpOffsets[imgNum][level][1] = yoffset;
    }

    /**
     * Get the current copy offset. The return values are copied into the
     * user provided array as [xoffset, yoffset].
     *
     * @param imgNum The index of the image that this offset applies to
     * @param level The mipmap level that this corresponds to
     * @param offsets An array to copy the values into
     */
    @Override
    public void getCopyOffset(int imgNum, int level, int[] offsets)
    {
        offsets[0] = mpOffsets[imgNum][level][0];
        offsets[1] = mpOffsets[imgNum][level][1];
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
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    @Override
    public void setSources(int mipMapMode,
                           int format,
                           TextureSource[] texSources,
                           int num)
        throws InvalidWriteTimingException
    {
        super.setSources(mipMapMode, format, texSources, num);

        // Check to see if the source is a multipass source or not.
        int last_mp = 0;
        for(int i = 0; i < num; i++)
        {
            if(texSources[i] instanceof MultipassTextureSource)
                mpSourceIndices[last_mp++] = i;
        }

        mpNumSources = last_mp;
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

            // boundary modes are ignored on Cube maps
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

            gl.glTexParameteri(GL.GL_TEXTURE_CUBE_MAP,
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

            gl.glTexParameteri(GL.GL_TEXTURE_CUBE_MAP,
                               GL.GL_TEXTURE_MIN_FILTER,
                               mode);

            if(anisotropicMode != ANISOTROPIC_MODE_NONE)
            {
                gl.glTexParameterf(GL.GL_TEXTURE_CUBE_MAP,
                                   GL.GL_TEXTURE_MAX_ANISOTROPY_EXT,
                                   anisotropicDegree);
            }

            gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);

            if(priority >= 0)
            {
                gl.glTexParameterf(GL2.GL_TEXTURE_3D,
                                   GL2.GL_TEXTURE_PRIORITY,
                                   priority);
            }

            if(borderColor != null)
            {
                gl.glTexParameterfv(GL2.GL_TEXTURE_3D,
                                    GL2.GL_TEXTURE_BORDER_COLOR,
                                    borderColor,
                                    0);
            }

            if(format == FORMAT_DEPTH_COMPONENT)
            {
                gl.glTexParameterf(GL2.GL_TEXTURE_3D,
                                   GL2.GL_DEPTH_TEXTURE_MODE,
                                   depthComponentMode);

                gl.glTexParameterf(GL2.GL_TEXTURE_3D,
                                   GL2.GL_TEXTURE_COMPARE_MODE,
                                   compareMode);

                gl.glTexParameterf(GL2.GL_TEXTURE_3D,
                                   GL2.GL_TEXTURE_COMPARE_FUNC,
                                   compareFunction);
            }
        }

        if(imageChanged.getState(gl))
        {
            imageChanged.put(gl, false);

            for(int i = 0; i < 6; i++)
            {
                if(sources[i] == null)
                    continue;

                int num_levels = sources[i].getNumLevels();

                for(int j = 0; j < num_levels; j++)
                {
                    TextureComponent2D tex = (TextureComponent2D)sources[i];
                    ByteBuffer pixels = tex.getData(j);
                    pixels.rewind();

                    int width = sources[i].getWidth();
                    int height = tex.getHeight();
                    int comp_format = sources[i].getFormat(j);
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
                                    int_format = GL.GL_LUMINANCE;
                                    ext_format = GL.GL_LUMINANCE;
                                    break;

                                case FORMAT_ALPHA:
                                    int_format = GL.GL_ALPHA;
                                    ext_format = GL.GL_ALPHA;
                            }
                            break;
                    }

                    gl.glTexImage2D(TEXTURE_TARGETS[i],
                                    0,
                                    int_format,
                                    width,
                                    height,
                                    j,
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
                }
            }
        }

        // Any updates? Do those now
        for(int i = 0; i < 6; i++)
        {
            int num_updates = updateManagers[i].getNumUpdatesPending(gl);

            if(num_updates != 0)
            {
                TextureUpdateData[] tud = updateManagers[i].getUpdatesAndClear(gl);

                for(int j = 0; j < num_updates; j++)
                {
                    tud[j].pixels.rewind();
                    gl.glTexSubImage2D(GL.GL_TEXTURE_2D,
                                       tud[j].level,
                                       tud[j].x,
                                       tud[j].y,
                                       tud[j].width,
                                       tud[j].height,
                                       tud[j].format,
                                       GL.GL_UNSIGNED_BYTE,
                                       tud[j].pixels);
                }
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
     * Set the boundary handling for the S parameter.
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
     * Get the current boundary handling for the S parameter.
     *
     * @return The current mode.
     */
    public int getBoundaryModeT()
    {
        return boundaryModeT;
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

        mpOffsets = new int[6][][];
        mpReadBuffer = new int[6];
        mpSourceIndices = new int[6];

        if(numSources == 0)
            return;

        int last_mp = 0;

        for(int i = 0; i < numSources; i++)
        {
            int num_mipmaps =
                (mipMapMode == MODE_BASE_LEVEL) ? 1 : sources[i].getNumLevels();

            if(!(sources[i] instanceof MultipassTextureSource))
            {
                mpOffsets[i] = new int[num_mipmaps][2];
                mpSourceIndices[last_mp++] = i;
            }
            else
            {
                for(int j = 0; j < num_mipmaps; j++)
                {
                    int comp_format = sources[i].getFormat(j);
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

        mpNumSources = last_mp;
    }

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

        TextureCubicEnvironmentMap cem = (TextureCubicEnvironmentMap)tex;

        if(height != cem.height)
            return height < cem.height ? -1 : 1;

        if(boundaryModeS != cem.boundaryModeS)
            return boundaryModeS < cem.boundaryModeS ? -1 : 1;

        if(boundaryModeT != cem.boundaryModeT)
            return boundaryModeT < cem.boundaryModeT ? -1 : 1;

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

        if(!(tex instanceof TextureCubicEnvironmentMap))
            return false;

        TextureCubicEnvironmentMap cem = (TextureCubicEnvironmentMap)tex;

        if((height != cem.height) ||
           (boundaryModeS != cem.boundaryModeS) ||
           (boundaryModeT != cem.boundaryModeT))
            return false;

        return true;
    }
}
