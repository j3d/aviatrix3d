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
 * A marker interface that represents a class capable of managing
 * the complete rendering pipeline.
 * <p>
 *
 * A manager is used to handle a system-specific rendering technique. The goal
 * is to manage the contained pipeline(s) in a way that is most efficient to
 * the hardware provided. Thus, it is expected there will be many different
 * types of managers to suit the many hardware configurations available.
 * <p>
 *
 * Example implementations of the pipeline manager would be one that handles
 * all the pipelines with simultaneous threads, each pinned to a particular
 * CPU/Graphics pipe that the machine has. Another implementation may hold all
 * the pipelines for sequential evaluation piping the output from one into the
 * input for another (eg for handling dynamic cubic environment maps).
 * <p>
 *
 * Basic common methods are provided for all implementations to use. It is
 * expected that implementations will add additional technique-specific
 * extension methods to the basic features.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface RenderPipelineManager
{
    /**
     * Tell render to start or stop rendering. If currently running, it
     * will wait until all the pipelines have completed their current cycle
     * and will then halt.
     *
     * @param state True if to enable rendering
     */
    public void setEnabled(boolean state);

    /**
     * Get the current render state of the manager.
     *
     * @return true if the manager is currently running
     */
    public boolean isEnabled();

    /**
     * Force a single render of all pipelines now. Ignores the enabled and
     * cycle time settings to cause a single render at this point in time.
     * If a render is currently in progress, the request is ignored.
     */
    public void renderOnce();

    /**
     * Set the minimum duty cycle of the render manager. This is the type in
     * milliseconds that should be the minimum between frames and can be used
     * to throttle the rendering loop to a maximum frame rate should other
     * systems require CPU time.
     *
     * @param cycleTime The minimum time in milliseconds between frames
     */
    public void setMinimumFrameInterval(int cycleTime);

    /**
     * Add a pipeline to be rendered to the manager. A duplicate registration
     * or null value is ignored.
     *
     * @param pipe The new pipe instance to be added
     */
    public void addPipeline(RenderPipeline pipe);

    /**
     * Remove an already registered pipeline from the manager. A or null value
     * or one that is not currently registered is ignored.
     *
     * @param pipe The pipe instance to be removed
     */
    public void removePipeline(RenderPipeline pipe);
}