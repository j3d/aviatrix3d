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

import org.j3d.util.I18nManager;
import org.testng.annotations.BeforeMethod;
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
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testConstructionWithInvalidStrategy() throws Exception
    {
        new TextureUpdateStateManager(-4963);
    }
}
