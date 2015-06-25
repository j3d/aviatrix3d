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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

import org.j3d.aviatrix3d.rendering.BoundingVolume;

/**
 * Unit test for the bounding sphere
 *
 * @author justin
 */
public class BoundingSphereTest
{
    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        BoundingSphere class_under_test = new BoundingSphere();

        assertEquals(class_under_test.getType(), BoundingVolume.SPHERE_BOUNDS, "Wrong bounds type specified");

        float[] center = new float[3];
        float[] min = new float[3];
        float[] max = new float[3];

        class_under_test.getCenter(center);
        assertEquals(center[0], 0.0f, "Center X coordinate incorrect");
        assertEquals(center[1], 0.0f, "Center Y coordinate incorrect");
        assertEquals(center[2], 0.0f, "Center Z coordinate incorrect");

        assertEquals(class_under_test.getRadius(), 1.0f, "Incorrect default radius");

        class_under_test.getExtents(min, max);

        assertEquals(min[0], -1.0f, "Min extents X coordinate incorrect");
        assertEquals(min[1], -1.0f, "Min extents Y coordinate incorrect");
        assertEquals(min[2], -1.0f, "Min extents Z coordinate incorrect");

        assertEquals(max[0], 1.0f, "Max extents X coordinate incorrect");
        assertEquals(max[1], 1.0f, "Max extents Y coordinate incorrect");
        assertEquals(max[2], 1.0f, "Max extents Z coordinate incorrect");

        assertNotNull(class_under_test.toString(), "No string representation available");
    }

    @Test(groups = "unit", dataProvider = "point tests")
    public void testPointIntersection(float[] centre,
                                      float radius,
                                      float[] testPoint,
                                      boolean expectedResult)
    {
        BoundingSphere class_under_test = new BoundingSphere(centre, radius);

        assertEquals(class_under_test.checkIntersectionPoint(testPoint),
                     expectedResult,
                     "Point intersection incorrect");
    }

    @Test(groups = "unit", dataProvider = "ray tests")
    public void testRayIntersection(float[] centre,
                                    float radius,
                                    float[] testPoint,
                                    float[] testDirection,
                                    boolean expectedResult)
    {
        BoundingSphere class_under_test = new BoundingSphere(centre, radius);

        assertEquals(class_under_test.checkIntersectionRay(testPoint, testDirection),
                     expectedResult,
                     "Ray intersection incorrect");
    }

    @Test(groups = "unit", dataProvider = "segment tests")
    public void testLineSegmentIntersection(float[] centre,
                                            float radius,
                                            float[] testStart,
                                            float[] testEnd,
                                            boolean expectedResult)
    {
        BoundingSphere class_under_test = new BoundingSphere(centre, radius);

        assertEquals(class_under_test.checkIntersectionSegment(testStart, testEnd),
                     expectedResult,
                     "Ray intersection incorrect");
    }

    @DataProvider(name = "point tests")
    public Object[][] generatePointTestsData()
    {
        Object[][] ret_val = new Object[4][4];

        ret_val[0][0] = new float[] { 0, 0, 0 };
        ret_val[0][1] = 1;
        ret_val[0][2] = new float[] {  0.1f, 0.0f, 0.0f };
        ret_val[0][3] = true;

        ret_val[1][0] = new float[] { 0, 0, 0 };
        ret_val[1][1] = 1;
        ret_val[1][2] = new float[] {  2.5f, 0.0f, 0.0f };
        ret_val[1][3] = false;

        ret_val[2][0] = new float[] { -2.0f, -2.0f, -2.0f };
        ret_val[2][1] = 1;
        ret_val[2][2] = new float[] {  0, 0, 0 };
        ret_val[2][3] = false;

        ret_val[3][0] = new float[] { 2.0f, 2.0f, 2.0f };
        ret_val[3][1] = 0.5f;
        ret_val[3][2] = new float[] {  -0.5f, 0, 0 };
        ret_val[3][3] = false;

        return ret_val;
    }

    @DataProvider(name = "ray tests")
    public Object[][] generateRayTestsData()
    {
        Object[][] ret_val = new Object[8][5];

        // Ray starts inside sphere, pointing out
        ret_val[0][0] = new float[] { 0, 0, 0 };
        ret_val[0][1] = 1;
        ret_val[0][2] = new float[] {  0.0f, 0.0f, 0.0f };
        ret_val[0][3] = new float[] {  1.0f, 0.0f, 0.0f };
        ret_val[0][4] = true;

        ret_val[1][0] = new float[] { 0, 0, 0 };
        ret_val[1][1] = 1;
        ret_val[1][2] = new float[] {  0.0f, 0.0f, 0.0f };
        ret_val[1][3] = new float[] {  0.0f, 1.0f, 0.0f };
        ret_val[1][4] = true;

        ret_val[2][0] = new float[] { 0, 0, 0 };
        ret_val[2][1] = 1;
        ret_val[2][2] = new float[] {  0.0f, 0.0f, 0.0f };
        ret_val[2][3] = new float[] {  0.0f, 0.0f, 1.0f };
        ret_val[2][4] = true;

        // Ray outside the sphere pointing through
        ret_val[3][0] = new float[] { 0, 0, 0 };
        ret_val[3][1] = 1;
        ret_val[3][2] = new float[] { -3.0f, 0.0f, 0.0f };
        ret_val[3][3] = new float[] {  1.0f, 0.0f, 0.0f };
        ret_val[3][4] = true;

        ret_val[4][0] = new float[] { 0, 0, 0 };
        ret_val[4][1] = 1;
        ret_val[4][2] = new float[] { -3.0f, 0.0f, 0.0f };
        ret_val[4][3] = new float[] { -1.0f, 0.0f, 0.0f };
        ret_val[4][4] = false;

        ret_val[5][0] = new float[] { 0, 0, 0 };
        ret_val[5][1] = 1;
        ret_val[5][2] = new float[] { -3.0f, 0.0f, 0.0f };
        ret_val[5][3] = new float[] {  0.0f, 1.0f, 0.0f };
        ret_val[5][4] = false;

        ret_val[6][0] = new float[] { 0, 0, 0 };
        ret_val[6][1] = 1;
        ret_val[6][2] = new float[] { -3.0f, 0.0f, 0.0f };
        ret_val[6][3] = new float[] {  0.0f, 0.0f, 1.0f };
        ret_val[6][4] = false;

        ret_val[7][0] = new float[] { 0, 0, 0 };
        ret_val[7][1] = 1;
        ret_val[7][2] = new float[] { 1.5f, 0.0f, 0.0f };
        ret_val[7][3] = new float[] { 0.0f, 1.0f, 0.0f };
        ret_val[7][4] = false;

        return ret_val;
    }

    @DataProvider(name = "segment tests")
    public Object[][] generateSegmentTestsData()
    {
        // Segment and box are almost identical in tests
        Object[][] ret_val = new Object[6][5];

        // segment completely contained
        ret_val[0][0] = new float[] { 0, 0, 0 };
        ret_val[0][1] = 1;
        ret_val[0][2] = new float[] { -0.5f, -0.5f, -0.5f };
        ret_val[0][3] = new float[] {  0.5f,  0.5f,  0.5f };
        ret_val[0][4] = true;

        // Segment completely outside but passing through
        ret_val[1][0] = new float[] { 0, 0, 0 };
        ret_val[1][1] = 1;
        ret_val[1][2] = new float[] { -1.5f, -1.5f, -1.5f };
        ret_val[1][3] = new float[] {  1.5f,  1.5f,  1.5f };
        ret_val[1][4] = true;

        // Segment partial - positive
        ret_val[2][0] = new float[] { 0, 0, 0 };
        ret_val[2][1] = 1;
        ret_val[2][2] = new float[] {  0.0f,  0.0f,  0.0f };
        ret_val[2][3] = new float[] {  1.5f,  1.5f,  1.5f };
        ret_val[2][4] = true;

        // Segment partial - negative
        ret_val[3][0] = new float[] { 0, 0, 0 };
        ret_val[3][1] = 1;
        ret_val[3][2] = new float[] { -1.5f, -1.5f, -1.5f };
        ret_val[3][3] = new float[] {  0.0f,  0.0f,  0.0f };
        ret_val[3][4] = true;

        // No overlap - negative
        ret_val[4][0] = new float[] { 0, 0, 0 };
        ret_val[4][1] = 1;
        ret_val[4][2] = new float[] { -1.5f, -1.5f, -1.5f };
        ret_val[4][3] = new float[] { -1.25f,-1.25f,-1.25f };
        ret_val[4][4] = false;

        // No overlap - positive
        ret_val[5][0] = new float[] { 0, 0, 0 };
        ret_val[5][1] = 1;
        ret_val[5][2] = new float[] {  1.25f, 1.25f, 1.25f };
        ret_val[5][3] = new float[] {  1.5f,  1.5f,  1.5f };
        ret_val[5][4] = false;

        return ret_val;
    }
}
