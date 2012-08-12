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

import javax.media.opengl.GL;

// Local imports
import org.j3d.aviatrix3d.rendering.ShaderSourceRenderable;

/**
 * Representation of a single Shader Object code that will form the final
 * shader program.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.17 $
 */
public class ShaderObject extends SceneGraphObject
    implements ShaderSourceRenderable
{
    /** Source for the shader object when set */
    private String[] sourceStrings;

    /** The ID of the shader object that has been allocated here */
    private HashMap<GL, Integer> objectIdMap;

    /** Is this a fragment or vertex program being represented here? */
    private boolean vertexSource;

    /** Flag indicating the current compiled state. */
    private HashMap<GL, Boolean> compiled;

    /** Flag indicating whether the compilation process should be confirmed */
    private boolean confirmCompile;

    /** Flag indicating if the user has requested a new log string */
    private boolean logRequested;

    /** The last fetched info string, if any */
    private String infoString;

    /**
     * Constructs a Shader Object of the specific kind. Shader objects must be
     * one of fragment or vertex. A value of true for the argument will set
     * this object to be a vertex shader. A value of false, will make it a
     * fragment shader.
     */
    public ShaderObject(boolean isVertexShader)
    {
        vertexSource = isVertexShader;
        logRequested = false;
        confirmCompile = false;
        compiled = new HashMap<GL, Boolean>();
        objectIdMap = new HashMap<GL, Integer>();
    }

    //---------------------------------------------------------------
    // Methods defined by SceneGraphObject
    //---------------------------------------------------------------

    /**
     * Set the scenegraph update handler for this node.  It will notify
     * all its children of the value. A null value will clear the current
     * handler.
     *
     * @param handler The instance to use as a handler
     */
    protected void setUpdateHandler(NodeUpdateHandler handler)
    {
        super.setUpdateHandler(handler);

        if((compiled.size() == 0) && (updateHandler != null))
            updateHandler.shaderRequiresInit(this, true);

        if(logRequested && (updateHandler != null))
            updateHandler.shaderRequiresLogInfo(this, true);
    }

    //---------------------------------------------------------------
    // Methods defined by ShaderSourceRenderable
    //---------------------------------------------------------------

    /**
     * Internal method to have the containing ShaderProgram request a compile
     * of this object, if available. If the code has already been compiled, do
     * nothing.
     *
     * @param gl The gl context to draw with
     */
    public void initialize(GL gl)
    {
        Boolean comp = compiled.get(gl);
        if((comp != null && comp.booleanValue()) || sourceStrings == null)
            return;

        // Do we need to create a shader object first?
        Integer o_id = objectIdMap.get(gl);
        int object_id = 0;

        if(o_id == null)
        {
            int type = vertexSource ?
                       GL.GL_VERTEX_SHADER_ARB :
                       GL.GL_FRAGMENT_SHADER_ARB;

            object_id = gl.glCreateShaderObjectARB(type);
            objectIdMap.put(gl, new Integer(object_id));
        }
        else
            object_id = o_id.intValue();

        gl.glShaderSourceARB(object_id,
                             sourceStrings.length,
                             sourceStrings,
                             (int[])null,
                             0);

        gl.glCompileShaderARB(object_id);

        if(confirmCompile)
        {
            int[] bool = new int[1];
            gl.glGetObjectParameterivARB(object_id,
                                         GL.GL_OBJECT_COMPILE_STATUS_ARB,
                                         bool,
                                         0);
            compiled.put(gl, (bool[0] == 1) ? Boolean.TRUE : Boolean.FALSE);
        }
        else
            compiled.put(gl, Boolean.TRUE);
    }

    /**
     * The user requested log information about the shader object, so now is
     * the time to fetch it.
     *
     * @param gl The gl context to draw with
     */
    public void fetchLogInfo(GL gl)
    {
        Integer o_id = objectIdMap.get(gl);
        if(o_id == null)
            return;

        int object_id = o_id.intValue();

        int[] length = new int[1];

        gl.glGetObjectParameterivARB(object_id,
                                     GL.GL_OBJECT_INFO_LOG_LENGTH_ARB,
                                     length,
                                     0);

        // Locally allocate for the moment as I don't think this will be
        // called that often.
        if(length[0] > 0)
        {
            byte[] info = new byte[length[0]];
            gl.glGetInfoLogARB(object_id, info.length, length, 0, info, 0);

            infoString = new String(info, 0, length[0]);
        }
        else
            infoString = null;

        logRequested = false;
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
        ShaderObject abs = (ShaderObject)o;
        return compareTo(abs);
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
        if(!(o instanceof ShaderObject))
            return false;
        else
            return equals((ShaderObject)o);
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Query to find out whether this is a fragment or vertex shader object
     * encapsulation.
     *
     * @return true if this is a vertex shader, false for fragment
     */
    public boolean isVertexShader()
    {
        return vertexSource;
    }

    /**
     * Set the program string that is to be registered by this shader. Setting
     * a value of null will be ignored, unless the object has not yet been
     * compiled. A copy of the arrays is made.
     * <p>
     *
     * Setting a new set of source strings does not automatically imply that
     * the code should be recompiled at the next available oppourtunity. To
     * force a recompilation (and thus any downstream effects like linking)
     * you will also need to call the {@link #compile()} method
     * as well, after calling this one.
     *
     * @param str The new program string(s) to be registered or null
     * @param numValid The number of valid strings to use from the array
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setSourceStrings(String[] str, int numValid)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if((str == null) || (str.length == 0) || (numValid == 0))
            sourceStrings = null;
        else
        {
            sourceStrings = new String[numValid];
            System.arraycopy(str, 0, sourceStrings, 0, numValid);
        }
    }

    /**
     * Get the currently set source strings. If none is set, or it has been
     * cleared already, return null. This returns the reference to the internal
     * array, so don't modify anything in the array.
     *
     * @return The current string or null
     */
    public String[] getSourceStrings()
    {
        return sourceStrings;
    }

    /**
     * Clear the source string so that it is no longer needed. Once the
     * source has been compiled by OpenGL (by calling glCompileShader), the
     * string is no longer needed by the application level code.  It can be
     * safely removed with no harmful effects to the application.
     *
     * @throws InvalidWriteTimingException An attempt was made to clear outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void clearSourceStrings()
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        sourceStrings = null;
    }

    /**
     * Mark this code as needing compilation at the next available
     * oppourtunity. This will queue the shader up to be compiled at the next
     * frame render cycle.
     */
    public void compile()
    {
        compiled.clear();

        if(updateHandler != null)
            updateHandler.shaderRequiresInit(this, false);
    }

    /**
     * Request that the shader fetch the last run log of this program. This
     * will queue up a request for the next frame, which will find the log
     * string. The result of this will be found in the getLastInfoLog() method.
     * If this object is not part of a live scene graph, the request is
     * ignored.
     *
     * @throws InvalidWriteTimingException An attempt was made to request this
     *     outside of the updateSceneGraph() call
     */
    public void requestInfoLog()
        throws InvalidWriteTimingException
    {
        if(updateHandler != null && !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(getAppUpdateWriteTimingMessage());

        if(updateHandler != null)
            updateHandler.shaderRequiresLogInfo(this, false);

        logRequested = true;
    }

    /**
     * Get the last fetched information log from this shader. If the log has
     * not yet been fetched then null is returned.
     *
     * @return The last info log string or null
     */
    public String getLastInfoLog()
    {
        return infoString;
    }

    /**
     * Query the current compilation state. An object is compiled if the
     * current source string has undergone the OpenGL compilation process and
     * was successful. It is not compiled if the previous string was compiled
     * and then a new set of source strings has been set since and the code has
     * been marked as needing compilation.
     *
     * @return true if the code is compiled and no changes
     * @deprecated Should not be called any more by client code.
     */
    public boolean isCompiled()
    {
        return compiled.size() != 0;
    }

    /**
     * Query the current compilation state for this code under the given GL
     * context. An object is compiled if the
     * current source string has undergone the OpenGL compilation process and
     * was successful. It is not compiled if the previous string was compiled
     * and then a new set of source strings has been set since and the code has
     * been marked as needing compilation.
     *
     * @param gl The GL context to check compilation on
     * @return true if the code is compiled and no changes
     */
    protected boolean isCompiled(GL gl)
    {
        Boolean comp = compiled.get(gl);
        return (comp != null && comp.booleanValue());
    }

    /**
     * Not implemented yet.
     */
    public void markForDeletion()
    {
    }

    /**
     * Have the class confirm whether or not the source successfully compiled.
     * The standard OpenGL calls are non-blocking when a compile takes place.
     * It is not known whether the compile has succeeded until some
     * indeterminant time later. That is the normal process this class takes.
     * If the end user wants to first comfirm that the compile succeeds before
     * returning to executing the rest of the render loop, then set this flag.
     * In doing so, expect performance penalties for the frame that does the
     * work of compiling this. Individually, this may not be significant, but
     * with a number of objects to compile and shader programs to link in a
     * single frame could cause a large pause.
     *
     * @param enable true if the code should confirm compilation succeeds
     */
    public void requestCompilationConfirmation(boolean enable)
    {
        confirmCompile = enable;
    }

    /**
     * Check to see the current state of whether compilation should be
     * confirmed.
     *
     * @return true if the confirmation needs to take place
     */
    public boolean isCompilationConfirmed(boolean enable)
    {
        return confirmCompile;
    }

    /**
     * Request the object_id for this shader object so that it can be linked
     * with the containing program.
     *
     * @param gl The GL context to used for the caller
     * @return The object_id of the compiled source or 0 if not compiled yet
     */
    protected int getShaderId(GL gl)
    {
        Integer o_id = (Integer)objectIdMap.get(gl);

        return (o_id == null) ? 0 : o_id.intValue();
    }

    /**
     * Re-initialise this shader because the underlying GL context has
     * changed. This should also reinitialise any resources that it is
     * dependent on.
     *
     * @param gl The GL context to reinitialise with
     */
    void reinitialize(GL gl)
    {
        initialize(gl);
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param so The object instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(ShaderObject so)
    {
        if(so == null)
            return 1;

        if(so == this)
            return 0;

        if(sourceStrings != so.sourceStrings)
        {
            if(sourceStrings == null)
                return -1;
            else if(so.sourceStrings == null)
                return 1;

            // Not quite correct if the same strings are used, but in different
            // order.
            for(int i = 0; i < sourceStrings.length; i++)
            {
                int res = sourceStrings[i].compareTo(so.sourceStrings[i]);
                if(res != 0)
                    return res;
            }
        }

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param so The object instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(ShaderObject so)
    {
        if(so == this)
            return true;

        if(so == null)
            return false;

        if(sourceStrings != so.sourceStrings)
        {
            if(sourceStrings == null || so.sourceStrings == null)
                return false;

            // Not quite correct if the same strings are used, but in different
            // order.
            for(int i = 0; i < sourceStrings.length; i++)
            {
                if(!sourceStrings[i].equals(so.sourceStrings[i]))
                    return false;
            }
        }

        return true;
    }
}
