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
import java.awt.image.*;

import java.nio.FloatBuffer;
import java.util.HashMap;

// Local imports

/**
 * Common representation of vertex-based geometry.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public abstract class VertexGeometry extends Geometry
{
    /** Coordinate information is included in the vertex values */
    protected static final int COORDINATES = 0x01;

    /** Mask to clear the coordinate setting */
    protected static final int COORDINATE_CLEAR = 0xFFFFFFFE;

    /** Normal information is included in the vertex values */
    protected static final int NORMALS = 0x02;

    /** Mask to clear the normal setting */
    protected static final int NORMAL_CLEAR = 0xFFFFFFFD;

    /** Mask to work out if texture coordinates are used */
    protected static final int TEXTURE_MASK = 0xF0;

    /** Mask to clear the texture coordinate setting */
    protected static final int TEXTURE_CLEAR = 0xFFFFFF0F;

    /** Single set of texture coordinates are included in the vertex values */
    protected static final int TEXTURE_COORDINATE_SINGLE = 0x10;

    /** Multiple sets of texture coordinates are included in the vertex values */
    protected static final int TEXTURE_COORDINATE_MULTI = 0x20;

    /** Has a valid texture set provided */
    protected static final int TEXTURE_SET_AVAILABLE = 0x0100;

    /** Has a valid texture set provided */
    protected static final int TEXTURE_SET_CLEAR = 0xFFFFF0FF;

    /** Mask to work out if colours are used */
    protected static final int COLOR_MASK = 0xF000;

    /** Mask to clear the texture coordinate setting */
    protected static final int COLOR_CLEAR = 0xFFFF0FFF;

    /** RGB colour values are supplied in the data */
    protected static final int COLOR_3 = 0x1000;

    /** RGBA colour values are supplied in the data */
    protected static final int COLOR_4 = 0x2000;


    // IDs for the texture set provision

    /** 1D texture coordinates are included in the vertex values */
    public static final int TEXTURE_COORDINATE_1 = 1;

    /** 2D texture coordinates are included in the vertex values */
    public static final int TEXTURE_COORDINATE_2 = 2;

    /** 3D texture coordinates are included in the vertex values */
    public static final int TEXTURE_COORDINATE_3 = 3;

    /** 4D texture coordinates are included in the vertex values */
    public static final int TEXTURE_COORDINATE_4 = 4;



    /** A mapping between glContext and displayListID(Integer) */
    protected HashMap displayListMap;

    /** Buffer for holding vertex data */
    protected FloatBuffer vertexBuffer;

    /** Buffer for holding colour data */
    protected FloatBuffer colorBuffer;

    /** Buffer for holding normal data */
    protected FloatBuffer normalBuffer;

    /** Buffer for holding texture coordinate data */
    protected FloatBuffer textureBuffer;

    /** Reference to the user array of coordinates used */
    protected float[] coordinates;

    /** Number of valid entries in the coordinate array */
    protected int numCoords;

    /** Reference to the user array of normals used */
    protected float[] normals;

    /** Reference to the user array of textures used, indexed by set */
    protected float[][] textures;

    /** The number of valid texture arrays in the textures variable */
    protected int numTextureArrays;

    /** The texture set map array that describes how to map the arrays */
    protected int[] textureSets;

    /** Flags for the texture type for each array. */
    protected int[] textureTypes;

    /** The number of texture sets to use  from the textureSet array */
    protected int numTextureSets;

    /** Reference to the user array of colors used */
    protected float[] colors;

    /** The format of the geometry used */
    protected int vertexFormat;

    /**
     * Flag to say that the display lists must be cleared and regenerated
     * because some state changed
     */
    protected boolean stateChanged;

    /**
     * Constructs an instance with pre-defined values with default values.
     *
     * @param format The type of vertex information to render
     */
    protected VertexGeometry()
    {
        displayListMap = new HashMap(1);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the number of vertices to the new number.
     *
     * @param count The new number, must be >= 0
     * @throw IllegalStateException This call was not made during the update
     *    callback if the node is live
     * @throw IllegalArgumentException The number is negative
     */
    public void setValidVertexCount(int count)
        throws IllegalStateException, IllegalArgumentException
    {
        if(count < 0)
            throw new IllegalArgumentException("Vertex count is negative");

        stateChanged = true;
        numCoords = count;
    }

    /**
     * Set the vertex array reference to the new array. The number of valid
     * items is taken to be the length of the array divided by three. This
     * replaces the existing vertex list array reference with the new reference.
     *
     * @param vtx The new array reference to use for vertex information
     * @throw IllegalStateException This call was not made during the update
     *    callback if the node is live
     */
    public void setVertices(float[] vertices)
        throws IllegalStateException
    {
        setVertices(vertices, (vertices == null) ? 0 : vertices.length / 3);
    }

    /**
     * Set the vertex array reference to the new array. The number of valid
     * items is taken from the second parameter. This replaces the existing
     * vertex list array reference with the new reference.
     *
     * @param vtx The new array reference to use for vertex information
     * @param numValid The number of valid values to use in the array
     * @throw IllegalStateException This call was not made during the update
     *    callback if the node is live
     * @throw IllegalArgumentException The number is negative
     */
    public void setVertices(float[] vertices, int numValid)
        throws IllegalStateException, IllegalArgumentException
    {
        if(numValid < 0)
            throw new IllegalArgumentException("Vertex count is negative");

        stateChanged = true;
        coordinates = vertices;
        numCoords = numValid;

        if(numValid > vertexBuffer.capacity())
            vertexBuffer = FloatBuffer.allocate(numValid * 3);
        else
            vertexBuffer.clear();

        vertexBuffer.put(vertices, 0, numValid * 3);

        if(numCoords == 0)
            vertexFormat &= COORDINATE_CLEAR;
        else
            vertexFormat |= COORDINATES;
    }

    /**
     * Set the color array reference to the new array. The number of valid
     * items is taken to be the length of the array divided by three. This
     * replaces the existing vertex list array reference with the new reference.
     *
     * @param hasAlpha true if this is 4 component colour, false for 3 component
     * @param colors The new array reference to use for color information
     * @throw IllegalStateException This call was not made during the update
     *    callback if the node is live
     * @throw IllegalArgumentException The length of the colors array is less
     *    than the number of declared vertices
     */
    public void setColors(boolean hasAlpha, float[] colors)
        throws IllegalStateException, IllegalArgumentException
    {
        int num_valid = 0;

        // check based on format
        if(colors != null)
        {
            if(hasAlpha)
            {
                if(colors.length / 4 < numCoords)
                    throw new IllegalArgumentException("Color array too short " +
                                                   "for 4 component colour");

                vertexFormat |= COLOR_4;
                num_valid = numCoords * 4;
            }
            else
            {
                if(colors.length / 3 < numCoords)
                    throw new IllegalArgumentException("Color array too short " +
                                                   "for 3 component colour");
                vertexFormat |= COLOR_3;
                num_valid = numCoords * 3;
            }
        }
        else
        {
            vertexFormat &= COLOR_CLEAR;
        }

        stateChanged = true;
        this.colors = colors;

        if(num_valid > colorBuffer.capacity())
            colorBuffer = FloatBuffer.allocate(num_valid);
        else
            colorBuffer.clear();

        vertexBuffer.put(colors, 0, num_valid);
    }

    /**
     * Set the normal array reference to the new array. The number of valid
     * items is taken to be the length of the array divided by three. This
     * replaces the existing normal list array reference with the new reference.
     *
     * @param normals The new array reference to use for normal information
     * @throw IllegalStateException This call was not made during the update
     *    callback if the node is live
     */
    public void setNormals(float[] normals)
        throws IllegalStateException, IllegalArgumentException
    {
        if((normals != null) && (normals.length < numCoords * 3))
            throw new IllegalArgumentException("Normal array too short");

        stateChanged = true;
        this.normals = normals;

        if(numCoords * 3 > normalBuffer.capacity())
            normalBuffer = FloatBuffer.allocate(numCoords * 3);
        else
            normalBuffer.clear();

        normalBuffer.put(normals, 0, numCoords * 3);

        if(normals == null)
            vertexFormat &= NORMAL_CLEAR;
        else
            vertexFormat |= NORMALS;
    }

    /**
     * Set the texture set map to the new mapping. The number of sets defined
     * is the length of the array.
     *
     * @param set The new set to use
     * @throw IllegalStateException This call was not made during the update
     *    callback if the node is live
     */
    public void setTextureSetMap(int[] set)
        throws IllegalStateException
    {
        setTextureSetMap(set, (set == null) ? 0 : set.length);
    }

    /**
     * Set the texture set map to the new mapping. The number of sets defined
     * is the numValid parameter.
     *
     * @param set The new set to use
     * @param numValid The length of the set to use
     * @throw IllegalStateException This call was not made during the update
     *    callback if the node is live
     */
    public void setTextureSetMap(int[] set, int numValid)
        throws IllegalStateException
    {
        if(set == null || numValid == 0)
        {
            vertexFormat &= TEXTURE_SET_CLEAR;
            numTextureSets = 0;
        }
        else
        {
            numTextureSets = numValid;
            vertexFormat |= TEXTURE_SET_AVAILABLE;
        }

        textureSets = set;
        stateChanged = true;
    }

    /**
     * Set a single texture array reference to the new array. The number of valid
     * items is taken to be the length of the array divided by three. This
     * replaces the existing tex coord list array reference with the new reference.
     *
     * @param type The texture type - 1D, 2D, 3D, 4D.
     * @param textureSet The set to update with these arrays
     * @param texCoords The new array reference to use for vertex information
     * @throw IllegalStateException This call was not made during the update
     *    callback if the node is live
     */
    public void setTextureCoordinates(int type,
                                      int textureSet,
                                      float[] texCoords)
        throws IllegalStateException, IllegalArgumentException
    {
        if(textureSet < 0 || textureSet >= numTextureArrays)
            throw new IllegalArgumentException("Invalid texture set specified: " +
                                               textureSet);

        int num_valid = 0;

        if(texCoords != null)
        {
            switch(type)
            {
                case TEXTURE_COORDINATE_1:
                    if(texCoords.length < numCoords)
                        throw new IllegalArgumentException("texCoord array too short " +
                                                       "for 1D texture coordiantes");
                    break;

                case TEXTURE_COORDINATE_2:
                    if(texCoords.length / 2 < numCoords)
                        throw new IllegalArgumentException("texCoord array too short " +
                                                       "for 2D texture coordiantes");
                    break;

                case TEXTURE_COORDINATE_3:
                    if(texCoords.length / 3 < numCoords)
                        throw new IllegalArgumentException("texCoord array too short " +
                                                       "for 3D texture coordiantes");
                    break;

                case TEXTURE_COORDINATE_4:
                    if(texCoords.length / 4 < numCoords)
                        throw new IllegalArgumentException("texCoord array too short " +
                                                       "for 4D texture coordiantes");
                    break;

                default:
                    throw new IllegalArgumentException("Invalid texture type: " + type);
            }
        }

        stateChanged = true;

        if(texCoords.length> normalBuffer.capacity())
            textureBuffer = FloatBuffer.allocate(texCoords.length);
        else
            textureBuffer.clear();

        textureBuffer.put(texCoords, 0, texCoords.length);
    }

    /**
     * Replace all the texture array reference with the new array. The number of
     * valid items is taken to be the length of the array divided by the vertex
     * format defined for this instance. This replaces the existing tex coord
     * list array reference with the new reference.
     *
     * @param types The sets of texture coordinate types that match each array
     * @param textureSet The set to update with these arrays
     * @param texCoords The new array reference to use for vertex information
     * @throw IllegalStateException This call was not made during the update
     *    callback if the node is live
     */
    public void setTextureCoordinates(int[] types, float[][] texCoords)
        throws IllegalStateException, IllegalArgumentException
    {
        setTextureCoordinates(types,
                              texCoords,
                              (texCoords == null) ? 0 : texCoords.length);
    }

    /**
     * Replace all the texture array reference to the new array. The number of
     * valid texture coordinates is taken from the numValid parameter. The
     * number of available sets is defined by numSets parameter.
     *
     * @param texCoords The new array reference to use for vertex information
     * @param numSets The number of texture sets that are valid
     * @throw IllegalStateException This call was not made during the update
     *    callback if the node is live
     */
    public void setTextureCoordinates(int[] types,
                                      float[][] texCoords,
                                      int numSets)
        throws IllegalStateException, IllegalArgumentException
    {
        if(texCoords == null)
        {
            vertexFormat &= TEXTURE_CLEAR;
            numTextureArrays = 0;
            textureBuffer.clear();
        }
        else
        {
            if((numSets < 0) || (texCoords.length < numSets))
                throw new IllegalArgumentException("Invalid set size");

            if((types == null) || (types.length < numSets))
                throw new IllegalArgumentException("Not enough types specified");

            int total = 0;

            // check the types for all valid values
            for(int i = 0; i < numSets; i++)
            {
                switch(types[i])
                {
                    case TEXTURE_COORDINATE_1:
                        if(texCoords[i].length < numCoords)
                            throw new IllegalArgumentException(
                                "Texture coordinate set " + i + " does not have " +
                                "enough values for a 1D texture");
                        total += texCoords[i].length;
                        break;

                    case TEXTURE_COORDINATE_2:
                        if(texCoords[i].length < numCoords * 2)
                            throw new IllegalArgumentException(
                                "Texture coordinate set " + i + " does not have " +
                                "enough values for a 2D texture");
                        total += texCoords[i].length;
                        break;

                    case TEXTURE_COORDINATE_3:
                        if(texCoords[i].length < numCoords * 3)
                            throw new IllegalArgumentException(
                                "Texture coordinate set " + i + " does not have " +
                                "enough values for a 3D texture");
                        total += texCoords[i].length;
                        break;

                    case TEXTURE_COORDINATE_4:
                        if(texCoords[i].length < numCoords * 4)
                            throw new IllegalArgumentException(
                                "Texture coordinate set " + i + " does not have " +
                                "enough values for a 4D texture");
                        total += texCoords[i].length;
                        break;

                    default:
                        throw new IllegalArgumentException("Invalid texture type");
                }
            }

            numTextureArrays = numSets;

            // just have to set some flag to say textures are available.
            vertexFormat |= (numSets > 1) ? TEXTURE_COORDINATE_SINGLE :
                                            TEXTURE_COORDINATE_MULTI;

            if(total > normalBuffer.capacity())
                textureBuffer = FloatBuffer.allocate(total);
            else
                textureBuffer.clear();

            for(int i = 0; i < numSets; i++)
                textureBuffer.put(texCoords[i], 0, texCoords[i].length);
        }

        textures = texCoords;
        textureTypes = types;


        stateChanged = true;
    }
}
