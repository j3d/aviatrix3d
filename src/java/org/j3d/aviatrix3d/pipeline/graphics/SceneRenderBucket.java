/*****************************************************************************
 *                        Yumtech, Inc Copyright (c) 2001 - 2006
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

/**
 * Data holder that passes the information about what is to be rendered from a
 * single scene within a layer from the output of the {@link GraphicsCullStage}
 * through to the {@link GraphicsSortStage}.
 *
 * @author Justin Couch
 * @version $Revision: 3.3 $
 */
public class SceneRenderBucket
{
    /** The initial size of the nodes list */
    private static final int LIST_START_SIZE = 200;

    /** External rendering environment information */
    public GraphicsEnvironmentData data;

    /** List of processed nodes based on the scene they came from */
    public GraphicsCullOutputDetails[] nodes;

    /** Number of nodes in this scene */
    public int numNodes;

    /**
     * Parent nodes that hold the subscene in the main scene graph. Each
     * index is the scene parent of this scene included (needed for pBuffer
     * GL context handling at render time). If this is null, then the parent
     * is the main canvas that is being rendered to.
     */
    public OffscreenBufferRenderable[] sceneParents;

    /** A list of the multipass texture sources found. */
    public MultipassDetails[] generatedTextures;

    /** The number of multipass textures to be rendered for this scene */
    public int numGeneratedTextures;

    /**
     * Create a default instance of this bucket. All lists are initialised to null
     * except the {@link #nodes} variable, which is set to an internal
     * default size. The graphics environment data is initialised.
     */
    public SceneRenderBucket()
    {
        this(LIST_START_SIZE);
    }

    /**
     * Create a instance of this bucket with the node list initialised to the
     * given size. All lists are initialised to null except the {@link #nodes}
     * variable, which is set to an internal given size and populated with
     * the class instances. The graphics environment data is initialised.
     *
     * @param size The number of items in the nodes list to create
     */
    public SceneRenderBucket(int size)
    {
        nodes = new GraphicsCullOutputDetails[size];

        for(int i = 0; i < size; i++)
            nodes[i] = new GraphicsCullOutputDetails();

        data = new GraphicsEnvironmentData();
    }

    /**
     * Check and resize the lists if necessary to accomodate the requested
     * number of passes.
     *
     * @param size The minimum number of cull items to have allocated
     */
    public void ensureCapacity(int size)
    {
        if(nodes.length >= size)
            return;

        GraphicsCullOutputDetails[] tmp = new GraphicsCullOutputDetails[size];

        System.arraycopy(nodes, 0, tmp, 0, nodes.length);

        for(int i = nodes.length; i < size; i++)
           tmp[i] = new GraphicsCullOutputDetails();

        nodes = tmp;
    }
}
