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

package org.j3d.aviatrix3d.pipeline;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.rendering.LayerCullable;

import org.j3d.util.ErrorReporter;

/**
 * A marker interface that represents a single complete rendering pipeline
 * that is independent of the output device type.
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
 * @version $Revision: 2.6 $
 */
public interface RenderPipeline
{
    /**
     * Register an error reporter with the engine so that any errors generated
     * by the node's internals can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter);

    /**
     * Start the pipeline functioning now. All steps will be called and this
     * method will not return until all are completed. Only the swap
     * function is not called in this time as we need to have this called
     * separately if the system is running parallel pipelines.
     * <p>
     * The return value indicates success or failure in the ability to
     * render this frame. Typically it will indicate failure if the
     * underlying surface has been disposed of, either directly through the
     * calling of the method on this interface, or through an internal check
     * mechanism. If failure is indicated, then check to see if the surface has
     * been disposed of and discontinue rendering if it has.
     *
     * @return true if the drawing succeeded, or false if not
     */
    public boolean render();

    /**
     * Cause the surface to redraw the next frame only, with no processing of
     * the pipeline. This is typically an optimisation step when nothing has
     * changed in user land, so there's no processing that needs to be done.
     * Skip the processing and tell the drawable surface to render again what
     * it already has set from the previous frame.
     * <p>
     * The return value indicates success or failure in the ability to
     * render this frame. Typically it will indicate failure if the
     * underlying surface has been disposed of, either directly through the
     * calling of the method on this interface, or through an internal check
     * mechanism. If failure is indicated, then check to see if the surface has
     * been disposed of and discontinue rendering if it has.
     *
     * @return true if the drawing succeeded, or false if not
     */
    public boolean displayOnly();

    /**
     * Set the request data that should be passed along with the next frame.
     * This is temporary that is passed along at the next render() or
     * displayOnly() call and will be cleared after that.
     *
     * @param data The data instance to pass this next frame
     */
    public void setRequestData(RenderableRequestData data);

    /**
     * Set the set of layers to be used by this pipeline. Providing an argument
     * of a zero number of layers remove the layers from being rendered,
     * causing the pipeline to function as a no-op when rendered.
     * <p>
     *
     * If a scene is currently set, and a non-zero number of layers is
     * provided, this will remove the scene and use the layers instead.
     *
     * @param layers The collection of layers, in order, to render
     * @param numLayers The number of valid layers to use
     */
    public void setRenderableLayers(LayerCullable[] layers, int numLayers);

    /**
     * Force a halt of the current processing. Any processing in progress
     * should exit immediately. Used to abort the current scene processing due
     * to application shutdown or complete scene replacement.
     */
    public void halt();
}
