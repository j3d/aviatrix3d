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
 * A cullable that represents the scene contained in a viewport or
 * viewport layer.
 * <p>
 *
 * The scene can represent either a single or multipass scene instance. In
 * either case, at least one render pass cullable should be made available.
 *
 * @author Justin Couch
 * @version $Revision: 2.2 $
 */
public interface SceneCullable extends Cullable
{
    /**
     * If there is any user-defined data given, fetch it through this method.
     * In a single-pass scene this will return the same object as the first
     * pass cullable instance.
     *
     * @return Whatever object the user has set
     */
    public Object getUserData();

    /**
     * Check to see if this is a multipass cullable or single pass.
     *
     * @return true if this is a multipass cullable
     */
    public boolean isMultipassScene();

    /**
     * Get the primary view environment information. This applies to the whole
     * scene before any multipass processing is done. If this is a multipass
     * scene, the frustum information is ignored as each pass applies
     * separately.
     */
    public ViewEnvironmentCullable getViewCullable();

    /**
     * Get the rendering effects processing user interface that applies to
     * this frame. If there is non, returns null. In a multipass environment
     * this is called at prior to any pass being made, and only after all
     * the passes have been complete.
     *
     * @return The effects processor to apply to this frame
     */
    public RenderEffectsProcessor getRenderEffectsProcessor();

    /**
     * Get the cullable layer child that for the given layer index. For a single
     * pass scene this represents everything about the scene to be rendered.
     * The view environment of this scene is the same as that of the first
     * render pass.
     *
     * @param passIndex The index of the pass to fetch
     * @return The layer cullable at the given index or null
     */
    public RenderPassCullable getCullablePass(int passIndex);

    /**
     * Returns the number of valid cullable rendering passes to process. In a
     * single pass scene return 1.
     *
     * @return A number greater than or equal to zero
     */
    public int numCullableChildren();
}
