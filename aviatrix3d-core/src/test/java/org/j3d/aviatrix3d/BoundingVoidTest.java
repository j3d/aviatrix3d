/*
 * **************************************************************************
 *                        Copyright j3d.org (c) 2000 - ${year}
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read docs/lgpl.txt for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * **************************************************************************
 */

package org.j3d.aviatrix3d;

import org.j3d.maths.vector.AxisAngle4d;
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector4d;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

import org.j3d.aviatrix3d.rendering.BoundingVolume;

/**
 * Unit test for the bounding void
 *
 * @author justin
 */
public class BoundingVoidTest
{
    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        BoundingVoid class_under_test = new BoundingVoid();
        assertEquals(class_under_test.getType(), BoundingVolume.NULL_BOUNDS, "Incorrect bound type");

        float[] result = new float[3];
        class_under_test.getCenter(result);

        assertEquals(result[0], 0.0f, "Incorrect X center");
        assertEquals(result[1], 0.0f, "Incorrect Y center");
        assertEquals(result[2], 0.0f, "Incorrect Z center");

        float[] result2 = new float[3];
        class_under_test.getExtents(result, result2);

        assertTrue(Float.isNaN(result[0]), "Min X should be NaN");
        assertTrue(Float.isNaN(result[1]), "Min Y should be NaN");
        assertTrue(Float.isNaN(result[2]), "Min Z should be NaN");

        assertTrue(Float.isNaN(result2[0]), "Max X should be NaN");
        assertTrue(Float.isNaN(result2[1]), "Max Y should be NaN");
        assertTrue(Float.isNaN(result2[2]), "Max Z should be NaN");

        assertNotNull(class_under_test.toString(), "No string representation available");
    }

    @Test(groups = "unit")
    public void testTransform() throws Exception
    {
        AxisAngle4d rotation = new AxisAngle4d();
        rotation.set(0, 1, 0, Math.PI / 2);
        Matrix4d test_matrix = new Matrix4d();
        test_matrix.set(rotation);

        BoundingVoid class_under_test = new BoundingVoid();
        class_under_test.transform(test_matrix);

        float[] result = new float[3];
        float[] result2 = new float[3];
        class_under_test.getExtents(result, result2);

        assertTrue(Float.isNaN(result[0]), "Min X should be NaN");
        assertTrue(Float.isNaN(result[1]), "Min Y should be NaN");
        assertTrue(Float.isNaN(result[2]), "Min Z should be NaN");

        assertTrue(Float.isNaN(result2[0]), "Max X should be NaN");
        assertTrue(Float.isNaN(result2[1]), "Max Y should be NaN");
        assertTrue(Float.isNaN(result2[2]), "Max Z should be NaN");
    }

    @Test(groups = "unit")
    public void testPointIntersection() throws Exception
    {
        final float[] TEST_POINT = { 0, 0, 0 };

        BoundingVoid class_under_test = new BoundingVoid();
        assertFalse(class_under_test.checkIntersectionPoint(TEST_POINT), "Should not find any intersection");
    }

    @Test(groups = "unit")
    public void testRayIntersection() throws Exception
    {
        final float[] TEST_POINT = { 0, 0, 0 };
        final float[] TEST_DIRECTION = { 1, 0, 0 };

        BoundingVoid class_under_test = new BoundingVoid();
        assertFalse(class_under_test.checkIntersectionRay(TEST_POINT, TEST_DIRECTION),
                    "Should not find any intersection");
    }

    @Test(groups = "unit")
    public void testSegmentIntersection() throws Exception
    {
        final float[] TEST_START = { 0, 0, 0 };
        final float[] TEST_END = { 1, 0, 0 };

        BoundingVoid class_under_test = new BoundingVoid();
        assertFalse(class_under_test.checkIntersectionSegment(TEST_START, TEST_END),
                    "Should not find any intersection");
    }

    @Test(groups = "unit")
    public void testSphereIntersection() throws Exception
    {
        final float[] TEST_POINT = { 0, 0, 0 };
        final float TEST_RADIUS = (float)Math.random();

        BoundingVoid class_under_test = new BoundingVoid();
        assertFalse(class_under_test.checkIntersectionSphere(TEST_POINT, TEST_RADIUS),
                    "Should not find any intersection");
    }

    @Test(groups = "unit")
    public void testTriangleIntersection() throws Exception
    {
        final float[] TEST_V0 = { 0, 0, 0 };
        final float[] TEST_V1 = { 1, 0, 0 };
        final float[] TEST_V2 = { 0, 1, 0 };

        BoundingVoid class_under_test = new BoundingVoid();
        assertFalse(class_under_test.checkIntersectionTriangle(TEST_V0, TEST_V1, TEST_V2),
                    "Should not find any intersection");
    }

    @Test(groups = "unit")
    public void testCylinderIntersection() throws Exception
    {
        final float[] TEST_CENTER = { 0, 0, 0 };
        final float[] TEST_DIRECTION = { 1, 0, 0 };
        final float TEST_RADIUS = (float)Math.random();
        final float TEST_HEIGHT = (float)Math.random();

        BoundingVoid class_under_test = new BoundingVoid();
        assertFalse(class_under_test.checkIntersectionCylinder(TEST_CENTER, TEST_DIRECTION, TEST_RADIUS, TEST_HEIGHT),
                    "Should not find any intersection");
    }

    @Test(groups = "unit")
    public void testConeIntersection() throws Exception
    {
        final float[] TEST_VERTEX = { 0, 0, 0 };
        final float[] TEST_DIRECTION = { 1, 0, 0 };
        final float TEST_ANGLE = (float)Math.random() * 0.5f;

        BoundingVoid class_under_test = new BoundingVoid();
        assertFalse(class_under_test.checkIntersectionCone(TEST_VERTEX, TEST_DIRECTION, TEST_ANGLE),
                    "Should not find any intersection");
    }

    @Test(groups = "unit")
    public void testBoxIntersection() throws Exception
    {
        final float[] TEST_MIN_EXTENT = { 0, 0, 0 };
        final float[] TEST_MAX_EXTENT = { 1, 0, 0 };

        BoundingVoid class_under_test = new BoundingVoid();
        assertFalse(class_under_test.checkIntersectionBox(TEST_MIN_EXTENT, TEST_MAX_EXTENT),
                    "Should not find any intersection");
    }

    @Test(groups = "unit")
    public void testFrustumIntersection() throws Exception
    {
        final Vector4d[] TEST_FRUSTUM_PLANES = new Vector4d[6];

        BoundingVoid class_under_test = new BoundingVoid();
        assertEquals(class_under_test.checkIntersectionFrustum(TEST_FRUSTUM_PLANES, new Matrix4d()),
                     BoundingVolume.FRUSTUM_ALLOUT,
                     "Should not find any intersection");
    }
}
