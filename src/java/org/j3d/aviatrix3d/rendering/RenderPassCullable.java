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

package org.j3d.aviatrix3d.rendering;

// External imports
// None

// Local imports
// None

/**
 * A cullable that represents a single rendering pass within a viewport.
 * <p>
 *
 * The render pass cullable can be used for both single and multipass
 * rendering.
 *
 *
 * @author Justin Couch
 * @version $Revision: 2.6 $
 */
public interface RenderPassCullable extends Cullable
{
    /**
     * If there is any user-defined data given, fetch it through this method.
     *
     * @return Whatever object the user has set
     */
    public Object getUserData();

    /**
     * Check to see if this render pass is valid to render for this
     * frame.
     *
     * @return true if this is valid to process
     */
    public boolean isEnabled();

    /**
     * Check to see if this represents a 2D scene that has no 3D rendering
     * capabilities. A purely 2D scene sets up the view environment quite
     * different to a full 3D scene.
     *
     * @return true if this is a 2D scene rather than a 3D version
     */
    public boolean is2D();

    /**
     * Check to see if this render pass is the one that also has the
     * spatialised audio to be rendered for this frame. See the package
     * documentation for more information about how this state is managed.
     *
     * @return true if this is the source that should be rendered this
     *   this frame.
     */
    public boolean isAudioSource();

    /**
     * Get the primary view environment.
     */
    public ViewEnvironmentCullable getViewCullable();

    /**
     * Get the cullable object representing the active viewpoint that in this
     * environment.
     *
     * @return The viewpoint renderable to use
     */
    public EnvironmentCullable getViewpointCullable();

    /**
     * Get the cullable object representing the active background that in this
     * environment. If no background is set, this will return null.
     *
     * @return The background renderable to use
     */
    public EnvironmentCullable getBackgroundCullable();

    /**
     * Get the cullable object representing the active fog in this environment.
     * If no fog is set or this is a pass in a multipass rendering, this will
     * return null. If the underlying fog node is currently disabled or not
     * labeled as global, then this method should return null.
     *
     * @return The fog renderable to use
     */
    public LeafCullable getFogCullable();

    /**
     * Get the primary cullable that represents the root of the scene graph.
     * If this is a multipass cullable, this should return null.
     */
    public Cullable getRootCullable();

    /**
     * Fetch the renderable used to control the viewport setting. If the
     * default environment is to be used, this will return null.
     *
     * @return A renderable instance if custom viewport handling is need
     *   or null if not
     */
    public ViewportRenderable getViewportRenderable();

    /**
     * Fetch renderable information about the general buffer. If this is a
     * single pass scene, this will return null.
     *
     * @return The state representation for the general buffer
     */
    public BufferStateRenderable getGeneralBufferRenderable();

    /**
     * Fetch renderable information about the colour buffer. If this is a
     * single pass scene, this will return null.
     *
     * @return The state representation for the colour buffer
     */
    public BufferStateRenderable getColorBufferRenderable();

    /**
     * Fetch renderable information about the depth buffer. If this is a
     * single pass scene, this will return null.
     *
     * @return The state representation for the depth buffer
     */
    public BufferStateRenderable getDepthBufferRenderable();

    /**
     * Fetch renderable information about the stencil buffer. If this is a
     * single pass scene, this will return null.
     *
     * @return The state representation for the stencil buffer
     */
    public BufferStateRenderable getStencilBufferRenderable();

    /**
     * Fetch renderable information about the accumulation buffer. If this is a
     * single pass scene, this will return null.
     *
     * @return The state representation for the accumulation buffer
     */
    public BufferStateRenderable getAccumBufferRenderable();
}
