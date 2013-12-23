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

package org.j3d.aviatrix3d.pipeline.audio;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.pipeline.RenderPipeline;

/**
 * A marker interface that represents a single complete audio rendering
 * pipeline.
 * <p>
 *
 * A pipeline represents all of the steps that may be accomplished
 * within a rendering cycle - culling, sorting and drawing. While an end-user
 * may wish to directly call the methods on this interface directly to control
 * their own rendering, it is recommended that a dedicated pipeline manager be
 * used for this task.
 * <p>
 *
 * If the pipeline does not have a audio device registered, it will still
 * complete all the steps up to that point. If no scene is registered, no
 * functionality is performed - render() will return immediately.
 *
 * @author Alan Hudson
 * @version $Revision: 2.0 $
 */
public interface AudioRenderPipeline extends RenderPipeline
{
    /**
     * Register an audio output device that this pipeline will send its output
     * to. Setting a null value will remove the current device.
     *
     * @param device The device instance to use or replace
     */
    public void setAudioOutputDevice(AudioOutputDevice device);

    /**
     * Get the currently registered audio device instance. If none is set,
     * return null.
     *
     * @return The currently set device instance or null
     */
    public AudioOutputDevice getAudioOutputDevice();
}
