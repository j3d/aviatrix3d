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
import java.nio.IntBuffer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.picking.NotPickableException;

/**
 * An OpenGL IndexedTriangleFanArray.
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
 *     strip with less than 3 vertices listed.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.33 $
 */
public class IndexedTriangleFanArray extends IndexedVertexGeometry
{
    /** Message when the fan count is < 3 */
    private static final String MIN_COUNT_PROP =
        "org.j3d.aviatrix3d.IndexedTriangleFanArray.minStripSizeMsg";

    /** The fan counts of valid triangles for each fan */
    private int[] fanCounts;

    /** The number of valid fans to read from the array */
    private int numFans;

    /** Buffer holding the current index list */
    private IntBuffer fanBuffer;

    /** The number of indices required for the fan geometry */
    private int numFanIndicesUsed;

    /**
     * Constructs a IndexedTriangleFanArray with default values.
     */
    public IndexedTriangleFanArray()
    {
        initPolygonDetails(3);
    }

    /**
     * Constructs an instance.
     *
     * @param useVbo Should we use vertex buffer objects
     * @param vboHint Hints for how to setup VBO.  Valid values are VBO_HINT_*
     */
    public IndexedTriangleFanArray(boolean useVbo, int vboHint)
    {
        super(useVbo, vboHint);

        initPolygonDetails(3);
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
        if((numFans == 0) || ((vertexFormat & COORDINATE_MASK) == 0))
            return;

        setVertexState(gl);

        Integer vbo_id = null;

        if(vboAvailable && useVbo)
        {
            vbo_id = (Integer)vboElementIdMap.get(gl);
            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vbo_id.intValue());

            int fan_offset = 0;
            for(int i = 0; i < numFans; i++)
            {
                gl.glDrawElements(GL.GL_TRIANGLE_FAN,
                                  fanCounts[i],
                                  GL.GL_UNSIGNED_INT,
                                  fan_offset * 4);

                fan_offset += fanCounts[i];
            }

            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
        else
        {
/*
            int fan_offset = 0;
            for(int i = 0; i < numFans; i++)
            {
                indexBuffer.position(fan_offset);
                gl.glDrawElements(GL.GL_TRIANGLE_FAN,
                                  fanCounts[i],
                                  GL.GL_UNSIGNED_INT,
                                  indexBuffer);

                fan_offset += fanCounts[i];
            }
*/
        }


/*
        gl.glMultiDrawElements(GL.GL_TRIANGLE_FAN,
                               fanBuffer,
                               GL.GL_UNSIGNED_INT,
                               indexBuffer,
                               numFans);
*/
        clearVertexState(gl);
    }

    //----------------------------------------------------------
    // Methods defined by Geometry
    //----------------------------------------------------------

    /**
     * Check to see if this geometry is making the geometry visible or
     * not. Returns true if the defined number of coordinates, indices
     * and fans are all non-zero.
     *
     * @return true when the geometry is visible
     */
    @Override
    protected boolean isVisible()
    {
        return super.isVisible() && numFans != 0;
    }

    /**
     * Check for all intersections against this geometry using a line segment and
     * return the exact distance away of the closest picking point.
     *
     * @param start The start point of the segment
     * @param end The end point of the segment
     * @param findAny True if it only has to find a single intersection and can
     *   exit as soon as it finds the first intersection. False if it must find
     *   the closest polygon
     * @param dataOut An array to put the data in for the intersection. Exact
     *   format is described by the flags
     * @param dataOutFlags A set of derived-class specific flags describing what
     *   data should be included in the output array
     * @return True if an intersection was found according to the input request
     * @throws NotPickableException This object has been marked as non pickable,
     *   but you decided to try to call the method anyway
     */
    @Override
    public boolean pickLineSegment(float[] start,
                                   float[] end,
                                   boolean findAny,
                                   float[] dataOut,
                                   int dataOutFlags)
        throws NotPickableException
    {
        // Call the super version to do our basic checking for us. Ignore the
        // return value as we're about to go calculate that ourselves.
        super.pickLineSegment(start, end, findAny, dataOut, dataOutFlags);

        float shortest_length = Float.POSITIVE_INFINITY;
        float this_length;
        boolean found = false;
        float out_x = 0;
        float out_y = 0;
        float out_z = 0;

        // copy the end var to a local for the time being as we're going to
        // reuse end as a direction vector.
        float end_x = end[0];
        float end_y = end[1];
        float end_z = end[2];

        float x = end[0] - start[0];
        float y = end[1] - start[1];
        float z = end[2] - start[2];

        float vec_len = (float)Math.sqrt(x * x + y * y + z * z);
        end[0] = x;
        end[1] = y;
        end[2] = z;

        int coord_offset = 0;
        int idx = 0;
        int coord_comps = (vertexFormat & COORDINATE_MASK);

        for(int i = 0; i < numFans; i++)
        {
            int num_tris = fanCounts[i] - 2;

            // Copy in the first coordinate of the fan. Always common
            // for each triangle
            switch(coord_comps)
            {
                case 2:
                    idx = indices[coord_offset] * 2;
                    wkPolygon[0] = coordinates[idx];
                    wkPolygon[1] = coordinates[idx + 1];
                    wkPolygon[2] = 0;
                    coord_offset += 2;
                    break;

                case 3:
                    idx = indices[coord_offset] * 3;
                    wkPolygon[0] = coordinates[idx];
                    wkPolygon[1] = coordinates[idx + 1];
                    wkPolygon[2] = coordinates[idx + 2];
                    coord_offset += 3;
                    break;

                case 4:
                    idx = indices[coord_offset] * 4;
                    wkPolygon[0] = coordinates[idx];
                    wkPolygon[1] = coordinates[idx + 1];
                    wkPolygon[2] = coordinates[idx + 2];
                    coord_offset += 4;
                    break;
            }

            for(int j = 0; j < num_tris; j++)
            {
                switch(coord_comps)
                {
                    case 2:
                        idx = indices[coord_offset] * 2;
                        wkPolygon[3] = coordinates[idx];
                        wkPolygon[4] = coordinates[idx + 1];
                        wkPolygon[5] = 0;

                        idx = indices[coord_offset + 1] * 2;
                        wkPolygon[6] = coordinates[idx];
                        wkPolygon[7] = coordinates[idx + 1];
                        wkPolygon[8] = 0;
                        coord_offset += 2;
                        break;

                    case 3:
                        idx = indices[coord_offset] * 3;
                        wkPolygon[3] = coordinates[idx];
                        wkPolygon[4] = coordinates[idx + 1];
                        wkPolygon[5] = coordinates[idx + 2];

                        idx = indices[coord_offset + 1] * 3;
                        wkPolygon[6] = coordinates[idx];
                        wkPolygon[7] = coordinates[idx + 1];
                        wkPolygon[8] = coordinates[idx + 2];
                        coord_offset += 3;
                        break;

                    case 4:
                        idx = indices[coord_offset] * 4;
                        wkPolygon[3] = coordinates[idx];
                        wkPolygon[4] = coordinates[idx + 1];
                        wkPolygon[5] = coordinates[idx + 2];

                        idx = indices[coord_offset + 1] * 4;
                        wkPolygon[6] = coordinates[idx];
                        wkPolygon[7] = coordinates[idx + 1];
                        wkPolygon[8] = coordinates[idx + 2];
                        coord_offset += 4;
                        break;
                }

                if(ray3DTriangleChecked(start, end, vec_len, dataOut))
                {
                    found = true;

                    if(findAny)
                        break;

                    float l_x = start[0] - dataOut[0];
                    float l_y = start[1] - dataOut[1];
                    float l_z = start[2] - dataOut[2];

                    this_length = l_x * l_x + l_y * l_y + l_z * l_z;

                    if(this_length < shortest_length)
                    {
                        shortest_length = this_length;
                        out_x = dataOut[0];
                        out_y = dataOut[1];
                        out_z = dataOut[2];
                    }
                }
            }

            // Shift to the start of the next fan
            coord_offset++;
        }

        dataOut[0] = out_x;
        dataOut[1] = out_y;
        dataOut[2] = out_z;

        // Copy it back again.
        end[0] = end_x;
        end[1] = end_y;
        end[2] = end_z;

        return found;
    }

    /**
     * Check for all intersections against this geometry using a line ray and
     * return the exact distance away of the closest picking point. Default
     * implementation always returns false indicating that nothing was found.
     * Derived classes should override and provide a real implementation.
     *
     * @param origin The start point of the ray
     * @param direction The direction vector of the ray
     * @param findAny True if it only has to find a single intersection and can
     *   exit as soon as it finds the first intersection. False if it must find
     *   the closest polygon
     * @param dataOut An array to put the data in for the intersection. Exact
     *   format is described by the flags
     * @param dataOutFlags A set of derived-class specific flags describing what
     *   data should be included in the output array
     * @return True if an intersection was found according to the input request
     * @throws NotPickableException This object has been marked as non pickable,
     *   but you decided to try to call the method anyway
     */
    @Override
    public boolean pickLineRay(float[] origin,
                               float[] direction,
                               boolean findAny,
                               float[] dataOut,
                               int dataOutFlags)
        throws NotPickableException
    {
        // Call the super version to do our basic checking for us. Ignore the
        // return value as we're about to go calculate that ourselves.
        super.pickLineRay(origin, direction, findAny, dataOut, dataOutFlags);

        float shortest_length = Float.POSITIVE_INFINITY;
        float this_length;
        boolean found = false;
        float out_x = 0;
        float out_y = 0;
        float out_z = 0;

        int coord_offset = 0;
        int idx = 0;
        int coord_comps = (vertexFormat & COORDINATE_MASK);

        for(int i = 0; i < numFans; i++)
        {
            int num_tris = fanCounts[i] - 2;

            // Copy in the first coordinate of the fan. Always common
            // for each triangle
            switch(coord_comps)
            {
                case 2:
                    idx = indices[coord_offset] * 2;
                    wkPolygon[0] = coordinates[idx];
                    wkPolygon[1] = coordinates[idx + 1];
                    wkPolygon[2] = 0;
                    coord_offset += 2;
                    break;

                case 3:
                    idx = indices[coord_offset] * 3;
                    wkPolygon[0] = coordinates[idx];
                    wkPolygon[1] = coordinates[idx + 1];
                    wkPolygon[2] = coordinates[idx + 2];
                    coord_offset += 3;
                    break;

                case 4:
                    idx = indices[coord_offset] * 4;
                    wkPolygon[0] = coordinates[idx];
                    wkPolygon[1] = coordinates[idx + 1];
                    wkPolygon[2] = coordinates[idx + 2];
                    coord_offset += 4;
                    break;
            }

            for(int j = 0; j < num_tris; j++)
            {
                switch(coord_comps)
                {
                    case 2:
                        idx = indices[coord_offset] * 2;
                        wkPolygon[3] = coordinates[idx];
                        wkPolygon[4] = coordinates[idx + 1];
                        wkPolygon[5] = 0;

                        idx = indices[coord_offset + 1] * 2;
                        wkPolygon[6] = coordinates[idx];
                        wkPolygon[7] = coordinates[idx + 1];
                        wkPolygon[8] = 0;
                        coord_offset += 2;
                        break;

                    case 3:
                        idx = indices[coord_offset] * 3;
                        wkPolygon[3] = coordinates[idx];
                        wkPolygon[4] = coordinates[idx + 1];
                        wkPolygon[5] = coordinates[idx + 2];

                        idx = indices[coord_offset + 1] * 3;
                        wkPolygon[6] = coordinates[idx];
                        wkPolygon[7] = coordinates[idx + 1];
                        wkPolygon[8] = coordinates[idx + 2];
                        coord_offset += 3;
                        break;

                    case 4:
                        idx = indices[coord_offset] * 4;
                        wkPolygon[3] = coordinates[idx];
                        wkPolygon[4] = coordinates[idx + 1];
                        wkPolygon[5] = coordinates[idx + 2];

                        idx = indices[coord_offset + 1] * 4;
                        wkPolygon[6] = coordinates[idx];
                        wkPolygon[7] = coordinates[idx + 1];
                        wkPolygon[8] = coordinates[idx + 2];
                        coord_offset += 4;
                        break;
                }

                if(ray3DTriangleChecked(origin, direction, 0, dataOut))
                {
                    found = true;

                    if(findAny)
                        break;

                    float l_x = origin[0] - dataOut[0];
                    float l_y = origin[1] - dataOut[1];
                    float l_z = origin[2] - dataOut[2];

                    this_length = l_x * l_x + l_y * l_y + l_z * l_z;

                    if(this_length < shortest_length)
                    {
                        shortest_length = this_length;
                        out_x = dataOut[0];
                        out_y = dataOut[1];
                        out_z = dataOut[2];
                    }
                }
            }

            // Shift to the start of the next fan
            coord_offset++;
        }

        dataOut[0] = out_x;
        dataOut[1] = out_y;
        dataOut[2] = out_z;

        return found;
    }

    /**
     * Internal method to recalculate the implicit bounds of this Node.
     */
    @Override
    protected void recomputeBounds()
    {
        numIndicesUsed = numFanIndicesUsed;
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
        IndexedTriangleFanArray geom = (IndexedTriangleFanArray)o;
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
        if(!(o instanceof IndexedTriangleFanArray))
            return false;
        else
            return equals((IndexedTriangleFanArray)o);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the number of valid fans to use. A check is performed to make
     * sure that the number of vertices high enough to support the total
     * of all the fan counts so make sure to call setVertex() with the
     * required array length before calling this method. Each fan must be
     * a minumum length of three.
     *
     * @param counts The array of counts
     * @param num The number of valid items to read from the array
     * @throws IllegalArgumentException Invalid total fan count or
     *   individual fan count < 3
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds changed callback method
     */
    public void setFanCount(int[] counts, int num)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        if(num == 0)
        {
            numRequiredCoords = 0;
            numFanIndicesUsed = 0;
            fanCounts = null;
            fanBuffer.clear();
            return;
        }

        numFanIndicesUsed = 0;
        for(int i = 0; i < num; i++)
        {
            if(counts[i] < 3)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(MIN_COUNT_PROP) + i;
                throw new IllegalArgumentException(msg);
            }

            numFanIndicesUsed += counts[i];
        }

        if(fanCounts == null || fanCounts.length < num)
            fanCounts = new int[num];

        numFans = num;

        if(num > 0)
            System.arraycopy(counts, 0, fanCounts, 0, num);

        if(fanBuffer == null || numIndices > fanBuffer.capacity())
            fanBuffer = createBuffer(numIndices);
        else
            fanBuffer.clear();

        fanBuffer.put(counts, 0, num);
        fanBuffer.rewind();
    }

    /**
     * Get the number of valid fans that are defined for this geometry.
     *
     * @return a positive number
     */
    public int getValidFanCount()
    {
        return numFans;
    }

    /**
     * Get the sizes of the valid fans. The passed array must be big enough
     * to contain all the fan counts.
     *
     * @param values An array to copy the strip values into
     */
    public void getFanCount(int[] values)
    {
        System.arraycopy(fanCounts, 0, values, 0, numFans);
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param ta The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(IndexedTriangleFanArray ta)
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
    public boolean equals(IndexedTriangleFanArray ta)
    {
        return (ta == this);
    }
}
