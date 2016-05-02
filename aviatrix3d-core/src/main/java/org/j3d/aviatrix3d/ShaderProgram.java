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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

// Local imports
import org.j3d.aviatrix3d.rendering.DeletableRenderable;
import org.j3d.aviatrix3d.rendering.ShaderComponentRenderable;
import org.j3d.aviatrix3d.rendering.ShaderSourceRenderable;

/**
 * Representation of a complete GLSLang shader program.
 * <p>
 *
 * The behaviour of this object mirrors that of the OpenGL specification. To
 * create a program, you have to collect a number of objects, have them
 * compiled, then assemble and link the objects into a single application. In
 * addition, any runtime structural changes to the program will not take
 * effect until the next time that the program is (re)linked.
 * <p>
 *
 * A program is separate from the arguments that can be passed to it. This
 * class represents the raw shader program that can be called upon to do some
 * processing work. Vertex attribute values are set through the calls in the
 * {@link VertexGeometry} class. Uniform values are defined by the accompanying
 * {@link ShaderArguments} class. This allows a single instance of this program
 * to be created, yet used in different parts of the scene graph with different
 * settings (eg a marble shader that has different colour combos).
 *
 * @author Justin Couch
 * @version $Revision: 2.23 $
 */
public class ShaderProgram extends NodeComponent
    implements DeletableRenderable,
               ShaderSourceRenderable,
               ShaderComponentRenderable
{
    /** Flag indicating if the user has requested a new log string */
    private boolean logRequested;

    /** Flag indicating if we've completed the link process */
    private HashMap<GL, Boolean> linked;

    /** Flag indicating whether the link process should be confirmed */
    private boolean confirmLink;

    /** List of shader objects that are queued to be added at the next link */
    private ArrayList<ShaderObject> pendingAdds;

    /** List of shader objects that are queued to be removed at the next link */
    private ArrayList<ShaderObject> pendingDeletes;

    /** List of all the current shader objects */
    private ArrayList<ShaderObject> currentObjects;

    /** Attribute name string to the Integer index needed */
    private HashMap<String, Integer> attributeNames;

    /** Mapping of GL context to shader program ID */
    private HashMap<GL, Integer> programIdMap;

    /** The last fetched info string, if any */
    private String infoString;

    /**
     * Constructs a Shader program with nothing set.
     */
    public ShaderProgram()
    {
        logRequested = false;
        confirmLink = false;
        currentObjects = new ArrayList<>();
        attributeNames = new HashMap<>();
        programIdMap = new HashMap<>();

        linked = new HashMap<>();
    }

    //---------------------------------------------------------------
    // Methods defined by ShaderComponentRenderable
    //---------------------------------------------------------------

    @Override
    public int getComponentType()
    {
        return PROGRAM_SHADER;
    }

    @Override
    public boolean isValid(GL2 gl)
    {
        Integer p_id = programIdMap.get(gl);
        return (p_id != null);
    }

    @Override
    public int getProgramId(GL2 gl)
    {
        Integer p_id = programIdMap.get(gl);
        return (p_id == null) ? 0 : p_id.intValue();
    }

    @Override
    public void reinitialize(GL2 gl)
    {
        int size = currentObjects.size();
        for(int i = 0; i < size; i++)
        {
            // Not the best. Should not assume the full Object instance
            // here, but the renderable interface instead.
            ShaderObject obj = currentObjects.get(i);
            obj.reinitialize(gl);
        }

        initialize(gl);
    }

    //---------------------------------------------------------------
    // Methods defined by ObjectRenderable
    //---------------------------------------------------------------

    @Override
    public void render(GL2 gl)
    {
        Boolean link = linked.get(gl);

        if(link == null || !link.booleanValue())
            return;

        initialize(gl);

        Integer p_id = programIdMap.get(gl);

        if(p_id == null)
            return;

        gl.glUseProgramObjectARB(p_id.intValue());
    }

    @Override
    public void postRender(GL2 gl)
    {
        gl.glUseProgramObjectARB(0);
    }

    //---------------------------------------------------------------
    // Methods defined by ShaderSourceRenderable
    //---------------------------------------------------------------

    @Override
    public void initialize(GL2 gl)
    {
        Boolean link = linked.get(gl);

        if(link != null && link.booleanValue())
            return;

        boolean new_program = false;

        // Do we need to create a shader program first?
        Integer p_id = programIdMap.get(gl);
        int program_id = 0;

        if(p_id == null)
        {
            program_id = gl.glCreateProgram();
            programIdMap.put(gl, program_id);
            new_program = true;
        }
        else
            program_id = p_id.intValue();

        // detach anything that has been previously removed
        if(!new_program && pendingDeletes != null)
        {
            int size = pendingDeletes.size();
            for (ShaderObject obj : pendingDeletes)
            {
                gl.glDetachObjectARB(program_id, obj.getShaderId(gl));
            }

            pendingDeletes = null;
        }

        // Add anything new
        if(!new_program && pendingAdds != null)
        {
            for (ShaderObject obj : pendingAdds)
            {
                if (!obj.isCompiled(gl))
                    obj.initialize(gl);

                gl.glAttachObjectARB(program_id, obj.getShaderId(gl));
            }

            pendingAdds = null;
        }

        // If this is a new program ID, will need to reattach all the
        // program objects, so go through the whole list and attach here
        if(new_program)
        {
            for(ShaderObject obj : currentObjects)
            {
                if (!obj.isCompiled(gl))
                    obj.initialize(gl);

                gl.glAttachObjectARB(program_id, obj.getShaderId(gl));
            }
        }

        // Assign the attribute names and indices
        if(attributeNames.size() != 0)
        {
            for (Map.Entry<String, Integer> attrib : attributeNames.entrySet())
            {
                gl.glBindAttribLocation(program_id, attrib.getValue(), attrib.getKey());
            }
        }

        // Finally link everything together.
        gl.glLinkProgramARB(program_id);

        if(confirmLink)
        {
            int[] bool = new int[1];
            gl.glGetObjectParameterivARB(program_id,
                                         GL2.GL_OBJECT_LINK_STATUS_ARB,
                                         bool,
                                         0);
            linked.put(gl, (bool[0] == 1) ? Boolean.TRUE : Boolean.FALSE);
        }
        else
            linked.put(gl, Boolean.TRUE);
    }

    @Override
    public void fetchLogInfo(GL2 gl)
    {
        Integer p_id = programIdMap.get(gl);
        if(p_id == null)
            return;

        int program_id = p_id.intValue();

        int[] length = new int[1];

        gl.glGetObjectParameterivARB(program_id,
                                     GL2.GL_OBJECT_INFO_LOG_LENGTH_ARB,
                                     length,
                                     0);

        // Locally allocate for the moment as I don't think this will be
        // called that often.
        if(length[0] > 0)
        {
            byte[] info = new byte[length[0]];
            gl.glGetInfoLogARB(program_id, info.length, length, 0, info, 0);

            infoString = new String(info, 0, length[0]);
        }
        else
            infoString = null;

        logRequested = false;
    }

    //---------------------------------------------------------------
    // Methods defined by DeletableRenderable
    //---------------------------------------------------------------

    @Override
    public void cleanup(GL2 gl)
    {
        Integer p_id = programIdMap.remove(gl);
        if(p_id == null)
            return;

        int program_id = p_id.intValue();

        gl.glDeleteObjectARB(program_id);

        linked.remove(gl);
    }

    //---------------------------------------------------------------
    // Methods defined by SceneGraphObject
    //---------------------------------------------------------------

    @Override
    protected void setUpdateHandler(NodeUpdateHandler handler)
    {
        if(handler == updateHandler)
            return;

        super.setUpdateHandler(handler);

        for (ShaderObject currentObject : currentObjects)
        {
            currentObject.setUpdateHandler(updateHandler);
        }

        if((linked.size() == 0) && (updateHandler != null))
            updateHandler.shaderRequiresInit(this, true);

        if(logRequested && (updateHandler != null))
            updateHandler.shaderRequiresLogInfo(this, true);
    }

    @Override
    protected void setLive(boolean state)
    {
        if(state)
            liveCount++;
        else if(liveCount > 0)
            liveCount--;

        if((liveCount == 0) || !alive)
        {
            for (ShaderObject obj : currentObjects)
            {
                obj.setLive(state);
            }

            super.setLive(state);

            if(!state && updateHandler != null)
                updateHandler.requestDeletion(this);
        }
    }

    //---------------------------------------------------------------
    // Methods defined by Comparable
    //---------------------------------------------------------------

    @Override
    public int compareTo(Object o)
        throws ClassCastException
    {
        ShaderProgram sh = (ShaderProgram)o;
        return compareTo(sh);
    }

    //---------------------------------------------------------------
    // Methods defined by Object
    //---------------------------------------------------------------

    @Override
    public boolean equals(Object o)
    {
        return o instanceof ShaderProgram && equals((ShaderProgram) o);
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Add a shader object to this program. If the shader object is already
     * registered, the request is ignored. The object does not need to be
     * initialised or compiled at this point.
     *
     * @param obj The object instance to add
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void addShaderObject(ShaderObject obj)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(!currentObjects.contains(obj))
        {
            if(pendingAdds == null)
                pendingAdds = new ArrayList<ShaderObject>();

            currentObjects.add(obj);
            pendingAdds.add(obj);
        }
    }

    /**
     * Remove the shader object from this program. If ths shader object is not
     * already registered the request is ignored.
     *
     * @param obj The object instance to remove
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void removeShaderObject(ShaderObject obj)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(currentObjects.contains(obj))
        {
            if(pendingDeletes == null)
                pendingDeletes = new ArrayList<ShaderObject>();

            currentObjects.remove(obj);
            pendingDeletes.add(obj);
        }
    }

    /**
     * Get the number of currently registered shader objects.
     *
     * @return A value >= 0
     */
    public int getNumShaderObjects()
    {
        return currentObjects.size();
    }

    /**
     * Get the current shader objects. The array must be at least
     * {@link #getNumShaderObjects()} in length.
     *
     * @param objects An array to copy values into
     */
    public void getShaderObjects(ShaderObject[] objects)
    {
        currentObjects.toArray(objects);
    }

    /**
     * Request that the shader link at the next available oppourtunity. If any
     * of the used ShaderObjects have not yet been compiled, they will be
     * compiled just before linking.
     *
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void link()
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        linked.clear();

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
     * Bind the given attribute name to a specific index. This index can then
     * be used in the <code>setAttributes()</code> methods of
     * {@link VertexGeometry}. Note that, in accordance with the rules of
     * OpenGL, this binding will not take place until the next link() call.
     *
     * @param name The attribute name to bind the index to
     * @param index The index value for the attrib name to correspond to the
     *   vertex attribute values
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void bindAttributeName(String name, int index)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        attributeNames.put(name, index);
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
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void requestLinkConfirmation(boolean enable)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        confirmLink = enable;
    }

    /**
     * Check to see the current state of whether compilation should be
     * confirmed.
     *
     * @return true if the confirmation needs to take place
     */
    public boolean isLinkConfirmed()
    {
        return confirmLink;
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param sh The program instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(ShaderProgram sh)
    {
        if(sh == null)
            return 1;

        if(sh == this)
            return 0;

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param sh The program instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(ShaderProgram sh)
    {
        if(sh == this)
            return true;

        if(sh == null)
            return false;

        if(currentObjects.size() != sh.currentObjects.size())
            return false;

/*
        int num_objects = currentObjects.size();

        for(int i = 0; i < num_objects; i++)
        {
     *
        // hmmm... what to do here to avoid a O(n^2) op?
        }
*/
        return false;
    }
}
