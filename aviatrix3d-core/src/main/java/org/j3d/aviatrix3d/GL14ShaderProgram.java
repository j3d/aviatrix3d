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
import java.util.HashMap;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

// Local imports
import org.j3d.aviatrix3d.rendering.ShaderComponentRenderable;

/**
 * Base class representing a single shader program that can be applied to the
 * rendering pipeline.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.10 $
 * @deprecated When moving to OpenGL 3 or later, these older shaders are not supported.
 */
public abstract class GL14ShaderProgram extends NodeComponent
    implements ShaderComponentRenderable
{
    /** The program string that is stored as the shader */
    protected String programString;

    /** Flag to say that the string has changed and should be recompiled */
    protected boolean programChanged;

    /** Mapping of GL context to shader program ID */
    protected HashMap<GL, Integer> programIdMap;


    /**
     * Constructs a Shader with default values.
     */
    public GL14ShaderProgram()
    {
        programIdMap = new HashMap<GL, Integer>();
    }

    //---------------------------------------------------------------
    // Methods defined by ShaderComponentRenderable
    //---------------------------------------------------------------

    /**
     * Check to see if this is linked for the given GL context. This tests to
     * see if a valid program ID has already been assigned, indicating that at
     * least an internal link(gl) call has been made.
     *
     *
     * @param gl The GL context to test for linkage against
     * @return true if there is a valid ID to work with
     */
    @Override
    public boolean isValid(GL2 gl)
    {
        Integer p_id = programIdMap.get(gl);
        return (p_id != null);
    }

    /**
     * Fetch the ID handle for this program for the given context.
     *
     *
     * @param gl The GL context to get the ID for
     * @return The ID value or 0 if none
     */
    @Override
    public int getProgramId(GL2 gl)
    {
        Integer p_id = programIdMap.get(gl);
        return (p_id == null) ? 0 : p_id.intValue();
    }

    /**
     * Re-initialise this shader because the underlying GL context has
     * changed. This should also reinitialise any resources that it is
     * dependent on.
     *
     * @param gl The GL context to reinitialise with
     */
    @Override
    public void reinitialize(GL2 gl)
    {
        // Do nothing for these cases.
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Set the program string that is to be registered by this shader. Setting
     * a value of null will clear the current shader and prevent it from being
     * rendered next frame.
     *
     * @param str The new program string to be registered or null
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setProgramString(String str)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(programString != str)
        {
            programChanged = true;
            programString = str;
        }
    }

    /**
     * Get the currently set program string. If none is set, return null.
     *
     * @return The current string or null
     */
    public String getProgramString()
    {
        return programString;
    }
}
