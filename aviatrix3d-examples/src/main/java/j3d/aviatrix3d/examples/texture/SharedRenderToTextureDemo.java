package j3d.aviatrix3d.examples.texture;

// Standard imports

import java.awt.*;
import java.awt.event.*;

import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;


// Application Specific imports
import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.pipeline.graphics.*;

import org.j3d.aviatrix3d.output.graphics.SimpleAWTSurface;
import org.j3d.aviatrix3d.output.graphics.DebugAWTSurface;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;

/**
 * Example application that demonstrates how to put together a single-threaded
 * rendering system.
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
public class SharedRenderToTextureDemo extends Frame
        implements WindowListener
{
    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    public SharedRenderToTextureDemo()
    {
        super("Aviatrix Render To Texture Demo");

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

        GraphicsCullStage culler = new FrustumCullStage();
        culler.setOffscreenCheckEnabled(true);

        GraphicsSortStage sorter = new StateAndTransparencyDepthSortStage();
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
        Component comp = (Component) surface.getSurfaceObject();
        add(comp, BorderLayout.CENTER);
    }

    /**
     * Setup the basic scene which consists of a quad and a viewpoint
     */
    private void setupSceneGraph()
    {
        // View group
        Viewpoint vp = new Viewpoint();

        Vector3d trans = new Vector3d(0, 0, 1);

        Matrix4d mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);

        // Flat panel that has the viewable object as the demo
        float[] coord = {
                0, 0, -1, 0.25f, 0, -1, 0, 0.25f, -1,
                0.25f, 0, -1, 0.25f, 0.25f, -1, 0, 0.25f, -1,
                0.25f, 0, -1, 0.5f, 0, -1, 0.25f, 0.25f, -1,
                0.5f, 0, -1, 0.5f, 0.25f, -1, 0.25f, 0.25f, -1
        };

        float[] normal = {
                0, 0, 1, 0, 0, 1, 0, 0, 1,
                0, 0, 1, 0, 0, 1, 0, 0, 1,
                0, 0, 1, 0, 0, 1, 0, 0, 1,
                0, 0, 1, 0, 0, 1, 0, 0, 1
        };
        float[][] tex_coord = {
                {
                        0, 0, 1, 0, 0, 1, 1, 0, 1, 1, 0, 1,
                        0, 0, 1, 0, 0, 1, 1, 0, 1, 1, 0, 1
                }
        };
        int[] tex_type = {VertexGeometry.TEXTURE_COORDINATE_2};

        TriangleArray geom = new TriangleArray();
        geom.setVertices(TriangleArray.COORDINATE_3, coord, 6);
        geom.setTextureCoordinates(tex_type, tex_coord, 1);

        Material material = new Material();
        material.setDiffuseColor(new float[]{0, 0, 1});
        material.setEmissiveColor(new float[]{0, 0, 1});
        material.setSpecularColor(new float[]{1, 1, 1});

//        app.setMaterial(material);

        // The texture requires its own set of capabilities.
        GraphicsRenderingCapabilities caps = new GraphicsRenderingCapabilities();
        caps.doubleBuffered = false;

        OffscreenTexture2D texture = new OffscreenTexture2D(caps, 600, 600);
        setupTextureSceneGraph(texture);

        TextureUnit[] tu = new TextureUnit[1];
        tu[0] = new TextureUnit();
        tu[0].setTexture(texture);

        Appearance r_app = new Appearance();
        r_app.setTextureUnits(tu, 1);

        Shape3D r_shape = new Shape3D();
        r_shape.setGeometry(geom);
        r_shape.setAppearance(r_app);

        Vector3d translation = new Vector3d(0.1f, 0, 0);
        Matrix4d obj_transform = new Matrix4d();
        obj_transform.setIdentity();
        obj_transform.setTranslation(translation);

        TransformGroup tg = new TransformGroup();
        tg.setTransform(obj_transform);
        tg.addChild(r_shape);
        scene_root.addChild(tg);

        tu = new TextureUnit[1];
        tu[0] = new TextureUnit();
        tu[0].setTexture(texture);

        Appearance l_app = new Appearance();
        l_app.setTextureUnits(tu, 1);

        Shape3D l_shape = new Shape3D();
        l_shape.setGeometry(geom);
        l_shape.setAppearance(l_app);

        translation.x = -0.3d;
        obj_transform.setTranslation(translation);

        tg = new TransformGroup();
        tg.setTransform(obj_transform);
        tg.addChild(l_shape);
        scene_root.addChild(tg);

        SimpleScene scene = new SimpleScene();
        scene.setRenderedGeometry(scene_root);
        scene.setActiveView(vp);

        // Then the basic layer and viewport at the top:
        SimpleViewport view = new SimpleViewport();
        view.setDimensions(0, 0, 600, 600);
        view.setScene(scene);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(view);

        Layer[] layers = {layer};
        displayManager.setLayers(layers, 1);
    }

    /**
     * Convenience method to set up the details of the texture.
     */
    private void setupTextureSceneGraph(OffscreenTexture2D texture)
    {
        texture.setClearColor(1, 0, 0, 1);

        Group scene_root = new Group();

        TransformGroup grp = new TransformGroup();

        Vector3d trans = new Vector3d(0, 0, 0);

        Matrix4d mat = new Matrix4d();
        mat.setIdentity();
        mat.setScale(10);
        mat.setTranslation(trans);

        // Flat panel that has the viewable object as the demo
        float[] coord = {
                0, 0, -1, 0.5f, 0, -1, 0, 0.5f, -1,
                0.5f, 0, -1, 0.5f, 0.5f, -1, 0, 0.5f, -1
        };

        float[] color = {
                0, 0, 1, 0, 1, 0, 1, 0, 0,
                0, 1, 1, 0, 1, 1, 1, 0, 1
        };

        float[] normal = {
                0, 0, 1, 0, 0, 1, 0, 0, 1,
                0, 0, 1, 0, 0, 1, 0, 0, 1,
                0, 0, 1, 0, 0, 1, 0, 0, 1,
                0, 0, 1, 0, 0, 1, 0, 0, 1
        };

        TriangleArray geom = new TriangleArray();
        geom.setVertices(TriangleArray.COORDINATE_3, coord, 6);
        geom.setColors(false, color);

        Material material = new Material();
        material.setDiffuseColor(new float[]{0, 0, 1});
        material.setEmissiveColor(new float[]{0, 0, 1});
        material.setSpecularColor(new float[]{1, 1, 1});

        Appearance app = new Appearance();
//        app.setMaterial(material);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);

//        grp.setTransform(mat);
        grp.addChild(shape);

        // Give the texture it's own separate viewpoint.
        Viewpoint vp = new Viewpoint();

        trans.set(0, 0.0f, 5f);

        mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup vp_grp = new TransformGroup();
        vp_grp.setTransform(mat);
        vp_grp.addChild(vp);

        scene_root.addChild(grp);
        scene_root.addChild(vp_grp);

        SimpleScene scene = new SimpleScene();
        scene.setRenderedGeometry(scene_root);
        scene.setActiveView(vp);

        SimpleViewport sceneViewport = new SimpleViewport();
        sceneViewport.setDimensions(0, 0, 600, 600);
        sceneViewport.setScene(scene);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(sceneViewport);

        /*
        RenderPass renderPass = new RenderPass();
        renderPass.setRenderedGeometry(scene_root);
        renderPass.setActiveView(vp);

        MultipassScene scene = new MultipassScene();
        scene.addRenderPass(renderPass);

        // Then the basic layer and viewport at the top:
        MultipassViewport sceneViewport = new MultipassViewport();
        sceneViewport.setDimensions(0, 0, 600, 600);
        sceneViewport.setScene(scene);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(sceneViewport);

        */

        Layer[] layers = {layer};

        texture.setLayers(layers, 1);
        texture.setRepaintRequired(true);
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
        SharedRenderToTextureDemo demo = new SharedRenderToTextureDemo();
        demo.setVisible(true);
    }
}
