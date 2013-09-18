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
 * Marker interface for all objects that can render themself given just a GL
 * context and GLU information.
 * <p>
 *
 * Objects that handle their own state independently is usable by both
 * {@link org.j3d.aviatrix3d.Node} and {@link org.j3d.aviatrix3d.NodeComponent}
 * classes.
 * <p>
 *
 * <h3>Assumable Preconditions</h3>
 *
 * <ul>
 * <li>All state required to render this node shall be already available.
 *  For example, if this is the geometry, all the appearance state will
 *  already be set in the pipeline.
 * </li>
 * <li>Any transformations provided by parent transformations will be
 *  already applied to the modelview stack. </li>
 * </ul>
 *
 * <h3>Implementor Requirements</h3>
 *
 * <ul>
 * <li>The methods must be re-entrant as they can be called from multiple
 * places at once. For example, multiple pipes rendering the same object
 * on different screens simultaneously.
 * </li>
 * <li>
 * </li>
 * </ul>
 *
 * <h3>Implementor Guidelines</h3>
 *
 * @author Justin Couch
 * @version $Revision: 2.2 $
 */
public interface ObjectRenderable extends Renderable
{
    /**
     * This method is called to render this node. All OpenGL commands needed
     * to render the node should be executed.
     *
     * @param gl The gl context to draw with
     */
    public void render(GL2 gl);

    /*
     * This method is called after an object has been rendered and should clean
     * up their local state. This method must be re-entrant.
     *
     * @param gl The gl context to draw with
     */
    public void postRender(GL2 gl);
}
