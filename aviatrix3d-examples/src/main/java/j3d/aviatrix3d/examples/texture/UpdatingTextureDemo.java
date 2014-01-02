package j3d.aviatrix3d.examples.texture;

// Standard imports

import java.awt.*;
import java.awt.event.*;

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

/**
 * Example application that demonstrates updating textures on the fly with
 * different subsection updates.
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public class UpdatingTextureDemo extends Frame
        implements WindowListener
{
    private static final int XSIZE = 64;
    private static final int YSIZE = 64;

    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    public UpdatingTextureDemo()
    {
        super("Aviatrix Demo Showing Dynamic Textures");

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
        byte[] tex_buffer = new byte[XSIZE * YSIZE * 3];
        int pos = 0;

        for (int y = 0; y < YSIZE; y++)
        {
            for (int x = 0; x < XSIZE; x++)
            {
                tex_buffer[pos++] = (byte) 0x0;
                tex_buffer[pos++] = (byte) 0xFF;
                tex_buffer[pos++] = (byte) 0x0;
            }
        }

        ByteTextureComponent2D img_comp =
                new ByteTextureComponent2D(TextureComponent.FORMAT_RGB,
                                           XSIZE,
                                           YSIZE,
                                           tex_buffer);


        Texture texture = new Texture2D(Texture.FORMAT_RGB, img_comp);

        // View group
        Viewpoint vp = new Viewpoint();

        Vector3d trans = new Vector3d();
        trans.set(0, 0, 0.5);

        Matrix4d mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);

        // Flat panel that has the viewable object as the demo
        float[] coord = {
                0, 0, -0.5f, 0.25f, 0, -0.5f, 0, 0.25f, -0.5f,
                0.25f, 0, -0.5f, 0.25f, 0.25f, -0.5f, 0, 0.25f, -0.5f
        };

        float[] normal = {
                0, 0, 1, 0, 0, 1, 0, 0, 1,
                0, 0, 1, 0, 0, 1, 0, 0, 1
        };
        float[][] tex_coord = {{0, 0, 1, 0, 0, 1, 1, 0, 1, 1, 0, 1,}};
        int[] tex_type = {VertexGeometry.TEXTURE_COORDINATE_2};

        TriangleArray geom = new TriangleArray();
        geom.setVertices(TriangleArray.COORDINATE_3, coord, 6);
        geom.setTextureCoordinates(tex_type, tex_coord, 1);

        Material material = new Material();
        material.setDiffuseColor(new float[]{0, 0, 1});
        material.setEmissiveColor(new float[]{0, 0, 1});
        material.setSpecularColor(new float[]{1, 1, 1});

        TextureUnit[] tu = new TextureUnit[1];
        tu[0] = new TextureUnit();
        tu[0].setTexture(texture);

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

        TextureUpdater anim = new TextureUpdater(img_comp);
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

    public static void main(String[] args)
    {
        UpdatingTextureDemo demo = new UpdatingTextureDemo();
        demo.setVisible(true);
    }
}
