/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2006
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
import javax.media.opengl.GL2;

// Local imports
import org.j3d.aviatrix3d.rendering.BufferStateRenderable;

/**
 * Describes attributes used when interacting with the colour buffer.
 * <p>
 *
 * Including this state class in the global setup automatically enables
 * colour buffering. If an instance of this state is not
 * included in the scene, the default values, and depth testing are enabled
 * automatically. Use this class to override the defaults, or to deliberately
 * disable depth testing.
 * <p>
 *
 * The default setup for this class has all 4 colour channels enabled:
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 2.4 $
 */
public class ColorBufferState extends BufferState
    implements BufferStateRenderable
{
    /** Red channel enabled */
    private boolean redEnabled;

    /** Green channel enabled */
    private boolean greenEnabled;

    /** Blue channel enabled */
    private boolean blueEnabled;

    /** Alpha channel enabled */
    private boolean alphaEnabled;

    /** Red channel clear value */
    private float red;

    /** Green channel clear value  */
    private float green;

    /** Blue channel clear value */
    private float blue;

    /** Alpha channel clear value */
    private float alpha;

    /** Flag indicating if this state should clear the current buffer state */
    private boolean clearState;

    /**
     * Constructs a state set with default values.
     */
    public ColorBufferState()
    {
        red = 0;
        green = 0;
        blue = 0;
        alpha = 0;
        redEnabled = true;
        greenEnabled = true;
        blueEnabled = true;
        alphaEnabled = true;

        clearState = true;
    }

    //---------------------------------------------------------------
    // Methods defined by BufferStateRenderable
    //---------------------------------------------------------------

    /**
     * Get the type of buffer this state represents.
     *
     * @return One of the _BUFFER constants
     */
    public int getBufferType()
    {
        return COLOR_BUFFER;
    }

    /**
     * Get the GL buffer bit flag that this state class represents. Used for
     * bulk clearing all the states at once.
     *
     * @return The bit state constant for Stencil Buffers
     */
    public int getBufferBitMask()
    {
        return GL.GL_COLOR_BUFFER_BIT;
    }

    /**
     * Check to see if this buffer should be cleared at the start of this run.
     * If it should be, add the bit mask from this state to the global list.
     *
     * @return true if the state should be cleared
     */
    public boolean checkClearBufferState()
    {
        return clearState;
    }

    /**
     * Issue ogl commands needed for this buffer to set the initial state,
     * including the initial enabling.
     *
     * @param gl The gl context to draw with
     */
    public void setBufferState(GL2 gl)
    {
        gl.glClearColor(red, green, blue, alpha);
        gl.glColorMask(redEnabled, greenEnabled, blueEnabled, alphaEnabled);
    }

    /**
     * Issue ogl commands needed for this component to change the state,
     * assuming that it is already enabled.
     *
     * @param gl The gl context to draw with
     */
    public void updateBufferState(GL2 gl)
    {
        gl.glClearColor(red, green, blue, alpha);
        gl.glColorMask(redEnabled, greenEnabled, blueEnabled, alphaEnabled);
    }

    /**
     * Restore all state to the default values.
     *
     * @param gl The gl context to draw with
     */
    public void clearBufferState(GL2 gl)
    {
        gl.glClearColor(0, 0, 0, 0);
        gl.glColorMask(true, true, true, true);
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
    public int compareTo(Object o)
        throws ClassCastException
    {
        ColorBufferState sa = (ColorBufferState)o;
        return compareTo(sa);
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
    public boolean equals(Object o)
    {
        if(!(o instanceof ColorBufferState))
            return false;
        else
            return equals((ColorBufferState)o);

    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Set the flag for whether the buffer state should be cleared when this
     * state object is executed.
     *
     * @param clear True if the buffer should be cleared when this is the first
     *   state to be used in the current operation
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setClearBufferState(boolean clear)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        clearState = clear;
    }

    /**
     * Set the flags describing which channels should be enabled in the colour
     * buffer.
     *
     * @param red true if the red channel should be enabled
     * @param green true if the green channel should be enabled
     * @param blue true if the blue channel should be enabled
     * @param alpha true if the alpha channel should be enabled
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setColorMask(boolean red,
                             boolean green,
                             boolean blue,
                             boolean alpha)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        redEnabled = red;
        greenEnabled = green;
        blueEnabled = blue;
        alphaEnabled = alpha;
    }

    /**
     * Check to see if the red channel is currently enabled
     *
     * @return true if the red channel is currently being written
     */
    public boolean isRedEnabled()
    {
        return redEnabled;
    }

    /**
     * Check to see if the green channel is currently enabled
     *
     * @return true if the green channel is currently being written
     */
    public boolean isGreenEnabled()
    {
        return greenEnabled;
    }

    /**
     * Check to see if the blue channel is currently enabled
     *
     * @return true if the blue channel is currently being written
     */
    public boolean isBlueEnabled()
    {
        return blueEnabled;
    }

    /**
     * Check to see if the alpha channel is currently enabled
     *
     * @return true if the alpha channel is currently being written
     */
    public boolean isAlphaEnabled()
    {
        return alphaEnabled;
    }

    /**
     * Set the value that each of the colour channels should be cleared to.
     * The values are automatically clamped by OpenGL to the range [-1,1].
     *
     * @param red The red channel clear value
     * @param green The green channel clear value
     * @param blue The blue channel clear value
     * @param alpha The alpha channel clear value
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data update callback method
     */
    public void setClearColor(float red, float green, float blue, float alpha)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha= alpha;
    }

    /**
     * Get the current red channel clear value
     *
     * @return The current red channel clear value [-1,1]
     */
    public float getRed()
    {
        return red;
    }

    /**
     * Get the current green channel clear value
     *
     * @return The current green channel clear value [-1,1]
     */
    public float getGreen()
    {
        return green;
    }

    /**
     * Get the current blue channel clear value
     *
     * @return The current blue channel clear value [-1,1]
     */
    public float getBlue()
    {
        return blue;
    }

    /**
     * Get the current alpha channel clear value
     *
     * @return The current alpha channel clear value [-1,1]
     */
    public float getAlpha()
    {
        return alpha;
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param cbs The attributes instance to be comcbsred
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(ColorBufferState cbs)
    {
        if(cbs == null)
            return 1;

        if(cbs == this)
            return 0;

        if(clearState != cbs.clearState)
            return clearState ? 1 : -1;

        if(redEnabled != cbs.redEnabled)
            return redEnabled ? 1 : -1;

        if(greenEnabled != cbs.greenEnabled)
            return greenEnabled ? 1 : -1;

        if(blueEnabled != cbs.blueEnabled)
            return blueEnabled ? 1 : -1;

        if(alphaEnabled != cbs.alphaEnabled)
            return alphaEnabled ? 1 : -1;

        if(red != cbs.red)
            return red < cbs.red? -1 : 1;

        if(green != cbs.green)
            return green < cbs.green? -1 : 1;

        if(blue != cbs.blue)
            return blue < cbs.blue? -1 : 1;

        if(alpha != cbs.alpha)
            return alpha < cbs.alpha? -1 : 1;

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param cbs The attributes instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(ColorBufferState cbs)
    {
        if(cbs == this)
            return true;

        if((cbs == null) ||
           (clearState != cbs.clearState) ||
           (redEnabled != cbs.redEnabled) ||
           (greenEnabled != cbs.greenEnabled) ||
           (blueEnabled != cbs.blueEnabled)||
           (alphaEnabled != cbs.alphaEnabled) ||
           (red != cbs.red) ||
           (green != cbs.green) ||
           (blue != cbs.blue) ||
           (alpha != cbs.alpha))
            return false;

        return true;
    }
}
