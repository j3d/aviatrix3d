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

package org.j3d.aviatrix3d.rendering;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit tests for the renderable instruction data holder
 *
 * @author justin
 */
public class RenderableInstructionsTest
{
    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        RenderableInstructions class_under_test = new RenderableInstructions();
        assertNotNull(class_under_test.localTransform, "Tranform matrix not constructed");
        assertNull(class_under_test.instructions, "Instructions defined by default");
        assertFalse(class_under_test.hasTransform, "Should not have a transform");
    }
}
