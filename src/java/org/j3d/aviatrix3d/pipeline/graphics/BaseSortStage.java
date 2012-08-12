/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2007
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
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.HashMap;

import javax.vecmath.Matrix4f;

import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;
import org.j3d.util.I18nManager;

// Local imports
//import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.rendering.*;

import org.j3d.aviatrix3d.pipeline.RenderOp;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsRequestData;

/**
 * Implementation of the common code needed by all sort stage implementations.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 3.19 $
 */
public abstract class BaseSortStage implements GraphicsSortStage
{
	/** Message for the numSurfaces < 0 */
	private static final String INVALID_SURFACE_COUNT_PROP =
		"org.j3d.aviatrix3d.pipeline.graphics.BaseSortStage.invalidSurfaceCountMsg";

    /** The size to grow the render list if needed */
    private static final int REALLOC_SIZE = 1024;

    /** The initial size of the instruction list */
    protected static final int LIST_START_SIZE = 1;

    /** Receiver for the output of this stage */
    private SortedGeometryReceiver receiver;

    /** Output array for passing on to the receiver */
    protected GraphicsInstructions[] commandList;

    /** A semi-unique ID counter used for assigning light IDs. May wrap */
    protected int lastGlobalId;

    /** Map of the first occurance of a scene parent to it's render instruction */
    protected HashMap<OffscreenBufferRenderable, GraphicsInstructions> instructionMap;

    /** Flag indicating a shutdown of the current processing is requested */
    protected boolean terminate;

    /** Local reporter to put errors in */
    protected ErrorReporter errorReporter;

    /**
     * Create an empty sorting stage that initialises the internal structures
     * to assume that there is a minumum number of surfaces, both on and
     * offscreen.
     *
     * @param numSurfaces The number of surfaces that we're likely to
     *    encounter. Must be a non-negative number
	 * @throws IllegalArgumentException numSurfaces was < 0
     */
    protected BaseSortStage(int numSurfaces)
    {
		if(numSurfaces < 0)
		{
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INVALID_SURFACE_COUNT_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Integer(numSurfaces) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
		}

        errorReporter = DefaultErrorReporter.getDefaultReporter();
        commandList = new GraphicsInstructions[numSurfaces];
        lastGlobalId = 0;

        for(int i = 0; i < numSurfaces; i++)
            commandList[i] = new GraphicsInstructions();

        instructionMap =
            new HashMap<OffscreenBufferRenderable, GraphicsInstructions>();
    }

    //---------------------------------------------------------------
    // Methods defined by GraphicsSortStage
    //---------------------------------------------------------------

    /**
     * Sort the listing of layers and nodes. Do not return until the
     * sort has been completed.
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
     * @param otherData data to be passed along unprocessed
     * @param profilingData The timing and load data
     * @param layers The list of layers that need to be further processed
     * @param numLayers The number of valid layers in each scene to process
     * @param numScenes The number of valid scenes to process
     * @param sceneParents Parent node that holds the subscene in the main
     *   scene graph
     */
    public void sort(GraphicsRequestData otherData,
                     GraphicsProfilingData profilingData,
                     ViewportCollection[][] layers,
                     int[] numLayers,
                     int numScenes,
                     OffscreenBufferRenderable[][] sceneParents)
    {
        long stime = System.nanoTime();

        // Buffer ID is not used currently.
        terminate = false;

        if(commandList.length < numScenes)
        {
            GraphicsInstructions[] tmp = new GraphicsInstructions[numScenes];

            System.arraycopy(commandList, 0, tmp, 0, commandList.length);

            for(int i = commandList.length; i < numScenes; i++)
                tmp[i] = new GraphicsInstructions();

            commandList = tmp;
        }

        // While doing these loops, we need to find the main scene and ensure
        // that it is the last item in the output list. Since everything starts
        // the other way around, we just reverse the order during processing.
        int main_idx = 0;
        int curr_scene = 0;

        for(int i = 0 ; i < numScenes && !terminate; i++)
        {
            commandList[i].ensureEnvDataCapacity(numLayers[i]);

            if(sceneParents[i][0] == null)
            {
                main_idx = i;
                continue;
            }

            commandList[curr_scene].pbuffer = sceneParents[i][0];
            commandList[curr_scene].parentSource = sceneParents[i][1];

            GraphicsInstructions original =
                (GraphicsInstructions)instructionMap.get(sceneParents[i][0]);

            if(original != null)
            {
                commandList[curr_scene].copyOf = original;
            }
            else
            {
                commandList[curr_scene].copyOf = null;
                sortSingleSurface(layers[i],
                                  numLayers[i],
                                  commandList[curr_scene],
                                  profilingData);

                instructionMap.put(sceneParents[i][0],
                                   commandList[curr_scene]);
            }

            curr_scene++;
        }

        if(terminate)
        {
            instructionMap.clear();
            return;
        }

        commandList[curr_scene].pbuffer = null;

        sortSingleSurface(layers[main_idx],
                          numLayers[main_idx],
                          commandList[curr_scene],
                          profilingData);

        if(terminate)
        {
            instructionMap.clear();
            return;
        }

        profilingData.sceneSortTime = System.nanoTime() - stime;

        if(receiver != null)
            receiver.sortedOutput(otherData, profilingData, commandList, numScenes);

        instructionMap.clear();
    }


    /**
     * Register a reciever for the output of the sorter. If the value is null,
     * it will clear the currently set receiver.
     *
     * @param sgr The receiver instance to add or null
     */
    public void setSortedGeometryReceiver(SortedGeometryReceiver sgr)
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

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Sort a single set of nodes into the output details of a single layer of
     * a single viewport and place in the provided GraphicsInstructions
     * instance. The implementation of this method should only concern itself
     * with this set of nodes and not worry about dealing with nested scenes or
     * other viewports.
     *
     * @param nodes The list of nodes to perform sorting on
     * @param numNodes The number of valid items in the nodes array
     * @param data The environment data used during sorting
     * @param instr Instruction instant to put the details into
     * @param instrCount Offset of current number of valid instructions
     * @return The current instruction count after sorting
     */
    protected abstract int sortNodes(GraphicsCullOutputDetails[] nodes,
                                     int numNodes,
                                     GraphicsEnvironmentData data,
                                     GraphicsInstructions instr,
                                     int instrCount);

    /**
     * Sort a single set of 2D nodes into the output details of a single layer
     * of a single viewport and place in the provided GraphicsInstructions
     * instance. The implementation of this method should only concern itself
     * with this set of nodes and not worry about dealing with nested scenes or
     * other viewports.
     *
     * @param nodes The list of nodes to perform sorting on
     * @param numNodes The number of valid items in the nodes array
     * @param data The environment data used during sorting
     * @param instr Instruction instant to put the details into
     * @param instrCount Offset of current number of valid instructions
     * @return The current instruction count after sorting
     */
    protected abstract int sort2DNodes(GraphicsCullOutputDetails[] nodes,
                                       int numNodes,
                                       GraphicsEnvironmentData data,
                                       GraphicsInstructions instr,
                                       int instrCount);

    /**
     * Estimate the required size of the instruction list needed for this scene
     * to be processed. This is an initial rough estimate that will be used to
     * make sure the arrays are at least big enough to start with. There is no
     * issue if this underestimates, as most sorting will continually check and
     * resize as needed. However, each resize is costly, so the closer this can
     * be to estimating the real size, the better for performance.
     *
     * @param scene The scene bucket to use for the source
     * @return A greater than zero value
     */
    protected abstract int estimateInstructionSize(SceneRenderBucket scene);

    /**
     * Estimate the required size of the instruction list needed for this scene
     * to be processed. This is an initial rough estimate that will be used to
     * make sure the arrays are at least big enough to start with. There is no
     * issue if this underestimates, as most sorting will continually check and
     * resize as needed. However, each resize is costly, so the closer this can
     * be to estimating the real size, the better for performance.
     *
     * @param scene The scene bucket to use for the source
     * @return A greater than zero value
     */
    protected abstract int estimateInstructionSize(MultipassRenderBucket scene);

    /**
     * Sort a single set of layers (one large scene) for rendering purposes.
     *
     * @param layers The set of layers for this surface
     * @param numLayers The number of valid layers to sort
     * @param instr Instruction instant to put the details into
     * @param profilingData The timing and load data
     */
    private void sortSingleSurface(ViewportCollection[] layers,
                                   int numLayers,
                                   GraphicsInstructions instr,
                                   GraphicsProfilingData profilingData)
    {
        int instr_count = 0;
        int view_count = 0;

        checkInstructionCapacity(layers, numLayers, instr);

        for(int i = 0; i < numLayers && !terminate; i++)
        {
            for(int j = 0; j < layers[i].numViewports && !terminate; j++)
            {
                ViewportLayerCollection vpl = layers[i].viewports[j];

                if (vpl.numBuckets == 0)
                   continue;

                instr.renderOps[instr_count] = RenderOp.START_VIEWPORT;
                instr.renderList[instr_count].clear();
                instr_count++;
                int s_cnt = 0;
                int m_cnt = 0;

                for(int k = 0; k < vpl.numBuckets && !terminate; k++)
                {
                    instr.renderOps[instr_count] = RenderOp.START_LAYER;
                    instr.renderList[instr_count].clear();
                    instr_count++;

                    // Base output on whether to use a normal or multipass
                    // scene.
                    switch(vpl.sceneType[k])
                    {
                        case ViewportLayerCollection.SINGLE_SCENE:
                            SceneRenderBucket b = vpl.scenes[s_cnt++];
                            instr.renderData[view_count++] = b.data;
                            profilingData.sceneSortInput += b.numNodes;
                            instr_count = sortNodes(b.nodes,
                                                    b.numNodes,
                                                    b.data,
                                                    instr,
                                                    instr_count);
                            break;

                        case ViewportLayerCollection.MULTIPASS_SCENE:
                            MultipassDetails md = vpl.multipass[m_cnt].mainScene;
                            instr.renderData[view_count++] = md.globalData;

                            // Copy in all the local view environments. Since
                            // these can be independent of the actual sorting, just
                            // copy direct right here.
                            for(int l = 0; l < md.numPasses; l++)
                                instr.renderData[view_count++] = md.data[l];

                            instr_count = sortMultipassLayer(vpl.multipass[m_cnt++],
                                                             instr,
                                                             instr_count,
                                                             profilingData);
                            break;

                        case ViewportLayerCollection.FLAT_SCENE:
                            b = vpl.scenes[s_cnt++];
                            instr.renderData[view_count++] = b.data;

                            instr_count = sort2DNodes(b.nodes,
                                                      b.numNodes,
                                                      b.data,
                                                      instr,
                                                      instr_count);
                            break;

                    }

                    instr.renderOps[instr_count] = RenderOp.STOP_LAYER;
                    instr.renderList[instr_count].clear();
                    instr_count++;
                }

                instr.renderOps[instr_count] = RenderOp.STOP_VIEWPORT;
                instr.renderList[instr_count].clear();
                instr_count++;
            }
        }

        instr.numValid = instr_count;

        for (int i = instr_count; i < instr.renderList.length; i++)
        {
        	// Early out as usually exits faster
        	if (instr.renderList[i].renderable == null &&
        	    instr.renderList[i].instructions == null) {

        	    break;
        	}
            instr.renderList[i].clear();
        }
    }

    /**
     * Convenience method to walk through all the layers, viewports etc and
     * determine a roughly correct size for the GraphicsInstruction list, and
     * resize as necessary.
     */
    private void checkInstructionCapacity(ViewportCollection[] layers,
                                          int numLayers,
                                          GraphicsInstructions instr)
    {
        int instr_count = 0;
        int data_count = 0;

        for(int i = 0; i < numLayers; i++)
        {
            for(int j = 0; j < layers[i].numViewports; j++)
            {
                ViewportLayerCollection vpl = layers[i].viewports[j];
                instr_count += 2; // start/stop layer commands

                data_count += vpl.numScenes;

                for(int k = 0; k < vpl.numScenes; k++)
                    instr_count += estimateInstructionSize(vpl.scenes[k]);

                if(vpl.numMultipass != 0)
                    instr_count += 2; // start/stop multipass

                for(int k = 0; k < vpl.numMultipass; k++)
                {
                    instr_count += estimateInstructionSize(vpl.multipass[k]);

                    // +1 for the globalData scene, plus one for each pass
                    data_count += vpl.multipass[k].mainScene.numPasses + 1;

                    // Doesn't handle multipass textures yet.
                }
            }
        }

        if(terminate)
            return;

        // Do not use the realloc() method here as it will do extra array copies
        // that we don't need at the start of the frame.
        if(instr.renderList.length < instr_count)
        {
            GraphicsDetails[] tmp = new GraphicsDetails[instr_count];

            System.arraycopy(instr.renderList,
                             0,
                             tmp,
                             0,
                             instr.renderList.length);

            for(int i = instr.renderList.length; i < instr_count; i++)
                tmp[i] = new GraphicsDetails();

            instr.renderList = tmp;
            instr.renderOps = new int[instr_count];
        }

        if(terminate)
            return;

        if(instr.renderData.length < data_count)
        {
            GraphicsEnvironmentData[] tmp =
                new GraphicsEnvironmentData[data_count];

            System.arraycopy(instr.renderData,
                             0,
                             tmp,
                             0,
                             instr.renderData.length);

            for(int i = instr.renderData.length; i < data_count; i++)
                tmp[i] = new GraphicsEnvironmentData();

            instr.renderData = tmp;
        }
    }

    /**
     * Reallocate the renderList and renderOps arrays.
     * Grows by a minimum of REALLOC_SIZE.
     *
     * @param instr The instructions to resize.
     * @param reqdSize The minimum required size
     */
    protected void realloc(GraphicsInstructions instr, int reqdSize)
    {
    	int req = reqdSize;
        if (reqdSize < instr.renderList.length)
            reqdSize = instr.renderList.length + REALLOC_SIZE;
        else if(reqdSize - instr.renderList.length < REALLOC_SIZE)
            reqdSize += REALLOC_SIZE;

        GraphicsDetails[] tmp = new GraphicsDetails[reqdSize];
        int[] roTmp = new int[reqdSize];

        System.arraycopy(instr.renderList, 0, tmp, 0, instr.renderList.length);
        System.arraycopy(instr.renderOps, 0, roTmp, 0, instr.renderOps.length);

        for(int i = instr.renderList.length; i < reqdSize; i++)
            tmp[i] = new GraphicsDetails();

        instr.renderList = tmp;
        instr.renderOps = roTmp;
    }

    /**
     * Sort a single set of nodes into the output details of a multipass layer
     * of a single viewport and place in the provided GraphicsInstructions
     * instance. The implementation of this method should only concern itself
     * with this scene and not worry about dealing with nested scenes or other
     * viewports.
     *
     * @param scene The scene instance to perform sorting on
     * @param instr Instruction instant to put the details into
     * @param instrCount Offset of current number of valid instructions
	 * GraphicsProfilingData profilingData
     * @return The current instruction count after sorting
     */
    private int sortMultipassLayer(MultipassRenderBucket scene,
                                   GraphicsInstructions instr,
                                   int instrCount,
								   GraphicsProfilingData profilingData)
    {
        MultipassDetails details = scene.mainScene;
        int idx = instrCount;

        instr.renderOps[idx] = RenderOp.START_MULTIPASS;
        instr.renderList[idx].clear();
        idx++;

        ViewportRenderable view_state = null;
        BufferStateRenderable general_state = null;
        BufferStateRenderable color_state = null;
        BufferStateRenderable stencil_state = null;
        BufferStateRenderable depth_state = null;
        BufferStateRenderable accum_state = null;

        for(int i = 0; i < details.numPasses && !terminate; i++)
        {
            // First load up all viewport changes
            if(details.buffers[i].viewportState != null)
            {
                if((view_state == null) ||
                   !details.buffers[i].viewportState.equals(view_state))
                {
                    instr.renderOps[idx] = RenderOp.SET_VIEWPORT_STATE;
                    instr.renderList[idx].renderable =
                        details.buffers[i].viewportState;
                    idx++;
                }
            }
            else if(view_state != null)
            {
                instr.renderOps[idx] = RenderOp.STOP_VIEWPORT_STATE;
                instr.renderList[idx].renderable = null;
                idx++;
            }

            // Next load up all the buffer changes
            if(details.buffers[i].generalBufferState != null)
            {
                if(general_state == null)
                {
                    instr.renderOps[idx] = RenderOp.START_BUFFER_STATE;
                    instr.renderList[idx].renderable =
                        details.buffers[i].generalBufferState;
                    idx++;
                }
                else if(!details.buffers[i].generalBufferState.equals(general_state))
                {
                    instr.renderOps[idx] = RenderOp.CHANGE_BUFFER_STATE;
                    instr.renderList[idx].renderable =
                        details.buffers[i].generalBufferState;
                    idx++;
                }
            }
            else if(general_state != null)
            {
                instr.renderOps[idx] = RenderOp.STOP_BUFFER_STATE;
                instr.renderList[idx].renderable = general_state;
                idx++;
            }

            if(details.buffers[i].colorBufferState != null)
            {
                if(color_state == null)
                {
                    instr.renderOps[idx] = RenderOp.START_BUFFER_STATE;
                    instr.renderList[idx].renderable =
                        details.buffers[i].colorBufferState;
                    idx++;
                }
                else if(!details.buffers[i].colorBufferState.equals(color_state))
                {
                    instr.renderOps[idx] = RenderOp.CHANGE_BUFFER_STATE;
                    instr.renderList[idx].renderable =
                        details.buffers[i].colorBufferState;
                    idx++;
                }
            }
            else if(color_state != null)
            {
                instr.renderOps[idx] = RenderOp.STOP_BUFFER_STATE;
                instr.renderList[idx].renderable = color_state;
                idx++;
            }

            if(details.buffers[i].stencilBufferState != null)
            {
                if(stencil_state == null)
                {
                    instr.renderOps[idx] = RenderOp.START_BUFFER_STATE;
                    instr.renderList[idx].renderable =
                        details.buffers[i].stencilBufferState;
                    idx++;
                }
                else if(!details.buffers[i].stencilBufferState.equals(stencil_state))
                {
                    instr.renderOps[idx] = RenderOp.CHANGE_BUFFER_STATE;
                    instr.renderList[idx].renderable =
                        details.buffers[i].stencilBufferState;
                    idx++;
                }
            }
            else if(stencil_state != null)
            {
                instr.renderOps[idx] = RenderOp.STOP_BUFFER_STATE;
                instr.renderList[idx].renderable = stencil_state;
                idx++;
            }

            // First load up all the buffer changes
            if(details.buffers[i].depthBufferState != null)
            {
                if(depth_state == null)
                {
                    instr.renderOps[idx] = RenderOp.START_BUFFER_STATE;
                    instr.renderList[idx].renderable =
                        details.buffers[i].depthBufferState;
                    idx++;
                }
                else if(!details.buffers[i].depthBufferState.equals(depth_state))
                {
                    instr.renderOps[idx] = RenderOp.CHANGE_BUFFER_STATE;
                    instr.renderList[idx].renderable =
                        details.buffers[i].depthBufferState;
                    idx++;
                }
            }
            else if(depth_state != null)
            {
                instr.renderOps[idx] = RenderOp.STOP_BUFFER_STATE;
                instr.renderList[idx].renderable = depth_state;
                idx++;
            }

            // Treat the accumulation buffer state a little differently.
            // If we don't have a state set already, we need to at least
            // clear the buffers, so call start state here.
            if(details.buffers[i].accumBufferState != null)
            {
                // If we have a state from the previous pass, and this one says
                // to clear the buffer, make sure that we tell the old state
                // to GL_RETURN, then set the clear bit.
                if(accum_state != null &&
                   details.buffers[i].accumBufferState.checkClearBufferState())
                {
                    instr.renderOps[idx] = RenderOp.STOP_BUFFER_STATE;
                    instr.renderList[idx].renderable =
                        details.buffers[i].accumBufferState;
                    idx++;
                }

                if(accum_state == null)
                {
                    instr.renderOps[idx] = RenderOp.START_BUFFER_STATE;
                    instr.renderList[idx].renderable =
                        details.buffers[i].accumBufferState;
                    idx++;
                }
                else
                {
                    instr.renderOps[idx] = RenderOp.SET_BUFFER_CLEAR;
                    instr.renderList[idx].renderable =
                        details.buffers[i].accumBufferState;
                    idx++;
                }
            }

            general_state = details.buffers[i].generalBufferState;
            color_state = details.buffers[i].colorBufferState;
            depth_state = details.buffers[i].depthBufferState;
            stencil_state = details.buffers[i].stencilBufferState;

            instr.renderOps[idx] = RenderOp.START_MULTIPASS_PASS;
            instr.renderList[idx].clear();
            idx++;

			profilingData.sceneSortInput += details.numNodes[i];

            idx = sortNodes(details.nodes[i],
                            details.numNodes[i],
                            details.data[i],
                            instr,
                            idx);

            instr.renderOps[idx] = RenderOp.STOP_MULTIPASS_PASS;
            instr.renderList[idx].clear();
            idx++;

            // Accumulation buffer goes at the end because we want to take the
            // current colour buffer and accumulate them now that the pass
            // is complete.
            if(details.buffers[i].accumBufferState != null)
            {
                // ignore the case when accum_state == null as the following
                // check will automatically include it anyway.
                instr.renderOps[idx] = RenderOp.CHANGE_BUFFER_STATE;
                instr.renderList[idx].renderable =
                    details.buffers[i].accumBufferState;
                idx++;
            }
            else if(accum_state != null)
            {
                instr.renderOps[idx] = RenderOp.STOP_BUFFER_STATE;
                instr.renderList[idx].renderable = accum_state;
                idx++;
            }

            accum_state = details.buffers[i].accumBufferState;
        }

        // Clean up all the states now.
        if(general_state != null)
        {
            instr.renderOps[idx] = RenderOp.STOP_BUFFER_STATE;
            instr.renderList[idx].renderable = general_state;
            idx++;
        }

        if(color_state != null)
        {
            instr.renderOps[idx] = RenderOp.STOP_BUFFER_STATE;
            instr.renderList[idx].renderable = color_state;
            idx++;
        }

        if(depth_state != null)
        {
            instr.renderOps[idx] = RenderOp.STOP_BUFFER_STATE;
            instr.renderList[idx].renderable = depth_state;
            idx++;
        }

        if(stencil_state != null)
        {
            instr.renderOps[idx] = RenderOp.STOP_BUFFER_STATE;
            instr.renderList[idx].renderable = stencil_state;
            idx++;
        }

        if(accum_state != null)
        {
            instr.renderOps[idx] = RenderOp.STOP_BUFFER_STATE;
            instr.renderList[idx].renderable = accum_state;
            idx++;
        }

        instr.renderOps[idx] = RenderOp.STOP_MULTIPASS;
        instr.renderList[idx].clear();
        idx++;

        return idx;
    }
}
