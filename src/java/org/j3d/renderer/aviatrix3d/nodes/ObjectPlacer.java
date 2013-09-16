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

package org.j3d.renderer.aviatrix3d.nodes;

// External imports
import javax.media.opengl.GL;

// Local imports
// None

/**
 * Internal data representation class used during the per-object depth sorted
 * versions of the geometry.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
class ObjectPlacer implements Comparable {

    /**
     * The distance the object lies from some place - typically the
     * viewpoint. For fast calculations, this is usually really the
     * distance squared.  Exact meaning of what this represents is not
     * particularly important, only that it provides some metric for
     * relative comparison of two objects.
     */
    float distance;

    /**
     * The index of what this object represents back in the underlying data
     * array of the geomety.
     */
    int index;

    //---------------------------------------------------------------
    // Methods defined by Comparable
    //---------------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param o The object to be compared
     * @return -1, 0 or 1 depending on order
     * @throws ClassCastException The specified object's type prevents it from
     *    being compared to this Object
     */
    public int compareTo(Object o)
        throws ClassCastException
    {
        ObjectPlacer place = (ObjectPlacer)o;

         if(place.distance > distance)
            return 1;
        else if(place.distance < distance)
            return -1;

        return 0;
    }
}
