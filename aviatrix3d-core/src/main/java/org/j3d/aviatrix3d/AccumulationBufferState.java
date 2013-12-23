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
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;
import javax.media.opengl.GL2;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.BufferStateRenderable;

/**
 * Describes attributes used when interacting with the accumulation buffer.
 * <p>
 *
 * Including this state class in the global setup automatically enables
 * the accumulation buffer. It also has a slightly special status in that,
 * unlike the other buffer types, the state is executed at the end of the
 * rendering pass rather than at the beginning. That is because accumulation
 * buffers are used to take the current colour buffer and copy it into the
 * accumulation buffer using one of a set of functions (or copy the
 * accumulation buffer back to the colour buffer in the case of GL_RETURN).
 * <p>
 *
 * The default setup for this class is:
 * <p>
 * <ul>
 * <li>Function: RETURN</li>
 * <li>Value: 1.0</li>
 * </ul>
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>invalidAccFunctionMsg: Error message when the given function type
 *     does not match one of the available types.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 2.5 $
 */
public class AccumulationBufferState extends BufferState
    implements BufferStateRenderable
{
    /** Message when the buffer function is not a valid value */
    private static final String INVALID_FUNCTION_PROP =
        "org.j3d.aviatrix3d.AccumulationBufferState.invalidAccFunctionMsg";

    /**
     * Take the current buffer colours multiply them by value and then add
     * with the accumulation buffer.
     */
    public static final int FUNCTION_ACCUMULATE = GL2.GL_ACCUM;

    /** Replace the values in the accumulation buffer with the current colours */
    public static final int FUNCTION_LOAD = GL2.GL_LOAD;

    /** Add the value to the current accumulation buffer values. */
    public static final int FUNCTION_ADD = GL2.GL_ADD;

    /**
     * Multiply the contents of the current buffer by the value. Note that for
     * this function, the value is clamped to [-1, 1] by OpenGL.
     * */
    public static final int FUNCTION_MULTIPLY = GL2.GL_MULT;

    /** Save the contents of the current accumulation buffer back to the colour buffer */
//    public static final int FUNCTION_RETURN = GL.GL_RETURN;

    /** The minimum range for the depth test */
    private float value;

    /** The stencil function mode to use */
    private int function;

    /** Flag indicating if this state should clear the current buffer state */
    private boolean clearState;

    /** Red channel clear value */
    private float red;

    /** Green channel clear value  */
    private float green;

    /** Blue channel clear value */
    private float blue;

    /** Alpha channel clear value */
    private float alpha;

    /**
     * Constructs a state set with default values.
     */
    public AccumulationBufferState()
    {
        function = FUNCTION_ACCUMULATE;
        value = 1;
        red = 0;
        green = 0;
        blue = 0;
        alpha = 0;

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
    @Override
    public int getBufferType()
    {
        return ACCUMULATION_BUFFER;
    }

    /**
     * Get the GL buffer bit flag that this state class represents. Used for
     * bulk clearing all the states at once.
     *
     * @return The bit state constant for Stencil Buffers
     */
    @Override
    public int getBufferBitMask()
    {
        return GL2.GL_ACCUM_BUFFER_BIT;
    }

    /**
     * Check to see if this buffer should be cleared at the start of this run.
     * If it should be, add the bit mask from this state to the global list.
     *
     * @return true if the state should be cleared
     */
    @Override
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
    @Override
    public void setBufferState(GL2 gl)
    {
        gl.glClearAccum(red, green, blue, alpha);
    }

    /**
     * Issue ogl commands needed for this component to change the state,
     * assuming that it is already enabled.
     *
     * @param gl The gl context to draw with
     */
    @Override
    public void updateBufferState(GL2 gl)
    {
        gl.glAccum(function, value);
    }

    /**
     * Restore all state to the default values and copy the buffer to the
     * colour buffer.
     *
     * @param gl The gl context to draw with
     */
    @Override
    public void clearBufferState(GL2 gl)
    {
        gl.glAccum(GL2.GL_RETURN, 1.0f);
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
    @Override
    public int compareTo(Object o)
        throws ClassCastException
    {
        AccumulationBufferState abs = (AccumulationBufferState)o;
        return compareTo(abs);
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
    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof AccumulationBufferState))
            return false;
        else
            return equals((AccumulationBufferState)o);

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
     *   of the NodeUpdateListener data update callback method
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
     * Set the operation that should be performed when accumulating colour
     * values into the accumulation buffer. This should be one of the
     * FUNCTION_ constants at the top of this class.
     *
     * @param func One of the FUNCTION_ constants
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data update callback method
     * @throws IllegalArgumentException The operation value was not one of the
     *   valid types.
     */
    public void setAccumFunction(int func)
        throws IllegalArgumentException,
               InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        switch(func)
        {
            case FUNCTION_ACCUMULATE:
            case FUNCTION_LOAD:
            case FUNCTION_ADD:
            case FUNCTION_MULTIPLY:
//            case FUNCTION_RETURN:
                function = func;
                break;

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(INVALID_FUNCTION_PROP);

                Locale lcl = intl_mgr.getFoundLocale();
                NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

                Object[] msg_args = { new Integer(func) };
                Format[] fmts = { n_fmt };

                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                msg_fmt.setFormats(fmts);
                String msg = msg_fmt.format(msg_args);
                throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Get the current operation used when accumulating colour values.
     *
     * @return One of the FUNCTION_ constants
     */
    public int getAccumFunction()
    {
        return function;
    }

    /**
     * Set the value that accompanies some of the operation types. Consult the
     * docs for individual functions for how the value may be used.
     *
     * @param val An arbitrary value defined by the user
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data update callback method
     */
    public void setValue(float val)
        throws IllegalArgumentException,
               InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        value = val;
    }

    /**
     * Get the value operand used with some of the function types.
     *
     * @return Any arbitrary value set by the user
     */
    public float getValue()
    {
        return value;
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
     * @param abs The attributes instance to be comabsred
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(AccumulationBufferState abs)
    {
        if(abs == null)
            return 1;

        if(abs == this)
            return 0;

        if(clearState != abs.clearState)
            return clearState ? 1 : -1;

        if(function != abs.function)
            return function < abs.function? -1 : 1;

        if(value != abs.value)
            return value < abs.value? -1 : 1;

        if(red != abs.red)
            return red < abs.red? -1 : 1;

        if(green != abs.green)
            return green < abs.green? -1 : 1;

        if(blue != abs.blue)
            return blue < abs.blue? -1 : 1;

        if(alpha != abs.alpha)
            return alpha < abs.alpha? -1 : 1;

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param abs The attributes instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(AccumulationBufferState abs)
    {
        if(abs == this)
            return true;

        if((abs == null) ||
           (clearState != abs.clearState) ||
           (function != abs.function) ||
           (value != abs.value) ||
           (red != abs.red) ||
           (green != abs.green) ||
           (blue != abs.blue) ||
           (alpha != abs.alpha))
            return false;

        return true;
    }
}
