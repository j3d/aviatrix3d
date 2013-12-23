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
 * An OpenGL PointArray.
 * <p>
 *
 * Points cannot be picked in the traditional manner using geometry-based
 * picking. The only way to pick them is based on their bounds.
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public class PointArray extends BufferGeometry
{
    /**
     * Constructs a PointArray with default values.
     */
    public PointArray()
    {
    }

    /**
     * Issue ogl commands needed for this renderable object.
     *
     * @param gl The gl context to draw with
     */
    public void render(GL gl)
    {
        // No coordinates, do nothing.
        if((vertexFormat & COORDINATE_MASK) == 0)
            return;

        setVertexState(gl);

        gl.glDrawArrays(GL.GL_POINTS, 0, numCoords);

        clearVertexState(gl);
    }

    //---------------------------------------------------------------
    // Methods defined by Comparable
    //---------------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param o The objec to be compared
     * @return -1, 0 or 1 depending on order
     * @throws ClassCastException The specified object's type prevents it from
     *    being compared to this Object
     */
    public int compareTo(Object o)
        throws ClassCastException
    {
        PointArray geom = (PointArray)o;
        return compareTo(geom);
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
        if(!(o instanceof PointArray))
            return false;
        else
            return equals((PointArray)o);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param ta The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(PointArray ta)
    {
        if(ta == null)
            return 1;

        if(ta == this)
            return 0;

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param ta The geometry instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(PointArray ta)
    {
        return (ta == this);
    }
}
