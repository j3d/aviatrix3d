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
 * Unit tests for the texture update data holder
 *
 * @author justin
 */
public class TextureUpdateDataTest
{
    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        TextureUpdateData class_under_test = new TextureUpdateData();

        assertEquals(class_under_test.x, 0, "X texel should be zero");
        assertEquals(class_under_test.y, 0, "Y texel should be zero");
        assertEquals(class_under_test.z, 0, "Z texel should be zero");

        assertEquals(class_under_test.width, 0, "Texture width should be zero");
        assertEquals(class_under_test.height, 0, "Texture height should be zero");
        assertEquals(class_under_test.depth, 0, "Texture depth should be zero");

        assertEquals(class_under_test.level, 0, "Mip-map level should be zero");
        assertEquals(class_under_test.format, 0, "No byte format should be defined");
        assertNull(class_under_test.pixels, "No pixel data should be defined");

    }
}
