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

package org.j3d.aviatrix3d.management;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit tests for the privileged action.
 *
 * @author justin
 */
public class ThreadPrivilegedActionTest
{
    @Test(groups = "unit", timeOut = 60000)
    public void testBasicConstruction() throws Exception
    {
        final Object locker = new Object();

        Runnable test_runnable = new Runnable()
        {
            @Override
            public void run()
            {
                synchronized (locker)
                {
                    locker.notifyAll();
                }
            }
        };

        ThreadPrivilegedAction class_under_test = new ThreadPrivilegedAction(test_runnable);

        synchronized (locker)
        {
            locker.wait();
        }

        assertNotNull(class_under_test.getThread(), "No thread object defined in the running");
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testMissingRunnable() throws Exception
    {
        new ThreadPrivilegedAction(null);
    }
}
