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

package org.j3d.aviatrix3d.management;

// External imports
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;

import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;
import org.j3d.util.HashSet;
import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.picking.PickingManager;

import org.j3d.aviatrix3d.rendering.DeletableRenderable;
import org.j3d.aviatrix3d.rendering.ShaderSourceRenderable;

/**
 * Implementation of the {@link RenderManager} that uses a single thread
 * for all of the processing steps.
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
 * @version $Revision: 1.26 $
 */
public class SingleThreadRenderManager
    implements Runnable, NodeUpdateHandler, RenderManager
{
    /** Message when trying to call renderOnce() while this is active */
    private static final String ACTIVE_RENDERING_PROP =
		"org.j3d.aviatrix3d.management.SingleThreadRenderManager.invalidSetTimingMsg";

    /** Message when we've caught an error in userland code */
    private static final String USER_UPDATE_ERR_PROP =
		"org.j3d.aviatrix3d.management.SingleThreadRenderManager.appUpdateCallbackErrMsg";

    /** Message when we've caught an error during the appShutdown callback */
    private static final String USER_SHUTDOWN_ERR_PROP =
		"org.j3d.aviatrix3d.management.SingleThreadRenderManager.userShutdownCallbackErrMsg";

    /**
     * Message when the data change listener callback attempts to add
     * another  data listener during processing.
     */
    private static final String DATA_TIMING_PROP =
		"org.j3d.aviatrix3d.management.SingleThreadRenderManager.dataSetTimingMsg";

    /**
     * Message when the bounds change listener callback attempts to add
     * another  data listener during processing.
     */
    private static final String BOUNDS_TIMING_PROP =
		"org.j3d.aviatrix3d.management.SingleThreadRenderManager.boundsSetTimingMsg";

    /**
     * Message when the data change listener callback attempts to add
     * another shader init during processing.
     */
    private static final String SHADER_INIT_TIMING_PROP =
		"org.j3d.aviatrix3d.management.SingleThreadRenderManager.shaderInitTimingMsg";

    /**
     * Message when the data change listener callback attempts to add
     * another shader log request during processing.
     */
    private static final String SHADER_LOG_TIMING_PROP =
		"org.j3d.aviatrix3d.management.SingleThreadRenderManager.shaderLogTimingMsg";

    /**
     * Message when there was an exception generated in the user-land
     * dataChanged() callback.
     */
    private static final String DATA_CALLBACK_ERROR_PROP =
		"org.j3d.aviatrix3d.management.SingleThreadRenderManager.dataUpdateCallbackErrMsg";

    /**
     * Message when there was an exception generated in the user-land
     * boundsChanged() callback.
     */
    private static final String BOUNDS_CALLBACK_ERROR_PROP =
		"org.j3d.aviatrix3d.management.SingleThreadRenderManager.boundsUpdateCallbackErrMsg";

    /**
     * Message when there was an exception generated in the internal code
     * that implements the updateBoundsAndNotify() callback.
     */
    private static final String BOUNDS_UPDATE_ERROR_PROP =
		"org.j3d.aviatrix3d.management.SingleThreadRenderManager.boundsUpdateErrMsg";

    /**
     * Message when there was an exception generated in the internal code
     * that implements the disableActiveAudioState() callback.
     */
    private static final String SOUND_UPDATE_ERROR_PROP =
		"org.j3d.aviatrix3d.management.SingleThreadRenderManager.soundUpdateErrMsg";

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

    /** All the displays that we should be managing */
    private ArrayList<DisplayCollection> displays;

    /** Displays that are pending for addition at the start of the next loop */
    private ArrayList<DisplayCollection> addedDisplays;

    /** Displays that are pending for removal at the start of the next loop */
    private ArrayList<DisplayCollection> removedDisplays;

    /** Displays that have failed to draw and are now considered "broken" */
    private ArrayList<DisplayCollection> usableDisplays;

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

    /** Queue for holding deleted textures */
    private DeletableRenderable[] deletionQueue;

    /** The number of items to delete this frame */
    private int numDeletables;

    /** Flag for the writable state used by the NodeUpdateHandler query */
    private boolean pickPermitted;

    /** The current object that is having the update callback called */
    private Object writableBoundsObject;

    /** The current object that is having the update callback called */
    private Object writableDataObject;

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
     * Construct a new render manager with no graphicsPipelines or renderers
     * registered.
     */
    public SingleThreadRenderManager()
    {
        enabled = false;
        terminate = false;
        processing = false;

        pickPermitted = false;
        sceneChanged = false;
        haltOnError = true;

        displays = new ArrayList<DisplayCollection>();
        addedDisplays = new ArrayList<DisplayCollection>();
        removedDisplays = new ArrayList<DisplayCollection>();
        usableDisplays = new ArrayList<DisplayCollection>();

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

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        lastDataChangeItem = 0;
        lastBoundsChangeItem = 0;
        lastShaderInitItem = 0;
        lastShaderLogItem = 0;
        numDeletables = 0;

        minimumCycleTime = 0;

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

    @Override
    public void setErrorReporter(ErrorReporter reporter)
    {
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter = reporter;

        pickHandler.setErrorReporter(errorReporter);
    }

    @Override
    public void setHaltOnError(boolean state)
    {
        haltOnError = state;
    }

    @Override
    public boolean isHaltingOnError()
    {
        return haltOnError;
    }

    @Override
    public synchronized void setEnabled(boolean state)
    {
        if(enabled == state)
        {
            return;
        }

        if(state)
        {
            enabled = true;
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
            pickPermitted = true;
        }

        for(int i = 0; i < displays.size() && !terminate; i++)
        {
            DisplayCollection c = displays.get(i);
            c.setEnabled(state);
            if (state && !usableDisplays.contains(c))
            {
                usableDisplays.add(c);
            }
        }
    }

    @Override
    public boolean isEnabled()
    {
        return enabled;
    }

    @Override
    public synchronized void renderOnce()
        throws IllegalStateException
    {
        if(enabled)
		{
			I18nManager intl_mgr = I18nManager.getManager();
			String msg = intl_mgr.getString(ACTIVE_RENDERING_PROP);
            throw new IllegalStateException(msg);
		}

        processDisplayChanges();
        processChangeList();
        processShaderLists();

        for(int i = 0; i < displays.size(); i++)
        {
            DisplayCollection c = displays.get(i);
            c.process();
        }
    }

    @Override
    public void requestFullSceneRender()
    {
        sceneChanged = true;
    }

    @Override
    public void setMinimumFrameInterval(int cycleTime)
    {
        minimumCycleTime = cycleTime;
    }

    @Override
    public int getMinimumFrameInterval()
    {
        return minimumCycleTime;
    }

    @Override
    public void addDisplay(DisplayCollection collection)
    {
        if(collection == null || displays.contains(collection) ||
           addedDisplays.contains(collection))
        {
            removedDisplays.remove(collection);
            return;
        }

        addedDisplays.add(collection);
        removedDisplays.remove(collection);

    }

    @Override
    public void removeDisplay(DisplayCollection collection)
    {
        if(collection == null)
            return;

        removedDisplays.add(collection);
        addedDisplays.remove(collection);
    }

    @Override
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

    @Override
    public void setApplicationObserver(ApplicationUpdateObserver obs)
    {
        observer = obs;
    }

    @Override
    public void disableInternalShutdown()
    {
        if(shutdownThread != null)
        {
            AccessController.doPrivileged(
                new PrivilegedAction<Object>()
                {
                    @Override
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

    @Override
    public synchronized void shutdown()
    {
        // If this has already been called once, ignore it. Most of the
        // variables will have been nulled out by now.
        if(terminate || !enabled)
            return;

        terminate = true;

        setEnabled(false);

        // The setEnabled() call above may have resulted in the thread exiting
        // before we get to this line of code, resulting in runtimeThread
        // already being null.
        if(runtimeThread != null)
            runtimeThread.interrupt();

        for(int i = 0; i < displays.size(); i++)
        {
            DisplayCollection c = displays.get(i);
            c.shutdown();
        }

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
    @Override
    public void run()
    {
        boolean valid_drawable = true;
        while(enabled && !terminate && valid_drawable)
        {
            // Throttle the apps runtime
            long start_time = System.currentTimeMillis();

            processDisplayChanges();

            if(terminate)
                break;

            if(!terminate && observer != null)
            {
                pickPermitted = true;

                for(int i = 0; i < displays.size() && !terminate; i++)
                {
                    DisplayCollection c = displays.get(i);
                    c.enableLayerChange(true);
                }

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

                for(int i = 0; i < displays.size() && !terminate; i++)
                {
                    DisplayCollection c = displays.get(i);
                    c.enableLayerChange(false);
                }
            }

            if(terminate)
                break;

            // TODO: Need to handle deleted Audio objects?

            // Look out for any deleted objects and hand them on if needed.
            for(int i = 0; i < displays.size() && !terminate; i++)
            {
                DisplayCollection c = displays.get(i);
                c.queueDeletedObjects(deletionQueue, numDeletables);
                for(int j = numDeletables - 1; j >= 0; j--)
                {
                    deletionQueue[j] = null;
                }
                numDeletables = 0;
            }

            if(terminate)
                break;

            // Send to the graphicsDevice any shaders we have received
            // requests for
            processShaderLists();

            if(terminate)
                break;

            // Can we skip the processing if nothing changed last frame?
            if((lastDataChangeItem == 0) && (lastBoundsChangeItem == 0) &&
                !sceneChanged)
            {
                for(int i = 0; i < usableDisplays.size() && !terminate; i++)
                {
                    DisplayCollection c = usableDisplays.get(i);

                    if(!c.displayOnly())
                    {
                        usableDisplays.remove(c);
                        i--;
                    }
                }

                //if(usableDisplays.size() == 0)
                //{
                    // rem: preventing the thread from exiting in
                    // case the dc has not been enable in time
                    //valid_drawable = false;
                //}
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

                for(int i = 0; i < usableDisplays.size() && !terminate; i++)
                {
                    DisplayCollection c = usableDisplays.get(i);
                    if(!c.process())
                    {
                        usableDisplays.remove(c);
                        i--;
                    }
                }

                //if(usableDisplays.size() == 0)
                //{
                    // rem: preventing the thread from exiting in
                    // case the dc has not been enable in time
                    //valid_drawable = false;
                //}
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
                    if(terminate)
                        break;
                }
            }

            Thread.yield();
        }

        for(int i = 0; i < displays.size() && !terminate; i++)
        {
            DisplayCollection c = displays.get(i);
            c.shutdown();
        }

        runtimeThread = null;
    }

    //---------------------------------------------------------------
    // Methods defined by NodeUpdateHandler
    //---------------------------------------------------------------

    @Override
    public boolean isDataWritePermitted(Object src)
    {
        return (src == writableDataObject) || !enabled;
    }

    @Override
    public boolean isBoundsWritePermitted(Object src)
    {
        return (src == writableBoundsObject) || !enabled;
    }

    @Override
    public boolean isPickingPermitted()
    {
        return !enabled || pickPermitted;
    }

    @Override
    public void notifyUpdateRequired()
    {
        sceneChanged = true;
    }

    @Override
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
            boundsChangeListenerSet.contains(l))
        {
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

    @Override
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

    @Override
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

    @Override
    public PickingManager getPickingManager()
    {
        return pickHandler;
    }

    @Override
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

    @Override
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

    @Override
    public void requestDeletion(DeletableRenderable deletable)
    {
        resizeDeletionQueue();
        deletionQueue[numDeletables++] = deletable;
    }

    @Override
    public void rescindDeletionRequest(DeletableRenderable deletable)
    {
        for(int i = 0; i < numDeletables; i++)
        {
            if (deletionQueue[i] == deletable)
            {
                int lastDeletableIndex = numDeletables - 1;
                System.arraycopy(deletionQueue, (i + 1), deletionQueue, i, (lastDeletableIndex - i));
                deletionQueue[lastDeletableIndex] = null;
                numDeletables = lastDeletableIndex;
                break;
            }
        }
    }

    //---------------------------------------------------------------
    // Misc Internal methods
    //---------------------------------------------------------------

    /**
     * Process the added and removed displays now.
     */
    private void processDisplayChanges()
    {
        if(addedDisplays.size() != 0)
        {
            for(int i = 0; i < addedDisplays.size() && !terminate; i++)
            {
                DisplayCollection c = addedDisplays.get(i);

                c.setUpdateHandler(this);
                c.setEnabled(true);
                displays.add(c);
                usableDisplays.add(c);

                // We've had a new display added, so we want to force a new
                // run of the pipelines for all pipes.
                sceneChanged = true;
            }

            addedDisplays.clear();
        }

        if(terminate)
            return;

        if(removedDisplays.size() != 0)
        {
            for(int i = 0; i < removedDisplays.size() && !terminate; i++)
            {
                DisplayCollection c = removedDisplays.get(i);

                displays.remove(c);
                usableDisplays.remove(c);
                c.setEnabled(false);
                c.setUpdateHandler(null);

                // We've had a display removed, so we want to force a new
                // run of the pipelines for all pipes.
                sceneChanged = true;
            }

            removedDisplays.clear();
        }
    }

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
     * Process the shader lists and pass them on to the displays for processing
     * next frame.
     */
    private void processShaderLists()
    {
        if((lastShaderInitItem == 0) && (lastShaderLogItem == 0))
        {
            return;
        }

        for(int i = 0; i < displays.size() && !terminate; i++)
        {
            DisplayCollection c = displays.get(i);
            c.queueShaderObjects(shaderInitList,
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
    private void resizeDataChangeList()
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
    private void resizeBoundsChangeList()
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
    private void resizeShaderInitList()
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
    private void resizeShaderLogList()
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

    /**
     * Resize the array if needed. Marked as final in order to encourage the
     * compiler to inline the code for faster execution
     */
    private void resizeDeletionQueue()
    {
        if(numDeletables == deletionQueue.length)
        {
            int old_size = deletionQueue.length;
            int new_size = old_size + CHANGELIST_INCREMENT;

            DeletableRenderable[] tmp_nodes =
                new DeletableRenderable[new_size];

            System.arraycopy(deletionQueue, 0, tmp_nodes, 0, old_size);

            deletionQueue = tmp_nodes;
        }
    }
}
