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
import com.jogamp.opengl.*;

import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;

import com.jogamp.opengl.swt.GLCanvas;
import org.eclipse.swt.widgets.Composite;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.GraphicsRenderingCapabilities;
import org.j3d.aviatrix3d.GraphicsRenderingCapabilitiesChooser;
import org.j3d.aviatrix3d.output.graphics.*;

import org.j3d.aviatrix3d.rendering.OffscreenBufferRenderable;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsProfilingData;
import org.j3d.aviatrix3d.rendering.ProfilingData;

/**
 * Implementation of the most drawable surface, supporting stereo rendering
 * capabilities.
 * <p>
 *
 * This implementation of GraphicsOutputDevice renders to a normal GLCanvas instance
 * and provides pBuffer support as needed. Stereo support is provided based on
 * the underlying hardware capabilities in combination with the user requested
 * features.
 * <p>
 *
 * In implementing the alternate frame mode rendering, there's some odd
 * artifacts/bugs in the rendering process. What seems to be happening is if
 * we draw left and then right on the same canvas one after the other, the left
 * one always gets drawn, and the right one is drawn, with the left cleared very
 * quickly. The effect is that the right image is very prominent, but the left
 * is almost not seen at all.
 * <p>
 * So, for now the implementation uses an internal flag to draw to
 * alternate eyes on each alternate call to display() from the external
 * rendering thread. This really slows the renderer down from a frame rate
 * perspective, so make sure your rendering cycle time is halved if you are
 * using that to control frame rate.
 * <p>
 *
 * <b>Note:</b> The lightweight flag is ignored currently.
 *
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>unknownStereoPolicyMsg: Error message when an unknown stereo type is requested</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 3.8 $
 */
public class StereoSWTSurface extends BaseSWTSurface
{
    /** Message when the sterop policy doesn't have one of the required types */
    private static final String UNKNOWN_STEREO_PROP =
        "org.j3d.renderer.aviatrix3d.swt.output.StereoSWTSurface.unknownStereoPolicyMsg";

    /** Indicator of the current type of stereo rendering to use */
    private int stereoRenderType;

    /** Flag indicating if quadbuffered stereo is an option */
    private boolean quadBuffersAvailable;

    /** The current eye separation to use */
    private float eyeSeparation;

    /** The user requested capabilities for this canvas */
    private GLCapabilities requestedCapabilities;

    /** The user-defined capabilities chooser for this canvas */
    private GLCapabilitiesChooser requestedChooser;

    /** Flag saying whether we should generate light or heavyweight canvases */
    private boolean useLightweight;

    /** When running alternate frame mode, which frame are we on now? */
    private boolean renderLeftFrame;

    /** The parent component to use for this component */
    private Composite parentWidget;

    /** The SWT rendering style to use for the created component */
    private int swtStyle;

    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param parent The parent component that this surface uses for the canvas
     * @param style The SWT style bits to use on the created canvas
     * @param caps A set of required capabilities for this canvas.
     * @param policy The policy to currently use
     * @throws IllegalArgumentException The policy type is not one of the legal
     *    selections.
     */
    public StereoSWTSurface(Composite parent,
                            int style,
                            GraphicsRenderingCapabilities caps,
                            int policy)
    {
        this(parent, style, caps, null, null, policy);
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
     * @param policy The policy to currently use
     * @throws IllegalArgumentException The policy type is not one of the legal
     *    selections.
     */
    public StereoSWTSurface(Composite parent,
                            int style,
                            GraphicsRenderingCapabilities caps,
                            GraphicsRenderingCapabilitiesChooser chooser,
                            int policy)
    {
        this(parent, style, caps, chooser, null, policy);
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
     * @param policy The policy to currently use
     * @throws IllegalArgumentException The policy type is not one of the legal
     *    selections.
     */
    public StereoSWTSurface(Composite parent,
                            int style,
                            GraphicsRenderingCapabilities caps,
                            boolean lightweight,
                            int policy)
    {
        this(parent, style, caps, null, null, lightweight, policy);
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
     * @param policy The policy to currently use
     * @throws IllegalArgumentException The policy type is not one of the legal
     *    selections.
     */
    public StereoSWTSurface(Composite parent,
                            int style,
                            GraphicsRenderingCapabilities caps,
                            GraphicsRenderingCapabilitiesChooser chooser,
                            boolean lightweight,
                            int policy)
    {
        this(parent, style, caps, chooser, null, lightweight, policy);
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
     * @param policy The policy to currently use
     * @throws IllegalArgumentException The policy type is not one of the legal
     *    selections.
     */
    public StereoSWTSurface(Composite parent,
                            int style,
                            GraphicsRenderingCapabilities caps,
                            BaseSurface sharedWith,
                            int policy)
    {
        this(parent, style, caps, null, sharedWith, false, policy);
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
     * @param policy The policy to currently use
     * @throws IllegalArgumentException The policy type is not one of the legal
     *    selections.
     */
    public StereoSWTSurface(Composite parent,
                            int style,
                            GraphicsRenderingCapabilities caps,
                            GraphicsRenderingCapabilitiesChooser chooser,
                            BaseSurface sharedWith,
                            int policy)
    {
        this(parent, style, caps, chooser, sharedWith, false, policy);
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
     * @param policy The policy to currently use
     * @throws IllegalArgumentException The policy type is not one of the legal
     *    selections.
     */
    public StereoSWTSurface(Composite parent,
                            int style,
                            GraphicsRenderingCapabilities caps,
                            BaseSurface sharedWith,
                            boolean lightweight,
                            int policy)
    {
        this(parent, style, caps, null, sharedWith, lightweight, policy);
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
     * @param policy The policy to currently use
     * @throws IllegalArgumentException The policy type is not one of the legal
     *    selections.
     */
    public StereoSWTSurface(Composite parent,
                            int style,
                            GraphicsRenderingCapabilities caps,
                            GraphicsRenderingCapabilitiesChooser chooser,
                            BaseSurface sharedWith,
                            boolean lightweight,
                            int policy)
    {
        super(sharedWith);

        useLightweight = lightweight;
        parentWidget = parent;
        swtStyle = style;

        renderLeftFrame = true;
        quadBuffersAvailable = false;
        eyeSeparation = 0.001f;

        init(parent, style, caps, chooser);

        setStereoRenderingPolicy(policy);
   }

    //---------------------------------------------------------------
    // Methods defined by GraphicsOutputDevice
    //---------------------------------------------------------------

    @Override
    public boolean isStereoAvailable()
    {
        return true;
    }

    @Override
    public boolean isQuadStereoAvailable()
    {
        return quadBuffersAvailable;
    }

    @Override
    public void setStereoEyeSeparation(float sep)
    {
        eyeSeparation = (sep < 0) ? -sep : sep;

        if(canvasRenderer instanceof StereoRenderingProcessor)
        {
            StereoRenderingProcessor r =
                (StereoRenderingProcessor)canvasRenderer;
            r.setStereoEyeSeparation(sep);
        }
    }

    @Override
    public float getStereoEyeSeparation()
    {
        return eyeSeparation;
    }

    @Override
    public void setStereoRenderingPolicy(int policy)
    {
        switch(policy)
        {
            case NO_STEREO:
                canvasRenderer = new StandardRenderingProcessor(this);
                break;

            case QUAD_BUFFER_STEREO:
                canvasRenderer =
                    new QuadBufferStereoProcessor(this);
                StereoRenderingProcessor r = (StereoRenderingProcessor)canvasRenderer;
                r.setStereoEyeSeparation(eyeSeparation);
                break;

            case ALTERNATE_FRAME_STEREO:
                canvasRenderer = new SingleEyeStereoProcessor(this);
                r = (StereoRenderingProcessor)canvasRenderer;
                r.setStereoEyeSeparation(eyeSeparation);
                break;

            case TWO_CANVAS_STEREO:
				errorReporter.warningReport("Dual canvas stereo is not implemented yet", null);
                return;

            default:
				I18nManager intl_mgr = I18nManager.getManager();
				String msg_pattern = intl_mgr.getString(UNKNOWN_STEREO_PROP);

				Locale lcl = intl_mgr.getFoundLocale();

				NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

				Object[] msg_args = { policy };
				Format[] fmts = { n_fmt };
				MessageFormat msg_fmt =
					new MessageFormat(msg_pattern, lcl);
				msg_fmt.setFormats(fmts);
				String msg = msg_fmt.format(msg_args);

                throw new IllegalArgumentException(msg);
        }

        stereoRenderType = policy;
    }

    @Override
    public int getStereoRenderingPolicy()
    {
        return stereoRenderType;
    }

    @Override
    public Object getSurfaceObject()
    {
        // Since we know that the canvas is GLJPanel or GLCanvas, we can just
        // return the raw drawable here for casting.
        return canvas;
    }

    //---------------------------------------------------------------
    // Methods defined by BaseSWTSurface
    //---------------------------------------------------------------

    @Override
    protected void initCanvas(Composite parent, int style, GLCapabilities caps, GLCapabilitiesChooser chooser)
    {
        swtCanvas = new GLCanvas(parentWidget, swtStyle, requestedCapabilities, requestedChooser);
        swtCanvas.addControlListener(resizer);

        canvas = swtCanvas;
    }

    //---------------------------------------------------------------
    // Methods defined by BaseSurface
    //---------------------------------------------------------------

    @Override
    public boolean completeCanvasInitialisation(GL gl)
    {
        byte[] params = new byte[1];
        gl.glGetBooleanv(GL2.GL_STEREO, params, 0);

        quadBuffersAvailable = (params[0] == GL.GL_TRUE);

        return true;
    }

    @Override
    protected void preMainCanvasDraw()
    {
        switch(stereoRenderType)
        {
            case ALTERNATE_FRAME_STEREO:
                SingleEyeStereoProcessor r =
                    (SingleEyeStereoProcessor)canvasRenderer;


                r.setEyeToRender(renderLeftFrame);
                renderLeftFrame = !renderLeftFrame;

// TODO: I think this should be called twice in the same frame.  But it doesn't seem to work right
/*
                r.setEyeToRender(true);
                canvasRenderer.render();
                renderLeftFrame = !renderLeftFrame;

                if(count > 1)
                {
                    for(int i = 0; i < count && !terminate; i++)
                    {
                        if(surfaces[i] != null)
                            surfaces[i].display();
                    }
                }

                r.setEyeToRender(false);
                canvasRenderer.render();
*/
                break;

            default:
        }
    }
}
