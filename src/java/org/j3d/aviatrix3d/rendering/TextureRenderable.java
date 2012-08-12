/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004-2006
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
import javax.media.opengl.GL;
import javax.media.opengl.GLContext;

// Local imports
// None

/**
 * Extended version of the {@link org.j3d.aviatrix3d.rendering.ComponentRenderable}
 * interface that provides additional handling for textures.
 * <p>
 *
 * Textures can come in several diffferent forms and this interface provides a
 * way to map between the holding {@link org.j3d.aviatrix3d.TextureUnit} class
 * and the rendering/culling stage with addtional information about the form
 * of the contained texture without needing to pull apart the TextureUnit
 * itself.
 * <p>
 *
 * Working with offscreen buffers such as Pbuffers and Frame Buffer Objects
 * requires some cooperation between these interfaces and the rendering
 * pipeline. The pipeline requires rendering the offscreen buffer in a separate
 * rendering pass to the use(s) of that buffer. The rendering pass parent is
 * the OffscreenBufferRenderable. The user of that buffer is this interface.
 * When this interface is about to be rendered, some buffers require an explicit
 * binding operation to be performed. Which buffer is used is dependent on how
 * the scene is constructed and how rendering proceeds, so a level of
 * abstraction is needed to separate the specifics of the buffer and the
 * usage of it. To do so is the work of the {@link OffscreenBufferDescriptor}.
 * When a buffer is created, that is registered with the parent renderable.
 * When a buffer is used, it is fetched from this interface.
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public interface TextureRenderable
    extends ComponentRenderable,
            TransparentRenderable
{
    /**
     * Activate the texture now. This will be called before the render() method
     * and is used to perform any enabling functionality for the given
     * renderable.
     *
     * @param gl The GL context to render with
     * @param externalData Some implementation-specific external data to
     *   aid in the rendering
     */
    public void activateTexture(GL gl, Object externalData);

    /**
     * Deactivate the texture now so that it is no longer a valid rendering
     * target.
     *
     * @param gl The GL context to render with
     * @param externalData Some implementation-specific external data to
     *   aid in the rendering
     */
    public void deactivateTexture(GL gl, Object externalData);

    /**
     * Check to see if the contained texture is an offscreen renderable such as
     * OffscreenTexture2D or MRTOffscreenTexture2D that contains
     * a subscene graph to be rendered. If is is, then the
     * {@link #getOffscreenSource()} method will return the contained cullable.
     *
     * @return true if the texture contains an offscreen source
     */
    public boolean isOffscreenSource();

    /**
     * Fetch the offscreen texture source that this renderable holds on to.
     *
     * @return The contained offscreen texture or null if none
     */
    public OffscreenCullable getOffscreenSource();

    /**
     * Check to see if this is an offscreen buffer that is separately rendered
     * rather than using explicit image data. Offscreen buffers do not
     * necessarily contain a sub scene graph. The {@link #isOffscreenSource()}
     * method is used to determine that.
     *
     * @return true if this represents an offscreen buffer of some sort
     */
    public boolean isOffscreenBuffer();

    /**
     * Fetch the underlying source buffer for the offscreen rendering that
     * relates to this particular texture. The buffer descriptor would be
     * fetched from the matching offscreen texture source that was registered
     * below this class.
     *
     * @param context The containing context to find the matching buffer for
     * @return A buffer descriptor for that context, or null if none found
     */
    public OffscreenBufferDescriptor getBuffer(GLContext context);
}

