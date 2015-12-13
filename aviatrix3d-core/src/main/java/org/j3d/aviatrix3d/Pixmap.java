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
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.*;
import org.j3d.aviatrix3d.picking.*;

/**
 * A Pixmap wraps 2D screen-aligned pixel blits.
 * <p>
 *
 * By default, all pixmaps are pickable.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>pickTimingMsg: Error message when attempting to pick outside the app
 *     update observer callback.</li>
 * <li>notPickableMsg: Error message when the user has set the pickmask to
 *     zero and then requested a pick directly on this object.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 2.10 $
 */
public class Pixmap extends Leaf2D
    implements PickableObject,
               SinglePickTarget,
               LeafCullable,
               ShapeRenderable,
               ObjectRenderable
{
    /** Message when attempting to pick the object at the wrong time */
    private static final String PICK_TIMING_PROP =
        "org.j3d.aviatrix3d.Group.pickTimingMsg";

    /**
     * You are attempting to pick a node you have marked as not-pickable, yet
     * called the pick method on this class anyway.
     */
    private static final String PICKABLE_FALSE_PROP =
        "org.j3d.aviatrix3d.Group.notPickableMsg";

    /** Flag indicating if this object is pickable currently */
    private int pickFlags;

    /** The pixels/bits rendered on screen */
    private Raster geom;

    /**
     * Creates a shape with no geometry or appearance set.
     */
    public Pixmap()
    {
        pickFlags = 0xFFFFFFFF;
    }

    //---------------------------------------------------------------
    // Methods defined by LeafCullable
    //---------------------------------------------------------------

    /**
     * Get the type that this cullable represents.
     *
     * @return One of the _CULLABLE constants
     */
    @Override
    public int getCullableType()
    {
        return GEOMETRY_CULLABLE;
    }

    /**
     * Get the child renderable of this object.
     *
     * @return an array of nodes
     */
    @Override
    public Renderable getRenderable()
    {
        return this;
    }

    //---------------------------------------------------------------
    // Methods defined by ShapeRenderable
    //---------------------------------------------------------------

    /**
     * State check to see whether the shape in it's current setup
     * is visible. Various reasons for this are stated in the class docs.
     *
     * @return true if the shape has something to render
     */
    @Override
    public boolean isVisible()
    {
//        boolean app_ok = (app != null) ? app.isVisible() : true;
        boolean geom_ok = (geom != null) ? geom.isVisible() : false;

        return geom_ok; // && app_ok;
    }

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
    @Override
    public boolean is2D()
    {
        return true;
    }

    /**
     * Get the centre of the shape object. Used for transparency depth sorting
     * based on the center of the bounds of the object.
     *
     * @param center The object to copy the center coordinates in to
     */
    @Override
    public void getCenter(float[] center)
    {
        bounds.getCenter(center);
    }

    /**
     * Fetch the renderable that represents the geometry of this shape.
     *
     * @return The current geometry renderable or null if none
     */
    @Override
    public GeometryRenderable getGeometryRenderable()
    {
        return geom;
    }

    /**
     * Fetch the renderable that represents the visual appearance modifiers of
     * this shape.
     *
     * @return The current appearance renderable or null if none
     */
    @Override
    public AppearanceRenderable getAppearanceRenderable()
    {
        // We don't have appearances right now
        return null;
    }

    //---------------------------------------------------------------
    // Methods defined by ObjectRenderable
    //---------------------------------------------------------------

    /**
     * This method is called to render this node.  All openGL commands needed
     * to render the node should be executed.  Any transformations needed
     * should be added to the transformation stack premultiplied.  This
     * method must be re-entrant as it can be called from multiple
     * places at once.
     *
     * @param gl The gl context to draw with
     */
    @Override
    public void render(GL2 gl)
    {
        // TODO:
        // In the future, this will need to directly fetch the renderable from
        // each object and act on that rather than on the object itself.
//        if(app != null)
//            app.render(gl);

        if(geom != null)
            geom.render(gl);
    }

    /*
     * This method is called after a node has been rendered.  This method
     * must be re-entrant.
     *
     * @param gl The gl context to draw with
     */
    @Override
    public void postRender(GL2 gl)
    {
//        if(app != null)
//            app.postRender(gl);
    }

    //---------------------------------------------------------------
    // Methods defined by Node
    //---------------------------------------------------------------

    /**
     * Update this node's bounds and then call the parent to update it's
     * bounds. Used to propogate bounds changes from the leaves of the tree
     * to the root.
     */
    @Override
    protected void updateBounds()
    {
        recomputeBounds();

        if(parent != null)
            parent.updateBounds();
    }

    /**
     * Mark this node as having dirty bounds due to one of it's children having
     * their bounds changed.
     */
    @Override
    protected void markBoundsDirty()
    {
        if(parent != null)
            parent.markBoundsDirty();
    }

    /**
     * Internal method to recalculate the implicit bounds of this Node. By
     * default the bounds are a point sphere, so derived classes should
     * override this method with something better.
     */
    @Override
    protected void recomputeBounds()
    {
        if(!implicitBounds)
            return;

        if(geom == null)
        {
            bounds = INVALID_BOUNDS;
        }
        else
        {
            if((bounds == null) || (bounds == INVALID_BOUNDS))
                bounds = new BoundingBox();

            BoundingBox bbox = (BoundingBox)bounds;
            BoundingVolume box_bounds = geom.getBounds();

            if (box_bounds instanceof BoundingVoid)
                bounds = INVALID_BOUNDS;
            else {
                float[] min = new float[3];
                float[] max = new float[3];
                box_bounds.getExtents(min, max);

                bbox.setMinimum(min);
                bbox.setMaximum(max);
            }
        }
    }

    /**
     * Request a recomputation of the bounds of this object. If this object is
     * not currently live, you can request a recompute of the bounds to get the
     * most current values. If this node is currently live, then the request is
     * ignored.
     * <p>
     * This will request the geometry to recompute the bounds. If the geometry
     * is found to be live during this process, it will not update, and thus
     * the value used will be the last updated (ie from the previous frame it
     * was processed).
     */
    @Override
    public void requestBoundsUpdate()
    {
        if(alive || !implicitBounds || (geom == null) || geom.isLive())
            return;

        geom.recomputeBounds();
        recomputeBounds();
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

        if(geom != null)
            geom.setLive(state);

//        if(app != null)
//            app.setLive(state);

        // This will also force the geometry recompute of the bounds.
        super.setLive(state);
    }

    //---------------------------------------------------------------
    // Methods defined by SceneGraphObject
    //---------------------------------------------------------------

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

//        if(app != null)
//            app.setUpdateHandler(updateHandler);

        if(geom != null)
            geom.setUpdateHandler(updateHandler);
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
    @Override
    protected void checkForCyclicChild(SceneGraphObject parent)
        throws CyclicSceneGraphStructureException
    {
        if(parent == this)
            throw new CyclicSceneGraphStructureException();

//        if(app != null)
//            app.checkForCyclicChild(parent);

        // Don't bother with geometry for now. That's just wasted CPU cycles.
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
     * Check for all intersections against this geometry and it's children to
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

        PickingManager picker = updateHandler.getPickingManager();

        picker.pickBatch(this, reqs, numRequests);
    }

    /**
     * Check for all intersections against this geometry and it's children to
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

        PickingManager picker = updateHandler.getPickingManager();

        picker.pickSingle(this, req);
    }

    //---------------------------------------------------------------
    // Methods defined by SinglePickTarget
    //---------------------------------------------------------------

    /**
     * Return the child that is pickable of from this target. If there is none
     * then return null.
     *
     * @return The child pickable object or null
     */
    @Override
    public PickTarget getPickableChild()
    {
        return (geom instanceof PickTarget) ? (PickTarget)geom : null;
    }

    //---------------------------------------------------------------
    // Methods defined by PickTarget
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
        return SINGLE_PICK_TYPE;
    }

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
    // Methods defined by Comparable
    //---------------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param o The objec to be compared
     * @return -1, 0 or 1 depending on order
     * @throws ClassCastException The specified object's type prevents it from
     *    being compared to this Object
     */
    @Override
    public int compareTo(Object o)
        throws ClassCastException
    {
        Pixmap app = (Pixmap)o;
        return compareTo(app);
    }

    //---------------------------------------------------------------
    // Methods defined by Object
    //---------------------------------------------------------------

    /**
     * Compare this object for equality to the given object.
     *
     * @param o The object to be compared
     * @return True if these represent the same values
     */
    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof Pixmap))
            return false;
        else
            return equals((Pixmap)o);
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Get the current geometry associated with this shape.
     *
     * @return the current instance
     */
    public Raster getRaster()
    {
        return geom;
    }

    /**
     * Set the pixel data for this pixmap. A null value will clear the drawable
     * pixels.
     *
     * @param raster The pixel data to be drawn
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds changed callback method
     */
    public void setRaster(Raster raster)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        if(geom != null)
        {
            geom.setLive(false);
            geom.removeParent(this);
        }

        geom = raster;

        if(geom != null)
        {
            geom.addParent(this);
            geom.setLive(alive);
            geom.setUpdateHandler(updateHandler);
        }
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param sh The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(Pixmap sh)
    {
        if(sh == null)
            return 1;

        if(sh == this)
            return 0;

/*
        if(app != sh.app)
        {
            if(app == null)
                return -1;
            else if(sh.app == null)
                return 1;

            int res = app.compareTo(sh.app);
            if(res != 0)
                return res;
        }
*/

        if(geom != sh.geom)
        {
            if(geom == null)
                return -1;
            else if(sh.geom == null)
                return 1;

            int res = geom.compareTo(sh.geom);
            if(res != 0)
                return res;
        }

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param sh The shape instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(Pixmap sh)
    {
        if(sh == this)
            return true;

        if(sh == null)
            return false;

//        if((app != sh.app) && ((app == null) || !app.equals(sh.app)))
//            return false;

        if((geom != sh.geom) && ((geom == null) || !geom.equals(sh.geom)))
            return false;

        return true;
    }
}
