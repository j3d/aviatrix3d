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
 * Bounds described as an axis-aligned bounding volume.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class BoundingBox extends BoundingVolume
{
    /** The minimum coordinates of the box */
    private float[] min;

    /** The maximum coordinates of the box */
    private float[] max;

    /**
     * The default constructor with the sphere radius as one and
     * center at the origin.
     */
    public BoundingBox()
    {
        min = new float[3];
        max = new float[3];
    }

    /**
     * Construct a bounding box with minimum and maximum positions.
     *
     * @param min The minimum position of the bounds
     * @param max The maximum position of the bounds
     */
    public BoundingBox(float[] min, float[] max)
    {
        this();

        this.min[0] = min[0];
        this.min[1] = min[1];
        this.min[2] = min[2];

        this.max[0] = max[0];
        this.max[1] = max[1];
        this.max[2] = max[2];
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
     * Set both of the bounds of the box.
     *
     * @param min The minimum position of the bounds
     * @param max The maximum position of the bounds
     */
    public void setBounds(float[] min, float[] max)
    {
        this.min[0] = min[0];
        this.min[1] = min[1];
        this.min[2] = min[2];

        this.max[0] = max[0];
        this.max[1] = max[1];
        this.max[2] = max[2];
    }

    /**
     * Set the minimum bounds for the box.
     *
     * @param pos The new position of the box to be used
     */
    public void setMinimum(float[] pos)
    {
        min[0] = pos[0];
        min[1] = pos[1];
        min[2] = pos[2];
    }

    /**
     * Get the minimum bounds position of the box.
     *
     * @param pos The position to copy the values into
     */
    public void getMinimum(float[] pos)
    {
        pos[0] = min[0];
        pos[1] = min[1];
        pos[2] = min[2];
    }

    /**
     * Set the maximum bounds for the box.
     *
     * @param pos The new position of the center of the sphere to be used
     */
    public void setMaximum(float[] pos)
    {
        max[0] = pos[0];
        max[1] = pos[1];
        max[2] = pos[2];
    }

    /**
     * Get the maximum bounds position of the box.
     *
     * @param pos The position to copy the values axto
     */
    public void getMaximum(float[] pos)
    {
        pos[0] = max[0];
        pos[1] = max[1];
        pos[2] = max[2];
    }
}
