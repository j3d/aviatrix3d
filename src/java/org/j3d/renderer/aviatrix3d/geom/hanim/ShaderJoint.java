/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004-2005-
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.renderer.aviatrix3d.geom.hanim;

// External imports
import java.util.ArrayList;
import java.nio.FloatBuffer;

import javax.vecmath.Matrix4f;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.geom.hanim.HAnimJoint;
import org.j3d.geom.hanim.HAnimObject;
import org.j3d.geom.hanim.HAnimObjectParent;

/**
 * Joint node implementation that specifically deals with shader-rendered
 * joints.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
class ShaderJoint extends AVJoint implements ShaderObjectParent
{
    /**
     * Create a new, default instance of the site.
     */
    ShaderJoint()
    {
    }

    //----------------------------------------------------------
    // Methods defined by HAnimJoint
    //----------------------------------------------------------

    /**
     * Set a new value for the skinCoordIndex of this joint. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * as long as the numValid field value.
     *
     * @param val The new skinCoordIndex value to use
     * @param numValid The number of valid values to read from the index list
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    public void setSkinCoordIndex(int[] val, int numValid)
    {
        super.setSkinCoordIndex(val, numValid);

        if(parent != null)
            ((ShaderObjectParent)parent).childAttributesChanged(this);
    }

    /**
     * Set a new value for the skinCoordWeight of this joint. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * as long as the currently set skinCoordIndex values length.
     *
     * @param val The new skinCoordWeight value to use
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    public void setSkinCoordWeight(float[] val)
    {
        super.setSkinCoordWeight(val);

        if(parent != null)
            ((ShaderObjectParent)parent).childAttributesChanged(this);
    }

    /**
     * Set the parent of this node to the given reference. Any previous
     * reference is removed.
     *
     * @param parent The new parent instance to use
     * @param srcCoords The array for the original, unmodified coordinates
     * @param numCoords Number of valid coordinate values
     * @param srcNormals The array for the original, unmodified normals
     * @param numNormals Number of valid normal values
     * @param destCoords The array/buffer for the transformed coordinates
     * @param destNormals The array/buffer for the transformed normals
     */
    protected void setParent(HAnimObjectParent parent,
                             float[] srcCoords,
                             int numCoords,
                             float[] srcNormals,
                             int numNormals,
                             Object destCoords,
                             Object destNormals)
    {
        super.setParent(parent,
                        srcCoords,
                        numCoords,
                        srcNormals,
                        numNormals,
                        destCoords,
                        destNormals);

        if(parent != null)
            ((ShaderObjectParent)parent).childAttributesChanged(this);
    }

    //----------------------------------------------------------
    // Methods defined by ShaderObjectParent
    //----------------------------------------------------------

    /**
     * Notification that the child has changed something in the attributes and
     * will need to reset the new values. A change could be in the weights,
     * indexed fields or new children added.
     *
     * @param child Reference to the child that has changed
     */
    public void childAttributesChanged(HAnimObject child)
    {
        if(parent != null)
            ((ShaderObjectParent)parent).childAttributesChanged(child);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Update the attributes needed by the shader. An option is provided to
     * control whether the update should only be for this node or all children
     * too.
     *
     * @param weights List of weights to update
     * @param indicies The index list of bones effecting a given vertex
     * @param boneCount Counter of the number of bones effecting this
     *    vertex
     * @param recurse True if this should recurse into all children
     */
    void updateAttributes(float[] weights,
                          float[] indices,
                          float[] boneCount,
                          boolean recurse)
    {
        for(int i = 0; i < numSkinCoord; i++)
        {
            // Limit ourselves to a max of 4 bones influencing a vertex. Once
            // we hit the limit ignore any more.
            int bone_offset = (int)boneCount[skinCoordIndex[i]];
            if(bone_offset > 3)
                continue;

            boneCount[skinCoordIndex[i]] = 1;
            weights[skinCoordIndex[i] * 4 + bone_offset] = skinCoordWeight[i];
            indices[skinCoordIndex[i] * 4 + bone_offset] = objectIndex;
        }

        if(recurse)
        {
            for(int i = 0; i < numChildren; i++)
            {
                if(children[i] instanceof ShaderJoint)
                    ((ShaderJoint)children[i]).updateAttributes(weights,
                                                                indices,
                                                                boneCount,
                                                                recurse);
            }
        }
    }

    /**
     * Update the matrix for this object in the array. The matrix array is
     * in column-major order. This method will also recurse into the contained
     * joints to animate those too.
     *
     * @param matrices Array of matrices to write to
     */
    void updateMatrices(float[] matrices)
    {
        matrices[objectIndex * 16]      = globalMatrix.m00;
        matrices[objectIndex * 16 + 1]  = globalMatrix.m10;
        matrices[objectIndex * 16 + 2]  = globalMatrix.m20;
        matrices[objectIndex * 16 + 3]  = globalMatrix.m30;

        matrices[objectIndex * 16 + 4]  = globalMatrix.m01;
        matrices[objectIndex * 16 + 5]  = globalMatrix.m11;
        matrices[objectIndex * 16 + 6]  = globalMatrix.m21;
        matrices[objectIndex * 16 + 7]  = globalMatrix.m31;

        matrices[objectIndex * 16 + 8]  = globalMatrix.m02;
        matrices[objectIndex * 16 + 9]  = globalMatrix.m12;
        matrices[objectIndex * 16 + 10] = globalMatrix.m22;
        matrices[objectIndex * 16 + 11] = globalMatrix.m32;

        matrices[objectIndex * 16 + 12] = globalMatrix.m03;
        matrices[objectIndex * 16 + 13] = globalMatrix.m13;
        matrices[objectIndex * 16 + 14] = globalMatrix.m23;
        matrices[objectIndex * 16 + 15] = globalMatrix.m33;

        for(int i = 0; i < numChildren; i++)
        {
            if(children[i] instanceof ShaderJoint)
                ((ShaderJoint)children[i]).updateMatrices(matrices);
        }
    }
}
