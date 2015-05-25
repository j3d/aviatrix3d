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

import java.util.List;

import org.hamcrest.Matcher;

/**
 * Matches a null as a valid argument
 *
 * @author justin
 */
class NullMatcher extends BaseAV3DMatcher
{
    NullMatcher()
    {
    }

    // ----- Methods defined by Matcher --------------------------------------

    @Override
    public boolean matches(Object o)
    {
        return o == null;
    }

    // ----- Methods defined by Object ---------------------------------------

    @Override
    public String toString()
    {
        return "a null value";
    }
}
