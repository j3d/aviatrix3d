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
// None

// Local imports
// None

/**
 * A cullable that contains multiple cullables as children objects.
 * <p>
 *
 * Sometimes instances of this are combined with {@link TransformCullable}
 * to indicate the prescense of a transformation at this level.
 *
 * @author Justin Couch
 * @version $Revision: 2.2 $
 */
public interface GroupCullable extends Cullable
{
    /**
     * Check to see if this cullable is mulitparented. If it is, then that
     * will cause problems for code that needs to know information like the
     * transformation to the root of the scene graph.
     *
     * @return true if there are multiple parents
     */
    public boolean hasMultipleParents();

    /**
     * Get the parent cullable of this instance. If this node has multiple
     * direct parents, then this should return null.
     *
     * @return The parent instance or null if none
     */
    public Cullable getCullableParent();

    /**
     * Get the currently set bounds for this object. If no explicit bounds have
     * been set, then an implicit set of bounds is returned based on the
     * current scene graph state.
     *
     * @return The current bounds of this object
     */
    public BoundingVolume getBounds();

    /**
     * Get the list of children that are valid to be rendered according to
     * the rules of the grouping node.
     *
     * @return an array of nodes
     */
    public Cullable[] getCullableChildren();

    /**
     * Returns the number of valid renderable children to process. If there are
     * no valid renderable children return -1.
     *
     * @return A number greater than or equal to zero or -1
     */
    public int numCullableChildren();
}
