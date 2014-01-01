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

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLProfile;

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

        @Override
        public RenderingProcessor createRenderingProcessor(GLContext ctx)
        {
            return mockRenderingProcessor;
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

    @Test(groups = "unit")
    public void testCapabilitiesConversion() throws Exception
    {
        BaseSurface classUnderTest = new TestSurface();

        GLProfile testProfile = GLProfile.getMinimum(true);

        GraphicsRenderingCapabilities testCapabilities = new GraphicsRenderingCapabilities();
        testCapabilities.doubleBuffered = false;
        testCapabilities.redBits = createRandomInt(16);
        testCapabilities.greenBits = createRandomInt(16);
        testCapabilities.blueBits = createRandomInt(16);
        testCapabilities.alphaBits = createRandomInt(16);

        testCapabilities.accumRedBits = createRandomInt(16);
        testCapabilities.accumGreenBits = createRandomInt(16);
        testCapabilities.accumBlueBits = createRandomInt(16);
        testCapabilities.accumAlphaBits = createRandomInt(16);

        testCapabilities.depthBits = createRandomInt(16);
        testCapabilities.stencilBits = createRandomInt(8);

        testCapabilities.backgroundOpaque = false;
        testCapabilities.transparentValueRed = createRandomInt(8);
        testCapabilities.transparentValueGreen = createRandomInt(8);
        testCapabilities.transparentValueBlue = createRandomInt(8);
        testCapabilities.transparentValueAlpha = createRandomInt(8);

        GLCapabilities result = classUnderTest.convertCapabilities(testCapabilities, testProfile);

        assertNotNull(result, "No capabilities were created");

        // Check that the defaults are maintained for these ones.
        assertTrue(result.getHardwareAccelerated(), "Default not hardware accelerated");
        assertFalse(result.getStereo(), "Default has stereo enabled");
        assertFalse(result.isFBO(), "Default should not be an FBO");
        assertFalse(result.isPBuffer(), "Default should not be an FBO");

        assertFalse(result.getDoubleBuffered(), "Double buffered flag ignored");
        assertEquals(result.getRedBits(), testCapabilities.redBits, "Red bits not copied correctly");
        assertEquals(result.getGreenBits(), testCapabilities.greenBits, "Green bits not copied correctly");
        assertEquals(result.getBlueBits(), testCapabilities.blueBits, "Blue bits not copied correctly");
        assertEquals(result.getAlphaBits(), testCapabilities.alphaBits, "Alpha bits not copied correctly");

        assertEquals(result.getDepthBits(), testCapabilities.depthBits, "Depth bits not copied correctly");
        assertEquals(result.getStencilBits(), testCapabilities.stencilBits, "stencil bits not copied correctly");

        assertEquals(result.getAccumRedBits(),
                     testCapabilities.accumRedBits,
                     "Accumulation red bits not copied correctly");
        assertEquals(result.getAccumGreenBits(),
                     testCapabilities.accumGreenBits,
                     "Accumulation green bits not copied correctly");
        assertEquals(result.getAccumBlueBits(),
                     testCapabilities.accumBlueBits,
                     "Accumulation blue bits not copied correctly");
        assertEquals(result.getAccumAlphaBits(),
                     testCapabilities.accumAlphaBits,
                     "Accumulation alpha bits not copied correctly");

        assertFalse(result.isBackgroundOpaque(), "Background is not transparent");
        assertEquals(result.getTransparentRedValue(),
                     testCapabilities.transparentValueRed,
                     "Transparent red value not copied correctly");
        assertEquals(result.getTransparentGreenValue(),
                     testCapabilities.transparentValueGreen,
                     "Transparent value bits not copied correctly");
        assertEquals(result.getTransparentBlueValue(),
                     testCapabilities.transparentValueBlue,
                     "Transparent blue value not copied correctly");
        assertEquals(result.getTransparentAlphaValue(),
                     testCapabilities.transparentValueAlpha,
                     "Transparent alpha value not copied correctly");
    }

    /**
     * Convenience function to create random ints between 0 and max  for testing.
     *
     * @param max The max value in the range to select
     * @return An integer [0, max]
     */
    private int createRandomInt(int max)
    {
        return (int)(Math.random() * max);
    }
}
