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
        initComplete = true;
        // Do nothing in this case
        return true;
    }

    @Override
    public void enable(GLContext context) throws GLException
    {
        // do nothing in this case
    }

    @Override
    public void disable(GLContext context) throws GLException
    {
        // do nothing in this case
    }

    @Override
    public void bindBuffer(GLContext parentContext)
    {
        if(textureId != 0)
        {
            GL gl = parentContext.getGL();
            gl.glBindTexture(GL.GL_TEXTURE_2D, textureId);
        }
    }

    @Override
    public void unbindBuffer(GLContext parentContext)
    {
        if(textureId != 0)
        {
            GL gl = parentContext.getGL();
            gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
        }
    }

    @Override
    public void resize(GLContext context)
    {
        // Do nothing for child render targets
    }

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

