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
import javax.vecmath.Matrix4f;

// Application specific imports
import gl4java.GLFunc;
import gl4java.GLEnum;
import gl4java.drawable.GLDrawable;

/**
 * Representation of a point light source.
 * <p>
 *
 * A positional light has a position, but no location.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class PointLight extends Light
{
    /** The colour of the light */
    protected float[] position;

    /**
     * Creates a light with the colour set to black.
     */
    public PointLight()
    {
        super();

        position = new float[3];
    }

    /**
     * Create a light with the given base colour.Colour must be in the range
     * [0, 1] otherwise an exception is generated.
     *
     * @param col The new colour value to use
     */
    public PointLight(float[] col)
        throws IllegalArgumentException
    {
        super(col);

        position = new float[3];
    }

    /**
     * Create a light with the given base colour.Colour must be in the range
     * [0, 1] otherwise an exception is generated.
     *
     * @param col The new colour value to use
     * @param pos The position of the light
     */
    public PointLight(float[] col, float[] pos)
        throws IllegalArgumentException
    {
        super(col);

        position = new float[] { pos[0], pos[1], pos[2] };
    }

    //---------------------------------------------------------------
    // Methods defined by SceneGraphObject
    //---------------------------------------------------------------

    /**
     * This method is called to render this node. All openGL commands needed
     * to render the node should be executed.  Any transformations needed
     * should be added to the transformation stack premultiplied.  This
     * method must be re-entrant as it can be called from multiple
     * places at once.
     *
     * @param gld The drawable for setting the state
     */
    public void render(GLDrawable gld)
    {
        if(!enabled)
            return;

        GLFunc gl = gld.getGL();
    }

    /*
     * This method is called after a node has been rendered.  This method
     * must be re-entrant.
     *
     * @param gld The drawable for resetting the state
     */
    public void postRender(GLDrawable gld)
    {
        if(!enabled)
            return;

        GLFunc gl = gld.getGL();

        //gl.glPopMatrix();
    }

    //---------------------------------------------------------------
    // Misc local methods
    //---------------------------------------------------------------

    /**
     * Set the position to the new value. Position is a vector that the
     * light is shining.
     *
     * @param pos The new position value to use
     */
    public void setPosition(float[] pos)
    {
        position[0] = pos[0];
        position[1] = pos[1];
        position[2] = pos[2];
    }

    /**
     * Set the position to the new value. Position is a vector that the
     * light is shining.
     *
     * @param x The x component of the position value to use
     * @param y The y component of the position value to use
     * @param z The z component of the position value to use
     */
    public void setPosition(float x, float y, float z)
    {
        position[0] = x;
        position[1] = y;
        position[2] = z;
    }

    /**
     * Retrieve the current position vector from the light.
     *
     * @param pos An array to copy the position into
     */
    public void getPosition(float[] pos)
    {
        pos[0] = position[0];
        pos[1] = position[1];
        pos[2] = position[2];
    }
}
