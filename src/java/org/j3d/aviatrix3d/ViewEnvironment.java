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

// External imports
// None

// Local imports
// None

/**
 * Abstract representation of the physical environment setup used to connect
 * a virtual Viewpoint object to the real one that is rendered on a drawable
 * surface.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface ViewEnvironment
{
    /**
     * Set the stereo flag used for this environment.
     *
     * @param stereo True if stereo should be rendered
     */
    public void setStereoEnabled(boolean stereo);

    /**
     * Check to see if stereo has been enabled for this environment.
     *
     * @return true if stereo rendering is to be used
     */
    public boolean getStereoEnabled();
}