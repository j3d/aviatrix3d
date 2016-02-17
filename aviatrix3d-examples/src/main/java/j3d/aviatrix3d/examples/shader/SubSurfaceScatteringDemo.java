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
import org.j3d.maths.vector.Point3d;
import org.j3d.maths.vector.Vector3d;

// Local imports
import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.pipeline.graphics.*;

import org.j3d.aviatrix3d.output.graphics.DebugAWTSurface;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;


import org.j3d.geom.GeometryData;
import org.j3d.geom.BoxGenerator;
import org.j3d.geom.SphereGenerator;
import org.j3d.util.DataUtils;
import org.j3d.util.MatrixUtils;
import org.j3d.loaders.stl.STLFileReader;

/**
 * Example application showing off a simple sub surface scattering demo. Code
 * and ideas are sourced from the depth map version described in Ch 16 of
 * GPU Gems #1.
 *
 * @see http://http.developer.nvidia.com/GPUGems/gpugems_ch16.html
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public class SubSurfaceScatteringDemo extends Frame
    implements WindowListener
{
    /** Vertex shader file name for the depth pass */
    private static final String DEPTH_PASS_VTX_SHADER_FILE =
        "shaders/examples/subsurf/depth_pass_vert.glsl";

    /** Fragment shader file name for the depth pass */
    private static final String DEPTH_PASS_FRAG_SHADER_FILE =
        "shaders/examples/subsurf/depth_pass_frag.glsl";

    /** Render pass vertex shader string */
    private static final String RENDER_PASS_VERTEX_SHADER_FILE =
        "shaders/examples/subsurf/render_pass_vert.glsl";

    /** Fragment shader file name for the rendering pass */
    private static final String RENDER_PASS_FRAG_SHADER_FILE =
        "shaders/examples/subsurf/render_pass_frag.glsl";

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

    /** Matrix utils for doing rotations */
    private MatrixUtils matrixUtils;

    /**
     * Construct a new shader demo instance.
     */
    public SubSurfaceScatteringDemo()
    {
        super("Subsurface scattering Demo");

        setLayout(new BorderLayout());
        addWindowListener(this);

        matrixUtils = new MatrixUtils();

        setupAviatrix();

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
//        surface = new SimpleAWTSurface(caps);
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
    private void load(String filename)
    {
        Background bg = createBackground();

        // View group
        Viewpoint vp = new Viewpoint();

        Vector3d trans = new Vector3d();
        trans.set(0, 0, 75f);

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

        Point3d light_pos = new Point3d();
        light_pos.set(50, 0, -50);

        Vector3d light_posv = new Vector3d();
        light_posv.set(light_pos.x, light_pos.y, light_pos.z);

        Matrix4d mat = new Matrix4d();
        mat.set(light_posv);


        TransformGroup light_group = new TransformGroup();
        light_group.setTransform(mat);
        light_group.addChild(light_shape);

        scene_root.addChild(light_group);

        SubSurfaceAnimator anim = new SubSurfaceAnimator(light_group);

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

        // Variables passed as uniforms to the shader. Not all are initialised
        // straight away. Several are filled in by the
        float[] sigma = { 0.00008f };
        float[] rim_scale = { 0.001f };
        float[] max_trans_depth = { 0.01f };
        float[] depth_scale = { 0.1f };
        float[] light_colour = { 0.5f, 0.5f, 0.5f, 1.0f };
        float[] object_base_colour = { 0.8f, 0.8f, 0.1f, 1.0f };
        float[] object_specular_colour = { 0.9f, 0.9f, 0.4f, 1.0f };
        float[] extinction_coeff = { 0.5f, 0.5f, 0.5f, 0 };
        float[] specular_coeff = { 0.1f };
        float[] depth_texture_size = { WINDOW_SIZE, WINDOW_SIZE };

        float[] light_proj_mat = new float[16];

        float[] light_pos_mat =
        {
            1, 0, 0, (float)light_pos.x,
            0, 1, 0, (float)light_pos.y,
            0, 0, 1, (float)light_pos.z,
            0, 0, 0, 1
        };

        ShaderArguments shader_args = new ShaderArguments();
        shader_args.setUniform("sigma", 1, sigma, 1);
        shader_args.setUniform("rimScalar", 1, rim_scale, 1);
        shader_args.setUniformSampler("lightDepthTexture", 0);
        shader_args.setUniformSampler("viewerDepthTexture", 1);
        shader_args.setUniform("lightColor", 4, light_colour, 1);
        shader_args.setUniform("objectBaseColor", 4, object_base_colour, 1);
        shader_args.setUniform("objectSpecularColor", 4, object_specular_colour, 1);
        shader_args.setUniform("extinctionCoefficient", 4, extinction_coeff, 1);
        shader_args.setUniform("specularCoefficient", 1, specular_coeff, 1);
        shader_args.setUniform("maxTransparentDepth", 1, max_trans_depth, 1);
        shader_args.setUniform("viewDepthTextureSize", 2, depth_texture_size, 1);
        shader_args.setUniformMatrix("lightPosMatrix", 4, light_pos_mat, 1, false);

        GLSLangShader shader = new GLSLangShader();
        shader.setShaderProgram(shader_prog);
        shader.setShaderArguments(shader_args);

        data.coordinates = null;
        data.normals = null;


        // Sphere to render the shader onto
        VertexGeometry output_geom = null;
        File file = new File(filename);

        try
        {
            STLFileReader reader = new STLFileReader(file);

            int[] num_faces = reader.getNumOfFacets();
            double[][] tri_vertex = new double[3][3];
            double[] tri_normal = new double[3];

            float[] normals = new float[num_faces[0] * 9];
            float[] coords = new float[num_faces[0] * 9];
            int idx = 0;

            for(int i = 0; i < num_faces[0]; i++)
            {
                if(!reader.getNextFacet(tri_normal, tri_vertex))
                    break;

                coords[idx] = (float)tri_vertex[0][0];
                coords[idx + 1] = (float)tri_vertex[0][1];
                coords[idx + 2] = (float)tri_vertex[0][2];

                coords[idx + 3] = (float)tri_vertex[1][0];
                coords[idx + 4] = (float)tri_vertex[1][1];
                coords[idx + 5] = (float)tri_vertex[1][2];

                coords[idx + 6] = (float)tri_vertex[2][0];
                coords[idx + 7] = (float)tri_vertex[2][1];
                coords[idx + 8] = (float)tri_vertex[2][2];

                normals[idx] =  (float)tri_normal[0];
                normals[idx + 1] =  (float)tri_normal[1];
                normals[idx + 2] =  (float)tri_normal[2];

                normals[idx + 3] =  (float)tri_normal[0];
                normals[idx + 4] =  (float)tri_normal[1];
                normals[idx + 5] =  (float)tri_normal[2];

                normals[idx + 6] =  (float)tri_normal[0];
                normals[idx + 7] =  (float)tri_normal[1];
                normals[idx + 8] =  (float)tri_normal[2];

                idx += 9;
            }

            TriangleArray ta = new TriangleArray();
            ta.setVertices(TriangleArray.COORDINATE_3, coords, num_faces[0] * 3);
            ta.setNormals(normals);

            output_geom = ta;
        }
        catch(IOException ioe)
        {
            System.out.println("unable to load file " + file);
            BoxGenerator box_gen = new BoxGenerator(4f, 4f, 2f);
            box_gen.generate(data);

            TriangleArray box_geom = new TriangleArray();
            box_geom.setValidVertexCount(data.vertexCount);
            box_geom.setVertices(TriangleArray.COORDINATE_3, data.coordinates);
            box_geom.setNormals(data.normals);

            output_geom = box_geom;
        }

        // Debug Flat panel that has the viewable object as the demo
        /*
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
        */

        OffscreenTexture2D light_map = createLightDepthMap(output_geom,
                                                      light_pos,
                                                      light_proj_mat,
                                                      depth_scale,
                                                      anim);

        OffscreenTexture2D depth_map = createViewpointDepthMap(output_geom,
                                                               view_mat,
                                                               anim,
                                                               WINDOW_SIZE,
                                                               WINDOW_SIZE);

        shader_args.setUniformMatrix("lightProjMatrix", 4, light_proj_mat, 1, false);
        shader_args.setUniform("depthScale", 1, depth_scale, 1);

        TextureUnit textures[] = new TextureUnit[2];
        textures[0] = new TextureUnit();
        textures[0].setTexture(light_map);
        textures[1] = new TextureUnit();
        textures[1].setTexture(depth_map);


        Appearance app = new Appearance();
        app.setTextureUnits(textures, 2);
        app.setShader(shader);

        Shape3D shape = new Shape3D();
        shape.setGeometry(output_geom);
//        shape.setGeometry(test_geom);
        shape.setAppearance(app);

        Matrix4d rot_mat  = new Matrix4d();
        matrixUtils.rotateY(PI_4, rot_mat);

        TransformGroup anim_rotation = new TransformGroup();
        anim_rotation.setTransform(rot_mat);
        anim_rotation.addChild(shape);

        matrixUtils.rotateX(PI_4, rot_mat);

        TransformGroup main_rotation = new TransformGroup();
        main_rotation.setTransform(rot_mat);
        main_rotation.addChild(anim_rotation);

        scene_root.addChild(main_rotation);

        anim.setRenderPassArgs(anim_rotation, shader_prog, shader_args);

//        scene_root.addChild(createFlag());

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


//        Layer[] layers = { layer };
//        displayManager.setLayers(layers, 1);

        Layer bg_layer = createBackgroundLayer();
        Layer[] layers = { bg_layer, layer };
        displayManager.setLayers(layers, 2);

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
//        Background bg = new ColorBackground(new float[] { 0.5f, 0, 0, 1});

        ShapeBackground bg = new ShapeBackground();
        bg.addShape(createFlag());

        bg.setColor(0.5f, 0, 0, 1);

        return bg;
    }


    /**
     * Create a polygon with a flag textured on it for test purposes
     */
    private Shape3D createFlag()
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

        File tex_file = DataUtils.lookForFile("images/examples/shader/flags/australia.png", getClass(), null);
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

        return bg_shape;
    }

    /**
     * Set up the pbuffer as a depth texture that the code will write to with the
     * object from the position of the light
     */
    private OffscreenTexture2D createLightDepthMap(VertexGeometry worldGeom,
                                                   Point3d lightPos,
                                                   float[] lightProjMatrix,
                                                   float[] depthScale,
                                                   SubSurfaceAnimator anim)
    {
        // Set up the capabilities for a 32bit depth-only texture that
        // we'll be rendering to.
        GraphicsRenderingCapabilities caps = new GraphicsRenderingCapabilities();
        caps.doubleBuffered = false;
        caps.useFloatingPointBuffers = true;

        // Place this viewpoint in the light's position
        Viewpoint vp = new Viewpoint();

        Matrix4d vp_mat = new Matrix4d();

        Vector3d up = new Vector3d();
        up.y = 1;

        MatrixUtils utils = new MatrixUtils();
        utils.lookAt(lightPos, new Point3d(), up, vp_mat);

        utils.inverse(vp_mat, vp_mat);

        TransformGroup vp_tx = new TransformGroup();
        vp_tx.setTransform(vp_mat);
        vp_tx.addChild(vp);

        String[] vert_shader_txt = loadShaderFile(DEPTH_PASS_VTX_SHADER_FILE);
        String[] frag_shader_txt = loadShaderFile(DEPTH_PASS_FRAG_SHADER_FILE);

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

        float[] blur = { 0.005f };

        ShaderArguments shader_args = new ShaderArguments();
        shader_args.setUniform("grow", 1, blur, 1);

        GLSLangShader shader = new GLSLangShader();
        shader.setShaderProgram(shader_prog);
        shader.setShaderArguments(shader_args);

        Appearance app = new Appearance();
        app.setShader(shader);

        Shape3D object = new Shape3D();
        object.setAppearance(app);
        object.setGeometry(worldGeom);


        Matrix4d rot_mat  = new Matrix4d();
        matrixUtils.rotateY(PI_4, rot_mat);

        TransformGroup anim_rotation = new TransformGroup();
        anim_rotation.setTransform(rot_mat);
        anim_rotation.addChild(object);

        matrixUtils.rotateX(PI_4, rot_mat);

        TransformGroup main_rotation = new TransformGroup();
        main_rotation.setTransform(rot_mat);
        main_rotation.addChild(anim_rotation);

        Group scene_root = new Group();
        scene_root.addChild(main_rotation);
        scene_root.addChild(vp_tx);

        anim.setLightDepthPassParams(anim_rotation, shader_prog, shader_args);


        SimpleScene scene = new SimpleScene();
        scene.setRenderedGeometry(scene_root);
        scene.setActiveView(vp);

        // Then the basic layer and viewport at the top:
        SimpleViewport view = new SimpleViewport();
        view.setDimensions(0, 0, TEXTURE_SIZE, TEXTURE_SIZE);
        view.setScene(scene);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(view);

        Layer[] layers = { layer };

        ViewEnvironment view_env = scene.getViewEnvironment();
        view_env.setFarClipDistance(100);
        view_env.getProjectionMatrix(lightProjMatrix);

        depthScale[0] = 1 / (float)view_env.getFarClipDistance();
        shader_args.setUniform("depthScaleFactor", 1, depthScale, 1);

        // Copy this matrix into a temp matrix, then multiply it by the projection
        // matrix to get the final proj matrix for the light, finally copying it
        // back in to the original array to hand back to the shader
        Matrix4d light_mat = new Matrix4d();
        light_mat.m00 = lightProjMatrix[0];
        light_mat.m01 = lightProjMatrix[1];
        light_mat.m02 = lightProjMatrix[2];
        light_mat.m03 = lightProjMatrix[3];

        light_mat.m10 = lightProjMatrix[4];
        light_mat.m11 = lightProjMatrix[5];
        light_mat.m12 = lightProjMatrix[6];
        light_mat.m13 = lightProjMatrix[7];

        light_mat.m20 = lightProjMatrix[8];
        light_mat.m21 = lightProjMatrix[9];
        light_mat.m22 = lightProjMatrix[10];
        light_mat.m23 = lightProjMatrix[11];

        light_mat.m30 = lightProjMatrix[12];
        light_mat.m31 = lightProjMatrix[13];
        light_mat.m32 = lightProjMatrix[14];
        light_mat.m33 = lightProjMatrix[15];

        vp_mat.mul(light_mat, vp_mat);

        lightProjMatrix[0] = (float)vp_mat.m00;
        lightProjMatrix[1] = (float)vp_mat.m01;
        lightProjMatrix[2] = (float)vp_mat.m02;
        lightProjMatrix[3] = (float)vp_mat.m03;

        lightProjMatrix[4] = (float)vp_mat.m10;
        lightProjMatrix[5] = (float)vp_mat.m11;
        lightProjMatrix[6] = (float)vp_mat.m12;
        lightProjMatrix[7] = (float)vp_mat.m13;

        lightProjMatrix[8] = (float)vp_mat.m20;
        lightProjMatrix[9] = (float)vp_mat.m21;
        lightProjMatrix[10] = (float)vp_mat.m22;
        lightProjMatrix[11] = (float)vp_mat.m23;

        lightProjMatrix[12] = (float)vp_mat.m30;
        lightProjMatrix[13] = (float)vp_mat.m31;
        lightProjMatrix[14] = (float)vp_mat.m32;
        lightProjMatrix[15] = (float)vp_mat.m33;

        // Use a fixed size depth map for now. This should resize in some
        // proportion to the actual viewport size,and probaly the main
        // window size.
        OffscreenTexture2D texture =
            new OffscreenTexture2D(caps,
                                   TEXTURE_SIZE,
                                   TEXTURE_SIZE,
                                   Texture.FORMAT_RGBA);

        texture.setClearColor(0, 0, 1, 1);
        texture.setRepaintRequired(true);
        texture.setLayers(layers, 1);
        texture.setBoundaryModeS(Texture.BM_CLAMP_TO_EDGE);
        texture.setBoundaryModeT(Texture.BM_CLAMP_TO_EDGE);
        texture.setCompareMode(Texture.COMPARE_MODE_NONE);
        texture.setMinFilter(Texture.MINFILTER_BASE_LEVEL_LINEAR);

        return texture;
    }

    /**
     * Set up the pbuffer as a depth texture that the code will write to with the
     * object from the position of the light
     */
    private OffscreenTexture2D createViewpointDepthMap(VertexGeometry worldGeom,
                                                       Matrix4d viewMatrix,
                                                       SubSurfaceAnimator anim,
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

        Viewpoint vp = new Viewpoint();

        TransformGroup vp_tx = new TransformGroup();
        vp_tx.setTransform(viewMatrix);
        vp_tx.addChild(vp);

        PolygonAttributes poly_attr = new PolygonAttributes();
        poly_attr.setCulledFace(PolygonAttributes.CULL_FRONT);

        Appearance app = new Appearance();
        app.setPolygonAttributes(poly_attr);

        Shape3D object = new Shape3D();
        object.setAppearance(app);
        object.setGeometry(worldGeom);

        Matrix4d rot_mat  = new Matrix4d();
        matrixUtils.rotateY(PI_4, rot_mat);

        TransformGroup anim_rotation = new TransformGroup();
        anim_rotation.setTransform(rot_mat);
        anim_rotation.addChild(object);

        matrixUtils.rotateX(PI_4, rot_mat);

        TransformGroup main_rotation = new TransformGroup();
        main_rotation.setTransform(rot_mat);
        main_rotation.addChild(anim_rotation);

        Group scene_root = new Group();
        scene_root.addChild(main_rotation);
        scene_root.addChild(vp_tx);

        anim.setCameraDepthPassParams(anim_rotation);

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

    /**
     * Create a simple layer for the background
     */
    private Layer createBackgroundLayer()
    {
        Viewpoint vp = new Viewpoint();
        vp.setHeadlightEnabled(true);

        Vector3d trans = new Vector3d();
        trans.set(0, 0, 10);

        Matrix4d mat = new Matrix4d();
        mat.set(trans);

        TransformGroup viewpointTransform = new TransformGroup();
        viewpointTransform.addChild(vp);
        viewpointTransform.setTransform(mat);

        Group sceneRoot = new Group();
        sceneRoot.addChild(viewpointTransform);

        SimpleScene scene = new SimpleScene();

        ViewEnvironment viewEnvironment = scene.getViewEnvironment();
        viewEnvironment.setProjectionType(ViewEnvironment.ORTHOGRAPHIC_PROJECTION);
        viewEnvironment.setOrthoParams(-1, 1, -1, 1);
        viewEnvironment.setClipDistance(1.0, 1000.0);

        // if 6 colors provided then overlay a gradient shape
        sceneRoot.addChild(makeRectangle());

        scene.setRenderedGeometry(sceneRoot);
        scene.setActiveView(vp);

        SimpleViewport viewport = new SimpleViewport();
        viewport.setDimensions(0, 0, WINDOW_SIZE, WINDOW_SIZE);
        viewport.setScene(scene);

        SimpleLayer bg_layer = new SimpleLayer();
        bg_layer.setViewport(viewport);

        return bg_layer;
    }

    /**
     * makeRectangle builds a quadArray shape to create a
     * rectangle with two colors, blended top-to-bottom.
     * @return a Shape3D that can be inserted into the scene.
     */
    private Shape3D makeRectangle()
    {
        /* error handling to catch arrays of incorrect length */
        float[] color =  {0.5f, 0.5f, 0.5f, 0f, 0f, 0f};

        Shape3D rectangle = new Shape3D();
        QuadArray qA = new QuadArray();

        //(-1,1)  __________  (1, 1)
        //       |          |
        //       |          |
        //(-1,-1)|__________| (1, -1)
        float[] f =
        {
             -1f, -1f, 0f,
             1f, -1f, 0f,
             1f,  1f, 0f,
             -1f,  1f, 0f
        };

        float[] gradiantColor =
        {
                color[0], color[1], color[2],
                color[0], color[1], color[2],
                color[3], color[4], color[5],
                color[3], color[4], color[5]
        };

        qA.setVertices(QuadArray.COORDINATE_3, f, f.length / 3);
        qA.setColors(false, gradiantColor);

        rectangle.setGeometry(qA);

        return rectangle;
    }

    public static void main(String[] args)
    {
        SubSurfaceScatteringDemo demo = new SubSurfaceScatteringDemo();
        demo.load(args[0]);
        demo.setVisible(true);
    }
}
