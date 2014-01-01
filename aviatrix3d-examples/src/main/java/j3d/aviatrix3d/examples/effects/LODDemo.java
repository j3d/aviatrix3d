package j3d.aviatrix3d.examples.effects;

// External imports
import java.awt.*;
import java.awt.event.*;

import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.output.graphics.DebugAWTSurface;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.DefaultGraphicsPipeline;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;
import org.j3d.aviatrix3d.pipeline.graphics.GenericCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.NullSortStage;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsSortStage;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;

import org.j3d.renderer.aviatrix3d.nodes.LODGroup;

import org.j3d.geom.GeometryData;
import org.j3d.geom.SphereGenerator;
import org.j3d.geom.BoxGenerator;

/**
 * Example application that demonstrates how to use the OctTree extension
 * class in org.j3d.renderer.aviatrix3d.geom.volume.
 *
 * The demo creates a single level of detail and moves in and out by animating
 * the viewpoint through the range that would trigger the detail level change.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class LODDemo extends Frame
    implements WindowListener
{
    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    public LODDemo()
    {
        super("OctTree Aviatrix Demo");

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

        GraphicsCullStage culler = new GenericCullStage();
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
        vp.setHeadlightEnabled(true);

        Vector3d trans = new Vector3d();
        trans.set(0, 0, 1);

        Matrix4d mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup view_tx = new TransformGroup();
        view_tx.addChild(vp);
        view_tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(view_tx);

        // Flat panel that has the viewable object as the demo
        // Sphere to render the shader onto
        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA;

        SphereGenerator sphere_gen = new SphereGenerator(0.1f, 32);
        sphere_gen.generate(data);

        TriangleArray geom1 = new TriangleArray();
        geom1.setVertices(TriangleArray.COORDINATE_3,
                          data.coordinates,
                          data.vertexCount);
        geom1.setNormals(data.normals);

        Material material = new Material();
        material.setDiffuseColor(new float[] { 0, 0, 1 });
        material.setEmissiveColor(new float[] { 0, 0, 1 });
        material.setSpecularColor(new float[] { 1, 1, 1 });

        Appearance app1 = new Appearance();
        app1.setMaterial(material);

        Shape3D sphere_shape = new Shape3D();
        sphere_shape.setGeometry(geom1);
        sphere_shape.setAppearance(app1);


        BoxGenerator box_gen = new BoxGenerator(0.2f, 0.2f, 0.2f);
        box_gen.generate(data);

        TriangleArray geom2 = new TriangleArray();
        geom2.setVertices(TriangleArray.COORDINATE_3,
                          data.coordinates,
                          data.vertexCount);
        geom2.setNormals(data.normals);

        material = new Material();
        material.setDiffuseColor(new float[] { 0, 1, 0 });
        material.setEmissiveColor(new float[] { 0, 1, 0 });
        material.setSpecularColor(new float[] { 1, 1, 1 });

        Appearance app2 = new Appearance();
        app2.setMaterial(material);

        Shape3D box_shape = new Shape3D();
        box_shape.setGeometry(geom2);
        box_shape.setAppearance(app2);

        LODGroup lod = new LODGroup(true);
        lod.addChild(sphere_shape);
        lod.addChild(box_shape);
        lod.setRange(0, 1.0f);
        lod.setRange(1, 2.0f);

        scene_root.addChild(lod);

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

        VpAnimation anim = new VpAnimation(view_tx);
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
        LODDemo demo = new LODDemo();
        demo.setVisible(true);
    }
}
