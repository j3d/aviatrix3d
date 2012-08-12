
// External imports
// None

// Local imports
import org.j3d.aviatrix3d.*;

/**
 * Animator for the Electro shader demo.
 * <p>
 * Animates the texture coordinates to give a nice electric arc effect
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public class ElectroAnimation
    implements ApplicationUpdateObserver, NodeUpdateListener
{
	/** Time elapsed since the last call */
	int timeElapsed;

	/** Last time call */
	long lastTime;

	/** Mesh surface to draw electro animation onto */
    private TriangleStripArray geometry;

    /** The 3D texture coordinate array to update */
    private float[] texture3d;

    /**
     * ElectroAnimation constructor
     *
     * @param geom Mesh to draw electro effect onto.
     */
    public ElectroAnimation(TriangleStripArray geom)
    {
    	timeElapsed = 0;
        geometry = geom;

        float[] coords =
        {
             1, 1, 1,
            -1, 1, 1,
             1, 0, 1,
            -1, 0, 1,
             1, -1, 1,
            -1, -1, 1,
        };

        int[] strips = { 6 };

        geometry.setVertices(TriangleArray.COORDINATE_3, coords);
        geometry.setStripCount(strips, 1);

        texture3d = new float[6 * 3];

        lastTime = System.currentTimeMillis() & 0x0000ffff;

        float z = (((float)timeElapsed / 1000.0f) * 0.8f);
        float y = z * 1.82f;

        texture3d[0] = -1;
        texture3d[1] = y - 1;
        texture3d[2] = z;

        texture3d[3] = 1;
        texture3d[4] = y - 1;
        texture3d[5] = z;

        texture3d[6] = -1;
        texture3d[7] = y;
        texture3d[8] = z;

        texture3d[9] = 1;
        texture3d[10] = y;
        texture3d[11] = z;

        texture3d[12] = -1;
        texture3d[13] = y - 1;
        texture3d[14] = z;

        texture3d[15] = 1;
        texture3d[16] = y - 1;
        texture3d[17] = z;

        float[] texture2d = new float[6 * 2];
        int[] tex_sets = { 0, 1 };
        int[] tex_types =
        {
            VertexGeometry.TEXTURE_COORDINATE_3,
            VertexGeometry.TEXTURE_COORDINATE_2,
        };

        texture2d[0] = 1;
        texture2d[1] = -0.4f;

        texture2d[2] = 1;
        texture2d[3] = 0.4f;

        texture2d[4] = 0;
        texture2d[5] = -0.4f;

        texture2d[6] = 0;
        texture2d[7] = 0.4f;

        texture2d[8] = -1;
        texture2d[9] = -0.4f;

        texture2d[10] = -1;
        texture2d[11] = 0.4f;

        float[][] tex_coords = { texture3d, texture2d };

        geometry.setTextureCoordinates(tex_types,
                                       tex_coords,
                                       2);
        geometry.setTextureSetMap(tex_sets, 2);
    }

    //---------------------------------------------------------------
    // Methods defined by ApplicationUpdateObserver
    //---------------------------------------------------------------

    /**
     * Notification that now is a good time to update the scene graph.
     */
    public void updateSceneGraph()
    {
        geometry.dataChanged(this);
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
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src)
    {
    	long curTime = System.currentTimeMillis() & 0x0000ffff;

    	long diff = (curTime - lastTime);

        timeElapsed += diff;

        if(timeElapsed < 0) {
        	timeElapsed = 0;
        }

        lastTime = curTime;

        float z = (((float)timeElapsed / 1000.0f) * 0.8f);
        float y = z * 1.82f;

        texture3d[0] = -1;
        texture3d[1] = y - 1;
        texture3d[2] = z;

        texture3d[3] = 1;
        texture3d[4] = y - 1;
        texture3d[5] = z;

        texture3d[6] = -1;
        texture3d[7] = y;
        texture3d[8] = z;

        texture3d[9] = 1;
        texture3d[10] = y;
        texture3d[11] = z;

        texture3d[12] = -1;
        texture3d[13] = y - 1;
        texture3d[14] = z;

        texture3d[15] = 1;
        texture3d[16] = y - 1;
        texture3d[17] = z;

        geometry.setTextureCoordinates(VertexGeometry.TEXTURE_COORDINATE_3,
                                       0,
                                       texture3d);
    }
}
