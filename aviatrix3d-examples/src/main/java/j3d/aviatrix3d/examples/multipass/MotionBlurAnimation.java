package j3d.aviatrix3d.examples.multipass;

// External imports
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

// Local imports
import org.j3d.aviatrix3d.*;

/**
 * Example application that demonstrates how to put together a single-threaded
 * rendering system.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class MotionBlurAnimation
    implements ApplicationUpdateObserver, NodeUpdateListener
{
    /** Matrices representing each of the blurred object positions. */
    private Matrix4d[] matrix;

    /** Work variable to update the translation with */
    private Vector3d translation;

    /** The current angle */
    private float angle;

    /** The scene graph node to update */
    private TransformGroup[] transform;

    /**
     *
     */
    public MotionBlurAnimation(TransformGroup[] tx)
    {
        translation = new Vector3d();
        transform = tx;

        int size = tx.length;
        matrix = new Matrix4d[size];

        for(int i = 0; i < size; i++)
        {
            matrix[i] = new Matrix4d();
            matrix[i].setIdentity();
        }
    }

    //---------------------------------------------------------------
    // Methods defined by ApplicationUpdateObserver
    //---------------------------------------------------------------

    /**
     * Notification that now is a good time to update the scene graph.
     */
    public void updateSceneGraph()
    {
        for(int i = 0; i < transform.length; i++)
            transform[i].boundsChanged(this);

        // Calculate the new position. First shift the positions down one spot
        // and use the last item as the new matrix to fill in.
        Matrix4d mat = matrix[transform.length - 1];

        System.arraycopy(matrix, 0, matrix, 1, transform.length - 1);

        matrix[0] = mat;

        angle += Math.PI / 8;

        float x = 0.5f * (float)Math.sin(angle);
        float y = 0.5f * (float)Math.cos(angle);

        translation.x = x;
        translation.y = y;

        mat.setTranslation(translation);
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
        // find the matching transform
        for(int i = 0; i < transform.length; i++)
        {
            if(transform[i] == src)
                transform[i].setTransform(matrix[i]);
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
