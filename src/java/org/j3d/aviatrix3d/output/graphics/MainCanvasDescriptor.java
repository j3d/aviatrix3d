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
import org.j3d.aviatrix3d.rendering.OffscreenBufferRenderable;

/**
 * Buffer descriptor that encapsulated the main onscreen canvas, rather than
 * any of the offscreen buffers.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 3.8 $
 */
class MainCanvasDescriptor extends BaseBufferDescriptor
{
    /** The main canvas GL context instance */
    private GLContext localContext;

    /**
     * Construct a new instance of the pbuffer descriptor.
     */
    MainCanvasDescriptor()
    {
        // No offscreen buffer renderable for the main canvas.
        super(null);
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
        initComplete = true;

        bufferWidth = ownerRenderable.getWidth();
        bufferHeight = ownerRenderable.getHeight();

        return true;
    }

    /**
     * Reinitialise this descriptor because the GL context has changed.
     */
    public void reinitialize()
    {
        // Do nothing for the main canvas.
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
            int status = context.makeCurrent();
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
        context.release();
    }

    /**
     * Bind the current buffer to this context now. Default implementation does
     * nothing. Override for the pbuffer render-to-texture-specific case.
     */
    public void bindBuffer(GLContext parentContext)
    {
    }

    /**
     * Unbind the current buffer from this context now. Default implementation
     * does nothing. Override for the pbuffer render-to-texture-specific case.
     */
    public void unbindBuffer(GLContext parentContext)
    {
    }

    /**
     * Finish rendering this buffer and copy it in to the destination texture.
     *
     * @param context The GL context this buffer comes from
     */
    public void swapBuffers(GLContext context)
    {
        GLDrawable drawable = context.getGLDrawable();
        drawable.swapBuffers();
    }

    /**
     * Resize this description. No need to do anything for the main canvas.
     *
     * @param context The GL context this buffer comes from
     */
    public void resize(GLContext context)
    {
        bufferWidth = ownerRenderable.getWidth();
        bufferHeight = ownerRenderable.getHeight();
    }

    /**
     * Remove this buffer object from existance. Will delete the handle that
     * OpenGL has and turns it back to uninitialised.
     *
     * @param context The GL context this buffer comes from
     */
    public void delete(GLContext context)
    {
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Once initialised, set the GLContext instance to this value.
     *
     * @param ctx The local context of the main canvas
     */
    void setLocalContext(GLContext ctx)
    {
        localContext = ctx;
    }
}

