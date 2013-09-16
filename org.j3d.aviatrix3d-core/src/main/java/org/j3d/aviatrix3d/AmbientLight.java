/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2005
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

// Local imports
// None

/**
 * Representation of a purely ambient light source with no other abilities.
 * <p>
 *
 * An ambient light has no direction or location, but allows a base colour
 * to be added to all objects within its influence. This class allows for the
 * basic lighting abilities to be used, without needing to drag in any other
 * sort of light source like the other derived light types do.
 *
 * @author Justin Couch
 * @version $Revision: 1.16 $
 */
public class AmbientLight extends Light
{
    /**
     * Creates a light with the colour set to black.
     */
    public AmbientLight()
    {
        super(AMBIENT_TYPE);
    }

    /**
     * Create a light with the given base colour.Colour must be in the range
     * [0, 1] otherwise an exception is generated. Automatically
     *
     * @param col The new colour value to use
     * @throws IllegalArgumentException The colour value is out of range
     */
    public AmbientLight(float[] col)
        throws IllegalArgumentException
    {
        super(AMBIENT_TYPE, col);

        ambientColor[0] = col[0];
        ambientColor[1] = col[1];
        ambientColor[2] = col[2];

        // Zero out the diffuse component of this light for the basic
        // setup.
        diffuseColor[0] = 0;
        diffuseColor[1] = 0;
        diffuseColor[2] = 0;
    }

    //---------------------------------------------------------------
    // Methods defined by ComponentRenderable
    //---------------------------------------------------------------

    /**
     * Overloaded form of the render() method to render the light details given
     * the specific Light ID used by OpenGL. Since the active light ID for this
     * node may vary over time, a fixed ID cannot be used by OpenGL. The
     * renderer will always call this method rather than the normal render()
     * method. The normal post render will still be called
     *
     * @param gl The GL context to render with
     * @param lightId the ID of the light to make GL calls with
     */
    public void render(GL gl, Object lightId)
    {
        int l_id = ((Integer)lightId).intValue();

        gl.glLightfv(l_id, GL.GL_SPECULAR, specularColor, 0);
        gl.glLightfv(l_id, GL.GL_DIFFUSE, diffuseColor, 0);
        gl.glLightfv(l_id, GL.GL_AMBIENT, ambientColor, 0);

        gl.glEnable(l_id);
    }

    /*
     * Overloaded form of the postRender() method to render the light details given
     * the specific Light ID used by OpenGL. Since the active light ID for this
     * node may vary over time, a fixed ID cannot be used by OpenGL. The
     * renderer will always call this method rather than the normal postRender()
     * method. The normal post render will still be called
     *
     * @param gl The GL context to render with
     * @param lightId the ID of the light to make GL calls with
     */
    public void postRender(GL gl, Object lightId)
    {
        gl.glDisable(((Integer)lightId).intValue());
    }
}
