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

package org.j3d.aviatrix3d.rendering;

// External imports
import javax.media.opengl.GL2;

// Local imports
// None

/**
 * A renderable object that contributes to part of a shader.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 2.2 $
 */
public interface ShaderComponentRenderable extends ObjectRenderable
{
    /** The component represents a vertex shader */
    public static final int VERTEX_SHADER = 1;

    /** The component represents a fragment shader */
    public static final int FRAGMENT_SHADER = 2;

    /** The component represents a geometry shader */
    public static final int GEOMETRY_SHADER = 3;

    /** The component represents a complete GLSL shader program */
    public static final int PROGRAM_SHADER = 4;

    /**
     * Get the type of component this state represents.
     *
     * @return One of the _SHADER constants
     */
    public int getComponentType();

    /**
     * Check to see if this is linked for the given GL context. This tests to
     * see if a valid program ID has already been assigned, indicating that at
     * least an internal link(gl) call has been made.
     *
     *
     * @param gl The GL context to test for linkage against
     * @return true if there is a valid ID to work with
     */
    public boolean isValid(GL2 gl);

    /**
     * Fetch the ID handle for this program for the given context.
     *
     *
     * @param gl The GL context to get the ID for
     * @return The ID value or 0 if none
     */
    public int getProgramId(GL2 gl);

    /**
     * Re-initialise this shader because the underlying GL context has
     * changed. This should also reinitialise any resources that it is
     * dependent on.
     *
     * @param gl The GL context to reinitialise with
     */
    public void reinitialize(GL2 gl);
}
