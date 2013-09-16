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

package org.j3d.aviatrix3d.pipeline.audio;

// External imports
import java.util.ArrayList;

// Local imports
import org.j3d.aviatrix3d.rendering.*;

import org.j3d.aviatrix3d.pipeline.RenderableRequestData;

import org.j3d.maths.vector.Matrix4d;
import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;
import org.j3d.util.I18nManager;

/**
 * Handles the scenegraph maintenance and culling operations.
 * <p>
 *
 * The culling phase generates a list of nodes to render.
 * <p>
 *
 * Since this is a null culling stage, and thus no culling is performed, if the
 * scene graph contains instances of Cullable, they are ignored and traversal
 * stops at that point.
 * <p>
 *
 * <b>Note:</b><p>
 * Layers are not implemented yet.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>sharedViewpointMsg: Viewpoint with multiple parents found</li>
 * </ul>
 *
 * @author Alan Hudson
 * @version $Revision: 2.9 $
 */
public class NullAudioCullStage implements AudioCullStage
{
    private static final String SHARED_VP_MSG_PROP =
        "org.j3d.aviatrix3d.pipeline.audio.NullAudioCullStage.sharedViewpointMsg";

    /** Initial scene depth used for the transform stack */
    private static final int TRANSFORM_DEPTH_SIZE = 64;

    /** The increment size of the stack if it gets overflowed */
    private static final int STACK_INCREMENT = 32;

    /** The initial size of the children list */
    private static final int LIST_START_DEPTH = 200;

    /** The increment size of the list if it gets overflowed */
    private static final int LIST_INCREMENT = 20;

    /** The list of culled nodes to send to the next stage */
    private AudioCullOutputDetails[][] outputCullList;

    /** List of valid culled nodes as we process them */
    private AudioCullOutputDetails[][] validCullList;

    /** List that is being used to fill values into */
    private AudioCullOutputDetails[] workCullList;

    /** list of culled nodes list sizes to send to the next stage */
    private int[] outputCullSize;

    /** List of environment data as we process them */
    private AudioEnvironmentData[] validEnvData;

    /** list of environment data to send to the next stage */
    private AudioEnvironmentData[] outputEnvData;

    /** List of culled nodes list sizes as we process them */
    private int[] validCullSize;

    /** Index to the next output list in the validCullList */
    private int lastOutputList;

    /** A stack used to control the depth of the transform tree */
    private Matrix4d[] transformStack;

    /** Index to the next place to add items in the transformStack */
    private int lastTxStack;

    /** Handler for the output */
    private CulledAudioReceiver receiver;

    /** Path to the current viewpoint */
    private ArrayList<Cullable> currentViewpointPath;

    /** Matrix used for pre-computing the view stack */
    private Matrix4d viewMatrix1;

    /** Matrix used for pre-computing the view stack */
    private Matrix4d viewMatrix2;

    /** Flag indicating a shutdown of the current processing is requested */
    private boolean terminate;

    /** Local reporter to put errors in */
    private ErrorReporter errorReporter;

    /**
     * Create a basic instance of this class with the list initial internal
     * setup for the given number of renderable surfaces. The size is just an
     * initial esstimate, and is used for optimisation purposes to prevent
     * frequent array reallocations internally. As such, the number does not
     * have to be perfect, just good enough.
     *
     */
    public NullAudioCullStage()
    {
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        int numDevices = 1;
        outputCullList = new AudioCullOutputDetails[numDevices][];
        validCullList = new AudioCullOutputDetails[numDevices][LIST_START_DEPTH];
        outputEnvData = new AudioEnvironmentData[numDevices];
        validEnvData = new AudioEnvironmentData[numDevices];
        outputCullSize = new int[numDevices];
        validCullSize = new int[numDevices];

        currentViewpointPath = new ArrayList<Cullable>(TRANSFORM_DEPTH_SIZE);
        viewMatrix1 = new Matrix4d();
        viewMatrix2 = new Matrix4d();

        // populate the list with valid instances...
        for(int i = 0; i < numDevices; i++)
            validEnvData[i] = new AudioEnvironmentData();

        // populate the list with valid instances...
        for(int i = 0; i < numDevices; i++)
            for(int j = 0; j < LIST_START_DEPTH; j++)
                validCullList[i][j] = new AudioCullOutputDetails();

        transformStack = new Matrix4d[TRANSFORM_DEPTH_SIZE];

        // populate the list with valid instances...
        for(int i = 0; i < TRANSFORM_DEPTH_SIZE; i++)
            transformStack[i] = new Matrix4d();
    }

    //---------------------------------------------------------------
    // Methods defined by AudioCullStage
    //---------------------------------------------------------------

    /**
     * Register a reciever for the output of the sorter. If the value is null,
     * it will clear the currently set receiver.
     *
     * @param sgr The receiver instance to add or null
     */
    public void setCulledAudioReceiver(CulledAudioReceiver sgr)
    {
        receiver = sgr;
    }

    //---------------------------------------------------------------
    // Methods defined by CullStage
    //---------------------------------------------------------------

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the node's internals can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter)
    {
        errorReporter = reporter;

        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Update and cull the scenegraph defined by a set of layers. This
     * generates an ordered list of nodes to render. It will not return until
     * the culling is complete.
     *
     * @param otherData data to be passed along unprocessed
     * @param profilingData The timing and load data
     * @param layers The collection of layers, in order, to render
     * @param numLayers The number of valid layers to use
     */
    public void cull(RenderableRequestData otherData,
                     ProfilingData profilingData,
                     LayerCullable[] layers,
                     int numLayers)
    {
        terminate = false;

        for(int i = 0; i < numLayers && !terminate; i++)
        {
            if(layers[i] == null)
                continue;

            LayerCullable c = layers[i];
            if(processLayer(c, 0, i))
                break;
        }

        // It is likely in this case that we're doing single-scene
        // rendering and will be doing it all the time. Possibly optimise
        // this by avoiding the copy altogether, but not 100% this won't
        // break in some corner case right now.

        outputEnvData[0] = validEnvData[0];
        outputCullList[0] = validCullList[0];
        outputCullSize[0] = validCullSize[0];

        if((lastOutputList != 0) && (receiver != null) && !terminate)
            receiver.culledOutput(otherData,
                                  outputEnvData[0],
                                  outputCullList[0],
                                  outputCullSize[0]);
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
    // Local methods
    //---------------------------------------------------------------

    /**
     * Process the layers of a pbuffer texture source.
     *
     * @param subsceneId The index of the subscene in the viewport output
     * @param layerId The index of the layer in the output
     * @return true if we found the active audio layer, false if we should
     *    continue looking
     */
    private boolean processLayer(LayerCullable layer,
                                 int subsceneId,
                                 int layerId)
    {
        int num_views = layer.numCullableChildren();

        for(int i = 0; i < num_views; i++)
        {
            ViewportCullable c = layer.getCullableViewport(i);
            if(cullViewport(c, subsceneId, layerId, i))
                return true;
        }

        return false;
    }

    /**
     * Process a single viewport for culling.
     *
     * @param view The viewport instance to process now
     * @param subsceneId The index of the subscene in the viewport output
     * @param layerId The index of the layer in the output
     * @param viewIndex The index of the viewport in the parent
     *    ViewportCollection
     */
    private boolean cullViewport(ViewportCullable view,
                                 int subsceneId,
                                 int layerId,
                                 int viewIndex)
    {
        if(view == null || !view.isValid())
            return false;

        int num_layers = view.numCullableChildren();
        boolean found = false;

        for(int i = 0; i < num_layers && !found && !terminate; i++)
        {
            ViewportLayerCullable layer = view.getCullableLayer(i);

            if(layer.isMultipassViewport())
                found = cullMultipassViewportLayer(layer.getCullableScene(),
                                                   subsceneId,
                                                   layerId,
                                                   viewIndex,
                                                   i);
            else  if(layer.isAudioSource()) {
                found = true;
                cullSingleViewportLayer(layer.getCullableScene(),
                                        subsceneId,
                                        layerId,
                                        viewIndex,
                                        i);
            }
        }

        return found;
    }

    /**
     * Cull through a multipass viewport layer.
     *
     * @param scene The scene instance to process
     * @param subsceneId The index of the subscene in the viewport output
     * @param layerId The index of the layer in the output
     * @param viewIndex The index of the viewport in the parent
     *    ViewportCollection
     * @param layerIndex The index of the layer within the viewport
     */
    private void cullSingleViewportLayer(SceneCullable scene,
                                         int subsceneId,
                                         int layerId,
                                         int viewIndex,
                                         int layerIndex)
    {
        RenderPassCullable pass = scene.getCullablePass(0);
        fillEnvData(scene, validEnvData[0]);

        if(pass.is2D())
            cullScene2D(pass, subsceneId, layerId, viewIndex, layerIndex);
        else
            cullScene(pass, subsceneId, layerId, viewIndex, layerIndex);
    }

    /**
     * Cull through a multipass viewport layer.
     *
     * @param scene The scene instance to process
     * @param subsceneId The index of the subscene in the viewport output
     * @param layerId The index of the layer in the output
     * @param viewIndex The index of the viewport in the parent
     *    ViewportCollection
     * @param layerIndex The index of the layer within the viewport
     */
    private boolean cullMultipassViewportLayer(SceneCullable scene,
                                              int subsceneId,
                                              int layerId,
                                              int viewIndex,
                                              int layerIndex)
    {
        int num_passes = scene.numCullableChildren();
        boolean found = false;

        for(int i = 0; i < num_passes && !found && !terminate; i++)
        {
            RenderPassCullable pass = scene.getCullablePass(i);
            found = pass.isAudioSource();
            cullRenderPass(pass,
                           i,
                           subsceneId,
                           layerId,
                           viewIndex,
                           layerIndex);
        }

        return found;
    }

    /**
     * Update and cull the scenegraph. This generates an ordered list
     * of nodes to render. It will not return until the culling is complete.
     *
     * @param scene The scene instance to cull
     * @param subsceneId The index of the subscene in the viewport output
     * @param layerId The index of the layer in the output
     * @param viewIndex The index of the viewport in the parent
     *    ViewportCollection
     * @param layerIndex The index of the layer within the viewport
     */
    private void cullScene(RenderPassCullable scene,
                           int subsceneId,
                           int layerId,
                           int viewIndex,
                           int layerIndex)
    {
        // Buffer ID is not used currently.
        terminate = false;
        lastOutputList = 0;
        lastTxStack = 0;

        workCullList = validCullList[0];

        Cullable node = scene.getRootCullable();

        if(terminate)
            return;

        if(node instanceof GroupCullable)
        {
            // set the top matrix stack
            if(node instanceof TransformCullable)
                ((TransformCullable)node).getTransform(transformStack[0]);
            else
                transformStack[0].setIdentity();

            validCullSize[0] = findAllNodes((GroupCullable)node, 0);
            validCullList[0] = workCullList;
            lastOutputList++;
        }
        else if(node instanceof SingleCullable)
        {
            // set the top matrix stack back to identity
            transformStack[0].setIdentity();
            validCullSize[0] = findNextNode((SingleCullable)node,  0);
            validCullList[0] = workCullList;
            lastOutputList++;
        }
        else if(node instanceof LeafCullable)
        {
            findAudioLeaf((LeafCullable)node, 0);

            validCullSize[0] = 1;
            validCullList[0] = workCullList;
            lastOutputList++;
        }
    }

    /**
     * Update and cull a 2D scenegraph. This generates an ordered list
     * of nodes to render. It will not return until the culling is complete.
     *
     * @param scene The scene instance to cull
     * @param subsceneId The index of the subscene in the viewport output
     * @param layerId The index of the layer in the output
     * @param viewIndex The index of the viewport in the parent
     *    ViewportCollection
     * @param layerIndex The index of the layer within the viewport
     */
    private void cullScene2D(RenderPassCullable scene,
                                int subsceneId,
                                int layerId,
                                int viewIndex,
                                int layerIndex)
    {
    }

    /**
     * Update and cull the a single pass from a multipass rendering. This
     * generates an ordered list of nodes to render in the same was as a normal
     * scene, but with fewer items updated, such as only a single background
     * for all passes. It will not return until the culling is complete.
     *
     * @param pass The rendering pass instance to cull
     * @param subsceneId The index of the subscene in the viewport output
     * @param layerId The index of the layer in the output
     * @param viewIndex The index of the viewport in the parent
     *    ViewportCollection
     * @param layerIndex The index of the layer within the viewport
     */
    private void cullRenderPass(RenderPassCullable pass,
                                           int passNumber,
                                           int subsceneId,
                                           int layerId,
                                           int viewIndex,
                                           int layerIndex)
    {
    }

    /**
     * Update and cull the scenegraph of a single node without doing any other
     * processing. This generates an ordered list
     * of nodes to render. It will not return until the culling is complete.
     *
     * @param node The root node to start the cull from
     */
    private int cullSingle(GroupCullable node)
    {
        // set the top matrix stack back to identity
        transformStack[0].setIdentity();

        if(node instanceof TransformCullable)
            ((TransformCullable)node).getTransform(transformStack[0]);

        return findAllNodes((GroupCullable)node, 0);
    }

    /**
     * Recursive walk of the tree to find all the renderable nodes.
     *
     * @param group The parent node to walk into
     * @param cullEndIndex The current last item on the cull list
     * @return The index of the last item on the cull list
     */
    private int findAllNodes(GroupCullable group, int cullEndIndex)
    {
        if(terminate)
            return 0;

        Cullable[] kids  = group.getCullableChildren();
        int size = group.numCullableChildren();

        int ret_val = cullEndIndex;

        for(int i = 0; i < size && !terminate; i++)
        {
            // If a tg, push the new TX onto the stack
            boolean is_tx = (kids[i] instanceof TransformCullable);

            if(is_tx)
            {
                resizeStack();
                TransformCullable tg = (TransformCullable)kids[i];
                tg.getTransform(transformStack[lastTxStack + 1]);

                transformStack[lastTxStack + 1].mul(transformStack[lastTxStack],
                                                  transformStack[lastTxStack + 1]);
                lastTxStack++;
            }

            if(kids[i] instanceof GroupCullable)
            {
                ret_val = findAllNodes((GroupCullable)kids[i], ret_val);
            }
            else if(kids[i] instanceof SingleCullable)
            {
                ret_val = findNextNode((SingleCullable)kids[i],  ret_val);
            }
            else if(kids[i] instanceof LeafCullable)
            {
                ret_val = findAudioLeaf((LeafCullable)kids[i], ret_val);
            }

            // Now pop the stacks
            if(is_tx)
                lastTxStack--;
        }

        return ret_val;
    }

    /**
     * From the given SingleCullable instance keep finding a non-sharedNode
     * instance, before continuing on the recursion
     *
     * @param root The parent node to walk into
     * @param cullEndIndex The current last item on the cull list
     * @return The index of the last item on the cull list
     */
    private int findNextNode(SingleCullable root, int cullEndIndex)
    {
        if(terminate)
            return 0;

        int ret_val = cullEndIndex;

        Cullable current = root.getCullableChild();

        while(current != null && current instanceof SingleCullable)
            current = ((SingleCullable)current).getCullableChild();

        // Now just continue on normally. If the result of the above was a
        // null then it just falls off the end of this if/else naturally.

        // If a tg, push the new TX onto the stack
        boolean is_tx = (current instanceof TransformCullable);

        if(is_tx)
        {
            resizeStack();
            TransformCullable tg = (TransformCullable)current;
            tg.getTransform(transformStack[lastTxStack + 1]);

            transformStack[lastTxStack + 1].mul(transformStack[lastTxStack],
                                              transformStack[lastTxStack + 1]);
            lastTxStack++;
        }

        if(current instanceof GroupCullable)
        {
            ret_val = findAllNodes((GroupCullable)current, cullEndIndex);
        }
        else if(current instanceof LeafCullable)
        {
            ret_val = findAudioLeaf((LeafCullable)current, ret_val);
        }

        // Now pop the stack.
        if(is_tx)
            lastTxStack--;

        return ret_val;
    }


    /**
     * Look at a leaf cullable and extract any audio values from it.
     *
     * @param leaf The cullable instance to check
     * @param cullEndIndex The position in the cull output array to start
     * @return The final count in the output array after processing
     */
    private int findAudioLeaf(LeafCullable leaf, int cullEndIndex)
    {
        if(leaf.getCullableType() != LeafCullable.AUDIO_CULLABLE)
            return cullEndIndex;

        Renderable r = leaf.getRenderable();
        if(!(r instanceof AudioRenderable))
            return cullEndIndex;

        int ret_val = cullEndIndex;
        AudioRenderable ar = (AudioRenderable)r;

        // Check the visibility state and ignore if not visible.
        if(ar.isEnabled())
        {
            resizeCullList(ret_val);
            workCullList[ret_val].renderable = ar;

            Matrix4d mat = transformStack[lastTxStack];

            // Transpose the matrix in place as it is being copied
            workCullList[ret_val].transform.set(mat);

            ret_val++;
        }

        return ret_val;
    }

    /**
     * Take the scene and fill in a AudioEnvironmentData instance.
     *
     * @param scene The scene to take data from
     * @param envData Data instance to copy it to
ed
     */
    private void fillEnvData(SceneCullable scene,
                             AudioEnvironmentData envData)
    {
        ViewEnvironmentCullable view = scene.getViewCullable();
        envData.userData = scene.getUserData();

        RenderPassCullable pass = scene.getCullablePass(0);
        EnvironmentCullable vp = pass.getViewpointCullable();
        envData.viewpoint = (ObjectRenderable)vp.getRenderable();


        // walk back up the path to find the current root of
        // the scene graph and check that there is no SharedGroupCullable
        // instances along the way.
        Cullable parent = vp.getCullableParent();

        while(parent != null)
        {
            if(parent instanceof TransformCullable)
                currentViewpointPath.add(parent);

            if(parent instanceof GroupCullable)
            {
                if(((GroupCullable)parent).hasMultipleParents())
                {
                    I18nManager intl_mgr = I18nManager.getManager();

                    String msg = intl_mgr.getString(SHARED_VP_MSG_PROP);
                    throw new IllegalArgumentException(msg);
                }

                parent = ((GroupCullable)parent).getCullableParent();
            }
            else if(parent instanceof SingleCullable)
            {
                if(((SingleCullable)parent).hasMultipleParents())
                {
                    I18nManager intl_mgr = I18nManager.getManager();

                    String msg = intl_mgr.getString(SHARED_VP_MSG_PROP);
                    throw new IllegalArgumentException(msg);
                }

                parent = ((SingleCullable)parent).getCullableParent();
            }
            else
                parent = null;
        }

        int last_tx = currentViewpointPath.size();
        viewMatrix2.setIdentity();

        for(int i = last_tx - 1; i >= 0; i--)
        {
            TransformCullable c =
                (TransformCullable)currentViewpointPath.get(i);
            c.getTransform(viewMatrix1);
            viewMatrix2.mul(viewMatrix2, viewMatrix1);
        }

        currentViewpointPath.clear();

        envData.viewTransform.set(viewMatrix2);
    }

    /**
     * Resize the list if needed. Marked as final in order to encourage the
     * compiler to inline the code for faster execution
     */
    private void resizeCullList(int cur_size)
    {
        if((cur_size + 1) == workCullList.length)
        {
            int old_size = workCullList.length;
            int new_size = old_size + LIST_INCREMENT;

            AudioCullOutputDetails[] tmp_nodes = new AudioCullOutputDetails[new_size];

            System.arraycopy(workCullList, 0, tmp_nodes, 0, old_size);

            for(int i = old_size; i < new_size; i++)
                tmp_nodes[i] = new AudioCullOutputDetails();

            workCullList = tmp_nodes;
        }
    }

    /**
     * Resize the transform stack if needed. Marked as final in order to
     * encourage the compiler to inline the code for faster execution
     */
    private void resizeStack()
    {
        if((lastTxStack + 1) == transformStack.length)
        {
            int old_size = transformStack.length;
            int new_size = old_size + STACK_INCREMENT;

            Matrix4d[] tmp_matrix = new Matrix4d[new_size];

            System.arraycopy(transformStack, 0, tmp_matrix, 0, old_size);

            for(int i = old_size; i < new_size; i++)
                tmp_matrix[i] = new Matrix4d();

            transformStack = tmp_matrix;
        }
    }
}
