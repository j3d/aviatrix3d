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
 * Base representation of a class that can representing bound information.
 * <p>
 *
 * Bounds describe a 3D volume, and various abstract intersection methods are
 * defined. All methods must be implemented as various methods may be called
 * during different parts of the rendering cycle. All bounds and rays are
 * assumed to be represented in the same local coordinate space.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public abstract class BoundingVolume
{
    /**
     * The default constructor.
     */
    protected BoundingVolume()
    {
    }

    /**
     * Check for the given point lieing inside this bounds.
     *
     * @param pos The location of the point to test against
     * @return true if the point lies inside this bounds
     */
    public abstract boolean checkIntersectionPoint(float[] pos);

    /**
     * Check for the given line intersecting this bounds. The line is
     * described in normal vector form of <code>ax + by + cz + d = 0</code>.
     *
     * @param coeff The 4 constants for the line equation
     * @return true if the line intersects this bounds
     */
    public abstract boolean checkIntersectionLine(float[] coeff);

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
     * Check for the given line segment intersecting this bounds. The line is
     * described as the line going from the start point in a given direction
     * with a length in that direction.
     *
     * @param start The start location of the segment
     * @param dir The direction vector of the segment
     * @param length The length to the segment
     * @return true if the segment intersects this bounds
     */
    public abstract boolean checkIntersectionSegment(float[] start,
                                                     float[] dir,
                                                     float length);

    /**
     * Transform the current postion by the given transformation matrix.
     *
     * @param mat The matrix to transform this bounds by
     */
    public abstract void transform(Matrix4d mat);
}
