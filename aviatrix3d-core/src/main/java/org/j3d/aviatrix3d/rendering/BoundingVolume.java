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

package org.j3d.aviatrix3d.rendering;

// External imports
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector4d;

// Local imports
// None


/**
 * Base representation of a class that can representing bound information.
 * <p>
 *
 * Bounds describe a 3D volume, and various abstract intersection methods are
 * defined. All methods must be implemented as various methods may be called
 * during different parts of the rendering cycle. All bounds and rays are
 * assumed to be represented in the same local coordinate space.
 * <p>
 *
 * All intersection tests assume that the incoming picking details have been
 * transformed to the local coordinate space that this volume exists in. The
 * one exception to this rule is the view frustum intersection tests. The
 * reason for this is performance - transforming 6 planes of the view matrix
 * requires 6 matrix-vector multiplications, whereas by providing the reverse
 * we only need to perform 2 matrix-vector mulitplications. Since view frustum
 * culling is such a performance critical path of the rendering system we have
 * decided to optimise for speed here rather than consistent API coding.
 *
 * @author Justin Couch
 * @version $Revision: 2.3 $
 */
public abstract class BoundingVolume
{
    /** The bounding volume type is not valid (ie it has no bounds) */
    public static final int NULL_BOUNDS = 0;

    /** The bounding volume type is sphere */
    public static final int SPHERE_BOUNDS = 1;

    /** The bounding volume type is sphere */
    public static final int BOX_BOUNDS = 2;

    /** The bounding volume type is user provided geometry */
    public static final int GEOMETRY_BOUNDS = 3;

    /** The frustum check revealed all points outside the frustum */
    public static final int FRUSTUM_ALLOUT = 0;

    /** The frustum check revealed all points inside the frustum */
    public static final int FRUSTUM_ALLIN = 2;

    /** The frustum check revealed some points inside the frustum */
    public static final int FRUSTUM_PARTIAL = 1;

    /**
     * The default constructor.
     */
    protected BoundingVolume()
    {
    }

    /**
     * The type of bounds this object represents.
     *
     * @return One of the constant types defined
     */
    public abstract int getType();

    /**
     * Get the maximum extents of the bounding volume.
     *
     * @param min The minimum position of the bounds
     * @param max The maximum position of the bounds
     */
    public abstract void getExtents(float[] min, float[] max);

    /**
     * Get the center of the bounding volume.
     *
     * @param center The center of the bounds will be copied here
     */
    public abstract void getCenter(float[] center);

    /**
     * Check for the given point lieing inside this bounds.
     *
     * @param pos The location of the point to test against
     * @return true if the point lies inside this bounds
     */
    public abstract boolean checkIntersectionPoint(float[] pos);

    /**
     * Check for the given ray intersecting this bounds. The line is
     * described as a starting point and a vector direction.
     *
     * @param pos The start location of the ray
     * @param dir The direction vector of the ray
     * @return true if the ray intersects this bounds
     */
    public abstract boolean checkIntersectionRay(float[] pos, float[] dir);

    /**
     * Check for the given line segment intersecting this bounds. The line is
     * described as the line connecting the start and end points.
     *
     * @param start The start location of the segment
     * @param end The start location of the segment
     * @return true if the segment intersects this bounds
     */
    public abstract boolean checkIntersectionSegment(float[] start,
                                                     float[] end);

    /**
     * Check for the given sphere intersecting this bounds. The sphere is
     * described by a centre location and the radius.
     *
     * @param center The location of the sphere's center
     * @param radius The radius of the sphere
     * @return true if the sphere intersects this bounds
     */
    public abstract boolean checkIntersectionSphere(float[] center,
                                                    float radius);

    /**
     * Check for the given triangle intersecting this bounds. Assumes the 
     * three coordinates of the triangle are declared in RH coordinate system.
     *
     * @param v0 The first vertex of the triangle
     * @param v1 The second vertex of the triangle
     * @param v2 The third vertex of the triangle
     * @return true if the sphere intersects this bounds
     */
    public abstract boolean checkIntersectionTriangle(float[] v0,
                                                      float[] v1,
                                                      float[] v2);

    /**
     * Check for the given cylinder segment intersecting this bounds. The
     * cylinder is described by a centre location, axial direction and the
     * radius.
     *
     * @param center The location of the cylinder's center
     * @param direction A unit vector indicating the axial direction
     * @param radius The radius of the cylinder
     * @param height The half-height of the cylinder from the center point
     * @return true if the sphere intersects this bounds
     */
    public abstract boolean checkIntersectionCylinder(float[] center,
                                                      float[] direction,
                                                      float radius,
                                                      float height);

    /**
     * Check for the given cone intersecting this bounds. The
     * cone is described by the location of the vertex, a direction vector
     * and the spread angle of the cone. The cone is considered to be infinite
     * in length.
     *
     * @param vertex The location of the cone's vertex
     * @param direction A unit vector indicating the axial direction
     * @param angle The spread angle of the cone
     * @return true if the sphere intersects this bounds
     */
    public abstract boolean checkIntersectionCone(float[] vertex,
                                                  float[] direction,
                                                  float angle);

    /**
     * Check for the given AA box intersecting this bounds. The box is
     * described by the minimum and maximum extents on each axis.
     *
     * @param minExtents The minimum extent value on each axis
     * @param maxExtents The maximum extent value on each axis
     * @return true if the box intersects this bounds
     */
    public abstract boolean checkIntersectionBox(float[] minExtents,
                                                 float[] maxExtents);

    /**
     * Check whether this volume intersects with the view frustum.
     *
     * @param planes The 6 planes of the frustum
     * @param mat The vworld to local transformation matrix
     * @return int FRUSTUM_ALLOUT, FRUSTUM_ALLIN, FRUSTUM_PARTIAL.
     */
    public abstract int checkIntersectionFrustum(Vector4d[] planes,
                                                 Matrix4d mat);


    /**
     * Transform the current postion by the given transformation matrix.
     *
     * @param mat The matrix to transform this bounds by
     */
    public abstract void transform(Matrix4d mat);
}
