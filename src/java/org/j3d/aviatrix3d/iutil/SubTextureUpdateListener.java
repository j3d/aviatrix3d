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

package org.j3d.aviatrix3d.iutil;

// External imports
import java.nio.ByteBuffer;

// Local imports
// None

/**
 * Internal interface used to communicate update notifications for the source
 * data used for textures.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface SubTextureUpdateListener
{
    /**
     * Notification that the texture has updated a section of the texture.
     * This is generic for all textures correctly. If a coordinate is not used,
     * it will be set to 0 for position and 1 for width.
     *
     * @param x The start location x coordinate in texel space
     * @param y The start location y coordinate in texel space
     * @param z The start location z coordinate in texel space
     * @param width The width of the update in texel space
     * @param height The height of the update in texel space
     * @param depth The depth of the update in texel space
     * @param level The mipmap level that changed
     * @param pixels Buffer of the data that has updated
     */
    public void textureUpdated(int x,
                               int y,
                               int z,
                               int width,
                               int height,
                               int depth,
                               int level,
                               byte[] pixels);
}
