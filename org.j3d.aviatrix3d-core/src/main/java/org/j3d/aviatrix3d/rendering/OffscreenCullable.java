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
 * A cullable that represents and offscreen rendered piece of scene graph that
 * will be used later by a parent object.
 * <p>
 *
 * An offscreen cullable has two parts - the scene graph that it contains, and
 * the buffer that it should be rendered to. The buffer could be a pbuffer or
 * frame buffer object (FBO). The implementation is not specified at this level.
 *
 * @author Justin Couch
 * @version $Revision: 2.2 $
 */
public interface OffscreenCullable extends Cullable
{
    /**
     * Get the current state of the repainting enabled flag.
     *
     * @return true when the texture requires re-drawing
     */
    public boolean isRepaintRequired();

    /**
     * Get the cullable layer child that for the given layer index.
     *
     * @return The layer cullable at the given index or null
     */
    public LayerCullable getCullableLayer(int layerIndex);

    /**
     * Returns the number of valid cullable children to process. If there are
     * no valid cullable children, return 0.
     *
     * @return A number greater than or equal to zero or -1
     */
    public int numCullableChildren();

    /**
     * Fetch the renderable that this offscreen cullable will draw to.
     *
     * @return The renderable instance that we deposit pixels to
     */
   public OffscreenBufferRenderable getOffscreenRenderable();
}
