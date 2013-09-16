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
 * Buffer descriptor that encapsulated FBOs.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 3.31 $
 */
class FBODescriptor extends BaseBufferDescriptor
{
    /** The FBO ID that we are wrapping */
    private int bufferId;

    /** The underlying depth buffer ID if one was asked for */
    private int depthBufferId;

    /** The underlying stencil buffer ID if one was asked for */
    private int stencilBufferId;

    /** The texture ID if a separate depth texture was asked for */
    private int depthTextureId;

    /** The internal format for the storage */
    private int internalFormat;

    /** Local and global context for this descriptor */
    private GLContext localContext;

    /**
     * A map from the draw buffer ID to the texture buffer ID. The index is the
     * render target index (always linear) and the value at that index is the
     * OpenGL texture ID.
     */
    private int[] mrtTextureIdMap;

    /** Matching set of child descriptors for the parent */
    private FBORenderTargetDescriptor[] childDescriptors;

    /** Matching child descriptors for the depth buffer */
    private FBORenderTargetDescriptor depthDescriptor;

    /**
     * Construct a new instance of the pbuffer descriptor.
     *
     * @param owner The renderable that we are wrapping
     */
    FBODescriptor(OffscreenBufferRenderable owner)
    {
        super(owner);
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
        localContext = parentContext;

        boolean ret_val = createFBO(localContext, false);

        initComplete = ret_val;

        return ret_val;
    }

    /**
     * Reinitialise this descriptor because the GL context has changed.
     */
    public void reinitialize()
    {
        createFBO(localContext, true);
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
        EnableState ret_val = EnableState.ENABLE_FAILED;

        GLContext cur_ctx = GLContext.getCurrent();

        // Do we have a current context for us already? If not, make it current
        // to draw to now.
        if(cur_ctx == context)
        {
            ret_val = EnableState.ENABLE_OK;
        }
        else
        {
            int status = localContext.makeCurrent();

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
        }

        if((ret_val == EnableState.ENABLE_OK) && (bufferId != 0))
        {
            GL gl = localContext.getGL();
            gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
            gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, bufferId);
            gl.glPushAttrib(GL.GL_VIEWPORT_BIT);
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
        if(bufferId != 0)
        {
            GL gl = localContext.getGL();
            gl.glPopAttrib();
            gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);
        }
    }

    /**
     * Bind the current buffer to this context now. Default implementation does
     * nothing. Override for the pbuffer render-to-texture-specific case.
     */
    public void bindBuffer(GLContext parentContext)
    {
        if(mrtTextureIdMap[0] != 0)
        {
            GL gl = parentContext.getGL();
            gl.glBindTexture(GL.GL_TEXTURE_2D, mrtTextureIdMap[0]);
        }
    }

    /**
     * Unbind the current buffer from this context now. Default implementation
     * does nothing. Override for the pbuffer render-to-texture-specific case.
     */
    public void unbindBuffer(GLContext parentContext)
    {
        if(mrtTextureIdMap[0] != 0)
        {
            GL gl = parentContext.getGL();
            gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
        }
    }

    /**
     * Finish rendering this buffer and copy it in to the destination texture.
     *
     * @param context The GL context this buffer comes from
     */
    public void swapBuffers(GLContext context)
    {
        // do nothing for the FBOs
    }

    /**
     * Resize the underlying textures, leaving the buffer ID intact.
     *
     * @param context The GL context this buffer comes from
     */
    public void resize(GLContext context)
    {
        if(bufferId == 0)
            return;

        int width = ownerRenderable.getWidth();
        int height = ownerRenderable.getHeight();

        // No point doing useless work
        if((width == bufferWidth) && (height == bufferHeight))
            return;

        delete(context);
        createFBO(context, true);
    }

    /**
     * Remove this buffer object from existance. Will delete the handle that
     * OpenGL has and turns it back to uninitialised.
     *
     * @param context The GL context this buffer comes from
     */
    public void delete(GLContext context)
    {
        if(bufferId == 0)
            return;

        GL gl = context.getGL();

        int[] tmp = { bufferId };
        gl.glDeleteFramebuffersEXT(1, tmp, 0);

        if(depthBufferId != 0)
        {
            tmp[0] = depthBufferId;
            gl.glDeleteRenderbuffersEXT(1, tmp, 0);
        }

        if(stencilBufferId != 0)
        {
            tmp[0] = stencilBufferId;
            gl.glDeleteRenderbuffersEXT(1, tmp, 0);
        }

        if(depthTextureId != 0)
        {
            tmp[0] = depthTextureId;
            gl.glDeleteTextures(1, tmp, 0);
        }

        gl.glDeleteTextures(mrtTextureIdMap.length, mrtTextureIdMap, 0);

        bufferId = 0;
        depthBufferId = 0;
        stencilBufferId = 0;
        depthTextureId = 0;
        mrtTextureIdMap = null;

        // Leave the child descriptors list alone, just in case this
        // is an update
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Get the buffer descriptor for the given render target index. If index
     * is zero, returns null as this object is instance 0.
     *
     * @param index The render target index for descriptors
     * @return the descriptor at that index
     */
    BaseBufferDescriptor getChildDescriptor(int index)
    {
        return childDescriptors[index];
    }

    /**
     * Get the buffer descriptor for the separate depth render target. If not
     * defined, returns null.
     *
     * @return the descriptor at that index
     */
    BaseBufferDescriptor getDepthDescriptor()
    {
        return depthDescriptor;
    }

    /**
     * Create a new FBO definition.
     *
     * @param context
     * @param update
     */
    private boolean createFBO(GLContext context, boolean update)
    {
        // FBOs don't support stencil buffers currently, so exit immediately if
        // the user has requestd them.
        BufferSetupData caps = ownerRenderable.getBufferSetup();

        int num_targets = caps.getNumRenderTargets();
        mrtTextureIdMap = new int[num_targets];

        if(!update)
            childDescriptors = new FBORenderTargetDescriptor[num_targets];

        GL gl = localContext.getGL();

        int width = ownerRenderable.getWidth();
        int height = ownerRenderable.getHeight();
        int ext_format = ownerRenderable.getFormat();
        int int_format = ext_format;
        int byte_format = GL.GL_UNSIGNED_BYTE;
        int depth_format;
        boolean is_depth_only_texture = false;
        boolean separate_depth = ownerRenderable.hasSeparateDepthRenderable();

        bufferWidth = width;
        bufferHeight = height;

        int[] depth_bits = new int[1];
        gl.glGetIntegerv(GL.GL_DEPTH_BITS, depth_bits, 0);

        if(depth_bits[0] == 16)
            depth_format = GL.GL_DEPTH_COMPONENT16_ARB;
        else
            depth_format = GL.GL_DEPTH_COMPONENT24_ARB;

        if(ext_format == GL.GL_DEPTH_COMPONENT)
        {
            int_format = depth_format;
            byte_format = GL.GL_UNSIGNED_INT;
            is_depth_only_texture = true;
        }

        // Look at the capabilities and add various buffers to it based on the
        // requested caps

        if(caps.useFloatingPointColorBuffer())
        {
            if(depth_bits[0] == 16)
                byte_format = GL.GL_HALF_FLOAT_ARB;
            else
                byte_format = GL.GL_FLOAT;
        }

        int[] tex_id = new int[num_targets];
        gl.glGenTextures(num_targets, tex_id, 0);

        mrtTextureIdMap[0] = tex_id[0];

        gl.glBindTexture(GL.GL_TEXTURE_2D, mrtTextureIdMap[0]);
        gl.glTexParameteri(GL.GL_TEXTURE_2D,
                           GL.GL_TEXTURE_WRAP_S,
                           GL.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL.GL_TEXTURE_2D,
                           GL.GL_TEXTURE_WRAP_T,
                           GL.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL.GL_TEXTURE_2D,
                           GL.GL_TEXTURE_MIN_FILTER,
                           GL.GL_NEAREST);
        gl.glTexParameteri(GL.GL_TEXTURE_2D,
                           GL.GL_TEXTURE_MAG_FILTER,
                           GL.GL_NEAREST);

        if(is_depth_only_texture)
        {
            gl.glTexParameteri(GL.GL_TEXTURE_2D,
                               GL.GL_DEPTH_TEXTURE_MODE,
                               GL.GL_LUMINANCE);
            gl.glTexParameteri(GL.GL_TEXTURE_2D,
                               GL.GL_TEXTURE_COMPARE_FUNC,
                               GL.GL_LEQUAL);
            gl.glTexParameteri(GL.GL_TEXTURE_2D,
                               GL.GL_TEXTURE_COMPARE_MODE,
                               GL.GL_COMPARE_R_TO_TEXTURE);
        }

        gl.glTexImage2D(GL.GL_TEXTURE_2D,
                        0,
                        int_format,
                        width,
                        height,
                        0,
                        ext_format,
                        byte_format,
                        null);

        if(!is_depth_only_texture)
        {
            for(int i = 1; i < num_targets; i++)
            {
                mrtTextureIdMap[i] = tex_id[i];

                gl.glBindTexture(GL.GL_TEXTURE_2D, mrtTextureIdMap[i]);
                gl.glTexParameteri(GL.GL_TEXTURE_2D,
                                   GL.GL_TEXTURE_WRAP_S,
                                   GL.GL_CLAMP_TO_EDGE);
                gl.glTexParameteri(GL.GL_TEXTURE_2D,
                                   GL.GL_TEXTURE_WRAP_T,
                                   GL.GL_CLAMP_TO_EDGE);
                gl.glTexParameteri(GL.GL_TEXTURE_2D,
                                   GL.GL_TEXTURE_MIN_FILTER,
                                   GL.GL_NEAREST);
                gl.glTexParameteri(GL.GL_TEXTURE_2D,
                                   GL.GL_TEXTURE_MAG_FILTER,
                                   GL.GL_NEAREST);

                gl.glTexImage2D(GL.GL_TEXTURE_2D,
                                0,
                                int_format,
                                width,
                                height,
                                0,
                                ext_format,
                                byte_format,
                                null);

                if(update)
                {
                    childDescriptors[i].updateTextureId(mrtTextureIdMap[i]);
                }
                else
                {
                    childDescriptors[i] =
                        new FBORenderTargetDescriptor(mrtTextureIdMap[i],
                                                      ownerRenderable);
                }
            }

            if(separate_depth)
            {
                tex_id = new int[1];
                gl.glGenTextures(1, tex_id, 0);

                depthTextureId = tex_id[0];

                gl.glBindTexture(GL.GL_TEXTURE_2D, depthTextureId);
                gl.glTexParameteri(GL.GL_TEXTURE_2D,
                                   GL.GL_TEXTURE_MIN_FILTER,
                                   GL.GL_NEAREST);
                gl.glTexParameteri(GL.GL_TEXTURE_2D,
                                   GL.GL_TEXTURE_MAG_FILTER,
                                   GL.GL_NEAREST);

                gl.glTexParameteri(GL.GL_TEXTURE_2D,
                                   GL.GL_DEPTH_TEXTURE_MODE,
                                   GL.GL_LUMINANCE);

                gl.glTexParameteri(GL.GL_TEXTURE_2D,
                                   GL.GL_TEXTURE_COMPARE_FUNC,
                                   GL.GL_LEQUAL);
                gl.glTexParameteri(GL.GL_TEXTURE_2D,
                                   GL.GL_TEXTURE_COMPARE_MODE,
                                   GL.GL_COMPARE_R_TO_TEXTURE);

                // Some tutorials seem to prefer GL_NONE for this.
                // Doesn't seem to make any difference
                //                   GL.GL_NONE);

                gl.glTexImage2D(GL.GL_TEXTURE_2D,
                                0,
                                depth_format,
                                width,
                                height,
                                0,
                                GL.GL_DEPTH_COMPONENT,
                                byte_format,
                                null);

                if(update)
                {
                    depthDescriptor.updateTextureId(depthTextureId);
                }
                else
                {
                    depthDescriptor =
                        new FBORenderTargetDescriptor(depthTextureId,
                                                      ownerRenderable);
                }
            }

            gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
        }

        if(!separate_depth && caps.getDepthBits() != 0)
        {
            int[] depth_id = new int[1];
            gl.glGenRenderbuffersEXT(1, depth_id, 0);
            depthBufferId = depth_id[0];

            gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, depthBufferId);
            gl.glRenderbufferStorageEXT(GL.GL_RENDERBUFFER_EXT,
                                        depth_format,
                                        width,
                                        height);
        }

        int[] tmp_id = new int[1];
        gl.glGenFramebuffersEXT(1, tmp_id, 0);
        bufferId = tmp_id[0];

        gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, bufferId);

        if(is_depth_only_texture)
        {
            gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT,
                                         GL.GL_DEPTH_ATTACHMENT_EXT,
                                         GL.GL_TEXTURE_2D,
                                         mrtTextureIdMap[0],
                                         0);
            gl.glDrawBuffer(GL.GL_NONE);
            gl.glReadBuffer(GL.GL_NONE);
        }
        else
        {
            if(caps.getStencilBits() != 0)
            {
                // Separate stencil buffers don't seem to work. Use the
                // packed with depth buffer format instead. This is left
                // in here for the time when separate buffers may work.

                // just in case they gave us no depth buffer bits, create a
                // buffer here for it.
                if(!separate_depth && depthBufferId == 0)
                {
                    int[] stencil_id = new int[1];
                    gl.glGenRenderbuffersEXT(1, stencil_id, 0);
                    depthBufferId = stencil_id[0];

                    gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT,
                                             depthBufferId);
                }

                gl.glRenderbufferStorageEXT(GL.GL_RENDERBUFFER_EXT,
                                            GL.GL_DEPTH_STENCIL_EXT,
                                            width,
                                            height);

                gl.glFramebufferRenderbufferEXT(GL.GL_FRAMEBUFFER_EXT,
                                                GL.GL_STENCIL_ATTACHMENT_EXT,
                                                GL.GL_RENDERBUFFER_EXT,
                                                depthBufferId);
            }

            for(int i = 0; i < num_targets; i++)
            {
                gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT,
                                             GL.GL_COLOR_ATTACHMENT0_EXT + i,
                                             GL.GL_TEXTURE_2D,
                                             mrtTextureIdMap[i],
                                             0);

            }

            if(separate_depth)
            {
                gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT,
                                             GL.GL_DEPTH_ATTACHMENT_EXT,
                                             GL.GL_TEXTURE_2D,
                                             depthTextureId,
                                             0);
            }
            else if(depthBufferId != 0)
            {
                gl.glFramebufferRenderbufferEXT(GL.GL_FRAMEBUFFER_EXT,
                                                GL.GL_DEPTH_ATTACHMENT_EXT,
                                                GL.GL_RENDERBUFFER_EXT,
                                                depthBufferId);
            }
        }

        int ok = gl.glCheckFramebufferStatusEXT(GL.GL_FRAMEBUFFER_EXT);

        if(gl instanceof TraceGL)
            checkFBOStatus(ok);

        boolean ret_val = (ok == GL.GL_FRAMEBUFFER_COMPLETE_EXT);

        // If something failed along the way, just kill it all now
        if(!ret_val)
            delete(localContext);
        else
        {
            if(num_targets > 1)
            {
                int[] buffers = new int[num_targets];

                for(int i = 0; i < num_targets; i++)
                    buffers[i] = GL.GL_COLOR_ATTACHMENT0_EXT + i;

                gl.glDrawBuffers(num_targets, buffers, 0);
            }

            if(caps.useFloatingPointColorBuffer() && caps.useUnclampedColorBuffer())
            {
                gl.glClampColorARB(GL.GL_CLAMP_VERTEX_COLOR_ARB, GL.GL_FALSE);
                gl.glClampColorARB(GL.GL_CLAMP_FRAGMENT_COLOR_ARB, GL.GL_FALSE);
            }

            gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);
        }

        return ret_val;
    }

    /**
     * Internal convenience method that can print out the FBO creation status
     * code that was generated.
     *
     * @param statusCode The value that came from the check status method
     */
    private void checkFBOStatus(int statusCode)
    {
		StringBuilder bldr = new StringBuilder("FBO creation status: ");

        switch(statusCode)
        {
            case GL.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT:
                bldr.append("incomplete attachment");
                break;

            case GL.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT:
                bldr.append("missing attachment");
                break;

            case GL.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT:
                bldr.append("incomplete dimensions");
                break;

            case GL.GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT:
                bldr.append("incomplete formats");
                break;

            case GL.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT:
                bldr.append("incomplete draw buffer");
                break;

            case GL.GL_FRAMEBUFFER_UNSUPPORTED_EXT:
                bldr.append("unsupported format(s)");
                break;

            default:
                bldr.append("Success! ");
        }

		errorReporter.messageReport(bldr.toString());
    }
}

