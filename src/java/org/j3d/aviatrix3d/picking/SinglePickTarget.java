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
 * A picking target that contains a single child pickable target.
 * <p>
 *
 * Examples of this would be {@link org.j3d.aviatrix3d.SharedNode} and
 * {@link org.j3d.aviatrix3d.Shape3D}.
 *
 * <h3>Implementor Guidelines</h3>
 *
 * <p>
 * There is no requirement that the picking and rendering subgraphs look the
 * same. There may be no pickable child even when there is a renderable child.
 * </p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface SinglePickTarget extends PickTarget
{
    /**
     * Return the child that is pickable of from this target. If there is none
     * then return null.
     *
     * @return The child pickable object or null
     */
    public PickTarget getPickableChild();
}
