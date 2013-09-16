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

// Local imports
import org.j3d.aviatrix3d.rendering.BoundingVolume;

/**
 * An internal marker interface that indicates an object that can support
 * pick processing requests.
 * <p>
 *
 * This is an inward-facing interface used to by the implementation of the
 * {@link PickingManager} to walk a scene graph of objects traversing them for
 * picking processing requests. This is a base interface, from which derived
 * interface types describe particular pickable structures that can be used
 * to fetch descendent objects.
 * <p>
 *
 * End users should never call the methods on this or derived interfaces.
 * <p>
 *
 * <h3>Implementor Guidelines</h3>
 *
 * <p>
 * There is no requirement to implement a separate set of logic for the picking
 * bounds versus the geometry bounds. The implementation is free to use the
 * object instance for both, yet can also provide a more optimised form if it
 * desires.
 * </p>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public interface PickTarget
{
    /** Standard type for a group picking type */
    public static final int GROUP_PICK_TYPE = 1;

    /** Standard type for a single picking type */
    public static final int SINGLE_PICK_TYPE = 2;

    /** Standard type for a leaf picking type */
    public static final int LEAF_PICK_TYPE = 3;

    /** Standard type for a custom picking type */
    public static final int CUSTOM_PICK_TYPE = 4;

    /**
     * Return the type constant that represents the type of pick target this
     * is. Used to provided optimised picking implementations.
     *
     * @return One of the _PICK_TYPE constants
     */
    public int getPickTargetType();

    /**
     * Check the given pick mask against the node's internal pick mask
     * representation. If there is a match in one or more bitfields then this
     * will return true, allowing picking to continue to process for this
     * target.
     *
     * @param mask The bit mask to check against
     * @return true if the mask has an overlapping set of bitfields
     */
    public boolean checkPickMask(int mask);

    /**
     * Get the bounds of this picking target so that testing can be performed
     * on the object.
     *
     * @return A representation of the volume representing the pickable objects
     */
    public BoundingVolume getPickableBounds();
}
