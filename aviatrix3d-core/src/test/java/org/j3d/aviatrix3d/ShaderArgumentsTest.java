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

package org.j3d.aviatrix3d;

import org.j3d.util.I18nManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit tests for the ShaderArguments class
 *
 * @author justin
 */
public class ShaderArgumentsTest
{
    @BeforeMethod(groups = "unit")
    public void setupTests() throws Exception
    {
        I18nManager intl_mgr = I18nManager.getManager();
        intl_mgr.setApplication("StandardRenderingProcessorTest", "config.i18n.org-j3d-aviatrix3d-resources-core");
    }

    @Test(groups = "unit")
    public void testBasicFloatUniformRegistration() throws Exception
    {
        final String TEST_UNIFORM_NAME = "uni";
        final int TEST_UNIFORM_COUNT = 1;
        final int TEST_UNIFORM_SIZE = 2;

        ShaderArguments class_under_test = new ShaderArguments();

        // Set it the first time - should always pass
        class_under_test.setUniform(TEST_UNIFORM_NAME,
                                    TEST_UNIFORM_SIZE,
                                    new float[TEST_UNIFORM_COUNT * TEST_UNIFORM_SIZE],
                                    TEST_UNIFORM_COUNT);

        assertEquals(class_under_test.getUniformCount(TEST_UNIFORM_NAME),
                     TEST_UNIFORM_COUNT,
                     "Incorrect count found");

        assertEquals(class_under_test.getUniformSize(TEST_UNIFORM_NAME),
                     TEST_UNIFORM_SIZE,
                     "Incorrect size found");

        assertEquals(class_under_test.getUniformType(TEST_UNIFORM_NAME),
                     ShaderArguments.FLOAT_UNIFORM_TYPE,
                     "Wrong type registered, expected float");
    }

    @Test(groups = "unit")
    public void testBasicIntUniformRegistration() throws Exception
    {
        final String TEST_UNIFORM_NAME = "uni";
        final int TEST_UNIFORM_COUNT = 1;
        final int TEST_UNIFORM_SIZE = 2;

        ShaderArguments class_under_test = new ShaderArguments();

        // Set it the first time - should always pass
        class_under_test.setUniform(TEST_UNIFORM_NAME,
                                    TEST_UNIFORM_SIZE,
                                    new int[TEST_UNIFORM_COUNT * TEST_UNIFORM_SIZE],
                                    TEST_UNIFORM_COUNT);

        assertEquals(class_under_test.getUniformCount(TEST_UNIFORM_NAME),
                     TEST_UNIFORM_COUNT,
                     "Incorrect count found");

        assertEquals(class_under_test.getUniformSize(TEST_UNIFORM_NAME),
                     TEST_UNIFORM_SIZE,
                     "Incorrect size found");

        assertEquals(class_under_test.getUniformType(TEST_UNIFORM_NAME),
                     ShaderArguments.INT_UNIFORM_TYPE,
                     "Wrong type registered, expected int");
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testDataSizeChangeFromFloat() throws Exception
    {
        final String TEST_UNIFORM_NAME = "uni";
        final int TEST_UNIFORM_COUNT = 1;

        ShaderArguments class_under_test = new ShaderArguments();

        // Set it the first time - should always pass
        class_under_test.setUniform(TEST_UNIFORM_NAME, 1, new float[1], TEST_UNIFORM_COUNT);

        // Different size, should result in the exception.
        class_under_test.setUniform(TEST_UNIFORM_NAME, 2, new float[2], TEST_UNIFORM_COUNT);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testDataSizeChangeFromInt() throws Exception
    {
        final String TEST_UNIFORM_NAME = "uni";
        final int TEST_UNIFORM_COUNT = 1;

        ShaderArguments class_under_test = new ShaderArguments();

        // Set it the first time - should always pass
        class_under_test.setUniform(TEST_UNIFORM_NAME, 1, new int[1], TEST_UNIFORM_COUNT);

        // Different size, should result in the exception.
        class_under_test.setUniform(TEST_UNIFORM_NAME, 2, new int[2], TEST_UNIFORM_COUNT);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testDataSizeChangeFromMatrix() throws Exception
    {
        final String TEST_UNIFORM_NAME = "uni";
        final int TEST_UNIFORM_COUNT = 1;

        ShaderArguments class_under_test = new ShaderArguments();

        // Set it the first time - should always pass
        class_under_test.setUniformMatrix(TEST_UNIFORM_NAME, 1, new float[1], TEST_UNIFORM_COUNT, true);

        // Different size, should result in the exception.
        class_under_test.setUniformMatrix(TEST_UNIFORM_NAME, 2, new float[4], TEST_UNIFORM_COUNT, true);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testDataTypeChangeFromSamplerToInt() throws Exception
    {
        final String TEST_UNIFORM_NAME = "uni";
        final int TEST_UNIFORM_COUNT = 1;

        ShaderArguments class_under_test = new ShaderArguments();

        // Set it the first time - should always pass
        class_under_test.setUniformSampler(TEST_UNIFORM_NAME, 1);

        // Different size, should result in the exception.
        class_under_test.setUniform(TEST_UNIFORM_NAME, 1, new int[1], TEST_UNIFORM_COUNT);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testDataTypeChangeFromSamplerToFloat() throws Exception
    {
        final String TEST_UNIFORM_NAME = "uni";
        final int TEST_UNIFORM_COUNT = 1;

        ShaderArguments class_under_test = new ShaderArguments();

        // Set it the first time - should always pass
        class_under_test.setUniformSampler(TEST_UNIFORM_NAME, 1);

        // Different size, should result in the exception.
        class_under_test.setUniform(TEST_UNIFORM_NAME, 1, new float[1], TEST_UNIFORM_COUNT);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testDataTypeChangeFromSamplerToMatrix() throws Exception
    {
        final String TEST_UNIFORM_NAME = "uni";
        final int TEST_UNIFORM_COUNT = 1;

        ShaderArguments class_under_test = new ShaderArguments();

        // Set it the first time - should always pass
        class_under_test.setUniformSampler(TEST_UNIFORM_NAME, 1);

        // Different size, should result in the exception.
        class_under_test.setUniformMatrix(TEST_UNIFORM_NAME, 1, new float[1], TEST_UNIFORM_COUNT, true);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testDataSizeChangeFromFloatToInt() throws Exception
    {
        final String TEST_UNIFORM_NAME = "uni";
        final int TEST_UNIFORM_COUNT = 1;

        ShaderArguments class_under_test = new ShaderArguments();

        // Set it the first time - should always pass
        class_under_test.setUniform(TEST_UNIFORM_NAME, 1, new float[1], TEST_UNIFORM_COUNT);

        // Different size, should result in the exception.
        class_under_test.setUniform(TEST_UNIFORM_NAME, 1, new int[2], TEST_UNIFORM_COUNT);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testDataSizeChangeFromFloatToMatrix() throws Exception
    {
        final String TEST_UNIFORM_NAME = "uni";
        final int TEST_UNIFORM_COUNT = 1;

        ShaderArguments class_under_test = new ShaderArguments();

        // Set it the first time - should always pass
        class_under_test.setUniform(TEST_UNIFORM_NAME, 1, new float[1], TEST_UNIFORM_COUNT);

        // Different size, should result in the exception.
        class_under_test.setUniformMatrix(TEST_UNIFORM_NAME, 1, new float[2], TEST_UNIFORM_COUNT, true);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testDataSizeChangeFromFloatToSampler() throws Exception
    {
        final String TEST_UNIFORM_NAME = "uni";
        final int TEST_UNIFORM_COUNT = 1;

        ShaderArguments class_under_test = new ShaderArguments();

        // Set it the first time - should always pass
        class_under_test.setUniform(TEST_UNIFORM_NAME, 1, new float[1], TEST_UNIFORM_COUNT);

        // Different size, should result in the exception.
        class_under_test.setUniformSampler(TEST_UNIFORM_NAME, 1);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testDataSizeChangeFromIntToFloat() throws Exception
    {
        final String TEST_UNIFORM_NAME = "uni";
        final int TEST_UNIFORM_COUNT = 1;

        ShaderArguments class_under_test = new ShaderArguments();

        // Set it the first time - should always pass
        class_under_test.setUniform(TEST_UNIFORM_NAME, 1, new int[1], TEST_UNIFORM_COUNT);

        // Different size, should result in the exception.
        class_under_test.setUniform(TEST_UNIFORM_NAME, 1, new float[2], TEST_UNIFORM_COUNT);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testDataSizeChangeFromIntToMatrix() throws Exception
    {
        final String TEST_UNIFORM_NAME = "uni";
        final int TEST_UNIFORM_COUNT = 1;

        ShaderArguments class_under_test = new ShaderArguments();

        // Set it the first time - should always pass
        class_under_test.setUniform(TEST_UNIFORM_NAME, 1, new int[1], TEST_UNIFORM_COUNT);

        // Different size, should result in the exception.
        class_under_test.setUniformMatrix(TEST_UNIFORM_NAME, 1, new float[2], TEST_UNIFORM_COUNT, true);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testDataSizeChangeFromIntToSampler() throws Exception
    {
        final String TEST_UNIFORM_NAME = "uni";
        final int TEST_UNIFORM_COUNT = 1;

        ShaderArguments class_under_test = new ShaderArguments();

        // Set it the first time - should always pass
        class_under_test.setUniform(TEST_UNIFORM_NAME, 1, new int[1], TEST_UNIFORM_COUNT);

        // Different size, should result in the exception.
        class_under_test.setUniformSampler(TEST_UNIFORM_NAME, 1);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testDataSizeChangeFromMatrixToInt() throws Exception
    {
        final String TEST_UNIFORM_NAME = "uni";
        final int TEST_UNIFORM_COUNT = 1;

        ShaderArguments class_under_test = new ShaderArguments();

        // Set it the first time - should always pass
        class_under_test.setUniformMatrix(TEST_UNIFORM_NAME, 1, new float[1], TEST_UNIFORM_COUNT, true);

        // Different size, should result in the exception.
        class_under_test.setUniform(TEST_UNIFORM_NAME, 1, new int[4], TEST_UNIFORM_COUNT);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testDataSizeChangeFromMatrixToFloat() throws Exception
    {
        final String TEST_UNIFORM_NAME = "uni";
        final int TEST_UNIFORM_COUNT = 1;

        ShaderArguments class_under_test = new ShaderArguments();

        // Set it the first time - should always pass
        class_under_test.setUniformMatrix(TEST_UNIFORM_NAME, 1, new float[1], TEST_UNIFORM_COUNT, true);

        // Different size, should result in the exception.
        class_under_test.setUniform(TEST_UNIFORM_NAME, 1, new float[4], TEST_UNIFORM_COUNT);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testDataSizeChangeFromMatrixToSampler() throws Exception
    {
        final String TEST_UNIFORM_NAME = "uni";
        final int TEST_UNIFORM_COUNT = 1;

        ShaderArguments class_under_test = new ShaderArguments();

        // Set it the first time - should always pass
        class_under_test.setUniformMatrix(TEST_UNIFORM_NAME, 1, new float[1], TEST_UNIFORM_COUNT, true);

        // Different size, should result in the exception.
        class_under_test.setUniformSampler(TEST_UNIFORM_NAME, 1);
    }
}
