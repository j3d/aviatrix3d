/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004-2005-
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.renderer.aviatrix3d.geom.hanim;

// External imports
// None

// Local imports
import org.j3d.geom.hanim.HAnimObject;
import org.j3d.geom.hanim.HAnimObjectParent;

/**
 * Marker of a class that can act as a parent to a HAnimObject.
 * <p>
 *
 * Used to abstract the parent information away from nodes, as for example,
 * a joint could have either a joint or a humanoid as the parent.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
interface ShaderObjectParent extends HAnimObjectParent
{
    /**
     * Notification that the child has changed something in the attributes and
     * will need to reset the new values. A change could be in the weights,
     * indexed fields or new children added.
     *
     * @param child Reference to the child that has changed
     */
    public void childAttributesChanged(HAnimObject child);
}
