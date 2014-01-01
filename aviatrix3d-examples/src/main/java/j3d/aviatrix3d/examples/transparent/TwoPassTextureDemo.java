package j3d.aviatrix3d.examples.transparent;

// Standard imports
import java.awt.*;
import java.awt.event.*;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
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
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.DefaultGraphicsPipeline;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;
import org.j3d.aviatrix3d.pipeline.graphics.NullCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.TransparencyDepthSortStage;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsSortStage;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;
import org.j3d.util.MatrixUtils;

/**
 * Example application that demonstrates the use of two pass rendering for
 * transparent objects for dealing with oddball texture types.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class TwoPassTextureDemo extends Frame
    implements WindowListener
{
    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the two pass layers etc */
    private SingleDisplayCollection tpDisplayManager;

    /** Manager for the single pass layers etc */
    private SingleDisplayCollection spDisplayManager;


    public TwoPassTextureDemo()
    {
        super("Transparent Texture Demo");

        setLayout(new GridLayout(1, 2));
        addWindowListener(this);

        setupAviatrix();
        setupSceneGraph();

        setSize(800, 400);
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
        // Assemble two single-threaded pipelines - one surface with two pass
        // rendering enabled, another without it, to demonstrate the
        // differences
        GraphicsRenderingCapabilities caps = new GraphicsRenderingCapabilities();

        // Single pass version
        GraphicsCullStage sp_culler = new NullCullStage();
        sp_culler.setOffscreenCheckEnabled(false);

        GraphicsSortStage sp_sorter = new TransparencyDepthSortStage();
        GraphicsOutputDevice sp_surface = new SimpleAWTSurface(caps);

        // Defaults to false, but set it to be explicit anyway,
        // for demo purposes
        sp_surface.enableTwoPassTransparentRendering(false);

        DefaultGraphicsPipeline sp_pipeline = new DefaultGraphicsPipeline();

        sp_pipeline.setCuller(sp_culler);
        sp_pipeline.setSorter(sp_sorter);
        sp_pipeline.setGraphicsOutputDevice(sp_surface);

        spDisplayManager = new SingleDisplayCollection();
        spDisplayManager.addPipeline(sp_pipeline);



        // Two pass version
        GraphicsCullStage tp_culler = new NullCullStage();
        tp_culler.setOffscreenCheckEnabled(false);

        GraphicsSortStage tp_sorter = new TransparencyDepthSortStage();
        GraphicsOutputDevice tp_surface = new SimpleAWTSurface(caps);
        tp_surface.enableTwoPassTransparentRendering(true);
        tp_surface.setAlphaTestCutoff(0.5f);

        DefaultGraphicsPipeline tp_pipeline = new DefaultGraphicsPipeline();

        tp_pipeline.setCuller(tp_culler);
        tp_pipeline.setSorter(tp_sorter);
        tp_pipeline.setGraphicsOutputDevice(tp_surface);

        tpDisplayManager = new SingleDisplayCollection();
        tpDisplayManager.addPipeline(tp_pipeline);

        // Render manager
        sceneManager = new SingleThreadRenderManager();
        sceneManager.addDisplay(spDisplayManager);
        sceneManager.addDisplay(tpDisplayManager);
        sceneManager.setMinimumFrameInterval(100);

        // Before putting the pipeline into run mode, put the canvas on
        // screen first.

        // Single pass on left
        Panel p1 = new Panel(new BorderLayout());
        p1.add(new Label("Single Pass", Label.CENTER), BorderLayout.NORTH);

        Component sp_comp = (Component)sp_surface.getSurfaceObject();
        p1.add(sp_comp, BorderLayout.CENTER);

        Panel p2 = new Panel(new BorderLayout());
        p2.add(new Label("Two Pass", Label.CENTER), BorderLayout.NORTH);

        Component tp_comp = (Component)tp_surface.getSurfaceObject();
        p2.add(tp_comp, BorderLayout.CENTER);

        add(p1);
        add(p2);
    }

    /**
     * Setup the basic scene which consists of a quad and a viewpoint
     */
    private void setupSceneGraph()
    {

        TextureComponent2D argbImg = loadImage("textures/mytree.png");

        // View group
        Viewpoint vp = new Viewpoint();

        Vector3d trans = new Vector3d();
        trans.set(0, 0.5f, 0.5f);

        MatrixUtils mu = new MatrixUtils();

        Matrix4d mat = new Matrix4d();
        mu.rotateX(Math.PI * -0.25f, mat);
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);

        // Flat panel that has the viewable object as the demo
        float[] coord =
        {
            -0.25f, -0.25f, 0,
             0.25f, -0.25f, 0,
             0.25f,  0.25f, 0,
            -0.25f,  0.25f, 0
        };

        float[] normal = { 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1 };

        float[][] tex_coord = { { 0, 0,  1, 0,  1, 1,   0, 1 } };

        int[] tex_type = { VertexGeometry.TEXTURE_COORDINATE_2 };

        QuadArray geom = new QuadArray();
        geom.setVertices(QuadArray.COORDINATE_3, coord, 4);
        geom.setTextureCoordinates(tex_type, tex_coord, 1);

        Material material = new Material();
        material.setDiffuseColor(new float[] { 0, 0, 1 });
        material.setEmissiveColor(new float[] { 0, 0, 1 });
        material.setSpecularColor(new float[] { 1, 1, 1 });

        Appearance app = new Appearance();
        app.setMaterial(material);

        if(argbImg != null)
        {
            Texture2D texture = new Texture2D();
            texture.setSources(Texture.MODE_BASE_LEVEL,
                              Texture.FORMAT_RGBA,
                              new TextureComponent[] { argbImg },
                              1);

            TextureUnit[] tu = new TextureUnit[1];
            tu[0] = new TextureUnit();
            tu[0].setTexture(texture);

            app.setTextureUnits(tu, 1);
        }

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);

        SharedNode transparent_shape = new SharedNode();
        transparent_shape.setChild(shape);

        Matrix4d transform = new Matrix4d();
        mu.rotateY(Math.PI * -0.25f, transform);

        TransformGroup tg = new TransformGroup();
        tg.setTransform(transform);
        tg.addChild(transparent_shape);


        Matrix4d transform2 = new Matrix4d();
        mu.rotateY(Math.PI * 0.25f, transform2);

        TransformGroup tg2 = new TransformGroup();
        tg2.setTransform(transform2);
        tg2.addChild(transparent_shape);


        ColorBackground cbg = new ColorBackground(new float[] {0,0,1,1});

        scene_root.addChild(cbg);
        scene_root.addChild(tg);
        scene_root.addChild(tg2);

        SimpleScene scene = new SimpleScene();
        scene.setRenderedGeometry(scene_root);
        scene.setActiveBackground(cbg);
        scene.setActiveView(vp);

        // Then the basic layer and viewport at the top:
        SimpleViewport view = new SimpleViewport();
        view.setDimensions(0, 0, 400, 400);
        view.setScene(scene);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(view);

        Layer[] layers = { layer };

        tpDisplayManager.setLayers(layers, 1);
        spDisplayManager.setLayers(layers, 1);
    }

    private TextureComponent2D loadImage(String name)
    {
        // Load the texture image
        TextureComponent2D img_comp = null;
        int img_width = 0;
        int img_height = 0;

        try
        {
            File f = new File(name);
            if(!f.exists())
                System.out.println("Can't find texture source file");

            FileInputStream is = new FileInputStream(f);

            BufferedInputStream stream = new BufferedInputStream(is);
            BufferedImage img = ImageIO.read(stream);

            img_width = img.getWidth(null);
            img_height = img.getHeight(null);
            int format = TextureComponent.FORMAT_RGB;

            switch(img.getType())
            {
                case BufferedImage.TYPE_3BYTE_BGR:
                case BufferedImage.TYPE_CUSTOM:
                case BufferedImage.TYPE_INT_RGB:
                    System.out.println("Texture uses RGB");
                    break;

                case BufferedImage.TYPE_4BYTE_ABGR:
                case BufferedImage.TYPE_INT_ARGB:
                    System.out.println("Texture uses RGBA");
                    format = TextureComponent.FORMAT_RGBA;
                    break;

                case BufferedImage.TYPE_BYTE_INDEXED:
                    ColorModel cm = img.getColorModel();
                    if(cm.hasAlpha())
                    {
                        System.out.println("Texture is Indexed color with alpha");
                        format = TextureComponent.FORMAT_RGBA;
                    }
                    else
                    {
                        System.out.println("Texture is Indexed color with alpha");
                        format = TextureComponent.FORMAT_RGB;
                    }
                    break;

                default:
                   System.out.println("Texture Defaults to RGB: " + img);
            }

            img_comp = new ImageTextureComponent2D(format,
                                                   img_width,
                                                   img_height,
                                                   img);

            return img_comp;
        }
        catch(IOException ioe)
        {
            System.out.println("Error reading image: " + ioe);
            return null;
        }
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

    public static void main(String[] args)
    {
        TwoPassTextureDemo demo = new TwoPassTextureDemo();
        demo.setVisible(true);
    }
}
