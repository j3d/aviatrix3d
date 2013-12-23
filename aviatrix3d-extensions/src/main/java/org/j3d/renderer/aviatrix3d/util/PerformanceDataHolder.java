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

// External Imports
// None

// Local imports
// None

/**
 * A data holder for inserting into a priority queue.
 * <p>
 * Implements comparable so that the sorting is automatic upon inserting into
 * a priority list. Priority is based on the given priority, which a higher
 * number going to the front of the list. If two have the same priority, then
 * order is determined based on the order of creation. An instance created
 * before another will have a higher priority.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class PerformanceDataHolder implements Comparable
{

    /** Global constant for tracking creation serial numbers */
    private static int order;

    /**
     * The priority that was stored with this holder. The higher
     * the number, the greater the priority.
     */
    final int priority;


    /** The listener that this holder maintains */
    SystemPerformanceListener listener;

    /**
     * Serial number of this data holder, for resolving priority
     * conflicts.
     */
    private final int serial;

    /**
     * Create an instance of the data holder with the given
     * priority.
     *
     * @param pri The priority of this listener
     * @param l The listener instance to hold on to
     */
    PerformanceDataHolder(int pri, SystemPerformanceListener l)
    {
        priority = pri;
        listener = l;

        serial = order++;
    }

    //---------------------------------------------------------------
    // Methods defined by Comparable
    //---------------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param o The objec to be compared
     * @return -1, 0 or 1 depending on order
     * @throws ClassCastException The specified object's type prevents it from
     *    being compared to this Object
     */
    @Override
    public int compareTo(Object o)
        throws ClassCastException
    {
        PerformanceDataHolder data = (PerformanceDataHolder)o;
        if(data == this)
            return 0;

        if(data.priority < priority)
            return -1;
        else if(data.priority > priority)
            return 1;

        // if they have the same priority, just use the serial number
        if(data.serial < serial)
            return -1;
        else if(data.serial > serial)
            return 1;

        return 0;
    }

    //---------------------------------------------------------------
    // Methods defined by Object
    //---------------------------------------------------------------

    /**
     * Compare this object for equality to the given object. Comparison will
     * look for both another of the same instance, or for the listener
     * directly.
     *
     * @param o The object instance to compare against this one
     * @return True if these represent the same listener instance
     */
    @Override
    public boolean equals(Object o)
    {
        if(o instanceof PerformanceDataHolder)
        {
            PerformanceDataHolder pdh = (PerformanceDataHolder)o;
            return pdh.listener == listener;
        }
        else if(o instanceof SystemPerformanceListener)
        {
            SystemPerformanceListener l = (SystemPerformanceListener)o;
            return listener == l;
        }

        return false;
    }
}
