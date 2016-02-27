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

package org.j3d.renderer.aviatrix3d.pipeline;

import org.j3d.util.I18nManager;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

import org.j3d.aviatrix3d.SimpleViewport;
import org.j3d.aviatrix3d.Viewport;

/**
 * Unit tests for the viewport layout data class
 *
 * @author justin
 */
public class ViewportLayoutDataTest
{
    @BeforeClass(groups = "unit")
    public void setupClass() throws Exception
    {
        I18nManager intl_mgr = I18nManager.getManager();
        intl_mgr.setApplication(getClass().getName(), "config.i18n.org-j3d-aviatrix3d-resources-extensions");
    }

    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        final float TEST_X = 0.2f;
        final float TEST_Y = 0.3f;
        final float TEST_WIDTH = 0.6f;
        final float TEST_HEIGHT = 0.5f;

        Viewport testView = new SimpleViewport();

        ViewportLayoutData class_under_test =
            new ViewportLayoutData(TEST_X, TEST_Y, TEST_WIDTH, TEST_HEIGHT, testView);

        assertEquals(class_under_test.startX, TEST_X, "Incorrect X saved");
        assertEquals(class_under_test.startY, TEST_Y, "Incorrect Y saved");
        assertEquals(class_under_test.width, TEST_WIDTH, "Incorrect width saved");
        assertEquals(class_under_test.height, TEST_HEIGHT, "Incorrect height saved");
        assertSame(class_under_test.viewport, testView, "Viewport not set");
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidWidth() throws Exception
    {
        new ViewportLayoutData(0.2f, 0.3f, -0.2f, 0.5f, new SimpleViewport());
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidHeight() throws Exception
    {
        new ViewportLayoutData(0.2f, 0.3f, 0.4f, -0.5f, new SimpleViewport());
    }
}
