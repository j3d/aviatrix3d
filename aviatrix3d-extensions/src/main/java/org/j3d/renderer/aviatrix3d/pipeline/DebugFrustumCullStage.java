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

package org.j3d.renderer.aviatrix3d.pipeline;

// External imports
import org.j3d.maths.vector.*;

// Local imports
import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.pipeline.graphics.*;
import org.j3d.aviatrix3d.rendering.*;
import org.j3d.util.MatrixUtils;

/**
 * Implementation of view frustum culling that provides visual debugging
 * information in the scene.
 * <p>
 *
 * For each object in the scene, this will render a box around its bounds,
 * including each grouping node in the scene heirarchy.
 *
 * @author Alan Hudson
 * @version $Revision: 1.5 $
 */
public class DebugFrustumCullStage extends BaseCullStage
{
    /** Working var for the view frustum */
    private double[] viewFrustum;

    /** Working var to calculate the view matrix */
    private Matrix4d viewMatrix;

    /** The 8 bounding points of the frustum volume */
    private Point4d[] frustumPoints;

    /** The planes describing this frustum */
    private Vector4d[] frustumPlanes;

    /** Projection matrix used to generate the frustum planes with. */
    private Matrix4d prjMatrix;

    /** Temporary used to fetch the minimum extents of a bounds object */
    private float[] t1;

    /** Temporary used to fetch the maximum extents of a bounds object */
    private float[] t2;

    /** Temporary used to fetch the center of a bounds object */
    private float[] c1;

    /** Temporary used to fetch the size of a bounds object */
    private float[] c2;

    /** Current field of view saved from the setupEnvData methods */
    private float fieldOfView;

    /** Current viewport aspectRatio saved from the setupEnvData methods */
    private float aspectRatio;

    /** Flag to say whether to trash the viewpoint position or not */
    private boolean globalViewpoint;

    /** Utility for messing about with matrix manipulation */
    private MatrixUtils matrixUtils;

    /**
     * Create a basic instance of this class with the list assuming there are
     * no off-screen buffers in use for the initial internal setup.
     *
     * @param useGlobalView The rendered viewpoint is set to look down on the
     *    entire scene if set true, otherwise uses the normal value
     */
    public DebugFrustumCullStage(boolean useGlobalView)
    {
        this(LIST_START_LENGTH, useGlobalView);
    }

    /**
     * Create a basic instance of this class with the list initial internal
     * setup for the given number of renderable surfaces. The size is just an
     * initial esstimate, and is used for optimisation purposes to prevent
     * frequent array reallocations internally. As such, the number does not
     * have to be perfect, just good enough.
     *
     * @param numSurfaces Total number of surfaces to prepare rendering for
     * @param useGlobalView The rendered viewpoint is set to look down on the
     *    entire scene if set true, otherwise uses the normal value
     */
    public DebugFrustumCullStage(int numSurfaces, boolean useGlobalView)
    {
        super(numSurfaces);

        viewMatrix = new Matrix4d();
        prjMatrix = new Matrix4d();
        frustumPoints = new Point4d[8];
        for(int i=0; i < 8; i++)
            frustumPoints[i] = new Point4d();

        frustumPlanes = new Vector4d[6];
        for(int i=0; i < 6; i++)
            frustumPlanes[i] = new Vector4d();

        t1 = new float[3];
        t2 = new float[3];
        c1 = new float[3];
        c2 = new float[3];
        globalViewpoint = useGlobalView;

        matrixUtils = new MatrixUtils();
    }

    //---------------------------------------------------------------
    // Methods defined by BaseCullStage
    //---------------------------------------------------------------

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
    @Override
    protected void cullScene(RenderPassCullable scene,
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

        SceneRenderBucket bucket =
            outputLayers[subsceneId][layerId].viewports[viewIndex].scenes[layerIndex];

        workCullList = bucket.nodes;
        validSceneParents[0][0] = null;
        validSceneParents[0][1] = null;

        Cullable node = scene.getRootCullable();
        bucket.data.layerId = layerId;
        bucket.data.subLayerId = layerIndex;

        if(terminate)
            return;

        if(node instanceof TransformCullable)
            ((TransformCullable)node).getTransform(transformStack[0]);
        else
            transformStack[0].setIdentity();

        workCullList[0].renderable =
            new DebugFrustumRenderable(fieldOfView,
                                       aspectRatio,
                                       bucket.data.viewFrustum);


        matrixUtils.inverse(bucket.data.viewTransform, viewMatrix2);
        workCullList[0].transform.set(viewMatrix2);

        workCullList[0].numLights = 0;
        workCullList[0].numClipPlanes = 0;

        if(node instanceof GroupCullable)
        {
            viewFrustum = bucket.data.viewFrustum;
            viewMatrix.set(bucket.data.viewTransform);

            updateFrustum(bucket.data);

            bucket.numNodes =
                findAllNodes((GroupCullable)node, false, false, 1);
            bucket.nodes = workCullList;
        }
        else if(node instanceof SingleCullable)
        {
            viewFrustum = bucket.data.viewFrustum;
            viewMatrix.set(bucket.data.viewTransform);

            updateFrustum(bucket.data);

            bucket.numNodes =
                findNextNode((SingleCullable)node, false, false, 1);
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
                workCullList[1].transform.setIdentity();

                BoundingVolume bounds = cullable.getBounds();
                bounds.getExtents(t1, t2);
                bounds.getCenter(c1);
                ((BoundingBox)bounds).getSize(c2);
                r = new DebugShapeRenderable(false,
                                             t1,
                                             t2,
                                             c1,
                                             c2,
                                             workCullList[1].transform);

                if(checkOffscreens)
                    checkForOffscreens((ShapeRenderable)r);

                workCullList[1].renderable = r;
                workCullList[1].transform.setIdentity();
                workCullList[1].numLights = 0;
                workCullList[1].numClipPlanes = 0;

                bucket.numNodes = 2;
                bucket.nodes = workCullList;
            }
        }

        if(globalViewpoint)
        {
            matrixUtils.rotateX((float)(Math.PI * -0.5), viewMatrix);
            viewMatrix.m03 = 0;
            viewMatrix.m13 = 100;
            viewMatrix.m23 = 0;
            viewMatrix.m33 = 1;

            matrixUtils.inverse(viewMatrix, viewMatrix2);

            bucket.data.cameraTransform[0] = viewMatrix2.m00;
            bucket.data.cameraTransform[1] = viewMatrix2.m10;
            bucket.data.cameraTransform[2] = viewMatrix2.m20;
            bucket.data.cameraTransform[3] = viewMatrix2.m30;
            bucket.data.cameraTransform[4] = viewMatrix2.m01;
            bucket.data.cameraTransform[5] = viewMatrix2.m11;
            bucket.data.cameraTransform[6] = viewMatrix2.m21;
            bucket.data.cameraTransform[7] = viewMatrix2.m31;
            bucket.data.cameraTransform[8] = viewMatrix2.m02;
            bucket.data.cameraTransform[9] = viewMatrix2.m12;
            bucket.data.cameraTransform[10] = viewMatrix2.m22;
            bucket.data.cameraTransform[11] = viewMatrix2.m32;
            bucket.data.cameraTransform[12] = viewMatrix2.m03;
            bucket.data.cameraTransform[13] = viewMatrix2.m13;
            bucket.data.cameraTransform[14] = viewMatrix2.m23;
            bucket.data.cameraTransform[15] = viewMatrix2.m33;
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
    @Override
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
            outputLayers[subsceneId][layerId].viewports[viewIndex].scenes[layerIndex];

        workCullList = bucket.nodes;
        validSceneParents[0][0] = null;
        validSceneParents[0][1] = null;

        Cullable node = scene.getRootCullable();
        bucket.data.layerId = layerId;
        bucket.data.subLayerId = layerIndex;

        if(terminate)
            return;

        if(node instanceof TransformCullable)
            ((TransformCullable)node).getTransform(transformStack[0]);
        else
            transformStack[0].setIdentity();

        workCullList[0].renderable =
            new DebugFrustumRenderable(fieldOfView,
                                       aspectRatio,
                                       bucket.data.viewFrustum);

        workCullList[0].transform.set(bucket.data.viewTransform);
        workCullList[0].numLights = 0;
        workCullList[0].numClipPlanes = 0;

        if(node instanceof GroupCullable)
        {
            viewFrustum = bucket.data.viewFrustum;
            viewMatrix.set(bucket.data.viewTransform);

            updateFrustum(bucket.data);

            bucket.numNodes =
                findAllNodes((GroupCullable)node, false, false, 1);
            bucket.nodes = workCullList;
        }
        else if(node instanceof SingleCullable)
        {
            viewFrustum = bucket.data.viewFrustum;
            viewMatrix.set(bucket.data.viewTransform);

            updateFrustum(bucket.data);

            bucket.numNodes =
                findNextNode((SingleCullable)node, false, false, 1);
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
                workCullList[1].transform.setIdentity();

                BoundingVolume bounds = cullable.getBounds();
                bounds.getExtents(t1, t2);
                bounds.getCenter(c1);
                ((BoundingBox)bounds).getSize(c2);
                r = new DebugShapeRenderable(false, t1, t2, c1, c2, workCullList[1].transform);

                if(checkOffscreens)
                    checkForOffscreens((ShapeRenderable)r);

                workCullList[1].renderable = r;
                workCullList[1].transform.setIdentity();
                workCullList[1].numLights = 0;
                workCullList[1].numClipPlanes = 0;

                bucket.numNodes = 2;
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
    @Override
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
            outputLayers[subsceneId][layerId].viewports[viewIndex];

        MultipassDetails bucket = c.multipass[c.numMultipass].mainScene;

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

        if(node instanceof TransformCullable)
            ((TransformCullable)node).getTransform(transformStack[0]);
        else
            transformStack[0].setIdentity();

        workCullList[0].renderable =
            new DebugFrustumRenderable(fieldOfView,
                                       aspectRatio,
                                       bucket.data[passNumber].viewFrustum);

        workCullList[0].transform.set(bucket.data[passNumber].viewTransform);
        workCullList[0].numLights = 0;
        workCullList[0].numClipPlanes = 0;

        if(node instanceof GroupCullable)
        {
            viewFrustum = bucket.data[passNumber].viewFrustum;
            viewMatrix.set(bucket.data[passNumber].viewTransform);

            updateFrustum(bucket.data[passNumber]);

            bucket.numNodes[passNumber] =
                findAllNodes((GroupCullable)node, false, false, 1);
            bucket.nodes[passNumber] = workCullList;
        }
        else if(node instanceof SingleCullable)
        {
            viewFrustum = bucket.data[passNumber].viewFrustum;
            viewMatrix.set(bucket.data[passNumber].viewTransform);

            updateFrustum(bucket.data[passNumber]);

            bucket.numNodes[passNumber] =
                findNextNode((SingleCullable)node, false, false, 1);
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
                workCullList[1].transform.setIdentity();

                BoundingVolume bounds = cullable.getBounds();
                bounds.getExtents(t1, t2);
                bounds.getCenter(c1);
                ((BoundingBox)bounds).getSize(c2);
                r = new DebugShapeRenderable(false, t1, t2, c1, c2, workCullList[1].transform);

                if(checkOffscreens)
                    checkForOffscreens((ShapeRenderable)r);

                workCullList[1].renderable = r;
                workCullList[1].transform.setIdentity();
                workCullList[1].numLights = 0;
                workCullList[1].numClipPlanes = 0;

                bucket.numNodes[passNumber] = 2;
                bucket.nodes[passNumber] = workCullList;
            }
        }
    }

    /**
     * Take a simple scene and fill in a GraphicsEnvironmentData instance.
     *
     * @param scene The scene to take data from
     * @param envData Data instance to copy it to
     */
    @Override
    protected void fillSingleEnvData(SceneCullable scene,
                                     GraphicsEnvironmentData envData)
    {
        super.fillSingleEnvData(scene, envData);

        ViewEnvironmentCullable view = scene.getViewCullable();
        fieldOfView = (float)view.getFieldOfView();

        int[] dim = view.getViewportDimensions();
        aspectRatio = dim[2] / (float)dim[3];
    }

    /**
     * Take a simple scene and fill in a GraphicsEnvironmentData instance.
     *
     * @param scene The scene to take data from
     * @param envData Data instance to copy it to
     */
    @Override
    protected void fillMultipassEnvData(SceneCullable scene,
                                        GraphicsEnvironmentData envData)
    {
        super.fillMultipassEnvData(scene, envData);

        ViewEnvironmentCullable view = scene.getViewCullable();
        fieldOfView = (float)view.getFieldOfView();

        int[] dim = view.getViewportDimensions();
        aspectRatio = dim[2] / (float)dim[3];
    }

    /**
     * Take a simple scene and fill in a GraphicsEnvironmentData instance.
     *
     * @param pass The render pass to take data from
     * @param envData Data instance to copy it to
     * @param bufferData The object to copy the extra buffer state to
     */
    @Override
    protected void fillRenderPassEnvData(RenderPassCullable pass,
                                         GraphicsEnvironmentData envData,
                                         BufferDetails bufferData)
    {
        super.fillRenderPassEnvData(pass, envData, bufferData);

        ViewEnvironmentCullable view = pass.getViewCullable();
        fieldOfView = (float)view.getFieldOfView();

        int[] dim = view.getViewportDimensions();
        aspectRatio = dim[2] / (float)dim[3];
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Recursive walk of the tree to find all the renderable nodes.
     *
     * @param group The parent node to walk into
     * @param cullEndIndex The current last item on the cull list
     * @param allInBounds All of this group is inside the view frustum
     * @param ignoreTrans Ignore the last transformation
     * @return The index of the last item on the cull list
     */
    private int findAllNodes(GroupCullable group,
                             boolean allInBounds,
                             boolean ignoreTrans,
                             int cullEndIndex)
    {
        if(terminate)
            return 0;

        BoundingVolume bounds = group.getBounds();
        boolean childAllInBounds = false;

        int matIdx;
        if(ignoreTrans)
            matIdx = lastTxStack - 1;
        else
            matIdx = lastTxStack;

        Matrix4d group_mat = transformStack[matIdx];
        int ret_val = cullEndIndex;

        if(!allInBounds)
        {
            int result = bounds.checkIntersectionFrustum(frustumPlanes,
                                                         group_mat);

            bounds.getCenter(c1);

            if(bounds instanceof BoundingBox)
                ((BoundingBox)bounds).getSize(c2);
            else
            {
                c2[0] = 0;
                c2[1] = 0;
                c2[2] = 0;
            }

            bounds.getExtents(t1, t2);
            resizeCullList(cullEndIndex);

            switch(result)
            {
                case BoundingVolume.FRUSTUM_ALLOUT:
                    workCullList[cullEndIndex].renderable =
                        new DebugGroupRenderable(true, false, t1, t2, c1, c2, group_mat);
                    workCullList[cullEndIndex].localFog = null;
                    workCullList[cullEndIndex].transform = group_mat;
                    workCullList[cullEndIndex].numLights = 0;
                    workCullList[cullEndIndex].numClipPlanes = 0;

                    return cullEndIndex + 1;

                case BoundingVolume.FRUSTUM_ALLIN:
                    childAllInBounds = true;

                    workCullList[cullEndIndex].renderable =
                        new DebugGroupRenderable(false, false, t1, t2, c1, c2, group_mat);
                    workCullList[cullEndIndex].localFog = null;
                    workCullList[cullEndIndex].transform = group_mat;
                    workCullList[cullEndIndex].numLights = 0;
                    workCullList[cullEndIndex].numClipPlanes = 0;

                    ret_val++;
                    break;

                case BoundingVolume.FRUSTUM_PARTIAL:
                    workCullList[cullEndIndex].renderable =
                        new DebugGroupRenderable(false, true, t1, t2, c1, c2, group_mat);
                    workCullList[cullEndIndex].localFog = null;
                    workCullList[cullEndIndex].transform = group_mat;
                    workCullList[cullEndIndex].numLights = 0;
                    workCullList[cullEndIndex].numClipPlanes = 0;

                    ret_val++;
                    break;
            }
        }

        Cullable[] kids  = group.getCullableChildren();
        int size = group.numCullableChildren();

        int num_lights = 0;
        int num_clips = 0;
        boolean have_local_fog = false;

        // Find all the lights at this level first. Place them first into
        // the queue
        for(int i = 0; i < size && !terminate; i++)
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
                ret_val = findAllNodes((GroupCullable)kids[i],
                                       childAllInBounds,
                                       is_tx,
                                       ret_val);
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
                        // Use the group bounds first
                        bounds.getCenter(c1);
                        ((BoundingBox)bounds).getSize(c2);

                        BoundingVolume l_bounds = cullable.getBounds();
                        l_bounds.getExtents(t1, t2);
                        r = new DebugShapeRenderable(false, t1, t2, c1, c2, group_mat);

                        // Walk into the shape and check that we don't have any
                        // offscreen textures to render.
                        if(checkOffscreens)
                            checkForOffscreens((ShapeRenderable)kids[i]);

                        resizeCullList(ret_val);
                        workCullList[ret_val].renderable = r;
                        workCullList[ret_val].localFog = fogStack[lastFogStack];

                        Matrix4d mat = transformStack[lastTxStack];

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
                }
            }
            else if(kids[i] instanceof SingleCullable)
            {
                ret_val = findNextNode((SingleCullable)kids[i],
                                       childAllInBounds,
                                       false,
                                       ret_val);
            }

            // Now pop the stacks.
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
     * @param allInBounds All of this group is inside the view frustum
     * @param ignoreTrans Ignore the last transformation
     * @param cullEndIndex The current last item on the cull list
     * @return The index of the last item on the cull list
     */
    private int findNextNode(SingleCullable root,
                             boolean allInBounds,
                             boolean ignoreTrans,
                             int cullEndIndex)
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
            ret_val = findAllNodes((GroupCullable)current,
                                   allInBounds,
                                   is_tx,
                                   ret_val);
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
                    BoundingVolume bounds = cullable.getBounds();
                    bounds.getExtents(t1, t2);
                    bounds.getCenter(c1);
                    ((BoundingBox)bounds).getSize(c2);
                    r = new DebugShapeRenderable(false, t1, t2, c1, c2, transformStack[lastTxStack]);

                    // Walk into the shape and check that we don't have any
                    // offscreen textures to render.
                    if(checkOffscreens)
                        checkForOffscreens((ShapeRenderable)current);

                    resizeCullList(cullEndIndex);
                    workCullList[cullEndIndex].renderable = r;
                    workCullList[ret_val].localFog = fogStack[lastFogStack];

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

        // Now pop the stack.
        if(is_tx)
            lastTxStack--;

        return ret_val;
    }

    /**
     * Update the local viewfrustum copy with information from the
     * environment data.
     *
     * @param envData Data instance to use as the source of the frustum
     */
    private void updateFrustum(GraphicsEnvironmentData envData)
    {
        // Create projection matrix
        double left = viewFrustum[0];
        double right = viewFrustum[1];
        double bottom = viewFrustum[2];
        double top = viewFrustum[3];
        double nearval = viewFrustum[4];
        double farval = viewFrustum[5];
        double x, y, z, w;
        double a, b, c, d;

        x = (2.0f * nearval) / (right - left);
        y = (2.0f * nearval) / (top - bottom);
        a = (right + left) / (right - left);
        b = (top + bottom) / (top - bottom);
        c = -(farval + nearval) / ( farval - nearval);
        d = -(2.0f * farval * nearval) / (farval - nearval);

        prjMatrix.m00 = x;
        prjMatrix.m01 = 0;
        prjMatrix.m02 = a;
        prjMatrix.m03 = 0;
        prjMatrix.m10 = 0;
        prjMatrix.m11 = y;
        prjMatrix.m12 = b;
        prjMatrix.m13 = 0;
        prjMatrix.m20 = 0;
        prjMatrix.m21 = 0;
        prjMatrix.m22 = c;
        prjMatrix.m23 = d;
        prjMatrix.m30 = 0;
        prjMatrix.m31 = 0;
        prjMatrix.m32 = -1;
        prjMatrix.m33 = 0;

        matrixUtils.inverse(viewMatrix, viewMatrix);
        viewMatrix.mul(prjMatrix, viewMatrix);

        // Put result into opengl format
        prjMatrix.m00 = viewMatrix.m00;
        prjMatrix.m01 = viewMatrix.m10;
        prjMatrix.m02 = viewMatrix.m20;
        prjMatrix.m03 = viewMatrix.m30;
        prjMatrix.m10 = viewMatrix.m01;
        prjMatrix.m11 = viewMatrix.m11;
        prjMatrix.m12 = viewMatrix.m21;
        prjMatrix.m13 = viewMatrix.m31;
        prjMatrix.m20 = viewMatrix.m02;
        prjMatrix.m21 = viewMatrix.m12;
        prjMatrix.m22 = viewMatrix.m22;
        prjMatrix.m23 = viewMatrix.m32;
        prjMatrix.m30 = viewMatrix.m03;
        prjMatrix.m31 = viewMatrix.m13;
        prjMatrix.m32 = viewMatrix.m23;
        prjMatrix.m33 = viewMatrix.m33;

        float t;
        /* Extract the numbers for the RIGHT plane */
        x = prjMatrix.m03 - prjMatrix.m00;
        y = prjMatrix.m13 - prjMatrix.m10;
        z = prjMatrix.m23 - prjMatrix.m20;
        w = prjMatrix.m33 - prjMatrix.m30;

        /* Normalize the result */
        t = 1.0f / (float)Math.sqrt(x * x + y * y + z * z);
        frustumPlanes[0].x = x * t;
        frustumPlanes[0].y = y * t;
        frustumPlanes[0].z = z * t;
        frustumPlanes[0].w = w * t;
/*
        frustumPlanes[0].x = x;
        frustumPlanes[0].y = y;
        frustumPlanes[0].z = z;
        frustumPlanes[0].w = w;
*/

        /* Extract the numbers for the LEFT plane */
        x = prjMatrix.m03 + prjMatrix.m00;
        y = prjMatrix.m13 + prjMatrix.m10;
        z = prjMatrix.m23 + prjMatrix.m20;
        w = prjMatrix.m33 + prjMatrix.m30;

        t = 1.0f / (float)Math.sqrt(x * x + y * y + z * z);
        frustumPlanes[1].x = x * t;
        frustumPlanes[1].y = y * t;
        frustumPlanes[1].z = z * t;
        frustumPlanes[1].w = w * t;
/*
        frustumPlanes[1].x = x;
        frustumPlanes[1].y = y;
        frustumPlanes[1].z = z;
        frustumPlanes[1].w = w;
*/

        /* Extract the BOTTOM plane */
        x = prjMatrix.m03 + prjMatrix.m01;
        y = prjMatrix.m13 + prjMatrix.m11;
        z = prjMatrix.m23 + prjMatrix.m21;
        w = prjMatrix.m33 + prjMatrix.m31;

        t = 1.0f / (float)Math.sqrt(x * x + y * y + z * z);
        frustumPlanes[2].x = x * t;
        frustumPlanes[2].y = y * t;
        frustumPlanes[2].z = z * t;
        frustumPlanes[2].w = w * t;
/*
        frustumPlanes[2].x = x;
        frustumPlanes[2].y = y;
        frustumPlanes[2].z = z;
        frustumPlanes[2].w = w;
*/

        /* Extract the TOP plane */
        x = prjMatrix.m03 - prjMatrix.m01;
        y = prjMatrix.m13 - prjMatrix.m11;
        z = prjMatrix.m23 - prjMatrix.m21;
        w = prjMatrix.m33 - prjMatrix.m31;

        t = 1.0f / (float)Math.sqrt(x * x + y * y + z * z);
        frustumPlanes[3].x = x * t;
        frustumPlanes[3].y = y * t;
        frustumPlanes[3].z = z * t;
        frustumPlanes[3].w = w * t;
/*
        frustumPlanes[3].x = x;
        frustumPlanes[3].y = y;
        frustumPlanes[3].z = z;
        frustumPlanes[3].w = w;
*/


        /* Extract the FAR plane */
        x = prjMatrix.m03 - prjMatrix.m02;
        y = prjMatrix.m13 - prjMatrix.m12;
        z = prjMatrix.m23 - prjMatrix.m22;
        w = prjMatrix.m33 - prjMatrix.m32;

        t = 1.0f / (float)Math.sqrt(x * x + y * y + z * z);
        frustumPlanes[4].x = x * t;
        frustumPlanes[4].y = y * t;
        frustumPlanes[4].z = z * t;
        frustumPlanes[4].w = w * t;
/*
        frustumPlanes[4].x = x;
        frustumPlanes[4].y = y;
        frustumPlanes[4].z = z;
        frustumPlanes[4].w = w;
*/

        /* Extract the NEAR plane */
        x = prjMatrix.m03 + prjMatrix.m02;
        y = prjMatrix.m13 + prjMatrix.m12;
        z = prjMatrix.m23 + prjMatrix.m22;
        w = prjMatrix.m33 + prjMatrix.m32;

        t = 1.0f / (float)Math.sqrt(x * x + y * y + z * z);
        frustumPlanes[5].x = x * t;
        frustumPlanes[5].y = y * t;
        frustumPlanes[5].z = z * t;
        frustumPlanes[5].w = w * t;

/*
        frustumPlanes[5].x = x;
        frustumPlanes[5].y = y;
        frustumPlanes[5].z = z;
        frustumPlanes[5].w = w;
*/
    }
}
