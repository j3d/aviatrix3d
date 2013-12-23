/*****************************************************************************
 *                    Yumetech, Inc Copyright (c) 2004 - 2007
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
import org.j3d.aviatrix3d.pipeline.RenderableRequestData;

/**
 * Handles any sort of rendering sort ability.
 * <p>
 *
 * Typical sorting operations that may implement this interface are for
 * depth or priority sorting.
 * <p>
 *
 * A sorter will have a reciever for its output. If no receiver is registered
 * the sorter should still operate because an implemenation could choose to
 * poll for the output of the sorter at any time too. The output should always
 * remain valid, regardless of whether another sort is in progress. If a sort
 * is in progress then the output is for the previous sort step.
 *
 * @author Alan Hudson
 * @version $Revision: 2.2 $
 */
public interface AudioSortStage
{
    /**
     * Sort the listing of nodes in the given array. Do not return until the
     * sort has been completed.
     * <p>
     * For the 2D array of objects, contains
     * the list of final subscenes to send to the final stage. First index is
     * the direct owner of the scene contents. The second index is the
     * scene parent of the scene included (needed for pBuffer GL context
     * handling at render time). If this second one is null, then the parent
     * is the main canvas that is being rendered to.
     *
     * @param otherData data to be processed before the rendering
     * @param data External rendering environment information
     * @param nodes List of processed nodes based on the scene they came from
     * @param numNodes Number of nodes in each scene
     */
    public void sort(RenderableRequestData otherData,
                     AudioEnvironmentData data,
                     AudioCullOutputDetails[] nodes,
                     int numNodes);

    /**
     * Register a reciever for the output of the sorter. If the value is null,
     * it will clear the currently set receiver.
     *
     * @param sgr The receiver instance to add or null
     */
    public void setSortedAudioReceiver(SortedAudioReceiver sgr);

    /**
     * Force a halt of the current processing. Any processing in progress
     * should exit immediately. Used to abort the current scene processing due
     * to application shutdown or complete scene replacement.
     */
    public void halt();
}
