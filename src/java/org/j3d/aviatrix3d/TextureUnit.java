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
 * Describes a texture stage and its associated texture and attributes.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class TextureUnit extends NodeComponent
{
    /** The texture object */
    private int texObject;

    /** The Texture for the unit. */
    private Texture texture;

    /** Has the texture object reference changed */
    private boolean texChanged;

    /** The Texture Attributes for the unit. */
    private TextureAttributes tatts;

    /** Has the texture attribute object reference changed */
    private boolean tattsChanged;

    /** The Texture Coordinate Generation for the unit. */
    private TexCoordGeneration tcg;

    /** Has the texture attribute object reference changed */
    private boolean tcgChanged;

    /** The Texture transform for the unit. */
    //private Matrix transform;

    private HashMap dispListMap = new HashMap(1);
    private int listName;

    /**
     * Constructs a Texture Unit with default values.
     */
    public TextureUnit()
    {
        texObject = 0;
    }

    /**
     * Construct a Texture Unit with the specified texture, attributes
     * and coordinate generation.
     */
    public TextureUnit(Texture texture,
                       TextureAttributes tatts,
                       TexCoordGeneration tcg)
    {

        this();

        this.texture = texture;
        texChanged = true;

        this.tatts = tatts;
        tattsChanged = true;

        this.tcg = tcg;
    }

    /**
     * Set the texture for this stage.
     *
     * @param texture The texture.  Null disables the stage.
     */
    public void setTexture(Texture texture)
    {
        this.texture = texture;
        texChanged = true;
    }

    /**
     * Set the texture attributes for this stage.
     *
     * @param tatts The texture attributes.  Null clears.
     */
    public void setTextureAttributes(TextureAttributes tatts)
    {
        this.tatts = tatts;
        tattsChanged = true;
    }

    /**
     * Set the texture coordinate generation for this stage.
     *
     * @param tog The texture coordinate generation.  Null clears.
     */
    public void setTexCoordGeneration(Texture texture)
    {
        this.tcg = tcg;
    }

    //----------------------------------------------------------
    // Methods overriding the NodeComponent class
    //----------------------------------------------------------

    /**
     * Issue ogl commands needed for this component
     *
     * @param gld The drawable for reseting the state
     */
    public void renderState(GLDrawable gld)
    {
        GLFunc gl = gld.getGL();
        GLContext glj = gld.getGLContext();

        // TODO: Should we move this to the Appearance node
        gl.glPushAttrib(GLEnum.GL_TEXTURE_BIT);

        Integer listName = (Integer)dispListMap.get(glj);

        if(texObject == 0)
        {
            int texName[] = new int[1];

            gl.glGenTextures(1, texName);
            texObject = texName[0];

            // Setup Texture state
            if (texture != null)
                texture.renderState(gld);

            // Setup TextureAttribute state
            if (tatts != null)
                tatts.renderState(gld);
            else
                gl.glTexEnvf(gl.GL_TEXTURE_ENV, gl.GL_TEXTURE_ENV_MODE, gl.GL_REPLACE);

            // Setup TexCoordGeneration state
            if (tcg != null)
                tcg.renderState(gld);


//            listName = new Integer(gl.glGenLists(1));

//            gl.glNewList(listName.intValue(), GLEnum.GL_COMPILE_AND_EXECUTE);

            gl.glEnable(gl.GL_TEXTURE_2D);
            // TODO: Is it safe to start with a 2D Texture for everything?
            gl.glTexEnvf(gl.GL_TEXTURE_ENV, gl.GL_TEXTURE_ENV_MODE, gl.GL_REPLACE);
            gl.glBindTexture(gl.GL_TEXTURE_2D, texObject);

//            gl.glEndList();
//            dispListMap.put(glj, listName);
        }
        else
        {

//            gl.glCallList(listName.intValue());

            gl.glBindTexture(gl.GL_TEXTURE_2D, texObject);

            // TODO: Why does this have to be here, is it not part of the TextureObject?
            gl.glTexEnvf(gl.GL_TEXTURE_ENV, gl.GL_TEXTURE_ENV_MODE, gl.GL_REPLACE);

            // Setup Texture state
            if (texture != null)
                texture.renderState(gld);

            // Setup TexttureAttribute state
            if (tatts != null && (tattsChanged || tatts.hasChanged()))
                tatts.renderState(gld);

            // Setup TexCoordGeneration state
            if (tcg != null && (tcgChanged || tcg.hasChanged()))
                tcg.renderState(gld);
        }
    }

    /**
     * Restore all openGL state to the given drawable.
     *
     * @param gld The drawable for reseting the state
     */
    public void restoreState(GLDrawable gld)
    {
        GLFunc gl = gld.getGL();

        if (texture != null)
            texture.restoreState(gld);

        // TODO: Should we move this the Appearance node
        // All other state restored by attribute pop
        gl.glPopAttrib();
    }
}