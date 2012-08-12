/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004-2005
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
 * Marker interface for {@link org.j3d.aviatrix3d.NodeComponent} classes that
 * need to render themselves using some extra external identifier.
 * <p>
 *
 * The generation and interpretation of the external data is dependent on
 * the specific derived type. For example, for lights, the data represents the
 * GL light ID (eg GL_LIGHT0) to be used for the glEnable() call. The data
 * may be generated as part of the rendering stage or sorting stage, depending
 * on the object. Refer to the individual implementing class documentation for
 * more details.
 *
 * @author Justin Couch
 * @version $Revision: 2.2 $
 */
public interface ComponentRenderable extends Renderable
{
    /**
     * Render the object using the provided of external system data value.
     * Typically the custom data is an Integer with the specific object ID to
     * use - such as Light ID used by OpenGL for
     * <code>glEnable(GL_LIGHTX)</code>. Since the active ID for this
     * node may vary over time, a fixed ID cannot be used by OpenGL or the node
     * internals.
     *
     * @param gl The GL context to render with
     * @param externalData Some implementation-specific external data to
     *   aid in the rendering
     */
    public void render(GL gl, Object externalData);

    /*
     * Method to turn off the rendering state for the given ID that corresponds
     * to the input given by the initial render call.
     *
     * @param gl The GL context to render with
     * @param externalData Some implementation-specific external data to
     *   aid in the rendering
     */
    public void postRender(GL gl, Object externalData);
}
