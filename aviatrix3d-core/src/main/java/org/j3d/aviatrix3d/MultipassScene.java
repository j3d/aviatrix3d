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
import java.util.ArrayList;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.RenderPassCullable;
import org.j3d.aviatrix3d.rendering.SceneCullable;
import org.j3d.aviatrix3d.rendering.ViewEnvironmentCullable;

/**
 * Representation of the top level structure of a piece of scene graph that
 * permits multipass rendering to be performed.
 * <p>
 *
 * A multipass scene consists of a set of consecutive rendering passes over
 * a single set of buffers. Buffers may or may not be cleared on each pass,
 * depending on the buffer state attributes that are set. The end result is
 * a single image that forms the content of a layer.
 * <p>
 *
 * A multipass scene has a single background node that is rendered at the
 * start of the first pass and not applied to any other passes. Fog and
 * viewpoints are applied on a per-pass basis.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>sharedBgMsg: Error message when the set background has more than one
 *     possible parent.</li>
 * <li>invalidPassIndexMsg: Error message when trying to set</li>
 * <li>nullPassMsg: Error message when someone tried to pass us a null
 *     RenderPass</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 2.10 $
 */
public class MultipassScene extends Scene
    implements SceneCullable
{
    /** Message when setting the active background if it contains a shared parent */
    private static final String SHARED_BG_PROP =
        "org.j3d.aviatrix3d.MultipassScene.sharedBgMsg";

    /** Requested a pass object that we don't have */
    private static final String PASS_RANGE_PROP =
        "org.j3d.aviatrix3d.MultipassScene.invalidPassIndexMsg";

    /** The render pass argument to a method was null */
    private static final String NULL_PASS_PROP =
        "org.j3d.aviatrix3d.MultipassScene.nullPassMsg";

    /** The list of passes to be rendered */
    private ArrayList<RenderPass> renderPasses;

    /** The current background instance */
    private Background currentBackground;

    /**
     * Create a default instance of this scene with no content provided.
     */
    public MultipassScene()
    {
        renderPasses = new ArrayList<RenderPass>();
    }

    //----------------------------------------------------------
    // Methods defined by SceneCullable
    //----------------------------------------------------------

    /**
     * Check to see if this is a multipass cullable or single pass.
     *
     * @return true if this is a multipass cullable
     */
    @Override
    public boolean isMultipassScene()
    {
        return true;
    }

    /**
     * Get the primary view environment information. This applies to the whole
     * scene before any multipass processing is done. If this is a multipass
     * scene, the frustum information is ignored as each pass applies
     * separately.
     */
    @Override
    public ViewEnvironmentCullable getViewCullable()
    {
        return viewEnvironment;
    }

    /**
     * Get the cullable layer child that for the given layer index. For a single
     * pass scene this represents everything about the scene to be rendered.
     * The view environment of this scene is the same as that of the first
     * render pass.
     *
     * @param passIndex The index of the pass to fetch
     * @return The layer cullable at the given index or null
     */
    @Override
    public RenderPassCullable getCullablePass(int passIndex)
    {
        if(passIndex < 0 || passIndex >= renderPasses.size())
            return null;
        else
            return (RenderPassCullable)renderPasses.get(passIndex);
    }

    /**
     * Returns the number of valid cullable rendering passes to process. In a
     * single pass scene return 1.
     *
     * @return A number greater than or equal to zero
     */
    @Override
    public int numCullableChildren()
    {
        return renderPasses.size();
    }

    //----------------------------------------------------------
    // Methods defined by Scene
    //----------------------------------------------------------

    /**
     * Set the viewport dimensions from the parent viewport. These dimensions
     * are pushed down through the scene to the viewport.
     *
     * @param x The lower left x coordinate for the view
     * @param y The lower left y coordinate for the view
     * @param width The width of the viewport in pixels
     * @param height The height of the viewport in pixels
     */
    @Override
    void setViewportDimensions(int x, int y, int width, int height)
    {
        super.setViewportDimensions(x, y, width, height);

        for(int i = 0; i < renderPasses.size(); i++)
        {
            RenderPass p = (RenderPass)renderPasses.get(i);
            p.setEnvViewportDimensions(x, y, width, height);
        }
    }

    //----------------------------------------------------------
    // Methods defined by ScenegraphObject
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

        super.setLive(state);

        for(int i = 0; i < renderPasses.size(); i++)
        {
            RenderPass p = renderPasses.get(i);
            p.setLive(state);
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
        super.setUpdateHandler(handler);

        for(int i = 0; i < renderPasses.size(); i++)
        {
            RenderPass p = renderPasses.get(i);
            p.setUpdateHandler(handler);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Add a new pass to the end of the current rendering list.
     *
     * @param pass The rendering pass description to use
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     * @throws IllegalArgumentException The object passed was null
     */
    public void addRenderPass(RenderPass pass)
        throws InvalidWriteTimingException, IllegalArgumentException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(getAppUpdateWriteTimingMessage());

        if(pass == null)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NULL_PASS_PROP);
            throw new IllegalArgumentException(msg);
        }

        pass.setUpdateHandler(updateHandler);
        pass.setLive(alive);

        renderPasses.add(pass);
    }


    /**
     * Replace the render pass at the given index with a different pass
     * representation.
     *
     * @param passNumber The index of the pass to return, zero based
     * @param pass The rendering pass description to use
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     * @throws IllegalArgumentException The index is out of range or the pass
     *   object was null
     */
    public void setRenderPass(int passNumber, RenderPass pass)
        throws InvalidWriteTimingException, IllegalArgumentException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(getAppUpdateWriteTimingMessage());

        if(pass == null)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NULL_PASS_PROP);
            throw new IllegalArgumentException(msg);
        }

        if(passNumber < 0 || passNumber >= renderPasses.size())
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(PASS_RANGE_PROP);
            throw new IllegalArgumentException(msg);
        }

        pass.setUpdateHandler(updateHandler);
        pass.setLive(alive);

        RenderPass old_pass = renderPasses.get(passNumber);
        old_pass.setLive(false);

        renderPasses.set(passNumber, pass);
    }

    /**
     * Remove the render pass at the given index.
     * @param passNumber The index of the pass to return, zero based
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     * @throws IllegalArgumentException The index is out of range
     */
    public void removeRenderPass(int passNumber)
        throws InvalidWriteTimingException, IllegalArgumentException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(getAppUpdateWriteTimingMessage());

        if(passNumber < 0 || passNumber >= renderPasses.size())
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(PASS_RANGE_PROP);
            throw new IllegalArgumentException(msg);
        }

        RenderPass old_pass = renderPasses.remove(passNumber);
        old_pass.setLive(false);
    }

    /**
     * Get the render pass at the given pass number.
     *
     * @param passNumber The index of the pass to return, zero based
     * @return The pass container at the given index
     * @throws IllegalArgumentException The index is out of range
     */
    public RenderPass getRenderPass(int passNumber)
        throws IllegalArgumentException
    {
        if(passNumber < 0 || passNumber >= renderPasses.size())
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(PASS_RANGE_PROP);
            throw new IllegalArgumentException(msg);
        }

        return renderPasses.get(passNumber);
    }


    /**
     * Get all the render passes currently registered.
     *
     * @param passes An array to copy everything into
     * @throws IllegalArgumentException The index is out of range
     */
    public void getRenderPasses(RenderPass[] passes)
        throws IllegalArgumentException
    {
        renderPasses.toArray(passes);
    }

    /**
     * Request the number of rendering passes currently registered with this scene.
     *
     * @return A non-negative value
     */
    public int numRenderPasses()
    {
        return renderPasses.size();
    }

    /**
     * Set the background path that should be applied to the current surface.
     * The output drawn will be a combination of this information and that
     * of the view environment.
     *
     * @param bg The instance of the active background
     * @throws IllegalArgumentException The path contains a SharedGroup or
     *    the node is not live
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void setActiveBackground(Background bg)
        throws InvalidWriteTimingException, IllegalArgumentException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(getAppUpdateWriteTimingMessage());

        if(bg != null)
        {
            Node parent = bg.getParent();

            while(parent != null)
            {
                if((parent instanceof SharedGroup) ||
                   (parent instanceof SharedNode))
                {
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg = intl_mgr.getString(SHARED_BG_PROP);
                    throw new IllegalArgumentException(msg);
                }

                parent = parent.getParent();
            }
        }

        currentBackground = bg;
    }

    /**
     * Get the currently set active view. If none is set, return null.
     *
     * @return The current view instance or null
     */
    public Background getActiveBackground()
    {
        return currentBackground;
    }
}
