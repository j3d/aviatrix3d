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
// None

// Local imports
// None

/**
 * Used to represent a picking request to the system.
 * <p>
 *
 * To pick within the scene graph, the user fills out the fields of this class
 * and then calls the appropriate pick method on the node or group that is the
 * picking root. A pick consists of the geometry type, information about that
 * geometry (origin, extents, radius etc) and then whether to only pick a
 * single object, multiple objects and whether to sort them.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class PickRequest
{
    /**
     * Find anything that is pickable, regardless of the mask set on the
     * geometry. This is set to 0xFFFFFFFF
     */
    public static final int FIND_ALL = 0xFFFFFFFF;

    /**
     * Find any general pickable node type. THis is the generic catch-all
     * picking mask that would be used in the majority of scene graphs. This
     * has a value of 0x1.
     */
    public static final int FIND_GENERAL = 0x1;

    /**
     * Find anything that is defined as being collidable. Collidable is useful
     * for doing stuff like terrain following. This flag has a value of 0x2.
     */
    public static final int FIND_COLLIDABLES = 0x2;

    /**
     * Define the picking as being an object that is visible. Typically this
     * will be combined with the Frustum pick type. This flag has a value of
     * 0x4.
     */
    public static final int FIND_VISIBLES = 0x4;

    /**
     * Define the picking as being an object that is near something. Typically
     * this will be used for implementing a user-defined proximity sensor for
     * triggering different types of behaviours, LODs etc. This flag has a
     * value of 0x8.
     */
    public static final int FIND_PROXIMITY = 0x8;

    /**
     * Pick using a point location. The only sort types that are valid are
     * ANY or ALL. ORDERED and CLOSEST are treated as ALL and ANY respectively.
     * Only {@link #origin} is used as input and it describes the point's
     * location in world space.
     */
    public static final int PICK_POINT = 1;

    /**
     * Pick using a ray location. All sort types that are valid and are
     * calculated relative to the origin. The ray uses {@link #origin} as
     * the start point and {@link #destination} is the direction vector.
     */
    public static final int PICK_RAY = 2;

    /**
     * Pick using a segment of a line. All sort types that are valid and are
     * calculated relative to the origin. The segments uses {@link #origin}
     * as the start point and {@link #destination} is the end point.
     */
    public static final int PICK_LINE_SEGMENT = 3;


    /**
     * Pick using an infinite cylinder. The only sort types that are valid are
     * ANY or ALL. ORDERED and CLOSEST are treated as ALL and ANY respectively.
     * <p>
     * {@link #origin} and {@link #destination} represent the centers of the two
     * end points of the cylinder. {@link #additionalData} represents the
     * radius of the cylinder.
     */
    public static final int PICK_CYLINDER = 4;

    /**
     * Pick using a length of cylinder. The only sort types that are valid are
     * ANY or ALL. ORDERED and CLOSEST are treated as ALL and ANY respectively.
     * <p>
     * {@link #origin} and {@link #destination} represent the centers of the two
     * end points of the cylinder. {@link #additionalData} represents the
     * radius of the cylinder.
     */
    public static final int PICK_CYLINDER_SEGMENT = 5;

    /**
     * Pick using an infinite cone. The only sort types that are valid are
     * ANY or ALL. ORDERED and CLOSEST are treated as ALL and ANY respectively.
     * <p>
     * {@link #origin} represents the location of the point of the cone and
     * {@link #destination} is the vector representing the axis. The cone is
     * infinite in length, with {@link #additionalData} representing the
     * spread angle of the cone
     */
    public static final int PICK_CONE = 6;

    /**
     * Pick using a length of cone. The only sort types that are valid are
     * ANY or ALL. ORDERED and CLOSEST are treated as ALL and ANY respectively.
     * <p>
     * {@link #origin} represents the location of the point of the cone and
     * {@link #destination} is the vector representing the axis. The cone is
     * infinite in length, with {@link #additionalData} representing the
     * spread angle of the cone
     */
    public static final int PICK_CONE_SEGMENT = 7;

    /**
     * Pick using a box. The only sort types that are valid are
     * ANY or ALL. ORDERED and CLOSEST are treated as ALL and ANY respectively.
     * <p>
     * {@link #origin} represents the minimum extents of the box while
     * {@link #destination} represents the maximum extents.
     */
    public static final int PICK_BOX = 8;

    /**
     * Pick using a sphere. The only sort types that are valid are
     * ANY or ALL. ORDERED and CLOSEST are treated as ALL and ANY respectively.
     * <p>
     * {@link #origin} is used as the center of the sphere and it describes
     * the location world space. {@link #additionalData} is used to specify the
     * radius of the sphere.
     */
    public static final int PICK_SPHERE = 9;

    /**
     * Pick the geometry that is in the given view frustum. When using this
     * pick type, the end user should re-allocate the origin variable to be
     * 24 units in length and place the equations for the 6 bounding planes
     * in the array in the order: right, left, bottom, top, near, far.
     * <p>
     * A positive pick is any bounding volume or geometry that is explicitly
     * not completely out of the frustum. So long as part of the geometry falls
     * within the frustum, it is considered a successful find.
     */
    public static final int PICK_FRUSTUM = 10;

    /** Pick all objects that intersect, but don't sort them */
    public static final int SORT_ALL = 1;

    /** Pick anything that matches - most likely the first one it comes across */
    public static final int SORT_ANY = 2;

    /**
     * Pick all objects and sort by closest order. See individual pick geometry
     * type for further details as this may not be supported always.
     */
    public static final int SORT_ORDERED = 3;

    /**
     * Pick only the closest object. See individual pick geometry
     * type for further details as this may not be supported always.
     */
    public static final int SORT_CLOSEST = 4;

    /**
     * What sort of thing should we be picking for. This should be set to one
     * or more of the mask flags that have been bitwise OR'd together. Leaving
     * this at the default value of 0 means nothing would be picked - not a
     * particularly useful setting my Dear Watson.
     */
    public int pickType;

    /** What type of geometry intersection testing should be performed? */
    public int pickGeometryType;

    /**
     * When the picking geometry is line or segment based, should the pick
     * action be against the bounds or the actual geometry?
     */
    public boolean useGeometry;

    /** How the return data be sorted.  Must be one of the SORT_X types. */
    public int pickSortType;

    /** The starting location of the picking request */
    public float[] origin;

    /**
     * The ending location or the direction vector, depending on the geometry
     * type requested.
     */
    public float[] destination;

    /**
     * When the geometry type is a cone, This is the spread angle in radians.
     * When the geometry type is a cylinder, this is the radius of the
     * cylinder. For any other pick type, this is ignored.
     */
    public float additionalData;

    /**
     * The picked data is placed here. The user may optionally provide the
     * data here and the picking routines will update the values. If no data
     * is provided, then the picking system will create a new instance for
     * the user. For single picks (closest, any) then this object will be an
     * instance of SceneGraphPath. For multiple picks (sorted, all) then this
     * will be an ArrayList<SceneGraphPath> instance and will contain all of
     * the scene graph paths that were found.
     */
    public Object foundPaths;

    /** The number of valid picks that were found in this request. */
    public int pickCount;

    /**
     * If this flag is set to true, generate the local to virtual world
     * matrix for the picked result. This flag is set to true by default.
     */
    public boolean generateVWorldMatrix;


    /**
     * Create a new instance of this class with all values set to their
     * defaults. No pick geometry type or sort type is specified.
     */
    public PickRequest()
    {
        origin = new float[3];
        destination = new float[3];

        generateVWorldMatrix = true;
        useGeometry = false;
    }
}
