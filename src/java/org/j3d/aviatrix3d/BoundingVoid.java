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
import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;

// Local imports
import org.j3d.aviatrix3d.rendering.BoundingVolume;

/**
 * Bounds described as something that does not exist in the scene graph.
 * <p>
 * Used to describe objects that cannot be represented as a point in space,
 * such as fog, background or a shape with no geometry set. Whenever a pick
 * is made against this it always returns null. Any values returned are
 * considered invalid.
 *
 * @author Justin Couch
 * @version $Revision: 1.12 $
 */
public class BoundingVoid extends BoundingVolume
{
    /**
     * The default constructor with the sphere radius as one and
     * center at the origin.
     */
    public BoundingVoid()
    {
    }

    //---------------------------------------------------------------
    // Methods defined by BoundingVolume
    //---------------------------------------------------------------

    /**
     * The type of bounds this object represents.
     *
     * @return One of the constant types defined
     */
    public int getType()
    {
        return NULL_BOUNDS;
    }

    /**
     * Get the maximum extents of the bounding volume - Float.NaN.
     *
     * @param min The minimum position of the bounds
     * @param max The maximum position of the bounds
     */
    public void getExtents(float[] min, float[] max)
    {
        min[0] = Float.NaN;
        min[1] = Float.NaN;
        min[2] = Float.NaN;

        max[0] = Float.NaN;
        max[1] = Float.NaN;
        max[2] = Float.NaN;
    }

    /**
     * Get the center of the bounding volume.
     *
     * @param center The center of the bounds will be copied here
     */
    public void getCenter(float[] center)
    {
        center[0] = 0;
        center[1] = 0;
        center[2] = 0;
    }

    /**
     * Check for the given point lieing inside this bounds.
     *
     * @param pos The location of the point to test against
     * @return true if the point lies inside this bounds
     */
    public boolean checkIntersectionPoint(float[] pos)
    {
        return false;
    }

    /**
     * Check for the given line intersecting this bounds. The line is
     * described in normal vector form of <code>ax + by + cz + d = 0</code>.
     *
     * @param coeff The 4 constants for the line equation
     * @return true if the line intersects this bounds
     */
    public boolean checkIntersectionLine(float[] coeff)
    {
        return false;
    }

    /**
     * Check for the given ray intersecting this bounds. The line is
     * described as a starting point and a vector direction.
     *
     * @param pos The start location of the ray
     * @param dir The direction vector of the ray
     * @return true if the ray intersects this bounds
     */
    public boolean checkIntersectionRay(float[] pos, float[] dir)
    {
        return false;
    }

    /**
     * Check for the given line segment intersecting this bounds. The line is
     * described as the line connecting the start and end points.
     *
     * @param start The start location of the segment
     * @param end The start location of the segment
     * @return true if the segment intersects this bounds
     */
    public boolean checkIntersectionSegment(float[] start,
                                            float[] end)

    {
        return false;
    }

    /**
     * Check for the given line segment intersecting this bounds. The line is
     * described as the line going from the start point in a given direction
     * with a length in that direction.
     *
     * @param start The start location of the segment
     * @param dir The direction vector of the segment
     * @param length The length to the segment
     * @return true if the segment intersects this bounds
     */
    public boolean checkIntersectionSegment(float[] start,
                                            float[] dir,
                                            float length)
    {
        return false;
    }

    /**
     * Check for the given sphere intersecting this bounds. The sphere is
     * described by a centre location and the radius.
     *
     * @param center The location of the sphere's center
     * @param radius The radius of the sphere
     * @return true if the sphere intersects this bounds
     */
    public boolean checkIntersectionSphere(float[] center, float radius)
    {
        return false;
    }

    /**
     * Check for the given triangle intersecting this bounds. Assumes the 
     * three coordinates of the triangle are declared in RH coordinate system.
     *
     * @param v0 The first vertex of the triangle
     * @param v1 The second vertex of the triangle
     * @param v2 The third vertex of the triangle
     * @return true if the sphere intersects this bounds
     */
    public boolean checkIntersectionTriangle(float[] v0, float[] v1, float[] v2)
    {
        return false;
    }

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
    public boolean checkIntersectionCylinder(float[] center,
                                             float[] direction,
                                             float radius,
                                             float height)
    {
        return false;
    }

    /**
     * Check for the given cone intersecting this bounds. The
     * cone is described by the location of the vertex, a direction vector
     * and the spread angle of the cone.
     *
     * @param vertex The location of the cone's vertex
     * @param direction A unit vector indicating the axial direction
     * @param angle The spread angle of the cone
     * @return true if the sphere intersects this bounds
     */
    public boolean checkIntersectionCone(float[] vertex,
                                         float[] direction,
                                         float angle)
    {
        return false;
    }

    /**
     * Check for the given AA box intersecting this bounds. The box is
     * described by the minimum and maximum extents on each axis.
     *
     * @param minExtents The minimum extent value on each axis
     * @param maxExtents The maximum extent value on each axis
     * @return true if the box intersects this bounds
     */
    public boolean checkIntersectionBox(float[] minExtents, float[] maxExtents)
    {
        return false;
    }

    /**
     * Check whether this volume intersects with the view frustum.
     *
     * @param planes The 6 planes of the frustum
     * @param mat The vworld to local transformation matrix
     * @return int FRUSTUM_ALLOUT, FRUSTUM_ALLIN, FRUSTUM_PARTIAL.
     */
    public int checkIntersectionFrustum(Vector4f[] planes, Matrix4d mat)
    {
        return FRUSTUM_ALLOUT;
    }

    /**
     * Check whether this volume intersects with the view frustum.
     *
     * @param planes The 6 planes of the frustum
     * @param mat The vworld to local transformation matrix
     * @return int FRUSTUM_ALLOUT, FRUSTUM_ALLIN, FRUSTUM_PARTIAL.
     */
    public int checkIntersectionFrustum(Vector4f[] planes, Matrix4f mat)
    {
        return FRUSTUM_ALLOUT;
    }

    /**
     * Transform the current postion by the given transformation matrix.
     *
     * @param mat The matrix to transform this bounds by
     */
    public void transform(Matrix4d mat)
    {
    }

    /**
     * Transform the current postion by the given transformation matrix.
     *
     * @param mat The matrix to transform this bounds by
     */
    public void transform(Matrix4f mat)
    {
    }

    //---------------------------------------------------------------
    // Methods defined by Object
    //---------------------------------------------------------------

    /**
     * Generate a string representation of this box.
     *
     * @return A string representing the bounds information
     */
    public String toString()
    {
        return "Bounding VOID";
    }
}
