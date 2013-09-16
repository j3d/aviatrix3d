/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2006
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

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.*;

/**
 * Base representation of a light source that corresponds to the base set of
 * capabilities that all lights in OpenGL have.
 * <p>
 *
 * By default a light is not enabled and the colour is set to black. Lights
 * also have a secondary specular value that can be provided. By default it
 * is black (ie has no effect). An ambient component can also be provided with
 * this light source in addition to it's derived effects.
 * <p>
 *
 * <b>Rendering Implementation Tips</b>
 * <p>
 * The external data passed to the ComponentRenderable calls shall be an
 * <code>Integer</code> instance that represents the GL identifier of the
 * Light (GL_LIGHT0 + i) this instance is working with.
 * <p>
 *
 * See the {@link EffectRenderable} interface for definition of the scoping
 * and effects bounds.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>redComponentRangeMsg: Error message when red component is not [0,1]</li>
 * <li>greenComponentRangeMsg: Error message when green component is not [0,1]</li>
 * <li>blueComponentRangeMsg: Error message when blue component is not [0,1]</li>
 * <li>alphaComponentRangeMsg: Error message when alpha component is not [0,1]</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.24 $
 */
public abstract class Light extends Leaf
    implements LeafCullable, ComponentRenderable, EffectRenderable
{
    /** Message when the drawing mode is not a valid value */
    protected static final String INVALID_RED_PROP =
        "org.j3d.aviatrix3d.Light.redComponentRangeMsg";

    /** Message when the drawing mode is not a valid value */
    protected static final String INVALID_GREEN_PROP =
        "org.j3d.aviatrix3d.Light.greenComponentRangeMsg";

    /** Message when the drawing mode is not a valid value */
    protected static final String INVALID_BLUE_PROP =
        "org.j3d.aviatrix3d.Light.blueComponentRangeMsg";

    /** Message when the drawing mode is not a valid value */
    protected static final String INVALID_ALPHA_PROP =
        "org.j3d.aviatrix3d.Light.alphaComponentRangeMsg";


    /** Internal type to describe that this is a spotlight light type */
    protected static final int SPOT_TYPE = 1;

    /** Internal type to describe that this is a point light type */
    protected static final int POINT_TYPE = 2;

    /** Internal type to describe that this is a directional light type */
    protected static final int DIRECTIONAL_TYPE = 3;

    /** Internal type to describe that this is a pure ambient light type */
    protected static final int AMBIENT_TYPE = 4;

    /** The colour of the light */
    protected float[] ambientColor;

    /** The colour of the light */
    protected float[] diffuseColor;

    /** The colour of the light */
    protected float[] specularColor;

    /** The enabled state */
    protected boolean enabled;

    /**
     * Flag indicating whether this should be a global (true) or locally-scoped
     * light (false). By default it is local only.
     */
    protected boolean globalOnly;

    /**
     * A bounding volume used to restrict the scope of what the light effects.
     * If this is set, any object that intersects with this bounds will be
     * effected by this light. Deciding what to intersect will depend on the
     * setting of the {@link #globalOnly} state. The bounds are in the local
     * coordinate system of this light.
     */
    protected BoundingVolume effectBounds;

    /** the type of light this is */
    private final int lightType;

    /**
     * Creates a light with the colour and specular colour set to black.
     *
     * @param type The type of light that this one is
     */
    protected Light(int type)
    {
        lightType = type;

        diffuseColor = new float[4];
        diffuseColor[3] = 1;

        specularColor = new float[4];
        specularColor[3] = 1;

        ambientColor = new float[4];
        ambientColor[3] = 1;

        enabled = false;
        globalOnly = false;
    }

    /**
     * Create a light with the given base diffuse colour.Colour must be in the
     * range [0, 1] otherwise an exception is generated.
     *
     * @param type The type of light that this one is
     * @param diffuse The diffuse colour value to use
     * @throws IllegalArgumentException The colour value is out of range
     */
    protected Light(int type, float[] diffuse)
        throws IllegalArgumentException
    {
        this(type);

		checkColourRange(diffuse[0], INVALID_RED_PROP);
		checkColourRange(diffuse[1], INVALID_GREEN_PROP);
		checkColourRange(diffuse[2], INVALID_BLUE_PROP);

        diffuseColor[0] = diffuse[0];
        diffuseColor[1] = diffuse[1];
        diffuseColor[2] = diffuse[2];
    }

    //---------------------------------------------------------------
    // Methods defined by LeafCullable
    //---------------------------------------------------------------

    /**
     * Get the type that this cullable represents.
     *
     * @return One of the _CULLABLE constants
     */
    public int getCullableType()
    {
        return LIGHT_CULLABLE;
    }

    /**
     * Get the child renderable of this object.
     *
     * @return an array of nodes
     */
    public Renderable getRenderable()
    {
        return this;
    }

    //----------------------------------------------------------
    // Methods defined by EffectRenderable
    //----------------------------------------------------------

    /**
     * Get the current enabled state of the light.
     *
     * @return The current state
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Get the current setting of the global-only flag.
     *
     * @return true if for global use only, false otherwise
     */
    public boolean isGlobalOnly()
    {
        return globalOnly;
    }

    /**
     * Get the current bounding volume that this light effects. If the light is
     * to effect everything for infinite distance, then this will return null.
     *
     * @return A bounding volume if there is to be bounds, null for none.
     */
    public BoundingVolume getEffectBounds()
    {
        return effectBounds;
    }

    //---------------------------------------------------------------
    // Methods defined by Comparable
    //---------------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * <p>
     *
     * Derived classes should extend this one to add the extra comparisons
     * needed.
     *
     * @param o The object to be compared
     * @return -1, 0 or 1 depending on order
     * @throws ClassCastException The specified object's type prevents it from
     *    being compared to this Object
     */
    public int compareTo(Object o)
        throws ClassCastException
    {
        Light l = (Light)o;
        return compareTo(l);
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Set the enabled state of the light. Can use this to turn it on and off
     * in a general fashion.
     *
     * @param state The new state of the light
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setEnabled(boolean state)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        enabled = state;
    }

    /**
     * Set the global scope state of the light. We can use this to turn
     * define whether this light should effect everything or just those scoped
     * to the same group as the parent of this light.
     *
     * @param state The new state of the light
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setGlobalOnly(boolean state)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        globalOnly = state;
    }

    /**
     * Set the bounds that will effect the range of this light. The bounds are
     * used as a secondary scoping capability to define which objects should or
     * should not be effected based on whether their bounding volumes intersect
     * with this volume. If the light is to effect everything then this value
     * should be set to null, otherwise it defines the bounds in the local
     * coordinate system of the light.
     *
     * @param bounds A volume to use or null to clear
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setEffectBounds(BoundingVolume bounds)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        effectBounds = bounds;
        this.bounds = bounds;
    }

    /**
     * Set the ambient colour to the new value. Colour must be in the range
     * [0, 1] otherwise an exception is generated.
     *
     * @param col The new colour value to use
     * @throws IllegalArgumentException The colour value is out of range
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setAmbientColor(float[] col)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(col[0] < 0 || col[0] > 1)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(INVALID_RED_PROP) + col[0];
            throw new IllegalArgumentException(msg);
        }

        if(col[1] < 0 || col[1] > 1)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(INVALID_GREEN_PROP) + col[1];
            throw new IllegalArgumentException(msg);
        }

        if(col[2] < 0 || col[2] > 1)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(INVALID_BLUE_PROP) + col[2];
            throw new IllegalArgumentException(msg);
        }


        ambientColor[0] = col[0];
        ambientColor[1] = col[1];
        ambientColor[2] = col[2];
    }

    /**
     * Retrieve the current colour value from the light.
     *
     * @param col An array to copy the colour value into
     */
    public void getAmbientColor(float[] col)
    {
        col[0] = ambientColor[0];
        col[1] = ambientColor[1];
        col[2] = ambientColor[2];
    }

    /**
     * Set the diffuse colour component to the new value. Colour must be in the
     * range [0, 1] otherwise an exception is generated.
     *
     * @param col The new colour value to use
     * @throws IllegalArgumentException The colour value is out of range
     */
    public void setDiffuseColor(float[] col)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(col[0] < 0 || col[0] > 1)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(INVALID_RED_PROP) + col[0];
            throw new IllegalArgumentException(msg);
        }

        if(col[1] < 0 || col[1] > 1)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(INVALID_GREEN_PROP) + col[1];
            throw new IllegalArgumentException(msg);
        }

        if(col[2] < 0 || col[2] > 1)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(INVALID_BLUE_PROP) + col[2];
            throw new IllegalArgumentException(msg);
        }


        diffuseColor[0] = col[0];
        diffuseColor[1] = col[1];
        diffuseColor[2] = col[2];
    }

    /**
     * Retrieve the current diffuse colour value from the light.
     *
     * @param col An array to copy the colour value into
     */
    public void getDiffuseColor(float[] col)
    {
        col[0] = diffuseColor[0];
        col[1] = diffuseColor[1];
        col[2] = diffuseColor[2];
    }

    /**
     * Set the colour to the new value. Colour must be in the range
     * [0, 1] otherwise an exception is generated.
     *
     * @param col The new colour value to use
     * @throws IllegalArgumentException The colour value is out of range
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setSpecularColor(float[] col)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(col[0] < 0 || col[0] > 1)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(INVALID_RED_PROP) + col[0];
            throw new IllegalArgumentException(msg);
        }

        if(col[1] < 0 || col[1] > 1)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(INVALID_GREEN_PROP) + col[1];
            throw new IllegalArgumentException(msg);
        }

        if(col[2] < 0 || col[2] > 1)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(INVALID_BLUE_PROP) + col[2];
            throw new IllegalArgumentException(msg);
        }

        specularColor[0] = col[0];
        specularColor[1] = col[1];
        specularColor[2] = col[2];
    }

    /**
     * Retrieve the current colour value from the light.
     *
     * @param col An array to copy the colour value into
     */
    public void getSpecularColor(float[] col)
    {
        col[0] = specularColor[0];
        col[1] = specularColor[1];
        col[2] = specularColor[2];
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param l The light instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(Light l)
    {
        if(l == null)
            return 1;

        if(l == this)
            return 0;

        // are we the same type of light?
        if(l.lightType != lightType)
            return lightType < l.lightType ? -1 : 1;

        if(enabled != l.enabled)
            return enabled ? 1 : -1;

        if(globalOnly != l.globalOnly)
            return globalOnly ? 1 : -1;

        int res = compareColor3(diffuseColor, l.diffuseColor);
        if(res != 0)
            return res;

        res = compareColor3(specularColor, l.specularColor);
        if(res != 0)
            return res;

        return compareColor3(ambientColor, l.ambientColor);
    }

    /**
     * Compare 2 color arrays of length 3 for equality
     *
     * @param a The first colour array to check
     * @param b The first colour array to check
     * @return -1 if a[i] < b[i], +1 if a[i] > b[i], otherwise 0
     */
    protected int compareColor3(float[] a, float[] b)
    {
        if(a[0] < b[0])
            return -1;
        else if (a[0] > b[0])
            return 1;

        if(a[1] < b[1])
            return -1;
        else if (a[1] > b[1])
            return 1;

        if(a[2] < b[2])
            return -1;
        else if (a[2] > b[2])
            return 1;

        return 0;
    }

	/**
	 * Convenience method to check the colour range. If OK, returns normally,
	 * otherwise generates an exception.
	 *
	 * @param c The colour attribute to check
	 * @param prop The name of the property to use for the error message.
	 * @throws IllegalArgumentException A colour range component is out of the
	 *   range [0,1]
	 */
	private void checkColourRange(float c, String prop)
	{
        if((c < 0) || (c > 1))
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(prop);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Float(c) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }
	}
}
