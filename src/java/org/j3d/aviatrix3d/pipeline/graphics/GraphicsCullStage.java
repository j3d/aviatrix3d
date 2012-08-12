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

package org.j3d.aviatrix3d.pipeline.graphics;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.Layer;
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
 * The culling stage is responsible for looking at the offscreen renderable
 * surfaces as well as the main screen. Since most scenes will not require
 * any offscreen rendering, convenience methods are defined to allow the user
 * to turn on/off these checks. Offscreen rendering, and the checking for
 * extra renderables can be a huge CPU hog so it is advisable to make sure that
 * it is turned off if you don't need it. An ideal implementation will be able
 * to handle dynamically switching between the two states between frames
 * without the need to restart.
 * <p>
 *
 * Output is to be sent to the registered listener.
 *
 * @author Justin Couch
 * @version $Revision: 3.0 $
 */
public interface GraphicsCullStage extends CullStage
{
    /**
     * Set the flag for whether to check for offscreen textures or not. By
     * default, this flag is set to true.
     *
     * @param state true if offscreen textures should be looked for
     */
    public void setOffscreenCheckEnabled(boolean state);

    /**
     * Find out what the current offscreen check state is.
     *
     * @return true if the checking is being performed
     */
    public boolean isOffscreenCheckEnabled();

    /**
     * Register a reciever for the output of the sorter. If the value is null,
     * it will clear the currently set receiver.
     *
     * @param sgr The receiver instance to add or null
     */
    public void setCulledGeometryReceiver(CulledGeometryReceiver sgr);

    /**
     * Set the eyepoint offset from the centre position. This is used to model
     * offset view frustums, such as multiple displays or a powerwall. This
     * method will be called with the appropriate values from the
     * RenderPipeline that this culler is inserted into.
     *
     * @param x The x axis offset
     * @param y The y axis offset
     * @param z The z axis offset
     */
    public void setEyePointOffset(float x, float y, float z);

    /**
     * Set the orientation of this screen relative to the user's normal view
     * direction. The normal orientation of the screen is along the negative
     * Z axis. This method provides and axis-angle reorientation of that
     * direction to one that is facing the screen. Typically this will just
     * involve a rotation around the Y axis of some amount (45 and 90 deg
     * being the most common used in walls and caves). This method will be
     * called with the appropriate values from the RenderPipeline that this
     * culler is inserted into.
     *
     * @param x The x axis component
     * @param y The y axis component
     * @param z The z axis component
     * @param a The angle to rotate around the axis in radians
     * @throws IllegalArgumentException The length of the axis is zero
     */
    public void setScreenOrientation(float x, float y, float z, float a)
      throws IllegalArgumentException;
}
