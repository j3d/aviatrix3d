
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
import org.j3d.aviatrix3d.picking.PickRequest;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.DefaultGraphicsPipeline;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;
import org.j3d.aviatrix3d.pipeline.graphics.NullCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.NullSortStage;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsSortStage;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;

import org.j3d.geom.GeometryData;
import org.j3d.geom.BoxGenerator;

/**
 * Example application that demonstrates how to put together a single-threaded
 * rendering system.
 *
 * @author Justin Couch
 * @version $Revision: 1.10 $
 */
public class LinePickDemo extends Frame
    implements WindowListener
{
    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;


    public LinePickDemo()
    {
        super("Aviatrix Simple Picking Demo");

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

/*
        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA;

        BoxGenerator generator = new BoxGenerator(0.15f, 0.15f, 0.15f);
        generator.generate(data);

        TriangleArray geom = new TriangleArray();
        geom.setVertices(TriangleArray.COORDINATE_3,
                         data.coordinates,
                         data.vertexCount);
        geom.setNormals(data.normals);

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLE_FANS;
        data.geometryComponents = GeometryData.NORMAL_DATA;

        BoxGenerator generator = new BoxGenerator(0.15f, 0.15f, 0.15f);
        generator.generate(data);

        TriangleFanArray geom = new TriangleFanArray();
        geom.setVertices(TriangleStripArray.COORDINATE_3,
                         data.coordinates,
                         data.vertexCount);
        geom.setNormals(data.normals);
        geom.setFanCount(data.stripCounts, data.numStrips);

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLE_STRIPS;
        data.geometryComponents = GeometryData.NORMAL_DATA;

        BoxGenerator generator = new BoxGenerator(0.15f, 0.15f, 0.15f);
        generator.generate(data);

        TriangleStripArray geom = new TriangleStripArray();
        geom.setVertices(TriangleStripArray.COORDINATE_3,
                         data.coordinates,
                         data.vertexCount);
        geom.setNormals(data.normals);
        geom.setStripCount(data.stripCounts, data.numStrips);
*/

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.QUADS;
        data.geometryComponents = GeometryData.NORMAL_DATA;

        BoxGenerator generator = new BoxGenerator(0.15f, 0.15f, 0.15f);
        generator.generate(data);

        QuadArray geom = new QuadArray();
        geom.setVertices(QuadArray.COORDINATE_3,
                         data.coordinates,
                         data.vertexCount);
        geom.setNormals(data.normals);


        Material material = new Material();
        material.setEmissiveColor(new float[] { 0, 0, 1 });

        Appearance app = new Appearance();
        app.setMaterial(material);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);

        trans.set(0.5f, 0, 0);
        Matrix4f mat2 = new Matrix4f();
        mat2.setIdentity();
        mat2.setTranslation(trans);

        TransformGroup shape_transform = new TransformGroup();
        shape_transform.addChild(shape);
        shape_transform.setTransform(mat2);

//
        data = new GeometryData();
        data.geometryType = GeometryData.QUADS;
        data.geometryComponents = GeometryData.NORMAL_DATA;

        generator = new BoxGenerator(0.1f, 0.1f, 0.1f);
        generator.generate(data);

        QuadArray geom_2 = new QuadArray();
        geom_2.setVertices(QuadArray.COORDINATE_3,
                         data.coordinates,
                         data.vertexCount);
        geom_2.setNormals(data.normals);

        Material material_2 = new Material();
        material_2.setEmissiveColor(new float[] { 1, 0, 0 });

        Appearance app_2 = new Appearance();
        app_2.setMaterial(material_2);

        Shape3D shape_2 = new Shape3D();
        shape_2.setGeometry(geom_2);
        shape_2.setAppearance(app_2);
//

        trans.set(0.25f, 0.25f, 0);
        mat2.setTranslation(trans);

        TransformGroup shape_transform_2 = new TransformGroup();
        shape_transform_2.addChild(shape_2);
        shape_transform_2.setTransform(mat2);

        scene_root.addChild(shape_transform);
        scene_root.addChild(shape_transform_2);


        LinePickHandler anim = new LinePickHandler(scene_root,
                                                     shape_transform,
                                                     material);

        // Place a point object where the picker is
        float[] coords = new float[6];
        coords[0] = 10;
        coords[1] = 0.1f;
        coords[3] = -0.1f;
        LineArray line_geom = new LineArray();
        line_geom.setVertices(LineArray.COORDINATE_3,
                               coords,
                               2);

        material = new Material();
        material.setEmissiveColor(new float[] { 1, 0, 0 });

        app = new Appearance();
        app.setMaterial(material);

        shape = new Shape3D();
        shape.setGeometry(line_geom);
        shape.setAppearance(app);
        shape.setPickMask(0);

        scene_root.addChild(shape);

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
        LinePickDemo demo = new LinePickDemo();
    }
}
