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

package org.j3d.renderer.aviatrix3d.nodes;

// External imports
import java.awt.image.*;
import java.nio.*;

import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;

import javax.media.opengl.GL;

import org.j3d.util.I18nManager;
import org.j3d.util.IntHashMap;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.rendering.GeometryRenderable;
import org.j3d.aviatrix3d.iutil.ShaderAttribValue;

/**
 * Base, unsafe, representation of geometry that uses NIO buffers directly
 * from the user.
 * <p>
 *
 * The code in this class is inherently unsafe in a multithreaded environment
 * because the data structures used by the user are the references that the
 * internal rendering also uses. That means a user can easily replace the
 * values in the arrays without ever needing to go through the callback
 * mechanism. The tradeoff this gives is reduced memory consumption and
 * somewhat faster speed due to no longer needing to copy arrays into buffers
 * every frame.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>numAttribsMsg: Error message when the attribute array size in the params
 *     is smaller than the current coord data.</li>
 * <li>invalidAttribSizeMsg: Error message when the attribute size is not [0,4].</li>
 * <li>negVertexCountMsg: Error message when given a negative vertex count.</li>
 * <li>normalsSizeMsg: Error message when the normal array is too small.</li>
 * <li>texCoordsSizeMsg: Error message when the tex coord array is too small.</li>
 * <li>fogCoordsSizeMsg: Error message when the fog coord array is too small.</li>
 * <li>secondaryColorsSizeMsg: Error message when the secondary color array is
 *     too small.</li>
 * <li>color3SizeMsg: Error message the array is the wrong size for 3 component
 *     colour values.</li>
 * <li>color4SizeMsg: Error message the array is the wrong size for 4 component
 *     colour values.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.15 $
 */
public abstract class BufferGeometry extends Geometry
    implements GeometryRenderable
{
    /** Attribute array is not long enough for the coords */
    private static final String NUM_ATTRIBS_PROP =
        "org.j3d.renderer.aviatrix3d.nodes.BufferGeometry.numAttribsMsg";

    /** Error message when the user-provided size is not valid */
    private static final String INVALID_ATTRIB_SIZE_PROP =
        "org.j3d.renderer.aviatrix3d.nodes.BufferGeometry.invalidAttribSizeMsg";


    /** Error message when user provides a negative vertex count */
    private static final String NEG_VTX_SIZE_PROP =
        "org.j3d.renderer.aviatrix3d.nodes.BufferedGeometry.negVertexCountMsg";

    /** Error message when numValid and vertex array lengths are different */
    private static final String VTX_SIZE_MATCH_PROP =
        "org.j3d.renderer.aviatrix3d.nodes.BufferedGeometry.vertexSizeMismatchMsg";

    /**
     * Error message when not enough values provided in the user array
     * for normals.
     */
    private static final String NORMALS_SIZE_PROP =
        "org.j3d.renderer.aviatrix3d.nodes.BufferedGeometry.normalsSizeMsg";

    /**
     * Error message when not enough values provided in the user array
     * for all variants of texture coordinates.
     */
    private static final String TEXCOORDS_SINGLE_SIZE_PROP =
        "org.j3d.renderer.aviatrix3d.nodes.BufferedGeometry.texCoordsSingleSizeMsg";

    /**
     * Error message when not enough values provided in the user array
     * for all variants of texture coordinates.
     */
    private static final String TEXCOORDS_MULTI_SIZE_PROP =
        "org.j3d.renderer.aviatrix3d.nodes.BufferedGeometry.texCoordsMultiSizeMsg";

    /**
     * Error message when not enough values provided in the user array
     * for all variants of texture coordinates.
     */
    private static final String INVALID_TEX_COORD_DIM_PROP =
        "org.j3d.renderer.aviatrix3d.nodes.BufferedGeometry.numTexCoordsDimMsg";

    /** Message when the user gives us a dodgy textureSet id */
    private static final String INVALID_TEX_SET_PROP =
        "org.j3d.renderer.aviatrix3d.nodes.BufferedGeometry.invalidTextureSetMsg";

    /** Message when the user does not give us a set of texture coordinates */
    private static final String MISSING_TEX_SET_PROP =
        "org.j3d.renderer.aviatrix3d.nodes.BufferedGeometry.missingTextureSetMsg";

    /**
     * Message when the number of texture sets is negative or less than the provided
     * array length.
     */
    private static final String TEX_SET_MISMATCH_PROP =
        "org.j3d.renderer.aviatrix3d.nodes.BufferedGeometry.texSetSizeMismatchMsg";

    /**
     * Message when the number of texture sets type is negative or less than the provided
     * array length.
     */
    private static final String TEX_TYPE_MISMATCH_PROP =
        "org.j3d.renderer.aviatrix3d.nodes.BufferedGeometry.texTypeSizeMismatchMsg";

    /**
     * Error message when not enough values provided in the user array
     * for fog coordinates.
     */
    private static final String FOGCOORDS_SIZE_PROP =
        "org.j3d.renderer.aviatrix3d.nodes.BufferedGeometry.fogCoordsSizeMsg";

    /**
     * Error message when not enough values provided in the user array
     * for secondary colour values.
     */
    private static final String SECONDARY_COLORS_SIZE_PROP =
        "org.j3d.renderer.aviatrix3d.nodes.BufferedGeometry.secondaryColorsSizeMsg";

    /**
     * Error message when not enough values provided in the user array
     * for 3 component colours.
     */
    private static final String COLOR_3_SIZE_PROP =
        "org.j3d.renderer.aviatrix3d.nodes.BufferedGeometry.color3SizeMsg";

    /**
     * Error message when not enough values provided in the user array
     * for 4 component colours.
     */
    private static final String COLOR_4_SIZE_PROP =
        "org.j3d.renderer.aviatrix3d.nodes.BufferedGeometry.color4SizeMsg";

    /** 2D Coordinate information is included in the vertex values */
    public static final int COORDINATE_2 = 0x02;

    /** 3D Coordinate information is included in the vertex values */
    public static final int COORDINATE_3 = 0x03;

    /** 4D Coordinate information is included in the vertex values */
    public static final int COORDINATE_4 = 0x04;

    /** Mask to work out what coordinates are used */
    protected static final int COORDINATE_MASK = 0x07;

    /** Mask to clear the coordinate setting */
    protected static final int COORDINATE_CLEAR = 0xFFFFFFF8;

    /** Normal information is included in the vertex values */
    protected static final int NORMALS = 0x08;

    /** Mask to clear the normal setting */
    protected static final int NORMAL_CLEAR = 0xFFFFFFF7;

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

    /** Mask to clear the per-vertex colour setting */
    protected static final int COLOR_CLEAR = 0xFFFF0FFF;

    /** RGB colour values are supplied in the data */
    protected static final int COLOR_3 = 0x1000;

    /** RGBA colour values are supplied in the data */
    protected static final int COLOR_4 = 0x2000;

    /** Mask to work out if edge flags are used */
    protected static final int EDGE_MASK = 0x10000;

    /** Mask to clear the edge flag setting */
    protected static final int EDGE_CLEAR = 0xFFFEFFFF;

    /** Edge values are supplied in the data */
    protected static final int EDGES = 0x10000;

    /** Mask to work out if secondary colours are used */
    protected static final int COLOR2_MASK = 0x20000;

    /** Mask to clear the per-vertex secondary colour setting */
    protected static final int COLOR2_CLEAR = 0xFFFDFFFF;

    /** Secondary color values are supplied in the data */
    protected static final int COLOR2 = 0x20000;

    /** Mask to work out if fog coordinates are used */
    protected static final int FOG_MASK = 0x40000;

    /** Mask to clear the fog coordinate setting */
    protected static final int FOG_CLEAR = 0xFFFBFFFF;

    /** Fog coordinate values are supplied in the data */
    protected static final int FOG = 0x40000;

    /** Mask to work out if shader vertex attributes are used */
    protected static final int ATTRIB_MASK = 0x80000;

    /** Mask to clear the shader vertex attributes  setting */
    protected static final int ATTRIB_CLEAR = 0xFFF7FFFF;

    /** Edge values are shader vertex attributes in the data */
    protected static final int ATTRIBS = 0x80000;



    // IDs for the texture set provision

    /** 1D texture coordinates are included in the vertex values */
    public static final int TEXTURE_COORDINATE_1 = 1;

    /** 2D texture coordinates are included in the vertex values */
    public static final int TEXTURE_COORDINATE_2 = 2;

    /** 3D texture coordinates are included in the vertex values */
    public static final int TEXTURE_COORDINATE_3 = 3;

    /** 4D texture coordinates are included in the vertex values */
    public static final int TEXTURE_COORDINATE_4 = 4;

    /** The current 2D coordinate list that we work from */
    private float[] working2dCoords;

    /** Working places for a single quad/triangle */
    protected float[] wkPolygon;

    /** Buffer for holding vertex data */
    protected FloatBuffer vertexBuffer;

    /** Buffer for holding colour data */
    protected FloatBuffer colorBuffer;

    /** Buffer for holding normal data */
    protected FloatBuffer normalBuffer;

    /** Buffer for holding fog coordinate data */
    protected FloatBuffer fogBuffer;

    /** Buffer for holding secondary color data */
    protected FloatBuffer color2Buffer;

    /** Buffer for holding texture coordinate data */
    protected FloatBuffer[] textureBuffer;

    /** Number of valid entries in the coordinate array */
    protected int numCoords;

    /** The number of valid texture arrays in the textures variable */
    protected int numTextureArrays;

    /** The texture set map array that describes how to map the arrays */
    protected int[] textureSets;

    /** Flags for the texture type for each array. */
    protected int[] textureTypes;

    /** List of offsets into the texture array
    /** The number of texture sets to use  from the textureSet array */
    protected int numTextureSets;

    /**
     * Map of the attribute Ids to their data (ref to user array). Not
     * allocated unless needed to save on memory footprint as this is a
     * relatively rare usage -  only when shaders are in use too.
     */
    protected IntHashMap attributes;

    /** Listing of the valid attribute IDs for rendering */
    protected int[] attribIds;

    /** The format of the geometry used */
    protected int vertexFormat;

    /**
    /**
     * Constructs an instance with pre-defined values with default values.
     */
    protected BufferGeometry()
    {
        vertexBuffer = FloatBuffer.allocate(0);
        normalBuffer = FloatBuffer.allocate(0);
        colorBuffer = FloatBuffer.allocate(0);
        fogBuffer = FloatBuffer.allocate(0);
        color2Buffer = FloatBuffer.allocate(0);

        textureBuffer = new FloatBuffer[1];
        bounds = new BoundingBox();
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
        // Ignore stuff that doesn't change the state
        if(state == alive)
            return;

        boolean old_state = alive;

        super.setLive(state);

        if(!old_state && state)
            recomputeBounds();
    }

    //----------------------------------------------------------
    // Methods defined by Geometry
    //----------------------------------------------------------

    /**
     * Check to see if this geometry is making the geometry visible or
     * not. Returns true if the defined number of coordinates is non-zero.
     *
     * @return true when the geometry is visible
     */
    protected boolean isVisible()
    {
        return numCoords != 0;
    }

    /**
     * Check to see whether this shape is something that represents 2D or 3D
     * geometry. Pure 2D geometry is not effected by any
     * {@link org.j3d.aviatrix3d.rendering.EffectRenderable}, while 3D is.
     * Note that this can be changed depending on the type of geometry itself.
     * A Shape3D node with an IndexedLineArray that only has 2D coordinates is
     * as much a 2D geometry as a raster object.
     *
     * @return True if this is 2D geometry, false if this is 3D
     */
    public boolean is2D()
    {
        return (vertexFormat & COORDINATE_2) != 0;
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Get the current vertex format type - 2D, 3D, or 4D.
     *
     * @return The number of dimensions to the coordinates - 2D, 3D or 4D
     */
    public int getVertexType()
    {
        return (vertexFormat & COORDINATE_MASK);
    }

    /**
     * Get the number of vertices that are valid in the geometry arrays.
     *
     * @return a number >= 0
     */
    public int getValidVertexCount()
    {
        return numCoords;
    }

    /**
     * Set the number of vertices to the new number.
     * <p>
     *
     * In a live scene graph, can only be called during the bounds changed
     * callback.
     *
     * @param count The new number, must be >= 0
     * @throws IllegalArgumentException The number is negative
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds changed callback method
     */
    public void setValidVertexCount(int count)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        if(count < 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(NEG_VTX_SIZE_PROP);

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

        numCoords = count;
    }

    /**
     * Set the vertex array reference to the new array. The number of valid
     * items is taken to be the length of the array divided by three. This
     * replaces the existing vertex list array reference with the new reference.
     * <p>
     *
     * In a live scene graph, can only be called during the bounds changed
     * callback.
     *
     * @param type The number of dimensions to the coordinates - 2D, 3D or 4D
     * @param vertices The new array reference to use for vertex information
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds changed callback method
     */
    public void setVertices(int type, FloatBuffer vertices)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        int num_vert = (vertices == null) ? 0 : vertices.limit() / type;
        setVertices(type, vertices, num_vert);
    }

    /**
     * Retrieve the vertices that are currently set. The array must be at
     * least as long as the valid vertex count, times 3. If none are set
     * currently or have been cleared, the provided array is untouched.
     *
     * @param vertices The array to copy the values into
     * @throws ArrayIndexOutOfBoundsException The provided array is too short
     */
    public void getVertices(FloatBuffer vertices)
    {
        vertices.put(vertexBuffer);
    }

    /**
     * Set the vertex array reference to the new array. The number of valid
     * items is taken from the second parameter. This replaces the existing
     * vertex list array reference with the new reference.
     * <p>
     *
     * In a live scene graph, can only be called during the bounds changed
     * callback.
     *
     * @param type The number of dimensions to the coordinates - 2D, 3D or 4D
     * @param vertices The new array reference to use for vertex information
     * @param numValid The number of valid values to use in the array
     * @throws IllegalArgumentException The number is negative
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds changed callback method
     */
    public void setVertices(int type, FloatBuffer vertices, int numValid)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        if(numValid < 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(NEG_VTX_SIZE_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Integer(numValid) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        int vtx_size = type & 0x07;
        numCoords = numValid;

        if(numValid == 0)
        {
            vertexFormat &= COORDINATE_CLEAR;
            vertexBuffer.clear();
            return;
        }

        if(numValid * vtx_size > vertices.limit())
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(VTX_SIZE_MATCH_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args =
            {
                new Integer(vertices.limit()),
                new Integer(numValid * vtx_size)
            };
            Format[] fmts = { n_fmt, n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        vertexBuffer = vertices;
        vertexFormat |= type;
    }

    /**
     * Set the color array reference to the new array. The number of valid
     * items is taken to be the length of the array divided by three. This
     * replaces the existing vertex list array reference with the new reference.
     * <p>
     *
     * In a live scene graph, can only be called during the data changed
     * callback.
     *
     * @param hasAlpha true if this is 4 component colour, false for 3 component
     * @param colors The new array reference to use for color information
     * @throws IllegalArgumentException The length of the colors array is less
     *    than the number of declared vertices
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setColors(boolean hasAlpha, FloatBuffer colors)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        int num_valid = 0;

        // check based on format
        if(colors != null)
        {
            if(hasAlpha)
            {
                if(colors.limit() / 4 < numCoords)
                {
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg_pattern = intl_mgr.getString(COLOR_4_SIZE_PROP);

                    Locale lcl = intl_mgr.getFoundLocale();

                    NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

                    Object[] msg_args = { new Integer(colors.limit() / 4) };
                    Format[] fmts = { n_fmt };
                    MessageFormat msg_fmt =
                        new MessageFormat(msg_pattern, lcl);
                    msg_fmt.setFormats(fmts);
                    String msg = msg_fmt.format(msg_args);

                    throw new IllegalArgumentException(msg);
                }

                vertexFormat |= COLOR_4;
                num_valid = numCoords * 4;
            }
            else
            {
                if(colors.limit() / 3 < numCoords)
                {
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg_pattern = intl_mgr.getString(COLOR_4_SIZE_PROP);

                    Locale lcl = intl_mgr.getFoundLocale();

                    NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

                    Object[] msg_args = { new Integer(colors.limit() / 3) };
                    Format[] fmts = { n_fmt };
                    MessageFormat msg_fmt =
                        new MessageFormat(msg_pattern, lcl);
                    msg_fmt.setFormats(fmts);
                    String msg = msg_fmt.format(msg_args);

                    throw new IllegalArgumentException(msg);
                }

                vertexFormat |= COLOR_3;
                num_valid = numCoords * 3;
            }
        }
        else
        {
            vertexFormat &= COLOR_CLEAR;
        }

        colorBuffer = colors;

        // TODO:
        // Would like to add some ability to specify the checking policy here.
        // For example if the user provides an array of 4 component colours,
        // but none of them are actually transparent, (ie all alphas are 1.0)
        // then it would be nice to set validAlpha to false so that we can
        // avoid putting this on the transparent sorted list.
        //
        // Various ways of doing this - method parameter, a separate settable
        // "policy" API call, or maybe a static flag that is set from a
        // system property.
        validAlpha = hasAlpha;
    }

    /**
     * Retrieve the colours that are currently set. The array must be at
     * least as long as the valid vertex count, times 3 or 4, depending on
     * whether alpha values are currently set. If none are set
     * currently or have been cleared, the provided array is untouched.
     *
     * @param col The array to copy the values into
     * @throws ArrayIndexOutOfBoundsException The provided array is too short
     */
    public void getColors(FloatBuffer col)
    {
        if(colorBuffer != null)
        {
            col.rewind();
            col.put(colorBuffer);
        }
    }

    /**
     * Set the normal array reference to the new array. The number of valid
     * items is taken to be the length of the array divided by three. This
     * replaces the existing normal list array reference with the new reference.
     * <p>
     *
     * In a live scene graph, can only be called during the data changed
     * callback.
     *
     * @param normals The new array reference to use for normal information
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setNormals(FloatBuffer normals)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if((normals != null) && (normals.limit() < numCoords * 3))
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(NORMALS_SIZE_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args =
            {
                new Integer(normals.limit()),
                new Integer(numCoords * 3)
            };

            Format[] fmts = { n_fmt, n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        normalBuffer = normals;

        if(normals == null)
            vertexFormat &= NORMAL_CLEAR;
        else
            vertexFormat |= NORMALS;
    }

    /**
     * Retrieve the normals that are currently set. The array must be at
     * least as long as the valid vertex count, times 3. If none are set
     * currently or have been cleared, the provided array is untouched.
     *
     * @param n The array to copy the values into
     * @throws ArrayIndexOutOfBoundsException The provided array is too short
     */
    public void getNormals(FloatBuffer n)
    {
        if(normalBuffer != null)
        {
            n.rewind();
            n.put(normalBuffer);
        }
    }

    /**
     * Set the texture set map to the new mapping. The number of sets defined
     * is the length of the array.
     * <p>
     *
     * In a live scene graph, can only be called during the data changed
     * callback.
     *
     * @param set The new set to use
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setTextureSetMap(int[] set)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        setTextureSetMap(set, (set == null) ? 0 : set.length);
    }

    /**
     * Set the texture set map to the new mapping. The number of sets defined
     * is the numValid parameter.
     * <p>
     *
     * In a live scene graph, can only be called during the data changed
     * callback.
     *
     * @param set The new set to use
     * @param numValid The length of the set to use
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setTextureSetMap(int[] set, int numValid)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

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
    }

    /**
     * Set a single texture array reference to the new array. The number of valid
     * items is taken to be the length of the array divided by three. This
     * replaces the existing tex coord list array reference with the new reference.
     * <p>
     *
     * In a live scene graph, can only be called during the data changed
     * callback.
     *
     * @param type The texture type - 1D, 2D, 3D, 4D.
     * @param textureSet The set to update with these arrays
     * @param texCoords The new array reference to use for vertex information
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void setTextureCoordinates(int type,
                                      int textureSet,
                                      FloatBuffer texCoords)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(textureSet < 0 || textureSet >= numTextureArrays)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INVALID_TEX_SET_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args =
            {
                new Integer(textureSet),
                new Integer(numTextureArrays)
            };

            Format[] fmts = { n_fmt, n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        int num_valid = 0;

        if(texCoords != null)
        {
            switch(type)
            {
                case TEXTURE_COORDINATE_1:
                    checkTexCoordSize(texCoords, 1);
                    break;

                case TEXTURE_COORDINATE_2:
                    checkTexCoordSize(texCoords, 2);
                    break;

                case TEXTURE_COORDINATE_3:
                    checkTexCoordSize(texCoords, 3);
                    break;

                case TEXTURE_COORDINATE_4:
                    checkTexCoordSize(texCoords, 4);
                    break;

                default:
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg_pattern = intl_mgr.getString(INVALID_TEX_COORD_DIM_PROP);

                    Locale lcl = intl_mgr.getFoundLocale();

                    NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

                    Object[] msg_args = { new Integer(type) };

                    Format[] fmts = { n_fmt };
                    MessageFormat msg_fmt =
                        new MessageFormat(msg_pattern, lcl);
                    msg_fmt.setFormats(fmts);
                    String msg = msg_fmt.format(msg_args);

                    throw new IllegalArgumentException(msg);
            }
        }

        textureBuffer[textureSet] = texCoords;
    }

    /**
     * Replace all the texture array reference with the new array. The number of
     * valid items is taken to be the length of the array divided by the vertex
     * format defined for this instance. This replaces the existing tex coord
     * list array reference with the new reference.
     * <p>
     *
     * In a live scene graph, can only be called during the data changed
     * callback.
     *
     * @param types The sets of texture coordinate types that match each array
     * @param texCoords The new array reference to use for vertex information
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setTextureCoordinates(int[] types, FloatBuffer[] texCoords)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        setTextureCoordinates(types,
                              texCoords,
                              (texCoords == null) ? 0 : texCoords.length);
    }

    /**
     * Replace all the texture array reference to the new array. The number of
     * valid texture coordinates is taken from the numValid parameter. The
     * number of available sets is defined by numSets parameter.
     * <p>
     *
     * In a live scene graph, can only be called during the data changed
     * callback.
     *
     * @param types The sets of texture coordinate types that match each array
     * @param texCoords The new array reference to use for vertex information
     * @param numSets The number of texture sets that are valid
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setTextureCoordinates(int[] types,
                                      FloatBuffer[] texCoords,
                                      int numSets)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if((texCoords == null) || (numSets == 0))
        {
            vertexFormat &= TEXTURE_CLEAR;
            numTextureArrays = 0;
        }
        else
        {
            if((numSets < 0) || (texCoords.length < numSets))
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(TEX_SET_MISMATCH_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

                Object[] msg_args =
                {
                    new Integer(numSets),
                    new Integer(texCoords.length)
                };

                Format[] fmts = { n_fmt, n_fmt };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                msg_fmt.setFormats(fmts);
                String msg = msg_fmt.format(msg_args);

                throw new IllegalArgumentException(msg);
            }

            if((types == null) || (types.length < numSets))
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(TEX_SET_MISMATCH_PROP);

                Locale lcl = intl_mgr.getFoundLocale();

                NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

                Object[] msg_args =
                {
                    new Integer(numSets),
                    new Integer(types == null ? 0 : types.length)
                };

                Format[] fmts = { n_fmt, n_fmt };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                msg_fmt.setFormats(fmts);
                String msg = msg_fmt.format(msg_args);

                throw new IllegalArgumentException(msg);
            }

            // check the types for all valid values
            for(int i = 0; i < numSets; i++)
            {
                if((texCoords[i] == null) || (texCoords[i].limit() == 0))
                {
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg_pattern = intl_mgr.getString(MISSING_TEX_SET_PROP);

                    Locale lcl = intl_mgr.getFoundLocale();

                    NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

                    Object[] msg_args =
                    {
                        new Integer(i),
                        new Integer(numCoords)
                    };

                    Format[] fmts = { n_fmt, n_fmt };
                    MessageFormat msg_fmt =
                        new MessageFormat(msg_pattern, lcl);
                    msg_fmt.setFormats(fmts);
                    String msg = msg_fmt.format(msg_args);

                    throw new IllegalArgumentException(msg);
                }

                int num_tex_coords = 0;
                switch(types[i])
                {
                    case TEXTURE_COORDINATE_1:
                        checkTexCoordSize(i, texCoords[i],  1);
                        break;

                    case TEXTURE_COORDINATE_2:
                        checkTexCoordSize(i, texCoords[i],  2);
                        break;

                    case TEXTURE_COORDINATE_3:
                        checkTexCoordSize(i, texCoords[i],  3);
                        break;

                    case TEXTURE_COORDINATE_4:
                        checkTexCoordSize(i, texCoords[i],  4);
                        break;

                    default:
                        I18nManager intl_mgr = I18nManager.getManager();
                        String msg_pattern = intl_mgr.getString(INVALID_TEX_COORD_DIM_PROP);

                        Locale lcl = intl_mgr.getFoundLocale();

                        NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

                        Object[] msg_args = { new Integer(types[i]) };

                        Format[] fmts = { n_fmt };
                        MessageFormat msg_fmt =
                            new MessageFormat(msg_pattern, lcl);
                        msg_fmt.setFormats(fmts);
                        String msg = msg_fmt.format(msg_args);

                        throw new IllegalArgumentException(msg);
                }
            }

            numTextureArrays = numSets;

            if(numTextureSets == 0)
            {
                textureSets = new int[numSets];
                for(int i = 0; i < numSets; i++)
                    textureSets[i] = i;

                numTextureSets = numSets;
            }

            // just have to set some flag to say textures are available.
            vertexFormat |= (numSets > 1) ? TEXTURE_COORDINATE_SINGLE :
                                            TEXTURE_COORDINATE_MULTI;
        }

        textureBuffer = texCoords;
        textureTypes = types;
    }

    /**
     * Retrieve the texture coordinates that are currently set. The array must
     * be at least as long as the valid vertex count, times 3. If none are set
     * currently or have been cleared, the provided array is untouched.
     *
     * @param coords The array to copy the values into
     * @throws ArrayIndexOutOfBoundsException The provided array is too short
     */
    public void getTextureCoordinates(FloatBuffer[] coords)
    {
        if(textureBuffer == null)
            return;

        for(int i = 0; i < numTextureSets; i++)
            coords[i].put(textureBuffer[i]);
    }

    /**
     * Set the fog coordinate reference to the new array. The number of valid
     * items is taken to be the length of the array (there's only one coord per
     * vertex). This replaces the existing fog coordinate array reference with
     * the new reference. If set to null, will clear the use of fog coordinates.
     * Setting this value will also cause the system to automatically make use
     * of them rather than the fragment depth (it will inherently call
     * <code>glFogi(GL_FOG_COORDINATE_SOURCE, GL_FOG_COORDINATE)</code> before
     * setting these values and then clear it afterwards.
     * <p>
     *
     * In a live scene graph, can only be called during the data changed
     * callback.
     *
     * @param coords The new array reference to use for z depth values
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setFogCoordinates(FloatBuffer coords)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if((coords != null) && (coords.limit() < numCoords))
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(FOGCOORDS_SIZE_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args =
            {
                new Integer(coords.limit()),
                new Integer(numCoords)
            };

            Format[] fmts = { n_fmt, n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        fogBuffer = coords;

        if(fogBuffer == null)
            vertexFormat &= FOG_CLEAR;
        else
            vertexFormat |= FOG;
    }

    /**
     * Retrieve the fog coordinates that are currently set. The array must be
     * at least as long as the valid vertex count, times 3. If none are set
     * currently or have been cleared, the provided array is untouched.
     *
     * @param fogs The array to copy the values into
     * @throws ArrayIndexOutOfBoundsException The provided array is too short
     */
    public void getFogCoordinates(FloatBuffer fogs)
    {
        if(fogBuffer != null)
            fogs.put(fogBuffer);
    }

    /**
     * Set the secondary color reference to the new array. The number of valid
     * items is taken to be the length of the array divided by three as
     * secondary color values cannot have an alpha value specified. This
     * replaces the existing secondary color array reference with the new
     * reference.
     * <p>
     *
     * In a live scene graph, can only be called during the data changed
     * callback.
     *
     * @param colors The new array reference to use for secondary color
     *    information
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void setSecondaryColors(FloatBuffer colors)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if((colors != null) && (colors.limit() < numCoords * 3))
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(SECONDARY_COLORS_SIZE_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args =
            {
                new Integer(colors.limit()),
                new Integer(numCoords * 3)
            };

            Format[] fmts = { n_fmt, n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        color2Buffer = colors;

        if(colors == null)
            vertexFormat &= COLOR2_CLEAR;
        else
            vertexFormat |= COLOR2;
    }

    /**
     * Retrieve the secondary colors that are currently set. The array must be at
     * least as long as the valid vertex count, times 3. If none are set
     * currently or have been cleared, the provided array is untouched.
     *
     * @param cols The array to copy the values into
     * @throws ArrayIndexOutOfBoundsException The provided array is too short
     */
    public void getSecondaryColors(FloatBuffer cols)
    {
        if(color2Buffer != null)
            cols.put(color2Buffer);
    }

    /**
     * Set the attribute values at the given index to a new value. The array
     * provided must have a length of the number of coordinates times the size.
     * Setting a size of -1 means to clear this attribute index from being
     * used in future geometry updates. The size parameter must be one of
     * -1, 1, 2, 3 or 4.
     * <p>
     *
     * No checks are made on the index value and it is assumed the user
     * provides valid values for this after binding the index in the
     * {@link ShaderProgram} class.
     * <p>
     * <b>Note:</b> If the index provided is zero, then this attribute will
     * replace the vertex values (eg a glVertex() call) with the attribute
     * value, as per the OpenGL specification.
     * <p>
     *
     * In a live scene graph, can only be called during the data changed
     * callback.
     *
     * @param index The attribute index to set these values for
     * @param size The number of components: -1, 1, 2, 3 or 4
     * @param attribs The new array reference to use for attribute information
     * @param normalise true if the values should be normalised [-1.0, 1.0]
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setAttributes(int index,
                              int size,
                              FloatBuffer attribs,
                              boolean normalise)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(size == -1)
        {
            if(attributes == null)
                return;

            attributes.remove(index);

            if(attributes.size() == 0)
                vertexFormat &= ATTRIB_CLEAR;

            return;
        }

        checkAttribSize(size, attribs);

        if(attributes == null)
            attributes = new IntHashMap();

        ShaderAttribValue val = (ShaderAttribValue)attributes.get(index);

        if(val == null)
        {
            val = new ShaderAttribValue();
            attributes.put(index, val);
        }

        val.size = size;
        val.normalise = normalise;
        val.dataType = GL.GL_FLOAT;
        val.data = attribs;

        vertexFormat |= ATTRIBS;
    }

    /**
     * Set the attribute values at the given index to a new value. The array
     * provided must have a length of the number of coordinates times the size.
     * Setting a size of -1 means to clear this attribute index from being
     * used in future geometry updates. The size parameter must be one of
     * -1, 1, 2, 3 or 4.
     * <p>
     *
     * No checks are made on the index value and it is assumed the user
     * provides valid values for this after binding the index in the
     * {@link ShaderProgram} class.
     * <p>
     * <b>Note:</b> If the index provided is zero, then this attribute will
     * replace the vertex values (eg a glVertex() call) with the attribute
     * value, as per the OpenGL specification.
     * <p>
     *
     * In a live scene graph, can only be called during the data changed
     * callback.
     *
     * @param index The attribute index to set these values for
     * @param size The number of components: -1, 1, 2, 3 or 4
     * @param attribs The new array reference to use for attribute information
     * @param normalise true if the values should be normalised [-1.0, 1.0]
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setAttributes(int index,
                              int size,
                              DoubleBuffer attribs,
                              boolean normalise)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(size == -1)
        {
            if(attributes == null)
                return;

            attributes.remove(index);

            if(attributes.size() == 0)
                vertexFormat &= ATTRIB_CLEAR;

            return;
        }

        checkAttribSize(size, attribs);

        if(attributes == null)
            attributes = new IntHashMap();

        ShaderAttribValue val = (ShaderAttribValue)attributes.get(index);

        if(val == null)
        {
            val = new ShaderAttribValue();
            attributes.put(index, val);
        }

        val.size = size;
        val.normalise = normalise;
        val.dataType = GL.GL_DOUBLE;
        val.data = attribs;

        vertexFormat |= ATTRIBS;
    }

    /**
     * Set the attribute values at the given index to a new value. The array
     * provided must have a length of the number of coordinates times the size.
     * Setting a size of -1 means to clear this attribute index from being
     * used in future geometry updates. The size parameter must be one of
     * -1, 1, 2, 3 or 4.
     * <p>
     *
     * No checks are made on the index value and it is assumed the user
     * provides valid values for this after binding the index in the
     * {@link ShaderProgram} class.
     * <p>
     * <b>Note:</b> If the index provided is zero, then this attribute will
     * replace the vertex values (eg a glVertex() call) with the attribute
     * value, as per the OpenGL specification.
     * <p>
     *
     * In a live scene graph, can only be called during the data changed
     * callback.
     *
     * @param index The attribute index to set these values for
     * @param size The number of components: -1, 1, 2, 3 or 4
     * @param attribs The new array reference to use for attribute information
     * @param normalise true if the values should be normalised [-1.0, 1.0]
     * @param signed false if this is unsigned ints, true for signed values
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setAttributes(int index,
                              int size,
                              IntBuffer attribs,
                              boolean normalise,
                              boolean signed)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(size == -1)
        {
            if(attributes == null)
                return;

            attributes.remove(index);

            if(attributes.size() == 0)
                vertexFormat &= ATTRIB_CLEAR;

            return;
        }

        checkAttribSize(size, attribs);

        if(attributes == null)
            attributes = new IntHashMap();

        ShaderAttribValue val = (ShaderAttribValue)attributes.get(index);

        if(val == null)
        {
            val = new ShaderAttribValue();
            attributes.put(index, val);
        }

        val.size = size;
        val.normalise = normalise;
        val.dataType = signed ? GL.GL_INT : GL.GL_UNSIGNED_INT;
        val.data = attribs;

        vertexFormat |= ATTRIBS;
    }

    /**
     * Set the attribute values at the given index to a new value. The array
     * provided must have a length of the number of coordinates times the size.
     * Setting a size of -1 means to clear this attribute index from being
     * used in future geometry updates. The size parameter must be one of
     * -1, 1, 2, 3 or 4.
     * <p>
     *
     * No checks are made on the index value and it is assumed the user
     * provides valid values for this after binding the index in the
     * {@link ShaderProgram} class.
     * <p>
     * <b>Note:</b> If the index provided is zero, then this attribute will
     * replace the vertex values (eg a glVertex() call) with the attribute
     * value, as per the OpenGL specification.
     * <p>
     *
     * In a live scene graph, can only be called during the data changed
     * callback.
     *
     * @param index The attribute index to set these values for
     * @param size The number of components: -1, 1, 2, 3 or 4
     * @param attribs The new array reference to use for attribute information
     * @param normalise true if the values should be normalised [-1.0, 1.0]
     * @param signed false if this is unsigned shorts, true for signed values
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setAttributes(int index,
                              int size,
                              ShortBuffer attribs,
                              boolean normalise,
                              boolean signed)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(size == -1)
        {
            if(attributes == null)
                return;

            attributes.remove(index);

            if(attributes.size() == 0)
                vertexFormat &= ATTRIB_CLEAR;

            return;
        }

        checkAttribSize(size, attribs);

        if(attributes == null)
            attributes = new IntHashMap();

        ShaderAttribValue val = (ShaderAttribValue)attributes.get(index);

        if(val == null)
        {
            val = new ShaderAttribValue();
            attributes.put(index, val);
        }

        val.size = size;
        val.normalise = normalise;
        val.dataType = signed ? GL.GL_SHORT : GL.GL_UNSIGNED_SHORT;
        val.data = attribs;

        vertexFormat |= ATTRIBS;
    }

    /**
     * Set the attribute values at the given index to a new value. The array
     * provided must have a length of the number of coordinates times the size.
     * Setting a size of -1 means to clear this attribute index from being
     * used in future geometry updates. The size parameter must be one of
     * -1, 1, 2, 3 or 4.
     * <p>
     *
     * No checks are made on the index value and it is assumed the user
     * provides valid values for this after binding the index in the
     * {@link ShaderProgram} class.
     * <p>
     * <b>Note:</b> If the index provided is zero, then this attribute will
     * replace the vertex values (eg a glVertex() call) with the attribute
     * value, as per the OpenGL specification.
     * <p>
     *
     * In a live scene graph, can only be called during the data changed
     * callback.
     *
     * @param index The attribute index to set these values for
     * @param size The number of components: -1, 1, 2, 3 or 4
     * @param attribs The new array reference to use for attribute information
     * @param normalise true if the values should be normalised [-1.0, 1.0]
     * @param signed false if this is unsigned shorts, true for signed values
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setAttributes(int index,
                              int size,
                              ByteBuffer attribs,
                              boolean normalise,
                              boolean signed)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(size == -1)
        {
            if(attributes == null)
                return;

            attributes.remove(index);

            if(attributes.size() == 0)
                vertexFormat &= ATTRIB_CLEAR;

            return;
        }

        checkAttribSize(size, attribs);

        if(attributes == null)
            attributes = new IntHashMap();

        ShaderAttribValue val = (ShaderAttribValue)attributes.get(index);

        if(val == null)
        {
            val = new ShaderAttribValue();
            attributes.put(index, val);
        }

        val.size = size;
        val.normalise = normalise;
        val.dataType = signed ? GL.GL_BYTE : GL.GL_UNSIGNED_BYTE;
        val.data = attribs;

        vertexFormat |= ATTRIBS;
    }

    /**
     * Convenience method to pass everything to the rendering pipeline.
     * Will check for the various masks being set and only send the
     * needed data. The only call not made here is the
     * <code>glDrawArrays()</code> or equivalent.
     *
     * @param gl The gl context to draw with
     */
    protected void setVertexState(GL gl)
    {
        //  State enable first
        int vtx_size = vertexFormat & 0x07;
        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);

        gl.glVertexPointer(vtx_size, GL.GL_FLOAT, 0, vertexBuffer);

        if((vertexFormat & NORMALS) != 0)
        {
            gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
            gl.glNormalPointer(GL.GL_FLOAT, 0, normalBuffer);
        }

        if((vertexFormat & TEXTURE_MASK) != 0)
        {
            // single texturing or multi-texturing
            if(numTextureSets == 1)
            {
                gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
                gl.glTexCoordPointer(textureTypes[0],
                                     GL.GL_FLOAT,
                                     0,
                                     textureBuffer[0]);
            }
            else
            {
                for(int i = 0; i < numTextureSets; i++)
                {
                    int set_id = textureSets[i];
                    gl.glClientActiveTexture(GL.GL_TEXTURE0 + i);
                    gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
                    gl.glTexCoordPointer(textureTypes[set_id],
                                         GL.GL_FLOAT,
                                         0,
                                         textureBuffer[set_id]);
                }
            }
        }

        if((vertexFormat & COLOR_MASK) != 0)
        {
            gl.glEnableClientState(GL.GL_COLOR_ARRAY);
            int size = ((vertexFormat & COLOR_3) != 0) ? 3 : 4;
            gl.glColorPointer(size, GL.GL_FLOAT, 0, colorBuffer);
        }

        if((vertexFormat & COLOR2_MASK) != 0)
        {
            gl.glEnableClientState(GL.GL_SECONDARY_COLOR_ARRAY);
            gl.glSecondaryColorPointer(3, GL.GL_FLOAT, 0, color2Buffer);
        }

        if((vertexFormat & FOG_MASK) != 0)
        {
            gl.glFogi(GL.GL_FOG_COORDINATE_SOURCE, GL.GL_FOG_COORDINATE);
            gl.glEnableClientState(GL.GL_FOG_COORDINATE_ARRAY);
            gl.glFogCoordPointer(GL.GL_FLOAT, 0, fogBuffer);
        }

        if((vertexFormat & ATTRIBS) != 0)
        {
            int num_attribs = attributes.size();
            attribIds = attributes.keySet(attribIds);

            for(int i = 0; i < num_attribs; i++)
            {
                int id = attribIds[i];

                ShaderAttribValue val = (ShaderAttribValue)attributes.get(id);
                gl.glEnableVertexAttribArrayARB(id);
                gl.glVertexAttribPointerARB(id,
                                            val.size,
                                            val.dataType,
                                            val.normalise,
                                            0,
                                            val.data);
            }
        }
    }

    /**
     * Convenience method to clear the previously set state in the rendering
     * pipeline. Should be called just after the derived class calls
     * <code>glDrawArrays()</code> or equivalent.
     *
     * @param gl The gl context to draw with
     */
    protected void clearVertexState(GL gl)
    {
        if((vertexFormat & ATTRIBS) != 0)
        {
            int num_attribs = attributes.size();

            for(int i = 0; i < num_attribs; i++)
                gl.glDisableVertexAttribArrayARB(attribIds[i]);
        }

        if((vertexFormat & FOG_MASK) != 0)
        {
            gl.glDisableClientState(GL.GL_FOG_COORDINATE_ARRAY);
            gl.glFogi(GL.GL_FOG_COORDINATE_SOURCE, GL.GL_FRAGMENT_DEPTH);
        }

        if((vertexFormat & COLOR2_MASK) != 0)
        {
            gl.glDisableClientState(GL.GL_SECONDARY_COLOR_ARRAY);
            gl.glSecondaryColor3f(1f,1f,1f);
        }

        if((vertexFormat & COLOR_MASK) != 0)
        {
            gl.glDisableClientState(GL.GL_COLOR_ARRAY);
            gl.glColor4f(1f,1f,1f,1f);
        }


        if((vertexFormat & TEXTURE_MASK) != 0)
        {
            // single texturing or multi-texturing
            if(numTextureSets == 1)
            {
                gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
            }
            else
            {
                for(int i = numTextureSets - 1; i >= 0; i--)
                {
                    gl.glClientActiveTexture(GL.GL_TEXTURE0 + i);
                    gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
                }
            }
        }

        if((vertexFormat & NORMALS) != 0)
            gl.glDisableClientState(GL.GL_NORMAL_ARRAY);

        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
    }

    /**
     * Update this node's bounds and then call the parent to update it's
     * bounds. Used to propogate bounds changes from the leaves of the tree
     * to the root. A node implementation may decide when and where to tell
     * the parent(s)s that updates are ready.
     */
    protected void updateBounds()
    {
        if(numCoords != 0)
            super.updateBounds();
    }

    /**
     * Internal method to recalculate the implicit bounds of this Node. By
     * default the bounds are a point sphere, so derived classes should
     * override this method with something better.
     */
    protected void recomputeBounds()
    {
        if(numCoords == 0)
            return;

        vertexBuffer.rewind();

        float min_x = vertexBuffer.get();
        float min_y = vertexBuffer.get();
        float min_z = 0;
        float min_w = 0;

        float max_x = min_x;
        float max_y = min_y;
        float max_z = 0;
        float max_w = 0;
        int cnt;

        // This block has been benchmarked and this structure is fastest(jdk 1.4.2_01-5).
        switch(vertexFormat & COORDINATE_MASK)
        {
            case 2:
                for(int i = 1; i < numCoords; i++)
                {
                    float x = vertexBuffer.get();
                    float y = vertexBuffer.get();

                    if(x < min_x)
                        min_x = y;
                    else if(x > max_x)
                        max_x = x;

                    if(y < min_y)
                        min_y = y;
                    else if(y > max_y)
                        max_y = y;
                }

                break;

            case 3:
                min_z = vertexBuffer.get();
                max_z = min_z;

                for(int i = 1; i < numCoords; i++)
                {
                    float x = vertexBuffer.get();
                    float y = vertexBuffer.get();
                    float z = vertexBuffer.get();

                    if(x < min_x)
                        min_x = y;
                    else if(x > max_x)
                        max_x = x;

                    if(y < min_y)
                        min_y = y;
                    else if(y > max_y)
                        max_y = y;

                    if(z < min_z)
                        min_z = z;
                    else if(z > max_z)
                        max_z = z;
                }

                break;

            case 4:
                min_z = vertexBuffer.get();
                max_z = min_z;
                min_w = vertexBuffer.get();
                max_w = min_w;
                cnt = 4;

                for(int i = 1; i < numCoords; i++)
                {
                    float x = vertexBuffer.get();
                    float y = vertexBuffer.get();
                    float z = vertexBuffer.get();

                    if(x < min_x)
                        min_x = y;
                    else if(x > max_x)
                        max_x = x;

                    if(y < min_y)
                        min_y = y;
                    else if(y > max_y)
                        max_y = y;

                    if(z < min_z)
                        min_z = z;
                    else if(z > max_z)
                        max_z = z;

// Don't do anything with the 4th coord for now. Read and discard
                    vertexBuffer.get();
                }

                break;
        }

        BoundingBox bbox = (BoundingBox)bounds;
        bbox.setMinimum(min_x, min_y, min_z);
        bbox.setMaximum(max_x, max_y, max_z);

        vertexBuffer.rewind();
    }

    /**
     * Initialize the internal arrays to a given size for the picking.
     *
     * @param numCoords The number of coordinates in the polygon
     */
    protected void initPolygonDetails(int numCoords)
    {
        // assign the working coords to be big enough for a triangle as
        if(working2dCoords == null)
        {
            working2dCoords = new float[numCoords * 2];
            wkPolygon = new float[numCoords * 3];
        }
    }

    /**
     * Private version of the ray - Polygon intersection test that does not
     * do any bounds checking on arrays and assumes everything is correct.
     * Allows fast calls to this method for internal use as well as more
     * expensive calls with checks for the public interfaces.
     * <p>
     * This method does not use wkPoint.
     *
     * @param origin The origin of the ray
     * @param direction The direction of the ray
     * @param length An optional length for to make the ray a segment. If
     *   the value is zero, it is ignored
     * @param dataOut The intersection coordinates and more
     * @return true if there was an intersection, false if not
     */
    protected boolean ray3DTriangleChecked(float[] origin,
                                           float[] direction,
                                           float length,
                                           float[] dataOut)
    {
        int i, j;

        float v0_x = wkPolygon[3] - wkPolygon[0];
        float v0_y = wkPolygon[4] - wkPolygon[1];
        float v0_z = wkPolygon[5] - wkPolygon[2];

        float v1_x = wkPolygon[6] - wkPolygon[3];
        float v1_y = wkPolygon[7] - wkPolygon[4];
        float v1_z = wkPolygon[8] - wkPolygon[5];

        // calculate the normal from the cross product of the two vectors
        float n_x = v0_y * v1_z - v0_z * v1_y;
        float n_y = v0_z * v1_x - v0_x * v1_z;
        float n_z = v0_x * v1_y - v0_y * v1_x;

        // degenerate polygon if the length of the normal is zero
        if(n_x * n_x + n_y * n_y + n_z * n_z == 0)
            return false;

        // normal dot direction
        float n_dot_dir =
            n_x * direction[0] + n_y * direction[1] + n_z * direction[2];

        // ray and plane parallel?
        if(n_dot_dir == 0)
            return false;

        // d dot first point of the polygon
        float d = n_x * wkPolygon[0] +
                  n_y * wkPolygon[1] +
                  n_z * wkPolygon[2];

        float n_dot_o = n_x * origin[0] + n_y * origin[1] + n_z * origin[2];

        // t = (d - N.O) / N.D
        float t = (d - n_dot_o) / n_dot_dir;

        // intersection before the origin
        if(t < 0)
            return false;

        // So we have an intersection with the plane of the polygon and the
        // segment/ray. Using the winding rule to see if inside or outside
        // First store the exact intersection point anyway, regardless of
        // whether this is an intersection or not.
        dataOut[0] = origin[0] + direction[0] * t;
        dataOut[1] = origin[1] + direction[1] * t;
        dataOut[2] = origin[2] + direction[2] * t;

        // Intersection point after the end of the segment?
        if(length != 0)
        {
            float x = origin[0] - dataOut[0];
            float y = origin[1] - dataOut[1];
            float z = origin[2] - dataOut[2];

            if((x * x + y * y + z * z) > (length * length))
                return false;
        }

        // bounds check
        // find the dominant axis to resolve to a 2 axis system
        float abs_nrm_x = (n_x >= 0) ? n_x : -n_x;
        float abs_nrm_y = (n_y >= 0) ? n_y : -n_y;
        float abs_nrm_z = (n_z >= 0) ? n_z : -n_z;

        int dom_axis;

        if(abs_nrm_x > abs_nrm_y)
            dom_axis = 0;
        else
            dom_axis = 1;

        if(dom_axis == 0)
        {
            if(abs_nrm_x < abs_nrm_z)
                dom_axis = 2;
        }
        else if(abs_nrm_y < abs_nrm_z)
        {
            dom_axis = 2;
        }

        // Map all the coordinates to the 2D plane. The u and v coordinates
        // are interleaved as u == even indicies and v = odd indicies

        // Steps 1 & 2 combined
        // 1. For NV vertices [Xn Yn Zn] where n = 0..Nv-1, project polygon
        // vertices [Xn Yn Zn] onto dominant coordinate plane (Un Vn).
        // 2. Translate (U, V) polygon so intersection point is origin from
        // (Un', Vn').
        j = 5;

        switch(dom_axis)
        {
            case 0:
                for(i = 3; --i >= 0; )
                {
                    working2dCoords[j--] = wkPolygon[i * 3 + 2] - dataOut[2];
                    working2dCoords[j--] = wkPolygon[i * 3 + 1] - dataOut[1];
                }
                break;

            case 1:
                for(i = 3; --i >= 0; )
                {
                    working2dCoords[j--] = wkPolygon[i * 3 + 2] - dataOut[2];
                    working2dCoords[j--] = wkPolygon[i * 3]     - dataOut[0];
                }
                break;

            case 2:
                for(i = 3; --i >= 0; )
                {
                    working2dCoords[j--] = wkPolygon[i * 3 + 1] - dataOut[1];
                    working2dCoords[j--] = wkPolygon[i * 3]     - dataOut[0];
                }
                break;
        }

        int sh;  // current sign holder
        int nsh; // next sign holder
        float dist;
        int crossings = 0;

        // Step 4.
        // Set sign holder as f(V' o) ( V' value of 1st vertex of 1st edge)
        if(working2dCoords[1] < 0.0)
            sh = -1;
        else
            sh = 1;

        for(i = 0; i < 3; i++)
        {
            // Step 5.
            // For each edge of polygon (Ua' V a') -> (Ub', Vb') where
            // a = 0..Nv-1 and b = (a + 1) mod Nv

            // b = (a + 1) mod Nv
            j = (i + 1) % 3;

            int i_u = i * 2;     // index of Ua'
            int j_u = j * 2;     // index of Ub'
            int i_v = i * 2 + 1;       // index of Va'
            int j_v = j * 2 + 1;       // index of Vb'

            // Set next sign holder (Nsh) as f(Vb')
            // Nsh = -1 if Vb' < 0
            // Nsh = +1 if Vb' >= 0
            if(working2dCoords[j_v] < 0.0)
                nsh = -1;
            else
                nsh = 1;

            // If Sh <> NSH then if = then edge doesn't cross +U axis so no
            // ray intersection and ignore

            if(sh != nsh)
            {
                // if Ua' > 0 and Ub' > 0 then edge crosses + U' so Nc = Nc + 1
                if((working2dCoords[i_u] > 0.0) && (working2dCoords[j_u] > 0.0))
                {
                    crossings++;
                }
                else if ((working2dCoords[i_u] > 0.0) ||
                         (working2dCoords[j_u] > 0.0))
                {
                    // if either Ua' or U b' > 0 then line might cross +U, so
                    // compute intersection of edge with U' axis
                    dist = working2dCoords[i_u] -
                           (working2dCoords[i_v] *
                            (working2dCoords[j_u] - working2dCoords[i_u])) /
                           (working2dCoords[j_v] - working2dCoords[i_v]);

                    // if intersection point is > 0 then must cross,
                    // so Nc = Nc + 1
                    if(dist > 0)
                        crossings++;
                }

                // Set SH = Nsh and process the next edge
                sh = nsh;
            }
        }

        // Step 6. If Nc odd, point inside else point outside.
        // Note that we have already stored the intersection point way back up
        // the start.
        return ((crossings % 2) == 1);
    }

    /**
     * Private version of the ray - Polygon intersection test that does not
     * do any bounds checking on arrays and assumes everything is correct.
     * Allows fast calls to this method for internal use as well as more
     * expensive calls with checks for the public interfaces.
     * <p>
     * This method does not use wkPoint.
     *
     * @param origin The origin of the ray
     * @param direction The direction of the ray
     * @param length An optional length for to make the ray a segment. If
     *   the value is zero, it is ignored
     * @param dataOut The intersection coordinates and more
     * @return true if there was an intersection, false if not
     */
    protected boolean ray3DQuadChecked(float[] origin,
                                       float[] direction,
                                       float length,
                                       float[] dataOut)
    {
        int i, j;

        float v0_x = wkPolygon[3] - wkPolygon[0];
        float v0_y = wkPolygon[4] - wkPolygon[1];
        float v0_z = wkPolygon[5] - wkPolygon[2];

        float v1_x = wkPolygon[6] - wkPolygon[3];
        float v1_y = wkPolygon[7] - wkPolygon[4];
        float v1_z = wkPolygon[8] - wkPolygon[5];

        // calculate the normal from the cross product of the two vectors
        float n_x = v0_y * v1_z - v0_z * v1_y;
        float n_y = v0_z * v1_x - v0_x * v1_z;
        float n_z = v0_x * v1_y - v0_y * v1_x;

        // degenerate polygon if the length of the normal is zero
        if(n_x * n_x + n_y * n_y + n_z * n_z == 0)
            return false;

        // normal dot direction
        float n_dot_dir =
            n_x * direction[0] + n_y * direction[1] + n_z * direction[2];

        // ray and plane parallel?
        if(n_dot_dir == 0)
            return false;

        // d dot first point of the polygon
        float d = n_x * wkPolygon[0] +
                  n_y * wkPolygon[1] +
                  n_z * wkPolygon[2];

        float n_dot_o = n_x * origin[0] + n_y * origin[1] + n_z * origin[2];

        // t = (d - N.O) / N.D
        float t = (d - n_dot_o) / n_dot_dir;

        // intersection before the origin
        if(t < 0)
            return false;

        // So we have an intersection with the plane of the polygon and the
        // segment/ray. Using the winding rule to see if inside or outside
        // First store the exact intersection point anyway, regardless of
        // whether this is an intersection or not.
        dataOut[0] = origin[0] + direction[0] * t;
        dataOut[1] = origin[1] + direction[1] * t;
        dataOut[2] = origin[2] + direction[2] * t;

        // Intersection point after the end of the segment?
        if(length != 0)
        {
            float x = origin[0] - dataOut[0];
            float y = origin[1] - dataOut[1];
            float z = origin[2] - dataOut[2];

            if((x * x + y * y + z * z) > (length * length))
                return false;
        }

        // bounds check

        // find the dominant axis to resolve to a 2 axis system
        float abs_nrm_x = (n_x >= 0) ? n_x : -n_x;
        float abs_nrm_y = (n_y >= 0) ? n_y : -n_y;
        float abs_nrm_z = (n_z >= 0) ? n_z : -n_z;

        int dom_axis;

        if(abs_nrm_x > abs_nrm_y)
            dom_axis = 0;
        else
            dom_axis = 1;

        if(dom_axis == 0)
        {
            if(abs_nrm_x < abs_nrm_z)
                dom_axis = 2;
        }
        else if(abs_nrm_y < abs_nrm_z)
        {
            dom_axis = 2;
        }

        // Map all the coordinates to the 2D plane. The u and v coordinates
        // are interleaved as u == even indicies and v = odd indicies

        // Steps 1 & 2 combined
        // 1. For NV vertices [Xn Yn Zn] where n = 0..Nv-1, project polygon
        // vertices [Xn Yn Zn] onto dominant coordinate plane (Un Vn).
        // 2. Translate (U, V) polygon so intersection point is origin from
        // (Un', Vn').
        j = 7;   // 2 * numCoords - 1

        switch(dom_axis)
        {
            case 0:
                for(i = 4; --i >= 0; )
                {
                    working2dCoords[j--] = wkPolygon[i * 3 + 2] - dataOut[2];
                    working2dCoords[j--] = wkPolygon[i * 3 + 1] - dataOut[1];
                }
                break;

            case 1:
                for(i = 4; --i >= 0; )
                {
                    working2dCoords[j--] = wkPolygon[i * 3 + 2] - dataOut[2];
                    working2dCoords[j--] = wkPolygon[i * 3]     - dataOut[0];
                }
                break;

            case 2:
                for(i = 4; --i >= 0; )
                {
                    working2dCoords[j--] = wkPolygon[i * 3 + 1] - dataOut[1];
                    working2dCoords[j--] = wkPolygon[i * 3]     - dataOut[0];
                }
                break;
        }

        int sh;  // current sign holder
        int nsh; // next sign holder
        float dist;
        int crossings = 0;

        // Step 4.
        // Set sign holder as f(V' o) ( V' value of 1st vertex of 1st edge)
        if(working2dCoords[1] < 0.0)
            sh = -1;
        else
            sh = 1;

        for(i = 0; i < 4; i++)
        {
            // Step 5.
            // For each edge of polygon (Ua' V a') -> (Ub', Vb') where
            // a = 0..Nv-1 and b = (a + 1) mod Nv

            // b = (a + 1) mod Nv
            j = (i + 1) % 4;

            int i_u = i * 2;     // index of Ua'
            int j_u = j * 2;     // index of Ub'
            int i_v = i * 2 + 1;       // index of Va'
            int j_v = j * 2 + 1;       // index of Vb'

            // Set next sign holder (Nsh) as f(Vb')
            // Nsh = -1 if Vb' < 0
            // Nsh = +1 if Vb' >= 0
            if(working2dCoords[j_v] < 0.0)
                nsh = -1;
            else
                nsh = 1;

            // If Sh <> NSH then if = then edge doesn't cross +U axis so no
            // ray intersection and ignore

            if(sh != nsh)
            {
                // if Ua' > 0 and Ub' > 0 then edge crosses + U' so Nc = Nc + 1
                if((working2dCoords[i_u] > 0.0) && (working2dCoords[j_u] > 0.0))
                {
                    crossings++;
                }
                else if ((working2dCoords[i_u] > 0.0) ||
                         (working2dCoords[j_u] > 0.0))
                {
                    // if either Ua' or U b' > 0 then line might cross +U, so
                    // compute intersection of edge with U' axis
                    dist = working2dCoords[i_u] -
                           (working2dCoords[i_v] *
                            (working2dCoords[j_u] - working2dCoords[i_u])) /
                           (working2dCoords[j_v] - working2dCoords[i_v]);

                    // if intersection point is > 0 then must cross,
                    // so Nc = Nc + 1
                    if(dist > 0)
                        crossings++;
                }

                // Set SH = Nsh and process the next edge
                sh = nsh;
            }
        }

        // Step 6. If Nc odd, point inside else point outside.
        // Note that we have already stored the intersection point way back up
        // the start.
        return ((crossings % 2) == 1);
    }

    /**
     * Convenience method used to check on the incoming attribute array and
     * check that it is big enough.
     *
     * @param size The provided size data
     * @param attribs The array data to check on
     * @throws IllegalArgumentException one of the check conditions has failed
     */
    private void checkAttribSize(int size, Buffer attribs)
        throws IllegalArgumentException
    {
        if((attribs != null) && (attribs.limit() < numCoords * size))
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(NUM_ATTRIBS_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args =
            {
                new Integer(attribs.limit()),
                new Integer(numCoords * size)
            };

            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        if(size < 1 || size > 4)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INVALID_ATTRIB_SIZE_PROP);

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
    }

    /**
     * Check the coordinate buffer has enough items specified in it for the
     * number of dimensions. If OK, return normally, otherwise throw an
     * exception.
     *
     * @param coords The set of coordinates to check on
     * @param dimesion The number of dimensions to check against
     * @throws IllegalArgumentException The number of coordinates wasn't enough
     */
    private void checkTexCoordSize(FloatBuffer coords, int dimensions)
        throws IllegalArgumentException
    {
        if(coords.limit() < numCoords * dimensions)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(TEXCOORDS_SINGLE_SIZE_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args =
            {
                new Integer(coords.limit()),
                dimensions + "D",
                new Integer(numCoords),
            };

            Format[] fmts = { n_fmt, null, n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Check the coordinate buffer has enough items specified in it for the
     * number of dimensions. If OK, return normally, otherwise throw an
     * exception.
     *
     * @param index The array index this came from, for message purposes
     * @param coords The set of coordinates to check on
     * @param dimesion The number of dimensions to check against
     * @throws IllegalArgumentException The number of coordinates wasn't enough
     */
    private void checkTexCoordSize(int index, FloatBuffer coords, int dimensions)
        throws IllegalArgumentException
    {
        if(coords.limit() < numCoords * dimensions)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(TEXCOORDS_MULTI_SIZE_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args =
            {
                new Integer(index),
                new Integer(coords.limit()),
                dimensions + "D",
                new Integer(numCoords),
            };

            Format[] fmts = { n_fmt, n_fmt, null, n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }
    }
}
