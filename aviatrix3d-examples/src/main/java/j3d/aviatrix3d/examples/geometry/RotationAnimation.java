package j3d.aviatrix3d.examples.geometry;

// External imports
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

// Local imports
import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.pipeline.graphics.ViewportResizeManager;

import org.j3d.util.MatrixUtils;

/**
 * Example application that demonstrates how to put together a single-threaded
 * rendering system.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class RotationAnimation
    implements ApplicationUpdateObserver, NodeUpdateListener
{
    /** Matrix used to update the transform */
    private Matrix4d matrix;

    /** The scene graph node to update */
    private TransformGroup transform;

    /** The current angle */
    private float angle;

    /** Utility for doing matrix rotations */
    private MatrixUtils matrixUtils;

    /** Handler for screen resizes */
    private ViewportResizeManager resizeManager;

    /**
     *
     */
    public RotationAnimation(TransformGroup tx, ViewportResizeManager resizer)
    {
        transform = tx;
        resizeManager = resizer;

        matrixUtils = new MatrixUtils();
        matrix = new Matrix4d();
        matrix.setIdentity();
    }

    //---------------------------------------------------------------
    // Methods defined by ApplicationUpdateObserver
    //---------------------------------------------------------------

    /**
     * Notification that now is a good time to update the scene graph.
     */
    public void updateSceneGraph()
    {
        transform.boundsChanged(this);
        resizeManager.sendResizeUpdates();
    }

    /**
     * Notification that the AV3D internal shutdown handler has detected a
     * system-wide shutdown. The aviatrix code has already terminated rendering
     * at the point this method is called, only the user's system code needs to
     * terminate before exiting here.
     */
    public void appShutdown()
    {
        // do nothing
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
        angle += Math.PI / 500;

        matrixUtils.rotateY(angle, matrix);

        transform.setTransform(matrix);
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
}
