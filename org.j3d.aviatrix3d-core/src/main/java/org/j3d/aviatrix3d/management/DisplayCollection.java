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
// None

// Local imports
import org.j3d.aviatrix3d.InvalidWriteTimingException;
import org.j3d.aviatrix3d.Layer;
import org.j3d.aviatrix3d.NodeUpdateHandler;
import org.j3d.aviatrix3d.pipeline.RenderPipeline;

import org.j3d.aviatrix3d.rendering.DeletableRenderable;
import org.j3d.aviatrix3d.rendering.ShaderSourceRenderable;

import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;

/**
 * A marker interface that represents a class capable of managing
 * a single set of layers and the pipeline(s) needed to render them.
 * <p>
 *
 * The collection groups together a set of layers that are to appear on
 * a surface and the pipelines needed to render them. The idea is to allow
 * a single rendering manager manage one or more scenegraphs and all of the
 * rendering infrastructure needed, yet keep all the updates synchronised. A
 * typical example of this would a MDI application that has multiple windows
 * each with their own set of layers and viewpoints onto a single shared
 * scene graph. This differs from the single window with layers creating
 * separate viewports that are constrained to that window.
 * <p>
 *
 * <b>Implementation Notes</b>
 * <p>
 *
 * This class contains a number of protected methods. These are called by the
 * {@link RenderManager} implementation at different times in the rendering
 * cycle with appropriate information. These are methods that should not be
 * called by the normal user, so they have been made protected to allow
 * package-only access to them, yet remain seeable by implementors of this
 * class outside of this package.
 * <p>
 *
 * Changing the layers and pipelines are subject to the same timing
 * restrictions as the rest of the scene graph. Layers may only be changed
 * during the dataChanged() callbacks.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public abstract class DisplayCollection
{
    /** The initial size of the shader init/log lists */
    private static final int LIST_START_SIZE = 64;

    /** The shader renderables that need to be initialised */
    protected ShaderSourceRenderable[] shaderInitList;

    /** The shader renderables that want log information */
    protected ShaderSourceRenderable[] shaderLogList;

    /** Queue for holding deleted textures */
    protected DeletableRenderable[] deletionList;

    /** The number of shader init requestors */
    protected int numShaderInit;

    /** The number of shader log requestors */
    protected int numShaderLog;

    /** The number of items to delete this frame */
    protected int numDeletables;

    /** Current enabled state */
    protected boolean enabled;

    /** Flag that the runtime thread should be terminated at next chance */
    protected boolean terminate;

    /** Error reporter used to send out messages */
    protected ErrorReporter errorReporter;

    /** Flag controlling layer change timing */
    protected boolean writeEnabled;

    /**
     * Initialise the basic structures of this collection.
     */
    protected DisplayCollection()
    {
        enabled = false;
        terminate = false;
        writeEnabled = true;

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        shaderInitList = new ShaderSourceRenderable[LIST_START_SIZE];
        shaderLogList = new ShaderSourceRenderable[LIST_START_SIZE];

        deletionList = new DeletableRenderable[LIST_START_SIZE];

    }

    /**
     * Force a single render of all pipelines now contained in this collection
     * now. Blocks until all rendering is complete (based on the definition of
     * the implementing class).
     * <p>
     * In general, it is inadvisable that method be called by end users as it is
     * normally managed by the RenderManager.
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
    public abstract boolean process();

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
    public abstract boolean displayOnly();

    /**
     * Force a halt of the current processing. Any processing in progress
     * should exit immediately. Used to abort the current scene processing due
     * to application shutdown or complete scene replacement.
     */
    public abstract void halt();

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
    public abstract void setLayers(Layer[] layers, int numLayers)
        throws IllegalArgumentException, InvalidWriteTimingException;

    /**
     * Get the number of layers that are currently set. If no layers are set,
     * or a scene is set, this will return zero.
     *
     * @return a value greater than or equal to zero
     */
    public abstract int numLayers();

    /**
     * Fetch the current layers that are set. The values will be copied into
     * the user-provided array. That array must be at least
     * {@link #numLayers()} in length. If not, this method does nothing (the
     * provided array will be unchanged).
     *
     * @param layers An array to copy the values into
     */
    public abstract void getLayers(Layer[] layers);

    /**
     * Add a pipeline to be rendered to the manager. A duplicate registration
     * or null value is ignored.
     *
     * @param pipe The new pipe instance to be added
     * @throws IllegalStateException The system is currently management and
     *   should be disabled first.
     */
    public abstract void addPipeline(RenderPipeline pipe)
        throws IllegalStateException;

    /**
     * Remove an already registered pipeline from the manager. A or null value
     * or one that is not currently registered is ignored.
     *
     * @param pipe The pipe instance to be removed
     * @throws IllegalStateException The system is currently management and
     *   should be disabled first.
     */
    public abstract void removePipeline(RenderPipeline pipe)
        throws IllegalStateException;

    /**
     * Set the update handler that controls synchronisations of write/read
     * process to the scene graph.
     *
     * @param handler The new handler instance to use
     */
    protected abstract void setUpdateHandler(NodeUpdateHandler handler);

    /**
     * Notification that the timing model permits changing the layers now.
     *
     * @param state true to enable layers to be changed, false to disable
     */
    protected void enableLayerChange(boolean state)
    {
        writeEnabled = state;
    }

    /**
     * Queue up a objects for deletion with the next rendering pass. This is
     * called once per rendering cycle with the values to be processed in the
     * next pass. It will contain the complete list, so you can assume that
     * the list size only needs to be set once.  Local copies should be made
     * of the array as it may be overwritten by the caller during the next
     * cycle.
     *
     * @param deleted The items to be processed for deletion
     * @param num The number of valid items in this array
     */
    protected void queueDeletedObjects(DeletableRenderable[] deleted, int num)
    {
        if(num > deletionList.length)
            deletionList = new DeletableRenderable[num];

        System.arraycopy(deleted, 0, deletionList, 0, num);

        numDeletables = num;
    }

    /**
     * Queue up shader objects that need some pre-processing done. This is
     * called once per rendering cycle with the values to be processed in the
     * next pass. It will contain the complete list, so you can assume that
     * the list size only needs to be set once.  Local copies should be made
     * of the array as it may be overwritten by the caller during the next
     * cycle.
     *
     * @param initList The items to be processed GLSL compilation
     * @param numInit The number of valid items in the init array
     * @param logList The items requesting shader log info
     * @param numLog The number of valid items in the log array
     */
    protected void queueShaderObjects(ShaderSourceRenderable[] initList,
                                      int numInit,
                                      ShaderSourceRenderable[] logList,
                                      int numLog)
    {
        if(numInit > shaderInitList.length)
            shaderInitList = new ShaderSourceRenderable[numInit];

        if(numLog > shaderLogList.length)
            shaderLogList = new ShaderSourceRenderable[numLog];

        System.arraycopy(initList, 0, shaderInitList, 0, numInit);
        System.arraycopy(logList, 0, shaderLogList, 0, numLog);

        numShaderInit = numInit;
        numShaderLog = numLog;
    }

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
     * Notification to shutdown the internals of the renderer because the
     * application is about to exit. Normally this will be called by the
     * containing {@link RenderManager} and should not need to be called by
     * end users.
     */
    public void shutdown()
    {
        terminate = true;
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
        return terminate;
    }
}
