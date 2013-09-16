/*****************************************************************************
 *                  Yumetech, Inc Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d.rendering;

// External imports
import org.j3d.maths.vector.Matrix4d;

// Local imports
// None

/**
 * Container for returning the details about what should be rendered from a
 * node that implements the {@link CustomRenderable} interface.
 * <p>
 *
 * The data stored in this class is considered to be temporary only - it lasts
 * just enough time to process the children for renderable objects to be passed
 * to the sort stage.
 *
 * @author Justin Couch
 * @version $Revision: 2.2 $
 */
public class RenderableInstructions
{
    /**
     * Flag to indicate if the transform needs to be used from this class
     * as part of the traversal of the scene graph.
     */
    public boolean hasTransform;

    /**
     * The local transformation matrix that should be applied as part of the
     * traversal process. If no transformation is needed. then set
     * {@link #hasTransform} to false and ignore this matrix.
     */
    public Matrix4d localTransform;

    /**
     * Any local data that may need to be passed around. Not useful for the
     * Cullable users, but good for RenderableCustomObject that may need to
     * pass specific instructions back to itself during the rendering loop.
     */
    public Object instructions;

    /**
     * Initialise a new instance of this instruction.
     */
    public RenderableInstructions()
    {
        localTransform = new Matrix4d();
        hasTransform = false;
    }
}
