
// Standard imports
import javax.media.opengl.GLCapabilities;

// Application Specific imports
import org.j3d.aviatrix3d.*;

/**
 * Handler for illustrating updating textures on demand.
 *
 * Since sub-image updates are ignored for any texture that has not yet
 * been part of a live scene graph, or drawn yet we have to put a set of
 * frame delays into the system to make sure the texture has been drawn
 * at least once.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
class TextureUpdater
    implements ApplicationUpdateObserver, NodeUpdateListener
{
    // Simple texture source to see the output
    private static final byte[] TEX_UPDATE_DATA =
    {
        (byte)0xFF, (byte)0,    (byte)0,    (byte)0xFF, (byte)0,   (byte)0,
        (byte)0xFF, (byte)0,    (byte)0,    (byte)0xFF, (byte)0,   (byte)0,
        (byte)0xFF, (byte)0,    (byte)0,    (byte)0xFF, (byte)0,   (byte)0,
        (byte)0xFF, (byte)0,    (byte)0,    (byte)0xFF, (byte)0,   (byte)0,
        (byte)0xFF, (byte)0,    (byte)0,    (byte)0xFF, (byte)0,   (byte)0,
        (byte)0xFF, (byte)0,    (byte)0,    (byte)0xFF, (byte)0,   (byte)0,
        (byte)0xFF, (byte)0,    (byte)0,    (byte)0xFF, (byte)0,   (byte)0,
        (byte)0xFF, (byte)0,    (byte)0,    (byte)0xFF, (byte)0,   (byte)0,
        (byte)0,    (byte)0,    (byte)0xFF, (byte)0,    (byte)0,   (byte)0xFF,
        (byte)0,    (byte)0,    (byte)0xFF, (byte)0,    (byte)0,   (byte)0xFF,
        (byte)0,    (byte)0,    (byte)0xFF, (byte)0,    (byte)0,   (byte)0xFF,
        (byte)0,    (byte)0,    (byte)0xFF, (byte)0,    (byte)0,   (byte)0xFF,
        (byte)0,    (byte)0,    (byte)0xFF, (byte)0,    (byte)0,   (byte)0xFF,
        (byte)0,    (byte)0,    (byte)0xFF, (byte)0,    (byte)0,   (byte)0xFF,
        (byte)0,    (byte)0,    (byte)0xFF, (byte)0,    (byte)0,   (byte)0xFF,
        (byte)0,    (byte)0,    (byte)0xFF, (byte)0,    (byte)0,   (byte)0xFF,
        (byte)0xFF, (byte)0,    (byte)0,    (byte)0xFF, (byte)0,   (byte)0,
        (byte)0xFF, (byte)0,    (byte)0,    (byte)0xFF, (byte)0,   (byte)0,
        (byte)0xFF, (byte)0,    (byte)0,    (byte)0xFF, (byte)0,   (byte)0,
        (byte)0xFF, (byte)0,    (byte)0,    (byte)0xFF, (byte)0,   (byte)0,
        (byte)0xFF, (byte)0,    (byte)0,    (byte)0xFF, (byte)0,   (byte)0,
        (byte)0xFF, (byte)0,    (byte)0,    (byte)0xFF, (byte)0,   (byte)0,
        (byte)0xFF, (byte)0,    (byte)0,    (byte)0xFF, (byte)0,   (byte)0,
        (byte)0xFF, (byte)0,    (byte)0,    (byte)0xFF, (byte)0,   (byte)0,
        (byte)0,    (byte)0,    (byte)0xFF, (byte)0,    (byte)0,   (byte)0xFF,
        (byte)0,    (byte)0,    (byte)0xFF, (byte)0,    (byte)0,   (byte)0xFF,
        (byte)0,    (byte)0,    (byte)0xFF, (byte)0,    (byte)0,   (byte)0xFF,
        (byte)0,    (byte)0,    (byte)0xFF, (byte)0,    (byte)0,   (byte)0xFF,
        (byte)0,    (byte)0,    (byte)0xFF, (byte)0,    (byte)0,   (byte)0xFF,
        (byte)0,    (byte)0,    (byte)0xFF, (byte)0,    (byte)0,   (byte)0xFF,
        (byte)0,    (byte)0,    (byte)0xFF, (byte)0,    (byte)0,   (byte)0xFF,
        (byte)0,    (byte)0,    (byte)0xFF, (byte)0,    (byte)0,   (byte)0xFF,
    };

    /** The scene graph node to update */
    private ByteTextureComponent2D texSource;

    /** Current X location of last update */
    private int xLoc;

    /** Current X location of last update */
    private int yLoc;

    /** Have we updated already? */
    private int updateCount;

    /**
     *
     */
    public TextureUpdater(ByteTextureComponent2D source)
    {
        texSource = source;
        updateCount = 0;
    }

    //---------------------------------------------------------------
    // Methods defined by ApplicationUpdateObserver
    //---------------------------------------------------------------

    /**
     * Notification that now is a good time to update the scene graph.
     */
    public void updateSceneGraph()
    {
        if(++updateCount == 3)
        {
            texSource.dataChanged(this);
        }
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
        texSource.updateSubImage(5, 5, 8, 8, 0, TEX_UPDATE_DATA);
    }
}
