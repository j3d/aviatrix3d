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

// Standard imports
import java.util.HashMap;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLU;

// Local imports
// None

/**
 * Describes the material properties of an object.
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class Material extends NodeComponent
{
    /** Default diffuse colour */
    private static final float[] DEFAULT_DIFFUSE = {0.8f, 0.8f, 0.8f};

    /** Default ambient colour */
    private static final float[] DEFAULT_AMBIENT = {0.2f, 0.2f, 0.2f};

    /** Default emissive colour */
    private static final float[] DEFAULT_EMISSIVE = {0, 0, 0};

    /** Default specular colour */
    private static final float[] DEFAULT_SPECULAR = {0, 0, 0};

    /** Default shininess colour */
    private static final float DEFAULT_SHININESS = 0.2f;


    /** Diffuse colour components */
    private float[] diffuseColor;

    /** Ambient colour components */
    private float[] ambientColor;

    /** Emissive colour components */
    private float[] emissiveColor;

    /** Specular colour components */
    private float[] specularColor;

    /** shinine colour factor */
    private float shininess;

    /** A mapping between glContext and displayListID(Integer) */
    private HashMap dispListMap;

    /**
     * Constructs a material with default values.
     * <pre>
     * ambient color = (0.2, 0.2, 0.2)
     * emisive color = (0.0, 0.0, 0.0)
     * diffuse color = (0.8, 0.8, 0.8)
     * specular color = (0, 0, 0)
     * shininess = 0.2
     */
    public Material()
    {
        this(DEFAULT_AMBIENT,
             DEFAULT_EMISSIVE,
             DEFAULT_DIFFUSE,
             DEFAULT_SPECULAR,
             DEFAULT_SHININESS);
    }

    /**
     * Create a new material with all the colours specified.
     *
     * @param ambientColor
     * @param emisiveColor
     * @param diffuseColor
     * @param specularColor
     * @param shininess
     */
    public Material(float[] ambientColor,
                    float[] emissiveColor,
                    float[] diffuseColor,
                    float[] specularColor,
                    float shininess)
    {
        this.diffuseColor = new float[3];
        this.diffuseColor[0] = diffuseColor[0];
        this.diffuseColor[1] = diffuseColor[1];
        this.diffuseColor[2] = diffuseColor[2];

        this.ambientColor = new float[3];
        this.ambientColor[0] = ambientColor[0];
        this.ambientColor[1] = ambientColor[1];
        this.ambientColor[2] = ambientColor[2];

        this.specularColor = new float[3];
        this.specularColor[0] = specularColor[0];
        this.specularColor[1] = specularColor[1];
        this.specularColor[2] = specularColor[2];

        this.emissiveColor = new float[3];
        this.emissiveColor[0] = emissiveColor[0];
        this.emissiveColor[1] = emissiveColor[1];
        this.emissiveColor[2] = emissiveColor[2];

        // Convert [0,1] to [0,128] for openGL
        this.shininess = 128 * shininess;

        dispListMap = new HashMap(1);
    }

    /**
     * Set the shininess factor.
     *
     * @param s The shininess factor value
     */
    public void setShininess(float s)
    {
        // Convert [0,1] to [0,128] for openGL
        shininess = 128 * s;
    }

    /**
     * Set the diffuse color to the new value.
     *
     * @param col The new colour to use
     */
    public void setDiffuseColor(float[] col)
    {
        diffuseColor[0] = col[0];
        diffuseColor[1] = col[1];
        diffuseColor[2] = col[2];
    }

    /**
     * Set the specular color to the new value.
     *
     * @param col The new colour to use
     */
    public void setSpecularColor(float[] col)
    {
        specularColor[0] = col[0];
        specularColor[1] = col[1];
        specularColor[2] = col[2];
    }

    /**
     * Set the emissive color to the new value.
     *
     * @param col The new colour to use
     */
    public void setEmissiveColor(float[] col)
    {
        emissiveColor[0] = col[0];
        emissiveColor[1] = col[1];
        emissiveColor[2] = col[2];
    }

    /**
     * Set the ambient color to the new value.
     *
     * @param col The new colour to use
     */
    public void setAmbientColor(float[] col)
    {
        ambientColor[0] = col[0];
        ambientColor[1] = col[1];
        ambientColor[2] = col[2];
    }

    /**
     * Issue ogl commands needed for this component
     *
     * @param gld The drawable for reseting the state
     */
    public void renderState(GL gl, GLU glu)
    {
        Integer listName = (Integer)dispListMap.get(gl);

        gl.glPushAttrib(GL.GL_LIGHTING_BIT);

        if(listName == null)
        {
            listName = new Integer(gl.glGenLists(1));

            gl.glNewList(listName.intValue(), GL.GL_COMPILE_AND_EXECUTE);

            // TODO: Do we need to set BACK props for two sided lighting?

            gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, diffuseColor);
            gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, ambientColor);
            gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, specularColor);
            gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_EMISSION, emissiveColor);
            gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, shininess);

            gl.glEndList();
            dispListMap.put(gl, listName);
        }
        else
        {
            gl.glCallList(listName.intValue());
        }

    }

    /**
     * Restore all openGL state to the given drawable
     *
     * @param gld The drawable for reseting the state
     */
    public void restoreState(GL gl, GLU glu)
    {
        gl.glPopAttrib();
    }
}