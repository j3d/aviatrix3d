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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import org.j3d.aviatrix3d.Layer;
import org.j3d.aviatrix3d.pipeline.RenderableRequestData;
import org.j3d.aviatrix3d.pipeline.audio.AudioRenderPipeline;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsRenderPipeline;

/**
 * Unit tests for single-threaded rendering.
 *
 * @author justin
 */
public class SingleDisplayCollectionTest
{
    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        SingleDisplayCollection class_under_test = new SingleDisplayCollection();
        assertEquals(class_under_test.numLayers(), 0, "Should not have any layers yet");
        assertFalse(class_under_test.isDisposed(), "Newly created should not be disposed");
        assertFalse(class_under_test.isEnabled(), "Should not be enabled by default");
    }

    @Test(groups = "unit")
    public void testLayerManagement() throws Exception
    {
        Layer[] test_layers = { new TestLayer() };

        SingleDisplayCollection class_under_test = new SingleDisplayCollection();
        class_under_test.setLayers(test_layers, 1);

        assertEquals(class_under_test.numLayers(), 1, "Didn't register the layer properly");

        Layer[] fetch_result = new Layer[1];
        class_under_test.getLayers(fetch_result);

        assertSame(test_layers[0], fetch_result[0], "Didn't return our original layer");
    }

    @Test(groups = "unit")
    public void testGraphicsPipelineHandling() throws Exception
    {
        Layer[] test_layers = { new TestLayer() };

        SingleDisplayCollection class_under_test = new SingleDisplayCollection();
        class_under_test.setLayers(test_layers, 1);

        GraphicsRenderPipeline mock_pipeline = mock(GraphicsRenderPipeline.class);
        when(mock_pipeline.render()).thenReturn(true);

        class_under_test.addPipeline(mock_pipeline);
        class_under_test.setEnabled(true);

        assertTrue(class_under_test.process(), "Did not process anything");
        verify(mock_pipeline, times(1)).setRequestData(any(RenderableRequestData.class));
    }

    @Test(groups = "unit")
    public void testAudioPipelineHandling() throws Exception
    {
        Layer[] test_layers = { new TestLayer() };

        SingleDisplayCollection class_under_test = new SingleDisplayCollection();
        class_under_test.setLayers(test_layers, 1);

        AudioRenderPipeline mock_pipeline = mock(AudioRenderPipeline.class);
        when(mock_pipeline.render()).thenReturn(true);

        class_under_test.addPipeline(mock_pipeline);

        // Check the layers got sent....

        class_under_test.setEnabled(true);

        assertTrue(class_under_test.process(), "Did not process anything");
        verify(mock_pipeline, times(1)).setRequestData(any(RenderableRequestData.class));
    }

    @Test(groups = "unit")
    public void testNoPipelineProcessingWhenDisabled() throws Exception
    {
        Layer[] test_layers = { new TestLayer() };

        SingleDisplayCollection class_under_test = new SingleDisplayCollection();
        class_under_test.setLayers(test_layers, 1);

        GraphicsRenderPipeline mock_graphics_pipeline = mock(GraphicsRenderPipeline.class);
        AudioRenderPipeline mock_audio_pipeline = mock(AudioRenderPipeline.class);
        when(mock_graphics_pipeline.render()).thenReturn(true);
        when(mock_audio_pipeline.render()).thenReturn(true);

        class_under_test.addPipeline(mock_graphics_pipeline);
        class_under_test.addPipeline(mock_audio_pipeline);
        class_under_test.setEnabled(false);

        assertFalse(class_under_test.process(), "Should not process anything as a result");

        verify(mock_graphics_pipeline, never()).render();
        verify(mock_audio_pipeline, never()).render();
    }

}
