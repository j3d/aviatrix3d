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

package org.j3d.aviatrix3d.output.graphics;

import org.testng.annotations.Test;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;

import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import org.j3d.aviatrix3d.GraphicsRenderingCapabilities;

/**
 * Unit tests for the capabilities util class
 *
 * @author justin
 */
public class CapabilitiesUtilsTest
{
    @Test(groups = "unit")
    public void testToJOGLConversion() throws Exception
    {
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

        testCapabilities.useSampleBuffers = true;
        testCapabilities.numSamples = createRandomInt(4);

        GLCapabilities result = CapabilitiesUtils.convertCapabilities(testCapabilities, testProfile);

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

        assertTrue(result.getSampleBuffers(), "Sample buffer flag not set");
        assertEquals(result.getNumSamples(), testCapabilities.numSamples, "Sample count wrong");

    }

    @Test(groups = "unit")
    public void testFromJOGLConversion() throws Exception
    {
        GLCapabilities testCapabilities = new GLCapabilities(GLProfile.getDefault());
        testCapabilities.setDoubleBuffered(false);
        testCapabilities.setRedBits(createRandomInt(16));
        testCapabilities.setGreenBits(createRandomInt(16));
        testCapabilities.setBlueBits(createRandomInt(16));
        testCapabilities.setAlphaBits(createRandomInt(16));

        testCapabilities.setAccumRedBits(createRandomInt(16));
        testCapabilities.setAccumGreenBits(createRandomInt(16));
        testCapabilities.setAccumBlueBits(createRandomInt(16));
        testCapabilities.setAccumAlphaBits(createRandomInt(16));

        testCapabilities.setDepthBits(createRandomInt(16));
        testCapabilities.setStencilBits(createRandomInt(8));

        testCapabilities.setBackgroundOpaque(false);
        testCapabilities.setTransparentRedValue(createRandomInt(8));
        testCapabilities.setTransparentGreenValue(createRandomInt(8));
        testCapabilities.setTransparentBlueValue(createRandomInt(8));
        testCapabilities.setTransparentAlphaValue(createRandomInt(8));

        testCapabilities.setSampleBuffers(true);
        testCapabilities.setNumSamples(createRandomInt(4));

        GraphicsRenderingCapabilities result = CapabilitiesUtils.convertCapabilities(testCapabilities);

        assertNotNull(result, "No capabilities were created");

        // Check that the defaults are maintained for these ones.
        assertFalse(result.useFloatingPointBuffers, "Floating point buffers requested");


        assertFalse(result.doubleBuffered, "Double buffered flag ignored");
        assertEquals(result.redBits, testCapabilities.getRedBits(), "Red bits not copied correctly");
        assertEquals(result.greenBits, testCapabilities.getGreenBits(), "Green bits not copied correctly");
        assertEquals(result.blueBits, testCapabilities.getBlueBits(), "Blue bits not copied correctly");
        assertEquals(result.alphaBits, testCapabilities.getAlphaBits(), "Alpha bits not copied correctly");

        assertEquals(result.depthBits, testCapabilities.getDepthBits(), "Depth bits not copied correctly");
        assertEquals(result.stencilBits, testCapabilities.getStencilBits(), "stencil bits not copied correctly");

        assertEquals(result.accumRedBits,
                     testCapabilities.getAccumRedBits(),
                     "Accumulation red bits not copied correctly");
        assertEquals(result.accumGreenBits,
                     testCapabilities.getAccumGreenBits(),
                     "Accumulation green bits not copied correctly");
        assertEquals(result.accumBlueBits,
                     testCapabilities.getAccumBlueBits(),
                     "Accumulation blue bits not copied correctly");
        assertEquals(result.accumAlphaBits,
                     testCapabilities.getAccumAlphaBits(),
                     "Accumulation alpha bits not copied correctly");

        assertFalse(result.backgroundOpaque, "Background is not transparent");
        assertEquals(result.transparentValueRed,
                     testCapabilities.getTransparentRedValue(),
                     "Transparent red value not copied correctly");
        assertEquals(result.transparentValueGreen,
                     testCapabilities.getTransparentGreenValue(),
                     "Transparent value bits not copied correctly");
        assertEquals(result.transparentValueBlue,
                     testCapabilities.getTransparentBlueValue(),
                     "Transparent blue value not copied correctly");
        assertEquals(result.transparentValueAlpha,
                     testCapabilities.getTransparentAlphaValue(),
                     "Transparent alpha value not copied correctly");

        assertTrue(result.useSampleBuffers, "Sample buffer flag not set");
        assertEquals(result.numSamples, testCapabilities.getNumSamples(), "Sample count wrong");

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
