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

import org.j3d.aviatrix3d.output.graphics.SimpleAWTSurface;
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

/**
 * Example application that demonstrates how to put together a single-threaded
 * rendering system.
 *
 * @author Justin Couch
 * @version $Revision: 1.14 $
 */
public class Texture3DDemo extends Frame
        implements WindowListener
{
    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    public Texture3DDemo()
    {
        super("Aviatrix 3D Texture Demo");

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
        // Load the texture image
        BufferedImage[] src_img = new BufferedImage[2];

        src_img[0] = loadImage("textures/3d_texture_1.gif");
        src_img[1] = loadImage("textures/3d_texture_2.gif");

        ImageTextureComponent3D[] img_comp = {
                new ImageTextureComponent3D(TextureComponent.FORMAT_RGB, src_img)
        };

        // View group
        Viewpoint vp = new Viewpoint();

        Vector3d trans = new Vector3d();
        trans.set(0, 0.3, 1);

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

        Texture3D texture = new Texture3D();
        texture.setSources(Texture.MODE_BASE_LEVEL,
                           Texture.FORMAT_RGB,
                           img_comp,
                           1);

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
//        app.setMaterial(material);
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
        Texture3DDemo demo = new Texture3DDemo();
        demo.setVisible(true);
    }
}
