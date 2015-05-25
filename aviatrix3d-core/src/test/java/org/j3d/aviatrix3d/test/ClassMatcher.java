/*
 * **************************************************************************
 *                        Copyright j3d.org (c) 2000 - ${year}
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read docs/lgpl.txt for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * **************************************************************************
 */

package org.j3d.aviatrix3d.test;

/**
 * Matches a class of a specific type and cannot be null
 *
 * @author justin
 */
class ClassMatcher extends BaseAV3DMatcher
{
    /** The target class type that we want to match any of the incoming argument to */
    private final Class desiredClass;

    ClassMatcher(Class cls)
    {
        desiredClass = cls;
    }

    // ----- Methods defined by Matcher --------------------------------------

    @Override
    public boolean matches(Object o)
    {
        return o != null && desiredClass.isAssignableFrom(o.getClass());
    }

    // ----- Methods defined by Object ---------------------------------------

    @Override
    public String toString()
    {
        return "a match for class type " + desiredClass.getSimpleName();
    }
}
