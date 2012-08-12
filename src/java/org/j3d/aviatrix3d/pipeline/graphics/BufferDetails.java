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

package org.j3d.aviatrix3d.pipeline.graphics;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.rendering.BufferStateRenderable;
import org.j3d.aviatrix3d.rendering.ViewportRenderable;

/**
 * Class for passing the various rendering buffer renderable representations
 * through the pipeline.
 * <p>
 *
 * Multipass rendering require information the different buffers that can be
 * rendered to. In a lot of cases these use buffers that are not just the
 * normal colour and depth values.
 *
 * @author Justin Couch
 * @version $Revision: 3.4 $
 */
public class BufferDetails
{
    /** Renderable information about the viewport, if any */
    public ViewportRenderable viewportState;

    /** Renderable information about the general buffer */
    public BufferStateRenderable generalBufferState;

    /** Renderable information about the colour buffer */
    public BufferStateRenderable colorBufferState;

    /** Renderable information about the depth buffer */
    public BufferStateRenderable depthBufferState;

    /** Renderable information about the stencil buffer */
    public BufferStateRenderable stencilBufferState;

    /** Renderable information about the accumulation buffer */
    public BufferStateRenderable accumBufferState;
}
