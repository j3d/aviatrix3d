package j3d.aviatrix3d.examples.shader;

// External imports
import java.util.ArrayList;

import org.j3d.maths.vector.Matrix4d;

import org.j3d.util.MatrixUtils;
import org.j3d.util.interpolator.PositionInterpolator;
import org.j3d.util.interpolator.RotationInterpolator;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.pipeline.graphics.GraphicsResizeListener;

/**
 * Simple test helper for getting the status of a shader loading
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
class DeferredShadingAnimator
    implements ApplicationUpdateObserver,
               NodeUpdateListener,
               GraphicsResizeListener
{
    /** Length of the animation cycle in milliseconds */
    private static final int CYCLE_TIME = 10000;

    /** Frame counter so we know when to print out shader debug */
    private int frameCount;

    /** list of shader holders to look at */
    private ArrayList<ShaderHolder> shaderList;

    /** List of viewpoint transforms that need to be updated each frame */
    private ArrayList<TransformGroup> viewTxList;

    /** Collection of textures to be resized to the full window size */
    private ArrayList<MRTOffscreenTexture2D> fullWindowResizeList;

    /** Collection of textures to be resized to the full window size */
    private ArrayList<Viewport> fullEnvResizeList;

    /** Collection of shader arguments to be updated on full window size */
    private ArrayList<ShaderArguments> fullShaderArgsResizeList;

    /** Collection of textures to be resized to the full window size */
    private ArrayList<MRTOffscreenTexture2D> halfWindowResizeList;

    /** Collection of textures to be resized to the full window size */
    private ArrayList<Viewport> halfEnvResizeList;

    /** Collection of shader arguments to be updated on full window size */
    private ArrayList<ShaderArguments> halfShaderArgsResizeList;

    /** Flag indicating we've just had a window resize */
    private boolean windowSizeChanged;

    /** The size of the window that we should be updating to (x, y, w, h) */
    private int[] windowDimensions;

    /** Time of the last frame for animation purposes */
    private long firstFrameTime;

    /** Where we are in the cycle */
    private Matrix4d vpMatrix;

    /** Position interpolation for moving the VP */
    private PositionInterpolator posInterp;

    /** Position interpolation for moving the VP */
    private RotationInterpolator rotInterp;

    /** Utility for doing matrix rotations */
    private MatrixUtils matrixUtils;

    /**
     *
     */
    public DeferredShadingAnimator()
    {
        firstFrameTime = System.currentTimeMillis();
        shaderList = new ArrayList<ShaderHolder>();
        viewTxList = new ArrayList<TransformGroup>();
        fullWindowResizeList = new ArrayList<MRTOffscreenTexture2D>();
        halfWindowResizeList = new ArrayList<MRTOffscreenTexture2D>();
        fullEnvResizeList = new ArrayList<Viewport>();
        halfEnvResizeList = new ArrayList<Viewport>();
        fullShaderArgsResizeList = new ArrayList<ShaderArguments>();
        halfShaderArgsResizeList = new ArrayList<ShaderArguments>();

        windowDimensions = new int[4];
        windowSizeChanged = false;

        vpMatrix = new Matrix4d();
        posInterp = new PositionInterpolator();
        rotInterp = new RotationInterpolator();

        matrixUtils = new MatrixUtils();

        // Setup the keyframes for position interpolation. First position
        // corresponds to the start point. Uses a [0,1] fractional scale
        posInterp.addKeyFrame(0, 0, 0, 20f);
        posInterp.addKeyFrame(0.25f, 3, 0, 17f);
        posInterp.addKeyFrame(0.50f, 0, 0, 20f);
        posInterp.addKeyFrame(0.75f, -3, 0, 17f);
        posInterp.addKeyFrame(1.00f, 0, 0, 20f);

        rotInterp.addKeyFrame(0,     0, 1, 0, 0.0f);
        rotInterp.addKeyFrame(0.25f, 0, 1, 0, 0.2f);
        rotInterp.addKeyFrame(0.50f, 0, 1, 0, 0.0f);
        rotInterp.addKeyFrame(0.75f, 0, 1, 0, -0.2f);
        rotInterp.addKeyFrame(1.00f, 0, 1, 0, 0.0f);
    }

    //---------------------------------------------------------------
    // Methods defined by ApplicationUpdateObserver
    //---------------------------------------------------------------

    /**
     * Notification that now is a good time to update the scene graph.
     */
    public void updateSceneGraph()
    {
        long current_time = System.currentTimeMillis();

        float cycle_fraction = (current_time - firstFrameTime) % CYCLE_TIME;
        cycle_fraction *= 1.0f / CYCLE_TIME;

        float[] new_pos = posInterp.floatValue(cycle_fraction);
        float[] new_angle = rotInterp.floatValue(cycle_fraction);

        matrixUtils.rotateY(new_angle[3], vpMatrix);
        vpMatrix.m03 = new_pos[0];
        vpMatrix.m13 = new_pos[1];
        vpMatrix.m23 = new_pos[2];

        for(int i = 0; i < viewTxList.size(); i++)
        {
            TransformGroup tg = viewTxList.get(i);

            if(tg.isLive())
                tg.boundsChanged(this);
        }

        if(windowSizeChanged)
        {
            for(int i = 0; i < fullWindowResizeList.size(); i++)
            {
                MRTOffscreenTexture2D tex = fullWindowResizeList.get(i);

                if(tex.isLive())
                    tex.dataChanged(this);
            }

            for(int i = 0; i < fullShaderArgsResizeList.size(); i++)
            {
                ShaderArguments args = fullShaderArgsResizeList.get(i);
                if(args.isLive())
                    args.dataChanged(this);
            }

            for(int i = 0; i < fullEnvResizeList.size(); i++)
            {
                Viewport env = fullEnvResizeList.get(i);

                if(env.isLive())
                    env.setDimensions(windowDimensions[0],
                                      windowDimensions[1],
                                      windowDimensions[2],
                                      windowDimensions[3]);
            }

            for(int i = 0; i < halfWindowResizeList.size(); i++)
            {
                MRTOffscreenTexture2D tex = halfWindowResizeList.get(i);
                if(tex.isLive())
                    tex.dataChanged(this);
            }

            for(int i = 0; i < halfShaderArgsResizeList.size(); i++)
            {
                ShaderArguments args = halfShaderArgsResizeList.get(i);
                if(args.isLive())
                    args.dataChanged(this);
            }

            for(int i = 0; i < halfEnvResizeList.size(); i++)
            {
                // Div 2 on int pixels will round down. Is OK because we're
                // already at half size and using texture scaling
                Viewport env = halfEnvResizeList.get(i);

                if(env.isLive())
                    env.setDimensions(windowDimensions[0] / 2,
                                      windowDimensions[1] / 2,
                                      windowDimensions[2] / 2,
                                      windowDimensions[3] / 2);
            }

            windowSizeChanged = false;
        }

        if(frameCount == 4)
        {
            for(int i =  0; i < shaderList.size(); i++)
            {
                ShaderHolder h = shaderList.get(i);

                System.out.println(h.title);
                System.out.println("vert log " + h.vertShader.getLastInfoLog());
                System.out.println("frag log " + h.fragShader.getLastInfoLog());
                System.out.println("link log " + h.completeShader.getLastInfoLog());
            }

            frameCount++;
        }
        else if(frameCount < 4)
            frameCount++;
    }

    /**
     * Notification that the AV3D internal shutdown handler has detected a
     * system-wide shutdown. The aviatrix code has already terminated rendering
     * at the point this method is called, only the user's system code needs to
     * terminate before exiting here.
     */
    public void appShutdown()
    {
        // do nothing
    }

    //----------------------------------------------------------
    // Methods defined by UpdateListener
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src)
    {
        if(src instanceof TransformGroup)
        {
            ((TransformGroup)src).setTransform(vpMatrix);
        }
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src)
    {
        if(src instanceof MRTOffscreenTexture2D)
        {
            MRTOffscreenTexture2D tex = (MRTOffscreenTexture2D)src;

            if(fullWindowResizeList.contains(src))
                tex.resize(windowDimensions[2],
                           windowDimensions[3]);
            else
                tex.resize(windowDimensions[2] / 2,
                           windowDimensions[3] / 2);
        }
        else if(src instanceof ShaderArguments)
        {
            ShaderArguments args = (ShaderArguments)src;
            float[] tex_size = new float[2];

            if(fullShaderArgsResizeList.contains(src))
            {
                tex_size[0] = windowDimensions[2];
                tex_size[1] = windowDimensions[3];
            }
            else
            {
                tex_size[0] = windowDimensions[2] * 0.5f;
                tex_size[1] = windowDimensions[3] * 0.5f;
            }


            args.setUniform("texSize", 2, tex_size, 1);
        }
    }

    //----------------------------------------------------------
    // Methods defined by GraphicsResizeListener
    //----------------------------------------------------------

    /**
     * Notification that the surface resized.
     */
    public void graphicsDeviceResized(int x, int y, int width, int height)
    {
        windowDimensions[0] = x;
        windowDimensions[1] = y;
        windowDimensions[2] = width;
        windowDimensions[3] = height;

        windowSizeChanged = true;
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Add a viewpoint transform that needs to be updated each frame.
     */
    void addViewTransform(TransformGroup tx)
    {
        viewTxList.add(tx);
    }

    /**
     * Add a new shader for watching, to the list
     */
    void addShader(String title,
                   ShaderObject vert,
                   ShaderObject frag,
                   ShaderProgram comp)
    {
        ShaderHolder h = new ShaderHolder();

        h.title = title;
        h.vertShader = vert;
        h.fragShader = frag;
        h.completeShader = comp;

        shaderList.add(h);
    }

    /**
     * Add a full window viewport only to resize.
     */
    void addFullWindowResize(Viewport env)
    {
        fullEnvResizeList.add(env);
    }

    /**
     * Add a full window shader args to resize.
     */
    void addFullWindowResize(ShaderArguments args)
    {
        fullShaderArgsResizeList.add(args);
    }

    /**
     * Add a half window shader args to resize.
     */
    void addHalfWindowResize(ShaderArguments args)
    {
        halfShaderArgsResizeList.add(args);
    }

    /**
     * Add a full window offscreen texture to resize.
     */
    void addFullWindowResize(MRTOffscreenTexture2D tex, Viewport env)
    {
        fullWindowResizeList.add(tex);
        fullEnvResizeList.add(env);
    }

    /**
     * Add a half window size offscreen texture to resize.
     *
     */
    void addHalfWindowResize(MRTOffscreenTexture2D tex, Viewport env)
    {
        halfWindowResizeList.add(tex);
        halfEnvResizeList.add(env);
    }
}
