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

// Application specific imports
import gl4java.GLFunc;
import gl4java.GLContext;
import gl4java.GLEnum;
import gl4java.drawable.GLDrawable;


/**
 * An OpenGL TriangleArray.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
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
    public void renderState(GLDrawable gld)
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

        GLFunc gl = gld.getGL();
        GLContext glj = gld.getGLContext();

        Integer listName = (Integer)displayListMap.get(glj);

        if(listName == null)
        {
            listName = new Integer(gl.glGenLists(1));

            gl.glNewList(listName.intValue(), GLEnum.GL_COMPILE_AND_EXECUTE);

            gl.glEnableClientState(GLEnum.GL_VERTEX_ARRAY);
            gl.glVertexPointer(3, GLEnum.GL_FLOAT, 0, coordinates);

            if((vertexFormat & NORMALS) != 0)
            {
                gl.glEnableClientState(GLEnum.GL_NORMAL_ARRAY);
                gl.glNormalPointer(GLEnum.GL_FLOAT, 0, normals);
            }

            if((vertexFormat & TEXTURE_MASK) != 0)
            {
                // single texturing or multi-texturing
                if(numTextureArrays == 1)
                {
                    gl.glEnableClientState(GLEnum.GL_TEXTURE_COORD_ARRAY);
                    gl.glTexCoordPointer(textureTypes[0],
                                         GLEnum.GL_FLOAT,
                                         0,
                                         textures[0]);
                }
                else
                {
                    // Only sets the first for now. Need to look into how to handle
                    // the multitexture case.
                    gl.glEnableClientState(GLEnum.GL_TEXTURE_COORD_ARRAY);
                    gl.glTexCoordPointer(textureTypes[0],
                                         GLEnum.GL_FLOAT,
                                         0,
                                         textures[0]);
                }
            }

            if((vertexFormat & COLOR_MASK) != 0)
            {
                int size = ((vertexFormat & COLOR_3) != 0) ? 3 : 4;

                gl.glEnableClientState(GLEnum.GL_COLOR_ARRAY);
                gl.glTexCoordPointer(size,
                                     GLEnum.GL_FLOAT,
                                     0,
                                     colors);
            }

            gl.glDrawArrays(gl.GL_TRIANGLES, 0, numCoords);

            gl.glEndList();
            displayListMap.put(glj, listName);
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
    public void restoreState(GLDrawable gld)
    {
        GLFunc gl = gld.getGL();

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