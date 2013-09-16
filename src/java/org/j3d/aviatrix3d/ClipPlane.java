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
import org.j3d.aviatrix3d.rendering.*;

/**
 * Representation of a single clip plane that can be used to clip a model in
 * addition to the normal view volume clipping.
 * <p>
 *
 * ClipPlane objects may be placed at any point in the scene graph heirarchy
 * and is effected by all parent transforms. Each clip plane is accumulated
 * and pushed to the leaf node, in a similar manner that lights are. As such,
 * there is no requirement to provide the clip plane index as this will be
 * assigned automatically at rendering time. A benefit of this feature is that
 * it allows you to specify several different clip planes at different
 * locations in the heirarchy and have them apply to the local coordinate
 * system.
 * <p>
 *
 * <b>Rendering Implementation Tips</b>
 * <p>
 * The external data passed to the ComponentRenderable calls shall be an
 * <code>Integer</code> instance that represents the GL identifier of the
 * clip plane (GL_CLIPPLANE0 + i) this instance is working with.
 * <p>
 *
 * See the {@link EffectRenderable} interface for definition of the scoping
 * and effects bounds.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>equationSizeMsg: Error message the plane equation has <4 components.</li>
 *
 * @author Justin Couch
 * @version $Revision: 1.14 $
 */
public class ClipPlane extends Leaf
    implements LeafCullable,
               ComponentRenderable,
               EffectRenderable,
               Comparable
{
    /** Error message if the array size is too small */
    private static final String EQ_SIZE_PROP =
        "org.j3d.aviatrix3d.ClipPlane.equationSizeMsg";

    /** The colour of the light */
    private double[] planeEquation;

    /** The enabled state */
    private boolean enabled;

    /**
     * Flag indicating whether this should be a global (true) or locally-scoped
     * light (false). By default it is local only.
     */
    private boolean globalOnly;

    /**
     * A bounding volume used to restrict the scope of what the light effects.
     * If this is set, any object that intersects with this bounds will be
     * effected by this light. Deciding what to intersect will depend on the
     * setting of the {@link #globalOnly} state. The bounds are in the local
     * coordinate system of this light.
     */
    private BoundingVolume effectBounds;

    /**
     * Creates a new clip plane using the default plane equation.
     */
    public ClipPlane()
    {
        planeEquation = new double[4];

        enabled = false;
        globalOnly = false;
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
        return CLIP_CULLABLE;
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
    // Methods defined by ComponentRenderable
    //---------------------------------------------------------------

    /**
     * Overloaded form of the render() method to render the clip details given
     * the specific clip plane ID used by OpenGL. Since the active plane ID for
     * this node may vary over time, a fixed ID cannot be used by OpenGL. The
     * renderer will always call this method rather than the normal render()
     * method.
     *
     * @param gl The GL context to render with
     * @param planeId the ID of the plane to make GL calls with
     */
    public void render(GL gl, Object planeId)
    {
        gl.glClipPlane(((Integer)planeId).intValue(), planeEquation, 0);
        gl.glEnable(((Integer)planeId).intValue());
    }

    /**
     * Overloaded form of the postRender() method to render the clip details
     * given the specific clip plane ID used by OpenGL. Since the active plane
     * ID for this node may vary over time, a fixed ID cannot be used by OpenGL.
     * The renderer will always call this method rather than the normal
     * postRender() method.
     *
     * @param gl The GL context to render with
     * @param planeId the ID of the plane to make GL calls with
     */
    public void postRender(GL gl, Object planeId)
    {
        gl.glDisable(((Integer)planeId).intValue());
    }

    //---------------------------------------------------------------
    // Methods defined by Comparable
    //---------------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param o The object to be compared
     * @return -1, 0 or 1 depending on order
     * @throws ClassCastException The specified object's type prevents it from
     *    being compared to this Object
     */
    public int compareTo(Object o)
        throws ClassCastException
    {
        ClipPlane cp = (ClipPlane)o;
        return compareTo(cp);
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
     *   of the NodeUpdateListener data change callback method
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
     *   of the NodeUpdateListener data change callback method
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
     *   of the NodeUpdateListener data change callback method
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
     * Set the plane equation to the new value. The equation is defined by the
     * standard 4-component representation of Ax + By + Cz + D = 0. The length
     * of provided array must have at least 4 components.
     *
     * @param eq The new equation values to use
     * @throws IllegalArgumentException The array is not at least 4 items long
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data change callback method
     */
    public void setPlaneEquation(double[] eq)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(eq == null || eq.length < 4)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(EQ_SIZE_PROP);
            throw new IllegalArgumentException(msg);
        }

        planeEquation[0] = eq[0];
        planeEquation[1] = eq[1];
        planeEquation[2] = eq[2];
        planeEquation[3] = eq[3];
    }

    /**
     * Retrieve the current plane equation values. The array needs to be at
     * least length 4.
     *
     * @param eq An array to copy the equation values into
     */
    public void getPlaneEquation(double[] eq)
    {
        eq[0] = planeEquation[0];
        eq[1] = planeEquation[1];
        eq[2] = planeEquation[2];
        eq[3] = planeEquation[3];
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param cp The plane instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(ClipPlane cp)
    {
        if(cp == null)
            return 1;

        if(cp == this)
            return 0;

        if(enabled != cp.enabled)
            return enabled ? 1 : -1;

        if(globalOnly != cp.globalOnly)
            return globalOnly ? 1 : -1;

        return compareVector4(planeEquation, cp.planeEquation);
    }

    /**
     * Compare 2 vector arrays of length 4 for equality
     *
     * @param a The first vector array to check
     * @param b The first vector array to check
     * @return -1 if a[i] < b[i], +1 if a[i] > b[i], otherwise 0
     */
    private int compareVector4(double[] a, double[] b)
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

        if(a[3] < b[3])
            return -1;
        else if (a[3] > b[3])
            return 1;

        return 0;
    }
}
