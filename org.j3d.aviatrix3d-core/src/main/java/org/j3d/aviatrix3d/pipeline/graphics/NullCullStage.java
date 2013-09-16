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


// Local imports
import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.rendering.*;
import org.j3d.maths.vector.Matrix4d;

/**
 * Handles the scenegraph maintenance and culling operations.
 * <p>
 *
 * The culling phase generates a list of nodes to render.
 * A future optimization will sort the render list by OGL state.
 * <p>
 *
 * By default, the implementation will walk into shape3Ds looking for any
 * offscreen textures to be rendered. If you know that you do not have any
 * in the scene, then you can set an internal flag to not look for them, thus
 * achieving a simple performance boost.
 * <p>
 *
 * Since this is a null culling stage, and thus no culling is performed, if the
 * scene graph contains instances of CustomCullable, they are ignored and traversal
 * stops at that point.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 3.9 $
 */
public class NullCullStage extends BaseCullStage
{
    /**
     * Create a basic instance of this class with the list assuming there are
     * no off-screen buffers in use for the initial internal setup.
     */
    public NullCullStage()
    {
        this(LIST_START_LENGTH);
    }

    /**
     * Create a basic instance of this class with the list initial internal
     * setup for the given number of renderable surfaces. The size is just an
     * initial esstimate, and is used for optimisation purposes to prevent
     * frequent array reallocations internally. As such, the number does not
     * have to be perfect, just good enough.
     *
     * @param numSurfaces Total number of surfaces to prepare rendering for
     */
    public NullCullStage(int numSurfaces)
    {
        super(numSurfaces);
    }

    //---------------------------------------------------------------
    // Methods defined by BaseCullStage
    //---------------------------------------------------------------

    /**
     * Update and cull the scenegraph. This generates an ordered list
     * of nodes to render. It will not return until the culling is complete.
     *
     * @param scene The scene instance to cull
     * @param viewIndex The index of the viewport in the parent
     *    ViewportCollection
     * @param layerIndex The index of the layer within the viewport
     */
    protected void cullScene(RenderPassCullable scene,
                             int subsceneId,
                             int layerId,
                             int viewIndex,
                             int layerIndex)
    {
        // Buffer ID is not used currently.
        lastLight = 0;
        lastClip = 0;
        lastTxStack = 0;
        lastFogStack = 0;
        activeParent = null;

        SceneRenderBucket bucket =
            workLayers[subsceneId][layerId].viewports[viewIndex].scenes[layerIndex];

        workCullList = bucket.nodes;
        validSceneParents[0][0] = null;
        validSceneParents[0][1] = null;

        Cullable node = scene.getRootCullable();
        bucket.data.layerId = layerId;
        bucket.data.subLayerId = layerIndex;

        if(terminate)
            return;

        if(node instanceof GroupCullable)
        {
            // set the top matrix stack
            if(node instanceof TransformCullable)
                ((TransformCullable)node).getTransform(transformStack[0]);
            else
                transformStack[0].setIdentity();

            bucket.numNodes = findAllNodes((GroupCullable)node, 0);
            bucket.nodes = workCullList;
        }
        else if(node instanceof SingleCullable)
        {
            // set the top matrix stack back to identity
            transformStack[0].setIdentity();
            bucket.numNodes = findNextNode((SingleCullable)node,  0);
            bucket.nodes = workCullList;
        }
        else if(node instanceof LeafCullable)
        {
            LeafCullable cullable = (LeafCullable)node;
            // if it is not geometry, nothing to do here.
            if(cullable.getCullableType() != LeafCullable.GEOMETRY_CULLABLE)
                return;

            Renderable r = cullable.getRenderable();

            // Walk into the shape and check that we don't have any
            // offscreen textures to render.

            if((r instanceof ShapeRenderable) && ((ShapeRenderable)r).is2D())
            {
                if(checkOffscreens)
                    checkForOffscreens((ShapeRenderable)r);

                workCullList[0].renderable = r;
                workCullList[0].transform.setIdentity();
                workCullList[0].numLights = 0;
                workCullList[0].numClipPlanes = 0;

                bucket.numNodes = 1;
                bucket.nodes = workCullList;
            }
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
    protected void cullScene2D(RenderPassCullable scene,
                               int subsceneId,
                               int layerId,
                               int viewIndex,
                               int layerIndex)
    {
        // Buffer ID is not used currently.
        lastLight = 0;
        lastClip = 0;
        lastTxStack = 0;
        lastFogStack = 0;
        activeParent = null;

        SceneRenderBucket bucket =
            workLayers[subsceneId][layerId].viewports[viewIndex].scenes[layerIndex];

        workCullList = bucket.nodes;
        validSceneParents[0][0] = null;
        validSceneParents[0][1] = null;

        Cullable node = scene.getRootCullable();
        bucket.data.layerId = layerId;
        bucket.data.subLayerId = layerIndex;

        if(terminate)
            return;

        if(node instanceof GroupCullable)
        {
            // set the top matrix stack
            if(node instanceof TransformCullable)
                ((TransformCullable)node).getTransform(transformStack[0]);
            else
                transformStack[0].setIdentity();

            bucket.numNodes = findAllNodes((GroupCullable)node, 0);
            bucket.nodes = workCullList;
        }
        else if(node instanceof SingleCullable)
        {
            // set the top matrix stack back to identity
            transformStack[0].setIdentity();
            bucket.numNodes = findNextNode((SingleCullable)node,  0);
            bucket.nodes = workCullList;
        }
        else if(node instanceof LeafCullable)
        {
            LeafCullable cullable = (LeafCullable)node;
            // if it is not geometry, nothing to do here.
            if(cullable.getCullableType() != LeafCullable.GEOMETRY_CULLABLE)
                return;

            Renderable r = cullable.getRenderable();

            if((r instanceof ShapeRenderable) && ((ShapeRenderable)r).is2D())
            {
                if(checkOffscreens)
                    checkForOffscreens((ShapeRenderable)r);

                workCullList[0].renderable = r;
                workCullList[0].transform.setIdentity();
                workCullList[0].numLights = 0;
                workCullList[0].numClipPlanes = 0;

                bucket.numNodes = 1;
                bucket.nodes = workCullList;
            }
        }
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
    protected void cullRenderPass(RenderPassCullable pass,
                                  int passNumber,
                                  int subsceneId,
                                  int layerId,
                                  int viewIndex,
                                  int layerIndex)
    {
        lastLight = 0;
        lastClip = 0;
        lastTxStack = 0;
        lastFogStack = 0;
        activeParent = null;

        // Assumes main scene only. Doesn't yet handle nested texture rendering
        ViewportLayerCollection c =
            workLayers[subsceneId][layerId].viewports[viewIndex];

        MultipassDetails bucket =
            c.multipass[c.numMultipass].mainScene;

        workCullList = bucket.nodes[passNumber];
        validSceneParents[0][0] = null;
        validSceneParents[0][1] = null;

        Cullable node = pass.getRootCullable();
        fillRenderPassEnvData(pass,
                              bucket.data[passNumber],
                              bucket.buffers[passNumber]);

        bucket.data[passNumber].layerId = layerId;
        bucket.data[passNumber].subLayerId = layerIndex;

        if(terminate)
            return;

        if(node instanceof GroupCullable)
        {
            // set the top matrix stack back to identity
            if(node instanceof TransformCullable)
                ((TransformCullable)node).getTransform(transformStack[0]);
            else
                transformStack[0].setIdentity();

            bucket.numNodes[passNumber] = findAllNodes((GroupCullable)node, 0);
            bucket.nodes[passNumber] = workCullList;
        }
        else if(node instanceof SingleCullable)
        {
            // set the top matrix stack back to identity
            transformStack[0].setIdentity();

            bucket.numNodes[passNumber] = findNextNode((SingleCullable)node,  0);
            bucket.nodes[passNumber] = workCullList;
        }
        else if(node instanceof LeafCullable)
        {
            LeafCullable cullable = (LeafCullable)node;
            // if it is not geometry, nothing to do here.
            if(cullable.getCullableType() != LeafCullable.GEOMETRY_CULLABLE)
                return;

            Renderable r = cullable.getRenderable();

            if((r instanceof ShapeRenderable) && ((ShapeRenderable)r).is2D())
            {
                if(checkOffscreens)
                    checkForOffscreens((ShapeRenderable)r);

                workCullList[0].renderable = r;
                workCullList[0].transform.setIdentity();
                workCullList[0].numLights = 0;
                workCullList[0].numClipPlanes = 0;

                bucket.numNodes[passNumber] = 1;
                bucket.nodes[passNumber] = workCullList;
            }
        }
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

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

        int num_lights = 0;
        int num_clips = 0;
        int ret_val = cullEndIndex;
        boolean have_local_fog = false;

        // Find all the lights and clip planes at this level first. Place
        // them first into the queue
        for(int i = 0; i < size; i++)
        {
            if(!(kids[i] instanceof LeafCullable))
                continue;

            LeafCullable cullable = (LeafCullable)kids[i];
            Renderable r = cullable.getRenderable();

            if(!(r instanceof EffectRenderable))
                continue;

            EffectRenderable effect = (EffectRenderable)r;

            if(!effect.isEnabled())
                continue;

            // TODO:
            // Need to handle global options for lights and clip planes

            switch(cullable.getCullableType())
            {
                case LeafCullable.LIGHT_CULLABLE:
                    resizeLightList();
                    lightList[lastLight] = effect;

                    Matrix4d mat = transformStack[lastTxStack];

                    // Transpose the matrix in place as it is being copied
                    lightTxList[lastLight][0] = (float)mat.m00;
                    lightTxList[lastLight][1] = (float)mat.m10;
                    lightTxList[lastLight][2] = (float)mat.m20;
                    lightTxList[lastLight][3] = (float)mat.m30;

                    lightTxList[lastLight][4] = (float)mat.m01;
                    lightTxList[lastLight][5] = (float)mat.m11;
                    lightTxList[lastLight][6] = (float)mat.m21;
                    lightTxList[lastLight][7] = (float)mat.m31;

                    lightTxList[lastLight][8] = (float)mat.m02;
                    lightTxList[lastLight][9] = (float)mat.m12;
                    lightTxList[lastLight][10] = (float)mat.m22;
                    lightTxList[lastLight][11] = (float)mat.m32;

                    lightTxList[lastLight][12] = (float)mat.m03;
                    lightTxList[lastLight][13] = (float)mat.m13;
                    lightTxList[lastLight][14] = (float)mat.m23;
                    lightTxList[lastLight][15] = (float)mat.m33;

                    lastLight++;
                    num_lights++;
                    break;

                case LeafCullable.CLIP_CULLABLE:
                    resizeClipList();
                    clipList[lastClip] = effect;

                    mat = transformStack[lastTxStack];

                    // Transpose the matrix in place as it is being copied
                    clipTxList[lastClip][0] = (float)mat.m00;
                    clipTxList[lastClip][1] = (float)mat.m10;
                    clipTxList[lastClip][2] = (float)mat.m20;
                    clipTxList[lastClip][3] = (float)mat.m30;

                    clipTxList[lastClip][4] = (float)mat.m01;
                    clipTxList[lastClip][5] = (float)mat.m11;
                    clipTxList[lastClip][6] = (float)mat.m21;
                    clipTxList[lastClip][7] = (float)mat.m31;

                    clipTxList[lastClip][8] = (float)mat.m02;
                    clipTxList[lastClip][9] = (float)mat.m12;
                    clipTxList[lastClip][10] = (float)mat.m22;
                    clipTxList[lastClip][11] = (float)mat.m32;

                    clipTxList[lastClip][12] = (float)mat.m03;
                    clipTxList[lastClip][13] = (float)mat.m13;
                    clipTxList[lastClip][14] = (float)mat.m23;
                    clipTxList[lastClip][15] = (float)mat.m33;

                    lastClip++;
                    num_clips++;
                    break;

                case LeafCullable.FOG_CULLABLE:
                    if(!effect.isGlobalOnly() && !have_local_fog)
                    {
                        resizeFogStack();

                        have_local_fog = true;
                        lastFogStack++;
                        fogStack[lastFogStack] = effect;
                    }
                    break;
            }
        }

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
                LeafCullable cullable = (LeafCullable)kids[i];

                if(cullable.getCullableType() == LeafCullable.GEOMETRY_CULLABLE)
                {
                    Renderable r = cullable.getRenderable();

                    if((r instanceof ShapeRenderable) &&
                       ((ShapeRenderable)r).isVisible())
                    {
                        // Check the visibility state and ignore if not visible.
                        if(((ShapeRenderable)r).is2D())
                        {
                            resizeCullList(ret_val);
                            workCullList[ret_val].renderable = r;
                            workCullList[ret_val].localFog = null;

                            Matrix4d mat = transformStack[lastTxStack];

                            // Transpose the matrix in place as it is being copied
                            workCullList[ret_val].transform.set(mat);
                            workCullList[ret_val].numLights = 0;
                            workCullList[ret_val].numClipPlanes = 0;

                            ret_val++;
                        }
                        else
                        {
                            if(checkOffscreens)
                                checkForOffscreens((ShapeRenderable)r);

                            resizeCullList(ret_val);
                            workCullList[ret_val].renderable = r;
                            workCullList[ret_val].localFog = fogStack[lastFogStack];

                            Matrix4d mat = transformStack[lastTxStack];

                            // Transpose the matrix in place as it is being copied
                            workCullList[ret_val].transform.set(mat);
                            int src_size = (workCullList[ret_val].lights == null) ?
                                            0 :
                                            workCullList[ret_val].lights.length;

                            if(src_size < lastLight)
                            {
                                // up the size of the array
                                VisualDetails[] tmp = new VisualDetails[lastLight];
                                if(src_size != 0)
                                {
                                    System.arraycopy(workCullList[ret_val].lights,
                                                     0,
                                                     tmp,
                                                     0,
                                                     src_size);
                                }

                                for(int j = src_size; j < lastLight; j++)
                                    tmp[j] = new VisualDetails();

                                workCullList[ret_val].lights = tmp;
                            }

                            // copy in the light information.
                            VisualDetails[] l_tmp = workCullList[ret_val].lights;
                            workCullList[ret_val].numLights = lastLight;

                            for(int j = 0; j < lastLight; j++)
                                l_tmp[j].update(lightList[j], lightTxList[j]);

                            // Same thing again but for clip planes
                            src_size = (workCullList[ret_val].clipPlanes == null) ?
                                        0 :
                                        workCullList[ret_val].clipPlanes.length;

                            if(src_size < lastClip)
                            {
                                // up the size of the array
                                VisualDetails[] tmp = new VisualDetails[lastClip];
                                if(src_size != 0)
                                {
                                    System.arraycopy(workCullList[ret_val].clipPlanes,
                                                     0,
                                                     tmp,
                                                     0,
                                                     src_size);
                                }

                                for(int j = src_size; j < lastClip; j++)
                                    tmp[j] = new VisualDetails();

                                workCullList[ret_val].clipPlanes = tmp;
                            }

                            // copy in the light information.
                            VisualDetails[] c_tmp = workCullList[ret_val].clipPlanes;
                            workCullList[ret_val].numClipPlanes = lastClip;

                            for(int j = 0; j < lastClip; j++)
                                c_tmp[j].update(clipList[j], clipTxList[j]);

                            ret_val++;
                        }
                    } // not visible
                } // Not a cullable geom
            }

            // Now pop the stacks
            if(is_tx)
                lastTxStack--;
        }

        // Pop the valid lights from the stack
        lastLight -= num_lights;
        lastClip -= num_clips;

        if(have_local_fog)
        {
            fogStack[lastFogStack] = null;
            lastFogStack--;
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

        // Now just continue on normally. If the result of the above was a
        // null then it just falls off the end of this if/else naturally.
        if(current instanceof GroupCullable)
        {
            ret_val = findAllNodes((GroupCullable)current, cullEndIndex);
        }
        else if(current instanceof LeafCullable)
        {
            LeafCullable cullable = (LeafCullable)current;

            if(cullable.getCullableType() == LeafCullable.GEOMETRY_CULLABLE)
            {
                Renderable r = cullable.getRenderable();

                if((r instanceof ShapeRenderable) &&
                   ((ShapeRenderable)r).isVisible())
                {
                    // Check the visibility state and ignore if not visible.
                    if(((ShapeRenderable)r).is2D())
                    {
                        resizeCullList(ret_val);
                        workCullList[ret_val].renderable = r;
                        workCullList[ret_val].localFog = null;

                        Matrix4d mat = transformStack[lastTxStack];

                        // Transpose the matrix in place as it is being copied
                        workCullList[ret_val].transform.set(mat);
                        workCullList[ret_val].numLights = 0;
                        workCullList[ret_val].numClipPlanes = 0;

                        ret_val++;
                    }
                    else
                    {
                        // Walk into the shape and check that we don't have any
                        // offscreen textures to render.
                        if(checkOffscreens)
                            checkForOffscreens((ShapeRenderable)current);

                        resizeCullList(cullEndIndex);
                        workCullList[cullEndIndex].renderable = r;
                        workCullList[cullEndIndex].localFog = fogStack[lastFogStack];

                        Matrix4d mat = transformStack[lastTxStack];

                        // Transpose the matrix in place as it is being copied
                        workCullList[cullEndIndex].transform.set(mat);

                        int src_size = (workCullList[cullEndIndex].lights == null) ?
                                        0 :
                                        workCullList[cullEndIndex].lights.length;

                        if(src_size < lastLight)
                        {
                            // up the size of the array

                            VisualDetails[] tmp = new VisualDetails[lastLight];
                            if(src_size != 0)
                            {
                                System.arraycopy(workCullList[cullEndIndex].lights,
                                                 0,
                                                 tmp,
                                                 0,
                                                 src_size);
                            }

                            for(int j = src_size; j < lastLight; j++)
                                tmp[j] = new VisualDetails();

                            workCullList[cullEndIndex].lights = tmp;
                        }

                        // copy in the light information.
                        VisualDetails[] l_tmp = workCullList[cullEndIndex].lights;
                        workCullList[cullEndIndex].numLights = lastLight;

                        for(int j = 0; j < lastLight; j++)
                            l_tmp[j].update(lightList[j], lightTxList[j]);

                        // Same again, but with clip information
                        src_size = (workCullList[cullEndIndex].clipPlanes == null) ?
                                   0 :
                                   workCullList[cullEndIndex].clipPlanes.length;

                        if(src_size < lastClip)
                        {
                            // up the size of the array

                            VisualDetails[] tmp = new VisualDetails[lastClip];
                            if(src_size != 0)
                            {
                                System.arraycopy(workCullList[cullEndIndex].clipPlanes,
                                                 0,
                                                 tmp,
                                                 0,
                                                 src_size);
                            }

                            for(int j = src_size; j < lastClip; j++)
                                tmp[j] = new VisualDetails();

                            workCullList[cullEndIndex].clipPlanes = tmp;
                        }

                        // copy in the clip information.
                        VisualDetails[] c_tmp = workCullList[cullEndIndex].clipPlanes;
                        workCullList[cullEndIndex].numClipPlanes = lastClip;

                        for(int j = 0; j < lastClip; j++)
                            c_tmp[j].update(clipList[j], clipTxList[j]);

                        cullEndIndex++;

                        ret_val++;
                    }
                }
            }
        }

        // Now pop the stack.
        if(is_tx)
            lastTxStack--;

        return ret_val;
    }
}
