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
import java.awt.image.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import java.util.HashMap;

import javax.media.opengl.GL;

// Local imports
// None

/**
 * A marker interface to ensure a Texture component contains 1D data
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public abstract class TextureComponent1D extends TextureComponent
{
    /**
     * Constructs an image with default values.
     *
     * @param numLevels The number of mipmap levels to create
     */
    public TextureComponent1D(int numLevels)
    {
        super(numLevels);
    }
}
