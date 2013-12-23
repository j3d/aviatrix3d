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

package org.j3d.aviatrix3d.pipeline;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.rendering.LayerCullable;
import org.j3d.aviatrix3d.rendering.ProfilingData;
import org.j3d.util.ErrorReporter;

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
 * @version $Revision: 2.4 $
 */
public interface CullStage
{
    /**
     * Update and cull the scenegraph defined by a set of layers. This
     * generates an ordered list of nodes to render. It will not return until
     * the culling is complete.
     *
     * @param otherData data to be passed along unprocessed
     * @param profilingData The timing and load data on each stage
     * @param layers The collection of layers, in order, to render
     * @param numLayers The number of valid layers to use
     */
    public void cull(RenderableRequestData otherData,
                     ProfilingData profilingData,
                     LayerCullable[] layers,
                     int numLayers);

    /**
     * Force a halt of the current processing. Any processing in progress
     * should exit immediately. Used to abort the current scene processing due
     * to application shutdown or complete scene replacement.
     */
    public void halt();

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the node's internals can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter);
}
