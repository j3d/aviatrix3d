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

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.text.MessageFormat;
import java.util.Locale;

import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;
import org.j3d.util.I18nManager;

import org.j3d.aviatrix3d.pipeline.graphics.GraphicsListenerMulticaster;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsResizeListener;

// Local imports

/**
 * Internal handles for sending out AWT resize events to our graphics resize
 * listeners.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>listenerExceptionMsg: Error message when there was a user-land exception
 *     sending the resize callback</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 3.3 $
 */
class GLResizeHandler
{
    /** Error message when the user code barfs */
    private static final String SIZE_ERROR_PROP =
        "org.j3d.aviatrix3d.output.graphics.GLResizeHandler.listenerExceptionMsg";

    /** Listeners for resize events */
    private GraphicsResizeListener listeners;

    /** Error reporter used to send out messages */
    private ErrorReporter errorReporter;

    /** The last recorded width of the component, in pixels */
    private int currentWidth;

    /** The last recorded height of the component, in pixels */
    private int currentHeight;

    /**
     * Construct handler for rendering objects to the main screen.
     */
    GLResizeHandler()
    {
        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the node's internals can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    void setErrorReporter(ErrorReporter reporter)
    {
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter = reporter;
    }

    /**
     * Add a resize listener instance to this surface. Duplicate listener
     * instance add requests are ignored, as are null values.
     *
     * @param l The new listener instance to add
     */
    void addGraphicsResizeListener(GraphicsResizeListener l)
    {
        listeners = GraphicsListenerMulticaster.add(listeners, l);
    }

    /**
     * Remove a resize listener from this surface. If the listener is not
     * currently registered the request is ignored.
     *
     * @param l The listener instance to remove
     */
    void removeGraphicsResizeListener(GraphicsResizeListener l)
    {
        listeners = GraphicsListenerMulticaster.remove(listeners, l);
    }

    /**
     * Fire off the resize event based on the component resizing.
     *
     */
    void fireResizeEvent(int x, int y, int width, int height)
    {
        if(listeners == null)
            return;

        try
        {
            // Always 0,0 for the X,Y location here because we're always
            // working in a full window size. The X and Y are the
            // coordinates relative to the parent component's coordinate
            // space.
            listeners.graphicsDeviceResized(x, y, width, height);
        }
        catch(Exception e)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            Locale lcl = intl_mgr.getFoundLocale();
            String msg_pattern = intl_mgr.getString(SIZE_ERROR_PROP);

            Object[] msg_args = { listeners.getClass().getName() };

            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);

            String msg = msg_fmt.format(msg_args);

            errorReporter.errorReport(msg, e);
        }
    }
}
