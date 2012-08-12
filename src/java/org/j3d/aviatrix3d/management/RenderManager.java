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

package org.j3d.aviatrix3d.management;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.ApplicationUpdateObserver;
import org.j3d.aviatrix3d.InvalidWriteTimingException;
import org.j3d.aviatrix3d.Layer;
import org.j3d.aviatrix3d.pipeline.RenderPipeline;

import org.j3d.aviatrix3d.picking.PickingManager;

import org.j3d.util.ErrorReporter;

/**
 * A marker interface that represents a class capable of managing
 * the complete management pipeline.
 * <p>
 *
 * A manager is used to handle a system-specific management technique. The goal
 * is to manage the contained pipeline(s) in a way that is most efficient to
 * the hardware provided. Thus, it is expected there will be many different
 * types of managers to suit the many hardware configurations available.
 * <p>
 *
 * Example implementations of the pipeline manager would be one that handles
 * all the pipelines with simultaneous threads, each pinned to a particular
 * CPU/Graphics pipe that the machine has. Another implementation may hold all
 * the pipelines for sequential evaluation piping the output from one into the
 * input for another (eg for handling dynamic cubic environment maps).
 * <p>
 *
 * Basic common methods are provided for all implementations to use. It is
 * expected that implementations will add additional technique-specific
 * extension methods to the basic features.
 * <p>
 *
 * <b>Controlling Rendering Updates</b>
 * <p>
 *
 * Almost all applications will wish to control the rate at which management
 * occurs. Typically this is just so that the rest of the application and
 * user's system remains interactive (the default setup is to use 100% CPU to
 * run as fast as possible). Alternatively the application may have a very
 * small rate of change, or wish to run in an offscreen-management mode (eg
 * server-based for web-delivery to an end user).
 * <p>
 *
 * The basic frame rate is controlled by the {@link #setMinimumFrameInterval(int)}
 * method. This provides the minimum time, in milliseconds, between sequential
 * frame managements. If the management of a single frame takes longer than this
 * time, then the next frame will begin as soon as possible. (Note, we do not
 * currently provide any mechanism for guaranteeing a specific frame rate,
 * only a maximum framerate). For example, setting a value of 20 will result
 * in a maximum of 50 frames per second.
 * <p>
 *
 * To return the system to an unregulated frame rate, after having set the
 * value previously, call <code>setMinimumFrameInterval()</code> method with a
 * value of 0.
 * <p>
 *
 * If you wish to render on demand, then set the render manager to disabled
 * it is disabled by default on construction). Then, whenever you need to
 * repaint the surface, call the {@link #renderOnce()} method.
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public interface RenderManager
{
    /**
     * Register an error reporter with the engine so that any errors generated
     * by the node's internals can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter);

    /**
     * Set whether the manager should automatically halt management if an error
     * or exception is detected during the user callback processing. If this is
     * set to true (the default) then processing immediately halts and sets the
     * state to disabled as soon as an error is detected. The management
     * contexts are not disposed of. We just terminate the current management
     * process if this is detected.
     * <p>
     * If the value is set to false, then the error is caught, but management
     * continues on regardless.
     * <p>
     * In both states, the error that is caught is reported through the
     * currently registered {@link ErrorReporter} instance as an error message.
     *
     * @param state true to enable halting, false to disable
     */
    public void setHaltOnError(boolean state);

    /**
     * Check to see the current halt on error state.
     *
     * @return true if the system halts on an error condition
     * @see #setHaltOnError
     */
    public boolean isHaltingOnError();

    /**
     * Tell render to start or stop management. If currently running, it
     * will wait until all the pipelines have completed their current cycle
     * and will then halt.
     *
     * @param state True if to enable management
     */
    public void setEnabled(boolean state);

    /**
     * Get the current render state of the manager.
     *
     * @return true if the manager is currently running
     */
    public boolean isEnabled();

    /**
     * Force a single render of all pipelines now. Ignores the enabled and
     * cycle time settings to cause a single render at this point in time.
     * If a render is currently in progress, an exception is generated. If
     * this method is called, the application observer is not called
     * beforehand. It assumes that you'll be making the scene graph updates
     * yourself before calling this method.
     *
     * @throws IllegalStateException The system is currently management and
     *   should be disabled first.
     */
    public void renderOnce()
        throws IllegalStateException;

    /**
     * Request that the manager perform a full scene render pass and update,
     * ignoring any usual optimisations that it may take. For performance
     * reasons, a render manager may elect to not run or update some portions
     * of the scene graph. This method requests that the next frame only should
     * ignore those optimisations and process the full scene graph.
     * <p>
     *
     * This method will work both in automated management and with the
     * {@link #renderOnce()} method.
     */
    public void requestFullSceneRender();

    /**
     * Set the minimum duty cycle of the render manager. This is the type in
     * milliseconds that should be the minimum between frames and can be used
     * to throttle the management loop to a maximum frame rate should other
     * systems require CPU time. This can be changed at any time.
     * <p>
     *
     * Setting a value of zero will return the render to an un-regulated state
     * where the code will attempt to render as fast as possible (ie guaranteed
     * 100% CPU usage).
     *
     * @param cycleTime The minimum time in milliseconds between frames or zero
     */
    public void setMinimumFrameInterval(int cycleTime);

    /**
     * Fetch the currently set duty cycle value.
     *
     * @return The duty cycle time, in milliseconds
     */
    public int getMinimumFrameInterval();

    /**
     * Add a dislay collection to be managed. A duplicate registration
     * or null value is ignored.
     *
     * @param collection The new collection instance to be added
     * @throws IllegalStateException The system is currently management and
     *   should be disabled first.
     */
    public void addDisplay(DisplayCollection collection)
        throws IllegalStateException;

    /**
     * Remove an already registered display collection from the manager. A or
     * null value or one that is not currently registered is ignored.
     *
     * @param collection The collection instance to be removed
     * @throws IllegalStateException The system is currently management and
     *   should be disabled first.
     */
    public void removeDisplay(DisplayCollection collection)
        throws IllegalStateException;

    /**
     * Get the picking handler instance that is registered with the system.
     *
     * @return the current instance of the picking system
     */
    public PickingManager getPickingManager();

    /**
     * Set the picking handler to use. Overrides the default implementation
     * that is used. If null is passed, the code reverts to the default
     * implementation. This can only be set when the manager is not active
     *
     * @param mgr The new pick manager instance to use, or null
     * @throws IllegalStateException The system is currently management and
     *   should be disabled first.
     */
    public void setPickingManager(PickingManager mgr)
        throws IllegalStateException;

    /**
     * Register an observer that can be used to know when the application is
     * safe to update the scene graph. A value of null will remove the
     * currently set value.
     *
     * @param obs The observer instance to use
     */
    public void setApplicationObserver(ApplicationUpdateObserver obs);

    /**
     * Disable the internal shutdown hook system. It will be up to the calling
     * application to make sure the {@link #shutdown()} method is called to
     * turn off the OpenGL management system. If it does not, there is a good
     * possibility of a crash of the system.
     * <p>
     *
     * If the internal shutdown is disabled, then the shutdown callback of the
     * <code>ApplicationUpdateObserver</code> will not be called.
     */
    public void disableInternalShutdown();

    /**
     * Notification to shutdown the internals of the renderer because the
     * application is about to exit.
     */
    public void shutdown();
}
