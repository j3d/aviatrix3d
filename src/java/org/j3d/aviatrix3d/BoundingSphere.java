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
 * @version $Revision: 1.1 $
 */
public class BoundingSphere extends BoundingVolume
{
    /** The center of the sphere */
    private float[] center;

    /** The radius expressed as a squared value for faster calcs */
    private float radiusSquared;

    /**
     * The default constructor with the sphere radius as one and
     * center at the origin.
     */
    public BoundingSphere()
    {
        center = new float[3];
        radiusSquared = 1;
    }

    /**
     * Constructor a bounding sphere with a set radius and position.
     *
     * @param pos The new position of the center of the sphere to be used
     * @param radius The new radius value to use
     * @throws IllegalArgumentException Radius was negative
     */
    public BoundingSphere(float[] pos, float radius)
    {
        if(radius < 0)
            throw new IllegalArgumentException("Negative radius value");

        center = new float[3];
        center[0] = pos[0];
        center[1] = pos[1];
        center[2] = pos[2];

        radiusSquared = radius * radius;
    }

    //---------------------------------------------------------------
    // Methods defined by BoundingVolume
    //---------------------------------------------------------------

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
     * Get the current center the of the sphere.
     *
     * @param pos The position to copy the values into
     */
    public void getCenter(float[] pos)
    {
        pos[0] = center[0];
        pos[1] = center[1];
        pos[2] = center[2];
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
    }

    /**
     * Get the current radius of the sphere
     *
     * @return A non-negative value
     */
    public float getRadius()
    {
        return (float)Math.sqrt(radiusSquared);
    }

}
