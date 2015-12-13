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
 * Marker describing a {@link Texture} that can make use of multipass rendering
 * component defined by a {@link MultipassTextureSource} to generate the source
 * data.
 * <p>
 *
 * <b>Note: This class does nothing in Aviatrix3D 1.0</b>
 * <p>
 *
 * The interface allows for multiple image sources to be updated, such as a
 * cubic environment map. Each method allows the setting of the values for each
 * image source separately. For some implementations of this class, not all
 * values of the image index will be acceptable. In that case, any invalid index
 * will throw an InvalidArgumentException. As OpenGL requires, it assumes
 * that all sources within a given texture index will be the same.
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 * @deprecated Use {@link OffscreenTexture2D} or {@link MRTOffscreenTexture2D}
 */
public interface MultipassTextureDestination
{
    /**
     * Check to see how many multipass texture sources are actually set for.
     * this instance of the destination.
     *
     * @return The number of defined sources >= 0
     */
    public int numMultipassSources();

    /**
     * Fetch all of the currently specified multipass texture sources in this
     * instance of the class. The values are copied into the arrays, along with
     * their corresponding image index and level for mipmapping. The array must
     * be at least as long as {@link #numMultipassSources()}.
     *
     * @param sources An array to copy the current sources into
     * @param images The indices of each of the source images
     */
    public void getMultipassSources(MultipassTextureSource[] sources,
                                    int[] images);

    /**
     * The multipass source has completed rendering and the implemented class
     * should now copy the image data across now. The location information will
     * be derived from the viewport size defined as part of the source's
     * ViewEnvironment.
     *
     * @param gl The gl context to draw with
     *     * @param x The x offset in pixels to start the copy from
     * @param y The y offset in pixels to start the copy from
     * @param width The width in pixels of the texture that was rendered
     * @param height The height in pixels of the texture that was rendered
     * @param imgNum The index of the texture source to copy to
     * @param level The mipmap level that this corresponds to
     */
    public void updateMultipassSource(GL2 gl,
                                      int x,
                                      int y,
                                      int width,
                                      int height,
                                      int imgNum,
                                      int level);

    /**
     * Set the buffer that this texture should read it's input from during the
     * update callback.
     *
     * @param imgNum The index of the image that this offset applies to
     * @param buffer The identifier of the buffer to read from
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void setReadBuffer(int imgNum, int buffer)
        throws InvalidWriteTimingException;

    /**
     * Get the current read buffer that is being used.
     *
     * @param imgNum The index of the image that this offset applies to
     * @return One of the buffer indicies.
     */
    public int getReadBuffer(int imgNum);

    /**
     * Set the offsets in this texture to use for update the sub image
     * update values.
     *
     * @param imgNum The index of the image that this offset applies to
     * @param xoffset The x offset in pixels to start the copy at
     * @param yoffset The y offset in pixels to start the copy at
     * @param level The mipmap level that this corresponds to
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void setCopyOffset(int imgNum, int level, int xoffset, int yoffset)
        throws InvalidWriteTimingException;

    /**
     * Get the current copy offset. The return values are copied into the
     * user provided array as [xoffset, yoffset].
     *
     * @param imgNum The index of the image that this offset applies to
     * @param level The mipmap level that this corresponds to
     * @param offsets An array to copy the values into
     */
    public void getCopyOffset(int imgNum, int level, int[] offsets);
}
