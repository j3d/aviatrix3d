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

package org.j3d.aviatrix3d.iutil;

// External imports
import java.util.HashMap;
import java.nio.Buffer;

// Local imports
// None

/**
 * Internal data holder class to hold argument values inside the
 * Geometry classes for shader attribute values.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class ShaderAttribValue
{
    /** The type of data for this value. See constants above */
    public int dataType;

    /** The number of items in a single value - 1, 2, 3 or 4 */
    public int size;

    /** Flag for whether the matrix values should be normalised */
    public boolean normalise;

    /** The buffer holding the data */
    public Buffer data;

    /**
     * Create a new instance of the value. Set the uniform location to -1
     * to indicate it has not been fetched yet.
     */
    public ShaderAttribValue()
    {
        normalise = false;
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
        if(!(o instanceof ShaderAttribValue))
            return false;
        else
            return equals((ShaderAttribValue)o);
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
    public boolean equals(ShaderAttribValue sav)
    {
        if(sav == this)
            return true;

        if(sav == null)
            return false;

        if((dataType != sav.dataType) ||
           (size != sav.size) ||
           (normalise != sav.normalise) ||
           (data != sav.data))
            return false;

        // We probably should go through and check each byte of the data array,
        // but that would be horribly expensive.

        return true;
    }
}
