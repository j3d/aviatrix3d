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
// None

/**
 * Representation of the top level structure of a piece of scene graph that
 * can form a coherent rendering in 2D.
 * <p>
 *
 * A 2D scene has a fixed view environment that is not editable by the end
 * users. The viewpoint is required to be an ortho viewport and is constructed
 * so that a OpenGL value of 1.0 corresponds to a single pixel. The viewpoint
 * continually updates its ortho params based on the viewport size.
 * Note that although a viewpoint is specified here, all the parameters of it
 * are ignored. All we use is the transformation information from the root of
 * scene graph to this viewpoint location in order to know where to place the
 * view information in 2D space (ie it provides the ability to pan and zoom.
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
 * @version $Revision: 2.4 $
 */
public class Scene2D extends Scene
{
    /** Message when setting the active view if it contains a shared parent */
    private static final String SHARED_VP_PROP =
        "org.j3d.aviatrix3d.Scene2D.sharedVpMsg";

    /** Message when setting the active background if it contains a shared parent */
    private static final String SHARED_BG_PROP =
        "org.j3d.aviatrix3d.Scene2D.sharedBgMsg";

    /** The Scene Graph renderableObjects */
    private Group renderableObjects;

    /** The current viewpoint instance */
    private Viewpoint currentViewpoint;

    /** The current background instance */
    private Background currentBackground;

    /**
     * Create a default instance of this scene with no content provided.
     */
    public Scene2D()
    {
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
