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
import java.util.HashMap;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.AppearanceAttributeRenderable;
import org.j3d.aviatrix3d.rendering.DeletableRenderable;

/**
 * Describes attributes used when rendering a polygon.
 * <p>
 *
 * Attributes control the visibility and some lighting calculations of the
 * polygon. You can control which face to cull, which way to wind the polygons
 * to calculate front and back faces, as well as some lighting control.
 * <p>
 *
 * The default setup of this class mirrors the OpenGL defaults:
 * <ul>
 * <li>antialiased false</li>
 * <li>CCW true</li>
 * <li>use two-sided lighting false</li>
 * <li>use separate specular false</li>
 * <li>culling CULL_NONE</li>
 * <li>polygon offset factor 0</li>
 * <li>polygon offset unit 1</li>
 * <li>front draw mode  DRAW_FILLED</li>
 * <li>back draw mode DRAW_FILLED</li>
 * </ul>
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>stippleArraySizeMsg: Error message when the stipple array is < 1024.</li>
 * <li>invalidCullTypeMsg: Error message when the cull type requested is invalid.</li>
 * <li>invalidDrawTypeMsg: Error message when the draw type requested is invalid.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.29 $
 */
public class PolygonAttributes extends NodeComponent
    implements AppearanceAttributeRenderable, DeletableRenderable
{
    /** Message when the drawing mode is not a valid value */
    private static final String INVALID_DRAW_PROP =
        "org.j3d.aviatrix3d.PolygonAttributes.invalidDrawTypeMsg";

    /** Message when the face cull mode is not a valid value */
    private static final String INVALID_CULL_PROP =
        "org.j3d.aviatrix3d.PolygonAttributes.invalidCullTypeMsg";

    /** Message when the face cull mode is not a valid value */
    private static final String STIPPLE_SIZE_PROP =
        "org.j3d.aviatrix3d.PolygonAttributes.stippleArraySizeMsg";

    /** The cull mode says to draw both front and back faces */
    public static final int CULL_NONE = 0;

    /** The cull mode says to draw front faces only */
    public static final int CULL_FRONT = GL.GL_FRONT;

    /** The cull mode says to draw back faces only */
    public static final int CULL_BACK = GL.GL_BACK;

    /** The cull mode says to not draw any faces */
    public static final int CULL_BOTH = GL.GL_FRONT_AND_BACK;

    /** Draw the face as the points of the vertices only */
    public static final int DRAW_POINT = GL2.GL_POINT;

    /** Draw the face as outline lines only */
    public static final int DRAW_LINE = GL2.GL_LINE;

    /** Draw the face filled as a solid object */
    public static final int DRAW_FILLED = GL2.GL_FILL;

    /** The rendering draw mode for the front face */
    private int frontDrawMode;

    /** The rendering draw mode for the back face */
    private int backDrawMode;

    /** Winding direction for the polygon */
    private boolean isCCW;

    /** Cull operations to be performed */
    private int cullFace;

    /** The polygon offset factor amount */
    private float polyOffsetFactor;

    /** The polygon offset unit amount */
    private float polyOffsetUnit;

    /** Has an attribute changed */
    private boolean stateChanged;

    /** Should this be antialiased? */
    private boolean antialias;

    /** Are we using 2-sided lighting now? */
    private boolean useTwoSidedLighting;

    /** Should we light with separate specular color? */
    private boolean useSeparateSpecular;

    /** Should be use flat shading or smooth shading */
    private boolean useFlatShading;

    /** Map of display contexts to maps for the setting */
    private HashMap<GL, Integer> setDisplayListMap;

    /** Map of display contexts to maps for the restore */
    private HashMap<GL, Integer> clearDisplayListMap;

    /** Stipple pattern for the polygon, if desired */
    private byte[] stipple;

    /**
     * Constructs a attribute set with default values.
     */
    public PolygonAttributes()
    {
        stateChanged = true;
        antialias = false;
        isCCW = true;
        useTwoSidedLighting = false;
        useSeparateSpecular = false;
        useFlatShading = false;

        cullFace = CULL_NONE;
        polyOffsetFactor = 0;
        polyOffsetUnit = 1;

        frontDrawMode = DRAW_FILLED;
        backDrawMode = DRAW_FILLED;

        setDisplayListMap = new HashMap<GL, Integer>(1);
        clearDisplayListMap = new HashMap<GL, Integer>(1);
    }

    //---------------------------------------------------------------
    // Methods defined by AppearanceAttributeRenderable
    //---------------------------------------------------------------

    /**
     * Get the type this visual attribute represents.
     *
     * @return One of the _ATTRIBUTE constants
     */
    @Override
    public int getAttributeType()
    {
        return POLYGON_ATTRIBUTE;
    }

    //---------------------------------------------------------------
    // Methods defined by ObjectRenderable
    //---------------------------------------------------------------

    /**
     * Issue ogl commands needed for this component
     *
     * @param gl The gl context to draw with
     */
    @Override
    public void render(GL2 gl)
    {
        // If we have changed state, then clear the old display lists
        if(stateChanged)
        {
            synchronized(setDisplayListMap)
            {
				if(setDisplayListMap.size() != 0)
				{
					Integer listName = setDisplayListMap.remove(gl);
					
					if(listName != null)
						gl.glDeleteLists(listName.intValue(), 1);
				}
                setDisplayListMap.clear();
				if(clearDisplayListMap.size() != 0)
				{
					Integer listName = clearDisplayListMap.remove(gl);
					
					if(listName != null)
						gl.glDeleteLists(listName.intValue(), 1);
				}
                clearDisplayListMap.clear();
                stateChanged = false;
            }
        }

        Integer listName = (Integer)setDisplayListMap.get(gl);

        if(listName == null)
        {
            listName = new Integer(gl.glGenLists(1));

            gl.glNewList(listName.intValue(), GL2.GL_COMPILE);

            if(antialias)
                gl.glEnable(GL2.GL_POLYGON_SMOOTH);

            if(!isCCW)
                gl.glFrontFace(GL.GL_CW);


            if(cullFace == CULL_NONE)
                gl.glDisable(GL.GL_CULL_FACE);
            else if(cullFace != CULL_BACK)
                gl.glCullFace(cullFace);

            if(useTwoSidedLighting)
                gl.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE, GL.GL_TRUE);

            if(useSeparateSpecular)
                gl.glLightModeli(GL2.GL_LIGHT_MODEL_COLOR_CONTROL,
                                 GL2.GL_SEPARATE_SPECULAR_COLOR);

            if(useFlatShading)
                gl.glShadeModel(GL2.GL_FLAT);

            // Using polygon offsets requires that the polygon mode be
            // explicitly set, but we don't want to set it if we don't
            // have to.
            if(polyOffsetFactor != 0)
            {
                gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);
                gl.glPolygonOffset(polyOffsetFactor, polyOffsetUnit);

                gl.glPolygonMode(GL.GL_FRONT, frontDrawMode);
                gl.glPolygonMode(GL.GL_BACK, backDrawMode);
            }
            else
            {
                if(frontDrawMode != DRAW_FILLED)
                    gl.glPolygonMode(GL.GL_FRONT, frontDrawMode);

                if(backDrawMode != DRAW_FILLED)
                    gl.glPolygonMode(GL.GL_BACK, backDrawMode);
            }

            if(stipple != null)
            {
                gl.glEnable(GL2.GL_POLYGON_STIPPLE);
                gl.glPixelStorei(GL.GL_UNSIGNED_BYTE, 1);
                gl.glPolygonStipple(stipple, 0);
            }

            gl.glEndList();
            setDisplayListMap.put(gl, listName);
        }

        gl.glCallList(listName.intValue());
    }

    /**
     * Restore all openGL state to the given drawable.
     *
     * @param gl The gl context to draw with
     */
    @Override
    public void postRender(GL2 gl)
    {
        Integer listName = clearDisplayListMap.get(gl);

        if(listName == null)
        {
            listName = new Integer(gl.glGenLists(1));

            gl.glNewList(listName.intValue(), GL2.GL_COMPILE);

            if(antialias)
                gl.glDisable(GL2.GL_POLYGON_SMOOTH);

            if(polyOffsetFactor != 0)
                gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);
            else
            {
                if(frontDrawMode != DRAW_FILLED)
                    gl.glPolygonMode(GL.GL_FRONT, GL2.GL_FILL);

                if(backDrawMode != DRAW_FILLED)
                    gl.glPolygonMode(GL.GL_BACK, GL2.GL_FILL);
            }

            if(stipple != null)
                gl.glDisable(GL2.GL_POLYGON_STIPPLE);

            if(useTwoSidedLighting)
                gl.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE, GL.GL_FALSE);

            if(useSeparateSpecular)
                gl.glLightModeli(GL2.GL_LIGHT_MODEL_COLOR_CONTROL,
                                 GL2.GL_SINGLE_COLOR);

            if(useFlatShading)
                gl.glShadeModel(GL2.GL_SMOOTH);

            if(!isCCW)
                gl.glFrontFace(GL.GL_CCW);

            if(cullFace == CULL_NONE)
                gl.glEnable(GL.GL_CULL_FACE);
            else if(cullFace != CULL_BACK)
                gl.glCullFace(GL.GL_BACK);

            gl.glEndList();
            clearDisplayListMap.put(gl, listName);
        }

        gl.glCallList(listName.intValue());
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
    @Override
    public int compareTo(Object o)
        throws ClassCastException
    {
        PolygonAttributes ta = (PolygonAttributes)o;
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
    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof PolygonAttributes))
            return false;
        else
            return equals((PolygonAttributes)o);

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
    @Override
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
    @Override
    public void cleanup(GL2 gl)
    {
		if(setDisplayListMap.size() != 0)
        {
            Integer listName = setDisplayListMap.remove(gl);

            if(listName != null)
			{
                gl.glDeleteLists(listName.intValue(), 1);
			}
        }
		if(clearDisplayListMap.size() != 0)
        {
            Integer listName = clearDisplayListMap.remove(gl);

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
     * Set the CCW flag. By default CCW is true.
     *
     * @param state True to use CCW triangles, false for CW
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setCCW(boolean state)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(isCCW != state)
        {
            isCCW = state;
            stateChanged = true;
        }
    }

    /**
     * Check the state of the CCW flag setting for this geometry.
     *
     * @return true if the vertices are rendered counter clockwise
     */
    public boolean isCCW()
    {
        return isCCW;
    }

    /**
     * Set the two-sided lighting flag. By default only single sided lighting is
     * in use.
     *
     * @param state True to use two sided lighting triangles, false for single sided
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setTwoSidedLighting(boolean state)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(useTwoSidedLighting != state)
        {
            useTwoSidedLighting = state;
            stateChanged = true;
        }
    }

    /**
     * Check the state of the two-sided lighting flag setting for this geometry.
     *
     * @return true if the both sides should have lighting calcs done
     */
    public boolean isTwoSidedLighting()
    {
        return useTwoSidedLighting;
    }

    /**
     * Set the separated specular lighting flag. By default only single lighting
     * color is calculated.
     *
     * @param state True to generate a separated specular component
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setSeparateSpecular(boolean state)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(useSeparateSpecular != state)
        {
            useSeparateSpecular = state;
            stateChanged = true;
        }
    }

    /**
     * Check the state of the separate specular lighting flag setting for this
     * geometry.
     *
     * @return true if the a separate specular component should be computed
     */
    public boolean isSeparateSpecular()
    {
        return useSeparateSpecular;
    }

    /**
     * Set the shading style to use either flat or smooth shading. lighting flag. By default only single lighting
     * color is calculated.
     *
     * @param state True to render as flat shaded polygons
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setFlatShaded(boolean state)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(useFlatShading != state)
        {
            useFlatShading = state;
            stateChanged = true;
        }
    }

    /**
     * Check the state of the separate specular lighting flag setting for this
     * geometry.
     *
     * @return true if flat shading is used
     */
    public boolean isFlatShaded()
    {
        return useFlatShading;
    }

    /**
     * Set which face is to be culled. It should be one of the base
     * constants defined by this class.
     *
     * @param face The face that should be culled
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setCulledFace(int face)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        switch(face)
        {
            case CULL_NONE:
            case CULL_FRONT:
            case CULL_BACK:
            case CULL_BOTH:
                if(face != cullFace)
                {
                    cullFace = face;
                    stateChanged = true;
                }
                break;

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(INVALID_CULL_PROP);
                throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Get the current face culled flag.
     *
     * @return one of the four CULL_X constants
     */
    public int getCulledFace()
    {
        return cullFace;
    }

    /**
     * Set the draw mode for either the front or back face.
     *
     * @param front true if this is the front-face setting
     * @param mode The mode to use for this face
     * @throws IllegalArgumentException Invalid mode supplied
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setDrawMode(boolean front, int mode)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        switch(mode)
        {
            case DRAW_POINT:
            case DRAW_LINE:
            case DRAW_FILLED:
                if(front)
                {
                    if(mode != frontDrawMode)
                    {
                        frontDrawMode = mode;
                        stateChanged = true;
                    }
                }
                else
                {
                    if(mode != backDrawMode)
                    {
                        backDrawMode = mode;
                        stateChanged = true;
                    }
                }
                break;

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(INVALID_DRAW_PROP);
                throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Get the draw mode for the specified face.
     *
     * @param front true if requesting the front-face setting
     */
    public int getDrawMode(boolean front)
    {
        return front ? frontDrawMode : backDrawMode;
    }

    /**
     * Set the polygon offset details. Factor and unit applied as per the
     * OpenGL specification.
     *
     * @param factor The offset factor mulitplier
     * @param units The offset unit multiplier
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setPolygonOffset(float factor, float units)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(polyOffsetFactor != factor)
        {
            polyOffsetFactor = factor;
            stateChanged = true;
        }

        if(polyOffsetUnit != units)
        {
            polyOffsetUnit = units;
            stateChanged = true;
        }
    }

    /**
     * Get the current polygon offset details. The array is used to copy the
     * current values into and therefore must be at least 2 items in length.
     * Index 0 is the factor, index 1 is the units
     *
     * @param values The array to copy the current values into
     */
    public void getPolygonOffset(float[] values)
    {
        values[0] = polyOffsetFactor;
        values[1] = polyOffsetUnit;
    }

    /**
     * Set the stipple mask to be used on the polygon. If the stipple is null
     * then it will clear the current stipple setup. If not null, it must be
     * at least an array length 1024 (32x32). The value is used by reference
     *
     *
     * @param pattern The bytes of the pattern or null
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setStipplePattern(byte[] pattern)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(pattern != null)
        {
            if(pattern.length < 1024)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(STIPPLE_SIZE_PROP);
                throw new IllegalArgumentException(msg);
            }

            stipple = pattern;
        }
        else
        {
            stipple = null;
        }
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param pa The attributes instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(PolygonAttributes pa)
    {
        if(pa == null)
            return 1;

        if(pa == this)
            return 0;

        if(antialias != pa.antialias)
            return antialias ? 1 : -1;

        if(cullFace != pa.cullFace)
            return cullFace < pa.cullFace ? -1 : 1;

        if(isCCW != pa.isCCW)
            return isCCW ? 1 : -1;

        if(useTwoSidedLighting != pa.useTwoSidedLighting)
            return useTwoSidedLighting ? 1 : -1;

        if(useSeparateSpecular != pa.useSeparateSpecular)
            return useSeparateSpecular ? 1 : -1;

        if(frontDrawMode != pa.frontDrawMode)
            return frontDrawMode < pa.frontDrawMode ? -1 : 1;

        if(backDrawMode != pa.backDrawMode)
            return backDrawMode < pa.backDrawMode ? -1 : 1;

        if(polyOffsetFactor != pa.polyOffsetFactor)
            return polyOffsetFactor < pa.polyOffsetFactor ? -1 : 1;

        if(polyOffsetUnit != pa.polyOffsetUnit)
            return polyOffsetUnit < pa.polyOffsetUnit ? -1 : 1;

        if(useFlatShading != pa.useFlatShading)
            return useFlatShading ? 1 : -1;

        // If both null, or both point to the same array, then consider them
        // equivalent. Otherwise do a bytewise check on them for equivalence.
        if(stipple != pa.stipple)
        {
            if(stipple == null)
                return -1;
            else if(pa.stipple == null)
                return 1;

            // ARGH! Do we really have to compare the stipple patterns? Ouch...
            // Let's cheat and only care about the first 1024 bytes.

            for(int i = 0; i < 1024; i++)
                if(stipple[i] != pa.stipple[i])
                    return stipple[i] < pa.stipple[i] ? -1 : 1;
        }

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param pa The attributes instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(PolygonAttributes pa)
    {
        if(pa == this)
            return true;

        if((pa == null) ||
           (antialias != pa.antialias) ||
           (cullFace != pa.cullFace) ||
           (isCCW != pa.isCCW) ||
           (useTwoSidedLighting != pa.useTwoSidedLighting) ||
           (useSeparateSpecular != pa.useSeparateSpecular) ||
           (frontDrawMode != pa.frontDrawMode) ||
           (backDrawMode != pa.backDrawMode) ||
           (polyOffsetFactor != pa.polyOffsetFactor) ||
           (polyOffsetUnit != pa.polyOffsetUnit) ||
           (useFlatShading != pa.useFlatShading))
            return false;

        // If both null, or both point to the same array, then consider them
        // equivalent. Otherwise do a bytewise check on them for equivalence.
        if(stipple != pa.stipple)
        {
            if(stipple == null || pa.stipple == null)
                return false;

            // ARGH! Do we really have to compare the stipple patterns? Ouch...
            // Let's cheat and only care about the first 1024 bytes.

            for(int i = 0; i < 1024; i++)
                if(stipple[i] != pa.stipple[i])
                    return false;
        }

        return true;
    }
}
