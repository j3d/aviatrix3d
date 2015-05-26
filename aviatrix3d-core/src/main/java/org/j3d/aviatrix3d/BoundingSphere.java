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
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector4d;
import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.BoundingVolume;

/**
 * Bounds described as a spherical volume.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>negRadiusMsg: Error message when the radius is negative.</li>
 * </ul>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.17 $
 */
public class BoundingSphere extends BoundingVolume
{
	/** Attribute array is not long enough for the coords */
	private static final String NEG_RADIUS_PROP =
		"org.j3d.aviatrix3d.BoundingSphere.negRadiusMsg";

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
		this(1);
    }

    /**
     * Construct a bounding sphere at the origin with a set radius.
     *
     * @param radius The new radius value to use
     * @throws IllegalArgumentException Radius was negative
     */
    public BoundingSphere(float radius)
    {
        checkRadius(radius);

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
        checkRadius(radius);

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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public boolean checkIntersectionPoint(float[] pos)
    {
        float x = pos[0] - center[0];
        float y = pos[1] - center[1];
        float z = pos[2] - center[2];
        float d = x * x + y * y + z * z;

        return d <= radiusSquared;
    }

    /**
     * Check for the given ray intersecting this bounds. The line is
     * described as a starting point and a vector direction.
     *
     * @param pos The start location of the ray
     * @param dir The direction vector of the ray
     * @return true if the ray intersects this bounds
     */
    @Override
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
    @Override
    public boolean checkIntersectionSegment(float[] start, float[] end)

    {
        return false;
    }

    /**
     * Check for the given sphere intersecting this bounds. The sphere is
     * described by a centre location and the radius.
     *
     * @param c The location of the sphere's center
     * @param r The radius of the sphere
     * @return true if the sphere intersects this bounds
     */
    @Override
    public boolean checkIntersectionSphere(float[] c, float r)
    {
        float x = c[0] - center[0];
        float y = c[1] - center[1];
        float z = c[2] - center[2];

        float distance = (float)Math.sqrt(x * x + y * y + z * z);

        return distance <= (radius + r);
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public boolean checkIntersectionBox(float[] minExtents, float[] maxExtents)
    {
        float d_min = 0;

        if(center[0] < minExtents[0])
        {
            float d = center[0] - minExtents[0];
            d_min += d * d;
        }
        else if(center[0] > maxExtents[0])
        {
            float d = center[0] - maxExtents[0];
            d_min += d * d;
        }

        if(center[1] < minExtents[1])
        {
            float d = center[1] - minExtents[1];
            d_min += d * d;
        }
        else if(center[1] > maxExtents[1])
        {
            float d = center[1] - maxExtents[1];
            d_min += d * d;
        }

        if(center[2] < minExtents[2])
        {
            float d = center[2] - minExtents[2];
            d_min += d * d;
        }
        else if(center[2] > maxExtents[2])
        {
            float d = center[2] - maxExtents[2];
            d_min += d * d;
        }

        return d_min <= radiusSquared;
    }

    /**
     * Check whether this volume intersects with the view frustum.
     *
     * @param planes The 6 planes of the frustum
     * @param mat The vworld to local transformation matrix
     * @return int FRUSTUM_ALLOUT, FRUSTUM_ALLIN, FRUSTUM_PARTIAL.
     */
    @Override
    public int checkIntersectionFrustum(Vector4d[] planes, Matrix4d mat)
    {
        return FRUSTUM_PARTIAL;
    }

    /**
     * Transform the current postion by the given transformation matrix.
     *
     * @param mat The matrix to transform this bounds by
     */
    @Override
    public void transform(Matrix4d mat)
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
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder("Bounding Sphere: center: ( ");
        buf.append(center[0]);
        buf.append(' ');
        buf.append(center[1]);
        buf.append(' ');
        buf.append(center[2]);
        buf.append(") r: ");
        buf.append(radius);

        return buf.toString();
    }

    //---------------------------------------------------------------
    // Local methods
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
        checkRadius(radius);

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

	/**
	 * Convenience method to check for negative radius. If the radius is OK
	 * it returns normally.
	 *
	 * @param radius The radius value to check against
	 * @throws IllegalArgumentException The radius is negative or zero
	 */
	private void checkRadius(float radius)
		throws IllegalArgumentException
	{
		if (radius < 0) 
		{
			I18nManager intl_mgr = I18nManager.getManager();
			String msg_pattern = intl_mgr.getString(NEG_RADIUS_PROP);
			
			Locale lcl = intl_mgr.getFoundLocale();
			
			NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);
			
			Object[] msg_args = { radius };
			
			Format[] fmts = { n_fmt };
			MessageFormat msg_fmt =
				new MessageFormat(msg_pattern, lcl);
			msg_fmt.setFormats(fmts);
			String msg = msg_fmt.format(msg_args);
			
			throw new IllegalArgumentException(msg);
		}
	}
}
