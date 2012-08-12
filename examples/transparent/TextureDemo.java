
// Standard imports
import java.awt.*;
import java.awt.event.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;

import java.net.URL;

import javax.imageio.ImageIO;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import javax.media.opengl.GLCapabilities;

// Application Specific imports
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.output.graphics.SimpleAWTSurface;
import org.j3d.aviatrix3d.output.graphics.DebugAWTSurface;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.DefaultGraphicsPipeline;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;
import org.j3d.aviatrix3d.pipeline.graphics.NullCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.TransparencyDepthSortStage;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsSortStage;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;

/**
 * Example application that demonstrates how to put together a single-threaded
 * rendering system.
 *
 * @author Justin Couch
 * @version $Revision: 1.11 $
 */
public class TextureDemo extends Frame
    implements WindowListener
{
    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    public TextureDemo()
    {
        super("Transparent Texture Demo");

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
        GLCapabilities caps = new GLCapabilities();
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);

        GraphicsCullStage culler = new NullCullStage();
        culler.setOffscreenCheckEnabled(false);

        GraphicsSortStage sorter = new TransparencyDepthSortStage();
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
    private void setupSceneGraph()
    {

        TextureComponent2D custImg = loadImage("textures/ButtonForward.png");
        TextureComponent2D argbImg = loadImage("textures/mytree.png");

        // View group
        Viewpoint vp = new Viewpoint();

        Vector3f trans = new Vector3f(0, 0, 1);

        Matrix4f mat = new Matrix4f();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);

        // Flat panel that has the viewable object as the demo
        float[] coord = { 0, 0, -1,     0.25f, 0, -1,     0, 0.25f, -1,
                          0.25f, 0, -1, 0.25f, 0.25f, -1, 0, 0.25f, -1,
                          0.25f, 0, -1, 0.5f, 0, -1,      0.25f, 0.25f, -1,
                          0.5f, 0, -1,  0.5f, 0.25f, -1,  0.25f, 0.25f, -1 };

        float[] normal = { 0, 0, 1, 0, 0, 1, 0, 0, 1,
                           0, 0, 1, 0, 0, 1, 0, 0, 1,
                           0, 0, 1, 0, 0, 1, 0, 0, 1,
                           0, 0, 1, 0, 0, 1, 0, 0, 1};
        float[][] tex_coord = { { 0, 0,  1, 0,  0, 1,   1, 0,  1, 1, 0, 1,
                                  0, 0,  1, 0,  0, 1,   1, 0,  1, 1, 0, 1 } };
        int[] tex_type = { VertexGeometry.TEXTURE_COORDINATE_2 };

        TriangleArray geom = new TriangleArray();
        geom.setVertices(TriangleArray.COORDINATE_3, coord, 6);
        geom.setTextureCoordinates(tex_type, tex_coord, 1);

        Material material = new Material();
        material.setDiffuseColor(new float[] { 0, 0, 1 });
        material.setEmissiveColor(new float[] { 0, 0, 1 });
        material.setSpecularColor(new float[] { 1, 1, 1 });

        Appearance app = new Appearance();
        app.setMaterial(material);

        if(custImg != null)
        {
//            Texture2D texture = new Texture2D(Texture.FORMAT_RGB,
//                                              custImg);

            Texture2D texture = new Texture2D();
            texture.setSources(Texture.MODE_BASE_LEVEL,
                              Texture.FORMAT_RGB,
                              new TextureComponent[] { custImg },
                              1);

            TextureUnit[] tu = new TextureUnit[1];
            tu[0] = new TextureUnit();
            tu[0].setTexture(texture);

            app.setTextureUnits(tu, 1);
        }

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);


        Appearance app2 = new Appearance();
        app2.setMaterial(material);

        if(argbImg != null)
        {
//            Texture2D texture = new Texture2D(Texture.FORMAT_RGB,
//                                              argbImg);

            Texture2D texture2 = new Texture2D();
            texture2.setSources(Texture.MODE_BASE_LEVEL,
                              Texture.FORMAT_RGB,
                              new TextureComponent[] { argbImg },
                              1);

            TextureUnit[] tu2 = new TextureUnit[1];
            tu2[0] = new TextureUnit();
            tu2[0].setTexture(texture2);

            app2.setTextureUnits(tu2, 1);
        }

        TransformGroup tg2 = new TransformGroup();
        Matrix4f transform2 = new Matrix4f();
        transform2.setIdentity();
        transform2.setTranslation(new Vector3f(-0.5f, 0, 0));
        tg2.setTransform(transform2);

        Shape3D shape2 = new Shape3D();
        shape2.setGeometry(geom);
        shape2.setAppearance(app2);
        tg2.addChild(shape2);


        TransformGroup tg = new TransformGroup();
        Matrix4f transform = new Matrix4f();
        transform.setIdentity();
        transform.setTranslation(new Vector3f(0, 0, -1));
        tg.setTransform(transform);

        Shape3D backShape = new Shape3D();
        Material material2 = new Material();
        material2.setDiffuseColor(new float[] { 1, 0, 0 });
        material2.setEmissiveColor(new float[] { 1, 0, 0 });
        material2.setSpecularColor(new float[] { 1, 1, 1 });

        Appearance backApp = new Appearance();
        backApp.setMaterial(material2);
        backShape.setGeometry(geom);
        backShape.setAppearance(backApp);
        tg.addChild(backShape);


        ColorBackground cbg = new ColorBackground(new float[] {0,0,1,1});

        scene_root.addChild(cbg);
        scene_root.addChild(tg);
        scene_root.addChild(tg2);
        scene_root.addChild(shape);

        SimpleScene scene = new SimpleScene();
        scene.setRenderedGeometry(scene_root);
        scene.setActiveBackground(cbg);
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
                    System.out.println("TD RGB");
                    break;

                case BufferedImage.TYPE_4BYTE_ABGR:
                case BufferedImage.TYPE_INT_ARGB:
                    System.out.println("TD RGBA");
                    format = TextureComponent.FORMAT_RGBA;
                    break;
                default:
                   System.out.println("Default to RGB");
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
        TextureDemo demo = new TextureDemo();
        demo.setVisible(true);
    }
}
