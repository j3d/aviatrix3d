/*****************************************************************************
 *                  Yumetech, Inc Copyright (c) 2004 - 2006
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

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.AppearanceAttributeRenderable;

/**
 * Describes attributes used for controlling alpha test state during any
 * drawing operations.
 * <p>
 *
 *
 * <b>Note</b>
 * <p>
 * Use of this class will automatically cause the containing appearance and
 * shape nodes to be placed in to the transparency sort bucket of the rendering
 * operations rather than state sorted. Use of this class sparingly is suggested
 * as alpha testing/blending is performed by the rendering pipeline separately.
 *
 * The default blending mode is set up to mimic the defaults used by OpenGL.
 * However, this class should only be used when the <i>GL_ARB_imaging</i>
 * subset is available. This code automatically checks for it's existance and
 * will disable calling itself if it detects the lack of existance.
 * <p>
 *
 * <b>Default Values</b>
 * <p>
 *
 * When intialised, this class follows the default OpenGL values for alpha
 * testing:
 *
 * <pre>
 * Alpha Function:    GL_ALWAYS
 * Alpha test value:  0.0
 * </pre>
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>invalidAlphaFunctionMsg: Error message when the given function type
 *     does not match one of the available types.</li>
 * <li>invalidAlphaRangeMsg: Error message when the given depth value is
 *     not in the range [0,1].</li>
 * </ul>
 *
 *
 * @author Justin Couch
 * @version $Revision: 2.3 $
 */
public class AlphaAttributes extends NodeComponent
    implements AppearanceAttributeRenderable
{
    /** Message when the stencil operation is not a valid value */
    private static final String INVALID_RANGE_PROP =
        "org.j3d.aviatrix3d.AlphaAttributes.invalidAlphaRangeMsg";

    /** Message when the buffer function is not a valid value */
    private static final String INVALID_FUNCTION_PROP =
        "org.j3d.aviatrix3d.AlphaAttributes.invalidAlphaFunctionMsg";

    /** Comparison function that always fails.*/
    public static final int FUNCTION_NEVER = GL.GL_NEVER;

    /** Comparison function that passes if current alpha < test alpha. */
    public static final int FUNCTION_LESS = GL.GL_LESS;

    /** Comparison function that passes if current alpha <= test alpha. */
    public static final int FUNCTION_LESS_OR_EQUAL = GL.GL_LEQUAL;

    /** Comparison function that passes if current alpha > test alpha. */
    public static final int FUNCTION_GREATER = GL.GL_GREATER;

    /** Comparison function that passes if current alpha >= test alpha. */
    public static final int FUNCTION_GREATER_OR_EQUAL = GL.GL_GEQUAL;

    /** Comparison function that passes if current alpha == test alpha. */
    public static final int FUNCTION_EQUAL = GL.GL_EQUAL;

    /** Comparison function that passes if current alpha is not equal range. */
    public static final int FUNCTION_NOTEQUAL = GL.GL_NOTEQUAL;

    /** Comparison function that always passes. */
    public static final int FUNCTION_ALWAYS = GL.GL_ALWAYS;


    /** The stencil function mode to use */
    private int function;

    /**
     * The cutoff value of alpha that we test against with the set function.
     * Standard OpenGL range of [0,1]. Defaults to 0.0 as per OpenGL.
     */
    private float alphaTestValue;

    /** Saved state between start and stop render */
    private int savedFunction;

    /** Saved state of the alpha test value between start and stop render */
    private float savedTestValue;

    /** Saved state of the alpha test state between start and stop render */
    private boolean savedEnable;

    /** Scratch var to read out the test value state */
    private float[] tmpFloat;

    /** Scratch var to read out the test function state */
    private int[] tmpInt;

    /**
     * Constructs a attribute set with default values as specified above.
     */
    public AlphaAttributes()
    {
        function = FUNCTION_ALWAYS;

        alphaTestValue = 0.0f;
        tmpFloat = new float[1];
        tmpInt = new int[1];
    }

    //---------------------------------------------------------------
    // Methods defined by AppearanceAttributeRenderable
    //---------------------------------------------------------------

    /**
     * Get the type this visual attribute represents.
     *
     * @return One of the _ATTRIBUTE constants
     */
    public int getAttributeType()
    {
        return ALPHA_ATTRIBUTE;
    }

    //---------------------------------------------------------------
    // Methods defined by ObjectRenderable
    //---------------------------------------------------------------

    /**
     * Issue ogl commands needed for this component
     *
     * @param gl The gl context to draw with
     */
    public void render(GL gl)
    {
        savedEnable = gl.glIsEnabled(GL.GL_ALPHA_TEST);

        if(!savedEnable)
            gl.glEnable(GL.GL_ALPHA_TEST);

        gl.glGetIntegerv(GL.GL_ALPHA_TEST_FUNC, tmpInt, 0);
        gl.glGetFloatv(GL.GL_ALPHA_TEST_REF, tmpFloat, 0);

        savedFunction = tmpInt[0];
        savedTestValue = tmpFloat[0];

        gl.glAlphaFunc(function, alphaTestValue);
    }

    /**
     * Restore all openGL state to the given drawable. Does nothing as it
     * assumes the system will disable blending outside of this class.
     *
     * @param gl The gl context to draw with
     */
    public void postRender(GL gl)
    {
        if(!savedEnable)
            gl.glDisable(GL.GL_ALPHA_TEST);

        gl.glAlphaFunc(savedFunction, savedTestValue);

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
        AlphaAttributes ta = (AlphaAttributes)o;
        return compareTo(ta);
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
        if(!(o instanceof AlphaAttributes))
            return false;
        else
            return equals((AlphaAttributes)o);

    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Set the determines whether the alpha tests passes.
     * This should be one of the FUNCTION_ constants at the top of this class.
     *
     * @param func One of the FUNCTION_ constants
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     * @throws IllegalArgumentException The operation value was not one of the
     *   valid types.
     */
    public void setAlphaFunction(int func)
        throws IllegalArgumentException,
               InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        switch(func)
        {
            case FUNCTION_NEVER:
            case FUNCTION_ALWAYS:
            case FUNCTION_LESS:
            case FUNCTION_LESS_OR_EQUAL:
            case FUNCTION_GREATER:
            case FUNCTION_GREATER_OR_EQUAL:
            case FUNCTION_EQUAL:
            case FUNCTION_NOTEQUAL:
                function = func;
                break;

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(INVALID_FUNCTION_PROP) + func;
                throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Get the current operation used when the alpha tests pass.
     *
     * @return One of the FUNCTION_ constants
     */
    public int getAlphaFunction()
    {
        return function;
    }

    /**
     * Set the cutoff value for alpha blending. The cut off is compared against the
	 * current alpha value, so must be in the range [0,1]. The default value is 0.
     *
     * @param value The alpha component cut off value to use
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     * @throws IllegalArgumentException The cutoff value was outside the range [0,1]
     */
    public void setAlphaCutoff(float value)
        throws IllegalArgumentException,
               InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(value < 0 || value > 1)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(INVALID_RANGE_PROP) + value;
            throw new IllegalArgumentException(msg);
        }

        alphaTestValue = value;
    }

    /**
     * Get the current cutoff component value. The default value is 0.
     *
     * @return A value in the range [0,1]
     */
    public float getAlphaCutoff()
    {
        return alphaTestValue;
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param aa The attributes instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(AlphaAttributes aa)
    {
        if(aa == null)
            return 1;

        if(aa == this)
            return 0;

        if(alphaTestValue != aa.alphaTestValue)
            return alphaTestValue < aa.alphaTestValue ? -1 : 1;

        if(function != aa.function)
            return function < aa.function? -1 : 1;

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param aa The attributes instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(AlphaAttributes aa)
    {
        if(aa == this)
            return true;

        if((aa == null) ||
           (alphaTestValue != aa.alphaTestValue) ||
           (function != aa.function))
            return false;

        return true;
    }
}
