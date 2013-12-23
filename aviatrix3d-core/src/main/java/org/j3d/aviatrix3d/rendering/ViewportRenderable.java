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
import javax.media.opengl.GL2;

// Local imports
// None

/**
 * Renderable representation of a modification of the viewport settings used
 * in a single pass of the multipass rendering.
 * <p>
 *
 * The viewport can be modified in an individual pass to achieve unusual
 * effects.
 *
 * @author Justin Couch
 * @version $Revision: 2.1 $
 */
public interface ViewportRenderable extends Renderable
{
    /**
     * Render the viewport changes now
     *
     * @param gl The GL context to render with
     */
    public void render(GL2 gl);
}
