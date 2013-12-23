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

package org.j3d.aviatrix3d.rendering;

// External imports
// None

// Local imports
// None

/**
 * A renderable that represents a separate rendering space to an offscreen
 * buffer, rather than to the main buffer.
 * <p>
 *
 * This class encapsulates both pbuffer and frame buffer object renderable
 * capabilities.
 * <p>
 *
 * <b>Assumptions and Requirements</b>
 * <ul>
 * <li> An explicit size must be set. If either size dimension is not set
 *      the buffer is not rendered to. </li>
 * <li> Conventions follow the usual OpenGL requirements. For example, a
 *      1D texture needs to be created with the height of 1 and width of
 *      the required texture size.
 * </li>
 * <li> There are no checks for nPoT size requirements. It will create whatever
 *     size is requested.
 * </li>
 * <li>The GLCapabilities is used to construct the underlying buffer when
 *     creating pbuffers. It is used as a guide when creating FBOs. In both
 *     cases, the format is used to describe what texture format the buffer is
 *     applied as after the rendering takes place. For example, an offscreen
 *     buffer may need both depth and colour values to render a scene, but it
 *     only needs to apply the colour buffers to the texture as it is
 *     rendered on the object.
 * </li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 2.2 $
 */
public interface OffscreenRenderTargetRenderable extends Renderable
{
    /**
     * Get the height of the buffer in pixels. If no size is set, this returns
     * -1.
     *
     * @return a number >= -1
     */
    public int getHeight();

    /**
     * Get the width of the buffer in pixels. If no size is set, this returns
     * -1.
     *
     * @return a number >= -1
     */
    public int getWidth();

    /**
     * Check to see if this is a child render target of a parent multiple
     * render target offscreen buffer. Returns true if it is.
     *
     * @return true if this is a child, false if the parent
     */
    public boolean isChildRenderTarget();

    /**
     * Get the requested buffer setup that describes this offscreen buffer. Only
     * called once when the buffer is first constructed.
     *
     * @return The requested capabilities of the buffer that needs to be created
     */
    public BufferSetupData getBufferSetup();

    /**
     * Get the currently registered pBuffer for the given key object. If there
     * is no buffer registered for the current context, return null.
     *
     * @param obj The key used to register the buffer with
     * @return buffer The buffer instance to use here.
     */
    public OffscreenBufferDescriptor getBuffer(Object obj);

    /**
     * Register a pBuffer for a given key object.
     *
     * @param obj The key used to register the buffer with
     * @param buffer The buffer instance to use here.
     */
    public void registerBuffer(Object obj, OffscreenBufferDescriptor buffer);

    /**
     * Remove an already registered pBuffer for a given key object.
     *
     * @param obj The key used to register the buffer with
     */
    public void unregisterBuffer(Object obj);
}
