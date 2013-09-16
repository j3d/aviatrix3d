
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
import org.j3d.aviatrix3d.pipeline.graphics.NullSortStage;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsSortStage;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;

import org.j3d.geom.GeometryData;
import org.j3d.geom.BoxGenerator;
import org.j3d.util.MatrixUtils;

/**
 * Example application that demonstrates how to put together a single-threaded
 * rendering system.
 *
 * @author Justin Couch
 * @version $Revision: 1.11 $
 */
public class TextureTransformDemo extends Frame
    implements WindowListener
{
    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    public TextureTransformDemo()
    {
        super("Aviatrix 2D Texture Demo");

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
        // Load the texture image
        ImageTextureComponent2D img_comp = null;
        int img_width = 0;
        int img_height = 0;

        try
        {
            File f = new File("textures/transform_test.gif");
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
                    break;

                case BufferedImage.TYPE_4BYTE_ABGR:
                case BufferedImage.TYPE_INT_ARGB:
                    format = TextureComponent.FORMAT_RGBA;
                    break;

                default:
                    System.out.println("unknown type");
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

        // Sphere to render the shader onto
        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA|
                                  GeometryData.TEXTURE_2D_DATA;

        BoxGenerator generator = new BoxGenerator(0.2f, 0.2f, 0.2f);
        generator.generate(data);

        int[] tex_type = { VertexGeometry.TEXTURE_COORDINATE_2 };
        float[][] tex_coord = new float[1][data.vertexCount * 2];
        int[] tex_sets = { 0, 0 };

        System.arraycopy(data.textureCoordinates, 0, tex_coord[0], 0,
                         data.vertexCount * 2);

        TriangleArray geom = new TriangleArray();
        geom.setVertices(TriangleArray.COORDINATE_3,
                         data.coordinates,
                         data.vertexCount);
        geom.setNormals(data.normals);
        geom.setTextureCoordinates(tex_type, tex_coord, 1);
        geom.setTextureSetMap(tex_sets);

        Material material = new Material();
        material.setDiffuseColor(new float[] { 0, 0, 1 });
        material.setEmissiveColor(new float[] { 0, 0, 1 });
        material.setSpecularColor(new float[] { 1, 1, 1 });

        if(img_comp != null)
        {
            Texture2D texture = new Texture2D();
            texture.setSources(Texture.MODE_BASE_LEVEL,
                              Texture.FORMAT_RGB,
                              new TextureComponent[] { img_comp },
                              1);


            // place 4 shapes into the scene with different transformations
            // applied.
            // None    shift
            // rotate  scale
            TextureUnit[] tu = new TextureUnit[1];
            Matrix4f tex_transform = new Matrix4f();
            Matrix4f obj_transform = new Matrix4f();
            Vector3f translation = new Vector3f();

            tex_transform.setIdentity();
            obj_transform.setIdentity();

            tu[0] = new TextureUnit();
            tu[0].setTexture(texture);

            Appearance app = new Appearance();
            app.setMaterial(material);
            app.setTextureUnits(tu, 1);

            Shape3D shape = new Shape3D();
            shape.setGeometry(geom);
            shape.setAppearance(app);

            translation.x = -0.15f;
            translation.y =  0.15f;
            obj_transform.setTranslation(translation);

            TransformGroup tg = new TransformGroup();
            tg.setTransform(obj_transform);
            tg.addChild(shape);
            scene_root.addChild(tg);

            // Translated texture
            translation.x = 0.25f;
            tex_transform.setTranslation(translation);

            tu[0] = new TextureUnit();
            tu[0].setTexture(texture);
            tu[0].setTextureTransform(tex_transform);

            app = new Appearance();
            app.setMaterial(material);
            app.setTextureUnits(tu, 1);

            shape = new Shape3D();
            shape.setGeometry(geom);
            shape.setAppearance(app);

            translation.x =  0.15f;
            translation.y =  0.15f;
            obj_transform.setTranslation(translation);

            tg = new TransformGroup();
            tg.setTransform(obj_transform);
            tg.addChild(shape);
            scene_root.addChild(tg);

            // Rotated texture
            MatrixUtils mu = new MatrixUtils();
            mu.rotateY((float)(Math.PI / 2), tex_transform);

            tu[0] = new TextureUnit();
            tu[0].setTexture(texture);
            tu[0].setTextureTransform(tex_transform);

            app = new Appearance();
            app.setMaterial(material);
            app.setTextureUnits(tu, 1);

            shape = new Shape3D();
            shape.setGeometry(geom);
            shape.setAppearance(app);

            translation.x =  -0.15f;
            translation.y =  -0.15f;
            obj_transform.setTranslation(translation);

            tg = new TransformGroup();
            tg.setTransform(obj_transform);
            tg.addChild(shape);
            scene_root.addChild(tg);

            // Scaled texture
            tex_transform.setIdentity();
            tex_transform.setScale(2);

            tu[0] = new TextureUnit();
            tu[0].setTexture(texture);
            tu[0].setTextureTransform(tex_transform);

            app = new Appearance();
            app.setMaterial(material);
            app.setTextureUnits(tu, 1);

            shape = new Shape3D();
            shape.setGeometry(geom);
            shape.setAppearance(app);

            translation.x =   0.15f;
            translation.y =  -0.15f;
            obj_transform.setTranslation(translation);

            tg = new TransformGroup();
            tg.setTransform(obj_transform);
            tg.addChild(shape);
            scene_root.addChild(tg);
        }

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

    public static void main(String[] args)
    {
        TextureTransformDemo demo = new TextureTransformDemo();
        demo.setVisible(true);
    }
}
