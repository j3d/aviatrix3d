/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2006
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

// External imports
// None

// Local imports
import org.j3d.maths.vector.Matrix4d;
import org.j3d.util.MatrixUtils;

import org.j3d.aviatrix3d.rendering.TransformCullable;
import org.j3d.aviatrix3d.rendering.BoundingVolume;
import org.j3d.aviatrix3d.picking.TransformPickTarget;

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
 * @version $Revision: 1.32 $
 */
public class TransformGroup extends Group
    implements TransformPickTarget, TransformCullable
{
    /** Global shared copy of the matrix utilities for inversion */
    private static final MatrixUtils matrixUtils = new MatrixUtils();

    /** Local transformation added to the parent transforms */
    private Matrix4d localTransform;

    /** Inverse of the transformation */
    private Matrix4d inverseTransform;

    /**
     * The default constructor
     */
    public TransformGroup()
    {
        localTransform = new Matrix4d();
        localTransform.setIdentity();

        inverseTransform = new Matrix4d();
        inverseTransform.setIdentity();
    }

    /**
     * Construct a TransformGroup given a matrix.
     *
     * @param trans The matrix to use for transformation
     */
    public TransformGroup(Matrix4d trans)
    {
        this();

        setTransform(trans);
    }

    //---------------------------------------------------------------
    // Methods defined by Node
    //---------------------------------------------------------------

    /**
     * Internal method to recalculate the implicit bounds of this Node.
     * Overrides the group version to take into account the transform
     * stack applied to each child.
     */
    protected void recomputeBounds()
    {
        if(!implicitBounds)
            return;

        // Completely override the base class version as we need to transform
        // each point into the local coordinate system before calculating the
        // min and max extents.

        if(lastChild == 0)
        {
            bounds = INVALID_BOUNDS;
            return;
        }

        BoundingVolume bds = null;
        int start_child = 0;

        // Go looking for a non-null, non-void starting bounds
        for( ; start_child < lastChild; start_child++)
        {
            if(childList[start_child] == null)
                continue;

            bds = childList[start_child].getBounds();

            if(!(bds instanceof BoundingVoid))
                break;
        }

        if(start_child == lastChild)
        {
            bounds = INVALID_BOUNDS;
            return;
        }

        if (bds == null) {
            bounds = INVALID_BOUNDS;
            return;
        }

        // Set up the initial conditions
        bds.getExtents(wkVec1, wkVec2);

        float m1_x = wkVec1[0];
        float m1_y = wkVec1[1];
        float m1_z = wkVec1[2];

        float m2_x = wkVec2[0];
        float m2_y = wkVec2[1];
        float m2_z = wkVec2[2];

        // Start with  ax, ay, az
        transform();

        float min_x = wkVec2[0];
        float min_y = wkVec2[1];
        float min_z = wkVec2[2];

        float max_x = wkVec2[0];
        float max_y = wkVec2[1];
        float max_z = wkVec2[2];

        // ax, ay, bz
        wkVec1[2] = m2_z;

        transform();

        if(min_x > wkVec2[0])
            min_x = wkVec2[0];

        if(min_y > wkVec2[1])
            min_y = wkVec2[1];

        if(min_z > wkVec2[2])
            min_z = wkVec2[2];

        if(max_x < wkVec2[0])
            max_x = wkVec2[0];

        if(max_y < wkVec2[1])
            max_y = wkVec2[1];

        if(max_z < wkVec2[2])
            max_z = wkVec2[2];

        // ax, by, bz
        wkVec1[1] = m2_y;

        transform();

        if(min_x > wkVec2[0])
            min_x = wkVec2[0];

        if(min_y > wkVec2[1])
            min_y = wkVec2[1];

        if(min_z > wkVec2[2])
            min_z = wkVec2[2];

        if(max_x < wkVec2[0])
            max_x = wkVec2[0];

        if(max_y < wkVec2[1])
            max_y = wkVec2[1];

        if(max_z < wkVec2[2])
            max_z = wkVec2[2];

        // ax, by, az
        wkVec1[2] = m1_z;

        transform();

        if(min_x > wkVec2[0])
            min_x = wkVec2[0];

        if(min_y > wkVec2[1])
            min_y = wkVec2[1];

        if(min_z > wkVec2[2])
            min_z = wkVec2[2];

        if(max_x < wkVec2[0])
            max_x = wkVec2[0];

        if(max_y < wkVec2[1])
            max_y = wkVec2[1];

        if(max_z < wkVec2[2])
            max_z = wkVec2[2];


        // bx, by, az
        wkVec1[0] = m2_x;

        transform();

        if(min_x > wkVec2[0])
            min_x = wkVec2[0];

        if(min_y > wkVec2[1])
            min_y = wkVec2[1];

        if(min_z > wkVec2[2])
            min_z = wkVec2[2];

        if(max_x < wkVec2[0])
            max_x = wkVec2[0];

        if(max_y < wkVec2[1])
            max_y = wkVec2[1];

        if(max_z < wkVec2[2])
            max_z = wkVec2[2];


        // bx, ay, az
        wkVec1[1] = m1_y;

        transform();

        if(min_x > wkVec2[0])
            min_x = wkVec2[0];

        if(min_y > wkVec2[1])
            min_y = wkVec2[1];

        if(min_z > wkVec2[2])
            min_z = wkVec2[2];

        if(max_x < wkVec2[0])
            max_x = wkVec2[0];

        if(max_y < wkVec2[1])
            max_y = wkVec2[1];

        if(max_z < wkVec2[2])
            max_z = wkVec2[2];

        // bx, ay, bz
        wkVec1[2] = m2_z;

        transform();

        if(min_x > wkVec2[0])
            min_x = wkVec2[0];

        if(min_y > wkVec2[1])
            min_y = wkVec2[1];

        if(min_z > wkVec2[2])
            min_z = wkVec2[2];

        if(max_x < wkVec2[0])
            max_x = wkVec2[0];

        if(max_y < wkVec2[1])
            max_y = wkVec2[1];

        if(max_z < wkVec2[2])
            max_z = wkVec2[2];

        // bx, by, bz
        wkVec1[1] = m2_y;

        transform();

        if(min_x > wkVec2[0])
            min_x = wkVec2[0];

        if(min_y > wkVec2[1])
            min_y = wkVec2[1];

        if(min_z > wkVec2[2])
            min_z = wkVec2[2];

        if(max_x < wkVec2[0])
            max_x = wkVec2[0];

        if(max_y < wkVec2[1])
            max_y = wkVec2[1];

        if(max_z < wkVec2[2])
            max_z = wkVec2[2];


        // So we're down to the last update here. Time to actually
        // update the bounds based on the now-updated bounds of the
        // remaining children.
        for(int i = start_child; i < lastChild; i++)
        {
            if(childList[i] == null)
                continue;

            bds = childList[i].getBounds();

            if(bds instanceof BoundingVoid)
                continue;

            bds.getExtents(wkVec1, wkVec2);

            m1_x = wkVec1[0];
            m1_y = wkVec1[1];
            m1_z = wkVec1[2];

            m2_x = wkVec2[0];
            m2_y = wkVec2[1];
            m2_z = wkVec2[2];

            // Start with  ax, ay, az
            transform();

            if(min_x > wkVec2[0])
                min_x = wkVec2[0];

            if(min_y > wkVec2[1])
                min_y = wkVec2[1];

            if(min_z > wkVec2[2])
                min_z = wkVec2[2];

            if(max_x < wkVec2[0])
                max_x = wkVec2[0];

            if(max_y < wkVec2[1])
                max_y = wkVec2[1];

            if(max_z < wkVec2[2])
                max_z = wkVec2[2];


            // ax, ay, bz
            wkVec1[2] = m2_z;

            transform();

            if(min_x > wkVec2[0])
                min_x = wkVec2[0];

            if(min_y > wkVec2[1])
                min_y = wkVec2[1];

            if(min_z > wkVec2[2])
                min_z = wkVec2[2];

            if(max_x < wkVec2[0])
                max_x = wkVec2[0];

            if(max_y < wkVec2[1])
                max_y = wkVec2[1];

            if(max_z < wkVec2[2])
                max_z = wkVec2[2];

            // ax, by, bz
            wkVec1[1] = m2_y;

            transform();

            if(min_x > wkVec2[0])
                min_x = wkVec2[0];

            if(min_y > wkVec2[1])
                min_y = wkVec2[1];

            if(min_z > wkVec2[2])
                min_z = wkVec2[2];

            if(max_x < wkVec2[0])
                max_x = wkVec2[0];

            if(max_y < wkVec2[1])
                max_y = wkVec2[1];

            if(max_z < wkVec2[2])
                max_z = wkVec2[2];

            // ax, by, az
            wkVec1[2] = m1_z;

            transform();

            if(min_x > wkVec2[0])
                min_x = wkVec2[0];

            if(min_y > wkVec2[1])
                min_y = wkVec2[1];

            if(min_z > wkVec2[2])
                min_z = wkVec2[2];

            if(max_x < wkVec2[0])
                max_x = wkVec2[0];

            if(max_y < wkVec2[1])
                max_y = wkVec2[1];

            if(max_z < wkVec2[2])
                max_z = wkVec2[2];

            // bx, by, az
            wkVec1[0] = m2_x;

            transform();

            if(min_x > wkVec2[0])
                min_x = wkVec2[0];

            if(min_y > wkVec2[1])
                min_y = wkVec2[1];

            if(min_z > wkVec2[2])
                min_z = wkVec2[2];

            if(max_x < wkVec2[0])
                max_x = wkVec2[0];

            if(max_y < wkVec2[1])
                max_y = wkVec2[1];

            if(max_z < wkVec2[2])
                max_z = wkVec2[2];

            // bx, ay, az
            wkVec1[1] = m1_y;

            transform();

            if(min_x > wkVec2[0])
                min_x = wkVec2[0];

            if(min_y > wkVec2[1])
                min_y = wkVec2[1];

            if(min_z > wkVec2[2])
                min_z = wkVec2[2];

            if(max_x < wkVec2[0])
                max_x = wkVec2[0];

            if(max_y < wkVec2[1])
                max_y = wkVec2[1];

            if(max_z < wkVec2[2])
                max_z = wkVec2[2];

            // bx, ay, bz
            wkVec1[2] = m2_z;

            transform();

            if(min_x > wkVec2[0])
                min_x = wkVec2[0];

            if(min_y > wkVec2[1])
                min_y = wkVec2[1];

            if(min_z > wkVec2[2])
                min_z = wkVec2[2];

            if(max_x < wkVec2[0])
                max_x = wkVec2[0];

            if(max_y < wkVec2[1])
                max_y = wkVec2[1];

            if(max_z < wkVec2[2])
                max_z = wkVec2[2];

            // bx, by, bz
            wkVec1[1] = m2_y;

            transform();

            if(min_x > wkVec2[0])
                min_x = wkVec2[0];

            if(min_y > wkVec2[1])
                min_y = wkVec2[1];

            if(min_z > wkVec2[2])
                min_z = wkVec2[2];

            if(max_x < wkVec2[0])
                max_x = wkVec2[0];

            if(max_y < wkVec2[1])
                max_y = wkVec2[1];

            if(max_z < wkVec2[2])
                max_z = wkVec2[2];
        }

        if((bounds instanceof BoundingVoid) || (bounds == null))
            bounds = new BoundingBox();

        BoundingBox bbox = (BoundingBox)bounds;
        bbox.setMinimum(min_x, min_y, min_z);
        bbox.setMaximum(max_x, max_y, max_z);
    }

    //---------------------------------------------------------------
    // Methods defined by TransformPickTarget
    //---------------------------------------------------------------

    /**
     * Get the inverse version of the local transform. The default
     * implementation does nothing.
     *
     * @param mat The matrix to copy the transform data to
     */
    public void getInverseTransform(Matrix4d mat)
    {
        mat.set(inverseTransform);
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Set the transform matrix for this class.
     *
     * @param trans The matrix.  Copy by value semantics.
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setTransform(Matrix4d trans)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        localTransform.set(trans);

        matrixUtils.inverse(localTransform, inverseTransform);
    }

    /**
     * Get the current local transformation value.
     *
     * @param mat The matrix to copy the transform data to
     */
    public void getTransform(Matrix4d mat)
    {
        mat.set(localTransform);
    }

    /**
     * Transform the values in wkVec1 to the local coordinate system and
     * place it in wkVec2.
     */
    private void transform()
    {
        double x = wkVec1[0];
        double y = wkVec1[1];
        double z = wkVec1[2];

        wkVec2[0] = x * localTransform.m00 +
                    y * localTransform.m01 +
                    z * localTransform.m02 +
                        localTransform.m03;

        wkVec2[1] = x * localTransform.m10 +
                    y * localTransform.m11 +
                    z * localTransform.m12 +
                        localTransform.m13;

        wkVec2[2] = x * localTransform.m20 +
                    y * localTransform.m21 +
                    z * localTransform.m22 +
                        localTransform.m23;

    }
}
