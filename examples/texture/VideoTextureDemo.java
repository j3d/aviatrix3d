
// Standard imports
import java.awt.*;
import java.awt.event.*;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;

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
import org.j3d.aviatrix3d.pipeline.graphics.NullSortStage;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsSortStage;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;

import org.j3d.geom.GeometryData;
import org.j3d.geom.BoxGenerator;

/**
 * Example application that demonstrates integration of JMF for video rendering
 * as a texture.
 *
 * @author Justin Couch
 * @version $Revision: 1.9 $
 */
public class VideoTextureDemo extends Frame
    implements WindowListener
{
    /** Location of the video file to play */
    private static final String MPEG_SOURCE = "textures/MovieTextureTest.mpg";

    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    public VideoTextureDemo()
    {
        super("Aviatrix Demo Showing JMF Video Textures");

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
    private void setupSceneGraph()
    {
        // Create the raw texture and initialise it to grey
        int tex_size = VideoTextureRenderer.TEXTURE_FRAME_SIZE *
                       VideoTextureRenderer.TEXTURE_FRAME_SIZE * 3;

        byte[] tex_buffer = new byte[tex_size];
        int pos = 0;

        for(int i = 0; i < tex_size; i++)
            tex_buffer[pos++] = (byte)0x00;

        ByteTextureComponent2D img_comp =
            new ByteTextureComponent2D(TextureComponent.FORMAT_RGB,
                                       VideoTextureRenderer.TEXTURE_FRAME_SIZE,
                                       VideoTextureRenderer.TEXTURE_FRAME_SIZE,
                                       tex_buffer);


        Texture texture = new Texture2D(Texture.FORMAT_RGB, img_comp);

        // Create the URL pointing to the movie
        URL movie_url = null;
        try
        {
            File f = new File(MPEG_SOURCE);
            movie_url = f.toURL();
        }
        catch(MalformedURLException mue)
        {
            System.out.println("Unable to locate MPEG file");
        }

        // View group
        Viewpoint vp = new Viewpoint();

        Vector3f trans = new Vector3f(0.1f, 0.1f, -0.15f);

        Matrix4f mat = new Matrix4f();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);

        // Flat panel that has the viewable object as the demo
        float[] coord = { 0, 0, -0.5f,     0.25f, 0, -0.5f,     0, 0.25f, -0.5f,
                          0.25f, 0, -0.5f, 0.25f, 0.25f, -0.5f, 0, 0.25f, -0.5f };

        float[] normal = { 0, 0, 1, 0, 0, 1, 0, 0, 1,
                           0, 0, 1, 0, 0, 1, 0, 0, 1};
        float[][] tex_coord = { { 0, 0,  1, 0,  0, 1,   1, 0,  1, 1, 0, 1, } };
        int[] tex_type = { VertexGeometry.TEXTURE_COORDINATE_2 };

        TriangleArray geom = new TriangleArray();
        geom.setVertices(TriangleArray.COORDINATE_3, coord, 6);
        geom.setTextureCoordinates(tex_type, tex_coord, 1);

        Material material = new Material();
        material.setDiffuseColor(new float[] { 0, 0, 1 });
        material.setEmissiveColor(new float[] { 0, 0, 1 });
        material.setSpecularColor(new float[] { 1, 1, 1 });

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

        Layer[] layers = { layer };
        displayManager.setLayers(layers, 1);

        VideoTextureRenderer vtr = new VideoTextureRenderer(img_comp);
        VideoTextureUpdater vtu = new VideoTextureUpdater(movie_url, vtr);
        sceneManager.setApplicationObserver(vtu);
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
        VideoTextureDemo demo = new VideoTextureDemo();
        demo.setVisible(true);
    }
}
