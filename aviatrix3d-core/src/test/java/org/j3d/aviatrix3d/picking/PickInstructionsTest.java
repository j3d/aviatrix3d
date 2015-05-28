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

package org.j3d.aviatrix3d.picking;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit tests for the pick instruction data holder
 *
 * @author justin
 */
public class PickInstructionsTest
{
    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        PickInstructions class_under_test = new PickInstructions();
        assertFalse(class_under_test.hasTransform, "Transform should not be set");
        assertNotNull(class_under_test.localTransform, "Missing matrix");
        assertNotNull(class_under_test.children, "Children should be allocated");
        assertEquals(class_under_test.numChildren, 0, "Child count should be zero");
    }

    @Test(groups = "unit")
    public void testChildResize() throws Exception
    {
        final int TEST_CHILD_INC = 5;
        PickInstructions class_under_test = new PickInstructions();
        int current_size = class_under_test.children.length;

        class_under_test.numChildren = 2;
        class_under_test.resizeChildren(current_size - 1);

        assertEquals(class_under_test.children.length,
                     current_size - 1,
                     "Did not resize smaller");
        assertEquals(class_under_test.numChildren, 0, "Resize smaller didn't reset children count");

        class_under_test.numChildren = 2;
        class_under_test.resizeChildren(current_size + TEST_CHILD_INC);

        assertEquals(class_under_test.children.length,
                     current_size+ TEST_CHILD_INC,
                     "Did not resize larger");
        assertEquals(class_under_test.numChildren, 0, "Resize larger didn't reset children count");
    }
}
