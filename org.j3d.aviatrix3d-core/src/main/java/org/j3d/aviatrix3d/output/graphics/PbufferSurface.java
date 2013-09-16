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
import javax.media.opengl.*;

import java.text.MessageFormat;
import java.util.Locale;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsResizeListener;

/**
 * Implementation of a drawable surface that only renders to an offscreen
 * pbuffer.
 * <p>
 *
 * <b>Resizing information</b>
 *
 * Pbuffers cannot be resized, so all queries to add and remove resize
 * listeners are silently ignored. <i>However</i>, we do something a little
 * different. When the listener add request is received, we <i>immediately</i>
 * call the listener with the size of this surface. This allows applications
 * that depend on getting some resize information (eg using our utility
 * {@link org.j3d.aviatrix3d.pipeline.graphics.ViewportResizeManager} class)
 * to still behave consistently.
 * <p>
 * Beyond this initial call, no resize events will ever be generated. Listeners
 * are never kept by the implementation.
 *
 * @author Justin Couch
 * @version $Revision: 3.7 $
 */
public class PbufferSurface extends BaseSurface
{
    /** Error message when the resize listener class we called barfs */
    private static final String RESIZE_CALLBACK_PROP =
        "org.j3d.aviatrix3d.output.graphics.PbufferSurface.resizeCallbackErr";

    /** Error message when JOGL can't create Pbuffers for us */
    private static final String NOT_AVAILABLE_PROP =
        "org.j3d.aviatrix3d.output.graphics.PbufferSurface.noPbufferMsg";

    /** The width of this surface in pixels */
    private final int surfaceWidth;

    /** The height of this surface in pixels */
    private final int surfaceHeight;

    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param width The width of the surface in pixels
     * @param height The height of the surface in pixels
     * @throws PbufferUnavailableException When JOGL tells us that pbuffers
     *    are not available on this platform or capabilities
     */
    public PbufferSurface(GLCapabilities caps, int width, int height)
        throws PbufferUnavailableException
    {
        this(caps, null, null, width, height);
    }

    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     * @param width The width of the surface in pixels
     * @param height The height of the surface in pixels
     * @throws PbufferUnavailableException When JOGL tells us that pbuffers
     *    are not available on this platform or capabilities
     */
    public PbufferSurface(GLCapabilities caps,
                          GLCapabilitiesChooser chooser,
                          int width,
                          int height)
        throws PbufferUnavailableException
    {
        this(caps, chooser, null, width, height);
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
     * @param width The width of the surface in pixels
     * @param height The height of the surface in pixels
     * @throws PbufferUnavailableException When JOGL tells us that pbuffers
     *    are not available on this platform or capabilities
     */
    public PbufferSurface(GLCapabilities caps,
                          BaseSurface sharedSurface,
                          int width,
                          int height)
        throws PbufferUnavailableException
    {
        this(caps, null, sharedSurface, width, height);
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
     * @param width The width of the surface in pixels
     * @param height The height of the surface in pixels
     * @throws PbufferUnavailableException When JOGL tells us that pbuffers
     *    are not available on this platform or capabilities
     */
    public PbufferSurface(GLCapabilities caps,
                          GLCapabilitiesChooser chooser,
                          BaseSurface sharedSurface,
                          int width,
                          int height)
        throws PbufferUnavailableException
    {
        super(sharedSurface);

        surfaceWidth = width;
        surfaceHeight = height;

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

    /**
     * Add a resize listener instance to this surface. Duplicate listener
     * instance add requests are ignored, as are null values.
     *
     * @param l The new listener instance to add
     */
    @Override
    public void addGraphicsResizeListener(GraphicsResizeListener l)
    {
        try
        {
            // Never resizes, so just call it once now and send through
            // the info to the end listener.
            l.graphicsDeviceResized(0, 0, surfaceWidth, surfaceHeight);
        }
        catch(Exception e)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(RESIZE_CALLBACK_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            Object[] msg_args = { l.getClass().getName() };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            String msg = msg_fmt.format(msg_args);
            errorReporter.errorReport(msg, e);
        }
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
        // Ignored
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
     * @throws PbufferUnavailableException When JOGL tells us that pbuffers
     *    are not available on this platform or capabilities
     */
    private void init(GLCapabilities caps, GLCapabilitiesChooser chooser)
        throws PbufferUnavailableException
    {
        GLContext shared_context = null;

        if(sharedSurface != null)
            shared_context = sharedSurface.getGLContext();

        GLDrawableFactory factory = GLDrawableFactory.getFactory();

        if(!factory.canCreateGLPbuffer())
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NOT_AVAILABLE_PROP);

            throw new PbufferUnavailableException(msg);
        }

        canvas = factory.createGLPbuffer(caps,
                                         chooser,
                                         surfaceWidth,
                                         surfaceHeight,
                                         shared_context);

        canvasContext = ((GLAutoDrawable)canvas).getContext();
        canvasRenderer =
            new StandardRenderingProcessor(canvasContext, this);

        //Jonathon:  Requires these two things to be setup
        //for thumbnail generator not to fall into a infinite loop
        canvasDescriptor.setLocalContext(canvasContext);
        canvasRenderer.setOwnerBuffer(canvasDescriptor);

        init();
    }
}
