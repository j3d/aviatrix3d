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
 * A general purpose container class to allow end users to extend the basic
 * Node capabilities, while still providing a way to manage scene graph
 * state such as liveness calls and update handlers.
 * <p>
 *
 * A lot of the state calls of children can only be called by extending the
 * class and calling protected methods. When a child scenegraph object is
 * defined for a collection of children outside of the base aviatrix3d package
 * there is no way for passing in liveness state due to the protected nature
 * of the required methods. This class provides methods that allow a derived
 * class to call back into the package and set those states.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 2.1 $
 */
public abstract class BaseNode extends Node
{
    /**
     * The default constructor
     */
    protected BaseNode()
    {
    }

    /**
     * Specify this nodes parent. Should not be called directly by external
     * callers. Setting a value of null will clear the existing parent. Bit
     * broken right now as it doesn't handle multiple-parents like needed in
     * a proper scene graph. If the node has a parent already, an exception
     * is generated. Note that this method is ignored if the derived type is
     * SharedGroup.
     *
     * @param node The node instance to set the parent on
     * @param p The new parent instance to call or null
     * @throws AlreadyParentedException There is a valid parent already set
     */
    protected void setParent(Node node, Node p)
        throws AlreadyParentedException
    {
        node.setParent(p);
    }

    /**
     * Remove a parent from this node. An alternate way to remove a parent
     * from the list.
     *
     * @param node The node instance to set the parent on
     * @param p The new parent instance to remove from the list
     */
    protected void removeParent(Node node, Node p)
    {
        node.removeParent(p);
    }

    /**
     * Set the live state of the given node to this state.
     *
     * @param state The new liveness state to set
     * @param node The node instance to set the live state on
     */
    protected void setLive(SceneGraphObject node, boolean state)
    {
        node.setLive(state);
    }

    /**
     * Check to see if this node is the same reference as the passed node.
     * This is the upwards check to ensure that there is no cyclic scene graph
     * structures at the point where someone adds a node to the scenegraph.
     * When the reference and this are the same, an exception is generated.
     *
     * @param child The reference to check against this class
     * @param node The node instance to check on
     * @throws CyclicSceneGraphStructureException Equal parent and child
     */
    protected void checkForCyclicParent(SceneGraphObject node,
                                        SceneGraphObject child)
        throws CyclicSceneGraphStructureException
    {
        if(child == this)
            throw new CyclicSceneGraphStructureException();

        node.checkForCyclicParent(child);
    }

    /**
     * Check to see if this node is the same reference as the passed node that
     * is a parent of this node. This is the downwards check to ensure that
     * there is no cyclic scene graph structures at the point where someone
     * adds a node to the scenegraph. When the reference and this are the
     * same, an exception is generated. Since each class may have different
     * lists of child node setups, this should be overriden by any class that
     * can take children, and have the call passed along to the children.
     *
     * @param node The node instance to check on
     * @param parent The reference to check against this class
     * @throws CyclicSceneGraphStructureException Equal parent and child
     */
    protected void checkForCyclicChild(SceneGraphObject node,
                                       SceneGraphObject parent)
        throws CyclicSceneGraphStructureException
    {
        if(parent == this)
            throw new CyclicSceneGraphStructureException();

        node.checkForCyclicChild(parent);
    }

    /**
     * Set the scenegraph update handler for this node.  It will notify
     * all its children of the value. A null value will clear the current
     * handler.
     *
     * @param node The node instance to set the state on
     */
    protected void setUpdateHandler(SceneGraphObject node)
    {
        node.setUpdateHandler(updateHandler);
    }

    /**
     * Clear the scenegraph update handler for this node.
     *
     * @param node The node instance to set the state on
     */
    protected void clearUpdateHandler(SceneGraphObject node)
    {
        node.setUpdateHandler(null);
    }
}
