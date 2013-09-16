/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004-2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d.pipeline.graphics;

// External imports
// None

// Local imports
// None

/**
 * A listener to pass on the low-level window resize events to the external
 * application for further processing.
 * <p>
 *
 * Since Aviatrix3D requires explicit viewport setting, then this listener will
 * be useful to get the current size information passed back to the
 * {@link org.j3d.aviatrix3d.Viewport} class for handling the processing.
 *
 * @author Justin Couch
 * @version $Revision: 3.1 $
 */
public interface GraphicsResizeListener
{
    /**
     * Notification that the graphics output device has changed dimensions to
     * the given size. Dimensions are in pixels.
     *
     * @param x The lower left x coordinate for the view
     * @param y The lower left y coordinate for the view
     * @param width The width of the viewport in pixels
     * @param height The height of the viewport in pixels
     */
    public void graphicsDeviceResized(int x, int y, int width, int height);
}
