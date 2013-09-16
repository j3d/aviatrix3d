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

package org.j3d.aviatrix3d.output.graphics;

// External imports
// None

// Local imports
import org.j3d.util.ErrorReporter;

/**
 * Internal class that deals with the current state of the canvas and whether
 * it should have a valid context right now.
 * <p>
 *
 * Based on concepts originally developed by Benoit Vautrin of EDF France.
 * See <a href="http://bugzilla.j3d.org/show_bug.cgi?id=28">Bug #28</a>
 * for more details.
 *
 * @author Justin Couch
 * @version $Revision: 3.1 $
 */
public interface SurfaceMonitor
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
     * Check to see if the surface is visible. If it is not visible, we don't
     * want to do any rendering.
     *
     * @return True when the surface is really visible
     */
    public boolean isVisible();

    /**
     * Check to see if we should generate a new context now.
     *
     * @return true when the conditions require grabbing a new context
     */
    public boolean requiresNewContext();
}
