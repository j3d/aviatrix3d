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
// None

// Local imports
// None

/**
 * A source for texture information that is dynamically generated as required
 * per frame.
 * <p>
 *
 * <b>Note: This class does nothing in Aviatrix3D 1.0</b>
 * <p>
 *
 * The texture width and height are dynamically found each time it is
 * requested. If no scene is set, or the scene does not have a
 * ViewEnvironment with a valid viewport set, then the width and height
 * will both return zero.
 *
 * @author Justin Couch
 * @version $Revision: 1.10 $
 * @deprecated Use {@link OffscreenTexture2D} or {@link MRTOffscreenTexture2D}
 */
public class MultipassTextureComponent extends NodeComponent
    implements MultipassTextureSource
{
    /** The format */
    protected int format;

    /** The Scene Graph viewLayer */
    protected ViewportLayer viewLayer;

    /** The current clear colour */
    protected float[] clearColor;

    /** The number of levels in this component. */
    protected int numLevels;

    /** Flag for the per-frame repaint setup */
    private boolean repaintNeeded;

    /** The listing of formats defined for each level */
    protected int[] formats;

    /** The observer instance to use for update callbacks */
    protected MultipassRenderObserver observer;

    /** The collection of GL buffer IDs needed by the rendering */
    protected int glBuffers;

    /** The width of the buffer to create in pixels */
    private int bufferWidth;

    /** The width of the buffer to create in pixels */
    private int bufferHeight;

    /**
     * Construct a multipass texture with the buffer set to the colour buffer
     * only and of the specified dimensions. Both the width and height must be
     * a power of two.
     *
     * @param width The width of the texture to create
     * @param height The height of the texture to create
     * @throws IllegalArgumentException Either the width or height are not
     *   powers of two
     */
    public MultipassTextureComponent(int width, int height)
    {
        bufferWidth = width;
        bufferHeight = height;

        clearColor = new float[4];
        glBuffers = COLOR_BUFFER;
    }

    //---------------------------------------------------------------
    // Methods defined by MultipassTextureSource
    //---------------------------------------------------------------

    /**
     * Fetch the observer instance that may be associated with the texture
     * source. If no instance is associated, this will return null.
     *
     * @return The current observer instance, or null if none
     */
    public MultipassRenderObserver getRenderObserver()
    {
        return observer;
    }

    /**
     * Get the list of buffers that are required to be rendered by this
     * source. This will be used by the glClear() call to clear the
     * appropriate buffers. These are the buffers that are to be cleared
     * and used at the start of the multipass rendering. If the application
     * needs to clear buffers during individual passes, that should be
     * performed as part of the MultipassRenderObserver callbacks.
     *
     * @return A bitwise OR mask of the required buffers
     */
    public int getUsedBuffers()
    {
        return glBuffers;
    }

    /**
     * Set the number of levels of mipmap generation that should be rendered.
     * Each level will become a separate rendering pass that will be updated.
     * A check is performed to make sure that the number of levels does not
     * produce a situation where the width or height goes negative in thier
     * values - eg a starting size of 32 pixels square and requesting 6 levels
     * of mipmaps being generated.
     *
     * @param numLevels The number of levels to render
     * @throws IllegalArgumentException The number of levels is more than what
     *   the current size could reduce to
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setNumLevels(int numLevels)
        throws InvalidWriteTimingException, IllegalArgumentException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        this.numLevels = numLevels;
    }

    /**
     * Get the root of the currently rendered scene. If none is set, this will
     * return null.
     *
     * @return The current scene root or null.
     */
    public ViewportLayer getViewportLayer()
    {
        return viewLayer;
    }

    //---------------------------------------------------------------
    // Methods defined by OffscreenTextureSource
    //---------------------------------------------------------------

    /**
     * Get the height of the texture in pixels. If no image is set, this returns
     * -1.
     *
     * @return a number >= -1
     */
    public int getHeight()
    {
        return bufferHeight;
    }

    /**
     * Get the current state of the repainting enabled flag.
     *
     * @return true when the texture requires re-drawing
     */
    public boolean isRepaintRequired()
    {
        return repaintNeeded;
    }

    /**
     * Set the background colour that this surface should be cleared to before
     * the drawing step. Colours range from 0 to 1 in the normal manner.
     *
     * @param col An array of at least length 4 to copy values into
     */
    public void getClearColor(float[] col)
    {
        col[0] = clearColor[0];
        col[1] = clearColor[1];
        col[2] = clearColor[2];
        col[3] = clearColor[3];
    }

    //---------------------------------------------------------------
    // Methods defined by TextureSource
    //---------------------------------------------------------------

    /**
     * Get the width of this image.
     *
     * @return the width.
     */
    public int getWidth()
    {
        return bufferHeight;
    }

    /**
     * Get the number of levels for the mipmapping in this source.
     *
     * @return The number of levels.
     */
    public int getNumLevels()
    {
        return numLevels;
    }

    /**
     * Get the format of this image at the given mipmap level.
     *
     * @param level The mipmap level to get the format for
     * @return the format.
     */
    public int getFormat(int level)
    {
        return formats[level];
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
    protected void checkForCyclicChild(SceneGraphObject parent)
        throws CyclicSceneGraphStructureException
    {
        if(parent == this)
            throw new CyclicSceneGraphStructureException();

        if(viewLayer != null)
            viewLayer.checkForCyclicChild(parent);
    }

    /**
     * Notification that this object is live now. Overridden to make sure that
     * the live state of the nodes represents the same state as the parent
     * scene graph.
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

            if(viewLayer != null)
                viewLayer.setLive(state);
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

        if(viewLayer != null)
            viewLayer.setUpdateHandler(updateHandler);
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

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
     * @param layer The new scene instance to use or null
     * @throws CyclicSceneGraphStructureException Equal parent and child
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the data changed callback method
     */
    public void setViewportLayer(ViewportLayer layer)
        throws InvalidWriteTimingException, CyclicSceneGraphStructureException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        // Check all the parents are not ourself right now
        if(viewLayer != null)
            viewLayer.setLive(false);

        if(layer != null)
            layer.checkForCyclicChild(this);

        viewLayer = layer;

        if(viewLayer != null)
        {
            viewLayer.setViewportDimensions(0, 0, bufferWidth, bufferHeight);
            viewLayer.setUpdateHandler(updateHandler);
            viewLayer.setLive(alive);
        }
    }

    /**
     * Set the observer instance to be associated with the texture source.
     * To clear the current instance, use a value of null
     *
     * @param obs The new observer instance to use, or null if none
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the data changed callback method
     */
    public void setRenderObserver(MultipassRenderObserver obs)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        observer = obs;
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
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void setRepaintRequired(boolean enable)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(getAppUpdateWriteTimingMessage());

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
     *   of the NodeUpdateListener callback method
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
     * Set the list of buffers that this source needs to write to.
     *
     * @param buffers The list of buffers bitwise OR together
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener callback method
     */
    public void setUsedBuffers(int buffers)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        glBuffers = buffers;
    }
}
