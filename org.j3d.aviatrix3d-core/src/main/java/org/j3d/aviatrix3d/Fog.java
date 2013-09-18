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
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.*;

/**
 * Describes a fog rendering effect.
 * <p>
 *
 * All types of fog are reprensented in a single class. If the parameter
 * type is not useful for the fog mode, it is ignored.
 * <p>
 *
 * Fog may act either locally or globally. If an instance of the fog node is
 * registered with the ViewEnvironment, then it shall act as a global fog
 * effect for the world. If it is not, then may act as a local effect, scoped
 * the group node that contains it (like lights). Local fog effects must be
 * explicitly enabled, otherwise only the global is used. If, during a
 * traversal from the root of the tree down to a leaf, multiple Fog node
 * instances are encountered, then only the closest to the Leaf is used. If
 * multiple instances are provided in the same Group node instance, the chosen
 * one is implementation independent.
 * <p>
 *
 * When combined with fog coordinates on the geometry, local fog provides
 * volumetric fog effects.
 * <p>
 *
 * By default, fog is not enabled and global only.
 * <p>
 *
 * <b>Rendering Implementation Tips</b>
 * <p>
 * See the {@link EffectRenderable} interface for definition of the scoping
 * and effects bounds.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>invertedRangeMsg: Error message when the caller tries to set a minimum
 *     range greater than the maximum range.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.25 $
 */
public class Fog extends Leaf
    implements LeafCullable, ObjectRenderable, EffectRenderable
{
    /** Message when the linear ranges are reversed */
    private static final String FOG_RANGE_PROP =
        "org.j3d.aviatrix3d.Fog.invertedRangeMsg";

    /** Set the fog mode to exponential curve */
    public static final int EXPONENTIAL = GL2.GL_EXP;

    /** Set the fog mode to exponential-squared curve */
    public static final int EXPONENTIAL_2 = GL2.GL_EXP2;

    /** Set the fog mode to linear */
    public static final int LINEAR = GL.GL_LINEAR;

    /** The current mode in use */
    private int mode;

    /** Density function if this is an exponential type */
    private float density;

    /** Start distance parameter for linear fog */
    private float startDistance;

    /** End (max density) distance parameter for linear fog */
    private float endDistance;

    /** Base colour of the fog */
    private float[] color;

    /** Is this a local or global fog? */
    private boolean globalOnly;

    /** The enabled state */
    private boolean enabled;

    /**
     * A bounding volume used to restrict the scope of what the light effects.
     * If this is set, any object that intersects with this bounds will be
     * effected by this light. Deciding what to intersect will depend on the
     * setting of the {@link #globalOnly} state. The bounds are in the local
     * coordinate system of this light.
     */
    private BoundingVolume effectBounds;

    /**
     * Constructs a fog node with the default mode set to linear the
     * colour set to white and global only.
     */
    public Fog()
    {
        this(LINEAR, null, true);
    }

    /**
     * Construct a fog using the given mode and with the colour set to white.
     *
     * @param mode One of LINEAR, EXPONENTIAL or EXPONENTIAL_2
     */
    public Fog(int mode)
    {
        this(mode, null, true);
    }

    /**
     * Construct a fog using the given mode and the option of selecting local
     * or global effects. The colour set to white.
     *
     * @param mode One of LINEAR, EXPONENTIAL or EXPONENTIAL_2
     * @param global true if this is a global-only fog effect
     */
    public Fog(int mode, boolean global)
    {
        this(mode, null, global);
    }

    /**
     * Construct a fog using the given mode and colour.
     *
     * @param mode One of LINEAR, EXPONENTIAL or EXPONENTIAL_2
     * @param c The initial colour to use for the fog
     */
    public Fog(int mode, float[] c)
    {
        this(mode, c, true);
    }

    /**
     * Construct a fog using the given colour and assumes a linear mode.
     *
     * @param c The initial colour to use for the fog
     */
    public Fog(float[] c)
    {
        this(LINEAR, c, true);
    }

    /**
     * Construct a fog using the given colour and choice of gloval effect,
     * assuming a linear mode.
     *
     * @param c The initial colour to use for the fog
     * @param global true if this is a global-only fog effect
     */
    public Fog(float[] c, boolean global)
    {
        this(LINEAR, c, global);
    }

    /**
     * Construct a fog using the given mode and colour and selection of global
     * state.
     *
     * @param mode One of LINEAR, EXPONENTIAL or EXPONENTIAL_2
     * @param c The initial colour to use for the fog
     * @param global true if this is a global-only fog effect
     */
    public Fog(int mode, float[] c, boolean global)
    {
        this.mode = mode;
        endDistance = 1;

        color = new float[4];

        if(c == null)
        {
            color[0] = 1;
            color[1] = 1;
            color[2] = 1;
            color[3] = 1;
        }
        else
        {
            color[0] = c[0];
            color[1] = c[1];
            color[2] = c[2];
            color[3] = 1;
        }

        globalOnly = global;
        enabled = false;
    }

    //---------------------------------------------------------------
    // Methods defined by LeafCullable
    //---------------------------------------------------------------

    /**
     * Get the type that this cullable represents.
     *
     * @return One of the _CULLABLE constants
     */
    @Override
    public int getCullableType()
    {
        return FOG_CULLABLE;
    }

    /**
     * Get the child renderable of this object.
     *
     * @return an array of nodes
     */
    @Override
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
    @Override
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Get the current setting of the global-only flag.
     *
     * @return true if for global use only, false otherwise
     */
    @Override
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
    @Override
    public BoundingVolume getEffectBounds()
    {
        return effectBounds;
    }

    //----------------------------------------------------------
    // Methods defined by ObjectRenderable
    //----------------------------------------------------------

    /**
     * Issue ogl commands needed for this component
     *
     * @param gl The gl context to draw with
     */
    @Override
    public void render(GL2 gl)
    {
        gl.glFogi(GL2.GL_FOG_MODE, mode);
        gl.glFogfv(GL2.GL_FOG_COLOR, color, 0);

        switch(mode)
        {
            case LINEAR:
                gl.glFogf(GL2.GL_FOG_START, startDistance);
                gl.glFogf(GL2.GL_FOG_END, endDistance);
                break;

            case EXPONENTIAL:
            case EXPONENTIAL_2:
                gl.glFogf(GL2.GL_FOG_DENSITY, density);
                break;
        }
    }

    /**
     * Restore all openGL state to the given drawable.
     *
     * @param gl The gl context to draw with
     */
    @Override
    public void postRender(GL2 gl)
    {
    }

    //----------------------------------------------------------
    // Methods defined by Node
    //----------------------------------------------------------

    /**
     * Mark this node as having dirty bounds due to one of it's children having
     * their bounds changed.
     */
    @Override
    protected void markBoundsDirty()
    {
        // Bounds are not used by Fog.
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
        Fog sh = (Fog)o;
        return compareTo(sh);
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
        if(!(o instanceof Fog))
            return false;
        else
            return equals((Fog)o);
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

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
     * Set whether the fog should act in local or global mode.
     *
     * @param enable true to force this fog to be used for global rendering only
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setGlobalOnly(boolean enable)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        globalOnly = enable;
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
    }

    /**
     * Change the fog colour to the new colour. Fog colour only takes RGB.
     *
     * @param c The colour to copy in
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setColor(float[] c)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        color[0] = c[0];
        color[1] = c[1];
        color[2] = c[2];
    }

    /**
     * Get the current drawing colour
     *
     * @param c An array of length 3 or more to copy the colour to
     */
    public void getColor(float[] c)
    {
        c[0] = color[0];
        c[1] = color[1];
        c[2] = color[2];
    }

    /**
     * Set the distance functions for the fog when in linear mode.
     *
     * @param start The closest distance that fog starts at
     * @param end The distance that the fog is fully opaque at
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setLinearDistance(float start, float end)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(start > end)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(FOG_RANGE_PROP);
            throw new IllegalArgumentException(msg);
        }

        startDistance = start;
        endDistance = end;
    }

    /**
     * Get the two distance values for the linear fog settings.
     * The values are returned in the provided array with index 0 being the
     * start and index 1 being the end.
     *
     * @param values An array to copy the values into
     */
    public void getLinearDistance(float[] values)
    {
        values[0] = startDistance;
        values[1] = endDistance;
    }

    /**
     * Set the exponential decay factor.
     *
     * @param rate A value that should be greater than zero
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setDensityRate(float rate)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        density = rate;
    }

    /**
     * Get the current decay rate.
     *
     * @return a value > 0
     */
    public float getDensityRate()
    {
        return density;
    }

    /**
     * Set the type of fog to be rendered. The type is one of the 3 pre-defined
     * modes described in the class header.
     *
     * @param type One of EXPONENTIAL, EXPONENTIAL_2, or LINEAR
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setMode(int type)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        mode = type;
    }

    /**
     * Get the current decay rate.
     *
     * @return One of EXPONENTIAL, EXPONENTIAL_2, or LINEAR
     */
    public int getMode()
    {
        return mode;
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param fog The program instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(Fog fog)
    {
        if(fog == null)
            return 1;

        if(fog == this)
            return 0;

        if(mode != fog.mode)
            return mode < fog.mode? -1 : 1;

        if(enabled != fog.enabled)
            return enabled ? 1 : -1;

        if(globalOnly != fog.globalOnly)
            return globalOnly ? 1 : -1;

        int res = compareColor3(color, fog.color);
        if(res != 0)
            return res;

        if(density != fog.density)
            return density < fog.density ? -1 : 1;

        if(endDistance != fog.endDistance)
            return endDistance < fog.endDistance? -1 : 1;

        if(mode != fog.startDistance)
            return startDistance < fog.startDistance? -1 : 1;

       return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param fog The program instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(Fog fog)
    {
        if(fog == this)
            return true;

        if((fog == null) ||
           (mode != fog.mode) ||
           (globalOnly != fog.globalOnly) ||
           (enabled != fog.enabled) ||
           !equalsColor3(color, fog.color) ||
           (density != fog.density) ||
           (endDistance != fog.endDistance) ||
           (startDistance != fog.startDistance))
            return false;

        return true;
    }

    /**
     * Compare 2 color arrays of length 3 for equality
     *
     * @param a The first colour array to check
     * @param b The first colour array to check
     * @return -1 if a[i] < b[i], +1 if a[i] > b[i], otherwise 0
     */
    private int compareColor3(float[] a, float[] b)
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
     * Compare 2 color arrays of length 3 for equality
     *
     * @param a The first colour array to check
     * @param b The first colour array to check
     * @return true if they have the same values, false otherwise
     */
    private boolean equalsColor3(float[] a, float[] b)
    {
        return (a[0] == b[0]) && (a[1] == b[1]) && (a[2] == b[2]);
    }
}
