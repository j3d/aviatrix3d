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
import javax.media.opengl.GL;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLException;

import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;

// Local imports
import org.j3d.aviatrix3d.rendering.OffscreenBufferDescriptor;
import org.j3d.aviatrix3d.rendering.OffscreenBufferRenderable;

/**
 * Common basic descriptor for all the different types of buffer
 * implementations.
 * <p>
 *
 * A descriptor at this level abstracts away most of the basic interactions that
 * rendering code needs to perform. Not all the methods need to do something,
 * as the different types of buffers will have varying implementation
 * requirements.
 * <p>
 *
 * <b>GLContext usage</b>
 * <p>
 *
 * Buffers have two GLContext scopes. The first is the GLContext instance
 * that this buffer is created under. The second GLContext is the local
 * context that renders within that buffer. For example, you have a main scene
 * with the parent context and then a Pbuffer will have its own separate
 * local context. However, the lines can become blurred when looking at other
 * buffer types, such as FBOs. In that case, the local and parent context are
 * the same reference. However, at the conceptual level, the buffer descriptor
 * makes this distinction.
 * <p>
 * Most methods will use the parent context as that is needed to operate
 * buffer control. Where needed, the end user code can extract the local
 * context.
 *
 * @author Justin Couch
 * @version $Revision: 3.6 $
 */
public abstract class BaseBufferDescriptor implements OffscreenBufferDescriptor
{
    /** Flag indicating the current initialisation state */
    protected boolean initComplete;

    /** The renderable object that we are representing as a buffer */
    protected OffscreenBufferRenderable ownerRenderable;

    /** The current width of the buffer in pixels */
    protected int bufferWidth;

    /** The current height of the buffer in pixels */
    protected int bufferHeight;

    /** Error reporter instance to use */
    protected ErrorReporter errorReporter;

    /**
     * Construct an instance of this class that will contain a buffer with the
     * requested capabilties.
     *
     * @param owner The renderable that we are wrapping
     */
    protected BaseBufferDescriptor(OffscreenBufferRenderable owner)
    {
        errorReporter = DefaultErrorReporter.getDefaultReporter();
        ownerRenderable = owner;
        initComplete = false;
    }

    /**
     * Check to see if this buffer has been initialised yet.
     *
     * @return True if it has been initialised, false for not
     */
    public boolean isInitialised()
    {
       return initComplete;
    }

    /**
     * Initialise an instance of this buffer now within the given parent
     * context.
     *
     * @param parentContext The parent context to create the buffer in
     * @return boolean true if initialisation succeeded for this buffer
     *    false if it was not possible or had an error when creating this
     *    buffer type.
     */
    public abstract boolean initialise(GLContext parentContext);

    /**
     * Reinitialise this descriptor because the GL context has changed.
     */
    public abstract void reinitialize();

    /**
     * Fetch the local context for this buffer. If the buffer has not yet been
     * initialised, this will return null.
     *
     * @return The context for the buffer, or null
     */
    public abstract GLContext getLocalContext();

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
    public abstract EnableState enable(GLContext context)
        throws GLException;

    /**
     * This buffer is no longer eligable for rendering to now.
     *
     * @param context The GL context this buffer comes from
     * @throws GLException Exception when something at the low-level went
     *    wrong.
     */
    public abstract void disable(GLContext context)
        throws GLException;

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
    public abstract void swapBuffers(GLContext context);

    /**
     * Resize the underlying textures, leaving the buffer ID intact.
     *
     * @param context The GL context this buffer comes from
     */
    public abstract void resize(GLContext context);

    /**
     * Remove this buffer object from existance. Will delete the handle that
     * OpenGL has and turns it back to uninitialised.
     *
     * @param context The GL context this buffer comes from
     */
    public abstract void delete(GLContext context);

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the node's internals can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter)
    {
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter = reporter;
    }
}

