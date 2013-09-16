/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2007
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

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;

import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;
import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.*;
import org.j3d.aviatrix3d.pipeline.graphics.*;

/**
 * Implementation of the most basic drawable surface, supporting the minimal
 * number of features that is to be used by other, more complete
 * implementations.
 * <p>
 *
 * This implementation of GraphicsOutputDevice renders to a normal GLCanvas
 * or GLJPanel (depending on if "lightweight" is true) instance
 * and provides pBuffer support as needed. Stereo support is not provided and
 * all associated methods always indicate negative returns on query about
 * support.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>makeCurrentFailMsg: Error message because the GL context isn't current</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 3.50 $
 */
public abstract class BaseSurface
    implements GraphicsOutputDevice
{
    /** Message when the GL context failed to initialise */
    private static final String FAILED_CONTEXT_PROP =
        "org.j3d.aviatrix3d.output.graphics.BaseSurface.makeCurrentFailMsg";

    /** Message when the GL context failed to initialise */
    private static final String TEST_CONTEXT_PROP =
        "org.j3d.aviatrix3d.output.graphics.BaseSurface.makeCurrentAttemptMsg";

    /** Message when the GL context failed to initialise */
    private static final String PASS_CONTEXT_PROP =
        "org.j3d.aviatrix3d.output.graphics.BaseSurface.makeCurrentSuccessMsg";

    /** The initial size of the children list */
    private static final int LIST_START_SIZE = 20;

    /** The real canvas that we draw to */
    protected GLDrawable canvas;

    /** The context of the main canvas */
    protected GLContext canvasContext;

    /**
     * Manager of surface status so that we know what to do with the
     * context (ie refresh it or makeCurrent again). This must be
     * initialised in the constructor.
     */
    protected SurfaceMonitor surfaceMonitor;

    /** Abstract representation of the underlying primary renderer */
    protected RenderingProcessor canvasRenderer;

    /** Buffer descriptor for the main canvas */
    protected MainCanvasDescriptor canvasDescriptor;

    /** Local storage of the nodes that need to be rendered */
    protected OffscreenBufferRenderable[] renderableList;

    /** Number of items in the renderable list */
    protected int numRenderables;

    /** Flag to say that init has been called on the canvas */
    protected boolean initComplete;

    /** List of extensions we need to check on at startup */
    private String[] extensionList;

    /** Number of extension types to check for */
    private int numExtensions;

    /** Maps from a ofscreen texture instance to the rendererProcessor for it */
    protected Map<OffscreenBufferRenderable, RenderingProcessor> rendererMap;

    /** Used to fetch clear colour values when setting up pBuffers */
    private float[] colourTmp;

    /** Flag indicating if this surface is shared with another */
    protected BaseSurface sharedSurface;

    /** Flag to say whether the underlying factory can create pbuffers */
    protected boolean canCreatePBuffers;

    /** Error reporter used to send out messages */
    protected ErrorReporter errorReporter;

    /** Flag indicating whether rendering should be stopped right now */
    protected boolean terminate;

    /** Single threaded rendering mode operation state. Defaults to false. */
    protected boolean singleThreaded;

    /**
     * Alpha Test value for the parts of transparent objects that should be
     * handled as opaque
     */
    protected float alphaCutoff;

    /**
     * Flag indicating if we should do single or two-pass rendering of
     * transparent objects.
     */
    protected boolean useTwoPassTransparent;

    /**
     * Flag indicating whether the subscene list has been updated and thus
     * requires rendering again. Used to avoid the rendering of subscenes in
     * the main loop if nothing in them changed since the last frame.
     */
    private boolean updatedSubscenes;

    /**
     * Construct a surface shares it's GL context with the given surface. This
     * is useful for constructing multiple view displays of the same scene
     * graph, but from different viewing directions, such as in a CAD
     * application.
     * <p>
     * If the sharedWith parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     *
     * @param sharedWith The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     */
    protected BaseSurface(BaseSurface sharedWith)
    {
        sharedSurface = sharedWith;
        terminate = false;
        singleThreaded = false;
        canvasDescriptor = new MainCanvasDescriptor();

        alphaCutoff = 1.0f;
        useTwoPassTransparent = false;
        updatedSubscenes = true;
    }

    //---------------------------------------------------------------
    // Methods defined by GraphicsOutputDevice
    //---------------------------------------------------------------

    /**
     * Check to see whether this surface supports stereo rendering - which is
     * does not. Always returns false. May be overridden by derived class to
     * provide a different answer.
     *
     * @return false Stereo is not available
     */
    @Override
    public boolean isStereoAvailable()
    {
        return false;
    }

    /**
     * Check to see whether this surface supports Quad buffer stereo rendering
     * - which it does not. Always returns false for this implementation. May
     * be overridden by derived class to provide a different answer.
     *
     * @return false The surface does not support stereo at all
     */
    @Override
    public boolean isQuadStereoAvailable()
    {
        return false;
    }

    /**
     * Set the eye separation value when rendering stereo. The default value is
     * 0.33 for most applications. The absolute value of the separation is
     * always used. Ignored for this implementation. May be overridden by
     * derived class to provide a different answer.
     *
     * @param sep The amount of eye separation
     */
    @Override
    public void setStereoEyeSeparation(float sep)
    {
    }

    /**
     * Get the current eye separation value - always returns 0. May be
     * overridden by derived class to provide a different answer.
     *
     * @return sep The amount of eye separation
     */
    @Override
    public float getStereoEyeSeparation()
    {
        return 0;
    }

    /**
     * Set the rendering policy used when handling stereo. The policy must be
     * one of the _STEREO constants defined in this interface. May be
     * overridden by derived class to provide a different answer.
     *
     * @param policy The policy to currently use
     * @throws IllegalArgumentException The policy type is not one of the legal
     *    selections.
     */
    @Override
    public void setStereoRenderingPolicy(int policy)
    {
    }

    /**
     * Get the current stereo rendering policy in use. If not explicitly set by
     * the user, then it will default to <code>NO_STEREO</code>. May be
     * overridden by derived class to provide a different answer.
     *
     * @return One of the *_STEREO values
     */
    @Override
    public int getStereoRenderingPolicy()
    {
        return NO_STEREO;
    }

    /**
     * Set the background colour that this surface should be cleared to before
     * the drawing step. Colours range from 0 to 1 in the normal manner.
     *
     * @param r The red component of the background clear colour
     * @param g The green component of the background clear colour
     * @param b The blue component of the background clear colour
     * @param a The alpha component of the background clear colour
     */
    @Override
    public void setClearColor(float r, float g, float b, float a)
    {
        canvasRenderer.setClearColor(r, g, b, a);
    }

    /**
     * Set whether we should always force a local colour clear before
     * beginning any drawing. If this is set to false, then we can assume that
     * there is at least one background floating around that we can use to
     * clear whatever was drawn in the previous frame, and so we can ignore the
     * glClear(GL.GL_COLOR_BUFFER_BIT) call. The default is set to true.
     *
     * @param state true if we should always locally clear first
     */
    @Override
    public void setColorClearNeeded(boolean state)
    {
        canvasRenderer.setColorClearNeeded(state);
    }

    /**
     * Enable or disable two pass rendering of transparent objects. By default
     * it is disabled. This flag applies to this surface and any offscreen
     * surfaces that are children of this surface (FBOs, PBuffers etc).
     *
     * @param state true if we should enable two pass rendering
     */
    @Override
    public void enableTwoPassTransparentRendering(boolean state)
    {
        useTwoPassTransparent = state;

        if(canvasRenderer != null)
            canvasRenderer.enableTwoPassTransparentRendering(state);

        for(int i = 0; i < numRenderables; i++)
        {
            if(renderableList[i] != null)
            {
                RenderingProcessor rp = rendererMap.get(renderableList[i]);
                rp.enableTwoPassTransparentRendering(state);
            }
        }
    }

    /**
     * Check the state of the two pass transprent rendering flag.
     *
     * @return true if two pass rendering of transparent objects is enabled
     */
    @Override
    public boolean isTwoPassTransparentEnabled()
    {
        return useTwoPassTransparent;
    }

    /**
     * If two pass rendering of transparent objects is enabled, this is the alpha
     * test value used when deciding what to render. The default value is 1.0. No
     * sanity checking is performed, but the value should be between [0,1].
     * <p>
     * This flag applies to this surface and any offscreen
     * surfaces that are children of this surface (FBOs, PBuffers etc).
     *
     * @param cutoff The alpha value at which to enable rendering
     */
    @Override
    public void setAlphaTestCutoff(float cutoff)
    {
        alphaCutoff = cutoff;

        if(canvasRenderer != null)
            canvasRenderer.setAlphaTestCutoff(cutoff);

        for(int i = 0; i < numRenderables; i++)
        {
            if(renderableList[i] != null)
            {
                RenderingProcessor rp = rendererMap.get(renderableList[i]);
                rp.setAlphaTestCutoff(cutoff);
            }
        }
    }

    /**
     * Get the current value of the alpha test cutoff number. Will always
     * return the currently set number regardless of the state of the
     * two pass rendering flag.
     *
     * @return The currently set cut off value
     */
    @Override
    public float getAlphaTestCutoff()
    {
        return alphaCutoff;
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
        if(renderableList.length < numValid)
            renderableList = new OffscreenBufferRenderable[numValid];
        else
        {
            // Null out what is left of the array of nodes for GC
            for(int i = numValid; i < renderableList.length; i++)
                renderableList[i] = null;
        }

        numRenderables = numValid;

        for(int i = numRenderables; --i >= 0 ; )
        {
            GraphicsInstructions ri = commands[i];

            // Is this the main canvas or one of the offscreen drawables?
            if(ri.pbuffer != null)
            {
                OffscreenBufferRenderable tex = ri.pbuffer;
                RenderingProcessor rp = rendererMap.get(tex);

                if(rp == null)
                {
                    rp = createRenderingProcessor(canvasContext);
                    rp.enableSingleThreaded(singleThreaded);
                    rendererMap.put(tex, rp);

                    if(ri.parentSource == null)
                        canvasRenderer.addChildBuffer(tex, rp);
                    else
                    {
                        RenderingProcessor parent_rp =
                            rendererMap.get(ri.parentSource);
                        parent_rp.addChildBuffer(tex, rp);
                    }
                }
                else if(ri.pbuffer.hasBufferResized())
                {
                    if(ri.parentSource == null)
                        canvasRenderer.updateChildBuffer(tex);
                    else
                    {
                        RenderingProcessor parent_rp =
                            rendererMap.get(ri.parentSource);
                        parent_rp.updateChildBuffer(tex);
                    }
                }

                renderableList[i] = tex;

                // do we need to go find the original data specification
                // or use the local values?
                if(ri.copyOf != null)
                {
                    ri = (GraphicsInstructions)ri.copyOf;
                }

                tex.getClearColor(colourTmp);
                rp.setClearColor(colourTmp[0],
                                 colourTmp[1],
                                 colourTmp[2],
                                 colourTmp[3]);

                // NOTE:
                // Don't pass in otherData to the child renderables.
                // It is going to be created as part of this parent
                // renderable anyway, so we don't need to init this
                // multiple times.
                rp.setDrawableObjects(null,
                                      ri.renderList,
                                      ri.renderOps,
                                      ri.numValid,
                                      ri.renderData);
            }
            else
            {
                canvasRenderer.setDrawableObjects(otherData,
                                                  ri.renderList,
                                                  ri.renderOps,
                                                  ri.numValid,
                                                  ri.renderData);

                renderableList[i] = null;
            }
        }

        updatedSubscenes = true;
    }

    /**
     * Swap the buffers now if the surface supports multiple buffer drawing.
     * For surfaces that don't support multiple buffers, this does nothing.
     */
    @Override
    public void swap()
    {
        if(!terminate)
        {
            // JC:
            // Sometimes we manage to get JOGL into a weird state where it has
            // a blocking point internally with its threading model. At points
            // this will throw an InterruptedException from the internals. We
            // really don't care about that, so we just catch and ignore if we
            // happen to see that. Anything else may be important, so we wrap
            // and re-throw it as a RuntimeException.
            try
            {
                canvasRenderer.swapBuffers();
            }
            catch(GLException e)
            {
                if(!(e.getCause() instanceof InterruptedException))
                    throw e;
            }
        }
    }

    /**
     * Get the surface to VWorld transformation matrix.
     *
     * @param x The X coordinate in the entire surface
     * @param y The Y coordinate in the entire surface
     * @param layer The layer ID to fetch from. Layer 0 is the front-most
     * @param subLayer The ID of the viewport-layer that is needed. If there
     *   are no sub-layers, use 0.
     * @param matrix The matrix to copy into
     * @param deviceId A user-defined identifier for the requesting device when
     *    using the lastFound items
     * @param useLastFound Should we skip the search process and use the last
     *    data found for this layer/sublayer combo.
     *
     * @return Whether the coordinates where on the layer
     */
    @Override
    public boolean getSurfaceToVWorld(int x,
                                   int y,
                                   int layer,
                                   int subLayer,
                                   Matrix4f matrix,
                                   String deviceId,
                                   boolean useLastFound)
    {
        return canvasRenderer.getSurfaceToVWorld(x,
                                                 y,
                                                 layer,
                                                 subLayer,
                                                 matrix,
                                                 deviceId,
                                                 useLastFound);
    }

    /**
     * Convert a pixel location to surface coordinates.
     *
     * @param x The X coordinate
     * @param y The Y coordinate
     * @param layer The layer ID to fetch from. Layer 0 is the front-most
     * @param subLayer The ID of the viewport-layer that is needed. If there
     *   are no sub-layers, use 0.
     * @param position The converted position.  It must be preallocated.
     * @param deviceId A user-defined identifier for the requesting device when
     *    using the lastFound items
     * @param useLastFound Should we skip the search process and use the last
     *    data found for this layer/sublayer combo.
     *
     * @return Whether the coordinates where on the layer
     */
    @Override
    public boolean getPixelLocationInSurface(int x,
                                          int y,
                                          int layer,
                                          int subLayer,
                                          Point3f position,
                                          String deviceId,
                                          boolean useLastFound)
    {
        return canvasRenderer.getPixelLocationInSurface(x,
                                                 y,
                                                 layer,
                                                 subLayer,
                                                 position,
                                                 deviceId,
                                                 useLastFound);
    }

    /**
     * Get the Center Eye position in surface coordinates.
     *
     * @param x The X coordinate in the entire surface
     * @param y The Y coordinate in the entire surface
     * @param layer The layer ID to fetch from. Layer 0 is the front-most
     * @param subLayer The ID of the viewport-layer that is needed. If there
     *   are no sub-layers, use 0.
     * @param position The current eye position.  It must be preallocated.
     * @param deviceId A user-defined identifier for the requesting device when
     *    using the lastFound items
     * @param useLastFound Should we skip the search process and use the last
     *    data found for this layer/sublayer combo.
     *
     * @return Whether the coordinates where on the layer
     */
    @Override
    public boolean getCenterEyeInSurface(int x,
                                         int y,
                                         int layer,
                                         int subLayer,
                                         Point3f position,
                                         String deviceId,
                                         boolean useLastFound)
    {
        return canvasRenderer.getCenterEyeInSurface(x,
                                                    y,
                                                    layer,
                                                    subLayer,
                                                    position,
                                                    deviceId,
                                                    useLastFound);
    }

    /**
     * Add a surface info listener instance to this surface. Duplicate listener
     * instance add requests are ignored, as are null values.
     *
     * @param l The new listener instance to add
     */
    @Override
    public void addSurfaceInfoListener(SurfaceInfoListener l)
    {
        if(canvasRenderer != null)
            canvasRenderer.addSurfaceInfoListener(l);
    }

    /**
     * Remove a surface info listener from this surface. If the listener is not
     * currently registered the request is ignored.
     *
     * @param l The listener instance to remove
     */
    @Override
    public void removeSurfaceInfoListener(SurfaceInfoListener l)
    {
        if(canvasRenderer != null)
            canvasRenderer.removeSurfaceInfoListener(l);
    }

    //---------------------------------------------------------------
    // Methods defined by OutputDevice
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
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter = reporter;

        canvasRenderer.setErrorReporter(reporter);
        surfaceMonitor.setErrorReporter(errorReporter);
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
    @Override
    public boolean draw(ProfilingData profilingData)
    {
        // tell the draw lock that it's ok to run now, so long as it's not
        // called before the canvas has completed initialisation.
        if(!initComplete)
            if(!initCanvas())
                return false;

        // TODO:
        // Would be nice to alter this so that we can dynamically query for
        // available extensions after the first run has started. Would need some
        // sort of flag here that checks for new queries and executes it when/          // set.

        // Take local reference in case the setDrawableObjects decides to
        // update values right now.
        int count = numRenderables;
        OffscreenBufferRenderable[] surfaces = renderableList;
        boolean draw_continue = true;
        GraphicsProfilingData gpd = null;

        if(profilingData instanceof GraphicsProfilingData)
            gpd = (GraphicsProfilingData)profilingData;

        EnableState status = EnableState.ENABLE_FAILED;
        boolean reinit_required = false;


        // Always prepare the main canvas first.
        if(!terminate)
        {
            status = canvasRenderer.prepareData();
            draw_continue = (status == EnableState.ENABLE_OK) ||
                (status == EnableState.ENABLE_REINIT);

            if(status == EnableState.ENABLE_REINIT)
            {
                reinit_required = true;
                canvasRenderer.reinitialize();
            }
        }

        if(updatedSubscenes)
        {
            for(int i = 0; i < count && !terminate && draw_continue; i++)
            {
                if(surfaces[i] != null)
                {
                    RenderingProcessor rp = rendererMap.get(surfaces[i]);
                    status = rp.prepareData();

                    draw_continue = (status == EnableState.ENABLE_OK) ||
                        (status == EnableState.ENABLE_REINIT);

                    if(draw_continue)
                    {
                        if((status == EnableState.ENABLE_REINIT) ||
                           reinit_required)
                        {
                            reinit_required = true;
                            rp.reinitialize();
                        }

                        draw_continue = rp.render(gpd);
                        rp.swapBuffers();
                    }
                }
            }
            updatedSubscenes = false;
        }

        // Always render the main canvas last.
        if(!terminate && draw_continue)
            draw_continue = canvasRenderer.render(gpd);

//        if(canvasRenderer.hasOtherDataUpdates())
//            updatedSubscenes = true;
        return !terminate && draw_continue;
    }

    /**
     * Instruct this surface that you have finished with the resources needed
     * and to dispose all rendering resources.
     */
    @Override
    public void dispose()
    {
        terminate = true;

        canvasRenderer.halt();

        for(int i = 0; i < numRenderables; i++)
        {
            if(renderableList[i] != null)
            {
                RenderingProcessor rp = rendererMap.get(renderableList[i]);
                rp.halt();
            }
        }
    }

    /**
     * Check to see the disposal state of the surface. Will return true if the
     * {@link #dispose} method has been called or an internal dispose handler
     * has detected the underlying surface is no longer valid to draw to.
     *
     * @return true if the surface is disposed and no longer usable
     */
    @Override
    public boolean isDisposed()
    {
        return terminate;
    }

    /**
     * Notification that this surface is being drawn to with a single thread.
     * This can be used to optmise internal state handling when needed in a
     * single versus multithreaded environment.
     * <p>
     *
     * This method should never be called by end user code. It is purely for
     * the purposes of the {@link org.j3d.aviatrix3d.management.RenderManager}
     * to inform the device about what state it can expect.
     *
     * @param state true if the device can expect single threaded behaviour
     */
    @Override
    public void enableSingleThreaded(boolean state)
    {
        singleThreaded = state;

        if(canvasRenderer != null)
            canvasRenderer.enableSingleThreaded(state);

        for(int i = 0; i < numRenderables; i++)
        {
            if(renderableList[i] != null)
            {
                RenderingProcessor rp = rendererMap.get(renderableList[i]);
                rp.enableSingleThreaded(state);
            }
        }
    }

    /**
     * If the output device is marked as single threaded, this instructs the
     * device that the current rendering thread has exited. Next time the draw
     * method is called, a new rendering context will need to be created for
     * a new incoming thread instance. Also, if any other per-thread resources
     * are around, clean those up now. This is called just before that thread
     * exits.
     */
    @Override
    public void disposeSingleThreadResources()
    {
        if(canvasRenderer != null)
            canvasRenderer.disposeSingleThreadResources();

        for(int i = 0; i < numRenderables; i++)
        {
            if(renderableList[i] != null)
            {
                RenderingProcessor rp = rendererMap.get(renderableList[i]);
                rp.disposeSingleThreadResources();
            }
        }
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Create a new RenderingProcessor instance for this surface. Used
     * extended implementations of this surface to create a processor instance
     * that matches the specifics. The default implementation provides an
     * instance of {@link StandardRenderingProcessor}. Implementations may
     * override this to provide their own processor instance.
     *
     * @param ctx The parent context to use for the processor
     * @return The rendering processor instance to use
     */
    protected RenderingProcessor createRenderingProcessor(GLContext ctx)
    {
        return new StandardRenderingProcessor(ctx, this);
    }

    /**
     * Add an extension string to check for at startup.
     *
     * @param glExtensionString String to check for being valid
     */
    public void checkForExtension(String glExtensionString)
    {
        if(extensionList == null || extensionList.length == numExtensions)
        {
            String[] tmp = new String[numExtensions + 5];

            if(extensionList != null)
                System.arraycopy(extensionList, 0, tmp, 0, numExtensions);
            extensionList = tmp;
        }

        extensionList[numExtensions++] = glExtensionString;
    }

    /**
     * Check to see whether this object is currently shared. If it is shared
     * return true, otherwise return false.
     */
    public boolean isShared()
    {
        return sharedSurface != null;
    }

    /**
     * Used during initialisation of the system for the first time. This is
     * called just after the extension strings have been checked, but before
     * we return back to the main rendering loop. The default implementation is
     * empty.
     * <p>
     * The return value indicates success or failure in the ability to
     * initialise this surface. Typically it will indicate failure if the
     * underlying surface has been disposed of or a failure to find the
     * capabilities needed. The default implementation returns true.
     *
     * @param gl An initialised, current gl context to play with
     * @return true if the initialisation succeeded, or false if not
     */
    public boolean completeCanvasInitialisation(GL gl)
    {
        return true;
    }

    /**
     * Common internal initialisation for the constructors. Derived classes
     * must call this during their constructor otherwise this class will crash
     * in some spectacular ways.
     */
    protected void init()
    {
        GLDrawableFactory fac = GLDrawableFactory.getFactory();
        canCreatePBuffers = fac.canCreateGLPbuffer();

        colourTmp = new float[4];

        initComplete = false;

        renderableList = new OffscreenBufferRenderable[LIST_START_SIZE];
        numRenderables = 0;

        numExtensions = 0;

        rendererMap =
            new HashMap<OffscreenBufferRenderable, RenderingProcessor>();

        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Called by the drawable immediately after the OpenGL context is
     * initialized; the GLContext has already been made current when
     * this method is called. Any surface that overrides the draw method
     * should also call this method if the class variable {@link #initComplete}
     * is currently set to false.
     * <p>
     * The return value indicates success or failure in the ability to
     * initialise this surface. Typically it will indicate failure if the
     * underlying surface has been disposed of or a failure to find the
     * capabilities needed. The default implementation returns true.
     *
     * @return true if the initialisation succeeded, or false if not
     */
    protected boolean initCanvas()
    {
        if(canvasContext == null)
            return false;

        if(surfaceMonitor != null && !surfaceMonitor.isVisible())
            return initComplete;

        int status = canvasContext.makeCurrent();

        switch(status)
        {
            case GLContext.CONTEXT_NOT_CURRENT:

                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(TEST_CONTEXT_PROP);

                errorReporter.messageReport(msg);

                status = canvasContext.makeCurrent();

                if (status == GLContext.CONTEXT_NOT_CURRENT)
                {
                    // Exit right now as there's nothing left to do.
                    msg = intl_mgr.getString(FAILED_CONTEXT_PROP);

                    errorReporter.errorReport(msg, null);
                    return false;
                }
                else
                {
                    msg = intl_mgr.getString(PASS_CONTEXT_PROP);
                    errorReporter.messageReport(msg);
                }

                break;

            default:
                // do nothing for the other cases
        }

        GL gl = canvasContext.getGL();

        // Check for extensions:
        for(int i = 0; i < numExtensions; i++)
        {
            if(!gl.isExtensionAvailable(extensionList[i]))
                errorReporter.messageReport("Extension " +
                                            extensionList[i] +
                                            " not available");
        }

        if(completeCanvasInitialisation(gl))
        {
            canvasContext.release();
            initComplete = true;
        }

        return initComplete;
    }

    /**
     * Package local method to fetch the GLContext that this surface has.
     * Allows there to be sharing between different surface types - for example
     * having an elumens surface on one screen and a normal renderer on
     * another.
     *
     * @return The context used by this surface
     */
    public GLContext getGLContext()
    {
        return canvasContext;
    }

    /**
     * Get the context object from the shared surface, if there is one set.
     * If there is no surface set then this will return null
     *
     * @return The context from the shared surface or null
     */
    protected GLContext getSharedGLContext()
    {
        return  (sharedSurface != null) ? sharedSurface.getGLContext() : null;
    }
}
