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

import org.j3d.maths.vector.Matrix4d;

/**
 * Marker interface for all grouping objects that wish to interact with the
 * picking system, but provide some sort of customised handling.
 * <p>
 *
 * This interface is used by programmers that require fairly complex
 * implementation logic at the point of picking (eg a billboarded object
 * that should be pickable from any direction, even though it is represented
 * as a plane facing the current viewpoint location).
 *
 *
 * <h3>Implementor Requirements</h3>
 *
 * <ul>
 * <li>The methods must be re-entrant as they can be called from multiple
 * places at once. For example, multiple pipes rendering the same object
 * on different screens simultaneously.
 * </li>
 * <li>All state must be maintained within this class but it should not
 * directly do picking from the provided {@link PickRequest} object. That
 * object is provided so that the implementation may make some calculations
 * about where the picking object is currently located in the local space
 * and other items that may be useful, such as the picking type. The bounds
 * of the object will not be tested first before this method is called. The
 * implementor can use the request object to do that testing and return
 * the appropriate list of children.
 * </li>
 * </ul>
 *
 * <h3>Implementor Guidelines</h3>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public interface CustomPickTarget extends PickTarget
{
    /**
     * This node is being subjected to picking, so process the provided data
     * and return the instructions on the list of available children and any
     * transformation information to the system.
     * <p>
     *
     * @param output Fill in the results of the picking evaluation here
     * @param vworldTx The transformation from the root of the scene to
     *    this node according to the current traversal path
     * @param req The picking request made of this object
     */
    public void pickChildren(PickInstructions output,
                             Matrix4d vworldTx,
                             PickRequest req);
}
