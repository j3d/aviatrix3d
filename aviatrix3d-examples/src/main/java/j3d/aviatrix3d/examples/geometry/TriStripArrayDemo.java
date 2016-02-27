package j3d.aviatrix3d.examples.geometry;

// External imports
import java.awt.*;
import java.awt.event.*;

import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;
import org.j3d.renderer.aviatrix3d.pipeline.ViewportResizeManager;

// Local imports
import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.output.graphics.DebugAWTSurface;
import org.j3d.aviatrix3d.pipeline.graphics.*;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;

/**
 * Example application that demonstrates the use of TriStrip Arrays
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class TriStripArrayDemo extends Frame
    implements WindowListener, ApplicationUpdateObserver
{
    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    private ViewportResizeManager resizeManager;

    public TriStripArrayDemo()
    {
        super("Aviatrix TriStripArray Demo");

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
        resizeManager = new ViewportResizeManager();

        // Assemble a simple single-threaded pipeline.
        GraphicsRenderingCapabilities caps = new GraphicsRenderingCapabilities();

        GraphicsCullStage culler = new NullCullStage();
        culler.setOffscreenCheckEnabled(false);

        GraphicsSortStage sorter = new NullSortStage();
        surface = new DebugAWTSurface(caps);
        surface.addGraphicsResizeListener(resizeManager);

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

        Vector3d trans = new Vector3d();
        trans.set(0, 0, 1);

        Matrix4d mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);

        // Flat panel that has the viewable object as the demo
//        int[] index = {0, 1, 2};
//        float[] coord = { 0, 0, -1,  0.25f, 0, -1, 0, 0.25f, -1 };
//        float[] normal = { 0, 0, 1, 0, 0, 1, 0, 0, 1 };
//        int[] fanCount = {3};

        int[] index = {7, 4, 3, 1, 2};
        float[] coord = {0.5f, 0.5f, -0.5f, 0.5f, -0.5f, 0, 0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0, -0.5f, 0.5f, 0, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f, 0};
        float[] normal = {0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1};
        int[] fanCount = {5};

        IndexedTriangleFanArray geom = new IndexedTriangleFanArray();
        geom.setVertices(IndexedTriangleFanArray.COORDINATE_3, coord);
        geom.setIndices(index, 5);
        geom.setIndices(index, 3);
        geom.setFanCount(fanCount, 1);
        geom.setNormals(normal);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);

        trans.set(0.2f, 0, 0);
        Matrix4d mat2 = new Matrix4d();
        mat2.setIdentity();
        mat2.set(0.25);
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
        view.setDimensions(0, 0, 500, 500);
        view.setScene(scene);

        resizeManager.addManagedViewport(view);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(view);

        Layer[] layers = { layer };
        displayManager.setLayers(layers, 1);
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
        TriStripArrayDemo demo = new TriStripArrayDemo();
        demo.setVisible(true);
    }
}
