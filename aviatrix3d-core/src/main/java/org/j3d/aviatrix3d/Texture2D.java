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
 * Describes the 2D texture that can be applied to an object.
 * <p>
 *
 * This texture supports using multipass rendering as a source. By default the
 * read buffer is the back buffer. Copy offsets are set to zero.
 * <p>
 *
 * For the purposes of state sorting, the multipass data is not considered.
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.45 $
 */
public class Texture2D extends Texture
    implements MultipassTextureDestination
{
    /** The boundary mode S value */
    protected int boundaryModeT;

    /** The height of the main texture. */
    protected int height;

    /** The buffer that multipass source should read from */
    protected int mpReadBuffer;

    /** The offset for the multipass copy update per-mipmap. */
    protected int[][] mpOffsets;

    /** The current number of mp sources defined */
    protected int mpNumSources;

    /**
     * Constructs a texture with default values.
     */
    public Texture2D()
    {
        super(GL.GL_TEXTURE_2D);
        height = -1;
        numSources = 0;
        mpNumSources = 0;

        init();
    }

    /**
     * Constructs a texture using a single image. Mipmap mode is derived from
     * the number of levels provided in the component.
     *
     * @param format The format to render the single image as
     * @param singleImage The source image to use
     */
    public Texture2D(int format, TextureComponent2D singleImage)
    {
        super(GL.GL_TEXTURE_2D, 1);

        this.format = format;
        sources = new TextureSource[1];
        sources[0] = singleImage;
        numSources = 1;

        mipMapMode = singleImage.getNumLevels() == 1 ?
                     MODE_BASE_LEVEL :
                     MODE_MIPMAP;

        mpNumSources = (singleImage instanceof MultipassTextureSource) ? 1 : 0;

        width = singleImage.getWidth();
        height = singleImage.getHeight();

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

        sources[0] = (MultipassTextureSource)sources[0];
        images[0] = 0;
    }

    /**
     * The multipass source has completed rendering and the implemented class
     * should now copy the image data across now.
     *
     * @param gl The gl context to draw with
     * @param x The x offset in pixels to start the copy from
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
        gl.glReadBuffer(mpReadBuffer);

        // Not handling mipmaps right now.
        Integer t_id = (Integer)textureIdMap.get(gl);
        if(t_id == null)
        {
            int[] tex_id_tmp = new int[1];
            gl.glGenTextures(1, tex_id_tmp, 0);
            textureIdMap.put(gl, new Integer(tex_id_tmp[0]));

            gl.glBindTexture(textureType, tex_id_tmp[0]);

            int comp_format = sources[0].getFormat(level);
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

            gl.glCopyTexImage2D(GL.GL_TEXTURE_2D,
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
            gl.glCopyTexSubImage2D(GL.GL_TEXTURE_2D,
                                   level,
                                   mpOffsets[level][0],
                                   mpOffsets[level][1],
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

        mpReadBuffer = buffer;
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
        return mpReadBuffer;
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
     *   of the NodeUpdateListener callback method
     */
    @Override
    public void setCopyOffset(int imgNum, int level, int xoffset, int yoffset)
        throws InvalidWriteTimingException
    {
        mpOffsets[level][0] = xoffset;
        mpOffsets[level][1] = yoffset;
    }

    /**
     * Get the current copy offset. The return values are copied into the
     * user provided array as [xoffset, yoffset]. For 2D textures the image
     * number is ignored.
     *
     * @param imgNum The index of the image that this offset applies to
     * @param level The mipmap level that this corresponds to
     * @param offsets An array to copy the values into
     */
    @Override
    public void getCopyOffset(int imgNum, int level, int[] offsets)
    {
        offsets[0] = mpOffsets[level][0];
        offsets[1] = mpOffsets[level][1];
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

        if(num <= 0)
            return;

        // Check to see if the source is a multipass source or not.
        mpNumSources = (texSources[0] instanceof MultipassTextureSource) ? 1 : 0;

        if(sources[0] instanceof TextureComponent)
            height = ((TextureComponent2D)sources[0]).getHeight();
        else if(sources[0] instanceof MultipassTextureSource)
            height = ((MultipassTextureSource)sources[0]).getHeight();

        stateChanged.setAll(true);
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

            if (tex_id_tmp[0] == 0) {
                // 0 happens when we are not on the openGL context.  Not sure how
                // we get there but this is a reasonable response.
                return;
            }

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

            gl.glTexParameteri(GL.GL_TEXTURE_2D,
                               GL.GL_TEXTURE_WRAP_S,
                               boundaryModeS);

            gl.glTexParameteri(GL.GL_TEXTURE_2D,
                               GL.GL_TEXTURE_WRAP_T,
                               boundaryModeT);

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

            gl.glTexParameteri(GL.GL_TEXTURE_2D,
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
                    mode = (numSources > 1) ?
                           GL.GL_LINEAR_MIPMAP_LINEAR :
                           GL.GL_LINEAR;
                    break;
            }

            gl.glTexParameteri(GL.GL_TEXTURE_2D,
                               GL.GL_TEXTURE_MIN_FILTER,
                               mode);

            if(anisotropicMode != ANISOTROPIC_MODE_NONE)
            {
                gl.glTexParameterf(GL.GL_TEXTURE_2D,
                                   GL.GL_TEXTURE_MAX_ANISOTROPY_EXT,
                                   anisotropicDegree);
            }

            if(priority >= 0)
            {
                gl.glTexParameterf(GL.GL_TEXTURE_2D,
                                   GL2.GL_TEXTURE_PRIORITY,
                                   priority);
            }

            if(borderColor != null)
            {
                gl.glTexParameterfv(GL.GL_TEXTURE_2D,
                                    GL2.GL_TEXTURE_BORDER_COLOR,
                                    borderColor,
                                    0);
            }

            if(format == FORMAT_DEPTH_COMPONENT)
            {
                gl.glTexParameterf(GL.GL_TEXTURE_2D,
                                   GL2.GL_DEPTH_TEXTURE_MODE,
                                   depthComponentMode);

                gl.glTexParameterf(GL.GL_TEXTURE_2D,
                                   GL2.GL_TEXTURE_COMPARE_MODE,
                                   compareMode);

                gl.glTexParameterf(GL.GL_TEXTURE_2D,
                                   GL2.GL_TEXTURE_COMPARE_FUNC,
                                   compareFunction);
            }
        }

        if(imageChanged.getState(gl))
        {
            imageChanged.put(gl, false);

            TextureComponent2D tex_comp = (TextureComponent2D)sources[0];

            int img_count =
                (mipMapMode == MODE_BASE_LEVEL) ? 1 : tex_comp.getNumLevels();

            int width = tex_comp.getWidth();
            int height = tex_comp.getHeight();

            for(int i = 0; i < img_count; i++)
            {
                ByteBuffer pixels = tex_comp.getData(i);
                pixels.rewind();
                int comp_format = tex_comp.getFormat(i);
                int int_format = GL.GL_RGB;
                int ext_format = GL.GL_RGB;

                int num_comps = 0;

                switch(comp_format)
                {
                    case TextureComponent.FORMAT_RGB:
                        int_format = GL.GL_RGB;
                        ext_format = GL.GL_RGB;
                        num_comps = 3;
                        break;

                    case TextureComponent.FORMAT_RGBA:
                        int_format = GL.GL_RGBA;
                        ext_format = GL.GL_RGBA;
                        num_comps = 4;
                        break;

                    case TextureComponent.FORMAT_BGR:
                        int_format = GL2.GL_BGR;
                        ext_format = GL2.GL_BGR;
                        num_comps = 3;
                        break;

                    case TextureComponent.FORMAT_BGRA:
                        int_format = GL.GL_BGRA;
                        ext_format = GL.GL_BGRA;
                        num_comps = 4;
                        break;


                    case TextureComponent.FORMAT_INTENSITY_ALPHA:
                        int_format = GL.GL_LUMINANCE_ALPHA;
                        ext_format = GL.GL_LUMINANCE_ALPHA;
                        num_comps = 2;

                        break;

                    case TextureComponent.FORMAT_SINGLE_COMPONENT:
                        switch(format)
                        {
                            case FORMAT_INTENSITY:
                                int_format = GL2.GL_INTENSITY;
                                ext_format = GL.GL_LUMINANCE;
                                num_comps = 1;
                                break;

                            case FORMAT_LUMINANCE:
                                int_format = GL.GL_LUMINANCE;
                                ext_format = GL.GL_LUMINANCE;
                                num_comps = 1;
                                break;

                            case FORMAT_ALPHA:
                                int_format = GL.GL_ALPHA;
                                ext_format = GL.GL_ALPHA;
                                num_comps = 1;
                        }
                        break;

                    default:
                }

                gl.glTexImage2D(GL.GL_TEXTURE_2D,
                                i,
                                int_format,
                                width,
                                height,
                                0,
                                ext_format,
                                GL.GL_UNSIGNED_BYTE,
                                pixels);

                pixels.clear();
                pixels = null;
    // TODO: Do we want this?  We lose caching but it saves one copy of the texture
    // TODO: This also messes up multi-canvas stuff
//                tex_comp.clearData(i);

                if(width > 1)
                    width = width >> 1;

                if(height > 1)
                    height = height >> 1;
            }
// TODO: This saves a copy, but then we can't resend to the graphics card
// Doesn't seem to helping anymore?  Someone else holding a reference?
//            tex_comp.clearLocalData();

        }

        // Any updates? Do those now
        int num_updates = updateManagers[0].getNumUpdatesPending(gl);

        if(num_updates != 0)
        {
            TextureUpdateData[] tud = updateManagers[0].getUpdatesAndClear(gl);

            for(int i = 0; i < num_updates; i++)
            {
                tud[i].pixels.rewind();
                gl.glTexSubImage2D(GL.GL_TEXTURE_2D,
                                   tud[i].level,
                                   tud[i].x,
                                   tud[i].y,
                                   tud[i].width,
                                   tud[i].height,
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

        Texture2D t2d = (Texture2D)tex;

        if(height != t2d.height)
            return height < t2d.height ? -1 : 1;

        if(boundaryModeS != t2d.boundaryModeS)
            return boundaryModeS < t2d.boundaryModeS ? -1 : 1;

        if(boundaryModeT != t2d.boundaryModeT)
            return boundaryModeT < t2d.boundaryModeT ? -1 : 1;

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

        if(!(tex instanceof Texture2D))
            return false;

        Texture2D t2d = (Texture2D)tex;

        if((height != t2d.height) ||
           (boundaryModeS != t2d.boundaryModeS) ||
           (boundaryModeT != t2d.boundaryModeT))
            return false;

        return true;
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Set the boundary handling for the T parameter.
     *
     * @param mode The new mode.
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
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
        mpReadBuffer = GL.GL_BACK;
        boundaryModeT = BM_CLAMP;

        if(numSources == 0)
            return;

        int num_mipmaps =
            (mipMapMode == MODE_BASE_LEVEL) ? 1 : sources[0].getNumLevels();

        if((sources[0] instanceof MultipassTextureSource))
        {
            mpOffsets = new int[num_mipmaps][2];
        }
        else
        {
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
    // TODO:
    // Check this logic to see whether the manager is per component or
    // per level of mipmap.
                updateManagers[i].setTextureFormat(tex_format);
                ((TextureComponent)sources[i]).addUpdateListener(updateManagers[i]);
            }
        }
    }
}
