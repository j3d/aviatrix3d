/*****************************************************************************
 *                        Yumtech, Inc Copyright (c) 2004 - 2006
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

/**
 * Data holder that passes the information about what is to be rendered from a
 * multipass scene within a layer from the output of the {@link GraphicsCullStage}
 * through to the {@link GraphicsSortStage}.
 *
 * @author Justin Couch
 * @version $Revision: 3.2 $
 */
public class MultipassRenderBucket
{
    /** The details of the main scene passes to be rendered */
    public MultipassDetails mainScene;

    /**
     * Parent nodes that hold the subscene in the main scene graph. Each
     * index is the scene parent of this scene included (needed for pBuffer
     * GL context handling at render time). If this is null, then the parent
     * is the main canvas that is being rendered to.
     */
    public OffscreenBufferRenderable[] sceneParents;

    /** A list of the multipass texture sources found. */
    public MultipassDetails[] generatedTextures;

    /** The number of multipass textures to be rendered for this scene */
    public int numGeneratedTextures;

    /**
     * Create a default instance of this bucket. All lists are initialised to
     * null. The mainScene instance is created.
     * The graphics environment data is initialised.
     */
    public MultipassRenderBucket()
    {
        mainScene = new MultipassDetails();
    }
}
