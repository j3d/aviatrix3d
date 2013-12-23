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

package org.j3d.renderer.aviatrix3d.swt.output;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.output.graphics.SurfaceMonitor;
import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;

/**
 * Internal class that deals with the current state of the canvas and whether
 * it should have a valid context right now for SWT surfaces.
 * <p>
 *
 * Based on concepts originally developed by Benoit Vautrin of EDF France.
 * See {@link http://bugzilla.j3d.org/show_bug.cgi?id=28 Bug #28}
 * for more details.
 *
 * <b>Note</b>: Currently does nothing as I need to work on the SWT handling
 * requirements that may need something more specific.
 *
 * @author Justin Couch
 * @version $Revision: 3.1 $
 */
class SWTSurfaceMonitor implements SurfaceMonitor
{
    /** Is the watched component currently visible. */
    private boolean visible;

    /** Has the current GL context been rendered invalid by surface size changes */
    private boolean invalidContext;

    /** Error reporter used to send out messages */
    private ErrorReporter errorReporter;

    /**
     * Construct handler for rendering objects to the main screen.
     */
    SWTSurfaceMonitor()
    {
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        visible = true;
        invalidContext = true;
    }

    //---------------------------------------------------------------
    // Methods defined by SurfaceMonitor
    //---------------------------------------------------------------

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the node's internals can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter)
    {
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter = reporter;
    }

    /**
     * Check to see if the surface is visible. If it is not visible, we don't
     * want to do any rendering.
     *
     * @return True when the surface is really visible
     */
    public boolean isVisible()
    {
        // Always return true for now until we work out if anything
        // equivalent to the AWT version is needed
        return true;
    }

    /**
     * Check to see if we should generate a new context now.
     *
     * @return true when the conditions require grabbing a new context
     */
    public boolean requiresNewContext()
    {
        // Always return false for now until we work out if anything
        // equivalent to the AWT version is needed
        return false;
    }
}
