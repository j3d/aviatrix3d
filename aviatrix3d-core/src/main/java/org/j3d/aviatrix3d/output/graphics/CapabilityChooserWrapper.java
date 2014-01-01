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

// External imports
import java.util.ArrayList;
import java.util.List;

import javax.media.nativewindow.CapabilitiesImmutable;
import javax.media.opengl.GLCapabilitiesChooser;
import javax.media.opengl.GLCapabilitiesImmutable;

// Local imports
import org.j3d.aviatrix3d.GraphicsRenderingCapabilities;
import org.j3d.aviatrix3d.GraphicsRenderingCapabilitiesChooser;

/**
 * Wrapper that converts from the JOGL capabilities chooser to the AV3D
 * version of it
 *
 * @author justin
 */
public class CapabilityChooserWrapper implements GLCapabilitiesChooser
{
    /** The end user-provider chooser that will be called when JOGL calls ours */
    private GraphicsRenderingCapabilitiesChooser userChooser;

    /** Construct a new instance of the chooser that wraps the given chooser */
    public CapabilityChooserWrapper(GraphicsRenderingCapabilitiesChooser chooser)
    {
        assert chooser != null : "Capabilities chooser cannot be null";
        userChooser = chooser;
    }

    // -----------------------------------------------------------------------
    // Methods defined by CapabilitiesChooser
    // -----------------------------------------------------------------------

    @Override
    public int chooseCapabilities(CapabilitiesImmutable desired,
                                  List<? extends CapabilitiesImmutable> available,
                                  int windowSystemRecommendedChoice)
    {
        if(!(desired instanceof GLCapabilitiesImmutable))
            throw new IllegalArgumentException("Cannot handle a non GLCapabilities for the desired input");

        GraphicsRenderingCapabilities av3d_desired = CapabilitiesUtils.convertCapabilities((GLCapabilitiesImmutable)desired);

        ArrayList<GraphicsRenderingCapabilities> av3d_available = new ArrayList<GraphicsRenderingCapabilities>();

        for(CapabilitiesImmutable caps: available)
        {
            if(!(caps instanceof GLCapabilitiesImmutable))
                throw new IllegalArgumentException("Cannot handle a non GLCapabilities in the available list");

            av3d_available.add(CapabilitiesUtils.convertCapabilities((GLCapabilitiesImmutable)caps));
        }

        return userChooser.chooseCapabilities(av3d_desired, av3d_available, windowSystemRecommendedChoice);
    }
}
