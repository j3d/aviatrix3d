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

import javax.vecmath.Matrix4f;

// Local imports
import org.j3d.util.MatrixUtils;

import org.j3d.aviatrix3d.rendering.AudioRenderable;
import org.j3d.aviatrix3d.pipeline.RenderOp;
import org.j3d.aviatrix3d.pipeline.RenderableRequestData;

/**
 * Implementation of the sort stage that does nothing.
 * <p>
 *
 * The sort stage just takes the given nodes and expands them into an array
 * renders and then immediately pops the node. No sorting on output is done.
 *
 * @author Alan Hudson
 * @version $Revision: 2.4 $
 */
public class NullAudioSortStage implements AudioSortStage
{
    /** The initial size of the instruction list */
    private static final int LIST_START_SIZE = 1;

    /** Receiver for the output of this stage */
    private SortedAudioReceiver receiver;

    /** Output array for passing on to the receiver */
    private AudioInstructions commandList;

    /** A semi-unique ID counter used for assigning light IDs. May wrap */
    private int lastGlobalId;

    /** Matrix Utilities to invert matrices */
    private MatrixUtils matrixUtils;

    /** Flag indicating a shutdown of the current processing is requested */
    private boolean terminate;

    /**
     * Create an empty sorting stage that initialises the internal structures
     * to assume that there is a minumum number of surfaces, both on and
     * offscreen.
     *
     */
    public NullAudioSortStage()
    {
        lastGlobalId = 0;

        commandList = new AudioInstructions();
        matrixUtils = new MatrixUtils();
    }

    /**
     * Sort the listing of nodes in the given array. Do not return until the
     * sort has been completed. If the sceneParent is the main scene graph and
     * not an offscreen texture, then that spot should be set to null in the list.
     *
     * @param otherData data to be processed before the rendering
     * @param data External rendering environment information
     * @param nodes List of processed nodes based on the scene they came from
     * @param numNodes Number of nodes in each scene
     */
    public void sort(RenderableRequestData otherData,
                     AudioEnvironmentData data,
                     AudioCullOutputDetails[] nodes,
                     int numNodes)
    {
        terminate = false;
        commandList.renderData = data;
        sortSingle(nodes, numNodes, commandList);

        // Don't pass stuff on if we can help it
        if(terminate)
            return;

        if(receiver != null)
            receiver.sortedOutput(otherData, commandList);
    }


    /**
     * Register a reciever for the output of the sorter. If the value is null,
     * it will clear the currently set receiver.
     *
     * @param sgr The receiver instance to add or null
     */
    public void setSortedAudioReceiver(SortedAudioReceiver sgr)
    {
        receiver = sgr;
    }

    /**
     * Force a halt of the current processing. Any processing in progress
     * should exit immediately. Used to abort the current scene processing due
     * to application shutdown or complete scene replacement.
     */
    public void halt()
    {
        terminate = true;
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Sort a single set of nodes into the output details and place in the
     * provided RenderInstructions instance.
     *
     * @param instr Instruction instant to put the details into
     */
    private void sortSingle(AudioCullOutputDetails[] nodes,
                            int numNodes,
                            AudioInstructions instr)
    {
        // check to see the array is big enough

        int req_size = numNodes << 1;

        if(instr.renderList.length < req_size)
        {
            AudioDetails[] tmp = new AudioDetails[req_size];

            System.arraycopy(instr.renderList, 0, tmp, 0, instr.renderList.length);

            for(int i = instr.renderList.length; i < req_size; i++)
                tmp[i] = new AudioDetails();

            instr.renderList = tmp;
            instr.renderOps = new int[req_size];
        }

        int idx = 0;

        for(int i = 0; i < numNodes && !terminate; i++)
        {
            // Now process the node type.
            if(!(nodes[i].renderable instanceof AudioRenderable))
                continue;

            AudioRenderable ar = (AudioRenderable)nodes[i].renderable;

            // For this simple one, just use the basic render command.
            // Something more complex would pull the shape apart and do
            // state/depth/transparency sorting in this section.

            instr.renderList[idx].renderable = nodes[i].renderable;

            if(ar.isSpatialised())
            {
                Matrix4f tx = nodes[i].transform;

                matrixUtils.inverse(tx, tx);

                instr.renderList[idx].transform = tx;
            }

            instr.renderOps[idx] = RenderOp.START_RENDER;
            idx++;

            instr.renderList[idx].renderable = nodes[i].renderable;
            instr.renderOps[idx] = RenderOp.STOP_RENDER;
            idx++;
        }

        instr.numValid = idx;
    }
}
