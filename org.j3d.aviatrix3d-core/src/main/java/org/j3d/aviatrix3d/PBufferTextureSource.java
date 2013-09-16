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
import java.nio.ByteBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLPbuffer;

// Local imports
// None

/**
 * Marker describing a texture that is rendered to an offscreen buffer, using
 * OpenGL pBuffers.
 * <p>
 *
 * Internally the system will use a pBuffer to render this texture source to
 * a complete rendered scene. As such, this represents a complete rendered
 * system of layers, multipass rendering and normal capabilties.
 *
 * @deprecated Most of the functionality of this class has moved to the 
 *   interface {@link org.j3d.aviatrix3d.rendering.OffscreenBufferRenderable}.
 * 
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public interface PBufferTextureSource extends OffscreenTextureSource
{
    /**
     * Set the collection of geometry that should be rendered to this
     * texture. The geometry is, in effect, a completely separate rendarable
     * space, with it's own culling and sorting pass. In addition, a check
     * is made to make sure that no cyclic scene graph structures are created,
     * as this can create really major headachesfor nested surface rendering.
     * A null value will clear the current geometry and result in only
     * rendering the background, if set. if not set, then whatever the default
     * colour is, is used (typically black).
     *
     * @param layers The collection of layers, in order, to render
     * @param numLayers The number of valid layers to use
     * @throws CyclicSceneGraphStructureException Equal parent and child
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void setLayers(Layer[] layers, int numLayers)
        throws InvalidWriteTimingException, CyclicSceneGraphStructureException;

    /**
     * Get the number of layers that are currently set. If no layers are set,
     * or a scene is set, this will return zero.
     *
     * @return a value greater than or equal to zero
     */
    public int numLayers();

    /**
     * Fetch the current layers that are set. The values will be copied into
     * the user-provided array. That array must be at least
     * {@link #numLayers()} in length. If not, this method does nothing (the
     * provided array will be unchanged).
     *
     * @param layers An array to copy the values into
     * @throws IllegalArgumentException The array provided is too small or null
     */
    public void getLayers(Layer[] layers)
        throws IllegalArgumentException;

    /**
     * Get the requested buffer setup that describes this offscreen texture.
     *
     * @return The defined capabilities setup for the texture
     * @deprecated Now replaced by the same method in 
     *   {@link org.j3d.aviatrix3d.rendering.OffscreenBufferRenderable}
     */
    public GLCapabilities getGLSetup();

    /**
     * Get the currently registered pBuffer for the given key object. If there
     * is no buffer registered for the current context, return null.
     *
     * @param obj The key used to register the buffer with
     * @return buffer The buffer instance to use here.
     */
    public GLPbuffer getBuffer(Object obj);

    /**
     * Register a pBuffer for a given key object.
     *
     * @param obj The key used to register the buffer with
     * @param buffer The buffer instance to use here.
     * @deprecated Now replaced by the same method in 
     *   {@link org.j3d.aviatrix3d.rendering.OffscreenBufferRenderable}
     */
    public void registerBuffer(Object obj, GLPbuffer buffer);

    /**
     * Remove an already registered pBuffer for a given key object.
     *
     * @param obj The key used to register the buffer with
     * @deprecated Now replaced by the same method in 
     *   {@link org.j3d.aviatrix3d.rendering.OffscreenBufferRenderable}
     */
    public void unregisterBuffer(Object obj);

    /**
     * Bind the underlying source buffer for the offscreen rendering.
     *
     * @param context The containing context to bind from
     * @deprecated Now replaced by the same method in 
     *   {@link org.j3d.aviatrix3d.rendering.OffscreenBufferRenderable}
     */
    public void bindBuffer(GLContext context);

    /**
     * Unbind the underlying source buffer for the offscreen rendering.
     *
     * @param context The containing context to bind from
     * @deprecated Now replaced by the same method in 
     *   {@link org.j3d.aviatrix3d.rendering.OffscreenBufferRenderable}
     */
    public void unbindBuffer(GLContext context);
}
