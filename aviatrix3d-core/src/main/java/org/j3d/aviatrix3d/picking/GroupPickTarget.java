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

package org.j3d.aviatrix3d.picking;

// External imports
// None

// Local imports
// None

/**
 * A picking target that contains a collection of zero or more children
 * pickable targets.
 * <p>
 *
 * This interface is used by programmers that require fairly complex
 * implementation logic at the point of picking (eg a billboarded object
 * that should be pickable from any direction, even though it is represented
 * as a plane facing the current viewpoint location).
 *
 * <h3>Implementor Guidelines</h3>
 *
 * <p>
 * There is no requirement that the picking and rendering subgraphs look the
 * same. There may be less pickable children than renderable children, so don't
 * automatically assume that there is.
 * </p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface GroupPickTarget extends PickTarget
{
    /**
     * Returns the number of valid pickable child targets to process. If there
     * are no valid children return -1.
     *
     * @return A number greater than or equal to zero or -1
     */
    public int numPickableChildren();

    /**
     * Return an array containing all of this group's pickable children.  This
     * structure is the nodes internal representation.  Check the
     * {@link #numPickableChildren()} call for how many valid objects are part
     * of this array.If there are none, this may return either a null or a
     * valid array, depending on the implementation.
     * <p>
     *
     * The list may contain null values.
     *
     * @return An array of pick targets
     */
    public PickTarget[] getPickableChildren();

    /**
     * Return the pickable target instance at the given index. If there is
     * nothing at the given index, null will be returned.
     *
     * @param idx The index of the child to get
     * @return The target object at the given index.
     */
    public PickTarget getPickableChild(int idx);
}
