package j3d.aviatrix3d.examples.shader;

// External imports
import java.awt.*;
import java.awt.event.*;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;

import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

// Local imports
import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.pipeline.graphics.*;

import org.j3d.aviatrix3d.output.graphics.SimpleAWTSurface;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;

import org.j3d.geom.GeometryData;
import org.j3d.geom.BoxGenerator;
import org.j3d.geom.SphereGenerator;
import org.j3d.util.MatrixUtils;

/**
 * Example application showing off using an offscreen buffer as a depth-only
 * system. Floating point textures are requested for better accuracy. The depth
 * value is read from the offscreen texture by the shader and converted to the
 * red chanel value of the texture that is drawn as a quad on screen.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class DepthTextureFBODemo extends Frame
    implements WindowListener
{
    /** Render pass vertex shader string */
    private static final String RENDER_PASS_VERTEX_SHADER_FILE =
        "subsurf/fbo_depth_pass_vert.glsl";

    /** Fragment shader file name for the rendering pass */
    private static final String RENDER_PASS_FRAG_SHADER_FILE =
        "subsurf/fbo_depth_pass_frag.glsl";

    /** Width and height of the offscreen texture, in pixels */
    private static final int TEXTURE_SIZE = 1024;

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
    public DepthTextureFBODemo()
    {
        super("Depth Texture Demo");

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

        GraphicsCullStage culler = new FrustumCullStage();
        culler.setOffscreenCheckEnabled(true);

        GraphicsSortStage sorter = new StateAndTransparencyDepthSortStage();
        surface = new SimpleAWTSurface(caps);
        surface.setColorClearNeeded(false);

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
        Background bg = createBackground();

        // View group
        Viewpoint vp = new Viewpoint();

        Vector3d trans = new Vector3d();
        trans.set(0, 0, 12f);

        Matrix4d view_mat = new Matrix4d();
        view_mat.setIdentity();
        view_mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(view_mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);
        scene_root.addChild(bg);

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA;

        // Sphere to represent the light position in the scene.
        SphereGenerator generator = new SphereGenerator(0.01f, 16);
        generator.generate(data);

        TriangleArray light_geom = new TriangleArray();
        light_geom.setValidVertexCount(data.vertexCount);
        light_geom.setVertices(TriangleArray.COORDINATE_3, data.coordinates);
        light_geom.setNormals(data.normals);

        Material light_mat = new Material();
        light_mat.setDiffuseColor(new float[] { 0.6f, 0.6f, 1 });
        light_mat.setEmissiveColor(new float[] { 0.6f, 0.6f, 1 });
        light_mat.setSpecularColor(new float[] { 1, 1, 1 });

        Appearance light_app = new Appearance();
        light_app.setMaterial(light_mat);

        Shape3D light_shape = new Shape3D();
        light_shape.setAppearance(light_app);
        light_shape.setGeometry(light_geom);

        Vector3d light_pos = new Vector3d();
        light_pos.set(0, 0, -5);

        Matrix4d mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(light_pos);


        TransformGroup light_group = new TransformGroup();
        light_group.setTransform(mat);
        light_group.addChild(light_shape);

        scene_root.addChild(light_group);

        // We just put in a small sphere for the
        String[] vert_shader_txt = loadShaderFile(RENDER_PASS_VERTEX_SHADER_FILE);
        String[] frag_shader_txt = loadShaderFile(RENDER_PASS_FRAG_SHADER_FILE);

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
        shader_args.setUniformSampler("viewerDepthTexture", 0);

        GLSLangShader shader = new GLSLangShader();
        shader.setShaderProgram(shader_prog);
        shader.setShaderArguments(shader_args);

        data.coordinates = null;
        data.normals = null;

        BoxGenerator box_gen = new BoxGenerator(4f, 4f, 2f);
        box_gen.generate(data);

        // Sphere to render the shader onto
        TriangleArray sphere_geom = new TriangleArray();
        sphere_geom.setValidVertexCount(data.vertexCount);
        sphere_geom.setVertices(TriangleArray.COORDINATE_3, data.coordinates);
        sphere_geom.setNormals(data.normals);

        // Flat panel that has the viewable object as the demo
        float[] coord = { -1.5f, -1.5f, 0,
                           1.5f, -1.5f, 0,
                           1.5f,  1.5f, 0,
                          -1.5f,  1.5f, 0};
        float[] normal = { 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1 };
        float[][] tex_coord = { { 0, 0, 1, 0, 1, 1, 0, 1 } };
        int[] tex_type = { VertexGeometry.TEXTURE_COORDINATE_2 };

        //float[] color = { 0, 0, 1, 0, 1, 0, 1, 0, 0 };

        QuadArray test_geom = new QuadArray();
        test_geom.setValidVertexCount(4);
        test_geom.setVertices(TriangleArray.COORDINATE_3, coord);
        test_geom.setNormals(normal);
        test_geom.setTextureCoordinates(tex_type, tex_coord, 1);

        OffscreenTexture2D depth_map = createViewpointDepthMap(sphere_geom,
                                                               view_mat,
                                                               WINDOW_SIZE,
                                                               WINDOW_SIZE);

        TextureUnit textures[] = new TextureUnit[1];
        textures[0] = new TextureUnit();
        textures[0].setTexture(depth_map);

        Appearance app = new Appearance();
        app.setTextureUnits(textures, 1);
        app.setShader(shader);

        Shape3D shape = new Shape3D();
        shape.setGeometry(test_geom);
        shape.setAppearance(app);

        MatrixUtils utils = new MatrixUtils();

        Matrix4d rot_mat  = new Matrix4d();
        utils.rotateY(PI_4, rot_mat);

        TransformGroup anim_rotation = new TransformGroup();
//        anim_rotation.setTransform(rot_mat);
        anim_rotation.addChild(shape);

        utils.rotateX(PI_4, rot_mat);

        TransformGroup main_rotation = new TransformGroup();
//        main_rotation.setTransform(rot_mat);
        main_rotation.addChild(anim_rotation);

        scene_root.addChild(main_rotation);

        SimpleScene scene = new SimpleScene();
        scene.setRenderedGeometry(scene_root);
        scene.setActiveView(vp);
        scene.setActiveBackground(bg);

        // Then the basic layer and viewport at the top:
        SimpleViewport view = new SimpleViewport();
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
     * Load the shader file. Find it relative to the classpath.
     *
     * @param name THe name of the file to load
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
    private TextureComponent2D loadImage(File f)
    {
        TextureComponent2D img_comp = null;

        try
        {
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

    private Background createBackground()
    {
        // Simple colour background that has a textured image quad in it
        float[] bg_coords = { -1, -1, -0.5f, 1, -1, -0.5f, 1, 1, -0.5f, -1, 1, -0.5f };
        float[] bg_normals = { 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1 };
        float[][] bg_texcoords = { { 0, 0, 1, 0, 1, 1, 0, 1 } };
        int[] tex_type = { VertexGeometry.TEXTURE_COORDINATE_2 };


        QuadArray bg_geom = new QuadArray();
        bg_geom.setValidVertexCount(4);
        bg_geom.setVertices(QuadArray.COORDINATE_3, bg_coords);
        bg_geom.setNormals(bg_normals);
        bg_geom.setTextureCoordinates(tex_type, bg_texcoords, 1);

        File tex_file = new File("textures/flags/australia.png");
        TextureComponent2D img_comp = loadImage(tex_file);
        Texture2D tex = new Texture2D(Texture2D.FORMAT_RGBA, img_comp);
        tex.setBoundaryModeS(Texture.BM_CLAMP_TO_EDGE);
        tex.setBoundaryModeT(Texture.BM_CLAMP_TO_EDGE);

        TextureUnit[] tu = new TextureUnit[1];
        tu[0] = new TextureUnit();
        tu[0].setTexture(tex);

        Appearance app = new Appearance();
        app.setTextureUnits(tu, 1);

        Shape3D bg_shape = new Shape3D();
        bg_shape.setGeometry(bg_geom);
        bg_shape.setAppearance(app);

        ShapeBackground bg = new ShapeBackground();
        bg.setColor(0.5f, 0, 0, 1);
        bg.addShape(bg_shape);

        return bg;
    }

    /**
     * Set up the pbuffer as a depth texture that the code will write to with the
     * object from the position of the light
     */
    private OffscreenTexture2D createViewpointDepthMap(VertexGeometry worldGeom,
                                                       Matrix4d viewMatrix,
                                                       int windowWidth,
                                                       int windowHeight)
    {
        // Set up a single render pass that just writes to the depth buffer
        // in inverted mode - we clear to 0 and then want to find the back
        // side of the object. To do that, invert the normal depth testing.
        // Turn off the colour buffer as we don't need it for this rendering.
        // Also, only render the back-facing polygons so that we get the depth
        // rather than front-facing.

        // Set up the capabilities for a 32bit depth-only texture that
        // we'll be rendering to.
        GraphicsRenderingCapabilities caps = new GraphicsRenderingCapabilities();
        caps.doubleBuffered = false;
        caps.useFloatingPointBuffers = true;

        Viewpoint vp = new Viewpoint();

        TransformGroup vp_tx = new TransformGroup();
        vp_tx.setTransform(viewMatrix);
        vp_tx.addChild(vp);

        PolygonAttributes poly_attr = new PolygonAttributes();
        poly_attr.setCulledFace(PolygonAttributes.CULL_FRONT);

        Appearance app = new Appearance();
//        app.setPolygonAttributes(poly_attr);

        Shape3D object = new Shape3D();
        object.setAppearance(app);
        object.setGeometry(worldGeom);

        MatrixUtils utils = new MatrixUtils();

        Matrix4d rot_mat  = new Matrix4d();
        utils.rotateY(PI_4, rot_mat);

        TransformGroup anim_rotation = new TransformGroup();
        anim_rotation.setTransform(rot_mat);
        anim_rotation.addChild(object);

        utils.rotateX(PI_4, rot_mat);

        TransformGroup main_rotation = new TransformGroup();
        main_rotation.setTransform(rot_mat);
        main_rotation.addChild(anim_rotation);

        Group scene_root = new Group();
        scene_root.addChild(main_rotation);
        scene_root.addChild(vp_tx);

        DepthBufferState dbs = new DepthBufferState();
        dbs.setClearBufferState(true);
        dbs.setClearDepth(0);
        dbs.setDepthFunction(DepthBufferState.FUNCTION_GREATER);

        ColorBufferState cbs = new ColorBufferState();
        cbs.setClearBufferState(true);
        cbs.setColorMask(false, false, false, false);

        RenderPass pass = new RenderPass();
        pass.setDepthBufferState(dbs);
        pass.setColorBufferState(cbs);

        pass.setRenderedGeometry(scene_root);
        pass.setActiveView(vp);

        MultipassScene scene = new MultipassScene();
        scene.addRenderPass(pass);

        ViewEnvironment env = scene.getViewEnvironment();
        env.setClipDistance(0.1, 100);

        // Then the basic layer and viewport at the top:
        MultipassViewport view = new MultipassViewport();
        view.setDimensions(0, 0, windowWidth, windowHeight);
        view.setScene(scene);

        // Uncomment this block and comment out the multipass block above
        // to demonstrate the same thing being rendered in a normal
        // non back to front method.
/*

        SimpleScene scene = new SimpleScene();
        scene.setRenderedGeometry(scene_root);
        scene.setActiveView(vp);

        SimpleViewport view = new SimpleViewport();
        view.setDimensions(0, 0, windowWidth, windowHeight);
        view.setScene(scene);
*/
        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(view);

        Layer[] layers = { layer };

        // Use a fixed size depth map for now. This should resize in some
        // proportion to the actual viewport size,and probaly the main
        // window size.
        OffscreenTexture2D texture =
            new OffscreenTexture2D(caps,
                                   windowWidth,
                                   windowHeight,
                                   Texture.FORMAT_DEPTH_COMPONENT);

// Use this version to see it as a colour texture rather than depth.
//                                   Texture.FORMAT_RGBA);

        texture.setClearColor(1, 0, 0, 1);
        texture.setRepaintRequired(true);
        texture.setLayers(layers, 1);
        texture.setBoundaryModeS(Texture.BM_CLAMP_TO_EDGE);
        texture.setBoundaryModeT(Texture.BM_CLAMP_TO_EDGE);
        texture.setCompareMode(Texture.COMPARE_MODE_NONE);
        texture.setCompareFunction(Texture.COMPARE_FUNCTION_LEQUAL);
        texture.setDepthFormat(Texture.FORMAT_LUMINANCE);
        texture.setMinFilter(Texture.MINFILTER_BASE_LEVEL_LINEAR);

        return texture;
    }

    public static void main(String[] args)
    {
        DepthTextureFBODemo demo = new DepthTextureFBODemo();
        demo.setVisible(true);
    }
}
