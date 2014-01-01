package j3d.aviatrix3d.examples.multipass;

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
import org.j3d.aviatrix3d.pipeline.graphics.NullCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.NullSortStage;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsSortStage;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;

import org.j3d.renderer.aviatrix3d.geom.Sphere;

/**
 * Example application that demonstrates a very basic 3-pass multipass
 * rendering using the stencil buffer.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class StencilDemo extends Frame
    implements WindowListener
{
    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    public StencilDemo()
    {
        super("Basic Stencil Buffer Demo");

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
        caps.stencilBits = 8;

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
        // The first render pass sets up the stencil values in the buffer. It
        // consists of an ortho viewpoint to draw a diamond in the middle of
        // the screen to set the stencil values
        Viewpoint vp = new Viewpoint();

        Group scene_root = new Group();
        scene_root.addChild(vp);

        // Flat panel that has the viewable object as the demo
        float[] coord = { 0, -1, 1, 0, 0, 1, -1, 0 };
        float[] yellow_diffuse = { 0.7f, 0.7f, 0.0f, 1.0f };
        float[] yellow_specular = { 1.0f, 1.0f, 1.0f, 1.0f };
        float[] blue_diffuse = { 0.1f, 0.1f, 0.7f, 1.0f };
        float[] blue_specular = { 0.1f, 1.0f, 1.0f, 1.0f };

        QuadArray geom = new QuadArray();
        geom.setVertices(TriangleArray.COORDINATE_2, coord);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);

        scene_root.addChild(shape);

        StencilBufferState sbs = new StencilBufferState();
        sbs.setStencilFunction(StencilBufferState.FUNCTION_ALWAYS);
        sbs.setFunctionReferenceValue(0x1);
        sbs.setFunctionCompareMask(0x1);
        sbs.setStencilFailOperation(StencilBufferState.STENCIL_REPLACE);
        sbs.setDepthFailOperation(StencilBufferState.STENCIL_REPLACE);
        sbs.setDepthPassOperation(StencilBufferState.STENCIL_REPLACE);

        RenderPass pass1 = new RenderPass();
        pass1.setRenderedGeometry(scene_root);
        pass1.setActiveView(vp);
        pass1.setStencilBufferState(sbs);

        ViewEnvironment view = pass1.getViewEnvironment();
        view.setProjectionType(ViewEnvironment.ORTHOGRAPHIC_PROJECTION);
        view.setOrthoParams(-3.0, 3.0, -3.0, 3.0);
        view.setClipDistance(-1.0, 1.0);

        // Second pass we need the stencil buffer to stay uncleared, but clear the
        // depth and colour buffers. Draw a blue sphere where the stencil is 1
        vp = new Viewpoint();
        vp.setHeadlightEnabled(true);
        vp.setHeadlightType(false);

        Vector3d trans = new Vector3d();
        trans.set(0, 0, 5);

        Matrix4d mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Material blue_mat = new Material();
        blue_mat.setLightingEnabled(true);
        blue_mat.setDiffuseColor(blue_diffuse);
        blue_mat.setSpecularColor(blue_specular);
        blue_mat.setShininess(0.8f);

        Appearance blue_app = new Appearance();
        blue_app.setMaterial(blue_mat);

        Sphere sphere = new Sphere(0.55f, blue_app);

        scene_root = new Group();
        scene_root.addChild(tx);
        scene_root.addChild(sphere);

        sbs = new StencilBufferState();
        sbs.setClearBufferState(false);
        sbs.setStencilFunction(StencilBufferState.FUNCTION_EQUAL);
        sbs.setFunctionReferenceValue(0x1);
        sbs.setFunctionCompareMask(0x1);
        sbs.setStencilFailOperation(StencilBufferState.STENCIL_KEEP);
        sbs.setDepthFailOperation(StencilBufferState.STENCIL_KEEP);
        sbs.setDepthPassOperation(StencilBufferState.STENCIL_KEEP);

        ColorBufferState cbs = new ColorBufferState();
        DepthBufferState dbs = new DepthBufferState();

        RenderPass pass2 = new RenderPass();
        pass2.setRenderedGeometry(scene_root);
        pass2.setActiveView(vp);
        pass2.setStencilBufferState(sbs);
        pass2.setColorBufferState(cbs);
        pass2.setDepthBufferState(dbs);

        // Pass three draws a yellow torus where the stencil value is not 1
        // We don't want to clear the colour or depth buffer on this pass
        // either.
        vp = new Viewpoint();
        vp.setHeadlightEnabled(true);
        vp.setHeadlightType(false);

        tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Material yellow_mat = new Material();
        yellow_mat.setLightingEnabled(true);
        yellow_mat.setDiffuseColor(yellow_diffuse);
        yellow_mat.setSpecularColor(yellow_specular);
        yellow_mat.setShininess(0.8f);

        Appearance yellow_app = new Appearance();
        yellow_app.setMaterial(yellow_mat);

//        Torus torus1 = new Torus(0.275f, 1.85f, yellow_app);
        Sphere torus1 = new Sphere(0.85f, yellow_app);

        trans = new Vector3d();
        trans.set(-0.3, 0, 0);

        mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup torus_tx1 = new TransformGroup();
        torus_tx1.setTransform(mat);
        torus_tx1.addChild(torus1);

//        Torus torus2 = new Torus(0.275f, 1.85f, yellow_app);
        Sphere torus2 = new Sphere(0.45f, yellow_app);

        trans = new Vector3d();
        trans.set(0.4, 0.2, 0.2);

        mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup torus_tx2 = new TransformGroup();
        torus_tx2.setTransform(mat);
        torus_tx2.addChild(torus2);

        scene_root = new Group();
        scene_root.addChild(tx);
        scene_root.addChild(torus_tx1);
        scene_root.addChild(torus_tx2);

        sbs = new StencilBufferState();
        sbs.setClearBufferState(false);
        sbs.setStencilFunction(StencilBufferState.FUNCTION_NOTEQUAL);
        sbs.setFunctionReferenceValue(0x1);
        sbs.setFunctionCompareMask(0x1);
        sbs.setStencilFailOperation(StencilBufferState.STENCIL_KEEP);
        sbs.setDepthFailOperation(StencilBufferState.STENCIL_KEEP);
        sbs.setDepthPassOperation(StencilBufferState.STENCIL_KEEP);

        cbs = new ColorBufferState();
        cbs.setClearBufferState(false);

        dbs = new DepthBufferState();
        dbs.setClearBufferState(false);

        RenderPass pass3 = new RenderPass();
        pass3.setRenderedGeometry(scene_root);
        pass3.setActiveView(vp);
        pass3.setStencilBufferState(sbs);
        pass3.setColorBufferState(cbs);
        pass3.setDepthBufferState(dbs);

        MultipassScene scene = new MultipassScene();
        scene.addRenderPass(pass1);
        scene.addRenderPass(pass2);
        scene.addRenderPass(pass3);

        // Then the basic layer and viewport at the top:
        MultipassViewport viewport = new MultipassViewport();
        viewport.setDimensions(0, 0, 500, 500);
        viewport.setScene(scene);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(viewport);

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
        StencilDemo demo = new StencilDemo();
        demo.setVisible(true);
    }
}
