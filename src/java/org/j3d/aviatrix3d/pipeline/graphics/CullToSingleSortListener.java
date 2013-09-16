/*****************************************************************************
 *                   Yumetech, Inc Copyright (c) 2004 - 2007
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
 * Adapter class that maps the output of a cull stage to a single sorter.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 3.5 $
 */
public class CullToSingleSortListener implements CulledGeometryReceiver
{
    /** The sorter to hand the nodes to */
    private GraphicsSortStage sorter;

    /**
     * Create a new default instance of this class with nothing set.
     */
    public CullToSingleSortListener()
    {
    }

    //---------------------------------------------------------------
    // Methods defined by CulledGeometryReceiver
    //---------------------------------------------------------------

    /**
     * Here's the sorted output list of nodes per layer. Each instance of
     * {@link ViewportCollection} represents the contents of a complete
     * layer.
     *
     * Note, this does not consider multipass texture sources right now.
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
     * @param otherData The optional renderable data to process this next frame
     * @param profilingData The timing and load data
     * @param layers The list of layers that need to be further processed
     * @param numLayers The number of valid layers in each scene to process
     * @param numScenes The number of valid scenes to process
     * @param sceneParents Parent node that holds the subscene in the main
     *   scene graph
     */
    public void culledOutput(GraphicsRequestData otherData,
                             GraphicsProfilingData profilingData,
                             ViewportCollection[][] layers,
                             int[] numLayers,
                             int numScenes,
                             OffscreenBufferRenderable[][] sceneParents)
    {
        // Need to fix the buffer ID setup here.
        if(sorter != null)
            sorter.sort(otherData,
                        profilingData,
                        layers,
                        numLayers,
                        numScenes,
                        sceneParents);
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
    public void setSorter(GraphicsSortStage s)
    {
        sorter = s;
    }
}
