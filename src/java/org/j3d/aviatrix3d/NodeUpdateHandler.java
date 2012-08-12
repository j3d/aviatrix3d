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
// None

// Local imports
import org.j3d.aviatrix3d.picking.PickingManager;
import org.j3d.aviatrix3d.rendering.DeletableRenderable;
import org.j3d.aviatrix3d.rendering.ShaderSourceRenderable;

/**
 * Abstract representation of a piece of code that wants to manage the node
 * update process internally to the scene graph.
 * <p>
 *
 * This interface is never directly called by user-land code. It's used to
 * abstract the process of maintaining a node as part of the scene graph, and
 * the implementation of the management system behind it. The management system
 * is responsible for coordinating and marshalling the user code into correct
 * timing for feeding directly to the rendering pipeline and beyond. As such,
 * it is responsible for preventing user updates at inappropriate times, and
 * also keeping track of what has been requested to update.
 * <p>
 *
 * Methods here provide both timing information, and ways of registering
 * objects for further processing. When a node implementation needs to know if
 * it is acceptable to make or allow certain changes, then these methods can be
 * queried to provide the appropriate guidance.
 *
 * @author Justin Couch
 * @version $Revision: 1.19 $
 */
public interface NodeUpdateHandler
{
    /**
     * Check to see if writing to the bounding information of the node is
     * permitted currently.
     *
     * @param src The object that is requesting the check
     * @return true if the end user can write, false if not
     */
    public boolean isBoundsWritePermitted(Object src);

    /**
     * Check to see if writing to the data information of the node is
     * permitted currently.
     *
     * @param src The object that is requesting the check
     * @return true if the end user can write, false if not
     */
    public boolean isDataWritePermitted(Object src);

    /**
     * Check to see if picking is permitted currently.
     *
     * @return true if the end user can pick, false if not
     */
    public boolean isPickingPermitted();

    /**
     * Feedback to the internals that a data object has changed and it requires
     * re-rendering. This should only be called by classes that can effect the
     * rendering but don't normally use the data/bounds write listeners (ie
     * changes are made during the app update portion of the scene graph).
     * Typically this would be used for things like the {@link ViewEnvironment}
     * changing the aspect ratio etc.
     */
    public void notifyUpdateRequired();

    /**
     * Notify the handler that you have updates to the SG that might alter
     * a node's bounds.
     *
     * @param l The change requestor
     * @param src The object that is passing this listener through.
     * @param intL Internal listener for making callbacks at a later time
     *    to propogate the bounds changes.
     * @return Was the notification accepted.  Duplicates will return false.
     * @throws InvalidListenerSetTimingException If called when the node called
     *    during one of the bounds/data changed callbacks
     */
    public boolean boundsChanged(NodeUpdateListener l,
                                 Object src,
                                 InternalNodeUpdateListener intL)
        throws InvalidListenerSetTimingException;

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
        throws InvalidListenerSetTimingException;

    /**
     * Notify the handler that you have updates to the SG that will not
     * alter a node's bounds.
     *
     * @param l The change requestor
     * @param src The object that is passing this listener through.
     * @throws InvalidListenerSetTimingException If called when the node called
     *    during one of the bounds/data changed callbacks
     */
    public void dataChanged(NodeUpdateListener l, Object src)
        throws InvalidListenerSetTimingException;

    /**
     * Get the picking handler so that we can do some picking operations.
     *
     * @return the current instance of the picking system
     */
    public PickingManager getPickingManager();

    /**
     * The shader object passed requires an initialisation be performed. Queue
     * the shader up for processing now.
     *
     * @param shader The shader instance to queue
     * @param updateResponse true if this is being made as a response to a
     *   node's setUpdateHandler() method
     */
    public void shaderRequiresInit(ShaderSourceRenderable shader,
                                   boolean updateResponse);

    /**
     * The shader object passed requires updating the log info. Queue
     * the shader up for processing now so that at the next oppourtunity it
     * can call glGetLogInfoARB.
     *
     * @param shader The shader instance to queue
     * @param updateResponse true if this is being made as a response to a
     *    node's setUpdateHandler() method
     */
    public void shaderRequiresLogInfo(ShaderSourceRenderable shader,
                                      boolean updateResponse);

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
    public void requestDeletion(DeletableRenderable deletable);

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
    public void rescindDeletionRequest(DeletableRenderable deletable);
}
