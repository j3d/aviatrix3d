
// External imports
import java.awt.*;
import java.awt.event.*;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import javax.media.opengl.GLCapabilities;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.output.graphics.SimpleAWTSurface;
import org.j3d.aviatrix3d.output.graphics.DebugAWTSurface;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.DefaultGraphicsPipeline;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;
import org.j3d.aviatrix3d.pipeline.graphics.NullCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.NullSortStage;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsSortStage;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;

/**
 * Example application that demonstrates a using the accumulation buffer with
 * multipass rendering to achieve a motion blur effect.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class MotionBlurDemo extends Frame
    implements WindowListener
{
    /** The number of passes for the motion blur */
    private static final int NUM_PASSES = 4;

    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    public MotionBlurDemo()
    {
        super("Motion Blur Multipass Demo");

        setLayout(new BorderLayout());
        addWindowListener(this);

        setupAviatrix();
        setupSceneGraph();

        setSize(600, 600);
        setLocation(40, 40);

        // Need to set visible first before starting the rendering thread due
        // to a bug in JOGL. See JOGL Issue #54 for more information on this.
        // http://jogl.dev.java.net
        setVisible(true);
    }

    /**
     * Setup the avaiatrix pipeline here
     */
    private void setupAviatrix()
    {
        // Assemble a simple single-threaded pipeline.
        GLCapabilities caps = new GLCapabilities();
        caps.setAccumAlphaBits(16);
        caps.setAccumBlueBits(16);
        caps.setAccumGreenBits(16);
        caps.setAccumRedBits(16);
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);

        GraphicsCullStage culler = new NullCullStage();
        culler.setOffscreenCheckEnabled(false);

        GraphicsSortStage sorter = new NullSortStage();
        surface = new DebugAWTSurface(caps);
        DefaultGraphicsPipeline pipeline = new DefaultGraphicsPipeline();

        pipeline.setCuller(culler);
        pipeline.setSorter(sorter);
        pipeline.setGraphicsOutputDevice(surface);

        displayManager = new SingleDisplayCollection();
        displayManager.addPipeline(pipeline);

        // Render manager
        sceneManager = new SingleThreadRenderManager();
        sceneManager.addDisplay(displayManager);
        sceneManager.setMinimumFrameInterval(30);

        // Before putting the pipeline into run mode, put the canvas on
        // screen first.
        Component comp = (Component)surface.getSurfaceObject();
        add(comp, BorderLayout.CENTER);
    }

    /**
     * Setup the basic scene which consists of a quad and a viewpoint
     */
    private void setupSceneGraph()
    {
        // Transform group for each pass
        TransformGroup[] shape_movers = new TransformGroup[NUM_PASSES];


        // Triangle fan array
        float[] coord = { 0, 0, -1,
                          0.25f, 0, -1,
                          0, 0.25f, -1,
                          -0.25f, 0, -1,
                          0, -0.25f, -1,
                          0.25f, 0, -1,
                          };

        float[] normal = { 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1 };
        float[] color = { 1, 1, 1, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0 };

        TriangleFanArray geom = new TriangleFanArray();

        geom.setVertices(TriangleArray.COORDINATE_3, coord);
        geom.setFanCount(new int[] { 6 }, 1);
        geom.setNormals(normal);
        geom.setColors(false, color);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);

        SharedNode shared_geom = new SharedNode();
        shared_geom.setChild(shape);

        MultipassScene scene = new MultipassScene();

        for(int i = 0; i < NUM_PASSES; i++)
        {
            // View group
            Viewpoint vp = new Viewpoint();
            Vector3f trans = new Vector3f(0, 0, 1);

            Matrix4f mat = new Matrix4f();
            mat.setIdentity();
            mat.setTranslation(trans);

            TransformGroup tx = new TransformGroup();
            tx.addChild(vp);
            tx.setTransform(mat);

            Group scene_root = new Group();
            scene_root.addChild(tx);

            TransformGroup shape_transform = new TransformGroup();
            shape_transform.addChild(shared_geom);

            scene_root.addChild(shape_transform);
            shape_movers[i] = shape_transform;

            AccumulationBufferState accum_state = new AccumulationBufferState();

            if(i == 0)
            {
                accum_state.setAccumFunction(AccumulationBufferState.FUNCTION_LOAD);
                accum_state.setValue(0.8f);
            }
            else
            {
                accum_state.setAccumFunction(AccumulationBufferState.FUNCTION_ACCUMULATE);
                accum_state.setValue(1f / NUM_PASSES);
                accum_state.setClearBufferState(false);
            }

            DepthBufferState depth_state = new DepthBufferState();
            ColorBufferState color_state = new ColorBufferState();

            RenderPass pass = new RenderPass();
            pass.setRenderedGeometry(scene_root);
            pass.setActiveView(vp);
            pass.setDepthBufferState(depth_state);
            pass.setColorBufferState(color_state);
            pass.setAccumulationBufferState(accum_state);

            scene.addRenderPass(pass);
        }

        // Then the basic layer and viewport at the top:
        MultipassViewport view = new MultipassViewport();
        view.setDimensions(0, 0, 500, 500);
        view.setScene(scene);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(view);

        Layer[] layers = { layer };
        displayManager.setLayers(layers, 1);

        MotionBlurAnimation anim = new MotionBlurAnimation(shape_movers);
        sceneManager.setApplicationObserver(anim);
    }

    //---------------------------------------------------------------
    // Methods defined by WindowListener
    //---------------------------------------------------------------

    /**
     * Ignored
     */
    public void windowActivated(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    public void windowClosed(WindowEvent evt)
    {
    }

    /**
     * Exit the application
     *
     * @param evt The event that caused this method to be called.
     */
    public void windowClosing(WindowEvent evt)
    {
        sceneManager.shutdown();
        System.exit(0);
    }

    /**
     * Ignored
     */
    public void windowDeactivated(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    public void windowDeiconified(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    public void windowIconified(WindowEvent evt)
    {
    }

    /**
     * When the window is opened, start everything up.
     */
    public void windowOpened(WindowEvent evt)
    {
        sceneManager.setEnabled(true);
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    public static void main(String[] args)
    {
        MotionBlurDemo demo = new MotionBlurDemo();
        demo.setVisible(true);
    }
}
