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
import org.j3d.geom.BoxGenerator;
import org.j3d.geom.SphereGenerator;
import org.j3d.geom.TorusGenerator;
import org.j3d.util.MatrixUtils;
import org.j3d.util.TriangleUtils;

/**
 * Example application demonstrating a simple depth of field renderer.
 * <p>
 *
 * Based on the discussion at:
 * http://www.gamedev.net/community/forums/topic.asp?topic_id=523491
 *
 * Not very good right now. Should look much more like the screen shot in that
 * thread.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class EdgeDetectionDemo extends Frame
    implements WindowListener
{
    /** App name to register preferences under */
    private static final String APP_NAME = "EdgeDetectionDemo";

    /** Image file holding the local normal map */
    private static final String NORMAL_MAP_FILE =
        "images/examples/shader/gbuffer_normal.png";

    /** Render pass vertex shader string */
    private static final String MAT_VTX_SHADER_FILE =
        "shaders/examples/global_illum/edge_depth_vert.glsl";

    /** Fragment shader file name for the rendering pass */
    private static final String MAT_FRAG_SHADER_FILE =
        "shaders/examples/global_illum/edge_depth_frag.glsl";

    /** Render pass vertex shader string */
    private static final String RENDER_VTX_SHADER_FILE =
        "shaders/examples/global_illum/edge_render_vert.glsl";

    /** Fragment shader file name for the rendering pass */
    private static final String RENDER_FRAG_SHADER_FILE =
        "shaders/examples/global_illum/edge_render_frag.glsl";

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

    /** Utility for doing matrix rotations */
    private MatrixUtils matrixUtils;

    /**
     * Construct a new shader demo instance.
     */
    public EdgeDetectionDemo()
    {
        super("Depth Of Field Demo");

        I18nManager intl_mgr = I18nManager.getManager();
        intl_mgr.setApplication(APP_NAME, "config.i18n.org-j3d-aviatrix3d-resources-core");

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
        Vector3d real_view_pos = new Vector3d();
        real_view_pos.set(0, 0, 15f);
        Vector3d render_view_pos = new Vector3d();
        render_view_pos.set(0, 0, 0.9f);

        // two quads to draw to
        float[] quad_coords = { -1, -1, 0, 1, -1, 0, 1, 1, 0, -1, 1, 0 };
        float[] quad_normals = { 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1 };
        float[][] tex_coord = { { 0, 0,  1, 0,  1, 1,  0, 1 } };

        int[] tex_type = { VertexGeometry.TEXTURE_COORDINATE_2 };
        float[] ambient_blend = { 1, 1, 1 };

        QuadArray sphere_geom = new QuadArray();
        sphere_geom.setValidVertexCount(4);
        sphere_geom.setVertices(TriangleArray.COORDINATE_3, quad_coords);
        sphere_geom.setNormals(quad_normals);
        sphere_geom.setTextureCoordinates(tex_type, tex_coord, 1);
        sphere_geom.setSingleColor(false, ambient_blend);

        MRTOffscreenTexture2D off_tex = createRenderTargetTexture(real_view_pos);

        TextureUnit[] tex_unit = { new TextureUnit(), new TextureUnit() };
        tex_unit[0].setTexture(off_tex);
        tex_unit[1].setTexture(off_tex.getRenderTarget(1));

        // Create the depth render shader
        String[] vert_shader_txt = loadShaderFile(RENDER_VTX_SHADER_FILE);
        String[] frag_shader_txt = loadShaderFile(RENDER_FRAG_SHADER_FILE);

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
        shader_args.setUniformSampler("normalMap", 0);
        shader_args.setUniformSampler("colorMap", 1);

        GLSLangShader shader = new GLSLangShader();
        shader.setShaderProgram(shader_prog);
        shader.setShaderArguments(shader_args);

        Appearance app_1 = new Appearance();
        app_1.setShader(shader);
        app_1.setTextureUnits(tex_unit, 2);

        Shape3D shape_1 = new Shape3D();
        shape_1.setGeometry(sphere_geom);
        shape_1.setAppearance(app_1);

        Viewpoint vp = new Viewpoint();

        Matrix4d view_mat = new Matrix4d();
        view_mat.setIdentity();
        view_mat.setTranslation(render_view_pos);

        TransformGroup tx = new TransformGroup();
        tx.setTransform(view_mat);
        tx.addChild(vp);

        SharedNode common_geom = new SharedNode();
        common_geom.setChild(shape_1);

        Group root_grp = new Group();
        root_grp.addChild(tx);
        root_grp.addChild(common_geom);

        SimpleScene main_scene = new SimpleScene();
        main_scene.setRenderedGeometry(root_grp);
        main_scene.setActiveView(vp);

        ViewEnvironment env = main_scene.getViewEnvironment();
        env.setProjectionType(ViewEnvironment.ORTHOGRAPHIC_PROJECTION);
        env.setClipDistance(-1, 1);
        env.setOrthoParams(-1.1, 1.1, -1.1, 1.1);

        // Then the basic layer and viewport at the top:
        SimpleViewport viewport = new SimpleViewport();
        viewport.setDimensions(0, 0, WINDOW_SIZE, WINDOW_SIZE);
        viewport.setScene(main_scene);


        ShaderLoadStatusCallback cb =
            new ShaderLoadStatusCallback(vert_shader, frag_shader, shader_prog);
        sceneManager.setApplicationObserver(cb);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(viewport);

        Layer[] layers = { layer };
        displayManager.setLayers(layers, 1);
    }

    /**
     * Create the contents of the offscreen texture that is being rendered
     */
    private MRTOffscreenTexture2D createRenderTargetTexture(Vector3d viewPos)
    {
        Viewpoint vp = new Viewpoint();

        Matrix4d view_mat = new Matrix4d();
        view_mat.setIdentity();
        view_mat.setTranslation(viewPos);

        TransformGroup tx = new TransformGroup();
        tx.setTransform(view_mat);
        tx.addChild(vp);

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.INDEXED_TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA |
                                  GeometryData.TEXTURE_2D_DATA;

        // Sphere to represent the light position in the scene.
        SphereGenerator s_generator = new SphereGenerator(2.5f, 32);
        s_generator.generate(data);

        float[] tangents = new float[data.vertexCount * 4];

        TriangleUtils.createTangents(data.indexesCount / 3,
                                     data.indexes,
                                     data.coordinates,
                                     data.normals,
                                     data.textureCoordinates,
                                     tangents);

        IndexedTriangleArray sphere_geom = new IndexedTriangleArray();
        sphere_geom.setValidVertexCount(data.vertexCount);
        sphere_geom.setVertices(TriangleArray.COORDINATE_3, data.coordinates);
        sphere_geom.setIndices(data.indexes, data.indexesCount);
        sphere_geom.setNormals(data.normals);
        sphere_geom.setAttributes(5, 4, tangents, false);

        data = new GeometryData();
        data.geometryType = GeometryData.INDEXED_TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA |
                                  GeometryData.TEXTURE_2D_DATA;

        BoxGenerator b_generator = new BoxGenerator(2.5f, 2.5f, 2.5f);
        b_generator.generate(data);

        // Generate tangent information

        tangents = new float[data.vertexCount * 4];

        TriangleUtils.createTangents(data.indexesCount / 3,
                                     data.indexes,
                                     data.coordinates,
                                     data.normals,
                                     data.textureCoordinates,
                                     tangents);

        IndexedTriangleArray box_geom = new IndexedTriangleArray();
        box_geom.setValidVertexCount(data.vertexCount);
        box_geom.setVertices(TriangleArray.COORDINATE_3, data.coordinates);
        box_geom.setIndices(data.indexes, data.indexesCount);
        box_geom.setNormals(data.normals);
        box_geom.setAttributes(5, 4, tangents, false);

        data = new GeometryData();
        data.geometryType = GeometryData.INDEXED_TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA |
                                  GeometryData.TEXTURE_2D_DATA;

        TorusGenerator t_generator = new TorusGenerator(0.75f, 4.5f);
        t_generator.generate(data);

        tangents = new float[data.vertexCount * 4];

        TriangleUtils.createTangents(data.indexesCount / 3,
                                     data.indexes,
                                     data.coordinates,
                                     data.normals,
                                     data.textureCoordinates,
                                     tangents);

        IndexedTriangleArray torus_geom = new IndexedTriangleArray();
        torus_geom.setValidVertexCount(data.vertexCount);
        torus_geom.setVertices(TriangleArray.COORDINATE_3, data.coordinates);
        torus_geom.setIndices(data.indexes, data.indexesCount);
        torus_geom.setNormals(data.normals);
        torus_geom.setAttributes(5, 4, tangents, false);

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

        GLSLangShader shader = new GLSLangShader();
        shader.setShaderProgram(shader_prog);

        Material mat = new Material();
        mat.setDiffuseColor(new float[] {1, 1, 0, 1});
        mat.setSpecularColor(new float[] {1, 1, 1, 1});

        TextureUnit[] tex_units = { new TextureUnit() };
        TextureComponent2D normal_comp = loadTextureImage(NORMAL_MAP_FILE);
        if(normal_comp != null)
        {
            Texture2D normal_tex = new Texture2D(Texture2D.FORMAT_RGB, normal_comp);
            normal_tex.setBoundaryModeS(Texture.BM_WRAP);
            normal_tex.setBoundaryModeT(Texture.BM_WRAP);

            tex_units[0].setTexture(normal_tex);
        }


        Appearance app = new Appearance();
        app.setTextureUnits(tex_units, 1);
        app.setMaterial(mat);
        app.setShader(shader);

        Shape3D sphere_shape = new Shape3D();
        sphere_shape.setGeometry(sphere_geom);
        sphere_shape.setAppearance(app);

        Shape3D box_shape = new Shape3D();
        box_shape.setGeometry(box_geom);
        box_shape.setAppearance(app);

        Shape3D torus_shape = new Shape3D();
        torus_shape.setGeometry(torus_geom);
        torus_shape.setAppearance(app);

        // Transform the geometry in some way
        Matrix4d geom_mat1 = new Matrix4d();
        matrixUtils.rotateX(PI_4, geom_mat1);

        Matrix4d geom_mat2 = new Matrix4d();
        matrixUtils.rotateY(PI_4, geom_mat2);

        geom_mat2.mul(geom_mat2, geom_mat1);
        geom_mat2.m03 = 3.0f;

        TransformGroup box_tx = new TransformGroup();
        box_tx.setTransform(geom_mat2);
        box_tx.addChild(box_shape);

        geom_mat1.setIdentity();
        geom_mat1.m03 = -3.0f;

        TransformGroup sphere_tx = new TransformGroup();
        sphere_tx.setTransform(geom_mat1);
        sphere_tx.addChild(sphere_shape);

        geom_mat1.setIdentity();
        geom_mat1.m03 = -3.0f;
        geom_mat1.m13 = -1.5f;

        TransformGroup torus_tx = new TransformGroup();
        torus_tx.setTransform(geom_mat1);
        torus_tx.addChild(torus_shape);

        Group root_grp = new Group();
        root_grp.addChild(tx);
        root_grp.addChild(box_tx);
        root_grp.addChild(sphere_tx);
        root_grp.addChild(torus_tx);

        SimpleScene main_scene = new SimpleScene();
        main_scene.setRenderedGeometry(root_grp);
        main_scene.setActiveView(vp);

        mainSceneEnv = main_scene.getViewEnvironment();
        mainSceneEnv.setNearClipDistance(0.5);
        mainSceneEnv.setFarClipDistance(200);

        // Then the basic layer and viewport at the top:
        SimpleViewport viewport = new SimpleViewport();
        viewport.setDimensions(0, 0, TEXTURE_SIZE, TEXTURE_SIZE);
        viewport.setScene(main_scene);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(viewport);

        Layer[] layers = { layer };

        // The texture requires its own set of capabilities.
        GraphicsRenderingCapabilities caps = new GraphicsRenderingCapabilities();
        caps.doubleBuffered = false;
        caps.depthBits = 24;

        MRTOffscreenTexture2D off_tex =
            new MRTOffscreenTexture2D(caps, TEXTURE_SIZE, TEXTURE_SIZE, 2, false);

        off_tex.setClearColor(0.5f, 0.5f, 0.5f, 1);
        off_tex.setRepaintRequired(true);
        off_tex.setLayers(layers, 1);

//        ShaderLoadStatusCallback cb =
//            new ShaderLoadStatusCallback(vert_shader, frag_shader, shader_prog);
//        sceneManager.setApplicationObserver(cb);

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
        EdgeDetectionDemo demo = new EdgeDetectionDemo();
        demo.setVisible(true);
    }
}
