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

package org.j3d.aviatrix3d;

// External imports
import com.jogamp.opengl.GL;

// Local imports
// None

/**
 * An observer and operator for working with multipass textures.
 * <p>
 *
 * The observer is used to interact directly in a multipass rendering process.
 * In multipass techniques, particularly those using the accumulation buffer,
 * more than one pass through a specific set of data is used. This observer is
 * used to allow application data to interact individually with each pass -
 * for example to jitter the viewpoint for each pass.
 * <p>
 *
 * This observer is not always required for multipass texture rendering. If it
 * is not provided, then a single pass is rendered. You only need to implement
 * and provide this if you need more than one pass.
 * <p>
 *
 * Multipass rendering does not get processed in a stereo environment. Any
 * multipass taking place is assumed to be written into the monoscopic back
 * buffer before enabling the stereo processing for the final output.
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public interface MultipassRenderObserver
{
    /**
     * Request how many passes this instance requires to be run.
     *
     * @return A number greater than or equal to zero
     */
    public int numPassesRequired();

    /**
     * Notification that the given pass number is about to start.
     *
     * @param passNumber The index of the pass about to be executed
     * @param mipmapLevel The level of mipmap being drawn 0 for the full
     *   detail and positive integers from there
     * @param gl The gl context to draw with
     */
    public void beginRenderPass(GL gl, int passNumber, int mipmapLevel);

    /**
     * Notification that the given pass number has just finished.
     *
     * @param passNumber The index of the pass just finished
     * @param mipmapLevel The level of mipmap being drawn 0 for the full
     *   detail and positive integers from there
     * @param gl The gl context to draw with
     */
    public void endRenderPass(GL gl, int passNumber, int mipmapLevel);
}
