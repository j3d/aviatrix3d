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

package org.j3d.aviatrix3d.picking;

// External imports
import java.util.ArrayList;

// Local imports
// None

/**
 * A marker interface that indicates the object that implements is capable of
 * supporting pick intersection requests.
 * <p>
 *
 * The end user can call these objects requesting a pick be made using the
 * given request object. From that request, processing will be performed and
 * the found objects are returned as part of the picking request.
 * <p>
 *
 * Pick intersection tests allows the classification of various picking types
 * through the use of bit masks. The masks are bitwise-OR'd together to form
 * the set of pickable capabilities. A number of pre-defined pick masks are
 * provided in the {@link PickRequest} class. Since the OR operation is used,
 * there are a total of 32 different "types" of pickable objects that could be
 * defined by the system.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface PickableObject
{
    /**
     * Find any general pickable node type. THis is the generic catch-all
     * picking mask that would be used in the majority of scene graphs. This
     * has a value of 0x1.
     */
    public static final int GENERAL_OBJECT = PickRequest.FIND_GENERAL;

    /**
     * Define the picking as being collidable. Collidable is useful
     * for doing stuff like terrain following. This flag has a value of 0x2.
     */
    public static final int COLLIDABLE_OBJECT = PickRequest.FIND_COLLIDABLES;

    /**
     * Define the picking as being an object that is visible. Typically this
     * will be combined with the Frustum pick type. This flag has a value of
     * 0x4.
     */
    public static final int VISIBLE_OBJECT = PickRequest.FIND_VISIBLES;

    /**
     * Define the picking as being an object that is near something. Typically
     * this will be used for implementing a user-defined proximity sensor for
     * triggering different types of behaviours, LODs etc. This flag has a
     * value of 0x8.
     */
    public static final int PROXIMITY_OBJECT = PickRequest.FIND_PROXIMITY;

    /**
     * Set the node as being pickable currently using the given bit mask.
     * A mask of 0 will completely disable picking.
     *
     * @param state A bit mask of available options to pick for
     */
    public void setPickMask(int state);

    /**
     * Get the current pickable state mask of this object. A value of zero
     * means it is completely unpickable.
     *
     * @return A bit mask of available options to pick for
     */
    public int getPickMask();

    /**
     * Check for all intersections against this geometry and it's children to
     * see if there is an intersection with the given set of requests.
     *
     * @param req The list of picks to be made, starting at this object
     * @param numRequests The number of valid pick requests to process
     * @throws NotPickableException This object has been marked as non pickable,
     *   but you decided to try to call the method anyway
     * @throws InvalidPickTimingException An attempt was made to pick outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void pickBatch(PickRequest[] req, int numRequests)
        throws NotPickableException, InvalidPickTimingException;

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
    public void pickSingle(PickRequest req)
        throws NotPickableException, InvalidPickTimingException;
}
