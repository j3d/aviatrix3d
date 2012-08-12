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
 * A cullable that represents a single visual composited layer.
 * <p>
 *
 * A layer has zero or more viewports to process.
 *
 * @author Justin Couch
 * @version $Revision: 2.3 $
 */
public interface ViewportLayerCullable extends Cullable
{
    /**
     * Check to see if this is a multipass cullable or single pass.
     *
     * @return true if this is a multipass cullable
     */
    public boolean isMultipassViewport();

    /**
     * Check to see if this render pass is the one that also has the 
     * spatialised audio to be rendered for this frame. If this is a multipass
     * layer then there is must return false and potentially one of the render
     * passes will be the active audio source.  See the package 
     * documentation for more information about how this state is managed.
     *
     * @return true if this is the source that should be rendered this
     *   this frame. 
     */
    public boolean isAudioSource();

    /**
     * Get the cullable layer child that for the given layer index.
     *
     * @return The layer cullable at the given index or null
     */
    public SceneCullable getCullableScene();
}
