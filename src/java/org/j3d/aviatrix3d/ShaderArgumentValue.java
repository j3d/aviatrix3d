/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004-2005
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
import java.util.HashMap;

import javax.media.opengl.GL;

// Local imports
// None

/**
 * Internal data holder class to hold argument values inside the
 * ShaderArgument class.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
class ShaderArgumentValue
{
    /** This value is an array of ints */
    static final int INT_ARRAY = 1;

    /** This value is an array of floats */
    static final int FLOAT_ARRAY = 2;

    /** This value is a matrix */
    static final int MATRIX = 3;

    /** This value is a single int for a sampler Id */
    static final int SAMPLER = 4;

    /** The type of data for this value. See constants above */
    int dataType;

    /** The data when an array of ints are provided */
    int[] intData;

    /** The data when an array of floats are provided */
    float[] floatData;

    /** The number of items in a single value - 1, 2, 3 or 4 */
    int size;

    /** The number of values to use out of the array */
    int count;

    /** Flag for whether the matrix values should be transposed */
    boolean transposeMatrix;

    /** Index of the uniform location value once fetched from GL */
    int uniformLocation;


    /**
     * Create a new instance of the value. Set the uniform location to -1
     * to indicate it has not been fetched yet.
     */
    ShaderArgumentValue()
    {
        uniformLocation = -1;
    }

    //---------------------------------------------------------------
    // Methods defined by Object
    //---------------------------------------------------------------

    /**
     * Compare this object for equality to the given object.
     *
     * @param o The object to be compared
     * @return True if these represent the same values
     */
    public boolean equals(Object o)
    {
        if(!(o instanceof ShaderArgumentValue))
            return false;
        else
            return equals((ShaderArgumentValue)o);
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param sav The shader value instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(ShaderArgumentValue sav)
    {
        if(sav == this)
            return true;

        if(sav == null)
            return false;

        if((dataType != sav.dataType) ||
           (size != sav.size) ||
           (count != sav.count) ||
           (transposeMatrix != sav.transposeMatrix) ||
           (uniformLocation != sav.uniformLocation))
            return false;

        int num = count * size;

        switch(dataType)
        {
            case INT_ARRAY:
                for(int i = 0; i < num; i++)
                {
                    if(intData[i] != sav.intData[i])
                        return false;
                }
                break;

            case FLOAT_ARRAY:
                for(int i = 0; i < num; i++)
                {
                    if(floatData[i] != sav.floatData[i])
                        return false;
                }
                break;

            case MATRIX:
                num *= size;
                for(int i = 0; i < num; i++)
                {
                    if(floatData[i] != sav.floatData[i])
                        return false;
                }
                break;

            case SAMPLER:
                if(intData[0] != sav.intData[0])
                    return false;

                break;
        }

        return true;
    }
}
