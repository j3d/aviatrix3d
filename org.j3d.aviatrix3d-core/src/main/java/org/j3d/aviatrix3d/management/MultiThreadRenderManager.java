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

package org.j3d.aviatrix3d.management;

// External imports
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;

import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;
import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.HashSet;
import org.j3d.aviatrix3d.picking.PickingManager;

import org.j3d.aviatrix3d.rendering.DeletableRenderable;
import org.j3d.aviatrix3d.rendering.ShaderSourceRenderable;

/**
 * Implementation of the {@link RenderManager} that uses separate
 * threads for each of the pipelines that it is managing, allowing it to run
 * multiple output surfaces simultaneously (eg Powerwall, CAVE etc).
 * <p>
 *
 * By default the manager does not start of enabled. An explicit enable call
 * will be needed to kick the management process off.
 * <p>
 *
 * Change List processing will process bounds changed items before data
 * changed.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>invalidSetTimingMsg: Error message when calling whilst this manager is
 *     active</li>
 * <li>appUpdateCallbackErrMsg: Error message on user exception in appUpdate
 *     callback</li>
 * <li>userShutdownCallbackErrMsg: Error message on user exception in
 *     appShutdown callback</li>
 * <li>dataSetTimingMsg: Error message when attempting to call dataChanged()
 *     during data callback</li>
 * <li>boundsSetTimingMsg: Error message when attempting to call
 *     boundsChanged() during bounds callback</li>
 * <li>shaderLogTimingMsg: Error message when attempting to register another
 *     shader init during internal processing</li>
 * <li>dataUpdateCallbackErrMsg: Error message attempting to register another
 *     shader log request during internal processing</li>
 * <li>boundsUpdateCallbackErrMsg: Error message on user exception during the
 *     bounds update callback</li>
 * <li>boundsUpdateErrMsg: Error message user exception during the data update
 *     callback</li>
 * <li>soundUpdateErrMsg: Error message user exception during the sound update
 *     callback</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.10 $
 */
public class MultiThreadRenderManager
    implements Runnable,
               NodeUpdateHandler,
               RenderManager,
               PipelineStateObserver
{
    /** Message when trying to call renderOnce() while this is active */
    private static final String ACTIVE_RENDERING_PROP =
		"org.j3d.aviatrix3d.management.MultiThreadRenderManager.invalidSetTimingMsg";

    /** Message when we've caught an error in userland code */
    private static final String USER_UPDATE_ERR_PROP =
		"org.j3d.aviatrix3d.management.MultiThreadRenderManager.appUpdateCallbackErrMsg";

    /** Message when we've caught an error during the appShutdown callback */
    private static final String USER_SHUTDOWN_ERR_PROP =
		"org.j3d.aviatrix3d.management.MultiThreadRenderManager.userShutdownCallbackErrMsg";

    /**
     * Message when the data change listener callback attempts to add
     * another  data listener during processing.
     */
    private static final String DATA_TIMING_PROP =
		"org.j3d.aviatrix3d.management.MultiThreadRenderManager.dataSetTimingMsg";

    /**
     * Message when the bounds change listener callback attempts to add
     * another  data listener during processing.
     */
    private static final String BOUNDS_TIMING_PROP =
		"org.j3d.aviatrix3d.management.MultiThreadRenderManager.boundsSetTimingMsg";

    /**
     * Message when the data change listener callback attempts to add
     * another  data listener during processing.
     */
    private static final String SHADER_INIT_TIMING_PROP =
		"org.j3d.aviatrix3d.management.MultiThreadRenderManager.shaderInitTimingMsg";

    /**
     * Message when the data change listener callback attempts to add
     * another  data listener during processing.
     */
    private static final String SHADER_LOG_TIMING_PROP =
		"org.j3d.aviatrix3d.management.MultiThreadRenderManager.shaderLogTimingMsg";

    /**
     * Message when there was an exception generated in the user-land
     * dataChanged() callback.
     */
    private static final String DATA_CALLBACK_ERROR_PROP =
		"org.j3d.aviatrix3d.management.MultiThreadRenderManager.dataUpdateCallbackErrMsg";

    /**
     * Message when there was an exception generated in the user-land
     * boundsChanged() callback.
     */
    private static final String BOUNDS_CALLBACK_ERROR_PROP =
		"org.j3d.aviatrix3d.management.MultiThreadRenderManager.boundsUpdateCallbackErrMsg";

    /**
     * Message when there was an exception generated in the internal code
     * that implements the updateBoundsAndNotify() callback.
     */
    private static final String BOUNDS_UPDATE_ERROR_PROP =
		"org.j3d.aviatrix3d.management.MultiThreadRenderManager.boundsUpdateErrMsg";

    /**
     * Message when there was an exception generated in the internal code
     * that implements the disableActiveAudioState() callback.
     */
    private static final String SOUND_UPDATE_ERROR_PROP =
		"org.j3d.aviatrix3d.management.MultiThreadRenderManager.soundUpdateErrMsg";


    /** The initial size of the children list */
    private static final int CHANGELIST_START_SIZE = 200;

    /** The increment size of the list if it gets overflowed */
    private static final int CHANGELIST_INCREMENT = 100;

    /** The initial size of the shader init/log lists */
    private static final int SHADERLIST_START_SIZE = 64;

    /** The increment size of the init/log list if it gets overflowed */
    private static final int SHADERLIST_INCREMENT = 32;

    /** The change requestors for data changed sets */
    private NodeUpdateListener[] dataChangeList;

    /** The change requestors for bounds changed sets */
    private NodeUpdateListener[] boundsChangeList;

    /** The source objects for data changed sets */
    private Object[] dataSourceList;

    /** The source objects for bounds changed sets */
    private Object[] boundsSourceList;

    /** The internal requestors for bounds changed sets */
    private InternalNodeUpdateListener[] boundsInternalList;

    /** The currently active layer for sound rendering */
    private InternalLayerUpdateListener activeSoundLayer;

    /** The current place to add change requestors */
    private int lastDataChangeItem;

    /** The current place to add change requestors */
    private int lastBoundsChangeItem;

    /** HashSet to determine duplicates in the change List */
    private HashSet dataChangeListenerSet;

    /** HashSet to determine duplicates in the change List */
    private HashSet dataChangeSrcSet;

    /** HashSet to determine duplicates in the change List */
    private HashSet boundsChangeListenerSet;

    /** HashSet to determine duplicates in the change List */
    private HashSet boundsChangeSrcSet;

    /** The change requestors for data changed sets */
    private ShaderSourceRenderable[] shaderInitList;

    /** The change requestors for bounds changed sets */
    private ShaderSourceRenderable[] shaderLogList;

    /** The current place to add shader init requestors */
    private int lastShaderInitItem;

    /** The current place to add shader log requestors */
    private int lastShaderLogItem;

    /** Set of shaders that require init processing */
    private HashSet shaderInitSet;

    /** Set of shaders that require log fetch processing */
    private HashSet shaderLogSet;

    /** The collection of display collections to manage */
    private ArrayList<DisplayCollection> displays;

    /** Threads that manage a pipeline. Thread corresponds to pipeline index */
    private DisplayCollectionThread[] displayThread;

    /** Number of valid display threads to process */
    private int numDisplayThreads;

    /** Mapping from the collection to its controlling thread */
    private HashMap<DisplayCollection, DisplayCollectionThread> displayThreadMap;

    /** Picking handler for managing the picking */
    private PickingManager pickHandler;

    /** The minimum frame cycle time */
    private int minimumCycleTime;

    /** Current enabled state */
    private boolean enabled;

    /** The thread that contains this runnable */
    private Thread runtimeThread;

    /** Flag that the runtime thread should be terminated at next chance */
    private boolean terminate;

    /** The external observer for keeping updates in check */
    private ApplicationUpdateObserver observer;

    /** Queue for holding deleted textures and other objects */
    private DeletableRenderable[] deletionQueue;

    /** The number of items to delete */
    private int numDeletables;

    /** Flag for the writable state used by the NodeUpdateHandler query */
    private boolean pickPermitted;

    /** The current object that is having the update callback called */
    private Object writableBoundsObject;

    /** The current object that is having the update callback called */
    private Object writableDataObject;

    /**
     * Lock object to prevent frame callbacks from finishing before we've
     * called all the other threads this frame.
     */
    private Object frameFinishLock;

    /** Per-frame counter used to track when a frame is complete */
    private int completedFrameCount;

    /** Object to synchronise between the various management threads */
    private Object renderWaitLock;

    /** Are we processing the change list */
    private boolean processing;

    /**
     * Flag indicating if we should halt the management cycle if an error is
     * detected during the management loop process. Set to true by default.
     */
    private boolean haltOnError;

    /**
     * Flag indicating that the scene has changed and it should be completely
     * rendered and not just skip to the display part.
     */
    private boolean sceneChanged;

    /** The thread used to handle the system shutdown hook */
    private Thread shutdownThread;

    /** Error reporter used to send out messages */
    private ErrorReporter errorReporter;

    /**
     * Construct a new render manager with no pipelines or renderers
     * registered. Starts by allocating space for 2 render pipes and
     * 1 audio pipe to be handled.
     */
    public MultiThreadRenderManager()
    {
        this(2);
    }

    /**
     * Construct a new render manager with no pipelines or renderers
     * registered, but with internal arrays setup for the given number of
     * displays to be added.
     *
     * @param numDisplays The initial number of displays to prepare
     */
    public MultiThreadRenderManager(int numDisplays)
    {
        enabled = false;
        terminate = false;
        processing = false;

        pickPermitted = false;
        sceneChanged = false;
        haltOnError = true;

        displays = new ArrayList<DisplayCollection>(numDisplays);
        displayThreadMap =
            new HashMap<DisplayCollection, DisplayCollectionThread>();
        displayThread = new DisplayCollectionThread[numDisplays];

        renderWaitLock = new Object();
        frameFinishLock = new Object();

        dataChangeList = new NodeUpdateListener[CHANGELIST_START_SIZE];
        dataSourceList = new Object[CHANGELIST_START_SIZE];
        dataChangeListenerSet = new HashSet(CHANGELIST_START_SIZE);
        dataChangeSrcSet = new HashSet(CHANGELIST_START_SIZE);
        boundsChangeList = new NodeUpdateListener[CHANGELIST_START_SIZE];
        boundsInternalList =
            new InternalNodeUpdateListener[CHANGELIST_START_SIZE];
        boundsSourceList = new Object[CHANGELIST_START_SIZE];
        boundsChangeListenerSet = new HashSet(CHANGELIST_START_SIZE);
        boundsChangeSrcSet = new HashSet(CHANGELIST_START_SIZE);
        shaderInitList = new ShaderSourceRenderable[SHADERLIST_START_SIZE];
        shaderInitSet = new HashSet(SHADERLIST_START_SIZE);
        shaderLogList = new ShaderSourceRenderable[SHADERLIST_START_SIZE];
        shaderLogSet = new HashSet(SHADERLIST_START_SIZE);

        pickHandler = new DefaultPickingHandler();
        deletionQueue = new DeletableRenderable[CHANGELIST_START_SIZE];

        lastDataChangeItem = 0;
        lastBoundsChangeItem = 0;
        lastShaderInitItem = 0;
        lastShaderLogItem = 0;

        minimumCycleTime = 0;
        completedFrameCount = 0;

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        shutdownThread = new ShutdownThread(this);
        AccessController.doPrivileged(
            new PrivilegedAction<Object>()
            {
                public Object run()
                {
                        Runtime rt = Runtime.getRuntime();
                        rt.addShutdownHook(shutdownThread);
                        return null;
                }
            }
        );
    }

    //---------------------------------------------------------------
    // Methods defined by RenderManager
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

        pickHandler.setErrorReporter(errorReporter);
    }

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
    public void setHaltOnError(boolean state)
    {
        haltOnError = state;
    }

    /**
     * Check to see the current halt on error state.
     *
     * @return true if the system halts on an error condition
     * @see #setHaltOnError
     */
    public boolean isHaltingOnError()
    {
        return haltOnError;
    }

    /**
     * Tell render to start or stop management. If currently running, it
     * will wait until all the pipelines have completed their current cycle
     * and will then halt.
     *
     * @param state True if to enable management
     */
    public synchronized void setEnabled(boolean state)
    {
        if(enabled == state)
            return;

        if(state)
        {
            enabled = true;
            terminate = false;
            pickPermitted = false;

            if(runtimeThread == null)
            {
                ThreadPrivilegedAction tpa = new ThreadPrivilegedAction(this);
                runtimeThread = tpa.getThread();
            }
        }
        else
        {
            enabled = false;
            terminate = true;
            pickPermitted = true;

            for(int i = 0; i < numDisplayThreads; i++)
                displayThread[i].halt();
        }
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
     * Force a single render of all pipelines now. Ignores the enabled and
     * cycle time settings to cause a single render at this point in time.
     * If a render is currently in progress, an exception is generated
     *
     * @throws IllegalStateException The system is currently management and
     *   should be disabled first.
     */
    public synchronized void renderOnce()
        throws IllegalStateException
    {
        if(enabled)
		{
			I18nManager intl_mgr = I18nManager.getManager();
			String msg = intl_mgr.getString(ACTIVE_RENDERING_PROP);
            throw new IllegalStateException(msg);
		}

        processChangeList();
        processShaderLists();

        for(int i = 0; i < numDisplayThreads; i++)
            displayThread[i].render();
    }

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
    public void requestFullSceneRender()
    {
        sceneChanged = true;
    }

    /**
     * Set the minimum duty cycle of the render manager. This is the type in
     * milliseconds that should be the minimum between frames and can be used
     * to throttle the management loop to a maximum frame rate should other
     * systems require CPU time.
     *
     * @param cycleTime The minimum time in milliseconds between frames
     */
    public void setMinimumFrameInterval(int cycleTime)
    {
        minimumCycleTime = cycleTime;
    }

    /**
     * Fetch the currently set duty cycle value.
     *
     * @return The duty cycle time, in milliseconds
     */
    public int getMinimumFrameInterval()
    {
        return minimumCycleTime;
    }

    /**
     * Add a dislay collection to be managed. A duplicate registration
     * or null value is ignored.
     *
     * @param collection The new collection instance to be added
     * @throws IllegalStateException The system is currently management and
     *   should be disabled first.
     */
    public void addDisplay(DisplayCollection collection)
        throws IllegalStateException
    {
        if(enabled)
		{
			I18nManager intl_mgr = I18nManager.getManager();
			String msg = intl_mgr.getString(ACTIVE_RENDERING_PROP);
            throw new IllegalStateException(msg);
		}

        if(collection == null)
            return;

        if(numDisplayThreads == displayThread.length)
        {
            DisplayCollectionThread[] tmp =
                new DisplayCollectionThread[numDisplayThreads + 2];
            System.arraycopy(displayThread, 0, tmp, 0, numDisplayThreads);
            displayThread = tmp;
        }

        displays.add(collection);
        displayThread[numDisplayThreads] =
            new DisplayCollectionThread(collection);
        displayThread[numDisplayThreads].setStateObserver(this);

        displayThreadMap.put(collection, displayThread[numDisplayThreads]);

        collection.setEnabled(true);
        numDisplayThreads++;
    }

    /**
     * Remove an already registered display collection from the manager. A or
     * null value or one that is not currently registered is ignored.
     *
     * @param collection The collection instance to be removed
     * @throws IllegalStateException The system is currently management and
     *   should be disabled first.
     */
    public void removeDisplay(DisplayCollection collection)
        throws IllegalStateException
    {
        if(enabled)
		{
			I18nManager intl_mgr = I18nManager.getManager();
			String msg = intl_mgr.getString(ACTIVE_RENDERING_PROP);
            throw new IllegalStateException(msg);
		}

        if(!displays.remove(collection))
            return;

        collection.setEnabled(false);

        DisplayCollectionThread th =
            (DisplayCollectionThread)displayThreadMap.remove(collection);

        for(int i = 0; i < numDisplayThreads; i++)
        {
            if(displayThread[i] == th)
            {
                displayThread[i].shutdown();
                displayThread[i].setStateObserver(null);

                System.arraycopy(displayThread,
                                 i + 1,
                                 displayThread,
                                 i,
                                 numDisplayThreads - i - 1);

                numDisplayThreads--;
                break;
            }
        }
    }

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
        throws IllegalStateException
    {
        if(enabled)
		{
			I18nManager intl_mgr = I18nManager.getManager();
			String msg = intl_mgr.getString(ACTIVE_RENDERING_PROP);
            throw new IllegalStateException(msg);
		}

        pickHandler = mgr;

        if(pickHandler == null)
            pickHandler = new DefaultPickingHandler();
    }

    /**
     * Register an observer that can be used to know when the application is
     * safe to update the scene graph. A value of null will remove the
     * currently set value.
     *
     * @param obs The observer instance to use
     */
    public void setApplicationObserver(ApplicationUpdateObserver obs)
    {
        observer = obs;
    }

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
    public void disableInternalShutdown()
    {
        if(shutdownThread != null)
        {
            AccessController.doPrivileged(
                new PrivilegedAction<Object>()
                {
                    public Object run()
                    {
                        Runtime rt = Runtime.getRuntime();
                        rt.removeShutdownHook(shutdownThread);
                        return null;
                    }
                }
            );

            shutdownThread = null;
        }
    }

    /**
     * Notification to shutdown the internals of the renderer because the
     * application is about to exit.
     */
    public synchronized void shutdown()
    {
        // If this has already been called once, ignore it. Most of the
        // variables will have been nulled out by now.
        if(terminate || !enabled)
            return;

        terminate = true;

        setEnabled(false);

        // If we have a shutdown thread then that means
        if((shutdownThread != null) && (observer != null))
        {
            try
            {
                observer.appShutdown();
            }
            catch(Exception e)
            {
				I18nManager intl_mgr = I18nManager.getManager();
				String msg = intl_mgr.getString(USER_SHUTDOWN_ERR_PROP);
                errorReporter.errorReport(msg, e);
            }

            observer = null;
        }
    }

    //---------------------------------------------------------------
    // Methods defined by Runnable
    //---------------------------------------------------------------

    /**
     * Run method used to synchronise the internal management state and the
     * external state of the canvas. Should never be called directly.
     */
    public void run()
    {
        for(int i = 0; i < numDisplayThreads; i++)
        {
            if(!displayThread[i].isAlive())
                displayThread[i].start();
        }

        while(!terminate)
        {
            // Throttle the apps runtime
            long start_time = System.currentTimeMillis();

            if(!terminate && observer != null)
            {
                pickPermitted = true;

                for(int i = 0; i < numDisplayThreads && !terminate; i++)
                    displayThread[i].enableLayerChange(true);

                try
                {
                    observer.updateSceneGraph();
                }
                catch(Exception e)
                {
					I18nManager intl_mgr = I18nManager.getManager();
					String msg = intl_mgr.getString(USER_UPDATE_ERR_PROP);
                    errorReporter.errorReport(msg, e);

                    if(haltOnError)
                    {
                        enabled = false;
                        return;
                    }
                }
                finally
                {
                    pickPermitted = false;
                }

                for(int i = 0; i < numDisplayThreads && !terminate; i++)
                    displayThread[i].enableLayerChange(true);
            }

            if(terminate)
                break;

            // Look out for any deleted objects and hand them on if needed.
            for(int i = 0; i < numDeletables; i++)
            {
                for(int j = 0; j < numDisplayThreads; j++)
                    displayThread[j].queueDeletedObjects(deletionQueue,
                                                         numDeletables);
            }

            if(terminate)
                break;

            processShaderLists();

            if(terminate)
                break;

            synchronized(frameFinishLock)
            {
                // Can we skip the processing if nothing changed last frame?
                if((lastDataChangeItem == 0) && (lastBoundsChangeItem == 0) &&
                   !sceneChanged)
                {
                    for(int i = 0; i < numDisplayThreads && !terminate; i++)
                        displayThread[i].displayOnly();
                }
                else
                {
                    if(!processChangeList() && haltOnError)
                    {
                        enabled = false;
                        return;
                    }

                    if(terminate)
                        break;

                    sceneChanged = false;

                    for(int i = 0; i < numDisplayThreads && !terminate; i++)
                        displayThread[i].render();
                }

                if(terminate)
                    break;
            }

            // Hang while we wait for notification that all the management
            // threads have finished before swapping all the buffers.
            if(!terminate && numDisplayThreads != 0)
            {
                try
                {
                    synchronized(renderWaitLock)
                    {
                        renderWaitLock.wait();
                    }
                }
                catch(InterruptedException ie)
                {
                }
            }

            if(terminate)
                break;

            long now = System.currentTimeMillis();
            long diff = now - start_time;

            if(diff < minimumCycleTime)
            {
                try
                {
                    Thread.sleep(minimumCycleTime - diff);
                }
                catch(InterruptedException ie)
                {
                }
            }

            Thread.yield();
        }

        runtimeThread = null;
    }

    //---------------------------------------------------------------
    // Methods defined by PipelineStateObserver
    //---------------------------------------------------------------

    /**
     * Notification that the frame state has finished management.
     */
    public synchronized void frameFinished()
    {
        synchronized(frameFinishLock)
        {
            if(++completedFrameCount >= numDisplayThreads)
            {
                synchronized(renderWaitLock)
                {
                    completedFrameCount = 0;
                    renderWaitLock.notify();
                }
            }
        }
    }

    //---------------------------------------------------------------
    // Methods defined by NodeUpdateHandler
    //---------------------------------------------------------------

    /**
     * Check to see if writing to the node is permitted currently.
     *
     * @param src The object that is requesting the check
     * @return true if the end user can write, false if not
     */
    public boolean isDataWritePermitted(Object src)
    {
        return (src == writableDataObject) || !enabled;
    }

    /**
     * Check to see if writing to the node is permitted currently.
     *
     * @param src The object that is requesting the check
     * @return true if the end user can write, false if not
     */
    public boolean isBoundsWritePermitted(Object src)
    {
        return (src == writableBoundsObject) || !enabled;
    }

    /**
     * Check to see if picking is permitted currently.
     *
     * @return true if the end user can pick, false if not
     */
    public boolean isPickingPermitted()
    {
        return !enabled || pickPermitted;
    }

    /**
     * Feedback to the internals that a data object has changed and it requires
     * re-management. This should only be called by classes that can effect the
     * management but don't normally use the data/bounds write listeners (ie
     * changes are made during the app update portion of the scene graph).
     * Typically this would be used for things like the {@link ViewEnvironment}
     * changing the aspect ratio etc.
     */
    public void notifyUpdateRequired()
    {
        sceneChanged = true;
    }

    /**
     * Notify the handler that you have updates to the SG that might alter
     * a node's bounds.
     *
     * @param l The change requestor
     * @param src The object that is passing this listener through.
     * @param intL Internal listener for making callbacks at a later time
     *    to propogate the bounds changes.
     * @throws InvalidListenerSetTimingException If called when the node called
     *    during one of the bounds/data changed callbacks
     */
    public synchronized boolean boundsChanged(NodeUpdateListener l,
                                              Object src,
                                              InternalNodeUpdateListener intL)
        throws InvalidListenerSetTimingException
    {
        if(processing)
		{
			I18nManager intl_mgr = I18nManager.getManager();
			String msg = intl_mgr.getString(BOUNDS_TIMING_PROP);
            throw new InvalidListenerSetTimingException(msg);
		}

        // Check for duplicates
        if(boundsChangeSrcSet.contains(src) &&
           boundsChangeListenerSet.contains(l)) {
            return false;
        }

        boundsChangeListenerSet.add(l);
        boundsChangeSrcSet.add(src);

        // Add to a single ChangeList for now
        resizeBoundsChangeList();
        boundsChangeList[lastBoundsChangeItem] = l;
        boundsInternalList[lastBoundsChangeItem] = intL;
        boundsSourceList[lastBoundsChangeItem] = src;
        lastBoundsChangeItem++;

        return true;
    }

    /**
     * Notify the handler that you have updates to the SG that will not
     * alter a node's bounds.
     *
     * @param l The change requestor
     * @param src The object that is passing this listener through.
     * @throws InvalidListenerSetTimingException If called when the node called
     *    during one of the bounds/data changed callbacks
     */
    public synchronized void dataChanged(NodeUpdateListener l, Object src)
        throws InvalidListenerSetTimingException
    {
        if(processing)
		{
			I18nManager intl_mgr = I18nManager.getManager();
			String msg = intl_mgr.getString(DATA_TIMING_PROP);
            throw new InvalidListenerSetTimingException(msg);
		}

        // Check for duplicates
        if(dataChangeSrcSet.contains(src) &&
           dataChangeListenerSet.contains(l))
            return;

        dataChangeListenerSet.add(l);
        dataChangeSrcSet.add(src);

        // Add to a single ChangeList for now
        resizeDataChangeList();
        dataChangeList[lastDataChangeItem] = l;
        dataSourceList[lastDataChangeItem] = src;
        lastDataChangeItem++;
    }

    /**
     * Notify the handler that you are now going to be the active layer for
     * sound rendering. Note that updating the active sound node means that
     * the other sound node is disabled. This will be called on the data change
     * callback normally. The source object will be an instance of either
     * Layer or ViewportLayer, depending on the circumstances.
     *
     * @param intL Internal listener for making callbacks at a later time
     *    to propogate when the target is no longer the active listener.
     */
    public void activeSoundLayerChanged(InternalLayerUpdateListener intL)
        throws InvalidListenerSetTimingException
    {
        // No need to do anything if we're resetting ourselves.
        if(activeSoundLayer == intL)
            return;

        if(activeSoundLayer != null)
        {
            try
            {
                activeSoundLayer.disableActiveAudioState();
            }
            catch(Exception e)
            {
				I18nManager intl_mgr = I18nManager.getManager();
				String msg = intl_mgr.getString(SOUND_UPDATE_ERROR_PROP);
                errorReporter.errorReport(msg, e);
            }
        }

        activeSoundLayer = intL;
    }

    /**
     * The shader object passed requires an initialisation be performed. Queue
     * the shader up for processing now.
     *
     * @param shader The shader instance to queue
     * @param updateResponse true if this is being made as a response to a
     *    node's  setUpdateHandler() method
     */
    public void shaderRequiresInit(ShaderSourceRenderable shader,
                                   boolean updateResponse)
    {
        if(processing && !updateResponse)
		{
			I18nManager intl_mgr = I18nManager.getManager();
			String msg = intl_mgr.getString(SHADER_INIT_TIMING_PROP);
            throw new InvalidListenerSetTimingException(msg);
		}

        // Check for duplicates
        if(shaderInitSet.contains(shader))
            return;

        shaderInitSet.add(shader);

        resizeShaderInitList();
        shaderInitList[lastShaderInitItem] = shader;
        lastShaderInitItem++;
    }

    /**
     * The shader object passed requires updating the log info. Queue
     * the shader up for processing now so that at the next oppourtunity it
     * can call glGetLogInfoARB.
     *
     * @param shader The shader instance to queue
     * @param updateResponse true if this is being made as a response to a
     *   node's  setUpdateHandler() method
     */
    public void shaderRequiresLogInfo(ShaderSourceRenderable shader,
                                      boolean updateResponse)
    {
        if(processing && !updateResponse)
		{
			I18nManager intl_mgr = I18nManager.getManager();
			String msg = intl_mgr.getString(SHADER_LOG_TIMING_PROP);
            throw new InvalidListenerSetTimingException(msg);
		}

        // Check for duplicates
        if(shaderLogSet.contains(shader))
            return;

        shaderLogSet.add(shader);

        resizeShaderLogList();
        shaderLogList[lastShaderLogItem] = shader;
        lastShaderLogItem++;
    }

    /**
     * Notification that the passed in renderable is wishing to mark itself
     * ready for deletion processing. For example, this could be because a
     * texture has had its contents replaced and needs to free up the old
     * texture object ID. The reasons why this object is now marked for
     * deletion are not defined - that is the sole discretion of the calling
     * code.
     * <p>
     *
     * This renderable instance will begin the deletion processing as soon
     * as the start of the next culling pass begins. Once it hits the output
     * device, deletion requests are guaranteed to be the first item that is
     * processed, before all other requests.
     * <p>
     * If the object is already in the queue, the request will be silently
     * ignored.
     *
     * @param deletable The renderable that will handle the cleanup at
     *     the appropriate time
     */
    public void requestDeletion(DeletableRenderable deletable)
    {
    }

    /**
     * Woops, we were in error, so please rescind that deletion request.
     * For example, during the data update change processing a texture was
     * reparented, first by deletion, then by addition to a new parent, this
     * would ensure that we don't continuing attempting to delete the texture
     * when we should not.
     * <p>
     *
     * You can only rescind request that has happened in this frame as the
     * delete requests are packaged up and sent off down the pipeline each
     * frame, then forgotten about during the rendering process.
     * <p>
     * Rescinding a request for an object no-longer in the queue (eg multiple
     * request, or just was never added in the first place), will be silently
     * ignored.
     *
     * @param deletable The renderable that should be removed from the queue.
     */
    public void rescindDeletionRequest(DeletableRenderable deletable)
    {
    }

    /**
     * Get the picking handler so that we can do some picking operations.
     *
     * @return the current instance of the picking system
     */
    public PickingManager getPickingManager()
    {
        return pickHandler;
    }

    //---------------------------------------------------------------
    // Misc Internal methods
    //---------------------------------------------------------------

    /**
     * Process the changeList now. If the user code generated an exception
     * during this time and haltOnError is true, then exit immediately and
     * return false. Otherwise return true.
     *
     * @return true if processing succeeded and not told to halt
     */
    private boolean processChangeList()
    {
        processing = true;

        for(int i = 0; i < lastBoundsChangeItem; i++)
        {
            writableBoundsObject = boundsSourceList[i];

            try
            {
                boundsChangeList[i].updateNodeBoundsChanges(boundsSourceList[i]);
            }
            catch(Exception e)
            {
				I18nManager intl_mgr = I18nManager.getManager();
				String msg = intl_mgr.getString(BOUNDS_CALLBACK_ERROR_PROP);
                errorReporter.errorReport(msg, e);

                if(haltOnError)
                    return false;
            }

            boundsChangeList[i] = null;
            boundsSourceList[i] = null;
        }

        writableBoundsObject = null;

        // Now go through and update all the bounds
        for(int i = 0; i < lastBoundsChangeItem; i++)
        {
            try
            {
                boundsInternalList[i].updateBoundsAndNotify();
            }
            catch(Exception e)
            {
				I18nManager intl_mgr = I18nManager.getManager();
				String msg = intl_mgr.getString(BOUNDS_UPDATE_ERROR_PROP);
                errorReporter.errorReport(msg, e);

                if(haltOnError)
                    return false;
            }

            boundsInternalList[i] = null;
        }

        lastBoundsChangeItem = 0;
        boundsChangeSrcSet.clear();
        boundsChangeListenerSet.clear();

        for(int i = 0; i < lastDataChangeItem; i++)
        {
            writableDataObject = dataSourceList[i];

            try
            {
                dataChangeList[i].updateNodeDataChanges(dataSourceList[i]);
            }
            catch(Exception e)
            {
				I18nManager intl_mgr = I18nManager.getManager();
				String msg = intl_mgr.getString(DATA_CALLBACK_ERROR_PROP);
                errorReporter.errorReport(msg, e);

                if(haltOnError)
                    return false;
            }

            dataChangeList[i] = null;
            dataSourceList[i] = null;
        }

        writableDataObject = null;
        lastDataChangeItem = 0;
        dataChangeSrcSet.clear();
        dataChangeListenerSet.clear();

        processing = false;

        return true;
    }

    /**
     * Process the shader lists and pass them on to the drawable for processing
     * next frame.
     */
    private void processShaderLists()
    {
        if((lastShaderInitItem == 0) && (lastShaderLogItem == 0))
            return;

        for(int i = 0; i < numDisplayThreads; i++)
        {
            displayThread[i].queueShaderObjects(shaderInitList,
                                                lastShaderInitItem,
                                                shaderLogList,
                                                lastShaderLogItem);
        }

        // now clear out the array.
        for(int i = 0; i < lastShaderInitItem; i++)
            shaderInitList[0] = null;

        for(int i = 0; i < lastShaderLogItem; i++)
            shaderLogList[0] = null;

        lastShaderInitItem = 0;
        lastShaderLogItem = 0;

        shaderInitSet.clear();
        shaderLogSet.clear();
    }

    /**
     * Resize the list if needed. Marked as final in order to encourage the
     * compiler to inline the code for faster execution
     */
    private final void resizeDataChangeList()
    {
        if((lastDataChangeItem + 1) == dataChangeList.length)
        {
            int old_size = dataChangeList.length;
            int new_size = old_size + CHANGELIST_INCREMENT;

            NodeUpdateListener[] tmp_nodes = new NodeUpdateListener[new_size];
            Object[] tmp_src = new Object[new_size];

            System.arraycopy(dataChangeList, 0, tmp_nodes, 0, old_size);
            System.arraycopy(dataSourceList, 0, tmp_src, 0, old_size);

            dataChangeList = tmp_nodes;
            dataSourceList = tmp_src;
        }
    }

    /**
     * Resize the list if needed. Marked as final in order to encourage the
     * compiler to inline the code for faster execution
     */
    private final void resizeBoundsChangeList()
    {
        if((lastBoundsChangeItem + 1) == boundsChangeList.length)
        {
            int old_size = boundsChangeList.length;
            int new_size = old_size + CHANGELIST_INCREMENT;

            NodeUpdateListener[] tmp_nodes = new NodeUpdateListener[new_size];
            InternalNodeUpdateListener[] tmp_int =
                new InternalNodeUpdateListener[new_size];
            Object[] tmp_src = new Object[new_size];

            System.arraycopy(boundsChangeList, 0, tmp_nodes, 0, old_size);
            System.arraycopy(boundsSourceList, 0, tmp_src, 0, old_size);
            System.arraycopy(boundsInternalList, 0, tmp_int, 0, old_size);

            boundsChangeList = tmp_nodes;
            boundsSourceList = tmp_src;
            boundsInternalList = tmp_int;
        }
    }


    /**
     * Resize the list if needed. Marked as final in order to encourage the
     * compiler to inline the code for faster execution
     */
    private final void resizeShaderInitList()
    {
        if((lastShaderInitItem + 1) == shaderInitList.length)
        {
            int old_size = shaderInitList.length;
            int new_size = old_size + SHADERLIST_INCREMENT;

            ShaderSourceRenderable[] tmp_nodes =
                new ShaderSourceRenderable[new_size];

            System.arraycopy(shaderInitList, 0, tmp_nodes, 0, old_size);

            shaderInitList = tmp_nodes;
        }
    }

    /**
     * Resize the list if needed. Marked as final in order to encourage the
     * compiler to inline the code for faster execution
     */
    private final void resizeShaderLogList()
    {
        if((lastShaderLogItem + 1) == shaderLogList.length)
        {
            int old_size = shaderLogList.length;
            int new_size = old_size + SHADERLIST_INCREMENT;

            ShaderSourceRenderable[] tmp_nodes =
                new ShaderSourceRenderable[new_size];

            System.arraycopy(shaderLogList, 0, tmp_nodes, 0, old_size);

            shaderLogList = tmp_nodes;
        }
    }
}
