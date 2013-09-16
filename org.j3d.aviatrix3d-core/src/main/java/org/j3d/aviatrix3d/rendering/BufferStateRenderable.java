/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2006
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
 * Marker describing a renderable object that is used to control one of the
 * OpenGL buffer states.
 * <p>
 *
 * The buffer state renderables are only used at the beginning of a rendering
 * pass as part of a Scene object. Buffer state renderables may be issued once
 * at the beginning of a drawing run and then never again. In multipass
 * rendering where you may want to accumulate state over multiple runs, one
 * instance may start the state, then several are used to change the state
 * between runs, and finally it is cleared at the end of  the last run.
 *
 * @author Justin Couch
 * @version $Revision: 2.2 $
 */
public interface BufferStateRenderable extends Renderable
{
    /** The buffer state represents Accumulation buffers */
    public static final int ACCUMULATION_BUFFER = 1;

    /** The buffer state represents stencil buffers */
    public static final int STENCIL_BUFFER = 2;

    /** The buffer state represents depth buffers */
    public static final int DEPTH_BUFFER = 3;

    /** The buffer state represents colour buffers */
    public static final int COLOR_BUFFER = 4;

    /** The buffer state represents general buffer management */
    public static final int GENERAL_BUFFER = 5;

    /**
     * Get the type of buffer this state represents.
     *
     * @return One of the _BUFFER constants
     */
    public int getBufferType();

    /**
     * Get the GL buffer bit flag that this state class represents. Used for
     * bulk clearing all the states at once.
     *
     * @return The bit state constant for Stencil Buffers
     */
    public int getBufferBitMask();

    /**
     * Check to see if this buffer should be cleared at the start of this run.
     * If it should be, add the bit mask from this state to the global list.
     *
     * @return true if the state should be cleared
     */
    public boolean checkClearBufferState();

    /**
     * Issue ogl commands needed for this buffer to set the initial state,
     * including the initial enabling.
     *
     * @param gl The gl context to draw with
     */
    public void setBufferState(GL gl);

    /**
     * Issue ogl commands needed for this component to change the state,
     * assuming that it is already enabled.
     *
     * @param gl The gl context to draw with
     */
    public void updateBufferState(GL gl);

    /**
     * Restore all state to the default values.
     *
     * @param gl The gl context to draw with
     */
    public void clearBufferState(GL gl);
}
