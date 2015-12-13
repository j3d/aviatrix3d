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

import org.j3d.aviatrix3d.GraphicsRenderingCapabilities;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLCapabilitiesImmutable;
import com.jogamp.opengl.GLProfile;

/**
 * Internal utility class for manipulating GLCapabilities from one form to another
 *
 * @author justin
 */
public class CapabilitiesUtils
{
    /**
     * Utility method to take the AV3D rendering capabilities and convert it into
     * something that JOGL uses. Allows us to hide JOGL-specific initialisation
     * from the end user so that they don't need to directly import anything.
     *
     * @param input The source capabilities to test.
     * @return A non-null converted form of the capabilities.
     */
    public static GLCapabilities convertCapabilities(GraphicsRenderingCapabilities input, GLProfile profile)
    {
        GLCapabilities retval = new GLCapabilities(profile);
        retval.setDoubleBuffered(input.doubleBuffered);

        retval.setRedBits(input.redBits);
        retval.setGreenBits(input.greenBits);
        retval.setBlueBits(input.blueBits);
        retval.setAlphaBits(input.alphaBits);

        retval.setDepthBits(input.depthBits);
        retval.setStencilBits(input.stencilBits);

        retval.setAccumRedBits(input.accumRedBits);
        retval.setAccumGreenBits(input.accumGreenBits);
        retval.setAccumBlueBits(input.accumBlueBits);
        retval.setAccumAlphaBits(input.accumAlphaBits);

        retval.setBackgroundOpaque(input.backgroundOpaque);
        retval.setTransparentRedValue(input.transparentValueRed);
        retval.setTransparentGreenValue(input.transparentValueGreen);
        retval.setTransparentBlueValue(input.transparentValueBlue);
        retval.setTransparentAlphaValue(input.transparentValueAlpha);

        retval.setSampleBuffers(input.useSampleBuffers);
        retval.setNumSamples(input.numSamples);

        return retval;
    }

    /**
     * Utility method to take the JOGL rendering capabilities and convert it into
     * something that AV3D uses. Allows us to hide JOGL-specific initialisation
     * from the end user so that they don't need to directly import anything.
     *
     * @param input The source capabilities to test.
     * @return A non-null converted form of the capabilities.
     */
    static GraphicsRenderingCapabilities convertCapabilities(GLCapabilitiesImmutable input)
    {
        GraphicsRenderingCapabilities retval = new GraphicsRenderingCapabilities();
        retval.doubleBuffered = input.getDoubleBuffered();

        retval.redBits = input.getRedBits();
        retval.greenBits = input.getGreenBits();
        retval.blueBits = input.getBlueBits();
        retval.alphaBits = input.getAlphaBits();

        retval.depthBits = input.getDepthBits();
        retval.stencilBits = input.getStencilBits();

        retval.accumRedBits = input.getAccumRedBits();
        retval.accumGreenBits = input.getAccumGreenBits();
        retval.accumBlueBits = input.getAccumBlueBits();
        retval.accumAlphaBits = input.getAccumAlphaBits();

        retval.backgroundOpaque = input.isBackgroundOpaque();
        retval.transparentValueRed = input.getTransparentRedValue();
        retval.transparentValueGreen = input.getTransparentGreenValue();
        retval.transparentValueBlue = input.getTransparentBlueValue();
        retval.transparentValueAlpha = input.getTransparentAlphaValue();

        retval.useSampleBuffers = input.getSampleBuffers();
        retval.numSamples = input.getNumSamples();

        return retval;
    }
}
