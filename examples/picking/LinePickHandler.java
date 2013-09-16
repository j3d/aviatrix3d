
// External imports

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import javax.media.opengl.GLCapabilities;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.picking.PickRequest;

/**
 * Example application that demonstrates how to put together a single-threaded
 * rendering system.
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public class LinePickHandler
    implements ApplicationUpdateObserver, NodeUpdateListener
{
    private static final float[] GREEN = { 0, 1, 0 };
    private static final float[] BLUE = { 0, 0, 1 };

    /** Work variable to update the translation with */
    private Vector3f translation;

    /** Matrix used to update the transform */
    private Matrix4f matrix;

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
     * Create a new handler for the LinePickDemo application that uses the
     * given transform group as input to the picking system.
     */
    public LinePickHandler(Group root, TransformGroup tx, Material mat)
    {
        pickRoot = root;
        material = mat;
        translation = new Vector3f();
        matrix = new Matrix4f();
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
        linePick1.origin[0] = -0.1f;
//        linePick1.destination[0] = 0.1f;
        linePick1.destination[0] = 1;
        linePick1.destination[1] = 0.01f;
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
        int old_count = linePick1.pickCount;

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
        angle += Math.PI / 100;

        float x = 0.4f * (float)Math.sin(angle);

        translation.x = x;

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
        if(linePick1.pickCount != 0)
            material.setEmissiveColor(GREEN);
        else
            material.setEmissiveColor(BLUE);
    }
}
