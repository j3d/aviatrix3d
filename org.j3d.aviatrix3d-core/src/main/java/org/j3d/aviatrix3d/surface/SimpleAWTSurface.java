/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d.surface;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.*;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;

/**
 * Interface representing the output of a render pipeline.
 * <p>
 *
 * The output may be any of the traditional types: pBuffer, screen or memory
 * or any non-traditional type like haptic devices, network streams etc.
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class SimpleAWTSurface
    implements DrawableSurface, GLEventListener
{
    /** Error message when the scene does not have a valid viewpoint */
    private static final String INVALID_VIEW_MSG =
        "The path does not terminate in a Viewpoint node.";

    /** Error message when the scene does not have a valid background */
    private static final String INVALID_BACKGROUND_MSG =
        "The path does not terminate in a Background node.";

    /** Error message when the scene does not have a valid fog */
    private static final String INVALID_FOG_MSG =
        "The path does not terminate in a Fog node.";

    /** The current viewpoint instance */
    private Viewpoint currentViewpoint;

    /** Path to the current viewpoint */
    private SceneGraphPath currentViewpointPath;

    /** The current fog instance */
    private Fog currentFog;

    /** Path to the current fog */
    private SceneGraphPath currentFogPath;

    /** The current background instance */
    private Background currentBackground;

    /** Path to the current background */
    private SceneGraphPath currentBackgroundPath;

    /** The current clear colour */
    private float[] clearColor;

    /** Current view environment */
    private ViewEnvironment viewEnvironment;

    /** Time of the last call. Used to implement frame cycle timing */
    private long lastRenderTime;

    /** The real canvas that we draw to */
    private GLCanvas canvas;

    /** Flag to say that colour needs to be reset this frame */
    private boolean resetColor;

    /**
     * Construct a surface that requires the given set of capabilities.
     *
     * @param caps A set of required capabilities for this canvas.
     */
    public SimpleAWTSurface(GLCapabilities caps)
    {
        clearColor = new float[4];

        GLDrawableFactory fac = GLDrawableFactory.getFactory();
        canvas = fac.createGLCanvas(caps);
        canvas.addGLEventListener(this);

        resetColor = false;
    }

    //---------------------------------------------------------------
    // Methods defined by DrawableSurface
    //---------------------------------------------------------------

    /**
     * Set the viewpoint path that should be applied to the current surface.
     * The output drawn will be a combination of this information and that
     * of the view environment.
     *
     * @param viewPath The path to the active viewpoint
     * @throws IllegalArgumentException The path is not to a viewpoint instance
     */
    public void setActiveView(SceneGraphPath viewPath)
        throws IllegalArgumentException
    {
        Leaf l = viewPath.getTerminalNode();
        if(l != null && !(l instanceof Viewpoint))
            throw new IllegalArgumentException(INVALID_VIEW_MSG);

        currentViewpoint = (Viewpoint)l;
        currentViewpointPath = viewPath;
    }

    /**
     * Set the fog that should be applied to the current surface.
     * The output drawn will be a combination of this information and that
     * of the view environment.
     *
     * @param fogPath The path to the active fog node
     * @throws IllegalArgumentException The path is not to a fog instance
     */
    public void setActiveFog(SceneGraphPath fogPath)
        throws IllegalArgumentException
    {
        Leaf l = fogPath.getTerminalNode();
        if(l != null && !(l instanceof Fog))
            throw new IllegalArgumentException(INVALID_FOG_MSG);

        currentFog = (Fog)l;
        currentFogPath = fogPath;
    }

    /**
     * Set the background path that should be applied to the current surface.
     * The output drawn will be a combination of this information and that
     * of the view environment.
     *
     * @param bgPath The path to the active background
     * @throws IllegalArgumentException The path is not to a background instance
     */
    public void setActiveBackground(SceneGraphPath bgPath)
        throws IllegalArgumentException
    {
        Leaf l = bgPath.getTerminalNode();
        if(l != null && !(l instanceof Background))
            throw new IllegalArgumentException(INVALID_BACKGROUND_MSG);

        currentBackground = (Background)l;
        currentBackgroundPath = bgPath;
        resetColor = true;
    }

    /**
     * Set the view environment that is used to render this surface.
     *
     * @param env The environment instance to use for the render setup
     */
    public void setViewEnvironment(ViewEnvironment env)
    {
    }

    /**
     * Set the background colour that this surface should be cleared to before
     * the drawing step. Colours range from 0 to 1 in the normal manner.
     *
     * @param r The red component of the background clear colour
     * @param g The green component of the background clear colour
     * @param b The blue component of the background clear colour
     * @param a The alpha component of the background clear colour
     */
    public void setClearColor(float r, float g, float b, float a)
    {
        clearColor[0] = r;
        clearColor[1] = g;
        clearColor[2] = b;
        clearColor[3] = a;

        resetColor = true;
    }

    /**
     * Instruct the surface to draw the collected set of nodes now. The
     * registered view environment is used to draw to this surface. If no
     * view is registered, the surface is cleared and then this call is
     * exited. The drawing surface does not swap the buffers at this point.
     */
    public void draw()
    {
    }

    /**
     * Swap the buffers now if the surface supports multiple buffer drawing.
     * For surfaces that don't support multiple buffers, this does nothing.
     */
    public void swap()
    {
    }

    /**
     * Get the underlying object that this surface is rendered to. If it is a
     * screen display device, the surface can be one of AWT Component or
     * Swing JComponent. An off-screen buffer would be a form of AWT Image etc.
     *
     * @return The drawable surface representation
     */
    public Object getSurfaceObject()
    {
        return this;
    }

    /**
     * Instruct this surface that you have finished with the resources needed
     * and to dispose all rendering resources.
     */
    public void dispose()
    {
        canvas.removeGLEventListener(this);

    }

    //---------------------------------------------------------------
    // Methods defined by GLEventListener
    //---------------------------------------------------------------

    /**
     * Called by the drawable immediately after the OpenGL context is
     * initialized; the GLContext has already been made current when
     * this method is called.
     *
     * @param drawable The display context to render to
     */
    public void init(GLDrawable drawable)
    {
        GL gl = drawable.getGL();
        GLU glu = drawable.getGLU();

        gl.glClearColor(clearColor[0],
                        clearColor[1],
                        clearColor[2],
                        clearColor[3]);
        gl.glEnable(GL.GL_CULL_FACE);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glEnable(GL.GL_LIGHTING);
        gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
    }

    /**
     * Called by the drawable when the surface resizes itself. Used to
     * reset the viewport dimensions.
     *
     * @param drawable The display context to render to
     */
    public void reshape(GLDrawable drawable,
                        int x,
                        int y,
                        int width,
                        int height)
    {
        float aspect_ratio = (float)width / (float)height;
        GL gl = drawable.getGL();
        GLU glu = drawable.getGLU();

        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();

        // TODO: hardcoded back clip, need to get value from ViewEnvironment
        glu.gluPerspective(45, aspect_ratio, 1, 1000);
        gl.glMatrixMode(GL.GL_MODELVIEW);
    }

    /**
     * Called by the drawable when the display mode or the display device
     * associated with the GLDrawable has changed.
     *
     * @param drawable The display context to render to
     */
    public void displayChanged(GLDrawable drawable,
                               boolean modeChanged,
                               boolean deviceChanged)
    {
    }

    /**
     * Called by the drawable to perform rendering by the client.
     *
     * @param gld The display context to render to
     */
    public void display(GLDrawable gld)
    {
        GL gl = gld.getGL();
        GLU glu = gld.getGLU();


        if(currentBackground != null)
        {
            if(!(currentBackground instanceof ColorBackground))
                gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

            currentBackground.render(gl, glu);

            // currentBackground.postRender(gl, glu);
        }
        else
        {
            if(resetColor)
            {
                gl.glClearColor(clearColor[0],
                                clearColor[1],
                                clearColor[2],
                                clearColor[3]);
            }

            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        }

        if(currentViewpoint != null)
            currentViewpoint.setupView(gl, glu);

//        d.draw(c.getRenderList(), c.getRenderOp(), c.getRenderListSize());
    }
}
