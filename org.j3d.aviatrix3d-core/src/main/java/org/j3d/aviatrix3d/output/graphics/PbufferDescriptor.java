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

package org.j3d.aviatrix3d.output.graphics;

// External imports
import javax.media.opengl.*;

// Local imports
import org.j3d.aviatrix3d.rendering.BufferSetupData;
import org.j3d.aviatrix3d.rendering.OffscreenBufferRenderable;

/**
 * Buffer descriptor that encapsulated Pbuffers.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 3.11 $
 */
class PbufferDescriptor extends BaseBufferDescriptor
{
    /**
     * Flag to say if pbuffer render to texture is available on the platform
     * we are running on. Some platforms have broken support for the render to
     * texture capabilities. If this is set true then we just won't bother
     * trying to create a render-to-texture pbuffer, we'll just drop that
     * requirement and go with creating a separate texture and do the manual
     * copying here.
     */
    protected static boolean avoidPbufferTextures = false;

    /** The Pbuffer instance that we are wrapping */
    private GLPbuffer pbuffer;

    /**
     * Flag indicating whether the Pbuffer we created ended up with
     * Render to texture being enabled.
     */
    private boolean haveRTT;

    /**
     * If we don't have RTT then this is the ID of the texture that we will
     * be copying the image data to and then binding later on. If we have RTT
     * textures, then this will be left unassigned.
     */
    private int textureId;

    /** The local context instance, based on the type of pbuffer created */
    private GLContext localContext;

    /**
     * On some platforms, we have a delayed startup of the pbuffer internals.
     * This means we can't determine if RTT is enabled based on the chosen
     * capabilities or have a reference to a local context. This flag lets
     * us know whether we are in that situation and need to keep trying stuff
     * until we have a real set of capabilties to play with.
     */
    private boolean delayedPbufferInitActive;

    /**
     * Static constructor to look at some platform-specific information.
     */
    static
    {
        String val = System.getProperty("os.name");
        if(val.equalsIgnoreCase("Mac OS X"))
            avoidPbufferTextures = true;

    }

    /**
     * Construct a new instance of the pbuffer descriptor.
     *
     * @param owner The renderable that we are wrapping
     */
    PbufferDescriptor(OffscreenBufferRenderable owner)
    {
        super(owner);

        delayedPbufferInitActive = false;
    }

    //---------------------------------------------------------------
    // Methods defined by BaseBufferDescriptor
    //---------------------------------------------------------------

    /**
     * Initialise an instance of this buffer now within the given parent
     * context.
     *
     * @param parentContext The parent context to create the buffer in
     * @return boolean true if initialisation succeeded for this buffer
     *    false if it was not possible or had an error when creating this
     *    buffer type.
     */
    public boolean initialise(GLContext parentContext)
    {
        BufferSetupData setup_data = ownerRenderable.getBufferSetup();

        // Build up local caps as needed
        GLCapabilities caps = new GLCapabilities();
        caps.setDepthBits(setup_data.getDepthBits());
        caps.setStencilBits(setup_data.getStencilBits());

        int fsaa_bits = setup_data.getNumAASamples();
        if(fsaa_bits != 0)
        {
            caps.setSampleBuffers(true);
            caps.setNumSamples(fsaa_bits);
        }

        bufferWidth = ownerRenderable.getWidth();
        bufferHeight = ownerRenderable.getHeight();

        GLDrawableFactory fac = GLDrawableFactory.getFactory();
        if(!fac.canCreateGLPbuffer())
            return false;

        // If we're on platforms that have problems with RTT and the
        // end user has asked for it, deliberately remove it from the
        // requesing capabilities.
        if(avoidPbufferTextures && caps.getPbufferRenderToTexture())
        {
            caps = (GLCapabilities)caps.clone();
            caps.setPbufferRenderToTexture(false);
        }

        pbuffer = fac.createGLPbuffer(caps,
                                      null,
                                      bufferWidth,
                                      bufferHeight,
                                      parentContext);

        GLCapabilitiesImmutable real_caps = pbuffer.getChosenGLCapabilities();

        if(real_caps == null)
            delayedPbufferInitActive = true;
        else
        {
            haveRTT = real_caps.getPbufferRenderToTexture();
        }

        localContext = pbuffer.getContext();

//System.out.println("Contexts: ");
//System.out.println("  parent " + parentContext);
//System.out.println("  kid " + localContext);

        initComplete = true;
        return true;
    }

    /**
     * Reinitialise this descriptor because the GL context has changed.
     */
    public void reinitialize()
    {
        // TODO:
        // Do nothing for now. Need to work on this
    }

    /**
     * Bind the current buffer to this context now.
     */
    public void bindBuffer(GLContext parentContext)
    {
        if(delayedPbufferInitActive)
        {
            if(!checkIfPbufferInitComplete())
                return;
        }

        if(haveRTT)
            pbuffer.bindTexture();
        else
        {
//System.out.println("  bind buffers " + textureId + " ctx " + parentContext);
            GL gl = parentContext.getGL();

            gl.glBindTexture(GL.GL_TEXTURE_2D, textureId);
        }
    }

    /**
     * Unbind the current buffer from this context now.
     */
    public void unbindBuffer(GLContext parentContext)
    {
        if(delayedPbufferInitActive)
        {
            if(!checkIfPbufferInitComplete())
                return;
        }

        if(haveRTT)
            pbuffer.releaseTexture();
        else
        {
//System.out.println("  unbind buffers " + textureId + " ctx " + parentContext);
            GL gl = parentContext.getGL();
            gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
        }
    }

    /**
     * Finish rendering this buffer and copy it in to the destination texture.
     *
     * @param context The GL context this buffer comes from
     */
    public void swapBuffers(GLContext parentContext)
    {
        if(delayedPbufferInitActive)
        {
            if(!checkIfPbufferInitComplete())
                return;
        }

        // Only do a swap if we don't have render to texture pbuffers.
        if(haveRTT)
            return;

//System.out.println("  swap buffers " + textureId + " ctx " + localContext);
        GL gl = localContext.getGL();

        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textureId);

        // trying different ways of getting the depth info over
        gl.glCopyTexSubImage2D(GL.GL_TEXTURE_2D,
                               0,
                               0,
                               0,
                               0,
                               0,
                               bufferWidth,
                               bufferHeight);
    }

    /**
     * Fetch the local context for this buffer. If the buffer has not yet been
     * initialised, this will return null.
     *
     * @return The context for the buffer, or null
     */
    public GLContext getLocalContext()
    {
        return localContext;
    }

    /**
     * Enable this buffer for rendering to now. A buffer may fail to enable
     * depending on the state of the underlying buffer. The state object
     * describes the options available.
     *
     * @param context The GL context this buffer comes from
     * @return The state that the enabling departed in
     * @throws GLException Exception when something at the low-level went
     *    wrong.
     */
    public EnableState enable(GLContext context)
        throws GLException
    {
        int status = localContext.makeCurrent();
        EnableState ret_val = EnableState.ENABLE_FAILED;

        switch(status)
        {
            case GLContext.CONTEXT_CURRENT:
                ret_val = EnableState.ENABLE_OK;
                break;

            case GLContext.CONTEXT_CURRENT_NEW:
                ret_val = EnableState.ENABLE_REINIT;
                break;

            case GLContext.CONTEXT_NOT_CURRENT:
                ret_val = EnableState.ENABLE_FAILED;
                break;
        }

        if(delayedPbufferInitActive)
        {
            if(!checkIfPbufferInitComplete())
                return ret_val;
        }

        if((ret_val != EnableState.ENABLE_FAILED) && !haveRTT &&
           (textureId == 0))
        {
            GL gl = localContext.getGL();

            int[] tex_id = new int[1];
            gl.glGenTextures(1, tex_id, 0);
            textureId = tex_id[0];

            int int_format = GL.GL_RGBA;
            int ext_format = GL.GL_RGBA;

            switch(ownerRenderable.getFormat())
            {
                case OffscreenBufferRenderable.FORMAT_DEPTH_COMPONENT:
                    ext_format = GL2.GL_DEPTH_COMPONENT;

                    int[] depth_bits = new int[1];
                    gl.glGetIntegerv(GL.GL_DEPTH_BITS, depth_bits, 0);

                    if(depth_bits[0] == 16)
                        int_format = GL2.GL_DEPTH_COMPONENT16;
                    else
                        int_format = GL2.GL_DEPTH_COMPONENT24;
                    break;

                default:
                    int_format = ownerRenderable.getFormat();
                    ext_format = ownerRenderable.getFormat();
            }

            gl.glActiveTexture(GL.GL_TEXTURE0);
            gl.glBindTexture(GL.GL_TEXTURE_2D, textureId);

            gl.glTexImage2D(GL.GL_TEXTURE_2D,
                            0,
                            int_format,
                            bufferWidth,
                            bufferHeight,
                            0,
                            ext_format,
                            GL.GL_UNSIGNED_INT,
                            null);
        }

        return ret_val;
    }

    /**
     * This buffer is no longer eligable for rendering to now.
     *
     * @param context The GL context this buffer comes from
     * @throws GLException Exception when something at the low-level went
     *    wrong.
     */
    public void disable(GLContext context)
        throws GLException
    {
//System.out.println("  releasing " + localContext);
        localContext.release();
    }

    /**
     * Resize this pbuffer. Does nothing right now.
     *
     * @param context The GL context this buffer comes from
     */
    public void resize(GLContext context)
    {
        // does nothing now. Should probably resize correctly.
    }

    /**
     * Remove this buffer object from existance. Will delete the handle that
     * OpenGL has and turns it back to uninitialised.
     *
     * @param context The GL context this buffer comes from
     */
    public void delete(GLContext context)
    {
        pbuffer.destroy();
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Convenience method to see if we can now fetch the pbuffer's chosen
     * capabilities. If we can, set the rest of the pbuffer internal variables.
     *
     * @return true if the initialisation is now complete
     */
    private boolean checkIfPbufferInitComplete()
    {
        GLCapabilitiesImmutable real_caps = pbuffer.getChosenGLCapabilities();

        if(real_caps == null)
            return false;

        haveRTT = real_caps.getPbufferRenderToTexture();
        delayedPbufferInitActive = false;

        return true;
    }
}

