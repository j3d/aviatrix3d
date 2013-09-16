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
 * Handles the output of the audio culling stage.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 2.1 $
 */
public interface CulledAudioReceiver
{
    /**
     * Here's the sorted output list of nodes. For the 2D array of objects, contains
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
    public void culledOutput(RenderableRequestData otherData,
                             AudioEnvironmentData data,
                             AudioCullOutputDetails[] nodes,
                             int numNodes);
}
