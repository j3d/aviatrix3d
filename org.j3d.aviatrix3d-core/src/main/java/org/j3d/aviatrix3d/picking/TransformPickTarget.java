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
import org.j3d.maths.vector.Matrix4d;

// Local imports
// None

/**
 * A picking target that contains a local transformation service.
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
 * <p>
 * Typically this is combined with the GroupPickTarget to represent transformed
 * sets of children, such as the {@link org.j3d.aviatrix3d.TransformGroup} node.
 * </p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface TransformPickTarget extends PickTarget
{
    /**
     * Get the local transform.
     *
     * @param mat The matrix to copy the transform data to
     */
    public void getTransform(Matrix4d mat);

    /**
     * Get the inverse version of the local transform.
     *
     * @param mat The matrix to copy the transform data to
     */
    public void getInverseTransform(Matrix4d mat);
}
