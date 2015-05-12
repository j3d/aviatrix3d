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

import org.j3d.util.ErrorReporter;
import org.j3d.util.I18nManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import org.j3d.aviatrix3d.InvalidWriteTimingException;
import org.j3d.aviatrix3d.Layer;
import org.j3d.aviatrix3d.pipeline.RenderableRequestData;
import org.j3d.aviatrix3d.pipeline.audio.AudioRenderPipeline;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsRenderPipeline;
import org.j3d.aviatrix3d.rendering.LayerCullable;

/**
 * Unit tests for single-threaded rendering.
 *
 * @author justin
 */
public class SingleDisplayCollectionTest
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

        // Now quickly check that putting in invalid arguments doesn't
        // cause a crash.
        class_under_test.getLayers(null);
        class_under_test.getLayers(new Layer[0]);
    }

    @Test(groups = "unit", dependsOnMethods = "testLayerManagement")
    public void testAddLayerAfterPipelinesSet() throws Exception
    {
        GraphicsRenderPipeline mock_graphics_pipeline = mock(GraphicsRenderPipeline.class);
        AudioRenderPipeline mock_audio_pipeline = mock(AudioRenderPipeline.class);

        Layer[] test_layers = { new TestLayer() };

        // Use the constructor to set pipelines this time, just to be different.
        SingleDisplayCollection class_under_test =
            new SingleDisplayCollection(mock_graphics_pipeline, mock_audio_pipeline);
        class_under_test.setLayers(test_layers, 1);

        verify(mock_graphics_pipeline, times(1)).setRenderableLayers(test_layers, 1);
        verify(mock_audio_pipeline, times(1)).setRenderableLayers(test_layers, 1);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testLayerAddSizeMismatch() throws Exception
    {
        Layer[] test_layers = { new TestLayer() };

        SingleDisplayCollection class_under_test = new SingleDisplayCollection();
        class_under_test.setLayers(test_layers, test_layers.length + 1);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testLayerAddNullArray() throws Exception
    {
        SingleDisplayCollection class_under_test = new SingleDisplayCollection();
        class_under_test.setLayers(null, 1);
    }

    @Test(groups = "unit")
    public void testGraphicsPipelineRendering() throws Exception
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
        verify(mock_pipeline, times(1)).render();
    }

    @Test(groups = "unit")
    public void testGraphicsPipelineDisplayOnly() throws Exception
    {
        Layer[] test_layers = { new TestLayer() };

        SingleDisplayCollection class_under_test = new SingleDisplayCollection();
        class_under_test.setLayers(test_layers, 1);

        GraphicsRenderPipeline mock_pipeline = mock(GraphicsRenderPipeline.class);
        when(mock_pipeline.displayOnly()).thenReturn(true);

        class_under_test.addPipeline(mock_pipeline);
        class_under_test.setEnabled(true);

        assertTrue(class_under_test.displayOnly(), "Did not process anything");
        verify(mock_pipeline, times(1)).setRequestData(any(RenderableRequestData.class));
        verify(mock_pipeline, times(1)).displayOnly();
    }

    @Test(groups = "unit")
    public void testAudioPipelineRendering() throws Exception
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
        verify(mock_pipeline, times(1)).render();
    }

    @Test(groups = "unit")
    public void testAudioPipelineDisplayOnly() throws Exception
    {
        Layer[] test_layers = { new TestLayer() };

        SingleDisplayCollection class_under_test = new SingleDisplayCollection();
        class_under_test.setLayers(test_layers, 1);

        AudioRenderPipeline mock_pipeline = mock(AudioRenderPipeline.class);
        when(mock_pipeline.displayOnly()).thenReturn(true);

        class_under_test.addPipeline(mock_pipeline);

        // Check the layers got sent....

        class_under_test.setEnabled(true);

        assertTrue(class_under_test.displayOnly(), "Did not process anything");
        verify(mock_pipeline, times(1)).setRequestData(any(RenderableRequestData.class));
        verify(mock_pipeline, times(1)).displayOnly();
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

    @Test(groups = "unit", expectedExceptions = InvalidWriteTimingException.class)
    public void testCantSetLayersWhenLayerWriteDisabled() throws Exception
    {
        Layer[] test_layers = { new TestLayer() };

        SingleDisplayCollection class_under_test = new SingleDisplayCollection();
        class_under_test.setEnabled(true);
        class_under_test.enableLayerChange(false);
        class_under_test.setLayers(test_layers, 1);
    }

    @Test(groups = "unit", expectedExceptions = IllegalStateException.class)
    public void testAddPipelineWhenLayerWriteDisabled() throws Exception
    {
        AudioRenderPipeline mock_pipeline = mock(AudioRenderPipeline.class);

        SingleDisplayCollection class_under_test = new SingleDisplayCollection();
        class_under_test.setEnabled(true);
        class_under_test.enableLayerChange(false);
        class_under_test.addPipeline(mock_pipeline);
    }

    @Test(groups = "unit")
    public void testRemovePipelines() throws Exception
    {
        GraphicsRenderPipeline mock_graphics_pipeline = mock(GraphicsRenderPipeline.class);
        AudioRenderPipeline mock_audio_pipeline = mock(AudioRenderPipeline.class);

        Layer[] test_layers = { new TestLayer() };

        // Use the constructor to set pipelines this time, just to be different.
        SingleDisplayCollection class_under_test =
            new SingleDisplayCollection(mock_graphics_pipeline, mock_audio_pipeline);
        class_under_test.setLayers(test_layers, 1);

        class_under_test.removePipeline(mock_graphics_pipeline);

        verify(mock_graphics_pipeline, times(1)).setRenderableLayers(null, 0);
        verify(mock_audio_pipeline, never()).setRenderableLayers(null, 0);

        class_under_test.removePipeline(mock_audio_pipeline);

        verify(mock_graphics_pipeline, times(1)).setRenderableLayers(null, 0);
        verify(mock_audio_pipeline, times(1)).setRenderableLayers(null, 0);
    }

    @Test(groups = "unit", expectedExceptions = IllegalStateException.class)
    public void testRemovePipelineWhenLayerWriteDisabled() throws Exception
    {
        AudioRenderPipeline mock_pipeline = mock(AudioRenderPipeline.class);

        SingleDisplayCollection class_under_test = new SingleDisplayCollection();
        class_under_test.setEnabled(true);
        class_under_test.addPipeline(mock_pipeline);

        // Just to make sure that the add really happened and didn't throw the
        // exception that we are trying to catch.
        verify(mock_pipeline, times(1)).setRenderableLayers(any(LayerCullable[].class), anyInt());

        // Now change the write state and attempt to remove it
        class_under_test.enableLayerChange(false);
        class_under_test.removePipeline(mock_pipeline);
    }

    @Test(groups = "unit")
    public void testShutdown() throws Exception
    {
        GraphicsRenderPipeline mock_graphics_pipeline = mock(GraphicsRenderPipeline.class);
        AudioRenderPipeline mock_audio_pipeline = mock(AudioRenderPipeline.class);

        SingleDisplayCollection class_under_test = new SingleDisplayCollection();
        class_under_test.setEnabled(true);
        class_under_test.addPipeline(mock_graphics_pipeline);
        class_under_test.addPipeline(mock_audio_pipeline);

        class_under_test.shutdown();

        verify(mock_graphics_pipeline, times(1)).halt();
        verify(mock_audio_pipeline, times(1)).halt();

        assertTrue(class_under_test.isDisposed(), "A shut down pipeline should be disposed");

        // Call shutdown again and make sure that halt() is not called a
        // second time on the pipelines. The times() should still be at
        // 1. If the shutdown went through, it would fail as times()
        // will have a value of 2.

        class_under_test.shutdown();

        verify(mock_graphics_pipeline, times(1)).halt();
        verify(mock_audio_pipeline, times(1)).halt();
    }

    @Test(groups = "unit")
    public void testHalt() throws Exception
    {
        GraphicsRenderPipeline mock_graphics_pipeline = mock(GraphicsRenderPipeline.class);
        AudioRenderPipeline mock_audio_pipeline = mock(AudioRenderPipeline.class);

        SingleDisplayCollection class_under_test = new SingleDisplayCollection();
        class_under_test.setEnabled(true);
        class_under_test.addPipeline(mock_graphics_pipeline);
        class_under_test.addPipeline(mock_audio_pipeline);

        class_under_test.halt();

        verify(mock_graphics_pipeline, times(1)).halt();
        verify(mock_audio_pipeline, times(1)).halt();

        assertFalse(class_under_test.isDisposed(), "Halt should not dispose the handler");

        // Call halt again and make sure that halt() is called a
        // second time on the pipelines. There should be no filtering
        // on the current state of the rest of the class

        class_under_test.halt();

        verify(mock_graphics_pipeline, times(2)).halt();
        verify(mock_audio_pipeline, times(2)).halt();
    }

    @Test(groups = "unit")
    public void testErrorReporterPropagation() throws Exception
    {
        GraphicsRenderPipeline mock_graphics_pipeline = mock(GraphicsRenderPipeline.class);
        AudioRenderPipeline mock_audio_pipeline = mock(AudioRenderPipeline.class);

        ErrorReporter test_reporter = mock(ErrorReporter.class);

        SingleDisplayCollection class_under_test = new SingleDisplayCollection();
        class_under_test.setEnabled(true);
        class_under_test.addPipeline(mock_graphics_pipeline);
        class_under_test.addPipeline(mock_audio_pipeline);

        class_under_test.setErrorReporter(test_reporter);

        verify(mock_graphics_pipeline, times(1)).setErrorReporter(test_reporter);
        verify(mock_audio_pipeline, times(1)).setErrorReporter(test_reporter);
    }
}
