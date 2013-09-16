
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
import org.j3d.aviatrix3d.pipeline.graphics.SimpleTransparencySortStage;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsSortStage;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;

/**
 * Example application that demonstrates a multiple level layers within a
 * single viewport. One object is placed in each layer and it
 * should show the two objects with the front layer always rendered in front
 * of the background layer. The front layer is fixed, but the back layer
 * animates in such a way that, if both objects were in the one scene, it
 * would move in front and then behind it.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class CombinedLayersDemo extends Frame
    implements WindowListener
{
    private static final float[] RED = {1, 0, 0};
    private static final float[] GREEN = {0, 1, 0};
    private static final float[] YELLOW = {1, 1, 0};
    private static final float[] BLUE = {0, 0, 1};
    private static final float[] GREY = {0.3f, 0.3f, 0.3f};
    private static final float[] TRANSPARENT_BLACK = {0, 0, 0, 1};

    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    public CombinedLayersDemo()
    {
        super("Combined Layer/Viewport Aviatrix3D Demo");

        setLayout(new BorderLayout());
        addWindowListener(this);

        setupAviatrix();
        setupSceneGraph();

        setSize(800, 800);
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

        GraphicsSortStage sorter = new SimpleTransparencySortStage();
        surface = new DebugAWTSurface(caps);
        surface.setColorClearNeeded(false);  // we're using a background
        DefaultGraphicsPipeline pipeline = new DefaultGraphicsPipeline();

        pipeline.setCuller(culler);
        pipeline.setSorter(sorter);
        pipeline.setGraphicsOutputDevice(surface);

        displayManager = new SingleDisplayCollection();
        displayManager.addPipeline(pipeline);

        // Render manager
        sceneManager = new SingleThreadRenderManager();
        sceneManager.addDisplay(displayManager);
        sceneManager.setMinimumFrameInterval(10);

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
        // Flat panel that has the viewable object as the demo
        float[] coord = { 0, 0, -1,
                          0.25f, 0, -1,
                          0.25f, 0.25f, -1,
                          0, 0.25f, -1 };
        float[] normal = { 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1};

        QuadArray geom = new QuadArray();
        geom.setValidVertexCount(4);
        geom.setVertices(QuadArray.COORDINATE_3, coord);
        geom.setNormals(normal);

        // Rear primary layer. This contains A spinning yellow quad
        Material material = new Material();
        material.setEmissiveColor(YELLOW);
        material.setTransparency(0.5f);

        Appearance app = new Appearance();
        app.setMaterial(material);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);

        TransformGroup layer1_group = new TransformGroup();
        layer1_group.addChild(shape);

        Viewpoint vp1 = new Viewpoint();
        Background bg = new ColorBackground(GREY);
        Vector3f trans = new Vector3f(0, 0, 1);

        Matrix4f mat = new Matrix4f();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp1);
        tx.setTransform(mat);

        Group view_group1 = new Group();
        view_group1.addChild(layer1_group);
        view_group1.addChild(tx);
        view_group1.addChild(bg);

        SimpleScene scene1 = new SimpleScene();
        scene1.setRenderedGeometry(view_group1);
        scene1.setActiveView(vp1);
        scene1.setActiveBackground(bg);

        SimpleViewport view1 = new SimpleViewport();
        view1.setDimensions(0, 0, 800, 800);
        view1.setScene(scene1);

        SimpleLayer back_layer = new SimpleLayer();
        back_layer.setViewport(view1);

        // The front primary layer. Contains two viewports. One has layers
        // the other is a single view into the middle layer of the other
        // viewport.
        material = new Material();
        material.setEmissiveColor(RED);
        material.setTransparency(0.5f);

        app = new Appearance();
        app.setMaterial(material);

        shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);

        trans = new Vector3f(-0.1f, 0, 0);
        Matrix4f mat2 = new Matrix4f();
        mat2.setIdentity();
        mat2.setTranslation(trans);

        TransformGroup layer2_1_group = new TransformGroup();
        layer2_1_group.addChild(shape);
        layer2_1_group.setTransform(mat2);

        Viewpoint vp2_1 = new Viewpoint();
        bg = new ColorBackground(TRANSPARENT_BLACK);
        trans = new Vector3f(0, 0, 1);

        mat = new Matrix4f();
        mat.setIdentity();
        mat.setTranslation(trans);

        tx = new TransformGroup();
        tx.addChild(vp2_1);
        tx.setTransform(mat);

        Group view_group2_1 = new Group();
        view_group2_1.addChild(layer2_1_group);
        view_group2_1.addChild(tx);
        view_group2_1.addChild(bg);

        SimpleScene scene2_1 = new SimpleScene();
        scene2_1.setRenderedGeometry(view_group2_1);
        scene2_1.setActiveView(vp2_1);
        scene2_1.setActiveBackground(bg);

        SimpleViewportLayer layer2_1 = new SimpleViewportLayer();
        layer2_1.setScene(scene2_1);


        // Middle layer. Object in the middle of the screen, no background
        material = new Material();
        material.setEmissiveColor(GREEN);

        app = new Appearance();
        app.setMaterial(material);

        shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);

        trans = new Vector3f(0, 0, 1);
        mat2 = new Matrix4f();
        mat2.setIdentity();
        mat2.setTranslation(trans);

        TransformGroup layer2_2_group = new TransformGroup();
        layer2_2_group.addChild(shape);
        layer2_2_group.setTransform(mat2);

        SharedNode common_group = new SharedNode();
        common_group.setChild(layer2_2_group);

        Viewpoint vp2_2 = new Viewpoint();

        mat = new Matrix4f();
        mat.setIdentity();
        mat.setTranslation(trans);

        tx = new TransformGroup();
        tx.addChild(vp2_2);
        tx.setTransform(mat);

        Group view_group2_2 = new Group();
        view_group2_2.addChild(common_group);
        view_group2_2.addChild(tx);

        SimpleScene scene2_2 = new SimpleScene();
        scene2_2.setRenderedGeometry(view_group2_2);
        scene2_2.setActiveView(vp2_2);

        SimpleViewportLayer layer2_2 = new SimpleViewportLayer();
        layer2_2.setScene(scene2_2);

        // Top layer. Object on the right of the screen, no background
        material = new Material();
        material.setEmissiveColor(BLUE);
        material.setTransparency(0.5f);

        app = new Appearance();
        app.setMaterial(material);

        shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);

        trans = new Vector3f(0.1f, 0, 1);
        mat2 = new Matrix4f();
        mat2.setIdentity();
        mat2.setTranslation(trans);

        Viewpoint vp2_3 = new Viewpoint();

        trans = new Vector3f(0, 0, 1);
        mat = new Matrix4f();
        mat.setIdentity();
        mat.setTranslation(trans);

        tx = new TransformGroup();
        tx.addChild(vp2_3);
        tx.setTransform(mat);

        TransformGroup layer2_3_group = new TransformGroup();
        layer2_3_group.addChild(shape);
        layer2_3_group.setTransform(mat2);

        Group view_group2_3 = new Group();
        view_group2_3.addChild(layer2_3_group);
        view_group2_3.addChild(tx);

        SimpleScene scene2_3 = new SimpleScene();
        scene2_3.setRenderedGeometry(view_group2_3);
        scene2_3.setActiveView(vp2_3);

        SimpleViewportLayer layer2_3 = new SimpleViewportLayer();
        layer2_3.setScene(scene2_3);

        ViewportLayer[] view_layers = { layer2_1, layer2_2, layer2_3 };

        CompositeViewport c_view1 = new CompositeViewport();
        c_view1.setDimensions(500, 100, 200, 200);
        c_view1.addViewportLayer(layer2_1);
        c_view1.addViewportLayer(layer2_2);
        c_view1.addViewportLayer(layer2_3);

        // Now the other viewport that just looks at the middle layer
        // scene graph from somewhat on the side.
        Viewpoint vp3 = new Viewpoint();

        mat = new Matrix4f();
        mat.setIdentity();
        mat.setTranslation(trans);

        trans = new Vector3f(1, 0, 1);
        mat = new Matrix4f();
        mat.setIdentity();
        mat.rotY((float)(Math.PI / 4));
        mat.setTranslation(trans);

        tx = new TransformGroup();
        tx.addChild(vp3);
        tx.setTransform(mat);

        Group view_group3 = new Group();
        view_group3.addChild(common_group);
        view_group3.addChild(tx);

        SimpleScene scene3 = new SimpleScene();
        scene3.setRenderedGeometry(view_group3);
        scene3.setActiveView(vp3);

        SimpleViewport c_view2 = new SimpleViewport();
        c_view2.setDimensions(100, 500, 200, 200);
        c_view2.setScene(scene3);

        // Assemble this layer
        CompositeLayer front_layer = new CompositeLayer();
        front_layer.addViewport(c_view1);
        front_layer.addViewport(c_view2);

        Layer[] layers = { back_layer, front_layer };
        displayManager.setLayers(layers, 2);

        CombinedLayerAnimation anim = new CombinedLayerAnimation(layer1_group,
                                                                 layer2_2_group);
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
        CombinedLayersDemo demo = new CombinedLayersDemo();
        demo.setVisible(true);
    }
}
