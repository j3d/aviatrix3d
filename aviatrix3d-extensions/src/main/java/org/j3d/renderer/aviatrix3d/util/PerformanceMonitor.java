/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.renderer.aviatrix3d.util;

// External imports
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;

import org.j3d.util.I18nManager;

// Local imports
// None

/**
 * Utility class for watching the performance of the application (measured by
 * frame rate) and then issuing notifications on when to upgrade or downgrade
 * the rendering infrastructure to keep it within the goal bounds.
 * <p>
 *
 * The system collects a set of listeners that register with the monitor and
 * provide their own priority. A higher priority listener will be called
 * before a lower priority listener for updates to both upgrade and downgrade
 * their performance.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>invalidMaxFPSMsg: Error message when trying to set the max FPS with
 *     a negative value. </li>
 * <li>invalidMinFPSMsg: Error message when trying to set the min FPS with
 *     a negative value. </li>
 * <li>invertedMinMaxFPSMsg: Error message when trying to set the min or max
 *     FPS so that max is less than min</li>
 * <li>invalidUpdateMsg: Error message when trying to set a negative
 *     update interval</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class PerformanceMonitor
{
    /** Message when the update interval is invalid */
    private static final String INVALID_MAX_FPS_PROP =
        "org.j3d.renderer.aviatrix3d.util.PerformanceMonitor.invalidMaxFPSMsg";

    /** Message when the update interval is invalid */
    private static final String INVALID_MIN_FPS_PROP =
        "org.j3d.renderer.aviatrix3d.util.PerformanceMonitor.invalidMinFPSMsg";

    /** Message when the minimum and maximum FPS would be upside down */
    private static final String INVALID_MIN_MAX_FPS_PROP =
        "org.j3d.renderer.aviatrix3d.util.PerformanceMonitor.invertedMinMaxFPSMsg";

    /** Message when the update interval is invalid */
    private static final String INVALID_UPDATE_PROP =
        "org.j3d.renderer.aviatrix3d.util.PerformanceMonitor.invalidUpdateMsg";

    /**
     * How long the intervals should be between notifications to the
     * performance monitor listeners. Time is in milliseconds.
     */
    private static final int DEFAULT_UPDATE_INTERVAL = 1000;

    /**
     * Threshold below which we want to start asking the system to drop the
     * performance enhancements.
     */
    private static final float DEFAULT_MIN_FPS = 15;

    /**
     * Threshold above which we want to start asking the system to add more
     * performance enhancements if it supports it.
     */
    private static final float DEFAULT_MAX_FPS = 45;

    /** The interval at which to check for updates, in ms */
    private int updateInterval;

    /** The minimum frame rate below which we should issue a downgrade */
    private float minAcceptableFPS;

    /** The maximum frame rate below which we should issue a downgrade */
    private float maxAcceptableFPS;

    /** Time that we last sent out a performance update */
    private long lastPerformanceUpdate;

    /** Number of frames between last and current update */
    private int frameCount;

    /** Things that need to be called every frame */
    private PriorityQueue<PerformanceDataHolder> performanceListeners;


    /**
     * Create a new monitor instance.
     */
    public PerformanceMonitor()
    {
        performanceListeners = new PriorityQueue<PerformanceDataHolder>();
        minAcceptableFPS = DEFAULT_MIN_FPS;
        maxAcceptableFPS = DEFAULT_MAX_FPS;
        updateInterval = DEFAULT_UPDATE_INTERVAL;

        lastPerformanceUpdate = System.currentTimeMillis();
    }

    //---------------------------------------------------------------
    // Methods defined by ApplicationUpdateObserver
    //---------------------------------------------------------------

    /**
     * Notification that now is a good time to update the scene graph.
     */
    public void updateMetrics()
    {

        frameCount++;

        long current_time = System.currentTimeMillis();
        long time_interval = current_time - lastPerformanceUpdate;
        if(time_interval > updateInterval)
        {
            lastPerformanceUpdate = current_time;

            float fps = (float)frameCount / (time_interval * 0.001f);
            frameCount = 0;

            if(fps < minAcceptableFPS)
            {
                Iterator<PerformanceDataHolder> itr =
                    performanceListeners.iterator();

                while(itr.hasNext())
                {
                    PerformanceDataHolder pdh = itr.next();
                    if(pdh.listener.downgradePerformance())
                        break;
                }
            }
            else if((fps > maxAcceptableFPS) && (maxAcceptableFPS != 0))
            {
                Iterator<PerformanceDataHolder> itr =
                    performanceListeners.iterator();

                while(itr.hasNext())
                {
                    PerformanceDataHolder pdh = itr.next();
                    if(pdh.listener.upgradePerformance())
                        break;
                }
            }
        }
    }

    /**
     * Notification that the AV3D internal shutdown handler has detected a
     * system-wide shutdown. The aviatrix code has already terminated rendering
     * at the point this method is called, only the user's system code needs to
     * terminate before exiting here.
     */
    public void appShutdown()
    {
        // do nothing
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Get the current checking interval. Time is in milliseconds.
     *
     * @return The interval time in milliseconds
     */
    public int getUpdateInterval()
    {
        return updateInterval;
    }

    /**
     * Set the update interval. The interval must be greater than zero. Time is
     * in milliseconds.
     *
     * @param interval The new interval value to set. Must be > zero
     * @throws IllegalArgumentException The value was not positive
     */
    public void setUpdateInterval(int interval)
        throws IllegalArgumentException
    {
        if(interval < 1)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INVALID_UPDATE_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Integer(interval) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt = new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);

            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        updateInterval = interval;
    }

    /**
     * Get the current minimum acceptable frame rate.
     *
     * @return A value greater than zero
     */
    public float getMinimumFrameRate()
    {
        return minAcceptableFPS;
    }

    /**
     * Get the current minimum acceptable frame rate. A value of zero means don't
     * send the minimum framerate message
     *
     * @param fps The frames per second number
     * @throws IllegalArgumentException The value was negative or greater than
     *    or equal to the max FPS
     */
    public void setMinimumFrameRate(float fps)
        throws IllegalArgumentException
    {
        if(fps < 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INVALID_MIN_FPS_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Float(fps) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt = new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);

            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        if(fps >= maxAcceptableFPS)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INVALID_MIN_MAX_FPS_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Float(fps),
                                  new Float(maxAcceptableFPS) };

            Format[] fmts = { n_fmt, n_fmt };
            MessageFormat msg_fmt = new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);

            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        minAcceptableFPS = fps;
    }

    /**
     * Get the current maximum acceptable frame rate.
     *
     * @return A value greater than zero
     */
    public float getMaximumFrameRate()
    {
        return maxAcceptableFPS;
    }

    /**
     * Get the current maximum acceptable frame rate. A value of zero means don't
     * send the maximum framerate message
     *
     * @param fps The frames per second number
     * @throws IllegalArgumentException The value was negative or less than or
     *    equal to the minimum FPS
     */
    public void setMaximumFrameRate(float fps)
        throws IllegalArgumentException
    {
        if(fps < 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INVALID_MAX_FPS_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Float(fps) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt = new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);

            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        if(fps <= minAcceptableFPS)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INVALID_MIN_MAX_FPS_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Float(minAcceptableFPS),
                                  new Float(fps) };

            Format[] fmts = { n_fmt, n_fmt };
            MessageFormat msg_fmt = new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);

            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        maxAcceptableFPS = fps;
    }

    /**
     * Add a system performance listener to the list to be processed. Duplicate
     * entries are ignored. A higher number for the priority increases its
     * chances of being called before the other listeners for controlling
     * performance.
     *
     * @param l The new listener instance to be handled
     */
    public synchronized void addPerformanceListener(SystemPerformanceListener l,
                                                    int priority)
    {
        if(!performanceListeners.contains(l))
        {
            PerformanceDataHolder pdh = new PerformanceDataHolder(priority, l);
            performanceListeners.add(pdh);
        }
    }

    /**
     * Remove a system performance listener from the current processing list.
     * If it is not currently added, it is silently ignored.
     *
     * @param l The listener instance to be removed
     */
    public void removePerformanceListener(SystemPerformanceListener l)
    {
        performanceListeners.remove(l);
    }
}
