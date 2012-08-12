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

// Local imports
import org.j3d.aviatrix3d.rendering.ShaderComponentRenderable;

/**
 * Node that handles Vertex shader implementation.
 * <p>
 *
 * The implementation is relatively simple using just a bind program call followed
 * by the parameter setting. If the application needs greater control than this,
 * such as multi-pass rendering, then derive this class and change the render-loop
 * code to do what is required.
 * <p>
 *
 * Parameters follow the OpenGL model. There are 96 evironment parameters and 96
 * local parameters. Both can be set through this class, though this may change in
 * a future design revision, to make environment parameters into a global setting.
 * <p>
 *
 * Though OpenGL can take the attributes as doubles, this is not supported by this
 * API currently.
 *
 * @author Justin Couch
 * @version $Revision: 1.23 $
 */
public class VertexShader extends GL14ShaderProgram
    implements ShaderComponentRenderable
{

    /** The global environment attributes used by the shader */
    private float[][] envParameters;

    /** The local environment attributes used by the shader */
    private float[][] localParameters;

    /** List of the valid environment parameters */
    private int[] validEnvironments;

    /** The number of valid items in the valid environment params list */
    private int numValidEnvironments;

    /** List of the valid local parameters */
    private int[] validLocals;

    /** The number of valid items in the valid local params list */
    private int numValidLocals;

    /**
     * Constructs a Vertex shader with default values. The internal arrays
     * for vertex attributes will be 16 and environment and local parameters
     * to 96.
     */
    public VertexShader()
    {
        this(16, 96);
    }

    /**
     * Create a vertext shader instance with a guaranteed maximum parameter
     * list size. If an attempt is made to read/set past this, an exception
     * will generated. This ID is the maximum parameter index that will be
     * used and the maxium number actually used.
     *
     * @param paramListSize The maximum number of environment and local
     *   parameters that will be used.
     * @param attrListSize The maximum number of vertex attributes that will
     *    be used
     */
    public VertexShader(int attrListSize, int paramListSize)
        throws InvalidWriteTimingException
    {
        envParameters = new float[paramListSize][];
        localParameters = new float[paramListSize][];

        validEnvironments = new int[paramListSize];
        validLocals = new int[paramListSize];
    }

    //---------------------------------------------------------------
    // Methods defined by ShaderComponentRenderable
    //---------------------------------------------------------------

    /**
     * Get the type of component this state represents.
     *
     * @return One of the _SHADER constants
     */
    public int getComponentType()
    {
        return VERTEX_SHADER;
    }

    //---------------------------------------------------------------
    // Methods defined by ObjectRenderable
    //---------------------------------------------------------------

    /**
     * Set up the rendering state now.
     *
     * @param gl The gl context to draw with
     */
    public void render(GL gl)
    {
        if(programString == null)
            return;

        Integer p_id = (Integer)programIdMap.get(gl);

        if(p_id == null)
        {
            // do we need to re-generate the program?
            if(programChanged)
            {
                int[] prog_tmp = new int[1];
                gl.glGenProgramsARB(1, prog_tmp, 0);
                int program_id = prog_tmp[0];

                gl.glBindProgramARB(GL.GL_VERTEX_PROGRAM_ARB, program_id);
                gl.glProgramStringARB(GL.GL_VERTEX_PROGRAM_ARB,
                                      GL.GL_PROGRAM_FORMAT_ASCII_ARB,
                                      programString.length(),
                                      programString);
                // TODO: This won't work in a multiscreen/context environment.
                programChanged = false;
            }
            else
                return;
        }
        else
            gl.glBindProgramARB(GL.GL_FRAGMENT_PROGRAM_ARB, p_id.intValue());

        for(int i = 0; i < numValidEnvironments; i++)
            gl.glProgramEnvParameter4fvARB(GL.GL_VERTEX_PROGRAM_ARB,
                                           validEnvironments[i],
                                           envParameters[validEnvironments[i]],
                                           0);

        for(int i = 0; i < numValidLocals; i++)
            gl.glProgramEnvParameter4fvARB(GL.GL_VERTEX_PROGRAM_ARB,
                                           validLocals[i],
                                           localParameters[validLocals[i]],
                                           0);

        gl.glEnable(GL.GL_VERTEX_PROGRAM_ARB);
    }

    /*
     * This method is called after a node has been rendered.  This method
     * must be re-entrant.
     *
     * @param gl The gl context to draw with
     */
    public void postRender(GL gl)
    {
        gl.glDisable(GL.GL_VERTEX_PROGRAM_ARB);
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
        VertexShader sh = (VertexShader)o;
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
    public boolean equals(Object o)
    {
        if(!(o instanceof VertexShader))
            return false;
        else
            return equals((VertexShader)o);
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Set the environment parameter as a float array.
     *
     * @param idx The index of the parameter to set
     * @param value A float array 4 in length of the values to use
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setEnvironmentParam(int idx, float[] value)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(envParameters[idx] == null)
            envParameters[idx] = new float[4];

        envParameters[idx][0] = value[0];
        envParameters[idx][1] = value[1];
        envParameters[idx][2] = value[2];
        envParameters[idx][3] = value[3];

        // is it in the list already? If not, add it
        boolean valid = false;
        for(int i = 0; i < numValidEnvironments; i++)
        {
            if(validEnvironments[i] == idx)
            {
                valid = true;
                break;
            }
        }

        if(!valid)
            validEnvironments[numValidEnvironments++] = idx;
    }

    /**
     * Set the local parameter as a float array.
     *
     * @param idx The index of the parameter to set
     * @param value A float array 4 in length of the values to use
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setLocalParam(int idx, float[] value)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(localParameters[idx] == null)
            localParameters[idx] = new float[4];

        localParameters[idx][0] = value[0];
        localParameters[idx][1] = value[1];
        localParameters[idx][2] = value[2];
        localParameters[idx][3] = value[3];

        // is it in the list already? If not, add it
        boolean valid = false;
        for(int i = 0; i < numValidLocals; i++)
        {
            if(validLocals[i] == idx)
            {
                valid = true;
                break;
            }
        }

        if(!valid)
            validLocals[numValidLocals++] = idx;
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param sh The shader instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(VertexShader sh)
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

        if(numValidEnvironments != sh.numValidEnvironments)
            return numValidEnvironments < sh.numValidEnvironments ? -1 : 1;
        else
        {
            for(int i = 0; i < numValidEnvironments; i++)
            {
                int idx1 = validEnvironments[i];
                int idx2 = sh.validEnvironments[i];

                if(idx1 != idx2)
                    return idx1 < idx2 ? -1 : 1;

                int res = compareVector(envParameters[idx1],
                                        sh.envParameters[idx2]);
                if(res != 0)
                    return res;
            }
        }

        if(numValidLocals != sh.numValidLocals)
            return numValidLocals < sh.numValidLocals ? -1 : 1;
        else
        {
            for(int i = 0; i < numValidLocals; i++)
            {
                int idx1 = validLocals[i];
                int idx2 = sh.validLocals[i];

                if(idx1 != idx2)
                    return idx1 < idx2 ? -1 : 1;

                int res = compareVector(localParameters[idx1],
                                        sh.localParameters[idx2]);
                if(res != 0)
                    return res;
            }
        }

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param sh The shader instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(VertexShader sh)
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

        if(numValidEnvironments == sh.numValidEnvironments)
        {
            for(int i = 0; i < numValidEnvironments; i++)
            {
                int idx1 = validEnvironments[i];
                int idx2 = sh.validEnvironments[i];

                if((idx1 != idx2) ||
                   !equalsVector(envParameters[idx1],
                                 sh.envParameters[idx2]))
                    return false;
            }
        }

        if(numValidLocals == sh.numValidLocals)
        {
            for(int i = 0; i < numValidLocals; i++)
            {
                int idx1 = validLocals[i];
                int idx2 = sh.validLocals[i];

                if((idx1 != idx2) ||
                   !equalsVector(localParameters[idx1],
                                 sh.localParameters[idx2]))
                    return false;
            }
        }

        return true;
    }

    /**
     * Compare 2 vector arrays of length 4 for equality
     *
     * @param a The first colour array to check
     * @param b The first colour array to check
     * @return -1 if a[i] < b[i], +1 if a[i] > b[i], otherwise 0
     */
    private int compareVector(float[] a, float[] b)
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
     * Compare 2 vector arrays of length 4 for equality
     *
     * @param a The first colour array to check
     * @param b The first colour array to check
     * @return true if they have the same values, false otherwise
     */
    private boolean equalsVector(float[] a, float[] b)
    {
        return (a[0] == b[0]) && (a[1] == b[1]) &&
               (a[2] == b[2]) && (a[3] == b[3]);
    }
}
