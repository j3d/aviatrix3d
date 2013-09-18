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
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.picking.NotPickableException;

/**
 * An OpenGL QuadArray representation.
 * <p>
 *
 * Quad Arrays may have an additional edge flag array supplied, in addition to
 * the normal geometry values.
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
 * <p><b>Internationalisation Resource Names</b></p>
 * <ul>
 * <li>invalidStencilFunctionMsg: Error message when the given function type
 *     does not match one of the available types.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.30 $
 */
public class QuadArray extends VertexGeometry
{
    /** Length of the edge array is too short for the coordinate data */
    private static final String EDGE_ARRAY_LENGTH_PROP =
        "org.j3d.aviatrix3d.QuadArray.edgeLengthMsg";

    /** Buffer for holding vertex data */
    private ByteBuffer edgeBuffer;

    /** Reference to the user array of colors used */
    private boolean[] edgeFlags;

    /** Offset to edge flags in VBO */
    private long edgeOffset = -1;

    /**
     * Constructs a QuadArray with default values.
     */
    public QuadArray()
    {
        edgeBuffer = ByteBuffer.allocate(0);

        initPolygonDetails(4);
    }

    /**
     * Constructs an instance.
     *
     * @param useVbo Should we use vertex buffer objects
     * @param vboHint Hints for how to setup VBO.  Valid values are VBO_HINT_*.
     */
    public QuadArray(boolean useVbo, int vboHint)
    {
        super(useVbo, vboHint);

        edgeBuffer = ByteBuffer.allocate(0);

        initPolygonDetails(4);
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

        if((vertexFormat & EDGES) != 0)
        {
            gl.glEnableClientState(GL2.GL_EDGE_FLAG_ARRAY);
            if (vboAvailable && useVbo)
                gl.glEdgeFlagPointer(0, edgeOffset);
            else
                gl.glEdgeFlagPointer(0, edgeBuffer);
        }

        gl.glDrawArrays(GL2.GL_QUADS, 0, numCoords);

        if((vertexFormat & EDGES) != 0)
        {
            gl.glDisableClientState(GL2.GL_EDGE_FLAG_ARRAY);
            gl.glEdgeFlag(true);
        }

        clearVertexState(gl);
    }

    //----------------------------------------------------------
    // Methods defined by VertexGeometry
    //----------------------------------------------------------

    /**
     * Set the number of vertices to the new number.
     * <p>
     *
     * In a live scene graph, can only be called during the bounds changed
     * callback.
     *
     * @param count The new number, must be >= 0
     * @throws IllegalArgumentException The number is negative
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    @Override
    public void setValidVertexCount(int count)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        super.setValidVertexCount(count);
        numRequiredCoords = count;
    }

    /**
     * Set the vertex array reference to the new array. The number of valid
     * items is taken from the second parameter. This replaces the existing
     * vertex list array reference with the new reference.
     * <p>
     *
     * In a live scene graph, can only be called during the bounds changed
     * callback.
     *
     * @param type The number of dimensions to the coordinates - 2D, 3D or 4D
     * @param vertices The new array reference to use for vertex information
     * @param numValid The number of valid values to use in the array
     * @throws IllegalArgumentException The number is negative
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    @Override
    public void setVertices(int type, float[] vertices, int numValid)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        super.setVertices(type, vertices, numValid);
        numRequiredCoords = numValid;
    }

    /**
     * Compute the total size of vertex buffer data, used for allocating VBOs.
     * It is called by <code>setVertexStateVBO</code>, and should not be called
     * other places. <p>
     * Must be overridden in subclasses that has vertex data in addition to what
     * is in <code>VertexGeometry</code>. See <code>TriangleArray</code> for
     * examples.
     */
    @Override
    protected int computeBufferSize()
    {
        int buf_size = super.computeBufferSize();
        if((vertexFormat & EDGES) != 0)
        {
            buf_size += numCoords;
        }
        return buf_size;
    }

    /**
     * Fill VBOs with vertex buffer data. The
     * VBO must be bound and allocated with glBufferData before the method is called.
     * This method is called by <code>setVertexStateVBO</code>, and should not be called
     * other places. <p>
     * Must be overridden in subclasses that has vertex data in addition to what
     * is in <code>VertexGeometry</code>. See <code>TriangleArray</code> for
     * examples.
     */
    @Override
    protected int fillBufferData(GL2 gl)
     {
        int offset = super.fillBufferData(gl);
        if((vertexFormat & EDGES) != 0)
        {
            edgeOffset = offset;
            gl.glBufferSubData(GL.GL_ARRAY_BUFFER, offset, numCoords, edgeBuffer);
            offset += numCoords;
        }
        return offset;
     }

    //----------------------------------------------------------
    // Methods defined by Geometry
    //----------------------------------------------------------

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

        int num_quads = numCoords / 4;
        int coord_comps = (vertexFormat & COORDINATE_MASK);

        for(int i = 0; i < num_quads; i++)
        {
            switch(coord_comps)
            {
                case 2:
                    wkPolygon[0] = coordinates[i * 3 * 2];
                    wkPolygon[1] = coordinates[i * 3 * 2 + 1];
                    wkPolygon[2] = 0;

                    wkPolygon[3] = coordinates[i * 3 * 2 + 2];
                    wkPolygon[4] = coordinates[i * 3 * 2 + 3];
                    wkPolygon[5] = 0;

                    wkPolygon[6] = coordinates[i * 3 * 2 + 4];
                    wkPolygon[7] = coordinates[i * 3 * 2 + 5];
                    wkPolygon[8] = 0;

                    wkPolygon[9] = coordinates[i * 3 * 2 + 6];
                    wkPolygon[10] = coordinates[i * 3 * 2 + 7];
                    wkPolygon[11] = 0;
                    break;

                case 3:
                    wkPolygon[0] = coordinates[i * 3 * 3];
                    wkPolygon[1] = coordinates[i * 3 * 3 + 1];
                    wkPolygon[2] = coordinates[i * 3 * 3 + 2];

                    wkPolygon[3] = coordinates[i * 3 * 3 + 3];
                    wkPolygon[4] = coordinates[i * 3 * 3 + 4];
                    wkPolygon[5] = coordinates[i * 3 * 3 + 5];

                    wkPolygon[6] = coordinates[i * 3 * 3 + 6];
                    wkPolygon[7] = coordinates[i * 3 * 3 + 7];
                    wkPolygon[8] = coordinates[i * 3 * 3 + 8];

                    wkPolygon[9] = coordinates[i * 3 * 3 + 10];
                    wkPolygon[10] = coordinates[i * 3 * 3 + 11];
                    wkPolygon[11] = coordinates[i * 3 * 3 + 12];
                    break;

                case 4:
                    wkPolygon[0] = coordinates[i * 3 * 4];
                    wkPolygon[1] = coordinates[i * 3 * 4 + 1];
                    wkPolygon[2] = coordinates[i * 3 * 4 + 2];

                    wkPolygon[3] = coordinates[i * 3 * 4 + 4];
                    wkPolygon[4] = coordinates[i * 3 * 4 + 5];
                    wkPolygon[5] = coordinates[i * 3 * 4 + 6];

                    wkPolygon[6] = coordinates[i * 3 * 4 + 8];
                    wkPolygon[7] = coordinates[i * 3 * 4 + 9];
                    wkPolygon[8] = coordinates[i * 3 * 4 + 10];

                    wkPolygon[9] = coordinates[i * 3 * 4 + 12];
                    wkPolygon[10] = coordinates[i * 3 * 4 + 13];
                    wkPolygon[11] = coordinates[i * 3 * 4 + 14];
                    break;
            }

            if(ray3DQuadChecked(start, end, vec_len, dataOut))
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

        int num_quads = numCoords / 4;
        int coord_comps = (vertexFormat & COORDINATE_MASK);

        for(int i = 0; i < num_quads; i++)
        {
            switch(coord_comps)
            {
                case 2:
                    wkPolygon[0] = coordinates[i * 3 * 2];
                    wkPolygon[1] = coordinates[i * 3 * 2 + 1];
                    wkPolygon[2] = 0;

                    wkPolygon[3] = coordinates[i * 3 * 2 + 2];
                    wkPolygon[4] = coordinates[i * 3 * 2 + 3];
                    wkPolygon[5] = 0;

                    wkPolygon[6] = coordinates[i * 3 * 2 + 4];
                    wkPolygon[7] = coordinates[i * 3 * 2 + 5];
                    wkPolygon[8] = 0;

                    wkPolygon[9] = coordinates[i * 3 * 2 + 6];
                    wkPolygon[10] = coordinates[i * 3 * 2 + 7];
                    wkPolygon[11] = 0;
                    break;

                case 3:
                    wkPolygon[0] = coordinates[i * 3 * 3];
                    wkPolygon[1] = coordinates[i * 3 * 3 + 1];
                    wkPolygon[2] = coordinates[i * 3 * 3 + 2];

                    wkPolygon[3] = coordinates[i * 3 * 3 + 3];
                    wkPolygon[4] = coordinates[i * 3 * 3 + 4];
                    wkPolygon[5] = coordinates[i * 3 * 3 + 5];

                    wkPolygon[6] = coordinates[i * 3 * 3 + 6];
                    wkPolygon[7] = coordinates[i * 3 * 3 + 7];
                    wkPolygon[8] = coordinates[i * 3 * 3 + 8];

                    wkPolygon[9] = coordinates[i * 3 * 3 + 10];
                    wkPolygon[10] = coordinates[i * 3 * 3 + 11];
                    wkPolygon[11] = coordinates[i * 3 * 3 + 12];
                    break;

                case 4:
                    wkPolygon[0] = coordinates[i * 3 * 4];
                    wkPolygon[1] = coordinates[i * 3 * 4 + 1];
                    wkPolygon[2] = coordinates[i * 3 * 4 + 2];

                    wkPolygon[3] = coordinates[i * 3 * 4 + 4];
                    wkPolygon[4] = coordinates[i * 3 * 4 + 5];
                    wkPolygon[5] = coordinates[i * 3 * 4 + 6];

                    wkPolygon[6] = coordinates[i * 3 * 4 + 8];
                    wkPolygon[7] = coordinates[i * 3 * 4 + 9];
                    wkPolygon[8] = coordinates[i * 3 * 4 + 10];

                    wkPolygon[9] = coordinates[i * 3 * 4 + 12];
                    wkPolygon[10] = coordinates[i * 3 * 4 + 13];
                    wkPolygon[11] = coordinates[i * 3 * 4 + 14];
                    break;
            }

            if(ray3DQuadChecked(origin, direction, 0, dataOut))
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

        dataOut[0] = out_x;
        dataOut[1] = out_y;
        dataOut[2] = out_z;

        return found;
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
        QuadArray geom = (QuadArray)o;
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
        if(!(o instanceof QuadArray))
            return false;
        else
            return equals((QuadArray)o);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the edge flag reference to the new array. The number of valid
     * items is taken to be the length of the array (there's only one flag per
     * edge). This replaces the existing edge flag array reference with the
     * new reference. If set to null, will clear the use of edge flags.
     * <p>
     *
     * In a live scene graph, can only be called during the data changed
     * callback.
     *
     * @param flags The new array reference to use for edge flag information
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setEdgeFlags(boolean[] flags)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if((flags != null) && (flags.length < numCoords))
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(EDGE_ARRAY_LENGTH_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args =
            {
                new Integer(flags.length),
                new Integer(numCoords)
            };
            Format[] fmts = { n_fmt, n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        edgeFlags = flags;

        if(numCoords > edgeBuffer.capacity())
            edgeBuffer = createBuffer(numCoords);
        else
            edgeBuffer.clear();

        if(flags == null)
            vertexFormat &= EDGE_CLEAR;
        else
        {
            // convert booleans to GL values.
            for(int i = 0; i < numCoords; i++)
                edgeBuffer.put((byte)(flags[i] ? GL.GL_TRUE : GL.GL_FALSE));

            vertexFormat |= EDGES;
            edgeBuffer.rewind();
        }
        dataChanged.setAll(true);
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param ta The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(QuadArray ta)
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
    public boolean equals(QuadArray ta)
    {
        return (ta == this);
    }

    /**
     * Convenience method to allocate a NIO buffer for the vertex handling that
     * handles bytes. Used for the edge flag setting.
     *
     * @param size The number of bytes (flags) to have in the array
     */
    private ByteBuffer createBuffer(int size)
    {
        ByteBuffer buf = ByteBuffer.allocateDirect(size);
        buf.order(ByteOrder.nativeOrder());

        return buf;
    }
}
