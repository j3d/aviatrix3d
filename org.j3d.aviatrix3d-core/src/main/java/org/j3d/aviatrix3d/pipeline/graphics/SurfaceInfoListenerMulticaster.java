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
import org.j3d.aviatrix3d.pipeline.OutputDevice;

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
 * <PRE><CODE>
 * public myComponent extends Component {
 *   SurfaceInfoListener nodeListener = null;
 *
 *   public void addNodeListener(SurfaceInfoListener l) {
 *     nodeListener = NodeListenerMulticaster.add(nodeListener, l);
 *   }
 *
 *   public void removeNodeListener(SurfaceInfoListener l) {
 *     nodeListener = NodeListenerMulticaster.remove(nodeListener, l);
 *   }
 *
 *   public void fireFieldChanged(int index) {
 *     if(nodeListener != null) {
 *       nodeListener.fieldChanged(index);
 *   }
 * }
 * </CODE></PRE>
 *
 * @author  Justin Couch
 * @version $Revision: 3.1 $
 */
public class SurfaceInfoListenerMulticaster implements SurfaceInfoListener
{
    /** Error message when the user code barfs */
    private static final String USER_ERROR_PROP =
        "org.j3d.aviatrix3d.pipeline.graphics.SurfaceInfoListenerMulticaster";

    /** The first listener in use by this class */
    private final SurfaceInfoListener a;

	/** The other listener managed by this instance */
    private final SurfaceInfoListener b;

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
    public SurfaceInfoListenerMulticaster(SurfaceInfoListener a,
                                          SurfaceInfoListener b)
    {
        this.a = a;
        this.b = b;
    }

    /**
     * Removes a listener from this multicaster and returns the
     * resulting multicast listener.
     * @param oldl the listener to be removed
     */
    public SurfaceInfoListener remove(SurfaceInfoListener oldl)
    {

        if(oldl == a)
            return b;

        if(oldl == b)
            return a;

        SurfaceInfoListener a2 = removeInternal(a, oldl);
        SurfaceInfoListener b2 = removeInternal(b, oldl);

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
    public static SurfaceInfoListener add(SurfaceInfoListener a,
                                             SurfaceInfoListener b)
    {
        return addInternal(a, b);
    }

    /**
     * Removes the old component-listener from component-listener-l and
     * returns the resulting multicast listener.
     * @param l component-listener-l
     * @param oldl the component-listener being removed
     */
    public static SurfaceInfoListener remove(SurfaceInfoListener l,
                                                SurfaceInfoListener oldl)
    {
        return removeInternal(l, oldl);
    }

    /**
     * Notification that the graphics output device has changed GL context
     * and this is the collection of new information.
     *
     * @param surface The output surface that caused the new info
     * @param info The collected set of information known
     */
    public void surfaceInfoChanged(OutputDevice surface, SurfaceInfo info)
    {
        try
        {
            a.surfaceInfoChanged(surface, info);
        }
        catch(Throwable th)
        {
            if(th instanceof Exception)
			{
				I18nManager intl_mgr = I18nManager.getManager();
				Locale lcl = intl_mgr.getFoundLocale();
				String msg_pattern = intl_mgr.getString(USER_ERROR_PROP);

				Object[] msg_args = { a.getClass().getName() };
				MessageFormat msg_fmt =
					new MessageFormat(msg_pattern, lcl);
				String msg = msg_fmt.format(msg_args);

				errorReporter.errorReport(msg, (Exception)th);
			}
            else
            {
                System.out.println("SurfaceInfoListenerMulticaster: Unknown BAAAAD error: " + th);
                th.printStackTrace();
            }
        }

        try
        {
            b.surfaceInfoChanged(surface, info);
        }
        catch(Throwable th)
        {
            if(th instanceof Exception)
			{
				I18nManager intl_mgr = I18nManager.getManager();
				Locale lcl = intl_mgr.getFoundLocale();
				String msg_pattern = intl_mgr.getString(USER_ERROR_PROP);

				Object[] msg_args = { b.getClass().getName() };
				MessageFormat msg_fmt =
					new MessageFormat(msg_pattern, lcl);
				String msg = msg_fmt.format(msg_args);

				errorReporter.errorReport(msg, (Exception)th);
			}
            else
            {
                System.out.println("SurfaceInfoListenerMulticaster: Unknown BAAAAD error: " + th);
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
    private static SurfaceInfoListener addInternal(SurfaceInfoListener a,
                                                      SurfaceInfoListener b)
    {
        if(a == null)
            return b;

        if(b == null)
            return a;

        return new SurfaceInfoListenerMulticaster(a, b);
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
    private static SurfaceInfoListener removeInternal(SurfaceInfoListener l,
                                 SurfaceInfoListener oldl)
    {
        if (l == oldl || l == null)
            return null;
        else if (l instanceof SurfaceInfoListenerMulticaster)
            return ((SurfaceInfoListenerMulticaster)l).remove(oldl);
        else
            return l;   // it's not here
    }
}
