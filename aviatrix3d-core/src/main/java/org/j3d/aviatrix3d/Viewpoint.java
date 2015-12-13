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
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.Cullable;
import org.j3d.aviatrix3d.rendering.EnvironmentCullable;
import org.j3d.aviatrix3d.rendering.ObjectRenderable;
import org.j3d.aviatrix3d.rendering.Renderable;

/**
 * A viewpoint into the scene.
 * <p>
 *
 * Viewpoints have their own implicit directional light (a headlight) that
 * is controlled separately to the normal in-scenegraph lights. Lights are
 * scoped by default and thus having the viewpoint also have a light following
 * it around is a pain to deal with, you can select on directly on the
 * viewpoint itself. This light is always pointing down the Z axis in the
 * local coordinate system.
 * <p>
 *
 * In addition to the headlight, a separate ambient light colour may be
 * provided. Although this is provided, note that whether it has any effect is
 * dependent on whether lighting is enabled on the object being rendered.
 * <p>
 *
 * <b>2D Scenes</b>
 * <p>
 * Since a 2D scene does not have any form of lighting enabled, this class
 * mostly acts as a placeholder to position where the view is located in a
 * 2D frame of reference. By modifying the pixel transformations above this
 * viewpoint, you can effect a 2D scrolling effect. All the other effects of
 * this node are ignored.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>redComponentRangeMsg: Error message when red component is not [0,1]</li>
 * <li>greenComponentRangeMsg: Error message when green component is not [0,1]</li>
 * <li>blueComponentRangeMsg: Error message when blue component is not [0,1]</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.25 $
 */
public class Viewpoint extends Leaf
    implements ObjectRenderable, EnvironmentCullable
{
    /** Message when the drawing mode is not a valid value */
    private static final String INVALID_RED_PROP =
        "org.j3d.aviatrix3d.Viewpoint.redComponentRangeMsg";

    /** Message when the drawing mode is not a valid value */
    private static final String INVALID_GREEN_PROP =
        "org.j3d.aviatrix3d.Viewpoint.greenComponentRangeMsg";

    /** Message when the drawing mode is not a valid value */
    private static final String INVALID_BLUE_PROP =
        "org.j3d.aviatrix3d.Viewpoint.blueComponentRangeMsg";

    /** Location of the local headlight when directional */
    private static final float[] DIR_LOCATION = { 0, 0, 1, 0 };

    /** Location of the local headlight when spot */
    private static final float[] SPOT_LOCATION = { 0, 0, 1, 1 };

    /** OpenGL default value for ambient lights. */
    private static final float[] DEFAULT_AMBIENT = { 0.2f, 0.2f, 0.2f, 1 };

    /** Flag indicating the headlight usage state */
    private boolean useHeadlight;

    /**
     * Flag indicating if the headlight should be a spot (false) or
     * directional light source.
     */
    private boolean useDirectionalLight;

    /** Flag indicating the global ambient usage state */
    private boolean useGlobalAmbient;

    /** The global ambient light color values */
    private float[] globalColor;

    /**
     * The default constructor where the headlight is implicitly turned off,
     * and the default light type is directional.
     */
    public Viewpoint()
    {
        useHeadlight = false;
        useDirectionalLight = true;
        useGlobalAmbient = false;
    }

    //----------------------------------------------------------
    // Methods defined by EnvironmentCullable
    //----------------------------------------------------------

    /**
     * Get the parent cullable of this instance.
     *
     * @return The parent instance
     */
    @Override
    public Cullable getCullableParent()
    {
        return (parent instanceof Cullable) ? (Cullable)parent : null;
    }

    /**
     * Get the renderable that represents the environment node rendering.
     *
     * @return The renderable responsible for this node
     */
    @Override
    public Renderable getRenderable()
    {
        return this;
    }

    //----------------------------------------------------------
    // Methods defined by ObjectRenderable
    //----------------------------------------------------------

    /**
     * This method is called to render this node.  All openGL commands needed
     * to render the node should be executed.  Any transformations needed
     * should be added to the transformation stack premultiplied.  This
     * method must be re-entrant as it can be called from multiple
     * places at once.
     *
     * @param gl The gl context to draw with
     */
    @Override
    public void render(GL2 gl)
    {
        if(useHeadlight)
        {
            gl.glEnable(GL2.GL_LIGHT0);
            if(!useDirectionalLight)
            {
                gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, SPOT_LOCATION, 0);
                gl.glLightf(GL2.GL_LIGHT0, GL2.GL_SPOT_CUTOFF, 45);
            }
            else
                gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, DIR_LOCATION, 0);
        }
        else {
            gl.glDisable(gl.GL_LIGHT0);
        }

        if(useGlobalAmbient)
            gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, globalColor, 0);
    }

    /*
     * This method is called after a node has been rendered.  This method
     * must be re-entrant.
     *
     * @param gl The gl context to draw with
     */
    @Override
    public void postRender(GL2 gl)
    {
        if(useHeadlight)
            gl.glDisable(GL2.GL_LIGHT0);
        else
            gl.glEnable(GL2.GL_LIGHT0);

        if(useGlobalAmbient)
            gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, DEFAULT_AMBIENT, 0);
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
        // Ignored as the bounds of a viewpoint are not valid. It is considered
        // a point in space.
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
        Viewpoint app = (Viewpoint)o;
        return compareTo(app);
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
        if(!(o instanceof Viewpoint))
            return false;
        else
            return equals((Viewpoint)o);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Get the current state of the headlight usage.
     *
     * @return true if the headlight is currently active
     */
    public boolean isHeadlightEnabled()
    {
        return useHeadlight;
    }

    /**
     * Turn the local headlight on/off.
     *
     * @param state true to turn the light on
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setHeadlightEnabled(boolean state)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());


        useHeadlight = state;
    }

    /**
     * Turn the global ambient lighting setting on or off.
     *
     * @param state true to turn the light on
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setGlobalAmbientLightEnabled(boolean state)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        useGlobalAmbient = state;

        // if we don't have it yet, create one and initialise it to the OpenGL
        // default values.
        if(globalColor == null)
            globalColor = new float[] { 0.2f, 0.2f, 0.2f, 1 };
    }

    /**
     * Get the current state of the global ambient light usage.
     *
     * @return true if the ambient light is currently active
     */
    public boolean isGlobalAmbientLightEnabled()
    {
        return useGlobalAmbient;
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
    public void setGlobalAmbientColor(float[] col)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

		checkColourRange(col[0], INVALID_RED_PROP);
		checkColourRange(col[1], INVALID_GREEN_PROP);
		checkColourRange(col[2], INVALID_BLUE_PROP);

        globalColor[0] = col[0];
        globalColor[1] = col[1];
        globalColor[2] = col[2];
    }

    /**
     * Retrieve the current colour value from the light.
     *
     * @param col An array to copy the colour value into
     */
    public void getGlobalAmbientColor(float[] col)
    {
        col[0] = globalColor[0];
        col[1] = globalColor[1];
        col[2] = globalColor[2];
    }

    /**
     * Get the current state of the headlight type.
     *
     * @return true if the headlight is currently a directional light
     */
    public boolean isDirectionalLight()
    {
        return useDirectionalLight;
    }

    /**
     * Change the style of the headlight between directional and spot types.
     *
     * @param state true to turn the light to directional, false for spot
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setHeadlightType(boolean state)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        useDirectionalLight = state;
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param vp The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(Viewpoint vp)
    {
        if(vp == null)
            return 1;

        if(vp == this)
            return 0;

        if(useDirectionalLight != vp.useDirectionalLight)
            return !useDirectionalLight ? -1 : 1;

        if(useGlobalAmbient != vp.useGlobalAmbient)
            return !useGlobalAmbient ? -1 : 1;

        int res = compareColor4(globalColor, vp.globalColor);
        if(res != 0)
            return res;

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param vp The viewpoint instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(Viewpoint vp)
    {
        if(vp == this)
            return true;

        if((vp == null) ||
           (useDirectionalLight != vp.useDirectionalLight) ||
           (useGlobalAmbient != vp.useGlobalAmbient))
            return false;

        if(!equalsColor4(globalColor, vp.globalColor))
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
    private int compareColor4(float[] a, float[] b)
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

    /**
     * Compare 2 color arrays of length 4 for equality
     *
     * @param a The first colour array to check
     * @param b The first colour array to check
     * @return true if they have the same values, false otherwise
     */
    private boolean equalsColor4(float[] a, float[] b)
    {
        return (a[0] == b[0]) &&
               (a[1] == b[1]) &&
               (a[2] == b[2]) &&
               (a[3] == b[3]);
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
