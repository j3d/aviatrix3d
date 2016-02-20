/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2006
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
import java.util.Arrays;

// Local imports


/**
 * Implementation of the sort stage that does only state sorting and ignores
 * transparency.
 * <p>
 *
 * The sorting is based on state changes only. For 2D nodes, it just expands
 * the array directly.
 *
 * @author Justin Couch
 * @version $Revision: 3.4 $
 */
public class StateSortStage extends BaseStateSortStage
{
    /**
     * Create an empty sorting stage that assumes just a single renderable
     * output.
     */
    public StateSortStage()
    {
        this(LIST_START_SIZE);
    }

    /**
     * Create an empty sorting stage that initialises the internal structures
     * to assume that there is a minumum number of surfaces, both on and
     * offscreen.
     *
     */
    public StateSortStage(int numSurfaces)
    {
        super(numSurfaces);
    }

    //---------------------------------------------------------------
    // Methods defined by BaseSortStage
    //---------------------------------------------------------------

    @Override
    protected int sortNodes(GraphicsCullOutputDetails[] nodes,
                            int numNodes,
                            GraphicsEnvironmentData data,
                            GraphicsInstructions instr,
                            int instrCount)
    {
        Arrays.sort(nodes, 0, numNodes, stateComparator);

        int idx = instrCount;

        int start = 0;
        boolean done = false;

        while(!done)
        {
            try
            {
                for(int i = start; i < numNodes && !terminate; i++)
                    idx = appendObject(nodes[i], instr, idx);

                done = true;
            }
            catch(ArrayIndexOutOfBoundsException be)
            {
                start = idx;

                realloc(instr, instr.numValid);
            }
        }

        done = false;

        while(!done)
        {
            try
            {
                idx = cleanupObjects(instr, idx);

                done = true;
            }
            catch(ArrayIndexOutOfBoundsException be)
            {
                realloc(instr, instr.numValid);
            }
        }

        return idx;
    }

    @Override
    protected int sort2DNodes(GraphicsCullOutputDetails[] nodes,
                              int numNodes,
                              GraphicsEnvironmentData data,
                              GraphicsInstructions instr,
                              int instrCount)
    {
        int idx = instrCount;
        int start = 0;
        boolean done = false;

        while(!done)
        {
            try
            {
                for(int i = start; i < numNodes && !terminate; i++)
                    idx = appendObject(nodes[i], instr, idx);

                done = true;
            }
            catch(ArrayIndexOutOfBoundsException be)
            {
                start = idx;

                realloc(instr, instr.numValid);
            }
        }

        done = false;

        while(!done)
        {
            try
            {
                idx = cleanupObjects(instr, idx);

                done = true;
            }
            catch(ArrayIndexOutOfBoundsException be)
            {
                realloc(instr, instr.numValid);
            }
        }

        return idx;
    }

    @Override
    protected int estimateInstructionSize(SceneRenderBucket scene)
    {
        return 4 + scene.numNodes * GUESS_NUM_COMPONENTS;
    }

    @Override
    protected int estimateInstructionSize(MultipassRenderBucket scene)
    {
        int instr_count = 2;

        for(int i = 0; i < scene.mainScene.numPasses; i++)
        {
            // Start/stop pass commands + up to 4 buffer state start
            // and stop commands.
            instr_count += 10 + scene.mainScene.numNodes[i] * GUESS_NUM_COMPONENTS;
        }

        return instr_count;
    }
}
