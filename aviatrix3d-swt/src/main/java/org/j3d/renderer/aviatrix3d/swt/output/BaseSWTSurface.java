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

package org.j3d.renderer.aviatrix3d.swt.output;

// External imports

import com.jogamp.opengl.*;

import com.jogamp.nativewindow.AbstractGraphicsDevice;
import com.jogamp.opengl.swt.GLCanvas;

import org.eclipse.swt.widgets.Composite;

// Local imports
import org.j3d.aviatrix3d.GraphicsRenderingCapabilities;
import org.j3d.aviatrix3d.GraphicsRenderingCapabilitiesChooser;
import org.j3d.aviatrix3d.output.graphics.BaseSurface;
import org.j3d.aviatrix3d.output.graphics.CapabilitiesUtils;
import org.j3d.aviatrix3d.output.graphics.CapabilityChooserWrapper;



/**
 * Extended base implementation of the basic drawable surface, but adding in
 * some SWT-specific features.
 * <p>
 *
 * <a href="http://www.eclipse.org/swt/">SWT</a> is an independent windowing
 * toolkit developed by IBM as part of the
 * <a href="http://www.eclipse.org/">Eclipse project</a>. It doesn't use AWT at
 * all. Note that to run this code we assume that you already have at least SWT
 * installed on your system, and probably even all of Eclipse.
 *
 * @author Justin Couch
 * @version $Revision: 3.4 $
 */
public abstract class BaseSWTSurface extends BaseSurface
{
    /** The SWT version of the OpenGL canvas */
    protected GLCanvas swtCanvas;

    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param sharedWith The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     */
    public BaseSWTSurface(BaseSurface sharedWith)
    {
        super(sharedWith);
    }

    @Override
    public Object getSurfaceObject()
    {
        return swtCanvas;
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Ask the final class to create the actual canvas now, based on the selected chooser and
     * caps that JOGL needs, rather than the AV3D-specific classes that the external caller
     * makes use of.
     *
     * @param parent The parent component that this surface uses for the canvas
     * @param style The SWT style bits to use on the created canvas
     * @param caps The capabilities to select for
     * @param chooser The optional chooser that will help select the required capabilities
     */
    protected abstract void initCanvas(Composite parent,
                                       int style,
                                       GLCapabilities caps,
                                       GLCapabilitiesChooser chooser);

    /**
     * Common internal initialisation for the constructors.
     *
     * @param parent The parent component that this surface uses for the canvas
     * @param style The SWT style bits to use on the created canvas
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     */
    protected void init(Composite parent,
                        int style,
                        GraphicsRenderingCapabilities caps,
                        GraphicsRenderingCapabilitiesChooser chooser)
    {
        GLDrawableFactory fac = GLDrawableFactory.getDesktopFactory();
        AbstractGraphicsDevice screen_device = fac.getDefaultDevice();
        GLProfile selected_profile = GLProfile.get(screen_device, GLProfile.GL2);

        GLCapabilities jogl_caps = CapabilitiesUtils.convertCapabilities(caps, selected_profile);
        GLCapabilitiesChooser jogl_chooser = chooser != null ? new CapabilityChooserWrapper(chooser) : null;

        initCanvas(parent, style, jogl_caps, jogl_chooser);
        initBasicDataStructures();
    }
}
