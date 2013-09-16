/*****************************************************************************
 *                   Yumetech, Inc Copyright (c) 2004 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d.management;

// External imports
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.InvalidWriteTimingException;
import org.j3d.aviatrix3d.Layer;
import org.j3d.aviatrix3d.NodeUpdateHandler;
import org.j3d.aviatrix3d.pipeline.RenderPipeline;

import org.j3d.aviatrix3d.pipeline.OutputDevice;
import org.j3d.aviatrix3d.pipeline.RenderPipeline;
import org.j3d.aviatrix3d.pipeline.RenderableRequestData;
import org.j3d.aviatrix3d.pipeline.audio.AudioRenderPipeline;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsRenderPipeline;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsRequestData;

import org.j3d.aviatrix3d.rendering.DeletableRenderable;
import org.j3d.aviatrix3d.rendering.LayerCullable;
import org.j3d.aviatrix3d.rendering.ShaderSourceRenderable;

import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;

/**
 * Display collection that manages just a single set of audio + graphics
 * pipeline and a single output surface.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>tooSmallLayerArrayMsg: setLayers() argument size mismatch</li>
 * <li>layerSetTimingMsg: call to setLayers() at the wrong time</li>
 * <li>invalidSetTimingMsg: call to any other method at the wrong time</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.9 $
 */
public class SingleDisplayCollection extends DisplayCollection
{
    /** Message when the setLayers() call doesn't have an array big enough */
    private static final String LAYER_SET_SIZE_ERR_PROP =
        "org.j3d.aviatrix3d.management.SingleDisplayCollection.tooSmallLayerArrayMsg";

    /** Message when attempting to set layers at the wrong time */
    private static final String LAYER_TIMING_MSG_PROP =
        "org.j3d.aviatrix3d.management.SingleDisplayCollection.layerSetTimingMsg";

    /**
     * Message when trying to call any of the set or render methods while the
     * class is currently processing the pipeline.
     */
    private static final String ACTIVE_RENDERING_MSG_PROP =
        "org.j3d.aviatrix3d.management.SingleDisplayCollection.invalidSetTimingMsg";


    /** The manager for the layers */
    private LayerContainer layerContainer;

    /** The list of layers this graphicsPipeline manages */
    private Layer[] layers;

    /** The number of layers to process */
    private int numLayers;

    /** Cullables extracted from each layer. Filled in each frame */
    private LayerCullable[] cullables;

    /** Pipeline to manager as part of the drawing process */
    private GraphicsRenderPipeline graphicsPipeline;

    /** Audio Pipeline to manager as part of the drawing process */
    private AudioRenderPipeline audioPipeline;

    /**
     * Flag to say whether a pipeline has been added since the last frame
     * process. If this is the case, they we want to ignore the displayOnly()
     * call and process that pipeline the first time anyway.
     */
    private boolean pipelineAdded;

    /**
     * Create a new instance of this collection with no pipelines preset.
     */
    public SingleDisplayCollection()
    {
        layerContainer = new LayerContainer();
        layers = new Layer[1];
        pipelineAdded = false;
    }

    /**
     * Constructs a new collection for a single channel
     *
     * @param graphicsPipe The graphics pipeline instance to be used
     */
    public SingleDisplayCollection(GraphicsRenderPipeline graphicsPipe)
    {
        this();

        graphicsPipeline = graphicsPipe;
    }

    /**
     * Constructs a new collection for a single channel based on the give
     * audio and graphics pipelines.
     *
     * @param graphicsPipe The graphics pipeline instance to be used
     */
    public SingleDisplayCollection(GraphicsRenderPipeline graphicsPipe,
                                   AudioRenderPipeline audioPipe)
    {
        this();

        graphicsPipeline = graphicsPipe;
        audioPipeline = audioPipe;
    }

    //---------------------------------------------------------------
    // Methods defined by DisplayCollection
    //---------------------------------------------------------------

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the node's internals can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter)
    {
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter = reporter;

        if(graphicsPipeline != null)
            graphicsPipeline.setErrorReporter(reporter);

        if(audioPipeline != null)
            audioPipeline.setErrorReporter(reporter);
    }

    /**
     * Tell render to start or stop management. If currently running, it
     * will wait until all the pipelines have completed their current cycle
     * and will then halt.
     *
     * @param state True if to enable management
     */
    public void setEnabled(boolean state)
    {
        enabled = state;
        layerContainer.setLive(state);
    }

    /**
     * Get the current render state of the manager.
     *
     * @return true if the manager is currently running
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Force a single render of all pipelines now contained in this collection
     * now. Blocks until all rendering is complete (based on the definition of
     * the implementing class).
     * <p>
     *
     * In general, it is inadvisable that method be called by end users as it is
     * normally managed by the RenderManager.
     * The return value indicates success or failure in the ability to
     * render this frame. Typically it will indicate failure if the
     * underlying surface has been disposed of, either directly through the
     * calling of the method on this interface, or through an internal check
     * mechanism. If failure is indicated, then check to see if the surface has
     * been disposed of and discontinue rendering if it has.
     *
     * @return true if the drawing succeeded, or false if not
     */
    public boolean process()
    {
        if(!enabled || terminate)
            return false;

        boolean ret_val = true;

        // Need to send on the deletable and shader objects here too.
        if(graphicsPipeline != null)
        {
            RenderableRequestData rrq = checkForGraphicsRequests();
            graphicsPipeline.setRequestData(rrq);

            ret_val = graphicsPipeline.render();
            if(ret_val)
                graphicsPipeline.swapBuffers();
        }

        if(terminate)
            return false;

        if(audioPipeline != null)
        {
            RenderableRequestData rrq = checkForAudioRequests();
            audioPipeline.setRequestData(rrq);

            ret_val &= audioPipeline.render();
        }

        // Always force this flag in the process case.
        pipelineAdded = false;
        clearDeletables();

        return ret_val;
    }

    /**
     * Cause the surface to redraw the next frame only, with no processing of
     * the pipeline. This is typically an optimisation step when nothing has
     * changed in user land, so there's no processing that needs to be done.
     * Skip the processing and tell the drawable surface to render again what
     * it already has set from the previous frame.
     * <p>
     * The return value indicates success or failure in the ability to
     * render this frame. Typically it will indicate failure if the
     * underlying surface has been disposed of, either directly through the
     * calling of the method on this interface, or through an internal check
     * mechanism. If failure is indicated, then check to see if the surface has
     * been disposed of and discontinue rendering if it has.
     *
     * @return true if the drawing succeeded, or false if not
     */
    public boolean displayOnly()
    {
        if(!enabled || terminate)
            return false;

        boolean ret_val = true;

        if(pipelineAdded)
        {
            ret_val = process();
        }
        else
        {
            // Need to send on the deletable and shader objects here too.
            if(graphicsPipeline != null)
            {
                RenderableRequestData rrq = checkForGraphicsRequests();
                graphicsPipeline.setRequestData(rrq);

                // Bug fix (by Sang Park): When shader nodes were being dynamically added to the scene graph,
                // shader programs weren't being initalized by the render processor.  Reason it wasn't being initalized
                // was because RenderableRequestData was never passed into the render processor. Below code fixes the
                // problem.

                // If there is no reqested data display redraw the last frame (added)
                if(rrq == null)
                {
                    ret_val = graphicsPipeline.displayOnly(); // This was the original code.
                }
                // Pass the request data to the render processor to process any new changes that happened in the scene graph (added)
                else
                {
                    ret_val = graphicsPipeline.render(); // This was added to pass in request data into the render processor
                }

                if(ret_val)
                    graphicsPipeline.swapBuffers();
            }

            if(terminate)
                return false;

            if(audioPipeline != null)
            {
                RenderableRequestData rrq = checkForAudioRequests();
                audioPipeline.setRequestData(rrq);

                ret_val &= audioPipeline.displayOnly();
            }

            clearDeletables();
        }

        return ret_val;
    }

    /**
     * Force a halt of the current processing. Any processing in progress
     * should exit immediately. Used to abort the current scene processing due
     * to application shutdown or complete scene replacement.
     */
    public void halt()
    {
        if(graphicsPipeline != null)
            graphicsPipeline.halt();

        if(audioPipeline != null)
            audioPipeline.halt();
    }

    /**
     * Set the set of layers for this manager. Setting a value of
     * <code>null</code> will remove the currently set of layers. If this is
     * set while a current scene is set, then the scene will be cleared. Layers
     * are presented in depth order - layers[0] is rendered before layers[1]
     * etc.
     * <p>
     * If this render manager is currently running, this method can only be
     * called during the main update
     *
     * @param layers The collection of layers, in order, to render
     * @param numLayers The number of valid layers to use
     * @throws IllegalArgumentException The length of the layers array is less
     *    than numLayers
     * @throws InvalidWriteTimingException The method was called with the
     *    system enabled and not during the app observer callback
     */
    public void setLayers(Layer[] layers, int numLayers)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(enabled && !writeEnabled)
		{
			I18nManager intl_mgr = I18nManager.getManager();
			String msg = intl_mgr.getString(LAYER_TIMING_MSG_PROP);
            throw new InvalidWriteTimingException(msg);
		}

        int size = layers == null ? 0 : layers.length;
        if(size < numLayers)
		{
			I18nManager intl_mgr = I18nManager.getManager();
			String msg_pattern = intl_mgr.getString(LAYER_SET_SIZE_ERR_PROP);

			Locale lcl = intl_mgr.getFoundLocale();

			NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

			Object[] msg_args = { new Integer(size), new Integer(numLayers) };
			Format[] fmts = { n_fmt };
			MessageFormat msg_fmt =
				new MessageFormat(msg_pattern, lcl);
			msg_fmt.setFormats(fmts);
			String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
		}

        if(this.layers.length < numLayers)
            this.layers = new Layer[numLayers];

        System.arraycopy(layers, 0, this.layers, 0, numLayers);
        this.numLayers = numLayers;

        // Allocate each time as this will rarely, if ever, change.
        cullables = new LayerCullable[numLayers];

        layerContainer.changeLayers(layers, numLayers);

        // Always use buffer ID 0 for a single-threaded system
        for(int i = 0; i < numLayers; i++)
            cullables[i] = layers[i].getCullable(0);

        if(graphicsPipeline != null)
            graphicsPipeline.setRenderableLayers(layers, numLayers);

        if(audioPipeline != null)
            audioPipeline.setRenderableLayers(layers, numLayers);
    }

    /**
     * Get the number of layers that are currently set. If no layers are set,
     * or a scene is set, this will return zero.
     *
     * @return a value greater than or equal to zero
     */
    public int numLayers()
    {
        return numLayers;
    }

    /**
     * Fetch the current layers that are set. The values will be copied into
     * the user-provided array. That array must be at least
     * {@link #numLayers()} in length. If not, this method does nothing (the
     * provided array will be unchanged).
     *
     * @param layers An array to copy the values into
     */
    public void getLayers(Layer[] layers)
    {
        if((layers == null) || (layers.length < numLayers))
            return;

        System.arraycopy(this.layers, 0, layers, 0, numLayers);
    }

    /**
     * Add a pipeline to be rendered to the manager. A duplicate registration
     * or null value is ignored.
     *
     * @param pipe The new pipe instance to be added
     * @throws IllegalStateException The system is currently management and
     *   should be disabled first.
     */
    public void addPipeline(RenderPipeline pipe)
        throws IllegalStateException
    {
        if(enabled && !writeEnabled)
		{
			I18nManager intl_mgr = I18nManager.getManager();
			String msg = intl_mgr.getString(ACTIVE_RENDERING_MSG_PROP);
            throw new IllegalStateException(msg);
		}

        if(pipe instanceof GraphicsRenderPipeline)
        {
            graphicsPipeline = (GraphicsRenderPipeline)pipe;

            if(graphicsPipeline != null)
                graphicsPipeline.setRenderableLayers(layers, numLayers);
        }
        else if(pipe instanceof AudioRenderPipeline)
        {
            audioPipeline = (AudioRenderPipeline)pipe;

            if(audioPipeline != null)
                audioPipeline.setRenderableLayers(layers, numLayers);
        }
    }

    /**
     * Remove an already registered pipeline from the manager. A or null value
     * or one that is not currently registered is ignored.
     *
     * @param pipe The pipe instance to be removed
     * @throws IllegalStateException The system is currently management and
     *   should be disabled first.
     */
    public void removePipeline(RenderPipeline pipe)
        throws IllegalStateException
    {
        if(enabled && !writeEnabled)
		{
			I18nManager intl_mgr = I18nManager.getManager();
			String msg = intl_mgr.getString(ACTIVE_RENDERING_MSG_PROP);
            throw new IllegalStateException(msg);
		}

        if(pipe instanceof GraphicsRenderPipeline)
        {
            if(pipe == graphicsPipeline)
            {
                graphicsPipeline.setRenderableLayers(null, 0);
                graphicsPipeline = null;
            }
        }
        else if(pipe instanceof AudioRenderPipeline)
        {
            if(pipe == audioPipeline)
            {
                audioPipeline.setRenderableLayers(null, 0);
                audioPipeline = null;
            }
        }
    }

    /**
     * Set the update handler that controls synchronisations of write/read
     * process to the scene graph.
     *
     * @param handler The new handler instance to use
     */
    protected void setUpdateHandler(NodeUpdateHandler handler)
    {
        layerContainer.setUpdateHandler(handler);
    }

    /**
     * Notification to shutdown the internals of the renderer because the
     * application is about to exit. Normally this will be called by the
     * containing {@link RenderManager} and should not need to be called by
     * end users.
     */
    public void shutdown()
    {
        // If this has already been called once, ignore it. Most of the
        // variables will have been nulled out by now.
        if(terminate || !enabled)
            return;

        terminate = true;

        if(graphicsPipeline != null)
            graphicsPipeline.halt();

        if(audioPipeline != null)
            audioPipeline.halt();
    }

    /**
     * Check to see if this pipeline is now inoperable. It may be inoperable
     * for one of many reasons, such as the output device is terminated, user
     * terminated or some abnormal internal condition.
     *
     * @return true if the collection is no longer operable
     */
    public boolean isDisposed()
    {
        if(super.isDisposed())
            return true;

        if(graphicsPipeline != null)
        {
            GraphicsOutputDevice dev =
                graphicsPipeline.getGraphicsOutputDevice();
            return dev != null ? dev.isDisposed() : false;
        }

        return false;
    }

    /**
     * Clear the deletables list now.
     */
    private void clearDeletables()
    {
        // now clear the array
        for(int i = 0; i < numDeletables; i++)
            deletionList[i] = null;

        numDeletables = 0;
    }

    /**
     * Check the various lists and form into a renderable request
     * info to pass on.
     *
     * @return A request structure if there is anything to pass on or null
     */
    private GraphicsRequestData checkForGraphicsRequests()
    {
        GraphicsRequestData grd = null;

        if(numDeletables != 0)
        {
            grd = new GraphicsRequestData();
            grd.deletionRequests = new DeletableRenderable[numDeletables];
            System.arraycopy(deletionList,
                             0,
                             grd.deletionRequests,
                             0,
                             numDeletables);
        }

        if(numShaderInit != 0)
        {
            if(grd == null)
                grd = new GraphicsRequestData();

            grd.shaderInitList = new ShaderSourceRenderable[numShaderInit];
            System.arraycopy(shaderInitList,
                             0,
                             grd.shaderInitList,
                             0,
                             numShaderInit);

            // now clear the array
            for(int i = 0; i < numShaderInit; i++)
                shaderInitList[i] = null;

            numShaderInit = 0;
        }

        if(numShaderLog != 0)
        {
            if(grd == null)
                grd = new GraphicsRequestData();

            grd.shaderLogList = new ShaderSourceRenderable[numShaderLog];
            System.arraycopy(shaderLogList,
                             0,
                             grd.shaderLogList,
                             0,
                             numShaderLog);

            // now clear the array
            for(int i = 0; i < numShaderLog; i++)
                shaderLogList[i] = null;

            numShaderLog = 0;
        }

        return grd;
    }

    /**
     * Check the various lists and form into a renderable request
     * info to pass on.
     *
     * @return A request structure if there is anything to pass on or null
     */
    private RenderableRequestData checkForAudioRequests()
    {
        RenderableRequestData rrd = null;

        if(numDeletables != 0)
        {
            rrd = new GraphicsRequestData();
            rrd.deletionRequests = new DeletableRenderable[numDeletables];
            System.arraycopy(deletionList,
                             0,
                             rrd.deletionRequests,
                             0,
                             numDeletables);
        }

        return rrd;
    }
}
