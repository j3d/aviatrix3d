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

// Local imports
import java.util.HashMap;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ByteOrder;

import javax.media.opengl.GL;

// External imports
import org.j3d.aviatrix3d.rendering.DeletableRenderable;

/**
 * Background node that renders a sky box using any or all of the 6 textures.
 * <p>
 *
 * Note that this will take any 2D texture component as the source. However,
 * if that image defines mipmapping, the background ignores it. Only the
 * first (primary) image is used to render the texture. The size of the box is
 * 2x2x2 centered at the local origin.
 * <p>
 *
 * This will set the background colour to a single colour for the entire
 * viewport. If used, this will override the setClearColor() on
 * {@link org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice}.
 *
 * @author Justin Couch
 * @version $Revision: 1.15 $
 */
public class BoxBackground extends Background
    implements DeletableRenderable
{
    /**
     * The size of the sky box - just small enough to fit inside the stock
     * view frustum dimensions.
     */
    private static final float BOX_SIZE = 1.0f;

    /** The 2D texture coordinates for the box side */
    private static final float[] BOX_TEX_COORDS =
    {
        0, 1,  0, 0,  1, 0, 1, 1
    };

    private static final float[] BACK_BOX_COORDS =
    {
         BOX_SIZE,  BOX_SIZE, BOX_SIZE,
         BOX_SIZE, -BOX_SIZE, BOX_SIZE,
        -BOX_SIZE, -BOX_SIZE, BOX_SIZE,
        -BOX_SIZE,  BOX_SIZE, BOX_SIZE,
    };

    /** Normals for the back side */
    private static final float[] BACK_BOX_NORMALS =
    {
        0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1
    };

    /** unit box front coordinates */
    private static final float[] FRONT_BOX_COORDS =
    {
        -BOX_SIZE,  BOX_SIZE, -BOX_SIZE,
        -BOX_SIZE, -BOX_SIZE, -BOX_SIZE,
         BOX_SIZE, -BOX_SIZE, -BOX_SIZE,
         BOX_SIZE,  BOX_SIZE, -BOX_SIZE
    };

    /** Normals for the front side */
    private static final float[] FRONT_BOX_NORMALS =
    {
         0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1
    };

    /** unit box left coordinates  */
    private static final float[] LEFT_BOX_COORDS =
    {
        -BOX_SIZE,  BOX_SIZE,  BOX_SIZE,
        -BOX_SIZE, -BOX_SIZE,  BOX_SIZE,
        -BOX_SIZE, -BOX_SIZE, -BOX_SIZE,
        -BOX_SIZE,  BOX_SIZE, -BOX_SIZE
    };

    /** Normals for the left side */
    private static final float[] LEFT_BOX_NORMALS =
    {
         1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0
    };

    /** unit box right coordinates  */
    private static final float[] RIGHT_BOX_COORDS =
    {
         BOX_SIZE,  BOX_SIZE, -BOX_SIZE,
         BOX_SIZE, -BOX_SIZE, -BOX_SIZE,
         BOX_SIZE, -BOX_SIZE,  BOX_SIZE,
         BOX_SIZE,  BOX_SIZE,  BOX_SIZE
    };

    /** Normals for the right side */
    private static final float[] RIGHT_BOX_NORMALS =
    {
         -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0
    };

    /** unit box top coordinates */
    private static final float[] TOP_BOX_COORDS =
    {
        -BOX_SIZE,  BOX_SIZE,  BOX_SIZE,
        -BOX_SIZE,  BOX_SIZE, -BOX_SIZE,
         BOX_SIZE,  BOX_SIZE, -BOX_SIZE,
         BOX_SIZE,  BOX_SIZE,  BOX_SIZE
    };

    /** Normals for the top side */
    private static final float[] TOP_BOX_NORMALS = {
         0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0
    };

    /** unit box bottom coordinates */
    private static final float[] BOTTOM_BOX_COORDS =
    {
        -BOX_SIZE, -BOX_SIZE, -BOX_SIZE,
        -BOX_SIZE, -BOX_SIZE,  BOX_SIZE,
         BOX_SIZE, -BOX_SIZE,  BOX_SIZE,
         BOX_SIZE, -BOX_SIZE, -BOX_SIZE
    };

    /** Normals for the bottom side */
    private static final float[] BOTTOM_BOX_NORMALS =
    {
         0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0
    };

    /** The texture belongs to the positive X axis */
    public static final int POSITIVE_X = 0;

    /** The texture belongs to the negative X axis */
    public static final int NEGATIVE_X = 1;

    /** The texture belongs to the positive Y axis */
    public static final int POSITIVE_Y = 2;

    /** The texture belongs to the negative Y axis */
    public static final int NEGATIVE_Y = 3;

    /** The texture belongs to the positive Z axis */
    public static final int POSITIVE_Z = 4;

    /** The texture belongs to the negative Z axis */
    public static final int NEGATIVE_Z = 5;

    /** The texture source for the background to use */
    private TextureComponent2D[] texture;

    /** Buffer for holding vertex data */
    private FloatBuffer[] vertexBuffer;

    /** Buffer for holding normal data */
    private FloatBuffer[] normalBuffer;

    /** Buffer for holding texture coordinate data */
    private FloatBuffer textureBuffer;

    /**
     * Flag to say that the display lists must be cleared and regenerated
     * because some state changed
     */
    private boolean stateChanged;

    /** The mapping of GL context to OpenGL texture ID*/
    private HashMap<GL, Integer>[] textureIdMap;


    /**
     * Constructs a background node for a base colour of black.
     */
    public BoxBackground()
    {
        this(null);
    }

    /**
     * Construct a background node for a user-provided colour. The colour
     * provided should have 3 or 4 elements. If 3 are provided, a fully opaque
     * background is assumed. If less than 3 elements are provided, an exception
     * is generated. If the array is null, this assumes the a default black
     * background.
     *
     * @param c The array of colours to use, or null
     * @throws IllegalArgumentException The colour array is not long enough
     */
    public BoxBackground(float[] c)
    {
        super(c);

        texture = new TextureComponent2D[6];

        stateChanged = false;
        textureIdMap = new HashMap[6];

        for(int i = 0; i < 6; i++)
            textureIdMap[i] = new HashMap<GL, Integer>();

        generateBoxGeom();
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
        for(int i = 0; i < 6; i++)
        {
            Integer t_id = (Integer)textureIdMap[i].remove(gl);
            if(t_id != null)
            {
                int tex_id_tmp[] = { t_id.intValue() };
                gl.glDeleteTextures(1, tex_id_tmp, 0);
            }
        }
    }

    //----------------------------------------------------------
    // Methods defined by BackgroundRenderable
    //----------------------------------------------------------

    /**
     * Check to see whether this shape is something that represents 2D or 3D
     * renderable background. Pure 2D backgrounds do not need transformation
     * stacks or frustums set up to render - they can blit straight to the
     * screen as needed.
     *
     * @return True if this is 2D background, false if this is 3D
     */
    public boolean is2D()
    {
        return false;
    }

    //----------------------------------------------------------
    // Methods defined by ObjectRenderable
    //----------------------------------------------------------

    /**
     * Issue ogl commands needed for this component
     *
     * @param gl The gl context to draw with
     */
    public void render(GL gl)
    {
        if(useClearColor)
        {
            gl.glClearColor(color[0], color[1], color[2], color[3]);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        }

        // Texture stuff now, if we have one.
        if(stateChanged)
        {
            for(int i = 0; i < 6; i++)
            {
                synchronized(textureIdMap[i])
                {
                    textureIdMap[i].clear();
                }
            }
            stateChanged = false;
        }


        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);

        for(int i = 0; i < 6; i++)
            renderGeom(gl, i);
    }

    /**
     * Restore all openGL state to the given drawable.
     *
     * @param gl The gl context to draw with
     */
    public void postRender(GL gl)
    {
        gl.glDisable(GL.GL_TEXTURE_2D);
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
    public int compareTo(Object o)
        throws ClassCastException
    {
        BoxBackground app = (BoxBackground)o;
        return compareTo(app);
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
        if(!(o instanceof BoxBackground))
            return false;
        else
            return equals((BoxBackground)o);
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Set the image source to be used for the background.
     *
     * @param side Which side of the box to get the texture for
     * @param srcImage The image data to use
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setTexture(int side, TextureComponent2D srcImage)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        texture[side] = srcImage;
        stateChanged = true;
    }

    /**
     * Get the current image that is being used on the background. If none is
     * defined, return null.
     *
     * @param side Which side of the box to get the texture for
     * @return The current texture source or null
     */
    public TextureComponent2D getTexture(int side)
    {
        return texture[side];
    }

    /**
     * Generate the data needed for the skysphere.
     *
     * @param facetCount Number of faces around the circumferance
     */
    private void generateBoxGeom()
    {
        // textures all share the same array, so process separately.
        textureBuffer = createBuffer(BOX_TEX_COORDS.length);
        textureBuffer.put(BOX_TEX_COORDS);
        textureBuffer.rewind();

        vertexBuffer = new FloatBuffer[6];
        normalBuffer = new FloatBuffer[6];

        for(int i = 0; i < 6; i++)
        {
            vertexBuffer[i] = createBuffer(12);
            normalBuffer[i] = createBuffer(12);
        }

        vertexBuffer[POSITIVE_X].put(RIGHT_BOX_COORDS);
        normalBuffer[POSITIVE_X].put(RIGHT_BOX_NORMALS);

        vertexBuffer[NEGATIVE_X].put(LEFT_BOX_COORDS);
        normalBuffer[NEGATIVE_X].put(LEFT_BOX_NORMALS);

        vertexBuffer[POSITIVE_Y].put(TOP_BOX_COORDS);
        normalBuffer[POSITIVE_Y].put(TOP_BOX_NORMALS);

        vertexBuffer[NEGATIVE_Y].put(BOTTOM_BOX_COORDS);
        normalBuffer[NEGATIVE_Y].put(BOTTOM_BOX_NORMALS);

        vertexBuffer[POSITIVE_Z].put(BACK_BOX_COORDS);
        normalBuffer[POSITIVE_Z].put(BACK_BOX_NORMALS);

        vertexBuffer[NEGATIVE_Z].put(FRONT_BOX_COORDS);
        normalBuffer[NEGATIVE_Z].put(FRONT_BOX_NORMALS);

        for(int i = 0; i < 6; i++)
        {
            vertexBuffer[i].rewind();
            normalBuffer[i].rewind();
        }
    }

    /**
     * Convenience method to allocate a NIO buffer for the vertex handling.
     *
     * @param size The number of floats to store
     * @return The float buffer big enough to hold the size
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
     * Render a piece of geometry now
     *
     * @param side Which side of the box to render
     * @param gl The gl context to draw with
     */
    private void renderGeom(GL gl, int side)
    {
        if(texture[side] == null)
            return;

        Integer t_id = textureIdMap[side].get(gl);
        if(t_id != null)
        {
            gl.glBindTexture(GL.GL_TEXTURE_2D, t_id.intValue());
        }
        else
        {
            // NOTE:
            // Assume, for now, that we need to regenerate the entire texture
            // because the imageComponent has changed. Once sub-image update is
            // implemented, we may want to change this logic
            int[] tex_id_tmp = new int[1];
            gl.glGenTextures(1, tex_id_tmp, 0);
            textureIdMap[side].put(gl, new Integer(tex_id_tmp[0]));

            gl.glBindTexture(GL.GL_TEXTURE_2D, tex_id_tmp[0]);

            gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
            gl.glTexParameteri(GL.GL_TEXTURE_2D,
                               GL.GL_TEXTURE_WRAP_S,
                               GL.GL_CLAMP_TO_EDGE);

            gl.glTexParameteri(GL.GL_TEXTURE_2D,
                               GL.GL_TEXTURE_WRAP_T,
                               GL.GL_CLAMP_TO_EDGE);

            gl.glTexParameteri(GL.GL_TEXTURE_2D,
                               GL.GL_TEXTURE_MAG_FILTER,
                               GL.GL_LINEAR);

            gl.glTexParameteri(GL.GL_TEXTURE_2D,
                               GL.GL_TEXTURE_MIN_FILTER,
                               GL.GL_LINEAR);

            ByteBuffer pixels = texture[side].getData(0);

            int comp_format = texture[side].getFormat(0);
            int width = texture[side].getWidth();
            int height = texture[side].getHeight();
            int int_format = GL.GL_RGB;
            int ext_format = GL.GL_RGB;

            switch(comp_format)
            {
                case TextureComponent.FORMAT_RGB:
                    int_format = GL.GL_RGB;
                    ext_format = GL.GL_RGB;
                    break;

                case TextureComponent.FORMAT_RGBA:
                    int_format = GL.GL_RGBA;
                    ext_format = GL.GL_RGBA;
                    break;

                case TextureComponent.FORMAT_BGR:
                    int_format = GL.GL_BGR;
                    ext_format = GL.GL_BGR;
                    break;

                case TextureComponent.FORMAT_BGRA:
                    int_format = GL.GL_BGRA;
                    ext_format = GL.GL_BGRA;
                    break;

                case TextureComponent.FORMAT_INTENSITY_ALPHA:
                    int_format = GL.GL_LUMINANCE_ALPHA;
                    ext_format = GL.GL_LUMINANCE_ALPHA;
                    break;

                case TextureComponent.FORMAT_SINGLE_COMPONENT:
                    int_format = GL.GL_LUMINANCE;
                    ext_format = GL.GL_LUMINANCE;
                    break;

                default:
            }

            gl.glTexImage2D(GL.GL_TEXTURE_2D,
                            0,
                            int_format,
                            width,
                            height,
                            0,
                            ext_format,
                            GL.GL_UNSIGNED_BYTE,
                            pixels);

        }

        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
        gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        gl.glVertexPointer(3, GL.GL_FLOAT, 0, vertexBuffer[side]);
        gl.glNormalPointer(GL.GL_FLOAT, 0, normalBuffer[side]);
        gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, textureBuffer);

        gl.glDrawArrays(GL.GL_QUADS, 0, 4);

        gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param bg The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(BoxBackground bg)
    {
        if(bg == null)
            return 1;

        if(bg == this)
            return 0;

        int res = compareColor4(color, bg.color);
        if(res != 0)
            return res;

        for(int i = 0; i < 6; i++)
        {
            if(texture[i] != bg.texture[i])
            {
                if(texture[i] == null)
                    return -1;
                else if(bg.texture[i] == null)
                    return 1;

                if(texture[i] != bg.texture[i])
                    return texture[i].hashCode() < bg.texture[i].hashCode() ? -1 : 1;
            }
        }

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param bg The background instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(BoxBackground bg)
    {
        if(bg == this)
            return true;

        if(bg == null)
            return false;

        if(!equalsColor4(color, bg.color))
            return false;

        for(int i = 0; i < 6; i++)
        {
            if(texture[i] != bg.texture[i])
                return false;
        }

        return true;
    }
}
