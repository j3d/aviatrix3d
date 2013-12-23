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

package org.j3d.aviatrix3d.rendering;

// External imports
import javax.media.opengl.GL;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;

// Local imports
// None

/**
 * Internal representation of the various states that a buffer can request for
 * its rendering.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 2.3 $
 */
public class BufferSetupData
{
    /** True when floating point colour buffers should be used */
    private boolean fpColorBuffer;

    /** True when we should disable the colour range clamping */
    private boolean unclampedColor;

    /** The number of bits of depth buffer precision */
    private int depthBits;

    /** Number of samples to use when FSAA is enabled. 0 disables it */
    private int fsaaSamples;

    /** Number of bits of precision for stencil buffers. 0 disables it */
    private int stencilBits;

    /** The number of render targets to use. Only applicable to FBOs */
    private int numRenderTargets;

    /** The index of the render target that this buffer represents */
    private int renderTargetIndex;

    /**
     * Create a default instance of the buffer setup data.
     */
    public BufferSetupData()
    {
        fpColorBuffer = false;
        unclampedColor = false;
        depthBits = 16;
        stencilBits = 0;
        fsaaSamples = 0;
    }

    /**
     * Set the enabled state of floating point colour buffers. Default is that
     * they are disabled.
     *
     * @param enable True to enable the use of colour buffers
     */
    public void enableFloatingPointColorBuffer(boolean enable)
    {
        fpColorBuffer = enable;
    }

    /**
     * Enable the use of floating point colour buffers rather than fixed point
     * of the default GL pipeline.
     *
     * @return true to use the floating point version
     */
    public boolean useFloatingPointColorBuffer()
    {
        return fpColorBuffer;
    }

    /**
     * Set the enabled state of clamped colour values. Default is that
     * they are disabled.
     *
     * @param enable True to remove the clamping of colour values in the output
     */
    public void enableUnclampedColorBuffer(boolean enable)
    {
        fpColorBuffer = enable;
    }

    /**
     * Enable the use of unclamped floating point colour buffers. Only used if
     * {@link #useFloatingPointColorBuffer} returns true. Ignored otherwise.
     * Defaults to returning false to mimic traditional GL fixed point pipeline
     * behaviour.
     *
     * @return true to use an unclamped colour buffer mode
     * @see <a href="http://www.opengl.org/registry/specs/ARB/color_buffer_float.txt">
	 *   ARB Color buffer spec</a>
     */
    public boolean useUnclampedColorBuffer()
    {
        return unclampedColor;
    }

    /**
     * Set the number of bits of precsions the depth buffer should have. Not sanity
     * checking is performed, but it should be a multiple of 8. If a value of 0 is
     * provided the depth buffer will be disabled for this buffer.
     *
     * @param depth The number of bits to use. Should be 0, 16, 24 or 32
     */
    public void setDepthBits(int depth)
    {
        depthBits = depth;
    }

    /**
     * Return the number of bits that should be used in the depth buffer
     * implementation. If the value is 0, do not enable the depth buffer for
     * this buffer. Default return value is 16 to match the most common usage
     * in OpenGL implementations.
     *
     * @return Zero or positive number
     */
    public int getDepthBits()
    {
        return depthBits;
    }

    /**
     * Set the number of bit planes to use in the stencil buffer. A value of 0
     * will disable the use of stencil buffers for this buffer. Typical value
     * should be a multiple of 8. Most implementations only provide 8 bits of
     * stencil depth. No sanity checking is performed on the requested depth.
     *
     * @param depth The stencil bit depth to use
     */
    public void setStencilBits(int depth)
    {
        stencilBits = depth;
    }

    /**
     * Return the number of bits that should be used in the stencil buffer
     * implementation. If the value is 0, do not enable the stencil buffer for
     * this buffer.
     *
     * @return Zero or positive number
     */
    public int getStencilBits()
    {
        return stencilBits;
    }

    /**
     * Set the number of samples that should be used for full screen antialiasing.
     * The default value of 0 disables FSAA.
     *
     * @param samples The number of samples to use. Should be a positive power
     *    of two or zero to disable
     */
    public void setNumAASamples(int samples)
    {
        fsaaSamples = samples;
    }

    /**
     * Return the number of samples that should be used for FSAA. If the number
     * is 0, don't use FSAA. The default value is 0.
     *
     * @return Zero or positive number
     */
    public int getNumAASamples()
    {
        return fsaaSamples;
    }

    /**
     * Set the number of render targets to create in the underlying buffer.
     *
     * @param count The number of targets to use
     */
    public void setNumRenderTargets(int count)
    {
        numRenderTargets = count;
    }

    /**
     * Return the number render targets that should be used for this buffer.
     *
     * @return Zero or positive number
     */
    public int getNumRenderTargets()
    {
        return numRenderTargets;
    }

    /**
     * Set the index of this buffer's render target in the underlying FBO.
     *
     * @param index The index of this render target
     */
    public void setRenderTargetIndex(int index)
    {
        renderTargetIndex = index;
    }

    /**
     * Return the index of the render target this represents
     *
     * @return Zero or positive number
     */
    public int getRenderTargetIndex()
    {
        return renderTargetIndex;
    }
}

