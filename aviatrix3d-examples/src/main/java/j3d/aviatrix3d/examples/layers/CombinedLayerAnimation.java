package j3d.aviatrix3d.examples.layers;

// External imports
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

// Local imports
import org.j3d.aviatrix3d.*;
import org.j3d.renderer.aviatrix3d.pipeline.ViewportResizeManager;

import org.j3d.util.MatrixUtils;

/**
 * Animator for moving an object about a circular path for the MultiLayerDemo.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class CombinedLayerAnimation
    implements ApplicationUpdateObserver, NodeUpdateListener
{
    /** Work variable to update the translation with */
    private Vector3d translation;

    /** Matrix used to update the transform */
    private Matrix4d matrix;

    /** The scene graph node to update */
    private TransformGroup movingTransform;

    /** The scene graph node to update */
    private TransformGroup rotatingTransform;

    /** The current angle */
    private float angle;

    /** The amount of rotation in radians */
    private float rotation;

    /** Utility for doing matrix rotations */
    private MatrixUtils matrixUtils;

    private ViewportResizeManager resizeManager;

    /**
     *
     */
    public CombinedLayerAnimation(TransformGroup rotator,
                                  TransformGroup mover,
                                  ViewportResizeManager resizer)
    {
        translation = new Vector3d();
        matrix = new Matrix4d();
        matrix.setIdentity();
        rotatingTransform = rotator;
        movingTransform = mover;

        matrixUtils = new MatrixUtils();
        resizeManager = resizer;
    }

    //---------------------------------------------------------------
    // Methods defined by ApplicationUpdateObserver
    //---------------------------------------------------------------

    /**
     * Notification that now is a good time to update the scene graph.
     */
    public void updateSceneGraph()
    {
        rotatingTransform.boundsChanged(this);
        movingTransform.boundsChanged(this);
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
    // Methods defined by NodeUpdateListener
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src)
    {
        if(src == movingTransform)
        {
            angle += Math.PI / 1000;

            float z = 0.5f * (float)Math.sin(angle);
            float y = 0.5f * (float)Math.cos(angle);

    //        translation.x = x;
            translation.y = y;
            translation.z = z;

            matrix.setIdentity();
            matrix.setTranslation(translation);

            movingTransform.setTransform(matrix);
        }
        else
        {
            rotation += Math.PI / 300;

            matrixUtils.rotateZ(rotation, matrix);

            rotatingTransform.setTransform(matrix);
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
}
