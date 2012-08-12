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
// None

// Local imports
// None

/**
 * Class for passing the detailed rendering information about a single
 * multipass rendering set through the pipeline.
 * <p>
 *
 * Multipass textures require information about the source, a callback to
 * make between passes, as well as the texture object(s) that want to make
 * use of this source.
 *
 * @author Justin Couch
 * @version $Revision: 3.3 $
 */
public class MultipassDetails
{
    /** The initial size of the nodes list */
    private static final int LIST_START_SIZE = 200;

    /** The number of passes to have */
    private static final int PASS_START_SIZE = 4;

    /**
     * External rendering environment information that is applied to
     * the entire setup. Typically this is used to contain just the
     * main background and the viewport size. Individual per-pass
     * environments are held in the {@link #data} variable.
     */
    public GraphicsEnvironmentData globalData;

    /** External rendering environment information for each pass. */
    public GraphicsEnvironmentData[] data;

    /** The list of state renderables for each pass. */
    public BufferDetails[] buffers;

    /**
     * List of processed nodes based on the scene they came from and the
     * rendering pass to be processed. [a][b] where a is the rendering pass
     * (numbered from 0 to n) and b is the nodes to be rendered in that pass.
     */
    public GraphicsCullOutputDetails[][] nodes;

    /** Number of nodes in each rendering pass. */
    public int[] numNodes;

    /** The number of valid passes to render this time. */
    public int numPasses;

    /**
     * Create a default instance of this bucket. All lists are initialised to null
     * except the {@link #nodes} variable, which is set to an internal
     * default size. The graphics environment data is initialised.
     */
    public MultipassDetails()
    {
        this(PASS_START_SIZE, LIST_START_SIZE);
    }

    /**
     * Create a instance of this bucket with the node list initialised to the
     * given size. All lists are initialised to null except the {@link #nodes}
     * variable, which is set to an internal given size and populated with
     * the class instances. The graphics environment data is initialised.
     *
     * @param numPasses The number of passes to preallocate
     * @param size The number of items in the nodes list to create
     */
    public MultipassDetails(int numPasses, int size)
    {
        numNodes = new int[numPasses];
        nodes = new GraphicsCullOutputDetails[numPasses][size];
        buffers = new BufferDetails[numPasses];
        data = new GraphicsEnvironmentData[size];

        for(int i = 0; i < numPasses; i++)
        {
            for(int j = 0; j < size; j++)
                nodes[i][j] = new GraphicsCullOutputDetails();

            buffers[i] = new BufferDetails();
            data[i] = new GraphicsEnvironmentData();
        }

        globalData = new GraphicsEnvironmentData();
    }

    /**
     * Check and resize the lists if necessary to accomodate the requested
     * number of passes.
     *
     * @param numPasses The minimum number of passes to have allocated
     */
    public void ensureCapacity(int numPasses)
    {
        if(nodes.length >= numPasses)
            return;

        GraphicsCullOutputDetails[][] tmp1 = new GraphicsCullOutputDetails[numPasses][];
        BufferDetails[] tmp2 = new BufferDetails[numPasses];
        GraphicsEnvironmentData[] tmp3 = new GraphicsEnvironmentData[numPasses];
        int[] tmp4 = new int[numPasses];

        for(int i = 0; i < nodes.length; i++)
        {
            tmp1[i] = nodes[i];
            tmp2[i] = buffers[i];
            tmp3[i] = data[i];
            tmp4[i] = numNodes[i];
        }

        for(int i = nodes.length; i < numPasses; i++)
        {
            tmp1[i] = new GraphicsCullOutputDetails[LIST_START_SIZE];
            tmp2[i] = new BufferDetails();
            tmp3[i] = new GraphicsEnvironmentData();

            for(int j = 0; j < LIST_START_SIZE; j++)
               tmp1[i][j] = new GraphicsCullOutputDetails();
        }

        nodes = tmp1;
        buffers = tmp2;
        data = tmp3;
        numNodes = tmp4;
    }
}
