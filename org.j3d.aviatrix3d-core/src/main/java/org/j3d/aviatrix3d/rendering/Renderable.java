/*****************************************************************************
 *                  Yumetech, Inc Copyright (c) 2004 - 2006
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
// None

// Local imports
// None

/**
 * Marker interface for all objects that would like to be capable of rendering
 * themself if given appropriate information.
 * <p>
 *
 * This interface should generally not be directly used. It is a base
 * description for derived types that provide the specific types of information
 * through specific methods. It is used to provide a single common interface so
 * that the implementing instance can be placed in the
 * {@link org.j3d.aviatrix3d.pipeline.RenderDetails}.
 *
 * @author Justin Couch
 * @version $Revision: 2.2 $
 */
public interface Renderable<T> extends Comparable<T>
{
    /**
     * Compare this object for equality to the given object. This provides a
     * local interface-driven access to the normal {@link java.lang.Object}
     * version of equals, without needing to cast.
     *
     * @param o The object to be compared
     * @return True if these represent the same values
     */
    public boolean equals(T o);
}
