/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d;

// Standard imports
// None

// Application specific imports
// None

/**
 * Abstract representation of a piece of code that wants to know about when
 * a scene graph node is requiring an update.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface NodeUpdateHandler
{
    /**
     * Notify the handler that you have updates to the SG that might alter
     * a nodes bounds.
     *
     * @param l The change requestor
     */
    void boundsChanged(NodeUpdateListener l);

    /**
     * Notify the handler that you have updates to the SG that will not
     * alter a nodes bounds.
     *
     * @param l The change requestor
     */
    void dataChanged(NodeUpdateListener l);
}