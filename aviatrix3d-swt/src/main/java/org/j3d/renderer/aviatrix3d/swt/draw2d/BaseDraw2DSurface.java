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

package org.j3d.renderer.aviatrix3d.swt.draw2d;

// External imports
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.eclipse.swt.widgets.Display;

import org.j3d.opengl.swt.draw2d.GLFigure;

// Local imports
import org.j3d.aviatrix3d.output.graphics.BaseSurface;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsResizeListener;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsProfilingData;

import org.j3d.util.ErrorReporter;

/**
 * Extended base implementation of the basic drawable surface, but adding in
 * some SWT-specific features.
 * <p>
 *
 * <a href="http://www.eclipse.org/swt/">SWT</a> is an independent windowing
 * toolkit developed by IBM as part of the
 * <a href="http://www.eclipse.org/">Eclipse project</a>. It doesn't use AWT at
 * all. Note that to run this code we assume that you already have at least SWT
 * installed on your system, and probably even all of Eclipse.
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public abstract class BaseDraw2DSurface extends BaseSurface
{
    /** Handler for dealing with the AWT to our graphics resize handler */
    protected FigureResizeHandler resizer;

    /** The SWT version of the OpenGL canvas */
    protected GLFigure glFigure;

    /**
     * Static constructor to make sure that the right system property is set
     * for our SWT-specific factory.
     */
    static
    {
        AccessController.doPrivileged(
            new PrivilegedAction<Object>()
            {
                public Object run()
                {
                    System.setProperty("opengl.factory.class.name",
                                       "org.j3d.opengl.swt.SWTRIDrawableFactory");
                    return null;
                }
            }
        );
    }

    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param sharedWith The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     */
    protected BaseDraw2DSurface(BaseSurface sharedWith)
    {
        super(sharedWith);

        resizer = new FigureResizeHandler();
    }

    //---------------------------------------------------------------
    // Methods defined by GraphicsOutputDisplay
    //---------------------------------------------------------------

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the node's internals can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    @Override
    public void setErrorReporter(ErrorReporter reporter)
    {
        super.setErrorReporter(reporter);
        resizer.setErrorReporter(errorReporter);
    }

    /**
     * Add a resize listener instance to this surface. Duplicate listener
     * instance add requests are ignored, as are null values.
     *
     * @param l The new listener instance to add
     */
    @Override
    public void addGraphicsResizeListener(GraphicsResizeListener l)
    {
        resizer.addGraphicsResizeListener(l);
    }

    /**
     * Remove a resize listener from this surface. If the listener is not
     * currently registered the request is ignored.
     *
     * @param l The listener instance to remove
     */
    @Override
    public void removeGraphicsResizeListener(GraphicsResizeListener l)
    {
        resizer.removeGraphicsResizeListener(l);
    }

    /**
     * Swap the buffers now if the surface supports multiple buffer drawing.
     * For surfaces that don't support multiple buffers, this does nothing.
     */
    @Override
    public void swap()
    {
        super.swap();

        Display d = glFigure.getDisplay();

        if(terminate || d.isDisposed())
            return;

        d.asyncExec(new Runnable()
        {
            public void run()
            {
                if(!terminate)
                    glFigure.repaint();
            }
        });
    }


    //---------------------------------------------------------------
    // Methods defined by OutputDevice
    //---------------------------------------------------------------

    /**
     * Overrides the base class to prevent any singlethreaded optimisations
     * being performed. This instance is a no-op due to the pbuffer
     */
    @Override
    public void enableSingleThreaded(boolean state)
    {
    }

    /**
     * Get the underlying object that this surface is rendered to. This method
     * returns a {@link org.j3d.opengl.swt.draw2d.GLFigure} object.
     *
     * @return The drawable surface representation
     */
    @Override
    public Object getSurfaceObject()
    {
        return glFigure;
    }

    /**
     * Instruct the surface to draw the collected set of nodes now. The
     * registered view environment is used to draw to this surface. If no
     * view is registered, the surface is cleared and then this call is
     * exited. The drawing surface does not swap the buffers at this point.
     * <p>
     * The return value indicates success or failure in the ability to
     * render this frame. Typically it will indicate failure if the
     * underlying surface has been disposed of, either directly through the
     * calling of the method on this interface, or through an internal check
     * mechanism. If failure is indicated, then check to see if the surface has
     * been disposed of and discontinue rendering if it has.
     *
     * @param profilingData The timing and load data
     * @return true if the drawing succeeded, or false if not
     */
    public boolean draw(GraphicsProfilingData profilingData)
    {
        if(glFigure.isZeroSized())
            return false;
        else
            return super.draw(profilingData);
    }
}
