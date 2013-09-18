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
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.HashMap;

import javax.media.opengl.GL2;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.ComponentRenderable;

/**
 * Representation of a set of arguments (uniform variables) that can be passed
 * to a shader program.
 * <p>
 *
 * Arguments are separate from the program so that the same program can be used
 * amongst a number of primitives, but with different setup information if
 * needed.
 * <p>
 *
 * If changing the value of an existing argument, no consistency checks are
 * made on whether the old type and new type match.
 * <p>
 *
 * The external data passed to the ComponentRenderable calls shall be an
 * <code>Integer</code> instance that represents the GL program identifier
 * generated for the shader program this instance is working with.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>invalidDataSizeMsg: Error message when the user tries to set a uniform
 *     data size that is not 1, 2, 3 or 4.</li>
 * <li>invalidMatrixSizeMsg: Error message when the user tries to set a uniform
 *     matrix size that is not 2, 3 or 4.</li>
 * <li>invalidCountMsg: Error message when the user tries to set a uniform
 *     matrix size that is not 2, 3 or 4.</li>
 * <li>notIntMsg: Error message when trying set a float to an int type</li>
 * <li>notFloatMsg: Error message when trying set an int to an float type</li>
 * <li>invalidUniformMsg: Error message when the user requests a uniform</li>
 *     can't be located in the underlying shader.
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.19 $
 */
public class ShaderArguments extends NodeComponent
    implements ComponentRenderable
{
    /** User-provided uniform can't be located in underlying shader */
    private static final String UNKNOWN_UNIFORM_PROP =
        "org.j3d.aviatrix3d.ShaderArguments.invalidUniformMsg";

    /** Message for when the uniform data size is not 1, 2, 3 or 4 */
    private static final String INVALID_DATA_SIZE_PROP =
        "org.j3d.aviatrix3d.ShaderArguments.invalidDataSizeMsg";

    /** Message for when the uniform matrix size is not 2, 3 or 4 */
    private static final String INVALID_MATRIX_SIZE_PROP =
        "org.j3d.aviatrix3d.ShaderArguments.invalidMatrixSizeMsg";

    /** Message for when the uniform count is less than 1 */
    private static final String INVALID_COUNT_PROP =
        "org.j3d.aviatrix3d.ShaderArguments.invalidCountMsg";

    /**
     * Message for when the uniform data type is not an int[] and the int[]
     * getter method has been called.
     */
    private static final String NOT_INT_TYPE_PROP =
        "org.j3d.aviatrix3d.ShaderArguments.notIntMsg";

    /**
     * Message for when the uniform data type is not an float[] and the float[]
     * getter method has been called.
     */
    private static final String NOT_FLOAT_TYPE_PROP =
        "org.j3d.aviatrix3d.ShaderArguments.notFloatMsg";

    /** The uniform data type is an int array */
    public static final int INT_UNIFORM_TYPE = ShaderArgumentValue.INT_ARRAY;

    /** The uniform data type is a float array */
    public static final int FLOAT_UNIFORM_TYPE = ShaderArgumentValue.FLOAT_ARRAY;

    /** The uniform data type is a matrix array */
    public static final int MATRIX_UNIFORM_TYPE = ShaderArgumentValue.MATRIX;

    /** The uniform data type is an int array */
    public static final int SAMPLER_UNIFORM_TYPE = ShaderArgumentValue.SAMPLER;


    /** Mapping of uniform var names to their current values */
    private HashMap<String, ShaderArgumentValue> values;

    /** Ignored var names for those that we have tried to fetch and failed */
    private HashSet ignoredNames;

    /** List of the values existing */
    private String[] varNames;

    /** Number of currently valid names */
    private int numVarNames;

    /**
     * Constructs a Shader with default values.
     */
    public ShaderArguments()
    {
        values = new HashMap<String, ShaderArgumentValue>();
        ignoredNames = new HashSet();
        varNames = new String[32];
        numVarNames = 0;
    }

    //---------------------------------------------------------------
    // Methods defined by ComponentRenderable
    //---------------------------------------------------------------

    /**
     * Assign the uniform values to the shader now.
     *
     * @param gl The GL context to write the values through
     * @param programId the ID of the program to make GL calls with
     */
    @Override
    public void render(GL2 gl, Object programId)
    {
        for(int i = 0; i < numVarNames; i++)
        {
            if(ignoredNames.contains(varNames[i]))
                continue;

            ShaderArgumentValue val = values.get(varNames[i]);

            if((val.uniformLocation == -1) && !ignoredNames.contains(varNames[i]))
            {
                val.uniformLocation =
                    gl.glGetUniformLocationARB(((Integer)programId).intValue(),
                                               varNames[i]);

                // Still not valid? Ignore it then.
                // TODO: Maybe we want to issue an error message here
                if(val.uniformLocation == -1)
                {
                    I18nManager intl_mgr = I18nManager.getManager();
                    Locale lcl = intl_mgr.getFoundLocale();
                    String msg_pattern = intl_mgr.getString(UNKNOWN_UNIFORM_PROP);

                    Object[] msg_args = { varNames[i] };
                    MessageFormat msg_fmt =
                        new MessageFormat(msg_pattern, lcl);
                    String msg = msg_fmt.format(msg_args);

                    //errorReporter.errorReport(msg, e);
                    System.out.println(msg);

                    ignoredNames.add(varNames[i]);
                    continue;
                }
            }

            switch(val.dataType)
            {
                case ShaderArgumentValue.INT_ARRAY:
                    switch(val.size)
                    {
                        case 1:
                            gl.glUniform1ivARB(val.uniformLocation,
                                               val.count,
                                               val.intData,
                                               0);
                            break;

                        case 2:
                            gl.glUniform2ivARB(val.uniformLocation,
                                               val.count,
                                               val.intData,
                                               0);
                            break;

                        case 3:
                            gl.glUniform3ivARB(val.uniformLocation,
                                               val.count,
                                               val.intData,
                                               0);
                            break;

                        case 4:
                            gl.glUniform4ivARB(val.uniformLocation,
                                               val.count,
                                               val.intData,
                                               0);
                            break;
                    }
                    break;

                case ShaderArgumentValue.FLOAT_ARRAY:
                    switch(val.size)
                    {
                        case 1:
                            gl.glUniform1fvARB(val.uniformLocation,
                                               val.count,
                                               val.floatData,
                                               0);
                            break;

                        case 2:
                            gl.glUniform2fvARB(val.uniformLocation,
                                               val.count,
                                               val.floatData,
                                               0);
                            break;

                        case 3:
                            gl.glUniform3fvARB(val.uniformLocation,
                                               val.count,
                                               val.floatData,
                                               0);
                            break;

                        case 4:
                            gl.glUniform4fvARB(val.uniformLocation,
                                               val.count,
                                               val.floatData,
                                               0);
                            break;
                    }
                    break;

                case ShaderArgumentValue.MATRIX:
                    switch(val.size)
                    {
                        case 2:
                            gl.glUniformMatrix2fvARB(val.uniformLocation,
                                                     val.count,
                                                     val.transposeMatrix,
                                                     val.floatData,
                                                     0);
                            break;

                        case 3:
                            gl.glUniformMatrix3fvARB(val.uniformLocation,
                                                     val.count,
                                                     val.transposeMatrix,
                                                     val.floatData,
                                                     0);
                            break;

                        case 4:
                            gl.glUniformMatrix4fvARB(val.uniformLocation,
                                                     val.count,
                                                     val.transposeMatrix,
                                                     val.floatData,
                                                     0);
                            break;
                    }
                    break;

                case ShaderArgumentValue.SAMPLER:
                    gl.glUniform1iARB(val.uniformLocation, val.intData[0]);
                    break;
            }
        }
    }

    /*
     * Overloaded form of the postRender() method to render the light details given
     * the specific Light ID used by OpenGL. Since the active light ID for this
     * node may vary over time, a fixed ID cannot be used by OpenGL. The
     * renderer will always call this method rather than the normal postRender()
     * method. The normal post render will still be called
     *
     * @param gl The GL context to render with
     * @param programId the ID of the program to make GL calls with
     */
    @Override
    public void postRender(GL2 gl, Object programId)
    {
        // do nothing
    }

    //---------------------------------------------------------------
    // Methods defined by Comparable
    //---------------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param o The objec to be compared
     * @return -1, 0 or 1 depending on order
     * @throws ClassCastException The specified object's type prevents it from
     *    being compared to this Object
     */
    @Override
    public int compareTo(Object o)
        throws ClassCastException
    {
        ShaderArguments sh = (ShaderArguments)o;
        return compareTo(sh);
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
        if(!(o instanceof ShaderArguments))
            return false;
        else
            return equals((ShaderArguments)o);
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Set the value of an float array uniform variable. This value will be set
     * the next time the shader is used. The array of provided data should be
     * at least as long as the size given.
     *
     * @param name The name of the uniform to be set
     * @param size A value of 1, 2, 3 or 4 depending on how many items
     * @param data The value(s) to be set
     * @param count The number of the values to set
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setUniform(String name, int size, float[] data, int count)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        // Validate the values before we get serious
        if(size < 1 || size > 4)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INVALID_DATA_SIZE_PROP);
            Locale lcl = intl_mgr.getFoundLocale();
            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Integer(size) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        if(count < 1)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INVALID_COUNT_PROP);
            Locale lcl = intl_mgr.getFoundLocale();
            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Integer(count) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        ShaderArgumentValue val = getValue(name);

        val.dataType = ShaderArgumentValue.FLOAT_ARRAY;
        val.size = size;
        val.count = count;

        int s2 = size * count;

        if(val.floatData == null || val.floatData.length < s2)
            val.floatData = new float[s2];

        System.arraycopy(data, 0, val.floatData, 0, s2);
    }

    /**
     * Set the value of an int array uniform variable. This value will be set
     * the next time the shader is used. The array of provided data should be
     * at least as long as the size given.
     *
     * @param name The name of the uniform to be set
     * @param size A value of 1, 2, 3 or 4 depending on how many items
     * @param data The value(s) to be set
     * @param count The number of the values to set
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setUniform(String name, int size, int[] data, int count)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        // Validate the values before we get serious
        if(size < 1 || size > 4)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INVALID_DATA_SIZE_PROP);
            Locale lcl = intl_mgr.getFoundLocale();
            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Integer(size) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        if(count < 1)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INVALID_COUNT_PROP);
            Locale lcl = intl_mgr.getFoundLocale();
            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Integer(count) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        ShaderArgumentValue val = getValue(name);

        val.dataType = ShaderArgumentValue.INT_ARRAY;
        val.size = size;
        val.count = count;

        int s2 = size * count;

        if(val.intData == null || val.intData.length < s2)
            val.intData = new int[s2];

        System.arraycopy(data, 0, val.intData, 0, s2);
    }

    /**
     * Set the value of an matrix uniform variable. This value will be set
     * the next time the shader is used. The array of provided data should must
     * be at least the size squared. The matrix values may be presented in
     * either column or row-major order in the array with the boolean flag used
     * to indicate which order to use when talking to OpenGL.
     *
     * @param name The name of the uniform to be set
     * @param size A value of 2, 3 or 4 depending on the matrix size (eg 2x2)
     * @param data The value(s) to be set
     * @param count The number of the values to set
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setUniformMatrix(String name,
                                 int size,
                                 float[] data,
                                 int count,
                                 boolean columnMajor)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        // Validate the values before we get serious
        if(size < 2 || size > 4)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INVALID_DATA_SIZE_PROP);
            Locale lcl = intl_mgr.getFoundLocale();
            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Integer(size) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        if(count < 1)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INVALID_COUNT_PROP);
            Locale lcl = intl_mgr.getFoundLocale();
            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Integer(count) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        ShaderArgumentValue val = getValue(name);

        val.dataType = ShaderArgumentValue.MATRIX;
        val.size = size;
        val.count = count;

        int s2 = size * size * count;

        if(val.floatData == null || val.floatData.length < s2)
            val.floatData = new float[s2];

        System.arraycopy(data, 0, val.floatData, 0, s2);
        val.transposeMatrix = !columnMajor;
    }

    /**
     * Convenience method to set the uniform name as a sampler for the given
     * texture unit number.
     *
     * @param name The name of the uniform to be set
     * @param textureUnitId The ID of the texture unit to assign to the sampler
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setUniformSampler(String name, int textureUnitId)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        // TODO:
        // Can samplers be in an array too? If so, we need to provide a
        // count ability here as well.
        ShaderArgumentValue val = getValue(name);

        val.dataType = ShaderArgumentValue.SAMPLER;
        val.size = 1;
        val.count = 1;

        if(val.intData == null)
            val.intData = new int[4];

        val.intData[0] = textureUnitId;
    }

    /**
     * Get the data type of this uniform. Returns the basic data type -
     * int, float, matrix or sampler. Use in combination with the other getter
     * methods to work out the real data size for any data fetching.
     * <p>
     * If the uniform name provided has not been set yet, this will return -1.
     *
     * @param name The name of the uniform to fetch the size of
     * @return One of the _UNIFORM_TYPE values or -1
     */
    public int getUniformType(String name)
    {
        ShaderArgumentValue val = getValue(name);
        return val == null ?  -1 : val.dataType; // since they are equivalent
    }

    /**
     * Get the data vector size for the given uniform name. For all the methods
     * that need a <code>size</code> parameter, this will return the value set
     * there. If the uniform name is used on a sampler (texture ID), this will
     * always return 1. If the uniform represents a matrix type, then this still
     * return 2, 3 or 4. Use the {@link #getUniformType(String)} method to
     * determine the underlying data type this represents.
     * <p>
     * If the uniform name provided has not been set yet, this will return -1.
     *
     * @param name The name of the uniform to fetch the size of
     * @return A value of 1, 2, 3 or 4  depending on how many items, or -1
     */
    public int getUniformSize(String name)
    {
        ShaderArgumentValue val = getValue(name);
        return val == null ?  -1 : val.size;
    }

    /**
     * Get the number of items declared for a uniform value. This returns the
     * number of elements in the array. For samplers, this always returns 1.
     *
     * @param name The name of the uniform to be set
     * @return A value greater than or equal to zero
     */
    public int getUniformCount(String name)
    {
        ShaderArgumentValue val = getValue(name);
        return val == null ?  -1 : val.count;
    }

    /**
     * Get the value of a uniform using a float array. Can be used to fetch
     * either the matrix types or float[] values. Type conversions for the
     * integer or sampler types are not performed and if requested an exception
     * will be generated. To work out if this type is a float-based type,
     * use {@link #getUniformType(String)}.
     * <p>
     * The provided array must be big enough to contain all of the data. The
     * length of the array must at least be:
     * <p>
     * <code>
     * int size = getUniformSize(name) * getUniformCount(name) *
     * (getUniformType(name) == MATRIX_UNIFORM_TYPE ? getUniformSize(name) : 1);
     * </code>
     * <p>
     *
     * If the uniform name is not known, nothing happens.
     *
     * @param name The name of the uniform to be set
     * @param data An array to copy values to
     * @throws InvalidDataTypeException Attempt to fetch an int type
     * @throws ArrayIndexOutOfBoundsException The provided array is too short
     */
    public void getUniform(String name, float[] data)
        throws InvalidDataTypeException
    {
        ShaderArgumentValue val = getValue(name);

        if(val == null)
            return;

        if(val.dataType == ShaderArgumentValue.INT_ARRAY ||
           val.dataType == ShaderArgumentValue.SAMPLER)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NOT_FLOAT_TYPE_PROP);
            throw new InvalidDataTypeException(msg);
        }

        // Check the size
        int req_size = val.size * val.count *
                       ((val.dataType == MATRIX_UNIFORM_TYPE) ? val.size : 1);

        System.arraycopy(val.floatData, 0, data, 0, req_size);
    }

    /**
     * Get the value of a uniform using an int array. Can be used to fetch
     * either the int[] types or sampler values. Type conversions for the
     * float or matrix types are not performed and if requested an exception
     * will be generated. To work out if this type is a int-based type,
     * use {@link #getUniformType(String)}.
     * <p>
     * The provided array must be big enough to contain all of the data. The
     * length of the array must at least be:
     * <p>
     * <code>
     * int size = getUniformSize(name) * getUniformCount(name);
     * </code>
     * <p>
     *
     * If the uniform name is not known, nothing happens.
     *
     * @param name The name of the uniform to be set
     * @param data An array to copy values to
     * @throws InvalidDataTypeException Attempt to fetch an int type
     * @throws ArrayIndexOutOfBoundsException The provided array is too short
     */
    public void getUniform(String name, int[] data)
        throws InvalidDataTypeException
    {
        ShaderArgumentValue val = getValue(name);

        if(val == null)
            return;

        if(val.dataType == ShaderArgumentValue.FLOAT_ARRAY ||
           val.dataType == ShaderArgumentValue.MATRIX)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NOT_INT_TYPE_PROP);
            throw new InvalidDataTypeException(msg);
        }

        // Check the size
        int req_size = val.size * val.count;

        System.arraycopy(val.intData, 0, data, 0, req_size);
    }

    /**
     * Remove the given uniform name from the settable argument list.
     * If the name isn't previously set, it silently ignores the request.
     *
     * @param name The name of the uniform variable to be removed.
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void removeUniform(String name)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        values.remove(name);

        // Run along the array looking for the matching name
        for(int i = 0; i < numVarNames; i++)
        {
            if(varNames[i].equals(name))
            {
                System.arraycopy(varNames,
                                 i + 1,
                                 varNames,
                                 i,
                                 numVarNames - i - 1);
                break;
            }
        }
    }

    /**
     * Fetch the Value object corresponding to the name. If there is no object,
     * create one.
     *
     * @param name The name of the argument to set
     * @return A value object for that name
     */
    private ShaderArgumentValue getValue(String name)
    {
        ShaderArgumentValue val = (ShaderArgumentValue)values.get(name);

        if(val == null)
        {
            if(varNames.length == numVarNames)
            {
                int new_size = varNames.length + 16;
                String[] tmp = new String[new_size];
                System.arraycopy(varNames, 0, tmp, 0, varNames.length);
                varNames = tmp;
            }

            varNames[numVarNames] = name;
            numVarNames++;

            val = new ShaderArgumentValue();
            values.put(name, val);
        }

        return val;
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param sh The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(ShaderArguments sh)
    {
        if(sh == null)
            return 1;

        if(sh == this)
            return 0;

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param sh The shader instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(ShaderArguments sh)
    {
        if(sh == this)
            return true;

        if(sh == null)
            return false;

        if(values.size() != sh.values.size())
            return false;

        for(int i = 0; i < numVarNames; i++)
        {
            ShaderArgumentValue val2 = sh.values.get(varNames[i]);

            if(val2 == null)
                return false;

            ShaderArgumentValue val1 = values.get(varNames[i]);

            if(!val1.equals(val2))
                return false;
        }

        return true;
    }
}
