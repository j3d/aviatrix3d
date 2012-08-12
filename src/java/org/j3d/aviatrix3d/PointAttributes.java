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

import javax.media.opengl.GL;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.AppearanceAttributeRenderable;
import org.j3d.aviatrix3d.rendering.DeletableRenderable;

/**
 * Describes attributes used when rendering a point.
 * <p>
 *
 * Points only have a two controllable attributes - the size and whether
 * antialiasing should be available.
 * <p>
 *
 * If point sprites are available, this class can be used to enable or
 * disable their use. Note that to use them completely, you also need to enable
 * the equivalent state with
 * {@link TextureAttributes#setPointSpriteCoordEnabled(boolean)}
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>noSpriteSptMsg: Error message when video card doesn't support point
 *     sprites</li>
 * <li>negPointSizeMsg: Error message when point size is <= 0</li>
 * <li>pointSizeInvertedMsg: Error message when min point size > max</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.15 $
 */
public class PointAttributes extends NodeComponent
    implements AppearanceAttributeRenderable, DeletableRenderable
{
    /** Message when we cannot handle point sprites */
    private static final String NO_POINT_SPRITE_PROP =
        "org.j3d.aviatrix3d.PointAttributes.noSpriteSptMsg";

    /** Message when the point size is <= 0 */
    private static final String POINT_SIZE_PROP =
        "org.j3d.aviatrix3d.PointAttributes.negPointSizeMsg";

    /** Message when the setting min point size is > max point size. */
    private static final String POINT_SIZE_INV_PROP =
        "org.j3d.aviatrix3d.PointAttributes.pointSizeInvertedMsg";

    /** The default factor components */
    private static final float[] DEFAULT_FACTORS = { 1, 1, 1 };

    /**
     * Global flag to know if we are capable of rendering point sprites.
     * This gets queried on the first time rendering is run and set
     * appropriately.
     */
    private static boolean hasPointSpriteAPI;

    /** Flag to say we've queried for the multitexture API capabilities */
    private static boolean queryComplete;


    /** The size of the point in pixels. */
    private float pointSize;

    /** Should this be antialiased? */
    private boolean antialias;

    /** Min point size parameter */
    private float minPointSize;

    /** Max point size parameter */
    private float maxPointSize;

    /** Override the minimum size if multisampling is enabled */
    private float fadeThresholdSize;

    /**
     * General flag indicating that the current setup requires the
     * attenuation factors to be passed to OpenGL.
     */
    private boolean needAttenuation;

    /** Attentuation parameters, if provided. null otherwise */
    private float[] attenuationFactors;

    /** Has an attribute changed */
    private boolean stateChanged;

    /** Map of display contexts to maps */
    private HashMap<GL, Integer> displayListMap;

    /** Enable or disable point sprite coordinates, if available */
    private boolean enablePointSprites;

    /**
     * Constructs a attribute set with default values of 1.0 point size and
     * no antialiasing.
     */
    public PointAttributes()
    {
        stateChanged = true;
        antialias = false;
        enablePointSprites = false;

        fadeThresholdSize = 1;
        pointSize = 1;
        minPointSize = 1;
        maxPointSize = 1;

        displayListMap = new HashMap<GL, Integer>(1);
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
        return POINT_ATTRIBUTE;
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
                //errorReporter.warningReport(msg, null);
                System.out.println(msg);
            }
        }

        // If we have changed state, then clear the old display lists
        if(stateChanged)
        {
            synchronized(displayListMap)
            {
				if(displayListMap.size() != 0)
				{
					Integer listName = displayListMap.remove(gl);
					
					if(listName != null)
						gl.glDeleteLists(listName.intValue(), 1);
				}
                displayListMap.clear();
                stateChanged = false;
            }
        }

        Integer listName = (Integer)displayListMap.get(gl);

        if(listName == null)
        {
            listName = new Integer(gl.glGenLists(1));

            gl.glNewList(listName.intValue(), GL.GL_COMPILE);

            gl.glPushAttrib(GL.GL_POINT_BIT);

            if(antialias)
                gl.glEnable(GL.GL_POINT_SMOOTH);

            if(pointSize != 1)
                gl.glPointSize(pointSize);

            if(needAttenuation)
                gl.glPointParameterfv(GL.GL_POINT_DISTANCE_ATTENUATION,
                                      attenuationFactors,
                                      0);

            if(minPointSize != 1)
                gl.glPointParameterf(GL.GL_POINT_SIZE_MIN, minPointSize);

            if(maxPointSize != 1)
                gl.glPointParameterf(GL.GL_POINT_SIZE_MAX, maxPointSize);

            if(fadeThresholdSize != 1)
                gl.glPointParameterf(GL.GL_POINT_FADE_THRESHOLD_SIZE,
                                     fadeThresholdSize);

            if(hasPointSpriteAPI && enablePointSprites)
                gl.glEnable(GL.GL_POINT_SPRITE_ARB);

            gl.glEndList();
            displayListMap.put(gl, listName);
        }

        gl.glCallList(listName.intValue());
    }

    /**
     * Restore all openGL state to the given drawable.
     *
     * @param gl The gl context to draw with
     */
    public void postRender(GL gl)
    {
        if(antialias)
            gl.glDisable(GL.GL_POINT_SMOOTH);

        if(pointSize != 1)
            gl.glPointSize(1);

        if(needAttenuation)
            gl.glPointParameterfv(GL.GL_POINT_DISTANCE_ATTENUATION,
                                  DEFAULT_FACTORS,
                                  0);

        if(minPointSize != 1)
            gl.glPointParameterf(GL.GL_POINT_SIZE_MIN, 1);

        if(maxPointSize != 1)
            gl.glPointParameterf(GL.GL_POINT_SIZE_MAX, 1);

        if(fadeThresholdSize != 1)
            gl.glPointParameterf(GL.GL_POINT_FADE_THRESHOLD_SIZE, 1);

        if(hasPointSpriteAPI && enablePointSprites)
            gl.glDisable(GL.GL_POINT_SPRITE_ARB);

        gl.glPopAttrib();
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
        PointAttributes ta = (PointAttributes)o;
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
        if(!(o instanceof PointAttributes))
            return false;
        else
            return equals((PointAttributes)o);

    }

    //----------------------------------------------------------
    // Methods defined by SceneGraphObject
    //----------------------------------------------------------

    /**
     * Notification that this object is live now. Overridden to make sure that
     * the live state of the nodes represents the same state as the parent
     * scene graph.
     *
     * @param state true if this should be marked as live now
     */
    protected void setLive(boolean state)
    {
        super.setLive(state);

        if(!state && updateHandler != null)
            updateHandler.requestDeletion(this);
    }
	
	//---------------------------------------------------------------
    // Methods defined by DeletableRenderable
    //---------------------------------------------------------------

    /**
     * Cleanup the object now for the given GL context.
     *
     * @param gl The gl context to draw with
     */
    public void cleanup(GL gl)
    {
		if(displayListMap.size() != 0)
        {
            Integer listName = displayListMap.remove(gl);

            if(listName != null)
			{
                gl.glDeleteLists(listName.intValue(), 1);
			}
        }
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Set the antialiased flag state. By antialiasing is off.
     *
     * @param state True to use antialiasing, false to turn it off
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setAntiAliased(boolean state)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(antialias != state)
        {
            antialias = state;
            stateChanged = true;
        }
    }

    /**
     * Check the state of the antialiased flag setting for this geometry.
     *
     * @return true if antialiasing is currently enabled
     */
    public boolean isAntiAliased()
    {
        return antialias;
    }

    /**
     * Set the size in pixels of the point size. Point size must be greater
     * than zero.
     *
     * @param size The size of the line in pixels
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     * @throws IllegalArgumentException Point size was non-positive
     */
    public void setPointSize(float size)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(size <= 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(POINT_SIZE_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Float(size) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        if(pointSize != size)
        {
            pointSize = size;
            stateChanged = true;
        }
    }

    /**
     * Get the current point size in use.
     *
     * @return A value greater than zero
     */
    public float getPointSize()
    {
        return pointSize;
    }

    /**
     * Set the minimum threshold size in pixels for when multisampling is
     * enabled. Point size must be greater than zero. This overrides the
     * normal minimum point size as per specification. A value of 1 will
     * disable this setting.
     *
     * @param size The size of the line in pixels
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     * @throws IllegalArgumentException Point size was non-positive
     */
    public void setFadeThresholdSize(float size)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(size <= 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(POINT_SIZE_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Float(size) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        if(fadeThresholdSize != size)
        {
            fadeThresholdSize = size;
            stateChanged = true;
        }
    }

    /**
     * Get the current point size in use.
     *
     * @return A value greater than zero
     */
    public float getFadeThresholdSize()
    {
        return fadeThresholdSize;
    }

    /**
     * Set the size in pixels of the point size. Point size must be greater
     * than zero.
     *
     * @param size The size of the line in pixels
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     * @throws IllegalArgumentException Point size was non-positive
     */
    public void setMinPointSize(float size)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(size <= 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(POINT_SIZE_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Float(size) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        if(size > maxPointSize)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(POINT_SIZE_INV_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Float(size), new Float(maxPointSize) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        if(minPointSize != size)
        {
            minPointSize = size;
            stateChanged = true;
        }
    }

    /**
     * Get the current minimum point size in use.
     *
     * @return A value greater than zero
     */
    public float getMinPointSize()
    {
        return minPointSize;
    }

    /**
     * Set the size in pixels of the point size. Point size must be greater
     * than zero.
     *
     * @param size The size of the line in pixels
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     * @throws IllegalArgumentException Point size was non-positive
     */
    public void setMaxPointSize(float size)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(size <= 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(POINT_SIZE_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Float(size) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        if(size < minPointSize)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(POINT_SIZE_INV_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Float(minPointSize), new Float(size) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        if(maxPointSize != size)
        {
            maxPointSize = size;
            stateChanged = true;
        }
    }

    /**
     * Get the current maximum point size in use.
     *
     * @return A value greater than zero
     */
    public float getMaxPointSize()
    {
        return maxPointSize;
    }

    /**
     * Set the blend colour to use for this texture. Blend is a 4-component
     * colour value. Setting all the factors to a value of 1 will effectively
     * disable the use of this function.
     *
     * @param a The first param of the attenuation function
     * @param b The second param of the attenuation function
     * @param c The third param of the attenuation function
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setAttenuationFactors(float a, float b, float c)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(attenuationFactors == null)
            attenuationFactors = new float[4];

        attenuationFactors[0] = a;
        attenuationFactors[1] = b;
        attenuationFactors[2] = c;

        needAttenuation = !((a == 1) && (b == 1) && (c == 1));


        stateChanged = true;
    }

    /**
     * Attenuation factor values. If none have been set or have been cleared,
     * nothing is copied in.
     *
     * @param factor An array, length 3 to copy the factors into
     */
    public void getAttenuationFactors(float[] factor)
    {
        if(attenuationFactors == null)
            return;

        factor[0] = attenuationFactors[0];
        factor[1] = attenuationFactors[1];
        factor[2] = attenuationFactors[2];
    }

    /**
     * Set the point sprite enabled flag. By default this is disabled.
     * Note that to use them completely, you also need to enable the
     * equivalent state with
     * {@link TextureAttributes#setPointSpriteCoordEnabled(boolean)}
     *
     * @param state true if this texture unit should enable point sprite
     *   coordinates. False to disable
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setPointSpriteEnabled(boolean state)
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
    public boolean isPointSpriteEnabled()
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
     * @param pa The attributes instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(PointAttributes pa)
    {
        if(pa == null)
            return 1;

        if(pa == this)
            return 0;

        if(antialias != pa.antialias)
            return antialias ? 1 : -1;

        if(pointSize != pa.pointSize)
            return pointSize < pa.pointSize ? -1 : 1;

        if(minPointSize != pa.minPointSize)
            return pointSize < pa.pointSize ? -1 : 1;

        if(maxPointSize != pa.maxPointSize)
            return pointSize < pa.pointSize ? -1 : 1;

        if(fadeThresholdSize != pa.fadeThresholdSize)
            return fadeThresholdSize < pa.fadeThresholdSize ? -1 : 1;


        if(needAttenuation != pa.needAttenuation)
            return needAttenuation ? 1 : -1;
        else if(needAttenuation)
        {
            if(attenuationFactors[0] < pa.attenuationFactors[0])
                return -1;
            else if(attenuationFactors[0] > pa.attenuationFactors[0])
                return 1;

            if(attenuationFactors[1] < pa.attenuationFactors[1])
                return -1;
            else if(attenuationFactors[1] > pa.attenuationFactors[1])
                return 1;

            if(attenuationFactors[2] < pa.attenuationFactors[2])
                return -1;
            else if(attenuationFactors[2] > pa.attenuationFactors[2])
                return 1;

            if(attenuationFactors[3] < pa.attenuationFactors[3])
                return -1;
            else if(attenuationFactors[3] > pa.attenuationFactors[3])
                return 1;
        }

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param pa The attributes instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(PointAttributes pa)
    {
        if(pa == this)
            return true;

        if((pa == null) ||
           (antialias != pa.antialias) ||
           (pointSize != pa.pointSize) ||
           (minPointSize != pa.minPointSize) ||
           (maxPointSize != pa.maxPointSize) ||
           (fadeThresholdSize != pa.fadeThresholdSize) ||
           (needAttenuation != pa.needAttenuation))
            return false;

        if(needAttenuation)
        {
            if((attenuationFactors[0] != pa.attenuationFactors[0]) ||
               (attenuationFactors[1] != pa.attenuationFactors[1]) ||
               (attenuationFactors[2] != pa.attenuationFactors[2]) ||
               (attenuationFactors[3] != pa.attenuationFactors[3]))
                return false;
        }

        return true;
    }
}
