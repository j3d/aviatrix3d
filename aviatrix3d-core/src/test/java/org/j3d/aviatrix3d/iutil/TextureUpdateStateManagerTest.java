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

import com.jogamp.opengl.GL;

import org.j3d.util.I18nManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

/**
 * Unit tests for the texture update state manager
 *
 * @author justin
 */
public class TextureUpdateStateManagerTest
{
    @BeforeMethod(groups = "unit")
    public void setupTests() throws Exception
    {
        I18nManager intl_mgr = I18nManager.getManager();
        intl_mgr.setApplication("TextureUpdateStateManagerTest", "config.i18n.org-j3d-aviatrix3d-resources-core");
    }

    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        TextureUpdateStateManager class_under_test =
            new TextureUpdateStateManager(TextureUpdateStateManager.UPDATE_BUFFER_ALL);

        GL test_key = mock(GL.class);

        assertEquals(class_under_test.size(), 0, "Default map should not contain any entries");
        assertTrue(class_under_test.isEmpty(), "Default map should be empty");
        assertEquals(class_under_test.getNumUpdatesPending(test_key), 0, "Cannot get an entry it does not contain");
        assertNull(class_under_test.getUpdatesAndClear(test_key), "Should not have any updates");
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testConstructionWithInvalidStrategy() throws Exception
    {
        new TextureUpdateStateManager(-4963);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testConstructionWithInvalidCapacity() throws Exception
    {
        new TextureUpdateStateManager(TextureUpdateStateManager.UPDATE_BUFFER_ALL, -5, 1.0f);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testConstructionWithInvalidLoadFactor() throws Exception
    {
        new TextureUpdateStateManager(TextureUpdateStateManager.UPDATE_BUFFER_ALL, 5, -0.5f);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testChangeOfInvalidStrategy() throws Exception
    {
        TextureUpdateStateManager class_under_test =
            new TextureUpdateStateManager(TextureUpdateStateManager.UPDATE_BUFFER_ALL);

        class_under_test.setUpdateStrategy(-29456);
    }

    @Test(groups = "unit")
    public void testContextManagement() throws Exception
    {
        GL test_key = mock(GL.class);

        TextureUpdateStateManager class_under_test =
            new TextureUpdateStateManager(TextureUpdateStateManager.UPDATE_BUFFER_ALL);

        class_under_test.addContext(test_key);

        assertEquals(class_under_test.size(), 1, "Wrong number of contexts cached");
        assertFalse(class_under_test.isEmpty(), "Should not be empty if size is non-zero");
        assertEquals(class_under_test.getNumUpdatesPending(test_key), 0, "Should not have any updates registered");

        // We should always get an array for a registered class. The number of valid items
        // in the array is the previous call, so we don't actually check anything about
        // the array here except that we have something.
        assertNotNull(class_under_test.getUpdatesAndClear(test_key), "Should find something for updates");

        class_under_test.removeContext(test_key);

        assertEquals(class_under_test.size(), 0, "Should not have contexts after removal");
        assertTrue(class_under_test.isEmpty(), "Should be empty after context removal");
    }

    @Test(groups = "unit")
    public void testClearContexts() throws Exception
    {
        GL test_key = mock(GL.class);

        TextureUpdateStateManager class_under_test =
            new TextureUpdateStateManager(TextureUpdateStateManager.UPDATE_BUFFER_ALL);

        class_under_test.addContext(test_key);

        assertEquals(class_under_test.size(), 1, "Wrong number of contexts cached");
        assertFalse(class_under_test.isEmpty(), "Should not be empty if size is non-zero");
        assertEquals(class_under_test.getNumUpdatesPending(test_key), 0, "Should not have any updates registered");

        // We should always get an array for a registered class. The number of valid items
        // in the array is the previous call, so we don't actually check anything about
        // the array here except that we have something.
        assertNotNull(class_under_test.getUpdatesAndClear(test_key), "Should find something for updates");

        class_under_test.clear();

        assertEquals(class_under_test.size(), 0, "Should not have contexts after removal");
        assertTrue(class_under_test.isEmpty(), "Should be empty after context removal");
    }

    @Test(groups = "unit", dataProvider = "update strategy")
    public void testUpdateWithSingle(int testStrategy) throws Exception
    {
        // Doesn't matter the test strategy, they should all result in the
        // same output when a single update is given.

        final int TEST_TEXTURE_WIDTH = 5;
        final int TEST_TEXTURE_HEIGHT = 5;
        final byte[] TEST_PIXELS = new byte[TEST_TEXTURE_WIDTH * TEST_TEXTURE_HEIGHT];
        final int TEST_TEXTURE_FORMAT = GL.GL_ALPHA;

        // generate some random test data.
        for(int i = 0; i < TEST_PIXELS.length; i++)
        {
            TEST_PIXELS[i] = (byte)(Math.random() * 255);
        }

        GL test_key = mock(GL.class);

        TextureUpdateStateManager class_under_test =
            new TextureUpdateStateManager(testStrategy);
        class_under_test.setTextureFormat(TEST_TEXTURE_FORMAT);
        class_under_test.addContext(test_key);

        class_under_test.textureUpdated(0, 0, 0, TEST_TEXTURE_WIDTH, TEST_TEXTURE_HEIGHT, 1, 0, TEST_PIXELS);

        assertEquals(class_under_test.size(), 1, "Wrong number of contexts cached");
        assertFalse(class_under_test.isEmpty(), "Should not be empty if size is non-zero");
        assertEquals(class_under_test.getNumUpdatesPending(test_key), 1, "Should have the update registered");

        TextureUpdateData[] result_updates = class_under_test.getUpdatesAndClear(test_key);

        assertNotNull(result_updates[0], "No update data found for the updated texture");
        assertEquals(result_updates[0].format, TEST_TEXTURE_FORMAT, "Texture format incorrectly set");
        assertEquals(result_updates[0].width, TEST_TEXTURE_WIDTH, "Texture width incorrectly set");
        assertEquals(result_updates[0].height, TEST_TEXTURE_HEIGHT, "Texture height incorrectly set");
        assertEquals(result_updates[0].depth, 1, "Texture depth incorrectly set");
        assertEquals(result_updates[0].level, 0, "Texture level incorrectly set");

        for(int i = 0; i < TEST_PIXELS.length; i++)
        {
            assertEquals(result_updates[0].pixels.get(i), TEST_PIXELS[i], "Wrong pixel copied at index " + i);
        }
    }

    @DataProvider(name = "update strategy")
    public Object[][] generateUpdateStrategyData()
    {
        return new Object[][]
        {
            { TextureUpdateStateManager.UPDATE_BUFFER_ALL },
            { TextureUpdateStateManager.UPDATE_BUFFER_LAST },
            { TextureUpdateStateManager.UPDATE_DISCARD_OVERWRITES },
        };
    }
}
