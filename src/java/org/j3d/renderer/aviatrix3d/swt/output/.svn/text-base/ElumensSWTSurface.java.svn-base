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

import org.eclipse.swt.widgets.Composite;

// Local imports
import org.j3d.opengl.swt.GLCanvas;

/**
 * Implementation of a surface using SWT that can render to an elumens output
 * device.
 * <p>
 *
 * <b>Note:</b> The lightweight flag is ignored currently.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 3.3 $
 */
public class ElumensSWTSurface extends BaseSWTSurface
    implements ElumensOutputDevice
{
    /**
     * Static constructor loads the native libraries that we need to interface
     * to this library with.
     */
    static
    {
        try
        {
            System.loadLibrary("spiclops");
            System.loadLibrary("elumens");
        }
        catch(Exception e)
        {
            System.out.println("Unable to locate Elumens libraries");
            System.out.print("Searching path: ");
            System.out.println(java.lang.System.getProperty("java.library.path"));

            e.printStackTrace();
        }
    }

    /**
     * Construct a surface that requires the given set of capabilities. This
     * surface acts as a standalone canvas.
     *
     * @param parent The parent component that this surface uses for the canvas
     * @param style The SWT style bits to use on the created canvas
     * @param caps A set of required capabilities for this canvas.
     */
    public ElumensSWTSurface(Composite parent,
                            int style,
                            GLCapabilities caps)
    {
        this(parent, style, caps, null, null);
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
    public ElumensSWTSurface(Composite parent,
                            int style,
                            GLCapabilities caps,
                            GLCapabilitiesChooser chooser)
    {
        this(parent, style, caps, chooser, null);
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
    public ElumensSWTSurface(Composite parent,
                            int style,
                            GLCapabilities caps,
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
     * @param chooser Custom algorithm for selecting one of the available
     *    GLCapabilities for the component;
     * @param lightweight If true, uses a GLJPanel (lightweight) JComponent,
     *   otherwise a GLCanvas. Note that setting this to true could negatively
     *   impact performance.
     */
    public ElumensSWTSurface(Composite parent,
                            int style,
                            GLCapabilities caps,
                            GLCapabilitiesChooser chooser,
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
    public ElumensSWTSurface(Composite parent,
                            int style,
                            GLCapabilities caps,
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
    public ElumensSWTSurface(Composite parent,
                            int style,
                            GLCapabilities caps,
                            GLCapabilitiesChooser chooser,
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
    public ElumensSWTSurface(Composite parent,
                            int style,
                            GLCapabilities caps,
                            BaseSurface sharedWith,
                            boolean lightweight)
    {
        this(parent, style, caps, null, sharedWith, lightweight);
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
    public ElumensSWTSurface(Composite parent,
                            int style,
                            GLCapabilities caps,
                            GLCapabilitiesChooser chooser,
                            BaseSurface sharedWith,
                            boolean lightweight)
    {
        super(sharedWith);

        init(parent, style, caps, chooser, lightweight);
    }

    //---------------------------------------------------------------
    // Methods defined by ElumensOutputDevice
    //---------------------------------------------------------------

    /**
     * Set the number of channels to display.  Calling this
     * will cause a reinitialization of renderer.
     *
     * @param channels The number of channels to render.
     */
    public void setNumberOfChannels(int channels)
    {
        ((ElumensRenderingProcessor)canvasRenderer).setNumberOfChannels(channels);
    }

    /**
     * Set the channel lens position.
     *
     * @param channel The ID of the channel(s) to affect
     * @param x The x position
     * @param y The y position
     * @param z The z position
     */
    public void setChannelLensPosition(int channel, float x, float y, float z)
    {
        ((ElumensRenderingProcessor)canvasRenderer).setChanLensPosition(channel,x,y,z);
    }

    /**
     * Set the channel eye position.
     *
     * @param channel The ID of the channel(s) to affect
     * @param x The x position
     * @param y The y position
     * @param z The z position
     */
    public void setChannelEyePosition(int channel, float x, float y, float z)
    {
        ((ElumensRenderingProcessor)canvasRenderer).setChanEyePosition(channel,x,y,z);
    }

    /**
     * Set the screen orientation.  Allows the project to rotated in software
     * for different hardware setups.
     *
     * @param r The roll
     * @param p The pitch
     * @param v The yaw
     */
    public void setScreenOrientation(double r, double p, double v)
    {
        ((ElumensRenderingProcessor)canvasRenderer).setScreenOrientation(r,p,v);
    }

    /**
     * Set the channel size in pixels.
     *
     * @param channel The ID of the channel(s) to affect
     * @param height The height in pixels
     * @param width The width in pixels
     */
    public void setChannelSize(int channel, int height, int width)
    {
        ((ElumensRenderingProcessor)canvasRenderer).setChanSize(channel,height,width);
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
                      GLCapabilities caps,
                      GLCapabilitiesChooser chooser,
                      boolean lightweight)
    {
        GLContext shared_context = null;

        if(sharedSurface != null)
            shared_context = sharedSurface.getGLContext();

        swtCanvas = new GLCanvas(parent, style, caps, chooser, shared_context);
        swtCanvas.addControlListener(resizer);

        canvas = swtCanvas.getGLDrawable();
        canvasContext = swtCanvas.getGLContext();

        canvasRenderer = new ElumensRenderingProcessor(canvasContext, this);

        init();
    }
}
