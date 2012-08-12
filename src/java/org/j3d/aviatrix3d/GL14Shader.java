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
import org.j3d.aviatrix3d.rendering.ComponentRenderable;
import org.j3d.aviatrix3d.rendering.ShaderComponentRenderable;
import org.j3d.aviatrix3d.rendering.ShaderRenderable;

/**
 * Shader container object for the shaders usable with OpenGL 1.4 with the
 * separate vertex and fragment shader code.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.14 $
 */
public class GL14Shader extends Shader
    implements ShaderRenderable
{
    /** The vertex shader used by the appearance */
    private VertexShader vertexShader;

    /** The fragment shader used by the appearance */
    private FragmentShader fragShader;

    /**
     * Constructs a Shader with default values.
     */
    public GL14Shader()
    {
    }

    //---------------------------------------------------------------
    // Methods defined by ShaderRenderable
    //---------------------------------------------------------------

    /**
     * Get an object that represents arguments that should be passed along
     * with the shader. If the shader has a full program component renderable.
     * then it will most likely have arguments too.
     *
     * @return An object representing any global argument lists
     */
    public ComponentRenderable getArgumentsRenderable()
    {
        return null;
    }

    /**
     * Get the component of this shader, if it has one. If the given type
     * is not recognised by this shader, return null.
     *
     * @param type One of the _SHADER constants from
     *    {@link ShaderComponentRenderable}
     * @return A matching component or null if none
     */
    public ShaderComponentRenderable getShaderRenderable(int type)
    {
        switch(type)
        {
            case ShaderComponentRenderable.FRAGMENT_SHADER:
                return fragShader;

            case ShaderComponentRenderable.VERTEX_SHADER:
                return vertexShader;

            default:
                return null;
        }
    }

    //---------------------------------------------------------------
    // Methods defined by ObjectRenderable
    //---------------------------------------------------------------

    /**
     * Issue ogl commands needed for this component.
     *
     * @param gl The gl context to draw with
     */
    public void render(GL gl)
    {
        if(vertexShader != null)
            vertexShader.render(gl);

        if(fragShader != null)
            fragShader.render(gl);
    }

    /**
     * Restore all openGL state.
     *
     * @param gl The gl context to draw with
     */
    public void postRender(GL gl)
    {
        if(fragShader != null)
            fragShader.postRender(gl);

        if(vertexShader != null)
            vertexShader.postRender(gl);
    }

    //---------------------------------------------------------------
    // Methods defined by SceneGraphObject
    //---------------------------------------------------------------

    /**
     * Notification that this object is live now. Overridden to make sure that
     * the live state of the nodes represents the same state as the parent
     * scene graph.
     *
     * @param state true if this should be marked as live now
     */
    protected void setLive(boolean state)
    {
        if(state)
            liveCount++;
        else if(liveCount > 0)
            liveCount--;

        if((liveCount == 0) || !alive)
        {
            super.setLive(state);

            if(vertexShader != null)
                vertexShader.setLive(state);

            if(fragShader != null)
                fragShader.setLive(state);
        }
    }

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

        if(fragShader != null)
            fragShader.setUpdateHandler(updateHandler);

        if(vertexShader != null)
            vertexShader.setUpdateHandler(updateHandler);
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
    public int compareTo(Object o)
        throws ClassCastException
    {
        GL14Shader sh = (GL14Shader)o;
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
        if(!(o instanceof GL14Shader))
            return false;
        else
            return equals((GL14Shader)o);

    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Set the vertexShader to use.  null will clear the vertexShader
     *
     * @param shader The new shader instance to use or null
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setVertexShader(VertexShader shader)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(vertexShader != null)
            vertexShader.setLive(false);

        vertexShader = shader;

        if(vertexShader != null)
        {
            vertexShader.setUpdateHandler(updateHandler);
            vertexShader.setLive(alive);
        }
    }

    /**
     * Get the current vertexShader in use.
     *
     * @return The current shader instance or null
     */
    public VertexShader getVertexShader()
    {
        return vertexShader;
    }

    /**
     * Set the fragment shader to use.  null will clear the shader.
     *
     * @param shader The new shader instance to use or null
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setFragmentShader(FragmentShader shader)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(fragShader != null)
            fragShader.setLive(false);

        fragShader = shader;

        if(fragShader != null)
        {
            fragShader.setUpdateHandler(updateHandler);
            fragShader.setLive(alive);
        }
    }

    /**
     * Get the current fragShader in use.
     *
     * @return The current shader instance or null
     */
    public FragmentShader getFragmentShader()
    {
        return fragShader;
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param sh The shader instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(GL14Shader sh)
    {
        if(sh == this)
            return 0;

        if(sh == null)
            return 1;

        if(vertexShader != sh.vertexShader)
        {
            if(vertexShader == null)
               return -1;
            else if(sh.vertexShader == null)
                return 1;

            int res = vertexShader.compareTo(sh.vertexShader);
            if(res != 0)
                return res;
        }

        if(fragShader != sh.fragShader)
        {
            if(fragShader == null)
               return -1;
            else if(sh.fragShader == null)
                return 1;

            int res = fragShader.compareTo(sh.fragShader);
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
    public boolean equals(GL14Shader sh)
    {
        if(sh == this)
            return true;

        if((sh == null) ||
           ((vertexShader != null) && !vertexShader.equals(sh.vertexShader)) ||
           ((fragShader != null) && !fragShader.equals(sh.fragShader)))
           return false;

        return true;
    }
}
