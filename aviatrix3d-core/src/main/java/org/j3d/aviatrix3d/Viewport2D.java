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
import org.j3d.aviatrix3d.rendering.ViewportCullable;
import org.j3d.aviatrix3d.rendering.ViewportLayerCullable;

/**
 * A viewport that contains a single scene, with no internal layering and is
 * restricted to 2D rendering only.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>hasParentMsg: Error message when the (internal) caller tries to
 *     call setParent() when this class already has a parent.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 2.5 $
 */
public class Viewport2D extends Viewport
    implements ViewportCullable,
               ViewportLayerCullable
{
    /** Message about code that is not valid parent */
    private static final String HAS_PARENT_PROP =
        "org.j3d.aviatrix3d.Viewport2D.hasParentMsg";

    /** The viewport that this layer manages */
    private Scene2D scene;

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
     * Construct a new, empty, viewport instance
     */
    public Viewport2D()
    {
        super(FLAT);

        activeSoundLayer = false;
    }

    //----------------------------------------------------------
    // Methods defined by ViewportCullable
    //----------------------------------------------------------

    /**
     * Get the cullable layer child that for the given layer index.
     *
     * @return The layer cullable at the given index or null
     */
    public ViewportLayerCullable getCullableLayer(int viewportIndex)
    {
        return this;
    }

    /**
     * Returns the number of valid cullable children to process. If there are
     * no valid cullable children, return 0.
     *
     * @return A number greater than or equal to zero
     */
    public int numCullableChildren()
    {
        return scene != null ? 1 : 0;
    }

    //----------------------------------------------------------
    // Methods defined by ViewportLayerCullable
    //----------------------------------------------------------

    /**
     * Check to see if this is a multipass cullable or single pass.
     *
     * @return true if this is a multipass cullable
     */
    @Override
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
    @Override
    public boolean isAudioSource()
    {
        return activeSoundLayer;
    }

    /**
     * Get the cullable layer child that for the given layer index.
     *
     * @return The layer cullable at the given index or null
     */
    @Override
    public SceneCullable getCullableScene()
    {
        return (scene instanceof SceneCullable) ?
               (SceneCullable)scene: null;
    }

    //----------------------------------------------------------
    // Methods defined by Viewport
    //----------------------------------------------------------

    /**
     * Set the dimensions of the viewport in pixels. Coordinates are defined in
     * the space of the parent component that is being rendered to.
     *
     * @param x The lower left x coordinate for the view
     * @param y The lower left y coordinate for the view
     * @param width The width of the viewport in pixels
     * @param height The height of the viewport in pixels
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     */
    @Override
    public void setDimensions(int x, int y, int width, int height)
        throws InvalidWriteTimingException
    {
        super.setDimensions(x, y, width, height);

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
    @Override
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
    @Override
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
     * Set a new scene instance to be used by this viewport.
     * <p>
     * Note that a scene cannot have more than one parent, so sharing it
     * between viewports will result in an error.
     *
     * @param sc The scene instance to use, or null to clear
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     * @throws AlreadyParentedException This scene already has a current parent
     *    preventing it from being used
     */
    public void setScene(Scene2D sc)
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

        sc.setUpdateHandler(updateHandler);
        sc.setViewportDimensions(viewX, viewY, viewWidth, viewHeight);
        sc.setLive(alive);
    }

    /**
     * Get the currently set scene instance. If no scene is set, null
     * is returned.
     *
     * @return The current scene instance or null
     */
    public Scene2D getScene()
    {
        return scene;
    }
}
