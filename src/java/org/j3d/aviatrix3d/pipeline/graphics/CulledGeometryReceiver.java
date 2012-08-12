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
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsRequestData;

/**
 * Handles the output of the geometry culling stage that should be passed onto
 * the next stage of the pipeline (sorting or other).
 * <p>
 *
 * The reciever is a blocking call - all data is expected to be managed before
 * it returns. In the case of a single-threaded pipeline implementation, that
 * implies that the next stage will process the data before returning. In a
 * multithreaded pipeline, the receiver will copy the data to somewhere that is
 * thread safe before allowing the geometry to return.
 *
 * @author Justin Couch
 * @version $Revision: 3.4 $
 */
public interface CulledGeometryReceiver
{
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
     * @param sceneParent Parent node that holds the subscene in the main
     *   scene graph
     */
    public void culledOutput(GraphicsRequestData otherData,
                             GraphicsProfilingData profilingData,
                             ViewportCollection[][] layers,
                             int[] numLayers,
                             int numScenes,
                             OffscreenBufferRenderable[][] sceneParent);
}
