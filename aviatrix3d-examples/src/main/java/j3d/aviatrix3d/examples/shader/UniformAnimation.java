package j3d.aviatrix3d.examples.shader;

// Standard imports
import org.j3d.util.interpolator.ColorInterpolator;

// Application Specific imports
import org.j3d.aviatrix3d.*;

/**
 * Simple animator for changing some argument values of the brick shader
 * on the fly.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class UniformAnimation
    implements ApplicationUpdateObserver, NodeUpdateListener
{
    /** How long the colour cycle should be in ms */
    private static final int CYCLE_INTERVAL = 10000;

    /** The list of shader arguments to update */
    private ShaderArguments shaderArgs;

    /** Interpolator key value */
    private float colourKey;

    /** Interpolation of colour values */
    private ColorInterpolator interpolator;

    /** Start of the cycle time */
    private long cycleStartTime;

    /**
     *
     */
    public UniformAnimation(ShaderArguments args)
    {
        shaderArgs = args;
        interpolator = new ColorInterpolator();
        interpolator.addRGBKeyFrame(0, 1, 0.3d, 0.2f, 0);
        interpolator.addRGBKeyFrame(0.5f, 0, 0.3d, 0.2f, 0);
        interpolator.addRGBKeyFrame(1, 1, 0.3d, 0.2f, 0);
        cycleStartTime = System.currentTimeMillis();
    }

    //---------------------------------------------------------------
    // Methods defined by ApplicationUpdateObserver
    //---------------------------------------------------------------

    /**
     * Notification that now is a good time to update the scene graph.
     */
    public void updateSceneGraph()
    {
        long curr_time = System.currentTimeMillis();
        long elapsed = (curr_time - cycleStartTime);
        if(elapsed > CYCLE_INTERVAL)
            cycleStartTime = curr_time;

        colourKey = elapsed / (float)CYCLE_INTERVAL; 
        shaderArgs.dataChanged(this);
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
         float[] col = interpolator.floatRGBValue(colourKey);
         shaderArgs.setUniform("BrickColor", 3, col, 1);
    }
}
