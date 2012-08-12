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

package org.j3d.aviatrix3d;

// External imports
import org.j3d.util.I18nManager;

// Local imports
// None

/**
 * The SceneGraphObject is a common superclass for all scene graph objects.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>boundsWriteTimingMsg: Error message when the user tries to write to
 *     a live node a property that effects its bounds outside of the
 *     node update listener bounds update method.</li>
 * <li>dataWriteTimingMsg: Error message when the user tries to write to
 *     a live node a general property that outside of the node update
 *     listener data update method.</li>
 * <li>mainloopWriteTimingMsg: Error message when the user tries to write to
 *     a live node a general property outside of the
 *     application update observer update method.</li>
 * <li>listenerSetTimingMsg: Error message when the user tries to register
 *     a node update listener on a non-live node.</li>
 * </ul>
 *
 * @author Alan Hudson
 * @version $Revision: 1.16 $
 */
public abstract class SceneGraphObject
{
    /** Property defining the error message used for bounds writing timing */
    private static final String BOUNDS_WRITE_TIMING_ERR_PROP =
        "org.j3d.aviatrix3d.SceneGraphObject.boundsWriteTimingMsg";

    /** Property defining the error message used for data writing timing */
    private static final String DATA_WRITE_TIMING_ERR_PROP =
        "org.j3d.aviatrix3d.SceneGraphObject.dataWriteTimingMsg";

    /** Property defining the error message used for app updatewriting timing */
    private static final String MAIN_WRITE_TIMING_ERR_PROP =
        "org.j3d.aviatrix3d.SceneGraphObject.mainloopWriteTimingMsg";

    /** Property defining the error message used for listener set timing */
    protected static final String LISTENER_SET_TIMING_ERR_PROP =
        "org.j3d.aviatrix3d.SceneGraphObject.listenerSetTimingMsg";

    /** The scene this node belongs to */
    protected NodeUpdateHandler updateHandler;

    /** User-provided data */
    private Object userData;

    /** Current live state of the object */
    protected boolean alive;

    /**
     * Set the user data to the new object. Null will clear the existing
     * object.
     *
     * @param data The new piece of data to set
     */
    public void setUserData(Object data)
    {
        userData = data;
    }

    /**
     * Get the currently set user data object. If none set, null is returned.
     *
     * @return The current user data or null
     */
    public Object getUserData()
    {
        return userData;
    }

    /**
     * Check to see whether this object is alive or not.
     *
     * @return true if this object is currently live
     */
    public boolean isLive()
    {
        return alive;
    }

    /**
     * Notification that this object's liveness state has changed.
     *
     * @param state true if this should be marked as live now
     */
    protected void setLive(boolean state)
    {
        alive = state;
    }

    /**
     * Check to see if this node is the same reference as the passed node.
     * This is the upwards check to ensure that there is no cyclic scene graph
     * structures at the point where someone adds a node to the scenegraph.
     * When the reference and this are the same, an exception is generated.
     *
     * @param child The reference to check against this class
     * @throws CyclicSceneGraphStructureException Equal parent and child
     */
    protected void checkForCyclicParent(SceneGraphObject child)
        throws CyclicSceneGraphStructureException
    {
        if(child == this)
            throw new CyclicSceneGraphStructureException();
    }

    /**
     * Check to see if this node is the same reference as the passed node that
     * is a parent of this node. This is the downwards check to ensure that
     * there is no cyclic scene graph structures at the point where someone
     * adds a node to the scenegraph. When the reference and this are the
     * same, an exception is generated. Since each class may have different
     * lists of child node setups, this should be overriden by any class that
     * can take children, and have the call passed along to the children.
     *
     * @param parent The reference to check against this class
     * @throws CyclicSceneGraphStructureException Equal parent and child
     */
    protected void checkForCyclicChild(SceneGraphObject parent)
        throws CyclicSceneGraphStructureException
    {
        if(parent == this)
            throw new CyclicSceneGraphStructureException();
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
        updateHandler = handler;
    }

    /**
     * Notify the node that you have updates to the node that will not
     * alter its bounds.
     *
     * @param l The change requestor
     * @throws InvalidListenerSetTimingException If called when the node is not live or
     *   if called during one of the bounds/data changed callbacks
     */
    public void dataChanged(NodeUpdateListener l)
        throws InvalidListenerSetTimingException
    {
        if(!isLive())
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(LISTENER_SET_TIMING_ERR_PROP);
            throw new InvalidListenerSetTimingException(msg);
        }

        // Ignore if we are not live
        if(updateHandler == null)
            return;

        updateHandler.dataChanged(l, this);
    }

    /**
     * Convenience method to fetch the data write change error message
     * from the internationalisation manager.
     *
     * @return The current internationalisation message
     */
    protected String getDataWriteTimingMessage()
    {
        I18nManager intl_mgr = I18nManager.getManager();

        return intl_mgr.getString(DATA_WRITE_TIMING_ERR_PROP);
    }

    /**
     * Convenience method to fetch the data write change error message
     * from the internationalisation manager.
     *
     * @return The current internationalisation message
     */
    protected String getBoundsWriteTimingMessage()
    {
        I18nManager intl_mgr = I18nManager.getManager();

        return intl_mgr.getString(BOUNDS_WRITE_TIMING_ERR_PROP);
    }

    /**
     * Convenience method to fetch the data write change error message
     * from the internationalisation manager.
     *
     * @return The current internationalisation message
     */
    protected String getAppUpdateWriteTimingMessage()
    {
        I18nManager intl_mgr = I18nManager.getManager();

        return intl_mgr.getString(MAIN_WRITE_TIMING_ERR_PROP);
    }
}
