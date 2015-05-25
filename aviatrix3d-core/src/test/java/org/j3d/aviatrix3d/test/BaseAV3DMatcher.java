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

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * Common base class for our custom matchers in order to avoid having to
 * implement a blank {@link #describeTo} method.
 *
 * @author justin
 */
abstract class BaseAV3DMatcher<T> extends BaseMatcher<T>
{
    @Override
    public void describeTo(Description description)
    {
        // Do nothing
    }
}
