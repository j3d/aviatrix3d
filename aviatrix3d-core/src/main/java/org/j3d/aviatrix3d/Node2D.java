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

package org.j3d.aviatrix3d;

// External imports
// None

// Local imports
// None

/**
 * A specialised version of the Node class that works in 2D screen space only.
 * <p>
 * This class is the base class for all 2D renderable nodes in the SceneGraph.
 * Although bounds are in a full 3D space using the normal bounding volume
 * objects, the user can assume that the depth (Z axis) values are always
 * ignored.
 * <p>
 * 2D geometry uses the screen coordinate space that is the same as Viewports.
 * The origin is in the lower left corner of the viewport with positive width
 * to left and positive height up.
 *
 * @author Justin Couch
 * @version $Revision: 2.3 $
 */
public abstract class Node2D extends Node
{
    /**
     * Construct a new instance of this node, with implicit bounds calculation.
     */
    protected Node2D()
    {
    }
}
