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

package org.j3d.aviatrix3d;

// External imports
// None

// Local imports
// None

/**
 * Interface representing the output of a render pipeline.
 * <p>
 *
 * The output may be any of the traditional types: pBuffer, screen or memory
 * or any non-traditional type like haptic devices, network streams etc.
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface DrawableSurface
{
    /**
     * Set the viewpoint path that should be applied to the current surface.
     * The output drawn will be a combination of this information and that
     * of the view environment.
     *
     * @param viewPath The path to the active viewpoint
     * @throws IllegalArgumentException The path is not to a viewpoint instance
     */
    public void setActiveView(SceneGraphPath viewPath)
        throws IllegalArgumentException;

    /**
     * Set the fog that should be applied to the current surface.
     * The output drawn will be a combination of this information and that
     * of the view environment.
     *
     * @param fogPath The path to the active fog node
     * @throws IllegalArgumentException The path is not to a fog instance
     */
    public void setActiveFog(SceneGraphPath fogPath)
        throws IllegalArgumentException;

    /**
     * Set the background path that should be applied to the current surface.
     * The output drawn will be a combination of this information and that
     * of the view environment.
     *
     * @param bgPath The path to the active background
     * @throws IllegalArgumentException The path is not to a background instance
     */
    public void setActiveBackground(SceneGraphPath bgPath)
        throws IllegalArgumentException;

    /**
     * Set the view environment that is used to render this surface.
     *
     * @param env The environment instance to use for the render setup
     */
    public void setViewEnvironment(ViewEnvironment env);

    /**
     * Set the background colour that this surface should be cleared to before
     * the drawing step. Colours range from 0 to 1 in the normal manner.
     *
     * @param r The red component of the background clear colour
     * @param g The green component of the background clear colour
     * @param b The blue component of the background clear colour
     * @param a The alpha component of the background clear colour
     */
    public void setClearColor(float r, float g, float b, float a);

    /**
     * Instruct the surface to draw the collected set of nodes now. The
     * registered view environment is used to draw to this surface. If no
     * view is registered, the surface is cleared and then this call is
     * exited. The drawing surface does not swap the buffers at this point.
     */
    public void draw();

    /**
     * Swap the buffers now if the surface supports multiple buffer drawing.
     * For surfaces that don't support multiple buffers, this does nothing.
     */
    public void swap();

    /**
     * Get the underlying object that this surface is rendered to. If it is a
     * screen display device, the surface can be one of AWT Component or
     * Swing JComponent. An off-screen buffer would be a form of AWT Image etc.
     *
     * @return The drawable surface representation
     */
    public Object getSurfaceObject();

    /**
     * Instruct this surface that you have finished with the resources needed
     * and to dispose all rendering resources.
     */
    public void dispose();
}