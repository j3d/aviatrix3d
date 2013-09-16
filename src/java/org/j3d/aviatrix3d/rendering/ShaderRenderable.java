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
import javax.media.opengl.GL;

// Local imports
// None

/**
 * A renderable object that is a programmable shader.
 * <p>
 *
 * The shader may be either single combined object, like the GLSL shaders,
 * or consist of components, such as the earlier GL 1.4 extensions.
 *
 * @author Justin Couch
 * @version $Revision: 2.2 $
 */
public interface ShaderRenderable extends ObjectRenderable
{
    /**
     * Get an object that represents arguments that should be passed along
     * with the shader. If the shader has a full program component renderable.
     * then it will most likely have arguments too.
     *
     * @return An object representing any global argument lists
     */
    public ComponentRenderable getArgumentsRenderable();

    /**
     * Get the component of this shader, if it has one. If the given type
     * is not recognised by this shader, return null.
     *
     * @param type One of the _SHADER constants from
     *    {@link ShaderComponentRenderable}
     * @return A matching component or null if none
     */
    public ShaderComponentRenderable getShaderRenderable(int type);
}
