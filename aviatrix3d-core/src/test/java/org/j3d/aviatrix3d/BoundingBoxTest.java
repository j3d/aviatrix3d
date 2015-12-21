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

import java.util.Arrays;

import org.j3d.maths.vector.AxisAngle4d;
import org.j3d.maths.vector.Matrix4d;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

import org.j3d.aviatrix3d.rendering.BoundingVolume;

/**
 * Unit tests for the basic bounding box primitive
 *
 * @author justin
 */
public class BoundingBoxTest
{
    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        BoundingBox class_under_test = new BoundingBox();

        assertEquals(class_under_test.getType(), BoundingVolume.BOX_BOUNDS, "Wrong bounds type specified");

        float[] center = new float[3];
        float[] min = new float[3];
        float[] max = new float[3];

        class_under_test.getCenter(center);
        assertEquals(center[0], 0.0f, "Center X coordinate incorrect");
        assertEquals(center[1], 0.0f, "Center Y coordinate incorrect");
        assertEquals(center[2], 0.0f, "Center Z coordinate incorrect");

        class_under_test.getExtents(min, max);

        assertEquals(min[0], 0.0f, "Min extents X coordinate incorrect");
        assertEquals(min[1], 0.0f, "Min extents Y coordinate incorrect");
        assertEquals(min[2], 0.0f, "Min extents Z coordinate incorrect");

        assertEquals(max[0], 0.0f, "Max extents X coordinate incorrect");
        assertEquals(max[1], 0.0f, "Max extents Y coordinate incorrect");
        assertEquals(max[2], 0.0f, "Max extents Z coordinate incorrect");

        class_under_test.getMinimum(min);

        assertEquals(min[0], 0.0f, "Min X coordinate incorrect");
        assertEquals(min[1], 0.0f, "Min Y coordinate incorrect");
        assertEquals(min[2], 0.0f, "Min Z coordinate incorrect");

        class_under_test.getMaximum(max);

        assertEquals(max[0], 0.0f, "Max X coordinate incorrect");
        assertEquals(max[1], 0.0f, "Max Y coordinate incorrect");
        assertEquals(max[2], 0.0f, "Max Z coordinate incorrect");

        assertNotNull(class_under_test.toString(), "No string representation available");
    }

    @Test(groups = "unit")
    public void testMinimums() throws Exception
    {
        final float[] TEST_MINIMUM = { -(float)Math.random(), -(float)Math.random(), -(float)Math.random() };

        BoundingBox class_under_test = new BoundingBox();
        class_under_test.setMinimum(TEST_MINIMUM);

        float[] result = new float[3];
        class_under_test.getMinimum(result);

        assertEquals(result, TEST_MINIMUM, "Didn't set minimum properly");

        // Clear and check again
        result = new float[3];
        class_under_test.getExtents(result, new float[3]);
        assertEquals(result, TEST_MINIMUM, "Didn't set minimum extent properly");
    }

    @Test(groups = "unit")
    public void testMaximums() throws Exception
    {
        final float[] TEST_MAXIMUM = { (float)Math.random(), (float)Math.random(), (float)Math.random() };

        BoundingBox class_under_test = new BoundingBox();
        class_under_test.setMaximum(TEST_MAXIMUM);

        float[] result = new float[3];
        class_under_test.getMaximum(result);

        assertEquals(result, TEST_MAXIMUM, "Didn't set maximum properly");

        // Clear and check again
        result = new float[3];
        class_under_test.getExtents(new float[3], result);
        assertEquals(result, TEST_MAXIMUM, "Didn't set maximum extent properly");

        result = new float[3];
        class_under_test.getSize(result);
        assertEquals(result[0], TEST_MAXIMUM[0] * 0.5f, "Didn't set x size properly");
        assertEquals(result[1], TEST_MAXIMUM[1] * 0.5f, "Didn't set y size properly");
        assertEquals(result[2], TEST_MAXIMUM[2] * 0.5f, "Didn't set z size properly");
    }

    @Test(groups = "unit")
    public void testTransform() throws Exception
    {
        float[] TEST_MINIMUM = { -1.0f, -2.0f, -3.0f };
        float[] TEST_MAXIMUM = {  1.0f,  2.0f,  3.0f };

        AxisAngle4d rotation = new AxisAngle4d();
        rotation.set(0, 1, 0, Math.PI / 2);
        Matrix4d test_matrix = new Matrix4d();
        test_matrix.set(rotation);

        BoundingBox class_under_test = new BoundingBox(TEST_MINIMUM, TEST_MAXIMUM);

        class_under_test.transform(test_matrix);

        float[] result = new float[3];
        class_under_test.getMinimum(result);

        assertEquals(result[0], TEST_MINIMUM[2], "Min didn't rotate x around Y axis");
        assertEquals(result[1], TEST_MINIMUM[1], "Min didn't rotate y around Y axis");
        assertEquals(result[2], TEST_MINIMUM[0], "Min didn't rotate z around Y axis");

        class_under_test.getMaximum(result);

        assertEquals(result[0], TEST_MAXIMUM[2], "Max didn't rotate x around Y axis");
        assertEquals(result[1], TEST_MAXIMUM[1], "Max didn't rotate y around Y axis");
        assertEquals(result[2], TEST_MAXIMUM[0], "Max didn't rotate z around Y axis");
    }

    @Test(groups = "unit", dataProvider = "point tests")
    public void testPointIntersection(float[] minExtents,
                                      float[] maxExtents,
                                      float[] testPoint,
                                      boolean expectedResult)
    {
        BoundingBox class_under_test = new BoundingBox(minExtents, maxExtents);

        assertEquals(class_under_test.checkIntersectionPoint(testPoint),
                     expectedResult,
                     "Point intersection incorrect");
    }

    @Test(groups = "unit", dataProvider = "ray tests")
    public void testRayIntersection(float[] minExtents,
                                    float[] maxExtents,
                                    float[] testPoint,
                                    float[] testDirection,
                                    boolean expectedResult)
    {
        BoundingBox class_under_test = new BoundingBox(minExtents, maxExtents);

        assertEquals(class_under_test.checkIntersectionRay(testPoint, testDirection),
                     expectedResult,
                     "Ray intersection incorrect");
    }

    @Test(groups = "unit", dataProvider = "sphere tests")
    public void testSphereIntersection(float[] minExtents,
                                       float[] maxExtents,
                                       float[] testPoint,
                                       float testRadius,
                                       boolean expectedResult)
    {
        BoundingBox class_under_test = new BoundingBox(minExtents, maxExtents);

        assertEquals(class_under_test.checkIntersectionSphere(testPoint, testRadius),
                     expectedResult,
                     "Sphere intersection incorrect");
    }

    @Test(groups = "unit", dataProvider = "triangle tests")
    public void testTriangleIntersection(float[] minExtents,
                                         float[] maxExtents,
                                         float[] testV0,
                                         float[] testV1,
                                         float[] testV2,
                                         boolean expectedResult)
    {
        BoundingBox class_under_test = new BoundingBox(minExtents, maxExtents);

        assertEquals(class_under_test.checkIntersectionTriangle(testV0, testV1, testV2),
                     expectedResult,
                     "Triangle intersection incorrect");
    }

    @Test(groups = "unit", dataProvider = "box tests")
    public void testBoxIntersection(float[] minExtents,
                                    float[] maxExtents,
                                    float[] testBoxMin,
                                    float[] testBoxMax,
                                    boolean expectedResult)
    {
        BoundingBox class_under_test = new BoundingBox(minExtents, maxExtents);

        assertEquals(class_under_test.checkIntersectionBox(testBoxMin, testBoxMax),
                     expectedResult,
                     "Box intersection incorrect");
    }

    @Test(groups = "unit", dataProvider = "segment tests")
    public void testSegmentIntersection(float[] minExtents,
                                        float[] maxExtents,
                                        float[] testSegmentStart,
                                        float[] testSegmentEnd,
                                        boolean expectedResult)
    {
        BoundingBox class_under_test = new BoundingBox(minExtents, maxExtents);

        assertEquals(class_under_test.checkIntersectionSegment(testSegmentStart, testSegmentEnd),
                     expectedResult,
                     "Segment intersection incorrect");
    }

    @DataProvider(name = "point tests")
    public Object[][] generatePointTestsData()
    {
        Object[][] ret_val = new Object[5][4];

        ret_val[0][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[0][1] = new float[] {  1.0f, 1.0f,   1.0f };
        ret_val[0][2] = new float[] {  0.0f, 0.0f, 0.0f };
        ret_val[0][3] = true;

        ret_val[1][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[1][1] = new float[] {  1.0f, 1.0f,   1.0f };
        ret_val[1][2] = new float[] {  2.0f, 0.0f, 0.0f };
        ret_val[1][3] = false;

        ret_val[2][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[2][1] = new float[] {  1.0f, 1.0f,   1.0f };
        ret_val[2][2] = new float[] {  0.0f, 2.0f, 0.0f };
        ret_val[2][3] = false;

        ret_val[3][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[3][1] = new float[] {  1.0f, 1.0f,   1.0f };
        ret_val[3][2] = new float[] {  0.0f, 0.0f, 2.0f };
        ret_val[3][3] = false;

        ret_val[4][0] = new float[] { -2.0f, 0.0f, 0.0f };
        ret_val[4][1] = new float[] { -1.0f, 2.0f, 2.0f };
        ret_val[4][2] = new float[] { -1.5f, 0.0f, 0.0f };
        ret_val[4][3] = true;

        return ret_val;
    }

    @DataProvider(name = "ray tests")
    public Object[][] generateRayTestsData()
    {
        Object[][] ret_val = new Object[7][5];

        // Ray starts inside box, pointing out
        ret_val[0][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[0][1] = new float[] {  1.0f, 1.0f,   1.0f };
        ret_val[0][2] = new float[] {  0.0f, 0.0f, 0.0f };
        ret_val[0][3] = new float[] {  1.0f, 0.0f, 0.0f };
        ret_val[0][4] = true;

        ret_val[1][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[1][1] = new float[] {  1.0f, 1.0f,   1.0f };
        ret_val[1][2] = new float[] {  0.0f, 0.0f, 0.0f };
        ret_val[1][3] = new float[] {  0.0f, 1.0f, 0.0f };
        ret_val[1][4] = true;

        ret_val[2][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[2][1] = new float[] {  1.0f, 1.0f,   1.0f };
        ret_val[2][2] = new float[] {  0.0f, 0.0f, 0.0f };
        ret_val[2][3] = new float[] {  0.0f, 0.0f, 1.0f };
        ret_val[2][4] = true;

        // Ray outside the box pointing through
        ret_val[3][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[3][1] = new float[] {  1.0f, 1.0f,   1.0f };
        ret_val[3][2] = new float[] { -3.0f, 0.0f, 0.0f };
        ret_val[3][3] = new float[] {  1.0f, 0.0f, 0.0f };
        ret_val[3][4] = true;

        ret_val[4][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[4][1] = new float[] {  1.0f, 1.0f,   1.0f };
        ret_val[4][2] = new float[] { -3.0f, 0.0f, 0.0f };
        ret_val[4][3] = new float[] { -1.0f, 0.0f, 0.0f };
        ret_val[4][4] = false;

        ret_val[5][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[5][1] = new float[] {  1.0f, 1.0f,   1.0f };
        ret_val[5][2] = new float[] { -3.0f, 0.0f, 0.0f };
        ret_val[5][3] = new float[] {  0.0f, 1.0f, 0.0f };
        ret_val[5][4] = false;

        ret_val[6][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[6][1] = new float[] {  1.0f, 1.0f,   1.0f };
        ret_val[6][2] = new float[] { -3.0f, 0.0f, 0.0f };
        ret_val[6][3] = new float[] {  0.0f, 0.0f, 1.0f };
        ret_val[6][4] = false;

        return ret_val;
    }

    @DataProvider(name = "sphere tests")
    public Object[][] generateSphereTestsData()
    {
        Object[][] ret_val = new Object[6][5];

        // Center and sphere wholy contained
        ret_val[0][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[0][1] = new float[] {  1.0f, 1.0f,   1.0f };
        ret_val[0][2] = new float[] {  0.0f, 0.0f, 0.0f };
        ret_val[0][3] = 0.25f;
        ret_val[0][4] = true;

        // Center inside, sphere bigger than box
        ret_val[1][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[1][1] = new float[] {  1.0f, 1.0f,   1.0f };
        ret_val[1][2] = new float[] {  0.0f, 0.0f, 0.0f };
        ret_val[1][3] = 3.5f;
        ret_val[1][4] = true;

        // Center outside, but sphere covers partially
        ret_val[2][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[2][1] = new float[] {  1.0f, 1.0f,   1.0f };
        ret_val[2][2] = new float[] {  2.0f, 0.0f, 0.0f };
        ret_val[2][3] = 1.5f;
        ret_val[2][4] = true;

        // Center outside, radius too small
        ret_val[3][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[3][1] = new float[] {  1.0f, 1.0f,   1.0f };
        ret_val[3][2] = new float[] { -3.0f, 0.0f, 0.0f };
        ret_val[3][3] = 0.5f;
        ret_val[3][4] = false;

        ret_val[4][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[4][1] = new float[] {  1.0f, 1.0f,   1.0f };
        ret_val[4][2] = new float[] {  4.0f, 0.0f, 0.0f };
        ret_val[4][3] = 0.5f;
        ret_val[4][4] = false;

        ret_val[5][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[5][1] = new float[] {  1.0f, 1.0f,   1.0f };
        ret_val[5][2] = new float[] {  0.0f, 1.5f, 3.0f };
        ret_val[5][3] = 0.5f;
        ret_val[5][4] = false;

        return ret_val;
    }

    @DataProvider(name = "triangle tests")
    public Object[][] generateTriangleTestsData()
    {
        Object[][] ret_val = new Object[6][6];

        // triangle completely contained
        ret_val[0][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[0][1] = new float[] {  1.0f, 1.0f,   1.0f };
        ret_val[0][2] = new float[] {  0.0f, 0.0f, 0.0f };
        ret_val[0][3] = new float[] {  0.5f, 0.0f, 0.0f };
        ret_val[0][4] = new float[] {  0.0f, 0.5f, 0.0f };
        ret_val[0][5] = true;

        ret_val[1][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[1][1] = new float[] {  1.0f, 1.0f,   1.0f };
        ret_val[1][2] = new float[] {  0.0f, 0.0f, 0.25f };
        ret_val[1][3] = new float[] {  0.5f, 0.0f, 0.0f };
        ret_val[1][4] = new float[] {  0.0f, 0.5f, 0.0f };
        ret_val[1][5] = true;

        // Partially contained - 2 corners
        ret_val[2][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[2][1] = new float[] {  1.0f, 1.0f,   1.0f };
        ret_val[2][2] = new float[] {  0.0f, 0.0f, 0.25f };
        ret_val[2][3] = new float[] {  1.5f, 0.0f, 0.0f };
        ret_val[2][4] = new float[] {  0.0f, 0.5f, 0.0f };
        ret_val[2][5] = true;

        // Partially contained - 1 corner
        ret_val[3][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[3][1] = new float[] {  1.0f, 1.0f,   1.0f };
        ret_val[3][2] = new float[] {  0.0f, 0.0f, 0.25f };
        ret_val[3][3] = new float[] {  1.5f, 0.0f, 0.0f };
        ret_val[3][4] = new float[] {  0.0f, 1.5f, 0.0f };
        ret_val[3][5] = true;

        // All corners outside, but passing through
        ret_val[4][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[4][1] = new float[] {  1.0f, 1.0f,   1.0f };
        ret_val[4][2] = new float[] {  0.0f, 0.0f, -1.5f };
        ret_val[4][3] = new float[] {  1.5f, 0.0f, 0.0f };
        ret_val[4][4] = new float[] {  0.0f, 1.5f, 0.0f };
        ret_val[4][5] = true;

        // All corners outside no intersection
        ret_val[5][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[5][1] = new float[] {  1.0f, 1.0f,   1.0f };
        ret_val[5][2] = new float[] {  0.0f, 0.0f, 1.5f };
        ret_val[5][3] = new float[] {  1.5f, 0.0f, 0.0f };
        ret_val[5][4] = new float[] {  0.0f, 0.0f, 0.0f };
        ret_val[5][5] = false;

        return ret_val;
    }

    @DataProvider(name = "box tests")
    public Object[][] generateBoxTestsData()
    {
        Object[][] ret_val = new Object[6][5];

        // box completely contained
        ret_val[0][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[0][1] = new float[] {  1.0f,  1.0f,  1.0f };
        ret_val[0][2] = new float[] { -0.5f, -0.5f, -0.5f };
        ret_val[0][3] = new float[] {  0.5f,  0.5f,  0.5f };
        ret_val[0][4] = true;

        // Box completely envelopes
        ret_val[1][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[1][1] = new float[] {  1.0f,  1.0f,  1.0f };
        ret_val[1][2] = new float[] { -1.5f, -1.5f, -1.5f };
        ret_val[1][3] = new float[] {  1.5f,  1.5f,  1.5f };
        ret_val[1][4] = true;

        // Box partial - positive
        ret_val[2][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[2][1] = new float[] {  1.0f,  1.0f,  1.0f };
        ret_val[2][2] = new float[] {  0.0f,  0.0f,  0.0f };
        ret_val[2][3] = new float[] {  1.5f,  1.5f,  1.5f };
        ret_val[2][4] = true;

        // Box partial - negative
        ret_val[3][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[3][1] = new float[] {  1.0f,  1.0f,  1.0f };
        ret_val[3][2] = new float[] { -1.5f, -1.5f, -1.5f };
        ret_val[3][3] = new float[] {  0.0f,  0.0f,  0.0f };
        ret_val[3][4] = true;

        // No overlap - negative
        ret_val[4][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[4][1] = new float[] {  1.0f,  1.0f,  1.0f };
        ret_val[4][2] = new float[] { -1.5f, -1.5f, -1.5f };
        ret_val[4][3] = new float[] { -1.25f,-1.25f,-1.25f };
        ret_val[4][4] = false;

        // No overlap - positive
        ret_val[5][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[5][1] = new float[] {  1.0f,  1.0f,  1.0f };
        ret_val[5][2] = new float[] {  1.25f, 1.25f, 1.25f };
        ret_val[5][3] = new float[] {  1.5f,  1.5f,  1.5f };
        ret_val[5][4] = false;

        return ret_val;
    }

    @DataProvider(name = "segment tests")
    public Object[][] generateSegmentTestsData()
    {
        // Segment and box are almost identical in tests
        Object[][] ret_val = new Object[6][5];

        // segment completely contained
        ret_val[0][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[0][1] = new float[] {  1.0f,  1.0f,  1.0f };
        ret_val[0][2] = new float[] { -0.5f, -0.5f, -0.5f };
        ret_val[0][3] = new float[] {  0.5f,  0.5f,  0.5f };
        ret_val[0][4] = true;

        // Segment completely outside but passing through
        ret_val[1][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[1][1] = new float[] {  1.0f,  1.0f,  1.0f };
        ret_val[1][2] = new float[] { -1.5f, -1.5f, -1.5f };
        ret_val[1][3] = new float[] {  1.5f,  1.5f,  1.5f };
        ret_val[1][4] = true;

        // Segment partial - positive
        ret_val[2][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[2][1] = new float[] {  1.0f,  1.0f,  1.0f };
        ret_val[2][2] = new float[] {  0.0f,  0.0f,  0.0f };
        ret_val[2][3] = new float[] {  1.5f,  1.5f,  1.5f };
        ret_val[2][4] = true;

        // Segment partial - negative
        ret_val[3][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[3][1] = new float[] {  1.0f,  1.0f,  1.0f };
        ret_val[3][2] = new float[] { -1.5f, -1.5f, -1.5f };
        ret_val[3][3] = new float[] {  0.0f,  0.0f,  0.0f };
        ret_val[3][4] = true;

        // No overlap - negative
        ret_val[4][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[4][1] = new float[] {  1.0f,  1.0f,  1.0f };
        ret_val[4][2] = new float[] { -1.5f, -1.5f, -1.5f };
        ret_val[4][3] = new float[] { -1.25f,-1.25f,-1.25f };
        ret_val[4][4] = false;

        // No overlap - positive
        ret_val[5][0] = new float[] { -1.0f, -1.0f, -1.0f };
        ret_val[5][1] = new float[] {  1.0f,  1.0f,  1.0f };
        ret_val[5][2] = new float[] {  1.25f, 1.25f, 1.25f };
        ret_val[5][3] = new float[] {  1.5f,  1.5f,  1.5f };
        ret_val[5][4] = false;

        return ret_val;
    }
}
