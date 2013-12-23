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
import org.j3d.aviatrix3d.pipeline.OutputDevice;

/**
 * A listener to pass on the graphics card capabilities detected whenever the
 * graphics device setup changes.
 * <p>
 *
 * Graphics devices rarely change during the running of an application, but if
 * they do, this will be called each time a change happens and we get a new
 * OpenGL context. It is guaranteed to be called once at the startup of the
 * application during the first running of the rendering cycle. This callback
 * is on an independent thread to the main rendering loop, so do not assume
 * that you are free to make scene graph modifications when this callback is
 * made.
 *
 * @author Justin Couch
 * @version $Revision: 3.2 $
 */
public interface SurfaceInfoListener
{
    /**
     * Notification that the graphics output device has changed GL context
     * and this is the collection of new information.
     *
     * @param surface The output surface that caused the new info
     * @param info The collected set of information known
     */
    public void surfaceInfoChanged(OutputDevice surface, SurfaceInfo info);
}
