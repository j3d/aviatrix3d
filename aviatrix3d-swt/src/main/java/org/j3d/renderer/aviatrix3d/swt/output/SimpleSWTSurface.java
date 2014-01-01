/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2006
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
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLCapabilitiesChooser;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLProfile;

import com.jogamp.opengl.swt.GLCanvas;
import org.eclipse.swt.widgets.Composite;

// Local imports
import org.j3d.aviatrix3d.GraphicsRenderingCapabilities;
import org.j3d.aviatrix3d.GraphicsRenderingCapabilitiesChooser;
import org.j3d.aviatrix3d.output.graphics.BaseSurface;
import org.j3d.aviatrix3d.output.graphics.CapabilitiesUtils;
import org.j3d.aviatrix3d.output.graphics.CapabilityChooserWrapper;
import org.j3d.aviatrix3d.output.graphics.StandardRenderingProcessor;

/**
 * Implementation of the most basic drawable surface using SWT.
 * <p>
 *
 * <b>Note:</b> The lightweight flag is ignored currently.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 3.4 $
 */
public class SimpleSWTSurface extends BaseSWTSurface
{
    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param parent The parent component that this surface uses for the canvas
     * @param style The SWT style bits to use on the created canvas
     * @param caps A set of required capabilities for this canvas.
     */
    public SimpleSWTSurface(Composite parent,
                            int style,
                            GraphicsRenderingCapabilities caps)
    {
        this(parent, style, caps, null, null, false);
    }

    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param parent The parent component that this surface uses for the canvas
     * @param style The SWT style bits to use on the created canvas
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     */
    public SimpleSWTSurface(Composite parent,
                            int style,
                            GraphicsRenderingCapabilities caps,
                            GraphicsRenderingCapabilitiesChooser chooser)
    {
        this(parent, style, caps, chooser, null, false);
    }

    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param parent The parent component that this surface uses for the canvas
     * @param style The SWT style bits to use on the created canvas
     * @param caps A set of required capabilities for this canvas.
     * @param lightweight If true, uses a GLJPanel (lightweight) JComponent,
     *   otherwise a GLCanvas. Note that setting this to true could negatively
     *   impact performance.
     */
    public SimpleSWTSurface(Composite parent,
                            int style,
                            GraphicsRenderingCapabilities caps,
                            boolean lightweight)
    {
        this(parent, style, caps, null, null, lightweight);
    }

    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param parent The parent component that this surface uses for the canvas
     * @param style The SWT style bits to use on the created canvas
     * @param caps A set of required capabilities for this canvas.
     * @param lightweight If true, uses a GLJPanel (lightweight) JComponent,
     *   otherwise a GLCanvas. Note that setting this to true could negatively
     *   impact performance.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     */
    public SimpleSWTSurface(Composite parent,
                            int style,
                            GraphicsRenderingCapabilities caps,
                            GraphicsRenderingCapabilitiesChooser chooser,
                            boolean lightweight)
    {
        this(parent, style, caps, chooser, null, lightweight);
    }

    /**
     * Construct a surface shares it's GL context with the given surface. This
     * is useful for constructing multiple view displays of the same scene graph,
     * but from different viewing directions, such as in a CAD application.
     * <p>
     * If the sharedWith parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     *
     * @param parent The parent component that this surface uses for the canvas
     * @param style The SWT style bits to use on the created canvas
     * @param caps A set of required capabilities for this canvas.
     * @param sharedWith The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     */
    public SimpleSWTSurface(Composite parent,
                            int style,
                            GraphicsRenderingCapabilities caps,
                            BaseSurface sharedWith)
    {
        this(parent, style, caps, null, sharedWith, false);
    }

    /**
     * Construct a surface shares it's GL context with the given surface. This
     * is useful for constructing multiple view displays of the same scene graph,
     * but from different viewing directions, such as in a CAD application.
     * <p>
     * If the sharedWith parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     *
     * @param parent The parent component that this surface uses for the canvas
     * @param style The SWT style bits to use on the created canvas
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     * @param sharedWith The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     */
    public SimpleSWTSurface(Composite parent,
                            int style,
                            GraphicsRenderingCapabilities caps,
                            GraphicsRenderingCapabilitiesChooser chooser,
                            BaseSurface sharedWith)
    {
        this(parent, style, caps, chooser, sharedWith, false);
    }

    /**
     * Construct a surface shares it's GL context with the given surface. This
     * is useful for constructing multiple view displays of the same scene graph,
     * but from different viewing directions, such as in a CAD application.
     * <p>
     * If the sharedWith parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     *
     * @param parent The parent component that this surface uses for the canvas
     * @param style The SWT style bits to use on the created canvas
     * @param caps A set of required capabilities for this canvas.
     * @param sharedWith The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     * @param lightweight If true, uses a GLJPanel (lightweight) JComponent,
     *   otherwise a GLCanvas. Note that setting this to true could negatively
     *   impact performance.
     */
    public SimpleSWTSurface(Composite parent,
                            int style,
                            GraphicsRenderingCapabilities caps,
                            BaseSurface sharedWith,
                            boolean lightweight)
    {
        super(sharedWith);

        init(parent, style, caps, null, lightweight);
    }

    /**
     * Construct a surface shares it's GL context with the given surface. This
     * is useful for constructing multiple view displays of the same scene graph,
     * but from different viewing directions, such as in a CAD application.
     * <p>
     * If the sharedWith parameter is null, then this is just treated as an
     * ordinary non-shared frame. The return flag will be set appropriately.
     *
     * @param parent The parent component that this surface uses for the canvas
     * @param style The SWT style bits to use on the created canvas
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     * @param sharedWith The surface that you'd like this surface to share
     *    the GL context with, if possible. May be null.
     * @param lightweight If true, uses a GLJPanel (lightweight) JComponent,
     *   otherwise a GLCanvas. Note that setting this to true could negatively
     *   impact performance.
     */
    public SimpleSWTSurface(Composite parent,
                            int style,
                            GraphicsRenderingCapabilities caps,
                            GraphicsRenderingCapabilitiesChooser chooser,
                            BaseSurface sharedWith,
                            boolean lightweight)
    {
        super(sharedWith);

        init(parent, style, caps, chooser, lightweight);
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Common internal initialisation for the constructors.
     *
     * @param parent The parent component that this surface uses for the canvas
     * @param style The SWT style bits to use on the created canvas
     * @param caps A set of required capabilities for this canvas.
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     * @param lightweight If true, uses a GLJPanel (lightweight) JComponent,
     *   otherwise a GLCanvas. Note that setting this to true could negatively
     *   impact performance.
     */
    private void init(Composite parent,
                      int style,
                      GraphicsRenderingCapabilities caps,
                      GraphicsRenderingCapabilitiesChooser chooser,
                      boolean lightweight)
    {
        GLContext shared_context = null;

        if(sharedSurface != null)
            shared_context = sharedSurface.getGLContext();

        GLCapabilities jogl_caps = CapabilitiesUtils.convertCapabilities(caps, GLProfile.getDefault());
        GLCapabilitiesChooser jogl_chooser = chooser != null ? new CapabilityChooserWrapper(chooser) : null;

        swtCanvas = new GLCanvas(parent, style, jogl_caps, jogl_chooser, shared_context);
        swtCanvas.addControlListener(resizer);

        canvas = swtCanvas.getDelegatedDrawable();
        canvasContext = swtCanvas.getContext();

        canvasRenderer =
            new StandardRenderingProcessor(canvasContext, this);

        init();
    }
}
