/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2006
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
import java.awt.Component;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.awt.GLJPanel;


// Local imports
import org.j3d.aviatrix3d.GraphicsRenderingCapabilities;
import org.j3d.aviatrix3d.GraphicsRenderingCapabilitiesChooser;

/**
 * Implementation of the most basic drawable surface, supporting the minimal
 * number of features.
 * <p>
 *
 * This implementation of GraphicsOutputDevice renders to a normal GLCanvas
 * or GLJPanel (depending on if "lightweight" is true) instance
 * and provides pBuffer support as needed. Stereo support is not provided and
 * all associated methods always indicate negative returns on query about
 * support.
 *
 * @author Justin Couch
 * @version $Revision: 3.12 $
 */
public class SimpleAWTSurface extends BaseAWTSurface
{
    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param caps A set of required capabilities for this canvas.
     */
    public SimpleAWTSurface(GraphicsRenderingCapabilities caps)
    {
        this(caps, null, null);
    }

    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     */
    public SimpleAWTSurface(GraphicsRenderingCapabilities caps, GraphicsRenderingCapabilitiesChooser chooser)
    {
        this(caps, chooser, null);
    }

    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param lightweight If true, uses a GLJPanel (lightweight) JComponent,
     *   otherwise a GLCanvas. Note that setting this to true could negatively
     *   impact performance.
     */
    public SimpleAWTSurface(GraphicsRenderingCapabilities caps, boolean lightweight)
    {
        this(caps, null, null, lightweight);
    }

    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     * @param lightweight If true, uses a GLJPanel (lightweight) JComponent,
     *   otherwise a GLCanvas. Note that setting this to true could negatively
     *   impact performance.
     */
    public SimpleAWTSurface(GraphicsRenderingCapabilities caps,
                            GraphicsRenderingCapabilitiesChooser chooser,
                            boolean lightweight)
    {
        this(caps, chooser, null, lightweight);
    }

    /**
     * Construct a surface shares it's GL context with the given surface. This
     * is useful for constructing multiple view displays of the same scene graph,
     * but from different viewing directions, such as in a CAD application.
     * <p>
     * If the sharedSurface parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param sharedSurface The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     */
    public SimpleAWTSurface(GraphicsRenderingCapabilities caps, BaseSurface sharedSurface)
    {
        this(caps, null, sharedSurface, false);
    }

    /**
     * Construct a surface shares it's GL context with the given surface. This
     * is useful for constructing multiple view displays of the same scene graph,
     * but from different viewing directions, such as in a CAD application.
     * <p>
     * If the sharedSurface parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     * @param sharedSurface The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     */
    public SimpleAWTSurface(GraphicsRenderingCapabilities caps,
                            GraphicsRenderingCapabilitiesChooser chooser,
                            BaseSurface sharedSurface)
    {
        this(caps, chooser, sharedSurface, false);
    }

    /**
     * Construct a surface shares it's GL context with the given surface. This
     * is useful for constructing multiple view displays of the same scene graph,
     * but from different viewing directions, such as in a CAD application.
     * <p>
     * If the sharedSurface parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param sharedSurface The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     * @param lightweight If true, uses a GLJPanel (lightweight) JComponent,
     *   otherwise a GLCanvas. Note that setting this to true could negatively
     *   impact performance.
     */
    public SimpleAWTSurface(GraphicsRenderingCapabilities caps,
                            BaseSurface sharedSurface,
                            boolean lightweight)
    {
        this(caps, null, sharedSurface, lightweight);
    }

    /**
     * Construct a surface shares it's GL context with the given surface. This
     * is useful for constructing multiple view displays of the same scene graph,
     * but from different viewing directions, such as in a CAD application.
     * <p>
     * If the sharedSurface parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     * @param sharedSurface The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     * @param lightweight If true, uses a GLJPanel (lightweight) JComponent,
     *   otherwise a GLCanvas. Note that setting this to true could negatively
     *   impact performance.
     */
    public SimpleAWTSurface(GraphicsRenderingCapabilities caps,
                            GraphicsRenderingCapabilitiesChooser chooser,
                            BaseSurface sharedSurface,
                            boolean lightweight)
    {
        super(sharedSurface, lightweight);

        init(caps, chooser);
    }

    //---------------------------------------------------------------
    // Methods defined by GraphicsOutputDevice
    //---------------------------------------------------------------

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
        // Since we know that the canvas is GLJPanel or GLCanvas, we can just
        // return the raw drawable here for casting.
        return canvas;
    }

    //---------------------------------------------------------------
    // Methods defined by BaseAWTSurface
    //---------------------------------------------------------------

    /**
     * Attempt to create a new lightweight canvas renderer now. This will only
     * be called whenever the user has signalled that this is a lightweight
     * renderer and we do not yet have a canvasRenderer instance created. If
     * this fails, silently exit. We'll attempt to do this next frame.
     *
     * @return true if this creation succeeded
     */
    @Override
    protected boolean createLightweightContext()
    {
        try
        {
            canvasContext = ((GLAutoDrawable)canvas).getContext();
        }
        catch(NullPointerException npe)
        {
            // This is unexpectedly thrown by the internals of the JOGL RI when
            // the surface has not yet been realised at the AWT level. Catch an
            // ignore, treating it as though context creation failed.
        }

        if(canvasContext != null)
        {
            ((GLJPanel)canvas).setAutoSwapBufferMode(false);
            canvasRenderer =
                new StandardRenderingProcessor(canvasContext, this);
            canvasDescriptor.setLocalContext(canvasContext);
            canvasRenderer.setOwnerBuffer(canvasDescriptor);
            return true;
        }

        return false;
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Common internal initialisation for the constructors.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     */
    private void init(GraphicsRenderingCapabilities caps, GraphicsRenderingCapabilitiesChooser chooser)
    {
        GLContext shared_context = null;

        if(sharedSurface != null)
            shared_context = sharedSurface.getGLContext();

        GLCapabilities jogl_caps = CapabilitiesUtils.convertCapabilities(caps, GLProfile.getDefault());
        GLCapabilitiesChooser jogl_chooser = chooser != null ? new CapabilityChooserWrapper(chooser) : null;

        if(lightweight)
        {
            canvas = new GLJPanel(jogl_caps, jogl_chooser, shared_context);

            // Don't fetch context here because the JOGL code doesn't
            // generate a valid context until the window has been drawn and
            // made visible the first time.
        }
        else
        {
            canvas = new GLCanvas(jogl_caps, jogl_chooser, shared_context, null);
            ((GLCanvas)canvas).setAutoSwapBufferMode(false);

            canvasContext = ((GLAutoDrawable)canvas).getContext();
            canvasRenderer =
                new StandardRenderingProcessor(canvasContext, this);
            canvasDescriptor.setLocalContext(canvasContext);
            canvasRenderer.setOwnerBuffer(canvasDescriptor);
        }


        GLAutoDrawable gld = (GLAutoDrawable)canvas;
        Component comp = (Component)canvas;

        AWTSurfaceMonitor mon = (AWTSurfaceMonitor)surfaceMonitor;

        comp.setIgnoreRepaint(true);
        comp.addComponentListener(resizer);
        comp.addHierarchyListener(resizer);
        comp.addComponentListener(mon);
        comp.addHierarchyListener(mon);
        gld.addGLEventListener(mon);

        init();
    }
}
