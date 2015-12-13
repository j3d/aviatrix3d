/*****************************************************************************
 *                   Yumetech, Inc Copyright (c) 2004 - 2006
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
import java.util.HashMap;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

// Local imports
import org.j3d.aviatrix3d.rendering.*;

/**
 * A single render target child of a {@link MRTOffscreenTexture2D} node.
 * <p>
 *
 * Due to the requirements for OpenGL 2.0, this will only work with frame buffer
 * objects and not Pbuffers.
 * <p>
 *
 * This texture is not directly instantiatable as it is a child of the multiple
 * render targets. It wraps a single render target for use later on by other
 * pieces of geometry or shaders.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 *  None
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 2.10 $
 */
public class MRTTexture2D extends Texture
    implements OffscreenRenderTargetRenderable
{
    /** The index in the list of render targets that this texture represents */
    private int renderTargetIndex;

    /** The height of the main texture. */
    protected int height;

    /** The boundary mode S value */
    private int boundaryModeT;

    /** Capabilities setup for this renderer */
    protected BufferSetupData bufferData;

    /** Maps the GL context to an already created PBuffer */
    private HashMap<Object, OffscreenBufferDescriptor> displayListMap;

    /**
     * Constructs an offscreen render target buffer. Class left as package
     * private construction to prevent external users overrriding this class
     *
     * @param width The width of the texture in pixels
     * @param height The height of the texture in pixels
     */
    MRTTexture2D(int width, int height, int targetId)
    {
        super(GL.GL_TEXTURE_2D);

        renderTargetIndex = targetId;
        this.height = height;
        this.width = width;

        numSources = 0;

        boundaryModeT = BM_CLAMP;
        displayListMap = new HashMap<Object, OffscreenBufferDescriptor>();
    }

    //---------------------------------------------------------------
    // Methods defined by Texture
    //---------------------------------------------------------------

    /**
     * Internal check to see if this texture has valid data. If it
     * doesn't then there is no point actually rendering it. Validity is
     * defined by whether there is a non-zero number of sources.
     *
     * @return true if the texture is valid for rendering
     */
    @Override
    boolean hasValidData()
    {
        return true;
    }

    /**
     * Set the images for this texture, overridden to provide an empty
     * implementation as this is handled by the FBO directly.
     *
     * @param mipMapMode Flag stating the type of texture mode to use
     * @param format Image format to use for grayscale images
     * @param texSources The source data to use, single for base level
     * @param num The valid number of images to use from the array
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
    @Override
    public int compareTo(Texture tex)
    {
        int res = super.compareTo(tex);
        if(res != 0)
            return res;

        MRTTexture2D o2d = (MRTTexture2D)tex;

        if(height != o2d.height)
            return height < o2d.height ? -1 : 1;

        if(boundaryModeS != o2d.boundaryModeS)
            return boundaryModeS < o2d.boundaryModeS ? -1 : 1;

        if(boundaryModeT != o2d.boundaryModeT)
            return boundaryModeT < o2d.boundaryModeT ? -1 : 1;


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

        if(!(tex instanceof MRTTexture2D))
            return false;

        MRTTexture2D o2d = (MRTTexture2D)tex;

        if((height != o2d.height) ||
           (boundaryModeS != o2d.boundaryModeS) ||
           (boundaryModeT != o2d.boundaryModeT))
            return false;

        return true;
    }

    //---------------------------------------------------------------
    // Methods defined by OffscreenRenderTargetRenderable
    //---------------------------------------------------------------

    /**
     * Get the height of the texture in pixels. If no image is set, this returns
     * -1.
     *
     * @return a number >= -1
     */
    @Override
    public int getHeight()
    {
        return height;
    }

    /**
     * Check to see if this is a child render target of a parent multiple
     * render target offscreen buffer. Returns true if it is.
     *
     * @return true if this is a child, false if the parent
     */
    @Override
    public boolean isChildRenderTarget()
    {
        return true;
    }

    /**
     * Get the requested buffer setup that describes this offscreen buffer. Only
     * called once when the buffer is first constructed.
     *
     * @return The requested capabilities of the buffer that needs to be created
     */
    @Override
    public BufferSetupData getBufferSetup()
    {
        return bufferData;
    }

    /**
     * Get the currently registered pBuffer for the given key object. If there
     * is no buffer registered for the current context, return null.
     *
     * @param obj The key used to register the buffer with
     * @return buffer The buffer instance to use here.
     */
    @Override
    public OffscreenBufferDescriptor getBuffer(Object obj)
    {
        return (OffscreenBufferDescriptor)displayListMap.get(obj);
    }

    /**
     * Register a pBuffer for a given key object.
     *
     * @param obj The key used to register the buffer with
     * @param buffer The buffer instance to use here.
     */
    @Override
    public void registerBuffer(Object obj, OffscreenBufferDescriptor buffer)
    {
        displayListMap.put(obj, buffer);
    }

    /**
     * Remove an already registered pBuffer for a given key object.
     *
     * @param obj The key used to register the buffer with
     */
    @Override
    public void unregisterBuffer(Object obj)
    {
        displayListMap.remove(obj);
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
        if(stateChanged.containsKey(gl) && !stateChanged.getState(gl))
            return;

        stateChanged.put(gl, false);

        gl.glTexParameteri(GL.GL_TEXTURE_2D,
                           GL.GL_TEXTURE_WRAP_S,
                           boundaryModeS);

        gl.glTexParameteri(GL.GL_TEXTURE_2D,
                           GL.GL_TEXTURE_WRAP_T,
                           boundaryModeT);
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

            // float[] val = new float[1];
            //gl.glGetFloatv(GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, val);
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
     * Request the index of this texture in the render target list. This
     * texture is never used for the primary buffer, so the index will always
     * be greater than one.
     *
     * @return a number greater than one
     */
    public int getRenderTargetIndex()
    {
        return renderTargetIndex;
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
     * Get the current boundary handling for the S parameter.
     *
     * @return The current mode.
     */
    public int getBoundaryModeT()
    {
        return boundaryModeT;
    }

    /**
     * Set the buffer data. Package private because this is only called by
     * the parent offscreen buffer.
     *
     * @param data The representation of the buffer state for this target
     */
    void setBufferData(BufferSetupData data)
    {
        bufferData = data;
    }

    /**
     * Set the texture format that the buffer is using. All MRT targets
     * should use the same texture format.
     *
     * @param fmt The format to use
     */
    void setFormat(int fmt)
    {
        format = fmt;
    }

    /**
     * Change the size of this texture. Unchecked operation because it assumes
     * the parent offscreen surface.
     *
     * @param w The new width of the buffer in pixels. Must be positive.
     * @param h The new height of the buffer in pixels. Must be positive.
     */
    void updateSize(int w, int h)
    {
        width = w;
        height = h;

        stateChanged.setAll(true);
    }
}
