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
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;

import javax.media.opengl.GL;

// Local imports
// None

/**
 * Marker interface for all leaf objects that wish to implement custom rendering
 * capabilities beyond just the basic GL callbacks, combining the custom rendering
 * of {@link Cullable} with the rendering callbacks of {@link
 * ComponentRenderable}.
 * <p>
 *
 * This interface is used by programmers that require fairly complex
 * implementation logic at the point of rendering. An example of this is volume
 * rendering that requires a set of viewpoint-specific cutting planes to be
 * generated every frame. This is not achievable using the standard renderable
 * interfaces as they have no way of determining the current viewpoint and
 * transformation stack during the rendering process. This is made worse by a
 * node that could have multiple parents, thus multiple transformation paths
 * to it within a single cull traversal. A developer uses this class to take
 * the given rendering information to generate a set of custom instructions that
 * are passed back at rendering time, then later passed back during the rendering
 * phase.
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
 * <li>This interface should only be implemented by classes that extend the
 * {@link org.j3d.aviatrix3d.Leaf} base class. Anywhere else in the scene graph
 * hierarchy will be ignored by the rendering system. Classes that want to be
 * culled as part of a {@link org.j3d.aviatrix3d.NodeComponent} should make use
 * of either {@link ObjectRenderable} or {@link ComponentRenderable}. Nodes
 * further up the tree that provide grouping and structural information, should
 * implement the {@link Cullable} interface.
 * </li>
 * <li>The methods must be re-entrant as they can be called from multiple
 * places at once. For example, multiple pipes rendering the same object
 * on different screens simultaneously.
 * </li>
 * <li>All state must be maintained within this class. It is assumed all
 * rendering takes place within the render method and there is no post-render
 * call. Thus any state that is changed should be restored by this
 * implementation within the context of the rendering call.
 * </li>
 * </ul>
 *
 * <h3>Implementor Guidelines</h3>
 *
 * @author Justin Couch
 * @version $Revision: 2.2 $
 */
public interface CustomRenderable extends Renderable
{
    /**
     * Check to see if this renderable object has anything that could be
     * interpreted as an alpha value. For example a Raster with RGBA values or
     * vertex geometry with 4-component colours for the vertices. Transparency
     * information is needed for depth sorting during rendering.
     */
    public boolean hasTransparency();

    /**
     * This node is being subjected to rendering, so process the provided data
     * and return the instructions to the rendering system. If additional
     * rendering instructions will be needed at the point that the {@link
     * #render(GL, Object)} method is called, then formulate that now, and
     * hand it back as part of the query mechanism.
     * <p>
     *
     * The culler will not make use of the children or numChildren variables
     * as part of the rendering system. Only the return value of this method is
     * used to determine if this object should be placed into the queue for
     * sorting and rendering.
     *
     * @param output Fill in the cull information here
     * @param vworldTx The transformation from the root of the scene to
     *    this node according to the current traversal path
     * @param viewTransform The transformation from the root of the scene
     *    graph to the active viewpoint
     * @param frustumPlanes Listing of frustum planes in the order: right,
     *    left, bottom, top, far, near
     * @param angularRes Angular resolution of the screen, or 0 if not
     *    calculable from the available data.
     * @return true if this should be rendered, false otherwise
     */
    public boolean processCull(RenderableInstructions output,
                               Matrix4f vworldTx,
                               Matrix4f viewTransform,
                               Vector4f[] frustumPlanes,
                               float angularRes);

    /**
     * Render object now using the pre-given set of custom rendering details.
     *
     * @param gl The GL context to render with
     * @param externalData Some implementation-specific external data to
     *   aid in the rendering that was generated in the processCull method.
     */
    public void render(GL gl, Object externalData);
}
