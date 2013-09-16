
// Standard imports
import java.awt.*;
import java.awt.event.*;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import javax.media.opengl.GLCapabilities;

// Application Specific imports
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

import org.j3d.geom.GeometryData;
import org.j3d.geom.SphereGenerator;

/**
 * Example application that demonstrates using multiple clip planes at
 * different levels of the heirarchy.
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
public class MultiClipPlaneDemo extends Frame
    implements WindowListener
{
    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    public MultiClipPlaneDemo()
    {
        super("Multiple Clip Plane Demo");

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

        Vector3f trans = new Vector3f(0, 0, 1);

        Matrix4f mat = new Matrix4f();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);

        // Sphere to render the shader onto
        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA;

        SphereGenerator generator = new SphereGenerator(0.1f);
        generator.generate(data);

        TriangleArray geom = new TriangleArray();
        geom.setVertices(TriangleArray.COORDINATE_3,
                         data.coordinates,
                         data.vertexCount);
        geom.setNormals(data.normals);

        Material material1 = new Material();
        material1.setDiffuseColor(new float[] { 0, 0, 1 });
        material1.setEmissiveColor(new float[] { 0, 0, 1 });
        material1.setSpecularColor(new float[] { 1, 1, 1 });

        PolygonAttributes pa = new PolygonAttributes();
//        pa.setDrawMode(true, PolygonAttributes.DRAW_LINE);
        pa.setCulledFace(PolygonAttributes.CULL_BACK);


        Appearance app1 = new Appearance();
        app1.setMaterial(material1);
        app1.setPolygonAttributes(pa);

        Material material2 = new Material();
        material2.setDiffuseColor(new float[] { 1, 0, 0 });
        material2.setEmissiveColor(new float[] { 1, 0, 0 });
        material2.setSpecularColor(new float[] { 1, 1, 1 });

        Appearance app2 = new Appearance();
        app2.setMaterial(material2);
        app2.setPolygonAttributes(pa);

        Shape3D shape1 = new Shape3D();
        shape1.setGeometry(geom);
        shape1.setAppearance(app1);

        Shape3D shape2 = new Shape3D();
        shape2.setGeometry(geom);
        shape2.setAppearance(app2);

        double[] plane_eq1 = { 0, 1, 0, 0 };
        double[] plane_eq2 = { 1, 0, 0, 0 };
        double[] plane_eq3 = { 1, 1, 0, 0 };

        ClipPlane cp1 = new ClipPlane();
        cp1.setPlaneEquation(plane_eq1);
        cp1.setEnabled(true);

        ClipPlane cp2 = new ClipPlane();
        cp2.setPlaneEquation(plane_eq2);
        cp2.setEnabled(true);

        ClipPlane cp3 = new ClipPlane();
        cp3.setPlaneEquation(plane_eq3);
        cp3.setEnabled(true);

        scene_root.addChild(cp1);

        trans = new Vector3f(-0.2f, 0.025f, 0);

        mat = new Matrix4f();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup left_tx = new TransformGroup();
        left_tx.setTransform(mat);
        left_tx.addChild(shape1);
        left_tx.addChild(cp2);

        scene_root.addChild(left_tx);

        trans = new Vector3f(0.2f, 0, 0);
        mat.setTranslation(trans);

        TransformGroup right_tx = new TransformGroup();
        right_tx.setTransform(mat);
        right_tx.addChild(shape2);
        right_tx.addChild(cp3);

        scene_root.addChild(right_tx);

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
        MultiClipPlaneDemo demo = new MultiClipPlaneDemo();
        demo.setVisible(true);
    }
}
