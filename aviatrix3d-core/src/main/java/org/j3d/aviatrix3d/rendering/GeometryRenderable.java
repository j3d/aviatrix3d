/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2006
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
import com.jogamp.opengl.GL2;

// Local imports
// None

/**
 * Marker interface for the terminal rendering state in OpenGL - a piece of
 * geometry.
 * <p>
 *
 * In OpenGL, sending geometry to the pipeline is the terminal state causing
 * something to be rendered. This interface marks that terminal position.
 * There is only a need for a single rendering call as we don't have other
 * state that needs to be backed out of. It is expected that the geometry
 * implementation will back out of any local state that it may have enabled
 * to do local rendering, such as vertex array client state, VBO binding etc.
 *
 * @author Justin Couch
 * @version $Revision: 2.1 $
 * @since Aviatrix3D 2.0
 */
public interface GeometryRenderable extends Renderable
{
    /**
     * Check to see if this geometry has anything that could be interpreted as
     * an alpha value. For example a Raster with RGBA values or vertex geometry
     * with 4-component colours for the vertices.
     *
     * @return true if there is any form of transparency
     */
    public boolean hasTransparency();

    /**
     * Render the geometry now.
     *
     * @param gl The GL context to render with
     */
    public void render(GL2 gl);
}
