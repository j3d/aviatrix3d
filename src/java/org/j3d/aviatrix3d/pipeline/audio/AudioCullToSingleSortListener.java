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
import org.j3d.aviatrix3d.SceneGraphObject;
import org.j3d.aviatrix3d.pipeline.RenderableRequestData;

/**
 * Adapter class that maps the output of a cull stage to a single sorter.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 2.2 $
 */
public class AudioCullToSingleSortListener implements CulledAudioReceiver
{
    /** The sorter to hand the nodes to */
    private AudioSortStage sorter;

    /**
     * Create a new default instance of this class with nothing set.
     */
    public AudioCullToSingleSortListener()
    {
    }

    //---------------------------------------------------------------
    // Methods defined by CulledAudioReceiver
    //---------------------------------------------------------------

    /**
     * Sort the listing of nodes in the given array. Do not return until the
     * sort has been completed. If the sceneParent is the main scene graph and
     * not an offscreen texture, then that spot should be set to null in the list.
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
                             int numNodes)
    {
        // Need to fix the buffer ID setup here.
        if(sorter != null)
            sorter.sort(otherData, data, nodes, numNodes);
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Set the sorter instance to be used as the target. Passing a value of
     * null will clear the current registered instance.
     *
     * @param s The sorter instance to use or null
     */
    public void setSorter(AudioSortStage s)
    {
        sorter = s;
    }
}
