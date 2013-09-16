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

package org.j3d.aviatrix3d.pipeline.graphics;

// External imports
import java.text.MessageFormat;
import java.util.Locale;

import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;
import org.j3d.util.I18nManager;

// Local imports
// None

/**
 * A class which implements efficient and thread-safe multi-cast event
 * dispatching for the events defined in this package.
 * <P>
 *
 * This class will manage an immutable structure consisting of a chain of
 * event listeners and will dispatch events to those listeners.  Because
 * the structure is immutable, it is safe to use this API to add/remove
 * listeners during the process of an event dispatch operation.
 * <P>
 *
 * An example of how this class could be used to implement a new
 * component which fires resize events:
 *
 * <pre><code>
 * public myComponent extends Component {
 *   GraphicsResizeListener nodeListener = null;
 *
 *   public void addNodeListener(GraphicsResizeListener l) {
 *     nodeListener = GraphicsListenerMulticaster.add(nodeListener, l);
 *   }
 *
 *   public void removeNodeListener(GraphicsResizeListener l) {
 *     nodeListener = GraphicsListenerMulticaster.remove(nodeListener, l);
 *   }
 *
 *   public void graphicsDeviceResized(int x, int y, int w, int h) {
 *     if(nodeListener != null) {
 *       nodeListener.fieldgraphicsDeviceResizedChanged(x, y, w, h);
 *   }
 * }
 * </code></pre>
 *
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>listenerExceptionMsg: Error message when there was a user-land exception
 *     sending the resize callback</li>
 * </ul>
 *
 * @author  Justin Couch
 * @version $Revision: 3.1 $
 */
public class GraphicsListenerMulticaster implements GraphicsResizeListener
{
    /** Error message when the user code barfs */
    private static final String SIZE_ERROR_PROP =
		"org.j3d.aviatrix3d.pipeline.graphics.GraphicsListenerMulticaster.listenerExceptionMsg";

    /** The node listeners in use by this class */
    private final GraphicsResizeListener a, b;

    /** Reporter instance for handing out errors */
    private static ErrorReporter errorReporter =
        DefaultErrorReporter.getDefaultReporter();

    /**
     * Creates an event multicaster instance which chains listener-a
     * with listener-b. Input parameters <code>a</code> and <code>b</code>
     * should not be <code>null</code>, though implementations may vary in
     * choosing whether or not to throw <code>NullPointerException</code>
     * in that case.
     * @param a listener-a
     * @param b listener-b
     */
    public GraphicsListenerMulticaster(GraphicsResizeListener a,
                                       GraphicsResizeListener b)
    {
        this.a = a;
        this.b = b;
    }

    /**
     * Removes a listener from this multicaster and returns the
     * resulting multicast listener.
     * @param oldl the listener to be removed
     */
    public GraphicsResizeListener remove(GraphicsResizeListener oldl)
    {

        if(oldl == a)
            return b;

        if(oldl == b)
            return a;

        GraphicsResizeListener a2 = removeInternal(a, oldl);
        GraphicsResizeListener b2 = removeInternal(b, oldl);

        if(a2 == a && b2 == b)
            return this;  // it's not here

        return addInternal(a2, b2);
    }

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public static void setErrorReporter(ErrorReporter reporter)
    {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Adds input-method-listener-a with input-method-listener-b and
     * returns the resulting multicast listener.
     * @param a input-method-listener-a
     * @param b input-method-listener-b
     */
    public static GraphicsResizeListener add(GraphicsResizeListener a,
                                             GraphicsResizeListener b)
    {
        return addInternal(a, b);
    }

    /**
     * Removes the old component-listener from component-listener-l and
     * returns the resulting multicast listener.
     * @param l component-listener-l
     * @param oldl the component-listener being removed
     */
    public static GraphicsResizeListener remove(GraphicsResizeListener l,
                                                GraphicsResizeListener oldl)
    {
        return removeInternal(l, oldl);
    }

    /**
     * Notification that the graphics output device has changed dimensions to
     * the given size. Dimensions are in pixels.
     *
     * @param x The lower left x coordinate for the view
     * @param y The lower left y coordinate for the view
     * @param width The width of the viewport in pixels
     * @param height The height of the viewport in pixels
     */
    public void graphicsDeviceResized(int x, int y, int width, int height)
    {
        try
        {
            a.graphicsDeviceResized(x, y, width, height);
        }
        catch(Throwable th)
        {
            if(th instanceof Exception)
			{
				I18nManager intl_mgr = I18nManager.getManager();
				Locale lcl = intl_mgr.getFoundLocale();
				String msg_pattern = intl_mgr.getString(SIZE_ERROR_PROP);

				Object[] msg_args = { a.getClass().getName() };
				MessageFormat msg_fmt =
					new MessageFormat(msg_pattern, lcl);
				String msg = msg_fmt.format(msg_args);

				errorReporter.errorReport(msg, (Exception)th);
			}
            else
            {
                System.out.println("GraphicsResizeListenerMulticaster: Unknown BAAAAD error: " + th);
                th.printStackTrace();
            }
        }

        try
        {
            b.graphicsDeviceResized(x, y, width, height);
        }
        catch(Throwable th)
        {
            if(th instanceof Exception)
			{
				I18nManager intl_mgr = I18nManager.getManager();
				Locale lcl = intl_mgr.getFoundLocale();
				String msg_pattern = intl_mgr.getString(SIZE_ERROR_PROP);

				Object[] msg_args = { b.getClass().getName() };
				MessageFormat msg_fmt =
					new MessageFormat(msg_pattern, lcl);
				String msg = msg_fmt.format(msg_args);

				errorReporter.errorReport(msg, (Exception)th);
			}
            else
            {
                System.out.println("GraphicsResizeListenerMulticaster: Unknown BAAAAD error: " + th);
                th.printStackTrace();
            }
        }
    }

    /**
     * Returns the resulting multicast listener from adding listener-a
     * and listener-b together.
     * If listener-a is null, it returns listener-b;
     * If listener-b is null, it returns listener-a
     * If neither are null, then it creates and returns
     * a new NodeListenerMulticaster instance which chains a with b.
     * @param a event listener-a
     * @param b event listener-b
     */
    private static GraphicsResizeListener addInternal(GraphicsResizeListener a,
                                                      GraphicsResizeListener b)
    {
        if(a == null)
            return b;

        if(b == null)
            return a;

        return new GraphicsListenerMulticaster(a, b);
    }

    /**
     * Returns the resulting multicast listener after removing the
     * old listener from listener-l.
     * If listener-l equals the old listener OR listener-l is null,
     * returns null.
     * Else if listener-l is an instance of NodeListenerMulticaster,
     * then it removes the old listener from it.
     * Else, returns listener l.
     * @param l the listener being removed from
     * @param oldl the listener being removed
     */
    private static GraphicsResizeListener removeInternal(GraphicsResizeListener l,
                                 GraphicsResizeListener oldl)
    {
        if (l == oldl || l == null)
            return null;
        else if (l instanceof GraphicsListenerMulticaster)
            return ((GraphicsListenerMulticaster)l).remove(oldl);
        else
            return l;   // it's not here
    }
}
