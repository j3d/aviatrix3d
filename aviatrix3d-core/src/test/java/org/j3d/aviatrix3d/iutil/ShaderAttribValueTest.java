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

package org.j3d.aviatrix3d.iutil;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit tests for the shader attribute value data holder
 *
 * @author justin
 */
public class ShaderAttribValueTest
{
    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        ShaderAttribValue class_under_test = new ShaderAttribValue();
        assertFalse(class_under_test.normalise, "Incorrect default normalise flag");
        assertEquals(class_under_test.size, 0, "Should have no data by default");
        assertNull(class_under_test.data, "No data buffer should be created by default");
        assertEquals(class_under_test.dataType, 0, "No data type should be defined by default");
    }

    @Test(groups = "unit")
    public void testEqualsDifferentClassType() throws Exception
    {
        ShaderAttribValue class_under_test = new ShaderAttribValue();

        assertFalse(class_under_test.equals(new Object()), "Equality should not work for Object");
    }

    @Test(groups = "unit")
    public void testEqualTwoDefaults() throws Exception
    {
        ShaderAttribValue class_under_test = new ShaderAttribValue();
        ShaderAttribValue test_class = new ShaderAttribValue();

        assertTrue(class_under_test.equals(test_class), "Two default classes should be equal");
    }

}
