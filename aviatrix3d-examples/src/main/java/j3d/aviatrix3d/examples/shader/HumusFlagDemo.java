/*****************************************************************************
 *                        Yumtech, Inc Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package j3d.aviatrix3d.examples.shader;

// External imports
import java.awt.*;
import java.awt.event.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;

import javax.imageio.ImageIO;
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

import org.j3d.geom.GeometryData;
import org.j3d.geom.CylinderGenerator;
import org.j3d.util.DataUtils;
import org.j3d.util.I18nManager;

/**
 * Demo that is a port of Humus' waving flag demo. Uses most of the same code
 * as the cloth demo, but with some different behaviour to simulate the flag
 * and wind physics.
 *
 * The original demo and code can be found here:
 * http://esprit.campus.luth.se/~humus/3D/index.php?page=OpenGL
 */
public class HumusFlagDemo extends Frame
    implements WindowListener
{
    private static final String APP_NAME = "examples.HumusFlagDemo";

    /** Names of the shader vertex file for the sphere */
    private static final String POLE_VERTEX_SHADER =
        "shaders/examples/simple/humus_flag_pole.vert";

    /** Names of the shader fragment file for the sphere */
    private static final String POLE_FRAG_SHADER =
        "shaders/examples/simple/humus_flag_pole.frag";

    /** Names of the shader vertex file for the sphere */
    private static final String LIGHTING_VERTEX_SHADER =
        "shaders/examples/simple/humus_flag_cloth.vert";

    /** Names of the shader fragment file for the sphere */
    private static final String LIGHTING_FRAG_SHADER =
        "shaders/examples/simple/humus_flag_cloth.frag";

    /** The number of vertices for the cloth in the X direction */
    private static final int CLOTH_SIZE_X = HumusFlagAnimator.CLOTH_SIZE_X;

    /** The number of vertices for the cloth in the T direction */
    private static final int CLOTH_SIZE_Y = HumusFlagAnimator.CLOTH_SIZE_Y;

    /** Value of the real size of the cloth */
    private static final float C_SIZE  = HumusFlagAnimator.C_SIZE;

    /** The texture type constant needed when updating texture coordinates */
    private static final int[] TEX_TYPES =
        { VertexGeometry.TEXTURE_COORDINATE_2 };

    /** Shader program shared between all the spheres */
    private ShaderProgram poleProgram;

    /** Shared geometry for the spheres */
    private IndexedTriangleStripArray sphereGeom;

    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    /** Create a new demo */
    public HumusFlagDemo()
    {
        super("Aviatrix3D Port of Humus Cloth Demo");

        I18nManager intl_mgr = I18nManager.getManager();
        intl_mgr.setApplication(APP_NAME, "config.i18n.org-j3d-aviatrix3d-resources-core");

        setLayout(new BorderLayout());
        addWindowListener(this);

        setupAviatrix();
        setupSceneGraph(3);

        setSize(600, 600);
        setLocation(40, 40);

        // Need to set visible first before starting the rendering thread due
        // to a bug in JOGL. See JOGL Issue #54 for more information on this.
        // http://jogl.dev.java.net
        setVisible(true);
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
     * Setup the avaiatrix pipeline here
     */
    private void setupAviatrix()
    {
        // Assemble a simple single-threaded pipeline.
        GraphicsRenderingCapabilities caps = new GraphicsRenderingCapabilities();

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
        sceneManager.setMinimumFrameInterval(20);

        // Before putting the pipeline into run mode, put the canvas on
        // screen first.
        Component comp = (Component)surface.getSurfaceObject();
        add(comp, BorderLayout.CENTER);
    }

    /**
     * Setup the basic scene which consists of a quad and a viewpoint
     *
     * @param nSpheres Number of sphere objects to pre-initialise
     */
    private void setupSceneGraph(int nSpheres)
    {
        // View group
        Viewpoint vp = new Viewpoint();

        // We could set these directly, but we don't because we'd like to
        // pass the values through to the shaders as well. More convenient and
        // we guarantee the same values then.
        Vector3d trans = new Vector3d();
        trans.set(0, 100, 600);
        Matrix4d mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);

        // Run through the flags directory and load every texture found into
        // the flags array as texture objects.
        File flags_dir = DataUtils.lookForFile("images/examples/shader/flags", getClass(), null);
        File[] files_in_dir = flags_dir.listFiles();
        Texture2D[] flag_textures = new Texture2D[files_in_dir.length];
        int num_flags = 0;

        for(int i = 0; i < files_in_dir.length; i++)
        {
            if (files_in_dir[i].isDirectory())
                continue;

            System.out.println("loading " + files_in_dir[i]);
            TextureComponent2D img_comp = loadImage(files_in_dir[i]);

            if (img_comp != null)
            {
                Texture2D tex = new Texture2D(Texture2D.FORMAT_RGBA, img_comp);
                tex.setBoundaryModeS(Texture.BM_CLAMP_TO_EDGE);
                tex.setBoundaryModeT(Texture.BM_CLAMP_TO_EDGE);

                flag_textures[num_flags++] = tex;
            }
        }

        // Big spheres for the cloth to drape over
        TransformGroup pole_tx = createPole(trans, new float[]{0.9f, 0.9f, 0.9f, 0});
        TransformGroup arrow_tx = createArrow(trans, new float[]{1, 0, 1, 0});
        scene_root.addChild(arrow_tx);
        scene_root.addChild(pole_tx);

        TransformGroup cloth_tx = createCloth(trans);
        TransformGroup light_tx = createLight();

        scene_root.addChild(cloth_tx);
        scene_root.addChild(light_tx);

        SimpleScene scene = new SimpleScene();
        scene.setRenderedGeometry(scene_root);
        scene.setActiveView(vp);

        HumusFlagAnimator anim =
            new HumusFlagAnimator(flag_textures,
                                  cloth_tx,
                                  light_tx,
                                  arrow_tx,
                                  pole_tx);
        sceneManager.setApplicationObserver(anim);

        // Then the basic layer and viewport at the top:
        SimpleViewport view = new SimpleViewport();
        view.setDimensions(0, 0, 500, 500);
        view.setScene(scene);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(view);

        Layer[] layers = { layer };
        displayManager.setLayers(layers, 1);
    }

    /**
     * Create a single sphere object and it's parent transform
     *
     * @param viewPos The fixed position of the viewer
     * @param
     */
    private TransformGroup createPole(Vector3d viewPos,
                                      float[] colour)
    {
        poleProgram = new ShaderProgram();
        ShaderObject vert_object = new ShaderObject(true);
        String[] source = loadShaderSource(POLE_VERTEX_SHADER);
        vert_object.setSourceStrings(source, 1);
        vert_object.compile();

        ShaderObject frag_object = new ShaderObject(false);
        source = loadShaderSource(POLE_FRAG_SHADER);
        frag_object.setSourceStrings(source, 1);
        frag_object.compile();

        poleProgram.addShaderObject(vert_object);
        poleProgram.addShaderObject(frag_object);
        poleProgram.link();

        CylinderGenerator generator = new CylinderGenerator(C_SIZE * (CLOTH_SIZE_Y - 1) * 2 + 10, 5, 64);
        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLE_STRIPS;
        data.geometryComponents = GeometryData.NORMAL_DATA;

        generator.generate(data);

        TriangleStripArray geom = new TriangleStripArray();
        geom.setVertices(TriangleArray.COORDINATE_3,
                               data.coordinates,
                               data.vertexCount);
        geom.setStripCount(data.stripCounts, data.numStrips);
        geom.setNormals(data.normals);

        ShaderArguments args = new ShaderArguments();
        float[] f_args = new float[4];
        args.setUniform("lightPos", 3, f_args, 1);
        args.setUniform("color", 4, colour, 1);

        f_args[0] = (float)viewPos.x;
        f_args[1] = (float)viewPos.y;
        f_args[2] = (float)viewPos.z;
        args.setUniform("camPos", 3, f_args, 1);

        GLSLangShader shader = new GLSLangShader();
        shader.setShaderArguments(args);
        shader.setShaderProgram(poleProgram);

        Appearance app = new Appearance();
        app.setVisible(true);
        app.setShader(shader);

        Shape3D shape = new Shape3D();
        shape.setAppearance(app);
        shape.setGeometry(geom);

        TransformGroup tg = new TransformGroup();
        tg.addChild(shape);

        return tg;
    }

    /**
     * Create an arrow object and it's parent transform
     *
     * @param viewPos The fixed position of the viewer
     * @param
     */
    private TransformGroup createArrow(Vector3d viewPos,
                                       float[] colour)
    {
        float[] normals =
        {
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
        };

        float rad = (float)Math.random() * 0.5f + 0.3f;
        float[] coords =
        {
            rad * 80 + 40,
            C_SIZE * (CLOTH_SIZE_Y - 1) + 5,
            0,
            rad * 80,
            C_SIZE * (CLOTH_SIZE_Y - 1) + 5,
            30,
            rad * 80,
            C_SIZE * (CLOTH_SIZE_Y - 1) + 5,
            10,
            0,
            C_SIZE * (CLOTH_SIZE_Y - 1) + 5,
            10,
            0,
            C_SIZE * (CLOTH_SIZE_Y - 1) + 5,
            -10,
            rad * 80,
            C_SIZE * (CLOTH_SIZE_Y - 1) + 5,
            -10,
            rad * 80,
            C_SIZE * (CLOTH_SIZE_Y - 1) + 5,
            -30,
        };

        int[] fan_counts = { 7 };

        TriangleFanArray geom = new TriangleFanArray();
        geom.setVertices(TriangleFanArray.COORDINATE_3, coords, 7);
        geom.setFanCount(fan_counts, 1);
        geom.setNormals(normals);

        ShaderArguments args = new ShaderArguments();
        float[] f_args = new float[4];
        args.setUniform("lightPos", 3, f_args, 1);
        args.setUniform("color", 4, colour, 1);

        f_args[0] = (float)viewPos.x;
        f_args[1] = (float)viewPos.y;
        f_args[2] = (float)viewPos.z;
        args.setUniform("camPos", 3, f_args, 1);

        GLSLangShader shader = new GLSLangShader();
        shader.setShaderArguments(args);
        shader.setShaderProgram(poleProgram);

        Appearance app = new Appearance();
        app.setVisible(true);
        app.setShader(shader);

        Shape3D shape = new Shape3D();
        shape.setAppearance(app);
        shape.setGeometry(geom);

        TransformGroup tg = new TransformGroup();
        tg.addChild(shape);

        return tg;
    }

    /**
     * Convenience method to make the geometry that represents the cloth.
     *
     * @return The group that contains the cloth geometry
     */
    private TransformGroup createCloth(Vector3d viewPos)
    {
        // Leave this uninitiliazed
        IndexedTriangleStripArray geom = new IndexedTriangleStripArray();

        int vtx_pos = 0;
        int tex_pos = 0;
        float[] vertices = new float[CLOTH_SIZE_Y * CLOTH_SIZE_X * 3];
        float[] normals = new float[CLOTH_SIZE_Y * CLOTH_SIZE_X * 3];
        float[][] texCoords = new float[1][CLOTH_SIZE_Y * CLOTH_SIZE_X * 2];

        int num_index = (CLOTH_SIZE_Y - 1) * CLOTH_SIZE_X * 2;
        int[] c_indicies = new int[num_index];
        int[] strip_len = new int[CLOTH_SIZE_Y - 1];

        for(int i = 0; i < CLOTH_SIZE_Y; i++)
        {
            for(int j = 0; j < CLOTH_SIZE_X; j++)
            {
                vertices[vtx_pos++] = C_SIZE * j;
                vertices[vtx_pos++] = C_SIZE * (CLOTH_SIZE_Y - 1 - i);
                vertices[vtx_pos++] = 0;

                texCoords[0][tex_pos++] = (float)j / (CLOTH_SIZE_X - 1);
                texCoords[0][tex_pos++] = (float)i / (CLOTH_SIZE_Y - 1);
            }
        }


        int idx = 0;
        for(int i = 0; i < CLOTH_SIZE_Y - 1; i++)
        {
            int i_offset = i * CLOTH_SIZE_X * 2;
            int x_offset = i * CLOTH_SIZE_X;

            for(int j = 0; j < CLOTH_SIZE_X; j++)
            {
                c_indicies[idx++] = x_offset + CLOTH_SIZE_X + j;
                c_indicies[idx++] = x_offset + j;
            }
        }

        for(int i = 0; i < CLOTH_SIZE_Y - 1; i++)
            strip_len[i] = CLOTH_SIZE_X * 2;

        geom.setVertices(VertexGeometry.COORDINATE_3,
                                  vertices,
                                  CLOTH_SIZE_Y * CLOTH_SIZE_X);
        geom.setIndices(c_indicies, num_index);
        geom.setStripCount(strip_len, CLOTH_SIZE_Y - 1);
        geom.setNormals(normals);
        geom.setTextureCoordinates(TEX_TYPES, texCoords, 1);

        // Create a new empty texture unit. It will have the texture set each
        // time the update changes.
        TextureUnit[] tex_units = { new TextureUnit() };

        ShaderObject vert_object = new ShaderObject(true);
        String[] source = loadShaderSource(LIGHTING_VERTEX_SHADER);
        vert_object.setSourceStrings(source, 1);
        vert_object.compile();

        ShaderObject frag_object = new ShaderObject(false);
        source = loadShaderSource(LIGHTING_FRAG_SHADER);
        frag_object.setSourceStrings(source, 1);
        frag_object.compile();

        ShaderProgram program = new ShaderProgram();
        program.addShaderObject(vert_object);
        program.addShaderObject(frag_object);
        program.link();

        ShaderArguments args = new ShaderArguments();
        float[] f_args = new float[4];
        args.setUniform("lightPos", 3, f_args, 1);

        f_args[0] = (float)viewPos.x;
        f_args[1] = (float)viewPos.y;
        f_args[2] = (float)viewPos.z;

        args.setUniform("camPos", 3, f_args, 1);

        // Need to tell it that texture unit 0 is a sampler named Base
        int[] tex_sampler = new int[1];
        args.setUniform("Base", 1, tex_sampler, 1);

        GLSLangShader cloth_shader = new GLSLangShader();
        cloth_shader.setShaderProgram(program);
        cloth_shader.setShaderArguments(args);

        PolygonAttributes pa = new PolygonAttributes();
        pa.setCulledFace(PolygonAttributes.CULL_NONE);

        Appearance app = new Appearance();
        app.setTextureUnits(tex_units, 1);
        app.setShader(cloth_shader);
        app.setPolygonAttributes(pa);

        Shape3D shape = new Shape3D();
        shape.setAppearance(app);
        shape.setGeometry(geom);

        TransformGroup tg = new TransformGroup();
        tg.addChild(shape);

        return tg;
    }

    /**
     * Convenience method to make the geometry that represents a light, though
     * is not a real light.
     *
     * @return The group that contains the light geometry
     */
    private TransformGroup createLight()
    {
        float[] coords = { 0, 0, 0,  1, 0, 0,  1, 1, 0,  0, 1, 0 };
        float[][] tex_coords = { { 0, 0,  1, 0,  1, 1,  0, 1 } };
        int[] tex_type = { VertexGeometry.TEXTURE_COORDINATE_2 };

        QuadArray geom = new QuadArray();
        geom.setVertices(QuadArray.COORDINATE_3,coords, 4);
        geom.setTextureCoordinates(tex_type, tex_coords, 1);

        File light_file = DataUtils.lookForFile("images/examples/shader/humus_particle.png", getClass(), null);
        TextureComponent2D img_comp = loadImage(light_file);
        Texture2D tex = new Texture2D(Texture2D.FORMAT_RGBA, img_comp);
        tex.setBoundaryModeS(Texture.BM_CLAMP_TO_EDGE);
        tex.setBoundaryModeT(Texture.BM_CLAMP_TO_EDGE);

        TextureUnit[] tex_units = { new TextureUnit() };
        tex_units[0].setTexture(tex);

        Appearance app = new Appearance();
        app.setTextureUnits(tex_units, 1);

        Shape3D shape = new Shape3D();
        shape.setAppearance(app);
        shape.setGeometry(geom);

        TransformGroup tg = new TransformGroup();
        tg.addChild(shape);

        return tg;
    }

    /**
     * Load a shader source file into an array of strings.
     *
     * @param file The name of the file to load
     * @return the strings that represent the source.
     */
    private String[] loadShaderSource(String file)
    {
        File f = new File(file);
        if(!f.exists())
        {
            System.out.println("Can't find shader " + file);
            return null;
        }

        String str = null;

        try
        {
            FileInputStream fis = new FileInputStream(f);
            byte[] raw_chars = new byte[(int)f.length()];
            byte[] readbuf = new byte[1024];
            int bytes_read = 0;
            int read_offset = 0;

            while((bytes_read = fis.read(readbuf, 0, 1024)) != -1)
            {
                System.arraycopy(readbuf, 0, raw_chars, read_offset, bytes_read);
                read_offset += bytes_read;
            }

                str = new String(raw_chars);
        }
        catch(IOException ioe)
        {
            System.out.println("error reading shader file " + ioe.getMessage());
            return null;
        }

        return new String[] { str };
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

    public static void main(String[] args)
    {
        HumusFlagDemo demo = new HumusFlagDemo();
        demo.setVisible(true);
    }
}
