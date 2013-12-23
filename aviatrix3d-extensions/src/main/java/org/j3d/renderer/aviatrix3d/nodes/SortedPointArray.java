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
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

// Local imports
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Point3d;
import org.j3d.maths.vector.Vector4d;
import org.j3d.util.MatrixUtils;

import org.j3d.aviatrix3d.rendering.CustomGeometryRenderable;

/**
 * An OpenGL PointArray that automatically depth sorts all the points every
 * frame.
 * <p>
 *
 * Points cannot be picked in the traditional manner using geometry-based
 * picking. The only way to pick them is based on their bounds.
 * <p>
 *
 * <b>Warning:</b> Current implementation does not render correctly for
 * multiple parent heirarchies.
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public class SortedPointArray extends BufferGeometry
    implements CustomGeometryRenderable
{
    /** The sorted version of the geometry array */
    private IntBuffer sortedVertexIndices;

    /** Tree used to sort the vertices. during calculations */
    private ObjectPlacer[] sortedVertices;

    /** Matrix utility code for doing inversions */
    private MatrixUtils matrixUtils;

    /** Temp matrix used to hold inverted camera matrix */
    private Matrix4d cameraMatrix;

    /** Temporary point location while working out the camera-space depth */
    private Point3d wkPoint;

    /**
     * Constructs a PointArray with default values.
     */
    public SortedPointArray()
    {
        wkPoint = new Point3d();
        cameraMatrix = new Matrix4d();
        matrixUtils = new MatrixUtils();
    }

    //---------------------------------------------------------------
    // Methods defined by SortedGeometryRenderable
    //---------------------------------------------------------------

    /**
     * Process the sorting on this node now. If needed some rendering
     * information that will be needed during real rendering can be returned.
     * If there is no information, return null.
     *
     * @param vworldTx The transformation from the root of the scene to
     *    this node according to the current traversal path
     * @param viewTransform The transformation from the root of the scene
     *    graph to the active viewpoint
     * @param viewFrustum The points of the viewfrustum
     * @param angularRes Angular resolution of the screen, or 0 if not
     *    calculable from the available data.
     * @return Any information that may be useful for the rendering step
     */
    @Override
    public Object processCull(Matrix4d vworldTx,
                              Matrix4d viewTransform,
                              Vector4d[] viewFrustum,
                              float angularRes)
    {
        // Check to make sure the buffer object is big enough.
        if(sortedVertexIndices == null ||
           sortedVertexIndices.capacity() < numCoords)
        {
            sortedVertices = new ObjectPlacer[numCoords];

            for(int i = 0; i < numCoords; i++)
                sortedVertices[i] = new ObjectPlacer();

            sortedVertexIndices = createBuffer(numCoords);
        }

        matrixUtils.inverse(viewTransform, cameraMatrix);

        for(int i = 0; i < numCoords; i++)
        {
            // transform the center of the bounds first to world space and then
            // into camera space to work out the depth.
            wkPoint.x = vertexBuffer.get(i * 3);
            wkPoint.y = vertexBuffer.get(i * 3 + 1);
            wkPoint.z = vertexBuffer.get(i * 3 + 2);

            vworldTx.transform(wkPoint, wkPoint);
            cameraMatrix.transform(wkPoint, wkPoint);

            sortedVertices[i].distance =
                (float)(wkPoint.x * wkPoint.x + wkPoint.y * wkPoint.y +
                wkPoint.z * wkPoint.z);

            sortedVertices[i].index = i;
        }

        // Now sort the list based on natural order.
        Arrays.sort(sortedVertices, 0, numCoords);

        // Take those now-sorted vertices and drop them into the vertex list
        sortedVertexIndices.rewind();

        for(int i = 0; i < numCoords; i++)
            sortedVertexIndices.put(sortedVertices[i].index);

        sortedVertexIndices.rewind();

        return null;
    }

    /**
     * Render the geometry now.
     *
     * @param gl The GL context to render with
     * @param externalData Some implementation-specific external data to
     *   aid in the rendering that was generated in the processCull method.
     */
    @Override
    public void render(GL2 gl, Object externalData)
    {
        // No coordinates, do nothing.
        if((vertexFormat & COORDINATE_MASK) == 0)
            return;

        setVertexState(gl);

        // Make sure that we do something valid in the case when they used a
        // cull stage that didn't process the sort information, but the
        // sort stage ended up selecting this method anyway.
        if(sortedVertexIndices != null)
        {
            gl.glDrawElements(GL.GL_POINTS,
                              numCoords,
                              GL.GL_UNSIGNED_INT,
                              sortedVertexIndices);
        }
        else
        {
            gl.glDrawArrays(GL.GL_POINTS, 0, numCoords);
        }

        clearVertexState(gl);
    }

    //---------------------------------------------------------------
    // Methods defined by GeometryRenderable
    //---------------------------------------------------------------

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
    @Override
    public int compareTo(Object o)
        throws ClassCastException
    {
        SortedPointArray geom = (SortedPointArray)o;
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
        if(!(o instanceof SortedPointArray))
            return false;
        else
            return equals((SortedPointArray)o);
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
    public int compareTo(SortedPointArray ta)
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
    public boolean equals(SortedPointArray ta)
    {
        return (ta == this);
    }

    /**
     * Convenience method to allocate a NIO buffer for the vertex handling that
     * handles floats.
     *
     * @param size The number of floats to have in the array
     */
    private IntBuffer createBuffer(int size)
    {
        // Need to allocate a byte buffer 4 times the size requested because the
        // size is treated as bytes, not number of floats.
        ByteBuffer buf = ByteBuffer.allocateDirect(size * 4);
        buf.order(ByteOrder.nativeOrder());
        IntBuffer ret_val = buf.asIntBuffer();

        return ret_val;
    }
}
