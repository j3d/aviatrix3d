/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
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
 * A marker interface that represents a single complete rendering pipeline.
 * <p>
 *
 * A pipeline represents all of the drawing steps that may be accomplished
 * within a rendering cycle - culling, sorting and drawing. While an end-user
 * may wish to directly call the methods on this interface directly to control
 * their own rendering, it is recommended that a dedicated pipeline manager be
 * used for this task.
 * <p>
 *
 * If the pipeline does not have a drawable surface registered, it will still
 * complete all the steps up to that point. If no scene is registered, no
 * functionality is performed - render() will return immediately.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface RenderPipeline
{
    /**
     * Register a drawing surface that this pipeline will send its output to.
     * Setting a null value will remove the current drawable surface.
     *
     * @param surface The surface instance to use or replace
     */
    public void setDrawableSurface(DrawableSurface surface);

    /**
     * Start the pipeline functioning now. All steps will be called and this
     * method will not return until all are completed.
     */
    public void render();

    /**
     * Set the root of the scene graph to be used by this pipeline. A value of
     * null will remove the scene from being rendered, causing the pipeline to
     * function as a no-op when rendered.
     *
     */
    public void setRenderableScene(Group root);
}