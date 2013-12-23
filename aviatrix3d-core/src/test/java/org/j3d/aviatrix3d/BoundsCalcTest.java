/**************************************************************************
 *                        Copyright j3d.org (c) 2000 - ${year}
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read docs/lgpl.txt for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ***************************************************************************/

package org.j3d.aviatrix3d;

// External imports
import java.awt.*;
import java.awt.event.*;

import javax.media.opengl.GLCapabilities;

// Local imports

import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.output.graphics.SimpleAWTSurface;
import org.j3d.aviatrix3d.pipeline.graphics.*;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;
import org.j3d.aviatrix3d.rendering.BoundingVolume;

import org.j3d.geom.GeometryData;
import org.j3d.geom.SphereGenerator;
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

/**
 * Test of the bounds recalculation subsystem.
 *
 * Frame 0: Setup initial scenegraph
 * Frame 1: Check bounds of initial graph
 * Frame 2: Add 1st child
 * Frame 3: Test bounds of single child
 * Frame 4: Add 2nd child
 * Frame 5: Test bounds of combined children
 * Frame 6: Remove 1st child
 * Frame 7: Test bounds
 * Frame 8: Remove 2nd child
 * Frame 9: Test bounds
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class BoundsCalcTest
    implements ApplicationUpdateObserver, WindowListener, NodeUpdateListener
{
    private static final boolean SORT = true;
    private static final boolean CULL = true;

    /** Should we make updates to the SG each frame to force a cull/sort */
    private static final boolean DYNAMIC = true;

    private Vector3d vpPos;
    private Matrix4d vpMat;
    private TransformGroup vpTx;

    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    private TransformGroup shape_transform;
    private Shape3D shape;
    private Shape3D shape2;
    private Group sceneRoot;

    private int frameCount = 0;

    public BoundsCalcTest()
    {
        vpPos = new Vector3d();
        vpPos.set(0, 0, 10);

        sceneRoot = new Group();

        setupAviatrix();
        setupSceneGraph();
    }

    /**
     * Setup the avaiatrix pipeline here
     */
    private void setupAviatrix()
    {
        // Render manager
        sceneManager = new SingleThreadRenderManager();
        sceneManager.setApplicationObserver(this);
        //sceneManager.setMinimumFrameInterval(100);
    }

    /**
     * Setup the basic scene which consists of a quad and a viewpoint
     */
    private void setupSceneGraph()
    {
        // View group

        Viewpoint vp = new Viewpoint();

        vpMat = new Matrix4d();
        vpMat.setIdentity();
        vpMat.set(vpPos);

        vpTx = new TransformGroup();
        vpTx.addChild(vp);
        vpTx.setTransform(vpMat);

        sceneRoot = new Group();
        sceneRoot.addChild(vpTx);

        // Sphere to render the shader onto
        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA;

        generate(data);

        Matrix4d mat = new Matrix4d();
        Vector3d trans = new Vector3d();

        TriangleArray geom = new TriangleArray();
        geom.setVertices(TriangleArray.COORDINATE_3,
                         data.coordinates,
                         data.vertexCount);
        geom.setNormals(data.normals);

        shape = new Shape3D();
        shape.setGeometry(geom);

        generate2(data);

        mat = new Matrix4d();
        trans = new Vector3d();

        geom = new TriangleArray();
        geom.setVertices(TriangleArray.COORDINATE_3,
                         data.coordinates,
                         data.vertexCount);
        geom.setNormals(data.normals);

        shape2 = new Shape3D();
        shape2.setGeometry(geom);

        trans.set(-1,0,0);
        mat.setIdentity();
        mat.set(trans);

        shape_transform = new TransformGroup();
        shape_transform.setTransform(mat);
        sceneRoot.addChild(shape_transform);

        SimpleScene scene = new SimpleScene();
        scene.setRenderedGeometry(sceneRoot);
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
    // Methods defined by ApplicationUpdateObserver
    //---------------------------------------------------------------

    /**
     * Notification that now is a good time to update the scene graph.
     */
    public void updateSceneGraph()
    {
        if (DYNAMIC)
            vpTx.boundsChanged(this);

        frameCount++;

        switch(frameCount) {
            case 2:
                System.out.println("Parent Bounds: " + shape_transform.getBounds());
                compareBounds(shape_transform.getBounds(), null, null);

                System.out.println("Scene Bounds: " + sceneRoot.getBounds());
                compareBounds(sceneRoot.getBounds(), null, null);
                System.out.println();
                break;

            case 3:
                shape_transform.boundsChanged(this);
                break;

            case 4:
                System.out.println("Parent Bounds: " + shape_transform.getBounds());
                compareBounds(shape_transform.getBounds(), new float[] {-2,0,0}, new float[] {0,2,1});

                System.out.println("Scene Bounds: " + sceneRoot.getBounds());
                compareBounds(sceneRoot.getBounds(), new float[] {-2,0,0}, new float[] {0,2,1});
                System.out.println();
                break;

            case 5:
                shape_transform.boundsChanged(this);
                break;

            case 6:
                System.out.println("Parent Bounds: " + shape_transform.getBounds());
                compareBounds(shape_transform.getBounds(), new float[] {-2,0,0}, new float[] {2,2,1});

                System.out.println("Scene Bounds: " + sceneRoot.getBounds());
                compareBounds(sceneRoot.getBounds(), new float[] {-2,0,0}, new float[] {2,2,1});
                System.out.println();
                break;

            case 7:
                shape_transform.boundsChanged(this);
                break;

            case 8:
                System.out.println("Parent Bounds: " + shape_transform.getBounds());
                compareBounds(shape_transform.getBounds(), new float[] {0,0,1}, new float[] {2,2,1});

                System.out.println("Scene Bounds: " + sceneRoot.getBounds());
                compareBounds(sceneRoot.getBounds(), new float[] {0,0,1}, new float[] {2,2,1});
                System.out.println();
                break;

            case 9:
                shape_transform.boundsChanged(this);
                break;

            case 10:
                System.out.println("Parent Bounds: " + shape_transform.getBounds());
                compareBounds(shape_transform.getBounds(), null, null);

                System.out.println("Scene Bounds: " + sceneRoot.getBounds());
                compareBounds(sceneRoot.getBounds(), null, null);
                System.out.println();
                break;

            case 11:
                System.exit(0);
        }
    }

    /**
     * Shutdown notification.
     */
    public void appShutdown()
    {
    }

    //----------------------------------------------------------
    // Methods required by the UpdateListener interface.
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src)
    {
        if (src == shape_transform) 
        {
            switch(frameCount) 
            {
                case 3:
                    System.out.println("Adding 1st Shape:" + shape);
                    shape_transform.addChild(shape);
                    break;

                case 5:
                    System.out.println("Adding 2nd Shape:" + shape2);
                    shape_transform.addChild(shape2);
                    break;

                case 7:
                    System.out.println("Removing 1st Shape:" + shape);
                    shape_transform.removeChild(shape);
                    break;

                case 9:
                    System.out.println("Removing 2nd Shape:" + shape);
                    shape_transform.removeChild(shape2);
                    break;
            }
        } else {
            vpTx.setTransform(vpMat);
        }
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src)
    {
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

    private void generate(GeometryData data)
    {
        data.coordinates = new float[] {1, 0, 1, 0, 2, 0,-1, 0, 1};
        data.vertexCount = data.coordinates.length / 3;
        data.normals = new float[] {0.0f, 0.4472136f, 0.8944272f, 0.0f, 0.4472136f, 0.8944272f, 0.0f, 0.4472136f, 0.8944272f};
    }

    private void generate2(GeometryData data)
    {
        data.coordinates = new float[] {3, 0, 1, 2, 2, 1,1, 0, 1};
        data.vertexCount = data.coordinates.length / 3;
        data.normals = new float[] {0.0f, 0.4472136f, 0.8944272f, 0.0f, 0.4472136f, 0.8944272f, 0.0f, 0.4472136f, 0.8944272f};
    }

    /**
     * Compare bounds to min/max params.
     *
     * @param bounds The bounds to compare
     * @return Are they the same
     */
    private boolean compareBounds(BoundingVolume bounds, float[] min, float[] max)
    {
        if ((bounds.getType() == BoundingVolume.NULL_BOUNDS) && (min == null))
            return true;
        else if (min == null || max == null)
            return false;

        if (!(bounds instanceof BoundingBox))
        {
            return false;
        }

        BoundingBox bbox = (BoundingBox) bounds;
        float[] min2 = new float[3];
        float[] max2 = new float[3];
        bbox.getMinimum(min2);
        bbox.getMaximum(max2);

        if (min[0] == min2[0] && min[1] == min2[1] && min[2] == min2[2] &&
            max[0] == max2[0] && max[1] == max2[1] && max[2] == max2[2])
        {
            return true;
        }

        System.out.println("**** FAIL BOUNDS: " + bounds);
        return false;
    }


    public static void main(String[] args)
    {
        BoundsCalcTest demo = new BoundsCalcTest();
    }
}
