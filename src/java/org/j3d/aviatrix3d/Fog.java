/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003
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
 * Describes a a fog rendering effect.
 * <p>
 *
 * All types of fog are reprensented in a single class. If the parameter
 * type is not useful for the fog mode, it is ignored.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class Fog extends Leaf
{
    /** Set the fog mode to exponential curve */
    public static final int EXPONENTIAL = 1;

    /** Set the fog mode to exponential-squared curve */
    public static final int EXPONENTIAL_2 = 2;

    /** Set the fog mode to linear */
    public static final int LINEAR = 3;

    /** Has the texture attribute object reference changed */
    private boolean fogChanged;

    /** The current mode in use */
    private int mode;

    /** Density function if this is an exponential type */
    private float density;

    /** Start distance parameter for linear fog */
    private float startDistance;

    /** End (max density) distance parameter for linear fog */
    private float endDistance;

    /** Base colour of the fog */
    private float[] color;

    /** Map of display contexts to maps */
    private HashMap dispListMap;

    /**
     * Constructs a fog node with the default mode set to linear and the
     * colour set to white.
     */
    public Fog()
    {
        this(LINEAR);
    }

    /**
     * Construct a fog using the given mode and the colour set to white.
     */
    public Fog(int mode)
    {
        this.mode = mode;
        dispListMap = new HashMap(1);
        color = new float[] { 1, 1, 1 };
    }

    /**
     * Construct a fog using the given mode and colour.
     */
    public Fog(int mode, float[] c)
    {
        this.mode = mode;
        dispListMap = new HashMap(1);
        color = new float[3];
        color[0] = c[0];
        color[1] = c[1];
        color[2] = c[2];
    }

    //----------------------------------------------------------
    // Methods overriding the Node class
    //----------------------------------------------------------

    /**
     * Issue ogl commands needed for this component
     *
     * @param gld The drawable for reseting the state
     */
    public void renderState(GL gl, GLU glu)
    {
        Integer listName = (Integer)dispListMap.get(gl);

        if(listName == null)
        {
            listName = new Integer(gl.glGenLists(1));

            gl.glNewList(listName.intValue(), GL.GL_COMPILE_AND_EXECUTE);

            gl.glFogi(GL.GL_FOG_MODE, mode);
            gl.glFogfv(GL.GL_FOG_COLOR, color);

            switch(mode)
            {
                case LINEAR:
                    gl.glFogf(GL.GL_FOG_START, startDistance);
                    gl.glFogf(GL.GL_FOG_END, endDistance);
                    break;

                case EXPONENTIAL:
                    gl.glFogf(GL.GL_FOG_DENSITY, density);
                    break;

                case EXPONENTIAL_2:
                    gl.glFogf(GL.GL_FOG_DENSITY, density);
                    break;
            }

            gl.glEndList();
            dispListMap.put(gl, listName);
        }
        else
        {
            gl.glCallList(listName.intValue());
        }
    }

    /**
     * Restore all openGL state to the given drawable.
     *
     * @param gld The drawable for reseting the state
     */
    public void restoreState(GL gl, GLU glu)
    {
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Change the fog colour to the new colour. Fog colour only takes RGB.
     *
     * @param c The colour to copy in
     */
    public void setColor(float[] c)
    {
        color[0] = c[0];
        color[1] = c[1];
        color[2] = c[2];
    }

    /**
     * Get the current drawing colour
     *
     * @param c An array of length 3 or more to copy the colour to
     */
    public void getColor(float[] c)
    {
        c[0] = color[0];
        c[1] = color[1];
        c[2] = color[2];
    }
}