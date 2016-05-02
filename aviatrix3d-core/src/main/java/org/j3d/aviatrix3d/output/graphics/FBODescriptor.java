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
import com.jogamp.opengl.*;

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

    @Override
    public boolean initialise(GLContext parentContext)
    {
        localContext = parentContext;

        boolean ret_val = createFBO(false);

        initComplete = ret_val;

        return ret_val;
    }

    @Override
    public void enable(GLContext localContext)
        throws GLException
    {
        if(bufferId != 0)
        {
            GL base_gl = localContext.getGL();
            GL2 gl = base_gl.getGL2();

            gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
            gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, bufferId);
            gl.glPushAttrib(GL2.GL_VIEWPORT_BIT);
        }
    }

    @Override
    public void disable(GLContext context)
        throws GLException
    {
        if(bufferId != 0)
        {
            GL base_gl = localContext.getGL();
            GL2 gl = base_gl.getGL2();

            gl.glPopAttrib();
            gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
        }
    }

    @Override
    public void bindBuffer(GLContext parentContext)
    {
        if(mrtTextureIdMap[0] != 0)
        {
            GL gl = parentContext.getGL();
            gl.glBindTexture(GL.GL_TEXTURE_2D, mrtTextureIdMap[0]);
        }
    }

    @Override
    public void unbindBuffer(GLContext parentContext)
    {
        if(mrtTextureIdMap[0] != 0)
        {
            GL gl = parentContext.getGL();
            gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
        }
    }

    @Override
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
        createFBO(true);
    }

    @Override
    public void delete(GLContext context)
    {
        if(bufferId == 0)
            return;

        GL base_gl = context.getGL();
        GL2 gl = base_gl.getGL2();

        int[] tmp = { bufferId };
        gl.glDeleteFramebuffers(1, tmp, 0);

        if(depthBufferId != 0)
        {
            tmp[0] = depthBufferId;
            gl.glDeleteRenderbuffers(1, tmp, 0);
        }

        if(stencilBufferId != 0)
        {
            tmp[0] = stencilBufferId;
            gl.glDeleteRenderbuffers(1, tmp, 0);
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
     * @param update true if we are just updating the existing texture ID
     *    false to create a new one.
     */
    private boolean createFBO(boolean update)
    {
        // FBOs don't support stencil buffers currently, so exit immediately if
        // the user has requested them.
        BufferSetupData caps = ownerRenderable.getBufferSetup();

        int num_targets = caps.getNumRenderTargets();
        mrtTextureIdMap = new int[num_targets];

        if(!update)
            childDescriptors = new FBORenderTargetDescriptor[num_targets];

        GL base_gl = localContext.getGL();
        GL2 gl = base_gl.getGL2();

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
            depth_format = GL.GL_DEPTH_COMPONENT16;
        else
            depth_format = GL.GL_DEPTH_COMPONENT24;

        if(ext_format == GL2.GL_DEPTH_COMPONENT)
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
                byte_format = GL.GL_HALF_FLOAT;
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
                               GL2.GL_DEPTH_TEXTURE_MODE,
                               GL.GL_LUMINANCE);
            gl.glTexParameteri(GL.GL_TEXTURE_2D,
                               GL2.GL_TEXTURE_COMPARE_FUNC,
                               GL.GL_LEQUAL);
            gl.glTexParameteri(GL.GL_TEXTURE_2D,
                               GL2.GL_TEXTURE_COMPARE_MODE,
                               GL2.GL_COMPARE_R_TO_TEXTURE);
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
                                   GL2.GL_DEPTH_TEXTURE_MODE,
                                   GL.GL_LUMINANCE);

                gl.glTexParameteri(GL.GL_TEXTURE_2D,
                                   GL2.GL_TEXTURE_COMPARE_FUNC,
                                   GL.GL_LEQUAL);
                gl.glTexParameteri(GL.GL_TEXTURE_2D,
                                   GL2.GL_TEXTURE_COMPARE_MODE,
                                   GL2.GL_COMPARE_R_TO_TEXTURE);

                // Some tutorials seem to prefer GL_NONE for this.
                // Doesn't seem to make any difference
                //                   GL.GL_NONE);

                gl.glTexImage2D(GL.GL_TEXTURE_2D,
                                0,
                                depth_format,
                                width,
                                height,
                                0,
                                GL2.GL_DEPTH_COMPONENT,
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
            gl.glGenRenderbuffers(1, depth_id, 0);
            depthBufferId = depth_id[0];

            gl.glBindRenderbuffer(GL.GL_RENDERBUFFER, depthBufferId);
            gl.glRenderbufferStorage(GL.GL_RENDERBUFFER,
                                        depth_format,
                                        width,
                                        height);
        }

        int[] tmp_id = new int[1];
        gl.glGenFramebuffers(1, tmp_id, 0);
        bufferId = tmp_id[0];

        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, bufferId);

        if(is_depth_only_texture)
        {
            gl.glFramebufferTexture2D(GL.GL_FRAMEBUFFER,
                                      GL.GL_DEPTH_ATTACHMENT,
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
                    gl.glGenRenderbuffers(1, stencil_id, 0);
                    depthBufferId = stencil_id[0];

                    gl.glBindRenderbuffer(GL.GL_RENDERBUFFER,
                                             depthBufferId);
                }

                gl.glRenderbufferStorage(GL.GL_RENDERBUFFER,
                                         GL.GL_DEPTH_STENCIL,
                                         width,
                                         height);

                gl.glFramebufferRenderbuffer(GL.GL_FRAMEBUFFER,
                                             GL.GL_STENCIL_ATTACHMENT,
                                             GL.GL_RENDERBUFFER,
                                             depthBufferId);
            }

            for(int i = 0; i < num_targets; i++)
            {
                gl.glFramebufferTexture2D(GL.GL_FRAMEBUFFER,
                                             GL.GL_COLOR_ATTACHMENT0 + i,
                                             GL.GL_TEXTURE_2D,
                                             mrtTextureIdMap[i],
                                             0);

            }

            if(separate_depth)
            {
                gl.glFramebufferTexture2D(GL.GL_FRAMEBUFFER,
                                          GL.GL_DEPTH_ATTACHMENT,
                                          GL.GL_TEXTURE_2D,
                                          depthTextureId,
                                          0);
            }
            else if(depthBufferId != 0)
            {
                gl.glFramebufferRenderbuffer(GL.GL_FRAMEBUFFER,
                                             GL.GL_DEPTH_ATTACHMENT,
                                             GL.GL_RENDERBUFFER,
                                             depthBufferId);
            }
        }

        int ok = gl.glCheckFramebufferStatus(GL.GL_FRAMEBUFFER);

        checkFBOStatus(ok);

        boolean ret_val = (ok == GL.GL_FRAMEBUFFER_COMPLETE);

        // If something failed along the way, just kill it all now
        if(!ret_val)
            delete(localContext);
        else
        {
            if(num_targets > 1)
            {
                int[] buffers = new int[num_targets];

                for(int i = 0; i < num_targets; i++)
                    buffers[i] = GL.GL_COLOR_ATTACHMENT0 + i;

                gl.glDrawBuffers(num_targets, buffers, 0);
            }

            if(caps.useFloatingPointColorBuffer() && caps.useUnclampedColorBuffer())
            {
                gl.glClampColor(GL2.GL_CLAMP_VERTEX_COLOR, GL.GL_FALSE);
                gl.glClampColor(GL2.GL_CLAMP_FRAGMENT_COLOR, GL.GL_FALSE);
            }

            gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
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
            case GL.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
                bldr.append("incomplete attachment");
                break;

            case GL.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
                bldr.append("missing attachment");
                break;

            case GL.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS:
                bldr.append("incomplete dimensions");
                break;

            case GL.GL_FRAMEBUFFER_INCOMPLETE_FORMATS:
                bldr.append("incomplete formats");
                break;

            case GL2.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
                bldr.append("incomplete draw buffer");
                break;

            case GL.GL_FRAMEBUFFER_UNSUPPORTED:
                bldr.append("unsupported format(s)");
                break;

            default:
                bldr.append("Success! ");
        }

		errorReporter.messageReport(bldr.toString());
    }
}

