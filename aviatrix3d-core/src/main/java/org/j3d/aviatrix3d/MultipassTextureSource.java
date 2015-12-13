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
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

// Local imports
// None

/**
 * Marker describing a texture source that that is rendered to the main surface
 * using a multipass technique.
 * <p>
 *
 * <b>Note: This class does nothing in Aviatrix3D 1.0</b>
 * <p>
 *
 * In a multipass source, the drawn surface still needs to conform to the power
 * of 2 size requirements. Since the viewport for the window is almost
 * guaranteed not to be a power of two, the user must make sure that they set
 * an explicit, correct, viewport size through the ViewEnvironment provided
 * with the scene.
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 * @deprecated Use {@link OffscreenTexture2D} or {@link MRTOffscreenTexture2D}
 */
public interface MultipassTextureSource extends OffscreenTextureSource
{
    /** The bitmask indicating the colour buffer is used. */
    public static final int COLOR_BUFFER = GL.GL_COLOR_BUFFER_BIT;

    /** The bitmask indicating the depth buffer is used. */
    public static final int DEPTH_BUFFER = GL.GL_DEPTH_BUFFER_BIT;

    /** The bitmask indicating the stencil buffer is used. */
    public static final int STENCIL_BUFFER = GL.GL_STENCIL_BUFFER_BIT;

    /** The bitmask indicating the accumulation buffer is used. */
    public static final int ACCUMULATION_BUFFER = GL2.GL_ACCUM_BUFFER_BIT;

    /**
     * Get the root of the currently rendered scene. If none is set, this will
     * return null.
     *
     * @return The current scene root or null.
     */
    public ViewportLayer getViewportLayer();

    /**
     * Fetch the observer instance that may be associated with the texture
     * source. If no instance is associated, this will return null.
     *
     * @return The current observer instance, or null if none
     */
    public MultipassRenderObserver getRenderObserver();

    /**
     * Get the list of buffers that are required to be rendered by this
     * source. This will be used by the glClear() call to clear the
     * appropriate buffers. These are the buffers that are to be cleared
     * and used at the start of the multipass rendering. If the application
     * needs to clear buffers during individual passes, that should be
     * performed as part of the MultipassRenderObserver callbacks.
     *
     * @return A bitwise OR mask of the required buffers
     */
    public int getUsedBuffers();

    /**
     * Set the number of levels of mipmap generation that should be rendered.
     * Each level will become a separate rendering pass that will be updated.
     * A check is performed to make sure that the number of levels does not
     * produce a situation where the width or height goes negative in thier
     * values - eg a starting size of 32 pixels square and requesting 6 levels
     * of mipmaps being generated.
     *
     * @param numLevels The number of levels to render
     * @throws IllegalArgumentException The number of levels is more than what
     *   the current size could reduce to
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void setNumLevels(int numLevels)
        throws InvalidWriteTimingException, IllegalArgumentException;

}
