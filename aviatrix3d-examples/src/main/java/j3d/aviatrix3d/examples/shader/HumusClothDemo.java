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
import org.j3d.geom.SphereGenerator;

/**
 * Demo that is a port of Humus' cloth falling over spheres code.
 *
 * The original demo and code can be found here:
 * http://esprit.campus.luth.se/~humus/3D/index.php?page=OpenGL
 */
public class HumusClothDemo extends Frame
    implements WindowListener
{
    /** Names of the shader vertex file for the sphere */
    private static final String SPHERE_VERTEX_SHADER =
        "demo_shaders/humus_cloth_sphere.vert";

    /** Names of the shader fragment file for the sphere */
    private static final String SPHERE_FRAG_SHADER =
        "demo_shaders/humus_cloth_sphere.frag";

    /** Names of the shader vertex file for the sphere */
    private static final String LIGHTING_VERTEX_SHADER =
        "demo_shaders/humus_cloth_lighting.vert";

    /** Names of the shader fragment file for the sphere */
    private static final String LIGHTING_FRAG_SHADER =
        "demo_shaders/humus_cloth_lighting.frag";

    /** Shader program shared between all the spheres */
    private ShaderProgram sphereProgram;

    /** Shared geometry for the spheres */
    private IndexedTriangleStripArray sphereGeom;

    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    /** Generator used to create our spheres */
    private SphereGenerator sphereGen;

    /** Create a new demo */
    public HumusClothDemo()
    {
        super("Aviatrix3D Port of Humus Cloth Demo");

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

        float angle_x = -0.48f;
        float angle_y = -0.25f;
        float angle_z = 0;
        float A       = (float)Math.cos(angle_x);
        float B       = (float)Math.sin(angle_x);
        float C       = (float)Math.cos(angle_y);
        float D       = (float)Math.sin(angle_y);
        float E       = (float)Math.cos(angle_z);
        float F       = (float)Math.sin(angle_z);
        float AD      = A * D;
        float BD      = B * D;

        Matrix4d mat = new Matrix4d();
        mat.m00 =   C * E;
        mat.m01 =  -C * F;
        mat.m02 =   D;
        mat.m03 = 0;
        mat.m10 =  BD * E + A * F;
        mat.m11 = -BD * F + A * E;
        mat.m12 =  -B * C;
        mat.m13 = 0;
        mat.m20 = -AD * E + B * F;
        mat.m21 =  AD * F + B * E;
        mat.m22 =  A * C;
        mat.m23 = 0;
        mat.m30 = 0;
        mat.m31 = 0;
        mat.m32 = 0;
        mat.m33 = 1;

        // We could set these directly, but we don't because we'd like to
        // pass the values through to the shaders as well. More convenient and
        // we guarantee the same values then.
        Vector3d trans = new Vector3d();
        trans.set(-120, 100, 350);
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);

        // Run through the flags directory and load every texture found into
        // the flags array as texture objects.
        File flags_dir = new File("textures/flags");
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
        TransformGroup[] sphere_tx_list = new TransformGroup[nSpheres];
        float[] colour = { 1, 1, 1, 1 };
        for(int i = 0; i < nSpheres; i++)
        {
            sphere_tx_list[i] = createSphere(trans, 1, colour);
            scene_root.addChild(sphere_tx_list[i]);
        }

        // 4 little spheres for the corners of the cloth
        TransformGroup[] corner_tx_list = new TransformGroup[4];
        colour[0] = 0.3f;
        colour[1] = 0.3f;

        for(int i = 0; i < 4; i++)
        {
            corner_tx_list[i] = createSphere(trans, 5, colour);
            scene_root.addChild(corner_tx_list[i]);
        }

        TransformGroup cloth_tx = createCloth(trans);
        TransformGroup light_tx = createLight();

        scene_root.addChild(cloth_tx);
        scene_root.addChild(light_tx);

        SimpleScene scene = new SimpleScene();
        scene.setRenderedGeometry(scene_root);
        scene.setActiveView(vp);

        HumusClothAnimator anim =
            new HumusClothAnimator(flag_textures,
                                   cloth_tx,
                                   light_tx,
                                   sphere_tx_list,
                                   corner_tx_list);
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
    private TransformGroup createSphere(Vector3d viewPos,
                                        float radius,
                                        float[] colour)
    {
        if(sphereProgram == null)
        {
            sphereProgram = new ShaderProgram();
            ShaderObject vert_object = new ShaderObject(true);
            String[] source = loadShaderSource(SPHERE_VERTEX_SHADER);
            vert_object.setSourceStrings(source, 1);
            vert_object.compile();

            ShaderObject frag_object = new ShaderObject(false);
            source = loadShaderSource(SPHERE_FRAG_SHADER);
            frag_object.setSourceStrings(source, 1);
            frag_object.compile();

            sphereProgram.addShaderObject(vert_object);
            sphereProgram.addShaderObject(frag_object);
            sphereProgram.link();
        }

        if((sphereGen == null) || sphereGen.getDimension() != radius)
        {
            sphereGen = new SphereGenerator(radius, 64);
            GeometryData data = new GeometryData();
            data.geometryType = GeometryData.INDEXED_TRIANGLE_STRIPS;
            data.geometryComponents = GeometryData.NORMAL_DATA |
                                      GeometryData.TEXTURE_2D_DATA;

            sphereGen.generate(data);

            int[] tex_type = { VertexGeometry.TEXTURE_COORDINATE_2 };
            float[][] tex_coord = new float[1][data.vertexCount * 2];

            System.arraycopy(data.textureCoordinates, 0, tex_coord[0], 0,
                             data.vertexCount * 2);

            sphereGeom = new IndexedTriangleStripArray();
            sphereGeom.setVertices(TriangleArray.COORDINATE_3,
                                   data.coordinates,
                                   data.vertexCount);
            sphereGeom.setIndices(data.indexes, data.indexesCount);
            sphereGeom.setStripCount(data.stripCounts, data.numStrips);
            sphereGeom.setNormals(data.normals);
            sphereGeom.setTextureCoordinates(tex_type, tex_coord, 1);
        }

        ShaderArguments args = new ShaderArguments();
        float[] f_args = new float[4];
        args.setUniform("spherePos", 3, f_args, 1);
        args.setUniform("lightPos", 3, f_args, 1);
        args.setUniform("color", 4, colour, 1);

        f_args[0] = (float)viewPos.x;
        f_args[1] = (float)viewPos.y;
        f_args[2] = (float)viewPos.z;
        args.setUniform("camPos", 3, f_args, 1);

        f_args[0] = 1;
        args.setUniform("sphereSize", 1, f_args, 1);

        GLSLangShader shader = new GLSLangShader();
        shader.setShaderArguments(args);
        shader.setShaderProgram(sphereProgram);

        Appearance app = new Appearance();
        app.setVisible(false);
        app.setShader(shader);

        Shape3D shape = new Shape3D();
        shape.setAppearance(app);
        shape.setGeometry(sphereGeom);

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

        // Create a new empty texture unit. It will have the texture set each
        // time the update changes.
        TextureUnit[] tex_units = { new TextureUnit() };

        ShaderProgram program = new ShaderProgram();
        ShaderObject vert_object = new ShaderObject(true);
        String[] source = loadShaderSource(LIGHTING_VERTEX_SHADER);
        vert_object.setSourceStrings(source, 1);
        vert_object.compile();

        ShaderObject frag_object = new ShaderObject(false);
        source = loadShaderSource(LIGHTING_FRAG_SHADER);
        frag_object.setSourceStrings(source, 1);
        frag_object.compile();

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
        int[] tex_pos = new int[1];
        args.setUniform("Base", 1, tex_pos, 1);

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

        File light_file = new File("textures/humus_particle.png");
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
        HumusClothDemo demo = new HumusClothDemo();
        demo.setVisible(true);
    }
}
