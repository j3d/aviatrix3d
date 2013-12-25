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
import java.nio.FloatBuffer;
import java.util.ArrayList;

// Local imports
import org.j3d.aviatrix3d.*;

/**
 * Common AV3D implementation of the Humanoid object that uses software
 * to implement the mesh skinning algorithm.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
class SoftwareSpeedHumanoid extends SoftwareHumanoid
{
    /**
     * Create a new, default instance of the site.
     */
    SoftwareSpeedHumanoid()
    {
    }

    //----------------------------------------------------------
    // Methods defined by HAnimHumanoid
    //----------------------------------------------------------

    /**
     * Set a new value for the skinCoord of this joint. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * a multiple of 3 units long.
     *
     * @param val The new skinCoord value to use
     * @param numElements The number of 3d-vectors in the array
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    @Override
    public void setSkinCoord(float[] val, int numElements)
    {
        super.setSkinCoord(val, numElements);

        if(coordsArray == null || coordsArray.length < numElements * 3)
            coordsArray = new float[numElements * 3];

        outputCoords = coordsArray;
    }

    /**
     * Set a new value for the skinNormal of this joint. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * a multiple of 3 units long.
     *
     * @param val The new skinNormal value to use
     * @param numElements The number of 3d-vectors in the array
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    @Override
    public void setSkinNormal(float[] val, int numElements)
    {
        super.setSkinNormal(val, numElements);

        if(normalsArray == null || normalsArray.length < numElements * 3)
            normalsArray = new float[numElements * 3];

        outputNormals = normalsArray;
    }

    /**
     * All the skeletal changes are in for this frame, so update the matrix
     * values now. If nothing has changed, don't bother doing any calculations
     * and return immediately.
     */
    @Override
    public void updateSkeleton()
    {
        // If no coordinates have been set, ignore this mesh
        if(outputCoords == null)
            return;

        // keep the flag because the superclass is going to overwrite it.
        // Need to update the skeleton first.
        boolean geom_changed = skeletonChanged;

        // zero out the arrays first so that the weighted calcs work correctly.
        if(geom_changed)
        {
            if(numSkinCoords != 0)
            {
                int size = numSkinCoords / 3;
                for(int i = 0; i < size; i++)
                {
                    if(dirtyCoordinates[i])
                    {
                        coordsArray[i * 3] = 0;
                        coordsArray[i * 3 + 1] = 0;
                        coordsArray[i * 3 + 2] = 0;
                    }
                }
            }

            if(numSkinNormals != 0)
            {
                int size = numSkinNormals / 3;
                for(int i = 0; i < size; i++)
                {
                    if(dirtyCoordinates[i])
                    {
                        normalsArray[i * 3] = 0;
                        normalsArray[i * 3 + 1] = 0;
                        normalsArray[i * 3 + 2] = 0;
                    }
                }
            }
        }

        super.updateSkeleton();

        if(geom_changed)
        {
            if((numSkinCoords != 0) && (bufferGeometry.size() != 0))
            {
                coordsBuffer.rewind();
                coordsBuffer.put(coordsArray, 0, numSkinCoords);
            }

            if((numSkinNormals != 0) && (bufferGeometry.size() != 0))
            {
                normalsBuffer.rewind();
                normalsBuffer.put(normalsArray, 0, numSkinNormals);
            }
        }
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Get the internal representation of the updated mesh skin coordinates.
     *
     * @return An object that is either a float[] or FloatBuffer, depending on
     *    the internal implementation used.
     */
    @Override
    public Object getUpdatedSkinCoords()
    {
        return coordsArray;
    }

    /**
     * Get the internal representation of the updated mesh skin normals.
     *
     * @return An object that is either a float[] or FloatBuffer, depending on
     *    the internal implementation used.
     */
    @Override
    public Object getUpdatedSkinNormals()
    {
        return normalsArray;
    }
}
