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
}
