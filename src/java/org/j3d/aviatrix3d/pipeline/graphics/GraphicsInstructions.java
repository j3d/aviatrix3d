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

package org.j3d.aviatrix3d.pipeline.graphics;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.rendering.OffscreenBufferRenderable;
import org.j3d.aviatrix3d.pipeline.RenderInstructions;

/**
 * Class for passing the detailed rendering information for a single surface.
 * from the sorter through to the renderable surface.
 * <p>
 *
 * Since pBuffers cannot be shared across different GL contexts with the
 * construct that JOGL is is using, when two parent textures reference a
 * single child, there will be two copies of this class, with two
 * different parentSource references, but only one copyOf set.
 *
 * @author Justin Couch
 * @version $Revision: 3.1 $
 */
public class GraphicsInstructions extends RenderInstructions
{
    /**
     * Visual data such as viewpoint, background etc. Initially assigned a
     * length of 1.
     */
    public GraphicsEnvironmentData[] renderData;

    /**
     * If set, the rendering should be done to an offscreen buffer that is
     * then handed to this texture placeholder for use in another drawable
     * object. If this is null, then render to the main on-screen drawable
     * object.
     */
    public OffscreenBufferRenderable pbuffer;

    /**
     * Reference to the parent texture source that this instance is to be
     * rendered into. If the parent source is the root canvas (ie main
     * drawable), this reference will be null.
     */
    public OffscreenBufferRenderable parentSource;


    /** The list of nodes in sorted order */
    public GraphicsDetails[] renderList;

    /**
     * Construct a new instance of this class with the arrays initialised
     * to a default size.
     */
    public GraphicsInstructions()
    {
        renderList = new GraphicsDetails[LIST_START_SIZE];

        for(int i = 0; i < LIST_START_SIZE; i++)
            renderList[i] = new GraphicsDetails();

        renderData = new GraphicsEnvironmentData[1];
    }

    /**
     * Resize the renderData variable to be at least the give size, copying
     * existing contents if needed. If the array is already bigger than that
     * requested, do nothing.
     *
     * @param reqdSize The number of elements required
     */
    public void ensureEnvDataCapacity(int reqdSize)
    {
        if(renderData.length >= reqdSize)
            return;

        GraphicsEnvironmentData[] tmp = new GraphicsEnvironmentData[reqdSize];

        for(int i = 0; i < renderData.length; i++)
            tmp[i] = renderData[i];

        renderData = tmp;
    }
}
