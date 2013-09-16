
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
 * Example application that demonstrates a multiple top-level layers with
 * single viewports in each layer. One object is placed in each layer and it
 * should show the two objects with the front layer always rendered in front
 * of the background layer. The front layer is fixed, but the back layer
 * animates in such a way that, if both objects were in the one scene, it
 * would move in front and then behind it.
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public class MultiLayerDemo extends Frame
    implements WindowListener
{
    private static final float[] RED = {1, 0, 0};
    private static final float[] GREEN = {0, 1, 0};
    private static final float[] BLUE = {0, 0, 1};
    private static final float[] GREY = {0.3f, 0.3f, 0.3f};

    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    public MultiLayerDemo()
    {
        super("Multiple Layer Aviatrix3D Demo");

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
        // View group


        // Flat panel that has the viewable object as the demo
        float[] coord = { 0, 0, -1,  0.25f, 0, -1,  0.25f, 0.25f, -1, 0.25f, 0, -1 };
        float[] normal = { 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1};

        QuadArray geom = new QuadArray();
        geom.setValidVertexCount(4);
        geom.setVertices(QuadArray.COORDINATE_3, coord);
        geom.setNormals(normal);

        // Rear-most layer. Object offset to the left
        Material material = new Material();
        material.setEmissiveColor(RED);
        material.setTransparency(0.5f);

        Appearance app = new Appearance();
        app.setMaterial(material);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);

        Vector3f trans = new Vector3f(-0.1f, 0, 0);
        Matrix4f mat2 = new Matrix4f();
        mat2.setIdentity();
        mat2.setTranslation(trans);

        TransformGroup layer1_group = new TransformGroup();
        layer1_group.addChild(shape);
        layer1_group.setTransform(mat2);

        Viewpoint vp1 = new Viewpoint();
        Background bg = new ColorBackground(GREY);
        trans = new Vector3f(0, 0, 1);

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


        TransformGroup layer2_group = new TransformGroup();
        layer2_group.addChild(shape);
        layer2_group.setTransform(mat2);

        Viewpoint vp2 = new Viewpoint();

        mat = new Matrix4f();
        mat.setIdentity();
        mat.setTranslation(trans);

        tx = new TransformGroup();
        tx.addChild(vp2);
        tx.setTransform(mat);

        Group view_group2 = new Group();
        view_group2.addChild(layer2_group);
        view_group2.addChild(tx);

        SimpleScene scene2 = new SimpleScene();
        scene2.setRenderedGeometry(view_group2);
        scene2.setActiveView(vp2);

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

        Viewpoint vp3 = new Viewpoint();

        trans = new Vector3f(0, 0, 1);
        mat = new Matrix4f();
        mat.setIdentity();
        mat.setTranslation(trans);

        tx = new TransformGroup();
        tx.addChild(vp3);
        tx.setTransform(mat);

        TransformGroup layer3_group = new TransformGroup();
        layer3_group.addChild(shape);
        layer3_group.setTransform(mat2);

        Group view_group3 = new Group();
        view_group3.addChild(layer3_group);
        view_group3.addChild(tx);

        SimpleScene scene3 = new SimpleScene();
        scene3.setRenderedGeometry(view_group3);
        scene3.setActiveView(vp3);


        // Then the viewports. Divide the screen up into 4 viewports
        SimpleViewport view1 = new SimpleViewport();
        view1.setDimensions(0, 0, 800, 800);
        view1.setScene(scene1);

        SimpleViewport view2 = new SimpleViewport();
        view2.setDimensions(0, 0, 800, 800);
        view2.setScene(scene2);

        SimpleViewport view3 = new SimpleViewport();
        view3.setDimensions(0, 0, 800, 800);
        view3.setScene(scene3);

        SimpleLayer layer1 = new SimpleLayer();
        layer1.setViewport(view1);

        SimpleLayer layer2 = new SimpleLayer();
        layer2.setViewport(view2);

        SimpleLayer layer3 = new SimpleLayer();
        layer3.setViewport(view3);

        Layer[] layers = { layer1, layer2, layer3 };
        displayManager.setLayers(layers, 3);

        ObjectLayerAnimation anim = new ObjectLayerAnimation(layer2_group);
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
        MultiLayerDemo demo = new MultiLayerDemo();
        demo.setVisible(true);
    }
}
