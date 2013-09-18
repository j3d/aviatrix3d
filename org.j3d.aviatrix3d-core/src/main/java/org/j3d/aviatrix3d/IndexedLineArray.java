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

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

// Local imports
// None

/**
 * An OpenGL LineArray.
 * <p>
 *
 * Lines cannot be picked in the traditional manner using geometry-based
 * picking. The only way to pick them is based on their bounds.
 *
 * <h3>Setting geometry</h3>
 *
 * <p>Part of the optimisation we make is to only copy into the underlying
 * structures the exact number of coordinates, normals etc that are needed.
 * To know this number, we need to know how many coordinates exist before
 * attempting to set anything else. When constructing, or updating, geometry,
 * you should always make sure that you first set the vertex list, then the
 * sizing information for the strip or fan counts, and then set normals as
 * needed. </p>
 *
 * @author Justin Couch
 * @version $Revision: 1.19 $
 */
public class IndexedLineArray extends IndexedVertexGeometry
{
    /**
     * Constructs a LineArray with default values.
     */
    public IndexedLineArray()
    {
    }

    /**
     * Constructs an instance.
     *
     * @param useVbo Should we use vertex buffer objects
     * @param vboHint Hints for how to setup VBO.  Valid values are VBO_HINT_*
     */
    public IndexedLineArray(boolean useVbo, int vboHint)
    {
        super(useVbo, vboHint);
    }

    //----------------------------------------------------------
    // Methods defined by GeometryRenderable
    //----------------------------------------------------------

    /**
     * Issue ogl commands needed for this renderable object.
     *
     * @param gl The gl context to draw with
     */
    @Override
    public void render(GL2 gl)
    {
        // No coordinates, do nothing.
        if((vertexFormat & COORDINATE_MASK) == 0)
            return;

        setVertexState(gl);

        if (vboAvailable && useVbo)
        {
            Integer vbo_id = (Integer)vboElementIdMap.get(gl);

            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vbo_id.intValue());

            gl.glDrawElements(GL.GL_LINES,
                              numIndices,
                              GL.GL_UNSIGNED_INT,
                              0);

            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
        else
        {
            gl.glDrawElements(GL.GL_LINES,
                              numIndices,
                              GL.GL_UNSIGNED_INT,
                              indexBuffer);
        }

        clearVertexState(gl);
    }

    //----------------------------------------------------------
    // Methods defined by Geometry
    //----------------------------------------------------------

    /**
     * Internal method to recalculate the implicit bounds of this Node.
     */
    @Override
    protected void recomputeBounds()
    {
        numIndicesUsed = numIndices;
        super.recomputeBounds();
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
    @Override
    public int compareTo(Object o)
        throws ClassCastException
    {
        IndexedLineArray geom = (IndexedLineArray)o;
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
    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof IndexedLineArray))
            return false;
        else
            return equals((IndexedLineArray)o);
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param ta The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(IndexedLineArray ta)
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
    public boolean equals(IndexedLineArray ta)
    {
        return (ta == this);
    }

}
