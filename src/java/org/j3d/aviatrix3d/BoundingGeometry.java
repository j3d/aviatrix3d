/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004-2005
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
 * Bounds described as an arbitrary piece of scene graph structure that is
 * not rendered to screen.
 * <p>
 *
 * In some cases, the scene graph prefers to use simplified proxy geometry to
 * describe the bounds of an object, rather than using the exact geometry or
 * bounding boxes. This allows the user to provide a custom set of bounds using
 * normal scene graph geometry, without it being rendered.
 * <p>
 *
 * Internally, this class holds no extra data other than the geometry it
 * represents. Requests for the information such as extents, center, etc will
 * ask the held geometry for it's top-level bounds object and compare against
 * that. It does <i>not</i> walk the contained geometry tree for further
 * checks. It is up to the internals of the scene graph API to do that. This
 * is a holder for non-renderable geometry, not a complete internal picker.
 *
 * @author Justin Couch
 * @version $Revision: 1.12 $
 */
public class BoundingGeometry extends BoundingVolume
{
    /** The geometry that this bounds holds */
    private Node boundingGeometry;

    /**
     * The default constructor with no geometry set.
     */
    public BoundingGeometry()
    {
    }

    /**
     * Construct a bounding sphere at the origin with a set radius.
     *
     * @param geom The geometry to use
     */
    public BoundingGeometry(Node geom)
    {
        boundingGeometry = geom;
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
        return GEOMETRY_BOUNDS;
    }

    /**
     * Get the maximum extents of the bounding volume. If there is no geometry
     * currently set, all values are set to Float.NaN.
     *
     * @param min The minimum position of the bounds
     * @param max The maximum position of the bounds
     */
    public void getExtents(float[] min, float[] max)
    {
        if(boundingGeometry == null)
        {
            min[0] = Float.NaN;
            min[1] = Float.NaN;
            min[2] = Float.NaN;

            max[0] = Float.NaN;
            max[1] = Float.NaN;
            max[2] = Float.NaN;
        }
        else
        {
            BoundingVolume b = boundingGeometry.getBounds();
            b.getExtents(min, max);
        }
    }

    /**
     * Get the center of the bounding volume.
     *
     * @param center The center of the bounds will be copied here
     */
    public void getCenter(float[] center)
    {
        if(boundingGeometry == null)
        {
            center[0] = Float.NaN;
            center[1] = Float.NaN;
            center[2] = Float.NaN;
        }
        else
        {
            BoundingVolume b = boundingGeometry.getBounds();
            b.getCenter(center);
        }
    }

    /**
     * Check for the given point lieing inside this bounds.
     *
     * @param pos The location of the point to test against
     * @return true if the point lies inside this bounds
     */
    public boolean checkIntersectionPoint(float[] pos)
    {
        boolean ret_val = false;

        if(boundingGeometry != null)
        {
            BoundingVolume b = boundingGeometry.getBounds();
            ret_val = b.checkIntersectionPoint(pos);
        }

        return ret_val;
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
        boolean ret_val = false;

        if(boundingGeometry != null)
        {
            BoundingVolume b = boundingGeometry.getBounds();
            ret_val = b.checkIntersectionRay(pos, dir);
        }

        return ret_val;
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
        boolean ret_val = false;

        if(boundingGeometry != null)
        {
            BoundingVolume b = boundingGeometry.getBounds();
            ret_val = b.checkIntersectionSegment(start, end);
        }

        return ret_val;
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
        boolean ret_val = false;

        if(boundingGeometry != null)
        {
            BoundingVolume b = boundingGeometry.getBounds();
            ret_val = b.checkIntersectionSphere(center, radius);
        }

        return ret_val;
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
        boolean ret_val = false;

        if(boundingGeometry != null)
        {
            BoundingVolume b = boundingGeometry.getBounds();
            ret_val = b.checkIntersectionTriangle(v0, v1, v2);
        }

        return ret_val;
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
        boolean ret_val = false;

        if(boundingGeometry != null)
        {
            BoundingVolume b = boundingGeometry.getBounds();
            ret_val = b.checkIntersectionCylinder(center,
                                                  direction,
                                                  radius,
                                                  height);
        }

        return ret_val;
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
        boolean ret_val = false;

        if(boundingGeometry != null)
        {
            BoundingVolume b = boundingGeometry.getBounds();
            ret_val = b.checkIntersectionCone(vertex, direction, angle);
        }

        return ret_val;
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
        boolean ret_val = false;

        if(boundingGeometry != null)
        {
            BoundingVolume b = boundingGeometry.getBounds();
            ret_val = b.checkIntersectionBox(minExtents, maxExtents);
        }

        return ret_val;
    }

    /**
     * Check whether this volume intersects with the view frustum.
     *
     * @param planes The 6 planes of the frustum
     * @param mat The vworld to local transformation matrix
     * @return int FRUSTUM_ALLOUT, FRUSTUM_ALLIN, FRUSTUM_PARTIAL.
     */
    public int checkIntersectionFrustum(Vector4f[] planes, Matrix4d mat) {
        int ret_val = FRUSTUM_ALLOUT;

        if(boundingGeometry != null)
        {
            BoundingVolume b = boundingGeometry.getBounds();
            ret_val = b.checkIntersectionFrustum(planes, mat);
        }

        return ret_val;
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
        int ret_val = FRUSTUM_ALLOUT;

        if(boundingGeometry != null)
        {
            BoundingVolume b = boundingGeometry.getBounds();
            ret_val = b.checkIntersectionFrustum(planes, mat);
        }

        return ret_val;
    }

    /**
     * Transform the current postion by the given transformation matrix.
     *
     * @param mat The matrix to transform this bounds by
     */
    public void transform(Matrix4d mat)
    {
        if(boundingGeometry != null)
        {
            BoundingVolume b = boundingGeometry.getBounds();
            b.transform(mat);
        }
    }

    /**
     * Transform the current postion by the given transformation matrix.
     *
     * @param mat The matrix to transform this bounds by
     */
    public void transform(Matrix4f mat)
    {
        if(boundingGeometry != null)
        {
            BoundingVolume b = boundingGeometry.getBounds();
            b.transform(mat);
        }
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Set the new geometry to use as the proxy. If a null value is passed, it
     * will clear the currently set value.
     *
     * @param geom The new geometry instance to use or null
     */
    public void setProxyGeometry(Node geom)
    {
        boundingGeometry = geom;
    }

    /**
     * Get the currently used proxy geometry.
     *
     * @return The current geometry or null if none set
     */
    public Node getProxyGeometry()
    {
        return boundingGeometry;
    }
}
