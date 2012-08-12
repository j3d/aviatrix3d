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

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.AppearanceAttributeRenderable;

/**
 * Describes attributes used when interacting with the depth buffer on a
 * per-object level.
 * <p>
 *
 * The default setup for this class is:
 * <p>
 * <ul>
 * <li>Minimum range: 0</li>
 * <li>Maximum range: 1</li>
 * <li>Depth Testing: true</li>
 * <li>Depth Write: true</li>
 * <li>Depth Function: FUNCTION_ALWAYS</li>
 * </ul>
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>invalidDepthFunctionMsg: Error message when the given function type
 *     does not match one of the available types.</li>
 * <li>invalidDepthRangeMsg: Error message when the given depth value is
 *     not in the range [0,1].</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public class DepthAttributes extends NodeComponent
    implements AppearanceAttributeRenderable
{
    /** Message when the stencil operation is not a valid value */
    private static final String INVALID_RANGE_PROP =
        "org.j3d.aviatrix3d.DepthAttributes.invalidDepthRangeMsg";

    /** Message when the buffer function is not a valid value */
    private static final String INVALID_FUNCTION_PROP =
        "org.j3d.aviatrix3d.DepthAttributes.invalidDepthFunctionMsg";

    /** Comparison function that always fails.*/
    public static final int FUNCTION_NEVER = GL.GL_NEVER;

    /** Comparison function that passes if current depth < range. */
    public static final int FUNCTION_LESS = GL.GL_LESS;

    /** Comparison function that passes if current depth <= range. */
    public static final int FUNCTION_LESS_OR_EQUAL = GL.GL_LEQUAL;

    /** Comparison function that passes if current depth > range. */
    public static final int FUNCTION_GREATER = GL.GL_GREATER;

    /** Comparison function that passes if current depth >= range. */
    public static final int FUNCTION_GREATER_OR_EQUAL = GL.GL_GEQUAL;

    /** Comparison function that passes if current depth == range. */
    public static final int FUNCTION_EQUAL = GL.GL_EQUAL;

    /** Comparison function that passes if current depth is not equal range. */
    public static final int FUNCTION_NOTEQUAL = GL.GL_NOTEQUAL;

    /** Comparison function that always passes. */
    public static final int FUNCTION_ALWAYS = GL.GL_ALWAYS;


    /** The minimum range for the depth test */
    private float minRange;

    /** The maximum range for the depth test */
    private float maxRange;

    /** The stencil function mode to use */
    private int function;

    /** Flag indicating if depth testing should be enabled or disabled */
    private boolean depthTest;

    /** Flag indicating if depth writing should be enabled or disabled */
    private boolean depthWrite;

    /**
     * Constructs a state set with default values.
     */
    public DepthAttributes()
    {
        function = FUNCTION_LESS;
        minRange = 0;
        maxRange = 1;

        depthTest = true;
        depthWrite = true;
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
        return DEPTH_ATTRIBUTE;
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
        if(depthTest)
            gl.glEnable(GL.GL_DEPTH_TEST);
        else
            gl.glDisable(GL.GL_DEPTH_TEST);

        gl.glDepthMask(depthWrite);
        gl.glDepthFunc(function);
        gl.glDepthRange(minRange, maxRange);
    }

    /**
     * Restore all openGL state to the given drawable. Does nothing as it
     * assumes the system will disable blending outside of this class.
     *
     * @param gl The gl context to draw with
     */
    public void postRender(GL gl)
    {
        if(!depthTest)
            gl.glEnable(GL.GL_DEPTH_TEST);

        gl.glDepthMask(true);
        gl.glDepthFunc(GL.GL_LESS);
        gl.glDepthRange(0, 1);
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
        DepthAttributes da = (DepthAttributes)o;
        return compareTo(da);
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
        if(!(o instanceof DepthAttributes))
            return false;
        else
            return equals((DepthAttributes)o);

    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Set the flag for whether the depth testing should be enabled or disabled.
     *
     * @param test True if testing should be enabled
     *   state to be used in the current operation
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void enableDepthTest(boolean test)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        depthTest = test;
    }

    /**
     * Check to see if depth testing is currently enabled.
     *
     * @return true when testing is enabled
     */
    public boolean isDepthTestEnabled()
    {
        return depthTest;
    }

    /**
     * Set the flag for whether the depth writing should be enabled or disabled.
     *
     * @param write True if writing should be enabled
     *   state to be used in the current operation
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void enableDepthWrite(boolean write)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        depthWrite = write;
    }

    /**
     * Check to see if depth writing is currently enabled.
     *
     * @return true when writing is enabled
     */
    public boolean isDepthWriteEnabled()
    {
        return depthWrite;
    }

    /**
     * Set minimum depth value that is acceptable to be rendered. The values
     * must be between zero and one. It is not required that the minimum range
     * be less than the max range.
     *
     * @param depth A distance between 0 and 1
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     * @throws IllegalArgumentException The value provided was out of range
     */
    public void setMinRange(float depth)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(depth < 0 || depth > 1)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(INVALID_RANGE_PROP) + depth;
            throw new IllegalArgumentException(msg);
        }

        minRange = depth;
    }

    /**
     * Get the current minimum depth range
     *
     * @return A value between 0 and 1
     */
    public float getMinRange()
    {
        return minRange;
    }

    /**
     * Set maximum depth value that is acceptable to be rendered. The values
     * must be between zero and one. It is not required that the minimum range
     * be less than the max range.
     *
     * @param depth A distance between 0 and 1
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     * @throws IllegalArgumentException The value provided was out of range
     */
    public void setMaxRange(float depth)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(depth < 0 || depth > 1)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(INVALID_RANGE_PROP) + depth;
            throw new IllegalArgumentException(msg);
        }

        maxRange = depth;
    }

    /**
     * Get the current minimum depth range
     *
     * @return A value between 0 and 1
     */
    public float getMaxRange()
    {
        return maxRange;
    }

    /**
     * Set the operation that should be performed if the depth tests pass.
     * This should be one of the FUNCTION_ constants at the top of this class.
     *
     * @param func One of the FUNCTION_ constants
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     * @throws IllegalArgumentException The operation value was not one of the
     *   valid types.
     */
    public void setDepthFunction(int func)
        throws IllegalArgumentException,
               InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        switch(func)
        {
            case FUNCTION_NEVER:
            case FUNCTION_ALWAYS:
            case FUNCTION_LESS:
            case FUNCTION_LESS_OR_EQUAL:
            case FUNCTION_GREATER:
            case FUNCTION_GREATER_OR_EQUAL:
            case FUNCTION_EQUAL:
            case FUNCTION_NOTEQUAL:
                function = func;
                break;

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(INVALID_FUNCTION_PROP) + func;
                throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Get the current operation used when the depth tests pass.
     *
     * @return One of the FUNCTION_ constants
     */
    public int getDepthFunction()
    {
        return function;
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param da The attributes instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(DepthAttributes da)
    {
        if(da == null)
            return 1;

        if(da == this)
            return 0;

        if(depthTest != da.depthTest)
            return depthTest ? 1 : -1;

        if(depthWrite != da.depthWrite)
            return depthWrite ? 1 : -1;

        if(minRange != da.minRange)
            return minRange < da.minRange? -1 : 1;

        if(maxRange != da.maxRange)
            return maxRange < da.maxRange ? -1 : 1;

        if(function != da.function)
            return function < da.function? -1 : 1;

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param da The attributes instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(DepthAttributes da)
    {
        if(da == this)
            return true;

        if((da == null) ||
           (depthTest != da.depthTest) ||
           (depthWrite != da.depthWrite) ||
           (minRange != da.minRange) ||
           (maxRange != da.maxRange)||
           (function != da.function))
            return false;

        return true;
    }
}
