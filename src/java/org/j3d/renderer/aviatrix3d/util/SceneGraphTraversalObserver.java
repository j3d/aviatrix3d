/*****************************************************************************
 *                           J3D.org Copyright (c) 2005
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.renderer.aviatrix3d.util;

// External imports
// none

// Local imports
import org.j3d.aviatrix3d.SceneGraphObject;

/**
 * An observer interface that reports on the scene graph traversal state.
 * <p>
 *
 * The observer will report the top of a use hierarchy. If the traverser, in
 * it's internal references, detects a reference re-use then the flag will be
 * passed indicating this state. Once a shared graph has been detected, the
 * traverser does not further re-descend the shared state.
 * <p>
 *
 * When reporting the parent node, if the root is the root node of the scene
 * graph, the parent reference will be null.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface SceneGraphTraversalObserver {

    /**
     * Notification of a scene graph object that has been traversed in the
     * scene.
     *
     * @param parent The parent node of this node
     * @param child The child node that is being observed
     * @param shared true if the object reference has already been traversed
     *    and this is beyond the first reference
     * @param depth The depth of traversal from the top of the tree.  Starts at 0 for top.
     */
    public void observedNode(SceneGraphObject parent,
                             SceneGraphObject child,
                             boolean shared,
                             int depth);
}
