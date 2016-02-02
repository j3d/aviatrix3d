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
// None

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

    @Override
    public boolean initialise(GLContext parentContext)
    {
        initComplete = true;

        resize(parentContext);

        return true;
    }

    @Override
    public void reinitialize()
    {
        // Do nothing for the main canvas.
    }

    @Override
    public void enable(GLContext context) throws GLException
    {

    }

    @Override
    public void disable(GLContext context) throws GLException
    {

    }

    @Override
    public void bindBuffer(GLContext parentContext)
    {
    }

    @Override
    public void unbindBuffer(GLContext parentContext)
    {
    }

    @Override
    public void resize(GLContext context)
    {
        GLDrawable drawable = context.getGLDrawable();
        bufferWidth = drawable.getSurfaceWidth();
        bufferHeight = drawable.getSurfaceHeight();
    }

    @Override
    public void delete(GLContext context)
    {
    }
}

