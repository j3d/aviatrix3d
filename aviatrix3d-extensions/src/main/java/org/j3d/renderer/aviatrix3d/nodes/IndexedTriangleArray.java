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
import java.nio.ByteBuffer;
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.InvalidWriteTimingException;
import org.j3d.aviatrix3d.picking.NotPickableException;

/**
 * An OpenGL Indexed TriangleArray.
 * <p>
 *
 * To do the drawing, this class uses the glDrawElements() function.
 *
 * <p><b>Internationalisation Resource Names</b></p>
 * <ul>
 * <li>invalidStencilFunctionMsg: Error message when the given function type
 *     does not match one of the available types.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.11 $
 */
public class IndexedTriangleArray extends IndexedBufferGeometry
{
    /** Length of the edge array is too short for the coordinate data */
    private static final String EDGE_ARRAY_LENGTH_PROP =
        "org.j3d.renderer.aviatrix3d.nodes.IndexedTriangleArray.edgeLengthMsg";

    /** Buffer for holding vertex data */
    private ByteBuffer edgeBuffer;

    /**
     * Constructs a TriangleArray with default values.
     */
    public IndexedTriangleArray()
    {
        edgeBuffer = ByteBuffer.allocate(0);
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
        if(((vertexFormat & COORDINATE_MASK) == 0) ||
           (numCoords == 0) || (numIndices == 0))
            return;

        setVertexState(gl);

        if((vertexFormat & EDGES) != 0)
        {
            gl.glEnableClientState(GL2.GL_EDGE_FLAG_ARRAY);
            gl.glEdgeFlagPointer(0, edgeBuffer);
        }

        gl.glDrawElements(GL.GL_TRIANGLES,
                          numIndices,
                          GL.GL_UNSIGNED_INT,
                          indexBuffer);

        if((vertexFormat & EDGES) != 0)
        {
            gl.glDisableClientState(GL2.GL_EDGE_FLAG_ARRAY);
            gl.glEdgeFlag(true);
        }

        clearVertexState(gl);
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

/*
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

        int num_tris = numCoords / 3;

        for(int i = 0; i < num_tris; i++)
        {
            System.arraycopy(coordinates, i * 9, wkPolygon, 0, 9);

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

        dataOut[0] = out_x;
        dataOut[1] = out_y;
        dataOut[2] = out_z;

        // Copy it back again.
        end[0] = end_x;
        end[1] = end_y;
        end[2] = end_z;

        return found;
*/
System.out.println("IndexedTriangleArray.pickLineSegment() not implemented yet");
        return false;
    }

    /**
     * Check for all intersections against this geometry using a line ray and
     * return the exact distance away of the closest picking point.
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

/*
        float shortest_length = Float.POSITIVE_INFINITY;
        float this_length;
        boolean found = false;
        float out_x = 0;
        float out_y = 0;
        float out_z = 0;

        int num_tris = numCoords / 3;

        for(int i = 0; i < num_tris; i++)
        {
            System.arraycopy(coordinates, i * 9, wkPolygon, 0, 9);

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

        dataOut[0] = out_x;
        dataOut[1] = out_y;
        dataOut[2] = out_z;

        return found;
*/
System.out.println("IndexedTriangleArray.pickLineRay() not implemented yet");
        return false;
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
        IndexedTriangleArray geom = (IndexedTriangleArray)o;
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
        if(!(o instanceof IndexedTriangleArray))
            return false;
        else
            return equals((IndexedTriangleArray)o);
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
    public void setEdgeFlags(ByteBuffer flags)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if((flags != null) && (flags.limit() < numCoords))
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(EDGE_ARRAY_LENGTH_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args =
            {
                new Integer(flags.limit()),
                new Integer(numCoords)
            };
            Format[] fmts = { n_fmt, n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        edgeBuffer = flags;

        if(flags == null)
            vertexFormat &= EDGE_CLEAR;
        else
            vertexFormat |= EDGES;
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param ta The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(IndexedTriangleArray ta)
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
    public boolean equals(IndexedTriangleArray ta)
    {
        return (ta == this);
    }
}
