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
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.awt.GLJPanel;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

// Local imports
import org.j3d.aviatrix3d.GraphicsRenderingCapabilities;
import org.j3d.aviatrix3d.GraphicsRenderingCapabilitiesChooser;
import org.j3d.aviatrix3d.rendering.ProfilingData;

/**
 * Implementation of drawable surface with the key mapping defined to allow the
 * GL trace to be debugged for a single frame.
 * <p>
 *
 * To dump a set of GL trace, the 'd' key is mapped to dump the next frame.
 * <p>
 * This implementation of GraphicsOutputDevice renders to a normal GLCanvas instance
 * and provides pBuffer support as needed. Stereo support is not provided and
 * all associated methods always indicate negative returns on query about
 * support.
 *
 * @author Justin Couch
 * @version $Revision: 3.19 $
 */
public class DebugAWTSurface extends BaseAWTSurface
    implements KeyListener
{
    /** Trigger to dump the next frame using the TraceGL class */
    private int dumpNextFrameCount;

    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param caps A set of required capabilities for this canvas.
     */
    public DebugAWTSurface(GraphicsRenderingCapabilities caps)
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
    public DebugAWTSurface(GraphicsRenderingCapabilities caps, GraphicsRenderingCapabilitiesChooser chooser)
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
    public DebugAWTSurface(GraphicsRenderingCapabilities caps, boolean lightweight)
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
    public DebugAWTSurface(GraphicsRenderingCapabilities caps,
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
    public DebugAWTSurface(GraphicsRenderingCapabilities caps, BaseSurface sharedSurface)
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
    public DebugAWTSurface(GraphicsRenderingCapabilities caps,
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
    public DebugAWTSurface(GraphicsRenderingCapabilities caps,
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
    public DebugAWTSurface(GraphicsRenderingCapabilities caps,
                           GraphicsRenderingCapabilitiesChooser chooser,
                           BaseSurface sharedSurface,
                           boolean lightweight)
    {
        super(sharedSurface, lightweight);

        dumpNextFrameCount = 0;

        init(caps, chooser);
    }

    //------------------------------------------------------------------------
    // Methods defined by KeyListener
    //------------------------------------------------------------------------

    @Override
    public void keyPressed(KeyEvent evt)
    {
        if(evt.getKeyCode() == KeyEvent.VK_D)
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

    @Override
    public void keyReleased(KeyEvent evt)
    {
    }

    @Override
    public void keyTyped(KeyEvent evt)
    {
    }

    //---------------------------------------------------------------
    // Methods defined by GraphicsOutputDevice
    //---------------------------------------------------------------

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

    @Override
    protected void initCanvas(GLCapabilities caps, GLCapabilitiesChooser chooser)
    {
        if(lightweight)
        {
            canvas = new GLJPanel(caps, chooser);

            // Don't fetch context here because the JOGL code doesn't
            // generate a valid context until the window has been drawn and
            // made visible the first time.
        }
        else
        {
            canvas = new GLCanvas(caps, chooser, null);
            canvas.setAutoSwapBufferMode(false);

            canvasRenderer = new DebugRenderingProcessor(this);
            canvasRenderer.setOwnerBuffer(canvasDescriptor);
        }

        Component comp = (Component)canvas;

        comp.setIgnoreRepaint(true);
        comp.addKeyListener(this);
        comp.addComponentListener(resizer);
        comp.addHierarchyListener(resizer);
        canvas.addGLEventListener(this);
    }

    //---------------------------------------------------------------
    // Methods defined by BaseSurface
    //---------------------------------------------------------------

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

    @Override
    protected RenderingProcessor createRenderingProcessor()
    {
        DebugRenderingProcessor proc = new DebugRenderingProcessor(this);

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
}
