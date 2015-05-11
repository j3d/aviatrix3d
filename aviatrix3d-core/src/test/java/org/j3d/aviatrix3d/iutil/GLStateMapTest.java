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

package org.j3d.aviatrix3d.iutil;

import javax.media.opengl.GL;

import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

/**
 * Unit tests for the GLStateMap
 *
 * @author justin
 */
public class GLStateMapTest
{
    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        GLStateMap class_under_test = new GLStateMap();

        GL test_key = mock(GL.class);

        assertEquals(class_under_test.size(), 0, "Default map should not contain any entries");
        assertTrue(class_under_test.isEmpty(), "Default map should be empty");
        assertFalse(class_under_test.containsKey(test_key), "Should not contain any key");
        assertFalse(class_under_test.remove(test_key), "Cannot remove successfully an entry it does not contain");
        assertFalse(class_under_test.getState(test_key), "Cannot get an entry it does not contain");
    }

    @Test(groups = "unit")
    public void testPutAndRemove() throws Exception
    {
        final boolean TEST_VALUE = true;
        final GL TEST_KEY = mock(GL.class);

        GLStateMap class_under_test = new GLStateMap();

        class_under_test.put(TEST_KEY, TEST_VALUE);

        assertEquals(class_under_test.size(), 1, "Did not correctly add an entry");
        assertFalse(class_under_test.isEmpty(), "Map should not be empty after adding");
        assertTrue(class_under_test.containsKey(TEST_KEY), "Key was not found as valid");
        assertSame(class_under_test.getState(TEST_KEY), TEST_VALUE, "Fetching key didn't return the same reference");

        // Now remove it and make sure the map is empty
        assertSame(class_under_test.remove(TEST_KEY), TEST_VALUE, "Removing key didn't return the same reference");

        assertEquals(class_under_test.size(), 0, "Default map should not contain any entries");
        assertTrue(class_under_test.isEmpty(), "Default map should be empty");
        assertFalse(class_under_test.containsKey(TEST_KEY), "Should not contain the key after removal");
        assertFalse(class_under_test.remove(TEST_KEY), "Cannot remove an entry twice");
        assertFalse(class_under_test.getState(TEST_KEY), "Cannot get an entry after it was removed");
    }
}
