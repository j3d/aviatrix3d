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

package org.j3d.aviatrix3d.pipeline;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.RenderPipeline;
import org.j3d.aviatrix3d.CullStage;
import org.j3d.aviatrix3d.SortStage;

/**
 * The default implementation of the rendering pipeline usable by most
 * applications.
 * <p>
 * This implementation is targeted towards single threaded architectures.
 * After setting the stages, the render command will not return until
 * everything is complete.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class DefaultRenderPipeline implements RenderPipeline
{
    /** The culling stage to be used */
    private CullStage culler;

    /** The sorting stage to be used */
    private SortStage sorter;

    /** Surface to draw to */
    private DrawableSurface drawable;

    /** The listener to pass cull to sort */
    private CullToSingleSortListener ctsListener;

    /** The listener to pass sort to the drawable listener */
    private SortToSingleDrawListener stdListener;

    /**
     * Create an instance of the pipeline with nothing registered.
     */
    public DefaultRenderPipeline()
    {
        ctsListener = new CullToSingleSortListener();
        stdListener = new SortToSingleDrawListener();
    }

    /**
     * Register a drawing surface that this pipeline will send its output to.
     * Setting a null value will remove the current drawable surface.
     *
     * @param surface The surface instance to use or replace
     */
    public void setDrawableSurface(DrawableSurface surface)
    {
    }

    /**
     * Start the pipeline functioning now. All steps will be called and this
     * method will not return until all are completed.
     */
    public void render()
    {
    }

    /**
     * Set the root of the scene graph to be used by this pipeline. A value of
     * null will remove the scene from being rendered, causing the pipeline to
     * function as a no-op when rendered.
     *
     */
    public void setRenderableScene(Group root)
    {
    }
}