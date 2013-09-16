/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004-2005
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

// External imports
// None

// Local imports
// None

/**
 * Listener used by the rendering system to provide internal notifications to
 * nodes about operations they must perform.
 * <p>
 *
 * End-user code cannot make use of this interface and should never be directly
 * implemented by them. It is used by the rendering system to provide internal
 * notifications across package boundaries, without exposing public methods
 * that should not be called by end-user code.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public interface InternalNodeUpdateListener
{
    /**
     * Notify this node to update it's bounds now and propogate those new
     * bounds to their parent(s).
     */
    public void updateBoundsAndNotify();
}
