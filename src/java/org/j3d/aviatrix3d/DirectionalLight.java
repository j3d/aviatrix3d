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
 * Representation of a directional light source.
 * <p>
 *
 * A directional light has a direction, but no location.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class DirectionalLight extends Light
{
    /** The colour of the light */
    protected float[] direction;

    /**
     * Creates a light with the colour set to black.
     */
    public DirectionalLight()
    {
        super();

        direction = new float[3];
    }

    /**
     * Create a light with the given base colour.Colour must be in the range
     * [0, 1] otherwise an exception is generated.
     *
     * @param col The new colour value to use
     */
    public DirectionalLight(float[] col)
        throws IllegalArgumentException
    {
        super(col);

        direction = new float[3];
    }

    /**
     * Create a light with the given base colour.Colour must be in the range
     * [0, 1] otherwise an exception is generated.
     *
     * @param col The new colour value to use
     * @param dir The direction of the light
     */
    public DirectionalLight(float[] col, float[] dir)
        throws IllegalArgumentException
    {
        super(col);

        direction = new float[] { dir[0], dir[1], dir[2] };
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
     * Set the direction to the new value. Direction is a vector that the
     * light is shining.
     *
     * @param dir The new direction value to use
     */
    public void setDirection(float[] dir)
    {
        direction[0] = dir[0];
        direction[1] = dir[1];
        direction[2] = dir[2];
    }

    /**
     * Set the direction to the new value. Direction is a vector that the
     * light is shining.
     *
     * @param x The x component of the direction value to use
     * @param y The y component of the direction value to use
     * @param z The z component of the direction value to use
     */
    public void setDirection(float x, float y, float z)
    {
        direction[0] = x;
        direction[1] = y;
        direction[2] = z;
    }

    /**
     * Retrieve the current direction vector from the light.
     *
     * @param dir An array to copy the direction into
     */
    public void getDirection(float[] dir)
    {
        dir[0] = direction[0];
        dir[1] = direction[1];
        dir[2] = direction[2];
    }
}
