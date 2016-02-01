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
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLCapabilitiesChooser;
import com.jogamp.opengl.GLContext;

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

        init(parent, style, caps, chooser);
    }

    //------------------------------------------------------------------------
    // Methods defined by KeyListener
    //------------------------------------------------------------------------

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

    @Override
    public void keyReleased(KeyEvent evt)
    {
    }


    //---------------------------------------------------------------
    // Methods defined by BaseSWTSurface
    //---------------------------------------------------------------

    @Override
    protected void initCanvas(Composite parent, int style, GLCapabilities caps, GLCapabilitiesChooser chooser)
    {
        swtCanvas = new GLCanvas(parent, style, caps, chooser);

        swtCanvas.addKeyListener(this);
        swtCanvas.addControlListener(resizer);

        canvas = swtCanvas;

        canvasRenderer = new DebugRenderingProcessor(this);
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
