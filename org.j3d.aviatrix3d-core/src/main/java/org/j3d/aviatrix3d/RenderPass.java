/*****************************************************************************
 *                   Yumetech, Inc Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d;

// External imports
import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.*;

/**
 * Representation of a single pass in a multipass rendering layer setup.
 * <p>
 *
 * A render pass encapsulates a viewpoint that is used to view the scene from,
 * the geometry structure to render, global fog, the geometry of the scene and
 * a collection of buffer states.
 * <p>
 *
 * Each pass has its own view environment that works in addition to the global
 * environment from the parent scene. Some aspects of this environment are
 * ignored for multipass rendering, such as the stereo flags.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>sharedVpMsg: Error message when the set background has more than one
 *     possible parent.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 2.12 $
 */
public class RenderPass extends SceneGraphObject
    implements RenderPassCullable
{
    /** Message when setting the active view if it contains a shared parent */
    private static final String SHARED_VP_PROP =
        "org.j3d.aviatrix3d.RenderPass.sharedVpMsg";

    /** Current view environment */
    private ViewEnvironment viewEnvironment;

    /** The Scene Graph renderableObjects */
    private Group renderableObjects;

    /** The current viewpoint instance */
    private Viewpoint currentViewpoint;

    /** The current fog instance */
    private Fog currentFog;

    /** Custom Viewport, if set */
    private MPViewportRenderable viewport;

    /** Buffer state for the stencil buffer */
    private StencilBufferState stencilState;

    /** Buffer state for the general buffer */
    private GeneralBufferState generalState;

    /** Buffer state for the depth buffer */
    private DepthBufferState depthState;

    /** Buffer state for the colour buffer */
    private ColorBufferState colorState;

    /** Buffer state for the accumulation buffer */
    private AccumulationBufferState accumState;

    /** Flag to describe whether this is the currently active sound layer */
    private boolean activeSoundLayer;

    /** Update handler for the external code. Not created until needed. */
    private InternalUpdater internalUpdater;

    /** Enabled state of this render pass */
    private boolean enabled;

    /**
     * Internal implementation of the InternalNodeUpdateListener. Done as an
     * inner class to hide the calls from public consumption.
     */
    private class InternalUpdater
        implements InternalLayerUpdateListener
    {

        /**
         * Notify this layer that it is no longer the active audio layer for
         * rendering purposes.
         */
        public void disableActiveAudioState()
        {
            activeSoundLayer = false;
        }
    }

    /**
     * Create a default instance of this scene with no content provided.
     */
    public RenderPass()
    {
        viewEnvironment = new ViewEnvironment();

        activeSoundLayer = false;
        enabled = true;
    }

    //----------------------------------------------------------
    // Methods defined by RenderPassCullable
    //----------------------------------------------------------

    /**
     * Check to see if this render pass is valid to render for this
     * frame.
     *
     * @return true if this is valid to process
     */
    @Override
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Check to see if this represents a 2D scene that has no 3D rendering
     * capabilities. A purely 2D scene sets up the view environment quite
     * different to a full 3D scene.
     *
     * @return true if this is a 2D scene rather than a 3D version
     */
    @Override
    public boolean is2D()
    {
        return false;
    }

    /**
     * Check to see if this render pass is the one that also has the
     * spatialised audio to be rendered for this frame. See the package
     * documentation for more information about how this state is managed.
     *
     * @return true if this is the source that should be rendered this
     *   this frame.
     */
    @Override
    public boolean isAudioSource()
    {
        return activeSoundLayer;
    }

    /**
     * Fetch the renderable used to control the viewport setting. If the
     * default environment is to be used, this will return null.
     *
     * @return A renderable instance if custom viewport handling is need
     *   or null if not
     */
    @Override
    public ViewportRenderable getViewportRenderable()
    {
        return viewport;
    }

    /**
     * Get the primary view environment.
     */
    @Override
    public ViewEnvironmentCullable getViewCullable()
    {
        return viewEnvironment;
    }

    /**
     * Get the cullable object representing the active viewpoint that in this
     * environment.
     *
     * @return The viewpoint renderable to use
     */
    @Override
    public EnvironmentCullable getViewpointCullable()
    {
        return currentViewpoint;
    }

    /**
     * Get the cullable object representing the active background that in this
     * environment. If no background is set, this will return null.
     *
     * @return The background renderable to use
     */
    @Override
    public EnvironmentCullable getBackgroundCullable()
    {
        return null;
    }

    /**
     * Get the cullable object representing the active fog in this environment.
     * If no fog is set or this is a pass in a multipass rendering, this will
     * return null. If the underlying fog node is currently disabled or not
     * labeled as global, then this method should return null.
     *
     * @return The fog renderable to use
     */
    @Override
    public LeafCullable getFogCullable()
    {
        return currentFog;
    }

    /**
     * Get the primary cullable that represents the root of the scene graph.
     * If this is a multipass cullable, this should return null.
     */
    @Override
    public Cullable getRootCullable()
    {
        return renderableObjects;
    }

    /**
     * Fetch renderable information about the general buffer. If this is a
     * single pass scene, this will return null.
     *
     * @return The state representation for the general buffer
     */
    @Override
    public BufferStateRenderable getGeneralBufferRenderable()
    {
        return generalState;
    }

    /**
     * Fetch renderable information about the colour buffer. If this is a
     * single pass scene, this will return null.
     *
     * @return The state representation for the colour buffer
     */
    @Override
    public BufferStateRenderable getColorBufferRenderable()
    {
        return colorState;
    }

    /**
     * Fetch renderable information about the depth buffer. If this is a
     * single pass scene, this will return null.
     *
     * @return The state representation for the depth buffer
     */
    @Override
    public BufferStateRenderable getDepthBufferRenderable()
    {
        return depthState;
    }

    /**
     * Fetch renderable information about the stencil buffer. If this is a
     * single pass scene, this will return null.
     *
     * @return The state representation for the stencil buffer
     */
    @Override
    public BufferStateRenderable getStencilBufferRenderable()
    {
        return stencilState;
    }

    /**
     * Fetch renderable information about the accumulation buffer. If this is a
     * single pass scene, this will return null.
     *
     * @return The state representation for the accumulation buffer
     */
    @Override
    public BufferStateRenderable getAccumBufferRenderable()
    {
        return accumState;
    }

    //----------------------------------------------------------
    // Methods defined by ScenegraphObject
    //----------------------------------------------------------

    /**
     * Notification that this object is live now. Overridden to make sure that
     * the live state of the nodes represents the same state as the parent
     * scene graph.
     *
     * @param state true if this should be marked as live now
     */
    @Override
    protected void setLive(boolean state)
    {
        // Ignore stuff that doesn't change the state
        if(state == alive)
            return;

        super.setLive(state);

        if(renderableObjects != null)
            renderableObjects.setLive(state);

        if(generalState != null)
            generalState.setLive(state);

        if(stencilState != null)
            stencilState.setLive(state);

        if(depthState != null)
            depthState.setLive(state);

        if(colorState != null)
            colorState.setLive(state);

        if(accumState != null)
            accumState.setLive(state);

        viewEnvironment.setLive(state);
    }

    /**
     * Set the scenegraph update handler for this node.  It will notify
     * all its children of the value. A null value will clear the current
     * handler.
     *
     * @param handler The instance to use as a handler
     */
    @Override
    protected void setUpdateHandler(NodeUpdateHandler handler)
    {
        super.setUpdateHandler(handler);

        if(renderableObjects != null)
            renderableObjects.setUpdateHandler(handler);

        if(generalState != null)
            generalState.setUpdateHandler(handler);

        if(stencilState != null)
            stencilState.setUpdateHandler(handler);

        if(depthState != null)
            depthState.setUpdateHandler(handler);

        if(colorState != null)
            colorState.setUpdateHandler(handler);

        if(accumState != null)
            accumState.setUpdateHandler(handler);

        viewEnvironment.setUpdateHandler(handler);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the enabled state of the light. Can use this to turn it on and off
     * in a general fashion.
     *
     * @param state The new state of the light
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setEnabled(boolean state)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        enabled = state;
    }

    /**
     * Set this layer to be the currently active sound layer. The previously
     * active layer will be disabled. This method can only be called during
     * the dataChanged() callback.
     */
    public void makeActiveSoundLayer()
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        activeSoundLayer = true;

        if(updateHandler != null)
        {
            if(internalUpdater == null)
                internalUpdater = new InternalUpdater();

            updateHandler.activeSoundLayerChanged(internalUpdater);
        }
    }

    /**
     * Check to see if this is the currently active layer for sound rendering.
     * This will only return true the frame after calling
     * {@link #makeActiveSoundLayer()}. The effects, however, will be rendered
     * starting the frame that this is set.
     *
     * @return true if this is the layer that will generate sound rendering
     */
    public boolean isActiveSoundLayer()
    {
        return activeSoundLayer;
    }

    /**
     * Get the currently set active view. If none is set, return null.
     *
     * @return The current view instance or null
     */
    public ViewEnvironment getViewEnvironment()
    {
        return viewEnvironment;
    }

    /**
     * Set the collection of geometry that should be rendered to this
     * texture.
     *
     * A null value will clear the current geometry and result in only
     * rendering the background, if set. if not set, then whatever the default
     * colour is, is used (typically black).
     *
     * @param geom The new geometry to use or null
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void setRenderedGeometry(Group geom)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(getAppUpdateWriteTimingMessage());

        if(renderableObjects != null)
        {
            renderableObjects.setUpdateHandler(null);
            renderableObjects.setLive(false);
        }

        renderableObjects = geom;

        if(renderableObjects != null)
        {
            renderableObjects.setUpdateHandler(updateHandler);
            renderableObjects.setLive(alive);
        }
    }

    /**
     * Get the root of the currently rendered scene. If none is set, this will
     * return null.
     *
     * @return The current scene root or null.
     */
    public Group getRenderedGeometry()
    {
        return renderableObjects;
    }

    /**
     * Set the viewpoint path that should be applied to the current surface.
     * The output drawn will be a combination of this information and that
     * of the view environment.
     *
     * @param vp The instance of the active viewpoint to use
     * @throws IllegalArgumentException The path contains a SharedGroup or
     *    the node is not live
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void setActiveView(Viewpoint vp)
        throws InvalidWriteTimingException, IllegalArgumentException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(getAppUpdateWriteTimingMessage());

        Node parent = vp.getParent();

        while(parent != null)
        {
            if((parent instanceof SharedGroup) ||
               (parent instanceof SharedNode))
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(SHARED_VP_PROP);
                throw new IllegalArgumentException(msg);
            }

            parent = parent.getParent();
        }

        currentViewpoint = vp;
    }

    /**
     * Get the currently set active view. If none is set, return null.
     *
     * @return The current view instance or null
     */
    public Viewpoint getActiveView()
    {
        return currentViewpoint;
    }

    /**
     * Set the general buffer state that should be applied to the during this
     * pass. A null value will clear the state.
     *
     * @param state The instance of the state to set
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void setGeneralBufferState(GeneralBufferState state)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(getAppUpdateWriteTimingMessage());

        generalState = state;
    }

    /**
     * Get the currently set general buffer state. If none is set, return null.
     *
     * @return The current buffer state or null
     */
    public GeneralBufferState getGeneralBufferState()
    {
        return generalState;
    }

    /**
     * Set the stencil buffer state that should be applied to the during this
     * pass. A null value will clear the state.
     *
     * @param state The instance of the state to set
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void setStencilBufferState(StencilBufferState state)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(getAppUpdateWriteTimingMessage());

        stencilState = state;
    }

    /**
     * Get the currently set stencil buffer state. If none is set, return null.
     *
     * @return The current buffer state or null
     */
    public StencilBufferState getStencilBufferState()
    {
        return stencilState;
    }

    /**
     * Set the depth buffer state that should be applied to the during this
     * pass. A null value will clear the state.
     *
     * @param state The instance of the state to set
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void setDepthBufferState(DepthBufferState state)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(getAppUpdateWriteTimingMessage());

        depthState = state;
    }

    /**
     * Get the currently set depth buffer state. If none is set, return null.
     *
     * @return The current buffer state or null
     */
    public DepthBufferState getDepthBufferState()
    {
        return depthState;
    }

    /**
     * Set the color buffer state that should be applied to the during this
     * pass. A null value will clear the state.
     *
     * @param state The instance of the state to set
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void setColorBufferState(ColorBufferState state)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(getAppUpdateWriteTimingMessage());

        colorState = state;
    }

    /**
     * Get the currently set color buffer state. If none is set, return null.
     *
     * @return The current buffer state or null
     */
    public ColorBufferState getColorBufferState()
    {
        return colorState;
    }

    /**
     * Set the accumulation buffer state that should be applied to the during this
     * pass. A null value will clear the state.
     *
     * @param state The instance of the state to set
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void setAccumulationBufferState(AccumulationBufferState state)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(getAppUpdateWriteTimingMessage());

        accumState = state;
    }

    /**
     * Get the currently set accumulation buffer state. If none is set, return null.
     *
     * @return The current buffer state or null
     */
    public AccumulationBufferState getAccumulationBufferState()
    {
        return accumState;
    }

    /**
     * Set the viewport dimensions from the parent viewport. These dimensions
     * are pushed down through the scene to the viewport.
     *
     * @param x The lower left x coordinate for the view
     * @param y The lower left y coordinate for the view
     * @param width The width of the viewport in pixels
     * @param height The height of the viewport in pixels
     */
    void setEnvViewportDimensions(int x, int y, int width, int height)
    {
        viewEnvironment.setViewportDimensions(x, y, width, height);
    }

    /**
     * Override the scene's viewport dimensions with a specific set for this
     * pass only. All values in pixels. To clear an explicitly set viewport,
     * set the width or height parameters to zero.
     *
     * @param x The left position of the location
     * @param y The top position of the location
     * @param w The width of the viewport
     * @param h The left position of the location
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void setViewportDimensions(int x, int y, int w, int h)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(getAppUpdateWriteTimingMessage());

        if((w == 0) || (h == 0))
            viewport = null;
        else
        {
            if(viewport == null)
                viewport = new MPViewportRenderable();

            viewport.setDimensions(x, y, w, h);
        }
    }

    /**
     * Get the viewport dimensions. If no dimensions have been previously set
     * this will set the array to all zeros.
     *
     * @param vals The array to copy dimensions in to
     */
    public void getViewportDimensions(float[] vals)
    {
        if(viewport != null)
            viewport.getDimensions(vals);
        else
        {
            vals[0] = 0;
            vals[1] = 0;
            vals[2] = 0;
            vals[3] = 0;
        }
    }
}
