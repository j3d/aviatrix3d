package j3d.aviatrix3d.examples.picking;

// External imports
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.picking.PickRequest;

/**
 * Example application that demonstrates how to put together a single-threaded
 * rendering system.
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public class MovingPickerHandler
    implements ApplicationUpdateObserver, NodeUpdateListener
{
    private static final float[] GREEN = { 0, 1, 0 };
    private static final float[] BLUE = { 0, 0, 1 };
    private static final float[] RED = { 1, 0, 0 };

    /** Work variable to update the translation with */
    private Vector3d translation;

    /** Matrix used to update the transform */
    private Matrix4d matrix;

    /** The scene graph node to update */
    private TransformGroup transform;

    /** Group that we pick against */
    private Group pickRoot;

    /** The current angle */
    private float angle;

    /** What we are going to be doing the pick with */
    private PickRequest linePick1;

    /** The material used for changing colours depending on pick state */
    private Material material;

    /**
     * Create a new handler for the MovingPickerPick application that uses the
     * given transform group as input to the picking system.
     */
    public MovingPickerHandler(Group root, TransformGroup tx, Material mat)
    {
        pickRoot = root;
        material = mat;
        translation = new Vector3d();
        matrix = new Matrix4d();
        matrix.setIdentity();
        transform = tx;

        // Change the two commented out lines to convert between ray and
        // segment picking.
        linePick1 = new PickRequest();
//        linePick1.pickGeometryType = PickRequest.PICK_LINE_SEGMENT;
        linePick1.pickGeometryType = PickRequest.PICK_RAY;
        linePick1.pickSortType = PickRequest.SORT_ALL;
        linePick1.pickType = PickRequest.FIND_ALL;
        linePick1.generateVWorldMatrix = false;
        linePick1.origin[1] = 0.3f;
        linePick1.destination[1] = -1;
    }

    //---------------------------------------------------------------
    // Methods defined by ApplicationUpdateObserver
    //---------------------------------------------------------------

// These are for metrics testing.
//    private long last_time = System.currentTimeMillis();
//    private int counter = 0;

    /**
     * Notification that now is a good time to update the scene graph.
     */
    public void updateSceneGraph()
    {
/*
        if(++counter == 100)
        {
            long new_time = System.currentTimeMillis();
            long t =  new_time - last_time;
            last_time = new_time;

            System.out.println(100 / (t / (1000f)) + " FPS");
            counter = 0;
        }
*/
        angle += Math.PI / 900;
        float x = 0.175f * (float)Math.sin(angle);
        translation.x = x;

        matrix.setTranslation(translation);

        int old_count = linePick1.pickCount;

        linePick1.origin[0] = x;

        transform.boundsChanged(this);
        pickRoot.pickSingle(linePick1);

        if(old_count != linePick1.pickCount)
            material.dataChanged(this);
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
        switch(linePick1.pickCount)
        {
            case 0:
                material.setEmissiveColor(RED);
                break;

            case 1:
                material.setEmissiveColor(GREEN);
                break;

            case 2:
                material.setEmissiveColor(BLUE);
                break;
        }
    }
}
