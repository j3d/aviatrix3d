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
 * Unit tests for the buffer data holder
 *
 * @author justin
 */
public class BufferSetupDataTest
{
    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        BufferSetupData class_under_test = new BufferSetupData();

        assertEquals(class_under_test.getDepthBits(), 16, "Wrong default depth buffer");
        assertEquals(class_under_test.getStencilBits(), 0, "Wrong stencil bit count");
        assertEquals(class_under_test.getNumAASamples(), 0, "Wrong FSAA sample count");
        assertEquals(class_under_test.getNumRenderTargets(), 0, "Wrong render target count");
        assertEquals(class_under_test.getRenderTargetIndex(), 0, "Wrong render target index");
        assertFalse(class_under_test.useFloatingPointColorBuffer(), "Should not use FP buffers by default");
        assertFalse(class_under_test.useUnclampedColorBuffer(), "Should clamp colour buffer by default");
    }

    @Test(groups = "unit")
    public void testDepthBits() throws Exception
    {
        final int TEST_DEPTH_SIZE = 8;

        BufferSetupData class_under_test = new BufferSetupData();
        class_under_test.setDepthBits(TEST_DEPTH_SIZE);

        assertEquals(class_under_test.getDepthBits(), TEST_DEPTH_SIZE, "Depth bits not set properly");
    }

    @Test(groups = "unit")
    public void testStencilBits() throws Exception
    {
        final int TEST_STENCIL_SIZE = 8;

        BufferSetupData class_under_test = new BufferSetupData();
        class_under_test.setStencilBits(TEST_STENCIL_SIZE);

        assertEquals(class_under_test.getStencilBits(), TEST_STENCIL_SIZE, "Stencil bits not set properly");
    }

    @Test(groups = "unit")
    public void testFSAASamples() throws Exception
    {
        final int TEST_SAMPLE_SIZE = 4;

        BufferSetupData class_under_test = new BufferSetupData();
        class_under_test.setNumAASamples(TEST_SAMPLE_SIZE);

        assertEquals(class_under_test.getNumAASamples(), TEST_SAMPLE_SIZE, "FSAA sample count not set properly");
    }

    @Test(groups = "unit")
    public void testRenderTargetIndex() throws Exception
    {
        final int TEST_TARGET_INDEX = 6;

        BufferSetupData class_under_test = new BufferSetupData();
        class_under_test.setRenderTargetIndex(TEST_TARGET_INDEX);

        assertEquals(class_under_test.getRenderTargetIndex(),
                     TEST_TARGET_INDEX,
                     "Render target index not set properly");
    }

    @Test(groups = "unit")
    public void testNumRenderTarget() throws Exception
    {
        final int TEST_TARGET_COUNT = 7;

        BufferSetupData class_under_test = new BufferSetupData();
        class_under_test.setNumRenderTargets(TEST_TARGET_COUNT);

        assertEquals(class_under_test.getNumRenderTargets(),
                     TEST_TARGET_COUNT,
                     "Render target count not set properly");
    }

    @Test(groups = "unit")
    public void testFPColorBuffer() throws Exception
    {
        BufferSetupData class_under_test = new BufferSetupData();
        final boolean INITIAL_STATE = class_under_test.useFloatingPointColorBuffer();


        class_under_test.enableFloatingPointColorBuffer(!INITIAL_STATE);

        assertEquals(class_under_test.useFloatingPointColorBuffer(),
                     !INITIAL_STATE,
                     "Colour buffer type not set properly");
    }

    @Test(groups = "unit")
    public void testClampColorBuffer() throws Exception
    {
        BufferSetupData class_under_test = new BufferSetupData();
        final boolean INITIAL_STATE = class_under_test.useUnclampedColorBuffer();


        class_under_test.enableUnclampedColorBuffer(!INITIAL_STATE);

        assertEquals(class_under_test.useUnclampedColorBuffer(),
                     !INITIAL_STATE,
                     "Colour buffer clamp not set properly");
    }
}
