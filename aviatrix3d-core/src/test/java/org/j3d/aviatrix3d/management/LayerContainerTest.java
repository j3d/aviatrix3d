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

package org.j3d.aviatrix3d.management;

import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.*;

import org.j3d.aviatrix3d.Layer;
import org.j3d.aviatrix3d.NodeUpdateHandler;
import org.j3d.aviatrix3d.NodeUpdateListener;
import org.j3d.aviatrix3d.rendering.ViewportCullable;

/**
 * Unit tests for the layer container internal class
 *
 * @author justin
 */
public class LayerContainerTest
{
    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        LayerContainer class_under_test = new LayerContainer();

        assertFalse(class_under_test.isLive(), "Default class should not be live");
        assertNull(class_under_test.getUserData(), "User data should not yet be defined");
    }

    @Test(groups = "unit")
    public void testChangeLayers() throws Exception
    {
        Layer[] test_layers = { new TestLayer() };

        LayerContainer class_under_test = new LayerContainer();
        class_under_test.changeLayers(test_layers, 1);

        assertFalse(test_layers[0].isLive(), "Child should not be live unless parent is");

        class_under_test.setLive(true);

        assertTrue(test_layers[0].isLive(), "Child was not propagated live status");

        // Remove the layers and make sure that the old child is no longer live again
        class_under_test.changeLayers(test_layers, 0);

        assertFalse(test_layers[0].isLive(), "Child should not be live after removal from parent");
    }

    @Test(groups = "unit")
    public void testLayerUpdateHandlerRegisterHandlerBefore() throws Exception
    {
        Layer[] test_layers = { new TestLayer() };

        NodeUpdateHandler test_handler = mock(NodeUpdateHandler.class);

        // Add update handler first so that we check layer set propagates the handler
        LayerContainer class_under_test = new LayerContainer();
        class_under_test.setLive(true);
        class_under_test.setUpdateHandler(test_handler);
        class_under_test.changeLayers(test_layers, 1);

        NodeUpdateListener mock_listener = mock(NodeUpdateListener.class);

        test_layers[0].dataChanged(mock_listener);

        verify(test_handler, times(1)).dataChanged(mock_listener, test_layers[0]);
    }

    @Test(groups = "unit")
    public void testLayerUpdateHandlerRegisterHandlerAfter() throws Exception
    {
        Layer[] test_layers = { new TestLayer() };

        NodeUpdateHandler test_handler = mock(NodeUpdateHandler.class);

        // Add update handler after the layer set to make sure everyone gets it.
        LayerContainer class_under_test = new LayerContainer();
        class_under_test.setLive(true);
        class_under_test.changeLayers(test_layers, 1);
        class_under_test.setUpdateHandler(test_handler);

        NodeUpdateListener mock_listener = mock(NodeUpdateListener.class);

        test_layers[0].dataChanged(mock_listener);

        verify(test_handler, times(1)).dataChanged(mock_listener, test_layers[0]);
    }
}
