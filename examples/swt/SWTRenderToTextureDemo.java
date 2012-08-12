
// External imports
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.output.graphics.SimpleSWTSurface;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.DefaultGraphicsPipeline;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;
import org.j3d.aviatrix3d.pipeline.graphics.NullCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.NullSortStage;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsSortStage;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;

/**
 * Example application that demonstrates Render to texture capabilities on
 * SWT surfaces.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class SWTRenderToTextureDemo
{
    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    public SWTRenderToTextureDemo(Shell parent)
    {
        setupAviatrix(parent);
        setupSceneGraph();

        sceneManager.setEnabled(true);
    }

    /**
     * Setup the avaiatrix pipeline here
     *
     * @param parent The parent widget of the display
     */
    private void setupAviatrix(Shell parent)
    {
        // Assemble a simple single-threaded pipeline.
        GLCapabilities caps = new GLCapabilities();
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);

        GraphicsCullStage culler = new NullCullStage(2);
        culler.setOffscreenCheckEnabled(true);

        GraphicsSortStage sorter = new NullSortStage();
        surface = new SimpleSWTSurface(parent, SWT.NONE, caps);
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
    }

    /**
     * Setup the basic scene which consists of a quad and a viewpoint
     */
    private void setupSceneGraph()
    {
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
//        app.setMaterial(material);

        // The texture requires its own set of capabilities.
        GLCapabilities caps = new GLCapabilities();
        caps.setDoubleBuffered(false);
        caps.setPbufferRenderToTexture(true);

        OffscreenTexture2D texture = new OffscreenTexture2D(caps, 128, 128);
        setupTextureSceneGraph(texture);

        TextureUnit[] tu = new TextureUnit[1];
        tu[0] = new TextureUnit();
        tu[0].setTexture(texture);

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
    }

    /**
     * Convenience method to set up the details of the texture.
     */
    private void setupTextureSceneGraph(OffscreenTexture2D texture)
    {
        texture.setClearColor(0, 1, 0, 1);

        Group scene_root = new Group();

        TransformGroup grp = new TransformGroup();

        Vector3f trans = new Vector3f(0, 0, 1);

        Matrix4f mat = new Matrix4f();
        mat.setIdentity();
        mat.setScale(10);
        mat.setTranslation(trans);

        // Flat panel that has the viewable object as the demo
        float[] coord = { 0, 0, -1,     0.5f, 0, -1,     0, 0.5f, -1,
                          0.5f, 0, -1, 0.5f, 0.5f, -1, 0, 0.5f, -1 };

        float[] color = { 0, 0, 1, 0, 1, 0, 1, 0, 0,
                          0, 1, 1, 0, 1, 1, 1, 0, 1 };

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
        geom.setColors(false, color);

        Material material = new Material();
        material.setDiffuseColor(new float[] { 0, 0, 1 });
        material.setEmissiveColor(new float[] { 0, 0, 1 });
        material.setSpecularColor(new float[] { 1, 1, 1 });

        Appearance app = new Appearance();
//        app.setMaterial(material);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);
        shape.setAppearance(app);

//        grp.setTransform(mat);
        grp.addChild(shape);

        // Give the texture it's own separate viewpoint.
        Viewpoint vp = new Viewpoint();

        trans.set(0, 0.5f, -0.5f);

        mat = new Matrix4f();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup vp_grp = new TransformGroup();
        vp_grp.setTransform(mat);
        vp_grp.addChild(vp);

        scene_root.addChild(grp);
        scene_root.addChild(vp_grp);

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

        texture.setLayers(layers, 1);
        texture.setRepaintRequired(true);
    }

    /**
     * Cause the rendering cycle to exit now.
     */
    public void shutdown()
    {
        sceneManager.shutdown();
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    public static void main(String[] args)
    {
        Shell shell = new Shell();
        shell.setLayout(new FillLayout());
        SWTRenderToTextureDemo demo = new SWTRenderToTextureDemo(shell);

        shell.setSize (640, 480);
        shell.setText("Aviatrix RTT + SWT Demo");
        shell.open();

        Display display = shell.getDisplay();

        while(!shell.isDisposed())
        {
            if(!display.readAndDispatch())
                display.sleep();
        }

        demo.shutdown();

        display.dispose();
    }
}
