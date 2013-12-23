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
 * A marker interface to ensure a Texture component contains 3D data
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public abstract class TextureComponent3D extends TextureComponent
{
    /** The height */
    protected int height;

    /** The depth */
    protected int depth;

    /**
     * Constructs an image with default values.
     *
     * @param numLevels The number of mipmap levels to create
     */
    public TextureComponent3D(int numLevels)
    {
        super(numLevels);
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Get the height of this image.
     *
     * @return the height.
     */
    public int getHeight()
    {
        return height;
    }

    /**
     * Get the current depth of the image component.
     *
     * @return A value >= 0
     */
    public int getDepth()
    {
        return depth;
    }
}
