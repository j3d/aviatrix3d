/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2006
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
import org.j3d.util.I18nManager;

// Local imports
// None

/**
 * Representation of scene graph objects that control the drawing buffer state
 * at the root of the scene graph.
 * <p>
 *
 * Buffer state information can be shared between scenes, though it is expected
 * to be unlikely.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>nullParentMsg: Error message when the user tries to set this
 *     in something that is not part of the transform heirarchy.</li>
 * <li>nullArrayParentMsg: Error message when the (internal) caller tries to
 *     call getParents() with a null array reference.</li>
 * </ul>
 *
 * @author Justin
 * @version $Revision: 2.4 $
 */
public abstract class BufferState extends SceneGraphObject
{
    /** Message for when the node is currently owned */
    private static final String NULL_PARENT_PROP =
        "org.j3d.aviatrix3d.BufferState.nullParentMsg";

    /** Message for getParent(null) case error */
    private static final String ARRAY_PARENT_NULL_PROP =
        "org.j3d.aviatrix3d.SharedNode.nullArrayParentMsg";

    /** The initial size of the children list */
    private static final int LIST_START_SIZE = 5;

    /** The increment size of the list if it gets overflowed */
    private static final int LIST_INCREMENT = 5;

    /** Listing of all the parents of this node */
    protected SceneGraphObject[] parentList;

    /** Index to the next place to add items in the nodeList */
    protected int lastParent;

    /**
     * Counter for how many times we've been marked as live so to know
     * when to notify the children of a change of state.
     */
    protected int liveCount;

    /**
     * Initialise a new instance of the component, setting up the internal
     * state needed.
     */
    protected BufferState()
    {
        parentList = new Node[LIST_START_SIZE];
        lastParent = 0;
        liveCount = 0;
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Specify this nodes parent, overridden to provide behaviour that appends
     * the node to the list rather than replacing it. The parent must be a group
     * node in this case.
     *
     * @param p The new parent instance to add to the list
     * @throws AlreadyParentedException There is a valid parent already set
     * @throws InvalidNodeTypeException Not a group node
     */
    protected void addParent(SceneGraphObject p)
        throws AlreadyParentedException, InvalidNodeTypeException
    {
        // Should never have this, but a sanity check just in case
        if(p != null)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NULL_PARENT_PROP);
            throw new InvalidNodeTypeException(msg);
        }

        // Check to see that this parent isn't already in the list
        for(int i = 0; i < lastParent; i++)
            if(parentList[i] == p)
                return;

        resizeList();
        parentList[lastParent++] = p;
    }

    /**
     * Remove a parent from this shared group. Since setParent() cannot be
     * used to remove a parent from the graph, you'll need to use this method
     * to remove the parent.
     *
     * @param p The new parent instance to remove from the list
     */
    protected void removeParent(SceneGraphObject p)
    {
        // find the location, move everything down one
        for(int i = 0; i < lastParent; i++)
        {
            if(parentList[i] == p)
            {
                int move_size = lastParent - i;
                if(move_size != 0)
                    System.arraycopy(parentList,
                                     i,
                                     parentList,
                                     i + 1,
                                     move_size);
                break;
            }
        }
    }

    /**
     * Request the number of parents this node currently contains
     *
     * @return a positive number
     */
    public int numParents()
    {
        return lastParent;
    }

    /**
     * Get the listing of the number of parents that this node currently has.
     * The provided array must be at least big enough to copy all the values
     * into it.
     *
     * @param parents An array to copy the parent listing into
     */
    public void getParents(Node[] parents)
    {
        if(parents == null)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(ARRAY_PARENT_NULL_PROP);
            throw new NullPointerException(msg);
        }

        System.arraycopy(parentList, 0, parents, 0, lastParent);
    }

    /**
     * Resize the list if needed. Marked as final in order to encourage the
     * compiler to inline the code for faster execution
     */
    private void resizeList()
    {
        if((lastParent + 1) == parentList.length)
        {
            int old_size = parentList.length;
            int new_size = old_size + LIST_INCREMENT;

            SceneGraphObject[] tmp_nodes = new SceneGraphObject[new_size];

            System.arraycopy(parentList, 0, tmp_nodes, 0, old_size);

            parentList = tmp_nodes;
        }
    }
}
