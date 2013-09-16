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
import org.j3d.aviatrix3d.rendering.SceneCullable;
import org.j3d.aviatrix3d.rendering.ViewportLayerCullable;

/**
 * An viewport layer definition that only allows a single, simple scene to
 * be drawn as it's contents.
 * <p>
 *
 * As with all viewport layers, the scene will encompass the entire area of
 * the viewport that contains it.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>hasParentMsg: Error message when the (internal) caller tries to
 *     call setParent() when this class already has a parent.</li>
 * </ul>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.9 $
 */
public class SimpleViewportLayer extends ViewportLayer
    implements ViewportLayerCullable
{
    /** Message about code that is not valid parent */
    private static final String HAS_PARENT_PROP =
        "org.j3d.aviatrix3d.SimpleViewportLayer.hasParentMsg";

    /** The scene that this layer manages */
    private SimpleScene scene;

    /** Flag to describe whether this is the currently active sound layer */
    private boolean activeSoundLayer;

    /** Update handler for the external code. Not created until needed. */
    private InternalUpdater internalUpdater;

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
     * Construct a new layer instance
     */
    public SimpleViewportLayer()
    {
        super(SIMPLE);

        activeSoundLayer = false;
    }

    //----------------------------------------------------------
    // Methods defined by ViewportLayerCullable
    //----------------------------------------------------------

    /**
     * Check to see if this is a multipass cullable or single pass.
     *
     * @return true if this is a multipass cullable
     */
    public boolean isMultipassViewport()
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
        return activeSoundLayer;
    }

    /**
     * Get the cullable layer child that for the given layer index.
     *
     * @return The layer cullable at the given index or null
     */
    public SceneCullable getCullableScene()
    {
        return (scene instanceof SceneCullable) ?
               (SceneCullable)scene: null;
    }

    //----------------------------------------------------------
    // Methods defined by ViewportLayer
    //----------------------------------------------------------

    /**
     * Set the viewport dimensions from the parent viewport. These dimensions
     * are pushed down through the scene to the viewport.
     *
     * @param x The lower left x coordinate for the view
     * @param y The lower left y coordinate for the view
     * @param width The width of the viewport in pixels
     * @param height The height of the viewport in pixels
     */
    void setViewportDimensions(int x, int y, int width, int height)
    {
        super.setViewportDimensions(x, y, width, height);

        if(scene != null)
            scene.setViewportDimensions(x, y, width, height);
    }

    //----------------------------------------------------------
    // Methods defined by ScenegraphObject
    //----------------------------------------------------------

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

        if(scene != null)
            scene.setUpdateHandler(handler);
    }

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

        if(scene != null)
            scene.setLive(state);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

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
     * Set a new scene instance to be used by this layer.
     * <p>
     * Note that a scene cannot have more than one parent, so sharing it
     * between layers will result in an error.
     *
     * @param sc The scene instance to use, or null to clear
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     * @throws AlreadyParentedException This scene already has a current parent
     *    preventing it from being used
     */
    public void setScene(SimpleScene sc)
        throws InvalidWriteTimingException, AlreadyParentedException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        scene = sc;

        // No scene? Ignore it.
        if(sc == null)
            return;

        if(sc.hasParent())
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(HAS_PARENT_PROP);
            throw new AlreadyParentedException(msg);
        }

        sc.setViewportDimensions(viewX, viewY, viewWidth, viewHeight);
        sc.setUpdateHandler(updateHandler);
        sc.setLive(alive);
    }

    /**
     * Get the currently set scene instance. If no scene is set, null
     * is returned.
     *
     * @return The current scene instance or null
     */
    public SimpleScene getScene()
    {
        return scene;
    }
}
