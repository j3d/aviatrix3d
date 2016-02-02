/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004-2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d.output.graphics;

// External imports
import com.jogamp.opengl.*;

import com.jogamp.nativewindow.AbstractGraphicsDevice;

// Local imports
import org.j3d.aviatrix3d.GraphicsRenderingCapabilities;
import org.j3d.aviatrix3d.GraphicsRenderingCapabilitiesChooser;

/**
 * Implementation of the most basic drawable surface extended to provide
 * AWT-specific features.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 3.7 $
 */
public abstract class BaseAWTSurface extends BaseSurface
{
    /** Flag indicating if we're a lightweight surface or not */
    protected final boolean lightweight;

    /**
     * Construct a surface shares it's GL context with the given surface. This
     * is useful for constructing multiple view displays of the same scene graph,
     * but from different viewing directions, such as in a CAD application.
     * <p>
     * If the sharedWith parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     *
     * @param sharedWith The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     * @param lightweight If true, uses a GLJPanel (lightweight) JComponent,
     *   otherwise a GLCanvas. Note that setting this to true could negatively
     *   impact performance.
     */
    protected BaseAWTSurface(BaseSurface sharedWith, boolean lightweight)
    {
        super(sharedWith);

        this.lightweight = lightweight;
    }

    //---------------------------------------------------------------
    // Methods defined by GraphicsOutputDevice
    //---------------------------------------------------------------

    @Override
    public Object getSurfaceObject()
    {
        // Since we know that the canvas is GLJPanel or GLCanvas, we can just
        // return the raw drawable here for casting.
        return canvas;
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Ask the final class to create the actual canvas now, based on the selected chooser and
     * caps that JOGL needs, rather than the AV3D-specific classes that the external caller
     * makes use of.
     *
     * @param caps The capabilities to select for
     * @param chooser The optional chooser that will help select the required capabilities
     */
    protected abstract void initCanvas(GLCapabilities caps, GLCapabilitiesChooser chooser);

    /**
     * Common internal initialisation for the constructors.
     *
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     */
    protected void init(GraphicsRenderingCapabilities caps, GraphicsRenderingCapabilitiesChooser chooser)
    {
        GLDrawableFactory fac = GLDrawableFactory.getDesktopFactory();
        AbstractGraphicsDevice screen_device = fac.getDefaultDevice();
        GLProfile selected_profile = GLProfile.get(screen_device, GLProfile.GL2);

        GLCapabilities jogl_caps = CapabilitiesUtils.convertCapabilities(caps, selected_profile);
        GLCapabilitiesChooser jogl_chooser = chooser != null ? new CapabilityChooserWrapper(chooser) : null;

        initCanvas(jogl_caps, jogl_chooser);
        initBasicDataStructures();
    }
}
