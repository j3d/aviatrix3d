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

package org.j3d.renderer.aviatrix3d.swt.output;

// External imports
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLCapabilitiesChooser;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLProfile;

import com.jogamp.opengl.swt.GLCanvas;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;

// Local imports
import org.j3d.aviatrix3d.GraphicsRenderingCapabilities;
import org.j3d.aviatrix3d.GraphicsRenderingCapabilitiesChooser;
import org.j3d.aviatrix3d.output.graphics.*;
import org.j3d.aviatrix3d.rendering.ProfilingData;

/**
 * Implementation of the surface using SWT that allows for single-shot
 * debugging output..
 *
 * <p>
 * To dump a set of GL trace, the 'd' key is mapped to dump the next frame.
 * <p>
 *
 * <b>Note:</b> The lightweight flag is ignored currently.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 3.10 $
 */
public class DebugSWTSurface extends BaseSWTSurface
    implements KeyListener
{
    /** Trigger to dump the next frame using the TraceGL class */
    private int dumpNextFrameCount;

    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param parent The parent component that this surface uses for the canvas
     * @param style The SWT style bits to use on the created canvas
     * @param caps A set of required capabilities for this canvas.
     */
    public DebugSWTSurface(Composite parent,
                            int style,
                            GraphicsRenderingCapabilities caps)
    {
        this(parent, style, caps, null, null);
    }

    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param parent The parent component that this surface uses for the canvas
     * @param style The SWT style bits to use on the created canvas
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     */
    public DebugSWTSurface(Composite parent,
                            int style,
                            GraphicsRenderingCapabilities caps,
                            GraphicsRenderingCapabilitiesChooser chooser)
    {
        this(parent, style, caps, chooser, null);
    }

    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param parent The parent component that this surface uses for the canvas
     * @param style The SWT style bits to use on the created canvas
     * @param caps A set of required capabilities for this canvas.
     * @param lightweight If true, uses a GLJPanel (lightweight) JComponent,
     *   otherwise a GLCanvas. Note that setting this to true could negatively
     *   impact performance.
     */
    public DebugSWTSurface(Composite parent,
                            int style,
                            GraphicsRenderingCapabilities caps,
                            boolean lightweight)
    {
        this(parent, style, caps, null, null, lightweight);
    }

    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param parent The parent component that this surface uses for the canvas
     * @param style The SWT style bits to use on the created canvas
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     * @param lightweight If true, uses a GLJPanel (lightweight) JComponent,
     *   otherwise a GLCanvas. Note that setting this to true could negatively
     *   impact performance.
     */
    public DebugSWTSurface(Composite parent,
                            int style,
                            GraphicsRenderingCapabilities caps,
                            GraphicsRenderingCapabilitiesChooser chooser,
                            boolean lightweight)
    {
        this(parent, style, caps, chooser, null, lightweight);
    }

    /**
     * Construct a surface shares it's GL context with the given surface. This
     * is useful for constructing multiple view displays of the same scene graph,
     * but from different viewing directions, such as in a CAD application.
     * <p>
     * If the sharedWith parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     *
     * @param parent The parent component that this surface uses for the canvas
     * @param style The SWT style bits to use on the created canvas
     * @param caps A set of required capabilities for this canvas.
     * @param sharedWith The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     */
    public DebugSWTSurface(Composite parent,
                            int style,
                            GraphicsRenderingCapabilities caps,
                            BaseSurface sharedWith)
    {
        this(parent, style, caps, null, sharedWith, false);
    }

    /**
     * Construct a surface shares it's GL context with the given surface. This
     * is useful for constructing multiple view displays of the same scene graph,
     * but from different viewing directions, such as in a CAD application.
     * <p>
     * If the sharedWith parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     *
     * @param parent The parent component that this surface uses for the canvas
     * @param style The SWT style bits to use on the created canvas
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     * @param sharedWith The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     */
    public DebugSWTSurface(Composite parent,
                            int style,
                            GraphicsRenderingCapabilities caps,
                            GraphicsRenderingCapabilitiesChooser chooser,
                            BaseSurface sharedWith)
    {
        this(parent, style, caps, chooser, sharedWith, false);
    }

    /**
     * Construct a surface shares it's GL context with the given surface. This
     * is useful for constructing multiple view displays of the same scene graph,
     * but from different viewing directions, such as in a CAD application.
     * <p>
     * If the sharedWith parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     *
     * @param parent The parent component that this surface uses for the canvas
     * @param style The SWT style bits to use on the created canvas
     * @param caps A set of required capabilities for this canvas.
     * @param sharedWith The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     * @param lightweight If true, uses a GLJPanel (lightweight) JComponent,
     *   otherwise a GLCanvas. Note that setting this to true could negatively
     *   impact performance.
     */
    public DebugSWTSurface(Composite parent,
                            int style,
                            GraphicsRenderingCapabilities caps,
                            BaseSurface sharedWith,
                            boolean lightweight)
    {
        this(parent, style, caps, null, sharedWith, lightweight);
    }

    /**
     * Construct a surface shares it's GL context with the given surface. This
     * is useful for constructing multiple view displays of the same scene graph,
     * but from different viewing directions, such as in a CAD application.
     * <p>
     * If the sharedWith parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     *
     * @param parent The parent component that this surface uses for the canvas
     * @param style The SWT style bits to use on the created canvas
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     * @param sharedWith The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     * @param lightweight If true, uses a GLJPanel (lightweight) JComponent,
     *   otherwise a GLCanvas. Note that setting this to true could negatively
     *   impact performance.
     */
    public DebugSWTSurface(Composite parent,
                            int style,
                            GraphicsRenderingCapabilities caps,
                            GraphicsRenderingCapabilitiesChooser chooser,
                            BaseSurface sharedWith,
                            boolean lightweight)
    {
        super(sharedWith);

        dumpNextFrameCount = 0;

        init(parent, style, caps, chooser, lightweight);
    }

    //------------------------------------------------------------------------
    // Methods defined by KeyListener
    //------------------------------------------------------------------------

    /**
     * Notification of a key press event. When the 'd' key is pressed, dump
     * the next frame to stdout.
     *
     * @param evt The key event that caused this method to be called
     */
    @Override
    public void keyPressed(KeyEvent evt)
    {
        if(evt.character == 'd')
        {
            ((DebugRenderingProcessor)canvasRenderer).traceNextFrames(1);

            for(int i = 0; i < numRenderables; i++)
            {
                if(renderableList[i] != null)
                {
                    RenderingProcessor rp = rendererMap.get(renderableList[i]);
                    ((DebugRenderingProcessor)rp).traceNextFrames(1);
                }
            }
        }
    }

    /**
     * Notification of a key release event. Does nothing for this
     * implementation.
     *
     * @param evt The key event that caused this method to be called
     */
    @Override
    public void keyReleased(KeyEvent evt)
    {
    }

    //---------------------------------------------------------------
    // Methods defined by BaseSurface
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
        boolean dumpNow = false;

        if(dumpNextFrameCount != 0)
        {
            dumpNextFrameCount--;
            dumpNow = true;
            errorReporter.messageReport("\n\n++++  Starting Frame  ++++\n");
        }

        boolean ret_val = super.draw(profilingData);

        if(dumpNow)
            errorReporter.messageReport("\n\n++++  Completed Frame  ++++\n");

        return ret_val;
    }

    /**
     * Overridden to provide instances of the debug rendering processor for
     * off screen textures.
     *
     * @param context The GLContext instance to wrap for this processor
     * @return The rendering processor instance to use
     */
    @Override
    protected RenderingProcessor createRenderingProcessor(GLContext context)
    {
        DebugRenderingProcessor proc =
            new DebugRenderingProcessor(context, this);

        int traces_left =
            ((DebugRenderingProcessor)canvasRenderer).getTraceCount();

        proc.traceNextFrames(traces_left);

        return proc;
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * For the next <i>count</count> rendered frames, output the GL state to
     * the standard output. If it is currently dumping, the number will be
     * reset to this value.
     *
     * @param count The number of frames to output the debug state.
     */
    public void traceNextFrames(int count)
    {
        dumpNextFrameCount = count;

        ((DebugRenderingProcessor)canvasRenderer).traceNextFrames(count);

        for(int i = 0; i < numRenderables; i++)
        {
            if(renderableList[i] != null)
            {
                RenderingProcessor rp = rendererMap.get(renderableList[i]);
                ((DebugRenderingProcessor)rp).traceNextFrames(count);
            }
        }
    }

    /**
     * Common internal initialisation for the constructors.
     *
     * @param parent The parent component that this surface uses for the canvas
     * @param style The SWT style bits to use on the created canvas
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     * @param lightweight If true, uses a GLJPanel (lightweight) JComponent,
     *   otherwise a GLCanvas. Note that setting this to true could negatively
     *   impact performance.
     */
    private void init(Composite parent,
                      int style,
                      GraphicsRenderingCapabilities caps,
                      GraphicsRenderingCapabilitiesChooser chooser,
                      boolean lightweight)
    {
        GLContext shared_context = null;

        if(sharedSurface != null)
            shared_context = sharedSurface.getGLContext();

        GLCapabilities jogl_caps = CapabilitiesUtils.convertCapabilities(caps, GLProfile.getDefault());
        GLCapabilitiesChooser jogl_chooser = chooser != null ? new CapabilityChooserWrapper(chooser) : null;

        swtCanvas = new GLCanvas(parent, style, jogl_caps, jogl_chooser, shared_context);

        swtCanvas.addKeyListener(this);
        swtCanvas.addControlListener(resizer);

        canvas = swtCanvas.getDelegatedDrawable();
        canvasContext = swtCanvas.getContext();

        canvasRenderer = new DebugRenderingProcessor(canvasContext, this);

        init();
    }
}
