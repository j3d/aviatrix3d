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
import java.util.ArrayList;

import org.j3d.util.I18nManager;


// Local imports
import org.j3d.aviatrix3d.rendering.ViewportCullable;
import org.j3d.aviatrix3d.rendering.ViewportLayerCullable;

/**
 * An viewport that may, itself contain a large collection of layers.
 * <p>
 *
 * Like the global layers, this viewport allows a selection of local layers
 * that are specific to this viewport to be defined. These viewport layers
 * follow a different structure from the main system layer. All layers in this
 * viewport are rendered at the full size of the viewport.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>hasParentMsg: Error message when the (internal) caller tries to
 *     call setParent() when this class already has a parent.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.11 $
 */
public class CompositeViewport extends Viewport
    implements ViewportCullable
{
    /** Message for when the node is currently owned */
    private static final String CURRENT_PARENT_PROP =
        "org.j3d.aviatrix3d.CompositeViewport.hasParentMsg";

    /** The list of internal layers being managed */
    private ArrayList<ViewportLayer> layers;

    /**
     * Construct a new, empty, viewport instance
     */
    public CompositeViewport()
    {
        super(COMPOSITE);

        layers = new ArrayList<ViewportLayer>();
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
        if(viewportIndex < 0 || viewportIndex >= layers.size())
            return null;
        else
            return (ViewportLayerCullable)layers.get(viewportIndex);
    }

    /**
     * Returns the number of valid cullable children to process. If there are
     * no valid cullable children, return 0.
     *
     * @return A number greater than or equal to zero
     */
    public int numCullableChildren()
    {
        return layers.size();
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
    public void setDimensions(int x, int y, int width, int height)
        throws InvalidWriteTimingException
    {
        super.setDimensions(x, y, width, height);

        int num = layers.size();

        for(int i = 0; i < num; i++)
        {
            ViewportLayer l = layers.get(i);
            l.setViewportDimensions(x, y, width, height);
        }
    }

    //----------------------------------------------------------
    // Methods defined by SceneGraphObject
    //----------------------------------------------------------

    /**
     * Set the viewportgraph update handler for this node.  It will notify
     * all its children of the value. A null value will clear the current
     * handler.
     *
     * @param handler The instance to use as a handler
     */
    protected void setUpdateHandler(NodeUpdateHandler handler)
    {
        super.setUpdateHandler(handler);

        int num = layers.size();

        for(int i = 0; i < num; i++)
            layers.get(i).setUpdateHandler(handler);
    }

    /**
     * Notification that this object is live now. Overridden to make sure that
     * the live state of the nodes represents the same state as the parent
     * viewport graph.
     *
     * @param state true if this should be marked as live now
     */
    protected void setLive(boolean state)
    {
        // Ignore stuff that doesn't change the state
        if(state == alive)
            return;

        super.setLive(state);

        int num = layers.size();

        for(int i = 0; i < num; i++)
            layers.get(i).setLive(state);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Add a new layer to be used by this layer. Adding null references
     * is silently ignored. The layer is added to the end of the current
     * listing.
     * <p>
     * Note that a layer cannot have more than one parent, so sharing it
     * between layers will result in an error.
     *
     * @param vp The layer instance to use
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data change callback method
     * @throws AlreadyParentedException This viewport already has a current parent
     *    preventing it from being used
     */
    public void addViewportLayer(ViewportLayer vp)
        throws InvalidWriteTimingException, AlreadyParentedException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        // No viewport? Ignore it.
        if(vp == null)
            return;

        if(vp.hasParent())
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(CURRENT_PARENT_PROP);
            throw new AlreadyParentedException(msg);
        }

        vp.setViewportDimensions(viewX, viewY, viewWidth, viewHeight);
        vp.setUpdateHandler(updateHandler);
        vp.setLive(alive);


        layers.add(vp);
    }

    /**
     * Add a new layer to be used by this layer at a specified position in
     * the array of layer. Adding null references is silently ignored. If the
     * index is greater than the current number of registered layer, the
     * viewport is added to the end of the current list. If the index is zero
     * or negative, it is inserted at the front of the list.
     * <p>
     * Note that a layer cannot have more than one parent, so sharing it
     * between layers will result in an error.
     *
     * @param vp The layer instance to use
     * @param index The position in the list to put this viewport
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data change callback method
     * @throws AlreadyParentedException This viewport already has a current parent
     *    preventing it from being used
     */
    public void insertViewportLayer(ViewportLayer vp, int index)
        throws InvalidWriteTimingException, AlreadyParentedException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        // No viewport? Ignore it.
        if(vp == null)
            return;

        if(vp.hasParent())
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(CURRENT_PARENT_PROP);
            throw new AlreadyParentedException(msg);
        }

        vp.setViewportDimensions(viewX, viewY, viewWidth, viewHeight);
        vp.setUpdateHandler(updateHandler);
        vp.setLive(alive);

        if(index <= 0)
            layers.add(0, vp);
        else if(index >= layers.size())
            layers.add(vp);
        else
            layers.add(index, vp);
    }

    /**
     * Remove the given layer from this layer. If the layer is not registered,
     * then the request is silently ignored.
     *
     * @param vp The layer instance to be removed
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data change callback method
     */
    public void removeViewportLayer(ViewportLayer vp)
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        // No viewport? Ignore it.
        if(vp == null)
            return;

        if(!layers.remove(vp))
        {
            vp.setUpdateHandler(null);
            vp.setLive(false);
        }
    }

    /**
     * Remove the given layer at the specified index from this layer. If the
     * index is out of bounds, null is returned and nothing happens. All
     * layers at indices above this number are shifted down by 1.
     *
     * @param num The index of the layer to remove
     * @return viewport The viewport that was at the given index
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data change callback method
     */
    public ViewportLayer removeViewportLayer(int num)
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        // No viewport? Ignore it.
        if(num < 0 || num > layers.size())
            return null;

        ViewportLayer vp = layers.remove(num);

        vp.setUpdateHandler(null);
        vp.setLive(false);

        return vp;
    }

    /**
     * Remove all the layers from this viewport.
     *
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data change callback method
     */
    public void clearViewportLayers()
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        int num = layers.size();

        for(int i = num - 1; i >= 0; i--)
        {
            ViewportLayer vp = layers.remove(num);

            vp.setUpdateHandler(null);
            vp.setLive(false);
        }
    }

    /**
     * Get the currently set layer instance at a specific index. If no layer is
     * set at that index, null is returned.
     *
     * @param num The index of the viewport to fetch
     * @return The current viewport instance or null
     */
    public ViewportLayer getViewportLayer(int num)
    {
        return layers.get(num);
    }

    /**
     * Return how many layers this viewport contains.
     *
     * @return A value >= 0
     */
    public int numViewportLayers()
    {
        return layers.size();
    }
}
