/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2006
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
import java.util.HashMap;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.DeletableRenderable;
import org.j3d.aviatrix3d.rendering.TransparentObjectRenderable;

/**
 * Describes the material properties of an object.
 * <p>
 *
 * Materials may be either single or double sided for an object. By default,
 * this class sets up for single-sided rendering. All material values are sent
 * to OpenGL as GL_FRONT_AND_BACK for the target unless otherwise specified.
 * Whatever these settings, the values will not be used unless the two-sided
 * lighting value is also enabled on the {@link PolygonAttributes} that
 * accompanies this material. Without that turned on, the back-face material
 * values defined here will not be used by OpenGL.
 * <p>
 *
 * Because the material node is used almost everywhere in scene graphs, and
 * two-sided material handling uses twice the amount of memory, we've made some
 * special rules about using it in order to reduce memory footprint. As the
 * use of separate front and back face colours are relatively rare in use, we
 * don't allocate the internal memory for their storage unless the flag is
 * turned on. This then forces another interaction rule. The memory structures
 * won't be in place unless the flag is turned on, so if you attempted to read
 * or write to them when the flag is turned off, NullPointerExceptions will be
 * generated all over the place. To prevent this, we perform a check before
 * every back-face access to make sure that the flag is currently set, and if
 * not, will issue our own exception {@link java.lang.IllegalStateException}.
 * <p>
 *
 * The default values for this class mirror the OpenGL defaults:
 * <ul>
 * <li>ambient color (0.2, 0.2, 0.2)</li>
 * <li>emisive color (0.0, 0.0, 0.0)</li>
 * <li>diffuse color (0.8, 0.8, 0.8)</li>
 * <li>specular color (0, 0, 0)</li>
 * <li>shininess  0.2</li>
 * <li>transparency 1</li>
 * <li>use lighting  true</li>
 * <li>separated back face colours false</li>
 * <li>use colorMaterial false</li>
 * <li>colorTarget AMBIENT_AND_DIFFUSE_TARGET</li>
 * <li>backColorTarget AMBIENT_AND_DIFFUSE_TARGET</li>
 * </ul>
 *
 * When combining transparency and color material handling, we need to control
 * what gets blended. Sometimes the incoming vertex values have 4 component
 * color, sometimes 3. Sometimes you'd like the material's transparency to
 * override the externally provided color's alpha value, and others not. To
 * deal with this situation, a separate flag is enabled allowing you to control
 * whether the transparency should be treated separately to the color material.
 * If this is set to true, the transparency is set up as a blending factor with
 * the OpenGL blending API - there is no need to make use of a separate
 * {@link BlendAttributes} object for this. If you also happen to provide a
 * {@link BlendAttributes} instance to the containing appearance, that will
 * override what happens internally to this class.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>wrongColorMaterialTargetMsg: Error message when a non _TARGET constant is
 *     used to set the target for the colour material.</li>
 * <li>backfaceWriteMsg: Error message when attempting to change a backface field
 *     with that ability not enabled</li>
 * <li>backfaceReadMsg: Error message when attempting to read a backface field
 *     with that ability not enabled</li>
 * <li>shininessRangeMsg: Error message when the shininess is not [0,1]</li>
 * <li>transparencyRangeMsg: Error message when the transparency is not [0,1]</li>
 * </ul>
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.47 $
 */
public class Material extends NodeComponent
    implements TransparentObjectRenderable, DeletableRenderable
{
    /** Set the color material target as the ambient light component */
    public static final int AMBIENT_TARGET = GL2.GL_AMBIENT;

    /** Set the color material target as the diffuse light component */
    public static final int DIFFUSE_TARGET = GL2.GL_DIFFUSE;

    /** Set the color material target as the specular light component */
    public static final int SPECULAR_TARGET = GL2.GL_SPECULAR;

    /** Set the color material target as the emissive light component */
    public static final int EMISSIVE_TARGET = GL2.GL_EMISSION;

    /**
     * Set the color material target as the ambient and diffuse light
     * component. This is the default setting if nothing is explicitly
     * set by the user.
     */
    public static final int AMBIENT_AND_DIFFUSE_TARGET =
        GL2.GL_AMBIENT_AND_DIFFUSE;

    /** When the user passes in an invalid material target */
    private static final String COLOR_MATERIAL_TARGET_PROP =
        "org.j3d.aviatrix3d.Material.wrongColorMaterialTargetMsg";

    /**
     * Error message when the user attempts to set a value for a backface
     * property when it is not currently enabled.
     */
    private static final String NO_BACKFACE_WRITE_PROP =
        "org.j3d.aviatrix3d.Material.backfaceWriteMsg";

    /**
     * Error message when the user attempts to read a value for a backface
     * property when it is not currently enabled.
     */
    private static final String NO_BACKFACE_READ_PROP =
        "org.j3d.aviatrix3d.Material.backfaceReadMsg";

    /** Error message when the shininess is out of the [0,1] range */
    private static final String SHININESS_RANGE_PROP =
        "org.j3d.aviatrix3d.Material.shininessRangeMsg";

    /** Error message when the transparency is out of the [0,1] range */
    private static final String TRANSPARENCY_RANGE_PROP =
        "org.j3d.aviatrix3d.Material.transparencyRangeMsg";

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

    /** Default transparency value */
    private static final float DEFAULT_TRANSPARENCY = 1;

    /**
     * Global flag to know if we are capable of rendering multitextures.
     * This gets queried on the first time rendering is run and set
     * appropriately. After that, if this is set to false, then any
     * texture unit that has it's ID greater than 0 is just ignored.
     */
    private static boolean hasImaging = false;

    /** Flag to say we've queried for the multitexture API capabilities */
    private static boolean queryComplete = false;


    /** Should lighting be used for this material? */
    private boolean useLighting;

    /** Diffuse colour components for the front face. */
    private float[] diffuseColor;

    /** Diffuse colour components for the back face. */
    private float[] backDiffuseColor;

    /** Ambient colour components for the front face. */
    private float[] ambientColor;

    /** Ambient colour components for the back face. */
    private float[] backAmbientColor;

    /** Emissive colour components for the front face. */
    private float[] emissiveColor;

    /** Emissive colour components for the back face. */
    private float[] backEmissiveColor;

    /** Specular colour components for the front face. */
    private float[] specularColor;

    /** Specular colour components for the back face. */
    private float[] backSpecularColor;

    /** shinine colour factor for the front face. */
    private float shininess;

    /** shinine colour factor for the front face. */
    private float backShininess;

    /** Enable color material handling */
    private boolean useColorMaterial;

    /** The target for the colour material for the front face, if set */
    private int colorTarget;

    /** The target for the colour material for the back face, if set */
    private int backColorTarget;

    /**
     * Flag to indicate if separate back and front face settings are to be
     * sent to OpenGL.
     */
    private boolean separatedBackFace;

    /** Flag to enable separate diffuse alpha blending */
    private boolean useSeparateDiffuseAlpha;

    /**
     * Flag indicating that we have the combination of non-opaque transparency
     * and the colorMaterial target set to use either the diffuse or
     * diffuse and ambient components. When this is the case, we need to set
     * a separate blend function up that points to the transparency value
     * from this instance. It assume that blending will be already set up by
     * the state sorting stage.
     */
    private boolean blendDiffuseAlpha;

    /** A mapping between glContext and displayListID(Integer) */
    private HashMap<GL, Integer> displayListMap;

    /** A mapping for displaylists that have been deleted */
    private HashMap<GL, Integer> deletedDisplayListMap;

    /**
     * Constructs a material with default values.
     * <pre>
     * ambient color = (0.2, 0.2, 0.2)
     * emisive color = (0.0, 0.0, 0.0)
     * diffuse color = (0.8, 0.8, 0.8)
     * specular color = (0, 0, 0)
     * shininess = 0.2
     * transparency = 1
     * </pre>
     */
    public Material()
    {
        this(DEFAULT_AMBIENT,
             DEFAULT_EMISSIVE,
             DEFAULT_DIFFUSE,
             DEFAULT_SPECULAR,
             DEFAULT_SHININESS,
             DEFAULT_TRANSPARENCY);
    }

    /**
     * Create a new material with all the colours specified.
     *
     * @param ambientColor
     * @param emissiveColor
     * @param diffuseColor
     * @param specularColor
     * @param shininess The shininess factor value
     * @param transparency A transparency value between 0 and 1
     */
    public Material(float[] ambientColor,
                    float[] emissiveColor,
                    float[] diffuseColor,
                    float[] specularColor,
                    float shininess,
                    float transparency)
    {
        this.diffuseColor = new float[4];
        this.diffuseColor[0] = diffuseColor[0];
        this.diffuseColor[1] = diffuseColor[1];
        this.diffuseColor[2] = diffuseColor[2];
        this.diffuseColor[3] = transparency;

        this.ambientColor = new float[4];
        this.ambientColor[0] = ambientColor[0];
        this.ambientColor[1] = ambientColor[1];
        this.ambientColor[2] = ambientColor[2];

        if(ambientColor.length > 3)
            this.ambientColor[3] = ambientColor[3];
        else
            this.ambientColor[3] = 1;

        this.specularColor = new float[4];
        this.specularColor[0] = specularColor[0];
        this.specularColor[1] = specularColor[1];
        this.specularColor[2] = specularColor[2];

        if(specularColor.length > 3)
            this.specularColor[3] = specularColor[3];
        else
            this.specularColor[3] = 1;

        this.emissiveColor = new float[4];
        this.emissiveColor[0] = emissiveColor[0];
        this.emissiveColor[1] = emissiveColor[1];
        this.emissiveColor[2] = emissiveColor[2];

        if(emissiveColor.length > 3)
            this.emissiveColor[3] = emissiveColor[3];
        else
            this.emissiveColor[3] = 1;

        // Convert [0,1] to [0,128] for openGL
        this.shininess = 128 * shininess;
        useLighting = true;
        separatedBackFace = false;
        blendDiffuseAlpha = false;

        useColorMaterial = false;
        colorTarget = AMBIENT_AND_DIFFUSE_TARGET;
        backColorTarget = AMBIENT_AND_DIFFUSE_TARGET;

        displayListMap = new HashMap<GL, Integer>(1);
        deletedDisplayListMap = new HashMap<GL, Integer>(1);
    }

    //----------------------------------------------------------
    // Methods defined by TransparentRenderable
    //----------------------------------------------------------

    /**
     * Ask the texture if it has any transparency values. The implementation
     * should determine this from it's internal set of state, such as looking
     * at the texture formats etc to see if they include an alpha channel
     *
     * @return true if any form of non-opaque rendering is defined
     */
    @Override
    public boolean hasTransparency()
    {
        return getTransparency() != 1;
    }

    //---------------------------------------------------------------
    // Methods defined by ObjectRenderable
    //---------------------------------------------------------------

    /**
     * Issue ogl commands needed for this component
     *
     * @param gl The gl context to draw with
     */
    @Override
    public void render(GL2 gl)
    {
        if(!queryComplete)
        {
             hasImaging = gl.isExtensionAvailable("GL_ARB_imaging");
             queryComplete = true;
        }

        // If we have changed state, then clear the old display lists
        if(deletedDisplayListMap.size() != 0)
        {
            Integer listName = deletedDisplayListMap.remove(gl);

            if(listName != null)
                gl.glDeleteLists(listName.intValue(), 1);
        }

        Integer listName = displayListMap.get(gl);

        if(listName == null)
        {

            listName = new Integer(gl.glGenLists(1));
			
            gl.glNewList(listName.intValue(), GL2.GL_COMPILE);

            if(useLighting)
                gl.glEnable(GL2.GL_LIGHTING);
            else
                gl.glDisable(GL2.GL_LIGHTING);

            if(useColorMaterial)
            {
                gl.glEnable(GL2.GL_COLOR_MATERIAL);
                if(separatedBackFace)
                {
                    gl.glColorMaterial(GL.GL_FRONT, colorTarget);
                    gl.glColorMaterial(GL.GL_BACK, backColorTarget);
                }
                else
                {
                    gl.glColorMaterial(GL.GL_FRONT_AND_BACK, colorTarget);
                }

                if(blendDiffuseAlpha)
                {
                    if(hasImaging)
                    {
                        gl.glBlendColor(0, 0, 0, diffuseColor[3]);
                        gl.glBlendFunc(GL.GL_SRC_ALPHA,
                                       GL2.GL_ONE_MINUS_CONSTANT_ALPHA);
                    }
                    else
                    {
                        gl.glBlendFunc(GL.GL_SRC_ALPHA,
                                       GL.GL_ONE_MINUS_SRC_ALPHA);
                    }
                }
            }
            else
            {
                gl.glDisable(GL2.GL_COLOR_MATERIAL);
            }

            if(separatedBackFace)
            {
                gl.glMaterialfv(GL.GL_FRONT, GL2.GL_DIFFUSE, diffuseColor, 0);
                gl.glMaterialfv(GL.GL_FRONT, GL2.GL_AMBIENT, ambientColor, 0);
                gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR, specularColor, 0);
                gl.glMaterialfv(GL.GL_FRONT, GL2.GL_EMISSION, emissiveColor, 0);
                gl.glMaterialf(GL.GL_FRONT, GL2.GL_SHININESS, shininess);

                gl.glMaterialfv(GL.GL_BACK, GL2.GL_DIFFUSE, backDiffuseColor, 0);
                gl.glMaterialfv(GL.GL_BACK, GL2.GL_AMBIENT, backAmbientColor, 0);
                gl.glMaterialfv(GL.GL_BACK, GL2.GL_SPECULAR, backSpecularColor, 0);
                gl.glMaterialfv(GL.GL_BACK, GL2.GL_EMISSION, backEmissiveColor, 0);
                gl.glMaterialf(GL.GL_BACK, GL2.GL_SHININESS, backShininess);
            }
            else
            {
                gl.glMaterialfv(GL.GL_FRONT_AND_BACK,
                                GL2.GL_DIFFUSE,
                                diffuseColor,
                                0);
                gl.glMaterialfv(GL.GL_FRONT_AND_BACK,
                                GL2.GL_AMBIENT,
                                ambientColor,
                                0);
                gl.glMaterialfv(GL.GL_FRONT_AND_BACK,
                                GL2.GL_SPECULAR,
                                specularColor,
                                0);
                gl.glMaterialfv(GL.GL_FRONT_AND_BACK,
                                GL2.GL_EMISSION,
                                emissiveColor,
                                0);
                gl.glMaterialf(GL.GL_FRONT_AND_BACK,
                                GL2.GL_SHININESS,
                                shininess);
            }

            gl.glEndList();
            displayListMap.put(gl, listName);
        }

        gl.glCallList(listName.intValue());
    }

    /**
     * Restore all openGL state to the given drawable
     *
     * @param gl The gl context to draw with
     */
    @Override
    public void postRender(GL2 gl)
    {
        if(useLighting)
            gl.glDisable(GL2.GL_LIGHTING);

        if(useColorMaterial)
            gl.glDisable(GL2.GL_COLOR_MATERIAL);
    }

    //---------------------------------------------------------------
    // Methods defined by Comparable
    //---------------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param o The object to be compared
     * @return -1, 0 or 1 depending on order
     * @throws ClassCastException The specified object's type prevents it from
     *    being compared to this Object
     */
    @Override
    public int compareTo(Object o)
        throws ClassCastException
    {
        Material mat = (Material)o;
        return compareTo(mat);
    }

    //---------------------------------------------------------------
    // Methods defined by Object
    //---------------------------------------------------------------

    /**
     * Compare this object for equality to the given object.
     *
     * @param o The object to be compared
     * @return True if these represent the same values
     */
    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof Material))
            return false;
        else
            return equals((Material)o);
    }

    //----------------------------------------------------------
    // Methods defined by SceneGraphObject
    //----------------------------------------------------------

    /**
     * Notification that this object is live now. Overridden to make sure that
     * the live state of the nodes represents the same state as the parent
     * scene graph.
     *
     * @param state true if this should be marked as live now
     */
    @Override
    protected void setLive(boolean state)
    {
        super.setLive(state);

        if(!state && updateHandler != null)
            updateHandler.requestDeletion(this);
    }
	
	//---------------------------------------------------------------
    // Methods defined by DeletableRenderable
    //---------------------------------------------------------------

    /**
     * Cleanup the object now for the given GL context.
     *
     * @param gl The gl context to draw with
     */
    @Override
    public void cleanup(GL2 gl)
    {
		if(deletedDisplayListMap.size() != 0)
        {
            Integer listName = deletedDisplayListMap.remove(gl);

            if(listName != null)
			{
                gl.glDeleteLists(listName.intValue(), 1);
			}
        }

		if(displayListMap.size() != 0)
        {
            Integer listName = displayListMap.remove(gl);

            if(listName != null)
			{
                gl.glDeleteLists(listName.intValue(), 1);
			}
        }
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Set the shininess factor for the front or combined faces. The
     * shininess should be a value between 0 and 1.
     *
     * @param s The shininess factor value
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data change callback method
     * @throws IllegalArgumentException The shininess value is outside the
     *   range of [0,1]
     */
    public void setShininess(float s)
        throws InvalidWriteTimingException, IllegalArgumentException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(s < 0 || s > 1)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(SHININESS_RANGE_PROP) + s;
            throw new IllegalArgumentException(msg);
        }

        // Convert [0,1] to [0,128] for openGL
        shininess = 128 * s;

        clearActiveList();
    }

    /**
     * Set the shininess factor for the back face value. The shininess should
     * be a value between 0 and 1.
     *
     * @param s The shininess factor value
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data change callback method
     * @throws IllegalStateException Attempting to write to the value when the
     *   separate backface flag is not turned on.
     */
    public void setBackShininess(float s)
        throws InvalidWriteTimingException, IllegalStateException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(!separatedBackFace)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NO_BACKFACE_WRITE_PROP);
            throw new IllegalStateException(msg);
        }

        if(s < 0 || s > 1)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(SHININESS_RANGE_PROP) + s;
            throw new IllegalArgumentException(msg);
        }

        // Convert [0,1] to [0,128] for openGL
        backShininess = 128 * s;

        clearActiveList();
    }

    /**
     * Get the current shininess value for the front or combined faces.
     *
     * @return a number between 0 and 1
     */
    public float getShininess()
    {
        return shininess / 128f;
    }

    /**
     * Get the current shininess value for the back face.
     *
     * @return a number between 0 and 1
     * @throws IllegalStateException Attempting to read the value when the
     *   separate backface flag is not turned on.
     */
    public float getBackShininess()
        throws IllegalStateException
    {
        if(!separatedBackFace)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NO_BACKFACE_READ_PROP);
            throw new IllegalStateException(msg);
        }

        return backShininess / 128f;
    }

    /**
     * Set the diffuse color to the new value for the front and combined faces.
     *
     * @param col The new colour to use
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data change callback method
     */
    public void setDiffuseColor(float[] col)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        diffuseColor[0] = col[0];
        diffuseColor[1] = col[1];
        diffuseColor[2] = col[2];

        clearActiveList();
    }

    /**
     * Set the diffuse color to the new value for the back face value.
     *
     * @param col The new colour to use
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data change callback method
     * @throws IllegalStateException Attempting to write to the value when the
     *   separate backface flag is not turned on.
     */
    public void setBackDiffuseColor(float[] col)
        throws InvalidWriteTimingException, IllegalStateException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(!separatedBackFace)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NO_BACKFACE_WRITE_PROP);
            throw new IllegalStateException(msg);
        }

        backDiffuseColor[0] = col[0];
        backDiffuseColor[1] = col[1];
        backDiffuseColor[2] = col[2];

        clearActiveList();
    }

    /**
     * Get the current value of the diffuse color of the front and combined
     * faces. The array should be at least length 3 or 4.
     *
     * @param col The array to copy the values into
     */
    public void getDiffuseColor(float[] col)
    {
        col[0] = diffuseColor[0];
        col[1] = diffuseColor[1];
        col[2] = diffuseColor[2];

        if(col.length > 3)
            col[3] = diffuseColor[3];
    }

    /**
     * Get the current value of the diffuse color for the back face. The array
     * should be at least length 3 or 4.
     *
     * @param col The array to copy the values into
     * @throws IllegalStateException Attempting to read the value when the
     *   separate backface flag is not turned on.
     */
    public void getBackDiffuseColor(float[] col)
        throws IllegalStateException
    {
        if(!separatedBackFace)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NO_BACKFACE_READ_PROP);
            throw new IllegalStateException(msg);
        }

        col[0] = backDiffuseColor[0];
        col[1] = backDiffuseColor[1];
        col[2] = backDiffuseColor[2];

        if(col.length > 3)
            col[3] = backDiffuseColor[3];
    }

    /**
     * Set the specular color to the new value for the front-face and combined
     * values.
     *
     * @param col The new colour to use
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data change callback method
     */
    public void setSpecularColor(float[] col)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        specularColor[0] = col[0];
        specularColor[1] = col[1];
        specularColor[2] = col[2];

        if(col.length > 3)
            specularColor[3] = col[3];
        else
            specularColor[3] = 1;

        clearActiveList();
    }

    /**
     * Set the specular color to the new value for the back face value.
     *
     * @param col The new colour to use
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data change callback method
     * @throws IllegalStateException Attempting to write to the value when the
     *   separate backface flag is not turned on.
     */
    public void setBackSpecularColor(float[] col)
        throws InvalidWriteTimingException, IllegalStateException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(!separatedBackFace)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NO_BACKFACE_WRITE_PROP);
            throw new IllegalStateException(msg);
        }

        backSpecularColor[0] = col[0];
        backSpecularColor[1] = col[1];
        backSpecularColor[2] = col[2];

        if(col.length > 3)
            backSpecularColor[3] = col[3];
        else
            backSpecularColor[3] = 1;

        clearActiveList();
    }

    /**
     * Get the current value of the specular color for the front or combined
     * faces. The array should be at least length 3 or 4.
     *
     * @param col The array to copy the values into
     */
    public void getSpecularColor(float[] col)
    {
        col[0] = specularColor[0];
        col[1] = specularColor[1];
        col[2] = specularColor[2];

        if(col.length > 3)
            col[3] = specularColor[3];
    }

    /**
     * Get the current value of the specular color for the back face. The array
     * should be at least length 3 or 4.
     *
     * @param col The array to copy the values into
     * @throws IllegalStateException Attempting to read the value when the
     *   separate backface flag is not turned on.
     */
    public void getBackSpecularColor(float[] col)
        throws IllegalStateException
    {
        if(!separatedBackFace)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NO_BACKFACE_READ_PROP);
            throw new IllegalStateException(msg);
        }

        col[0] = backSpecularColor[0];
        col[1] = backSpecularColor[1];
        col[2] = backSpecularColor[2];

        if(col.length > 3)
            col[3] = backSpecularColor[3];
    }

    /**
     * Set the emissive color to the new value for the front-face and combined
     * values.
     *
     * @param col The new colour to use
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data change callback method
     */
    public void setEmissiveColor(float[] col)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        emissiveColor[0] = col[0];
        emissiveColor[1] = col[1];
        emissiveColor[2] = col[2];

        if(col.length > 3)
            emissiveColor[3] = col[3];
        else
            emissiveColor[3] = 1;

        clearActiveList();
    }

    /**
     * Set the emissive color to the new value for the back face value.
     *
     * @param col The new colour to use
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data change callback method
     * @throws IllegalStateException Attempting to write to the value when the
     *   separate backface flag is not turned on.
     */
    public void setBackEmissiveColor(float[] col)
        throws InvalidWriteTimingException, IllegalStateException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(!separatedBackFace)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NO_BACKFACE_WRITE_PROP);
            throw new IllegalStateException(msg);
        }

        backEmissiveColor[0] = col[0];
        backEmissiveColor[1] = col[1];
        backEmissiveColor[2] = col[2];

        if(col.length > 3)
            backEmissiveColor[3] = col[3];
        else
            backEmissiveColor[3] = 1;

        clearActiveList();
    }

    /**
     * Get the current value of the emissive color for the front or combined
     * faces. The array should be at least length 3 or 4.
     *
     * @param col The array to copy the values into
     */
    public void getEmissiveColor(float[] col)
    {
        col[0] = emissiveColor[0];
        col[1] = emissiveColor[1];
        col[2] = emissiveColor[2];

        if(col.length > 3)
            col[3] = emissiveColor[3];
    }

    /**
     * Get the current value of the emissive color for the back face. The array
     * should be at least length 3 or 4.
     *
     * @param col The array to copy the values into
     * @throws IllegalStateException Attempting to read the value when the
     *   separate backface flag is not turned on.
     */
    public void getBackEmissiveColor(float[] col)
        throws IllegalStateException
    {
        if(!separatedBackFace)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NO_BACKFACE_READ_PROP);
            throw new IllegalStateException(msg);
        }

        col[0] = backEmissiveColor[0];
        col[1] = backEmissiveColor[1];
        col[2] = backEmissiveColor[2];

        if(col.length > 3)
            col[3] = backEmissiveColor[3];
    }

    /**
     * Set the ambient color to the new value for the front-face and combined
     * values.
     *
     * @param col The new colour to use
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void setAmbientColor(float[] col)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        ambientColor[0] = col[0];
        ambientColor[1] = col[1];
        ambientColor[2] = col[2];

        if(col.length > 3)
            this.ambientColor[3] = col[3];
        else
            this.ambientColor[3] = 1;

        clearActiveList();
    }

    /**
     * Set the ambient color to the new value for the back face value.
     *
     * @param col The new colour to use
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data change callback method
     * @throws IllegalStateException Attempting to write to the value when the
     *   separate backface flag is not turned on.
     */
    public void setBackAmbientColor(float[] col)
        throws InvalidWriteTimingException, IllegalStateException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(!separatedBackFace)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NO_BACKFACE_WRITE_PROP);
            throw new IllegalStateException(msg);
        }

        backAmbientColor[0] = col[0];
        backAmbientColor[1] = col[1];
        backAmbientColor[2] = col[2];

        if(col.length > 3)
            backAmbientColor[3] = col[3];
        else
            backAmbientColor[3] = 1;

        clearActiveList();
    }


    /**
     * Get the current value of the ambient color for the front or combined
     * faces. The array should be at least length 3 or 4.
     *
     * @param col The array to copy the values into.
     */
    public void getAmbientColor(float[] col)
    {
        col[0] = ambientColor[0];
        col[1] = ambientColor[1];
        col[2] = ambientColor[2];

        if(col.length > 3)
            col[3] = ambientColor[3];
    }

    /**
     * Get the current value of the ambient color for the back face. The array
     * should be at least length 3 or 4.
     *
     * @param col The array to copy the values into.
     * @throws IllegalStateException Attempting to read the value when the
     *   separate backface flag is not turned on.
     */
    public void getBackAmbientColor(float[] col)
        throws IllegalStateException
    {
        if(!separatedBackFace)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NO_BACKFACE_READ_PROP);
            throw new IllegalStateException(msg);
        }

        col[0] = backAmbientColor[0];
        col[1] = backAmbientColor[1];
        col[2] = backAmbientColor[2];

        if(col.length > 3)
            col[3] = backAmbientColor[3];
    }

    /**
     * Set the amount of transparency to be used for this material for the
     * front or combined faces. A value of 1 is fully opaque and 0 is totally
     * transparent.
     *
     * @param transparency A value between 0 and 1
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data change callback method
     */
    public void setTransparency(float transparency)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(transparency < 0 || transparency > 1)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(TRANSPARENCY_RANGE_PROP) +
                         transparency;
            throw new IllegalArgumentException(msg);
        }

        diffuseColor[3] = transparency;
        blendDiffuseAlpha = useSeparateDiffuseAlpha && (transparency != 0) &&
                           useColorMaterial &&
                          ((colorTarget == DIFFUSE_TARGET) ||
                           (colorTarget == AMBIENT_AND_DIFFUSE_TARGET));

        clearActiveList();
    }

    /**
     * Set the amount of transparency to be used for this material for the
     * back face value. A value of 1 is fully opaque and 0 is totally
     * transparent.
     *
     * @param transparency A value between 0 and 1
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data change callback method
     * @throws IllegalStateException Attempting to write to the value when the
     *   separate backface flag is not turned on.
     */
    public void setBackTransparency(float transparency)
        throws InvalidWriteTimingException, IllegalStateException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(!separatedBackFace)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NO_BACKFACE_WRITE_PROP);
            throw new IllegalStateException(msg);
        }

        if(transparency < 0 || transparency > 1)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(TRANSPARENCY_RANGE_PROP) +
                         transparency;
            throw new IllegalArgumentException(msg);
        }

        backDiffuseColor[3] = transparency;
        clearActiveList();
    }

    /**
     * Get the amount of transparency for the front or combined faces. A value
     * of 1 is fully opaque and 0 is totally transparent.
     * @return The current transparency value
     */
    public float getTransparency()
    {
        return diffuseColor[3];
    }

    /**
     * Get the amount of transparency for the back face. A value of 1 is fully
     * opaque and 0 is totally transparent.
     *
     * @return The current transparency value
     * @throws IllegalStateException Attempting to read the value when the
     *   separate backface flag is not turned on.
     */
    public float getBackTransparency()
        throws IllegalStateException
    {
        if(!separatedBackFace)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NO_BACKFACE_READ_PROP);
            throw new IllegalStateException(msg);
        }

        return backDiffuseColor[3];
    }

    /**
     * Set the flag that enables or disables lighting on any geometry using
     * this material.
     *
     * @param state true if lighting should be used
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void setLightingEnabled(boolean state)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(useLighting != state)
        {
            useLighting = state;
            clearActiveList();
        }
    }

    /**
     * Get the current flag defining whether lighting is currently enabled or
     * not for this object.
     *
     * @return true if lighting is currently enabled for this material
     */
    public boolean isLightingEnabled()
    {
        return useLighting;
    }

    /**
     * Set the flag that enables or disables separate transparency handling
     * from the color material values.
     *
     * @param state true if separating transparency from color material
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void setSeparateTransparencyEnabled(boolean state)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(useSeparateDiffuseAlpha != state)
        {
            useSeparateDiffuseAlpha = state;
            clearActiveList();
        }

        blendDiffuseAlpha = useSeparateDiffuseAlpha &&
                            (diffuseColor[3] != 0) &&
                            useColorMaterial &&
                            ((colorTarget == DIFFUSE_TARGET) ||
                             (colorTarget == AMBIENT_AND_DIFFUSE_TARGET));
    }

    /**
     * Get the current flag defining whether separate transparency and
     * color material lighting is currently enabled or not for this object.
     *
     * @return true if it currently enabled for this material
     */
    public boolean isSeparateTransparencyEnabled()
    {
        return useSeparateDiffuseAlpha;
    }

    /**
     * Set the flag that enables or disables color material lighting effects on
     * any geometry using this material.
     *
     * @param state true if color material should be used
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void setColorMaterialEnabled(boolean state)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(useColorMaterial != state)
        {
            useColorMaterial = state;
            clearActiveList();
        }

        blendDiffuseAlpha = useSeparateDiffuseAlpha && (diffuseColor[3] != 0) &&
                           useColorMaterial &&
                          ((colorTarget == DIFFUSE_TARGET) ||
                           (colorTarget == AMBIENT_AND_DIFFUSE_TARGET));
    }

    /**
     * Get the current flag defining whether color material lighting is
     * currently enabled or not for this object.
     *
     * @return true if it currently enabled for this material
     */
    public boolean isColorMaterialEnabled()
    {
        return useColorMaterial;
    }

    /**
     * Set the amount of colorTarget to be used for this material for the
     * front or combined faces.
     *
     * @param target One of the <code>_TARGET</code> constant values
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     * @throws IllegalArgumentException The target was not one of the
     *    <code>_TARGET</code> values
     */
    public void setColorMaterialTarget(int target)
        throws InvalidWriteTimingException, IllegalArgumentException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        switch(target)
        {
            case AMBIENT_TARGET:
            case DIFFUSE_TARGET:
            case EMISSIVE_TARGET:
            case SPECULAR_TARGET:
            case AMBIENT_AND_DIFFUSE_TARGET:
                break;

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(COLOR_MATERIAL_TARGET_PROP);
                throw new IllegalArgumentException(msg);
        }

        colorTarget = target;
        blendDiffuseAlpha = useSeparateDiffuseAlpha && (diffuseColor[3] != 0) &&
                           useColorMaterial &&
                          ((colorTarget == DIFFUSE_TARGET) ||
                           (colorTarget == AMBIENT_AND_DIFFUSE_TARGET));
        clearActiveList();
    }

    /**
     * Set the amount of colorTarget to be used for this material for the
     * back face value.
     *
     * @param target One of the <code>_TARGET</code> constant values
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     * @throws IllegalStateException Attempting to write to the value when the
     *   separate backface flag is not turned on.
     * @throws IllegalArgumentException The target was not one of the
     *    <code>_TARGET</code> values
     */
    public void setBackColorMaterialTarget(int target)
        throws InvalidWriteTimingException,
               IllegalStateException,
               IllegalArgumentException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(!separatedBackFace)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NO_BACKFACE_WRITE_PROP);
            throw new IllegalStateException(msg);
        }

        switch(target)
        {
            case AMBIENT_TARGET:
            case DIFFUSE_TARGET:
            case EMISSIVE_TARGET:
            case SPECULAR_TARGET:
            case AMBIENT_AND_DIFFUSE_TARGET:
                break;

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(COLOR_MATERIAL_TARGET_PROP);
                throw new IllegalArgumentException(msg);
        }

        backColorTarget = target;
        clearActiveList();
    }

    /**
     * Get the current target fr color materials for the front or combined faces.
     *
     * @return One of the <code>_TARGET</code> values for the front face
     */
    public int getColorMaterialTarget()
    {
        return colorTarget;
    }

    /**
     * Get the amount of colorTarget for the back face.
     *
     * @return One of the <code>_TARGET</code> values for the back face
     * @throws IllegalStateException Attempting to read the value when the
     *   separate backface flag is not turned on.
     */
    public int getBackColorMaterialTarget()
        throws IllegalStateException
    {
        if(!separatedBackFace)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NO_BACKFACE_READ_PROP);
            throw new IllegalStateException(msg);
        }

        return backColorTarget;
    }


    /**
     * Set the flag that enables or disables separate material lighting values
     * on any geometry using this material. Whether these values are actually
     * used or not is dependent on the accompanying * {@link PolygonAttributes}
     * class.
     * <p>
     * If this is enabling the separate back face values, all the existing
     * front faces are copied into the back face value.
     *
     * @param state true if two sided colours should be used
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void setSeparateBackfaceEnabled(boolean state)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(separatedBackFace != state)
        {
            separatedBackFace = state;
            clearActiveList();

            // Do we need to go through and allocate everything?
            if(separatedBackFace)
            {
                if(backDiffuseColor == null)
                    backDiffuseColor = new float[4];
                backDiffuseColor[0] = diffuseColor[0];
                backDiffuseColor[1] = diffuseColor[1];
                backDiffuseColor[2] = diffuseColor[2];
                backDiffuseColor[3] = diffuseColor[3];

                if(backAmbientColor == null)
                    backAmbientColor = new float[4];
                backAmbientColor[0] = ambientColor[0];
                backAmbientColor[1] = ambientColor[1];
                backAmbientColor[2] = ambientColor[2];
                backAmbientColor[3] = ambientColor[3];

                if(backSpecularColor == null)
                    backSpecularColor = new float[4];
                backSpecularColor[0] = specularColor[0];
                backSpecularColor[1] = specularColor[1];
                backSpecularColor[2] = specularColor[2];
                backSpecularColor[3] = specularColor[3];

                if(backEmissiveColor == null)
                    backEmissiveColor = new float[4];
                backEmissiveColor[0] = emissiveColor[0];
                backEmissiveColor[1] = emissiveColor[1];
                backEmissiveColor[2] = emissiveColor[2];
                backEmissiveColor[3] = emissiveColor[3];

                backShininess = shininess;
            }
        }
    }

    /**
     * Get the current state of whether separate back-face colour values are in
     * use.
     *
     * @return true if separate back material values are currently enabled for
     *   this material
     */
    public boolean isSeparateBackfaceEnabled()
    {
        return separatedBackFace;
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param mat The material to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(Material mat)
    {
        if(mat == this)
            return 0;

        if(mat == null)
            return 1;

        // check the material properties. Transparency is ignored
        // because we assume that the check before this has weeded
        // out the transparent from non-transparent for separate
        // sorting policies.
        if(useLighting != mat.useLighting)
            return !useLighting ? -1 : 1;

        if(separatedBackFace != mat.separatedBackFace)
            return !separatedBackFace ? -1 : 1;

        if(shininess != mat.shininess)
            return shininess > mat.shininess ? 1 : -1;

        int res = compareColor4(ambientColor, mat.ambientColor);
        if(res != 0)
            return res;

        res = compareColor4(diffuseColor, mat.diffuseColor);
        if(res != 0)
            return res;

        res = compareColor4(emissiveColor, mat.emissiveColor);
        if(res != 0)
            return res;

        res = compareColor4(specularColor, mat.specularColor);
        if(res != 0)
            return res;

        if(useColorMaterial != mat.useColorMaterial)
            return !useColorMaterial ? -1 : 1;

        // Only do these next sets of compares if the values are valid. If they
        // aren't, then there is no effect over the rendering pipeline output, so
        // we can just ignore it.
        if(useColorMaterial)
        {
            if(colorTarget != mat.colorTarget)
                return colorTarget > mat.colorTarget ? 1 : -1;

            if(separatedBackFace)
            {
                if(backColorTarget != mat.backColorTarget)
                    return backColorTarget > mat.backColorTarget ? 1 : -1;
            }
        }

        if(separatedBackFace)
        {
            if(backShininess != mat.backShininess)
                return backShininess > mat.backShininess ? 1 : -1;

            res = compareColor4(backAmbientColor, mat.backAmbientColor);
            if(res != 0)
                return res;

            res = compareColor4(backDiffuseColor, mat.backDiffuseColor);
            if(res != 0)
                return res;

            res = compareColor4(backEmissiveColor, mat.backEmissiveColor);
            if(res != 0)
                return res;

            res = compareColor4(backSpecularColor, mat.backSpecularColor);
            if(res != 0)
                return res;
        }

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param mat The material to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(Material mat)
    {
        if(mat == this)
            return true;

        if((mat == null) ||
           (useLighting != mat.useLighting) ||
           (shininess != mat.shininess) ||
           (separatedBackFace != mat.separatedBackFace) ||
           (useColorMaterial != mat.useColorMaterial) ||
           !equalsColor4(ambientColor, mat.ambientColor) ||
           !equalsColor4(diffuseColor, mat.diffuseColor) ||
           !equalsColor4(emissiveColor, mat.emissiveColor) ||
           !equalsColor4(specularColor, mat.specularColor))
            return false;

        if(useColorMaterial &&
           (colorTarget != mat.colorTarget) ||
           (separatedBackFace && (backColorTarget != mat.backColorTarget)))
            return false;

        if(separatedBackFace &&
           ((backShininess != mat.backShininess) ||
            !equalsColor4(backAmbientColor, mat.backAmbientColor) ||
            !equalsColor4(backDiffuseColor, mat.backDiffuseColor) ||
            !equalsColor4(backEmissiveColor, mat.backEmissiveColor) ||
            !equalsColor4(backSpecularColor, mat.backSpecularColor)))
           return false;

        return true;
    }

    /**
     * Check to see if the code has detected the lack of the imaging subset.
     * This call will always return false until the first time an instance of
     * this class has been rendered. After that time, the real answer is known.
     *
     * @return true if the blending operations are allowed
     */
    public boolean isBlendingAvailable()
    {
        return hasImaging;
    }

    /**
     * Shift all the values from the current display list across to the deleted
     * list.
     */
    private void clearActiveList()
    {
        if(displayListMap.size() == 0)
            return;

        deletedDisplayListMap.putAll(displayListMap);
        displayListMap.clear();
    }

    /**
     * Compare 2 color arrays of length 3 for equality
     *
     * @param a The first colour array to check
     * @param b The first colour array to check
     * @return -1 if a[i] < b[i], +1 if a[i] > b[i], otherwise 0
     */
    private int compareColor4(float[] a, float[] b)
    {
        if(a[0] < b[0])
            return -1;
        else if (a[0] > b[0])
            return 1;

        if(a[1] < b[1])
            return -1;
        else if (a[1] > b[1])
            return 1;

        if(a[2] < b[2])
            return -1;
        else if (a[2] > b[2])
            return 1;

        if(a[3] < b[3])
            return -1;
        else if (a[3] > b[3])
            return 1;

        return 0;
    }

    /**
     * Compare 2 color arrays of length 4 for equality
     *
     * @param a The first colour array to check
     * @param b The first colour array to check
     * @return true if they have the same values, false otherwise
     */
    private boolean equalsColor4(float[] a, float[] b)
    {
        return (a[0] == b[0]) &&
               (a[1] == b[1]) &&
               (a[2] == b[2]) &&
               (a[3] == b[3]);
    }
}
