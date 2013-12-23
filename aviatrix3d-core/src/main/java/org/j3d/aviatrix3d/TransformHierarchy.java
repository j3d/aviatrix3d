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
 * Marker interface used to identify any scene graph structural element that
 * form part of the transformation heirarchy.
 * <p>
 *
 * Internally this is used to check parent-child relationships by other
 * structural elements while allowing the grouping elements to properly
 * construct a scene graph with any arbitrarily defined user nodes. Any custom
 * node that participates in the grouping and transformantion structure must
 * implement this interface otherwise attempting to add a node to the
 * scenegraph will generate a {@link InvalidNodeTypeException} exception at
 * runtime.
 *
 * @author Justin Couch
 * @version $Revision: 2.1 $
 */
public interface TransformHierarchy
{
}
