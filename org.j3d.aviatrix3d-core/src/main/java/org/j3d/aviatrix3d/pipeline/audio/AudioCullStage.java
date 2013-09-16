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

package org.j3d.aviatrix3d.pipeline.audio;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.pipeline.CullStage;

/**
 * Handles the scenegraph per-frame culling operations.
 * <p>
 *
 * The culling phase generates a list of leaf nodes to render by removing
 * non-required sections of the scene graph. How this culling is performed
 * (if at all) is dependent on the implementation of this class. All that
 * is defined is a complete scene graph as input, and a grouped set of
 * nodes based on what must be kept together from a rendering perspective. Two
 * typical culling approaches are view frustum and BSP. Others may also be
 * implemented dependent on the application domain. Implementations may also
 * work concepts that are not 3D geometry-based, such as audio and haptics.
 * <p>
 *
 * Output is to be sent to the registered listener.
 *
 * @author Alan Hudson
 * @version $Revision: 2.0 $
 */
public interface AudioCullStage extends CullStage
{
    /**
     * Register a reciever for the output of the sorter. If the value is null,
     * it will clear the currently set receiver.
     *
     * @param sgr The receiver instance to add or null
     */
    public void setCulledAudioReceiver(CulledAudioReceiver sgr);
}
