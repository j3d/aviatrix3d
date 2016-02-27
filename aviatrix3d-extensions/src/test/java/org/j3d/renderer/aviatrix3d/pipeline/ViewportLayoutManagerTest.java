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
 * Unit tests for the layout manager
 *
 * @author justin
 */
public class ViewportLayoutManagerTest
{
    @BeforeClass(groups = "unit")
    public void setupClass() throws Exception
    {
        I18nManager intl_mgr = I18nManager.getManager();
        intl_mgr.setApplication(getClass().getName(), "config.i18n.org-j3d-aviatrix3d-resources-extensions");
    }

    @Test(groups = "unit")
    public void testAddNullViewNoComplaints() throws Exception
    {
        ViewportLayoutManager class_under_test = new ViewportLayoutManager();
        class_under_test.addManagedViewport(null, 0, 0, 1, 1);

    }

    @Test(groups = "unit",
          description = "Tests a single viewport that takes up the whole screen")
    public void testSingleSimpleView() throws Exception
    {
        final int TEST_SCREEN_X = 10;
        final int TEST_SCREEN_Y = 20;
        final int TEST_SCREEN_WIDTH = 100;
        final int TEST_SCREEN_HEIGHT = 150;

        Viewport testView = new SimpleViewport();

        ViewportLayoutManager class_under_test = new ViewportLayoutManager();
        class_under_test.addManagedViewport(testView, 0, 0, 1, 1);

        class_under_test.graphicsDeviceResized(TEST_SCREEN_X, TEST_SCREEN_Y, TEST_SCREEN_WIDTH, TEST_SCREEN_HEIGHT);
        class_under_test.sendResizeUpdates();

        assertEquals(testView.getX(), TEST_SCREEN_X, "The X position didn't move");
        assertEquals(testView.getY(), TEST_SCREEN_Y, "The Y position didn't move");
        assertEquals(testView.getWidth(), TEST_SCREEN_WIDTH, "Didn't resize the width");
        assertEquals(testView.getHeight(), TEST_SCREEN_HEIGHT, "Didn't resize the height");
    }

    @Test(groups = "unit",
          description = "Tests two viewports that don't overlap")
    public void testTwoNonOverlappingViews() throws Exception
    {
        final int TEST_SCREEN_X = 10;
        final int TEST_SCREEN_Y = 20;
        final int TEST_SCREEN_WIDTH = 100;
        final int TEST_SCREEN_HEIGHT = 150;
        final float TEST_X_1 = 0.0f;
        final float TEST_Y_1 = 0.0f;
        final float TEST_WIDTH_1 = 0.4f;
        final float TEST_HEIGHT_1 = 0.4f;
        final float TEST_X_2 = 0.4f;
        final float TEST_Y_2 = 0.0f;
        final float TEST_WIDTH_2 = 0.6f;
        final float TEST_HEIGHT_2 = 0.6f;

        Viewport testView1 = new SimpleViewport();
        Viewport testView2 = new SimpleViewport();

        ViewportLayoutManager class_under_test = new ViewportLayoutManager();
        class_under_test.addManagedViewport(testView1, TEST_X_1, TEST_Y_1, TEST_WIDTH_1, TEST_HEIGHT_1);
        class_under_test.addManagedViewport(testView2, TEST_X_2, TEST_Y_2, TEST_WIDTH_2, TEST_HEIGHT_2);

        class_under_test.graphicsDeviceResized(TEST_SCREEN_X, TEST_SCREEN_Y, TEST_SCREEN_WIDTH, TEST_SCREEN_HEIGHT);
        class_under_test.sendResizeUpdates();

        assertEquals(testView1.getX(), (int)(TEST_SCREEN_X + TEST_X_1 * TEST_SCREEN_WIDTH), "View 1 X position didn't move");
        assertEquals(testView1.getY(), (int)(TEST_SCREEN_Y + TEST_Y_1 * TEST_SCREEN_HEIGHT), "View 1 Y position didn't move");
        assertEquals(testView1.getWidth(), (int)(TEST_WIDTH_1 * TEST_SCREEN_WIDTH), "View 1 didn't resize the width");
        assertEquals(testView1.getHeight(), (int)(TEST_HEIGHT_1 * TEST_SCREEN_HEIGHT), "View 1 didn't resize the height");

        assertEquals(testView2.getX(), (int)(TEST_SCREEN_X + TEST_X_2 * TEST_SCREEN_WIDTH), "View 2 X position didn't move");
        assertEquals(testView2.getY(), (int)(TEST_SCREEN_Y + TEST_Y_2 * TEST_SCREEN_HEIGHT), "View 2 Y position didn't move");
        assertEquals(testView2.getWidth(), (int)(TEST_WIDTH_2 * TEST_SCREEN_WIDTH), "View 2 didn't resize the width");
        assertEquals(testView2.getHeight(), (int)(TEST_HEIGHT_2 * TEST_SCREEN_HEIGHT), "View 2 didn't resize the height");
    }

    @Test(groups = "unit",
          description = "Make sure that we don't add the same view twice and send two events")
    public void testAddViewTwice() throws Exception
    {
        final int TEST_SCREEN_X = 10;
        final int TEST_SCREEN_Y = 20;
        final int TEST_SCREEN_WIDTH = 100;
        final int TEST_SCREEN_HEIGHT = 150;
        final float TEST_X_1 = 0.0f;
        final float TEST_Y_1 = 0.0f;
        final float TEST_WIDTH_1 = 0.4f;
        final float TEST_HEIGHT_1 = 0.4f;
        final float TEST_X_2 = 0.4f;
        final float TEST_Y_2 = 0.0f;
        final float TEST_WIDTH_2 = 0.6f;
        final float TEST_HEIGHT_2 = 0.6f;

        Viewport testView1 = new SimpleViewport();

        ViewportLayoutManager class_under_test = new ViewportLayoutManager();
        class_under_test.addManagedViewport(testView1, TEST_X_1, TEST_Y_1, TEST_WIDTH_1, TEST_HEIGHT_1);
        class_under_test.addManagedViewport(testView1, TEST_X_2, TEST_Y_2, TEST_WIDTH_2, TEST_HEIGHT_2);

        class_under_test.graphicsDeviceResized(TEST_SCREEN_X, TEST_SCREEN_Y, TEST_SCREEN_WIDTH, TEST_SCREEN_HEIGHT);
        class_under_test.sendResizeUpdates();

        assertEquals(testView1.getX(), (int)(TEST_SCREEN_X + TEST_X_1 * TEST_SCREEN_WIDTH), "View 1 X position didn't move");
        assertEquals(testView1.getY(), (int)(TEST_SCREEN_Y + TEST_Y_1 * TEST_SCREEN_HEIGHT), "View 1 Y position didn't move");
        assertEquals(testView1.getWidth(), (int)(TEST_WIDTH_1 * TEST_SCREEN_WIDTH), "View 1 didn't resize the width");
        assertEquals(testView1.getHeight(), (int)(TEST_HEIGHT_1 * TEST_SCREEN_HEIGHT), "View 1 didn't resize the height");
    }

    @Test(groups = "unit",
        description = "Make sure that if we haven't had an event, we don't resize the viewport")
    public void testNoResizeIfNoChange() throws Exception
    {
        final int TEST_INITIAL_X = 10;
        final int TEST_INITIAL_Y = 20;
        final int TEST_INITIAL_WIDTH = 100;
        final int TEST_INITIAL_HEIGHT = 150;

        Viewport testView = new SimpleViewport();
        testView.setDimensions(TEST_INITIAL_X, TEST_INITIAL_Y, TEST_INITIAL_WIDTH, TEST_INITIAL_HEIGHT);

        ViewportLayoutManager class_under_test = new ViewportLayoutManager();
        class_under_test.addManagedViewport(testView, 0, 0, 1, 1);

        class_under_test.sendResizeUpdates();

        assertEquals(testView.getX(), TEST_INITIAL_X, "The X position didn't move");
        assertEquals(testView.getY(), TEST_INITIAL_Y, "The Y position didn't move");
        assertEquals(testView.getWidth(), TEST_INITIAL_WIDTH, "Didn't resize the width");
        assertEquals(testView.getHeight(), TEST_INITIAL_HEIGHT, "Didn't resize the height");
    }

    @Test(groups = "unit",
        description = "Make sure that if remove the viewport, it doesn't get the update")
    public void testNoResizeIfRemoved() throws Exception
    {
        final int TEST_INITIAL_X = 10;
        final int TEST_INITIAL_Y = 20;
        final int TEST_INITIAL_WIDTH = 100;
        final int TEST_INITIAL_HEIGHT = 150;
        final int TEST_SCREEN_X = 30;
        final int TEST_SCREEN_Y = 40;
        final int TEST_SCREEN_WIDTH = 120;
        final int TEST_SCREEN_HEIGHT = 90;

        Viewport testView = new SimpleViewport();
        testView.setDimensions(TEST_INITIAL_X, TEST_INITIAL_Y, TEST_INITIAL_WIDTH, TEST_INITIAL_HEIGHT);

        ViewportLayoutManager class_under_test = new ViewportLayoutManager();
        class_under_test.addManagedViewport(testView, 0, 0, 1, 1);

        class_under_test.graphicsDeviceResized(TEST_SCREEN_X, TEST_SCREEN_Y, TEST_SCREEN_WIDTH, TEST_SCREEN_HEIGHT);
        class_under_test.removeManagedViewport(testView);
        class_under_test.sendResizeUpdates();

        assertEquals(testView.getX(), TEST_INITIAL_X, "The X position didn't move");
        assertEquals(testView.getY(), TEST_INITIAL_Y, "The Y position didn't move");
        assertEquals(testView.getWidth(), TEST_INITIAL_WIDTH, "Didn't resize the width");
        assertEquals(testView.getHeight(), TEST_INITIAL_HEIGHT, "Didn't resize the height");
    }

    @Test(groups = "unit",
        description = "Make sure that if remove the viewport, it doesn't get the update")
    public void testNoResizeIfCleared() throws Exception
    {
        final int TEST_INITIAL_X = 10;
        final int TEST_INITIAL_Y = 20;
        final int TEST_INITIAL_WIDTH = 100;
        final int TEST_INITIAL_HEIGHT = 150;
        final int TEST_SCREEN_X = 30;
        final int TEST_SCREEN_Y = 40;
        final int TEST_SCREEN_WIDTH = 120;
        final int TEST_SCREEN_HEIGHT = 90;

        Viewport testView = new SimpleViewport();
        testView.setDimensions(TEST_INITIAL_X, TEST_INITIAL_Y, TEST_INITIAL_WIDTH, TEST_INITIAL_HEIGHT);

        ViewportLayoutManager class_under_test = new ViewportLayoutManager();
        class_under_test.addManagedViewport(testView, 0, 0, 1, 1);

        class_under_test.graphicsDeviceResized(TEST_SCREEN_X, TEST_SCREEN_Y, TEST_SCREEN_WIDTH, TEST_SCREEN_HEIGHT);
        class_under_test.clear();
        class_under_test.sendResizeUpdates();

        assertEquals(testView.getX(), TEST_INITIAL_X, "The X position didn't move");
        assertEquals(testView.getY(), TEST_INITIAL_Y, "The Y position didn't move");
        assertEquals(testView.getWidth(), TEST_INITIAL_WIDTH, "Didn't resize the width");
        assertEquals(testView.getHeight(), TEST_INITIAL_HEIGHT, "Didn't resize the height");
    }

    @Test(groups = "unit",
        description = "Make sure removing a random view has no effect")
    public void testRemoveUnregisteredView() throws Exception
    {
        final int TEST_SCREEN_X = 10;
        final int TEST_SCREEN_Y = 20;
        final int TEST_SCREEN_WIDTH = 100;
        final int TEST_SCREEN_HEIGHT = 150;

        Viewport testView = new SimpleViewport();

        ViewportLayoutManager class_under_test = new ViewportLayoutManager();
        class_under_test.addManagedViewport(testView, 0, 0, 1, 1);

        class_under_test.graphicsDeviceResized(TEST_SCREEN_X, TEST_SCREEN_Y, TEST_SCREEN_WIDTH, TEST_SCREEN_HEIGHT);
        class_under_test.removeManagedViewport(new SimpleViewport());
        class_under_test.sendResizeUpdates();

        assertEquals(testView.getX(), TEST_SCREEN_X, "The X position didn't move");
        assertEquals(testView.getY(), TEST_SCREEN_Y, "The Y position didn't move");
        assertEquals(testView.getWidth(), TEST_SCREEN_WIDTH, "Didn't resize the width");
        assertEquals(testView.getHeight(), TEST_SCREEN_HEIGHT, "Didn't resize the height");
    }


    @Test(groups = "unit",
        description = "Make sure removing a null view has no effect")
    public void testRemoveNullView() throws Exception
    {
        final int TEST_SCREEN_X = 10;
        final int TEST_SCREEN_Y = 20;
        final int TEST_SCREEN_WIDTH = 100;
        final int TEST_SCREEN_HEIGHT = 150;

        Viewport testView = new SimpleViewport();

        ViewportLayoutManager class_under_test = new ViewportLayoutManager();
        class_under_test.addManagedViewport(testView, 0, 0, 1, 1);

        class_under_test.graphicsDeviceResized(TEST_SCREEN_X, TEST_SCREEN_Y, TEST_SCREEN_WIDTH, TEST_SCREEN_HEIGHT);
        class_under_test.removeManagedViewport(null);
        class_under_test.sendResizeUpdates();

        assertEquals(testView.getX(), TEST_SCREEN_X, "The X position didn't move");
        assertEquals(testView.getY(), TEST_SCREEN_Y, "The Y position didn't move");
        assertEquals(testView.getWidth(), TEST_SCREEN_WIDTH, "Didn't resize the width");
        assertEquals(testView.getHeight(), TEST_SCREEN_HEIGHT, "Didn't resize the height");
    }
}
