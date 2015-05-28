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

package org.j3d.aviatrix3d.picking;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit test for the pick request object
 *
 * @author justin
 */
public class PickRequestTest
{
    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        PickRequest class_under_test = new PickRequest();

        assertEquals(class_under_test.pickCount, 0, "Should not have any picks");
        assertNotNull(class_under_test.destination, "Destination should be allocated");
        assertNotNull(class_under_test.origin, "Origin should be allocated");
        assertTrue(class_under_test.generateVWorldMatrix, "V-World matrix generation");
        assertFalse(class_under_test.useGeometry, "Should not use goemetry by default");
        assertEquals(class_under_test.pickSortType, 0, "No sorting by default");
        assertEquals(class_under_test.pickType, 0, "No pick type by default");
        assertEquals(class_under_test.pickGeometryType, 0, "No pick geometry by default");
    }
}
