
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
 * @version $Revision: 1.7 $
 */
public class SSAODemo extends Frame
    implements WindowListener
{
    /** App name to register preferences under */
    private static final String APP_NAME = "SSAODemo";

    /** Render GBuffer depth pass vertex shader file name */
    private static final String DEPTH_VTX_SHADER_FILE =
        "global_illum/ssao_normal_vert.glsl";

    /** Render GBuffer depth Fragment shader file name */
    private static final String DEPTH_FRAG_SHADER_FILE =
        "global_illum/ssao_normal_frag.glsl";

    /** Screen space ambient occlusion pass vertex shader file name */
    private static final String SSAO_VTX_SHADER_FILE =
        "global_illum/deferred_ssao_vert.glsl";

    /** Screen space ambient occlusion fragment shader file name */
    private static final String SSAO_FRAG_SHADER_FILE =
        "global_illum/deferred_ssao_frag.glsl";

    /**
     * Image file holding a random colour sample map for SSAO
     */
    private static final String RANDOM_MAP_FILE =
        "textures/ssao_noise.png";

    /** Width and height of the offscreen texture, in pixels */
    private static final int TEXTURE_SIZE = 512;

    /** Half Width and height of the offscreen texture, in pixels */
    private static final int HALF_TEXTURE_SIZE = 256;

    /** Width and height of the main window, in pixels */
    private static final int WINDOW_SIZE = 512;

    /** Sample radius to make for SSAO */
    private static final float SSAO_SAMPLE_RADIUS = 0.03f;

    /** PI / 4 for rotations */
    private static final float PI_4 = (float)(Math.PI * 0.25f);

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
    private BulkShaderLoadStatusCallback shaderCallback;

    /**
     * Construct a new shader demo instance.
     */
    public SSAODemo()
    {
        super("SSAO Shading Demo");

        I18nManager intl_mgr = I18nManager.getManager();
        intl_mgr.setApplication(APP_NAME, "config.i18n.av3dResources");

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
        GLCapabilities caps = new GLCapabilities();
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);

        GraphicsCullStage culler = new FrustumCullStage();
        culler.setOffscreenCheckEnabled(true);

        GraphicsSortStage sorter = new StateAndTransparencyDepthSortStage();
        surface = new DebugAWTSurface(caps);
        //surface = new SimpleAWTSurface(caps);
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
        shaderCallback = new BulkShaderLoadStatusCallback();

        Vector3f real_view_pos = new Vector3f(0, 0, 20.0f);

        MRTOffscreenTexture2D gbuffer_tex = createGBufferTexture(real_view_pos);

        MRTOffscreenTexture2D ssao_tex =
            createSSAOTexture(gbuffer_tex,
                              gbuffer_tex.getDepthRenderTarget(),
                              real_view_pos);

        // two quads to draw to
        float[] quad_coords = { -1, -1, -0.5f, 1, -1, -0.5f, 1, 1, -0.5f, -1, 1, -0.5f };
        float[] quad_normals = { 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1 };
        float[][] tex_coord = { { 0, 0,  1, 0,  1, 1,  0, 1 } };

        int[] tex_type = { VertexGeometry.TEXTURE_COORDINATE_2 };

        QuadArray real_geom = new QuadArray();
        real_geom.setValidVertexCount(4);
        real_geom.setVertices(TriangleArray.COORDINATE_3, quad_coords);
        real_geom.setNormals(quad_normals);
        real_geom.setTextureCoordinates(tex_type, tex_coord, 1);

        TextureAttributes tex_attr = new TextureAttributes();
        tex_attr.setTextureMode(TextureAttributes.MODE_REPLACE);

        TextureUnit[] tex_unit = { new TextureUnit() };

        tex_unit[0].setTexture(ssao_tex);
        tex_unit[0].setTextureAttributes(tex_attr);

        Appearance colour_app = new Appearance();
        colour_app.setTextureUnits(tex_unit, 1);

        Shape3D colour_shape = new Shape3D();
        colour_shape.setGeometry(real_geom);
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
        viewport.setDimensions(0, 0, WINDOW_SIZE, WINDOW_SIZE);
        viewport.setScene(main_scene);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(viewport);

        sceneManager.setApplicationObserver(shaderCallback);

        Layer[] layers = { layer };
        displayManager.setLayers(layers, 1);
    }

    /**
     * Creates the antialias pass output texture from the input texture
     */
    private MRTOffscreenTexture2D createSSAOTexture(Texture normalSource,
                                                    Texture depthSource,
                                                    Vector3f viewPos)
    {
        // two quads to draw to
        float[] quad_coords = { -1, -1, -0.5f, 1, -1, -0.5f, 1, 1, -0.5f, -1, 1, -0.5f };
        float[] quad_normals = { 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1 };
        float[][] tex_coord = { { 0, 0,  1, 0,  1, 1,  0, 1 } };

        int[] tex_type = { VertexGeometry.TEXTURE_COORDINATE_2 };
        float[] base_color = { 0, 0, 0 };

        QuadArray real_geom = new QuadArray();
        real_geom.setValidVertexCount(4);
        real_geom.setVertices(TriangleArray.COORDINATE_3, quad_coords);
        real_geom.setNormals(quad_normals);
        real_geom.setTextureCoordinates(tex_type, tex_coord, 1);
        real_geom.setSingleColor(false, base_color);

        TextureComponent2D random_comp = loadTextureImage(RANDOM_MAP_FILE);

        TextureUnit[] tex_unit =
        {
            new TextureUnit(),
            new TextureUnit(),
            new TextureUnit()
        };

        tex_unit[0].setTexture(normalSource);

        if(random_comp != null)
        {
            Texture2D random_tex = new Texture2D(Texture2D.FORMAT_RGB, random_comp);
            random_tex.setBoundaryModeS(Texture.BM_WRAP);
            random_tex.setBoundaryModeT(Texture.BM_WRAP);

            tex_unit[1].setTexture(random_tex);
        }

        tex_unit[2].setTexture(depthSource);

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
        shape.setGeometry(real_geom);
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

        off_tex.setClearColor(1, 1, 1, 1);
        off_tex.setRepaintRequired(true);
        off_tex.setLayers(layers, 1);

        shaderCallback.addShader("SSAO", vert_shader, frag_shader, shader_prog);

        return off_tex;
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

        // Create the gbuffer shader
        String[] vert_shader_txt = loadShaderFile(DEPTH_VTX_SHADER_FILE);
        String[] frag_shader_txt = loadShaderFile(DEPTH_FRAG_SHADER_FILE);

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

        Shape3D test_shape_1 = new Shape3D();
        test_shape_1.setGeometry(test_geom);

        Shape3D test_shape_2 = new Shape3D();
        test_shape_2.setGeometry(test_geom);

        Shape3D test_shape_3 = new Shape3D();
        test_shape_3.setGeometry(test_geom);

        Shape3D wall_shape = new Shape3D();
        wall_shape.setGeometry(wall_geom);

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
        geom_group.addChild(app_ovr);
        geom_group.addChild(geom_tx_1);
        geom_group.addChild(geom_tx_2);
        geom_group.addChild(geom_tx_3);
        geom_group.addChild(wall_shape);

        Group root_grp = new Group();
        root_grp.addChild(vp_tx);
        root_grp.addChild(geom_group);

        SimpleScene main_scene = new SimpleScene();
        main_scene.setRenderedGeometry(root_grp);
        main_scene.setActiveView(vp);

        mainSceneEnv = main_scene.getViewEnvironment();
        mainSceneEnv.setFarClipDistance(500);

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
            new MRTOffscreenTexture2D(caps, true, TEXTURE_SIZE, TEXTURE_SIZE, 1, true);

        off_tex.setClearColor(0, 0, 0, 1);
        off_tex.setRepaintRequired(true);
        off_tex.setLayers(layers, 1);

        shaderCallback.addShader("GBuffer", vert_shader, frag_shader, shader_prog);

        return off_tex;
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
        SSAODemo demo = new SSAODemo();
        demo.setVisible(true);
    }
}
