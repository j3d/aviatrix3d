/*
 * **************************************************************************
 *                        Copyright j3d.org (c) 2000 - 2014
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

package org.j3d.aviatrix3d.output.graphics;

import org.j3d.aviatrix3d.GraphicsRenderingCapabilities;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsResizeListener;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLProfile;

import static org.testng.Assert.*;

/**
 * Unit test for the common surface class
 *
 * @author justin
 */
public class BaseSurfaceTest
{
    @Mock
    private RenderingProcessor mockRenderingProcessor;

    private class TestSurface extends BaseSurface
    {
        TestSurface()
        {
            super(null);
        }

        @Override
        public void addGraphicsResizeListener(GraphicsResizeListener l)
        {

        }

        @Override
        public void removeGraphicsResizeListener(GraphicsResizeListener l)
        {

        }

        @Override
        public Object getSurfaceObject()
        {
            return null;
        }
    }

    @BeforeMethod(groups = "unit")
    public void setupTest() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        BaseSurface classUnderTest = new TestSurface();

        assertEquals(classUnderTest.getAlphaTestCutoff(), 1.0f, "Alpha cutoff default is set");
        assertEquals(classUnderTest.getStereoEyeSeparation(), 0.0f, "Eye separation is set");
        assertFalse(classUnderTest.isShared(), "Should not be shared by default");
        assertFalse(classUnderTest.isDisposed(), "Newly created should not be disposed");
        assertFalse(classUnderTest.isStereoAvailable(), "Stereo should not be configured");
        assertEquals(classUnderTest.getStereoRenderingPolicy(), GraphicsOutputDevice.NO_STEREO, "Stereo policy not NONE");
        assertFalse(classUnderTest.isQuadStereoAvailable(), "Quad stereo shouldn't be available");
        assertFalse(classUnderTest.isTwoPassTransparentEnabled(), "Two pass transparency enabled");
    }
}
