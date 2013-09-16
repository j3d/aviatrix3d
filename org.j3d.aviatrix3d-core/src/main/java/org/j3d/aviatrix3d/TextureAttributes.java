/*****************************************************************************
 *                   Yumetech, Inc Copyright (c) 2004 - 2006
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
import org.j3d.aviatrix3d.rendering.ObjectRenderable;

/**
 * Describes how a texture gets applied to the underlying geometry.
 * <p>
 *
 * All of these attributes are part of the texture object
 * so we only issue OGL commands when they change.  This will
 * update the Texture Object's values for all future uses.
 * <p>
 *
 * If point sprites are available, this class can be used to enable or
 * disable their use. Note that to use them completely, you also need to enable
 * the equivalent state with
 * {@link PointAttributes#setPointSpriteEnabled(boolean)}
 * <p>
 *
 * When providing texture unit identfiers for the source for the
 * combiner functions, not all the constants are defined. Since OpenGL
 * uses sequential numbering, if you need anything greater than 8, you
 * can specify the value buy making the call with
 * <i>SOURCE_TEXUTRE_0 + n</i> where n is the texture unit ID that you
 * want to use.
 * <p>
 * The default values for this class are:
 * <pre>
 * Texture Mode: GL.GL_REPLACE
 * RGB Scale: 1
 * Alpha Scale: 1
 *
 * RGB CombineMode: COMBINE_MODULATE
 * Alpha CombineMode: COMBINE_MODULATE
 *
 * RGB Source0: SOURCE_CURRENT_TEXTURE
 * RGB Source1: SOURCE_PREVIOUS_UNIT
 * RGB Source2: SOURCE_CONSTANT_COLOR
 *
 * Alpha Source0: SOURCE_CURRENT_TEXTURE
 * Alpha Source1: SOURCE_PREVIOUS_UNIT
 * Alpha Source2: SOURCE_CONSTANT_COLOR
 *
 * RGB Operand0: SRC_COLOR
 * RGB Operand1: SRC_COLOR
 * RGB Operand2: SRC_ALPHA
 *
 * Alpha Operand0: SRC_ALPHA
 * Alpha Operand1: SRC_ALPHA
 * Alpha Operand2: SRC_ALPHA
 * </pre>
 *
 * Point sprites are disabled by default.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>noPointSpritesMsg: Point sprite extension is not found on the card</li>
 * <li>invalidOperationIndexMsg: The operation index for the various combine
 *     functions is invalid</li>
 * </ul>
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.28 $
 */
public class TextureAttributes extends NodeComponent
    implements ObjectRenderable
{
    /** Message when we cannot handle point sprites */
    private static final String NO_POINT_SPRITE_PROP =
        "org.j3d.aviatrix3d.TextureAttributes.noPointSpritesMsg";

    /**
     * Message for an invalid index being provided to the setCombineSource
     * method.
     */
    private static final String INVALID_OP_IDX_PROP =
        "org.j3d.aviatrix3d.TextureAttributes.invalidOperationIndexMsg";

    /** Set the mode for applying textures to objects to GL_REPLACE */
    public static final int MODE_REPLACE = GL.GL_REPLACE;

    /** Set the mode for applying textures to objects to GL_MODULATE */
    public static final int MODE_MODULATE = GL.GL_MODULATE;

    /** Set the mode for applying textures to objects to GL_BLEND */
    public static final int MODE_BLEND = GL.GL_BLEND;

    /** Set the mode for applying textures to objects to GL_DECAL */
    public static final int MODE_DECAL = GL.GL_DECAL;

    /** Set the mode for applying textures to objects to GL_ADD */
    public static final int MODE_ADD = GL.GL_ADD;

    /** Set the mode for applying textures to objects to GL_COMBINE */
    public static final int MODE_COMBINE = GL.GL_COMBINE;

    /** Set combine mode for applying textures to objects to GL_REPLACE */
    public static final int COMBINE_REPLACE = GL.GL_REPLACE;

    /** Set combine mode for applying textures to objects to GL_MODULATE */
    public static final int COMBINE_MODULATE = GL.GL_MODULATE;

    /** Set combine mode for applying textures to objects to GL_ADD */
    public static final int COMBINE_ADD = GL.GL_ADD;

    /** Set combine mode for applying textures to objects to GL_ADD_SIGNED */
    public static final int COMBINE_ADD_SIGNED = GL.GL_ADD_SIGNED;

    /** Set combine mode for applying textures to objects to GL_INTERPOLATE */
    public static final int COMBINE_INTERPOLATE = GL.GL_INTERPOLATE;

    /** Set combine mode for applying textures to objects to GL_SUBTRACT */
    public static final int COMBINE_SUBTRACT = GL.GL_SUBTRACT;

    /** Set combine mode for applying textures to objects to GL_DOT3_RGB */
    public static final int COMBINE_DOT3_RGB = GL.GL_DOT3_RGB;

    /** Set combine mode for applying textures to objects to GL_DOT3_RGBA */
    public static final int COMBINE_DOT3_RGBA = GL.GL_DOT3_RGBA;

    /** The source is the current texture stage */
    public static final int SOURCE_CURRENT_TEXTURE = GL.GL_TEXTURE;

    /** The source is texture stage 0 */
    public static final int SOURCE_TEXTURE_0 = GL.GL_TEXTURE0;

    /** The source is texture stage 1 */
    public static final int SOURCE_TEXTURE_1 = GL.GL_TEXTURE1;

    /** The source is texture stage 2 */
    public static final int SOURCE_TEXTURE_2 = GL.GL_TEXTURE2;

    /** The source is texture stage 3 */
    public static final int SOURCE_TEXTURE_3 = GL.GL_TEXTURE3;

    /** The source is texture stage 4 */
    public static final int SOURCE_TEXTURE_4 = GL.GL_TEXTURE4;

    /** The source is texture stage 5 */
    public static final int SOURCE_TEXTURE_5 = GL.GL_TEXTURE5;

    /** The source is texture stage 6 */
    public static final int SOURCE_TEXTURE_6 = GL.GL_TEXTURE6;

    /** The source is texture stage 7 */
    public static final int SOURCE_TEXTURE_7 = GL.GL_TEXTURE7;

    /** The source is the provided texture blend colour */
    public static final int SOURCE_CONSTANT_COLOR = GL.GL_CONSTANT;

    /**
     * The source is the base colour of the object before texturing has
     * been applied.
     */
    public static final int SOURCE_BASE_COLOR = GL.GL_PRIMARY_COLOR;

    /** The source is the output of the previous texture unit */
    public static final int SOURCE_PREVIOUS_UNIT = GL.GL_PREVIOUS;

    /** Use the source colour for the incoming operation */
    public static final int SRC_COLOR = GL.GL_SRC_COLOR;

    /** Use one minus source colour for the incoming operation */
    public static final int ONE_MINUS_SRC_COLOR = GL.GL_ONE_MINUS_SRC_COLOR;

    /** Use the source alpha for the incoming operation */
    public static final int SRC_ALPHA = GL.GL_SRC_ALPHA;

    /** Use one minus source alpha for the incoming operation */
    public static final int ONE_MINUS_SRC_ALPHA = GL.GL_ONE_MINUS_SRC_ALPHA;


    /**
     * Global flag to know if we are capable of rendering point sprites.
     * This gets queried on the first time rendering is run and set
     * appropriately.
     */
    private static boolean hasPointSpriteAPI;

    /** Flag to say we've queried for the multitexture API capabilities */
    private static boolean queryComplete;


    /** blend colour if blend mode is used. Not allocated unless needed */
    private float[] blendColor;

    /** The texturing mode */
    private int texMode;

    /** RGB combine mode */
    private int rgbCombineMode;

    /** alpha combine mode */
    private int alphaCombineMode;

    /** The scale factor for rgb colours */
    private float rgbScale;

    /** The scale factor for alpha colours */
    private float alphaScale;

    // NOTE:
    // We may want to change the design here to use an array that is
    // only created when using texture combiners. That saves memory for
    // the majority of cases where we don't use them.

    /** The operand to use for the RGB mode */
    private int rgbOperand0;

    /** The operand to use for the RGB mode */
    private int rgbOperand1;

    /** The operand to use for the RGB mode */
    private int rgbOperand2;

    /** The operand to use for the alpha mode */
    private int alphaOperand0;

    /** The operand to use for the alpha mode */
    private int alphaOperand1;

    /** The operand to use for the alpha mode */
    private int alphaOperand2;

    /** The source for RGB arg0 */
    private int rgbSource0;

    /** The source for RGB arg1 */
    private int rgbSource1;

    /** The source for RGB arg2 */
    private int rgbSource2;

    /** The source for alpha arg0 */
    private int alphaSource0;

    /** The source for alpha arg1 */
    private int alphaSource1;

    /** The source for alpha arg2 */
    private int alphaSource2;

    /**
     * General flag indicating that the current setup requires the blend
     * colour to be passed through to OpenGL during the rendering.
     */
    private boolean needBlendColor;

    /** Enable or disable point sprite coordinates, if available */
    private boolean enablePointSprites;

    /**
     * Constructs a Texture Unit with default values.
     */
    public TextureAttributes()
    {
        // Sang:
        // By default opengl uses modulate mode.
        texMode = GL.GL_MODULATE;
        needBlendColor = false;
        enablePointSprites = false;

        rgbScale = 1;
        alphaScale = 1;

        rgbCombineMode = COMBINE_MODULATE;
        alphaCombineMode = COMBINE_MODULATE;

        rgbSource0 = SOURCE_CURRENT_TEXTURE;
        rgbSource1 = SOURCE_PREVIOUS_UNIT;
        rgbSource2 = SOURCE_CONSTANT_COLOR;

        alphaSource0 = SOURCE_CURRENT_TEXTURE;
        alphaSource1 = SOURCE_PREVIOUS_UNIT;
        alphaSource2 = SOURCE_CONSTANT_COLOR;

        rgbOperand0 = SRC_COLOR;
        rgbOperand1 = SRC_COLOR;
        rgbOperand2 = SRC_ALPHA;

        alphaOperand0 = SRC_ALPHA;
        alphaOperand1 = SRC_ALPHA;
        alphaOperand2 = SRC_ALPHA;
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
        if(!queryComplete)
        {
            hasPointSpriteAPI = gl.isExtensionAvailable("GL_ARB_point_sprite");
            queryComplete = true;

            if(!hasPointSpriteAPI)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(NO_POINT_SPRITE_PROP);
                System.out.println(msg);
            }
        }

        gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, texMode);

        if(hasPointSpriteAPI && enablePointSprites)
            gl.glTexEnvf(GL.GL_POINT_SPRITE_ARB,
                         GL.GL_COORD_REPLACE_ARB,
                         GL.GL_TRUE);

        if(needBlendColor && blendColor != null)
        {
            gl.glTexEnvfv(GL.GL_TEXTURE_ENV,
                          GL.GL_TEXTURE_ENV_COLOR,
                          blendColor,
                          0);
        }

        if(texMode == MODE_COMBINE)
        {
            gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                         GL.GL_COMBINE_RGB,
                         rgbCombineMode);

            // RGB first.
            switch(rgbCombineMode)
            {
                case COMBINE_REPLACE:
                    gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                                 GL.GL_SOURCE0_RGB,
                                 rgbSource0);
                    gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                                 GL.GL_OPERAND0_RGB,
                                 rgbOperand0);
                    break;

                case COMBINE_MODULATE:
                case COMBINE_ADD:
                case COMBINE_ADD_SIGNED:
                case COMBINE_SUBTRACT:

                    gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                                 GL.GL_SOURCE0_RGB,
                                 rgbSource0);
                    gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                                 GL.GL_OPERAND0_RGB,
                                 rgbOperand0);
                    gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                                 GL.GL_SOURCE1_RGB,
                                 rgbSource1);
                    gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                                 GL.GL_OPERAND1_RGB,
                                 rgbOperand1);
                    break;

                case COMBINE_INTERPOLATE:
                    gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                                 GL.GL_SOURCE0_RGB,
                                 rgbSource0);
                    gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                                 GL.GL_OPERAND0_RGB,
                                 rgbOperand0);
                    gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                                 GL.GL_SOURCE1_RGB,
                                 rgbSource1);
                    gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                                 GL.GL_OPERAND1_RGB,
                                 rgbOperand1);
                    gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                                 GL.GL_SOURCE2_RGB,
                                 rgbSource2);
                    gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                                 GL.GL_OPERAND2_RGB,
                                 rgbOperand2);
                    break;

                case COMBINE_DOT3_RGB:
                case COMBINE_DOT3_RGBA:
                    gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                                 GL.GL_SOURCE0_RGB,
                                 rgbSource0);
                    gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                                 GL.GL_OPERAND0_RGB,
                                 rgbOperand0);
                    gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                                 GL.GL_SOURCE1_RGB,
                                 rgbSource1);
                    gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                                 GL.GL_OPERAND1_RGB,
                                 rgbOperand1);
                    break;

            }

            gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                         GL.GL_COMBINE_ALPHA,
                         alphaCombineMode);

            // Now alpha.
            switch(alphaCombineMode)
            {
                case COMBINE_REPLACE:
                    gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                                 GL.GL_SOURCE0_ALPHA,
                                 alphaSource0);
                    gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                                 GL.GL_OPERAND0_ALPHA,
                                 alphaOperand0);
                    break;

                case COMBINE_MODULATE:
                case COMBINE_ADD:
                case COMBINE_ADD_SIGNED:
                case COMBINE_SUBTRACT:
                    gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                                 GL.GL_SOURCE0_ALPHA,
                                 alphaSource0);
                    gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                                 GL.GL_OPERAND0_ALPHA,
                                 alphaOperand0);
                    gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                                 GL.GL_SOURCE1_ALPHA,
                                 alphaSource1);
                    gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                                 GL.GL_OPERAND1_ALPHA,
                                 alphaOperand1);
                    break;

                case COMBINE_INTERPOLATE:
                    gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                                 GL.GL_SOURCE0_ALPHA,
                                 alphaSource0);
                    gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                                 GL.GL_OPERAND0_ALPHA,
                                 alphaOperand0);
                    gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                                 GL.GL_SOURCE1_ALPHA,
                                 alphaSource1);
                    gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                                 GL.GL_OPERAND1_ALPHA,
                                 alphaOperand1);
                    gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                                 GL.GL_SOURCE1_ALPHA,
                                 alphaSource2);
                    gl.glTexEnvf(GL.GL_TEXTURE_ENV,
                                 GL.GL_OPERAND2_ALPHA,
                                 alphaOperand2);
                    break;
            }

            gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_RGB_SCALE, rgbScale);
            gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_ALPHA_SCALE, alphaScale);
        }
    }

    /**
     * Restore all openGL state to the given drawable.
     *
     * @param gl The gl context to draw with
     */
    public void postRender(GL gl)
    {
        // State restoration will be handled by the TextureUnit
        if(hasPointSpriteAPI && enablePointSprites)
            gl.glTexEnvf(GL.GL_POINT_SPRITE_ARB,
                         GL.GL_COORD_REPLACE_ARB,
                         GL.GL_FALSE);

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
        TextureAttributes ta = (TextureAttributes)o;
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
        if(!(o instanceof TextureAttributes))
            return false;
        else
            return equals((TextureAttributes)o);

    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Set the texture mode. If set to blend mode, and no colour has
     * been provided yet, it defaults to all white.
     *
     * @param mode The new mode
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setTextureMode(int mode)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        texMode = mode;

        if((texMode == MODE_BLEND) && (blendColor == null))
        {
            needBlendColor = true;

            blendColor = new float[4];
            blendColor[0] = 1;
            blendColor[1] = 1;
            blendColor[2] = 1;
            blendColor[3] = 1;
        }
        else
        {
            needBlendColor = false;
        }
    }

    /**
     * Get the current texture mode in use.
     *
     * @return one of the MODE_* values
     */
    public int getTextureMode()
    {
        return texMode;
    }

    /**
     * Set the blend colour to use for this texture. Blend is a 4-component
     * colour value.
     *
     * @param r The red component of the blend colour
     * @param g The green component of the blend colour
     * @param b The blue component of the blend colour
     * @param a The alpha component of the blend colour
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setBlendColor(float r, float g, float b, float a)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(blendColor == null)
            blendColor = new float[4];

        blendColor[0] = r;
        blendColor[1] = g;
        blendColor[2] = b;
        blendColor[3] = a;
    }

    /**
     * Get the current blend colour. If it hasn't been set previously, the
     * call is ignored.
     *
     * @param col An array to copy the colour in, in RGBA format
     */
    public void getBlendColor(float[] col)
    {
        if(blendColor == null)
            return;

        col[0] = blendColor[0];
        col[1] = blendColor[1];
        col[2] = blendColor[2];
        col[3] = blendColor[3];
    }

    /**
     * Set the combine mode for the alpha or RGB side.
     *
     * @param alpha True if this is setting the combine mode for the alpha
     *    channel. False for the RGB channels
     * @param mode one of the valid COMBINE_* mode types
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setCombineMode(boolean alpha, int mode)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(alpha)
        {
            // need to do validity checking here
            alphaCombineMode = mode;
        }
        else
        {
            // need to do validity checking here
            rgbCombineMode = mode;
        }
    }

    /**
     * Get the combine mode in use. If the attributes are set up to not
     * use texture combiners, this will return the last set value.
     *
     * @param alpha true when this should fetch the alpha setting, false for
     *    the RGB setting
     * @return one of the valid COMBINE_* mode types
     */
    public int getCombineMode(boolean alpha)
    {
        return alpha ? alphaCombineMode : rgbCombineMode;
    }

    /**
     * Set the combine scale factor.
     *
     * @param alpha Scale the alpha channel (true) Or the Rgb channel
     * @param scale The amount to scale the channel by
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setCombineScale(boolean alpha, float scale)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if (alpha)
            alphaScale = scale;
        else
            rgbScale = scale;
    }

    /**
     * Get the combine scale factor.
     *
     * @param alpha True if to return the the alpha channel, false for RGB
     * @return The amount to scale the channel by
     */
    public float getCombineScale(boolean alpha)
    {
        return alpha ? alphaScale : rgbScale;
    }

    /**
     * Set the operand to use for alpha or RGB combine mode.
     *
     * @param alpha true when this should set the alpha setting, false for
     *    the RGB setting
     * @param opIdx A value of 0, 1 or 2 indicating which operand argument
     *    this should effect
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setCombineOperand(boolean alpha, int opIdx, int function)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(alpha)
        {
            // need to do validity checking here
            switch(opIdx)
            {
                case 0:
                    alphaOperand0 = function;
                    break;

                case 1:
                    alphaOperand1 = function;
                    break;

                case 2:
                    alphaOperand2 = function;
                    break;

                default:
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg = intl_mgr.getString(INVALID_OP_IDX_PROP) + opIdx;
                    throw new IllegalArgumentException(msg);
            }
        }
        else
        {
            switch(opIdx)
            {
                case 0:
                    rgbOperand0 = function;
                    break;

                case 1:
                    rgbOperand1 = function;
                    break;

                case 2:
                    rgbOperand2 = function;
                    break;

                default:
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg = intl_mgr.getString(INVALID_OP_IDX_PROP) + opIdx;
                    throw new IllegalArgumentException(msg);
            }
        }
    }

    /**
     * Get the current value of the combine operand in use.
     *
     * @param alpha true when this should fetch the alpha setting, false for
     *    the RGB setting
     * @param opIdx A value of 0, 1 or 2 indicating which operand argument
     *    this should fetch
     * @return one of the valid COMBINE_* mode types
     */
    public int getCombineOperand(boolean alpha, int opIdx)
    {
        int ret_val = 0;

        if(alpha)
        {
            // need to do validity checking here
            switch(opIdx)
            {
                case 0:
                    ret_val = alphaOperand0;
                    break;

                case 1:
                    ret_val = alphaOperand1;
                    break;

                case 2:
                    ret_val = alphaOperand2;
                    break;

                default:
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg = intl_mgr.getString(INVALID_OP_IDX_PROP) + opIdx;
                    throw new IllegalArgumentException(msg);
            }
        }
        else
        {
            switch(opIdx)
            {
                case 0:
                    ret_val = rgbOperand0;
                    break;

                case 1:
                    ret_val = rgbOperand1;
                    break;

                case 2:
                    ret_val = rgbOperand2;
                    break;

                default:
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg = intl_mgr.getString(INVALID_OP_IDX_PROP) + opIdx;
                    throw new IllegalArgumentException(msg);
            }
        }

        return ret_val;
    }

    /**
     * Set the combine source type for alpha or RGB combine mode.
     *
     * @param alpha true when this should set the alpha setting, false for
     *    the RGB setting
     * @param opIdx A value of 0, 1 or 2 indicating which operand argument
     *    this should effect
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setCombineSource(boolean alpha, int opIdx, int source)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(alpha)
        {
            switch(opIdx)
            {
                case 0:
                    alphaSource0 = source;
                    break;

                case 1:
                    alphaSource1 = source;
                    break;

                case 2:
                    alphaSource2 = source;
                    break;

                default:
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg = intl_mgr.getString(INVALID_OP_IDX_PROP) + opIdx;
                    throw new IllegalArgumentException(msg);
            }
        }
        else
        {
            switch(opIdx)
            {
                case 0:
                    rgbSource0 = source;
                    break;

                case 1:
                    rgbSource1 = source;
                    break;

                case 2:
                    rgbSource2 = source;
                    break;

                default:
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg = intl_mgr.getString(INVALID_OP_IDX_PROP) + opIdx;
                    throw new IllegalArgumentException(msg);
            }
        }

       needBlendColor = ((texMode == MODE_COMBINE) &&
                          ((rgbSource0 == SOURCE_CONSTANT_COLOR) ||
                           (rgbSource1 == SOURCE_CONSTANT_COLOR) ||
                           (rgbSource2 == SOURCE_CONSTANT_COLOR) ||
                           (alphaSource0 == SOURCE_CONSTANT_COLOR) ||
                           (alphaSource1 == SOURCE_CONSTANT_COLOR) ||
                           (alphaSource2 == SOURCE_CONSTANT_COLOR))) ||
                         (texMode == MODE_BLEND);
    }

    /**
     * Get the current value of the combine source function in use
     *
     * @param alpha true when this should fetch the alpha setting, false for
     *    the RGB setting
     * @param opIdx A value of 0, 1 or 2 indicating which operand argument
     *    this should fetch
     * @return one of the valid SRC_* types
     */
    public int getCombineSource(boolean alpha, int opIdx)
    {
        int ret_val = 0;

        if(alpha)
        {
            // need to do validity checking here
            switch(opIdx)
            {
                case 0:
                    ret_val = alphaSource0;
                    break;

                case 1:
                    ret_val = alphaSource1;
                    break;

                case 2:
                    ret_val = alphaSource2;
                    break;

                default:
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg = intl_mgr.getString(INVALID_OP_IDX_PROP) + opIdx;
                    throw new IllegalArgumentException(msg);
            }
        }
        else
        {
            switch(opIdx)
            {
                case 0:
                    ret_val = rgbSource0;
                    break;

                case 1:
                    ret_val = rgbSource1;
                    break;

                case 2:
                    ret_val = rgbSource2;
                    break;

                default:
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg = intl_mgr.getString(INVALID_OP_IDX_PROP) + opIdx;
                    throw new IllegalArgumentException(msg);
            }
        }

        return ret_val;
    }

    /**
     * Set the point sprite coordinate enabled flag. By default this is
     * disabled. Note that to use them completely, you also need to enable
     * the equivalent state with
     * {@link PointAttributes#setPointSpriteEnabled(boolean)}
     *
     * @param state true if this texture unit should enable point sprite
     *   coordinates. False to disable
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setPointSpriteCoordEnabled(boolean state)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        enablePointSprites = state;
    }

    /**
     * Get the current state of the whether the point sprites coordinates
     * are enabled.
     *
     * @return true if the point sprite coordinates are enabled
     */
    public boolean isPointSpriteCoordEnabled()
    {
        return enablePointSprites;
    }

    /**
     * Convenience method to check if this code has detected the prescense of
     * multitexture extensions. If none are found, this will return null.
     * However, one node instance has to have passed through the rendering
     * cycle for this to have detected it. A better option would be to make use
     * of the appropriate callbacks on the GraphicsOutputDevice APIs to detect
     * before you get to this point.
     *
     * @return true if multitexture is allowed
     */
    public boolean isPointSpriteAllowed()
    {
        return hasPointSpriteAPI;
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param ta The attributes instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(TextureAttributes ta)
    {
        if(ta == null)
            return 1;

        if(ta == this)
            return 0;

        // This compare is horribly inefficient. Would like to do something
        // based on a precalculated hash of most of the values.

        if(texMode < ta.texMode)
            return -1;
        else if(texMode > ta.texMode)
            return 1;

        if(rgbCombineMode < ta.rgbCombineMode)
            return -1;
        else if(rgbCombineMode > ta.rgbCombineMode)
            return 1;

        if(alphaCombineMode < ta.alphaCombineMode)
            return -1;
        else if(alphaCombineMode > ta.alphaCombineMode)
            return 1;

        if(rgbScale < ta.rgbScale)
            return -1;
        else if(rgbScale > ta.rgbScale)
            return 1;

        if(alphaScale < ta.alphaScale)
            return -1;
        else if(alphaScale > ta.alphaScale)
            return 1;

        if(rgbOperand0 < ta.rgbOperand0)
            return -1;
        else if(rgbOperand0 > ta.rgbOperand0)
            return 1;

        if(alphaOperand0 < ta.alphaOperand0)
            return -1;
        else if(alphaOperand0 > ta.alphaOperand0)
            return 1;

        if(rgbOperand1 < ta.rgbOperand1)
            return -1;
        else if(rgbOperand1 > ta.rgbOperand1)
            return 1;

        if(alphaOperand1 < ta.alphaOperand1)
            return -1;
        else if(alphaOperand1 > ta.alphaOperand1)
            return 1;

         if(rgbOperand2 < ta.rgbOperand2)
            return -1;
        else if(rgbOperand2 > ta.rgbOperand2)
            return 1;

        if(alphaOperand2 < ta.alphaOperand2)
            return -1;
        else if(alphaOperand2 > ta.alphaOperand2)
            return 1;

        // Finally the source values.
        if(rgbSource0 < ta.rgbSource0)
            return -1;
        else if(rgbSource0 > ta.rgbSource0)
            return 1;

        if(alphaSource0 < ta.alphaSource0)
            return -1;
        else if(alphaSource0 > ta.alphaSource0)
            return 1;

        if(rgbSource1 < ta.rgbSource1)
            return -1;
        else if(rgbSource1 > ta.rgbSource1)
            return 1;

        if(alphaSource1 < ta.alphaSource1)
            return -1;
        else if(alphaSource1 > ta.alphaSource1)
            return 1;

        if(rgbSource2 < ta.rgbSource2)
            return -1;
        else if(rgbSource2 > ta.rgbSource2)
            return 1;

        if(alphaSource2 < ta.alphaSource2)
            return -1;
        else if(alphaSource2 > ta.alphaSource2)
            return 1;

        if(enablePointSprites != ta.enablePointSprites)
            return enablePointSprites ? 1 : -1;

        if(needBlendColor != ta.needBlendColor)
            return needBlendColor ? 1 : -1;
        else if(needBlendColor)
        {
            if(blendColor[0] < ta.blendColor[0])
                return -1;
            else if(blendColor[0] > ta.blendColor[0])
                return 1;

            if(blendColor[1] < ta.blendColor[1])
                return -1;
            else if(blendColor[1] > ta.blendColor[1])
                return 1;

            if(blendColor[2] < ta.blendColor[2])
                return -1;
            else if(blendColor[2] > ta.blendColor[2])
                return 1;

            if(blendColor[3] < ta.blendColor[3])
                return -1;
            else if(blendColor[3] > ta.blendColor[3])
                return 1;
        }

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param ta The attributes instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(TextureAttributes ta)
    {
        if(ta == this)
            return true;

        if((ta == null) ||
           (texMode != ta.texMode) ||
           (rgbCombineMode != ta.rgbCombineMode) ||
           (alphaCombineMode != ta.alphaCombineMode) ||
           (rgbScale != ta.rgbScale) ||
           (alphaScale != ta.alphaScale) ||
           (rgbOperand0 != ta.rgbOperand0) ||
           (alphaOperand0 != ta.alphaOperand0) ||
           (rgbOperand1 != ta.rgbOperand1) ||
           (alphaOperand1 != ta.alphaOperand1) ||
           (rgbOperand2 != ta.rgbOperand2) ||
           (alphaOperand2 != ta.alphaOperand2) ||
           (rgbSource0 != ta.rgbSource0) ||
           (alphaSource0 != ta.alphaSource0) ||
           (rgbSource1 != ta.rgbSource1) ||
           (alphaSource1 != ta.alphaSource1) ||
           (rgbSource2 != ta.rgbSource2) ||
           (alphaSource2 != ta.alphaSource2) ||
           (needBlendColor != ta.needBlendColor) ||
           (enablePointSprites != ta.enablePointSprites))
            return false;

        if(needBlendColor)
        {
            if((blendColor[0] != ta.blendColor[0]) ||
               (blendColor[1] != ta.blendColor[1]) ||
               (blendColor[2] != ta.blendColor[2]) ||
               (blendColor[3] != ta.blendColor[3]))
                return false;
        }

        return true;
    }

}
