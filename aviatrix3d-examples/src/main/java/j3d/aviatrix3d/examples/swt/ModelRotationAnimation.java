package j3d.aviatrix3d.examples.swt;

// Standard imports
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

// Application Specific imports
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.pipeline.graphics.ViewportResizeManager;
import org.j3d.util.MatrixUtils;

/**
 * Simple animator for rotating a model around the Y axis.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class ModelRotationAnimation
    implements ApplicationUpdateObserver, NodeUpdateListener
{
    /** Matrix used to update the transform */
    private Matrix4d matrix;

    /** The scene graph node to update */
    private TransformGroup transform;

    /** The current angle */
    private float angle;

    /** Resize handling */
    private ViewportResizeManager resizer;

    /** Utility for performing matrix rotations */
    private MatrixUtils matrixUtils;

    /**
     *
     */
    public ModelRotationAnimation(TransformGroup tx, ViewportResizeManager resizer)
    {
        this.resizer = resizer;
        matrix = new Matrix4d();
        matrix.setIdentity();
        transform = tx;
        matrixUtils = new MatrixUtils();
    }

    //---------------------------------------------------------------
    // Methods defined by ApplicationUpdateObserver
    //---------------------------------------------------------------

    @Override
    public void updateSceneGraph()
    {
        resizer.sendResizeUpdates();
        transform.boundsChanged(this);
    }

    @Override
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
        angle += Math.PI / 300;

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
