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
import org.j3d.aviatrix3d.Node;


/**
 * Marker interface for the Aviatrix3D parts of the scene graph.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public interface AVHumanoidPart
{
    /**
     * Get the implemented scene graph object for this part.
     *
     * @return The scene graph object to use
     */
    public Node getSceneGraphObject();
}
