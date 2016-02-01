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
import com.jogamp.opengl.*;

import java.util.HashMap;
import java.util.Map;

import com.jogamp.nativewindow.AbstractGraphicsDevice;
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Point3d;
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
 * <li>makeCurrentAttemptMsg: Info message when attempting to make the GL context current</li>
 * <li>makeCurrentSuccessMsg: Info message when the GL context is current</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 3.50 $
 */
public abstract class BaseSurface
    implements GraphicsOutputDevice, GLEventListener
{

    /** Message when the GL context failed to initialise */
    private static final String PASS_CONTEXT_PROP =
        "org.j3d.aviatrix3d.output.graphics.BaseSurface.makeCurrentSuccessMsg";

    /** The initial size of the children list */
    private static final int LIST_START_SIZE = 20;

    /** The real canvas that we draw to */
    protected GLAutoDrawable canvas;

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
     * Temp variable to hold the profiling information during draw calls. This
     * is here because we get a call and then call GLAutoDrawable.display() and
     * then later get the callback that fills in this information. Not null
     * only during the draw() call
     */
    private GraphicsProfilingData profilingData;

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

    @Override
    public boolean isStereoAvailable()
    {
        return false;
    }

    @Override
    public boolean isQuadStereoAvailable()
    {
        return false;
    }

    @Override
    public void setStereoEyeSeparation(float sep)
    {
    }

    @Override
    public float getStereoEyeSeparation()
    {
        return 0;
    }

    @Override
    public void setStereoRenderingPolicy(int policy)
    {
    }

    @Override
    public int getStereoRenderingPolicy()
    {
        return NO_STEREO;
    }

    @Override
    public void setClearColor(float r, float g, float b, float a)
    {
        canvasRenderer.setClearColor(r, g, b, a);
    }

    @Override
    public void setColorClearNeeded(boolean state)
    {
        canvasRenderer.setColorClearNeeded(state);
    }

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

    @Override
    public boolean isTwoPassTransparentEnabled()
    {
        return useTwoPassTransparent;
    }

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

    @Override
    public float getAlphaTestCutoff()
    {
        return alphaCutoff;
    }

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
                    rp = createRenderingProcessor();
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
                canvas.swapBuffers();
            }
            catch(GLException e)
            {
                if(!(e.getCause() instanceof InterruptedException))
                    throw e;
            }
        }
    }

    @Override
    public boolean getSurfaceToVWorld(int x,
                                   int y,
                                   int layer,
                                   int subLayer,
                                   Matrix4d matrix,
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

    @Override
    public boolean getPixelLocationInSurface(int x,
                                          int y,
                                          int layer,
                                          int subLayer,
                                          Point3d position,
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

    @Override
    public boolean getCenterEyeInSurface(int x,
                                         int y,
                                         int layer,
                                         int subLayer,
                                         Point3d position,
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

    @Override
    public void addSurfaceInfoListener(SurfaceInfoListener l)
    {
        if(canvasRenderer != null)
            canvasRenderer.addSurfaceInfoListener(l);
    }

    @Override
    public void removeSurfaceInfoListener(SurfaceInfoListener l)
    {
        if(canvasRenderer != null)
            canvasRenderer.removeSurfaceInfoListener(l);
    }

    //---------------------------------------------------------------
    // Methods defined by OutputDevice
    //---------------------------------------------------------------

    @Override
    public void setErrorReporter(ErrorReporter reporter)
    {
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter = reporter;

        canvasRenderer.setErrorReporter(reporter);
    }

    @Override
    public boolean draw(ProfilingData profilingData)
    {
        // TODO:
        // Would be nice to alter this so that we can dynamically query for
        // available extensions after the first run has started. Would need some
        // sort of flag here that checks for new queries and executes it when
        // 65set.

        if(profilingData instanceof GraphicsProfilingData)
        {
            this.profilingData = (GraphicsProfilingData)profilingData;
        }

        canvas.display();

        this.profilingData = null;

        return true;
    }

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

        // Need this here or should we keep the Canvas open?
        // canvas.destroy();
    }

    @Override
    public boolean isDisposed()
    {
        return terminate;
    }

    //---------------------------------------------------------------
    // Methods defined by GLEventListener
    //---------------------------------------------------------------

    @Override
    public void init(GLAutoDrawable drawable)
    {
        initCanvas(drawable.getContext());
    }

    @Override
    public void dispose(GLAutoDrawable drawable)
    {

    }

    @Override
    public void display(GLAutoDrawable drawable)
    {
        GLContext localContext = drawable.getContext();

        // Always prepare the main canvas first.
        if(!terminate)
        {
            canvasRenderer.prepareData(localContext);
        }

        // Take local reference in case the setDrawableObjects decides to
        // update values right now.
        int count = numRenderables;
        OffscreenBufferRenderable[] surfaces = renderableList;

        if(updatedSubscenes)
        {
            for(int i = 0; i < count && !terminate; i++)
            {
                if(surfaces[i] != null)
                {
                    RenderingProcessor rp = rendererMap.get(surfaces[i]);
                    rp.prepareData(localContext);

                    if(terminate)
                    {
                        break;
                    }

                    rp.render(localContext, profilingData);
                }
            }
            updatedSubscenes = false;
        }

        // Always render the main canvas last.
        if(terminate)
        {
            return;
        }

        preMainCanvasDraw();

        canvasRenderer.render(localContext, profilingData);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
    {

    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Pre final canvas draw method that is called after the subscenes have been rendered
     * but before the main canvas is rendered. Default method is empty, but derived
     * classes can use it to do anything of interest. Typical use would be to select
     * which eye to draw for stereo rendering
     */
    protected void preMainCanvasDraw()
    {
        // Do nothing for the default
    }

    /**
     * Create a new RenderingProcessor instance for this surface. Used
     * extended implementations of this surface to create a processor instance
     * that matches the specifics. The default implementation provides an
     * instance of {@link StandardRenderingProcessor}. Implementations may
     * override this to provide their own processor instance.
     *
     * @return The rendering processor instance to use
     */
    protected RenderingProcessor createRenderingProcessor()
    {
        return new StandardRenderingProcessor(this);
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
    protected void initBasicDataStructures()
    {
        colourTmp = new float[4];

        initComplete = false;

        renderableList = new OffscreenBufferRenderable[LIST_START_SIZE];
        numRenderables = 0;

        numExtensions = 0;

        rendererMap = new HashMap<>();

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
    protected boolean initCanvas(GLContext canvasContext)
    {
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
            initComplete = true;
        }

        return initComplete;
    }
}
