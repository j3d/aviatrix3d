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

import org.j3d.util.I18nManager;

// Local imports
// None

/**
 * An OpenGL LineStripArray.
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
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>minStripSizeMsg: Error message when the strip list has at least one
 *     strip with less than 2 vertices listed.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.27 $
 */
public class IndexedLineStripArray extends IndexedVertexGeometry
{
    /** Message when the strip count is < 2 */
    private static final String MIN_COUNT_PROP =
        "org.j3d.aviatrix3d.IndexedLineStripArray.minStripSizeMsg";

    /** The strip counts of valid triangles for each strip */
    private int[] stripCounts;

    /** The number of valid strips to read from the array */
    private int numStrips;

    /** The number of indices required for the strip geometry */
    protected int numStripIndicesUsed;

    /**
     * Constructs a LineStripArray with default values.
     */
    public IndexedLineStripArray()
    {
    }

    /**
     * Constructs an instance.
     *
     * @param useVbo Should we use vertex buffer objects
     * @param vboHint Hints for how to setup VBO.  Valid values are VBO_HINT_*
     */
    public IndexedLineStripArray(boolean useVbo, int vboHint)
    {
        super(useVbo, vboHint);
    }

    //----------------------------------------------------------
    // Methods defined by Geometry
    //----------------------------------------------------------

    /**
     * Check to see if this geometry is making the geometry visible or
     * not. Returns true if the defined number of coordinates, indices
     * and strips are all non-zero.
     *
     * @return true when the geometry is visible
     */
    @Override
    protected boolean isVisible()
    {
        return super.isVisible() && numStrips != 0;
    }

    /**
     * Internal method to recalculate the implicit bounds of this Node.
     */
    @Override
    protected void recomputeBounds()
    {
        numIndicesUsed = numStripIndicesUsed;
        super.recomputeBounds();
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
        if((numStrips == 0) || ((vertexFormat & COORDINATE_MASK) == 0))
            return;

        setVertexState(gl);

        Integer vbo_id = null;

        if (vboAvailable && useVbo)
        {
            vbo_id = (Integer)vboElementIdMap.get(gl);
            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vbo_id.intValue());

            int strip_offset = 0;
            for(int i = 0; i < numStrips; i++)
            {

                gl.glDrawElements(GL.GL_LINE_STRIP,
                                  stripCounts[i],
                                  GL.GL_UNSIGNED_INT,
                                  strip_offset * 4);

                strip_offset += stripCounts[i];
            }

            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
        else
        {
            int strip_offset = 0;
            for(int i = 0; i < numStrips; i++)
            {

                indexBuffer.position(strip_offset);
                gl.glDrawElements(GL.GL_LINE_STRIP,
                                  stripCounts[i],
                                  GL.GL_UNSIGNED_INT,
                                  indexBuffer);

                strip_offset += stripCounts[i];
            }
        }

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
    @Override
    public int compareTo(Object o)
        throws ClassCastException
    {
        IndexedLineStripArray geom = (IndexedLineStripArray)o;
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
        if(!(o instanceof IndexedLineStripArray))
            return false;
        else
            return equals((IndexedLineStripArray)o);
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Set the number of valid strips to use. A check is performed to make
     * sure that the number of vertices high enough to support the total
     * of all the strip counts so make sure to call setVertex() with the
     * required array length before calling this method. Each strip must be
     * a minumum length of three.
     *
     * @param counts The array of counts
     * @param num The number of valid items to read from the array
     * @throws IllegalArgumentException Invalid total strip count or
     *   individual strip count < 2
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds changed callback method
     */
    public void setStripCount(int[] counts, int num)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        numStripIndicesUsed = 0;
        for(int i = 0; i < num; i++)
        {
            if(counts[i] < 2)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(MIN_COUNT_PROP) + i;
                throw new IllegalArgumentException(msg);
            }

            numStripIndicesUsed += counts[i];
        }

        if(stripCounts == null || stripCounts.length < num)
            stripCounts = new int[num];

        numStrips = num;

        if(num > 0)
            System.arraycopy(counts, 0, stripCounts, 0, num);
    }

    /**
     * Get the number of valid strips that are defined for this geometry.
     *
     * @return a positive number
     */
    public int getValidStripCount()
    {
        return numStrips;
    }

    /**
     * Get the sizes of the valid strips. The passed array must be big enough
     * to contain all the strips.
     *
     * @param values An array to copy the strip values into
     */
    public void getStripCount(int[] values)
    {
        System.arraycopy(stripCounts, 0, values, 0, numStrips);
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param ta The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(IndexedLineStripArray ta)
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
    public boolean equals(IndexedLineStripArray ta)
    {
        return (ta == this);
    }
}
