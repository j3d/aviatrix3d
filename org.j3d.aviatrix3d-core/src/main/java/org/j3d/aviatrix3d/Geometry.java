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
import org.j3d.aviatrix3d.picking.*;

import org.j3d.aviatrix3d.rendering.BoundingVolume;
import org.j3d.aviatrix3d.rendering.GeometryRenderable;

/**
 * Geometry is an abstract class that specifies the geometry component
 * information required by a Shape3D node.
 * <p>
 *
 * Geometry may take several forms. Vertex geometry like Triangle Arrays are
 * not the only form of shape information that is usable in the OpenGL
 * rendering pipeline. This represents the basic information that is common
 * to all geometry.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>invalidChildIndexMsg: Error message when the user provides an index
 *     for a child that is < 0 or > the number of children.</li>
 * <li>pickTimingMsg: Error message when attempting to pick outside the app
 *     update observer callback.</li>
 * <li>notPickableMsg: Error message when the user has set the pickmask to
 *     zero and then requested a pick directly on this object.</li>
 * </ul>
 *
 * @author Alan Hudson
 * @version $Revision: 1.36 $
 */
public abstract class Geometry extends NodeComponent
    implements PickableObject, LeafPickTarget, GeometryRenderable
{
    /** Message when attempting to pick the object at the wrong time */
    private static final String PICK_TIMING_PROP =
        "org.j3d.aviatrix3d.Geometry.pickTimingMsg";

    /**
     * You are attempting to pick a node you have marked as not-pickable, yet
     * called the pick method on this class anyway.
     */
    private static final String PICKABLE_FALSE_PROP =
        "org.j3d.aviatrix3d.Geometry.notPickableMsg";

    /** Message when the setParent does not receive a group */
    private static final String NULL_PARENT_PROP =
        "org.j3d.aviatrix3d.Geometry.nullParentMsg";

    /** The initial size of the children list */
    private static final int LIST_START_SIZE = 5;

    /** The increment size of the list if it gets overflowed */
    private static final int LIST_INCREMENT = 5;

    /** Sharable version of the null bounds object for those that need it */
    protected static final BoundingVoid INVALID_BOUNDS = new BoundingVoid();

    /** Listing of all the parents of this node */
    protected Node[] parentList;

    /** Index to the next place to add items in the nodeList */
    protected int lastParentList;

    /** Update handler for the external code. Not created until needed. */
    protected InternalNodeUpdateListener internalUpdater;

    /** Bounding volume set by the user */
    protected BoundingVolume bounds;

    /** Was the bounds automatically calculated? */
    protected boolean implicitBounds;

    /** Flag indicating if this object is pickable currently */
    protected int pickFlags;

    /** Flag indicating current object has alpha values actually set */
    protected boolean validAlpha;

    /**
     * Internal implementation of the InternalNodeUpdateListener. Done as an
     * inner class to hide the calls from public consumption.
     */
    private class InternalUpdater
        implements InternalNodeUpdateListener
    {

        /**
         * Notify this node to update it's bounds now and propogate those new
         * bounds to their parent(s).
         */
        public void updateBoundsAndNotify()
        {
            updateBounds();
        }
    }

    /**
     * The default constructor initialised the base values.
     * <p>
     * By default the values are:<br>
     * implicitBounds: true;<br>
     * pickable: true;<br>
     * validAlpha: false<br>
     */
    public Geometry()
    {
        parentList = new Node[LIST_START_SIZE];
        lastParentList = 0;
        implicitBounds = true;
        pickFlags = 0xFFFFFFFF;
        validAlpha = false;
    }

    //---------------------------------------------------------------
    // Methods defined by GeometryRenderable
    //---------------------------------------------------------------

    /**
     * Check to see if this geometry has anything that could be interpreted as
     * an alpha value. For example a Raster with RGBA values or vertex geometry
     * with 4-component colours for the vertices.
     */
    @Override
    public boolean hasTransparency()
    {
        return validAlpha;
    }

    //---------------------------------------------------------------
    // Methods defined by PickableObject
    //---------------------------------------------------------------

    /**
     * Set the node as being pickable currently using the given bit mask.
     * A mask of 0 will completely disable picking.
     *
     * @param state A bit mask of available options to pick for
     */
    @Override
    public void setPickMask(int state)
    {
        pickFlags = state;
    }

    /**
     * Get the current pickable state mask of this object. A value of zero
     * means it is completely unpickable.
     *
     * @return A bit mask of available options to pick for
     */
    @Override
    public int getPickMask()
    {
        return pickFlags;
    }

    /**
     * Check for all intersections against this geometry to
     * see if there is an intersection with the given set of requests.
     *
     * @param reqs The list of picks to be made, starting at this object
     * @param numRequests The number of valid pick requests to process
     * @throws NotPickableException This object has been marked as non pickable,
     *   but you decided to try to call the method anyway
     * @throws InvalidPickTimingException An attempt was made to pick outside
     *   of the ApplicationUpdateObserver callback method
     */
    @Override
    public void pickBatch(PickRequest[] reqs, int numRequests)
        throws NotPickableException, InvalidPickTimingException
    {
        if(updateHandler == null || !updateHandler.isPickingPermitted())
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(PICK_TIMING_PROP);
            throw new InvalidPickTimingException(msg);
        }

        if(pickFlags == 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(PICKABLE_FALSE_PROP);
            throw new NotPickableException(msg);
        }
    }

    /**
     * Check for all intersections against this geometry to
     * see if there is an intersection with the given set of requests.
     *
     * @param req The details of the pick to be made
     * @throws NotPickableException This object has been marked as non pickable,
     *   but you decided to try to call the method anyway
     * @throws InvalidPickTimingException An attempt was made to pick outside
     *   of the ApplicationUpdateObserver callback method
     */
    @Override
    public void pickSingle(PickRequest req)
        throws NotPickableException, InvalidPickTimingException
    {
        if(updateHandler == null || !updateHandler.isPickingPermitted())
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(PICK_TIMING_PROP);
            throw new InvalidPickTimingException(msg);
        }

        if(pickFlags == 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(PICKABLE_FALSE_PROP);
            throw new NotPickableException(msg);
        }
    }

    //---------------------------------------------------------------
    // Methods defined by LeafPickTarget
    //---------------------------------------------------------------

    /**
     * Return the type constant that represents the type of pick target this
     * is. Used to provided optimised picking implementations.
     *
     * @return One of the _PICK_TYPE constants
     */
    @Override
    public final int getPickTargetType()
    {
        return LEAF_PICK_TYPE;
    }

    /**
     * Check for all intersections against this geometry using a line segment and
     * return the exact distance away of the closest picking point. Default
     * implementation always returns false indicating that nothing was found.
     * Derived classes should override and provide a real implementation.
     *
     * @param start The start point of the segment
     * @param end The end point of the segment
     * @param findAny True if it only has to find a single intersection and can
     *   exit as soon as it finds the first intersection. False if it must find
     *   the closest polygon
     * @param dataOut An array to put the data in for the intersection. Exact
     *   format is described by the flags
     * @param dataOutFlags A set of derived-class specific flags describing what
     *   data should be included in the output array
     * @return True if an intersection was found according to the input request
     * @throws NotPickableException This object has been marked as non pickable,
     *   but you decided to try to call the method anyway
     */
    @Override
    public boolean pickLineSegment(float[] start,
                                   float[] end,
                                   boolean findAny,
                                   float[] dataOut,
                                   int dataOutFlags)
        throws NotPickableException
    {
        if(pickFlags == 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(PICKABLE_FALSE_PROP);
            throw new NotPickableException(msg);
        }

        return false;
    }

    /**
     * Check for all intersections against this geometry using a line ray and
     * return the exact distance away of the closest picking point. Default
     * implementation always returns false indicating that nothing was found.
     * Derived classes should override and provide a real implementation.
     *
     * @param origin The start point of the ray
     * @param direction The direction vector of the ray
     * @param findAny True if it only has to find a single intersection and can
     *   exit as soon as it finds the first intersection. False if it must find
     *   the closest polygon
     * @param dataOut An array to put the data in for the intersection. Exact
     *   format is described by the flags
     * @param dataOutFlags A set of derived-class specific flags describing what
     *   data should be included in the output array
     * @return True if an intersection was found according to the input request
     * @throws NotPickableException This object has been marked as non pickable,
     *   but you decided to try to call the method anyway
     */
    @Override
    public boolean pickLineRay(float[] origin,
                               float[] direction,
                               boolean findAny,
                               float[] dataOut,
                               int dataOutFlags)
        throws NotPickableException
    {
        if(pickFlags == 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(PICKABLE_FALSE_PROP);
            throw new NotPickableException(msg);
        }

        return false;
    }

    //---------------------------------------------------------------
    // Methods defined by PickTarget
    //---------------------------------------------------------------

    /**
     * Check the given pick mask against the node's internal pick mask
     * representation. If there is a match in one or more bitfields then this
     * will return true, allowing picking to continue to process for this
     * target.
     *
     * @param mask The bit mask to check against
     * @return true if the mask has an overlapping set of bitfields
     */
    @Override
    public boolean checkPickMask(int mask)
    {
        return ((pickFlags & mask) != 0);
    }

    /**
     * Get the bounds of this picking target so that testing can be performed
     * on the object.
     *
     * @return A representation of the volume representing the pickable objects
     */
    @Override
    public BoundingVolume getPickableBounds()
    {
        return bounds;
    }

    //---------------------------------------------------------------
    // Methods defined by NodeComponent
    //---------------------------------------------------------------

    /**
     * Add a parent to this node.
     *
     * @param p The new parent instance to add to the list
     * @throws AlreadyParentedException There is a valid parent already set
     * @throws InvalidNodeTypeException Not a group node
     */
    protected void addParent(Node p)
        throws AlreadyParentedException, InvalidNodeTypeException
    {
        if(p == null)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NULL_PARENT_PROP);
            throw new InvalidNodeTypeException(msg);
        }

        // Check to see that this parent isn't already in the list
        for(int i = 0; i < lastParentList; i++)
            if(parentList[i] == p)
                return;

        resizeList();
        parentList[lastParentList++] = p;
    }

    /**
     * Remove a parent from this shared group. Since setParent() cannot be
     * used to remove a parent from the graph, you'll need to use this method
     * to remove the parent.
     *
     * @param p The new parent instance to remove from the list
     */
    protected void removeParent(Node p)
    {
        // find the location, move everything down one
        for(int i = 0; i < lastParentList; i++)
        {
            if(parentList[i] == p)
            {
                int move_size = lastParentList - i;
                if(move_size != 0)
                    System.arraycopy(parentList,
                                     i,
                                     parentList,
                                     i + 1,
                                     move_size);
                break;
            }
        }
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Internal method to recalculate the implicit bounds of this Node. By
     * default the bounds are a point sphere, so derived classes should
     * override this method with something better.
     */
    protected abstract void recomputeBounds();

    /**
     * Check to see whether this shape is something that represents 2D or 3D
     * geometry. Pure 2D geometry is not effected by any
     * {@link org.j3d.aviatrix3d.rendering.EffectRenderable}, while 3D is.
     * Note that this can be changed depending on the type of geometry itself.
     * A Shape3D node with an IndexedLineArray that only has 2D coordinates is
     * as much a 2D geometry as a raster object.
     *
     * @return True if this is 2D geometry, false if this is 3D
     */
    public abstract boolean is2D();

    /**
     * Update this node's bounds and then call the parent to update it's
     * bounds. Used to propogate bounds changes from the leaves of the tree
     * to the root. A node implementation may decide when and where to tell
     * the parent(s)s that updates are ready.
     */
    protected void updateBounds()
    {
        recomputeBounds();

        for(int i = 0; i < lastParentList; i++)
            parentList[i].updateBounds();
    }

    /**
     * Mark this node as having dirty bounds due to it's geometry having
     * changed.
     */
    protected void markBoundsDirty()
    {
        for(int i = 0; i < lastParentList; i++)
            parentList[i].markBoundsDirty();
    }

    /**
     * Check to see if this geometry is making the geometry visible or
     * not. The default implementation always makes it visible, but
     * derived classes may choose not to. For example, there are no
     * valid vertices defined for a set of triangles.
     *
     * @return true when the geometry is visible
     */
    protected boolean isVisible()
    {
        return true;
    }

    /**
     * Notify the node that you have updates to the node that might alter
     * its bounds.
     *
     * @param l The change requestor
     * @throws InvalidListenerSetTimingException If called when the node is not live or
     *   if called during one of the bounds/data changed callbacks
     */
    public void boundsChanged(NodeUpdateListener l)
        throws InvalidListenerSetTimingException
    {
        if(!isLive())
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(LISTENER_SET_TIMING_ERR_PROP);
            throw new InvalidListenerSetTimingException(msg);
        }

        // Ignore till live
        if(updateHandler == null)
            return;

        if(internalUpdater == null)
            internalUpdater = new InternalUpdater();

        if(updateHandler.boundsChanged(l, this, internalUpdater))
        {
            for(int i = 0; i < lastParentList; i++)
                parentList[i].markBoundsDirty();
        }
    }

    /**
     * Set the bounds to the given explicit value. When set, auto computation
     * of the bounds of this node is turned off. A value of null can be used
     * to clear the current explicit bounds and return to auto computation.
     *
     * @param b The new bounds to use or null to clear
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds changed callback method
     */
    public void setBounds(BoundingVolume b)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        bounds = b;
        implicitBounds = (bounds == null);
    }

    /**
     * Get the currently set bounds for this object. If no explicit bounds have
     * been set, then an implicit set of bounds is returned based on the
     * current scene graph state.
     *
     * @return The current bounds of this object
     */
    public BoundingVolume getBounds()
    {
        // need to check and set a new instance here if needed.
        if(implicitBounds && bounds == null)
            recomputeBounds();

        return bounds;
    }

    /**
     * Resize the list if needed. Marked as final in order to encourage the
     * compiler to inline the code for faster execution
     */
    private final void resizeList()
    {
        if((lastParentList + 1) == parentList.length)
        {
            int old_size = parentList.length;
            int new_size = old_size + LIST_INCREMENT;

            Node[] tmp_nodes = new Node[new_size];

            System.arraycopy(parentList, 0, tmp_nodes, 0, old_size);

            parentList = tmp_nodes;
        }
    }
}
