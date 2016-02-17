package j3d.aviatrix3d.examples.shader;

// External imports
import java.awt.*;
import java.awt.event.*;

import java.awt.image.BufferedImage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

import org.j3d.util.DataUtils;
import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.pipeline.graphics.*;

import org.j3d.aviatrix3d.output.graphics.DebugAWTSurface;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;

import org.j3d.geom.GeometryData;
import org.j3d.geom.SphereGenerator;
import org.j3d.util.MatrixUtils;
import org.j3d.util.TriangleUtils;

/**
 * Example application demonstrating a GBuffer writing to multiple render
 * targets.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class GBufferDemo extends Frame
    implements WindowListener
{
    /** App name to register preferences under */
    private static final String APP_NAME = "GBufferDemo";

    /** Render pass vertex shader string */
    private static final String VERTEX_SHADER_FILE =
        "shaders/examples/simple/gbuffer_geom_vert.glsl";

    /** Fragment shader file name for the rendering pass */
    private static final String FRAG_SHADER_FILE =
        "shaders/examples/simple/gbuffer_geom_frag.glsl";

    /** Image file holding the local normal map */
    private static final String NORMAL_MAP_FILE =
        "images/examples/shader/gbuffer_normal.png";

    /** Image file holding the local colour map */
    private static final String COLOUR_MAP_FILE =
        "images/examples/shader/gbuffer_colour.png";

    /** Width and height of the offscreen texture, in pixels */
    private static final int TEXTURE_SIZE = 256;

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

    /** The view environment created for the main scene */
    private ViewEnvironment mainSceneEnv;

    /** Utility for processing matrix rotations */
    private MatrixUtils matrixUtils;

    /**
     * Construct a new shader demo instance.
     */
    public GBufferDemo()
    {
        super("Multiple Render Target Demo");

        I18nManager intl_mgr = I18nManager.getManager();
        intl_mgr.setApplication(APP_NAME, "config.i18n.av3dResources");

        setLayout(new BorderLayout());
        addWindowListener(this);

        matrixUtils = new MatrixUtils();

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
        // two quads to draw to
        float[] quad_coords = { -1, -1, 0, 1, -1, 0, 1, 1, 0, -1, 1, 0 };
        float[] quad_normals = { 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1 };
        float[][] tex_coord = { { 0, 0,  1, 0,  1, 1,  0, 1 } };

        int[] tex_type = { VertexGeometry.TEXTURE_COORDINATE_2 };

        QuadArray real_geom = new QuadArray();
        real_geom.setValidVertexCount(4);
        real_geom.setVertices(TriangleArray.COORDINATE_3, quad_coords);
        real_geom.setNormals(quad_normals);
        real_geom.setTextureCoordinates(tex_type, tex_coord, 1);

        MRTOffscreenTexture2D off_tex = createRenderTargetTexture();

        TextureUnit[] tex_unit = { new TextureUnit() };
        tex_unit[0].setTexture(off_tex);

        Material mater_1 = new Material();
        mater_1.setEmissiveColor(new float[] { 0, 1, 0, 1} );

        Appearance app_1 = new Appearance();
        app_1.setMaterial(mater_1);
        app_1.setTextureUnits(tex_unit, 1);

        Shape3D shape_1 = new Shape3D();
        shape_1.setGeometry(real_geom);
        shape_1.setAppearance(app_1);

        Vector3d trans = new Vector3d();
        trans.set(-1.05f, 1.05f, 0);

        Matrix4d mat_1 = new Matrix4d();
        mat_1.set(trans);

        TransformGroup normal_buffer_tx = new TransformGroup();
        normal_buffer_tx.setTransform(mat_1);
        normal_buffer_tx.addChild(shape_1);

        tex_unit = new TextureUnit[] { new TextureUnit() };
        tex_unit[0].setTexture(off_tex.getRenderTarget(1));

        Material mater_2 = new Material();
        mater_2.setEmissiveColor(new float[] { 0, 0, 1, 1} );

        Appearance app_2 = new Appearance();
        app_2.setMaterial(mater_2);
        app_2.setTextureUnits(tex_unit, 1);

        Shape3D shape_2 = new Shape3D();
        shape_2.setGeometry(real_geom);
        shape_2.setAppearance(app_2);

        trans = new Vector3d();
        trans.set(1.05f, 1.05f, 0);

        Matrix4d mat_2 = new Matrix4d();
        mat_2.set(trans);

        TransformGroup diffuse_buffer_tx = new TransformGroup();
        diffuse_buffer_tx.setTransform(mat_2);
        diffuse_buffer_tx.addChild(shape_2);

        tex_unit = new TextureUnit[] { new TextureUnit() };
        tex_unit[0].setTexture(off_tex.getRenderTarget(2));

        Material mater_3 = new Material();
        mater_3.setEmissiveColor(new float[] { 0, 0, 1, 1} );

        Appearance app_3 = new Appearance();
        app_3.setMaterial(mater_3);
        app_3.setTextureUnits(tex_unit, 1);

        Shape3D shape_3 = new Shape3D();
        shape_3.setGeometry(real_geom);
        shape_3.setAppearance(app_3);

        trans = new Vector3d();
        trans.set(-1.05f, -1.05f, 0);

        Matrix4d mat_3 = new Matrix4d();
        mat_3.set(trans);

        TransformGroup specular_buffer_tx = new TransformGroup();
        specular_buffer_tx.setTransform(mat_3);
        specular_buffer_tx.addChild(shape_3);

        tex_unit = new TextureUnit[] { new TextureUnit() };
        tex_unit[0].setTexture(off_tex.getRenderTarget(3));

        Material mater_4 = new Material();
        mater_4.setEmissiveColor(new float[] { 1, 0, 1, 1} );

        Appearance app_4 = new Appearance();
        app_4.setMaterial(mater_4);
        app_4.setTextureUnits(tex_unit, 1);

        Shape3D shape_4 = new Shape3D();
        shape_4.setGeometry(real_geom);
        shape_4.setAppearance(app_4);

        trans = new Vector3d();
        trans.set(1.05f, -1.05f, 0);

        Matrix4d mat_4 = new Matrix4d();
        mat_4.set(trans);

        TransformGroup depth_buffer_tx = new TransformGroup();
        depth_buffer_tx.setTransform(mat_4);
        depth_buffer_tx.addChild(shape_4);


        Viewpoint vp = new Viewpoint();

        trans = new Vector3d();
        trans.set(0, 0, 6);

        Matrix4d view_mat = new Matrix4d();
        view_mat.set(trans);

        TransformGroup tx = new TransformGroup();
        tx.setTransform(view_mat);
        tx.addChild(vp);

        Group root_grp = new Group();
        root_grp.addChild(tx);
        root_grp.addChild(normal_buffer_tx);
        root_grp.addChild(diffuse_buffer_tx);
        root_grp.addChild(specular_buffer_tx);
        root_grp.addChild(depth_buffer_tx);

        SimpleScene scene = new SimpleScene();
        scene.setRenderedGeometry(root_grp);
        scene.setActiveView(vp);

        // Then the basic layer and viewport at the top:
        SimpleViewport view = new SimpleViewport();
        view.setDimensions(0, 0, WINDOW_SIZE, WINDOW_SIZE);
        view.setScene(scene);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(view);

        Layer[] layers = { layer };
        displayManager.setLayers(layers, 1);
    }

    /**
     * Create the contents of the offscreen texture that is being rendered
     */
    private MRTOffscreenTexture2D createRenderTargetTexture()
    {
        Viewpoint vp = new Viewpoint();

        Vector3d trans = new Vector3d();
        trans.set(0, 0, 7f);

        Matrix4d view_mat = new Matrix4d();
        view_mat.set(trans);

        TransformGroup tx = new TransformGroup();
        tx.setTransform(view_mat);
        tx.addChild(vp);

        RenderPass depth_pass = new RenderPass();
        mainSceneEnv = depth_pass.getViewEnvironment();

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.INDEXED_TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA |
                                  GeometryData.TEXTURE_2D_DATA;

        // Sphere to represent the light position in the scene.
        SphereGenerator generator = new SphereGenerator(2.5f, 32);
        generator.generate(data);
//        BoxGenerator generator = new BoxGenerator(2.5f, 2.5f, 2.5f);
 //       generator.generate(data);

        float[][] tex_coord = { data.textureCoordinates };
        int[] tex_type = { VertexGeometry.TEXTURE_COORDINATE_2 };

        // Generate tangent information
        float[] tangents = new float[data.vertexCount * 4];

        TriangleUtils.createTangents(data.indexesCount / 3,
                                     data.indexes,
                                     data.coordinates,
                                     data.normals,
                                     data.textureCoordinates,
                                     tangents);

        IndexedTriangleArray real_geom = new IndexedTriangleArray();
        real_geom.setValidVertexCount(data.vertexCount);
        real_geom.setVertices(TriangleArray.COORDINATE_3, data.coordinates);
        real_geom.setIndices(data.indexes, data.indexesCount);
        real_geom.setNormals(data.normals);
        real_geom.setTextureCoordinates(tex_type, tex_coord, 1);
        real_geom.setAttributes(5, 4, tangents, false);

        // Load textures for the normal and colour maps
        TextureComponent2D normal_comp = loadTextureImage(NORMAL_MAP_FILE);
        TextureComponent2D colour_comp = loadTextureImage(COLOUR_MAP_FILE);

        TextureUnit[] tex_units = { new TextureUnit(), new TextureUnit() };

        if(normal_comp != null)
        {
            Texture2D normal_tex = new Texture2D(Texture2D.FORMAT_RGB, normal_comp);
            normal_tex.setBoundaryModeS(Texture.BM_WRAP);
            normal_tex.setBoundaryModeT(Texture.BM_WRAP);

            tex_units[0].setTexture(normal_tex);
        }

        if(colour_comp != null)
        {
            Texture2D colour_tex = new Texture2D(Texture2D.FORMAT_RGB, colour_comp);
            colour_tex.setBoundaryModeS(Texture.BM_WRAP);
            colour_tex.setBoundaryModeT(Texture.BM_WRAP);

            tex_units[1].setTexture(colour_tex);
        }

        // Create the gbuffer shader
        String[] vert_shader_txt = loadShaderFile(VERTEX_SHADER_FILE);
        String[] frag_shader_txt = loadShaderFile(FRAG_SHADER_FILE);

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

        float near_clip = (float)mainSceneEnv.getNearClipDistance();
        float far_clip = (float)mainSceneEnv.getFarClipDistance();

        float[] tile = { 1.0f };
        float[] d_planes =
        {
            -far_clip / (far_clip - near_clip),
            -far_clip * near_clip / (far_clip - near_clip)
        };

        ShaderArguments shader_args = new ShaderArguments();
        shader_args.setUniform("tileScale", 1, tile, 1);
        shader_args.setUniform("planes", 2, d_planes, 1);
        shader_args.setUniformSampler("normalMap", 0);
        shader_args.setUniformSampler("colourMap", 1);

        GLSLangShader shader = new GLSLangShader();
        shader.setShaderProgram(shader_prog);
        shader.setShaderArguments(shader_args);

        Material mat = new Material();
        mat.setDiffuseColor(new float[] {1, 1, 0, 1});
        mat.setSpecularColor(new float[] {1, 1, 1, 1});

        Appearance app = new Appearance();
        app.setMaterial(mat);
        app.setTextureUnits(tex_units, 2);
        app.setShader(shader);

        Shape3D shape = new Shape3D();
        shape.setGeometry(real_geom);
        shape.setAppearance(app);

        // Transform the geometry in some way
        Matrix4d geom_mat1 = new Matrix4d();
        matrixUtils.rotateX(PI_4, geom_mat1);

        Matrix4d geom_mat2 = new Matrix4d();
        matrixUtils.rotateY(PI_4, geom_mat2);

        geom_mat2.mul(geom_mat2, geom_mat1);

        TransformGroup geom_tx = new TransformGroup();
        geom_tx.setTransform(geom_mat2);
        geom_tx.addChild(shape);

        Group root_grp = new Group();
        root_grp.addChild(tx);
        root_grp.addChild(geom_tx);

        DepthBufferState depth_dbs = new DepthBufferState();
        depth_dbs.setClearBufferState(true);
        depth_dbs.enableDepthTest(true);
        depth_dbs.enableDepthWrite(true);
        depth_dbs.setDepthFunction(DepthBufferState.FUNCTION_LESS_OR_EQUAL);

        ColorBufferState depth_cbs = new ColorBufferState();
        depth_cbs.setClearBufferState(true);
        depth_cbs.setClearColor(0, 0, 0, 0);
        depth_cbs.setColorMask(false, false, false, false);

        depth_pass.setColorBufferState(depth_cbs);
        depth_pass.setDepthBufferState(depth_dbs);
        depth_pass.setRenderedGeometry(root_grp);
        depth_pass.setActiveView(vp);


        DepthBufferState material_dbs = new DepthBufferState();
        material_dbs.setClearBufferState(false);
        material_dbs.enableDepthTest(true);
        material_dbs.enableDepthWrite(false);
        material_dbs.setDepthFunction(DepthBufferState.FUNCTION_LESS_OR_EQUAL);

        ColorBufferState material_cbs = new ColorBufferState();
        material_cbs.setClearBufferState(false);
        material_cbs.setClearColor(0, 0, 0, 1);
        material_cbs.setColorMask(true, true, true, true);

        RenderPass material_pass = new RenderPass();
        material_pass.setColorBufferState(material_cbs);
        material_pass.setDepthBufferState(material_dbs);
        material_pass.setRenderedGeometry(root_grp);
        material_pass.setActiveView(vp);

        MultipassScene main_scene = new MultipassScene();
        main_scene.addRenderPass(depth_pass);
        main_scene.addRenderPass(material_pass);

        // Then the basic layer and viewport at the top:
        MultipassViewport viewport = new MultipassViewport();
        viewport.setDimensions(0, 0, TEXTURE_SIZE, TEXTURE_SIZE);
        viewport.setScene(main_scene);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(viewport);

        Layer[] layers = { layer };

        // The texture requires its own set of capabilities.
        GraphicsRenderingCapabilities caps = new GraphicsRenderingCapabilities();
        caps.doubleBuffered = false;
        caps.useFloatingPointBuffers = true;
        caps.depthBits = 24;

        MRTOffscreenTexture2D off_tex =
            new MRTOffscreenTexture2D(caps, TEXTURE_SIZE, TEXTURE_SIZE, 4);

        off_tex.setClearColor(0.5f, 0.5f, 0.5f, 1);
        off_tex.setRepaintRequired(true);
        off_tex.setLayers(layers, 1);

        ShaderLoadStatusCallback cb =
            new ShaderLoadStatusCallback(vert_shader, frag_shader, shader_prog);
        sceneManager.setApplicationObserver(cb);

        return off_tex;
    }

    /**
     * Load the shader file. Find it relative to the classpath.
     *
     * @param name THe name of the file to load
     */
    private String[] loadShaderFile(String name)
    {
        File file = DataUtils.lookForFile(name, getClass(), null);
        if(file == null)
        {
            System.out.println("Cannot find file " + name);
            return null;
        }

        String ret_val = null;

        try
        {
            FileReader is = new FileReader(file);
            StringBuilder buf = new StringBuilder();
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
    private TextureComponent2D loadTextureImage(String name)
    {
        TextureComponent2D img_comp = null;

        try
        {
            File file = DataUtils.lookForFile(name, getClass(), null);
            if(file == null)
            {
                System.out.println("Can't find texture source file");
                return null;
            }

            FileInputStream is = new FileInputStream(file);

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
        GBufferDemo demo = new GBufferDemo();
        demo.setVisible(true);
    }
}
