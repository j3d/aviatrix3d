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
import javax.vecmath.Matrix4f;

// Local imports
// None

/**
 * Container for returning the details about what should be picked as a
 * a set of children from a node that implements the {@link CustomPickTarget}
 * interface.
 * <p>
 *
 * The data stored in this class is considered to be temporary only - it lasts
 * just enough time to process the children for further pickable objects.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class PickInstructions
{
    /**
     * The array of children pickables that are valid children for travering
     * for further culling. If the array is not large enough, the user is
     * allowed to directly resize the array to something large enough, or make
     * use of the utility method of this class.
     */
    public PickTarget[] children;

    /** The number of children to process from the list. */
    public int numChildren;

    /**
     * Flag to indicate if the transform needs to be used from this class
     * as part of the traversal of the scene graph.
     */
    public boolean hasTransform;

    /**
     * The local transformation matrix that should be applied as part of the
     * traversal process. If no transformation is needed. then set
     * {@link #hasTransform} to false and ignore this matrix.
     */
    public Matrix4f localTransform;

    /**
     * Initialise a new instance of this instruction.
     */
    public PickInstructions()
    {
        children = new PickTarget[8];
        localTransform = new Matrix4f();
        hasTransform = false;
    }

    /**
     * Convenience method to resize the children array to be at least the
     * required minimum size. This method will always resize, so only call it
     * if it must be resized. The old values in the old array are discarded.
     *
     * @param size The minimum length that this array should be
     */
    public void resizeChildren(int size)
    {
        children = new PickTarget[size];
    }
}
