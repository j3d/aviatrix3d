/*****************************************************************************
 *                   Yumetech, Inc Copyright (c) 2004 - 2006
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

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.DeletableRenderable;
import org.j3d.aviatrix3d.rendering.ObjectRenderable;

/**
 * Describes a texture's automatic texture coordinate generation properties
 * per axis.
 * <p>
 *
 * This class allows texture coordinates to be specified for each axis of
 * an object separately. Only one instance of this class is needed per object
 * as all axes can be specified.
 * <p>
 *
 * Texture modes here directly correspond to the OpenGL constants of the same
 * type. Either are acceptable as parameters. All parameters can be set using
 * the {@link #setParameter(int,int,int,float[])} method. This takes 4
 * parameters, some of which are likely not to be used.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>hasParentMsg: Error message when the (internal) caller tries to
 *     call setParent() when this class already has a parent.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.29 $
 */
public class TexCoordGeneration extends NodeComponent
    implements ObjectRenderable, DeletableRenderable
{
    /** Message when an invalid coordinate is given */
    private static final String INVALID_COORD_PROP =
        "org.j3d.aviatrix3d.TexCoordGeneration.invalidCoordMsg";

    /** Message when the generation mode is invalid */
    private static final String INVALID_MODE_PROP =
        "org.j3d.aviatrix3d.TexCoordGeneration.invalidModeMsg";

    /** Message when the mode parameter type is invalid */
    private static final String INVALID_PARAM_PROP =
        "org.j3d.aviatrix3d.TexCoordGeneration.invalidParamMsg";

    /** Empty float array for passing parameters to the system */
    private static final float[] EMPTY_FLOAT_4 = {0, 0, 0, 0};

    /** Generate coordinates for a texture's S coordinate */
    public static final int TEXTURE_S = GL.GL_S;

    /** Generate coordinates for a texture's T coordinate  */
    public static final int TEXTURE_T = GL.GL_T;

    /** Generate coordinates for a texture's R coordinate */
    public static final int TEXTURE_R = GL.GL_R;

    /** Generate coordinates for a texture's Q coordinate  */
    public static final int TEXTURE_Q = GL.GL_Q;

    /**
     * Coordinate reference plane is user defined. Additional information
     * in the form of extra parameters (The MAP_* values) will need to be
     * provided.
     */
    public static final int MODE_GENERIC = GL.GL_TEXTURE_GEN_MODE;

    /**
     * Generate coordinates for a reference plane that is relative to the
     * object for the given axis. No value needs to be specified for the param
     * or value arguments.
     */
    public static final int MODE_OBJECT_PLANE = GL.GL_OBJECT_PLANE;

    /**
     * Generate coordinates for a reference plane that is relative to the
     * user's eye position for the given axis. No value needs to be specified
     * for the param or value arguments.
     */
    public static final int MODE_EYE_PLANE = GL.GL_EYE_PLANE;

    /**
     * When the mode is set to MODE_GENERIC, generate coordinates for the
     * given coordinate relative to a plane specified in the object's
     * coordinate system.
     */
    public static final int MAP_OBJECT_LINEAR = GL.GL_OBJECT_LINEAR;

    /**
     * When the mode is set to MODE_GENERIC, generate coordinates for the
     * given coordinate relative to a plane specified in the user's eye
     * position coordinate system.
     */
    public static final int MAP_EYE_LINEAR = GL.GL_EYE_LINEAR;

    /**
     * When the mode is set to MODE_GENERIC, generate coordinates for the
     * given axis in a spherical shape for env mapping.
     */
    public static final int MAP_SPHERICAL = GL.GL_SPHERE_MAP;

    /**
     * When the mode is set to MODE_GENERIC, generate coordinates using the
     * normals at the vertex. Used mostly in cubic environment mapping.
     */
    public static final int MAP_NORMALS = GL.GL_NORMAL_MAP;

    /**
     * When the mode is set to MODE_GENERIC, generate coordinates using the
     * normals at the vertex. Used mostly in cubic environment mapping.
     */
    public static final int MAP_REFLECTIONS = GL.GL_REFLECTION_MAP;

    /** Modes that are needed to be passed to the system for tex gen */
    private static final int[] ENABLE_MODE =
    {
        GL.GL_TEXTURE_GEN_S,
        GL.GL_TEXTURE_GEN_T,
        GL.GL_TEXTURE_GEN_R,
        GL.GL_TEXTURE_GEN_Q,
    };

    /** Transformation of matrix which is applied to the auto-generated texture coordinates */
    private float[] transformMatrix;

    /** If "transformMatrix" is an identity matrix this value is set to true */
    private boolean isIdentity;

    /** Parameters used when TEXTURE_GEN or OBJECT_LINEAR used */
    private float[][] parameters;

    /** The texture coordiante effected */
    private int[] coordinate;

    /** The parameter mode use */
    private int[] modes;

    /** The mapping type to use */
    private int[] mapping;

    /** The texture generation parametes */
    private int[] texgenparam;

    /** A mapping between glContext and displayListID(Integer) */
    private HashMap<GL, Integer> displayListMap;

    /** A mapping for displaylists that have been deleted */
    private HashMap<GL, Integer> deletedDisplayListMap;

    /**
     * Constructs a TexCoordGeneration with default values, which is
     * to say, do nothing.
     */
    public TexCoordGeneration()
    {
        parameters = new float[4][];
        coordinate = new int[4];
        modes = new int[4];
        mapping = new int[4];

        texgenparam = new int[4];

        transformMatrix = new float[16];
        transformMatrix[0] = 1.0f;
        transformMatrix[1] = 0.0f;
        transformMatrix[2] = 0.0f;
        transformMatrix[3] = 0.0f;
        transformMatrix[4] = 0.0f;
        transformMatrix[5] = 1.0f;
        transformMatrix[6] = 0.0f;
        transformMatrix[7] = 0.0f;
        transformMatrix[8] = 0.0f;
        transformMatrix[9] = 0.0f;
        transformMatrix[10] = 1.0f;
        transformMatrix[11] = 0.0f;
        transformMatrix[12] = 0.0f;
        transformMatrix[13] = 0.0f;
        transformMatrix[14] = 0.0f;
        transformMatrix[15] = 1.0f;

        isIdentity = true;

        texgenparam[0] = MODE_GENERIC;
        texgenparam[1] = MODE_GENERIC;
        texgenparam[2] = MODE_GENERIC;
        texgenparam[3] = MODE_GENERIC;

        displayListMap = new HashMap<GL, Integer>();
        deletedDisplayListMap = new HashMap<GL, Integer>();
    }

    /**
     * Create automatic coordinate generation for one axis with
     * the given set of abilities.
     *
     * @throws IllegalArgumentException Invalid axis, mode or parameter
     */
    public TexCoordGeneration(int axis, int mode, int parameter, float[] value)
    {
        this();

        setParameter(axis, mode, parameter, value);
    }

    //---------------------------------------------------------------
    // Methods defined by ObjectRenderable
    //---------------------------------------------------------------

    /**
     * Issue ogl commands needed for this component
     *
     * @param gl The gl context to draw with
     */
    public void render(GL gl)
    {
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

            gl.glNewList(listName.intValue(), GL.GL_COMPILE);

            gl.glPushMatrix();

            if(!isIdentity)
                gl.glMultMatrixf(transformMatrix, 0);

            //  State enable first
            for(int i = 0; i < 4; i++)
            {
                if(coordinate[i] == 0)
                    continue;

                gl.glTexGeni(coordinate[i], modes[i], mapping[i]);

                if(parameters[i] != null)
                    gl.glTexGenfv(coordinate[i],
                                  texgenparam[i],
                                  parameters[i],
                                  0);
                else
                    gl.glTexGenfv(coordinate[i],
                                  texgenparam[i],
                                  EMPTY_FLOAT_4, 0);

                gl.glEnable(ENABLE_MODE[i]);
            }

            gl.glPopMatrix();

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
    public void postRender(GL gl)
    {
        for(int i = 0; i < 4; i++)
        {
            if(coordinate[i] == 0)
                continue;

            gl.glDisable(ENABLE_MODE[i]);
        }
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
    public int compareTo(Object o)
        throws ClassCastException
    {
        TexCoordGeneration tcg = (TexCoordGeneration)o;
        return compareTo(tcg);
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
    public boolean equals(Object o)
    {
        if(!(o instanceof TexCoordGeneration))
            return false;
        else
            return equals((TexCoordGeneration)o);
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
    public void cleanup(GL gl)
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
     * Determines if the special transformation matrix an identity matrix
     * or not.
     *
     * @return True if identity or else returns false.
     */
    private boolean isTransformIdentity()
    {

        if(transformMatrix[0] == 1.0f &&
           transformMatrix[1] == 0.0f &&
           transformMatrix[2] == 0.0f &&
           transformMatrix[3] == 0.0f &&
           transformMatrix[4] == 0.0f &&
           transformMatrix[5] == 1.0f &&
           transformMatrix[6] == 0.0f &&
           transformMatrix[7] == 0.0f &&
           transformMatrix[8] == 0.0f &&
           transformMatrix[9] == 0.0f &&
           transformMatrix[10] == 1.0f &&
           transformMatrix[11] == 0.0f &&
           transformMatrix[12] == 0.0f &&
           transformMatrix[13] == 0.0f &&
           transformMatrix[14] == 0.0f &&
           transformMatrix[15] == 1.0f)
            return true;

        return false;
    }

    /**
     * Apply transformation to the texture generation
     *
     * @param transformMtx 4 by 4 matrix that transforms
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void applyTransform(float[] transformMtx)
        throws InvalidWriteTimingException
    {

        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        transformMatrix[0] = transformMtx[0];
        transformMatrix[1] = transformMtx[1];
        transformMatrix[2] = transformMtx[2];
        transformMatrix[3] = transformMtx[3];
        transformMatrix[4] = transformMtx[4];
        transformMatrix[5] = transformMtx[5];
        transformMatrix[6] = transformMtx[6];
        transformMatrix[7] = transformMtx[7];
        transformMatrix[8] = transformMtx[8];
        transformMatrix[9] = transformMtx[9];
        transformMatrix[10] = transformMtx[10];
        transformMatrix[11] = transformMtx[11];
        transformMatrix[12] = transformMtx[12];
        transformMatrix[13] = transformMtx[13];
        transformMatrix[14] = transformMtx[14];
        transformMatrix[15] = transformMtx[15];

        isIdentity = isTransformIdentity();
    }

    /**
     * Fetch the currently set mode value for the requested axis.
     *
     * @param axis One of the TEXTURE_x values
     * @return The current mode value (one of MODE_x)
     */
    public int getMode(int axis)
    {
        int ret_val = 0;

        switch(axis)
        {
            case TEXTURE_S:
                ret_val = modes[0];
                break;

            case TEXTURE_T:
                ret_val = modes[1];
                break;

            case TEXTURE_R:
                ret_val = modes[2];
                break;

            case TEXTURE_Q:
                ret_val = modes[3];
                break;

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(INVALID_COORD_PROP);
                throw new IllegalArgumentException(msg);
        }

        return ret_val;
    }

    /**
     * Clear the parameter settings for a specific axis. This will disable coordinate
     * generation on this axis.
     *
     * @param axis One of the TEXTURE_x values
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void clearParameter(int axis)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        switch(axis)
        {
            case TEXTURE_S:
                clearActiveList();
                coordinate[0] = 0;
                break;

            case TEXTURE_T:
                clearActiveList();
                coordinate[1] = 0;
                break;

            case TEXTURE_R:
                clearActiveList();
                coordinate[2] = 0;
                break;

            case TEXTURE_Q:
                clearActiveList();
                coordinate[3] = 0;
                break;

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(INVALID_COORD_PROP);
                throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Setup one of the axis parameters
     * @param axis One of the TEXTURE_x values
     * @param mode One of the MODE_x values
     * @param parameter One of the MAP_x values when the mode is set
     *    to MODE_GENERIC, otherwise ignored
     * @param texgeparam The symbolic name of the texture coordinate generation function.
           One of MODE_GENERIC, MODE_OBJECT_PLANE, or MODE_EYE_PLANE.
     * @param value Optional values, dependent on the parameter type
     * @throws IllegalArgumentException The either the mode or parameter is invalid
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setParameter(int axis,
                             int mode,
                             int parameter,
                             int texgeparam,
                             float[] value)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        checkMode(mode);
        checkTexGenFunc(texgeparam);

        if(mode == MODE_GENERIC)
            checkParameter(parameter);

        switch(axis)
        {
            case TEXTURE_S:
                coordinate[0] = TEXTURE_S;
                modes[0] = mode;
                mapping[0] = parameter;
                texgenparam[0] = texgeparam;
                if(value != null)
                {
                    if(parameters[0] == null)
                        parameters[0] = new float[4];

                    parameters[0][0] = value[0];
                    parameters[0][1] = value[1];
                    parameters[0][2] = value[2];
                    parameters[0][3] = value[3];
                }

                clearActiveList();
                break;

            case TEXTURE_T:
                coordinate[1] = TEXTURE_T;

                modes[1] = mode;
                mapping[1] = parameter;
                texgenparam[1] = texgeparam;
                if(value != null)
                {
                    if(parameters[1] == null)
                        parameters[1] = new float[4];

                    parameters[1][0] = value[0];
                    parameters[1][1] = value[1];
                    parameters[1][2] = value[2];
                    parameters[1][3] = value[3];
                }
                clearActiveList();
                break;

            case TEXTURE_R:
                coordinate[2] = TEXTURE_R;
                modes[2] = mode;
                mapping[2] = parameter;
                texgenparam[2] = texgeparam;
                if(value != null)
                {
                    if(parameters[2] == null)
                        parameters[2] = new float[4];

                    parameters[2][0] = value[0];
                    parameters[2][1] = value[1];
                    parameters[2][2] = value[2];
                    parameters[2][3] = value[3];
                }
                clearActiveList();
                break;

            case TEXTURE_Q:
                coordinate[3] = TEXTURE_Q;
                modes[3] = mode;
                mapping[3] = parameter;
                texgenparam[3] = texgeparam;
                if(value != null)
                {
                    if(parameters[3] == null)
                        parameters[3] = new float[4];

                    parameters[3][0] = value[0];
                    parameters[3][1] = value[1];
                    parameters[3][2] = value[2];
                    parameters[3][3] = value[3];
                }
                clearActiveList();
                break;

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(INVALID_COORD_PROP);
                throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Setup one of the axis parameters.
     *
     * @param axis One of the TEXTURE_x values
     * @param mode One of the MODE_x values
     * @param parameter One of the MAP_x values when the mode is set
     *    to MODE_GENERIC, otherwise ignored
     * @param value Optional values, dependent on the parameter type
     * @throws IllegalArgumentException The either the mode or parameter is invalid
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setParameter(int axis, int mode, int parameter, float[] value)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        checkMode(mode);

        if(mode == MODE_GENERIC)
            checkParameter(parameter);

        switch(axis)
        {
            case TEXTURE_S:
                coordinate[0] = TEXTURE_S;
                modes[0] = mode;
                mapping[0] = parameter;

                if(value != null)
                {
                    if(parameters[0] == null)
                        parameters[0] = new float[4];

                    parameters[0][0] = value[0];
                    parameters[0][1] = value[1];
                    parameters[0][2] = value[2];
                    parameters[0][3] = value[3];
                }

                clearActiveList();
                break;

            case TEXTURE_T:
                coordinate[1] = TEXTURE_T;

                modes[1] = mode;
                mapping[1] = parameter;
                if(value != null)
                {
                    if(parameters[1] == null)
                        parameters[1] = new float[4];

                    parameters[1][0] = value[0];
                    parameters[1][1] = value[1];
                    parameters[1][2] = value[2];
                    parameters[1][3] = value[3];
                }
                clearActiveList();
                break;

            case TEXTURE_R:
                coordinate[2] = TEXTURE_R;
                modes[2] = mode;
                mapping[2] = parameter;
                if(value != null)
                {
                    if(parameters[2] == null)
                        parameters[2] = new float[4];

                    parameters[2][0] = value[0];
                    parameters[2][1] = value[1];
                    parameters[2][2] = value[2];
                    parameters[2][3] = value[3];
                }
                clearActiveList();
                break;

            case TEXTURE_Q:
                coordinate[3] = TEXTURE_Q;
                modes[3] = mode;
                mapping[3] = parameter;
                if(value != null)
                {
                    if(parameters[3] == null)
                        parameters[3] = new float[4];

                    parameters[3][0] = value[0];
                    parameters[3][1] = value[1];
                    parameters[3][2] = value[2];
                    parameters[3][3] = value[3];
                }
                clearActiveList();
                break;

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(INVALID_COORD_PROP);
                throw new AlreadyParentedException(msg);
        }
    }


    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param tcg The generator instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(TexCoordGeneration tcg)
    {
        if(tcg == null)
            return 1;

        if(tcg == this)
            return 0;

        for(int i = 0; i < 4; i++)
        {
            if(coordinate[i] != tcg.coordinate[i])
                return coordinate[i] < tcg.coordinate[i] ? -1 : 1;
            else if(coordinate[i] != 0)
            {
                if(mapping[i] != tcg.mapping[i])
                    return mapping[i] < tcg.mapping[i] ? -1 : 1;

                if(texgenparam[i] != tcg.texgenparam[i])
                    return texgenparam[i] < tcg.texgenparam[i] ? -1 : 1;

                if(parameters[i] != tcg.parameters[i])
                {
                    if(parameters[i] == null)
                        return -1;
                    else if(tcg.parameters[i] == null)
                        return 1;

                    if(parameters[i][0] != tcg.parameters[i][0])
                        return parameters[i][0] < tcg.parameters[i][0] ? -1 : 1;

                    if(parameters[i][1] != tcg.parameters[i][1])
                        return parameters[i][1] < tcg.parameters[i][1] ? -1 : 1;

                    if(parameters[i][2] != tcg.parameters[i][2])
                        return parameters[i][2] < tcg.parameters[i][2] ? -1 : 1;

                    if(parameters[i][3] != tcg.parameters[i][3])
                        return parameters[i][3] < tcg.parameters[i][3] ? -1 : 1;
                }

                if(modes[i] != tcg.modes[i])
                    return modes[i] < tcg.modes[i] ? -1 : 1;
            }
        }

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param tcg The texture unit instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(TexCoordGeneration tcg)
    {
        if(tcg == this)
            return true;

        if(tcg == null)
            return false;

        for(int i = 0; i < 4; i++)
        {
            if(parameters[i] == null && tcg.parameters[i] == null)
				continue;

            if((parameters[i] == null && tcg.parameters[i] != null) ||
                parameters[i] != null && tcg.parameters[i] == null)
                return false;

			// This is almost always 4, but can't guarantee that, so do
			// the generic loop length thing.
			for(int j = 0; j < parameters.length; j++)
				if(parameters[i][j] != tcg.parameters[i][j])
					return false;
        }

        for(int i = 0; i < 4; i++)
        {
            if(coordinate[i] != tcg.coordinate[i])
                return false;
        }

        for(int i = 0; i < 4; i++)
        {
            if(modes[i] != tcg.modes[i])
                return false;
        }

        for(int i = 0; i < 4; i++)
        {
            if(mapping[i] != tcg.mapping[i])
                return false;
        }

        for(int i = 0; i < 4; i++)
        {
            if(texgenparam[i] != tcg.texgenparam[i])
                return false;
        }

        for(int i = 0; i < 16; i++)
        {
            if(transformMatrix[i] != tcg.transformMatrix[i])
                return false;
        }

        return true;
    }

    /**
     * Check the validity of the texture generation parameter value.
     * If invalid, throw the exception.
     *
     * @param texgenparam A single valued texture generation parameter,
     *     one of MAP_OBJECT_LINEAR, MAP_EYE_LINEAR, or MAP_SPHERICAL.
     * @author Sang Park
     */
    private void checkTexGenFunc(int texgenparam)
        throws IllegalArgumentException
    {
        switch(texgenparam) {
            case MODE_GENERIC:
            case MODE_OBJECT_PLANE:
            case MODE_EYE_PLANE:
                break;

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(INVALID_MODE_PROP);
                throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Check the validity of the mode argument. If invalid, throw the exception.
     *
     * @param mode One of the MODE_x values
     * @throws IllegalArgumentException Invalid mode type specified
     */
    private void checkMode(int mode)
        throws IllegalArgumentException
    {
        switch(mode)
        {
            case MODE_GENERIC:
            case MODE_OBJECT_PLANE:
            case MODE_EYE_PLANE:
                break;

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(INVALID_MODE_PROP);
                throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Check the parameter value for validity. Assumes that it has been checked for
     * MODE_GENERIC first.
     *
     * @param parameter One of the MAP_x values when the mode is set
     *    to MODE_GENERIC, otherwise ignored
     * @throws IllegalArgumentException Invalid mode type specified
     */
    private void checkParameter(int parameter)
        throws IllegalArgumentException
    {
        switch(parameter)
        {
            case MAP_OBJECT_LINEAR:
            case MAP_EYE_LINEAR:
            case MAP_SPHERICAL:
            case MAP_NORMALS:
            case MAP_REFLECTIONS:
                break;

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(INVALID_PARAM_PROP);
                throw new IllegalArgumentException(msg);
        }
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
}
