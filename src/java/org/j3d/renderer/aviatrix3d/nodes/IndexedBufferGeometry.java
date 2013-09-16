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

package org.j3d.renderer.aviatrix3d.nodes;

// External imports
import java.nio.*;

import javax.media.opengl.GL;

// Local imports
import org.j3d.aviatrix3d.InvalidWriteTimingException;

/**
 * Base class that defines indexed geometry types.
 * <p>
 *
 * The implementation assumes a single index is used to describe all the
 * information needed for rendering. An index will grab the same index from
 * vertex, normal, colour and texture arrays.
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public abstract class IndexedBufferGeometry extends BufferGeometry
{
    /**
     * The indices defining the geometry types. A reference to the
     * user-provided array. The current values are kept in the accompanying
     * IntBuffer instance.
     */
    protected int[] indices;

    /** The number of valid values to read from the array */
    protected int numIndices;

    /** Buffer holding the current index list */
    protected IntBuffer indexBuffer;

    //----------------------------------------------------------
    // Methods defined by Geometry
    //----------------------------------------------------------

    /**
     * Check to see if this geometry is making the geometry visible or
     * not. Returns true if the defined number of coordinates and indices
     * are both non-zero.
     *
     * @return true when the geometry is visible
     */
    protected boolean isVisible()
    {
        return super.isVisible() && numIndices != 0;
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the number of valid indexs to use. A check is performed to make
     * sure that the number of vertices high enough to support the total
     * of all the index counts so make sure to call setVertex() with the
     * required array length before calling this method. Each index must be
     * a minumum length of three.
     *
     * @param indexList The array of indices to set
     * @param num The number of valid items to read from the array
     * @throws IllegalArgumentException Invalid total index count or
     *   individual index count < 3
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds changed callback method
     */
    public void setIndices(int[] indexList, int num)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());


        if(indices == null || indices.length < num)
            indices = new int[num];

        numIndices = num;

        if(numIndices > indexBuffer.capacity())
            indexBuffer = createBuffer(numIndices);
        else
            indexBuffer.clear();

        indexBuffer.put(indices, 0, numIndices);
        indexBuffer.rewind();
    }

    /**
     * Get the number of valid indexs that are defined for this geometry.
     *
     * @return a positive number
     */
    public int getValidIndexCount()
    {
        return numIndices;
    }

    /**
     * Get the sizes of the valid indexs. The passed array must be big enough
     * to contain all the indexs.
     *
     * @param values An array to copy the index values into
     */
    public void getIndices(int[] values)
    {
        System.arraycopy(indices, 0, values, 0, numIndices);
    }

    /**
     * Convenience method to allocate a NIO buffer for the vertex handling that
     * handles floats.
     *
     * @param size The number of floats to have in the array
     */
    protected IntBuffer createBuffer(int size)
    {
        // Need to allocate a byte buffer 4 times the size requested because the
        // size is treated as bytes, not number of floats.
        ByteBuffer buf = ByteBuffer.allocateDirect(size * 4);
        buf.order(ByteOrder.nativeOrder());
        IntBuffer ret_val = buf.asIntBuffer();

        return ret_val;
    }
}
