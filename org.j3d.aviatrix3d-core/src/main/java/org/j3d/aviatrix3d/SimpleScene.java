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

package org.j3d.aviatrix3d;

// External imports
import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.*;

/**
 * Representation of the top level structure of a piece of scene graph that
 * can form a coherent rendering.
 * <p>
 *
 * A scene encapsulates a viewpoint that is used to view the scene from,
 * the geometry structure to render, data defining the view environment (eg
 * projection type) and global data, such as the background, global fog etc.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>sharedBgMsg: Error message when the set background has more than one
 *     possible parent.</li>
 * <li>sharedVpMsg: Error message when the set viewpoint has more than one
 *     possible parent.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 2.10 $
 */
public class SimpleScene extends Scene
    implements SceneCullable,
               RenderPassCullable
{
    /** Message when setting the active view if it contains a shared parent */
    private static final String SHARED_VP_PROP =
        "org.j3d.aviatrix3d.SimpleScene.sharedVpMsg";

    /** Message when setting the active background if it contains a shared parent */
    private static final String SHARED_BG_PROP =
        "org.j3d.aviatrix3d.SimpleScene.sharedBgMsg";

    /** The Scene Graph renderableObjects */
    private Group renderableObjects;

    /** The current viewpoint instance */
    private Viewpoint currentViewpoint;

    /** The current fog instance */
    private Fog currentFog;

    /** The current background instance */
    private Background currentBackground;

    /**
     * Create a default instance of this scene with no content provided.
     */
    public SimpleScene()
    {
    }

    //----------------------------------------------------------
    // Methods defined by SceneCullable
    //----------------------------------------------------------

    /**
     * Check to see if this render pass is valid to render for this
     * frame. Simple scenes cannot be disabled.
     *
     * @return true always
     */
    public boolean isEnabled()
    {
        return true;
    }

    /**
     * Check to see if this is a multipass cullable or single pass.
     *
     * @return true if this is a multipass cullable
     */
    public boolean isMultipassScene()
    {
        return false;
    }

    /**
     * Check to see if this render pass is the one that also has the
     * spatialised audio to be rendered for this frame. If this is a multipass
     * layer then there is must return false and potentially one of the render
     * passes will be the active audio source.  See the package
     * documentation for more information about how this state is managed.
     *
     * @return true if this is the source that should be rendered this
     *   this frame.
     */
    public boolean isAudioSource()
    {
        return false;
    }


    /**
     * Get the primary view environment information. This applies to the whole
     * scene before any multipass processing is done. If this is a multipass
     * scene, the frustum information is ignored as each pass applies
     * separately.
     */
    public ViewEnvironmentCullable getViewCullable()
    {
        return viewEnvironment;
    }

    /**
     * Get the cullable layer child that for the given layer index. For a single
     * pass scene this represents everything about the scene to be rendered.
     * The view environment of this scene is the same as that of the first
     * render pass.
     *
     * @param passIndex The index of the pass to fetch
     * @return The layer cullable at the given index or null
     */
    public RenderPassCullable getCullablePass(int passIndex)
    {
        return this;
    }

    /**
     * Returns the number of valid cullable rendering passes to process. In a
     * single pass scene return 1.
     *
     * @return A number greater than or equal to zero
     */
    public int numCullableChildren()
    {
        return 1;
    }

    //----------------------------------------------------------
    // Methods defined by RenderPassCullable
    //----------------------------------------------------------

    /**
     * Check to see if this represents a 2D scene that has no 3D rendering
     * capabilities. A purely 2D scene sets up the view environment quite
     * different to a full 3D scene.
     *
     * @return true if this is a 2D scene rather than a 3D version
     */
    public boolean is2D()
    {
        return false;
    }

    /**
     * Get the cullable object representing the active viewpoint that in this
     * environment.
     *
     * @return The viewpoint renderable to use
     */
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
    public EnvironmentCullable getBackgroundCullable()
    {
        return currentBackground;
    }

    /**
     * Get the cullable object representing the active fog in this environment.
     * If no fog is set or this is a pass in a multipass rendering, this will
     * return null. If the underlying fog node is currently disabled or not
     * labeled as global, then this method should return null.
     *
     * @return The fog renderable to use
     */
    public LeafCullable getFogCullable()
    {
        return currentFog;
    }

    /**
     * Get the primary cullable that represents the root of the scene graph.
     * If this is a multipass cullable, this should return null.
     */
    public Cullable getRootCullable()
    {
        return renderableObjects;
    }

    /**
     * Fetch the renderable used to control the viewport setting. If the
     * default environment is to be used, this will return null.
     *
     * @return A renderable instance if custom viewport handling is need
     *   or null if not
     */
    public ViewportRenderable getViewportRenderable()
    {
        return null;
    }

    /**
     * Fetch renderable information about the general buffer. If this is a
     * single pass scene, this will return null.
     *
     * @return The state representation for the general buffer
     */
    public BufferStateRenderable getGeneralBufferRenderable()
    {
        return null;
    }

    /**
     * Fetch renderable information about the colour buffer. If this is a
     * single pass scene, this will return null.
     *
     * @return Null always as this is a single pass scene
     */
    public BufferStateRenderable getColorBufferRenderable()
    {
        return null;
    }

    /**
     * Fetch renderable information about the depth buffer. If this is a
     * single pass scene, this will return null.
     *
     * @return Null always as this is a single pass scene
     */
    public BufferStateRenderable getDepthBufferRenderable()
    {
        return null;
    }

    /**
     * Fetch renderable information about the stencil buffer. If this is a
     * single pass scene, this will return null.
     *
     * @return Null always as this is a single pass scene
     */
    public BufferStateRenderable getStencilBufferRenderable()
    {
        return null;
    }

    /**
     * Fetch renderable information about the accumulation buffer. If this is a
     * single pass scene, this will return null.
     *
     * @return Null always as this is a single pass scene
     */
    public BufferStateRenderable getAccumBufferRenderable()
    {
        return null;
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
    protected void setLive(boolean state)
    {
        // Ignore stuff that doesn't change the state
        if(state == alive)
            return;

        super.setLive(state);

        if(renderableObjects != null)
            renderableObjects.setLive(state);
    }

    /**
     * Set the scenegraph update handler for this node.  It will notify
     * all its children of the value. A null value will clear the current
     * handler.
     *
     * @param handler The instance to use as a handler
     */
    protected void setUpdateHandler(NodeUpdateHandler handler)
    {
        super.setUpdateHandler(handler);

        if(renderableObjects != null)
            renderableObjects.setUpdateHandler(handler);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

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
			// rem: is this where an exception should be thrown?
			
        if(renderableObjects != null)
        {
			// rem: retain the update handler so that 
			// DeletableRenderable's will be queued for deletion
            //renderableObjects.setUpdateHandler(null);
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

        if(vp != null)
        {
            Node parent = vp.getParent();

            while(parent != null)
            {
                if((parent instanceof SharedGroup) ||
                   (parent instanceof SharedNode) ||
                   (parent instanceof SharedGroup2D) ||
                   (parent instanceof SharedNode2D))
                {
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg = intl_mgr.getString(SHARED_VP_PROP);
                    throw new IllegalArgumentException(msg);
                }

                parent = parent.getParent();
            }
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
     * Set the fog that should be applied to the current surface.
     * The output drawn will be a combination of this information and that
     * of the view environment.
     *
     * @param fog The instance of the active fog node
     * @throws IllegalArgumentException The path contains a SharedGroup or
     *    the node is not live
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void setActiveFog(Fog fog)
        throws InvalidWriteTimingException, IllegalArgumentException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(getAppUpdateWriteTimingMessage());

        currentFog = fog;
    }

    /**
     * Get the currently set active fog. If none is set, return null.
     *
     * @return The current view instance or null
     */
    public Fog getActiveFog()
    {
        return currentFog;
    }

    /**
     * Set the background path that should be applied to the current surface.
     * The output drawn will be a combination of this information and that
     * of the view environment.
     *
     * @param bg The instance of the active background
     * @throws IllegalArgumentException The path contains a SharedGroup or
     *    the node is not live
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void setActiveBackground(Background bg)
        throws InvalidWriteTimingException, IllegalArgumentException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(getAppUpdateWriteTimingMessage());

        if(bg != null)
        {
            Node parent = bg.getParent();

            while(parent != null)
            {
                if((parent instanceof SharedGroup) ||
                   (parent instanceof SharedNode) ||
                   (parent instanceof SharedGroup2D) ||
                   (parent instanceof SharedNode2D))
                {
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg = intl_mgr.getString(SHARED_BG_PROP);
                    throw new IllegalArgumentException(msg);
                }

                parent = parent.getParent();
            }
        }

        currentBackground = bg;
    }

    /**
     * Get the currently set active background. If none is set, return null.
     *
     * @return The current view instance or null
     */
    public Background getActiveBackground()
    {
        return currentBackground;
    }
}
