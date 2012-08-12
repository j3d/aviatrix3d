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

package org.j3d.aviatrix3d.pipeline.audio;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.rendering.LayerCullable;
import org.j3d.aviatrix3d.rendering.ProfilingData;
import org.j3d.aviatrix3d.pipeline.RenderableRequestData;

import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;

/**
 * The default implementation of the audio pipeline usable by most
 * applications.
 * <p>
 * This implementation is targeted towards single threaded architectures.
 * After setting the stages, the render command will not return until
 * everything is complete.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 2.9 $
 */
public class DefaultAudioPipeline implements AudioRenderPipeline
{
    /** The culling stage to be used */
    private AudioCullStage culler;

    /** The sorting stage to be used */
    private AudioSortStage sorter;

    /** Surface to draw to */
    private AudioOutputDevice device;

    /** The listener to pass cull to sort */
    private AudioCullToSingleSortListener ctsListener;

    /** The listener to pass sort to the drawable listener */
    private AudioSortToSingleDeviceListener stdListener;

    /** The list of layers this pipeline manages */
    private LayerCullable[] layers;

    /** The number of layers to process */
    private int numLayers;

    /** Time to stop the pipeline */
    private boolean terminate;

    /**
     * Other data to send down the pipe. Is only non-null between
     * the various set requests and the next render() or displayOnly()
     * calls. A new instance is created each time it is sent down the
     * pipeline.
     */
    private RenderableRequestData otherData;

    /** Local reporter to put errors in */
    private ErrorReporter errorReporter;

    /**
     * Create an instance of the pipeline with nothing registered.
     */
    public DefaultAudioPipeline()
    {
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        ctsListener = new AudioCullToSingleSortListener();
        stdListener = new AudioSortToSingleDeviceListener();
        terminate = false;
        layers = new LayerCullable[1];
    }

    /**
     * Construct a pipeline with the sort and cull stages provided.
     *
     * @param ss The sort stage instance to use
     * @param cs The cull stage instance to use
     */
    public DefaultAudioPipeline(AudioCullStage cs, AudioSortStage ss)
    {
        this();

        culler = cs;
        sorter = ss;

        if(cs != null)
            cs.setCulledAudioReceiver(ctsListener);

        if(ss != null)
        {
            ctsListener.setSorter(ss);
            ss.setSortedAudioReceiver(stdListener);
        }
    }

    //---------------------------------------------------------------
    // Methods defined by AudioRenderPipeline
    //---------------------------------------------------------------

    /**
     * Register a drawing surface that this pipeline will send its output to.
     * Setting a null value will remove the current drawable surface.
     *
     * @param device The audio output device instance to use or replace
     */
    public void setAudioOutputDevice(AudioOutputDevice device)
    {
        stdListener.setDevice(device);

        this.device = device;
    }

    /**
     * Get the currently registered drawable device instance. If none is set,
     * return null.
     *
     * @return The currently set surface instance or null
     */
    public AudioOutputDevice getAudioOutputDevice()
    {
        return device;
    }

    //---------------------------------------------------------------
    // Methods defined by RenderPipeline
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
        errorReporter = reporter;

        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Set the request data that should be passed along with the next frame.
     * This is temporary that is passed along at the next render() or
     * displayOnly() call and will be cleared after that.
     *
     * @param data The data instance to pass this next frame
     */
    public void setRequestData(RenderableRequestData data)
    {
        otherData = data;
    }

    /**
     * Start the pipeline functioning now. All steps will be called and this
     * method will not return until all are completed and the surface has
     * swapped.
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
    public boolean render()
    {
        if(numLayers == 0)
            return true;

        boolean draw_state = true;

        ProfilingData profilingData = new ProfilingData();

        if(culler != null)
        {
            culler.cull(otherData, profilingData, layers, numLayers);
            otherData = null;

            // then draw after the cull is complete
            if(!terminate && device != null)
                draw_state = device.draw(profilingData);
        }

        return draw_state;
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
        boolean draw_state = true;
        ProfilingData profilingData = new ProfilingData();

        if(!terminate && (numLayers != 0) && (device != null))
            draw_state = device.draw(profilingData);

        return draw_state;
    }

    /**
     * Set the set of layers to be used by this pipeline. Providing an argument
     * of a zero number of layers remove the layers from being rendered,
     * causing the pipeline to function as a no-op when rendered.
     * <p>
     *
     * If a scene is currently set, and a non-zero number of layers is
     * provided, this will remove the scene and use the layers instead.
     *
     * @param layers The collection of layers, in order, to render
     * @param numLayers The number of valid layers to use
     */
    public void setRenderableLayers(LayerCullable[] layers, int numLayers)
    {
        if(layers != null)
        {
            if(this.layers.length < numLayers)
                this.layers = new LayerCullable[numLayers];

            System.arraycopy(layers, 0, this.layers, 0, numLayers);
        }

        this.numLayers = numLayers;
    }

    /**
     * Force a halt of the current processing. Any processing in progress
     * should exit immediately. Used to abort the current scene processing due
     * to application shutdown or complete scene replacement.
     */
    public void halt()
    {
        terminate = true;

        if(culler != null)
            culler.halt();

        if(sorter != null)
            sorter.halt();

        if(device != null)
            device.dispose();
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Set the sorter instance to be used. If the instance is null, the current
     * sorter is removed.
     *
     * @param ss The sorter instance to use or null
     */
    public void setSorter(AudioSortStage ss)
    {
        ctsListener.setSorter(ss);

        if(sorter != null)
            sorter.setSortedAudioReceiver(null);

        if(ss != null)
            ss.setSortedAudioReceiver(stdListener);

        sorter = ss;
    }

    /**
     * Set the cull instance to be used. If the instance is null, the current
     * culler is removed.
     *
     * @param cs The cull instance to use or null
     */
    public void setCuller(AudioCullStage cs)
    {
        if(culler != null)
            culler.setCulledAudioReceiver(null);

        if(cs != null)
            cs.setCulledAudioReceiver(ctsListener);

        culler = cs;
    }
}
