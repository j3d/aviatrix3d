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

import java.util.Arrays;

import org.hamcrest.Matcher;

/**
 * Reimplementation of the custom matchers from Mockito for our own mocked
 * GL interfaces. We can't use the Mockito ones here because they require
 * thread-local handling and assumed verify() or when() calls to function
 * properly. This code just wraps the matchers needed to function.
 *
 * @author justin
 */
public class AV3DMatchers
{
    public static <T> Matcher avAny(final Class<T> cls)
    {
        return new ClassMatcher(cls);
    }

    public static Matcher avLong()
    {
        return new ClassMatcher(Long.class);
    }

    public static Matcher avInt()
    {
        return new ClassMatcher(Integer.class);
    }

    public static Matcher avShort()
    {
        return new ClassMatcher(Short.class);
    }

    public static Matcher avByte()
    {
        return new ClassMatcher(Byte.class);
    }

    public static Matcher avFloat()
    {
        return new ClassMatcher(Float.class);
    }

    public static Matcher avDouble()
    {
        return new ClassMatcher(Double.class);
    }

    public static Matcher avOr(Matcher... children)
    {
        return new OrMatcher(Arrays.asList(children));
    }

    public static Matcher avNull()
    {
        return new NullMatcher();
    }
}
