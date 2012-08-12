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
 * An viewport layer that allows multipass rendering to be performed within
 * this layer.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>hasParentMsg: Error message when the (internal) caller tries to
 *     call setParent() when this class already has a parent.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 2.6 $
 */
public class MultipassViewportLayer extends ViewportLayer
    implements ViewportLayerCullable
{
    /** Message about code that is not valid parent */
    private static final String CURRENT_PARENT_PROP =
        "org.j3d.aviatrix3d.MultipassViewportLayer.hasParentMsg";

    /** The scene that this layer manages */
    private MultipassScene scene;

    /**
     * Construct a new layer instance
     */
    public MultipassViewportLayer()
    {
        super(MULTIPASS);
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
        return true;
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
    // Local Methods
    //----------------------------------------------------------

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
    public void setScene(MultipassScene sc)
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
            String msg = intl_mgr.getString(CURRENT_PARENT_PROP);
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
    public MultipassScene getScene()
    {
        return scene;
    }
}
