/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
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

// Standard imports
import java.util.HashMap;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLU;

// Local imports
// None

/**
 * An OpenGL TriangleArray.
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class TriangleArray extends VertexGeometry
{
    /**
     * Constructs a TriangleArray with default values.
     */
    public TriangleArray()
    {
    }

    /**
     * Issue ogl commands needed for this renderable object.
     *
     * @param gld The drawable for resetting the state
     */
    public void renderState(GL gl, GLU glu)
    {
        // If we have changed state, then clear the old display lists
        if(stateChanged)
        {
            synchronized(displayListMap)
            {
                displayListMap.clear();
                stateChanged = false;
            }
        }

        // No coordinates, do nothing.
        if((vertexFormat & COORDINATES) == 0)
            return;

        Integer listName = (Integer)displayListMap.get(gl);

        if(listName == null)
        {
            listName = new Integer(gl.glGenLists(1));

            gl.glNewList(listName.intValue(), GL.GL_COMPILE_AND_EXECUTE);

            gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
            gl.glVertexPointer(3, GL.GL_FLOAT, 0, vertexBuffer);

            if((vertexFormat & NORMALS) != 0)
            {
                gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
                gl.glNormalPointer(GL.GL_FLOAT, 0, normalBuffer);
            }

            if((vertexFormat & TEXTURE_MASK) != 0)
            {
                // single texturing or multi-texturing
                if(numTextureArrays == 1)
                {
                    gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
                    gl.glTexCoordPointer(textureTypes[0],
                                         GL.GL_FLOAT,
                                         0,
                                         textureBuffer);
                }
                else
                {
                    // Only sets the first for now. Need to look into how to handle
                    // the multitexture case.
                    gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
                    gl.glTexCoordPointer(textureTypes[0],
                                         GL.GL_FLOAT,
                                         0,
                                         textureBuffer);
                }
            }

            if((vertexFormat & COLOR_MASK) != 0)
            {
                int size = ((vertexFormat & COLOR_3) != 0) ? 3 : 4;

                gl.glEnableClientState(GL.GL_COLOR_ARRAY);
                gl.glTexCoordPointer(size,
                                     GL.GL_FLOAT,
                                     0,
                                     colorBuffer);
            }

            gl.glDrawArrays(gl.GL_TRIANGLES, 0, numCoords);

            gl.glEndList();
            displayListMap.put(gl, listName);
        }
        else
        {
            gl.glCallList(listName.intValue());
        }
    }

    /**
     * Restore all openGL state.
     *
     * @param gld The drawable for resetting the state
     */
    public void restoreState(GL gl, GLU glu)
    {
        if((vertexFormat & COORDINATES) != 0)
        {
            gl.glDisableClientState(gl.GL_VERTEX_ARRAY);

            if((vertexFormat & NORMALS) != 0)
                gl.glDisableClientState(gl.GL_NORMAL_ARRAY);

            if((vertexFormat & TEXTURE_MASK) != 0)
                gl.glDisableClientState(gl.GL_TEXTURE_COORD_ARRAY);

            if((vertexFormat & COLOR_MASK) != 0)
                gl.glDisableClientState(gl.GL_COLOR_ARRAY);
        }
    }
}