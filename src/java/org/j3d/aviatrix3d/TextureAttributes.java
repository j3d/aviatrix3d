/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003
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

// Standard imports
import java.util.HashMap;

// Application specific imports
import gl4java.GLFunc;
import gl4java.GLContext;
import gl4java.GLEnum;
import gl4java.drawable.GLDrawable;

/**
 * Describes a texture's attributes.
 *
 * All of these attributes are part of the texture object
 * so we only issue OGL commands when they change.  This will
 * update the Texture Object's values for all future uses.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class TextureAttributes extends NodeComponent
{
    public static final int MODE_REPLACE = GLEnum.GL_REPLACE;
    public static final int MODE_MODULATE = GLEnum.GL_MODULATE;

    /** The texturing mode */
    private int texMode;

    /** Has an attribute changed */
    private boolean changed;

    /**
     * Constructs a Texture Unit with default values.
     */
    public TextureAttributes()
    {
        changed = true;
        texMode = GLEnum.GL_REPLACE;
    }

    /**
     * Issue ogl commands needed for this component
     *
     * @param gld The drawable for reseting the state
     */
    public void renderState(GLDrawable gld)
    {
        GLFunc gl = gld.getGL();

        gl.glTexEnvf(gl.GL_TEXTURE_ENV, gl.GL_TEXTURE_ENV_MODE, texMode);

        changed = false;
    }

    /**
     * Restore all openGL state to the given drawable.
     *
     * @param gld The drawable for reseting the state
     */
    public void restoreState(GLDrawable gld)
    {
        // State restoration will be handled by the TextureUnit
    }

    /**
     * Set the texture mode.
     *
     * @param mode The new mode
     */
    public void setTextureMode(int mode)
    {
        texMode = mode;

        changed = true;
    }
}