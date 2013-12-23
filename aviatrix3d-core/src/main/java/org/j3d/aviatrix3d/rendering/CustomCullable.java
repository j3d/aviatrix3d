/*****************************************************************************
 *                  Yumetech, Inc Copyright (c) 2004 - 2006
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
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector4d;

// Local imports
// None

/**
 * Marker interface that represent a generic node that is capable of deciding
 * whether it should cull it children.
 * <p>
 *
 * This interface is used for a generic rendering scheme for when more specific
 * nodes and {@link org.j3d.aviatrix3d.pipeline.CullStage} combination are not
 * being implemented. Due to the extra overheads of copies to and from the
 * matrices, using this interface is not the most efficient way of implementing
 * a rendering strategy.
 * <p>
 * Angular resolution may not be calculable from the available input data. For
 * example, an explicit viewport size is not yet available from the screen, or
 * the user has set the field of view to -1.
 *
 * @author Justin Couch
 * @version $Revision: 2.1 $
 */
public interface CustomCullable extends Cullable
{
    /**
     * Check this node for children to traverse. The angular resolution is
     * defined as Field Of View (in radians) / viewport width in pixels.
     *
     * @param output Fill in the child information here
     * @param vworldTx The transformation from the root of the scene to
     *    this node according to the current traversal path
     * @param viewTransform The transformation from the root of the scene
     *    graph to the active viewpoint
     * @param frustumPlanes Listing of frustum planes in the order: right,
     *    left, bottom, top, far, near
     * @param angularRes Angular resolution of the screen, or 0 if not
     *    calculable from the available data.
     */
    public void cullChildren(CullInstructions output,
                             Matrix4d vworldTx,
                             Matrix4d viewTransform,
                             Vector4d[] frustumPlanes,
                             float angularRes);
}
