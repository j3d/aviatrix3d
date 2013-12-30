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

package org.j3d.renderer.aviatrix3d.swt.output;

// External imports
import java.security.AccessController;
import java.security.PrivilegedAction;

// Local imports
import com.jogamp.opengl.swt.GLCanvas;
import org.j3d.aviatrix3d.output.graphics.BaseSurface;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsResizeListener;

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
 * @version $Revision: 3.4 $
 */
public class BaseSWTSurface extends BaseSurface
{
    /** Handler for dealing with the AWT to our graphics resize handler */
    protected SWTResizeHandler resizer;

    /** The SWT version of the OpenGL canvas */
    protected GLCanvas swtCanvas;

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
    public BaseSWTSurface(BaseSurface sharedWith)
    {
        super(sharedWith);

        resizer = new SWTResizeHandler();
        surfaceMonitor = new SWTSurfaceMonitor();
    }

    //---------------------------------------------------------------
    // Methods defined by GraphicsOutputDevice
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
     * Get the underlying object that this surface is rendered to. If it is a
     * screen display device, the surface can be one of AWT Component or
     * Swing JComponent. An off-screen buffer would be a form of AWT Image etc.
     *
     * @return The drawable surface representation
     */
    @Override
    public Object getSurfaceObject()
    {
        return swtCanvas;
    }
}
