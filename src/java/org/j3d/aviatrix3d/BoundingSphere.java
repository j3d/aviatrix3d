/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
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

// Standard imports
import javax.vecmath.Matrix4d;

// Application specific imports
// None

/**
 * Bounds described as a spherical volume.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class BoundingSphere extends BoundingVolume
{
    /** The center of the sphere */
    private float[] center;

    /** The radius expressed as a squared value for faster calcs */
    private float radiusSquared;

    /** Standard radius value */
    private float radius;

    /**
     * The default constructor with the sphere radius as one and
     * center at the origin.
     */
    public BoundingSphere()
    {
        center = new float[3];
        radiusSquared = 1;
        radius = 1;
    }

    /**
     * Construct a bounding sphere at the origin with a set radius.
     *
     * @param radius The new radius value to use
     * @throws IllegalArgumentException Radius was negative
     */
    public BoundingSphere(float radius)
    {
        if(radius < 0)
            throw new IllegalArgumentException("Negative radius value");

        center = new float[3];

        radiusSquared = radius * radius;
        this.radius = radius;
    }

    /**
     * Construct a bounding sphere with a set radius and position.
     *
     * @param pos The new position of the center of the sphere to be used
     * @param radius The new radius value to use
     * @throws IllegalArgumentException Radius was negative
     */
    public BoundingSphere(float[] pos, float radius)
    {
        if(radius < 0)
            throw new IllegalArgumentException("Negative radius value");

        center = new float[] { pos[0], pos[1], pos[2] };

        radiusSquared = radius * radius;
        this.radius = radius;
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
        return SPHERE_BOUNDS;
    }

    /**
     * Get the maximum extents of the bounding volume.
     *
     * @param min The minimum position of the bounds
     * @param max The maximum position of the bounds
     */
    public void getExtents(float[] min, float[] max)
    {
        min[0] = center[0] - radius;
        min[1] = center[1] - radius;
        min[2] = center[2] - radius;

        max[0] = center[0] + radius;
        max[1] = center[1] + radius;
        max[2] = center[2] + radius;
    }

    /**
     * Get the center of the bounding volume.
     *
     * @param center The center of the bounds will be copied here
     */
    public void getCenter(float[] center)
    {
        center[0] = this.center[0];
        center[1] = this.center[1];
        center[2] = this.center[2];
    }

    /**
     * Check for the given point lieing inside this bounds.
     *
     * @param pos The location of the point to test against
     * @return true if the point lies inside this bounds
     */
    public boolean checkIntersectionPoint(float[] pos)
    {
        float x = pos[0] - center[0];
        float y = pos[1] - center[1];
        float z = pos[2] - center[2];
        float d = x * x + y * y + z * z;

        return d <= radiusSquared;
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
        return raySphere(pos, dir);
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
     * Transform the current postion by the given transformation matrix.
     *
     * @param mat The matrix to transform this bounds by
     */
    public void transform(Matrix4d mat)
    {
    }

    //---------------------------------------------------------------
    // Misc local methods
    //---------------------------------------------------------------

    /**
     * Center the local center of the sphere to be used.
     *
     * @param pos The new position of the center of the sphere to be used
     */
    public void setCenter(float[] pos)
    {
        center[0] = pos[0];
        center[1] = pos[1];
        center[2] = pos[2];
    }

    /**
     * Set the radius of the sphere to the new value. When the
     * radius is zero, it is considered to be a point space.
     *
     * @param radius The new radius value to use
     * @throws IllegalArgumentException Radius was negative
     */
    public void setRadius(float radius)
    {
        if(radius < 0)
            throw new IllegalArgumentException("Negative radius value");

        radiusSquared = radius * radius;
        this.radius = radius;
    }

    /**
     * Get the current radius of the sphere
     *
     * @return A non-negative value
     */
    public float getRadius()
    {
        return radius;
    }

    /**
     * Internal method to set the square of the radius and normal radius
     * in a single call. Used by the boundsUtils which already have these
     * numbers precalculated.
     *
     * @param radius The normal radius value
     * @param r2 The radius squared value
     */
    void setRadius(float radius, float r2)
    {
        radiusSquared = r2;
        this.radius = radius;
    }

    /**
     * Get the current radius of the sphere
     *
     * @return A non-negative value
     */
    float getRadiusSquared()
    {
        return radiusSquared;
    }


    /**
     * Internal computation of the intersection point of the ray and a sphere.
     * Uses raw data types.
     *
     * @param origin The coordinates of the origin of the ray
     * @param direction The direction vector of the ray
     * @return true if there was an intersection, false if not
     */
    private boolean raySphere(float[] origin, float[] direction)
    {
        double Xc = center[0];
        double Yc = center[1];
        double Zc = center[2];

        double Xo = origin[0];
        double Yo = origin[1];
        double Zo = origin[2];
        double Xd = direction[0];
        double Yd = direction[1];
        double Zd = direction[2];

        // compute A, B, C
        double a = Xd * Xd + Yd * Yd + Zd * Zd;
        double b = 2 * (Xd * (Xo - Xc) + Yd * (Yo - Yc) + Zd * (Zo - Zc));
        double c = (Xo - Xc) * (Xo - Xc) + (Yo - Yc) * (Yo - Yc) +
                   (Zo - Zc) * (Zo - Zc) - radiusSquared;

        // compute discriminant
        double disc = b * b - 4 * a * c;

        return (disc >= 0);
    }
}
