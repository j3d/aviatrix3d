/*****************************************************************************
 *                        Yumtech, Inc Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package j3d.aviatrix3d.examples.shader;

// External imports
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

// Local imports
import org.j3d.aviatrix3d.*;
import org.j3d.util.MatrixUtils;

public class SubSurfaceAnimator
    implements ApplicationUpdateObserver,
               NodeUpdateListener
{
    /** The amount to rotate the object each frame, in radians */
    private static final float ROTATION_INC = (float)(Math.PI / 200);

    /* Transform that holds the geometry to the "light" */
    private TransformGroup lightGroup;

    /** Arguments for the cloth shader */
    private ShaderArguments lightDepthTexArgs;

    /** Arguments for the arrow shaders */
    private ShaderArguments renderPassArgs;

    /** Is this the first or second frame for getting logs? */
    private int frameCount;

    /** Shader for the depth rendering pass */
    private ShaderProgram lightDepthShader;

    /** Shader for the final rendering pass */
    private ShaderProgram renderShader;

    /** TG above the geometry in the main scene */
    private TransformGroup mainWorldTransform;

    /** TG above the geometry in the light depth pass */
    private TransformGroup lightDepthTransform;

    /** TG above the geometry in the pbuffer depth pass */
    private TransformGroup cameraDepthTransform;

    /** A utility matrix used for updating the transforms each frame */
    private Matrix4d matrix;

    /** The current angle of object rotation */
    private float rotation;

    /** Utility for performing matrix rotations */
    private MatrixUtils matrixUtils;

    /**
     *
     */
    public SubSurfaceAnimator(TransformGroup light)
    {
        lightGroup = light;

        frameCount = 0;
        matrix = new Matrix4d();
        matrixUtils = new MatrixUtils();
    }

    //---------------------------------------------------------------
    // Methods defined by ApplicationUpdateObserver
    //---------------------------------------------------------------

    /**
     * Notification that now is a good time to update the scene graph.
     */
    public void updateSceneGraph()
    {
/*
For debugging of shader purposes only
*/
//*
        frameCount++;

        if(frameCount == 2)
        {
            int num_objs = lightDepthShader.getNumShaderObjects();
            ShaderObject[] objs = new ShaderObject[num_objs];

            lightDepthShader.getShaderObjects(objs);

            for(int i = 0; i < num_objs; i++)
            {
                String log = objs[i].getLastInfoLog();
                System.out.println("Depth " + i + ": " + log);
            }

            System.out.println("Link: " + lightDepthShader.getLastInfoLog());

            num_objs = renderShader.getNumShaderObjects();
            objs = new ShaderObject[num_objs];

            renderShader.getShaderObjects(objs);

            for(int i = 0; i < num_objs; i++)
            {
                String log = objs[i].getLastInfoLog();
                System.out.println("Render " + i + ": " + log);
            }

            System.out.println("Link: " + renderShader.getLastInfoLog());
        }
//*/
        rotation = (float)((rotation + ROTATION_INC) % (2 * Math.PI));

        matrixUtils.rotateY(rotation, matrix);

        if(mainWorldTransform.isLive())
            mainWorldTransform.boundsChanged(this);

        if(lightDepthTransform.isLive())
            lightDepthTransform.boundsChanged(this);
        if(cameraDepthTransform.isLive())
            cameraDepthTransform.boundsChanged(this);
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
        if(src instanceof TransformGroup)
        {
            ((TransformGroup)src).setTransform(matrix);
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

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Set the parameters for the depth pass structures
     */
    void setLightDepthPassParams(TransformGroup modelTransform,
                                 ShaderProgram depthProg,
                                 ShaderArguments depthArgs)
    {
        lightDepthTransform = modelTransform;
        lightDepthShader = depthProg;
        lightDepthTexArgs = depthArgs;
    }

    /**
     * Set the parameters for the depth pass structures
     */
    void setCameraDepthPassParams(TransformGroup modelTransform)
    {
        cameraDepthTransform = modelTransform;
    }

    void setRenderPassArgs(TransformGroup modelTransform,
                           ShaderProgram renderProg,
                           ShaderArguments renderArgs)
    {
        mainWorldTransform = modelTransform;
        renderPassArgs = renderArgs;
        renderShader = renderProg;
    }
}
