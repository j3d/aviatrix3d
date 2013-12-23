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
import org.j3d.aviatrix3d.pipeline.RenderPipeline;

/**
 * A marker interface that represents a single complete rendering pipeline.
 * <p>
 *
 * A pipeline represents all of the drawing steps that may be accomplished
 * within a rendering cycle - culling, sorting and drawing. While an end-user
 * may wish to directly call the methods on this interface directly to control
 * their own rendering, it is recommended that a dedicated pipeline manager be
 * used for this task.
 * <p>
 *
 * If the pipeline does not have a drawable surface registered, it will still
 * complete all the steps up to that point. If no scene is registered, no
 * functionality is performed - render() will return immediately.
 *
 * @author Justin Couch
 * @version $Revision: 3.0 $
 */
public interface GraphicsRenderPipeline extends RenderPipeline
{
    /**
     * Register a drawing surface that this pipeline will send its output to.
     * Setting a null value will remove the current drawable surface.
     *
     * @param device The device instance to use or replace
     */
    public void setGraphicsOutputDevice(GraphicsOutputDevice device);

    /**
     * Get the currently registered drawable surface instance. If none is set,
     * return null.
     *
     * @return The currently set device instance or null
     */
    public GraphicsOutputDevice getGraphicsOutputDevice();

    /**
     * Set the eyepoint offset from the centre position. This is used to model
     * offset view frustums, such as multiple displays or a powerwall. Positive
     * values will move the eyepoint to the right, up and back, relative to the
     * screen's center, effectively moving the screen to the left, down and
     * away from the user.
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
     * being the most common used in walls and caves).
     *
     * @param x The x axis component
     * @param y The y axis component
     * @param z The z axis component
     * @param a The angle to rotate around the axis in radians
     */
    public void setScreenOrientation(float x, float y, float z, float a);

    /**
     * Instruct the drawable at the end of this pipeline to swap the buffers
     * now.
     */
    public void swapBuffers();
}
