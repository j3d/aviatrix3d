/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.renderer.aviatrix3d.util;

// External imports
import org.j3d.maths.vector.*;

import org.j3d.geom.IntersectionUtils;
import org.j3d.geom.GeometryData;
import org.j3d.util.MatrixUtils;

// Local imports
import org.j3d.aviatrix3d.*;

/**
 * An extension of the basic {@link org.j3d.geom.IntersectionUtils} class to
 * include Aviatrix3D-specific extensions for interacting directly with
 * {@link org.j3d.aviatrix3d.VertexGeometry} instances.
 * <p>
 *
 * @see org.j3d.geom.IntersectionUtils
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public class AVIntersectionUtils extends IntersectionUtils
{
    /** Place to invert the incoming transform for reverse mappings */
    private Matrix4d reverseTx;

    /** Temporary points for float <-> double conversion */
    private Point3d tmpOrigin;
    private Point3d tmpPoint;
    private Vector3d tmpDirection;

    /** Temporary arrays for working directly with the geometry */
    private float[] p1;
    private float[] p2;
    private float[] dataOut;

    /** Matrix utility code for doing inversions */
    private MatrixUtils matrixUtils;

    /**
     * Create a default instance of this class with no internal data
     * structures allocated.
     */
    public AVIntersectionUtils()
    {
        reverseTx = new Matrix4d();
        tmpOrigin = new Point3d();
        tmpPoint = new Point3d();
        tmpDirection = new Vector3d();

        p1 = new float[3];
        p2 = new float[3];
        dataOut = new float[12];

        matrixUtils = new MatrixUtils();
    }

    /**
     * Convenience method to pass in an item of geometry and ask the
     * intersection code to find out what the real geometry type is and
     * process it appropriately. If there is an intersection, the point will
     * contain the exact intersection point on the geometry.
     * <P>
     *
     * If the userData object for this geometry is an instance of
     * {@link GeometryData} we will use that in preferences to the actual
     * geometry.
     *
     * @param origin The origin of the ray
     * @param direction The direction of the ray
     * @param length An optional length for to make the ray a segment. If
     *   the value is zero, it is ignored
     * @param geom The geometry to test against
     * @param point The intersection point for returning
     * @param intersectOnly true if we only want to know if we have a
     *    intersection and don't really care which it is
     * @return true if there was an intersection, false if not
     */
    public boolean rayUnknownGeometry(Point3d origin,
                                      Vector3d direction,
                                      float length,
                                      VertexGeometry geom,
                                      Point3d point,
                                      boolean intersectOnly)
    {
        boolean ret_val = false;

        p1[0] = (float)origin.x;
        p1[1] = (float)origin.y;
        p1[2] = (float)origin.z;

        if(length > 0)
        {
System.out.println("AV3DIntersectionUtils: Need to fix line segment intersections!");
            p2[0] = (float)direction.x;
            p2[1] = (float)direction.y;
            p2[2] = (float)direction.z;

            ret_val = geom.pickLineSegment(p1, p2, intersectOnly, dataOut, 0);
        }
        else
        {
            p2[0] = (float)direction.x;
            p2[1] = (float)direction.y;
            p2[2] = (float)direction.z;

            ret_val = geom.pickLineRay(p1, p2, intersectOnly, dataOut, 0);
        }

        point.x = dataOut[0];
        point.y = dataOut[1];
        point.z = dataOut[2];

        return ret_val;
    }

    /**
     * Convenience method to process a {@link GeometryData} and ask the
     * intersection code to find out what the real geometry type is and
     * process it appropriately. If there is an intersection, the point will
     * contain the exact intersection point on the geometry.
     * <P>
     *
     * This code will be much more efficient than the other version because
     * we do not need to reallocate internal arrays all the time or have the
     * need to set capability bits, hurting performance optimisations. If the
     * geometry array does not understand the provided geometry type, it will
     * silently ignore the request and always return false.
     *
     * @param origin The origin of the ray
     * @param direction The direction of the ray
     * @param length An optional length for to make the ray a segment. If
     *   the value is zero, it is ignored
     * @param data The geometry to test against
     * @param point The intersection point for returning
     * @param vworldTransform Transformation matrix to go from the root of the
     *    world to this point
     * @param intersectOnly true if we only want to know if we have a
     *    intersection and don't really care which it is
     * @return true if there was an intersection, false if not
     */
    public boolean rayUnknownGeometry(Point3d origin,
                                      Vector3d direction,
                                      float length,
                                      GeometryData data,
                                      Matrix4d vworldTransform,
                                      Point3d point,
                                      boolean intersectOnly)
    {
        boolean ret_val = false;

        matrixUtils.inverse(vworldTransform, reverseTx);
        transform(origin, reverseTx, pickStart);
        transformNormal(direction, reverseTx, pickDir);

        switch(data.geometryType)
        {
            case GeometryData.TRIANGLES:
                ret_val = rayTriangleArray(pickStart,
                                           pickDir,
                                           length,
                                           data.coordinates,
                                           data.vertexCount / 3,
                                           point,
                                           intersectOnly);
                break;

            case GeometryData.QUADS:
                ret_val = rayQuadArray(pickStart,
                                       pickDir,
                                       length,
                                       data.coordinates,
                                       data.vertexCount / 4,
                                       point,
                                       intersectOnly);
                break;

            case GeometryData.TRIANGLE_STRIPS:
                ret_val = rayTriangleStripArray(pickStart,
                                                pickDir,
                                                length,
                                                data.coordinates,
                                                data.stripCounts,
                                                data.numStrips,
                                                point,
                                                intersectOnly);
                break;

            case GeometryData.TRIANGLE_FANS:
                ret_val = rayTriangleFanArray(pickStart,
                                              pickDir,
                                              length,
                                              data.coordinates,
                                              data.stripCounts,
                                              data.numStrips,
                                              point,
                                              intersectOnly);
                break;

            case GeometryData.INDEXED_QUADS:
                ret_val = rayIndexedQuadArray(pickStart,
                                              pickDir,
                                              length,
                                              data.coordinates,
                                              data.indexes,
                                              data.indexesCount,
                                              point,
                                              intersectOnly);
                break;

            case GeometryData.INDEXED_TRIANGLES:
                ret_val = rayIndexedTriangleArray(pickStart,
                                                  pickDir,
                                                  length,
                                                  data.coordinates,
                                                  data.indexes,
                                                  data.indexesCount,
                                                  point,
                                                  intersectOnly);
                break;
/*
            case GeometryData.INDEXED_TRIANGLE_STRIPS:
                ret_val = rayTriangleArray(pickStart,
                                           pickDir,
                                           length,
                                           data.coordinates,
                                           data.vertexCount,
                                           point,
                                           intersectOnly);
                break;

            case GeometryData.INDEXED_TRIANGLE_FANS:
                ret_val = rayTriangleArray(pickStart,
                                           pickDir,
                                           length,
                                           data.coordinates,
                                           data.vertexCount,
                                           point,
                                           intersectOnly);
                break;
*/
        }

        if(ret_val)
            transform(point, vworldTransform);

        return ret_val;
    }

    /**
     * Convenience method to pass in an item of geometry and ask the
     * intersection code to find out what the real geometry type is and
     * process it appropriately. If there is an intersection, the point will
     * contain the exact intersection point on the geometry.
     * <P>
     *
     * If the userData object for this geometry is an instance of
     * {@link GeometryData} we will use that in preferences to the actual
     * geometry.
     *
     * @param origin The origin of the ray
     * @param direction The direction of the ray
     * @param length An optional length for to make the ray a segment. If
     *   the value is zero, it is ignored
     * @param geom The geometry to test against
     * @param point The intersection point for returning
     * @param vworldTransform Transformation matrix to go from the root of the
     *    world to this point
     * @param intersectOnly true if we only want to know if we have a
     *    intersection and don't really care which it is
     * @return true if there was an intersection, false if not
     */
    public boolean rayUnknownGeometry(Point3d origin,
                                      Vector3d direction,
                                      float length,
                                      VertexGeometry geom,
                                      Matrix4d vworldTransform,
                                      Point3d point,
                                      boolean intersectOnly)
    {
        Object userdata = geom.getUserData();

        if(userdata instanceof GeometryData)
        {
            return rayUnknownGeometry(origin,
                                      direction,
                                      length,
                                      (GeometryData)userdata,
                                      vworldTransform,
                                      point,
                                      intersectOnly);
        }
        else if(geom instanceof TriangleArray)
        {
            return rayTriangleArray(origin,
                                    direction,
                                    length,
                                    (TriangleArray)geom,
                                    vworldTransform,
                                    point,
                                    intersectOnly);
        }
        else if(geom instanceof QuadArray)
        {
            return rayQuadArray(origin,
                                direction,
                                length,
                                (QuadArray)geom,
                                vworldTransform,
                                point,
                                intersectOnly);
        }
        else if(geom instanceof TriangleStripArray)
        {
            return rayTriangleStripArray(origin,
                                         direction,
                                         length,
                                         (TriangleStripArray)geom,
                                         vworldTransform,
                                         point,
                                         intersectOnly);
        }
        else if(geom instanceof TriangleFanArray)
        {
            return rayTriangleFanArray(origin,
                                       direction,
                                       length,
                                       (TriangleFanArray)geom,
                                       vworldTransform,
                                       point,
                                       intersectOnly);
        }
/*
        else if(geom instanceof IndexedTriangleArray)
        {
            return rayIndexedTriangleArray(origin,
                                           direction,
                                           length,
                                           (IndexedTriangleArray)geom,
                                           vworldTransform,
                                           point,
                                           intersectOnly);
        }
        else if(geom instanceof IndexedQuadArray)
        {
            return rayIndexedQuadArray(origin,
                                       direction,
                                       length,
                                       (IndexedQuadArray)geom,
                                       vworldTransform,
                                       point,
                                       intersectOnly);
        }
*/
        return false;
    }

    /**
     * Test the intersection of a ray or segment against the given triangle
     * array.If there is an intersection, the point will contain the exact
     * intersection point on the geometry.
     *
     * @param origin The origin of the ray
     * @param direction The direction of the ray
     * @param length An optional length for to make the ray a segment. If
     *   the value is zero, it is ignored
     * @param geom The geometry to test against
     * @param point The intersection point for returning
     * @param vworldTransform Transformation matrix to go from the root of the
     *    world to this point
     * @param intersectOnly true if we only want to know if we have a
     *    intersection and don't really care which it is
     * @return true if there was an intersection, false if not
     */
    public boolean rayTriangleArray(Point3d origin,
                                    Vector3d direction,
                                    float length,
                                    TriangleArray geom,
                                    Matrix4d vworldTransform,
                                    Point3d point,
                                    boolean intersectOnly)
    {
        int vtx_count = geom.getValidVertexCount();

        if((workingCoords == null) || (workingCoords.length != vtx_count * 3))
            workingCoords = new float[vtx_count * 3];

        // Get non-interleaved coords
        geom.getVertices(workingCoords);

        matrixUtils.inverse(vworldTransform, reverseTx);

        transform(origin, reverseTx, pickStart);
        transformNormal(direction, reverseTx, pickDir);
        boolean intersection;

        intersection = rayTriangleArray(pickStart,
                                        pickDir,
                                        length,
                                        workingCoords,
                                        vtx_count / 3,
                                        point,
                                        intersectOnly);

        if(intersection)
            transform(point, vworldTransform);

        return intersection;
    }

    /**
     * Test the intersection of a ray or segment against the given quad
     * array.If there is an intersection, the point will contain the exact
     * intersection point on the geometry.
     *
     * @param origin The origin of the ray
     * @param direction The direction of the ray
     * @param length An optional length for to make the ray a segment. If
     *   the value is zero, it is ignored
     * @param geom The geometry to test against
     * @param point The intersection point for returning
     * @param vworldTransform Transformation matrix to go from the root of the
     *    world to this point
     * @param intersectOnly true if we only want to know if we have a
     *    intersection and don't really care which it is
     * @return true if there was an intersection, false if not
     */
    public boolean rayQuadArray(Point3d origin,
                                Vector3d direction,
                                float length,
                                QuadArray geom,
                                Matrix4d vworldTransform,
                                Point3d point,
                                boolean intersectOnly)
    {
        int vtx_count = geom.getValidVertexCount();

        if((workingCoords == null) || (workingCoords.length != vtx_count * 3))
            workingCoords = new float[vtx_count * 4];

        geom.getVertices(workingCoords);

        matrixUtils.inverse(vworldTransform, reverseTx);

        transform(origin, reverseTx, pickStart);
        transformNormal(direction, reverseTx, pickDir);
        boolean intersection;

        intersection = rayTriangleArray(pickStart,
                                        pickDir,
                                        length,
                                        workingCoords,
                                        vtx_count / 4,
                                        point,
                                        intersectOnly);

        if(intersection)
            transform(point, vworldTransform);

        return intersection;
    }

    /**
     * Test the intersection of a ray or segment against the given triangle
     * strip array.If there is an intersection, the point will contain the
     * exact intersection point on the geometry.
     *
     * @param origin The origin of the ray
     * @param direction The direction of the ray
     * @param length An optional length for to make the ray a segment. If
     *   the value is zero, it is ignored
     * @param geom The geometry to test against
     * @param point The intersection point for returning
     * @param vworldTransform Transformation matrix to go from the root of the
     *    world to this point
     * @param intersectOnly true if we only want to know if we have a
     *    intersection and don't really care which it is
     * @return true if there was an intersection, false if not
     */
    public boolean rayTriangleStripArray(Point3d origin,
                                         Vector3d direction,
                                         float length,
                                         TriangleStripArray geom,
                                         Matrix4d vworldTransform,
                                         Point3d point,
                                         boolean intersectOnly)
    {
        int vtx_count = geom.getValidVertexCount();
        int strip_count = geom.getValidStripCount();

        if((workingCoords == null) || (workingCoords.length != vtx_count * 3))
            workingCoords = new float[vtx_count * 3];

        if((workingStrips == null) || (workingStrips.length != strip_count))
            workingStrips = new int[strip_count];

        geom.getVertices(workingCoords);
        geom.getStripCount(workingStrips);

        matrixUtils.inverse(vworldTransform, reverseTx);

        transform(origin, reverseTx, pickStart);
        transformNormal(direction, reverseTx, pickDir);
        boolean intersection;

        intersection = rayTriangleStripArray(pickStart,
                                             pickDir,
                                             length,
                                             workingCoords,
                                             workingStrips,
                                             strip_count,
                                             point,
                                             intersectOnly);

        if(intersection && !intersectOnly)
            transform(point, vworldTransform);

        return intersection;
    }

    /**
     * Test the intersection of a ray or segment against the given triangle
     * fan array.If there is an intersection, the point will contain the
     * exact intersection point on the geometry.
     *
     * @param origin The origin of the ray
     * @param direction The direction of the ray
     * @param length An optional length for to make the ray a segment. If
     *   the value is zero, it is ignored
     * @param geom The geometry to test against
     * @param point The intersection point for returning
     * @param vworldTransform Transformation matrix to go from the root of the
     *    world to this point
     * @param intersectOnly true if we only want to know if we have a
     *    intersection and don't really care which it is
     * @return true if there was an intersection, false if not
     */
    public boolean rayTriangleFanArray(Point3d origin,
                                       Vector3d direction,
                                       float length,
                                       TriangleFanArray geom,
                                       Matrix4d vworldTransform,
                                       Point3d point,
                                       boolean intersectOnly)
    {
        int vtx_count = geom.getValidVertexCount();
        int strip_count = geom.getValidFanCount();

        if((workingCoords == null) || (workingCoords.length != vtx_count * 3))
            workingCoords = new float[vtx_count * 3];

        if((workingStrips == null) || (workingStrips.length != strip_count))
            workingStrips = new int[strip_count];

        geom.getVertices(workingCoords);
        geom.getFanCount(workingStrips);

        matrixUtils.inverse(vworldTransform, reverseTx);

        transform(origin, reverseTx, pickStart);
        transformNormal(direction, reverseTx, pickDir);
        boolean intersection;

        intersection = rayTriangleFanArray(pickStart,
                                           pickDir,
                                           length,
                                           workingCoords,
                                           workingStrips,
                                           strip_count,
                                           point,
                                           intersectOnly);

        if(intersection && !intersectOnly)
            transform(point, vworldTransform);

        return intersection;

    }

    /**
     * Test the intersection of a ray or segment against the given indexed
     * triangle array.If there is an intersection, the point will contain the
     * exact intersection point on the geometry.
     *
     * @param origin The origin of the ray
     * @param direction The direction of the ray
     * @param length An optional length for to make the ray a segment. If
     *   the value is zero, it is ignored
     * @param geom The geometry to test against
     * @param point The intersection point for returning
     * @param vworldTransform Transformation matrix to go from the root of the
     *    world to this point
     * @param intersectOnly true if we only want to know if we have a
     *    intersection and don't really care which it is
     * @return true if there was an intersection, false if not
     */
/*
    public boolean rayIndexedTriangleArray(Point3d origin,
                                           Vector3d direction,
                                           float length,
                                           IndexedTriangleArray geom,
                                           Matrix4d vworldTransform,
                                           Point3d point,
                                           boolean intersectOnly)
    {
        int vtx_count = geom.getValidVertexCount();
        int index_count = geom.getIndexCount();

        if((workingCoords == null) || (workingCoords.length != vtx_count * 3))
            workingCoords = new float[vtx_count * 3];

        if((workingIndicies == null) || (workingIndicies.length != index_count))
            workingIndicies = new int[index_count];

        geom.getVertices(workingCoords);
        geom.getCoordinateIndices(0, workingIndicies);

        matrixUtils.inverse(vworldTransform, reverseTx);

        transform(origin, reverseTx, pickStart);
        transformNormal(direction, reverseTx, pickDir);
        boolean intersection;

        intersection = rayIndexedTriangleArray(pickStart,
                                               pickDir,
                                               length,
                                               workingCoords,
                                               workingIndicies,
                                               index_count,
                                               point,
                                               intersectOnly);

        if(intersection && !intersectOnly)
            vworldTransform.transform(point);

        return intersection;
    }
*/
    /**
     * Test the intersection of a ray or segment against the given indexed
     * quad array.If there is an intersection, the point will contain the
     * exact intersection point on the geometry.
     *
     * @param origin The origin of the ray
     * @param direction The direction of the ray
     * @param length An optional length for to make the ray a segment. If
     *   the value is zero, it is ignored
     * @param geom The geometry to test against
     * @param point The intersection point for returning
     * @param vworldTransform Transformation matrix to go from the root of the
     *    world to this point
     * @param intersectOnly true if we only want to know if we have a
     *    intersection and don't really care which it is
     * @return true if there was an intersection, false if not
     */
/*
    public boolean rayIndexedQuadArray(Point3d origin,
                                       Vector3d direction,
                                       float length,
                                       IndexedQuadArray geom,
                                       Matrix4d vworldTransform,
                                       Point3d point,
                                       boolean intersectOnly)
    {
        int vtx_count = geom.getValidVertexCount();
        int index_count = geom.getIndexCount();

        if((workingCoords == null) || (workingCoords.length != vtx_count * 3))
            workingCoords = new float[vtx_count * 3];

        if((workingIndicies == null) || (workingIndicies.length != index_count))
            workingIndicies = new int[index_count];

        geom.getVertices(workingCoords);
        geom.getCoordinateIndices(0, workingIndicies);

        matrixUtils.inverse(vworldTransform, reverseTx);

        transform(origin, reverseTx, pickStart);
        transformNormal(direction, reverseTx, pickDir);
        boolean intersection;

        intersection = rayIndexedQuadArray(pickStart,
                                           pickDir,
                                           length,
                                           workingCoords,
                                           workingIndicies,
                                           index_count,
                                           point,
                                           intersectOnly);

        if(intersection && !intersectOnly)
            vworldTransform.transform(point);

        return intersection;
    }
*/

    //----------------------------------------------------------
    // Lower level methods for individual polygons
    //----------------------------------------------------------

    /**
     * Transform the provided vector by the matrix and place it back in
     * the vector.
     *
     * @param vec The vector to be transformed
     * @param mat The matrix to do the transforming with
     */
    private void transform(Point3d vec, Matrix4d mat)
    {
        double a = vec.x;
        double b = vec.y;
        double c = vec.z;

        vec.x = mat.m00 * a + mat.m01 * b + mat.m02 * c + mat.m03;
        vec.y = mat.m10 * a + mat.m11 * b + mat.m12 * c + mat.m13;
        vec.z = mat.m20 * a + mat.m21 * b + mat.m22 * c + mat.m23;
    }

    /**
     * Transform the provided vector by the matrix and place it back in
     * the vector.
     *
     * @param vec The vector to be transformed
     * @param mat The matrix to do the transforming with
     * @param out The vector to be put the result in
     */
    private void transform(Point3d vec, Matrix4d mat, Point3d out)
    {
        double a = vec.x;
        double b = vec.y;
        double c = vec.z;

        out.x = mat.m00 * a + mat.m01 * b + mat.m02 * c + mat.m03;
        out.y = mat.m10 * a + mat.m11 * b + mat.m12 * c + mat.m13;
        out.z = mat.m20 * a + mat.m21 * b + mat.m22 * c + mat.m23;
    }

    /**
     * Transform the provided vector by the matrix and place it back in
     * the vector. The fourth element is assumed to be zero for normal
     * transformations.
     *
     * @param vec The vector to be transformed
     * @param mat The matrix to do the transforming with
     * @param out The vector to be put the result in
     */
    private void transformNormal(Vector3d vec, Matrix4d mat, Vector3d out)
    {
        double a = vec.x;
        double b = vec.y;
        double c = vec.z;

        out.x = mat.m00 * a + mat.m01 * b + mat.m02 * c;
        out.y = mat.m10 * a + mat.m11 * b + mat.m12 * c;
        out.z = mat.m20 * a + mat.m21 * b + mat.m22 * c;
    }
}
