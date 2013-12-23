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
import java.nio.*;

import java.lang.reflect.Array;
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.HashMap;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.GeometryRenderable;
import org.j3d.aviatrix3d.rendering.DeletableRenderable;
import org.j3d.aviatrix3d.iutil.ShaderAttribValue;
import org.j3d.aviatrix3d.iutil.GLStateMap;
import org.j3d.util.IntHashMap;

/**
 * Common representation of all vertex-based geometry.
 * <p>
 *
 * This class represents the raw values of the geometry. How these are turned
 * into a rendered primitive by OpenGL is dependent on the derived class.
 * Internally all the geometry is stored by reference to the user provided
 * array of data, but a copy is made and placed in an instance of a
 * {@link java.nio.FloatBuffer}. The buffer is used as it is an optimisation
 * step used at the OpenGL level for faster rendering. If the user changes
 * the values in their array, we won't know about it unless the user calls the
 * appropriate setter method, at which point we'll update our array.
 * <p>
 *
 * Currently the class does not take any sort of optimisation hints. This is
 * a design task in the future. For example, we would like to hint that the
 * class should use interleaved geometry instead of a single array for each
 * data type.
 * <p>
 *
 * For the alpha flag setting, the current approach is pretty dumb. If you
 * have <i>hasAlpha</i> as true in the <code>setColor()</code> method call then
 * we blindly assume that you really do have some values that have non-one
 * transparency, and thus it will get put into the list of transparent objects
 * during transparency sorting. If you really want to avoid this, then only
 * set 3-component colour values.
 * <p>
 *
 * Geometry may have one of two forms of color. The traditional per-vertex
 * colours are available. In this mode there must be one colour value for
 * each vertex value provided. We do not allow short arrays to be provided,
 * and arrays of values longer than the current number of vertices ignore the
 * extras. In the second mode, a single colour value can be supplied that is
 * applied to all the geometry in this instance. This is useful if you want
 * to have coloured geometry rendering without the lighting effects that would
 * require the use of material properties. An advantage is that far less data
 * is pushed to the video card and less state changing too. It is particularly
 * useful for CAD-style data that only uses per-part colours with no texturing
 * and no lighting.
 * <p>
 *
 * <b>Reading Values</b>
 * <p>
 * The data stored here is kept by reference as well as the copy to the
 * internal buffers. The getter methods provided here copy the referenced
 * array into the user provided array. This can be a significant performance
 * hit. If you need to access the data and the user application is keeping
 * references to the provided arrays, there is no need to use the getter
 * methods here - just read the array directly. We make no changes, so what you
 * put in is what you would get out too.
 * <p>
 *
 * <a name="picking_flags"><b>Picking Flags</b></a>
 * <p>
 *
 * A number of flags are defined for the picking that allow the user to define
 * what they would like returned in the data array. If more than one flag is
 * specified, the order needs to be known so that sense can be made from the
 * array. The order is defined as follows:
 * <ul>
 * <li>Vertex</li>
 * <li>Normal</li>
 * <li>Colour</li>
 * <li>TexCoords in order of unit (0, 1, 2,...)</li>
 * </ul>
 *
 * If a flag value of zero is defined, then nothing is returned, but a
 * geometry-base intersection test is performed and the return value is still
 * correctly set.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>noMultitextureMsg: Video card doesn't support multitexture rendering</li>
 * <li>noVBOMsg: Video card doesn't support vertex buffer objects</li>
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
 * @version $Revision: 1.79 $
 */
public abstract class VertexGeometry extends Geometry
    implements GeometryRenderable, DeletableRenderable
{
    /** Video card can't support multitexture */
    private static final String NO_MULTITEXTURE_PROP =
        "org.j3d.aviatrix3d.VertexGeometry.noMultitextureMsg";

    /** Video card can't support VBOs */
    private static final String NO_VBO_PROP =
        "org.j3d.aviatrix3d.VertexGeometry.noVBOMsg";

    /** Attribute array is not long enough for the coords */
    private static final String NUM_ATTRIBS_PROP =
        "org.j3d.aviatrix3d.VertexGeometry.numAttribsMsg";

    /** Error message when the user-provided size is not valid */
    private static final String INVALID_ATTRIB_SIZE_PROP =
        "org.j3d.aviatrix3d.VertexGeometry.invalidAttribSizeMsg";

    /** Error message when user provides a negative vertex count */
    private static final String NEG_VTX_SIZE_PROP =
        "org.j3d.aviatrix3d.VertexGeometry.negVertexCountMsg";

    /** Error message when numValid and vertex array lengths are different */
    private static final String VTX_SIZE_MATCH_PROP =
        "org.j3d.aviatrix3d.VertexGeometry.vertexSizeMismatchMsg";

    /**
     * Error message when not enough values provided in the user array
     * for normals.
     */
    private static final String NORMALS_SIZE_PROP =
        "org.j3d.aviatrix3d.VertexGeometry.normalsSizeMsg";

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
        "org.j3d.aviatrix3d.VertexGeometry.fogCoordsSizeMsg";

    /**
     * Error message when not enough values provided in the user array
     * for secondary colour values.
     */
    private static final String SECONDARY_COLORS_SIZE_PROP =
        "org.j3d.aviatrix3d.VertexGeometry.secondaryColorsSizeMsg";

    /**
     * Error message when not enough values provided in the user array
     * for 3 component colours.
     */
    private static final String COLOR_3_SIZE_PROP =
        "org.j3d.aviatrix3d.VertexGeometry.color3SizeMsg";

    /**
     * Error message when not enough values provided in the user array
     * for 4 component colours.
     */
    private static final String COLOR_4_SIZE_PROP =
        "org.j3d.aviatrix3d.VertexGeometry.color4SizeMsg";

    /** VBO Hint for Streamed geometry */
    public static final int VBO_HINT_STREAM = GL2.GL_STREAM_DRAW;

    /** VBO Hint for Static geometry */
    public static final int VBO_HINT_STATIC = GL.GL_STATIC_DRAW;

    /** VBO Hint for Dynamic geometry */
    public static final int VBO_HINT_DYNAMIC = GL.GL_DYNAMIC_DRAW;

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

    /**
     * A single colour value is supplied in the data. This is used in
     * combination with the COLOR_3 and COLOR_4 values.
     */
    protected static final int COLOR_SINGLE = 0x4000;

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

    // Picking data output flags

    /** Bit mask to select vertex information to be returned. */
    public static final int INTERSECT_COORDS = 0x01;

    /** Bit mask to select colour information to be returned. */
    public static final int INTERSECT_COLOR = 0x02;

    /** Bit mask to select normal information to be returned. */
    public static final int INTERSECT_NORMAL = 0x04;

    /** Bit mask to select the texture coordinates of just the first unit. */
    public static final int INTERSECT_TEXCOORDS_SINGLE = 0x08;

    /** Bit mask to select all texture coordinates of all units. */
    public static final int INTERSECT_TEXCOORDS_MULTI = 0x10;

    /** Bit mask combining everything to return all information available */
    public static final int INTERSECT_ALL = 0xFFFFFFFF;


    /**
     * Global flag to know if we are capable of rendering multitextures.
     * This gets queried on the first time rendering is run and set
     * appropriately. After that, if this is set to false, then any
     * texture unit that has it's ID greater than 0 is just ignored.
     */
    protected static boolean hasMultiTextureAPI;

    /**
     * Global flag indicating the maximum number of texture units the current
     * hardware supports. This is queried at the same time as the multitexture
     * API query. Initial value is set to 16.
     */
    protected static int maxTextureUnits;

    /** Flag to say we've queried for the multitexture API capabilities */
    private static boolean queryComplete;


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

    /** Reference to the user array of coordinates used */
    protected float[] coordinates;

    /** Number of valid entries in the coordinate array */
    protected int numCoords;

    /**
     * The number of coordinates actually required as defined by the geometry
     * type. This will typically be less than numCoords as it takes into
     * account the maximum index defined in indexed geometry.
     */
    protected int numRequiredCoords;

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

    /**
     * Maximum number of renderable sets. This is the minimum of
     * numTextureSets and maxTextureUnits. Anything more than this will be
     * ignored by the implementation.
     */
    protected int numRenderedTextureSets;

    /** Reference to the user array of colors used */
    protected float[] colors;

    /** Reference to the user array of secondary colors used */
    protected float[] color2s;

    /** Reference to the user array of fog coordinates used */
    protected float[] fogCoords;

    /** Listing of the valid attribute IDs for rendering */
    protected int[] attribIds;

    /**
     * Map of the attribute Ids to their data (ref to user array). Not
     * allocated unless needed to save on memory footprint as this is a
     * relatively rare usage -  only when shaders are in use too.
     */
    protected IntHashMap attributes;

    /** The format of the geometry used */
    protected int vertexFormat;

    /** Is the gl query for VBOs done? */
    protected static boolean vboQueryComplete;

    /** Are VBO's available */
    protected static boolean vboAvailable;

    /** Should we use VBO's to store data */
    protected boolean useVbo;

    /** How are VBO's going to be used */
    protected int vboHint;

    /** Map of VBO IDs.  Only created if using VBO's */
    protected HashMap<GL, Integer> vboIdMap;

    /** State map indicating sources have changed */
    protected GLStateMap dataChanged;

    /**
     * Constructs an instance with pre-defined values with default values.
     */
    protected VertexGeometry()
    {
        this(false,VBO_HINT_STATIC);
    }

    /**
     * Constructs an instance.
     *
     * @param useVbo Should we use vertex buffer objects
     * @param vboHint Hints for how to setup VBO.  Valid values are VBO_HINT_*.
     */
    protected VertexGeometry(boolean useVbo, int vboHint)
    {
        init();

        this.useVbo = useVbo;
        this.vboHint = vboHint;

        if(useVbo)
            vboIdMap = new HashMap<GL, Integer>();
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
    @Override
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
    @Override
    public boolean is2D()
    {
        return ((vertexFormat & COORDINATE_2) != 0);
    }

    /**
     * Update this node's bounds and then call the parent to update it's
     * bounds. Used to propogate bounds changes from the leaves of the tree
     * to the root. A node implementation may decide when and where to tell
     * the parent(s)s that updates are ready.
     */
    @Override
    protected void updateBounds()
    {
        if(numRequiredCoords != 0)
            super.updateBounds();
    }

    /**
     * Internal method to recalculate the implicit bounds of this Node. By
     * default the bounds are a point sphere, so derived classes should
     * override this method with something better.
     */
    @Override
    protected void recomputeBounds()
    {
        if(numRequiredCoords == 0)
            return;

        float min_x = coordinates[0];
        float min_y = coordinates[1];
        float min_z = 0;
        float min_w = 0;

        float max_x = coordinates[0];
        float max_y = coordinates[1];
        float max_z = 0;
        float max_w = 0;
        int cnt;

        // This block has been benchmarked and this structure is fastest(jdk 1.4.2_01-5).

        switch(vertexFormat & COORDINATE_MASK)
        {
            case 2:
                cnt = 2;
                for(int i = 1; i < numRequiredCoords; i++)
                {
                    if (coordinates[cnt] < min_x)
                        min_x = coordinates[cnt];
                    if (coordinates[cnt] > max_x)
                        max_x = coordinates[cnt];

                    if (coordinates[cnt + 1] < min_y)
                        min_y = coordinates[cnt + 1];
                    if (coordinates[cnt + 1] > max_y)
                        max_y = coordinates[cnt + 1];

                    cnt = cnt + 2;
                }

                break;

            case 3:
                min_z = coordinates[2];
                max_z = coordinates[2];
                cnt = 3;

                for(int i = 1; i < numRequiredCoords; i++)
                {
                    if (coordinates[cnt] < min_x)
                        min_x = coordinates[cnt];
                    if (coordinates[cnt] > max_x)
                        max_x = coordinates[cnt];

                    if (coordinates[cnt + 1] < min_y)
                        min_y = coordinates[cnt + 1];
                    if (coordinates[cnt + 1] > max_y)
                        max_y = coordinates[cnt + 1];


                    if (coordinates[cnt + 2] < min_z)
                        min_z = coordinates[cnt + 2];
                    if (coordinates[cnt + 2] > max_z)
                        max_z = coordinates[cnt + 2];

                    cnt = cnt + 3;
                }

                break;

            case 4:
                min_z = coordinates[2];
                max_z = coordinates[2];
                min_w = coordinates[3];
                max_w = coordinates[3];
                cnt = 4;

                for(int i = 1; i < numRequiredCoords; i++)
                {
                    if (coordinates[cnt] < min_x)
                        min_x = coordinates[cnt];
                    if (coordinates[cnt] > max_x)
                        max_x = coordinates[cnt];

                    if (coordinates[cnt + 1] < min_y)
                        min_y = coordinates[cnt + 1];
                    if (coordinates[cnt + 1] > max_y)
                        max_y = coordinates[cnt + 1];


                    if (coordinates[cnt + 2] < min_z)
                        min_z = coordinates[cnt + 2];
                    if (coordinates[cnt + 2] > max_z)
                        max_z = coordinates[cnt + 2];

                    // Don't do anything with the 4th coord for now.

                    cnt = cnt + 4;
                }

                break;
        }

        BoundingBox bbox = (BoundingBox)bounds;
        bbox.setMinimum(min_x, min_y, min_z);
        bbox.setMaximum(max_x, max_y, max_z);
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
        // Ignore stuff that doesn't change the state
        if(state == alive)
            return;

        boolean old_state = alive;

        super.setLive(state);

        if(!old_state && state)
            recomputeBounds();

        if(!state && updateHandler != null && vboAvailable && useVbo)
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
        if (vboAvailable && useVbo)
        {
            Integer vbo_id = (Integer)vboIdMap.get(gl);
            if(vbo_id != null)
            {
                int[] vbo_id_tmp = { vbo_id.intValue() };
                gl.glDeleteBuffers(1, vbo_id_tmp, 0);
                vboIdMap.remove(gl);
            }
        }
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
        dataChanged.setAll(true);
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
    public void setVertices(int type, float[] vertices)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        int num_vert = (vertices == null) ? 0 : vertices.length / type;
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
    public void getVertices(float[] vertices)
    {
        int vtx_size = vertexFormat & 0x07;
        System.arraycopy(coordinates, 0, vertices, 0, numCoords * vtx_size);
    }

    /**
     * Get the vertex values at the given index. The passed in array should be
     * at least as long as the coordinate size that this geometry represents.
     *
     * @param index The index of the coordinate to get
     * @param coord The array to copy the coordinate value in to
     * @throws ArrayIndexOutOfBoundsException If the coord array is not big
     *    enough for the coordinate dimensions
     */
    public void getVertex(int index, float[] coord)
    {
        int vtx_size = vertexFormat & 0x07;

        for(int i = 0; i < vtx_size; i++)
            coord[i] = coordinates[index * vtx_size + i];
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
    public void setVertices(int type, float[] vertices, int numValid)
        throws IllegalArgumentException,
               InvalidWriteTimingException
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

        if(numValid * vtx_size > vertices.length)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(VTX_SIZE_MATCH_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args =
            {
                new Integer(vertices.length),
                new Integer(numValid * vtx_size)
            };
            Format[] fmts = { n_fmt, n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        coordinates = vertices;

        if(numValid * vtx_size > vertexBuffer.capacity())
            vertexBuffer = createBuffer(numValid * vtx_size);
        else
            vertexBuffer.clear();

        vertexBuffer.put(vertices, 0, numValid * vtx_size);
        vertexBuffer.rewind();
        vertexFormat |= type;
        dataChanged.setAll(true);
    }

    /**
     * Set a single color value to be used by all the vertices. This
     * replaces the existing vertex list array reference with the new
     * reference to a single value. Setting a value of null will clear
     * all colour information, leaving no default vertex colouring.
     * <p>
     *
     * In a live scene graph, can only be called during the data changed
     * callback.
     *
     * @param hasAlpha true if this is 4 component colour, false for 3 component
     * @param color The new colour array
     * @throws IllegalArgumentException The length of the colors array is less
     *    than the number of declared vertices
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setSingleColor(boolean hasAlpha, float[] color)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(color != null)
        {
            if(hasAlpha)
            {
                if(color.length < 4)
                {
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg_pattern = intl_mgr.getString(COLOR_4_SIZE_PROP);

                    Locale lcl = intl_mgr.getFoundLocale();

                    NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

                    Object[] msg_args = { new Integer(color.length) };
                    Format[] fmts = { n_fmt };
                    MessageFormat msg_fmt =
                        new MessageFormat(msg_pattern, lcl);
                    msg_fmt.setFormats(fmts);
                    String msg = msg_fmt.format(msg_args);

                    throw new IllegalArgumentException(msg);
                }

                vertexFormat |= COLOR_4 | COLOR_SINGLE;
                colors = new float[4];
                colors[0] = color[0];
                colors[1] = color[1];
                colors[2] = color[2];
                colors[3] = color[3];
            }
            else
            {
                if(color.length < 3)
                {
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg_pattern = intl_mgr.getString(COLOR_3_SIZE_PROP);

                    Locale lcl = intl_mgr.getFoundLocale();

                    NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

                    Object[] msg_args = { new Integer(color.length) };
                    Format[] fmts = { n_fmt };
                    MessageFormat msg_fmt =
                        new MessageFormat(msg_pattern, lcl);
                    msg_fmt.setFormats(fmts);
                    String msg = msg_fmt.format(msg_args);

                    throw new IllegalArgumentException(msg);
                }

                vertexFormat |= COLOR_3 | COLOR_SINGLE;
                colors = new float[3];
                colors[0] = color[0];
                colors[1] = color[1];
                colors[2] = color[2];
            }
        }
        else
        {
            vertexFormat &= COLOR_CLEAR;
        }

        validAlpha = hasAlpha;
        dataChanged.setAll(true);
    }

    /**
     * Set the color array reference to the new array. The number of valid
     * items is taken to be the length of the array divided by three. This
     * replaces the existing vertex list array reference with the new
     * reference. If a single colour was previously set, then it is removed
     * and replaced with the per-vertex colours. Setting a value of null will
     * clear all colour information, leaving no default vertex colouring.
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
    public void setColors(boolean hasAlpha, float[] colors)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        int num_valid = 0;

        // check based on format
        if(colors != null)
        {
            vertexFormat &= ~COLOR_SINGLE;

            if(hasAlpha)
            {
                if(colors.length < numRequiredCoords * 4)
                {
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg_pattern = intl_mgr.getString(COLOR_4_SIZE_PROP);

                    Locale lcl = intl_mgr.getFoundLocale();

                    NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

                    Object[] msg_args = { new Integer(colors.length) };
                    Format[] fmts = { n_fmt };
                    MessageFormat msg_fmt =
                        new MessageFormat(msg_pattern, lcl);
                    msg_fmt.setFormats(fmts);
                    String msg = msg_fmt.format(msg_args);

                    throw new IllegalArgumentException(msg);
                }

                vertexFormat |= COLOR_4;
                num_valid = numRequiredCoords * 4;
            }
            else
            {
                if(colors.length < numRequiredCoords * 3)
                {
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg_pattern = intl_mgr.getString(COLOR_3_SIZE_PROP);

                    Locale lcl = intl_mgr.getFoundLocale();

                    NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

                    Object[] msg_args = { new Integer(colors.length) };
                    Format[] fmts = { n_fmt };
                    MessageFormat msg_fmt =
                        new MessageFormat(msg_pattern, lcl);
                    msg_fmt.setFormats(fmts);
                    String msg = msg_fmt.format(msg_args);

                    throw new IllegalArgumentException(msg);
                }

                vertexFormat |= COLOR_3;
                num_valid = numRequiredCoords * 3;
            }
        }
        else
        {
            vertexFormat &= COLOR_CLEAR;
        }

        this.colors = colors;

        if(num_valid > colorBuffer.capacity())
            colorBuffer = createBuffer(num_valid);
        else
            colorBuffer.clear();

        if(num_valid != 0)
        {
            colorBuffer.put(colors, 0, num_valid);
            colorBuffer.rewind();
        }

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
        dataChanged.setAll(true);
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
    public void getColors(float[] col)
    {
        if(colors != null)
        {
            if((vertexFormat & COLOR_SINGLE) != 0)
            {
                col[0] = colors[0];
                col[1] = colors[1];
                col[2] = colors[2];

                if(validAlpha)
                    col[3] = colors[3];
            }
            else
            {
                int num = numRequiredCoords * (validAlpha ? 4 : 3);
                System.arraycopy(colors, 0, col, 0, num);
            }
        }
    }

    /**
     * Get the color values at the given index. The passed in array should be
     * at least as long as the color size that this geometry represents. If a
     * single colour is used, then that single colour is always returned.
     *
     * @param index The index of the coordinate to get
     * @param color The array to copy the color value in to
     * @throws ArrayIndexOutOfBoundsException If the coord array is not big
     *    enough for the coordinate dimensions
     */
    public void getColor(int index, float[] color)
    {
        if(colors != null)
        {
            if((vertexFormat & COLOR_SINGLE) != 0)
            {
                color[0] = colors[0];
                color[1] = colors[1];
                color[2] = colors[2];

                if(validAlpha)
                    color[3] = colors[3];
            }
            else
            {
                int color_size = numRequiredCoords * (validAlpha ? 4 : 3);

                for(int i = 0; i < color_size; i++)
                    color[i] = coordinates[index * color_size + i];
            }
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
    public void setNormals(float[] normals)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if((normals != null) && (normals.length < numRequiredCoords * 3))
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(NORMALS_SIZE_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args =
            {
                new Integer(normals.length),
                new Integer(numRequiredCoords * 3)
            };

            Format[] fmts = { n_fmt, n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        this.normals = normals;

        if(numRequiredCoords * 3 > normalBuffer.capacity())
            normalBuffer = createBuffer(numRequiredCoords * 3);
        else
            normalBuffer.clear();

        if(normals == null)
            vertexFormat &= NORMAL_CLEAR;
        else
        {
            normalBuffer.put(normals, 0, numRequiredCoords * 3);
            normalBuffer.rewind();
            vertexFormat |= NORMALS;
        }
        dataChanged.setAll(true);
    }

    /**
     * Retrieve the normals that are currently set. The array must be at
     * least as long as the valid vertex count, times 3. If none are set
     * currently or have been cleared, the provided array is untouched.
     *
     * @param n The array to copy the values into
     * @throws ArrayIndexOutOfBoundsException The provided array is too short
     */
    public void getNormals(float[] n)
    {
        if(normals != null)
            System.arraycopy(normals, 0, n, 0, numRequiredCoords * 3);
    }

    /**
     * Get the color values at the given index. The passed in array should be
     * at least as long as the color size that this geometry represents. If a
     * single colour is used, then that single colour is always returned.
     *
     * @param index The index of the coordinate to get
     * @param n The array to copy the normal value in to
     * @throws ArrayIndexOutOfBoundsException If the normal array is not big
     *    enough
     */
    public void getNormal(int index, float[] n)
    {
        if(normals != null)
        {
            n[0] = normals[index * 3];
            n[1] = normals[index * 3 + 1];
            n[2] = normals[index * 3 + 2];
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

        numRenderedTextureSets = (numTextureSets < maxTextureUnits) ?
                                 numTextureSets :
                                 maxTextureUnits;
        dataChanged.setAll(true);
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
                                      float[] texCoords)
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
                    if(texCoords.length < numRequiredCoords)
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

        if(texCoords.length > textureBuffer[textureSet].capacity())
            textureBuffer[textureSet] = createBuffer(texCoords.length);
        else
            textureBuffer[textureSet].clear();

        textureBuffer[textureSet].put(texCoords, 0, texCoords.length);
        textureBuffer[textureSet].rewind();
        dataChanged.setAll(true);
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
    public void setTextureCoordinates(int[] types, float[][] texCoords)
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
                                      float[][] texCoords,
                                      int numSets)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if((texCoords == null) || (numSets == 0))
        {
            vertexFormat &= TEXTURE_CLEAR;

            for(int i = 0; i < numTextureArrays; i++)
                textureBuffer[i].clear();

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

            if(textureBuffer.length < numSets)
            {
                FloatBuffer[] tmp = new FloatBuffer[numSets];
                System.arraycopy(textureBuffer, 0, tmp, 0, textureBuffer.length);
                textureBuffer = tmp;
            }

            // check the types for all valid values
            for(int i = 0; i < numSets; i++)
            {
                if((texCoords[i] == null) || (texCoords[i].length == 0))
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

                        if((textureBuffer[i] == null) ||
                           (textureBuffer[i].capacity() < numRequiredCoords))
                            textureBuffer[i] = createBuffer(numRequiredCoords);
                        else
                            textureBuffer[i].clear();
                        textureBuffer[i].put(texCoords[i], 0, numRequiredCoords);
                        break;

                    case TEXTURE_COORDINATE_2:
                        checkTexCoordSize(i, texCoords[i],  2);
						
                        num_tex_coords = numRequiredCoords * 2;

                        if((textureBuffer[i] == null) ||
                           (textureBuffer[i].capacity() < num_tex_coords))
                            textureBuffer[i] = createBuffer(num_tex_coords);
                        else
                            textureBuffer[i].clear();
                        textureBuffer[i].put(texCoords[i], 0, num_tex_coords);
                        break;

                    case TEXTURE_COORDINATE_3:
                        checkTexCoordSize(i, texCoords[i],  3);

                        num_tex_coords = numRequiredCoords * 3;

                        if((textureBuffer[i] == null) ||
                           (textureBuffer[i].capacity() < num_tex_coords))
                            textureBuffer[i] = createBuffer(num_tex_coords);
                        else
                            textureBuffer[i].clear();
                        textureBuffer[i].put(texCoords[i], 0, num_tex_coords);
                        break;

                    case TEXTURE_COORDINATE_4:
                        checkTexCoordSize(i, texCoords[i],  4);

                        num_tex_coords = numRequiredCoords * 4;

                        if((textureBuffer[i] == null) ||
                           (textureBuffer[i].capacity() < num_tex_coords))
                            textureBuffer[i] = createBuffer(num_tex_coords);
                        else
                            textureBuffer[i].clear();

                        textureBuffer[i].put(texCoords[i], 0, num_tex_coords);
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

                textureBuffer[i].rewind();
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

        numRenderedTextureSets = (numTextureSets < maxTextureUnits) ?
                                 numTextureSets :
                                 maxTextureUnits;

        textures = texCoords;
        textureTypes = types;
        dataChanged.setAll(true);
    }

    /**
     * Retrieve the texture coordinates that are currently set. The array must
     * be at least as long as the valid vertex count, times 3. If none are set
     * currently or have been cleared, the provided array is untouched.
     *
     * @param coords The array to copy the values into
     * @throws ArrayIndexOutOfBoundsException The provided array is too short
     */
    public void getTextureCoordinates(float[][] coords)
    {
        if(textures == null)
            return;

        for(int i = 0; i < numTextureSets; i++)
        {
            int num_tex_coords = 0;
            switch(textureTypes[i])
            {
                case TEXTURE_COORDINATE_1:
                    num_tex_coords = 1;
                    break;

                case TEXTURE_COORDINATE_2:
                    num_tex_coords = 2;
                    break;

                case TEXTURE_COORDINATE_3:
                    num_tex_coords = 3;
                    break;

                case TEXTURE_COORDINATE_4:
                    num_tex_coords = 4;
                    break;
            }

            System.arraycopy(textures[i],
                             0,
                             coords[i],
                             0,
                             numRequiredCoords * num_tex_coords);
        }
    }

    /**
     * Retrieve the texture coordinates that are currently set. The array must
     * be at least as long as the valid vertex count, times 3. If none are set
     * currently or have been cleared, the provided array is untouched.
     *
     * @param coords The array to copy the values into
     * @throws ArrayIndexOutOfBoundsException The provided array is too short
     */
    public void getTextureCoordinate(int index, int set, float[] coords)
    {
        if(textures == null)
            return;

        switch(textureTypes[set])
        {
            case TEXTURE_COORDINATE_1:
                coords[0] = textures[set][index];
                break;

            case TEXTURE_COORDINATE_2:
                coords[0] = textures[set][index * 2];
                coords[1] = textures[set][index * 2 + 1];
                break;

            case TEXTURE_COORDINATE_3:
                coords[0] = textures[set][index * 3];
                coords[1] = textures[set][index * 3 + 1];
                coords[2] = textures[set][index * 3 + 2];
                break;

            case TEXTURE_COORDINATE_4:
                coords[0] = textures[set][index * 4];
                coords[1] = textures[set][index * 4 + 1];
                coords[2] = textures[set][index * 4 + 2];
                coords[3] = textures[set][index * 4 + 3];
                break;
        }
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
    public void setFogCoordinates(float[] coords)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if((coords != null) && (coords.length < numRequiredCoords))
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(FOGCOORDS_SIZE_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args =
            {
                new Integer(coords.length),
                new Integer(numRequiredCoords)
            };

            Format[] fmts = { n_fmt, n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        fogCoords = coords;

        if(numRequiredCoords > fogBuffer.capacity())
            fogBuffer = createBuffer(numCoords);
        else
            fogBuffer.clear();

        if(fogCoords == null)
            vertexFormat &= FOG_CLEAR;
        else
        {
            // convert booleans to GL values.
            fogBuffer.rewind();
            fogBuffer.put(fogCoords, 0, numRequiredCoords);
            fogBuffer.rewind();

            vertexFormat |= FOG;
        }
        dataChanged.setAll(true);
    }

    /**
     * Retrieve the fog coordinates that are currently set. The array must be
     * at least as long as the valid vertex count, times 3. If none are set
     * currently or have been cleared, the provided array is untouched.
     *
     * @param fogs The array to copy the values into
     * @throws ArrayIndexOutOfBoundsException The provided array is too short
     */
    public void getFogCoordinates(float[] fogs)
    {
        if(fogCoords != null)
            System.arraycopy(fogCoords, 0, fogs, 0, numRequiredCoords);
    }

    /**
     * Retrieve the fog coordinates at the given index. The array must be
     * at least length 3. If none are set
     * currently or have been cleared, the provided array is untouched.
     *
     * @param index The index of the coordinate to get
     * @param fogs The array to copy the fog coordinate value in to
     * @throws ArrayIndexOutOfBoundsException If the normal array is not big
     *    enough
     */
    public void getFogCoordinates(int index, float[] fogs)
    {
        if(fogCoords != null)
        {
            fogs[0] = fogCoords[index * 3];
            fogs[1] = fogCoords[index * 3 + 1];
            fogs[2] = fogCoords[index * 3 + 2];
        }
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
     *   of the NodeUpdateListener data changed callback method
     */
    public void setSecondaryColors(float[] colors)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if((colors != null) && (colors.length < numRequiredCoords * 3))
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(SECONDARY_COLORS_SIZE_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args =
            {
                new Integer(colors.length),
                new Integer(numRequiredCoords * 3)
            };

            Format[] fmts = { n_fmt, n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        this.color2s = colors;

        if(numCoords * 3 > color2Buffer.capacity())
            color2Buffer = createBuffer(numRequiredCoords * 3);
        else
            color2Buffer.clear();

        if(colors == null)
            vertexFormat &= COLOR2_CLEAR;
        else
        {
            color2Buffer.put(colors, 0, numRequiredCoords * 3);
            color2Buffer.rewind();
            vertexFormat |= COLOR2;
        }
        dataChanged.setAll(true);
    }

    /**
     * Retrieve the secondary colors that are currently set. The array must be at
     * least as long as the valid vertex count, times 3. If none are set
     * currently or have been cleared, the provided array is untouched.
     *
     * @param cols The array to copy the values into
     * @throws ArrayIndexOutOfBoundsException The provided array is too short
     */
    public void getSecondaryColors(float[] cols)
    {
        if(color2s != null)
            System.arraycopy(color2s, 0, cols, 0, numRequiredCoords * 3);
    }

    /**
     * Retrieve the fog coordinates at the given index. The array must be
     * at least length 3. If none are set
     * currently or have been cleared, the provided array is untouched.
     *
     * @param index The index of the coordinate to get
     * @param cols The array to copy the color value in to
     * @throws ArrayIndexOutOfBoundsException If the normal array is not big
     *    enough
     */
    public void getSecondaryColors(int index, float[] cols)
    {
        if(color2s != null)
        {
            cols[0] = color2s[index * 3];
            cols[1] = color2s[index * 3 + 1];
            cols[2] = color2s[index * 3 + 2];
        }
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
                              float[] attribs,
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
        FloatBuffer buf;

        if(val == null)
        {
            val = new ShaderAttribValue();
            attributes.put(index, val);
        }

        val.size = size;
        val.normalise = normalise;
        val.dataType = GL.GL_FLOAT;

        if((val.data instanceof FloatBuffer) &&
           (numRequiredCoords * size < val.data.capacity()))
        {
            buf = (FloatBuffer)val.data;
            buf.clear();
        }
        else
        {
            buf = createBuffer(numRequiredCoords * size);
            val.data = buf;
        }

        buf.put(attribs, 0, numRequiredCoords * size);
        buf.rewind();
        vertexFormat |= ATTRIBS;
        dataChanged.setAll(true);
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
                              double[] attribs,
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
        DoubleBuffer buf;

        if(val == null)
        {
            val = new ShaderAttribValue();
            attributes.put(index, val);
        }

        val.size = size;
        val.normalise = normalise;
        val.dataType = GL2.GL_DOUBLE;

        if((val.data instanceof DoubleBuffer) &&
           (numRequiredCoords * size < val.data.capacity()))
        {
            buf = (DoubleBuffer)val.data;
            buf.clear();
        }
        else
        {
            ByteBuffer bbuf = ByteBuffer.allocateDirect(numRequiredCoords * size * 8);
            bbuf.order(ByteOrder.nativeOrder());
            buf = bbuf.asDoubleBuffer();

            val.data = buf;
        }

        buf.put(attribs, 0, numRequiredCoords * size);
        buf.rewind();
        vertexFormat |= ATTRIBS;
        dataChanged.setAll(true);
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
                              int[] attribs,
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
        IntBuffer buf;

        if(val == null)
        {
            val = new ShaderAttribValue();
            attributes.put(index, val);
        }

        val.size = size;
        val.normalise = normalise;
        val.dataType = signed ? GL2.GL_INT : GL.GL_UNSIGNED_INT;

        if((val.data instanceof IntBuffer) &&
           (numRequiredCoords * size < val.data.capacity()))
        {
            buf = (IntBuffer)val.data;
            buf.clear();
        }
        else
        {
            ByteBuffer bbuf = ByteBuffer.allocateDirect(numRequiredCoords * size * 4);
            bbuf.order(ByteOrder.nativeOrder());
            buf = bbuf.asIntBuffer();

            val.data = buf;
        }

        buf.put(attribs, 0, numRequiredCoords * size);
        buf.rewind();
        vertexFormat |= ATTRIBS;
        dataChanged.setAll(true);
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
                              short[] attribs,
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
        ShortBuffer buf;

        if(val == null)
        {
            val = new ShaderAttribValue();
            attributes.put(index, val);
        }

        val.size = size;
        val.normalise = normalise;
        val.dataType = signed ? GL.GL_SHORT : GL.GL_UNSIGNED_SHORT;

        if((val.data instanceof ShortBuffer) &&
           (numRequiredCoords * size < val.data.capacity()))
        {
            buf = (ShortBuffer)val.data;
            buf.clear();
        }
        else
        {
            ByteBuffer bbuf = ByteBuffer.allocateDirect(numRequiredCoords * size * 2);
            bbuf.order(ByteOrder.nativeOrder());
            buf = bbuf.asShortBuffer();

            val.data = buf;
        }

        buf.put(attribs, 0, numRequiredCoords * size);
        buf.rewind();
        vertexFormat |= ATTRIBS;
        dataChanged.setAll(true);
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
                              byte[] attribs,
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
        ByteBuffer buf;

        if(val == null)
        {
            val = new ShaderAttribValue();
            attributes.put(index, val);
        }

        val.size = size;
        val.normalise = normalise;
        val.dataType = signed ? GL.GL_BYTE : GL.GL_UNSIGNED_BYTE;

        if((val.data instanceof ByteBuffer) &&
           (numRequiredCoords * size < val.data.capacity()))
        {
            buf = (ByteBuffer)val.data;
            buf.clear();
        }
        else
        {
            buf = ByteBuffer.allocateDirect(numRequiredCoords * size);
            buf.order(ByteOrder.nativeOrder());

            val.data = buf;
        }

        buf.put(attribs, 0, numRequiredCoords * size);
        buf.rewind();
        vertexFormat |= ATTRIBS;
        dataChanged.setAll(true);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Compute the total size of vertex buffer data, used for allocating VBOs.
     * It is called by <code>setVertexStateVBO</code>, and should not be called
     * other places. <p>
     * Must be overridden in subclasses that has vertex data in addition to what
     * is in <code>VertexGeometry</code>. See <code>TriangleArray</code> for
     * examples.
     */
    protected int computeBufferSize()
    {
        int buf_size = numRequiredCoords * (vertexFormat & 0x07) * 4;

        if((vertexFormat & NORMALS) != 0)
            buf_size += numRequiredCoords * 3 * 4;

        if((vertexFormat & TEXTURE_MASK) != 0)
        {
            // single texturing or multi-texturing
            if(hasMultiTextureAPI && numTextureSets > 1)
            {
                for(int i = 0; i < numRenderedTextureSets; i++)
                {
                    int set_id = textureSets[i];
                    switch(textureTypes[set_id])
                    {
                    case TEXTURE_COORDINATE_1:
                        buf_size += numRequiredCoords * 4;
                        break;

                    case TEXTURE_COORDINATE_2:
                        buf_size += numRequiredCoords * 4 * 2;
                        break;

                    case TEXTURE_COORDINATE_3:
                        buf_size += numRequiredCoords * 4 * 3;
                        break;

                    case TEXTURE_COORDINATE_4:
                        buf_size += numRequiredCoords * 4 * 4;
                        break;
                    }
                }
            }
            else
            {
                switch(textureTypes[0])
                {
                case TEXTURE_COORDINATE_1:
                    buf_size += numRequiredCoords * 4;
                    break;

                case TEXTURE_COORDINATE_2:
                    buf_size += numRequiredCoords * 4 * 2;
                    break;

                case TEXTURE_COORDINATE_3:
                    buf_size += numRequiredCoords * 4 * 3;
                    break;

                case TEXTURE_COORDINATE_4:
                    buf_size += numRequiredCoords * 4 * 4;
                    break;
                }
            }
        }

        if((vertexFormat & COLOR_MASK) != 0 && (vertexFormat & COLOR_SINGLE) == 0)
        {
            int size = ((vertexFormat & COLOR_3) != 0) ? 3 : 4;
            buf_size += numRequiredCoords * size * 4;
        }


        if((vertexFormat & COLOR2_MASK) != 0)
        {
            buf_size += numRequiredCoords * 3 * 4;
        }

        if((vertexFormat & FOG_MASK) != 0)
        {
            buf_size += numRequiredCoords * 4;
        }

        if((vertexFormat & ATTRIBS) != 0)
        {
            int num_attribs = attributes.size();
            attribIds = attributes.keySet(attribIds);

            for(int i = 0; i < num_attribs; i++)
            {
                int id = attribIds[i];
                ShaderAttribValue val = (ShaderAttribValue)attributes.get(id);
                buf_size += numRequiredCoords * 4 * val.size;
            }
        }
        return buf_size;
    }

    /**
     * Fill VBOs with vertex buffer data. The
     * VBO must be bound and allocated with glBufferData before the method is called.
     * This method is called by <code>setVertexStateVBO</code>, and should not be called
     * other places. <p>
     * Must be overridden in subclasses that has vertex data in addition to what
     * is in <code>VertexGeometry</code>. See <code>TriangleArray</code> for
     * examples.
     */
    protected int fillBufferData(GL2 gl)
    {
        int vtx_size = vertexFormat & 0x07;
        int offset = 0;
        gl.glBufferSubData(GL.GL_ARRAY_BUFFER,
                           offset,
                           numRequiredCoords * vtx_size * 4,
                           vertexBuffer);
        offset += numRequiredCoords * vtx_size * 4;

        if((vertexFormat & NORMALS) != 0)
        {
            gl.glBufferSubData(GL.GL_ARRAY_BUFFER,
                               offset,
                               numRequiredCoords * 3 * 4,
                               normalBuffer);
            offset += numRequiredCoords * 3 * 4;
        }

        if((vertexFormat & TEXTURE_MASK) != 0)
        {
            // single texturing or multi-texturing
            if(hasMultiTextureAPI && numTextureSets > 1)
            {
                for(int i = 0; i < numRenderedTextureSets; i++)
                {
                    int set_id = textureSets[i];

                    int size = 0;
                    switch(textureTypes[set_id])
                    {
                        case TEXTURE_COORDINATE_1:
                            size = numRequiredCoords * 4;
                            break;
                        case TEXTURE_COORDINATE_2:
                            size = numRequiredCoords * 4 * 2;
                            break;

                        case TEXTURE_COORDINATE_3:
                            size = numRequiredCoords * 4 * 3;
                            break;

                        case TEXTURE_COORDINATE_4:
                            size = numRequiredCoords * 4 * 4;
                            break;
                    }
                    gl.glBufferSubData(GL.GL_ARRAY_BUFFER,
                                       offset,
                                       size,
                                       textureBuffer[set_id]);
                    offset += size;
                }
            }
            else
            {
                int size = 0;
                switch(textureTypes[0])
                {
                    case TEXTURE_COORDINATE_1:
                        size = numRequiredCoords * 4;
                        break;
                    case TEXTURE_COORDINATE_2:
                        size = numRequiredCoords * 4 * 2;
                        break;
                     case TEXTURE_COORDINATE_3:
                        size = numRequiredCoords * 4 * 3;
                        break;
                     case TEXTURE_COORDINATE_4:
                        size = numRequiredCoords * 4 * 4;
                        break;
                }
                gl.glBufferSubData(GL.GL_ARRAY_BUFFER,
                                   offset,
                                   size,
                                   textureBuffer[0]);
                offset += size;
           }
        }

        if((vertexFormat & COLOR_MASK) != 0 && (vertexFormat & COLOR_SINGLE) == 0)
        {

            int size = ((vertexFormat & COLOR_3) != 0) ? 3 : 4;
            gl.glBufferSubData(GL.GL_ARRAY_BUFFER,
                               offset,
                               numRequiredCoords * size * 4,
                               colorBuffer);
            offset += numRequiredCoords * size * 4;
        }


        if((vertexFormat & COLOR2_MASK) != 0)
        {
            gl.glBufferSubData(GL.GL_ARRAY_BUFFER,
                               offset,
                               numRequiredCoords * 3 * 4,
                               color2Buffer);
            offset += numRequiredCoords * 3 * 4;
        }

        if((vertexFormat & FOG_MASK) != 0)
        {
            gl.glBufferSubData(GL.GL_ARRAY_BUFFER,
                               offset,
                               numRequiredCoords * 4,
                               fogBuffer);
            offset += numRequiredCoords * 4;
        }

        if((vertexFormat & ATTRIBS) != 0)
        {
            int num_attribs = attributes.size();
            attribIds = attributes.keySet(attribIds);
            for(int i = 0; i < num_attribs; i++)
            {
                int id = attribIds[i];
                ShaderAttribValue val = (ShaderAttribValue)attributes.get(id);
                gl.glBufferSubData(GL.GL_ARRAY_BUFFER,
                                   offset,
                                   numRequiredCoords * 4 * val.size,
                                   val.data);
                offset += numRequiredCoords * 4 * val.size;
            }
        }

        return offset;
    }

    /**
     * Common initialization logic.
     */
    private void init()
    {
        vertexBuffer = FloatBuffer.allocate(0);
        normalBuffer = FloatBuffer.allocate(0);
        colorBuffer = FloatBuffer.allocate(0);
        fogBuffer = FloatBuffer.allocate(0);
        color2Buffer = FloatBuffer.allocate(0);

        textureBuffer = new FloatBuffer[1];
        bounds = new BoundingBox();

        numRenderedTextureSets = 0;
        maxTextureUnits = 16;

        dataChanged = new GLStateMap();
    }

    /**
     * Handles state setting when VBOs are used. Called from
     * <code>setVertexState(GL gl)</code> only!
     */
    protected void setVertexStateVBO(GL2 gl)
    {
        Integer vbo_id = vboIdMap.get(gl);
        if(vbo_id == null)
        {
            int[] vbo_id_tmp = new int[1];
            gl.glGenBuffers(1, vbo_id_tmp, 0);
            vboIdMap.put(gl, new Integer(vbo_id_tmp[0]));

            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo_id_tmp[0]);

            // Set the flag so that we update later in the method
            dataChanged.put(gl, true);
        }
        else
        {
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo_id.intValue());
        }

        if(dataChanged.getState(gl))
        {
            // Compute buffer size
            int buf_size = computeBufferSize();

            // Reserve buffer object
            gl.glBufferData(GL.GL_ARRAY_BUFFER, buf_size, (Buffer)null, vboHint);

            // Fill buffer object
            fillBufferData(gl);

            dataChanged.put(gl, false);
        }

        // Set buffer offsets
        int vtx_size = vertexFormat & 0x07;
        long offset = 0;
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glVertexPointer(vtx_size, GL.GL_FLOAT, 0, offset);
        offset += numRequiredCoords * vtx_size * 4;

        if((vertexFormat & NORMALS) != 0)
        {
            gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
            gl.glNormalPointer(GL.GL_FLOAT, 0, offset);
            offset += numRequiredCoords * 3 * 4;
        }

        if((vertexFormat & TEXTURE_MASK) != 0)
        {
            // single texturing or multi-texturing
            if(hasMultiTextureAPI && numTextureSets > 1)
            {
                for(int i = 0; i < numRenderedTextureSets; i++)
                {
                    int set_id = textureSets[i];
                    gl.glClientActiveTexture(GL.GL_TEXTURE0 + i);
                    gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
                    gl.glTexCoordPointer(textureTypes[set_id],
                                         GL.GL_FLOAT,
                                         0,
                                         offset);

                    switch(textureTypes[set_id])
                    {
                        case TEXTURE_COORDINATE_1:
                            offset += numRequiredCoords * 4;
                            break;

                        case TEXTURE_COORDINATE_2:
                            offset += numRequiredCoords * 4 * 2;
                            break;

                        case TEXTURE_COORDINATE_3:
                            offset += numRequiredCoords * 4 * 3;
                            break;

                        case TEXTURE_COORDINATE_4:
                            offset += numRequiredCoords * 4 * 4;
                            break;
                    }
                }
            }
            else
            {
                gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
                gl.glTexCoordPointer(textureTypes[0],
                                     GL.GL_FLOAT,
                                     0,
                                     offset);
                switch(textureTypes[0])
                {
                    case TEXTURE_COORDINATE_1:
                        offset += numRequiredCoords * 4;
                        break;

                    case TEXTURE_COORDINATE_2:
                        offset += numRequiredCoords * 4 * 2;
                        break;

                    case TEXTURE_COORDINATE_3:
                        offset += numRequiredCoords * 4 * 3;
                        break;

                    case TEXTURE_COORDINATE_4:
                        offset += numRequiredCoords * 4 * 4;
                        break;
                 }
            }
        }

        if((vertexFormat & COLOR_MASK) != 0)
        {
            if((vertexFormat & COLOR_SINGLE) != 0)
            {
                if((vertexFormat & COLOR_3) != 0)
                    gl.glColor3f(colors[0], colors[1], colors[2]);
                else
                    gl.glColor4f(colors[0], colors[1], colors[2], colors[3]);
            }
            else
            {
                gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
                int size = ((vertexFormat & COLOR_3) != 0) ? 3 : 4;
                gl.glColorPointer(size, GL.GL_FLOAT, 0, offset);
                offset += numRequiredCoords * size * 4;
            }
        }

        if((vertexFormat & COLOR2_MASK) != 0)
        {
            gl.glEnableClientState(GL2.GL_SECONDARY_COLOR_ARRAY);
            gl.glSecondaryColorPointer(3, GL.GL_FLOAT, 0, offset);
            offset += numRequiredCoords * 3 * 4;
        }

        if((vertexFormat & FOG_MASK) != 0)
        {
            gl.glFogi(GL2.GL_FOG_COORDINATE_SOURCE, GL2.GL_FOG_COORDINATE);
            gl.glEnableClientState(GL2.GL_FOG_COORDINATE_ARRAY);
            gl.glFogCoordPointer(GL.GL_FLOAT, 0, offset);
            offset += numRequiredCoords * 4;
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
                                            offset);
                offset += numRequiredCoords * 4 * val.size;
            }
        }
    }


    /**
     * Convenience method to pass everything to the rendering pipeline.
     * Will check for the various masks being set and only send the
     * needed data. The only call not made here is the
     * <code>glDrawArrays()</code> or equivalent.
     *
     * @param gl The gl context to draw with
     */
    protected final void setVertexState(GL2 gl)
    {
        // Test for multi texturing
        if((vertexFormat & TEXTURE_MASK) != 0 && numTextureSets != 1 && !queryComplete)
        {
            hasMultiTextureAPI = gl.isFunctionAvailable("glClientActiveTexture");

            int[] tmp = new int[1];
            gl.glGetIntegerv(GL2.GL_MAX_TEXTURE_UNITS, tmp, 0);
            maxTextureUnits = tmp[0];

            numRenderedTextureSets = (numTextureSets < maxTextureUnits) ?
                                      numTextureSets :
                                      maxTextureUnits;
            queryComplete = true;

            if(!hasMultiTextureAPI)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(NO_MULTITEXTURE_PROP);
                System.out.println(msg);
            }
        }

        // Test for VBOs
        if(!vboQueryComplete)
        {
            vboAvailable = gl.isFunctionAvailable("glBufferData") &
                gl.isFunctionAvailable("glGenBuffers");

            vboQueryComplete = true;
        }

        if(vboAvailable && useVbo)
        {
            try
            {
                setVertexStateVBO(gl);
                return;
            }
            catch(javax.media.opengl.GLException gle)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(NO_VBO_PROP);
                System.out.println(msg);

                vboAvailable = false;
            }
        }

        //  State enable first
        if((vertexFormat & NORMALS) != 0)
        {
            gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
            gl.glNormalPointer(GL.GL_FLOAT, 0, normalBuffer);
        }

        if((vertexFormat & TEXTURE_MASK) != 0)
        {
            // single texturing or multi-texturing
            if(numTextureSets == 1)
            {
                gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
                gl.glTexCoordPointer(textureTypes[0],
                                     GL.GL_FLOAT,
                                     0,
                                     textureBuffer[0]);
            }
            else
            {
                if(hasMultiTextureAPI)
                {
                    for(int i = 0; i < numRenderedTextureSets; i++)
                    {
                        int set_id = textureSets[i];
                        gl.glClientActiveTexture(GL.GL_TEXTURE0 + i);
                        gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
                        gl.glTexCoordPointer(textureTypes[set_id],
                                             GL.GL_FLOAT,
                                             0,
                                             textureBuffer[set_id]);
                    }
                }
                else
                {
                    gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
                    gl.glTexCoordPointer(textureTypes[0],
                                         GL.GL_FLOAT,
                                         0,
                                         textureBuffer[0]);
                }
            }
        }

        if((vertexFormat & COLOR_MASK) != 0)
        {
            if((vertexFormat & COLOR_SINGLE) != 0)
            {
                if((vertexFormat & COLOR_3) != 0)
                    gl.glColor3f(colors[0], colors[1], colors[2]);
                else
                    gl.glColor4f(colors[0], colors[1], colors[2], colors[3]);
            }
            else
            {
                gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
                int size = ((vertexFormat & COLOR_3) != 0) ? 3 : 4;
                gl.glColorPointer(size, GL.GL_FLOAT, 0, colorBuffer);
            }
        }

        if((vertexFormat & COLOR2_MASK) != 0)
        {
            gl.glEnableClientState(GL2.GL_SECONDARY_COLOR_ARRAY);
            gl.glSecondaryColorPointer(3, GL.GL_FLOAT, 0, color2Buffer);
        }

        if((vertexFormat & FOG_MASK) != 0)
        {
            gl.glFogi(GL2.GL_FOG_COORDINATE_SOURCE, GL2.GL_FOG_COORDINATE);
            gl.glEnableClientState(GL2.GL_FOG_COORDINATE_ARRAY);
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

        int vtx_size = vertexFormat & 0x07;
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glVertexPointer(vtx_size, GL.GL_FLOAT, 0, vertexBuffer);
    }

    /**
     * Convenience method to clear the previously set state in the rendering
     * pipeline. Should be called just after the derived class calls
     * <code>glDrawArrays()</code> or equivalent.
     *
     * @param gl The gl context to draw with
     */
    protected final void clearVertexState(GL2 gl)
    {
        if((vertexFormat & ATTRIBS) != 0)
        {
            int num_attribs = attributes.size();

            for(int i = 0; i < num_attribs; i++)
                gl.glDisableVertexAttribArrayARB(attribIds[i]);
        }

        if((vertexFormat & FOG_MASK) != 0)
        {
            gl.glDisableClientState(GL2.GL_FOG_COORDINATE_ARRAY);
            gl.glFogi(GL2.GL_FOG_COORDINATE_SOURCE, GL2.GL_FRAGMENT_DEPTH);
        }

        if((vertexFormat & COLOR2_MASK) != 0)
        {
            gl.glDisableClientState(GL2.GL_SECONDARY_COLOR_ARRAY);
            gl.glSecondaryColor3f(1f,1f,1f);
        }

        if((vertexFormat & COLOR_MASK) != 0)
        {
            if((vertexFormat & COLOR_SINGLE) == 0)
                gl.glDisableClientState(GL2.GL_COLOR_ARRAY);

            gl.glColor4f(1f,1f,1f,1f);
        }


        if((vertexFormat & TEXTURE_MASK) != 0)
        {
            // single texturing or multi-texturing
            if(numTextureSets == 1)
            {
                gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
            }
            else
            {
                if(hasMultiTextureAPI)
                {
                    for(int i = numRenderedTextureSets - 1; i >= 0; i--)
                    {
                        gl.glClientActiveTexture(GL.GL_TEXTURE0 + i);
                        gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
                    }
                }
                else
                {
                    gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
                }
            }
        }

        if((vertexFormat & NORMALS) != 0)
            gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);

        gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);

        if (vboAvailable && useVbo)
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);

    }

    /**
     * Convenience method to allocate a NIO buffer for the vertex handling that
     * handles floats.
     *
     * @param size The number of floats to have in the array
     */
    private FloatBuffer createBuffer(int size)
    {
        // Need to allocate a byte buffer 4 times the size requested because the
        // size is treated as bytes, not number of floats.
        ByteBuffer buf = ByteBuffer.allocateDirect(size * 4);
        buf.order(ByteOrder.nativeOrder());
        FloatBuffer ret_val = buf.asFloatBuffer();

        return ret_val;
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

            int i_u = i * 2;           // index of Ua'
            int j_u = j * 2;           // index of Ub'
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

            int i_u = i * 2;
            // index of Ua'
            int j_u = j * 2;
            // index of Ub'
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
     * Convenience method to check if this code has detected the prescense of
     * multitexture extensions. If none are found, this will return null.
     * However, one node instance has to have passed through the rendering
     * cycle for this to have detected it. A better option would be to make use
     * of the appropriate callbacks on the DrawableSurface APIs to detect before
     * you get to this point.
     *
     * @return true if multitexture is allowed
     */
    public boolean isMultiTextureAllowed()
    {
        return hasMultiTextureAPI;
    }

    /**
     * Request the maximum number of texture units available on this hardware.
     * This is known after the first rendering process for any texture. Prior
     * to this the value will be initialised to 16.
     *
     * @return A value greater than or equal to zero
     */
    public int numTextureUnits()
    {
        return maxTextureUnits;
    }

    /**
     * Set whether Vertex Buffer Objects are used.  This will only apply on
     * graphics hardware that supports VBO's.
     *
     * @param enabled Should VBO's be used.
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setVBOEnabled(boolean enabled)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        useVbo = enabled;

        if (vboQueryComplete && vboAvailable == false)
            return;

        if (enabled)
        {
            if (vboIdMap == null)
                vboIdMap = new HashMap<GL, Integer>();
        }

        // TODO: Should we free the allocated buffer or wait for deletion
    }

    /**
     * Get whether Vertex Buffer Objects are used.  If VBO's are not supported
     * this will return false.
     *
     * @return Should VBO's be used.
     */
    public boolean getVBOEnabled()
    {
        return useVbo;
    }

    /**
     * Set how VBO's are optimized on the graphics card.  Valid values
     * are VBO_HINT_*
     *
     * @param hint The hint
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setVBOHint(int hint)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        vboHint = hint;
    }

    /**
     * Get how VBO's are optimized on the graphics card.  Valid values
     * are GL_STREAM_*,GL_STATIC_*, GL_DYNAMIC_*.
     *
     * @return The VBO hint
     */
    public int getVBOHint()
    {
        return vboHint;
    }

    /**
     * Convenience method used to check on the incoming attribute array and
     * check that it is big enough.
     *
     * @param size The provided size data
     * @param attribs The array data to check on
     * @throws IllegalArgumentException one of the check conditions has failed
     */
    private void checkAttribSize(int size, Object attribs)
        throws IllegalArgumentException
    {
        if((attribs != null) && (Array.getLength(attribs) < numRequiredCoords * size))
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(NUM_ATTRIBS_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args =
            {
                new Integer(Array.getLength(attribs)),
                new Integer(numRequiredCoords * size)
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
     * @param dimensions The number of dimensions to check against
     * @throws IllegalArgumentException The number of coordinates wasn't enough
     */
    private void checkTexCoordSize(float[] coords, int dimensions)
        throws IllegalArgumentException
    {
        if(coords.length < numCoords * dimensions)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(TEXCOORDS_SINGLE_SIZE_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args =
            {
                new Integer(coords.length),
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
     * @param dimensions The number of dimensions to check against
     * @throws IllegalArgumentException The number of coordinates wasn't enough
     */
    private void checkTexCoordSize(int index, float[] coords, int dimensions)
        throws IllegalArgumentException
    {
        if(coords.length < numCoords * dimensions)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(TEXCOORDS_MULTI_SIZE_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args =
            {
                new Integer(index),
                new Integer(coords.length),
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
