package j3d.aviatrix3d.examples.basic;

// External imports
import java.awt.*;
import java.awt.event.*;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.output.graphics.DebugAWTSurface;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.DefaultGraphicsPipeline;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;
import org.j3d.aviatrix3d.pipeline.graphics.NullCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.NullSortStage;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsSortStage;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

/**
 * Example application that demonstrates the use of edge flags for the 4
 * types of geometry that accepts them.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class EdgeFlagsDemo extends Frame
    implements WindowListener
{
    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    public EdgeFlagsDemo()
    {
        super("Aviatrix Edge Flags Demo");

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
        GraphicsRenderingCapabilities caps = new GraphicsRenderingCapabilities();

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
        float[] coord = { 0, 0, -1,
                          0.25f, 0, -1,
                          0.25f, 0.25f, -1,
                          0, 0.25f, -1 };
        float[] normal = { 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1 };
        boolean[] edge_flags = { true, false, true, true };
        int[] tri_index = { 0, 1, 2 };
        int[] quad_index = { 0, 1, 2, 3 };

        PolygonAttributes attribs = new PolygonAttributes();
        attribs.setDrawMode(true, PolygonAttributes.DRAW_LINE);
        Appearance app = new Appearance();
        app.setPolygonAttributes(attribs);

        TriangleArray tri = new TriangleArray();
        tri.setValidVertexCount(3);
        tri.setVertices(TriangleArray.COORDINATE_3, coord);
        tri.setNormals(normal);
        tri.setEdgeFlags(edge_flags);

        Shape3D tri_shape = new Shape3D();
        tri_shape.setGeometry(tri);
        tri_shape.setAppearance(app);

        trans.set(-0.5f, 0.5f, 0);
        Matrix4d mat2 = new Matrix4d();
        mat2.setIdentity();
        mat2.setTranslation(trans);

        TransformGroup tri_transform = new TransformGroup();
        tri_transform.addChild(tri_shape);
        tri_transform.setTransform(mat2);

        scene_root.addChild(tri_transform);

        QuadArray quad = new QuadArray();
        quad.setValidVertexCount(4);
        quad.setVertices(QuadArray.COORDINATE_3, coord);
        quad.setNormals(normal);
        quad.setEdgeFlags(edge_flags);

        Shape3D quad_shape = new Shape3D();
        quad_shape.setGeometry(quad);
        quad_shape.setAppearance(app);

        trans.set(0.5f, 0.5f, 0);
        mat2.setIdentity();
        mat2.setTranslation(trans);

        TransformGroup quad_transform = new TransformGroup();
        quad_transform.addChild(quad_shape);
        quad_transform.setTransform(mat2);

        scene_root.addChild(quad_transform);

        IndexedTriangleArray itri = new IndexedTriangleArray();
        itri.setValidVertexCount(3);
        itri.setIndices(tri_index, 3);
        itri.setVertices(TriangleArray.COORDINATE_3, coord);
        itri.setNormals(normal);
        itri.setEdgeFlags(edge_flags);

        Shape3D itri_shape = new Shape3D();
        itri_shape.setGeometry(itri);
        itri_shape.setAppearance(app);

        trans.set(-0.5f, -0.5f, 0);
        mat2.setIdentity();
        mat2.setTranslation(trans);

        TransformGroup itri_transform = new TransformGroup();
        itri_transform.addChild(itri_shape);
        itri_transform.setTransform(mat2);

        scene_root.addChild(itri_transform);

        IndexedQuadArray iquad = new IndexedQuadArray();
        iquad.setValidVertexCount(4);
        iquad.setVertices(QuadArray.COORDINATE_3, coord);
        iquad.setIndices(quad_index, 4);
        iquad.setNormals(normal);
        iquad.setEdgeFlags(edge_flags);

        Shape3D iquad_shape = new Shape3D();
        iquad_shape.setGeometry(iquad);
        iquad_shape.setAppearance(app);

        trans.set(0.5f, -0.5f, 0);
        mat2.setIdentity();
        mat2.setTranslation(trans);

        TransformGroup iquad_transform = new TransformGroup();
        iquad_transform.addChild(iquad_shape);
        iquad_transform.setTransform(mat2);

        scene_root.addChild(iquad_transform);

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
        EdgeFlagsDemo demo = new EdgeFlagsDemo();
        demo.setVisible(true);
    }
}
