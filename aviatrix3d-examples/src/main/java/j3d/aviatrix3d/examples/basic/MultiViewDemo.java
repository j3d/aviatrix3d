package j3d.aviatrix3d.examples.basic;

// Standard imports
import java.awt.*;
import java.awt.event.*;

// Application Specific imports
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.output.graphics.SimpleAWTSurface;
import org.j3d.aviatrix3d.pipeline.graphics.*;
import org.j3d.aviatrix3d.management.*;
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;
import org.j3d.renderer.aviatrix3d.pipeline.ViewportResizeManager;

/**
 * Example application that demonstrates having two surfaces sharing
 * GL contexts.
 *
 * Unfortunately the JOGL RI is severly bugged and it won't run currently
 * on windows. It crashes with a wglShareLists exception.
 *
 * To test on Windows change sceneManager to a SingleThreadRenderManager
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class MultiViewDemo extends Frame
    implements WindowListener, ApplicationUpdateObserver
{
    /** Manager for the scene graph handling */
    private RenderManager sceneManager;

    /** Manager for the layers on surface 1 */
    private SingleDisplayCollection displayManager1;

    /** Manager for the layers on surface 2 */
    private SingleDisplayCollection displayManager2;

    private ViewportResizeManager resizeManager;

    public MultiViewDemo()
    {
        super("MultiView Aviatrix Demo");

        setLayout(new GridLayout(1, 2));
        addWindowListener(this);

        setupAviatrix();
        setupSceneGraph();

        setSize(800, 400);
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
        resizeManager = new ViewportResizeManager();

        // Assemble a simple single-threaded pipeline.
        GraphicsRenderingCapabilities caps = new GraphicsRenderingCapabilities();

        SimpleAWTSurface main_surface = new SimpleAWTSurface(caps);
        main_surface.addGraphicsResizeListener(resizeManager);

        DefaultGraphicsPipeline pipeline1 = new DefaultGraphicsPipeline();

        GraphicsCullStage culler = new NullCullStage();
        culler.setOffscreenCheckEnabled(false);

        GraphicsSortStage sorter = new NullSortStage();

        pipeline1.setCuller(culler);
        pipeline1.setSorter(sorter);
        pipeline1.setGraphicsOutputDevice(main_surface);


        SimpleAWTSurface slave_surface = new SimpleAWTSurface(caps, main_surface);
        slave_surface.addGraphicsResizeListener(resizeManager);

        DefaultGraphicsPipeline pipeline2 = new DefaultGraphicsPipeline();

        culler = new NullCullStage();
        culler.setOffscreenCheckEnabled(false);
        sorter = new NullSortStage();

        pipeline2.setCuller(culler);
        pipeline2.setSorter(sorter);
        pipeline2.setGraphicsOutputDevice(slave_surface);

        displayManager1 = new SingleDisplayCollection();
        displayManager1.addPipeline(pipeline1);

        displayManager2 = new SingleDisplayCollection();
        displayManager2.addPipeline(pipeline2);

        // Render manager
        //sceneManager = new MultiThreadRenderManager();
        sceneManager = new SingleThreadRenderManager();

        sceneManager.addDisplay(displayManager1);
        sceneManager.addDisplay(displayManager2);
        sceneManager.setMinimumFrameInterval(100);
        sceneManager.setApplicationObserver(this);

        // Before putting the pipeline into run mode, put the canvas on
        // screen first.
        Component main_comp = (Component)main_surface.getSurfaceObject();
        Component slave_comp = (Component)slave_surface.getSurfaceObject();

        add(main_comp);
        add(slave_comp);
    }

    /**
     * Setup the basic scene which consists of a quad and a viewpoint
     */
    private void setupSceneGraph()
    {
        // View group

        Viewpoint vp = new Viewpoint();

        Vector3d trans = new Vector3d();
        trans.set(0, 0, 1);

        Matrix4d mat = new Matrix4d();
        mat.set(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);

        // Flat panel that has the viewable object as the demo
        float[] coord = { 0, 0, -1, 0.25f, 0, -1, 0, 0.25f, -1 };
        float[] normal = { 0, 0, 1, 0, 0, 1, 0, 0, 1 };
        float[] color = { 0, 0, 1, 0, 1, 0, 1, 0, 0 };

        TriangleArray geom = new TriangleArray();
        geom.setValidVertexCount(3);
        geom.setVertices(TriangleArray.COORDINATE_3, coord);
        geom.setNormals(normal);
        geom.setColors(false, color);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);

        trans.set(0.2f, 0.5f, 0);
        Matrix4d mat2 = new Matrix4d();
        mat2.setIdentity();
        mat2.setTranslation(trans);

        TransformGroup shape_transform = new TransformGroup();
        shape_transform.addChild(shape);
        shape_transform.setTransform(mat2);

        scene_root.addChild(shape_transform);

        SimpleScene scene = new SimpleScene();
        scene.setRenderedGeometry(scene_root);
        scene.setActiveView(vp);

        // Then the basic layer and viewport at the top:
        SimpleViewport view = new SimpleViewport();
        view.setDimensions(0, 0, 400, 400);
        view.setScene(scene);

        resizeManager.addManagedViewport(view);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(view);

        Layer[] layers = { layer };
        displayManager1.setLayers(layers, 1);
        displayManager2.setLayers(layers, 1);
    }

    //---------------------------------------------------------------
    // Methods defined by WindowListener
    //---------------------------------------------------------------

    @Override
    public void windowActivated(WindowEvent evt)
    {
    }

    @Override
    public void windowClosed(WindowEvent evt)
    {
    }

    @Override
    public void windowClosing(WindowEvent evt)
    {
        sceneManager.shutdown();
        System.exit(0);
    }

    @Override
    public void windowDeactivated(WindowEvent evt)
    {
    }

    @Override
    public void windowDeiconified(WindowEvent evt)
    {
    }

    @Override
    public void windowIconified(WindowEvent evt)
    {
    }

    @Override
    public void windowOpened(WindowEvent evt)
    {
        sceneManager.setEnabled(true);
    }

    //---------------------------------------------------------------
    // Methods defined by ApplicationUpdateObserver
    //---------------------------------------------------------------

    @Override
    public void updateSceneGraph()
    {
        resizeManager.sendResizeUpdates();
    }

    @Override
    public void appShutdown()
    {
        // do nothing
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    public static void main(String[] args)
    {
        MultiViewDemo demo = new MultiViewDemo();
        demo.setVisible(true);
    }
}
