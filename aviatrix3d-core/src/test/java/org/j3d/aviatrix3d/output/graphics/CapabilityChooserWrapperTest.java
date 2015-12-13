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

import java.util.ArrayList;
import java.util.List;

import com.jogamp.nativewindow.Capabilities;
import com.jogamp.nativewindow.CapabilitiesImmutable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;

import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

import org.j3d.aviatrix3d.GraphicsRenderingCapabilities;
import org.j3d.aviatrix3d.GraphicsRenderingCapabilitiesChooser;

/**
 * Unit tests for the capability chooser wrapper
 *
 * @author justin
 */
public class CapabilityChooserWrapperTest
{
    @Test(groups = "unit", expectedExceptions = AssertionError.class)
    public void testBadConstruction() throws Exception
    {
        new CapabilityChooserWrapper(null);
    }

    @Test(groups = "unit")
    public void testSimpleConvert() throws Exception
    {
        final int TEST_SELECTION = 1;

        GraphicsRenderingCapabilitiesChooser mockUserChooser = mock(GraphicsRenderingCapabilitiesChooser.class);


        GLCapabilities testDesiredCaps = new GLCapabilities(GLProfile.getDefault());
        generateRandomCapabilities(testDesiredCaps);

        List<GLCapabilities> testAvailableList = new ArrayList<GLCapabilities>();

        for(int i = 0; i < 3; i++)
        {
            GLCapabilities caps = new GLCapabilities(GLProfile.getDefault());
            generateRandomCapabilities(caps);
            testAvailableList.add(caps);
        }

        CapabilityChooserWrapper classUnderTest = new CapabilityChooserWrapper(mockUserChooser);
        classUnderTest.chooseCapabilities(testDesiredCaps, testAvailableList, TEST_SELECTION);

        ArgumentCaptor<GraphicsRenderingCapabilities> desiredCaptor =
            ArgumentCaptor.forClass(GraphicsRenderingCapabilities.class);

        ArgumentCaptor<List> availableCaptor = ArgumentCaptor.forClass(List.class);

        verify(mockUserChooser, times(1)).chooseCapabilities(desiredCaptor.capture(),
                                                             availableCaptor.capture(),
                                                             eq(TEST_SELECTION));

        GraphicsRenderingCapabilities resultDesired = desiredCaptor.getValue();

        assertNotNull(resultDesired, "Didn't convert the desired capabilities object");
        checkEquals(testDesiredCaps, resultDesired, "Desired");

        List resultAvailable = availableCaptor.getValue();

        assertNotNull(resultAvailable, "No available list given");
        assertEquals(resultAvailable.size(), testAvailableList.size(), "Available list has wrong number of entries");

        for(int i = 0; i < resultAvailable.size(); i++)
        {
            checkEquals(testAvailableList.get(i), (GraphicsRenderingCapabilities)resultAvailable.get(i), "Available " + i);
        }
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidDesiredCapabilities() throws Exception
    {
        final int TEST_SELECTION = 1;

        GraphicsRenderingCapabilitiesChooser mockUserChooser = mock(GraphicsRenderingCapabilitiesChooser.class);

        Capabilities testDesiredCaps = new Capabilities();

        List<GLCapabilities> testAvailableList = new ArrayList<GLCapabilities>();

        CapabilityChooserWrapper classUnderTest = new CapabilityChooserWrapper(mockUserChooser);
        classUnderTest.chooseCapabilities(testDesiredCaps, testAvailableList, TEST_SELECTION);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidAvailableCapabilities() throws Exception
    {
        final int TEST_SELECTION = 1;

        GraphicsRenderingCapabilitiesChooser mockUserChooser = mock(GraphicsRenderingCapabilitiesChooser.class);

        GLCapabilities testDesiredCaps = new GLCapabilities(GLProfile.getDefault());
        generateRandomCapabilities(testDesiredCaps);

        List<CapabilitiesImmutable> testAvailableList = new ArrayList<>();

        GLCapabilities caps = new GLCapabilities(GLProfile.getDefault());
        generateRandomCapabilities(caps);
        testAvailableList.add(caps);

        testAvailableList.add(new Capabilities());

        CapabilityChooserWrapper classUnderTest = new CapabilityChooserWrapper(mockUserChooser);

        classUnderTest.chooseCapabilities(testDesiredCaps, testAvailableList, TEST_SELECTION);
    }

    private void checkEquals(GLCapabilities joglCaps, GraphicsRenderingCapabilities av3dCaps, String prefix)
    {
        // Check that the defaults are maintained for these ones.
        assertFalse(av3dCaps.useFloatingPointBuffers, prefix + "Floating point buffers requested");


        assertEquals(av3dCaps.doubleBuffered, joglCaps.getDoubleBuffered(), prefix + ": Double buffered flag ignored");
        assertEquals(av3dCaps.redBits, joglCaps.getRedBits(), prefix + ": Red bits not copied correctly");
        assertEquals(av3dCaps.greenBits, joglCaps.getGreenBits(), prefix + ": Green bits not copied correctly");
        assertEquals(av3dCaps.blueBits, joglCaps.getBlueBits(), prefix + ": Blue bits not copied correctly");
        assertEquals(av3dCaps.alphaBits, joglCaps.getAlphaBits(), prefix + ": Alpha bits not copied correctly");

        assertEquals(av3dCaps.depthBits, joglCaps.getDepthBits(), prefix + ": Depth bits not copied correctly");
        assertEquals(av3dCaps.stencilBits, joglCaps.getStencilBits(), prefix + ": stencil bits not copied correctly");

        assertEquals(av3dCaps.accumRedBits,
                     joglCaps.getAccumRedBits(),
                     prefix + ": Accumulation red bits not copied correctly");
        assertEquals(av3dCaps.accumGreenBits,
                     joglCaps.getAccumGreenBits(),
                     prefix + ": Accumulation green bits not copied correctly");
        assertEquals(av3dCaps.accumBlueBits,
                     joglCaps.getAccumBlueBits(),
                     prefix + ": Accumulation blue bits not copied correctly");
        assertEquals(av3dCaps.accumAlphaBits,
                     joglCaps.getAccumAlphaBits(),
                     prefix + ": Accumulation alpha bits not copied correctly");

        assertEquals(av3dCaps.backgroundOpaque, joglCaps.isBackgroundOpaque(), "Background transparency incorrect");
        assertEquals(av3dCaps.transparentValueRed,
                     joglCaps.getTransparentRedValue(),
                     prefix + ": Transparent red value not copied correctly");
        assertEquals(av3dCaps.transparentValueGreen,
                     joglCaps.getTransparentGreenValue(),
                     prefix + ": Transparent value bits not copied correctly");
        assertEquals(av3dCaps.transparentValueBlue,
                     joglCaps.getTransparentBlueValue(),
                     prefix + ": Transparent blue value not copied correctly");
        assertEquals(av3dCaps.transparentValueAlpha,
                     joglCaps.getTransparentAlphaValue(),
                     prefix + ": Transparent alpha value not copied correctly");

        assertEquals(av3dCaps.useSampleBuffers, joglCaps.getSampleBuffers(), prefix + ": Sample buffer flag not set");
        assertEquals(av3dCaps.numSamples, joglCaps.getNumSamples(), prefix + ": Sample count wrong");
    }

    /**
     * Convenience to take the input caps and generate a random set of values for the
     * various bit sizes.
     *
     * @param testCapabilities The capabilities object to mess with
     */
    private void generateRandomCapabilities(GLCapabilities testCapabilities)
    {
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
