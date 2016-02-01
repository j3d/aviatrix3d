/*****************************************************************************
 *                   Yumetech, Inc Copyright (c) 2004 - 2007
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
import org.j3d.util.ErrorReporter;
import org.j3d.aviatrix3d.rendering.ProfilingData;

/**
 * Interface representing the output of a render pipeline.
 * <p>
 *
 * The output may be any of the traditional types: pBuffer, screen or memory
 * or any non-traditional type like haptic devices, network streams etc.
 *
 *
 * @author Justin Couch
 * @version $Revision: 2.9 $
 */
public interface OutputDevice
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
     * Instruct the surface to draw the collected set of nodes now. The
     * registered view environment is used to draw to this surface. If no
     * view is registered, the surface is cleared and then this call is
     * exited. The drawing surface does not swap the buffers at this point.
     * <p>
     * The return value indicates success or failure in the ability to
     * render this frame. Typically it will indicate failure if the
     * underlying surface has been disposed of, either directly through the
     * calling of the method on this interface, or through an internal check
     * mechanism. If failure is indicated, then check to see if the surface has
     * been disposed of and discontinue rendering if it has.
     *
     * @param profilingData The timing and load data
     * @return true if the drawing succeeded, or false if not
     */
    public boolean draw(ProfilingData profilingData);

    /**
     * Get the underlying object that this implementation is rendered to. If it
     * is a screen display device, the surface can be one of AWT Component or
     * Swing JComponent. An off-screen buffer would be a form of AWT Image etc.
     * Audio or haptics rendering will return output-specific objects.
     *
     * @return The drawable surface representation
     */
    public Object getSurfaceObject();

    /**
     * Instruct this surface that you have finished with the resources needed
     * and to dispose all rendering resources. The surface will not be used
     * again.
     */
    public void dispose();

    /**
     * Check to see the disposal state of the surface. Will return true if the
     * {@link #dispose} method has been called or an internal dispose handler
     * has detected the underlying surface is no longer valid to draw to.
     *
     * @return true if the surface is disposed and no longer usable
     */
    public boolean isDisposed();
}
