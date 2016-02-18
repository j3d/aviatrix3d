package j3d.aviatrix3d.examples.npr;

// External imports
import java.awt.*;
import java.awt.event.*;

import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;


import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.pipeline.graphics.*;

import org.j3d.aviatrix3d.output.graphics.DebugAWTSurface;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;

import org.j3d.geom.GeometryData;
import org.j3d.geom.SphereGenerator;

/**
 * Example application demonstrating the use of fixed function pipeline to do
 * do silhouette edge rendering using the stencil buffer.
 * <p>
 *
 * This is a translation of the original SGI demo.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 * @see http://www.opengl.org/resources/code/samples/glut_examples/advanced/silhouette.c
 */
public class StencilEdgeDemo extends Frame
    implements WindowListener
{
    /** App name to register preferences under */
    private static final String APP_NAME = "StencilEdgeDemo";

    /** Render pass vertex shader string */
    private static final String VERTEX_SHADER_FILE =
        "global_illum/normal_pass_vert.glsl";

    /** Fragment shader file name for the rendering pass */
    private static final String FRAG_SHADER_FILE =
        "global_illum/normal_pass_frag.glsl";

    /** Width and height of the offscreen texture, in pixels */
    private static final int TEXTURE_SIZE = 512;

    /** Width and height of the main window, in pixels */
    private static final int WINDOW_SIZE = 512;

    /** PI / 4 for rotations */
    private static final float PI_4 = (float)(Math.PI * 0.25f);

    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    /**
     * Construct a new shader demo instance.
     */
    public StencilEdgeDemo()
    {
        super("FF Stencil Edge Demo");

        I18nManager intl_mgr = I18nManager.getManager();
        intl_mgr.setApplication(APP_NAME, "config.i18n.org-j3d-aviatrix3d-resources-core");

        setLayout(new BorderLayout());
        addWindowListener(this);

        setupAviatrix();
        setupSceneGraph();

        setSize(WINDOW_SIZE, WINDOW_SIZE);
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

        GraphicsCullStage culler = new FrustumCullStage();
        culler.setOffscreenCheckEnabled(true);

        GraphicsSortStage sorter = new StateAndTransparencyDepthSortStage();
        surface = new DebugAWTSurface(caps);
        surface.setColorClearNeeded(true);
        surface.setClearColor(0, 0, 0, 1);

        DefaultGraphicsPipeline pipeline = new DefaultGraphicsPipeline();

        pipeline.setCuller(culler);
        pipeline.setSorter(sorter);
        pipeline.setGraphicsOutputDevice(surface);

        displayManager = new SingleDisplayCollection();
        displayManager.addPipeline(pipeline);

        // Render manager
        sceneManager = new SingleThreadRenderManager();
        sceneManager.addDisplay(displayManager);
        sceneManager.setMinimumFrameInterval(50);

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
//        Background bg = createBackground();

        Vector3d trans = new Vector3d();
        trans.set(0, 0, 7f);

        Matrix4d view_mat = new Matrix4d();
        view_mat.setIdentity();
        view_mat.setTranslation(trans);

        // Sphere to render for the outline
        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA;

        // Sphere to represent the light position in the scene.
        SphereGenerator generator = new SphereGenerator(50f, 16);
        generator.generate(data);

        TriangleArray real_geom = new TriangleArray();
        real_geom.setValidVertexCount(data.vertexCount);
        real_geom.setVertices(TriangleArray.COORDINATE_3, data.coordinates);
        real_geom.setNormals(data.normals);


        Shape3D shape = new Shape3D();
        shape.setGeometry(real_geom);

        float window_size_2 = WINDOW_SIZE * 0.5f;
        SharedGroup common_geom = new SharedGroup();
        common_geom.addChild(shape);

        // create the 4 stencil passes. First pass we set everything up to
        // clear, but the next 3 passes we don't want to clear
        StencilBufferState sbs = new StencilBufferState();
        sbs.setClearBufferState(true);
        sbs.setStencilFunction(StencilBufferState.FUNCTION_ALWAYS);
        sbs.setFunctionReferenceValue(0x1);
        sbs.setFunctionCompareMask(0x1);
        sbs.setStencilFailOperation(StencilBufferState.STENCIL_KEEP);
        sbs.setDepthFailOperation(StencilBufferState.STENCIL_KEEP);
        sbs.setDepthPassOperation(StencilBufferState.STENCIL_REPLACE);

        DepthBufferState dbs = new DepthBufferState();
        dbs.setClearBufferState(true);
        dbs.enableDepthTest(false);

        // Don't bother clearing the colour buffer until we get to the final
        // pass.
        ColorBufferState cbs = new ColorBufferState();
        cbs.setClearBufferState(true);
        cbs.setColorMask(false, false, false, false);

        RenderPass first_pass =
            createStencilPass(common_geom, view_mat,
                              -1, 0, WINDOW_SIZE - 1, WINDOW_SIZE);
        first_pass.setDepthBufferState(dbs);
        first_pass.setColorBufferState(cbs);
        first_pass.setStencilBufferState(sbs);

        ViewEnvironment ve = first_pass.getViewEnvironment();
        ve.setProjectionType(ViewEnvironment.ORTHOGRAPHIC_PROJECTION);
        ve.setOrthoParams(-window_size_2, window_size_2,
                          -window_size_2, window_size_2);
        ve.setClipDistance(-10.0, 10.0);

        // Reset all of the above so as to not clear the states
        sbs = new StencilBufferState();
        sbs.setClearBufferState(false);
        sbs.setStencilFunction(StencilBufferState.FUNCTION_ALWAYS);
        sbs.setFunctionReferenceValue(0x1);
        sbs.setFunctionCompareMask(0x1);
        sbs.setStencilFailOperation(StencilBufferState.STENCIL_KEEP);
        sbs.setDepthFailOperation(StencilBufferState.STENCIL_KEEP);
        sbs.setDepthPassOperation(StencilBufferState.STENCIL_REPLACE);

        dbs = new DepthBufferState();
        dbs.setClearBufferState(false);
        dbs.enableDepthTest(false);

        cbs = new ColorBufferState();
        cbs.setClearBufferState(false);
        cbs.setColorMask(false, false, false, false);

        RenderPass second_pass =
            createStencilPass(common_geom, view_mat,
                              1, 0, WINDOW_SIZE + 1, WINDOW_SIZE);
        second_pass.setDepthBufferState(dbs);
        second_pass.setColorBufferState(cbs);
        second_pass.setStencilBufferState(sbs);

        ve = second_pass.getViewEnvironment();
        ve.setProjectionType(ViewEnvironment.ORTHOGRAPHIC_PROJECTION);
        ve.setOrthoParams(-window_size_2, window_size_2,
                          -window_size_2, window_size_2);
        ve.setClipDistance(-10.0, 10.0);


        RenderPass third_pass =
            createStencilPass(common_geom, view_mat,
                              0, -1, WINDOW_SIZE, WINDOW_SIZE - 1);
        third_pass.setDepthBufferState(dbs);
        third_pass.setColorBufferState(cbs);
        third_pass.setStencilBufferState(sbs);

        ve = third_pass.getViewEnvironment();
        ve.setProjectionType(ViewEnvironment.ORTHOGRAPHIC_PROJECTION);
        ve.setOrthoParams(-window_size_2, window_size_2,
                          -window_size_2, window_size_2);
        ve.setClipDistance(-10.0, 10.0);


        RenderPass fourth_pass =
            createStencilPass(common_geom, view_mat,
                              0, 1, WINDOW_SIZE, WINDOW_SIZE + 1);
        fourth_pass.setDepthBufferState(dbs);
        fourth_pass.setColorBufferState(cbs);
        fourth_pass.setStencilBufferState(sbs);

        ve = fourth_pass.getViewEnvironment();
        ve.setProjectionType(ViewEnvironment.ORTHOGRAPHIC_PROJECTION);
        ve.setOrthoParams(-window_size_2, window_size_2,
                          -window_size_2, window_size_2);
        ve.setClipDistance(-10.0, 10.0);


        // 5th pass that cuts out the object in the center
        sbs = new StencilBufferState();
        sbs.setClearBufferState(false);
        sbs.setStencilFunction(StencilBufferState.FUNCTION_ALWAYS);
        sbs.setFunctionReferenceValue(0x0);
        sbs.setFunctionCompareMask(0x0);
        sbs.setStencilFailOperation(StencilBufferState.STENCIL_KEEP);
        sbs.setDepthFailOperation(StencilBufferState.STENCIL_KEEP);
        sbs.setDepthPassOperation(StencilBufferState.STENCIL_REPLACE);

        RenderPass fifth_pass =
            createStencilPass(common_geom, view_mat,
                              0, 0, WINDOW_SIZE, WINDOW_SIZE);
        fifth_pass.setDepthBufferState(dbs);
        fifth_pass.setColorBufferState(cbs);
        fifth_pass.setStencilBufferState(sbs);

        ve = fifth_pass.getViewEnvironment();
        ve.setProjectionType(ViewEnvironment.ORTHOGRAPHIC_PROJECTION);
        ve.setOrthoParams(-window_size_2, window_size_2,
                          -window_size_2, window_size_2);
        ve.setClipDistance(-10.0, 10.0);


        // Now the stencil pass where we draw a single big red quad to
        // show the stencil outline. Where the stencil passes,the quad
        // will draw.
        sbs = new StencilBufferState();
        sbs.setClearBufferState(false);
        sbs.setStencilFunction(StencilBufferState.FUNCTION_EQUAL);
        sbs.setFunctionReferenceValue(0x1);
        sbs.setFunctionCompareMask(0x1);
        sbs.setStencilFailOperation(StencilBufferState.STENCIL_KEEP);
        sbs.setDepthFailOperation(StencilBufferState.STENCIL_KEEP);
        sbs.setDepthPassOperation(StencilBufferState.STENCIL_REPLACE);

        dbs = new DepthBufferState();
        dbs.setClearBufferState(false);
        dbs.enableDepthTest(false);

        cbs = new ColorBufferState();
        cbs.setClearBufferState(false);
        cbs.setColorMask(true, true, true, true);

        float[] coord = { -WINDOW_SIZE, -WINDOW_SIZE, 0,
                           WINDOW_SIZE, -WINDOW_SIZE, 0,
                           WINDOW_SIZE,  WINDOW_SIZE, 0,
                          -WINDOW_SIZE,  WINDOW_SIZE, 0};
        float[] normal = { 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1 };

        QuadArray sil_geom = new QuadArray();
        sil_geom.setValidVertexCount(4);
        sil_geom.setVertices(TriangleArray.COORDINATE_3, coord);
        sil_geom.setNormals(normal);
        sil_geom.setSingleColor(false, new float[] { 1, 0, 0 });

        Shape3D sil_shape = new Shape3D();
        sil_shape.setGeometry(sil_geom);

        Viewpoint sil_vp = new Viewpoint();

        trans = new Vector3d();
        trans.set(0, 0, 7f);

        view_mat = new Matrix4d();
        view_mat.setIdentity();
        view_mat.setTranslation(trans);

        TransformGroup sil_tx = new TransformGroup();
        sil_tx.setTransform(view_mat);
        sil_tx.addChild(sil_vp);

        Group sil_grp = new Group();
        sil_grp.addChild(sil_tx);
        sil_grp.addChild(sil_shape);

        RenderPass silhoutte_pass = new RenderPass();
        silhoutte_pass.setRenderedGeometry(sil_grp);
        silhoutte_pass.setActiveView(sil_vp);

        silhoutte_pass.setDepthBufferState(dbs);
        silhoutte_pass.setColorBufferState(cbs);
        silhoutte_pass.setStencilBufferState(sbs);

        ve = silhoutte_pass.getViewEnvironment();
        ve.setProjectionType(ViewEnvironment.ORTHOGRAPHIC_PROJECTION);
        ve.setOrthoParams(-window_size_2, window_size_2,
                          -window_size_2, window_size_2);
        ve.setClipDistance(-10.0, 10.0);

        MultipassScene scene = new MultipassScene();
        scene.addRenderPass(first_pass);
        scene.addRenderPass(second_pass);
        scene.addRenderPass(third_pass);
        scene.addRenderPass(fourth_pass);
        scene.addRenderPass(fifth_pass);
        scene.addRenderPass(silhoutte_pass);

        ve = scene.getViewEnvironment();
        ve.setProjectionType(ViewEnvironment.ORTHOGRAPHIC_PROJECTION);
        ve.setOrthoParams(0, WINDOW_SIZE, 0, WINDOW_SIZE);
        ve.setClipDistance(-10.0, 10.0);

        // Then the basic layer and viewport at the top:
        MultipassViewport view = new MultipassViewport();
        view.setDimensions(0, 0, WINDOW_SIZE, WINDOW_SIZE);
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

    /**
     * Create the render pass with the window coords as given.
     */
    private RenderPass createStencilPass(SharedGroup commonScene,
                                         Matrix4d vpMat,
                                         int x,
                                         int y,
                                         int width,
                                         int height)
    {
        Viewpoint world_vp = new Viewpoint();

        TransformGroup vp_tx = new TransformGroup();
        vp_tx.addChild(world_vp);
        vp_tx.setTransform(vpMat);

        Group root_group = new Group();
        root_group.addChild(commonScene);
        root_group.addChild(vp_tx);

        RenderPass pass = new RenderPass();
        pass.setRenderedGeometry(commonScene);
        pass.setActiveView(world_vp);

        pass.setViewportDimensions(x, y, width, height);

        return pass;
    }

    public static void main(String[] args)
    {
        StencilEdgeDemo demo = new StencilEdgeDemo();
        demo.setVisible(true);
    }
}
