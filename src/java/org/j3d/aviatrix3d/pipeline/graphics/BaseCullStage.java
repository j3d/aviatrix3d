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
import org.j3d.util.*;

import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ArrayList;

import javax.vecmath.Matrix4f;

// Local imports
import org.j3d.aviatrix3d.rendering.*;

import org.j3d.aviatrix3d.ViewEnvironment;
import org.j3d.aviatrix3d.pipeline.RenderableRequestData;


/**
 * Common implementation of many of the capabilities required by any cull
 * stage.
 * <p>
 *
 * The class takes care of the majority of the basic implementation
 * requirements such as resizing various data structures, traversing the
 * top of the scene graph super structure (layers, viewports et al) and
 * processing offscreen sources, before sending onto the next stage.
 * All the derived class is required to do is handle traversing a single
 * scene instance and registering any pBuffer offscreens it happens to find.
 * This class will make sure that they are looked after appropriately.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>sharedViewpointMsg: Viewpoint with multiple parents found</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 3.38 $
 */
public abstract class BaseCullStage implements GraphicsCullStage
{
    /** Message when setting the active view if it contains a shared parent */
    private static final String SHARED_VP_PROP =
        "org.j3d.aviatrix3d.pipeline.graphics.BaseCullStage.sharedViewpointMsg";

    /** Message when the screen orientation axis is zero */
	private static final String ZERO_LENGTH_AXIS_PROP =
        "org.j3d.aviatrix3d.pipeline.graphics.BaseCullStage.nullAxisMsg";

    /** Initial scene depth used for the transform stack */
    private static final int TRANSFORM_DEPTH_SIZE = 64;

    /** The increment size of the stack if it gets overflowed */
    private static final int STACK_INCREMENT = 32;

    /** Initial size of the cull list for the number of offscreen surfaces */
    protected static final int LIST_START_LENGTH = 1;

    /** The increment size of the list if it gets overflowed */
    protected static final int LIST_INCREMENT = 20;

    /** The initial size of the light list */
    protected static final int LIGHT_START_SIZE = 32;

    /** The increment size of the list if it gets overflowed */
    protected static final int LIGHT_INCREMENT = 8;

    /** If set, recurse into Shape3Ds looking for offscreen textures */
    protected boolean checkOffscreens;

    /** Buffer list of sub scenes that still need processing */
    protected OffscreenCullable[] pendingSubscenes;

    /** Buffer list of sub scenes that still need processing */
    protected OffscreenCullable[] pendingParents;

    /** Index of the last subscene in the list */
    protected int lastSubscene;

    /** Working list of the layers that are to be passed to the output */
    protected ViewportCollection[][] workLayers;

    /** Final list of the layers that are to be passed to the output */
    protected ViewportCollection[][] outputLayers;

    /** Working list of the counts of layers in each subscene */
    protected int[] workLayerCounts;

    /** Final list of the counts of layers in each subscene */
    protected int[] outputLayerCounts;

    /** Index of the the next layer output */
    protected int lastOutputLayer;

    /** List of valid subscene parents as we process them */
    protected OffscreenBufferRenderable[][] validSceneParents;

    /** List of valid subscene parents as we process them */
    protected OffscreenBufferRenderable[][] outputSceneParents;

    /** List that is being used to fill values into */
    protected GraphicsCullOutputDetails[] workCullList;

    /** Index to the next output list in the validCullList */
    protected int lastOutputList;

    /** A stack used to control the depth of the transform tree */
    protected Matrix4f[] transformStack;

    /** Index to the next place to add items in the transformStack */
    protected int lastTxStack;

    /** A stack used to control the depth of the fog nodes down the tree */
    protected EffectRenderable[] fogStack;

    /** Index to the next place to add items in the fogStack */
    protected int lastFogStack;

    /** The list of lights nodes currently valid while traversing. */
    protected EffectRenderable[] lightList;

    /** The list of light transforms currently valid while traversing */
    protected float[][] lightTxList;

    /** Index to the next place to add items in the lightList */
    protected int lastLight;

    /** The list of clip plane nodes currently valid while traversing. */
    protected EffectRenderable[] clipList;

    /** The list of clip transforms currently valid while traversing */
    protected float[][] clipTxList;

    /** Index to the next place to add items in the clipList */
    protected int lastClip;

    /** A stack used to control the appearance override nodes down the tree */
    protected OverrideRenderable[] appearanceStack;

    /** Index to the next place to add items in the fogStack */
    protected int lastAppearanceStack;

    /** Collection of offscreen textures we've found this frame */
    protected HashSet processedPBufferTextures;

    /** Collection of textures we've found for the current parent texture */
    private HashSet visitedNodes;

    /** Handler for the output */
    private CulledGeometryReceiver receiver;

    /**
     * The active parent scene of the scene we are processing. If we are
     * processing the top level scene then this will be null
     */
    protected OffscreenCullable activeParent;

    /** Path to the current viewpoint */
    protected ArrayList<Cullable> currentViewpointPath;

    /** Matrix used for pre-computing the view stack */
    protected Matrix4f viewMatrix1;

    /** Matrix used for pre-computing the view stack */
    protected Matrix4f viewMatrix2;

    /** Rotation matrix for the orientation provided by user */
    protected Matrix4f orientationMatrix;

    /** Storage variable for the screen orientation values */
    protected float[] screenOrientation;

    /** Storage variable for the eye offset values */
    protected float[] eyePoint;

    /** Flag to say explicit screen orientation values have been provided */
    protected boolean useOrientation;

    /** Flag to say explicit eyepoint values have been provided */
    protected boolean useEyePoint;

    /** Flag indicating a shutdown of the current processing is requested */
    protected boolean terminate;

    /** Temporary variable for fetching layers from sub scenes */
    protected LayerCullable[] layersTmp;

    /** Matrix utility code for doing inversions */
    protected MatrixUtils matrixUtils;

    /** Local reporter to put errors in */
    protected ErrorReporter errorReporter;

    /**
     * Create a basic instance of this class with the list initial internal
     * setup for the given number of renderable surfaces. The size is just an
     * initial esstimate, and is used for optimisation purposes to prevent
     * frequent array reallocations internally. As such, the number does not
     * have to be perfect, just good enough.
     *
     * @param numSurfaces Total number of surfaces to prepare rendering for
     */
    public BaseCullStage(int numSurfaces)
    {
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        validSceneParents = new OffscreenBufferRenderable[numSurfaces][2];
        outputSceneParents = new OffscreenBufferRenderable[numSurfaces][2];
        pendingSubscenes = new OffscreenCullable[numSurfaces];
        pendingParents = new OffscreenCullable[numSurfaces];

        // Default for the most uses of a single scene/layer
        workLayers = new ViewportCollection[numSurfaces][1];
        workLayerCounts = new int[numSurfaces];

        outputLayers = new ViewportCollection[numSurfaces][1];
        outputLayerCounts = new int[numSurfaces];

        for(int i = 0; i < numSurfaces; i++)
            workLayers[i][0] = new ViewportCollection();

        layersTmp = new LayerCullable[1];

        processedPBufferTextures = new HashSet();
        visitedNodes = new HashSet();

        currentViewpointPath = new ArrayList<Cullable>(TRANSFORM_DEPTH_SIZE);

        viewMatrix1 = new Matrix4f();
        viewMatrix2 = new Matrix4f();
        orientationMatrix = new Matrix4f();
        orientationMatrix.setIdentity();

        screenOrientation = new float[4];
        eyePoint = new float[3];

        useEyePoint = false;
        useOrientation = false;

        // populate the list with valid instances...

        transformStack = new Matrix4f[TRANSFORM_DEPTH_SIZE];

        // populate the list with valid instances...
        for(int i = 0; i < TRANSFORM_DEPTH_SIZE; i++)
            transformStack[i] = new Matrix4f();

        fogStack = new EffectRenderable[TRANSFORM_DEPTH_SIZE];
        appearanceStack = new OverrideRenderable[TRANSFORM_DEPTH_SIZE];

        lightList = new EffectRenderable[LIGHT_START_SIZE];
        lightTxList = new float[LIGHT_START_SIZE][16];

        clipList = new EffectRenderable[LIGHT_START_SIZE];
        clipTxList = new float[LIGHT_START_SIZE][16];

        checkOffscreens = true;

        matrixUtils = new MatrixUtils();
    }

    //---------------------------------------------------------------
    // Methods defined by CullStage
    //---------------------------------------------------------------

    /**
     * Update and cull the scenegraph defined by a set of layers. This
     * generates an ordered list of nodes to render. It will not return until
     * the culling is complete.
     *
     * @param otherData data to be passed along unprocessed
     * @param profilingData The timing and load data on each stage
     * @param layers The collection of layers, in order, to render
     * @param numLayers The number of valid layers to use
     */
    public void cull(RenderableRequestData otherData,
                     ProfilingData profilingData,
                     LayerCullable[] layers,
                     int numLayers)
    {
        long stime = System.nanoTime();

        terminate = false;

        if(workLayers[0].length < numLayers)
        {
            workLayers = new ViewportCollection[1][numLayers];
            for(int i = 0; i < numLayers; i++)
                workLayers[0][i] = new ViewportCollection();

            workLayerCounts = new int[numLayers];
        }

        // Pretty simple implementation - for each layer, look at each viewport
        // and then each layer within that viewport. Find the scene and
        // process it.

        int layer = 0;
        lastOutputLayer = 0;
        lastOutputList = 0;
        lastSubscene = 0;

        for(int i = 0; i < numLayers && !terminate; i++)
        {
            if(layers[i] == null)
                continue;

            LayerCullable c = layers[i];
            processLayer(c, 0, i);

            layer++;
        }

        workLayerCounts[0] = layer;
        lastOutputList++;
        lastOutputLayer++;

        if(checkOffscreens)
        {
            // May need to shift inside the loop for cases where textures
            // contain scenes which contain further textures. Play a wait and
            // see game for now.
            resizeOutputLists();

            for(int i = 0; i < lastSubscene && !terminate; i++)
            {
                activeParent = pendingSubscenes[i];
                visitedNodes.clear();

                layer = 0;

                if(processedPBufferTextures.contains(pendingSubscenes[i]))
                {
                    validSceneParents[lastOutputList][0] =
                        pendingSubscenes[i].getOffscreenRenderable();
                    validSceneParents[lastOutputList][1] =
                        pendingParents[i] == null ?
                        null :
                        pendingParents[i].getOffscreenRenderable();
                }
                else
                {
                    int num_layers = pendingSubscenes[i].numCullableChildren();

                    for(int j = 0; j < num_layers; j++)
                    {

                        LayerCullable lc =
                             pendingSubscenes[i].getCullableLayer(j);

                        // Reset these for each separate subscene
                        lastLight = 0;
                        lastClip = 0;
                        lastTxStack = 0;
                        lastFogStack = 0;
                        lastAppearanceStack = 0;

                        processLayer(lc, i + 1, j);
                        layer++;
                    }

                    processedPBufferTextures.add(pendingSubscenes[i]);

                    validSceneParents[lastOutputList][0] =
                        pendingSubscenes[i].getOffscreenRenderable();
                    validSceneParents[lastOutputList][1] =
                        pendingParents[i] == null ?
                        null :
                        pendingParents[i].getOffscreenRenderable();
                }

                workLayerCounts[lastOutputLayer] = layer;
                lastOutputLayer++;
                lastOutputList++;
            }

            // Now reverse the list since we're doing a breadth-first search
            // for the subscenes, we end up with the most nested subscene last.
            // This is bad. So now reverse the order of the list so that the
            // most nested collection is first to be rendered and the main
            // screen is the last item.
            if(outputSceneParents.length < lastOutputList)
                outputSceneParents =
                    new OffscreenBufferRenderable[lastOutputList][2];

            for(int i = 0; i < lastOutputList; i++)
            {
                outputSceneParents[i][0] =
                    validSceneParents[lastOutputList - i - 1][0];
                outputSceneParents[i][1] =
                    validSceneParents[lastOutputList - i - 1][1];
            }

            if(outputLayers.length < lastOutputLayer)
            {
                outputLayers = new ViewportCollection[lastOutputLayer][];
                outputLayerCounts = new int[lastOutputLayer];
            }

            for(int i = 0; i < lastOutputLayer; i++)
            {
                outputLayers[i] = workLayers[lastOutputLayer - i - 1];
                outputLayerCounts[i] = workLayerCounts[lastOutputLayer - i - 1];
            }
        }
        else
        {
            // It is likely in this case that we're doing single-scene
            // rendering and will be doing it all the time. Possibly optimise
            // this by avoiding the copy altogether, but not 100% this won't
            // break in some corner case right now.
            outputSceneParents[0][0] = validSceneParents[0][0];
            outputSceneParents[0][1] = validSceneParents[0][1];

            outputLayers[0] = workLayers[0];
            outputLayerCounts[0] = workLayerCounts[0];
        }

        processedPBufferTextures.clear();
        visitedNodes.clear();

        cleanupOldRefs();

        profilingData.sceneCullTime = (System.nanoTime() - stime);

        if((receiver != null) && !terminate)
            receiver.culledOutput((GraphicsRequestData)otherData,
                                  (GraphicsProfilingData)profilingData,
                                  outputLayers,
                                  outputLayerCounts,
                                  lastOutputLayer,
                                  outputSceneParents);
    }

    /**
     * Register a reciever for the output of the sorter. If the value is null,
     * it will clear the currently set receiver.
     *
     * @param sgr The receiver instance to add or null
     */
    public void setCulledGeometryReceiver(CulledGeometryReceiver sgr)
    {
        receiver = sgr;
    }

    /**
     * Set the flag for whether to check for offscreen textures or not. By
     * default, this flag is set to true.
     *
     * @param state true if offscreen textures should be looked for
     */
    public void setOffscreenCheckEnabled(boolean state)
    {
        checkOffscreens = state;
    }

    /**
     * Find out what the current offscreen check state is.
     *
     * @return true if the checking is being performed
     */
    public boolean isOffscreenCheckEnabled()
    {
        return checkOffscreens;
    }

    /**
     * Set the eyepoint offset from the centre position. This is used to model
     * offset view frustums, such as multiple displays or a powerwall. This
     * method will be called with the appropriate values from the
     * RenderPipeline that this culler is inserted into.
     *
     * @param x The x axis offset
     * @param y The y axis offset
     * @param z The z axis offset
     */
    public void setEyePointOffset(float x, float y, float z)
    {
        useEyePoint = true;
        eyePoint[0] = x;
        eyePoint[1] = y;
        eyePoint[2] = z;
    }

    /**
     * Set the orientation of this screen relative to the user's normal view
     * direction. The normal orientation of the screen is along the negative
     * Z axis. This method provides and axis-angle reorientation of that
     * direction to one that is facing the screen. Typically this will just
     * involve a rotation around the Y axis of some amount (45 and 90 deg
     * being the most common used in walls and caves). This method will be
     * called with the appropriate values from the RenderPipeline that this
     * culler is inserted into.
     *
     * @param x The x axis component
     * @param y The y axis component
     * @param z The z axis component
     * @param a The angle to rotate around the axis in radians
     * @throws IllegalArgumentException The length of the axis is zero
     */
    public void setScreenOrientation(float x, float y, float z, float a)
        throws IllegalArgumentException
    {
        float n = x * x + y * y + z * z;
        if(n == 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(ZERO_LENGTH_AXIS_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Float(x), new Float(y), new Float(z) };
            Format[] fmts = { n_fmt, n_fmt, n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        useOrientation = true;
        screenOrientation[0] = x;
        screenOrientation[1] = y;
        screenOrientation[2] = z;
        screenOrientation[3] = a;

        double rcos = Math.cos(a);
        double rsin = Math.sin(a);

        orientationMatrix.m00 = (float)(rcos + x * x * (1 - rcos));
        orientationMatrix.m01 = (float)(z * rsin + y * x * (1 - rcos));
        orientationMatrix.m02 = (float)(-y * rsin + z * x * (1 - rcos));
        orientationMatrix.m10 = (float)(-z * rsin + x * y * (1 - rcos));
        orientationMatrix.m11 = (float)(rcos + y * y * (1 - rcos));
        orientationMatrix.m12 = (float)(x * rsin + z * y * (1 - rcos));
        orientationMatrix.m20 = (float)(y * rsin + x * z * (1 - rcos));
        orientationMatrix.m21 = (float)(-x * rsin + y * z * (1 - rcos));
        orientationMatrix.m22 = (float)(rcos + z * z * (1 - rcos));
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
    // Local methods
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
    protected abstract void cullScene(RenderPassCullable scene,
                                      int subsceneId,
                                      int layerId,
                                      int viewIndex,
                                      int layerIndex);

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
    protected abstract void cullScene2D(RenderPassCullable scene,
                                        int subsceneId,
                                        int layerId,
                                        int viewIndex,
                                        int layerIndex);

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
    protected abstract void cullRenderPass(RenderPassCullable pass,
                                           int passNumber,
                                           int subsceneId,
                                           int layerId,
                                           int viewIndex,
                                           int layerIndex);

    /**
     * Process the layers of a pbuffer texture source.
     *
     * @param subsceneId The index of the subscene in the viewport output
     * @param layerId The index of the layer in the output
     */
    protected void processLayer(LayerCullable layer,
                                int subsceneId,
                                int layerId)
    {
        if(layer == null)
            return;

        int num_views = layer.numCullableChildren();

        workLayers[subsceneId][layerId].ensureCapacity(num_views);
        workLayers[subsceneId][layerId].numViewports = 0;

        for(int i = 0; i < num_views; i++)
        {
            ViewportCullable c = layer.getCullableViewport(i);
            cullViewport(c, subsceneId, layerId, i);

            workLayers[subsceneId][layerId].numViewports++;
        }
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
    protected void cullViewport(ViewportCullable view,
                                int subsceneId,
                                int layerId,
                                int viewIndex)
    {
        if(view == null || !view.isValid())
            return;

        int num_layers = view.numCullableChildren();

        ViewportLayerCollection c =
            workLayers[subsceneId][layerId].viewports[viewIndex];

        c.ensureSceneCapacity(num_layers);
        c.numBuckets = 0;
        c.numScenes = 0;
        c.numMultipass = 0;

        int scene_count = 0;

        for(int i = 0; i < num_layers; i++)
        {
            ViewportLayerCullable layer = view.getCullableLayer(i);
            SceneCullable sc = layer.getCullableScene();

            if(sc == null)
                continue;

            if(layer.isMultipassViewport())
                cullMultipassViewportLayer(sc,
                                           subsceneId,
                                           layerId,
                                           viewIndex,
                                           scene_count);
            else
                cullSingleViewportLayer(sc,
                                        subsceneId,
                                        layerId,
                                        viewIndex,
                                        scene_count);

            scene_count++;
        }
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
    protected void cullSingleViewportLayer(SceneCullable scene,
                                           int subsceneId,
                                           int layerId,
                                           int viewIndex,
                                           int layerIndex)
    {
        ViewportLayerCollection c =
            workLayers[subsceneId][layerId].viewports[viewIndex];

        c.ensureSceneCapacity(1);
        c.scenes[layerIndex].ensureCapacity(1);
        RenderPassCullable pass = scene.getCullablePass(0);

        if(pass.is2D())
        {
            fill2DEnvData(scene, c.scenes[layerIndex].data);
            c.sceneType[c.numBuckets] = ViewportLayerCollection.FLAT_SCENE;

            cullScene2D(pass,
                        subsceneId,
                        layerId,
                        viewIndex,
                        layerIndex);
        }
        else
        {
            fillSingleEnvData(scene, c.scenes[layerIndex].data);
            c.sceneType[c.numBuckets] = ViewportLayerCollection.SINGLE_SCENE;

            cullScene(pass,
                      subsceneId,
                      layerId,
                      viewIndex,
                      layerIndex);
        }

        c.numBuckets++;
        c.numScenes++;
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
    protected void cullMultipassViewportLayer(SceneCullable scene,
                                              int subsceneId,
                                              int layerId,
                                              int viewIndex,
                                              int layerIndex)
    {
        int num_passes = scene.numCullableChildren();

        if(num_passes == 0)
            return;

        ViewportLayerCollection c =
            workLayers[subsceneId][layerId].viewports[viewIndex];

        c.sceneType[c.numBuckets] = ViewportLayerCollection.MULTIPASS_SCENE;

        c.ensureMultipassCapacity(c.numMultipass + 1);
        c.multipass[c.numMultipass].mainScene.ensureCapacity(num_passes);

        fillMultipassEnvData(scene,
                             c.multipass[c.numMultipass].mainScene.globalData);

        c.multipass[c.numMultipass].mainScene.numPasses = num_passes;

        int pass_num = 0;

        for(int i = 0; i < num_passes; i++)
        {
            RenderPassCullable pass = scene.getCullablePass(i);

            if((pass == null) || !pass.isEnabled())
                continue;

            cullRenderPass(pass,
                           pass_num,
                           subsceneId,
                           layerId,
                           viewIndex,
                           layerIndex);

            pass_num++;
        }

        c.numBuckets++;
        c.numMultipass++;
    }

    /**
     * Take a simple scene and fill in a GraphicsEnvironmentData instance.
     *
     * @param scene The scene to take data from
     * @param envData Data instance to copy it to
     */
    protected void fillSingleEnvData(SceneCullable scene,
                                     GraphicsEnvironmentData envData)
    {
        envData.userData = scene.getUserData();
        envData.effectsProcessor = scene.getRenderEffectsProcessor();

        ViewEnvironmentCullable view = scene.getViewCullable();
        envData.viewProjectionType = view.getProjectionType();
        envData.useStereo = view.isStereoEnabled();

        RenderPassCullable pass = scene.getCullablePass(0);

        EnvironmentCullable vp = pass.getViewpointCullable();
        envData.viewpoint = (ObjectRenderable)vp.getRenderable();

        EnvironmentCullable  bg = pass.getBackgroundCullable();
        if(bg != null)
            envData.background = (ObjectRenderable)bg.getRenderable();
        else
            envData.background = null;


        LeafCullable fog = pass.getFogCullable();
        if(fog != null)
            envData.fog = (ObjectRenderable)fog.getRenderable();
        else
            envData.fog = null;

        envData.eyeOffset[0] = eyePoint[0];
        envData.eyeOffset[1] = eyePoint[1];
        envData.eyeOffset[2] = eyePoint[2];

        // Calculate frustum parameters based on view environment
        int[] viewport = view.getViewportDimensions();
        envData.setViewport(viewport);

        int[] scissor = view.getScissorDimensions();

        // Only set scissor if width and height are non-zero, otherwise
        // use the viewport dimensions for the scissor call
        if(scissor[ViewEnvironmentCullable.VIEW_WIDTH] > 0 &&
           scissor[ViewEnvironmentCullable.VIEW_HEIGHT] > 0)
            envData.setScissor(scissor);
        else
            envData.setScissor(viewport);

        view.getViewFrustum(envData.viewFrustum);

        if(useEyePoint)
        {
            envData.viewFrustum[0] -= eyePoint[0];
            envData.viewFrustum[1] -= eyePoint[0];
            envData.viewFrustum[2] -= eyePoint[1];
            envData.viewFrustum[3] -= eyePoint[1];
            envData.viewFrustum[4] -= eyePoint[2];
            envData.viewFrustum[5] -= eyePoint[2];
        }

        // walk back up the path to find the current root of
        // the scene graph and check that there is no SharedGroup
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

                    String msg = intl_mgr.getString(SHARED_VP_PROP);
                    throw new IllegalArgumentException(msg);
                }

                parent = ((GroupCullable)parent).getCullableParent();
            }
            else if(parent instanceof SingleCullable)
            {
                if(((SingleCullable)parent).hasMultipleParents())
                {
                    I18nManager intl_mgr = I18nManager.getManager();

                    String msg = intl_mgr.getString(SHARED_VP_PROP);
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
            viewMatrix2.mul(viewMatrix1);
        }

        currentViewpointPath.clear();

        if(useOrientation)
            viewMatrix2.mul(orientationMatrix);

        envData.viewTransform.set(viewMatrix2);

        matrixUtils.inverse(envData.viewTransform, viewMatrix2);
        envData.cameraTransform[0] = viewMatrix2.m00;
        envData.cameraTransform[1] = viewMatrix2.m10;
        envData.cameraTransform[2] = viewMatrix2.m20;
        envData.cameraTransform[3] = viewMatrix2.m30;
        envData.cameraTransform[4] = viewMatrix2.m01;
        envData.cameraTransform[5] = viewMatrix2.m11;
        envData.cameraTransform[6] = viewMatrix2.m21;
        envData.cameraTransform[7] = viewMatrix2.m31;
        envData.cameraTransform[8] = viewMatrix2.m02;
        envData.cameraTransform[9] = viewMatrix2.m12;
        envData.cameraTransform[10] = viewMatrix2.m22;
        envData.cameraTransform[11] = viewMatrix2.m32;
        envData.cameraTransform[12] = viewMatrix2.m03;
        envData.cameraTransform[13] = viewMatrix2.m13;
        envData.cameraTransform[14] = viewMatrix2.m23;
        envData.cameraTransform[15] = viewMatrix2.m33;

        // Now do almost the same thing with the background node. Only
        // difference is that this time we deliberately set the translation
        // components to zero.
        // walk back up the path to find the current root of
        // the scene graph and check that there is no SharedGroup
        // instances along the way.
        if(envData.background != null)
        {
            parent = bg.getCullableParent();

            while(parent != null)
            {
                if(parent instanceof TransformCullable)
                    currentViewpointPath.add(parent);

                if(parent instanceof GroupCullable)
                {
                    if(((GroupCullable)parent).hasMultipleParents())
					{
						I18nManager intl_mgr = I18nManager.getManager();

						String msg = intl_mgr.getString(SHARED_VP_PROP);
						throw new IllegalArgumentException(msg);
					}

                    parent = ((GroupCullable)parent).getCullableParent();
                }
                else if(parent instanceof SingleCullable)
                {
                    if(((SingleCullable)parent).hasMultipleParents())
					{
						I18nManager intl_mgr = I18nManager.getManager();

						String msg = intl_mgr.getString(SHARED_VP_PROP);
						throw new IllegalArgumentException(msg);
					}

                    parent = ((SingleCullable)parent).getCullableParent();
                }
                else
                    parent = null;
            }

            last_tx = currentViewpointPath.size();
            viewMatrix2.setIdentity();

            for(int i = last_tx - 1; i >= 0; i--)
            {
                TransformCullable c =
                    (TransformCullable)currentViewpointPath.get(i);
                c.getTransform(viewMatrix1);
                viewMatrix2.mul(viewMatrix1);
            }

            currentViewpointPath.clear();

            viewMatrix2.m30 = 0;
            viewMatrix2.m31 = 0;
            viewMatrix2.m32 = 0;

            viewMatrix2.mul(envData.viewTransform);

            envData.backgroundTransform[0] = viewMatrix2.m00;
            envData.backgroundTransform[1] = viewMatrix2.m01;
            envData.backgroundTransform[2] = viewMatrix2.m02;
            envData.backgroundTransform[3] = 0;
            envData.backgroundTransform[4] = viewMatrix2.m10;
            envData.backgroundTransform[5] = viewMatrix2.m11;
            envData.backgroundTransform[6] = viewMatrix2.m12;
            envData.backgroundTransform[7] = 0;
            envData.backgroundTransform[8] = viewMatrix2.m20;
            envData.backgroundTransform[9] = viewMatrix2.m21;
            envData.backgroundTransform[10] = viewMatrix2.m22;
            envData.backgroundTransform[11] = 0;
            envData.backgroundTransform[12] = viewMatrix2.m30;
            envData.backgroundTransform[13] = viewMatrix2.m31;
            envData.backgroundTransform[14] = viewMatrix2.m32;
            envData.backgroundTransform[15] = viewMatrix2.m33;

            setupBackgroundFrustum(0.01,
                                   1.1,
                                   view.getFieldOfView(),
                                   envData);
        }
    }

    /**
     * Take a 2D scene and fill in a GraphicsEnvironmentData instance.
     *
     * @param scene The scene to take data from
     * @param envData Data instance to copy it to
     */
    protected void fill2DEnvData(SceneCullable scene,
                                 GraphicsEnvironmentData envData)
    {
        envData.userData = scene.getUserData();
        envData.effectsProcessor = scene.getRenderEffectsProcessor();


        RenderPassCullable pass = scene.getCullablePass(0);

        EnvironmentCullable vp = pass.getViewpointCullable();
        envData.viewpoint = (ObjectRenderable)vp.getRenderable();

        EnvironmentCullable  bg = pass.getBackgroundCullable();
        if(bg != null)
            envData.background = (ObjectRenderable)bg.getRenderable();
        else
            envData.background = null;

        // 2D Scene cannot use stereo. Also ignores eye point offsets.
        envData.useStereo = false;

        envData.eyeOffset[0] = 0;
        envData.eyeOffset[1] = 0;
        envData.eyeOffset[2] = 0;

        // Calculate frustum parameters based on view environment
        envData.viewProjectionType = ViewEnvironment.ORTHOGRAPHIC_PROJECTION;

        ViewEnvironmentCullable view = scene.getViewCullable();

        int[] viewport = view.getViewportDimensions();
        envData.setViewport(viewport);

        int[] scissor = view.getScissorDimensions();

        // Only set scissor if width and height are non-zero, otherwise
        // use the viewport dimensions for the scissor call
        if(scissor[ViewEnvironmentCullable.VIEW_WIDTH] > 0 &&
           scissor[ViewEnvironmentCullable.VIEW_HEIGHT] > 0)
            envData.setScissor(scissor);
        else
            envData.setScissor(viewport);

        envData.viewFrustum[0] = viewport[-ViewEnvironment.VIEW_WIDTH / 2];
        envData.viewFrustum[1] = viewport[-ViewEnvironment.VIEW_HEIGHT / 2];
        envData.viewFrustum[2] = viewport[ViewEnvironment.VIEW_WIDTH / 2];
        envData.viewFrustum[3] = viewport[ViewEnvironment.VIEW_HEIGHT / 2];
        envData.viewFrustum[4] = 1;
        envData.viewFrustum[5] = -1;

        // walk back up the path to find the current root of
        // the scene graph and check that there is no SharedGroup
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

                    String msg = intl_mgr.getString(SHARED_VP_PROP);
                    throw new IllegalArgumentException(msg);
                }

                parent = ((GroupCullable)parent).getCullableParent();
            }
            else if(parent instanceof SingleCullable)
            {
                if(((SingleCullable)parent).hasMultipleParents())
                {
                    I18nManager intl_mgr = I18nManager.getManager();

                    String msg = intl_mgr.getString(SHARED_VP_PROP);
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
            viewMatrix2.mul(viewMatrix1);
        }

        currentViewpointPath.clear();

        if(useOrientation)
            viewMatrix2.mul(orientationMatrix);

        envData.viewTransform.set(viewMatrix2);

        matrixUtils.inverse(envData.viewTransform, viewMatrix2);
        envData.cameraTransform[0] = viewMatrix2.m00;
        envData.cameraTransform[1] = viewMatrix2.m10;
        envData.cameraTransform[2] = viewMatrix2.m20;
        envData.cameraTransform[3] = viewMatrix2.m30;
        envData.cameraTransform[4] = viewMatrix2.m01;
        envData.cameraTransform[5] = viewMatrix2.m11;
        envData.cameraTransform[6] = viewMatrix2.m21;
        envData.cameraTransform[7] = viewMatrix2.m31;
        envData.cameraTransform[8] = viewMatrix2.m02;
        envData.cameraTransform[9] = viewMatrix2.m12;
        envData.cameraTransform[10] = viewMatrix2.m22;
        envData.cameraTransform[11] = viewMatrix2.m32;
        envData.cameraTransform[12] = viewMatrix2.m03;
        envData.cameraTransform[13] = viewMatrix2.m13;
        envData.cameraTransform[14] = viewMatrix2.m23;
        envData.cameraTransform[15] = viewMatrix2.m33;

        // Now do almost the same thing with the background node. Only
        // difference is that this time we deliberately set the translation
        // components to zero.
        // walk back up the path to find the current root of
        // the scene graph and check that there is no SharedGroup
        // instances along the way.
        if(envData.background != null)
        {
            envData.backgroundTransform[0] = 1;
            envData.backgroundTransform[1] = 0;
            envData.backgroundTransform[2] = 0;
            envData.backgroundTransform[3] = 0;
            envData.backgroundTransform[4] = 0;
            envData.backgroundTransform[5] = 1;
            envData.backgroundTransform[6] = 0;
            envData.backgroundTransform[7] = 0;
            envData.backgroundTransform[8] = 0;
            envData.backgroundTransform[9] = 0;
            envData.backgroundTransform[10] = 1;
            envData.backgroundTransform[11] = 0;
            envData.backgroundTransform[12] = 0;
            envData.backgroundTransform[13] = 0;
            envData.backgroundTransform[14] = 0;
            envData.backgroundTransform[15] = 1;

            envData.backgroundFrustum[0] = envData.viewFrustum[0];
            envData.backgroundFrustum[1] = envData.viewFrustum[1];
            envData.backgroundFrustum[2] = envData.viewFrustum[2];
            envData.backgroundFrustum[3] = envData.viewFrustum[3];
            envData.backgroundFrustum[4] = 1;
            envData.backgroundFrustum[5] = -1;
        }
    }

    /**
     * Take a simple scene and fill in a GraphicsEnvironmentData instance.
     *
     * @param scene The scene to take data from
     * @param envData Data instance to copy it to
     */
    protected void fillMultipassEnvData(SceneCullable scene,
                                        GraphicsEnvironmentData envData)
    {
        envData.userData = scene.getUserData();
        envData.effectsProcessor = scene.getRenderEffectsProcessor();

        ViewEnvironmentCullable view = scene.getViewCullable();
        envData.viewProjectionType = view.getProjectionType();
        envData.useStereo = view.isStereoEnabled();

        RenderPassCullable pass = scene.getCullablePass(0);

        envData.viewpoint = null;

        EnvironmentCullable bg = pass.getBackgroundCullable();
        if(bg != null)
            envData.background = (ObjectRenderable)bg.getRenderable();
        else
            envData.background = null;

        envData.viewProjectionType = view.getProjectionType();
        envData.useStereo = view.isStereoEnabled();
        envData.fog = null;

        envData.eyeOffset[0] = eyePoint[0];
        envData.eyeOffset[1] = eyePoint[1];
        envData.eyeOffset[2] = eyePoint[2];

        // Calculate frustum parameters based on view environment
        int[] viewport = view.getViewportDimensions();
        envData.setViewport(viewport);

        int[] scissor = view.getScissorDimensions();

        // Only set scissor if width and height are non-zero, otherwise
        // use the viewport dimensions for the scissor call
        if(scissor[ViewEnvironmentCullable.VIEW_WIDTH] > 0 &&
           scissor[ViewEnvironmentCullable.VIEW_HEIGHT] > 0)
            envData.setScissor(scissor);
        else
            envData.setScissor(viewport);

        view.getViewFrustum(envData.viewFrustum);

        if(useEyePoint)
        {
            envData.viewFrustum[0] -= eyePoint[0];
            envData.viewFrustum[1] -= eyePoint[0];
            envData.viewFrustum[2] -= eyePoint[1];
            envData.viewFrustum[3] -= eyePoint[1];
            envData.viewFrustum[4] -= eyePoint[2];
            envData.viewFrustum[5] -= eyePoint[2];
        }
    }

    /**
     * Take a simple scene and fill in a GraphicsEnvironmentData instance.
     *
     * @param pass The render pass to take data from
     * @param envData Data instance to copy it to
     * @param bufferData The object to copy the extra buffer state to
     */
    protected void fillRenderPassEnvData(RenderPassCullable pass,
                                         GraphicsEnvironmentData envData,
                                         BufferDetails bufferData)
    {
        bufferData.viewportState = pass.getViewportRenderable();
        bufferData.generalBufferState = pass.getGeneralBufferRenderable();
        bufferData.colorBufferState = pass.getColorBufferRenderable();
        bufferData.depthBufferState = pass.getDepthBufferRenderable();
        bufferData.stencilBufferState = pass.getStencilBufferRenderable();
        bufferData.accumBufferState = pass.getAccumBufferRenderable();

        ViewEnvironmentCullable view = pass.getViewCullable();

        envData.userData = pass.getUserData();
        envData.effectsProcessor = null;

        EnvironmentCullable vp = pass.getViewpointCullable();
        envData.viewpoint = (ObjectRenderable)vp.getRenderable();
        envData.background = null;

        envData.viewProjectionType = view.getProjectionType();
        envData.useStereo = false;

        // Not sure how valid an assumption this is when dealing
        // with the individual rendering passes.
        envData.eyeOffset[0] = eyePoint[0];
        envData.eyeOffset[1] = eyePoint[1];
        envData.eyeOffset[2] = eyePoint[2];

        // Calculate frustum parameters based on view environment
        int[] viewport = view.getViewportDimensions();
        envData.setViewport(viewport);

        int[] scissor = view.getScissorDimensions();

        // Only set scissor if width and height are non-zero, otherwise
        // use the viewport dimensions for the scissor call
        if(scissor[ViewEnvironmentCullable.VIEW_WIDTH] > 0 &&
           scissor[ViewEnvironmentCullable.VIEW_HEIGHT] > 0)
            envData.setScissor(scissor);
        else
            envData.setScissor(viewport);

        view.getViewFrustum(envData.viewFrustum);

        if(useEyePoint)
        {
            envData.viewFrustum[0] -= eyePoint[0];
            envData.viewFrustum[1] -= eyePoint[0];
            envData.viewFrustum[2] -= eyePoint[1];
            envData.viewFrustum[3] -= eyePoint[1];
            envData.viewFrustum[4] -= eyePoint[2];
            envData.viewFrustum[5] -= eyePoint[2];
        }

        // walk back up the path to find the current root of
        // the scene graph and check that there is no SharedGroup
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

                    String msg = intl_mgr.getString(SHARED_VP_PROP);
                    throw new IllegalArgumentException(msg);
                }

                parent = ((GroupCullable)parent).getCullableParent();
            }
            else if(parent instanceof SingleCullable)
            {
                if(((SingleCullable)parent).hasMultipleParents())
                {
                    I18nManager intl_mgr = I18nManager.getManager();

                    String msg = intl_mgr.getString(SHARED_VP_PROP);
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
            viewMatrix2.mul(viewMatrix1);
        }

        currentViewpointPath.clear();

        if(useOrientation)
            viewMatrix2.mul(orientationMatrix);

        envData.viewTransform.set(viewMatrix2);

        matrixUtils.inverse(envData.viewTransform, viewMatrix2);
        envData.cameraTransform[0] = viewMatrix2.m00;
        envData.cameraTransform[1] = viewMatrix2.m10;
        envData.cameraTransform[2] = viewMatrix2.m20;
        envData.cameraTransform[3] = viewMatrix2.m30;
        envData.cameraTransform[4] = viewMatrix2.m01;
        envData.cameraTransform[5] = viewMatrix2.m11;
        envData.cameraTransform[6] = viewMatrix2.m21;
        envData.cameraTransform[7] = viewMatrix2.m31;
        envData.cameraTransform[8] = viewMatrix2.m02;
        envData.cameraTransform[9] = viewMatrix2.m12;
        envData.cameraTransform[10] = viewMatrix2.m22;
        envData.cameraTransform[11] = viewMatrix2.m32;
        envData.cameraTransform[12] = viewMatrix2.m03;
        envData.cameraTransform[13] = viewMatrix2.m13;
        envData.cameraTransform[14] = viewMatrix2.m23;
        envData.cameraTransform[15] = viewMatrix2.m33;
    }

    /**
     * From the current view setup of FoV, near and far clipping distances and
     * the aspectRatio ratio, generate the 6 parameters that describe a view
     * frustum. These parameters are what could be used as arguments to the
     * glFrustum call. The parameter order is:
     * [x min, x max, y min, y max, z near, z far]
     *
     * @param nearClip The distance to the near clip plane
     * @param farClip The distance to the far clip plane
     * @param fov The field of view to use
     * @param data The environment data instance to fill in
     */
    private void setupBackgroundFrustum(double nearClip,
                                        double farClip,
                                        double fov,
                                        GraphicsEnvironmentData data)
    {
        double xmin = 0;
        double xmax = 0;
        double ymin = 0;
        double ymax = 0;

        double aspect_ratio = (double) data.viewport[GraphicsEnvironmentData.VIEW_WIDTH] /
                              data.viewport[GraphicsEnvironmentData.VIEW_HEIGHT];

        ymax = nearClip * Math.tan(fov * Math.PI / 360.0);
        ymin = -ymax;

        xmin = ymin * aspect_ratio;
        xmax = ymax * aspect_ratio;

        data.backgroundFrustum[0] = xmin;
        data.backgroundFrustum[1] = xmax;
        data.backgroundFrustum[2] = ymin;
        data.backgroundFrustum[3] = ymax;
        data.backgroundFrustum[4] = nearClip;
        data.backgroundFrustum[5] = farClip;
    }

    /**
     * Check a shape node for the offscreen textures that may be present. If
     * some are found, queue them up for processing.
     *
     * @param shape The object instance to process
     */
    protected void checkForOffscreens(ShapeRenderable shape)
    {
        AppearanceRenderable app = shape.getAppearanceRenderable();

        if(app == null)
            return;

        int num_units = app.numTextureRenderables();
        if(num_units == 0)
            return;

        for(int i =0; i < num_units; i++)
        {
            TextureRenderable tex = app.getTextureRenderable(i);

            // Only queue up the texture for repainting if it says it needs a
            // repaint and that we haven't already found this instance in this
            // frame.
            if(tex.isOffscreenSource())
            {
                OffscreenCullable offscreen = tex.getOffscreenSource();

                if(!visitedNodes.contains(offscreen) && offscreen.isRepaintRequired())
                {
                    resizeOffscreenList();
                    pendingSubscenes[lastSubscene] = offscreen;
                    pendingParents[lastSubscene] = activeParent;
                    visitedNodes.add(offscreen);
                    lastSubscene++;
                }
            }
        }
    }

    /**
     * Clean up the unused resources after the end of the cull process. This
     * releases any references that are no longer needed, and may have been
     * kept from the previous culling pass. This method is called just before
     * the output is sent on to the sorting stage.
     * <p>
     *
     * This base implementation cleans up the workLayers list. Derived
     * classes that override this method for their own needs should also
     * make sure to call this method too.
     */
    protected void cleanupOldRefs()
    {
        for(int i = 0; i < workLayers.length; i++)
        {
            for(int j = 0; j < workLayers[i].length; j++)
            {
                if(workLayers[i][j] == null)
                    break;

                for(int k = 0; k < workLayers[i][j].numViewports; k++)
                {
                    ViewportLayerCollection c = workLayers[i][j].viewports[k];

                    for(int l = 0; l < c.numScenes; l++)
                    {
                        SceneRenderBucket b = c.scenes[l];

                        for(int m = b.numNodes; m <  b.nodes.length; m++)
                        {
                            if(b.nodes[m] == null)
                                break;

                            b.nodes[m].renderable = null;
                            b.nodes[m].localFog   = null;
                            b.nodes[m].customData = null;

                            // Another option we could use here is to clear
                            // the renference to the details object too. May
                            // want to have a system property for that.
                            for(int n = 0; n < b.nodes[m].numLights; n++)
                                b.nodes[m].lights[n].clear();


                            for(int n = 0; n < b.nodes[m].numClipPlanes; n++)
                                b.nodes[m].clipPlanes[n].clear();

                            b.nodes[m].numLights = 0;
                            b.nodes[m].numClipPlanes = 0;
                        }
                    }
                }
            }
        }
    }

    /**
     * Resize the list if needed. Marked as final in order to encourage the
     * compiler to inline the code for faster execution
     */
    protected final void resizeCullList(int cur_size)
    {
        if((cur_size + 1) == workCullList.length)
        {
            int old_size = workCullList.length;
            int new_size = old_size + LIST_INCREMENT;

            GraphicsCullOutputDetails[] tmp_nodes =
                new GraphicsCullOutputDetails[new_size];

            System.arraycopy(workCullList, 0, tmp_nodes, 0, old_size);

            for(int i = old_size; i < new_size; i++)
                tmp_nodes[i] = new GraphicsCullOutputDetails();

            workCullList = tmp_nodes;
        }
    }

    /**
     * Resize the list if needed. Marked as final in order to encourage the
     * compiler to inline the code for faster execution
     */
    protected final void resizeOutputLists()
    {
        if((lastSubscene + 1) < validSceneParents.length)
            return;

        int old_size = validSceneParents.length;
        int new_size = old_size + LIST_INCREMENT;

        OffscreenBufferRenderable[][] tmp_obj =
            new OffscreenBufferRenderable[new_size][];

        System.arraycopy(validSceneParents, 0, tmp_obj, 0, old_size);

        for(int i = old_size; i < new_size; i++)
            tmp_obj[i] = new OffscreenBufferRenderable[2];

        validSceneParents = tmp_obj;

        ViewportCollection[][] tmp_vc = new ViewportCollection[new_size][];
        int[] tmp_lc = new int[new_size];

        System.arraycopy(workLayers, 0, tmp_vc, 0, workLayers.length);
        System.arraycopy(workLayerCounts, 0, tmp_lc, 0, workLayerCounts.length);

        for(int i = workLayers.length; i < new_size; i++)
        {
            tmp_vc[i] = new ViewportCollection[1];
            tmp_vc[i][0] = new ViewportCollection();
        }

        workLayers = tmp_vc;
        workLayerCounts = tmp_lc;
    }

    /**
     * Resize the list if needed. Marked as final in order to encourage the
     * compiler to inline the code for faster execution
     */
    protected final void resizeOffscreenList()
    {
        if((lastSubscene + 1) == pendingSubscenes.length)
        {
            int old_size = pendingSubscenes.length;
            int new_size = old_size + LIST_INCREMENT;

            OffscreenCullable[] tmp_nodes = new OffscreenCullable[new_size];
            System.arraycopy(pendingSubscenes, 0, tmp_nodes, 0, old_size);
            pendingSubscenes = tmp_nodes;

            tmp_nodes = new OffscreenCullable[new_size];
            System.arraycopy(pendingParents, 0, tmp_nodes, 0, old_size);
            pendingParents = tmp_nodes;
        }
    }

    /**
     * Resize the list if needed. Marked as final in order to encourage the
     * compiler to inline the code for faster execution
     */
    protected final void resizeLightList()
    {
        if((lastLight + 1) == lightList.length)
        {
            int old_size = lightList.length;
            int new_size = old_size + LIST_INCREMENT;

            EffectRenderable[] tmp_nodes = new EffectRenderable[new_size];

            System.arraycopy(lightList, 0, tmp_nodes, 0, old_size);

            lightList = tmp_nodes;

            float[][] tmp_tx  = new float[new_size][];
            System.arraycopy(lightTxList, 0, tmp_tx, 0, old_size);

            for(int i = old_size; i < new_size; i++)
                tmp_tx[i] = new float[16];

            lightTxList = tmp_tx;
        }
    }

    /**
     * Resize the list if needed. Marked as final in order to encourage the
     * compiler to inline the code for faster execution
     */
    protected final void resizeClipList()
    {
        if((lastClip + 1) == clipList.length)
        {
            int old_size = clipList.length;
            int new_size = old_size + LIST_INCREMENT;

            EffectRenderable[] tmp_nodes = new EffectRenderable[new_size];

            System.arraycopy(clipList, 0, tmp_nodes, 0, old_size);

            clipList = tmp_nodes;

            float[][] tmp_tx  = new float[new_size][];
            System.arraycopy(clipTxList, 0, tmp_tx, 0, old_size);

            for(int i = old_size; i < new_size; i++)
                tmp_tx[i] = new float[16];

            clipTxList = tmp_tx;
        }
    }

    /**
     * Resize the transform stack if needed. Marked as final in order to
     * encourage the compiler to inline the code for faster execution
     */
    protected final void resizeStack()
    {
        if((lastTxStack + 1) == transformStack.length)
        {
            int old_size = transformStack.length;
            int new_size = old_size + STACK_INCREMENT;

            Matrix4f[] tmp_matrix = new Matrix4f[new_size];

            System.arraycopy(transformStack, 0, tmp_matrix, 0, old_size);

            for(int i = old_size; i < new_size; i++)
                tmp_matrix[i] = new Matrix4f();

            transformStack = tmp_matrix;
        }
    }

    /**
     * Resize the fog stack if needed. Marked as final in order to
     * encourage the compiler to inline the code for faster execution
     */
    protected final void resizeFogStack()
    {
        if((lastFogStack + 1) == fogStack.length)
        {
            int old_size = fogStack.length;
            int new_size = old_size + STACK_INCREMENT;

            EffectRenderable[] tmp_fog = new EffectRenderable[new_size];

            System.arraycopy(fogStack, 0, tmp_fog, 0, old_size);

            fogStack = tmp_fog;
        }
    }

    /**
     * Resize the appearance stack if needed. Marked as final in order to
     * encourage the compiler to inline the code for faster execution
     */
    protected final void resizeAppearanceStack()
    {
        if((lastAppearanceStack + 1) == appearanceStack.length)
        {
            int old_size = appearanceStack.length;
            int new_size = old_size + STACK_INCREMENT;

            OverrideRenderable[] tmp_app = new OverrideRenderable[new_size];

            System.arraycopy(appearanceStack, 0, tmp_app, 0, old_size);

            appearanceStack = tmp_app;
        }
    }
}
