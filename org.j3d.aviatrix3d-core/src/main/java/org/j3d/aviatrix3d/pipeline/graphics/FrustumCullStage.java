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
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Point4d;
import org.j3d.maths.vector.Vector4d;

// Local imports
import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.rendering.*;

/**
 * Handles the complete set of scenegraph maintenance and culling operations.
 * <p>
 *
 * The culling phase generates a list of nodes to render trimmed according to
 * what is visible in the view frustum, and return values from
 * {@link CustomCullable} and {@link CustomRenderable}.
 * <p>
 *
 * By default, the implementation will walk into shape3Ds looking for any
 * offscreen textures to be rendered. If you know that you do not have any
 * in the scene, then you can set an internal flag to not look for them, thus
 * achieving a simple performance boost.
 * <p>
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 3.33 $
 */
public class FrustumCullStage extends BaseCullStage
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

    /** Array form of the projection matrix */
    private float[] projectionMatrix;

    /** Class for interacting with the current Cullable object */
    private CullInstructions cullInstructions;

    /** Class for interacting with the current CustomCullable object */
    private RenderableInstructions renderInstructions;

    /** Angular resolution for this scene. Field of view / viewport width */
    private float angularResolution;

    /** The list of global light bounds valid while traversing */
    private BoundingVolume[] lightBounds;

    /** The list of global lights valid while traversing. */
    private EffectRenderable[] globalLightList;

    /** The list of global light transforms valid while traversing */
    private float[][] globalLightTxList;

    /** The list of global light bounds valid while traversing */
    private BoundingVolume[] globalLightBounds;

    /** Index to the next place to add items in the globalLightList */
    private int lastGlobalLight;

    /** The list of global lights with bounds valid while traversing. */
    private EffectRenderable[] globalBoundedLightList;

    /** The list of bounded global light transforms valid while traversing */
    private float[][] globalBoundedLightTxList;

    /** The list of bounded global light bounds valid while traversing */
    private BoundingVolume[] globalBoundedLightBounds;

    /** Index to the next place to add items in the lightList */
    private int lastGlobalBoundedLight;

    /**
     * Create a basic instance of this class with the list assuming there are
     * no off-screen buffers in use for the initial internal setup.
     */
    public FrustumCullStage()
    {
        this(LIST_START_LENGTH);
    }

    /**
     * Create a basic instance of this class with the list initial internal
     * setup for the given number of renderable surfaces. The size is just an
     * initial estimate, and is used for optimisation purposes to prevent
     * frequent array reallocations internally. As such, the number does not
     * have to be perfect, just good enough.
     *
     * @param numSurfaces Total number of surfaces to prepare rendering for
     */
    public FrustumCullStage(int numSurfaces)
    {
        super(numSurfaces);

        cullInstructions = new CullInstructions();
        renderInstructions = new RenderableInstructions();

        prjMatrix = new Matrix4d();
        viewMatrix = new Matrix4d();

        projectionMatrix = new float[16];

        frustumPoints = new Point4d[8];
        for(int i=0; i < 8; i++)
            frustumPoints[i] = new Point4d();

        frustumPlanes = new Vector4d[6];
        for(int i=0; i < 6; i++)
            frustumPlanes[i] = new Vector4d();

        globalLightList = new EffectRenderable[LIGHT_START_SIZE];
        globalLightTxList = new float[LIGHT_START_SIZE][16];

        globalBoundedLightList = new EffectRenderable[LIGHT_START_SIZE];
        globalBoundedLightTxList = new float[LIGHT_START_SIZE][16];
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
    protected void cullScene(RenderPassCullable scene,
                             int subsceneId,
                             int layerId,
                             int viewIndex,
                             int layerIndex)
    {
        lastLight = 0;
        lastGlobalLight = 0;
        lastGlobalBoundedLight = 0;
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

        if(node instanceof TransformCullable)
            ((TransformCullable)node).getTransform(transformStack[0]);
        else
            transformStack[0].setIdentity();

        if(node instanceof CustomCullable)
        {
            viewFrustum = bucket.data.viewFrustum;
            viewMatrix.set(bucket.data.viewTransform);

            updateFrustum(scene.getViewCullable(), bucket.data);

            // updateFrustum trashed it, so reset so it can be used in
            // findAllNodes()
            viewMatrix.set(bucket.data.viewTransform);

            bucket.numNodes = findAllNodes((CustomCullable)node, 0);
            bucket.nodes = workCullList;
        }
        else if(node instanceof GroupCullable)
        {
            viewFrustum = bucket.data.viewFrustum;
            viewMatrix.set(bucket.data.viewTransform);

            updateFrustum(scene.getViewCullable(), bucket.data);

            // updateFrustum trashed it, so reset so it can be used in
            // findAllNodes()
            viewMatrix.set(bucket.data.viewTransform);

            bucket.numNodes =
                findAllNodes((GroupCullable)node, false, false, 0);
            bucket.nodes = workCullList;
        }
        else if(node instanceof SingleCullable)
        {
            viewFrustum = bucket.data.viewFrustum;
            viewMatrix.set(bucket.data.viewTransform);

            updateFrustum(scene.getViewCullable(), bucket.data);

            // updateFrustum trashed it, so reset so it can be used in
            // findAllNodes()
            viewMatrix.set(bucket.data.viewTransform);

            bucket.numNodes =
                findNextNode((SingleCullable)node, false, false, 0);
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

                // Manual set of the stack back to an identity matrix
                workCullList[0].transform.setIdentity();
                workCullList[0].numLights = 0;
                workCullList[0].numClipPlanes = 0;

                bucket.numNodes = 1;
                bucket.nodes = workCullList;
            }
        }
        processGlobalLights(bucket.numNodes);
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
        lastGlobalLight = 0;
        lastGlobalBoundedLight = 0;
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

        if(node instanceof CustomCullable)
        {
            // set the top matrix stack back to identity
            transformStack[0].setIdentity();
            viewFrustum = bucket.data[passNumber].viewFrustum;
            viewMatrix.set(bucket.data[passNumber].viewTransform);

            updateFrustum(pass.getViewCullable(), bucket.data[passNumber]);

            // updateFrustum trashed it, so reset so it can be used in
            // findAllNodes()
            viewMatrix.set(bucket.data[passNumber].viewTransform);

            bucket.numNodes[passNumber] = findAllNodes((CustomCullable)node, 0);
            bucket.nodes[passNumber] = workCullList;
        }
        else if(node instanceof GroupCullable)
        {
            // set the top matrix stack back to identity
            transformStack[0].setIdentity();

            if(node instanceof TransformCullable)
                ((TransformCullable)node).getTransform(transformStack[0]);
            else
                transformStack[0].setIdentity();

            viewFrustum = bucket.data[passNumber].viewFrustum;
            viewMatrix.set(bucket.data[passNumber].viewTransform);

            updateFrustum(pass.getViewCullable(), bucket.data[passNumber]);

            bucket.numNodes[passNumber] =
                findAllNodes((GroupCullable)node, false, false, 0);
            bucket.nodes[passNumber] = workCullList;
        }
        else if(node instanceof SingleCullable)
        {
            // set the top matrix stack back to identity
            transformStack[0].setIdentity();

            viewFrustum = bucket.data[passNumber].viewFrustum;
            viewMatrix.set(bucket.data[passNumber].viewTransform);

            updateFrustum(pass.getViewCullable(), bucket.data[passNumber]);

            bucket.numNodes[passNumber] =
                findNextNode((SingleCullable)node, false, false, 0);
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

        processGlobalLights(bucket.numNodes[passNumber]);
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
    protected void cullScene2D(RenderPassCullable scene,
                               int subsceneId,
                               int layerId,
                               int viewIndex,
                               int layerIndex)
    {
        lastLight = 0;
        lastGlobalLight = 0;
        lastGlobalBoundedLight = 0;
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

        if(node instanceof TransformCullable)
            ((TransformCullable)node).getTransform(transformStack[0]);
        else
            transformStack[0].setIdentity();

        if(node instanceof CustomCullable)
        {
            // set the top matrix stack back to identity
            transformStack[0].setIdentity();
            viewFrustum = bucket.data.viewFrustum;
            viewMatrix.set(bucket.data.viewTransform);

            updateFrustum(scene.getViewCullable(), bucket.data);

            // updateFrustum trashed it, so reset so it can be used in
            // findAllNodes()
            viewMatrix.set(bucket.data.viewTransform);

            bucket.numNodes = findAllNodes((CustomCullable)node, 0);
            bucket.nodes = workCullList;
        }
        else if(node instanceof GroupCullable)
        {
            viewFrustum = bucket.data.viewFrustum;
            viewMatrix.set(bucket.data.viewTransform);

            updateFrustum(scene.getViewCullable(), bucket.data);

            // updateFrustum trashed it, so reset so it can be used in
            // findAllNodes()
            viewMatrix.set(bucket.data.viewTransform);

            bucket.numNodes =
                findAllNodes((GroupCullable)node, false, false, 0);
            bucket.nodes = workCullList;
        }
        else if(node instanceof SingleCullable)
        {
            viewFrustum = bucket.data.viewFrustum;
            viewMatrix.set(bucket.data.viewTransform);

            updateFrustum(scene.getViewCullable(), bucket.data);

            // updateFrustum trashed it, so reset so it can be used in
            // findAllNodes()
            viewMatrix.set(bucket.data.viewTransform);

            bucket.numNodes =
                findNextNode((SingleCullable)node, false, false, 0);
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
                workCullList[0].renderable = r;

                // Manual set of the stack back to an identity matrix
                workCullList[0].transform.setIdentity();
                workCullList[0].numLights = 0;
                workCullList[0].numClipPlanes = 0;

                bucket.numNodes = 1;
                bucket.nodes = workCullList;
            }
        }
        processGlobalLights(bucket.numNodes);
    }

    /**
     * Take the scene and fill in a GraphicsEnvironmentData instance.
     *
     * @param scene The scene to take data from
     * @param envData Data instance to copy it to
     */
    protected void fillSingleEnvData(SceneCullable scene,
                                     GraphicsEnvironmentData envData)
    {
        super.fillSingleEnvData(scene, envData);

        ViewEnvironmentCullable view = scene.getViewCullable();
        switch(view.getProjectionType())
        {
            case ViewEnvironmentCullable.PERSPECTIVE_PROJECTION:
            case ViewEnvironmentCullable.INFINITE_PROJECTION:
                angularResolution = (float)view.getFieldOfView();
                break;

            case ViewEnvironmentCullable.ORTHOGRAPHIC_PROJECTION:
                double [] frustum = new double[6];
                view.getViewFrustum(frustum);
                double angle = Math.atan2(frustum[1], frustum[5]) -
                               Math.atan2(frustum[0], frustum[5]);
                angularResolution = (float)Math.toDegrees(angle);

           case ViewEnvironmentCullable.CUSTOM_PROJECTION:
                // TBD
                angularResolution = 1.0f;
                envData.projectionMatrix = projectionMatrix;
        }

        int[] viewport = view.getViewportDimensions();

        angularResolution /= viewport[2];
    }

    /**
     * Clean up the unused resources after the end of the cull process. This
     * releases any references that are no longer needed, and may have been
     * kept from the previous culling pass.
     */
    protected void cleanupOldRefs()
    {
        // Clear unused fields to help the GC
        renderInstructions.instructions = null;

        for(int i = 0; i < cullInstructions.children.length; ++i)
            cullInstructions.children[i] = null;

        for(int i = 0; i < lastGlobalLight; i++)
            globalLightList[i] = null;

        for(int i = 0; i < lastGlobalBoundedLight; i++)
            globalBoundedLightList[i] = null;

        super.cleanupOldRefs();
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Recursive walk of the tree to find all the renderable nodes, starting
     * from a Cullable object.
     *
     * @param root The parent node to walk into
     * @param cullEndIndex The current last item on the cull list
     * @return The index of the last item on the cull list
     */
    private int findAllNodes(CustomCullable root, int cullEndIndex)
    {
        if(terminate)
            return 0;

        root.cullChildren(cullInstructions,
                          transformStack[lastTxStack],
                          viewMatrix,
                          frustumPlanes,
                          angularResolution);

        if(cullInstructions.numChildren == 0)
            return cullEndIndex;

        boolean cull_tx = cullInstructions.hasTransform;

        if(cull_tx)
        {
            transformStack[lastTxStack + 1].mul(transformStack[lastTxStack],
                                                cullInstructions.localTransform);
            lastTxStack++;
        }

        // Make local copy of array because if the scene graph is nested with
        // custom cullables, then the contents of this array will be trashed.
        // Generates a small amount of garbage, but nothing too serious.
        Cullable[] kids  = (Cullable[])cullInstructions.children.clone();
        int size = cullInstructions.numChildren;

        int num_lights = 0;
        int num_clips = 0;
        int ret_val = cullEndIndex;
        boolean have_local_fog = false;
        boolean have_local_appearance = false;

        // Find all the effects and overrides at this level first. Place them
        // first into the queue
        for(int i = 0; i < size; i++)
        {
            if(!(kids[i] instanceof LeafCullable))
                continue;

            LeafCullable cullable = (LeafCullable)kids[i];
            Renderable r = cullable.getRenderable();

            if(!(r instanceof CascadeRenderable))
                continue;

            CascadeRenderable cascade = (CascadeRenderable)r;

            if(!cascade.isEnabled())
                continue;

            // TODO:
            // Need to handle global options for lights and clip planes

            switch(cullable.getCullableType())
            {
                case LeafCullable.LIGHT_CULLABLE:
                    EffectRenderable effect = (EffectRenderable)r;
                    if(effect.isGlobalOnly())
                    {
                        BoundingVolume bounds = effect.getEffectBounds();

                        if(bounds instanceof BoundingVoid)
                            continue;

                        Matrix4d mat = transformStack[lastTxStack];

                        if(bounds == null)
                        {
                            resizeGlobalLightList();
                            globalLightList[lastGlobalLight] = effect;

                            // Transpose the matrix in place as it is being copied
                            globalLightTxList[lastGlobalLight][0] = (float)mat.m00;
                            globalLightTxList[lastGlobalLight][1] = (float)mat.m10;
                            globalLightTxList[lastGlobalLight][2] = (float)mat.m20;
                            globalLightTxList[lastGlobalLight][3] = (float)mat.m30;

                            globalLightTxList[lastGlobalLight][4] = (float)mat.m01;
                            globalLightTxList[lastGlobalLight][5] = (float)mat.m11;
                            globalLightTxList[lastGlobalLight][6] = (float)mat.m21;
                            globalLightTxList[lastGlobalLight][7] = (float)mat.m31;

                            globalLightTxList[lastGlobalLight][8] = (float)mat.m02;
                            globalLightTxList[lastGlobalLight][9] = (float)mat.m12;
                            globalLightTxList[lastGlobalLight][10] = (float)mat.m22;
                            globalLightTxList[lastGlobalLight][11] = (float)mat.m32;

                            globalLightTxList[lastGlobalLight][12] = (float)mat.m03;
                            globalLightTxList[lastGlobalLight][13] = (float)mat.m13;
                            globalLightTxList[lastGlobalLight][14] = (float)mat.m23;
                            globalLightTxList[lastGlobalLight][15] = (float)mat.m33;

                            lastGlobalLight++;
                        }
                        else
                        {
                            resizeGlobalBoundedLightList();
                            globalBoundedLightList[lastGlobalBoundedLight] = effect;

                            // Transpose the matrix in place as it is being copied
                            globalBoundedLightTxList[lastGlobalBoundedLight][0] = (float)mat.m00;
                            globalBoundedLightTxList[lastGlobalBoundedLight][1] = (float)mat.m10;
                            globalBoundedLightTxList[lastGlobalBoundedLight][2] = (float)mat.m20;
                            globalBoundedLightTxList[lastGlobalBoundedLight][3] = (float)mat.m30;

                            globalBoundedLightTxList[lastGlobalBoundedLight][4] = (float)mat.m01;
                            globalBoundedLightTxList[lastGlobalBoundedLight][5] = (float)mat.m11;
                            globalBoundedLightTxList[lastGlobalBoundedLight][6] = (float)mat.m21;
                            globalBoundedLightTxList[lastGlobalBoundedLight][7] = (float)mat.m31;

                            globalBoundedLightTxList[lastGlobalBoundedLight][8] = (float)mat.m02;
                            globalBoundedLightTxList[lastGlobalBoundedLight][9] = (float)mat.m12;
                            globalBoundedLightTxList[lastGlobalBoundedLight][10] = (float)mat.m22;
                            globalBoundedLightTxList[lastGlobalBoundedLight][11] = (float)mat.m32;

                            globalBoundedLightTxList[lastGlobalBoundedLight][12] = (float)mat.m03;
                            globalBoundedLightTxList[lastGlobalBoundedLight][13] = (float)mat.m13;
                            globalBoundedLightTxList[lastGlobalBoundedLight][14] = (float)mat.m23;
                            globalBoundedLightTxList[lastGlobalBoundedLight][15] = (float)mat.m33;

                            lastGlobalBoundedLight++;
                        }
                    }
                    else
                    {
                        BoundingVolume l_bounds = effect.getEffectBounds();

                        if(l_bounds instanceof BoundingVoid)
                            continue;

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
                    }
                    break;

                case LeafCullable.CLIP_CULLABLE:
                    effect = (EffectRenderable)r;
                    resizeClipList();
                    clipList[lastClip] = effect;

                    Matrix4d mat = transformStack[lastTxStack];

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
                    effect = (EffectRenderable)r;
                    if(!effect.isGlobalOnly() && !have_local_fog)
                    {
                        resizeFogStack();

                        have_local_fog = true;
                        lastFogStack++;
                        fogStack[lastFogStack] = effect;
                    }
                    break;

                case LeafCullable.OVERRIDE_CULLABLE:
                    OverrideRenderable override = (OverrideRenderable)r;

                    // Check the one that is currently on the stack. If it is
                    // set to override lower items, then we ignore this update.
                    // If not set, then we add this item to the stack.

                    // If this one is set to override lower, we need to check
                    if((lastAppearanceStack == 0) ||
                       !appearanceStack[lastAppearanceStack].overrideLower())
                    {
                        resizeAppearanceStack();

                        have_local_appearance = true;
                        lastAppearanceStack++;
                        appearanceStack[lastAppearanceStack] = override;
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
                                       true,
                                       is_tx,
                                       ret_val);
            }
            else if(kids[i] instanceof LeafCullable)
            {
                LeafCullable cullable = (LeafCullable)kids[i];

                if(cullable.getCullableType() == LeafCullable.GEOMETRY_CULLABLE)
                {
                    Renderable r = cullable.getRenderable();

                    if(r instanceof ShapeRenderable)
                    {
                        ShapeRenderable sr = (ShapeRenderable)r;

                        if(!sr.isVisible())
                            continue;

                        GeometryRenderable gr = sr.getGeometryRenderable();
                        if(gr instanceof CustomGeometryRenderable)
                        {
                            CustomGeometryRenderable cgr = (CustomGeometryRenderable)gr;

                            workCullList[ret_val].customData =
                                cgr.processCull(transformStack[lastTxStack],
                                                viewMatrix,
                                                frustumPlanes,
                                                angularResolution);
                        }

                        // Check the visibility state and ignore if not visible.
                        if(sr.is2D())
                        {
                            resizeCullList(ret_val);
                            workCullList[ret_val].renderable = r;
                            workCullList[ret_val].localFog = null;
                            workCullList[ret_val].numLights = 0;
                            workCullList[ret_val].numClipPlanes = 0;

                            Matrix4d mat = transformStack[lastTxStack];

                            workCullList[ret_val].transform.set(mat);

                            ret_val++;
                        }
                        else
                        {
                            // Walk into the shape and check that we don't have any
                            // offscreen textures to render.
                            if(checkOffscreens)
                                checkForOffscreens(sr);

                            resizeCullList(ret_val);

                            if(lastAppearanceStack != 0)
                            {
                                OverrideRenderable ovr =
                                    appearanceStack[lastAppearanceStack];

                                AppearanceRenderable app_r =
                                    ovr.getAppearanceRenderable();

                                if(!ovr.useLocalOnlyAppearance())
                                {
                                    AppearanceRenderable leaf_app =
                                        sr.getAppearanceRenderable();

                                    app_r =
                                        new OverrideAppearanceProxyRenderable(app_r,
                                                                              leaf_app);
                                }
                                OverrideShapeProxyRenderable proxy_r =
                                    new OverrideShapeProxyRenderable(sr, app_r);
                                workCullList[ret_val].renderable = proxy_r;
                            }
                            else
                                workCullList[ret_val].renderable = r;

                            workCullList[ret_val].localFog = fogStack[lastFogStack];

                            Matrix4d mat = transformStack[lastTxStack];

                            workCullList[ret_val].transform.set(mat);

                            int src_size = (workCullList[ret_val].lights == null) ?
                                            0 :
                                            workCullList[ret_val].lights.length;

                            //int req_size = lastLight + lastGlobalLight +
                            //               lastGlobalBoundedLight;
                            int req_size = lastLight;

                            if(src_size < req_size)
                            {
                                // up the size of the array

                                VisualDetails[] tmp = new VisualDetails[req_size];
                                if(src_size != 0)
                                {
                                    System.arraycopy(workCullList[ret_val].lights,
                                                     0,
                                                     tmp,
                                                     0,
                                                     src_size);
                                }

                                for(int j = src_size; j < req_size; j++)
                                    tmp[j] = new VisualDetails();

                                workCullList[ret_val].lights = tmp;
                            }

                            // copy in the light information.
                            int valid_lights = 0;
                            BoundingVolume bv = cullable.getBounds();
                            workCullList[ret_val].cullableBounds = bv;
                            VisualDetails[] l_tmp = workCullList[ret_val].lights;
                            /*
                            for(int j = 0; j < lastGlobalLight; j++)
                            {
                                l_tmp[valid_lights].update(globalLightList[j],
                                                           globalLightTxList[j]);
                                valid_lights++;
                            }

                            for(int j = 0; j < lastGlobalBoundedLight; j++)
                            {
                                BoundingVolume effect =
                                    globalBoundedLightList[j].getEffectBounds();

                                if(checkBoundsIntersect(bv,
                                                        mat,
                                                        effect,
                                                        globalBoundedLightTxList[j]))
                                {
                                    l_tmp[valid_lights].update(
                                        //lightList[j],
                                        globalBoundedLightList[j],
                                        globalBoundedLightTxList[j]);
                                    valid_lights++;
                                }
                            }
                            */
                            for(int j = 0; j < lastLight; j++)
                            {
                                BoundingVolume effect = lightList[j].getEffectBounds();

                                if((effect == null) ||
                                   checkBoundsIntersect(bv,
                                                        mat,
                                                        effect,
                                                        lightTxList[j]))
                                {
                                    l_tmp[valid_lights].update(lightList[j],
                                                               lightTxList[j]);
                                    valid_lights++;
                                }
                            }

                            workCullList[ret_val].numLights = valid_lights;

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
                    else if(kids[i] instanceof CustomRenderable)
                    {
                        BoundingVolume bv = cullable.getBounds();
                        ret_val = findAllNodes((CustomRenderable)kids[i],
                                               ret_val,
                                               bv);
                    }
                }
            }
            else if(kids[i] instanceof SingleCullable)
            {
                ret_val = findNextNode((SingleCullable)kids[i],
                                       false,
                                       false,
                                       ret_val);
            }
            else if(kids[i] instanceof CustomCullable)
            {
                ret_val = findAllNodes((CustomCullable)kids[i], ret_val);
            }

            // Now pop the stacks.
            if(is_tx)
                lastTxStack--;
        }

        // Pop the valid lights from the stack
        for(int i = 0; i < num_lights; i++) {
            lightList[--lastLight] = null;
        }
        for(int i = 0; i < num_clips; i++) {
            clipList[--lastClip] = null;
        }

        if(have_local_fog)
        {
            fogStack[lastFogStack] = null;
            lastFogStack--;
        }

        if(have_local_appearance)
        {
            appearanceStack[lastAppearanceStack] = null;
            lastAppearanceStack--;
        }

        if(cull_tx)
            lastTxStack--;

        return ret_val;
    }

    /**
     * Recursive walk of the tree to find all the renderable nodes, starting
     * from a CustomRenderable object.
     *
     * @param root The parent node to walk into
     * @param cullEndIndex The current last item on the cull list
     * @param bounds The bounds passed down from the owning cullable
     * @return The index of the last item on the cull list
     */
    private int findAllNodes(CustomRenderable root,
                             int cullEndIndex,
                             BoundingVolume bounds)
    {
        if(terminate)
            return 0;

        if(!root.processCull(renderInstructions,
                             transformStack[lastTxStack],
                             viewMatrix,
                             frustumPlanes,
                             angularResolution))
            return cullEndIndex;

        if(renderInstructions.hasTransform)
        {
            transformStack[lastTxStack + 1].mul(transformStack[lastTxStack],
                                                renderInstructions.localTransform);
            lastTxStack++;
        }

        int ret_val = cullEndIndex;

        // NOTE:
        // Doubt this could ever happen, but let's just cover all our bases.
        if(checkOffscreens && (root instanceof ShapeRenderable))
            checkForOffscreens((ShapeRenderable)root);

        resizeCullList(ret_val);
        workCullList[ret_val].renderable = root;
        workCullList[ret_val].localFog = fogStack[lastFogStack];
        workCullList[ret_val].customData = renderInstructions.instructions;

        Matrix4d mat = transformStack[lastTxStack];

        workCullList[ret_val].transform.set(mat);

        int src_size = (workCullList[ret_val].lights == null) ?
                        0 :
                        workCullList[ret_val].lights.length;

        //int req_size = lastLight + lastGlobalLight +
        //               lastGlobalBoundedLight;
        int req_size = lastLight;

        if(src_size < req_size)
        {
            // up the size of the array

            VisualDetails[] tmp = new VisualDetails[req_size];
            if(src_size != 0)
            {
                System.arraycopy(workCullList[ret_val].lights,
                                 0,
                                 tmp,
                                 0,
                                 src_size);
            }

            for(int j = src_size; j < req_size; j++)
                tmp[j] = new VisualDetails();

            workCullList[ret_val].lights = tmp;
        }

        // copy in the light information.
        int valid_lights = 0;
        workCullList[ret_val].cullableBounds = bounds;
        VisualDetails[] l_tmp = workCullList[ret_val].lights;
        /*
        for(int j = 0; j < lastGlobalLight; j++)
        {
            l_tmp[valid_lights].update(globalLightList[j],
                                       globalLightTxList[j]);
            valid_lights++;
        }

        for(int j = 0; j < lastGlobalBoundedLight; j++)
        {
            BoundingVolume effect =
                globalBoundedLightList[j].getEffectBounds();

            if(checkBoundsIntersect(bounds,
                                    mat,
                                    effect,
                                    globalBoundedLightTxList[j]))
            {
                l_tmp[valid_lights].update(
                    //lightList[j],
                    globalBoundedLightList[j],
                    globalBoundedLightTxList[j]);
                valid_lights++;
            }
        }
        */
        for(int j = 0; j < lastLight; j++)
        {
            BoundingVolume effect = lightList[j].getEffectBounds();

            if((effect == null) ||
               checkBoundsIntersect(bounds,
                                    mat,
                                    effect,
                                    lightTxList[j]))
            {
                l_tmp[valid_lights].update(lightList[j],
                                           lightTxList[j]);
                valid_lights++;
            }
        }

        workCullList[ret_val].numLights = valid_lights;

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

        // copy in the clip plane information.
        VisualDetails[] c_tmp = workCullList[ret_val].clipPlanes;
        workCullList[ret_val].numClipPlanes = lastClip;

        for(int j = 0; j < lastClip; j++)
            c_tmp[j].update(clipList[j], clipTxList[j]);
        ret_val++;

        if(renderInstructions.hasTransform)
            lastTxStack--;

        return ret_val;
    }

    /**
     * Recursive walk of the tree to find all the renderable nodes.
     *
     * @param group The parent node to walk into
     * @param allInBounds All of this group is inside the view frustum
     * @param ignoreTrans Ignore the last transformation
     * @param cullEndIndex The current last item on the cull list
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
        boolean childAllInBounds = allInBounds;

        if(!allInBounds)
        {
            Matrix4d mat;

            int matIdx;

            if(ignoreTrans)
                matIdx = lastTxStack-1;
            else
                matIdx = lastTxStack;

            mat = transformStack[matIdx];

            int result = bounds.checkIntersectionFrustum(frustumPlanes, mat);

            switch(result)
            {
                case BoundingVolume.FRUSTUM_ALLOUT:
                    return cullEndIndex;

                case BoundingVolume.FRUSTUM_ALLIN:
                    childAllInBounds = true;
                    break;

                case BoundingVolume.FRUSTUM_PARTIAL:
                    break;
            }
        }

        Cullable[] kids  = group.getCullableChildren();
        int size = group.numCullableChildren();

        int num_lights = 0;
        int num_clips = 0;
        int ret_val = cullEndIndex;
        boolean have_local_fog = false;
        boolean have_local_appearance = false;

        // Find all the lights at this level first. Place them first into
        // the queue
        for(int i = 0; i < size; i++)
        {
            if(!(kids[i] instanceof LeafCullable))
                continue;

            LeafCullable cullable = (LeafCullable)kids[i];
            Renderable r = cullable.getRenderable();

            if(!(r instanceof CascadeRenderable))
                continue;

            CascadeRenderable cascade = (CascadeRenderable)r;

            if(!cascade.isEnabled())
                continue;


            // TODO:
            // Need to handle global options for clip planes

            switch(cullable.getCullableType())
            {
                case LeafCullable.LIGHT_CULLABLE:
                    EffectRenderable effect = (EffectRenderable)r;
                    if(effect.isGlobalOnly())
                    {
                        BoundingVolume l_bounds = effect.getEffectBounds();
                        if(l_bounds instanceof BoundingVoid)
                            continue;

                        Matrix4d mat = transformStack[lastTxStack];

                        if(bounds == null)
                        {
                            resizeGlobalLightList();
                            globalLightList[lastGlobalLight] = effect;

                            // Transpose the matrix in place as it is being copied
                            globalLightTxList[lastGlobalLight][0] = (float)mat.m00;
                            globalLightTxList[lastGlobalLight][1] = (float)mat.m10;
                            globalLightTxList[lastGlobalLight][2] = (float)mat.m20;
                            globalLightTxList[lastGlobalLight][3] = (float)mat.m30;

                            globalLightTxList[lastGlobalLight][4] = (float)mat.m01;
                            globalLightTxList[lastGlobalLight][5] = (float)mat.m11;
                            globalLightTxList[lastGlobalLight][6] = (float)mat.m21;
                            globalLightTxList[lastGlobalLight][7] = (float)mat.m31;

                            globalLightTxList[lastGlobalLight][8] = (float)mat.m02;
                            globalLightTxList[lastGlobalLight][9] = (float)mat.m12;
                            globalLightTxList[lastGlobalLight][10] = (float)mat.m22;
                            globalLightTxList[lastGlobalLight][11] = (float)mat.m32;

                            globalLightTxList[lastGlobalLight][12] = (float)mat.m03;
                            globalLightTxList[lastGlobalLight][13] = (float)mat.m13;
                            globalLightTxList[lastGlobalLight][14] = (float)mat.m23;
                            globalLightTxList[lastGlobalLight][15] = (float)mat.m33;

                            lastGlobalLight++;
                        }
                        else
                        {
                            resizeGlobalBoundedLightList();
                            globalBoundedLightList[lastGlobalBoundedLight] = effect;

                            // Transpose the matrix in place as it is being copied
                            globalBoundedLightTxList[lastGlobalBoundedLight][0] = (float)mat.m00;
                            globalBoundedLightTxList[lastGlobalBoundedLight][1] = (float)mat.m10;
                            globalBoundedLightTxList[lastGlobalBoundedLight][2] = (float)mat.m20;
                            globalBoundedLightTxList[lastGlobalBoundedLight][3] = (float)mat.m30;

                            globalBoundedLightTxList[lastGlobalBoundedLight][4] = (float)mat.m01;
                            globalBoundedLightTxList[lastGlobalBoundedLight][5] = (float)mat.m11;
                            globalBoundedLightTxList[lastGlobalBoundedLight][6] = (float)mat.m21;
                            globalBoundedLightTxList[lastGlobalBoundedLight][7] = (float)mat.m31;

                            globalBoundedLightTxList[lastGlobalBoundedLight][8] = (float)mat.m02;
                            globalBoundedLightTxList[lastGlobalBoundedLight][9] = (float)mat.m12;
                            globalBoundedLightTxList[lastGlobalBoundedLight][10] = (float)mat.m22;
                            globalBoundedLightTxList[lastGlobalBoundedLight][11] = (float)mat.m32;

                            globalBoundedLightTxList[lastGlobalBoundedLight][12] = (float)mat.m03;
                            globalBoundedLightTxList[lastGlobalBoundedLight][13] = (float)mat.m13;
                            globalBoundedLightTxList[lastGlobalBoundedLight][14] = (float)mat.m23;
                            globalBoundedLightTxList[lastGlobalBoundedLight][15] = (float)mat.m33;

                            lastGlobalBoundedLight++;
                        }
                    }
                    else
                    {
                        BoundingVolume l_bounds = effect.getEffectBounds();

                        if(l_bounds instanceof BoundingVoid)
                            continue;

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
                    }
                    break;

                case LeafCullable.CLIP_CULLABLE:
                    effect = (EffectRenderable)r;
                    resizeClipList();
                    clipList[lastClip] = effect;

                    Matrix4d mat = transformStack[lastTxStack];

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
                    effect = (EffectRenderable)r;
                    if(!effect.isGlobalOnly() && !have_local_fog)
                    {
                        resizeFogStack();

                        have_local_fog = true;
                        lastFogStack++;
                        fogStack[lastFogStack] = effect;
                    }
                    break;

                case LeafCullable.OVERRIDE_CULLABLE:
                    OverrideRenderable override = (OverrideRenderable)r;

                    // Check the one that is currently on the stack. If it is
                    // set to override lower items, then we ignore this update.
                    // If not set, then we add this item to the stack.

                    // If this one is set to override lower, we need to check
                    if((lastAppearanceStack == 0) ||
                       !appearanceStack[lastAppearanceStack].overrideLower())
                    {
                        resizeAppearanceStack();

                        have_local_appearance = true;
                        lastAppearanceStack++;
                        appearanceStack[lastAppearanceStack] = override;
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

                    if(r instanceof ShapeRenderable)
                    {
                        ShapeRenderable sr = (ShapeRenderable)r;

                        if(!sr.isVisible())
                            continue;

                        GeometryRenderable gr = sr.getGeometryRenderable();
                        if(gr instanceof CustomGeometryRenderable)
                        {
                            CustomGeometryRenderable cgr = (CustomGeometryRenderable)gr;

                            workCullList[ret_val].customData =
                                cgr.processCull(transformStack[lastTxStack],
                                                viewMatrix,
                                                frustumPlanes,
                                                angularResolution);
                        }

                        // Check the visibility state and ignore if not visible.
                        if(sr.is2D())
                        {
                            resizeCullList(cullEndIndex);
                            workCullList[ret_val].renderable = (Pixmap)kids[i];
                            workCullList[ret_val].numLights = 0;
                            workCullList[ret_val].numClipPlanes = 0;

                            Matrix4d mat = transformStack[lastTxStack];

                            // Transpose the matrix in place as it is being copied
                            workCullList[ret_val].transform.set(mat);

                            ret_val++;
                        }
                        else
                        {
                            // Walk into the shape and check that we don't have any
                            // offscreen textures to render.
                            if(checkOffscreens)
                                checkForOffscreens(sr);

                            resizeCullList(ret_val);
                            if(lastAppearanceStack != 0)
                            {
                                OverrideRenderable ovr =
                                    appearanceStack[lastAppearanceStack];

                                AppearanceRenderable app_r =
                                    ovr.getAppearanceRenderable();

                                if(!ovr.useLocalOnlyAppearance())
                                {
                                    AppearanceRenderable leaf_app =
                                        sr.getAppearanceRenderable();

                                    app_r =
                                        new OverrideAppearanceProxyRenderable(app_r,
                                                                              leaf_app);
                                }

                                OverrideShapeProxyRenderable proxy_r =
                                    new OverrideShapeProxyRenderable(sr, app_r);
                                workCullList[ret_val].renderable = proxy_r;
                            }
                            else
                                workCullList[ret_val].renderable = r;

                            workCullList[ret_val].localFog = fogStack[lastFogStack];

                            Matrix4d mat = transformStack[lastTxStack];

                            workCullList[ret_val].transform.set(mat);

                            int src_size = (workCullList[ret_val].lights == null) ?
                                            0 :
                                            workCullList[ret_val].lights.length;

                            //int req_size = lastLight + lastGlobalLight +
                            //               lastGlobalBoundedLight;
                            int req_size = lastLight;

                            if(src_size < req_size)
                            {
                                // up the size of the array

                                VisualDetails[] tmp = new VisualDetails[req_size];
                                if(src_size != 0)
                                {
                                    System.arraycopy(workCullList[ret_val].lights,
                                                     0,
                                                     tmp,
                                                     0,
                                                     src_size);
                                }

                                for(int j = src_size; j < req_size; j++)
                                    tmp[j] = new VisualDetails();

                                workCullList[ret_val].lights = tmp;
                            }

                            // copy in the light information.
                            int valid_lights = 0;
                            BoundingVolume bv = cullable.getBounds();
                            workCullList[ret_val].cullableBounds = bv;
                            VisualDetails[] l_tmp = workCullList[ret_val].lights;
                            /*
                            for(int j = 0; j < lastGlobalLight; j++)
                            {
                                l_tmp[valid_lights].update(globalLightList[j],
                                                           globalLightTxList[j]);
                                valid_lights++;
                            }

                            for(int j = 0; j < lastGlobalBoundedLight; j++)
                            {
                                BoundingVolume effect =
                                    globalBoundedLightList[j].getEffectBounds();

                                if(checkBoundsIntersect(bv,
                                                        mat,
                                                        effect,
                                                        globalBoundedLightTxList[j]))
                                {
                                    l_tmp[valid_lights].update(
                                        //lightList[j],
                                        globalBoundedLightList[j],
                                        globalBoundedLightTxList[j]);
                                    valid_lights++;
                                }
                            }
                            */
                            for(int j = 0; j < lastLight; j++)
                            {
                                BoundingVolume effect = lightList[j].getEffectBounds();

                                if((effect == null) ||
                                   checkBoundsIntersect(bv,
                                                        mat,
                                                        effect,
                                                        lightTxList[j]))
                                {
                                    l_tmp[valid_lights].update(lightList[j],
                                                               lightTxList[j]);
                                    valid_lights++;
                                }
                            }

                            workCullList[ret_val].numLights = valid_lights;

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

                            // copy in the clip plane information.
                            VisualDetails[] c_tmp = workCullList[ret_val].clipPlanes;
                            workCullList[ret_val].numClipPlanes = lastClip;

                            for(int j = 0; j < lastClip; j++)
                                c_tmp[j].update(clipList[j], clipTxList[j]);
                            ret_val++;
                        }
                    }
                    else if(kids[i] instanceof CustomRenderable)
                    {
                        BoundingVolume bv = cullable.getBounds();
                        ret_val = findAllNodes((CustomRenderable)kids[i],
                                               ret_val,
                                               bv);
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
            else if(kids[i] instanceof CustomCullable)
            {
                ret_val = findAllNodes((CustomCullable)kids[i], ret_val);
            }

            // Now pop the stacks.
            if(is_tx)
                lastTxStack--;
        }

        // Pop the valid lights from the stack
        for(int i = 0; i < num_lights; i++) {
            lightList[--lastLight] = null;
        }
        for(int i = 0; i < num_clips; i++) {
            clipList[--lastClip] = null;
        }

        if(have_local_fog)
        {
            fogStack[lastFogStack] = null;
            lastFogStack--;
        }

        if(have_local_appearance)
        {
            appearanceStack[lastAppearanceStack] = null;
            lastAppearanceStack--;
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

                if(r instanceof ShapeRenderable)
                {
                    ShapeRenderable sr = (ShapeRenderable)r;

                    if(sr.isVisible())
                    {
                        GeometryRenderable gr = sr.getGeometryRenderable();
                        if(gr instanceof CustomGeometryRenderable)
                        {
                            CustomGeometryRenderable cgr = (CustomGeometryRenderable)gr;

                            workCullList[ret_val].customData =
                                cgr.processCull(transformStack[lastTxStack],
                                                viewMatrix,
                                                frustumPlanes,
                                                angularResolution);
                        }

                        // Check the visibility state and ignore if not visible.
                        if(sr.is2D())
                        {
                            resizeCullList(cullEndIndex);
                            workCullList[ret_val].renderable = r;
                            workCullList[ret_val].numLights = 0;
                            workCullList[ret_val].numClipPlanes = 0;

                            Matrix4d mat = transformStack[lastTxStack];

                            // Transpose the matrix in place as it is being copied
                            workCullList[ret_val].transform.set(mat);

                            ret_val++;
                        }
                        else
                        {
                            // Walk into the shape and check that we don't have any
                            // offscreen textures to render.
                            if(checkOffscreens)
                                checkForOffscreens((ShapeRenderable)r);

                            resizeCullList(cullEndIndex);
                            if(lastAppearanceStack != 0)
                            {
                                AppearanceRenderable app_r =
                                    appearanceStack[lastAppearanceStack].getAppearanceRenderable();

                                OverrideShapeProxyRenderable proxy_r =
                                    new OverrideShapeProxyRenderable(sr, app_r);
                                workCullList[ret_val].renderable = proxy_r;
                            }
                            else
                                workCullList[ret_val].renderable = r;

                            workCullList[ret_val].localFog = fogStack[lastFogStack];

                            Matrix4d mat = transformStack[lastTxStack];

                            // Transpose the matrix in place as it is being copied
                            workCullList[cullEndIndex].transform.set(mat);

                            int src_size = (workCullList[cullEndIndex].lights == null) ?
                                            0 :
                                            workCullList[cullEndIndex].lights.length;

                            //int req_size = lastLight + lastGlobalLight +
                            //               lastGlobalBoundedLight;
                            int req_size = lastLight;

                            if(src_size < req_size)
                            {
                                // up the size of the array

                                VisualDetails[] tmp = new VisualDetails[req_size];
                                if(src_size != 0)
                                {
                                    System.arraycopy(workCullList[cullEndIndex].lights,
                                                     0,
                                                     tmp,
                                                     0,
                                                     src_size);
                                }

                                for(int j = src_size; j < req_size; j++)
                                    tmp[j] = new VisualDetails();

                                workCullList[cullEndIndex].lights = tmp;
                            }

                            // copy in the light information.
                            int valid_lights = 0;
                            BoundingVolume bv = cullable.getBounds();
                            workCullList[ret_val].cullableBounds = bv;
                            VisualDetails[] l_tmp = workCullList[ret_val].lights;
                            /*
                            for(int j = 0; j < lastGlobalLight; j++)
                            {
                                l_tmp[valid_lights].update(globalLightList[j],
                                                           globalLightTxList[j]);
                                valid_lights++;
                            }

                            for(int j = 0; j < lastGlobalBoundedLight; j++)
                            {
                                BoundingVolume effect =
                                    globalBoundedLightList[j].getEffectBounds();

                                if(checkBoundsIntersect(bv,
                                                        mat,
                                                        effect,
                                                        globalBoundedLightTxList[j]))
                                {
                                    l_tmp[valid_lights].update(
                                        globalBoundedLightList[j],
                                        globalBoundedLightTxList[j]);
                                    valid_lights++;
                                }
                            }
                            */
                            for(int j = 0; j < lastLight; j++)
                            {
                                BoundingVolume effect = lightList[j].getEffectBounds();

                                if((effect == null) ||
                                   checkBoundsIntersect(bv,
                                                        mat,
                                                        effect,
                                                        lightTxList[j]))
                                {
                                    l_tmp[valid_lights].update(lightList[j],
                                                               lightTxList[j]);
                                    valid_lights++;
                                }
                            }

                            workCullList[ret_val].numLights = valid_lights;

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
                else if(current instanceof CustomRenderable)
                {
                    BoundingVolume bv = cullable.getBounds();
                    ret_val =
                        findAllNodes((CustomRenderable)current, ret_val, bv);
                }
            }
        }
        else if(current instanceof CustomCullable)
        {
            ret_val = findAllNodes((CustomCullable)current, ret_val);
        }

        // Now pop the stack.
        if(is_tx)
            lastTxStack--;

        return ret_val;
    }

    /**
     * Convenience method to check if two sets of bounds, with their
     * associated transforms, intersect. The transforms are based on
     * the local scene root.
     */
    private boolean checkBoundsIntersect(BoundingVolume geomBounds,
                                         Matrix4d geomTx,
                                         BoundingVolume testBounds,
                                         float[] testTx)
    {
        return true;
    }

    /**
     * Recalculate the current frustum plane equations for the given render
     * environment data.
     *
     * @param viewEnv Current viewing environment data from the scene
     * @param envData The place to source the environment from
     */
    private void updateFrustum(ViewEnvironmentCullable viewEnv,
                               GraphicsEnvironmentData envData)
    {
        viewEnv.getProjectionMatrix(projectionMatrix);
        prjMatrix.m00 = projectionMatrix[0];
        prjMatrix.m01 = projectionMatrix[1];
        prjMatrix.m02 = projectionMatrix[2];
        prjMatrix.m03 = projectionMatrix[3];
        prjMatrix.m10 = projectionMatrix[4];
        prjMatrix.m11 = projectionMatrix[5];
        prjMatrix.m12 = projectionMatrix[6];
        prjMatrix.m13 = projectionMatrix[7];
        prjMatrix.m20 = projectionMatrix[8];
        prjMatrix.m21 = projectionMatrix[9];
        prjMatrix.m22 = projectionMatrix[10];
        prjMatrix.m23 = projectionMatrix[11];
        prjMatrix.m30 = projectionMatrix[12];
        prjMatrix.m31 = projectionMatrix[13];
        prjMatrix.m32 = projectionMatrix[14];
        prjMatrix.m33 = projectionMatrix[15];

        double x, y, z, w;

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

        /* Extract the numbers for the RIGHT plane */
        x = prjMatrix.m03 - prjMatrix.m00;
        y = prjMatrix.m13 - prjMatrix.m10;
        z = prjMatrix.m23 - prjMatrix.m20;
        w = prjMatrix.m33 - prjMatrix.m30;

        /* Normalize the result */
        float t = 1.0f / (float)Math.sqrt(x * x + y * y + z * z);
        frustumPlanes[0].x = x * t;
        frustumPlanes[0].y = y * t;
        frustumPlanes[0].z = z * t;
        frustumPlanes[0].w = w * t;

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

        /* Extract the FAR plane */
        x = prjMatrix.m03 - prjMatrix.m02;
        y = prjMatrix.m13 - prjMatrix.m12;
        z = prjMatrix.m23 - prjMatrix.m22;
        w = prjMatrix.m33 - prjMatrix.m32;

        /////////////////////////////////////////////////////////////////
        // rem - temporary change to fix x3d earth culling issue
        //t = 1.0f / (float)Math.sqrt(x * x + y * y + z * z);
        t = (float)Math.sqrt(x * x + y * y + z * z);
        if(t == 0)
            t = 1/1.19209290E-07f;
        else
            t = 1/t;
        /////////////////////////////////////////////////////////////////
        frustumPlanes[4].x = x * t;
        frustumPlanes[4].y = y * t;
        frustumPlanes[4].z = z * t;
        frustumPlanes[4].w = w * t;

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
    }

    /**
     * Resize the list if needed. Marked as final in order to encourage the
     * compiler to inline the code for faster execution
     */
    private final void resizeGlobalLightList()
    {
        if((lastGlobalLight + 1) == globalLightList.length)
        {
            int old_size = globalLightList.length;
            int new_size = old_size + LIST_INCREMENT;

            EffectRenderable[] tmp_nodes = new EffectRenderable[new_size];

            System.arraycopy(globalLightList, 0, tmp_nodes, 0, old_size);

            globalLightList = tmp_nodes;

            float[][] tmp_tx  = new float[new_size][];
            System.arraycopy(globalLightTxList, 0, tmp_tx, 0, old_size);

            for(int i = old_size; i < new_size; i++)
                tmp_tx[i] = new float[16];

            globalLightTxList = tmp_tx;
        }
    }

    /**
     * Resize the list if needed. Marked as final in order to encourage the
     * compiler to inline the code for faster execution
     */
    private final void resizeGlobalBoundedLightList()
    {
        if((lastGlobalBoundedLight + 1) == globalBoundedLightList.length)
        {
            int old_size = globalBoundedLightList.length;
            int new_size = old_size + LIST_INCREMENT;

            Light[] tmp_nodes = new Light[new_size];

            System.arraycopy(globalBoundedLightList, 0, tmp_nodes, 0, old_size);

            globalBoundedLightList = tmp_nodes;

            float[][] tmp_tx  = new float[new_size][];
            System.arraycopy(globalBoundedLightTxList, 0, tmp_tx, 0, old_size);

            for(int i = old_size; i < new_size; i++)
                tmp_tx[i] = new float[16];

            globalBoundedLightTxList = tmp_tx;
        }
    }

    /*
     * Add global lights as necessary.
     *
     * @param cullEndIndex The current last item on the cull list
     */
    private void processGlobalLights(int cullEndIndex) {

        int num_globals = lastGlobalLight + lastGlobalBoundedLight;

        if(num_globals == 0)
            return;

        for (int i = 0; i < cullEndIndex; i++)
        {
            Renderable r = workCullList[i].renderable;
            boolean addLights = false;

            if (r instanceof ShapeRenderable)
            {
                ShapeRenderable sr = (ShapeRenderable)r;
                addLights = sr.isVisible() && !sr.is2D();

            }
            else if (r instanceof CustomRenderable)
            {
                addLights = true;
            }

            if(!addLights)
                continue;

            int src_size = (workCullList[i].lights == null) ?
                0 :
                workCullList[i].lights.length;

            int valid_lights = workCullList[i].numLights;
            int req_size = valid_lights + num_globals;

            if(src_size < req_size)
            {
                // up the size of the array

                VisualDetails[] tmp = new VisualDetails[req_size];
                if(src_size != 0)
                {
                    System.arraycopy(workCullList[i].lights,
                        0,
                        tmp,
                        0,
                        src_size);
                }

                for(int j = src_size; j < req_size; j++)
                    tmp[j] = new VisualDetails();

                workCullList[i].lights = tmp;
            }

            // copy in the light information.
            VisualDetails[] l_tmp = workCullList[i].lights;

            for(int j = 0; j < lastGlobalLight; j++)
            {
                l_tmp[valid_lights].update(globalLightList[j],
                                           globalLightTxList[j]);
                valid_lights++;
            }

            for(int j = 0; j < lastGlobalBoundedLight; j++)
            {
                if(checkBoundsIntersect(
                    workCullList[i].cullableBounds,
                    workCullList[i].transform,
                    globalBoundedLightList[j].getEffectBounds(),
                    globalBoundedLightTxList[j]))
                {
                    l_tmp[valid_lights].update(globalBoundedLightList[j],
                                               globalBoundedLightTxList[j]);
                    valid_lights++;
                }
            }

            workCullList[i].numLights = valid_lights;
        }
    }
}
