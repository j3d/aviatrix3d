/*****************************************************************************
 *                Yumetech, Inc Copyright (c) 2004 - 2007
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
import org.j3d.aviatrix3d.rendering.OffscreenBufferRenderable;

import org.j3d.util.ErrorReporter;

/**
 * Handles any sort of rendering sort ability.
 * <p>
 *
 * Typical sorting operations that may implement this interface are for
 * state and transparency sorting.
 * <p>
 *
 * A sorter will have a reciever for its output. If no receiver is registered
 * the sorter should still operate because an implemenation could choose to
 * poll for the output of the sorter at any time too. The output should always
 * remain valid, regardless of whether another sort is in progress. If a sort
 * is in progress then the output is for the previous sort step.
 *
 * @author Justin Couch
 * @version $Revision: 3.5 $
 */
public interface GraphicsSortStage
{
    /**
     * Sort the listing of layers and nodes. Do not return until the
     * sort has been completed.
     * <p>
     *
     * For the 2D array of layers, it contains the list of final subscenes to
     * send to the final stage, with the second dimension describing the
     * layers, in rendering order. The first item will always be the main
     * scene that gets rendered to the canvas.
     * <p>
     * For the 2D array sceneParent, it allows for mapping the output of
     * internal scenes to the holding texture node. Index 0 is the direct
     * owner of the  scene contents. Index 1 is the scene parent of the scene
     * included (needed for pBuffer GL context handling at render time). If
     * this second one is null, then the parent is the main canvas that is
     * being rendered to.
     *
     * @param otherData data to be passed along unprocessed
     * @param profilingData The timing and load data
     * @param layers The list of layers that need to be further processed
     * @param numLayers The number of valid layers in each scene to process
     * @param numScenes The number of valid scenes to process
     * @param sceneParent Parent node that holds the subscene in the main
     *   scene graph
     */
    public void sort(GraphicsRequestData otherData,
                     GraphicsProfilingData profilingData,
                     ViewportCollection[][] layers,
                     int[] numLayers,
                     int numScenes,
                     OffscreenBufferRenderable[][] sceneParent);

    /**
     * Get the listing of the previously sorted nodes. The array parameter is
     * used to return the number of valid items in the returned array.
     */
//    public Node[] getSortedOutput(int[] numValid);

    /**
     * Register a reciever for the output of the sorter. If the value is null,
     * it will clear the currently set receiver.
     *
     * @param sgr The receiver instance to add or null
     */
    public void setSortedGeometryReceiver(SortedGeometryReceiver sgr);

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
