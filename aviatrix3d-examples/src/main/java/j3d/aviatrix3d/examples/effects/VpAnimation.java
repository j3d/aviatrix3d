package j3d.aviatrix3d.examples.effects;

// Standard imports
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;


// Application Specific imports
import org.j3d.aviatrix3d.*;

/**
 * Animator that moves the viewpoint in and out to illustrate the octtree
 * rendering demo.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class VpAnimation
    implements ApplicationUpdateObserver, NodeUpdateListener
{
    /** Work variable to update the translation with */
    private Vector3d translation;

    /** Matrix used to update the transform */
    private Matrix4d matrix;

    /** The scene graph node to update */
    private TransformGroup transform;

    /** The current angle of orientation */
    private float angle;

    /** The current distance from the center */
    private float distance;

    /**
     *
     */
    public VpAnimation(TransformGroup tx)
    {
        translation = new Vector3d();
        matrix = new Matrix4d();
        matrix.setIdentity();
        transform = tx;
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
        angle += Math.PI / 1000;
        distance -= Math.PI / 50;

        float radius = (float)(0.6f * (float)Math.sin(distance) + 1.5f);

//        float x = radius * (float)Math.sin(angle);
//        float y = radius * (float)Math.cos(angle);
//
//        translation.x = x;
//        translation.z = y;

        translation.z = radius;

        matrix.setTranslation(translation);

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
