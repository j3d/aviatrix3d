
// External imports
import java.awt.*;
import java.awt.event.*;

import java.io.*;
import java.io.IOException;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import javax.media.opengl.GLCapabilities;

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

import org.j3d.texture.procedural.PerlinNoiseGenerator;

/**
 * Example application that demonstrates a shader using fragment shading and
 * 3D textures to get an arc of lighting effect.
 * <p>
 *
 * The source of this code is directly pulled from:
 * <a href="http://esprit.campus.luth.se/~humus/">http://esprit.campus.luth.se/~humus/</a>
 *
 * @author Justin Couch
 * @version $Revision: 1.13 $
 */
public class ElectroEffectDemo extends Frame
    implements WindowListener
{
    private static final int XSIZE = 128;
    private static final int YSIZE = 32;
    private static final int ZSIZE = 32;
    private static final float XSCALE = 0.08f;
    private static final float YSCALE = 0.16f;
    private static final float ZSCALE = 0.16f;

    /** Fragment shaders file name */
    private static final String FRAG_SHADER_FILE = "demo_shaders/electro.fp";

    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    /** The shader for fragment processing */
    private FragmentShader fragShader;

    /**
     * Construct a new shader demo instance.
     */
    public ElectroEffectDemo()
    {
        super("OpenGL Shaders: Fragment + 3D Texture");

        setLayout(new BorderLayout());
        addWindowListener(this);

        setupAviatrix();
        setupSceneGraph(0);

        setSize(500, 500);
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
        sceneManager.setMinimumFrameInterval(0);

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
        PerlinNoiseGenerator noise_gen = new PerlinNoiseGenerator();

        int Size = XSIZE * YSIZE * ZSIZE;

        int[] noise_buffer = new int[Size];
        byte[] tex_buffer = new byte[Size];
        int min = 255;
        int max = 0;
        int pos = 0;

        for(int z = 0; z < ZSIZE; z++) {
            for(int y = 0; y < YSIZE; y++) {
                for(int x = 0; x < XSIZE; x++) {
                    double noise = noise_gen.tileableTurbulence3(XSCALE * x, YSCALE * y, ZSCALE * z, XSIZE * XSCALE, YSIZE * YSCALE, ZSIZE * ZSCALE, 16);

                    int t = (int)(127.5f * (1.0f + noise));

                    if(t > max)
                        max = t;
                    if(t < min)
                        min = t;

                    noise_buffer[pos++] = t;
                }
            }
        }

        for(int i = 0; i < Size; i++) {
        	tex_buffer[i] = (byte)((255 * (noise_buffer[i] - min)) / (max - min));
        }

        ByteTextureComponent3D image = new ByteTextureComponent3D(TextureComponent.FORMAT_SINGLE_COMPONENT,
									                              XSIZE,
									                              YSIZE,
									                              ZSIZE,
									                              tex_buffer);

        Texture3D tex = new Texture3D(Texture.FORMAT_INTENSITY, image);
        tex.setMagFilter(Texture.MAGFILTER_NICEST);
        tex.setMinFilter(Texture.MINFILTER_NICEST);
        tex.setBoundaryModeT(Texture.BM_WRAP);
        tex.setBoundaryModeR(Texture.BM_WRAP);
        tex.setBoundaryModeS(Texture.BM_WRAP);

        TextureUnit[] textures = { new TextureUnit() };
        textures[0].setTexture(tex);

        // View group
        Viewpoint vp = new Viewpoint();

        Vector3f trans = new Vector3f(0, 0, 3.0f);

        Matrix4f mat = new Matrix4f();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);

        TriangleStripArray geom = new TriangleStripArray();

        String shader_txt = loadFile(FRAG_SHADER_FILE);
        fragShader = new FragmentShader();
        fragShader.setProgramString(shader_txt);

        GL14Shader shader = new GL14Shader();
        shader.setFragmentShader(fragShader);

        Appearance app = new Appearance();
        app.setShader(shader);
        app.setTextureUnits(textures, 1);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);

        ElectroAnimation anim = new ElectroAnimation(geom);
        sceneManager.setApplicationObserver(anim);

        scene_root.addChild(shape);

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
     * @param file THe name of the file to load
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

    public static void main(String[] args)
    {
        ElectroEffectDemo demo = new ElectroEffectDemo();
        demo.setVisible(true);
    }
}
