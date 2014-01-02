package j3d.aviatrix3d.examples.texture;

// Standard imports

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

// Application Specific imports
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
import org.j3d.geom.BoxGenerator;
import org.j3d.texture.procedural.PerlinNoiseGenerator;

/**
 * Example application that demonstrates how to put together a single-threaded
 * rendering system.
 *
 * @author Justin Couch
 * @version $Revision: 1.12 $
 */
public class ByteTexture3DDemo extends Frame
        implements WindowListener
{
    private static final int XSIZE = 64;
    private static final int YSIZE = 64;
    private static final int ZSIZE = 64;
    private static final float XSCALE = 0.16f;
    private static final float YSCALE = 0.16f;
    private static final float ZSCALE = 0.16f;

    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    public ByteTexture3DDemo()
    {
        super("Aviatrix 3D Texture Demo using dynmic textures");

        setLayout(new BorderLayout());
        addWindowListener(this);

        setupAviatrix();
        setupSceneGraph();

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
        Component comp = (Component) surface.getSurfaceObject();
        add(comp, BorderLayout.CENTER);
    }

    /**
     * Setup the basic scene which consists of a quad and a viewpoint
     */
    private void setupSceneGraph()
    {
        PerlinNoiseGenerator noise_gen = new PerlinNoiseGenerator();

        byte[] tex_buffer = new byte[XSIZE * YSIZE * ZSIZE];
        int min = 255;
        int max = 0;
        int pos = 0;

        for (int z = 0; z < ZSIZE; z++)
        {
            for (int y = 0; y < YSIZE; y++)
            {
                for (int x = 0; x < XSIZE; x++)
                {
                    float noise = noise_gen.tileableTurbulence3(XSCALE * x,
                                                                YSCALE * y,
                                                                ZSCALE * z,
                                                                XSIZE * XSCALE,
                                                                YSIZE * YSCALE,
                                                                ZSIZE * ZSCALE,
                                                                16);
                    int t = (int) (127.5f * (1 + noise));
                    if (t > max)
                        max = t;
                    if (t < min)
                        min = t;
                    tex_buffer[pos++] = (byte) t;
                }
            }
        }

        float min_max = 1.0f / (max - min);
        for (int i = 0; i < XSIZE * YSIZE * ZSIZE; i++)
            tex_buffer[i] = (byte) ((255 * (tex_buffer[i] - min)) * min_max);

        ByteTextureComponent3D img_comp =
                new ByteTextureComponent3D(TextureComponent.FORMAT_SINGLE_COMPONENT,
                                           XSIZE,
                                           YSIZE,
                                           ZSIZE,
                                           tex_buffer);

/*
        // Simpler versions to see the output
        byte[] tex_buffer =
        {
              (byte)0xFF, (byte)0,
              (byte)0,    (byte)0xFF,
              (byte)0,    (byte)0xFF,
              (byte)0xFF, (byte)0

// 3 Color texture
//            (byte)0xFF, (byte)0, (byte)0,       (byte)0, (byte)0xFF, (byte)0,
//            (byte)0,    (byte)0, (byte)0xFF,    (byte)0, (byte)0xFF, (byte)0xFF,

//            (byte)0xFF, (byte)0xFF, (byte)0,    (byte)0xFF, (byte)0, (byte)0xFF,
//            (byte)0,    (byte)0xFF, (byte)0xFF, (byte)0,    (byte)0, (byte)0
        };

        ByteTextureComponent3D img_comp =
            new ByteTextureComponent3D(TextureComponent.FORMAT_SINGLE_COMPONENT,
                                       2,
                                       2,
                                       2,
                                       tex_buffer);

//        Texture texture = new Texture3D(Texture.FORMAT_RGB, img_comp);
*/

        Texture texture = new Texture3D(Texture.FORMAT_INTENSITY, img_comp);

        // View group
        Viewpoint vp = new Viewpoint();

        Vector3d trans = new Vector3d();
        trans.set(0, 0.3d, 1);

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
        data.geometryComponents = GeometryData.NORMAL_DATA |
                GeometryData.TEXTURE_3D_DATA;

        BoxGenerator generator = new BoxGenerator(0.2f, 0.2f, 0.2f);
        generator.generate(data);

        int[] tex_type = {VertexGeometry.TEXTURE_COORDINATE_3};
        float[][] tex_coord = new float[1][data.vertexCount * 3];

        System.arraycopy(data.textureCoordinates, 0, tex_coord[0], 0,
                         data.vertexCount * 3);

        TriangleArray geom = new TriangleArray();
        geom.setVertices(TriangleArray.COORDINATE_3,
                         data.coordinates,
                         data.vertexCount);
        geom.setNormals(data.normals);
        geom.setTextureCoordinates(tex_type, tex_coord, 1);

        Material material = new Material();
        material.setDiffuseColor(new float[]{0, 0, 1});
        material.setEmissiveColor(new float[]{0, 0, 1});
        material.setSpecularColor(new float[]{1, 1, 1});

/*
        Texture3D texture = new Texture3D();
        texture.setSources(Texture.MODE_BASE_LEVEL,
                          Texture.FORMAT_RGB,
                          img_comp,
                          1);
*/
        TexCoordGeneration coord_gen = new TexCoordGeneration();
        coord_gen.setParameter(TexCoordGeneration.TEXTURE_S,
                               TexCoordGeneration.MODE_GENERIC,
                               TexCoordGeneration.MAP_OBJECT_LINEAR,
                               null);

        coord_gen.setParameter(TexCoordGeneration.TEXTURE_T,
                               TexCoordGeneration.MODE_GENERIC,
                               TexCoordGeneration.MAP_OBJECT_LINEAR,
                               null);

        coord_gen.setParameter(TexCoordGeneration.TEXTURE_R,
                               TexCoordGeneration.MODE_GENERIC,
                               TexCoordGeneration.MAP_OBJECT_LINEAR,
                               null);

        TextureUnit[] tu = new TextureUnit[1];
        tu[0] = new TextureUnit();
        tu[0].setTexture(texture);
//        tu[0].setTexCoordGeneration(coord_gen);

        Appearance app = new Appearance();
        app.setMaterial(material);
        app.setTextureUnits(tu, 1);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);

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

        Layer[] layers = {layer};
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
     * Load a single image
     */
    private BufferedImage loadImage(String name)
    {
        BufferedImage img_comp = null;

        try
        {
            File f = new File(name);
            if (!f.exists())
                System.out.println("Can't find texture source file");

            FileInputStream is = new FileInputStream(f);

            BufferedInputStream stream = new BufferedInputStream(is);
            img_comp = ImageIO.read(stream);
        }
        catch (IOException ioe)
        {
            System.out.println("Error reading image: " + ioe);
        }

        return img_comp;
    }

    public static void main(String[] args)
    {
        ByteTexture3DDemo demo = new ByteTexture3DDemo();
        demo.setVisible(true);
    }
}
