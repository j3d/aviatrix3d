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
import org.j3d.aviatrix3d.rendering.OffscreenBufferRenderable;

/**
 * A child render target descriptor of a parent Buffer descriptor.
 * <p>
 * Used for setting up individual render targets of a multiple render target
 * system, allowing for each render target to act individually without
 * requiring the parent to be active, once the initial item has been created.
 *
 * @author Justin Couch
 * @version $Revision: 3.6 $
 */
class FBORenderTargetDescriptor extends BaseBufferDescriptor
{
    /** The underlying depth buffer ID if one was asked for */
    private int textureId;

    /** Local and global context for this descriptor */
    private GLContext localContext;

    /**
     * Construct a new instance of the pbuffer descriptor.
     *
     * @param texture The OpenGL ID of the texture that this child is
     *   wrapping
     * @param owner The renderable that we are wrapping
     */
    FBORenderTargetDescriptor(int texture, OffscreenBufferRenderable owner)
    {
        super(owner);
        textureId = texture;
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
    @Override
    public boolean initialise(GLContext parentContext)
    {
        localContext = parentContext;
        initComplete = true;
        // Do nothing in this case
        return true;
    }

    /**
     * Reinitialise this descriptor because the GL context has changed.
     */
    @Override
    public void reinitialize()
	{
		// do nothing in this case
	}

    /**
     * Fetch the local context for this buffer. If the buffer has not yet been
     * initialised, this will return null.
     *
     * @return The context for the buffer, or null
     */
    @Override
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
    @Override
    public EnableState enable(GLContext context)
        throws GLException
    {
        return EnableState.ENABLE_OK;
    }

    /**
     * This buffer is no longer eligable for rendering to now.
     *
     * @param context The GL context this buffer comes from
     * @throws GLException Exception when something at the low-level went
     *    wrong.
     */
    @Override
    public void disable(GLContext context)
        throws GLException
    {
    }

    /**
     * Bind the current buffer to this context now. Default implementation does
     * nothing. Override for the pbuffer render-to-texture-specific case.
     */
    @Override
    public void bindBuffer(GLContext parentContext)
    {
        if(textureId != 0)
        {
            GL gl = parentContext.getGL();
            gl.glBindTexture(GL.GL_TEXTURE_2D, textureId);
        }
    }

    /**
     * Unbind the current buffer from this context now. Default implementation
     * does nothing. Override for the pbuffer render-to-texture-specific case.
     */
    @Override
    public void unbindBuffer(GLContext parentContext)
    {
        if(textureId != 0)
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
    @Override
    public void swapBuffers(GLContext context)
    {
        // do nothing for the FBOs
    }

    /**
     * Resize this buffer object .
     *
     * @param context The GL context this buffer comes from
     */
    @Override
    public void resize(GLContext context)
    {
        // Do nothing for child render targets
    }

    /**
     * Remove this buffer object from existance. Will delete the handle that
     * OpenGL has and turns it back to uninitialised.
     *
     * @param context The GL context this buffer comes from
     */
    @Override
    public void delete(GLContext context)
    {
        // Do nothing for child render targets
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Update the texture ID to the new ID.
     */
    void updateTextureId(int id)
    {
        textureId = id;
    }
}

