/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
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
//import org.web3d.vecmath.Matrix4f;
import javax.vecmath.Matrix4f;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLU;

// Local imports
// None

/**
 * A grouping node that contains a transform.  The node contains a single
 * transformation that can position, scale and rotate all its children.
 *
 * The specified transformation must be Affine and have uniform scaling
 * components(SRT-transform).  This class will not
 * check this constraint, so expect odd results if you break this rule, up
 * to and including a possible core reactor meltdown in a foreign country.
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class TransformGroup extends Group
{
    /** Local transformation added to the parent transforms */
    private Matrix4f localTransform;

    // TODO: won't work for multi threads
    private float[] matrix;

    /**
     * The default constructor
     */
    public TransformGroup()
    {
        localTransform = new Matrix4f();
        localTransform.setIdentity();
        transform = new Matrix4f();
        matrix = new float[16];
    }

    /**
     * Construct a TransformGroup given a matrix.
     *
     * @param trans The matrix to use for transformation
     */
    public TransformGroup(Matrix4f trans)
    {
        this();

        localTransform.set(trans);
    }

    /**
     * Set the transform matrix for this class.
     *
     * @param trans The matrix.  Copy by value semantics.
     */
    public void setTransform(Matrix4f trans)
    {
        localTransform.set(trans);
        transform.mul(localTransform);
    }

    /**
     * Tell a node to update its transform.
     */
     public void updateTransform(Matrix4f parentTrans)
     {
        if(parentTrans != null)
        {
            transform.set(localTransform);
            transform.mul(parentTrans);
        }
     }

    /**
     * Set up the rendering state now.
     *
     * @param gld The drawable for setting the state
     */
    public void render(GL gl, GLU glu)
    {
        // TODO: can we stop this copy?  Transpose in place
        matrix[0] = localTransform.m00;
        matrix[1] = localTransform.m10;
        matrix[2] = localTransform.m20;
        matrix[3] = localTransform.m30;
        matrix[4] = localTransform.m01;
        matrix[5] = localTransform.m11;
        matrix[6] = localTransform.m21;
        matrix[7] = localTransform.m31;
        matrix[8] = localTransform.m02;
        matrix[9] = localTransform.m12;
        matrix[10] = localTransform.m22;
        matrix[11] = localTransform.m32;
        matrix[12] = localTransform.m03;
        matrix[13] = localTransform.m13;
        matrix[14] = localTransform.m23;
        matrix[15] = localTransform.m33;

        gl.glPushMatrix();
        gl.glMultMatrixf(matrix);
    }

    /*
     * This method is called after a node has been rendered.  This method
     * must be re-entrant.
     *
     * @param gld The drawable for resetting the state
     */
    public void postRender(GL gl, GLU glu)
    {
        gl.glPopMatrix();
    }
}