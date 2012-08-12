
// Standard imports
import java.awt.*;
import java.awt.event.*;

import java.io.File;
import java.net.MalformedURLException;

import java.net.URL;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import javax.media.opengl.GLCapabilities;

// Application Specific imports
import org.j3d.aviatrix3d.*;
import org.j3d.renderer.aviatrix3d.geom.*;

import org.j3d.aviatrix3d.output.graphics.SimpleAWTSurface;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.DefaultGraphicsPipeline;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;
import org.j3d.aviatrix3d.pipeline.graphics.NullCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.NullSortStage;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsSortStage;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;

import org.j3d.renderer.aviatrix3d.util.SceneGraphTraverser;
import org.j3d.renderer.aviatrix3d.util.SceneGraphTraversalObserver;

/**
 * Example application that demonstrates using the scene graph walker class
 * org.j3d.renderer.aviatrix3d.uitl.SceneGraphTraverser, based on the combined
 * fog demo.
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
public class SceneWalkDemo extends Frame
    implements WindowListener, SceneGraphTraversalObserver
{
    /** The colour to use for the fog and background */
    private static final float[] FOG_COLOUR = { 0.5f, 0.5f, 0.5f };

    /** Colour 1 to use for local fog effects */
    private static final float[] FOG_COLOUR1 = { 0, 0, 0.5f };

    /** Colour 3 to use for local fog effects */
    private static final float[] FOG_COLOUR3 = { 0, 0.5f, 0 };

    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    public SceneWalkDemo()
    {
        super("Combined Local & Global fog effect Aviatrix Demo");

        setLayout(new BorderLayout());
        addWindowListener(this);

        setupAviatrix();
        SceneGraphObject root = setupSceneGraph();

        setSize(600, 600);
        setLocation(40, 40);

        // Need to set visible first before starting the rendering thread due
        // to a bug in JOGL. See JOGL Issue #54 for more information on this.
        // http://jogl.dev.java.net
        setVisible(true);

        SceneGraphTraverser sgt = new SceneGraphTraverser();
        sgt.setObserver(this);
        sgt.traverseGraph(root);
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
        surface = new SimpleAWTSurface(caps);
        surface.setClearColor(FOG_COLOUR[0], FOG_COLOUR[1], FOG_COLOUR[2], 1);
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
    private SceneGraphObject setupSceneGraph()
    {
        // View group
        Viewpoint vp = new Viewpoint();
        vp.setHeadlightEnabled(true);

        Vector3f trans = new Vector3f(0, 1, 2);

        Matrix4f mat = new Matrix4f();
        mat.setIdentity();
        mat.rotX((float)(Math.PI / -8));
        mat.setTranslation(trans);


        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);

        // The root transform that we'll put all our fogged primitives
        // under.
        TransformGroup shape_transform = new TransformGroup();

        // Create the number of primitives to use
        createPrimitives(4, shape_transform);

        scene_root.addChild(shape_transform);

        Fog fog = new Fog(FOG_COLOUR);
        fog.setEnabled(true);
        fog.setLinearDistance(0.1f, 3f);
        scene_root.addChild(fog);

        SimpleScene scene = new SimpleScene();
        scene.setRenderedGeometry(scene_root);
        scene.setActiveView(vp);
        scene.setActiveFog(fog);

        // Then the basic layer and viewport at the top:
        SimpleViewport view = new SimpleViewport();
        view.setDimensions(0, 0, 500, 500);
        view.setScene(scene);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(view);

        Layer[] layers = { layer };
        displayManager.setLayers(layers, 1);

        return scene_root;
    }

    //---------------------------------------------------------------
    // Methods defined by SceneGraphTraversalObserver
    //---------------------------------------------------------------

    /**
     * Notification of a scene graph object that has been traversed in the
     * scene.
     *
     * @param parent The parent node of this node
     * @param child The child node that is being observed
     * @param shared true if the object reference has already been traversed
     *    and this is beyond the first reference
     * @param depth The depth of traversal from the top of the tree.  Starts at 0 for top.
     */
    public void observedNode(SceneGraphObject parent,
                             SceneGraphObject child,
                             boolean shared,
                             int depth)
    {
        for(int i=0; i < depth; i++) {
            System.out.print("   ");
        }

        if (parent == null)
        {
            System.out.println("ROOT");
            return;
        } else
        {
            System.out.println(parent.getClass().getName());
        }

        for(int i=0; i < depth+1; i++) {
            System.out.print("   ");
        }

        if (child == null)
        {
            System.out.println("NULL child?  Parent: " + parent);
        } else
        {
            System.out.print(child.getClass().getName());
            if(shared)
                System.out.println(" (copy) ");
            else
                System.out.println();
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

    /**
     * Create a collection of primitives to use and place them under the
     * parent group.
     *
     * @param num The number of primitives to make
     * @param parent The group node to add these to
     */
    private void createPrimitives(int num, Group parent)
    {
        double angle_inc = 2 * Math.PI / num;
        double angle = 0;
        Vector3f translation = new Vector3f();

        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();

        Material material = new Material();
        material.setDiffuseColor(new float[] { 1, 0, 0 });
        material.setSpecularColor(new float[] { 0.4f, 0.4f, 0.4f });
        material.setLightingEnabled(true);

        Appearance app = new Appearance();
        app.setMaterial(material);

        for(int i = 0; i < num; i++)
        {
            float x = 0.5f * (float)Math.sin(angle);
            float y = 0.5f * (float)Math.cos(angle);

            angle += angle_inc;

            translation.x = x;
            translation.z = y;

            matrix.setTranslation(translation);

            TransformGroup tg = new TransformGroup();
            tg.setTransform(matrix);

            parent.addChild(tg);

            switch(i % 4)
            {
                case 0:
                    // Box
                    Fog fog = new Fog(FOG_COLOUR1, false);
                    fog.setEnabled(true);
                    fog.setLinearDistance(1.5f, 3);
                    tg.addChild(fog);

                    Box box = new Box(0.125f, 0.125f, 0.125f, app);
                    tg.addChild(box);
                    break;

                case 1:
                    // Cone
                    Cone cone = new Cone(0.25f, 0.125f, app);
                    tg.addChild(cone);
                    break;

                case 2:
                    // cylinder
                    Cylinder cyl = new Cylinder(0.25f, 0.125f, app);
                    tg.addChild(cyl);
                    break;

                case 3:
                    fog = new Fog(FOG_COLOUR3, false);
                    fog.setEnabled(true);
                    fog.setLinearDistance(2, 3);
                    tg.addChild(fog);

                    // sphere
                    Sphere sphere = new Sphere(0.125f, app);
                    tg.addChild(sphere);

            }
        }
    }

    public static void main(String[] args)
    {
        SceneWalkDemo demo = new SceneWalkDemo();
        demo.setVisible(true);
    }
}
