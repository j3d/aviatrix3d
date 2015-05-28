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

package org.j3d.aviatrix3d.rendering;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit test for the basic profile data holder class
 *
 * @author justin
 */
public class ProfilingDataTest
{
    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        // Make sure everything starts as default
        ProfilingData class_under_test = new ProfilingData();

        assertEquals(class_under_test.sceneCullTime, 0, "Cull time default incorrect");
        assertEquals(class_under_test.sceneRenderTime, 0, "Render time default incorrect");
        assertEquals(class_under_test.sceneSortTime, 0, "State sort time default incorrect");
        assertEquals(class_under_test.sceneDrawTime, 0, "Draw time default incorrect");
        assertEquals(class_under_test.sceneSortInput, 0, "Sort count default incorrect");
    }
}
