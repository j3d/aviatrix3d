
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
import org.j3d.aviatrix3d.pipeline.graphics.NullSortStage;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsSortStage;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;

import org.j3d.renderer.aviatrix3d.geom.Sphere;

/**
 * Alternate version of the stencil buffer multipass rendering demo that
 * uses local attributes to combine passes 2 and 3 into a single pass.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class StencilDemo2 extends Frame
    implements WindowListener
{
    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    public StencilDemo2()
    {
        super("Alternate Stencil Buffer Demo");

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
        caps.setStencilBits(8);
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

        Vector3f trans = new Vector3f(0, 0, 5);

        Matrix4f mat = new Matrix4f();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        StencilAttributes sa1 = new StencilAttributes();
        sa1.setStencilFunction(StencilBufferState.FUNCTION_EQUAL);
        sa1.setFunctionReferenceValue(0x1);
        sa1.setFunctionCompareMask(0x1);
        sa1.setStencilFailOperation(StencilBufferState.STENCIL_KEEP);
        sa1.setDepthFailOperation(StencilBufferState.STENCIL_KEEP);
        sa1.setDepthPassOperation(StencilBufferState.STENCIL_KEEP);

        Material blue_mat = new Material();
        blue_mat.setLightingEnabled(true);
        blue_mat.setDiffuseColor(blue_diffuse);
        blue_mat.setSpecularColor(blue_specular);
        blue_mat.setShininess(0.8f);

        Appearance blue_app = new Appearance();
        blue_app.setMaterial(blue_mat);
        blue_app.setStencilAttributes(sa1);

        Sphere sphere = new Sphere(0.55f, blue_app);

        StencilAttributes sa2 = new StencilAttributes();
        sa2.setStencilFunction(StencilBufferState.FUNCTION_NOTEQUAL);
        sa2.setFunctionReferenceValue(0x1);
        sa2.setFunctionCompareMask(0x1);
        sa2.setStencilFailOperation(StencilBufferState.STENCIL_KEEP);
        sa2.setDepthFailOperation(StencilBufferState.STENCIL_KEEP);
        sa2.setDepthPassOperation(StencilBufferState.STENCIL_KEEP);

        Material yellow_mat = new Material();
        yellow_mat.setLightingEnabled(true);
        yellow_mat.setDiffuseColor(yellow_diffuse);
        yellow_mat.setSpecularColor(yellow_specular);
        yellow_mat.setShininess(0.8f);

        Appearance yellow_app = new Appearance();
        yellow_app.setMaterial(yellow_mat);
        yellow_app.setStencilAttributes(sa2);

//        Torus torus1 = new Torus(0.275f, 1.85f, yellow_app);
        Sphere torus1 = new Sphere(0.85f, yellow_app);

        trans = new Vector3f(-0.3f, 0, 0);

        mat = new Matrix4f();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup torus_tx1 = new TransformGroup();
        torus_tx1.setTransform(mat);
        torus_tx1.addChild(torus1);

//        Torus torus2 = new Torus(0.275f, 1.85f, yellow_app);
        Sphere torus2 = new Sphere(0.45f, yellow_app);

        trans = new Vector3f(0.4f, 0.2f, 0.2f);

        mat = new Matrix4f();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup torus_tx2 = new TransformGroup();
        torus_tx2.setTransform(mat);
        torus_tx2.addChild(torus2);

        scene_root = new Group();
        scene_root.addChild(tx);
        scene_root.addChild(sphere);
        scene_root.addChild(torus_tx1);
        scene_root.addChild(torus_tx2);

        ColorBufferState cbs = new ColorBufferState();
        DepthBufferState dbs = new DepthBufferState();

        RenderPass pass2 = new RenderPass();
        pass2.setRenderedGeometry(scene_root);
        pass2.setActiveView(vp);
        pass2.setColorBufferState(cbs);
        pass2.setDepthBufferState(dbs);

        MultipassScene scene = new MultipassScene();
        scene.addRenderPass(pass1);
        scene.addRenderPass(pass2);

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
        StencilDemo2 demo = new StencilDemo2();
        demo.setVisible(true);
    }
}
