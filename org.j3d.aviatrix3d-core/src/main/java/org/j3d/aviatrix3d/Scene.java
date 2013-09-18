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
// None

// Local imports
import org.j3d.aviatrix3d.rendering.RenderEffectsProcessor;

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
 * A scene may also provide an instance {@link RenderEffectsProcessor} that
 * allows the user to provide pre or post processing effects on a per-scene
 * basis. Note that if you are running layers, this will be pre and post
 * processing per layer, not per final rendering. The processor allows a
 * limited form of immediate-mode rendering.
 *
 * @author Justin Couch
 * @version $Revision: 1.14 $
 */
public abstract class Scene extends SceneGraphObject
{
    /** Current view environment */
    protected ViewEnvironment viewEnvironment;

    /** Current effects processor */
    protected RenderEffectsProcessor processor;

    /**
     * Create a default instance of this scene with no content provided.
     */
    public Scene()
    {
        viewEnvironment = new ViewEnvironment();
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

        viewEnvironment.setUpdateHandler(handler);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Register the scene processor to be used for this scene for pre and
     * post rendering effects. Setting a null value will clear the current
     * instance.
     *
     * @param prc The instance to use or null
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void setRenderEffectsProcessor(RenderEffectsProcessor prc)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(getAppUpdateWriteTimingMessage());

        processor = prc;
    }

    /**
     * Get the currently set scene processor instance. If none is set,
     * returns null.
     *
     * @return The current processor or null
     */
    public RenderEffectsProcessor getRenderEffectsProcessor()
    {
        return processor;
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
     * Internal check to see if this scene has an update handler registered.
     * If it does, that is a sign that it currently has a parent registered for
     * it.
     */
    boolean hasParent()
    {
        return updateHandler != null;
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
    void setViewportDimensions(int x, int y, int width, int height)
    {
        viewEnvironment.setViewportDimensions(x, y, width, height);
    }
}
