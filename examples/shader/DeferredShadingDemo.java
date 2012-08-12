
// External imports
import java.awt.*;
import java.awt.event.*;

import java.awt.image.BufferedImage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

import java.util.Random;

import javax.imageio.ImageIO;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import javax.media.opengl.GLCapabilities;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.pipeline.graphics.*;

import org.j3d.aviatrix3d.output.graphics.SimpleAWTSurface;
import org.j3d.aviatrix3d.output.graphics.DebugAWTSurface;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;

import org.j3d.geom.GeometryData;
import org.j3d.geom.BoxGenerator;
import org.j3d.geom.SphereGenerator;
import org.j3d.util.MatrixUtils;
import org.j3d.util.TriangleUtils;

/**
 * Example application demonstrating deferred shading with simple lighting.
 * <p>
 *
 * Based on the tutorial at:
 *
 * @author Justin Couch
 * @version $Revision: 1.22 $
 */
public class DeferredShadingDemo extends Frame
    implements WindowListener
{
    /** App name to register preferences under */
    private static final String APP_NAME = "DeferredShadingDemo";

    /** Render GBuffer depth pass vertex shader file name */
    private static final String SSAO_GBUFFER_VTX_SHADER_FILE =
        "global_illum/ssao_normal_vert.glsl";

    /** Render GBuffer depth Fragment shader file name */
    private static final String SSAO_GBUFFER_FRAG_SHADER_FILE =
        "global_illum/ssao_normal_frag.glsl";

    /** Screen space ambient occlusion pass vertex shader file name */
    private static final String SSAO_VTX_SHADER_FILE =
        "global_illum/deferred_ssao_vert.glsl";

    /** Screen space ambient occlusion fragment shader file name */
    private static final String SSAO_FRAG_SHADER_FILE =
        "global_illum/deferred_ssao_frag.glsl";

    /** Post processing anti alias pass vertex shader file name */
    private static final String AA_VTX_SHADER_FILE =
        "global_illum/deferred_aa_vert.glsl";

    /** Post processing anti alias fragment shader file name */
    private static final String AA_FRAG_SHADER_FILE =
        "global_illum/deferred_aa_frag.glsl";

    /** Post processing bloom fragment shader file name */
    private static final String BLOOM_FRAG_SHADER_FILE =
        "global_illum/deferred_bloom_frag.glsl";

    /** Post processing bloom pass vertex shader file name */
    private static final String BLOOM_VTX_SHADER_FILE =
        "global_illum/deferred_bloom_vert.glsl";

    /** Render pass vertex shader string */
    private static final String MAT_VTX_SHADER_FILE =
        "global_illum/deferred_material_vert.glsl";

    /** Fragment shader file name for the rendering pass */
    private static final String MAT_FRAG_SHADER_FILE =
        "global_illum/deferred_material_frag.glsl";

    /** Render pass vertex shader string */
    private static final String LIGHT_VTX_SHADER_FILE =
        "global_illum/deferred_light_vert.glsl";

    /** Fragment shader file name for the rendering pass */
    private static final String LIGHT_FRAG_SHADER_FILE =
        "global_illum/deferred_light_frag.glsl";

    /** Final stage source combiner vertex shader file name */
    private static final String FINAL_VTX_SHADER_FILE =
        "global_illum/deferred_final_vert.glsl";

    /** Final stage source combiner fragment shader file name */
    private static final String FINAL_FRAG_SHADER_FILE =
        "global_illum/deferred_final_frag.glsl";


    /** Image file holding the local normal map */
    private static final String NORMAL_MAP_FILE =
        "textures/gbuffer_normal.png";

    /** Image file holding the local colour map */
    private static final String COLOUR_MAP_FILE =
        "textures/gbuffer_colour.png";


    /**
     * Image file holding a random colour sample map for SSAO
     */
    private static final String RANDOM_MAP_FILE =
        "textures/ssao_noise.png";

    /** Width and height of the offscreen texture, in pixels */
    private static final int TEXTURE_SIZE = 512;

    /** Half Width and height of the offscreen texture, in pixels */
    private static final int HALF_TEXTURE_SIZE = 256;

    /** Sample radius to make for SSAO */
    private static final float SSAO_SAMPLE_RADIUS = 0.012f;

    /** PI / 4 for rotations */
    private static final float PI_4 = (float)(Math.PI * 0.25f);

    /**
     * Bytes for a 2x2 white RGB image used for when there is
     * no texture image to use on the geometry
     */
    private static final byte[] NO_COLOUR_IMAGE_SRC =
    {
        (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,
        (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,
    };

    /**
     * Bytes for a 2x2 blue RGB image used for when there is
     * no normal map. Blue represents straight out normals from the
     * surface of the object.
     */
    private static final byte[] NO_NORMALS_IMAGE_SRC =
    {
        (byte)0x00, (byte)0x00, (byte)0xFF, (byte)0x00, (byte)0x00, (byte)0xFF,
        (byte)0x00, (byte)0x00, (byte)0xFF, (byte)0x00, (byte)0x00, (byte)0xFF,
    };

    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    /** The view environment created for the main scene */
    private ViewEnvironment mainSceneEnv;

    /** Shared shader program for lighting passes */
    private ShaderProgram lightingShader;

    // Used to print out debugging messages */
    private DeferredShadingAnimator shaderCallback;

    /** Shared main scene geometry */
    private Group sceneGeometry;

    /** Texture quad used for the various render passes */
    private QuadArray baseQuad;

    /**
     * Construct a new shader demo instance.
     */
    public DeferredShadingDemo()
    {
        super("Deferred Shading Demo");

        I18nManager intl_mgr = I18nManager.getManager();
        intl_mgr.setApplication(APP_NAME, "config.i18n.av3dResources");

        setLayout(new BorderLayout());
        addWindowListener(this);

        setupAviatrix();
        setupSceneGraph();

        setSize(640, 480);
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

        GraphicsCullStage culler = new FrustumCullStage();
        culler.setOffscreenCheckEnabled(true);

//        GraphicsSortStage sorter = new StateSortStage();
        GraphicsSortStage sorter = new StateAndTransparencyDepthSortStage();
        surface = new SimpleAWTSurface(caps);
//        surface = new DebugAWTSurface(caps);
//        ((DebugAWTSurface)surface).traceNextFrames(20);
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
        sceneManager.setMinimumFrameInterval(30);

        // Before putting the pipeline into run mode, put the canvas on
        // screen first.
        Component comp = (Component)surface.getSurfaceObject();
        add(comp, BorderLayout.CENTER);
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
     * Setup the basic scene which consists of a quad and a viewpoint
     */
    private void setupSceneGraph()
    {
        shaderCallback = new DeferredShadingAnimator();
        surface.addGraphicsResizeListener(shaderCallback);

        createCommonQuad();

        Vector3f real_view_pos = new Vector3f(0, 0, 20.0f);

        MRTOffscreenTexture2D gbuffer_tex = createGBufferTexture(real_view_pos);

        MRTOffscreenTexture2D deferred_tex =
            createDeferredRenderTexture(gbuffer_tex, real_view_pos);

        MRTOffscreenTexture2D aa_tex =
            createAATexture(gbuffer_tex.getRenderTarget(1), deferred_tex);

        MRTOffscreenTexture2D bloom_tex = createBloomTexture(deferred_tex);

        MRTOffscreenTexture2D ssao_tex =
            createSSAOTexture(real_view_pos);

//        ssao_tex.setMinFilter(Texture.MINFILTER_NICEST);
//        ssao_tex.setMagFilter(Texture.MAGFILTER_NICEST);

        String[] vert_shader_txt = loadShaderFile(FINAL_VTX_SHADER_FILE);
        String[] frag_shader_txt = loadShaderFile(FINAL_FRAG_SHADER_FILE);

        ShaderObject vert_shader = new ShaderObject(true);
        vert_shader.setSourceStrings(vert_shader_txt, 1);
        vert_shader.requestInfoLog();
        vert_shader.compile();

        ShaderObject frag_shader = new ShaderObject(false);
        frag_shader.setSourceStrings(frag_shader_txt, 1);
        frag_shader.requestInfoLog();
        frag_shader.compile();

        ShaderProgram shader_prog = new ShaderProgram();
        shader_prog.addShaderObject(vert_shader);
        shader_prog.addShaderObject(frag_shader);
        shader_prog.requestInfoLog();
        shader_prog.link();

        ShaderArguments shader_args = new ShaderArguments();
        shader_args.setUniformSampler("baseMap", 0);
        shader_args.setUniformSampler("effectMap", 2);
        shader_args.setUniformSampler("ssoaMap", 1);

        GLSLangShader shader = new GLSLangShader();
        shader.setShaderProgram(shader_prog);
        shader.setShaderArguments(shader_args);

        TextureUnit[] tex_unit =
        {
            new TextureUnit(),
            new TextureUnit(),
            new TextureUnit()
        };

//        tex_unit[0].setTexture(gbuffer_tex);
//        tex_unit[0].setTexture(deferred_tex);
        tex_unit[0].setTexture(aa_tex);
        tex_unit[1].setTexture(ssao_tex);
        tex_unit[2].setTexture(bloom_tex);


        Appearance colour_app = new Appearance();
        colour_app.setTextureUnits(tex_unit, 2);
        colour_app.setShader(shader);

        Shape3D colour_shape = new Shape3D();
        colour_shape.setGeometry(baseQuad);
        colour_shape.setAppearance(colour_app);

        Viewpoint vp = new Viewpoint();

        Group root_grp = new Group();
        root_grp.addChild(vp);
        root_grp.addChild(colour_shape);

        SimpleScene main_scene = new SimpleScene();
        main_scene.setRenderedGeometry(root_grp);
        main_scene.setActiveView(vp);

        ViewEnvironment env = main_scene.getViewEnvironment();
        env.setProjectionType(ViewEnvironment.ORTHOGRAPHIC_PROJECTION);
        env.setClipDistance(-1, 1);
        env.setOrthoParams(-1, 1, -1, 1);

        SimpleViewport viewport = new SimpleViewport();
        viewport.setDimensions(0, 0, 640, 480);
        viewport.setScene(main_scene);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(viewport);

        shaderCallback.addShader("Final", vert_shader, frag_shader, shader_prog);
        shaderCallback.addFullWindowResize(viewport);

        sceneManager.setApplicationObserver(shaderCallback);

        Layer[] layers = { layer };
        displayManager.setLayers(layers, 1);
    }

    /**
     * Create the main detered shaded image texture that will be used for
     * post-processing.
     */
    private MRTOffscreenTexture2D createDeferredRenderTexture(MRTOffscreenTexture2D gBuffer,
                                                              Vector3f viewPos)
    {
        // Quads to draw to. Don't reuse the baseQuad here because we want to
        // set separate per-vertex colouring for ambient lighting. Just makes
        // sure we don't accidently effect anything else.
        float[] quad_coords = { -1, -1, -0.5f, 1, -1, -0.5f, 1, 1, -0.5f, -1, 1, -0.5f };
        float[] quad_normals = { 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1 };
        float[][] tex_coord = { { 0, 0,  1, 0,  1, 1,  0, 1 } };

        int[] tex_type = { VertexGeometry.TEXTURE_COORDINATE_2 };
        // Base colour here is the equivalent of the ambient light in the scene.
        // The ambient pass takes the diffuse colour and modulates it by this
        // amount to simulate base ambient lighting.
        float[] ambient_blend = { 0.4f, 0.4f, 0.4f };

        QuadArray real_geom = new QuadArray();
        real_geom.setValidVertexCount(4);
        real_geom.setVertices(TriangleArray.COORDINATE_3, quad_coords);
        real_geom.setNormals(quad_normals);
        real_geom.setTextureCoordinates(tex_type, tex_coord, 1);
        real_geom.setSingleColor(false, ambient_blend);

        TextureAttributes tex_attr = new TextureAttributes();
        tex_attr.setTextureMode(TextureAttributes.MODE_MODULATE);

        TextureUnit[] tex_unit = { new TextureUnit() };
        tex_unit[0].setTexture(gBuffer);
//        tex_unit[0].setTextureAttributes(tex_attr);

        Appearance app_1 = new Appearance();
        app_1.setTextureUnits(tex_unit, 1);

        Shape3D shape_1 = new Shape3D();
        shape_1.setGeometry(real_geom);
        shape_1.setAppearance(app_1);


        SharedNode common_geom = new SharedNode();
        common_geom.setChild(shape_1);

        Viewpoint vp = new Viewpoint();

        Group root_grp = new Group();
        root_grp.addChild(vp);
        root_grp.addChild(common_geom);

        // Set up the first render pass which does ambient lighting
        GeneralBufferState ambient_gbs = new GeneralBufferState();
        ambient_gbs.enableBlending(false);
        ambient_gbs.setSourceBlendFactor(GeneralBufferState.BLEND_ONE);
        ambient_gbs.setDestinationBlendFactor(GeneralBufferState.BLEND_ONE);

        DepthBufferState ambient_dbs = new DepthBufferState();
        ambient_dbs.setClearBufferState(true);
        ambient_dbs.enableDepthTest(false);
        ambient_dbs.enableDepthWrite(false);
        ambient_dbs.setDepthFunction(DepthBufferState.FUNCTION_LESS_OR_EQUAL);

        ColorBufferState ambient_cbs = new ColorBufferState();
        ambient_cbs.setClearBufferState(true);
        ambient_cbs.setClearColor(0, 0, 0, 1);

        RenderPass ambient_pass = new RenderPass();
        ambient_pass.setGeneralBufferState(ambient_gbs);
        ambient_pass.setDepthBufferState(ambient_dbs);
        ambient_pass.setColorBufferState(ambient_cbs);
        ambient_pass.setRenderedGeometry(root_grp);
        ambient_pass.setActiveView(vp);

        ViewEnvironment env = ambient_pass.getViewEnvironment();
        env.setProjectionType(ViewEnvironment.ORTHOGRAPHIC_PROJECTION);
        env.setClipDistance(-1, 1);
        env.setOrthoParams(-1, 1, -1, 1);

        // And now the light passes. One for each light.
        float[] light_pos_1 = { 20, 0, 10 };
        float[] light_pos_2 = { -20, 0, 10 };
        float[] light_colour_1 = { 0, 0, 0.3f };
        float[] light_colour_2 = { 0, 0.3f, 0 };
        float[] light_colour_3 = { 0.8f, 0, 0 };

        // Get the frustum planes for the light pass and then transform them to
        // the world coords.
        Vector4f[] frustum_planes = new Vector4f[6];
        for(int i = 0; i < 6; i++)
            frustum_planes[i] = new Vector4f();

        mainSceneEnv.generateViewFrustumPlanes(frustum_planes);

        MatrixUtils utils = new MatrixUtils();

        Matrix4f view_mat = new Matrix4f();
        view_mat.setIdentity();
//        view_mat.setTranslation(viewPos);

        Matrix4f inv_view = new Matrix4f();

        utils.inverse(view_mat, inv_view);

        for(int i = 0; i < 6; i++)
            inv_view.transform(frustum_planes[i]);

        // Generate the world view coordinates for each of the corners.
        int[][] pixels =
        {
            {0, 0},
            {TEXTURE_SIZE, 0},
            {TEXTURE_SIZE, TEXTURE_SIZE},
            {0, TEXTURE_SIZE}
        };

        float[] view_size = {0, 0, TEXTURE_SIZE, TEXTURE_SIZE};
        float[] proj_mat_arr = new float[16];

        mainSceneEnv.getProjectionMatrix(proj_mat_arr);
        Matrix4f proj_mat = new Matrix4f();
        Matrix4f unproj_mat = new Matrix4f();

        proj_mat.m00 = proj_mat_arr[0];
        proj_mat.m01 = proj_mat_arr[1];
        proj_mat.m02 = proj_mat_arr[2];
        proj_mat.m03 = proj_mat_arr[3];
        proj_mat.m10 = proj_mat_arr[4];
        proj_mat.m11 = proj_mat_arr[5];
        proj_mat.m12 = proj_mat_arr[6];
        proj_mat.m13 = proj_mat_arr[7];
        proj_mat.m20 = proj_mat_arr[8];
        proj_mat.m21 = proj_mat_arr[9];
        proj_mat.m22 = proj_mat_arr[10];
        proj_mat.m23 = proj_mat_arr[11];
        proj_mat.m30 = proj_mat_arr[12];
        proj_mat.m31 = proj_mat_arr[13];
        proj_mat.m32 = proj_mat_arr[14];
        proj_mat.m33 = proj_mat_arr[15];

        Matrix4f model_mat = new Matrix4f();
        model_mat.setIdentity();
        model_mat.setTranslation(viewPos);

        unproj_mat.mul(model_mat, proj_mat);
        utils.inverse(unproj_mat, unproj_mat);

        float[] view_vec = new float[12];

        for(int i = 0; i < 4; i++)
        {
            // Magic number of 10 comes from the deferred shading tutorial
            // that uses 10 here. NFI why.
            float in_x = ((pixels[i][0] - view_size[0]) / view_size[2]) * 2 - 1;
            float in_y = ((pixels[i][1] - view_size[1]) / view_size[3]) * 2 - 1;
            float in_z = 10  * 2 - 1;

            Vector4f window = new Vector4f(in_x, in_y, in_z, 1);

            unproj_mat.transform(window);

            if(window.w == 0)
                System.out.println("bogus w");

            window.w = 1 / window.w;

            Vector3f v = new Vector3f();
            v.x = window.x * window.w;
            v.y = window.y * window.w;
            v.z = window.z * window.w;

            v.sub(viewPos);
            v.normalize();
            model_mat.transform(v);

            view_vec[i * 3] = v.x;
            view_vec[i * 3 + 1] = v.y;
            view_vec[i * 3 + 2] = v.z;
        }

        real_geom.setAttributes(5, 3, view_vec, false);

        RenderPass light_pass_1 = createLightPass(frustum_planes,
                                                  light_pos_1,
                                                  light_colour_1,
                                                  80f,
                                                  common_geom,
                                                  gBuffer);

        RenderPass light_pass_2 = createLightPass(frustum_planes,
                                                  light_pos_2,
                                                  light_colour_2,
                                                  30f,
                                                  common_geom,
                                                  gBuffer);

        MultipassScene scene = new MultipassScene();
        scene.addRenderPass(ambient_pass);

        if(light_pass_1 != null)
            scene.addRenderPass(light_pass_1);

        if(light_pass_2 != null)
            scene.addRenderPass(light_pass_2);

        MultipassViewport viewport = new MultipassViewport();
        viewport.setDimensions(0, 0, TEXTURE_SIZE, TEXTURE_SIZE);
        viewport.setScene(scene);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(viewport);

        Layer[] layers = { layer };

        // The texture requires its own set of capabilities.
        GLCapabilities caps = new GLCapabilities();
        caps.setDoubleBuffered(false);
        caps.setPbufferRenderToTexture(true);
        caps.setPbufferFloatingPointBuffers(true);
        caps.setAlphaBits(8);

        MRTOffscreenTexture2D off_tex =
            new MRTOffscreenTexture2D(caps, TEXTURE_SIZE, TEXTURE_SIZE, 1);

        off_tex.setClearColor(1, 0, 0, 1);
        off_tex.setRepaintRequired(true);
        off_tex.setLayers(layers, 1);

        shaderCallback.addFullWindowResize(off_tex, viewport);

        return off_tex;
    }

    /**
     * Creates the antialias pass output texture from the input texture
     */
    private MRTOffscreenTexture2D createAATexture(Texture normalSource,
                                                  Texture colourSource)
    {
        TextureAttributes tex_attr = new TextureAttributes();
        tex_attr.setTextureMode(TextureAttributes.MODE_REPLACE);

        TextureUnit[] tex_unit = { new TextureUnit(), new TextureUnit() };
        tex_unit[0].setTexture(colourSource);
        tex_unit[0].setTextureAttributes(tex_attr);
        tex_unit[1].setTexture(normalSource);
        tex_unit[1].setTextureAttributes(tex_attr);

        String[] vert_shader_txt = loadShaderFile(AA_VTX_SHADER_FILE);
        String[] frag_shader_txt = loadShaderFile(AA_FRAG_SHADER_FILE);

        ShaderObject vert_shader = new ShaderObject(true);
        vert_shader.setSourceStrings(vert_shader_txt, 1);
        vert_shader.requestInfoLog();
        vert_shader.compile();

        ShaderObject frag_shader = new ShaderObject(false);
        frag_shader.setSourceStrings(frag_shader_txt, 1);
        frag_shader.requestInfoLog();
        frag_shader.compile();

        ShaderProgram shader_prog = new ShaderProgram();
        shader_prog.addShaderObject(vert_shader);
        shader_prog.addShaderObject(frag_shader);
        shader_prog.requestInfoLog();
        shader_prog.link();

        float[] threshold = { 1.0f };
        float[] tex_size = { TEXTURE_SIZE, TEXTURE_SIZE };

        ShaderArguments shader_args = new ShaderArguments();
        shader_args.setUniform("weight", 1, threshold, 1);
        shader_args.setUniform("texSize", 2, tex_size, 1);
        shader_args.setUniformSampler("colourMap", 0);
        shader_args.setUniformSampler("normalMap", 1);

        GLSLangShader shader = new GLSLangShader();
        shader.setShaderProgram(shader_prog);
        shader.setShaderArguments(shader_args);

        Appearance app = new Appearance();
        app.setTextureUnits(tex_unit, 2);
        app.setShader(shader);

        Shape3D shape_1 = new Shape3D();
        shape_1.setGeometry(baseQuad);
        shape_1.setAppearance(app);

        Viewpoint vp = new Viewpoint();

        Group root_grp = new Group();
        root_grp.addChild(vp);
        root_grp.addChild(shape_1);

        SimpleScene main_scene = new SimpleScene();
        main_scene.setRenderedGeometry(root_grp);
        main_scene.setActiveView(vp);

        ViewEnvironment env = main_scene.getViewEnvironment();
        env.setProjectionType(ViewEnvironment.ORTHOGRAPHIC_PROJECTION);
        env.setClipDistance(-1, 1);
        env.setOrthoParams(-1, 1, -1, 1);

        SimpleViewport viewport = new SimpleViewport();
        viewport.setDimensions(0, 0, TEXTURE_SIZE, TEXTURE_SIZE);
        viewport.setScene(main_scene);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(viewport);

        Layer[] layers = { layer };

        // The texture requires its own set of capabilities.
        GLCapabilities caps = new GLCapabilities();
        caps.setDoubleBuffered(false);
        caps.setPbufferRenderToTexture(true);
        caps.setPbufferFloatingPointBuffers(true);

        MRTOffscreenTexture2D off_tex =
            new MRTOffscreenTexture2D(caps, TEXTURE_SIZE, TEXTURE_SIZE, 1);

        off_tex.setClearColor(0, 0, 0, 1);
        off_tex.setRepaintRequired(true);
        off_tex.setLayers(layers, 1);

        shaderCallback.addShader("AA Pass", vert_shader, frag_shader, shader_prog);
        shaderCallback.addFullWindowResize(off_tex, viewport);
        shaderCallback.addFullWindowResize(shader_args);

        return off_tex;
    }

    /**
     * Creates the bloom passes output texture from the input texture
     */
    private MRTOffscreenTexture2D createBloomTexture(Texture colourSource)
    {
        TextureAttributes tex_attr = new TextureAttributes();
        tex_attr.setTextureMode(TextureAttributes.MODE_REPLACE);

        TextureUnit[] tex_unit = { new TextureUnit() };
        tex_unit[0].setTexture(colourSource);
        tex_unit[0].setTextureAttributes(tex_attr);

        String[] vert_shader_txt = loadShaderFile(BLOOM_VTX_SHADER_FILE);
        String[] frag_shader_txt = loadShaderFile(BLOOM_FRAG_SHADER_FILE);

        ShaderObject vert_shader = new ShaderObject(true);
        vert_shader.setSourceStrings(vert_shader_txt, 1);
        vert_shader.requestInfoLog();
        vert_shader.compile();

        ShaderObject frag_shader = new ShaderObject(false);
        frag_shader.setSourceStrings(frag_shader_txt, 1);
        frag_shader.requestInfoLog();
        frag_shader.compile();

        ShaderProgram shader_prog = new ShaderProgram();
        shader_prog.addShaderObject(vert_shader);
        shader_prog.addShaderObject(frag_shader);
        shader_prog.requestInfoLog();
        shader_prog.link();

        float[] h_tex_size = { 1.0f / TEXTURE_SIZE, 0 };
        float[] v_tex_size = { 0, 1.0f / TEXTURE_SIZE };

        ShaderArguments h_shader_args = new ShaderArguments();
        h_shader_args.setUniform("texSize", 2, h_tex_size, 1);
        h_shader_args.setUniformSampler("colourMap", 0);

        GLSLangShader h_shader = new GLSLangShader();
        h_shader.setShaderProgram(shader_prog);
        h_shader.setShaderArguments(h_shader_args);

        Appearance h_app = new Appearance();
        h_app.setTextureUnits(tex_unit, 1);
        h_app.setShader(h_shader);

        Shape3D h_shape = new Shape3D();
        h_shape.setGeometry(baseQuad);
        h_shape.setAppearance(h_app);

        Viewpoint vp = new Viewpoint();

        Group root_grp = new Group();
        root_grp.addChild(vp);
        root_grp.addChild(h_shape);

        SimpleScene main_scene = new SimpleScene();
        main_scene.setRenderedGeometry(root_grp);
        main_scene.setActiveView(vp);

        ViewEnvironment env = main_scene.getViewEnvironment();
        env.setProjectionType(ViewEnvironment.ORTHOGRAPHIC_PROJECTION);
        env.setClipDistance(-1, 1);
        env.setOrthoParams(-1, 1, -1, 1);

        SimpleViewport viewport = new SimpleViewport();
        viewport.setDimensions(0, 0, HALF_TEXTURE_SIZE, HALF_TEXTURE_SIZE);
        viewport.setScene(main_scene);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(viewport);

        Layer[] layers = { layer };

        // The texture requires its own set of capabilities.
        GLCapabilities caps = new GLCapabilities();
        caps.setDoubleBuffered(false);
        caps.setPbufferRenderToTexture(true);
        caps.setPbufferFloatingPointBuffers(true);

        MRTOffscreenTexture2D horizontal_bloom_tex =
            new MRTOffscreenTexture2D(caps, HALF_TEXTURE_SIZE, HALF_TEXTURE_SIZE, 1);

        horizontal_bloom_tex.setClearColor(0, 0, 0, 1);
        horizontal_bloom_tex.setRepaintRequired(true);
        horizontal_bloom_tex.setLayers(layers, 1);

        shaderCallback.addHalfWindowResize(horizontal_bloom_tex, viewport);

        // Now create the vertical bloom texture by using the horizontal as the
        // input source. Everything else is almost identical

        ShaderArguments v_shader_args = new ShaderArguments();
        v_shader_args.setUniform("texSize", 2, v_tex_size, 1);
        v_shader_args.setUniformSampler("colourMap", 0);

        GLSLangShader v_shader = new GLSLangShader();
        v_shader.setShaderProgram(shader_prog);
        v_shader.setShaderArguments(v_shader_args);

        TextureUnit[] v_tex_unit = { new TextureUnit() };
        v_tex_unit[0].setTexture(horizontal_bloom_tex);
        v_tex_unit[0].setTextureAttributes(tex_attr);

        Appearance v_app = new Appearance();
        v_app.setTextureUnits(v_tex_unit, 1);
        v_app.setShader(v_shader);

        Shape3D v_shape = new Shape3D();
        v_shape.setGeometry(baseQuad);
        v_shape.setAppearance(v_app);

        vp = new Viewpoint();

        root_grp = new Group();
        root_grp.addChild(vp);
        root_grp.addChild(v_shape);

        main_scene = new SimpleScene();
        main_scene.setRenderedGeometry(root_grp);
        main_scene.setActiveView(vp);

        env = main_scene.getViewEnvironment();
        env.setProjectionType(ViewEnvironment.ORTHOGRAPHIC_PROJECTION);
        env.setClipDistance(-1, 1);
        env.setOrthoParams(-1, 1, -1, 1);

        viewport = new SimpleViewport();
        viewport.setDimensions(0, 0, HALF_TEXTURE_SIZE, HALF_TEXTURE_SIZE);
        viewport.setScene(main_scene);

        layer = new SimpleLayer();
        layer.setViewport(viewport);

        layers = new Layer[1];
        layers[0] = layer;

        MRTOffscreenTexture2D vertical_bloom_tex =
            new MRTOffscreenTexture2D(caps, HALF_TEXTURE_SIZE, HALF_TEXTURE_SIZE, 1);

        vertical_bloom_tex.setClearColor(0, 0, 0, 1);
        vertical_bloom_tex.setRepaintRequired(true);
        vertical_bloom_tex.setLayers(layers, 1);

        shaderCallback.addShader("Bloom", vert_shader, frag_shader, shader_prog);
        shaderCallback.addHalfWindowResize(vertical_bloom_tex, viewport);
//shaderCallback.addFullWindowResize(shader_args);

        return vertical_bloom_tex;
    }

    /**
     * Create the contents of the offscreen texture that is being rendered
     */
    private MRTOffscreenTexture2D createGBufferTexture(Vector3f viewPos)
    {
        Viewpoint vp = new Viewpoint();

        Matrix4f view_mat = new Matrix4f();
        view_mat.setIdentity();
        view_mat.setTranslation(viewPos);

        TransformGroup vp_tx = new TransformGroup();
        vp_tx.setTransform(view_mat);
        vp_tx.addChild(vp);

        // Create the gbuffer shader
        String[] vert_shader_txt = loadShaderFile(MAT_VTX_SHADER_FILE);
        String[] frag_shader_txt = loadShaderFile(MAT_FRAG_SHADER_FILE);

        ShaderObject vert_shader = new ShaderObject(true);
        vert_shader.setSourceStrings(vert_shader_txt, 1);
        vert_shader.requestInfoLog();
        vert_shader.compile();

        ShaderObject frag_shader = new ShaderObject(false);
        frag_shader.setSourceStrings(frag_shader_txt, 1);
        frag_shader.requestInfoLog();
        frag_shader.compile();

        ShaderProgram shader_prog = new ShaderProgram();
        shader_prog.addShaderObject(vert_shader);
        shader_prog.addShaderObject(frag_shader);
        shader_prog.bindAttributeName("tangent", 5);
        shader_prog.requestInfoLog();
        shader_prog.link();

        float[] tile = { 1.0f };

        ShaderArguments shader_args = new ShaderArguments();
        shader_args.setUniform("tileScale", 1, tile, 1);
        shader_args.setUniformSampler("colourMap", 0);
        shader_args.setUniformSampler("normalMap", 1);

        GLSLangShader shader = new GLSLangShader();
        shader.setShaderProgram(shader_prog);
        shader.setShaderArguments(shader_args);

        Appearance global_app = new Appearance();
        global_app.setShader(shader);

        AppearanceOverride app_ovr = new AppearanceOverride();
        app_ovr.setEnabled(true);
        app_ovr.setLocalAppearanceOnly(false);
        app_ovr.setAppearance(global_app);

        Group geom_group = createTestScene();

        Group root_grp = new Group();
        root_grp.addChild(app_ovr);
        root_grp.addChild(vp_tx);
        root_grp.addChild(geom_group);

        SimpleScene main_scene = new SimpleScene();
        main_scene.setRenderedGeometry(root_grp);
        main_scene.setActiveView(vp);

        mainSceneEnv = main_scene.getViewEnvironment();
        mainSceneEnv.setFarClipDistance(500);

/*
 Use if no depth texture support
float near_clip = (float)mainSceneEnv.getNearClipDistance();
float far_clip = (float)mainSceneEnv.getFarClipDistance();
float[] d_planes =
{
    -far_clip / (far_clip - near_clip),
    -far_clip * near_clip / (far_clip - near_clip)
};

shader_args.setUniform("planes", 2, d_planes, 1);
*/

        // Then the basic layer and viewport at the top:
        SimpleViewport viewport = new SimpleViewport();
        viewport.setDimensions(0, 0, TEXTURE_SIZE, TEXTURE_SIZE);
        viewport.setScene(main_scene);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(viewport);

        Layer[] layers = { layer };

        // The texture requires its own set of capabilities.
        GLCapabilities caps = new GLCapabilities();
        caps.setDoubleBuffered(false);
        caps.setPbufferRenderToTexture(true);
        caps.setPbufferFloatingPointBuffers(true);
        caps.setDepthBits(24);
        caps.setAlphaBits(8);

        MRTOffscreenTexture2D off_tex =
            new MRTOffscreenTexture2D(caps, TEXTURE_SIZE, TEXTURE_SIZE, 3, true);

// Use when no depth texture support
//            new MRTOffscreenTexture2D(caps, TEXTURE_SIZE, TEXTURE_SIZE, 4, false);

        off_tex.setClearColor(0, 0, 0, 1);
        off_tex.setRepaintRequired(true);
        off_tex.setLayers(layers, 1);

        shaderCallback.addShader("Main GBuffer",
                                 vert_shader,
                                 frag_shader,
                                 shader_prog);
        shaderCallback.addViewTransform(vp_tx);
        shaderCallback.addFullWindowResize(off_tex, viewport);

        return off_tex;
    }

    /**
     * Creates the antialias pass output texture from the input texture
     */
    private MRTOffscreenTexture2D createSSAOTexture(Vector3f viewPos)
    {
        MRTOffscreenTexture2D gbuffer_tex =
            createSSAOGBufferTexture(viewPos);

        TextureComponent2D random_comp = loadTextureImage(RANDOM_MAP_FILE);

        TextureUnit[] tex_unit =
        {
            new TextureUnit(),
            new TextureUnit(),
            new TextureUnit()
        };

        tex_unit[0].setTexture(gbuffer_tex);

        if(random_comp != null)
        {
            Texture2D random_tex = new Texture2D(Texture2D.FORMAT_RGB, random_comp);
            random_tex.setBoundaryModeS(Texture.BM_WRAP);
            random_tex.setBoundaryModeT(Texture.BM_WRAP);

            tex_unit[1].setTexture(random_tex);
        }

        tex_unit[2].setTexture(gbuffer_tex.getDepthRenderTarget());

        String[] vert_shader_txt = loadShaderFile(SSAO_VTX_SHADER_FILE);
        String[] frag_shader_txt = loadShaderFile(SSAO_FRAG_SHADER_FILE);

        ShaderObject vert_shader = new ShaderObject(true);
        vert_shader.setSourceStrings(vert_shader_txt, 1);
        vert_shader.requestInfoLog();
        vert_shader.compile();

        ShaderObject frag_shader = new ShaderObject(false);
        frag_shader.setSourceStrings(frag_shader_txt, 1);
        frag_shader.requestInfoLog();
        frag_shader.compile();

        ShaderProgram shader_prog = new ShaderProgram();
        shader_prog.addShaderObject(vert_shader);
        shader_prog.addShaderObject(frag_shader);
        shader_prog.requestInfoLog();
        shader_prog.link();

        float[] strength = { 0.07f };
        float[] global_strength = { 1.08f };
        float[] falloff = { 0.000002f };
        float[] offset = { 18.5f };
        float[] sample_radius = { SSAO_SAMPLE_RADIUS };

        ShaderArguments shader_args = new ShaderArguments();
        shader_args.setUniform("globalStrength", 1, global_strength, 1);
        shader_args.setUniform("strength", 1, strength, 1);
        shader_args.setUniform("offset", 1, offset, 1);
        shader_args.setUniform("falloff", 1, falloff, 1);
        shader_args.setUniform("sampleRadius", 1, sample_radius, 1);
        shader_args.setUniformSampler("normalMap", 0);
        shader_args.setUniformSampler("randomNoiseMap", 1);
        shader_args.setUniformSampler("depthMap", 2);

        GLSLangShader shader = new GLSLangShader();
        shader.setShaderProgram(shader_prog);
        shader.setShaderArguments(shader_args);

        Appearance app = new Appearance();
        app.setTextureUnits(tex_unit, 3);
        app.setShader(shader);

        Shape3D shape = new Shape3D();
        shape.setGeometry(baseQuad);
        shape.setAppearance(app);

        Viewpoint vp = new Viewpoint();

        Group root_grp = new Group();
        root_grp.addChild(vp);
        root_grp.addChild(shape);

        SimpleScene main_scene = new SimpleScene();
        main_scene.setRenderedGeometry(root_grp);
        main_scene.setActiveView(vp);

        ViewEnvironment env = main_scene.getViewEnvironment();
        env.setProjectionType(ViewEnvironment.ORTHOGRAPHIC_PROJECTION);
        env.setClipDistance(-1, 1);
        env.setOrthoParams(-1, 1, -1, 1);

        SimpleViewport viewport = new SimpleViewport();
        viewport.setDimensions(0, 0, HALF_TEXTURE_SIZE, HALF_TEXTURE_SIZE);
        viewport.setScene(main_scene);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(viewport);

        Layer[] layers = { layer };

        // The texture requires its own set of capabilities.
        GLCapabilities caps = new GLCapabilities();
        caps.setDoubleBuffered(false);
        caps.setPbufferRenderToTexture(true);
        caps.setPbufferFloatingPointBuffers(true);

        MRTOffscreenTexture2D off_tex =
            new MRTOffscreenTexture2D(caps, HALF_TEXTURE_SIZE, HALF_TEXTURE_SIZE, 1);

        off_tex.setClearColor(0, 0, 0, 1);
        off_tex.setRepaintRequired(true);
        off_tex.setLayers(layers, 1);

        shaderCallback.addShader("SSAO", vert_shader, frag_shader, shader_prog);
        shaderCallback.addHalfWindowResize(off_tex, viewport);

        return off_tex;
    }

    /**
     * Create the contents of the offscreen texture that is being rendered
     */
    private MRTOffscreenTexture2D createSSAOGBufferTexture(Vector3f viewPos)
    {
        Viewpoint vp = new Viewpoint();

        Matrix4f view_mat = new Matrix4f();
        view_mat.setIdentity();
        view_mat.setTranslation(viewPos);

        TransformGroup vp_tx = new TransformGroup();
        vp_tx.setTransform(view_mat);
        vp_tx.addChild(vp);

        // Create the gbuffer shader
        String[] vert_shader_txt = loadShaderFile(SSAO_GBUFFER_VTX_SHADER_FILE);
        String[] frag_shader_txt = loadShaderFile(SSAO_GBUFFER_FRAG_SHADER_FILE);

        ShaderObject vert_shader = new ShaderObject(true);
        vert_shader.setSourceStrings(vert_shader_txt, 1);
        vert_shader.requestInfoLog();
        vert_shader.compile();

        ShaderObject frag_shader = new ShaderObject(false);
        frag_shader.setSourceStrings(frag_shader_txt, 1);
        frag_shader.requestInfoLog();
        frag_shader.compile();

        ShaderProgram shader_prog = new ShaderProgram();
        shader_prog.addShaderObject(vert_shader);
        shader_prog.addShaderObject(frag_shader);
        shader_prog.requestInfoLog();
        shader_prog.link();

        float[] depth_scale = { 1.0f / 3000.0f };

        ShaderArguments shader_args = new ShaderArguments();
        shader_args.setUniform("depthScale", 1, depth_scale, 1);

        GLSLangShader shader = new GLSLangShader();
        shader.setShaderProgram(shader_prog);
        shader.setShaderArguments(shader_args);

        Appearance app = new Appearance();
        app.setShader(shader);

        AppearanceOverride app_ovr = new AppearanceOverride();
        app_ovr.setEnabled(true);
        app_ovr.setAppearance(app);

        Group geom_group = createTestScene();

        Group root_grp = new Group();
        root_grp.addChild(app_ovr);
        root_grp.addChild(vp_tx);
        root_grp.addChild(geom_group);

        SimpleScene main_scene = new SimpleScene();
        main_scene.setRenderedGeometry(root_grp);
        main_scene.setActiveView(vp);

        mainSceneEnv = main_scene.getViewEnvironment();
        mainSceneEnv.setFarClipDistance(500);

        // Then the basic layer and viewport at the top:
        SimpleViewport viewport = new SimpleViewport();
        viewport.setDimensions(0, 0, HALF_TEXTURE_SIZE, HALF_TEXTURE_SIZE);
        viewport.setScene(main_scene);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(viewport);

        Layer[] layers = { layer };

        // The texture requires its own set of capabilities.
        GLCapabilities caps = new GLCapabilities();
        caps.setDoubleBuffered(false);
        caps.setPbufferRenderToTexture(true);
        caps.setPbufferFloatingPointBuffers(true);
        caps.setDepthBits(24);
        caps.setAlphaBits(8);

        MRTOffscreenTexture2D off_tex =
            new MRTOffscreenTexture2D(caps,
                                      true,
                                      HALF_TEXTURE_SIZE,
                                      HALF_TEXTURE_SIZE,
                                      1,
                                      true);

        off_tex.setClearColor(0, 0, 0, 1);
        off_tex.setRepaintRequired(true);
        off_tex.setLayers(layers, 1);

        shaderCallback.addShader("SSAO GBuffer",
                                 vert_shader,
                                 frag_shader,
                                 shader_prog);

        shaderCallback.addViewTransform(vp_tx);
        shaderCallback.addHalfWindowResize(off_tex, viewport);

        return off_tex;
    }

    /**
     * Create a render pass for a single light at the given position.
     *
     * @param lightPos The position of the light
     * @param radius The light radius to use
     * @return The render pass representing this light or null if there is
     *    no effect to be rendered for this combo
     */
    private RenderPass createLightPass(Vector4f[] frustumPlanes,
                                       float[] lightPos,
                                       float[] lightColour,
                                       float radius,
                                       SharedNode sceneGeom,
                                       MRTOffscreenTexture2D gbuffer)
    {
        if(lightingShader == null)
        {
            // Create the gbuffer shader
            String[] vert_shader_txt = loadShaderFile(LIGHT_VTX_SHADER_FILE);
            String[] frag_shader_txt = loadShaderFile(LIGHT_FRAG_SHADER_FILE);

            ShaderObject vert_shader = new ShaderObject(true);
            vert_shader.setSourceStrings(vert_shader_txt, 1);
            vert_shader.requestInfoLog();
            vert_shader.compile();

            ShaderObject frag_shader = new ShaderObject(false);
            frag_shader.setSourceStrings(frag_shader_txt, 1);
            frag_shader.requestInfoLog();
            frag_shader.compile();

            lightingShader = new ShaderProgram();
            lightingShader.addShaderObject(vert_shader);
            lightingShader.addShaderObject(frag_shader);
            lightingShader.bindAttributeName("viewCoords", 5);
            lightingShader.requestInfoLog();
            lightingShader.link();

            shaderCallback.addShader("Light Pass",
                                     vert_shader,
                                     frag_shader,
                                     lightingShader);
        }


        // Global appearance overrride for the light rendering pass
        float near_clip = (float)mainSceneEnv.getNearClipDistance();
        float far_clip = (float)mainSceneEnv.getFarClipDistance();
        float[] l_rad = { radius };
        float[] d_planes =
        {
            -far_clip / (far_clip - near_clip),
            -far_clip * near_clip / (far_clip - near_clip)
        };

        ShaderArguments shader_args = new ShaderArguments();
        shader_args.setUniform("lightPos", 3, lightPos, 1);
        shader_args.setUniform("lightRadius", 1, l_rad, 1);
        shader_args.setUniform("lightColor", 3, lightColour, 1);
        shader_args.setUniform("planes", 2, d_planes, 1);

        shader_args.setUniformSampler("diffuseMap", 0);
        shader_args.setUniformSampler("normalMap", 1);
        shader_args.setUniformSampler("specularMap", 2);
        shader_args.setUniformSampler("depthMap", 3);

        GLSLangShader shader = new GLSLangShader();
        shader.setShaderProgram(lightingShader);
        shader.setShaderArguments(shader_args);

        TextureUnit[] tex_unit = new TextureUnit[4];
        tex_unit[0] = new TextureUnit();
        tex_unit[0].setTexture(gbuffer);

        tex_unit[1] = new TextureUnit();
        tex_unit[1].setTexture(gbuffer.getRenderTarget(1));

        tex_unit[2] = new TextureUnit();
        tex_unit[2].setTexture(gbuffer.getRenderTarget(2));

        tex_unit[3] = new TextureUnit();
        tex_unit[3].setTexture(gbuffer.getDepthRenderTarget());

// Use when no depth texture support
//        tex_unit[3].setTexture(gbuffer.getRenderTarget(3));

        Appearance global_app = new Appearance();
        global_app.setShader(shader);
        global_app.setTextureUnits(tex_unit, 4);

        AppearanceOverride app_ovr = new AppearanceOverride();
        app_ovr.setEnabled(true);
        app_ovr.setAppearance(global_app);

        Viewpoint vp = new Viewpoint();

        Group root_grp = new Group();
        root_grp.addChild(vp);
        root_grp.addChild(sceneGeom);
        root_grp.addChild(app_ovr);


        for(int i = 0; i < 5; i++)
        {
            // calc distance of light from view planes. If the radius of the
            // light is completely outside the viewplane, no point doing this
            // rendering pass.
            float d = (lightPos[0] * frustumPlanes[i].x +
                      lightPos[1] * frustumPlanes[i].y +
                      lightPos[2] * frustumPlanes[i].z) -
                      frustumPlanes[i].w;


            if(d < -radius)
                return null;
        }


        // transform light position from global to view space. Need to make the
        // light a 4D vector so that the position is multiplied through
        Vector4f view_light_pos =
            new Vector4f(lightPos[0], lightPos[1], lightPos[2], 1);

// Need to do something about this
//        viewPos.transform(view_light_pos);

        int[] scissor = new int[4];

        int n = calcLightScissor(view_light_pos,
                                 radius,
                                 TEXTURE_SIZE,
                                 TEXTURE_SIZE,
                                 scissor);

        if(n == 0)
            return null;

        GeneralBufferState gbs = new GeneralBufferState();
        gbs.enableBlending(true);
        gbs.setSourceBlendFactor(GeneralBufferState.BLEND_ONE);
        gbs.setDestinationBlendFactor(GeneralBufferState.BLEND_ONE);

        DepthBufferState dbs = new DepthBufferState();
        dbs.setClearBufferState(false);
        dbs.enableDepthTest(false);
        dbs.enableDepthWrite(false);
        dbs.setDepthFunction(DepthBufferState.FUNCTION_LESS_OR_EQUAL);

        ColorBufferState cbs = new ColorBufferState();
        cbs.setClearBufferState(false);

        RenderPass light_pass = new RenderPass();
        light_pass.setGeneralBufferState(gbs);
        light_pass.setDepthBufferState(dbs);
        light_pass.setColorBufferState(cbs);
        light_pass.setRenderedGeometry(root_grp);
        light_pass.setActiveView(vp);

        ViewEnvironment env = light_pass.getViewEnvironment();
        env.setProjectionType(ViewEnvironment.ORTHOGRAPHIC_PROJECTION);
        env.setClipDistance(-1, 1);
        env.setOrthoParams(-1, 1, -1, 1);
        env.setScissorDimensions(scissor[0], scissor[1], scissor[2], scissor[3]);

        return light_pass;
    }

    /**
     * Calculate the light scissor space.
     *
     * @param lightPos The light's position in world space
     * @param radius The radius of the light's effect
     * @param sx The width size of the screen in pixels
     * @param sy The height size of the screen in pixels
     * @param scissor The bounds of the scissor to copy in to this
     */
    private int calcLightScissor(Vector4f lightPos,
                                 float radius,
                                 int sx,
                                 int sy,
                                 int[] scissor)
    {
        int[] rect = { 0, 0, sx, sy };
        float r2 = radius * radius;

        Vector3f l2 = new Vector3f();
        l2.x = lightPos.x * lightPos.x;
        l2.y = lightPos.y * lightPos.y;
        l2.z = lightPos.z * lightPos.z;

        float e1 = 1.2f;
        float e2 = 1.2f * (float)mainSceneEnv.getAspectRatio();

        float d = r2 * l2.x - (l2.x + l2.z) * (r2 - l2.z);

        if(d >= 0)
        {
            d = (float)Math.sqrt(d);

            float nx1 = (radius * lightPos.x + d) / (l2.x + l2.z);
            float nx2 = (radius * lightPos.x - d) / (l2.x + l2.z);

            float nz1 = (radius - nx1 * lightPos.x) / lightPos.z;
            float nz2 = (radius - nx2 * lightPos.x) / lightPos.z;

            float pz1 = (l2.x + l2.z - r2) /
                        (lightPos.z - (nz1 / nx1)  * lightPos.x);
            float pz2 = (l2.x + l2.z - r2) /
                        (lightPos.z - (nz2 / nx2)  * lightPos.x);

            if(pz1 < 0)
            {
                float fx = nz1 * e1 / nx1;
                int ix = (int)((fx + 1) * sx * 0.5f);

                float px = -pz1 * nz1 / nx1;

                if(px < lightPos.x)
                {
                    if(rect[0] < ix)
                        rect[0] = ix;
                }
                else
                {
                    if(rect[2] > ix)
                        rect[2] = ix;
               }
            }

            if(pz2 < 0)
            {
                float fx = nz2 * e1 / nx2;
                int ix = (int)((fx + 1) * sx * 0.5f);

                float px = -pz2 * nz2 / nx2;

                if(px < lightPos.x)
                {
                    if(rect[0] < ix)
                        rect[0] = ix;
                }
                else
                {
                    if(rect[2] > ix)
                        rect[2] = ix;
               }
            }
        }

        d = r2 * l2.y - (l2.y + l2.z) * (r2 - l2.z);

        if(d >= 0)
        {
            d = (float)Math.sqrt(d);

            float ny1 = (radius * lightPos.y + d) / (l2.y + l2.z);
            float ny2 = (radius * lightPos.y - d) / (l2.y + l2.z);

            float nz1 = (radius - ny1 * lightPos.y) / lightPos.z;
            float nz2 = (radius - ny2 * lightPos.y) / lightPos.z;

            float pz1 = (l2.y + l2.z - r2) /
                        (lightPos.z - (nz1 / ny1)  * lightPos.y);
            float pz2 = (l2.y + l2.z - r2) /
                        (lightPos.z - (nz2 / ny2)  * lightPos.y);

            if(pz1 < 0)
            {
                float fy = nz1 * e2 / ny1;
                int iy = (int)((fy + 1) * sy * 0.5f);

                float py = -pz1 * nz1 / ny1;

                if(py < lightPos.y)
                {
                    if(rect[1] < iy)
                        rect[1] = iy;
                }
                else
                {
                    if(rect[3] > iy)
                        rect[3] = iy;
               }
            }

            if(pz2 < 0)
            {
                float fy = nz2 * e2 / ny2;
                int iy = (int)((fy + 1) * sy * 0.5f);

                float py = -pz2 * nz2 / ny2;

                if(py < lightPos.y)
                {
                    if(rect[1] < iy)
                        rect[1] = iy;
                }
                else
                {
                    if(rect[3] > iy)
                        rect[3] = iy;
               }
            }
        }

        int n = (rect[2]  - rect[0]) * (rect[3] - rect[1]);

        if(n <= 0)
            return 0;

        scissor[0] = rect[0];
        scissor[1] = rect[1];
        scissor[2] = rect[2];
        scissor[3] = rect[3];

        return n;
    }

    /**
     * Create the shared quad definition that most geometry uses
     */
    private void createCommonQuad()
    {
        float[] quad_coords = { -1, -1, -0.5f, 1, -1, -0.5f, 1, 1, -0.5f, -1, 1, -0.5f };
        float[] quad_normals = { 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1 };
        float[][] tex_coord = { { 0, 0,  1, 0,  1, 1,  0, 1 } };

        int[] tex_type = { VertexGeometry.TEXTURE_COORDINATE_2 };

        QuadArray real_geom = new QuadArray();
        real_geom.setValidVertexCount(4);
        real_geom.setVertices(TriangleArray.COORDINATE_3, quad_coords);
        real_geom.setNormals(quad_normals);
        real_geom.setTextureCoordinates(tex_type, tex_coord, 1);

        baseQuad = real_geom;
    }

    /**
     * Create the base test scene geometry.
     */
    private Group createTestScene()
    {
        if(sceneGeometry != null)
            return sceneGeometry;

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.INDEXED_TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA |
                                  GeometryData.TEXTURE_2D_DATA;

        BoxGenerator b_generator = new BoxGenerator(2.5f, 2.5f, 2.5f);
        b_generator.generate(data);

        float[][] test_tex_coord = { data.textureCoordinates };
        int[] tex_type = { VertexGeometry.TEXTURE_COORDINATE_2 };

        // Generate tangent information
        float[] test_tangents = new float[data.vertexCount * 4];

        TriangleUtils.createTangents(data.indexesCount / 3,
                                     data.indexes,
                                     data.coordinates,
                                     data.normals,
                                     data.textureCoordinates,
                                     test_tangents);

        IndexedTriangleArray test_geom = new IndexedTriangleArray();
        test_geom.setValidVertexCount(data.vertexCount);
        test_geom.setVertices(TriangleArray.COORDINATE_3, data.coordinates);
        test_geom.setIndices(data.indexes, data.indexesCount);
        test_geom.setNormals(data.normals);
        test_geom.setTextureCoordinates(tex_type, test_tex_coord, 1);
        test_geom.setAttributes(5, 4, test_tangents, false);

        // 5 flat planes on the inside of the box for some reference walls
        // Everything repeated because each vertex has different normals
        // depending on the direction the wall faces
        float[] wall_coords = {
            -7, -7,  7,  -7, -7, -7, -7,  7, -7,  -7,  7,  7,
             7, -7, -7,   7, -7,  7,  7,  7,  7,   7,  7, -7,
            -7, -7, -7,   7, -7, -7,  7,  7, -7,  -7,  7, -7,
            -7, -7, -7,  -7, -7,  7,  7, -7,  7,   7, -7, -7,
        };

        float[] wall_normals = {
             1, 0, 0,  1, 0, 0,  1, 0, 0,  1, 0, 0,
            -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0,
             0, 0, 1,  0, 0, 1,  0, 0, 1,  0, 0, 1,
             0, 1, 0,  0, 1, 0,  0, 1, 0,  0, 1, 0,
        };

        int[] wall_indices = {
             0, 1, 2, 3,  4, 5, 6, 7,  8, 9, 10, 11, 12, 13, 14, 15
        };

        float[][] wall_tex_coords = { {
            0, 0,  1, 0,  1, 1,  0, 1,
            0, 0,  1, 0,  1, 1,  0, 1,
            0, 0,  1, 0,  1, 1,  0, 1,
            0, 0,  1, 0,  1, 1,  0, 1,
        } };

        IndexedQuadArray wall_geom = new IndexedQuadArray();
        wall_geom.setValidVertexCount(wall_coords.length / 3);
        wall_geom.setVertices(TriangleArray.COORDINATE_3, wall_coords);
        wall_geom.setIndices(wall_indices, wall_indices.length);
        wall_geom.setNormals(wall_normals);
        wall_geom.setTextureCoordinates(tex_type, wall_tex_coords, 1);

        // Load textures for the normal and colour maps
        TextureComponent2D normal_comp = loadTextureImage(NORMAL_MAP_FILE);
        TextureComponent2D colour_comp = loadTextureImage(COLOUR_MAP_FILE);

        TextureUnit[] text_tex_units = { new TextureUnit(), new TextureUnit() };

        if(normal_comp != null)
        {
            Texture2D normal_tex = new Texture2D(Texture2D.FORMAT_RGB, normal_comp);
            normal_tex.setBoundaryModeS(Texture.BM_WRAP);
            normal_tex.setBoundaryModeT(Texture.BM_WRAP);

            text_tex_units[1].setTexture(normal_tex);
        }

        if(colour_comp != null)
        {
            Texture2D colour_tex = new Texture2D(Texture2D.FORMAT_RGB, colour_comp);
            colour_tex.setBoundaryModeS(Texture.BM_WRAP);
            colour_tex.setBoundaryModeT(Texture.BM_WRAP);

            text_tex_units[0].setTexture(colour_tex);
        }


        Material tex_mat = new Material();
        tex_mat.setDiffuseColor(new float[] {0.8f, 0.8f, 0.8f } );
        tex_mat.setSpecularColor(new float[] {1, 1, 1 });
        tex_mat.setShininess(0.8f);

        Material colour_mat = new Material();
        colour_mat.setDiffuseColor(new float[] {0.8f, 0.8f, 0.8f });
        colour_mat.setSpecularColor(new float[] {0.4f, 0.4f, 0.4f });
        colour_mat.setShininess(0.8f);

        Appearance textured_app_1 = new Appearance();
        textured_app_1.setMaterial(tex_mat);
        textured_app_1.setTextureUnits(text_tex_units, 2);


        TextureComponent2D no_normal_comp =
            new ByteTextureComponent2D(TextureComponent.FORMAT_RGB,
                                       2,
                                       2,
                                       NO_NORMALS_IMAGE_SRC);

        TextureComponent2D no_texture_comp =
            new ByteTextureComponent2D(TextureComponent.FORMAT_RGB,
                                       2,
                                       2,
                                       NO_COLOUR_IMAGE_SRC);

        TextureUnit[] plain_tex_units = { new TextureUnit(), new TextureUnit() };

        Texture2D no_colour_tex =
            new Texture2D(Texture2D.FORMAT_RGB, no_texture_comp);
        no_colour_tex.setBoundaryModeS(Texture.BM_WRAP);
        no_colour_tex.setBoundaryModeT(Texture.BM_WRAP);

        plain_tex_units[0].setTexture(no_colour_tex);

        Texture2D no_normal_tex =
            new Texture2D(Texture2D.FORMAT_RGB, no_normal_comp);
        no_normal_tex.setBoundaryModeS(Texture.BM_WRAP);
        no_normal_tex.setBoundaryModeT(Texture.BM_WRAP);
        plain_tex_units[1].setTexture(no_normal_tex);


        Appearance plain_app_1 = new Appearance();
        plain_app_1.setMaterial(colour_mat);
        plain_app_1.setTextureUnits(plain_tex_units, 2);

        Shape3D test_shape_1 = new Shape3D();
        test_shape_1.setGeometry(test_geom);
        test_shape_1.setAppearance(textured_app_1);

        Shape3D test_shape_2 = new Shape3D();
        test_shape_2.setGeometry(test_geom);
        test_shape_2.setAppearance(textured_app_1);

        Shape3D test_shape_3 = new Shape3D();
        test_shape_3.setGeometry(test_geom);
        test_shape_3.setAppearance(textured_app_1);

        Shape3D wall_shape = new Shape3D();
        wall_shape.setGeometry(wall_geom);
        wall_shape.setAppearance(plain_app_1);

        // Transform the geometry in some way
        Matrix4f geom_mat1 = new Matrix4f();
        geom_mat1.setIdentity();
        geom_mat1.m03 = 0.5f;
        geom_mat1.m13 = -2.0f;
        geom_mat1.m23 = -1.0f;

        Matrix4f geom_mat2 = new Matrix4f();
        geom_mat2.setIdentity();
        geom_mat2.rotY(-PI_4);
        geom_mat2.m03 = 0.0f;
        geom_mat2.m13 = -4.5f;
        geom_mat2.m23 = -0.0f;

        Matrix4f geom_mat3 = new Matrix4f();
        geom_mat3.setIdentity();
        geom_mat3.rotY(PI_4 - 0.07f);
        geom_mat3.m03 = 1.0f;
        geom_mat3.m13 = -4.25f;
        geom_mat3.m23 = 0.0f;

        TransformGroup geom_tx_1 = new TransformGroup();
        geom_tx_1.setTransform(geom_mat1);
        geom_tx_1.addChild(test_shape_1);

        TransformGroup geom_tx_2 = new TransformGroup();
        geom_tx_2.setTransform(geom_mat2);
        geom_tx_2.addChild(test_shape_2);

        TransformGroup geom_tx_3 = new TransformGroup();
        geom_tx_3.setTransform(geom_mat3);
        geom_tx_3.addChild(test_shape_3);

        Group geom_group = new Group();
        geom_group.addChild(geom_tx_1);
        geom_group.addChild(geom_tx_2);
        geom_group.addChild(geom_tx_3);
        geom_group.addChild(wall_shape);

        sceneGeometry = new SharedGroup();
        sceneGeometry.addChild(geom_group);

        return sceneGeometry;
    }

    /**
     * Load the shader file. Find it relative to the classpath.
     *
     * @param file THe name of the file to load
     */
    private String[] loadShaderFile(String name)
    {
        File file = new File(name);
        if(!file.exists())
        {
            System.out.println("Cannot find file " + name);
            return null;
        }

        String ret_val = null;

        try
        {
            FileReader is = new FileReader(file);
            StringBuffer buf = new StringBuffer();
            char[] read_buf = new char[1024];
            int num_read = 0;

            while((num_read = is.read(read_buf, 0, 1024)) != -1)
                buf.append(read_buf, 0, num_read);

            is.close();

            ret_val = buf.toString();
        }
        catch(IOException ioe)
        {
            System.out.println("I/O error " + ioe);
        }

        return new String[] { ret_val };
    }

    /**
     * Generate an energy-minimised random sample pattern. Taken from here:
     * http://www.malmer.nu/index.php/2008-04-11_energy-minimization-is-your-friend/
     *
     * @param numSamples How many samples to create within the sphere
     * @param radius The radius of the sphere to generate
     */
    private float[] generateSamplePattern(int numSamples, float radius)
    {
        Vector3f[] sample_sphere = new Vector3f[numSamples];

        Random rndm = new Random();

        // construct random unit vectors
        for(int i = 0; i < numSamples; i++)
        {
            sample_sphere[i] = new Vector3f(rndm.nextFloat(),
                                            rndm.nextFloat(),
                                            rndm.nextFloat());
            sample_sphere[i].normalize();
        }

        // Energy minimization
        Vector3f force = new Vector3f();

        for(int iter = 0; iter < 100; iter++)
        {
            for( int i = 0; i < numSamples; i++)
            {
                Vector3f res = new Vector3f();
                Vector3f vec = sample_sphere[i];

                // Minimize with other samples
                for( int j = 0; j < numSamples; j++)
                {
                    force.sub(vec, sample_sphere[j]);

                    float fac = force.dot(force);

                    if(fac != 0.0f )
                    {
                        fac = 1.0f / fac;
                        res.x += fac * force.x;
                        res.y += fac * force.y;
                        res.z += fac * force.z;
                    }
                }

                res.scale(0.5f);
                sample_sphere[i].add(res);
                sample_sphere[i].normalize();
            }
        }

        // Now the energy is minimised, randomise the radius lengths;
        for(int i = 0; i < numSamples; i++)
            sample_sphere[i].scale(rndm.nextFloat() * radius);

        // Now turn it all in to a flat array suitable for passing to OGL.
        float[] ret_val = new float[numSamples * 3];

        for(int i = 0; i < numSamples; i++)
        {
            ret_val[i * 3] = sample_sphere[i].x;
            ret_val[i * 3 + 1] = sample_sphere[i].y;
            ret_val[i * 3 + 2] = sample_sphere[i].z;
        }

        return ret_val;
    }

    /**
     * Load a single image.
     */
    private TextureComponent2D loadTextureImage(String filename)
    {
        TextureComponent2D img_comp = null;

        try
        {
            File f = new File(filename);

            if(!f.exists())
                System.out.println("Can't find texture source file");

            FileInputStream is = new FileInputStream(f);

            BufferedInputStream stream = new BufferedInputStream(is);
            BufferedImage img = ImageIO.read(stream);

            int img_width = img.getWidth(null);
            int img_height = img.getHeight(null);
            int format = TextureComponent.FORMAT_RGB;

            switch(img.getType())
            {
                case BufferedImage.TYPE_3BYTE_BGR:
                case BufferedImage.TYPE_CUSTOM:
                case BufferedImage.TYPE_INT_RGB:
                    break;

                case BufferedImage.TYPE_4BYTE_ABGR:
                case BufferedImage.TYPE_INT_ARGB:
                    format = TextureComponent.FORMAT_RGBA;
                    break;
            }

            img_comp = new ImageTextureComponent2D(format,
                                            img_width,
                                            img_height,
                                            img);
        }
        catch(IOException ioe)
        {
            System.out.println("Error reading image: " + ioe);
        }

        return  img_comp;
    }

    public static void main(String[] args)
    {
        DeferredShadingDemo demo = new DeferredShadingDemo();
        demo.setVisible(true);
    }
}
