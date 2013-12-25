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
import org.j3d.geom.hanim.HAnimJoint;
import org.j3d.geom.hanim.HAnimObject;

/**
 * Common implementation of a joint object that does in-place software
 * evaluation of the skin mesh updates.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
abstract class SoftwareJoint extends AVJoint
{
    /** The array of flags to indicate the managed coord index */
    protected boolean[] dirtyCoordinates;

    /** Have we been marked as dirty this frame? */
    protected boolean dirty;

    /**
     * Create a new, default instance of the site.
     */
    SoftwareJoint()
    {
        dirty = false;
    }

    //----------------------------------------------------------
    // Methods defined by HAnimJoint
    //----------------------------------------------------------

    /**
     * Replace the existing children with the new set of children.
     *
     * @param kids The collection of child objects to now use
     * @param numValid The number kids to copy from the given array
     */
    @Override
    public void setChildren(HAnimObject[] kids, int numValid)
    {
        super.setChildren(kids, numValid);

        for(int i = 0; i < numChildren; i++)
        {
            if(children[i] instanceof HAnimJoint)
                ((SoftwareJoint)children[i]).setDirtyList(dirtyCoordinates);
        }
    }

    /**
     * Add a child node to the existing collection. Duplicates and null values
     * are allowed.
     *
     * @param kid The new child instance to add
     */
    @Override
    public void addChild(HAnimObject kid)
    {
        super.addChild(kid);

        if(kid instanceof HAnimJoint)
            ((SoftwareJoint)kid).setDirtyList(dirtyCoordinates);
    }

    /**
     * Send an update message to the parent, if one has not already been sent.
     */
    @Override
    protected void sendUpdateMsg()
    {
        if(!dirty && dirtyCoordinates != null)
        {
            for(int i = 0; i < numSkinCoord; i++)
                dirtyCoordinates[skinCoordIndex[i]] = true;

            for(int i = 0; i < numChildren; i++)
            {
                if(children[i] instanceof HAnimJoint)
                    ((SoftwareJoint)children[i]).parentDirty();
            }

            dirty = true;
        }

        super.sendUpdateMsg();
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the dirty coordinate list for this node to play with.
     *
     * @param dirtyList Array that we mark with dirty flags
     */
    void setDirtyList(boolean[] dirtyList)
    {
        dirtyCoordinates = dirtyList;

        for(int i = 0; i < numChildren; i++)
        {
            if(children[i] instanceof HAnimJoint)
                ((SoftwareJoint)children[i]).setDirtyList(dirtyList);
        }
    }

    /**
     * Notification that the parent is dirty for this frame.
     */
    void parentDirty()
    {
        if(!dirty && dirtyCoordinates != null)
        {
            for(int i = 0; i < numSkinCoord; i++)
                dirtyCoordinates[skinCoordIndex[i]] = true;

            for(int i = 0; i < numChildren; i++)
            {
                if(children[i] instanceof HAnimJoint)
                    ((SoftwareJoint)children[i]).parentDirty();
            }

            dirty = true;
        }
    }
}
