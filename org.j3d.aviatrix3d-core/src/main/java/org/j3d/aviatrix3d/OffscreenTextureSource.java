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
import javax.media.opengl.GLPbuffer;

// Local imports
// None

/**
 * Marker describing a texture source that gets it's source data from
 * an offscreen, direct rendering path.
 * <p>
 *
 * Examples of this type of texture are either multipass and pBuffer textures.
 * <p>
 *
 * An offscreen texture source does not directly define how the geometry is
 * presented to the the texture source. See the derived interfaces for methods
 * that allow setting of the geometry to be rendered.
 *
 * @author Justin Couch
 * @version $Revision: 1.9 $
 * @deprecated Use {@link OffscreenTexture2D} or {@link MRTOffscreenTexture2D}
 */
public interface OffscreenTextureSource extends TextureSource
{
    /**
     * Get the height of the texture in pixels. If no image is set, this returns
     * -1.
     *
     * @return a number >= -1
     */
    public int getHeight();

    /**
     * Get the current state of the repainting enabled flag.
     *
     * @return true when the texture requires re-drawing
     */
    public boolean isRepaintRequired();

    /**
     * Set the background colour that this surface should be cleared to before
     * the drawing step. Colours range from 0 to 1 in the normal manner.
     *
     * @param col An array of at least length 4 to copy values into
     */
    public void getClearColor(float[] col);
}
