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

import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;

import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.awt.GLJPanel;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.GraphicsRenderingCapabilities;
import org.j3d.aviatrix3d.GraphicsRenderingCapabilitiesChooser;

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
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>unknownStereoPolicyMsg: Error message when an unknown stereo type is requested</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 3.13 $
 */
public class StereoAWTSurface extends BaseAWTSurface
{
    /** Message when the sterop policy doesn't have one of the required types */
    private static final String UNKNOWN_STEREO_PROP =
        "org.j3d.aviatrix3d.output.graphics.StereoAWTSurface.unknownStereoPolicyMsg";

    /** Indicator of the current type of stereo rendering to use */
    private int stereoRenderType;

    /** Flag indicating if quadbuffered stereo is an option */
    private boolean quadBuffersAvailable;

    /** The current eye separation to use */
    private float eyeSeparation;

    /** When running alternate frame mode, which frame are we on now? */
    private boolean renderLeftFrame;

    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param policy The policy to currently use
     * @throws IllegalArgumentException The policy type is not one of the legal
     *    selections.
     */
    public StereoAWTSurface(GraphicsRenderingCapabilities caps, int policy)
    {
        this(caps, null, null, false, policy);
    }

    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     * @param policy The policy to currently use
     * @throws IllegalArgumentException The policy type is not one of the legal
     *    selections.
     */
    public StereoAWTSurface(GraphicsRenderingCapabilities caps,
                            GraphicsRenderingCapabilitiesChooser chooser,
                            int policy)
    {
        this(caps, chooser, null, false, policy);
    }

    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param lightweight If true, uses a GLJPanel (lightweight) JComponent,
     *   otherwise a GLCanvas. Note that setting this to true could negatively
     *   impact performance.
     * @param policy The policy to currently use
     * @throws IllegalArgumentException The policy type is not one of the legal
     *    selections.
     */
    public StereoAWTSurface(GraphicsRenderingCapabilities caps,
                            boolean lightweight,
                            int policy)
    {
        this(caps, null, null, lightweight, policy);
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
     * @param policy The policy to currently use
     * @throws IllegalArgumentException The policy type is not one of the legal
     *    selections.
     */
    public StereoAWTSurface(GraphicsRenderingCapabilities caps,
                            GraphicsRenderingCapabilitiesChooser chooser,
                            boolean lightweight,
                            int policy)
    {
        this(caps, chooser, null, lightweight, policy);
    }

    /**
     * Construct a surface shares it's GL context with the given surface. This
     * is useful for constructing multiple view displays of the same scene graph,
     * but from different viewing directions, such as in a CAD application.
     * <p>
     * If the sharedWith parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param sharedWith The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     * @param policy The policy to currently use
     * @throws IllegalArgumentException The policy type is not one of the legal
     *    selections.
     */
    public StereoAWTSurface(GraphicsRenderingCapabilities caps,
                            BaseSurface sharedWith,
                            int policy)
    {
        this(caps, null, sharedWith, false, policy);
    }

    /**
     * Construct a surface shares it's GL context with the given surface. This
     * is useful for constructing multiple view displays of the same scene graph,
     * but from different viewing directions, such as in a CAD application.
     * <p>
     * If the sharedWith parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     * @param sharedWith The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     * @param policy The policy to currently use
     * @throws IllegalArgumentException The policy type is not one of the legal
     *    selections.
     */
    public StereoAWTSurface(GraphicsRenderingCapabilities caps,
                            GraphicsRenderingCapabilitiesChooser chooser,
                            BaseSurface sharedWith,
                            int policy)
    {
        this(caps, chooser, sharedWith, false, policy);
    }

    /**
     * Construct a surface shares it's GL context with the given surface. This
     * is useful for constructing multiple view displays of the same scene graph,
     * but from different viewing directions, such as in a CAD application.
     * <p>
     * If the sharedWith parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     *
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
    public StereoAWTSurface(GraphicsRenderingCapabilities caps,
                            BaseSurface sharedWith,
                            boolean lightweight,
                            int policy)
    {
        this(caps, null, sharedWith, lightweight, policy);
    }

    /**
     * Construct a surface shares it's GL context with the given surface. This
     * is useful for constructing multiple view displays of the same scene graph,
     * but from different viewing directions, such as in a CAD application.
     * <p>
     * If the sharedWith parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     *
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
    public StereoAWTSurface(GraphicsRenderingCapabilities caps,
                            GraphicsRenderingCapabilitiesChooser chooser,
                            BaseSurface sharedWith,
                            boolean lightweight,
                            int policy)
    {
        super(sharedWith, lightweight);

        renderLeftFrame = true;
        quadBuffersAvailable = false;
        eyeSeparation = 0.001f;

        init(caps, chooser);

        setStereoRenderingPolicy(policy);
    }

    //---------------------------------------------------------------
    // Methods defined by GraphicsOutputDevice
    //---------------------------------------------------------------

    /**
     * Check to see whether this surface supports stereo rendering. As this is
     * not known until after initialisation, this method will return false
     * until it can determine whether or not stereo is available.
     *
     * @return true Stereo support is currently available
     */
    @Override
    public boolean isStereoAvailable()
    {
        return true;
    }

    /**
     * Check to see whether this surface supports Quad buffer stereo rendering.
     * Quadbuffers uses the GL_BACK_LEFT and GL_BACK_RIGHT for rendering pairs
     * rather than drawing alternate frames to the same window.
     * <p>
     * As this is not known until after initialisation, this method will return
     * false until it can determine whether or not stereo is available.
     *
     * @return true Stereo support is currently available
     */
    @Override
    public boolean isQuadStereoAvailable()
    {
        return quadBuffersAvailable;
    }

    /**
     * Set the eye separation value when rendering stereo. The default value is
     * 0.33 for most applications. The absolute value of the separation is
     * always used. Ignored for this implementation.
     *
     * @param sep The amount of eye separation
     */
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

    /**
     * Get the current eye separation value - always returns 0.
     *
     * @return sep The amount of eye separation
     */
    @Override
    public float getStereoEyeSeparation()
    {
        return eyeSeparation;
    }

    /**
     * Set the rendering policy used when handling stereo. The policy must be
     * one of the _STEREO constants defined in this interface.
     *
     * @param policy The policy to currently use
     * @throws IllegalArgumentException The policy type is not one of the legal
     *    selections.
     */
    @Override
    public void setStereoRenderingPolicy(int policy)
    {
        stereoRenderType = policy;

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

        canvasRenderer.setOwnerBuffer(canvasDescriptor);
    }

    /**
     * Get the current stereo rendering policy in use. If not explicitly set by
     * the user, then it will default to <code>NO_STEREO</code>.
     *
     * @return One of the *_STEREO values
     */
    @Override
    public int getStereoRenderingPolicy()
    {
        return stereoRenderType;
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
        }
        else
        {
            canvas = new GLCanvas(caps, chooser, null);
        }

        canvas.addGLEventListener(this);
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
