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
import org.j3d.aviatrix3d.rendering.ViewportCullable;

/**
 * An layer definition that only allows a single viewport that covers the
 * entire area as it's child.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>hasParentMsg: Error message when the (internal) caller tries to
 *     call setParent() when this class already has a parent.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public class SimpleLayer extends Layer
{
    /** Message about code that is not valid parent */
    private static final String HAS_PARENT_PROP =
        "org.j3d.aviatrix3d.SimpleLayer.hasParentMsg";

    /** The viewport that this layer manages */
    private Viewport viewport;

    /**
     * Construct a new layer instance
     */
    public SimpleLayer()
    {
        super(SIMPLE);
    }

    //---------------------------------------------------------------
    // Methods defined by LayerCullable
    //---------------------------------------------------------------

    /**
     * Get the cullable layer child that for the given layer index.
     *
     * @return The layer cullable at the given index or null
     */
    public ViewportCullable getCullableViewport(int viewportIndex)
    {
        return (viewport instanceof ViewportCullable) ?
               (ViewportCullable)viewport: null;
    }

    /**
     * Returns the number of valid cullable children to process. If there are
     * no valid cullable children, return 0.
     *
     * @return A number greater than or equal to zero
     */
    public int numCullableChildren()
    {
        return viewport != null ? 1 : 0;
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

        if(viewport != null)
            viewport.setUpdateHandler(handler);
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

        if(viewport != null)
            viewport.setLive(state);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set a new viewport instance to be used by this layer.
     * <p>
     * Note that a viewport cannot have more than one parent, so sharing it
     * between layers will result in an error.
     *
     * @param vp The viewport instance to use, or null to clear
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     * @throws AlreadyParentedException This scene already has a current parent
     *    preventing it from being used
     */
    public void setViewport(Viewport vp)
        throws InvalidWriteTimingException, AlreadyParentedException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        viewport = vp;

        // No scene? Ignore it.
        if(vp == null)
            return;

        if(vp.hasParent())
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(HAS_PARENT_PROP);
            throw new AlreadyParentedException(msg);
        }


        vp.setUpdateHandler(updateHandler);
        vp.setLive(alive);
    }

    /**
     * Get the currently set viewport instance. If no viewport is set, null
     * is returned.
     *
     * @return The current viewport instance or null
     */
    public Viewport getViewport()
    {
        return viewport;
    }
}
