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
 * Base representation of a light source.
 * <p>
 *
 * By default a light is not enabled and the colour is set to black.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public abstract class Light extends Leaf
{
    /** The colour of the light */
    protected float[] color;

    /** The enabled state */
    protected boolean enabled;

    /**
     * Creates a light with the colour set to black.
     */
    protected Light()
    {
        color = new float[3];
    }

    /**
     * Create a light with the given base colour.Colour must be in the range
     * [0, 1] otherwise an exception is generated.
     *
     * @param col The new colour value to use
     * @throw IllegalArgumentException The colour value is out of range
     */
    protected Light(float[] col)
        throws IllegalArgumentException
    {
        if(col[0] < 0 || col[0] > 1)
            throw new IllegalArgumentException("Red component out of range");

        if(col[1] < 0 || col[1] > 1)
            throw new IllegalArgumentException("Green component out of range");

        if(col[2] < 0 || col[2] > 1)
            throw new IllegalArgumentException("Blue component out of range");

        color = new float[] { col[0], col[1], col[2] };
    }

    //---------------------------------------------------------------
    // Misc local methods
    //---------------------------------------------------------------

    /**
     * Set the enabled state of the light. Can use this to turn it on and off
     * in a general fashion.
     *
     * @param state The new state of the light
     */
    public void setEnabled(boolean state)
    {
        enabled = state;
    }

    /**
     * Get the current enabled state of the light.
     *
     * @return The current state
     */
    public boolean getEnabled()
    {
        return enabled;
    }

    /**
     * Set the colour to the new value. Colour must be in the range
     * [0, 1] otherwise an exception is generated.
     *
     * @param col The new colour value to use
     * @throw IllegalArgumentException The colour value is out of range
     */
    public void setColor(float[] col)
        throws IllegalArgumentException
    {
        if(col[0] < 0 || col[0] > 1)
            throw new IllegalArgumentException("Red component out of range");

        if(col[1] < 0 || col[1] > 1)
            throw new IllegalArgumentException("Green component out of range");

        if(col[2] < 0 || col[2] > 1)
            throw new IllegalArgumentException("Blue component out of range");

        color[0] = col[0];
        color[1] = col[1];
        color[2] = col[2];
    }

    /**
     * Retrieve the current colour value from the light.
     *
     * @param col An array to copy the colour value into
     */
    public void getColor(float[] col)
    {
        col[0] = color[0];
        col[1] = color[1];
        col[2] = color[2];
    }
}
