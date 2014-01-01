
// Standard imports
import java.awt.*;
import java.awt.event.*;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import javax.media.opengl.GLCapabilities;

// Application Specific imports
import org.j3d.aviatrix3d.*;
import org.j3d.geom.particle.*;

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
import org.j3d.renderer.aviatrix3d.geom.particle.PointParticleSystem;

/**
 * Example application that demonstrates how to use a simple particle system.
 * Uses a simple line emitter with point particles to show the basic effect.
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
public class ParticleSystemDemo extends Frame
    implements WindowListener, ApplicationUpdateObserver
{
    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    /** The particle system manager that we want to clock */
    private ParticleSystemManager particleSystem;

    /**
     * Create a new demo instance.
     */
    public ParticleSystemDemo()
    {
        super("Aviatrix Particle System Demo");

        setLayout(new BorderLayout());
        addWindowListener(this);

        setupAviatrix();
        setupSceneGraph();

        setSize(400, 400);
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
        sceneManager.setMinimumFrameInterval(100);

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
        // View group

        Viewpoint vp = new Viewpoint();

        Vector3f trans = new Vector3f(0, -0.25f, 3);

        Matrix4f mat = new Matrix4f();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);

        // this will do basic color interpolations over time
        float[] time = { 0, 1.5f, 2.5f, 5 };
        float[] colors = {
           1, 1, 1, 1,
           1, 0, 0, 1,
           1, 1, 0, 1,
           1, 1, 0, 0
        };

        int particleCount = 10000;
        float[] direction = {0, -1, 0};

        // create the ParticleInitializer for the ParticleSystem
        // the initializer is used to control how long particles within
        // the system live and how they are reinitialized when they die.
        // this simple initializer lets particles live for 200 iterations
        // and moves them to point 0,0,0 when they die
        float[] line = { -1, 0, 0,
                         -0.5f, -0.5f, 0,
                         0, 0, 0,
                         0.5f, -0.5f, 0,
                         1, 0, 0  };

        ParticleInitializer emitter =
            new PolylineEmitter(6000,
                                particleCount,
                                line,
                                line.length / 3,
                                colors,
                                direction,
                                0.1f,
                                0.25f);

        emitter.setMass(0.0001f);
        emitter.setSurfaceArea(0.00004f);

//        TriangleFanParticleSystem smoke_system =
//           new TriangleFanParticleSystem("smoke", particleCount, 4);
//        QuadParticleSystem smoke_system =
//           new QuadParticleSystem("smoke", particleCount, 4);
        PointParticleSystem smoke_system =
           new PointParticleSystem("smoke", particleCount, 4);
        smoke_system.setParticleInitializer(emitter);

        smoke_system.addParticleFunction(new MaxTimeParticleFunction());
        smoke_system.addParticleFunction(new PhysicsFunction());

        ColorRampFunction colorRamp = new ColorRampFunction(time, colors, true);
        smoke_system.addParticleFunction(colorRamp);

        particleSystem = new ParticleSystemManager();
        particleSystem.addParticleSystem(smoke_system);

        // Now create the shape to put the particle system in
        Shape3D p_shape = new Shape3D();
        p_shape.setGeometry(smoke_system.getNode());

        scene_root.addChild(p_shape);

        SimpleScene scene = new SimpleScene();
        scene.setRenderedGeometry(scene_root);
        scene.setActiveView(vp);

        // Then the basic layer and viewport at the top:
        SimpleViewport view = new SimpleViewport();
        view.setDimensions(0, 0, 500, 500);
        view.setScene(scene);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(view);

        Layer[] layers = { layer };
        displayManager.setLayers(layers, 1);
        sceneManager.setApplicationObserver(this);
    }

    //---------------------------------------------------------------
    // Methods defined by ApplicationUpdateObserver
    //---------------------------------------------------------------

    /**
     * Notification that now is a good time to update the scene graph. Use it
     * to clock the particle system.
     */
    public void updateSceneGraph()
    {
        particleSystem.update();
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
        ParticleSystemDemo demo = new ParticleSystemDemo();
    }
}
