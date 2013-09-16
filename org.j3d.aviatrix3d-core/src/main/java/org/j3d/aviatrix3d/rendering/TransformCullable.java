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
import org.j3d.maths.vector.Matrix4d;

// Local imports
// None

/**
 * A Cullable type that allows a node to transform its child/children.
 * <p>
 *
 * The specified transformation must be Affine and have uniform scaling
 * components(SRT-transform).  This class will not
 * check this constraint, so expect odd results if you break this rule, up
 * to and including a possible core reactor meltdown in a foreign country.
 * <p>
 *
 * This interface is typically combined with one of the other cullables to
 * indicate prescene or lack of child objects. All this does is mark the
 * availability of a transformation at this point in the scene graph.
 *
 * @author Justin Couch
 * @version $Revision: 2.1 $
 */
public interface TransformCullable extends Cullable
{
    /**
     * Get the current local transformation value.
     *
     * @param mat The matrix to copy the transform data to
     */
    public void getTransform(Matrix4d mat);
}
