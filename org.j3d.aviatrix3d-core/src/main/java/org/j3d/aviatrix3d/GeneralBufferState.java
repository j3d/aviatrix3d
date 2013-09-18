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

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.BufferStateRenderable;

/**
 * Collection of state management options for a render pass for elements that
 * don't directly influence the backing buffer.
 * <p>
 *
 * The items configurable in this pass do not have any interaction with clearing
 * state in the render buffer (ie no glClear() bit to toggle).
 *
 * The default setup for this class is:
 * <p>
 * <ul>
 * <li>Blending: false</li>
 * <li>Blend Equation:    EQ_FUNC_ADD</li>
 * <li>RGB Source Mode:   BLEND_SRC_COLOR</li>
 * <li>RGB Dest Mode:     BLEND_ONE_MINUS_SRC_COLOR</li>
 * <li>Alpha Source Mode: BLEND_SRC_ALPHA</li>
 * <li>Alpha Dest Mode:   BLEND_ONE_MINUS_SRC_ALPHA</li>
 * <li>Separated Blend:   false</li>
 * </ul>
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>invalidAlphaDestMsg: Error message when the given function type
 *     does not match one of the available types.</li>
 * <li>invalidBlendMsg: Error message when the given blend source type
 *     does not match one of the available types.</li>
 * <li>invalidModeMsg: Error message when the given blend mode type
 *     does not match one of the available types.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 2.7 $
 */
public class GeneralBufferState extends BufferState
    implements BufferStateRenderable
{
    /** Message when the saturate value is set to the destination method */
    private static final String DEST_ALPHA_PROP =
        "org.j3d.aviatrix3d.GeneralBufferState.invalidAlphaDestMsg";

    /** Generic message for an unknown blend factor type */
    private static final String INVALID_BLEND_PROP =
        "org.j3d.aviatrix3d.GeneralBufferState.invalidBlendMsg";

    /** Generic message for an unknown blend mode type */
    private static final String INVALID_MODE_PROP =
        "org.j3d.aviatrix3d.GeneralBufferState.invalidModeMsg";

    /** Set the blend factor to zeros. */
    public static final int BLEND_ZERO = GL.GL_ZERO;

    /** Set the blend factor to ones. */
    public static final int BLEND_ONE = GL.GL_ONE;

    /** Set the blend factor to use the source object colour */
    public static final int BLEND_SRC_COLOR = GL.GL_SRC_COLOR;

    /** Set the blend factor to use one minus source object colour (1-src). */
    public static final int BLEND_ONE_MINUS_SRC_COLOR =
        GL.GL_ONE_MINUS_SRC_COLOR;

    /** Set the blend factor to use the destination object colour */
    public static final int BLEND_DEST_COLOR = GL.GL_DST_COLOR;

    /**
     * Set the blend factor to use one minus the destination object colour
     * (1-dest).
     */
    public static final int BLEND_ONE_MINUS_DEST_COLOR =
        GL.GL_ONE_MINUS_DST_COLOR;

    /** Set the blend factor to use the source object's alpha value */
    public static final int BLEND_SRC_ALPHA = GL.GL_SRC_ALPHA;

    /** Set the blend factor to use one minus source object alpha value (1-src). */
    public static final int BLEND_ONE_MINUS_SRC_ALPHA = GL.GL_ONE_MINUS_SRC_ALPHA;

    /** Set the blend factor to use the destination object's alpha value */
    public static final int BLEND_DEST_ALPHA = GL.GL_DST_ALPHA;

    /**
     * Set the blend factor to use one minus the destination object alpha value
     * (1-dest).
     */
    public static final int BLEND_ONE_MINUS_DEST_ALPHA =
        GL.GL_ONE_MINUS_DST_ALPHA;

    /**
     * Set the blend factor to use the provided constant colour. The constant
     * colour value is provide through the setBlendColour() method.
     */
    public static final int BLEND_CONSTANT_COLOR = GL2.GL_CONSTANT_COLOR;

    /**
     * Set the blend factor to use one minus the constant colour (1-c). The
     * constant colour value is provide through the setBlendColour() method.
     */
    public static final int BLEND_ONE_MINUS_CONSTANT_COLOR =
        GL2.GL_ONE_MINUS_CONSTANT_COLOR;

    /**
     * Set the blend factor to use the provided constant alpha value.The
     * constant colour value is provide through the setBlendColour() method.
     */
    public static final int BLEND_CONSTANT_ALPHA = GL2.GL_CONSTANT_ALPHA;

    /**
     * Set the blend factor to use one minus the constant colour alpha value
     * (1-c). The constant colour value is provide through the setBlendColour()
     * method.
     */
    public static final int BLEND_ONE_MINUS_CONSTANT_ALPHA =
        GL2.GL_ONE_MINUS_CONSTANT_ALPHA;

    /**
     * Set the blend function to saturage the colour value using the alpha
     * state. The RGB value is calculated using the min(Asrc, 1 - Adest). The
     * alpha value is treated as equivalent to GL_ONE.
     * Can only be used as the source blend factor. An error will be generated
     * if you try to use this for the destination factor.
     */
    public static final int BLEND_SRC_ALPHA_SATURATE = GL.GL_SRC_ALPHA_SATURATE;

    /** Set the blending equation to be C<sub>s</sub>S + C<sub>d</sub>D */
    public static final int EQ_FUNC_ADD = GL.GL_FUNC_ADD;

    /** Set the blending equation to be C<sub>s</sub>S - C<sub>d</sub>D */
    public static final int EQ_FUNC_SUBTRACT = GL.GL_FUNC_SUBTRACT;

    /** Set the blending equation to be C<sub>d</sub>D - C<sub>s</sub>S */
    public static final int EQ_FUNC_SUBTRACT_REVERSE = GL.GL_FUNC_REVERSE_SUBTRACT;

    /** Set the blending equation to be min(C<sub>d</sub>D, C<sub>s</sub>S) */
    public static final int EQ_FUNC_MIN = GL2.GL_MIN;

    /** Set the blending equation to be max(C<sub>d</sub>D, C<sub>s</sub>S) */
    public static final int EQ_FUNC_MAX = GL2.GL_MAX;


    /** Flag describing the blend state */
    private boolean enableBlend;

    /** The source mode for combined and RGB for the separate blending */
    private int rgbSourceMode;

    /** The destination mode for combined and RGB for the separate blending */
    private int rgbDestMode;

    /** Equation for combined and RGB for the separate blending */
    private int blendEquation;

    /** Source mode to use for alpha when separate blending */
    private int alphaSourceMode;

    /** Destination mode to use for alpha when separate blending */
    private int alphaDestMode;

    /** Flag indicating if separate RGB and Alpha functions should be set */
    private boolean useSeparatedBlend;

    /** Current state of depth testing just before this buffer modifies it */
    private boolean currentBlendState;

    /**
     * Constructs a state set with default values. Blending is disabled by
     * default.
     */
    public GeneralBufferState()
    {
        enableBlend = false;
        blendEquation = EQ_FUNC_ADD;
        rgbSourceMode = BLEND_SRC_COLOR;
        rgbDestMode = BLEND_ONE_MINUS_SRC_COLOR;

        alphaSourceMode = BLEND_SRC_ALPHA;
        alphaDestMode = BLEND_ONE_MINUS_SRC_ALPHA;

        useSeparatedBlend = false;
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
        return GENERAL_BUFFER;
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
        return 0;
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
        return false;
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
        currentBlendState = gl.glIsEnabled(GL.GL_BLEND);

        if(enableBlend != currentBlendState)
        {
            if(enableBlend)
                gl.glEnable(GL.GL_BLEND);
            else
                gl.glDisable(GL.GL_BLEND);
        }


        gl.glBlendEquation(blendEquation);
        if(useSeparatedBlend)
        {
            gl.glBlendFuncSeparate(rgbSourceMode,
                                   rgbDestMode,
                                   alphaSourceMode,
                                   alphaDestMode);
        }
        else
        {
            gl.glBlendFunc(rgbSourceMode, rgbDestMode);
        }
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
        if(enableBlend)
            gl.glEnable(GL.GL_BLEND);
        else
            gl.glDisable(GL.GL_BLEND);

        gl.glBlendEquation(blendEquation);

        if(useSeparatedBlend)
        {
            gl.glBlendFuncSeparate(rgbSourceMode,
                                   rgbDestMode,
                                   alphaSourceMode,
                                   alphaDestMode);
        }
        else
        {
            gl.glBlendFunc(rgbSourceMode, rgbDestMode);
        }
    }

    /**
     * Restore all state to the default values.
     *
     * @param gl The gl context to draw with
     */
    @Override
    public void clearBufferState(GL2 gl)
    {
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendEquation(EQ_FUNC_ADD);

        if(useSeparatedBlend)
        {
            gl.glBlendFuncSeparate(BLEND_SRC_COLOR,
                                   BLEND_ONE_MINUS_SRC_COLOR,
                                   BLEND_SRC_ALPHA,
                                   BLEND_ONE_MINUS_SRC_ALPHA);
        }
        else
        {
            gl.glBlendFunc(BLEND_SRC_ALPHA, BLEND_ONE_MINUS_SRC_ALPHA);
        }
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
        GeneralBufferState gbs = (GeneralBufferState)o;
        return compareTo(gbs);
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
        if(!(o instanceof GeneralBufferState))
            return false;
        else
            return equals((GeneralBufferState)o);

    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Set the flag for whether the blending should be enabled or disabled.
     *
     * @param test True if testing should be enabled
     *   state to be used in the current operation
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void enableBlending(boolean test)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        enableBlend = test;
    }

    /**
     * Check to see if depth testing is currently enabled.
     *
     * @return true when testing is enabled
     */
    public boolean isBlendingEnabled()
    {
        return enableBlend;
    }

    /**
     * Set the source blend factor to use. Used for both non-separated blending
     * and the RGB component when separated.
     *
     * @param factor The value to set for the factor
     * @throws IllegalArgumentException The blend factor is not one of the
     *   permitted types
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setSourceBlendFactor(int factor)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        // Check for valid constant
        switch(factor)
        {
            case BLEND_ZERO:
            case BLEND_ONE:
            case BLEND_SRC_COLOR:
            case BLEND_ONE_MINUS_SRC_COLOR:
            case BLEND_DEST_COLOR:
            case BLEND_ONE_MINUS_DEST_COLOR:
            case BLEND_SRC_ALPHA:
            case BLEND_ONE_MINUS_SRC_ALPHA:
            case BLEND_DEST_ALPHA:
            case BLEND_ONE_MINUS_DEST_ALPHA:
            case BLEND_CONSTANT_COLOR:
            case BLEND_ONE_MINUS_CONSTANT_COLOR:
            case BLEND_CONSTANT_ALPHA:
            case BLEND_ONE_MINUS_CONSTANT_ALPHA:
            case BLEND_SRC_ALPHA_SATURATE:
                break;

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(INVALID_BLEND_PROP) + factor;
                throw new IllegalArgumentException(msg);
        }

        rgbSourceMode = factor;
    }

    /**
     * Request the currently set source blend factor.
     *
     * @return One of the BLEND_* constant values
     */
    public int getSourceBlendFactor()
    {
        return rgbSourceMode;
    }

    /**
     * Set the destination blend factor to use. Used for both non-separated
     * blending and the RGB component when separated.
     *
     * @param factor The value to set for the factor
     * @throws IllegalArgumentException The blend factor is not one of the
     *   permitted types or is SRC_ALPHA_SATURATED
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setDestinationBlendFactor(int factor)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        // Check for valid constant
        switch(factor)
        {
            case BLEND_ZERO:
            case BLEND_ONE:
            case BLEND_SRC_COLOR:
            case BLEND_ONE_MINUS_SRC_COLOR:
            case BLEND_DEST_COLOR:
            case BLEND_ONE_MINUS_DEST_COLOR:
            case BLEND_SRC_ALPHA:
            case BLEND_ONE_MINUS_SRC_ALPHA:
            case BLEND_DEST_ALPHA:
            case BLEND_ONE_MINUS_DEST_ALPHA:
            case BLEND_CONSTANT_COLOR:
            case BLEND_ONE_MINUS_CONSTANT_COLOR:
            case BLEND_CONSTANT_ALPHA:
            case BLEND_ONE_MINUS_CONSTANT_ALPHA:
            case BLEND_SRC_ALPHA_SATURATE:
                break;

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(INVALID_BLEND_PROP) + factor;
                throw new IllegalArgumentException(msg);
        }

        if(factor == BLEND_SRC_ALPHA_SATURATE)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(DEST_ALPHA_PROP) + factor;
            throw new IllegalArgumentException(msg);
        }

        rgbDestMode = factor;
    }

    /**
     * Request the currently set destination blend factor.
     *
     * @return One of the BLEND_* constant values
     */
    public int getDestinationBlendFactor()
    {
        return rgbDestMode;
    }

    /**
     * Set the source blend factor to use. Only used for seperated blending
     * mode's alpha component. If non-separated mode is used, this setting is
     * ignored.
     *
     * @param factor The value to set for the factor
     * @throws IllegalArgumentException The blend factor is not one of the
     *   permitted types
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setAlphaSourceBlendFactor(int factor)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        // Check for valid constant
        switch(factor)
        {
            case BLEND_ZERO:
            case BLEND_ONE:
            case BLEND_SRC_COLOR:
            case BLEND_ONE_MINUS_SRC_COLOR:
            case BLEND_DEST_COLOR:
            case BLEND_ONE_MINUS_DEST_COLOR:
            case BLEND_SRC_ALPHA:
            case BLEND_ONE_MINUS_SRC_ALPHA:
            case BLEND_DEST_ALPHA:
            case BLEND_ONE_MINUS_DEST_ALPHA:
            case BLEND_CONSTANT_COLOR:
            case BLEND_ONE_MINUS_CONSTANT_COLOR:
            case BLEND_CONSTANT_ALPHA:
            case BLEND_ONE_MINUS_CONSTANT_ALPHA:
            case BLEND_SRC_ALPHA_SATURATE:
                break;

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(INVALID_BLEND_PROP) + factor;
                throw new IllegalArgumentException(msg);
        }

        alphaSourceMode = factor;
    }

    /**
     * Request the currently set source blend factor for the alpha component.
     *
     * @return One of the BLEND_* constant values
     */
    public int getAlphaSourceBlendFactor()
    {
        return rgbSourceMode;
    }

    /**
     * Set the destination blend factor to use. Only used for seperated blending
     * mode's alpha component. If non-separated mode is used, this setting is
     * ignored.
     *
     * @param factor The value to set for the factor
     * @throws IllegalArgumentException The blend factor is not one of the
     *   permitted types or is SRC_ALPHA_SATURATED
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setAlphaDestinationBlendFactor(int factor)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        // Check for valid constant
        switch(factor)
        {
            case BLEND_ZERO:
            case BLEND_ONE:
            case BLEND_SRC_COLOR:
            case BLEND_ONE_MINUS_SRC_COLOR:
            case BLEND_DEST_COLOR:
            case BLEND_ONE_MINUS_DEST_COLOR:
            case BLEND_SRC_ALPHA:
            case BLEND_ONE_MINUS_SRC_ALPHA:
            case BLEND_DEST_ALPHA:
            case BLEND_ONE_MINUS_DEST_ALPHA:
            case BLEND_CONSTANT_COLOR:
            case BLEND_ONE_MINUS_CONSTANT_COLOR:
            case BLEND_CONSTANT_ALPHA:
            case BLEND_ONE_MINUS_CONSTANT_ALPHA:
            case BLEND_SRC_ALPHA_SATURATE:
                break;

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(INVALID_BLEND_PROP) + factor;
                throw new IllegalArgumentException(msg);
        }

        if(factor == BLEND_SRC_ALPHA_SATURATE)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(DEST_ALPHA_PROP) + factor;
            throw new IllegalArgumentException(msg);
        }


        alphaDestMode = factor;
    }

    /**
     * Request the currently set destination blend factor for the alpha
     * component.
     *
     * @return One of the BLEND_* constant values
     */
    public int getAlphaDestinationBlendFactor()
    {
        return alphaDestMode;
    }

    /**
     * Instruct the system whether to use separated RGB and Alpha blending
     * functions. By default this is not enabled, but may be turned on or off
     * using this method.
     *
     * @param state True to enable the use of separate RGB and alpha blending
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setSeparatedBlendFactors(boolean state)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        useSeparatedBlend = state;
    }

    /**
     * Set the blend equation to use. By default, after multiplying the source
     * color by the source blend factor and the destination color by the destination
     * blend factor, the two values are then added together to get the final color.
     * If you want to use other operation besides addition, this function needs to
     * be called.  Available blending equations and their descriptions are listed below.
     *
     * <p>
     *
     * EQ_FUNC_ADD: Set the blending equation to be C<sub>s</sub>S + C<sub>d</sub>D
     * EQ_FUNC_SUBTRACT: Set the blending equation to be C<sub>s</sub>S - C<sub>d</sub>D
     * EQ_FUNC_SUBTRACT_REVERSE: Set the blending equation to be C<sub>d</sub>D - C<sub>s</sub>S
     * EQ_FUNC_MIN: Set the blending equation to be min(C<sub>d</sub>D, C<sub>s</sub>S)
     * EQ_FUNC_MAX: Set the blending equation to be max(C<sub>d</sub>D, C<sub>s</sub>S)
     *
     * @param mode Blending equation mode
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setBlendEquation(int mode)
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        // Check for valid constant
        switch(mode)
        {
            case EQ_FUNC_ADD:
            case EQ_FUNC_SUBTRACT:
            case EQ_FUNC_SUBTRACT_REVERSE:
            case EQ_FUNC_MIN:
            case EQ_FUNC_MAX:
                break;

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(INVALID_MODE_PROP) + mode;
                throw new IllegalArgumentException(msg);
        }

        blendEquation = mode;
    }

    /**
     * Request the currently set blend equation
     *
     * @return Blend equation value
     */
    public int getBlendEquation()
    {
        return blendEquation;
    }

    /**
     * Check to see the current state of whether separated blending is used or
     * not.
     *
     * @return true if separated RGB and Alpha blending is used
     */
    public boolean getSeparatedBlendFactors()
    {
        return useSeparatedBlend;
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param gbs The attributes instance to be comgbsred
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(GeneralBufferState gbs)
    {
        if(gbs == null)
            return 1;

        if(gbs == this)
            return 0;

        if(enableBlend != gbs.enableBlend)
            return enableBlend ? 1 : -1;

        if(useSeparatedBlend != gbs.useSeparatedBlend)
            return useSeparatedBlend ? 1 : -1;

        if(blendEquation != gbs.blendEquation)
            return blendEquation < gbs.blendEquation ? 1 : -1;

        if(rgbSourceMode != gbs.rgbSourceMode)
            return rgbSourceMode < gbs.rgbSourceMode ? -1 : 1;

        if(rgbDestMode != gbs.rgbDestMode)
            return rgbDestMode < gbs.rgbDestMode ? -1 : 1;

        if(alphaSourceMode != gbs.alphaSourceMode)
            return alphaSourceMode < gbs.alphaSourceMode ? -1 : 1;

        if(alphaDestMode != gbs.alphaDestMode)
            return alphaDestMode < gbs.alphaDestMode ? -1 : 1;

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param gbs The attributes instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(GeneralBufferState gbs)
    {
        if(gbs == this)
            return true;

        if((gbs == null) ||
           (enableBlend != gbs.enableBlend) ||
           (useSeparatedBlend != gbs.useSeparatedBlend) ||
           (blendEquation != gbs.blendEquation) ||
           (rgbSourceMode != gbs.rgbSourceMode) ||
           (rgbDestMode != gbs.rgbDestMode) ||
           (alphaSourceMode != gbs.alphaSourceMode) ||
           (alphaDestMode != gbs.alphaDestMode))
            return false;

        return true;
    }
}
