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
 * @version $Revision: 1.5 $
 */
public class MovingBatchPickerHandler
    implements ApplicationUpdateObserver, NodeUpdateListener
{
    private static final float[] GREEN = { 0, 1, 0 };
    private static final float[] BLUE = { 0, 0, 1 };
    private static final float[] RED = { 1, 0, 0 };

    /** Work variable to update the translation with */
    private Vector3d translation;

    /** Matrix used to update the lineTransform */
    private Matrix4d lineMatrix;

    /** The scene graph node to update */
    private TransformGroup lineTransform;

    /** Matrix used to update the pointTransform */
    private Matrix4d pointMatrix;

    /** The scene graph node to update */
    private TransformGroup pointTransform;

    /** Group that we pick against */
    private Group pickRoot;

    /** The current angle */
    private float angle;

    /** What we are going to be doing the pick with */
    private PickRequest[] batchPick;

    /** The material used for changing colours depending on pick state */
    private Material lineMaterial;

    /** The material used for changing colours depending on pick state */
    private Material pointMaterial;

    /**
     * Create a new handler for the MovingBatchPickerPick application that uses the
     * given lineTransform group as input to the picking system.
     */
    public MovingBatchPickerHandler(Group root,
                                    TransformGroup tx1,
                                    TransformGroup tx2,
                                    Material mat1,
                                    Material mat2)
    {
        pickRoot = root;
        lineMaterial = mat1;
        pointMaterial = mat2;

        translation = new Vector3d();

        lineMatrix = new Matrix4d();
        lineMatrix.setIdentity();
        lineTransform = tx1;

        pointMatrix = new Matrix4d();
        pointMatrix.setIdentity();
        pointTransform = tx2;

        batchPick = new PickRequest[2];

        batchPick[0] = new PickRequest();
        batchPick[0].pickGeometryType = PickRequest.PICK_RAY;
        batchPick[0].pickSortType = PickRequest.SORT_ALL;
        batchPick[0].pickType = PickRequest.FIND_ALL;
        batchPick[0].generateVWorldMatrix = false;
        batchPick[0].origin[1] = 0.3f;
        batchPick[0].destination[1] = -1;

        batchPick[1] = new PickRequest();
        batchPick[1].pickGeometryType = PickRequest.PICK_POINT;
        batchPick[1].pickSortType = PickRequest.SORT_ALL;
        batchPick[1].pickType = PickRequest.FIND_ALL;
        batchPick[1].generateVWorldMatrix = false;
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
        angle += Math.PI / 700;
        float x = 0.175f * (float)Math.sin(angle);
        float y = 0.175f * (float)Math.cos(angle);
        translation.x = x;

        lineMatrix.setTranslation(translation);

        translation.x = x * 0.4d;
        translation.y = y * 0.4d;

        pointMatrix.setTranslation(translation);

        int old_count1 = batchPick[0].pickCount;
        int old_count2 = batchPick[1].pickCount;

        batchPick[0].origin[0] = x;

        batchPick[1].origin[0] = x * 0.4f;
        batchPick[1].origin[1] = y * 0.4f;

        lineTransform.boundsChanged(this);
        pointTransform.boundsChanged(this);

        pickRoot.pickBatch(batchPick, 2);

        if(old_count1 != batchPick[0].pickCount)
            lineMaterial.dataChanged(this);

        if(old_count2 != batchPick[1].pickCount)
            pointMaterial.dataChanged(this);
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
        if(src == lineTransform)
            lineTransform.setTransform(lineMatrix);
        else
            pointTransform.setTransform(pointMatrix);
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src)
    {
        if(src == lineMaterial)
        {
            switch(batchPick[0].pickCount)
            {
                case 0:
                    lineMaterial.setEmissiveColor(RED);
                    break;

                case 1:
                    lineMaterial.setEmissiveColor(GREEN);
                    break;

                case 2:
                    lineMaterial.setEmissiveColor(BLUE);
                    break;
            }
        }
        else
        {
            switch(batchPick[1].pickCount)
            {
                case 0:
                    pointMaterial.setEmissiveColor(RED);
                    break;

                case 1:
                    pointMaterial.setEmissiveColor(GREEN);
                    break;

                case 2:
                    pointMaterial.setEmissiveColor(BLUE);
                    break;
            }

        }
    }
}
