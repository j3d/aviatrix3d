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

package org.j3d.aviatrix3d.rendering;

// External imports
import javax.media.opengl.GL;

// Local imports
// None

/**
 * Marker interface for shader objects that require interaction with the
 * render loop code outside of the normal app-cull-draw cycle.
 * <p>
 *
 * This interface is used by shaders that need some form of initialisation
 * process to take place during the loading cycle or when feedback is required.
 *
 * @author Justin Couch
 * @version $Revision: 2.1 $
 */
public interface ShaderSourceRenderable extends Renderable
{
    /**
     * Perform any initialisation needed at this time because the shader has
     * requested it. Typical initialisation is compiling or linking the shader,
     * but may also include creation of the appropriate object handles etc.
     *
     * @param gl The gl context to draw with
     */
    public void initialize(GL gl);

    /**
     * The user requested log information about the shader object, so now is
     * the time to fetch it.
     *
     * @param gl The gl context to draw with
     */
    public void fetchLogInfo(GL gl);
}
