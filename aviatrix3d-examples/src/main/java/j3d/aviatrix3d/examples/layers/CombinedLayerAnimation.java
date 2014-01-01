
// External imports
import java.awt.*;
import java.awt.event.*;

import java.io.File;
import java.net.MalformedURLException;

import java.net.URL;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import javax.media.opengl.GLCapabilities;

// Local imports
import org.j3d.aviatrix3d.*;

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
    private Vector3f translation;

    /** Matrix used to update the transform */
    private Matrix4f matrix;

    /** The scene graph node to update */
    private TransformGroup movingTransform;

    /** The scene graph node to update */
    private TransformGroup rotatingTransform;

    /** The current angle */
    private float angle;

    /** The amount of rotation in radians */
    private float rotation;

    /**
     *
     */
    public CombinedLayerAnimation(TransformGroup rotator,
                                  TransformGroup mover)
    {
        translation = new Vector3f();
        matrix = new Matrix4f();
        matrix.setIdentity();
        rotatingTransform = rotator;
        movingTransform = mover;
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

            matrix.setIdentity();
            matrix.rotZ(rotation);

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
