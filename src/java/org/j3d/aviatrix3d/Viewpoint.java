/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d;

// Standard imports
import java.util.HashMap;
//import org.web3d.vecmath.Matrix4f;
import javax.vecmath.Matrix4f;

// Application specific imports
import gl4java.GLFunc;
import gl4java.GLUFunc;
import gl4java.GLContext;
import gl4java.GLEnum;
import gl4java.drawable.GLDrawable;
import gl4java.utils.glut.GLUTFunc;
import gl4java.utils.glut.GLUTFuncLightImpl;

/**
 * A viewpoint into the scene.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class Viewpoint extends Leaf
{
    /** The display list for this item */
    private static int dispList;

    /** Is this the active viewpoint? */
    private boolean isActive;

    /** Work matrix to hold the transformation of the VP in */
    private float[] matrix;

    /**
     * The default constructor
     */
    public Viewpoint()
    {
        matrix = new float[16];

        isActive = false;
    }

    /**
     * This method is called to render this node.  All openGL commands needed
     * to render the node should be executed.  Any transformations needed
     * should be added to the transformation stack premultiplied.  This
     * method must be re-entrant as it can be called from multiple
     * places at once.
     */
    public void render(GLDrawable gld)
    {
        // Do nothing
    }

    /**
     * Setup the viewing matrix
     *
     * @param gld The drawable for setting the state
     */
    public void setupView(GLDrawable gld)
    {
        GLFunc gl = gld.getGL();
        GLUFunc glu = gld.getGLU();

        gl.glLoadIdentity();

        gl.glEnable(gl.GL_LIGHT0);

        // Wait till the nodes are initialized
        if (transform != null) {
            transform.invert();
            // TODO: can we stop this copy?  Transpose in place
            matrix[0] = transform.m00;
            matrix[1] = transform.m10;
            matrix[2] = transform.m20;
            matrix[3] = transform.m30;
            matrix[4] = transform.m01;
            matrix[5] = transform.m11;
            matrix[6] = transform.m21;
            matrix[7] = transform.m31;
            matrix[8] = transform.m02;
            matrix[9] = transform.m12;
            matrix[10] = transform.m22;
            matrix[11] = transform.m32;
            matrix[12] = transform.m03;
            matrix[13] = transform.m13;
            matrix[14] = transform.m23;
            matrix[15] = transform.m33;

            gl.glMultMatrixf(matrix);
        }

        //System.out.println("View matrix: " + transform);
        //SGUtils.printMatrix(gld,GLEnum.GL_MODELVIEW_MATRIX);
    }

    /*
     * This method is called after a node has been rendered.  This method
     * must be re-entrant.
     *
     * @param gld The drawable for resetting the state
     */
    public void postRender(GLDrawable gld)
    {
    }

    /**
     * Tell a node to update its transform.
     */
    public void updateTransform(Matrix4f parentTrans)
    {
        if (parentTrans != null)
        {
            // Default for all nodes is to use its parent transform
            transform = parentTrans;
        }
    }

    /**
     * Set this viewpoint to be the active viewpoint.
     *
     * @param active True to make this the new active viewpoint
     */
    protected void setActive(boolean active)
    {
         isActive = active;
    }
}