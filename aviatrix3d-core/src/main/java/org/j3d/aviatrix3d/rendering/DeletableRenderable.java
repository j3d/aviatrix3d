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
import javax.media.opengl.GL2;

// Local imports
// None

/**
 * Marker interface that permits an object to have a known, well-defined method
 * for being called to clean up it's internal state during the OpenGL rendering
 * cycle.
 * <p>
 *
 * This interface is used by objects such as textures, that should have an
 * explicit cleanup stage inside the OpenGL rendering loop.
 *
 * @author Justin Couch
 * @version $Revision: 2.1 $
 */
public interface DeletableRenderable extends Renderable
{
    /**
     * Cleanup the object now for the given GL context.
     *
     * @param gl The gl context to draw with
     */
    public void cleanup(GL2 gl);
}
