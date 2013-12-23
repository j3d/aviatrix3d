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

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.*;

/**
 * Describes the texture that is rendered to an offscreen buffer with the ability
 * to have multiple render targets.
 * <p>
 *
 * Due to the requirements for OpenGL 2.0, this will only work with frame buffer
 * objects and not Pbuffers.
 *
 * <b>Usage:</b>
 *
 * <p>
 * Since the format information is provided as part of the GLCapabilities
 * instance, normally there is no need for it to be passed into the constructor
 * like the other texture types do. The logic for determing the format is as
 * follows using the depth bit values:
 * <p>
 * <pre>
 *   if the alpha is non-zero
 *       if blue is non zero
 *           format = RGBA
 *       else if red is non-zero
 *           format = intensity-alpha
 *       else
 *           format = alpha
 *   else
 *       if blue is non-zero
 *           format = RGB
 *       else
 *           format = intensity
 * </pre>
 *
 * If this logic does not cover what you wish the texture to be, then there is
 * an overloaded constructor that can take the explicit format definition that
 * you wish to use. Note that if you wish to create a depth texture, this does
 * not cover that case and you need to explicitly set the format to
 * {@link #FORMAT_DEPTH_COMPONENT}.
 * <p>
 * Since an offscreen rendering process has a completely different set of viewing
 * parameters to the main scene, we have to include almost everything here as
 * well. A complete viewing environment must be set up to deal with the texture
 * rendering.
 * <p>
 * <b>Note:</b>
 * <p>
 * The current implementation does not work if you use this when using the null
 * sort stage. A further limitation is that if this is used as part of a
 * background (eg {@link ShapeBackground}) it will not be rendered correctly.
 *
 * <p>
 * TODO:<br>
 * If the scene is set but without root geometry, then the root geometry added
 * later, then the update handler is not correctly dealt with. It never gets
 * set. To overcome this, make sure you set a root group node before setting the
 * scene, even if it is just a proxy value.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>capsReqdMsg: Error message when attempting to construct an instance of
 *     this object without GLCapabilities.</li>
 * <li>nullLayersArrayMsg: Error message when the (internal) caller tries to
 *     call getParents() with a null array reference.</li>
 * <li>invalidNumTargetsMsg: The number of render targets supplied is less than
 *      one.</li>
 * <li>invalidTargetRangeMsg: Request for a target index that is negative or
 *     greater than the number of targets.</li>
 * <li>invalidDimensionMsg Either the width or height value given to the class
 *     is not positive
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 2.14 $
 */
public class MRTOffscreenTexture2D extends MRTTexture2D
    implements OffscreenBufferRenderable,
               OffscreenCullable
{
    /** Message for the index provided being out of range */
    private static final String NEED_CAPS_PROP =
        "org.j3d.aviatrix3d.MRTOffscreenTexture2D.capsReqdMsg";

    /** Message for getParent(null) case error */
    private static final String ARRAY_LAYERS_NULL_PROP =
        "org.j3d.aviatrix3d.MRTOffscreenTexture2D.nullLayersArrayMsg";

    /** Number of render targets supplied was less than one */
    private static final String INV_NUM_TARGETS_PROP =
        "org.j3d.aviatrix3d.MRTOffscreenTexture2D.invalidNumTargetsMsg";

    /** Number of render targets supplied was less than one */
    private static final String INV_TARGET_RANGE_PROP =
        "org.j3d.aviatrix3d.MRTOffscreenTexture2D.invalidTargetRangeMsg";

    /** Number of render targets supplied was less than one */
    private static final String INV_DIMENSION_PROP =
        "org.j3d.aviatrix3d.MRTOffscreenTexture2D.invalidDimensionMsg";

    /** The number of render targets to create an make available */
    private int numRenderTargets;

    /** The Scene Graph renderableObjects */
    private Layer[] layers;

    /** The number of valid layers to render */
    private int numLayers;

    /** The current clear colour */
    private float[] clearColor;

    /** Flag for the per-frame repaint setup */
    private boolean repaintNeeded;

    /** Flag for resizing this buffer */
    private boolean bufferResized;

    /** Maps the index of the render target to the wrapper for it */
    private HashMap<Integer, MRTTexture2D> renderTargetMap;

    /** If configured, this is the separated depth target */
    private MRTTexture2D depthTarget;

    /**
     * Constructs an offscreen texture that fits the given setup. All values
     * must be valid and non-negative. Floating point colour buffers are
     * disabled and no seperate depth target is generated.
     *
     * @param caps The capabilities needed to generate the Pbuffer with
     * @param width The width of the texture in pixels
     * @param height The height of the texture in pixels
     * @throws IllegalArgumentException The capabilities parameter is null
     *    or the number of targets is less than one
     */
    public MRTOffscreenTexture2D(GraphicsRenderingCapabilities caps,
                                 int width,
                                 int height,
                                 int numTargets)
    {
        this(caps, false, width, height, numTargets);
    }

    /**
     * Constructs an offscreen texture that fits the given setup. All values
     * must be valid and non-negative. Floating point colour buffers are
     * disabled and no seperate depth target is generated.
     *
     * @param caps The capabilities needed to generate the Pbuffer with
     * @param width The width of the texture in pixels
     * @param height The height of the texture in pixels
     * @param separateDepthTexture True is a separate depth texture render
     *    target should be created
     * @throws IllegalArgumentException The capabilities parameter is null
     *    or the number of targets is less than one
     */
    public MRTOffscreenTexture2D(GraphicsRenderingCapabilities caps,
                                 int width,
                                 int height,
                                 int numTargets,
                                 boolean separateDepthTexture)
    {
        this(caps, false, width, height, numTargets, separateDepthTexture);
    }

    /**
     * Constructs an offscreen texture that fits the given setup. All values
     * must be valid and non-negative.
     *
     * @param caps The capabilities needed to generate the Pbuffer with
     * @param unclampColorRange true to make use of vendor-specific
     *    extensions for floating point colour buffers
     * @param width The width of the texture in pixels
     * @param height The height of the texture in pixels
     * @throws IllegalArgumentException The capabilities parameter is null
     *    or the number of targets is less than one
     */
    public MRTOffscreenTexture2D(GraphicsRenderingCapabilities caps,
                                 boolean unclampColorRange,
                                 int width,
                                 int height,
                                 int numTargets)
    {
        this(caps, unclampColorRange, width, height, numTargets, false);
    }

    /**
     * Constructs an offscreen texture that fits the given setup. All values
     * must be valid and non-negative.
     *
     * @param caps The capabilities needed to generate the Pbuffer with
     * @param unclampColorRange true to make use of vendor-specific
     *    extensions for floating point colour buffers
     * @param width The width of the texture in pixels
     * @param height The height of the texture in pixels
     * @param separateDepthTexture True is a separate depth texture render
     *    target should be created
     * @throws IllegalArgumentException The capabilities parameter is null
     *    or the number of targets is less than one
     */
    public MRTOffscreenTexture2D(GraphicsRenderingCapabilities caps,
                                 boolean unclampColorRange,
                                 int width,
                                 int height,
                                 int numTargets,
                                 boolean separateDepthTexture)
    {
        this(caps, unclampColorRange, width, height, 0, numTargets, separateDepthTexture);
    }

    /**
     * Constructs an offscreen texture that fits the given setup and provides
     * a specific format that overrides the automatic determination. All values
     * must be valid and non-negative. Floating point colour buffers are
     * disabled.
     *
     * @param caps The capabilities needed to generate the Pbuffer with
     * @param width The width of the texture in pixels
     * @param height The height of the texture in pixels
     * @param format The format to associate with this texture
     * @throws IllegalArgumentException The capabilities parameter is null
     *    or the number of targets is less than one
     */
    public MRTOffscreenTexture2D(GraphicsRenderingCapabilities caps,
                                 int width,
                                 int height,
                                 int format,
                                 int numTargets)
    {
        this(caps, false, width, height, format, numTargets, false);
    }

    /**
     * Constructs an offscreen texture that fits the given setup and provides
     * a specific format that overrides the automatic determination. All values
     * must be valid and non-negative.
     *
     * @param caps The capabilities needed to generate the Pbuffer with
     * @param unclampColorRange true to make use of vendor-specific
     *    extensions for floating point colour buffers
     * @param width The width of the texture in pixels
     * @param height The height of the texture in pixels
     * @param fmt The format to associate with this texture
     * @param separateDepthTexture True is a separate depth texture render
     *    target should be created
     * @throws IllegalArgumentException The capabilities parameter is null
     *    or the number of targets is less than one
     */
    public MRTOffscreenTexture2D(GraphicsRenderingCapabilities caps,
                                 boolean unclampColorRange,
                                 int width,
                                 int height,
                                 int fmt,
                                 int numTargets,
                                 boolean separateDepthTexture)
    {
        super(width, height, 0);

        bufferResized = false;

        if(caps == null)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NEED_CAPS_PROP);
            throw new IllegalArgumentException(msg);
        }

        if(width < 1)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INV_DIMENSION_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args =
            {
                "width",
                new Integer(width)
            };

            Format[] fmts = { null, n_fmt };
            MessageFormat msg_fmt = new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);

            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        if(height < 1)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INV_DIMENSION_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args =
            {
                "height",
                new Integer(height)
            };

            Format[] fmts = { null, n_fmt };
            MessageFormat msg_fmt = new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);

            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        if(numTargets < 1)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INV_NUM_TARGETS_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Integer(numTargets) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt = new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);

            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        numRenderTargets = numTargets;
        format = fmt;

        if(format == 0)
        {
            int r = caps.redBits;
            int g = caps.greenBits;
            int b = caps.blueBits;
            int a = caps.alphaBits;

            if(a != 0)
            {
                if(b != 0)
                    format = Texture.FORMAT_RGBA;
                else if(r != 0)
                    format = FORMAT_INTENSITY_ALPHA;
                else
                    format = Texture.FORMAT_ALPHA;
            }
            else
            {
                if(b != 0)
                    format = Texture.FORMAT_RGB;
                else
                    format = Texture.FORMAT_INTENSITY;
            }
        }

        renderTargetMap = new HashMap<Integer, MRTTexture2D>();
        renderTargetMap.put(0, this);

        bufferData = new BufferSetupData();
        bufferData.setNumRenderTargets(numTargets);
        bufferData.setRenderTargetIndex(0);

        copyCapabilities(bufferData, caps, unclampColorRange);

        clearColor = new float[4];
        layers = new Layer[0];


        // Now go through and create render target wrappers
        for(int i = 1; i < numTargets; i++)
        {
            BufferSetupData data = new BufferSetupData();
            data.setNumRenderTargets(numTargets);
            data.setRenderTargetIndex(i);

            copyCapabilities(data, caps, unclampColorRange);

            MRTTexture2D target = new MRTTexture2D(width, height, i);
            target.setFormat(format);
            target.setBufferData(data);

            renderTargetMap.put(i, target);
        }

        if(separateDepthTexture)
        {
            BufferSetupData data = new BufferSetupData();
            data.setNumRenderTargets(numTargets);
            data.setRenderTargetIndex(0);

            data.enableUnclampedColorBuffer(unclampColorRange);
            data.enableFloatingPointColorBuffer(caps.useFloatingPointBuffers);
            data.setDepthBits(caps.depthBits);
            data.setStencilBits(0);
            data.setNumAASamples(0);

            depthTarget = new MRTTexture2D(width, height, 0);
            depthTarget.setFormat(Texture.FORMAT_DEPTH_COMPONENT);
            depthTarget.setBufferData(data);
        }
    }

    //---------------------------------------------------------------
    // Methods defined by OffscreenCullable
    //---------------------------------------------------------------

    /**
     * Get the current state of the repainting enabled flag.
     *
     * @return true when the texture requires re-drawing
     */
    @Override
    public boolean isRepaintRequired()
    {
        return repaintNeeded;
    }

    /**
     * Get the cullable layer child that for the given layer index.
     *
     * @return The layer cullable at the given index or null
     */
    @Override
    public LayerCullable getCullableLayer(int layerIndex)
    {
        if(layerIndex < 0 || layerIndex >= numLayers)
            return null;

        if(layers[layerIndex] == null)
            return null;

        // TODO:
        // When we go to full internal cullable representations then this should
        // pass in the correct buffer ID that has been passed down from on high.
        return layers[layerIndex].getCullable(0);
    }

    /**
     * Returns the number of valid cullable children to process. If there are
     * no valid renderable children return -1.
     *
     * @return A number greater than or equal to zero or -1
     */
    @Override
    public int numCullableChildren()
    {
        return numLayers;
    }


    /**
     * Fetch the renderable that this offscreen cullable will draw to.
     *
     * @return The renderable instance that we deposit pixels to
     */
    @Override
    public OffscreenBufferRenderable getOffscreenRenderable()
    {
       return this;
    }

    //---------------------------------------------------------------
    // Methods defined by OffscreenBufferRenderable
    //---------------------------------------------------------------

    /**
     * Set the background colour that this surface should be cleared to before
     * the drawing step. Colours range from 0 to 1 in the normal manner.
     *
     * @param col An array of at least length 4 to copy values into
     */
    @Override
    public void getClearColor(float[] col)
    {
        col[0] = clearColor[0];
        col[1] = clearColor[1];
        col[2] = clearColor[2];
        col[3] = clearColor[3];
    }

    /**
     * Get the number of render targets that this offscreen renderable manages. This
     * should always return at least 1, being itself.
     *
     * @return A value greater than zero
     */
    @Override
    public int getNumRenderTargets()
    {
        return numRenderTargets;
    }

    /**
     * Get the child render target at the given index. If the index 0 is given, this
     * will return a reference to ourselves.
     *
     * @param index The index of the target to fetch
     * @return The render target at the given index
     */
    @Override
    public OffscreenRenderTargetRenderable getRenderTargetRenderable(int index)
    {
        return renderTargetMap.get(index);
    }

    /**
     * Check to see if the depth buffer has its own separate renderable object.
     * Used when the offscreen needs to create the depth buffer separately as
     * a texture to use in shading.
     *
     * @return True if a separate depth texture is wanted
     */
    @Override
    public boolean hasSeparateDepthRenderable()
    {
        return depthTarget != null;
    }

    /**
     * If a separate depth render target has been requested, return the
     * renderable for that object now. If not requested, this returns null.
     *
     * @return The depth target renderable or null
     */
    @Override
    public OffscreenRenderTargetRenderable getDepthRenderable()
    {
        return depthTarget;
    }

    /**
     * Check to see if this buffer has resized since the last time it was used.
     * If so, recreate the underlying setup, but keep everything else the same.
     * Will reset the flag on read.
     *
     * @return true if the buffer has resized, requiring reallocation of the
     *   underlying buffer objects
     */
    @Override
    public boolean hasBufferResized()
    {
        boolean ret_val = bufferResized;
        bufferResized = false;

        return ret_val;
    }

    //---------------------------------------------------------------
    // Methods defined by OffscreenRenderTargetRenderable
    //---------------------------------------------------------------

    /**
     * Check to see if this is a child render target of a parent multiple
     * render target offscreen buffer. Returns true if it is.
     *
     * @return true if this is a child, false if the parent
     */
    @Override
    public boolean isChildRenderTarget()
    {
        return false;
    }

    //---------------------------------------------------------------
    // Methods defined by Texture
    //---------------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object. Derived instances
     * should override this to add texture-specific extensions.
     *
     * @param tex The texture instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    @Override
    public int compareTo(Texture tex)
    {
        int res = super.compareTo(tex);
        if(res != 0)
            return res;

        // If this is the parent versus child render target, put the
        // parent first in the list
        if(!(tex instanceof MRTOffscreenTexture2D))
            return -1;

        MRTOffscreenTexture2D o2d = (MRTOffscreenTexture2D)tex;

        if(numLayers != o2d.numLayers)
            return numLayers < o2d.numLayers ? -1 : 1;

        for(int i = 0; i < numLayers; i++)
        {
            res = layers[i].compareTo(o2d.layers[i]);
            if(res != 0)
                return res;
        }

        if(repaintNeeded != o2d.repaintNeeded)
            return repaintNeeded ? 1 : -1;

        if(clearColor[0] != o2d.clearColor[0])
            return clearColor[0] < o2d.clearColor[0] ? -1 : 1;

        if(clearColor[1] != o2d.clearColor[1])
            return clearColor[1] < o2d.clearColor[1] ? -1 : 1;

        if(clearColor[2] != o2d.clearColor[2])
            return clearColor[2] < o2d.clearColor[2] ? -1 : 1;

        if(clearColor[3] != o2d.clearColor[3])
            return clearColor[3] < o2d.clearColor[3] ? -1 : 1;

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param tex The texture instance to be compared
     * @return true if the objects represent identical values
     */
    @Override
    public boolean equals(Texture tex)
    {
        if(!(tex instanceof MRTOffscreenTexture2D))
            return false;

        if(!super.equals(tex))
            return false;

        MRTOffscreenTexture2D o2d = (MRTOffscreenTexture2D)tex;

        if((numLayers != o2d.numLayers) ||
           (repaintNeeded != o2d.repaintNeeded) ||
           (clearColor[0] != o2d.clearColor[0]) ||
           (clearColor[1] != o2d.clearColor[1]) ||
           (clearColor[2] != o2d.clearColor[2]) ||
           (clearColor[3] != o2d.clearColor[3]))
            return false;

        // so the number of layers is the same, check to see if one of them
        // is not equal to the others.
        for(int i = 0; i < numLayers; i++)
        {
            if(!layers[i].equals(o2d.layers[i]))
                return false;
        }

        return true;
    }

    //---------------------------------------------------------------
    // Methods defined by SceneGraphObject
    //---------------------------------------------------------------

    /**
     * Check to see if this node is the same reference as the passed node that
     * is a parent of this node. This is the downwards check to ensure that
     * there is no cyclic scene graph structures at the point where someone
     * adds a node to the scenegraph. When the reference and this are the
     * same, an exception is generated. Since each class may have different
     * lists of child node setups, this should be overriden by any class that
     * can take children, and have the call passed along to the children.
     *
     * @param parent The reference to check against this class
     * @throws CyclicSceneGraphStructureException Equal parent and child
     */
    @Override
    protected void checkForCyclicChild(SceneGraphObject parent)
        throws InvalidWriteTimingException, CyclicSceneGraphStructureException
    {
        if(parent == this)
            throw new CyclicSceneGraphStructureException();

        for(int i = 0; i < numLayers; i++)
        {
            if(layers[i] != null)
                layers[i].checkForCyclicChild(parent);
        }
    }

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
        if(state)
            liveCount++;
        else if(liveCount > 0)
            liveCount--;

        if((liveCount == 0) || !alive)
        {
            super.setLive(state);

            for(int i = 0; i < numLayers; i++)
            {
                if(layers[i] != null)
                    layers[i].setLive(state);
            }
        }
    }

    /**
     * Set the scenegraph update handler for this node.  It will notify
     * all its children of the value. A null value will clear the current
     * handler.
     *
     * @param handler The instance to use as a handler
     */
    @Override
    protected void setUpdateHandler(NodeUpdateHandler handler)
    {
        if(updateHandler == handler)
            return;

        super.setUpdateHandler(handler);

        for(int i = 0; i < numLayers; i++)
        {
            if(layers[i] != null)
                layers[i].setUpdateHandler(updateHandler);
        }
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Get the format for this texture. As this is a single pBuffer texture,
     * there is only ever one level, so the argument is ignored.
     *
     * @param level The mipmap level to get the format for
     * @return The format.
     */
    public int getFormat(int level)
    {
        return format;
    }

    /**
     * Get the numbered render target. Using an index of 0 will return a
     * reference to this class because this class is always considered to
     * be render target 0. A number out of range will generate an exception.
     *
     * @param index The render target number to get the texture representation
     *     for
     * @return The render target representation for that index
     * @throws IllegalArgumentException The index is out of range
     */
    public MRTTexture2D getRenderTarget(int index)
    {
        if(index < 0 || index >= numRenderTargets)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INV_TARGET_RANGE_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args =
            {
                new Integer(index),
                new Integer(numRenderTargets - 1)
            };

            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt = new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);

            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        return renderTargetMap.get(index);
    }

    /**
     * If the texture was configured with a separate offscreen depth texture
     * from the colour texture, get that texture representation.
     *
     * @return The render target representation for depth or null if not
     *    requested
     */
    public MRTTexture2D getDepthRenderTarget()
    {
        return depthTarget;
    }

    /**
     * Resize this buffer to a new window. Automatically sets the repaint
     * required flag.
     *
     * @param w The new width of the buffer in pixels. Must be positive.
     * @param h The new height of the buffer in pixels. Must be positive.
     * @throws IllegalArgumentException if the width or height <= 0
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void resize(int w, int h)
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(w < 1)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INV_DIMENSION_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args =
            {
                "width",
                new Integer(w)
            };

            Format[] fmts = { null, n_fmt };
            MessageFormat msg_fmt = new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);

            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        if(h < 1)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INV_DIMENSION_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args =
            {
                "height",
                new Integer(h)
            };

            Format[] fmts = { null, n_fmt };
            MessageFormat msg_fmt = new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);

            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        if(w != width || h != height)
        {
            if(isLive())
            {
                repaintNeeded = true;
                bufferResized = true;
            }

            updateSize(w, h);

            for(int i = 1; i < numRenderTargets; i++)
            {
                MRTTexture2D tex = renderTargetMap.get(i);
                tex.updateSize(w, h);
            }

            if(depthTarget != null)
            {
                depthTarget.updateSize(w, h);
            }
        }
    }

    /**
     * Set the collection of geometry that should be rendered to this
     * texture. The geometry is, in effect, a completely separate rendarable
     * space, with it's own culling and sorting pass. In addition, a check
     * is made to make sure that no cyclic scene graph structures are created,
     * as this can create really major headachesfor nested surface rendering.
     * A null value will clear the current geometry and result in only
     * rendering the background, if set. if not set, then whatever the default
     * colour is, is used (typically black).
     *
     * @param layers The collection of layers, in order, to render
     * @param numLayers The number of valid layers to use
     * @throws CyclicSceneGraphStructureException Equal parent and child
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setLayers(Layer[] layers, int numLayers)
        throws InvalidWriteTimingException, CyclicSceneGraphStructureException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        // Make sure things are valid first
        for(int i = 0; i < numLayers; i++)
            layers[i].checkForCyclicChild(this);

        int cur_size = this.layers.length;

        // clean up the old list first
        for(int i = 0; i < cur_size; i++)
        {
            if(alive)
                this.layers[i].setLive(false);
            this.layers[i].setUpdateHandler(null);
        }

        if(cur_size < numLayers)
            this.layers = new Layer[numLayers];

        if(numLayers != 0)
            System.arraycopy(layers, 0, this.layers, 0, numLayers);

        for(int i = numLayers; i < cur_size; i++)
            layers[i] = null;

        this.numLayers = numLayers;

        // clean up the old list first
        for(int i = 0; i < numLayers; i++)
        {
            this.layers[i].setLive(alive);
            this.layers[i].setUpdateHandler(updateHandler);
        }
    }

    /**
     * Get the number of layers that are currently set. If no layers are set,
     * or a scene is set, this will return zero.
     *
     * @return a value greater than or equal to zero
     */
    public int numLayers()
    {
        return numLayers;
    }

    /**
     * Fetch the current layers that are set. The values will be copied into
     * the user-provided array. That array must be at least
     * {@link #numLayers()} in length. If not, this method does nothing (the
     * provided array will be unchanged).
     *
     * @param layers An array to copy the values into
     * @throws IllegalArgumentException The array provided is too small or null
     */
    public void getLayers(Layer[] layers)
        throws IllegalArgumentException
    {
        if((layers == null) || (layers.length < numLayers))
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(ARRAY_LAYERS_NULL_PROP);
            throw new NullPointerException(msg);
        }

        System.arraycopy(this.layers, 0, layers, 0, numLayers);
    }

    /**
     * Set this texture as requiring a repaint for the next frame. If no
     * repaint is required, reset this to null at the point where no
     * repainting is required. The internal flag is a user-defined state,
     * so For the first frame at least, this should be set to true so that
     * the initial paint can be performed (assuming data is present, of
     * course).
     *
     * @param enable true to have this repaint the next frame
     */
    public void setRepaintRequired(boolean enable)
    {
        repaintNeeded = enable;
    }

    /**
     * Set the background colour that this surface should be cleared to before
     * the drawing step. Colours range from 0 to 1 in the normal manner.
     *
     * @param r The red component of the background clear colour
     * @param g The green component of the background clear colour
     * @param b The blue component of the background clear colour
     * @param a The alpha component of the background clear colour
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setClearColor(float r, float g, float b, float a)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        clearColor[0] = r;
        clearColor[1] = g;
        clearColor[2] = b;
        clearColor[3] = a;
    }

    /**
     * Convenience method to copy the GLCapabilities in to the internal
     * buffer data state class.
     *
     * @param data The data class to copy stuff in to
     * @param caps The GLCapabilities to copy stuff out of
     * @param unclampColorRange true if we want more than [0,1] colours
     */
    private void copyCapabilities(BufferSetupData data,
                                  GraphicsRenderingCapabilities caps,
                                  boolean unclampColorRange)
    {
        data.enableUnclampedColorBuffer(unclampColorRange);
        data.enableFloatingPointColorBuffer(caps.useFloatingPointBuffers);
        data.setDepthBits(caps.depthBits);
        data.setStencilBits(caps.stencilBits);

        if(caps.useSampleBuffers)
           bufferData.setNumAASamples(caps.numSamples);
        else
           bufferData.setNumAASamples(0);
    }
}
