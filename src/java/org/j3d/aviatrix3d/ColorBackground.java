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
 * Background node that represents a single solid 4-component colour.
 * <p>
 *
 * This will set the background colour to a single colour for the entire
 * viewport. If used, this will override the setClearColor() on
 * {@link org.j3d.aviatrix3d.DrawableSurface}.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class ColorBackground extends Background
{
    /** Base colour of the fog */
    private float[] color;

    /**
     * Constructs a background node for a base colour of black.
     */
    public ColorBackground()
    {
        color = new float[4];
    }

    /**
     * Construct a background node for a user-provided colour
     */
    public ColorBackground(float[] c)
    {
        this();
        color[0] = c[0];
        color[1] = c[1];
        color[2] = c[2];
        color[3] = c[3];
    }

    //----------------------------------------------------------
    // Methods overriding the Node class
    //----------------------------------------------------------

    /**
     * Issue ogl commands needed for this component
     *
     * @param gld The drawable for reseting the state
     */
    public void render(GL gl, GLU glu)
    {
        gl.glClearColor(color[0], color[1], color[2], color[3]);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
    }

    /**
     * Restore all openGL state to the given drawable.
     *
     * @param gld The drawable for reseting the state
     */
    public void postRender(GL gl, GLU glu)
    {
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Change the colour to the new colour. Colour takes RGBA.
     *
     * @param c The colour to copy in
     */
    public void setColor(float[] c)
    {
        color[0] = c[0];
        color[1] = c[1];
        color[2] = c[2];
        color[3] = c[3];
    }

    /**
     * Get the current drawing colour
     *
     * @param c An array of length 4 or more to copy the colour to
     */
    public void getColor(float[] c)
    {
        c[0] = color[0];
        c[1] = color[1];
        c[2] = color[2];
        c[3] = color[3];
    }
}