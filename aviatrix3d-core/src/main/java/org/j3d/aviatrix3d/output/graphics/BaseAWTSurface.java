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

import com.jogamp.nativewindow.AbstractGraphicsDevice;

// Local imports
import org.j3d.aviatrix3d.GraphicsRenderingCapabilities;
import org.j3d.aviatrix3d.GraphicsRenderingCapabilitiesChooser;
import org.j3d.aviatrix3d.rendering.ProfilingData;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsInstructions;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsRequestData;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsResizeListener;

import org.j3d.util.ErrorReporter;

import com.jogamp.opengl.awt.GLJPanel;

/**
 * Implementation of the most basic drawable surface extended to provide
 * AWT-specific features.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 3.7 $
 */
public abstract class BaseAWTSurface extends BaseSurface
{
    /** Handler for dealing with the AWT to our graphics resize handler */
    protected AWTResizeHandler resizer;

    /** Flag indicating if we're a lightweight surface or not */
    protected final boolean lightweight;

    /**
     * Construct a surface shares it's GL context with the given surface. This
     * is useful for constructing multiple view displays of the same scene graph,
     * but from different viewing directions, such as in a CAD application.
     * <p>
     * If the sharedWith parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     *
     * @param sharedWith The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     * @param lightweight If true, uses a GLJPanel (lightweight) JComponent,
     *   otherwise a GLCanvas. Note that setting this to true could negatively
     *   impact performance.
     */
    protected BaseAWTSurface(BaseSurface sharedWith, boolean lightweight)
    {
        super(sharedWith);

        resizer = new AWTResizeHandler();
        surfaceMonitor = new AWTSurfaceMonitor();

        this.lightweight = lightweight;
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
     * Update the list of items to be rendered to the current list. Draw them
     * at the next oppourtunity.
     *
     * @param otherData data to be processed before the rendering
     * @param commands The list of drawable surfaces to render
     * @param numValid The number of valid items in the array
     */
    @Override
    public void setDrawableObjects(GraphicsRequestData otherData,
                                   GraphicsInstructions[] commands,
                                   int numValid)
    {
        if(lightweight && canvasRenderer == null)
        {
            if(!createLightweightContext())
                return;
        }

        super.setDrawableObjects(otherData, commands, numValid);
    }

    /**
     * Swap the buffers now if the surface supports multiple buffer drawing.
     * For surfaces that don't support multiple buffers, this does nothing.
     */
    @Override
    public void swap()
    {
        if(lightweight && canvasRenderer == null)
            return;

        super.swap();

        if(lightweight)
            ((GLJPanel)canvas).repaint();
    }

    //---------------------------------------------------------------
    // Methods defined by OutputDevice
    //---------------------------------------------------------------

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
    @Override
    public boolean draw(ProfilingData profilingData)
    {
        long stime = System.nanoTime();

        if(lightweight && canvasRenderer == null)
        {
            if(!createLightweightContext())
                return false;
        }

        boolean ret_val = super.draw(profilingData);

        return ret_val;
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Attempt to create a new lightweight canvas renderer now. This will only
     * be called whenever the user has signalled that this is a lightweight
     * renderer and we do not yet have a canvasRenderer instance created. If
     * this fails, silently exit. We'll attempt to do this next frame.
     *
     * @return true if this creation succeeded
     */
    protected abstract boolean createLightweightContext();

    /**
     * Ask the final class to create the actual canvas now, based on the selected chooser and
     * caps that JOGL needs, rather than the AV3D-specific classes that the external caller
     * makes use of.
     *
     * @param caps The capabilities to select for
     * @param chooser The optional chooser that will help select the required capabilities
     */
    protected abstract void initCanvas(GLCapabilities caps, GLCapabilitiesChooser chooser);

    /**
     * Common internal initialisation for the constructors.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     */
    protected void init(GraphicsRenderingCapabilities caps, GraphicsRenderingCapabilitiesChooser chooser)
    {
        GLContext shared_context = null;

        if (sharedSurface != null)
            shared_context = sharedSurface.getGLContext();

        GLDrawableFactory fac = GLDrawableFactory.getDesktopFactory();
        AbstractGraphicsDevice screen_device = fac.getDefaultDevice();
        GLProfile selected_profile = GLProfile.get(screen_device, GLProfile.GL2);

        GLCapabilities jogl_caps = CapabilitiesUtils.convertCapabilities(caps, selected_profile);
        GLCapabilitiesChooser jogl_chooser = chooser != null ? new CapabilityChooserWrapper(chooser) : null;

        initCanvas(jogl_caps, jogl_chooser);
        init(screen_device, selected_profile);
    }
}
