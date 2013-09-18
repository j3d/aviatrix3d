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
 * Describes attributes used when rendering a line.
 * <p>
 *
 * Default size of a line is 1.0 pixels and is not antialiased. The predefined
 * line types are taken from the ISO internation register of line styles. The
 * definitions can be found at
 * <a href="http://jitc.fhu.disa.mil/nitf/graph_reg/graph_reg.htm">
 *  http://jitc.fhu.disa.mil/nitf/graph_reg/graph_reg.htm#LINETYPE</a>.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>negLineWidthMsg: Error message when the requested linewidth <= 0.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.16 $
 */
public class LineAttributes extends NodeComponent
    implements AppearanceAttributeRenderable, DeletableRenderable
{
    /** Message when the line size is <= 0 */
    private static final String LINE_SIZE_PROP =
        "org.j3d.aviatrix3d.LineAttributes.negLineWidthMsg";

    /**
     * Predefined line pattern that is the normal solid line. The register
     * identifier number is 1.
     */
    public static final short PATTERN_SOLID = 0;

    /**
     * Predefined line pattern that is a dashed line. The pattern is defined
     * as repeated 1110 pattern. The register identifier number is 2.
     */
    public static final short PATTERN_DASH = (short)0xEEEE;

    /**
     * Predefined line pattern that is a dotted line. The pattern is defined
     * as repeated 1010 pattern. The register identifier number is 3.
     */
    public static final short PATTERN_DOT = (short)0xAAAA;

    /**
     * Predefined line pattern that is a dash-dot line. The pattern is defined
     * as repeated 1110 0100 pattern. The register identifier number is 4.
     */
    public static final short PATTERN_DASH_DOT = (short)0xE4E4;

    /**
     * Predefined line pattern that is a dash-dot line. The pattern is defined
     * as repeated 1110 1010 pattern. The register identifier number is 5.
     */
    public static final short PATTERN_DASH_DOT_DOT = (short)0xEAEA;

    /**
     * Predefined line pattern that is equal sized dash and space. The pattern
     * is defined as repeated 1100 1100 pattern. The register identifier number
     * is 9.
     */
    public static final short PATTERN_STITCH_LINE = (short)0xCCCC;

    /**
     * Predefined line pattern that uses alternating long and short dashes. The
     * pattern is defined as repeated 1111 0110 pattern. The register identifier
     * number is 10.
     */
    public static final short PATTERN_CHAIN_LINE = (short)0xF6F6;

    /**
     * Predefined line pattern that uses alternating long and short dashes where
     * the beginning and end always end in a long segment. The pattern is
     * defined as repeated 1111 1011 1101 1111 pattern. The register identifier
     * number is 11.
     */
    public static final short PATTERN_CENTER_LINE = (short)0xFBFB;

    // Hidden line pattern is hard to implement correctly. Got an idea?

    /**
     * Predefined line pattern that is a long dash, short dash, short dash line
     * with evenly space dashes and the beginning and the end of a line always
     * ending in a dash of the same length. The pattern is defined as
     * 1110 1110 1110 1111. The register identifier number is 13.
     */
    public static final short PATTERN_PHANTOM_LINE = (short)0xEEEE;


    /** The thickness of the line in pixels. */
    private float lineSize;

    /** Should this be antialiased? */
    private boolean antialias;

    /** Has an attribute changed */
    private boolean stateChanged;

    /** Map of display contexts to maps */
    private HashMap<GL, Integer> displayListMap;

    /** Stipple pattern for the line, if desired */
    private short stipplePattern;

    /** Stipple scale factor for the line */
    private int stippleScale;

    /**
     * Constructs a attribute set with default values.
     */
    public LineAttributes()
    {
        stateChanged = true;
        antialias = false;
        lineSize = 1;
        stippleScale = 1;
        stipplePattern = 0;

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
    @Override
    public int getAttributeType()
    {
        return LINE_ATTRIBUTE;
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

            gl.glNewList(listName.intValue(), GL2.GL_COMPILE);

            gl.glPushAttrib(GL2.GL_LINE_BIT);

            if(antialias)
                gl.glEnable(GL.GL_LINE_SMOOTH);

            if(lineSize != 1)
                gl.glLineWidth(lineSize);

            if(stipplePattern != 0)
            {
                gl.glEnable(GL2.GL_LINE_STIPPLE);
                gl.glLineStipple(stippleScale, stipplePattern);
            }

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
    @Override
    public void postRender(GL2 gl)
    {
        if(antialias)
            gl.glDisable(GL.GL_LINE_SMOOTH);

        if(lineSize != 1)
            gl.glLineWidth(1);

        if(stipplePattern != 0)
            gl.glDisable(GL2.GL_LINE_STIPPLE);

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
    @Override
    public int compareTo(Object o)
        throws ClassCastException
    {
        LineAttributes ta = (LineAttributes)o;
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
        if(!(o instanceof LineAttributes))
            return false;
        else
            return equals((LineAttributes)o);

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
     * Set the size in pixels of the line size. Line size must be greater
     * than zero.
     *
     * @param size The size of the line in pixels
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     * @throws IllegalArgumentException Line size was non-positive
     */
    public void setLineWidth(float size)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(size <= 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(LINE_SIZE_PROP);
            throw new IllegalArgumentException(msg);
        }

        if(lineSize != size)
        {
            lineSize = size;
            stateChanged = true;
        }
    }

    /**
     * Get the current line size in use.
     *
     * @return A value greater than zero
     */
    public float getLineWidth()
    {
        return lineSize;
    }

    /**
     * Set the stipple pattern to be used on the line. A value of zero will
     * clear the current stipple pattern and return to a normal line.
     *
     * @param pattern The bit pattern used to draw the line
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setStipplePattern(short pattern)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(stipplePattern != pattern)
        {
            stateChanged = true;
            stipplePattern = pattern;
        }
    }

    /**
     * Get the current line stipple pattern. A zero value indicates no stipple
     * is applied.
     *
     * @return A value greater than zero
     */
    public short getStipplePattern()
    {
        return stipplePattern;
    }

    /**
     * Set the scale to be applied to the pattern. By default a dot is a single
     * pixel, while a dash is three pixels. This scale can change how big the
     * values are. The default value is 1.
     *
     * @param scale The scale factor to apply to the pattern
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setStippleScaleFactor(int scale)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(stippleScale != scale)
        {
            stateChanged = true;
            stippleScale = scale;
        }
    }

    /**
     * Get the current line stipple pattern. A zero value indicates no stipple
     * is applied.
     *
     * @return A value greater than zero
     */
    public int getStippleScaleFactor()
    {
        return stippleScale;
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param la The attributes instance to be comlared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(LineAttributes la)
    {
        if(la == null)
            return 1;

        if(la == this)
            return 0;

        if(antialias != la.antialias)
            return antialias ? 1 : -1;

        if(lineSize != la.lineSize)
            return lineSize < la.lineSize ? -1 : 1;

        // If both null, or both line to the same array, then consider them
        // equivalent. Otherwise do a bytewise check on them for equivalence.
        if(stipplePattern != la.stipplePattern)
            return stipplePattern < la.stipplePattern ? -1 : 1;

        if(stippleScale != la.stippleScale)
            return stippleScale < la.stippleScale ? -1 : 1;

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param la The attributes instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(LineAttributes la)
    {
        if(la == this)
            return true;

        if((la == null) ||
           (antialias != la.antialias) ||
           (lineSize != la.lineSize) ||
           (stipplePattern != la.stipplePattern) ||
           (stippleScale != la.stippleScale))
            return false;

        return true;
    }
}
