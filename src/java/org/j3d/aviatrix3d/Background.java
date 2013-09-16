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
import java.util.HashMap;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.Cullable;
import org.j3d.aviatrix3d.rendering.BackgroundRenderable;
import org.j3d.aviatrix3d.rendering.EnvironmentCullable;
import org.j3d.aviatrix3d.rendering.Renderable;


/**
 * Base collection of functionality marking background nodes of various types.
 * <p>
 *
 * Backgrounds are rendered as the first item but do not interact with the
 * normal geometry in the rendering process. Typically, backgrounds are
 * rendered in a fixed volume (a unit box or sphere is the most common) with
 * depthbuffer reads and writes disabled. Ordinary geometry is then drawn over
 * the top.  Backgrounds must fit within clipping planes of [0.1,1].
 * <p>
 * As such, backgrounds are not typically subject to most rendering effects,
 * such as lighting, fog, perspective projection etc.
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
 * @version $Revision: 1.13 $
 */
public abstract class Background extends Leaf
    implements BackgroundRenderable, EnvironmentCullable
{
    /** Message when the drawing mode is not a valid value */
    protected static final String INVALID_RED_PROP =
        "org.j3d.aviatrix3d.Background.redComponentRangeMsg";

    /** Message when the drawing mode is not a valid value */
    protected static final String INVALID_GREEN_PROP =
        "org.j3d.aviatrix3d.Background.greenComponentRangeMsg";

    /** Message when the drawing mode is not a valid value */
    protected static final String INVALID_BLUE_PROP =
        "org.j3d.aviatrix3d.Background.blueComponentRangeMsg";

    /** Message when the drawing mode is not a valid value */
    protected static final String INVALID_ALPHA_PROP =
        "org.j3d.aviatrix3d.Background.alphaComponentRangeMsg";

    /** Error message if the array is the wrong size */
    private static final String INVALID_SIZE_PROP =
        "org.j3d.aviatrix3d.Background.colorLengthMsg";

    /** Base colour of the background */
    protected float[] color;

    /** Map of display contexts to maps */
    protected HashMap dispListMap;

    /**
     * Flag whether the background should perform a clear of the colour
     * buffer before being drawn. In multilayer systems, we may not want to
     * do that, and instead just blend this layer's background with whatever
     * is previously rendered. By default this is set to always clear.
     */
    protected boolean useClearColor;

    /**
     * Constructs a background node with the colour set to opaque black.
     */
    protected Background()
    {
        this(null);
    }

    /**
     * Construct a background node for a user-provided colour. The colour
     * provided should have 3 or 4 elements. If 3 are provided, a fully opaque
     * background is assumed. If less than 3 elements are provided, an exception
     * is generated. If the array is null, this assumes the a default black
     * background.
     *
     * @param c The array of colours to use, or null
     * @throws IllegalArgumentException The colour array is not long enough
     */
    protected Background(float[] c)
    {
        color = new float[4];
        useClearColor = true;

        updateColor(c);
    }

    //----------------------------------------------------------
    // Methods defined by EnvironmentCullable
    //----------------------------------------------------------

    /**
     * Get the parent cullable of this instance.
     *
     * @return The parent instance
     */
    public Cullable getCullableParent()
    {
        return (parent instanceof Cullable) ? (Cullable)parent : null;
    }

    /**
     * Get the renderable that represents the environment node rendering.
     *
     * @return The renderable responsible for this node
     */
    public Renderable getRenderable()
    {
        return this;
    }

    //----------------------------------------------------------
    // Methods defined by Node
    //----------------------------------------------------------

    /**
     * Mark this node as having dirty bounds due to one of it's children having
     * their bounds changed.
     */
    protected void markBoundsDirty()
    {
        // Bounds are not used by backgrounds
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Enable or disable the clearing of the colour buffer before drawing the
     * background contents. The default value is true.
     *
     * @param state True to enable clearing of the colour buffer
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data callback method
     */
    public void setColorClearEnabled(boolean state)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        useClearColor = state;
    }

    /**
     * Check to see if the clearing of the colour buffer is enabled for this
     * instance. The default value is true.
     *
     * @return true if clearing of the colour buffer is currently performed
     */
    public boolean isColorClearEnabled()
    {
        return useClearColor;
    }

    /**
     * Change the colour to the new colour. Colour takes RGBA. If a 3 component
     * colour is set, assume a fully opaque colour. A null parameter sets the
     * colour back to black.
     *
     * @param c The colour to copy in
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data callback method
     * @throws IllegalArgumentException The colour array is not long enough
     */
    public void setColor(float[] c)
        throws InvalidWriteTimingException, IllegalArgumentException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        updateColor(c);
    }

    /**
     * Change the colour to the new colour. Colour takes RGBA.
     *
     * @param r The red colour component to use
     * @param g The green colour component to use
     * @param b The blue colour component to use
     * @param a The alpha colour component to use
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data callback method
	 * @throws IllegalArgumentException A colour range component is out of the
	 *   range [0,1]
     */
    public void setColor(float r, float g, float b, float a)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

		checkColourRange(r, INVALID_RED_PROP);
		checkColourRange(g, INVALID_GREEN_PROP);
		checkColourRange(b, INVALID_BLUE_PROP);
		checkColourRange(a, INVALID_ALPHA_PROP);

        color[0] = r;
        color[1] = g;
        color[2] = b;
        color[3] = a;
    }

    /**
     * Get the current drawing colour
     *
     * @param c An array of length 4 or more to copy the colour to
     */
    public void getColor(float[] c)
    {
        c[0] = color[0];
        c[1] = color[1];
        c[2] = color[2];
        c[3] = color[3];
    }

    /**
     * Internal convenience method to set the colour from an array.
     *
     * @param c The colour array to set from
     * @throws IllegalArgumentException The colour array is not long enough
     */
    private void updateColor(float[] c)
        throws IllegalArgumentException
    {
        if(c != null)
        {
            switch(c.length)
            {
                case 0:
                case 1:
                case 2:
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg = intl_mgr.getString(INVALID_SIZE_PROP);
                    throw new IllegalArgumentException(msg);

                case 3:
                    color[0] = c[0];
                    color[1] = c[1];
                    color[2] = c[2];
                    color[3] = 0;
                    break;

                case 4:
                    color[0] = c[0];
                    color[1] = c[1];
                    color[2] = c[2];
                    color[3] = c[3];
                    break;
            }
        }
        else
        {
            color[0] = 0;
            color[1] = 0;
            color[2] = 0;
            color[3] = 0;
        }
    }

    /**
     * Compare 2 color arrays of length 3 for equality
     *
     * @param a The first colour array to check
     * @param b The first colour array to check
     * @return -1 if a[i] < b[i], +1 if a[i] > b[i], otherwise 0
     */
    protected int compareColor4(float[] a, float[] b)
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
    protected boolean equalsColor4(float[] a, float[] b)
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
