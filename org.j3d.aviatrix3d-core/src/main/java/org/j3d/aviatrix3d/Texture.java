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

import javax.media.opengl.GL;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.DeletableRenderable;
import org.j3d.aviatrix3d.rendering.TransparentObjectRenderable;
import org.j3d.aviatrix3d.iutil.GLStateMap;
import org.j3d.aviatrix3d.iutil.TextureUpdateStateManager;

/**
 * Describes the basic textured appearance of an object.
 * <p>
 *
 * This is the base class for all texture objects used in Aviatrix3D. It does
 * not provide any functional capabilities, just a collection of the common
 * constants and state. It provides abilities based on the minimal setup for
 * a 1-dimensional texture. For settings for addtional dimensions (T and R)
 * please visit the appropriate derived class.
 * <p>
 * All textures default to the following setup during the constructor call:
 * <ul>
 * <li>Boundary Mode: Clamped</li>
 * <li>Min Filter: Fastest</li>
 * <li>Mag Filter: Fastest</li>
 * <li>Anisotropic Mode: None</li>
 * <li>Mipmap Mode: None (Base level only)</li>
 * <li>GenerateMipMap: None </li>
 * <li>GenerateMipMapHint: Fastest </li>
 * </ul>
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>invalidPriorityMsg: Error message when the priority is not in the range
 *     [0,1] or -1.</li>
 * </ul>
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.43 $
 */
public abstract class Texture extends NodeComponent
    implements DeletableRenderable,
               TransparentObjectRenderable
{
    /** When an unacceptable priority value is provided */
    private static final String INVALID_PRIORITY_PROP =
        "org.j3d.aviatrix3d.Texture.invalidPriorityMsg";

    /** When an unacceptable minFilter mode is provided */
    private static final String INVALID_MIN_FILTER_PROP =
        "org.j3d.aviatrix3d.Texture.invalidMinFilterMsg";

    /** When an unacceptable magFilter mode is provided */
    private static final String INVALID_MAG_FILTER_PROP =
        "org.j3d.aviatrix3d.Texture.invalidMagFilterMsg";

    /** Increment size for the pending update list */
    private static final int PENDING_LIST_INC = 10;

    /** MipMapMode constants - No Mip Map*/
    public static final int MODE_BASE_LEVEL = 0;

    /** MipMapMode constants - Use Mip Maps */
    public static final int MODE_MIPMAP = 1;

    /** GenerateMipMap constants - Use Mip Maps */
    public static final int GENERATE_MIPMAP = GL.GL_GENERATE_MIPMAP;

    /** GenerateMipMap Quality Hint */
    public static final int GENERATE_MIPMAP_HINT = GL.GL_GENERATE_MIPMAP_HINT;

    /** Set the mipmap generation to the don't care option */
    public static final int GENERATE_MIPMAP_DONT_CARE = GL.GL_DONT_CARE;

    /** Set the mipmap generation to the fastest option */
    public static final int GENERATE_MIPMAP_FASTEST = GL.GL_FASTEST;

    /** Set the mipmap generation to the highest quality option */
    public static final int GENERATE_MIPMAP_NICEST = GL.GL_NICEST;

    // Boundary Modes
    /** Boundary mode to repeat textures */
    public static final int BM_WRAP = GL.GL_REPEAT;

    /** Boundary mode to clamp textures */
    public static final int BM_CLAMP = GL.GL_CLAMP;

    /** Boundary mode to clamp the texture edge value without border */
    public static final int BM_CLAMP_TO_EDGE = GL.GL_CLAMP_TO_EDGE;

    /** Boundary mode to clamp the texture border colour */
    public static final int BM_CLAMP_TO_BOUNDARY = GL.GL_CLAMP_TO_BORDER;

    /** Boundary mode to use a mirror-repeat strategy */
    public static final int BM_MIRRORED_REPEAT = GL.GL_MIRRORED_REPEAT;

    // Maxification Filter Techniques

    /** Set the maginification filter to the fastest option */
    public static final int MAGFILTER_FASTEST = 0;

    /** Set the maginification filter to the highest quality option */
    public static final int MAGFILTER_NICEST = 1;

    /** Set the magnification filter to filtering using GL_NEAREST */
    public static final int MAGFILTER_BASE_LEVEL_POINT = 2;

    /** Set the maginification filter to linear filtering */
    public static final int MAGFILTER_BASE_LEVEL_LINEAR = 3;

    /** Set the maginfication filter to use the detail texture option */
    public static final int MAGFILTER_LINEAR_DETAIL = 4;

    /** Set the maginfication filter to use the detail texture's RGB values */
    public static final int MAGFILTER_LINEAR_DETAIL_RGB = 5;

    /** Set the maginfication filter to use the detail texture's alpha values */
    public static final int MAGFILTER_LINEAR_DETAIL_ALPHA = 6;

    // Minification Filter Techniques
    /** Set the minification filter to the fastest option */
    public static final int MINFILTER_FASTEST = 0;

    /** Set the mininification filter to the highest quality  option */
    public static final int MINFILTER_NICEST = 1;

    /** Set the mininification filter to filtering using GL_NEAREST */
    public static final int MINFILTER_BASE_LEVEL_POINT = 2;

    /** Set the mininification filter to linear filtering */
    public static final int MINFILTER_BASE_LEVEL_LINEAR = 3;

    /** Set the mininification filter to base point-base filtering */
    public static final int MINFILTER_MULTI_LEVEL_POINT = 4;

    /** Set the mininification filter to linear filtering */
    public static final int MINFILTER_MULTI_LEVEL_LINEAR = 5;

    // Anistropic Modes

    /** Disable anisotropic filtering */
    public static final int ANISOTROPIC_MODE_NONE = 0;

    /** Enable anisotropic filtering */
    public static final int ANISOTROPIC_MODE_SINGLE = 1;

    // Format values
    // More need to be added here later.

    /** Interpret the texture format as alpha only */
    public static final int FORMAT_ALPHA = GL.GL_ALPHA;

    /** Interpret the texture format as intensity only */
    public static final int FORMAT_INTENSITY = GL.GL_INTENSITY;

    /** Interpret the texture format as luminance only */
    public static final int FORMAT_LUMINANCE = GL.GL_LUMINANCE;

    /**
     * Interpret the texture format as intensity-alpha.
     *
     * @deprecated This is badly named and is actually setting
     *     GL_LUMINANCE_ALPHA. Use FORMAT_LUMINANCE_ALPHA
     */
    public static final int FORMAT_INTENSITY_ALPHA = GL.GL_LUMINANCE_ALPHA;

    /** Interpret the texture format as GL_LUMINANCE_ALPHA */
    public static final int FORMAT_LUMINANCE_ALPHA = GL.GL_LUMINANCE_ALPHA;

    /** Interpret the texture format as RGB */
    public static final int FORMAT_RGB = GL.GL_RGB;

    /** Interpret the texture format as RGBA */
    public static final int FORMAT_RGBA = GL.GL_RGBA;

    /** Interpret the texture format as a depth component texture */
    public static final int FORMAT_DEPTH_COMPONENT = GL.GL_DEPTH_COMPONENT;

    // Texture comparison constants

    /** The texture comparison mode is set to GL_NONE */
    public static final int COMPARE_MODE_NONE = GL.GL_NONE;

    /** The texture comparison mode is set to GL_COMPARE_R_TO_TEXTURE */
    public static final int COMPARE_MODE_R2TEX = GL.GL_COMPARE_R_TO_TEXTURE;

    /** The texture comparision function is less than or equal */
    public static final int COMPARE_FUNCTION_LEQUAL = GL.GL_LEQUAL;

    /** The texture comparision function is greater than or equal */
    public static final int COMPARE_FUNCTION_GEQUAL = GL.GL_GEQUAL;

    // Subimage update strategy constants

    /**
     * All sub image updates should be buffered until the next chance to
     * update. Best for when you are only updating small sections of the
     * screen.
     */
    public static final int UPDATE_BUFFER_ALL =
        TextureUpdateStateManager.UPDATE_BUFFER_ALL;

    /**
     * Each update should discard any previous updates recieved. Best used
     * when you know you'll only be updating one area, overwritting any earlier
     * updates - For example video texturing.
     */
    public static final int UPDATE_BUFFER_LAST =
        TextureUpdateStateManager.UPDATE_BUFFER_LAST;

    /**
     * Each update should check to see whether the area of this update overlaps
     * completely that of any other buffered updates. Useful if you're doing
     * scattered region updates where some parts may overlap but others don't.
     */
    public static final int UPDATE_DISCARD_OVERWRITES =
        TextureUpdateStateManager.UPDATE_DISCARD_OVERWRITES;



    /** The sources defined for this texture. */
    protected TextureSource[] sources;

    /** The number of valid items in the image array */
    protected int numSources;

    /** The Anisotropic Filtering Mode */
    protected int anisotropicMode;

    /** The Anisotropic Filtering Degree */
    protected float anisotropicDegree;

    /** The magnification filter */
    protected int magFilter;

    /** The minification filter */
    protected int minFilter;

    /** The boundary mode S value */
    protected int boundaryModeS;

    /** The mipMapMode */
    protected int mipMapMode;

    /** Should we generate mip maps */
    protected int generateMipMap;

    /** The quality hint for generateMipMap */
    protected int generateMipMapHint;

    /** The width of the main texture. */
    protected int width;

    /** The pixel format of the main texture image. */
    protected int format;

    /** The border colour, if set for the texture. If not set, is null. */
    protected float[] borderColor;

    /** State map indicating sources have changed */
    protected GLStateMap imageChanged;

    /**
     * Flag to say that the display lists must be cleared and regenerated
     * because some state changed
     */
    protected GLStateMap stateChanged;

    /** The priority of this texture, if set. A value of -1 if not set. */
    protected float priority;

    /** The submode if the texture type is GL_DEPTH_COMPONENT */
    protected int depthComponentMode;

    /** Texture comparison mode. Only for depth-component textures. */
    protected int compareMode;

    /** The texture comparison function. Only for depth-component textures. */
    protected int compareFunction;

    /** The GL type of the texture. */
    protected final int textureType;

    /** The mapping of GL context to OpenGL texture ID*/
    protected HashMap<GL, Integer> textureIdMap;

    /**
     * The update strategy to use for sub-image updates. Defaults to keep
     * latest.
     */
    protected int updateStrategy;

    /** Managers used to process updates from the component sub-image updates. */
    protected TextureUpdateStateManager[] updateManagers;

    /**
     * Constructs a texture with default values. The mipmap mode is set to
     * MODE_BASE_LEVEL. The update list is started at size 1, since the update
     * mode defaults to only keeping the last value.
     *
     * @param type One of the texture type constants
     */
    protected Texture(int type)
    {
        this(type, 1);
    }

    /**
     * Constructs a texture with default values. The mipmap mode is set to
     * MODE_BASE_LEVEL. The update list is started at size 1, since the update
     * mode defaults to only keeping the last value.
     *
     * @param type One of the texture type constants
     * @param numImg The number of sources to preload the internals with
     */
    protected Texture(int type, int numImg)
    {
        width = -1;
        textureType = type;
        priority = -1;

        depthComponentMode = FORMAT_LUMINANCE;
        compareMode = COMPARE_MODE_NONE;
        compareFunction = COMPARE_FUNCTION_GEQUAL;

        mipMapMode = MODE_BASE_LEVEL;
        generateMipMap = GL.GL_FALSE;
        generateMipMapHint = GENERATE_MIPMAP_DONT_CARE;

        boundaryModeS = BM_CLAMP;

        magFilter = MAGFILTER_FASTEST;
        minFilter = MINFILTER_FASTEST;

        updateStrategy = UPDATE_BUFFER_LAST;
        updateManagers = new TextureUpdateStateManager[numImg];

        for(int i = 0; i < numImg; i++)
            updateManagers[i] = new TextureUpdateStateManager(updateStrategy);

        textureIdMap = new HashMap<GL, Integer>();
        imageChanged = new GLStateMap();
        stateChanged = new GLStateMap();
    }

    //---------------------------------------------------------------
    // Methods defined by SceneGraphObject
    //---------------------------------------------------------------

    /**
     * Notification that this object is live now.
     *
     * @param state true if this should be marked as live now
     */
    protected void setLive(boolean state)
    {
        if(state)
            liveCount++;
        else if(liveCount > 0)
            liveCount--;

        if((liveCount == 0) || !alive)
        {
            super.setLive(state);

            for(int i = 0; i < numSources; i++)
                ((SceneGraphObject)sources[i]).setLive(state);

            if(!state && updateHandler != null)
                updateHandler.requestDeletion(this);
        }
    }

    /**
     * Set the scenegraph update handler for this node.  It will notify
     * all its children of the value. A null value will clear the current
     * handler.
     *
     * @param handler The instance to use as a handler
     */
    protected void setUpdateHandler(NodeUpdateHandler handler)
    {
        super.setUpdateHandler(handler);

        for(int i = 0; i < numSources; i++)
            ((SceneGraphObject)sources[i]).setUpdateHandler(updateHandler);
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
    public boolean hasTransparency()
    {
        if(numSources == 0)
            return false;

        boolean alpha_found = false;

        // Any of the following formats, this is is added
        // to the transparency collection.
        switch(format)
        {
            case FORMAT_ALPHA:
            case FORMAT_INTENSITY_ALPHA:
            case FORMAT_RGBA:
                alpha_found = true;
                break;
        }

        return alpha_found;
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
        Integer t_id = textureIdMap.get(gl);
        if(t_id != null)
        {
            int tex_id_tmp[] = { t_id.intValue() };
            gl.glDeleteTextures(1, tex_id_tmp, 0);
            textureIdMap.remove(gl);
        }
    }

    //---------------------------------------------------------------
    // Methods defined by Comparable
    //---------------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object. Derived instances
     * should override this to add texture-specific extensions.
     *
     * @param o The object to be compared
     * @return -1, 0 or 1 depending on order
     * @throws ClassCastException The specified object's type prevents it from
     *    being compared to this Object
     */
    public int compareTo(Object o)
        throws ClassCastException
    {
        Texture tex = (Texture)o;
        return compareTo(tex);
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
        if(!(o instanceof Texture))
            return false;
        else
            return equals((Texture)o);

    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Set a new collection of sources for this texture to use.
     *
     * @param mipMapMode Flag stating the type of texture mode to use
     * @param format Image format to use for grayscale sources
     * @param texSources The source data to use, single for base level
     * @param num The valid number of sources to use from the array
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setSources(int mipMapMode,
                           int format,
                           TextureSource[] texSources,
                           int num)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        // TODO: validity checking on these values
        width = (num == 0) ? -1 : texSources[0].getWidth();
        this.format = format;
        this.mipMapMode = mipMapMode;

        // remove listeners form any old sources. We can rely on the order
        // always being 0 to whatever for the level with no gaps as this
        // is the way
        for(int i = 0; i < numSources; i++)
        {
            if(sources[i] instanceof TextureComponent)
            {
                ((TextureComponent)sources[i]).removeUpdateListener(updateManagers[i]);
                updateManagers[i].clearPendingUpdates();

        // TODO: may need reference counting here
        //((SceneGraphObject)sources[i]).setLive(false);
            }
        }

        if(sources == null || sources.length < num)
        {
            sources = new TextureSource[num];
            TextureUpdateStateManager[] tmp =
                new TextureUpdateStateManager[num];

            System.arraycopy(updateManagers, 0, tmp, 0, numSources);
            updateManagers = tmp;

            for(int i = numSources; i < num; i++)
                updateManagers[i] = new TextureUpdateStateManager(updateStrategy);
        }
        else
        {
            // If the new lot is smaller in number than the old lot, clear out
            // the old ones that are still around.
            for(int i = num; i < numSources; i++)
                sources[i] = null;
        }

        System.arraycopy(texSources, 0, sources, 0, num);
        numSources = num;
        TextureComponent tc;

        for(int i = 0; i < num; i++)
        {
            if(sources[i] instanceof TextureComponent) {
                tc = (TextureComponent) sources[i];
                tc.setUpdateHandler(updateHandler);
                tc.setLive(true);
            }
        }

        imageChanged.setAll(true);

        for(int i = 0; i < numSources; i++)
        {
            if(!(sources[i] instanceof TextureComponent))
                continue;

            TextureComponent tex_comp = (TextureComponent)sources[i];

            int comp_format = tex_comp.getFormat(0);
            int tex_format = GL.GL_RGB;

            switch(comp_format)
            {
                case TextureSource.FORMAT_RGB:
                    tex_format = GL.GL_RGB;
                    break;

                case TextureSource.FORMAT_RGBA:
                    tex_format = GL.GL_RGBA;
                    break;

                case TextureSource.FORMAT_BGR:
                    tex_format = GL.GL_BGR;
                    break;

                case TextureSource.FORMAT_BGRA:
                    tex_format = GL.GL_BGRA;
                    break;

                case TextureSource.FORMAT_INTENSITY_ALPHA:
                case TextureSource.FORMAT_LUMINANCE_ALPHA:
                    tex_format = GL.GL_LUMINANCE_ALPHA;
                    break;

                case TextureSource.FORMAT_SINGLE_COMPONENT:
                    switch(format)
                    {
                        case FORMAT_INTENSITY:
                            tex_format = GL.GL_INTENSITY;
                            break;

                        case FORMAT_LUMINANCE:
                            tex_format = GL.GL_LUMINANCE;
                            break;

                        case FORMAT_ALPHA:
                            tex_format = GL.GL_ALPHA;
                    }
                    break;

                default:
            }

            updateManagers[i].setTextureFormat(tex_format);
            tex_comp.addUpdateListener(updateManagers[i]);
        }
    }

    /**
     * Get the texture type. This returns one of the GL constant types that
     * represent the texture type - 1D, 2D, etc.
     *
     * @return The type constant of this texture
     */
    public int getTextureType()
    {
        return textureType;
    }

    /**
     * Get the format for this texture.
     *
     * @return The format.
     */
    public int getFormat()
    {
        return format;
    }

    /**
     * Get the width of the texture in pixels. If no image is set, this returns
     * -1.
     *
     * @return a number >= -1
     */
    public int getWidth()
    {
        return width;
    }

    /**
     * Set the aniostropic filtering mode.
     *
     * @param mode The new mode.
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setAnisotropicFilterMode(int mode)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        anisotropicMode = mode;
        stateChanged.setAll(true);
    }

    /**
     * Get the current aniostropic filtering mode.
     *
     * @return The current mode.
     */
    public int getAnisotropicFilterMode()
    {
        return anisotropicMode;
    }

    /**
     * Set the anisotropic filtering degree.  Values greater
     * then the hardware supports will be clamped.
     *
     * @param degree The filtering degree.  1.0 is the default.
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setAnisotropicFilterDegree(float degree)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        anisotropicDegree = degree;

        // TODO: Need to clamp to hardware max
        stateChanged.setAll(true);
    }

    /**
     * Get the current anisotropic filtering degree. The value returned will
     * be the clamped maximum for the hardware support.
     *
     * @return The filtering degree.  1.0 is the default.
     */
    public float getAnisotropicFilterDegree()
    {
        return anisotropicDegree;
    }

    /**
     * Set the generateMipMap state.  This will only effect new texture
     * changes.
     *
     * @param generate Whether to generate mip maps
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setGenerateMipMap(boolean generate)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        generateMipMap = generate ? GL.GL_TRUE : GL.GL_FALSE;

        stateChanged.setAll(true);
    }

    /**
     * Get the current generateMipMap state.
     *
     * @return The generateMipMap state.  The default is false.
     */
    public boolean getGenerateMipMap()
    {
        return (generateMipMap == GL.GL_TRUE ? true : false);
    }

    /**
     * Set the generateMipMapHint value.  This will only effect new texture
     * changes.
     *
     * @param hint Hint on the quality of automatic mipmap generation
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setGenerateMipMapHint(int hint)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        generateMipMapHint = hint;

        stateChanged.setAll(true);
    }

    /**
     * Get the current generateMipMapHint value.
     *
     * @return The generateMipMapHint value.
     */
    public int getGenerateMipMapHint()
    {
        return generateMipMapHint;
    }

    /**
     * Set the magnification filtering mode.
     *
     * @param mode The new mode.
     */
    public void setMagFilter(int mode)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        switch(mode)
        {
            case MAGFILTER_FASTEST:
            case MAGFILTER_BASE_LEVEL_POINT:
            case MAGFILTER_NICEST:
            case MAGFILTER_BASE_LEVEL_LINEAR:
                break;

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(INVALID_MAG_FILTER_PROP);
                Locale lcl = intl_mgr.getFoundLocale();
                NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

                Object[] msg_args = { new Integer(mode) };
                Format[] fmts = { n_fmt };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                msg_fmt.setFormats(fmts);
                String msg = msg_fmt.format(msg_args);

                throw new IllegalArgumentException(msg);
        }

        magFilter = mode;
        stateChanged.setAll(true);
    }

    /**
     * Get the magnification filtering mode.
     *
     * @return The current mode.
     */
    public int getMagFilter()
    {
        return magFilter;
    }

    /**
     * Set the magnification filtering mode.
     *
     * @param mode The new mode.
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setMinFilter(int mode)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        switch(mode)
        {
            case MINFILTER_FASTEST:
            case MINFILTER_BASE_LEVEL_POINT:
            case MINFILTER_BASE_LEVEL_LINEAR:
            case MINFILTER_MULTI_LEVEL_LINEAR:
            case MINFILTER_MULTI_LEVEL_POINT:
            case MINFILTER_NICEST:
                break;

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(INVALID_MIN_FILTER_PROP);
                Locale lcl = intl_mgr.getFoundLocale();
                NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

                Object[] msg_args = { new Integer(mode) };
                Format[] fmts = { n_fmt };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                msg_fmt.setFormats(fmts);
                String msg = msg_fmt.format(msg_args);

                throw new IllegalArgumentException(msg);
        }

        minFilter = mode;
        stateChanged.setAll(true);
    }

    /**
     * Get the minification filtering mode.
     *
     * @return The current mode.
     */
    public int getMinFilter()
    {
        return minFilter;
    }

    /**
     * Set the boundary handling for the S parameter.
     *
     * @param mode The new mode.
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setBoundaryModeS(int mode)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        boundaryModeS = mode;
        stateChanged.setAll(true);
    }

    /**
     * Get the current boundary handling for the S parameter.
     *
     * @return The current mode.
     */
    public int getBoundaryModeS()
    {
        return boundaryModeS;
    }

    /**
     * Set the border color to the new value for the front-face and combined
     * values.
     *
     * @param col The new colour to use
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setBorderColor(float[] col)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(borderColor == null)
            borderColor = new float[4];

        borderColor[0] = col[0];
        borderColor[1] = col[1];
        borderColor[2] = col[2];

        if(col.length > 3)
            this.borderColor[3] = col[3];
        else
            this.borderColor[3] = 1;

        stateChanged.setAll(true);
    }

    /**
     * Get the current value of the border color for the texture, if set.
     * The array should be at least length 3 or 4. If not set, the array is
     * set to 0, 0, 0, 0.
     *
     * @param col The array to copy the values into.
     */
    public void getBorderColor(float[] col)
    {
        if(borderColor == null)
        {
            col[0] = 0;
            col[1] = 0;
            col[2] = 0;

            if(col.length > 3)
                col[3] = 0;
        }
        else
        {
            col[0] = borderColor[0];
            col[1] = borderColor[1];
            col[2] = borderColor[2];

            if(col.length > 3)
                col[3] = borderColor[3];
        }
    }

    /**
     * Set the format for the depth texture to be applied to an object. This is only
     * used when the primary format is a depth component texture.
     *
     * @param format One of luminance, intensity or alpha settings
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setDepthFormat(int format)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        depthComponentMode = format;
        stateChanged.setAll(true);
    }

    /**
     * Get the format for the depth texture .
     *
     * @return One of luminance, intensity or alpha settings
     */
    public int getDepthFormat()
    {
        return depthComponentMode;
    }

    /**
     * Set the texture comparison mode. This is only used when the texture is a
     * depth component texture.
     *
     * @param mode One of COMPARE_MODE_NONE or COMPARE_MODE_R2TEX
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setCompareMode(int mode)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        compareMode = mode;
        stateChanged.setAll(true);
    }

    /**
     * Get the current texture comparison mode.
     *
     * @return One of COMPARE_MODE_NONE or COMPARE_MODE_R2TEX
     */
    public int getCompareMode()
    {
        return compareMode;
    }

    /**
     * Set the texture comparison function. This is only used when the texture is a
     * depth component texture.
     *
     * @param func One of COMPARE_MODE_NONE or COMPARE_MODE_R2TEX
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setCompareFunction(int func)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        compareFunction = func;
        stateChanged.setAll(true);
    }

    /**
     * Get the current texture comparison functoin.
     *
     * @return One of COMPARE_MODE_NONE or COMPARE_MODE_R2TEX
     */
    public int getCompareFunction()
    {
        return compareFunction;
    }

    /**
     * Set the update strategy in use for working with sub image updates of the
     * components.
     *
     * @param strategy one of the UPDATE_ identifiers
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setUpdateStrategy(int strategy)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        updateStrategy = strategy;

        // tell all the existing listeners about the change
        for(int i = 0; i < numSources; i++)
            updateManagers[i].setUpdateStrategy(strategy);
    }

    /**
     * Get the current update strategy in use.
     *
     * @return The current strategy type constant of UPDATE_.
     */
    public int getUpdateStrategy()
    {
        return updateStrategy;
    }

    /**
     * Set the texture priority value. Priorities range from 0.0 to 1.0. The
     * default value if no priority is to be assigned is -1.
     *
     * @param pri A value between 0 and 1 or -1
     * @throws IllegalArgumentException The priority is outside the range
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setPriority(float pri)
        throws InvalidWriteTimingException, IllegalArgumentException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(pri != -1 && (pri < 0 || pri > 1))
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INVALID_PRIORITY_PROP);
            Locale lcl = intl_mgr.getFoundLocale();
            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Float(pri) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        priority = pri;

        // TODO: Need to clamp to hardware max
        stateChanged.setAll(true);
    }

    /**
     * Get the currently set priority value for the texture. This will be a
     * value between 0 and 1 or -1 if no priority is to be explicitly assigned.
     *
     * @return The current priority setting
     */
    public float getPriority()
    {
        return priority;
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object. Derived instances
     * should override this to add texture-specific extensions.
     *
     * @param tex The texture instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(Texture tex)
    {
        if(tex == null)
            return 1;

        if(tex == this)
            return 0;

        if(textureType != tex.textureType)
            return textureType < tex.textureType ? -1 : 1;

        if(minFilter != tex.minFilter)
            return minFilter < tex.minFilter ? -1 : 1;

        if(magFilter != tex.magFilter)
            return magFilter < tex.magFilter ? -1 : 1;

        if(mipMapMode != tex.mipMapMode)
            return mipMapMode < tex.mipMapMode ? -1 : 1;

        if(generateMipMap != tex.generateMipMap)
            return generateMipMap < tex.generateMipMap ? -1 : 1;

        if(generateMipMapHint != tex.generateMipMapHint)
            return generateMipMapHint < tex.generateMipMapHint ? -1 : 1;

        if(anisotropicMode != tex.anisotropicMode)
            return anisotropicMode < tex.anisotropicMode ? -1 : 1;

        if(anisotropicDegree != tex.anisotropicDegree)
            return anisotropicDegree < tex.anisotropicDegree ? -1 : 1;

        if(format != tex.format)
            return format < tex.format ? -1 : 1;

        if(width != tex.width)
            return width < tex.width ? -1 : 1;

        // compare order is changed here because we want high-priority textures
        // to be earlier in the texture listing than lower priority items.
        if(priority != tex.priority)
            return priority > tex.priority ? -1 : 1;

        if(format == FORMAT_DEPTH_COMPONENT)
        {
            if(depthComponentMode != tex.depthComponentMode)
                return depthComponentMode < tex.depthComponentMode ? -1 : 1;

            if(compareMode != tex.compareMode)
                return compareMode < tex.compareMode ? -1 : 1;

            if(compareFunction != tex.compareFunction)
                return compareFunction < tex.compareFunction ? -1 : 1;
        }

        if(borderColor != tex.borderColor)
        {
            if(borderColor == null)
                return -1;
            else if(tex.borderColor == null)
                return 1;

            int res = compareColor4(borderColor, tex.borderColor);
            if(res != 0)
                return res;
        }

        if(numSources != tex.numSources)
            return numSources < tex.numSources ? -1 : 1;
        else
        {
            for(int i = 0; i < numSources; i++)
            {
                if(sources[i] != tex.sources[i])
                    return sources[i].hashCode() < tex.sources[i].hashCode() ? -1 : 1;
            }
        }

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param tex The texture instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(Texture tex)
    {
        if(tex == this)
            return true;

        // NOTE:
        // Ignore any of the pending updates in the list. All we care about is
        // whether the source sources are the same instance. If they are not,
        // then automatically assume that these represent different textures.
        // It's waaay to costly to go through and check all the values
        // individually for every byte of the source data.

        if((tex == null) ||
           (textureType != tex.textureType) ||
           (minFilter != tex.minFilter) ||
           (magFilter != tex.magFilter) ||
           (mipMapMode != tex.mipMapMode) ||
           (anisotropicMode != tex.anisotropicMode) ||
           (anisotropicDegree != tex.anisotropicDegree) ||
           (format != tex.format) ||
           (width != tex.width) ||
           (numSources != tex.numSources) ||
           (priority != tex.priority))
            return false;

        if((format == FORMAT_DEPTH_COMPONENT) &&
           ((depthComponentMode != tex.depthComponentMode) ||
            (compareMode != tex.compareMode) ||
            (compareFunction != tex.compareFunction)))
            return false;

        if(((borderColor != tex.borderColor) &&
            (borderColor == null) || (tex.borderColor == null)) ||
           ((borderColor != null) &&
            !equalsColor4(borderColor, tex.borderColor)))
            return false;

        for(int i = 0; i < numSources; i++)
        {
            if(sources[i] != tex.sources[i])
                return false;
        }

        return true;
    }

    /**
     * Internal check to see if this texture has valid data. If it
     * doesn't then there is no point actually rendering it. Validity is
     * defined by whether there is a non-zero number of sources.
     *
     * @return true if the texture is valid for rendering
     */
    boolean hasValidData()
    {
        return (numSources != 0);
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
