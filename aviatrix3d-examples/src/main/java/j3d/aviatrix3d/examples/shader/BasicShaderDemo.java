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
 * Example application that demonstrates how to put together a single-threaded
 * rendering system.
 *
 * @author Justin Couch
 * @version $Revision: 1.16 $
 */
public class BasicShaderDemo extends Frame
    implements WindowListener
{
    /** List of available vertex shaders for the geometry */
    private static final String[] VTX_SHADER_FILE =
    {
        "demo_shaders/SimpleVert.txt",
        "demo_shaders/PhongVert.txt",
        "demo_shaders/MarbleVert.txt",
        "demo_shaders/BrickVert.txt",
        null
    };

    /** List of available fragment shaders for the geometry */
    private static final String[] FRAG_SHADER_FILE =
    {
        "demo_shaders/SimpleFrag.txt",
        "demo_shaders/PhongFrag.txt",
        "demo_shaders/MarbleFrag.txt",
        "demo_shaders/BrickFrag.txt",
        "demo_shaders/PaintedPlasticFrag.txt"
    };

    /** List of texture files needed for each shader */
    private static final String[][] TEXTURE_FILES =
    {
        { "textures/mr_smiley.png" },
        {},
        {"textures/noisef128.png", "textures/colSpline.png"},
        {},
    };

    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    /** The shader for vertex section */
    private VertexShader vtxShader;

    /** The shader for fragment processing */
    private FragmentShader fragShader;

    /**
     * Construct a new shader demo instance.
     */
    public BasicShaderDemo()
    {
        super("Basic Aviatrix Demo illustrating OpenGL Shaders");

        setLayout(new BorderLayout());
        addWindowListener(this);

        setupAviatrix();
        setupSceneGraph(1);

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
        sceneManager.setMinimumFrameInterval(100);

        // Before putting the pipeline into run mode, put the canvas on
        // screen first.
        Component comp = (Component)surface.getSurfaceObject();
        add(comp, BorderLayout.CENTER);
    }

    /**
     * Setup the basic scene which consists of a quad and a viewpoint
     */
    private void setupSceneGraph(int shaderNum)
    {
        TextureUnit[] textures = null;
        int num_textures = TEXTURE_FILES[shaderNum].length;

        if(num_textures != 0)
        {
            textures = new TextureUnit[num_textures];
            for(int i = 0; i < num_textures; i++)
                textures[i] = loadImage(TEXTURE_FILES[shaderNum][i]);
        }

        // View group
        Viewpoint vp = new Viewpoint();

        Vector3d trans = new Vector3d();
        trans.set(0, 0, 2f);

        Matrix4d mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);

        // Sphere to render the shader onto
        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA|
                                  GeometryData.TEXTURE_2D_DATA;

        SphereGenerator generator = new SphereGenerator(64, 50);
        generator.generate(data);

        int[] tex_type =
        {
            VertexGeometry.TEXTURE_COORDINATE_2,
            VertexGeometry.TEXTURE_COORDINATE_3
        };

        float[][] tex_coord =
        {
            new float[data.vertexCount * 2],
            new float[data.vertexCount * 3]
        };

        int[] tex_sets = { 0, 1, 1 };

        System.arraycopy(data.textureCoordinates, 0, tex_coord[0], 0,
                         data.vertexCount * 2);

        data.geometryComponents = GeometryData.TEXTURE_3D_DATA;
        data.textureCoordinates = null;

        generator.generate(data);

        System.arraycopy(data.textureCoordinates, 0, tex_coord[1], 0,
                         data.vertexCount * 3);

        TriangleArray geom = new TriangleArray();
        geom.setValidVertexCount(data.vertexCount);
        geom.setVertices(TriangleArray.COORDINATE_3, data.coordinates);
        geom.setNormals(data.normals);
        geom.setTextureCoordinates(tex_type, tex_coord, 2);
        geom.setTextureSetMap(tex_sets);

        Material material = new Material();
        material.setDiffuseColor(new float[] { 0, 0, 1 });
        material.setEmissiveColor(new float[] { 0, 0, 1 });
        material.setSpecularColor(new float[] { 1, 1, 1 });

        String shader_txt = loadFile(VTX_SHADER_FILE[shaderNum]);

        vtxShader = new VertexShader(1, 5);
        vtxShader.setProgramString(shader_txt);

        shader_txt = loadFile(FRAG_SHADER_FILE[shaderNum]);
        fragShader = new FragmentShader();
        fragShader.setProgramString(shader_txt);

        GL14Shader shader = new GL14Shader();
        shader.setVertexShader(vtxShader);
        shader.setFragmentShader(fragShader);

        Appearance app = new Appearance();
        app.setTextureUnits(textures, num_textures);
        app.setMaterial(material);
        app.setShader(shader);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);

        trans.set(0, 0, -8);
        Matrix4d mat2 = new Matrix4d();
        mat2.setIdentity();
        mat2.setTranslation(trans);
        mat2.setScale(0.05f);

        TransformGroup shape_tx = new TransformGroup();
        shape_tx.addChild(shape);
        shape_tx.setTransform(mat2);

        scene_root.addChild(shape_tx);

        SimpleScene scene = new SimpleScene();
        scene.setRenderedGeometry(scene_root);
        scene.setActiveView(vp);

        // Then the basic layer and viewport at the top:
        SimpleViewport view = new SimpleViewport();
        view.setDimensions(0, 0, 500, 500);
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
     * @param name The name of the file to load
     */
    private String loadFile(String name)
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

        return ret_val;
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Load a single image
     */
    private TextureUnit loadImage(String name)
    {
        TextureComponent2D comp = null;

        try
        {
            File f = new File(name);
            if(!f.exists())
                System.out.println("Can't find texture source file");

            FileInputStream is = new FileInputStream(f);

            BufferedInputStream stream = new BufferedInputStream(is);
            BufferedImage img = ImageIO.read(stream);

            if(img == null)
                return null;

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

            comp = new ImageTextureComponent2D(format,
                                               img_width,
                                               img_height,
                                               img);
        }
        catch(IOException ioe)
        {
            System.out.println("Error reading image: " + ioe);
        }

        TextureComponent2D[] img_comp = { comp };

        Texture2D texture = new Texture2D();
        texture.setSources(Texture.MODE_BASE_LEVEL,
                          Texture.FORMAT_RGBA,
                          img_comp,
                          1);

        TextureUnit tu = new TextureUnit();
        tu.setTexture(texture);

        return tu;
    }

    public static void main(String[] args)
    {
        BasicShaderDemo demo = new BasicShaderDemo();
        demo.setVisible(true);
    }
}
