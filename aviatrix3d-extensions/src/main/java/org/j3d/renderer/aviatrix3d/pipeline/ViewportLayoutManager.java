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

package org.j3d.renderer.aviatrix3d.pipeline;

// External imports

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.j3d.aviatrix3d.Viewport;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsResizeListener;

// Local imports

/**
 * Extension of the basic resizer manager that does layout management of the
 * viewport based on proportional configuration. It allows for multiple
 * viewports within a layer to be managed correctly while the window resizes.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 3.3 $
 */
public class ViewportLayoutManager
    implements GraphicsResizeListener
{
    /** The list of viewports to manage */
    private List<ViewportLayoutData> viewports;

    /** Flag saying that resizes have been recieved and not yet processed */
    private boolean newSizeSet;

    /** The lower left X position of the viewpoint, in pixels */
    private int viewX;

    /** The lower left Y position of the viewpoint, in pixels */
    private int viewY;

    /** The width of the viewport in pixels */
    private int viewWidth;

    /** The width of the viewport in pixels */
    private int viewHeight;

    /** Have we got any valid size set yet. */
    private boolean validSizeSet;

    /**
     * Create a new instance of this manager now.
     */
    public ViewportLayoutManager()
    {
        viewports = new ArrayList<>();
        newSizeSet = false;
        validSizeSet = false;
    }

    //---------------------------------------------------------------
    // Methods defined by GraphicsResizeListener
    //---------------------------------------------------------------

    @Override
    public void graphicsDeviceResized(int x, int y, int width, int height)
    {
        viewX = x;
        viewY = y;
        viewWidth = width;
        viewHeight = height;
        newSizeSet = true;
        validSizeSet = true;
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Clock the updates that should be sent now. This should only be called
     * during the application update cycle callbacks. Ideally this should be
     * called as the first thing in updateSceneGraph() method so that anything
     * else that relies on view frustum projection will have the latest
     * information.
     */
    public void sendResizeUpdates()
    {
        if(!newSizeSet)
        {
            return;
        }

        for(ViewportLayoutData viewportConfig : viewports)
        {
            updateViewport(viewportConfig);
        }

        newSizeSet = false;
    }

    /**
     * Add a viewport to be managed. Duplicate requests are ignored, as are
     * null values.
     *
     * @param view The viewport instance to use
     */
    public void addManagedViewport(Viewport view, float x, float y, float width, float height)
    {
        if(view == null)
        {
            return;
        }

        // check to see if we have one under management already.
        for(ViewportLayoutData viewConfig: viewports)
        {
            if(viewConfig.viewport == view)
            {
                return;
            }
        }

        ViewportLayoutData config = new ViewportLayoutData(x, y, width, height, view);

        viewports.add(config);

        if(validSizeSet && !view.isLive())
        {
            updateViewport(config);
        }
    }

    /**
     * Remove a viewport that is being managed. If the viewport is not
     * currently registered, the request is silently ignored.
     *
     * @param view The viewport instance to remove
     */
    public void removeManagedViewport(Viewport view)
    {
        if(view == null)
            return;

        Iterator<ViewportLayoutData> itr = viewports.iterator();

        while(itr.hasNext())
        {
            ViewportLayoutData config = itr.next();
            if(config.viewport == view)
            {
                itr.remove();
            }
        }
    }

    /**
     * Clear all of the current viewports from the manager. Typically used when
     * you want to completely change the scene.
     */
    public void clear()
    {
        viewports.clear();
    }

    /**
     * Internal common method to send a resize to a viewport object right now
     *
     * @param config The config to recalculate
     */
    private void updateViewport(ViewportLayoutData config)
    {
        int x = (int)(viewX + viewWidth * config.startX);
        int y = (int)(viewY + viewHeight * config.startY);
        int w = (int)(viewWidth * config.width);
        int h = (int)(viewHeight * config.height);

        config.viewport.setDimensions(x, y, w, h);
    }
}
