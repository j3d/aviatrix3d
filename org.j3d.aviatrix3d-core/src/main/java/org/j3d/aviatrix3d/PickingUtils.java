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
import org.j3d.aviatrix3d.picking.PickRequest;
import org.j3d.aviatrix3d.rendering.BoundingVolume;

/**
 * Utility functionality for picking management.
 * <p>
 *
 * This class is particularly useful for classes that are implementing the
 * {@link org.j3d.aviatrix3d.picking.CustomPickTarget} interface and need to
 * get through the basic picking questions of whether a specific pick request
 * intersects the given bounds. From there, the class implementation can then
 * decide how to further process the bounds or children.
 *
 * @author Justin Couch
 * @version $Revision: 2.2 $
 */
public class PickingUtils
{
    /** Temporary variables for fetching values */
    private float[] wkVec1;
    private float[] wkVec2;

    /**
     * Construct a default instance of the utils
     */
    public PickingUtils()
    {
        wkVec1 = new float[4];
        wkVec2 = new float[4];
    }

    /**
     * Check the given volume for an intersection based on the request. This
     * will automatically take the request and determine the correct
     * intersection request to make. The picking does not test for the mask
     * flags. It is assumed that the caller will do that before calling this
     * class.
     * <p>
     *
     * Notes:<br>
     * <ul>
     * <li>The cone segment intersection doesn't have an equivalent API on
     *     {@link BoundingVolume} so the request is treated as an infinite
     *     request for now.</li>
     * <li>The infinite cylinder picking does not have an equivalent API on
     *     {@link BoundingVolume} so the request is treated as a segment
     *     request for now.</li>
     * </ul>
     *
     * @param volume The volume to be tested for the pick intersection
     * @param req The request to process the pick intersection testing with
     */
    public boolean checkIntersection(BoundingVolume volume, PickRequest req)
    {
        switch(req.pickGeometryType)
        {
            case PickRequest.PICK_BOX:
                return volume.checkIntersectionBox(req.origin,
                                                   req.destination);

            case PickRequest.PICK_CONE:
            case PickRequest.PICK_CONE_SEGMENT:
                return volume.checkIntersectionCone(req.origin,
                                                    req.destination,
                                                    req.additionalData);


            case PickRequest.PICK_CYLINDER:
            case PickRequest.PICK_CYLINDER_SEGMENT:
                float x = req.origin[0] - req.destination[0];
                float y = req.origin[1] - req.destination[1];
                float z = req.origin[2] - req.destination[2];

                float height = (float)Math.sqrt(x * x + y * y + z * z);

                if(height == 0)
                    return false;

                // Start will be the center and end will be the axis vector
                wkVec1[0] = (req.origin[0] + req.destination[0]) * 0.5f;
                wkVec1[1] = (req.origin[1] + req.destination[0]) * 0.5f;
                wkVec1[2] = (req.origin[2] + req.destination[0]) * 0.5f;
                wkVec1[3] = 1;

                wkVec2[0] = x;
                wkVec2[1] = y;
                wkVec2[2] = z;
                wkVec2[3] = 1;

                float radius = req.additionalData;

                return volume.checkIntersectionCylinder(wkVec1,
                                                        wkVec2,
                                                        radius,
                                                        height);

            case PickRequest.PICK_FRUSTUM:
                break;

            case PickRequest.PICK_LINE_SEGMENT:
                wkVec1[0] = req.origin[0];
                wkVec1[1] = req.origin[1];
                wkVec1[2] = req.origin[2];
                wkVec1[3] = 1;

                wkVec2[0] = req.destination[0];
                wkVec2[1] = req.destination[1];
                wkVec2[2] = req.destination[2];
                wkVec2[3] = 1;

                return volume.checkIntersectionSegment(wkVec1, wkVec2);

            case PickRequest.PICK_POINT:
                return volume.checkIntersectionPoint(req.origin);

            case PickRequest.PICK_RAY:
                wkVec1[0] = req.origin[0];
                wkVec1[1] = req.origin[1];
                wkVec1[2] = req.origin[2];
                wkVec1[3] = 1;

                wkVec2[0] = req.destination[0];
                wkVec2[1] = req.destination[1];
                wkVec2[2] = req.destination[2];
                wkVec2[3] = 1;

                return volume.checkIntersectionRay(wkVec1, wkVec2);

            case PickRequest.PICK_SPHERE:
                return volume.checkIntersectionSphere(req.origin, req.additionalData);
        }

        return false;
    }
}
