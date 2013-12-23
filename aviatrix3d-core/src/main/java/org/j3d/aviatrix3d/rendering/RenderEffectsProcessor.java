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

package org.j3d.aviatrix3d.rendering;

// External imports
import javax.media.opengl.GL;

// Local imports
// None

/**
 * Observer of the rendering system that is informed of pre and post rendering
 * timing so that it may perform it's own additional drawing operations.
 * <p>
 *
 * The idea is to allow various pre and post-processing activities to be
 * performed on individual output surfaces (not just limited to the on-screen
 * drawable, but also with pBuffers etc). An example is a full-screen jitter,
 * blur effect or even fades/wipes between scenes. Currently the interface is
 * very experimental.
 * <p>
 *
 * It can be assumed that the GL context will be current for the appropriate
 * surface before any of the methods in this interface are called. The user
 * should not attempt any form of context state manipulation.
 *
 * @author Justin Couch
 * @version $Revision: 2.2 $
 */
public interface RenderEffectsProcessor
{
    /**
     * Perform any pre-rendering setup that you may need for this scene. After
     * this call, all normal scene graph rendering is performed by the surface.
     *
     * @param gl The current GL context wrapper to draw with
     * @param userData Some identifiable data provided by the user
     */
    public void preDraw(GL gl, Object userData);

    /**
     * Perform any post-rendering actions that you may need for this scene.
     * Called after the renderer has completed all drawing and just before the
     * buffer swap. The only thing to be called after calling this method is
     * glFlush().
     *
     * @param gl The current GL context wrapper to draw with
     * @param timingData Timing data for rendering operations
     * @param userData Some identifiable data provided by the user
     */
    public void postDraw(GL gl, ProfilingData timingData, Object userData);
}
