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
import javax.media.opengl.GL;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;

// Local imports
// None

/**
 * Extended version of the basic geometry renderable for geometry classes
 * that provide the option for internal per-primitive sorting each frame.
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
 * <b>Implementation Requirements</b>
 * <p>
 *
 * Geometry that implement this interface should also be able to function
 * as normal unsorted geometry when the pipeline process does not support
 * any form of depth sorting.
 *
 * @author Justin Couch
 * @version $Revision: 2.1 $
 * @since Aviatrix3D 2.0
 */
public interface CustomGeometryRenderable extends GeometryRenderable
{
    /**
     * Process the sorting on this node now. If needed some rendering
     * information that will be needed during real rendering can be returned.
     * If there is no information, return null.
     *
     * @param vworldTx The transformation from the root of the scene to
     *    this node according to the current traversal path
     * @param viewTransform The transformation from the root of the scene
     *    graph to the active viewpoint
     * @param viewFrustum The 6 planes of the view frustum
     * @param angularRes Angular resolution of the screen, or 0 if not
     *    calculable from the available data.
     * @return Any information that may be useful for the rendering step
     */
    public Object processCull(Matrix4f vworldTx,
                              Matrix4f viewTransform,
                              Vector4f[] viewFrustum,
                              float angularRes);
    /**
     * Render the geometry now using the supplied extra external data, using
     * the custom rendering routines.
     *
     * @param gl The GL context to render with
     * @param externalData Some implementation-specific external data to
     *   aid in the rendering that was generated in the processCull method.
     */
    public void render(GL gl, Object externalData);
}
