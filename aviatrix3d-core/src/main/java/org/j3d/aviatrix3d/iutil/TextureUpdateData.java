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
 * Internal data-holder class that holds the specs of the notification about
 * sub-image updates for a texture.
 * <p>
 *
 * The class does not know anything about the type of texture it is supposed
 * to be updating. Thus, it is entirely dependent on the various users to make
 * sure they do sane things. For example, a 2D texture component only gets used
 * with a 2D texture. For the coordinate dimensions that are not used, for
 * safety, the location should be set to 0 and the distance set to 1.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class TextureUpdateData
{
    /** The start location x coordinate in texel space. */
    public int x;

    /** The start location y coordinate in texel space. */
    public int y;

    /** The start location z coordinate in texel space. */
    public int z;

    /** The width of the update in texel space. */
    public int width;

    /** The height of the update in texel space. */
    public int height;

    /** The depth of the update in texel space. */
    public int depth;

    /** Which level this relates to in the mipmap space. */
    public int level;

    /**
     * The format (RGB, INTENSITY etc) of the data. Must be a GL constant,
     * and not one of the Aviatrix3D texture constants.
     */
    public int format;

    /** Buffer of the data that has updated. */
    public ByteBuffer pixels;
}
