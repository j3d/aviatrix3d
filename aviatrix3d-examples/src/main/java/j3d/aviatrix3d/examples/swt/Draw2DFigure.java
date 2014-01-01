/*****************************************************************************
 *                        Yumetech, Inc Copyright (c) 2005 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU GPL v2.0
 * Please read http://www.gnu.org/copyleft/gpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

// External imports
import javax.media.opengl.*;
import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.pipeline.graphics.*;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.widgets.Display;

import org.j3d.renderer.aviatrix3d.swt.draw2d.SimpleDraw2DSurface;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;

// Local imports
// None

/**
 * Example figure that shows how to combine Aviatrix3D with a lightweight
 * figure used in Draw2D.
 * <p>
 *
 * This figure can be used inside a GEF framework as well. It combines the
 * basic static demo scene with the scene graph update. When the figure is
 * resized, everything is resized with it. However, since we're not prodding
 * the scene with updates, the redraw is only repainting the existing window
 * and not resizing. If you want resizing of the viewport, then you'll need
 * to also extend this with an application update observer that pings the
 * resize manager each frame and/or also call renderOnce().
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class Draw2DFigure extends Figure
{
    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    /** Resize handling */
    private ViewportResizeManager resizer;

    /**
     * Create an Aviatrix3D test figure.
     */
    public Draw2DFigure(Display d)
    {
        setLayoutManager(new StackLayout());

        resizer = new ViewportResizeManager();

        setupAviatrix(d);
        setupSceneGraph();
    }

    //----------------------------------------------------------
    // Methods defined by Figure
    //----------------------------------------------------------

    /**
     * Change the sige of this figure to the new dimensions. Subclasses that
     * override this method should make sure it is called so that we can resize
     * the OpenGL information appropriately.
     *
     * @param r The rectangle representing the bounds
     */
    public void setBounds(Rectangle r)
    {
        super.setBounds(r);

        // Need to explicitly lay out the children because the GLFigure
        // doesn't always get the resize from here.
        layout();

        if(r.width != 0 && r.height != 0)
            sceneManager.setEnabled(true);
    }

    /**
     * Change the sige of this figure to the new dimensions. Subclasses that
     * override this method should make sure it is called so that we can resize
     * the OpenGL information appropriately.
     *
     * @param w The new width to use
     * @param h The new height to use
     */
    public void setSize(int w, int h)
    {
        super.setSize(w, h);

        // Need to explicitly lay out the children because the GLFigure
        // doesn't always get the resize from here.
        layout();

        if(w != 0 && h != 0)
            sceneManager.setEnabled(true);
    }

    /**
     * Overridden to track when this component is removed from the parent
     * figure and hence may be able to throw away OpenGL state. Subclasses
     * which override this method must call <code>super.removeNotify()</code>
     * in their <code>removeNotify()</code> method in order to function properly.
     */
    public void removeNotify()
    {
        sceneManager.setEnabled(false);

        super.removeNotify();
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Setup the avaiatrix pipeline here
     */
    private void setupAviatrix(Display device)
    {
        // Assemble a simple single-threaded pipeline.

        GraphicsCullStage culler = new SimpleFrustumCullStage();
        culler.setOffscreenCheckEnabled(false);

        GraphicsSortStage sorter = new StateAndTransparencyDepthSortStage();
        surface = new SimpleDraw2DSurface(device);
//        surface = new DebugDraw2DSurface(device);

        surface.setClearColor(0.2f, 0.2f, 0.2f, 1);
        surface.addGraphicsResizeListener(resizer);
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

        add((Figure)surface.getSurfaceObject());
    }

    /**
     * Setup the basic scene which is a viewpoint along with the model that
     * was loaded.
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
        float[] coord = { 0, 0, -1, 0.25f, 0, -1, 0, 0.25f, -1 };
        float[] normal = { 0, 0, 1, 0, 0, 1, 0, 0, 1 };
        float[] color = { 0, 0, 1, 0, 1, 0, 1, 0, 0 };

        TriangleArray geom = new TriangleArray();
        geom.setValidVertexCount(3);
        geom.setVertices(TriangleArray.COORDINATE_3, coord);
        geom.setNormals(normal);
        geom.setColors(false, color);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);

        trans.set(0, 0, 0);
        Matrix4f mat2 = new Matrix4f();
        mat2.setIdentity();
        mat2.setTranslation(trans);

        TransformGroup shape_transform = new TransformGroup();
        shape_transform.addChild(shape);
        shape_transform.setTransform(mat2);

        scene_root.addChild(shape_transform);

        SimpleScene scene = new SimpleScene();
        scene.setRenderedGeometry(scene_root);
        scene.setActiveView(vp);

        // Then the basic layer and viewport at the top:
        SimpleViewport view = new SimpleViewport();
        view.setDimensions(0, 0, 50, 50);
        view.setScene(scene);

        resizer.addManagedViewport(view);

        SimpleLayer layer = new SimpleLayer();
        layer.setViewport(view);

        Layer[] layers = { layer };
        displayManager.setLayers(layers, 1);
    }
}
