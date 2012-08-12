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
import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLAutoDrawable;

// Local imports
import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;

/**
 * Internal class that deals with the current state of the canvas and whether
 * it should have a valid context right now.
 * <p>
 *
 * Based on concepts originally developed by Benoit Vautrin of EDF France.
 * See {@link http://bugzilla.j3d.org/show_bug.cgi?id=28 Bug #28}
 * for more details.
 *
 * @author Justin Couch
 * @version $Revision: 3.3 $
 */
class AWTSurfaceMonitor extends ComponentAdapter
    implements SurfaceMonitor, HierarchyListener, GLEventListener
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
    AWTSurfaceMonitor()
    {
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        visible = false;
        invalidContext = true;
    }

    //---------------------------------------------------------------
    // Methods defined by HierarchyListener
    //---------------------------------------------------------------

    /**
     * Called by the AWT component when something changes in the component
     * heirarchy that this object is a descendent of.
     *
     * @param evt The event that caused this method to be called
     */
    public void hierarchyChanged(HierarchyEvent evt)
    {
        if((evt.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0)
        {
            Component c = (Component)evt.getSource();
            visible = c.isVisible();
        }
    }

    //---------------------------------------------------------------
    // Methods defined by ComponentListener
    //---------------------------------------------------------------

    /**
     * Notifcation of the resize of the main component
     *
     * @param evt The event that caused this method to be called
     */
    public void componentResized(ComponentEvent evt)
    {
        invalidContext = true;
    }

    //---------------------------------------------------------------
    // Methods defined by GLEventListener
    //---------------------------------------------------------------

    /**
     * Called by the drawable to initiate OpenGL rendering by the client.
     *
     * @param drawable The surface that caused this event
     */
    public void display(GLAutoDrawable drawable)
    {
    }

    /**
     * Called by the drawable when the display mode or the display device
     * associated with the GLAutoDrawable has changed.
     *
     * @param drawable The surface that caused this event
     */
    public void displayChanged(GLAutoDrawable drawable,
                               boolean modeChanged,
                               boolean deviceChanged)
    {
        invalidContext = true;
    }

    /**
     * Called by the drawable immediately after the OpenGL context is
     * initialized.
     *
     * @param drawable The surface that caused this event
     */
    public void init(GLAutoDrawable drawable)
    {
        invalidContext = true;
    }

    /**
     * Called by the drawable during the first repaint after the component
     * has been resized.
     *
     * @param drawable The surface that caused this event
     * @param x The x position that the reshape starts at
     * @param y The y position that the reshape starts at
     * @param width The width, in pixels, of the new drawable shape
     * @param height The height, in pixels, of the new drawable shape
     */
    public void reshape(GLAutoDrawable drawable,
                        int x,
                        int y,
                        int width,
                        int height)
    {
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
     * Check to see if the surface is visible.
     */
    public boolean isVisible()
    {
        return visible;
    }

    /**
     * Check to see if we should generate a new context now.
     */
    public boolean requiresNewContext()
    {
       return invalidContext;
    }
}
