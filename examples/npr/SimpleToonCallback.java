
// External imports
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import javax.media.opengl.GLCapabilities;

// Local imports
import org.j3d.aviatrix3d.*;

/**
 * Simple animator for rotating a model around the Y axis.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class SimpleToonCallback
    implements ApplicationUpdateObserver, NodeUpdateListener
{
    /** Vert shader to print the results from */
    private ShaderObject vertShader;

    /** Frag shader to print the results from */
    private ShaderObject fragShader;

    /** Compiled shader program to print the results from */
    private ShaderProgram completeShader;

    /** Frame counter so we know when to print out shader debug */
    private int frameCount;

    /** The transform above the ligth that we'll animate */
    private TransformGroup lightTransform;

    /** The current angle */
    private float angle;

    /** Work variable to update the translation with */
    private Vector3f translation;

    /** Matrix used to update the transform */
    private Matrix4f matrix;


    /**
     *
     */
    public SimpleToonCallback(TransformGroup lightTx,
                         ShaderObject vert,
                         ShaderObject frag,
                         ShaderProgram comp)
    {
        lightTransform = lightTx;
        vertShader = vert;
        fragShader = frag;
        completeShader = comp;

        translation = new Vector3f();
        matrix = new Matrix4f();
        matrix.setIdentity();
    }

    //---------------------------------------------------------------
    // Methods defined by ApplicationUpdateObserver
    //---------------------------------------------------------------

    /**
     * Notification that now is a good time to update the scene graph.
     */
    public void updateSceneGraph()
    {
        if(frameCount == 4)
        {
            System.out.println("frag log " + vertShader.getLastInfoLog());
            System.out.println("vert log " + fragShader.getLastInfoLog());
            System.out.println("link log " + completeShader.getLastInfoLog());
            frameCount++;
        }
        else if(frameCount < 4)
            frameCount++;

        lightTransform.boundsChanged(this);
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

    //---------------------------------------------------------------
    // Methods defined by NodeUpdateListener
    //---------------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src)
    {
        angle += Math.PI / 500;

        float x = 10 * (float)Math.sin(angle);
        float y = 10 * (float)Math.cos(angle);

        translation.x = x;
        translation.y = y;
        translation.z = 10;

        matrix.setTranslation(translation);

        lightTransform.setTransform(matrix);
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
