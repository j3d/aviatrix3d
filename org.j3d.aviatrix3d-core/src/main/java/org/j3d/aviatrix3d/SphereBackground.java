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
 * Background node that renders a single user-provided texture on a sphere.
 * <p>
 *
 * The background geometry is created internally to be a sphere. The user's
 * texture is applied to the sphere such that the seam is located in the
 * direction of the +Z axis (ie behind the viewer from the default view
 * direction along the -Z axis). The top of the texture image is located
 * at the +Y direction. If the texture contains some transparent section(s)
 * then the clear colour can be used as well.
 * <p>
 *
 * Note that this will take any 2D texture component as the source. However,
 * if that image defines mipmapping, the background ignores it. Only the
 * first (primary) image is used to render the texture.
 * <p>
 *
 * This will set the background colour to a single colour for the entire
 * viewport. If used, this will override the setClearColor() on
 * {@link org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice}.
 *
 * @author Justin Couch
 * @version $Revision: 1.13 $
 */
public class SphereBackground extends Background
    implements DeletableRenderable
{
    /** Default number of facets to use */
    private static final int DEFAULT_NUM_FACETS = 32;

    /** The texture source for the background to use */
    private TextureComponent2D texture;

    /** The width of the main texture. */
    protected int width;

    /** The height of the main texture. */
    protected int height;

    /** Length of each strip */
    private int stripLength;

    /** Number of strips */
    private int numStrips;

    /** Buffer for holding vertex data */
    private FloatBuffer vertexBuffer;

    /** Buffer for holding normal data */
    private FloatBuffer normalBuffer;

    /** Buffer for holding texture coordinate data */
    private FloatBuffer textureBuffer;

    /**
     * Flag to say that the display lists must be cleared and regenerated
     * because some state changed
     */
    protected boolean stateChanged;

    /** The mapping of GL context to OpenGL texture ID*/
    protected HashMap<GL, Integer> textureIdMap;


    /**
     * Constructs a background node for a base colour of black.
     */
    public SphereBackground()
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
    public SphereBackground(float[] c)
    {
        super(c);

        stateChanged = false;
        textureIdMap = new HashMap<GL, Integer>();

        generateSphereGeom(DEFAULT_NUM_FACETS);
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
        Integer t_id = (Integer)textureIdMap.get(gl);
        if(t_id != null)
        {
            int tex_id_tmp[] = { t_id.intValue() };
            gl.glDeleteTextures(1, tex_id_tmp, 0);
            textureIdMap.remove(gl);
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

        if(texture == null)
            return;

        // Texture stuff now, if we have one.
        if(stateChanged)
        {
            synchronized(textureIdMap)
            {
                textureIdMap.clear();
                stateChanged = false;
            }
        }

        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);

        Integer t_id = textureIdMap.get(gl);
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
            textureIdMap.put(gl, new Integer(tex_id_tmp[0]));

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

            ByteBuffer pixels = texture.getData(0);

            int comp_format = texture.getFormat(0);
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
        gl.glVertexPointer(3, GL.GL_FLOAT, 0, vertexBuffer);
        gl.glNormalPointer(GL.GL_FLOAT, 0, normalBuffer);
        gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, textureBuffer);

        int strip_offset = 0;
        for(int i = 0; i < numStrips; i++)
        {
            gl.glDrawArrays(GL.GL_TRIANGLE_STRIP,
                            strip_offset,
                            stripLength);

            strip_offset += stripLength;
        }

        gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
    }

    /**
     * Restore all openGL state to the given drawable.
     *
     * @param gl The gl context to draw with
     */
    public void postRender(GL gl)
    {
        if(texture == null)
            return;

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
        SphereBackground app = (SphereBackground)o;
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
        if(!(o instanceof SphereBackground))
            return false;
        else
            return equals((SphereBackground)o);
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Change the colour to the new colour. Colour takes RGBA.
     *
     * @param c The colour to copy in
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setColor(float[] c)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        color[0] = c[0];
        color[1] = c[1];
        color[2] = c[2];
        color[3] = c[3];
    }

    /**
     * Change the colour to the new colour. Colour takes RGBA.
     *
     * @param r The red colour component to use
     * @param g The green colour component to use
     * @param b The blue colour component to use
     * @param a The alpha colour component to use
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setColor(float r, float g, float b, float a)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        color[0] = r;
        color[1] = g;
        color[2] = b;
        color[3] = a;
    }

    /**
     * Get the current drawing colour
     *
     * @param c An array of length 4 or more to copy the colour to
     */
    public void getColor(float[] c)
    {
        c[0] = color[0];
        c[1] = color[1];
        c[2] = color[2];
        c[3] = color[3];
    }

    /**
     * Set the image source to be used for the background. If the texture is a
     * single component only, then a luminance texture format is used. Alpha-
     * only textures aren't really useful.
     *
     * @param srcImage The image data to use
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setTexture(TextureComponent2D srcImage)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        // TODO: validity checking on these values
        if(srcImage == null)
        {
            width = -1;
            height = -1;
        }
        else
        {
            width = srcImage.getWidth();
            height = srcImage.getHeight();
        }

        texture = srcImage;
        stateChanged = true;
    }

    /**
     * Get the current image that is being used on the background. If none is
     * defined, return null.
     *
     * @return The current texture source or null
     */
    public TextureComponent2D getTexture()
    {
        return texture;
    }

    /**
     * Generate the data needed for the skysphere.
     *
     * @param facetCount Number of faces around the circumferance
     */
    private void generateSphereGeom(int facetCount)
    {
        stripLength = (facetCount + 1) << 1;
        numStrips = facetCount >> 1;
        int vtx_count = (facetCount + 1) * facetCount;

        vertexBuffer = createBuffer(vtx_count * 3);
        normalBuffer = createBuffer(vtx_count * 3);
        textureBuffer = createBuffer(vtx_count * 2);

        // local constant to make math calcs faster
        double segment_angle = 2.0 * Math.PI / facetCount;
        float tex_angle = 1 / (float)facetCount;
        float[] cos_table;
        float[] sin_table;
        float[] s_table = new float[facetCount];

        cos_table = new float[facetCount];
        sin_table = new float[facetCount];

        for(int i = 0; i < facetCount; i++)
        {
            cos_table[i] = (float)Math.cos(segment_angle * i);
            sin_table[i] = (float)Math.sin(segment_angle * i);

            s_table[i] = i * tex_angle;
        }

        // Start at the top and work our way to the bottom. Top to bottom on
        // the outer loop, one strip all the way around on the inner.
        int half_face = facetCount / 2;
        float x, y, z;

        for(int i = 0; i < half_face; i++)
        {
            float y_top = (float)(Math.cos(segment_angle * i));
            float yr_top = (float)(Math.sin(segment_angle * i));
            float y_low = (float)(Math.cos(segment_angle * (i + 1)));
            float yr_low = (float)(Math.sin(segment_angle * (i + 1)));

            for(int j = 0; j < facetCount; j++)
            {
                x = -sin_table[j] * yr_top;
                y = y_top;
                z = cos_table[j] * yr_top;

                vertexBuffer.put(x);
                vertexBuffer.put(y);
                vertexBuffer.put(z);

                normalBuffer.put(-x);
                normalBuffer.put(-y);
                normalBuffer.put(-z);

                textureBuffer.put(j * tex_angle);
                textureBuffer.put((y + 1) * 0.5f);

                x = -sin_table[j] * yr_low;
                y = y_low;
                z = cos_table[j] * yr_low;

                vertexBuffer.put(x);
                vertexBuffer.put(y);
                vertexBuffer.put(z);

                normalBuffer.put(-x);
                normalBuffer.put(-y);
                normalBuffer.put(-z);

                textureBuffer.put(j * tex_angle);
                textureBuffer.put((y + 1) * 0.5f);
            }

            x = -sin_table[0] * yr_top;
            y = y_top;
            z = cos_table[0] * yr_top;

            vertexBuffer.put(x);
            vertexBuffer.put(y);
            vertexBuffer.put(z);

            normalBuffer.put(-x);
            normalBuffer.put(-y);
            normalBuffer.put(-z);

            textureBuffer.put(1);
            textureBuffer.put((y + 1) * 0.5f);

            x = -sin_table[0] * yr_low;
            y = y_low;
            z = cos_table[0] * yr_low;

            vertexBuffer.put(x);
            vertexBuffer.put(y);
            vertexBuffer.put(z);

            normalBuffer.put(-x);
            normalBuffer.put(-y);
            normalBuffer.put(-z);

            textureBuffer.put(1);
            textureBuffer.put((y + 1) * 0.5f);
        }

        vertexBuffer.rewind();
        normalBuffer.rewind();
        textureBuffer.rewind();
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
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param bg The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(SphereBackground bg)
    {
        if(bg == null)
            return 1;

        if(bg == this)
            return 0;

        int res = compareColor4(color, bg.color);
        if(res != 0)
            return res;

        if(texture != bg.texture)
        {
            if(texture == null)
                return -1;
            else if(bg.texture == null)
                return 1;

            if(texture != bg.texture)
                return texture.hashCode() < bg.texture.hashCode() ? -1 : 1;
        }

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param bg The background instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(SphereBackground bg)
    {
        if(bg == this)
            return true;

        if(bg == null)
            return false;

        if(!equalsColor4(color, bg.color))
            return false;

        if(texture != bg.texture)
            return false;

        return true;
    }
}
