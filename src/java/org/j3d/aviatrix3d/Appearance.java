/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
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
import net.java.games.jogl.GL;
import net.java.games.jogl.GLU;

// Local imports
// None

/**
 * Describes the appearance of an object.
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class Appearance extends NodeComponent
{
    /** The material properites */
    private Material material;

    /** The texture properties */
    private TextureUnit[] texUnits;

    /**
     * The default constructor.
     */
    public Appearance()
    {
    }

    /**
     * Set the material to use.  null will clear the material
     *
     * @param mat The new material
     */
    public void setMaterial(Material mat)
    {
        material = mat;
    }

    /**
     * Set the texture units to use.  Null will disable texturing.
     *
     * @param texture The new Texture
     */
    public void setTextureUnits(TextureUnit[] texUnits)
    {
        this.texUnits = texUnits;
    }

    /**
     * Issue ogl commands needed for this component.
     *
     * @param gld The surface to draw upon
     */
    public void renderState(GL gl, GLU glu)
    {
        if(material != null)
            material.renderState(gl, glu);

        if(texUnits != null)
        {
            int len = texUnits.length;

            for(int i=0; i < len; i++)
            {
                switch(i)
                {
                    case 0: gl.glActiveTextureARB(GL.GL_TEXTURE0_ARB);
                          break;
                    case 1: gl.glActiveTextureARB(GL.GL_TEXTURE1_ARB);
                          break;
                    case 2: gl.glActiveTextureARB(GL.GL_TEXTURE2_ARB);
                          break;
                    case 3: gl.glActiveTextureARB(GL.GL_TEXTURE3_ARB);
                          break;
                }

                texUnits[i].renderState(gl, glu);
            }
        }
    }

    /**
     * Restore all openGL state.
     *
     * @param gld The surface to draw upon
     */
    public void restoreState(GL gl, GLU glu)
    {
        if(texUnits != null)
        {
            int len = texUnits.length;

            for(int i=len-1; i >= 0; i--)
                texUnits[i].restoreState(gl, glu);
        }

        if(material != null)
            material.restoreState(gl, glu);
    }
}