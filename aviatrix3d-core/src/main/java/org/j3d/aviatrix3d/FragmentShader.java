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

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

// Local imports


/**
 * Node that handles an ARB fragment shader.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.16 $
 */
@Deprecated
public class FragmentShader extends GL14ShaderProgram
{
    /**
     * Constructs a TexCoordGeneration with default values.
     */
    public FragmentShader()
    {
    }

    //---------------------------------------------------------------
    // Methods defined by ShaderComponentRenderable
    //---------------------------------------------------------------

    /**
     * Get the type of component this state represents.
     *
     * @return One of the _SHADER constants
     */
    @Override
    public int getComponentType()
    {
        return FRAGMENT_SHADER;
    }

    //---------------------------------------------------------------
    // Methods defined by ObjectRenderable
    //---------------------------------------------------------------

    /**
     * Set up the rendering state now.
     *
     * @param gl The gl context to draw with
     */
    @Override
    public void render(GL2 gl)
    {
        if(programString == null)
            return;

        Integer p_id = programIdMap.get(gl);

        if(p_id == null)
        {
            // do we need to re-generate the program?
            if(programChanged)
            {
                int[] prog_tmp = new int[1];
                gl.glGenProgramsARB(1, prog_tmp, 0);
                int program_id = prog_tmp[0];

                programIdMap.put(gl, new Integer(program_id));

                gl.glBindProgramARB(GL2.GL_FRAGMENT_PROGRAM_ARB, program_id);
                gl.glProgramStringARB(GL2.GL_FRAGMENT_PROGRAM_ARB,
                                      GL2.GL_PROGRAM_FORMAT_ASCII_ARB,
                                      programString.length(),
                                      programString);

                // TODO: This won't work in a multiscreen/context environment.
                programChanged = false;
            }
            else
                return;
        }
        else
            gl.glBindProgramARB(GL2.GL_FRAGMENT_PROGRAM_ARB, p_id.intValue());

        gl.glEnable(GL2.GL_FRAGMENT_PROGRAM_ARB);
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
        gl.glDisable(GL2.GL_FRAGMENT_PROGRAM_ARB);
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
        FragmentShader sh = (FragmentShader)o;
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
        if(!(o instanceof FragmentShader))
            return false;
        else
            return equals((FragmentShader)o);
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param sh The shader instances to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(FragmentShader sh)
    {
        if(sh == null)
            return 1;

        if(sh == this)
            return 0;

        // compare the two strings first
        if(programString != sh.programString)
        {
            if(programString == null)
                return -1;
            else if(sh.programString == null)
                return 1;

            int res = programString.compareTo(sh.programString);
            if(res != 0)
                return res;
        }

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param sh The shader instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(FragmentShader sh)
    {
        if(sh == this)
            return true;

        if(sh == null)
            return false;

        if(programString != sh.programString)
        {
            if((programString == null) ||
               !programString.equals(sh.programString))
                return false;
        }

        return true;
    }
}
