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
// None

// Local imports
// None

/**
 * Marker interface representing a class that provides source data for a
 * {@link Texture} object.
 * <p>
 *
 * The source can come from many different places, such as external data
 * images, streams etc), or dynamically generated (multipass rendering). All
 * texture objects require a source of data, and a single source can be added
 * to multiple texture objects.
 * <p>
 *
 * It is not intended that this interface be used directly as there is not much
 * directly usable information. Derived versions of this interface will provide
 * more interesting and source-specific behaviour.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public interface TextureSource
{
    /** Specifies the source is in RGB format */
    public static final int FORMAT_RGB = 1;

    /** Specifies the source is in RGBA format */
    public static final int FORMAT_RGBA = 2;

    /** Specifies the source is in Windows BGR format */
    public static final int FORMAT_BGR = 3;

    /** Specifies the source is in Windows BGRA format */
    public static final int FORMAT_BGRA = 4;

    /** Specifies the source is in 2-component Intensity-Alpha format */
    public static final int FORMAT_INTENSITY_ALPHA = 5;

    /** Specifies the source is in 2-component Luminance--Alpha format */
    public static final int FORMAT_LUMINANCE_ALPHA = 7;

    /** Specifies the source is in 1-component Intensity or Alpha format */
    public static final int FORMAT_SINGLE_COMPONENT = 6;

    /**
     * Get the width of this image.
     *
     * @return the width.
     */
    public int getWidth();

    /**
     * Get the number of levels for the mipmapping in this source.
     *
     * @return The number of levels.
     */
    public int getNumLevels();

    /**
     * Get the format of this image at the given mipmap level.
     *
     * @param level The mipmap level to get the format for
     * @return the format.
     */
    public int getFormat(int level);
}
