/*****************************************************************************
 *                  Yumetech, Inc Copyright (c) 2004 - 2006
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
import com.jogamp.openal.AL;

import org.j3d.maths.vector.Matrix4d;

// Local imports
// None


/**
 * Marker interface for all objects that would like to be capable of rendering
 * themself using an audio renderer.
 * <p>
 *
 * This interface should generally not be directly used. It is a base
 * description for derived types that provide the specific types of information
 * through specific methods. It is used to provide a single common interface so
 * that the implementing instance can be placed in the
 * {@link org.j3d.aviatrix3d.pipeline.audio.AudioDetails}.
 *
 * @author Alan Hudson
 * @version $Revision: 2.2 $
 */
public interface AudioRenderable extends Renderable
{
    /**
     * State check to see whether the sound is enabled.
     *
     * @return true if the sound has something to render
     */
    public boolean isEnabled();

    /**
     * Check to see if this renderable is spatialised in any way. Spatialised
     * means it would require proper head tracking, where non-spatialised just
     * represents a basic noise such as background sound.
     *
     * @return true if this is a spatialised source, false otherwise
     */
    public boolean isSpatialised();

    /**
     * This method is called to render this node.  All openGL commands needed
     * to render the node should be executed.  Any transformations needed
     * should be added to the transformation stack premultiplied.  This
     * method must be re-entrant as it can be called from multiple
     * places at once.
     *
     * @param al The al context to render with
     * @param transform The transformation stack to this node
     */
    public void render(AL al, Matrix4d transform);

    /**
     * This method is called after a node has been rendered.  This method
     * must be re-entrant.
     *
     * @param al The al context to draw with
     */
    public void postRender(AL al);
}
