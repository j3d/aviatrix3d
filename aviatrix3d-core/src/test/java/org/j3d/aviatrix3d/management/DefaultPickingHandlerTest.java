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

import java.util.List;

import org.j3d.maths.vector.Matrix4d;
import org.j3d.util.ErrorReporter;
import org.j3d.util.I18nManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import org.j3d.aviatrix3d.BoundingBox;
import org.j3d.aviatrix3d.SceneGraphPath;
import org.j3d.aviatrix3d.picking.*;

/**
 * Unit tests for the default picking handler
 *
 * @author justin
 */
public class DefaultPickingHandlerTest
{
    @BeforeMethod(groups = "unit")
    public void setupTests() throws Exception
    {
        I18nManager intl_mgr = I18nManager.getManager();
        intl_mgr.setApplication("DefaultPickingHandlerTest", "config.i18n.org-j3d-aviatrix3d-resources-core");
    }

    // ------ General tests --------------------------------------------------

    @Test(groups = "unit", dataProvider = "pick types")
    public void testPickNoMaskMatch(int geometryType) throws Exception
    {
        PickRequest test_request = new PickRequest();
        test_request.pickGeometryType = geometryType;

        // Doesn't matter what these are for this test
        test_request.pickSortType = PickRequest.SORT_ANY;
        test_request.pickType = PickRequest.FIND_ALL;

        PickTarget mock_target = mock(PickTarget.class);
        when(mock_target.checkPickMask(anyInt())).thenReturn(false);

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);

        assertEquals(test_request.pickCount, 0, "Unexpected intersection");

        verify(mock_target, times(1)).checkPickMask(anyInt());
        verifyNoMoreInteractions(mock_target);
    }

    @Test(groups = "unit", dataProvider = "pick options")
    public void testAllSinglePick(int geometryType, float[] origin, float[] destination, float additionalData,
                                  int sortType, int pickType) throws Exception
    {
        PickRequest test_request = new PickRequest();
        test_request.pickGeometryType = geometryType;
        test_request.pickSortType = sortType;
        test_request.pickType = pickType;

        if(origin != null)
        {
            test_request.origin[0] = origin[0];
            test_request.origin[1] = origin[1];
            test_request.origin[2] = origin[2];
        }

        if(destination != null)
        {
            test_request.destination[0] = destination[0];
            test_request.destination[1] = destination[1];
            test_request.destination[2] = destination[2];
        }

        test_request.additionalData = additionalData;

        PickTarget mock_child = mock(LeafPickTarget.class);
        when(mock_child.checkPickMask(pickType)).thenReturn(true);
        when(mock_child.getPickTargetType()).thenReturn(PickTarget.LEAF_PICK_TYPE);
        when(mock_child.getPickableBounds()).thenReturn(new BoundingBox());

        SinglePickTarget mock_target = mock(SinglePickTarget.class);
        when(mock_target.checkPickMask(pickType)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.SINGLE_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());
        when(mock_target.getPickableChild()).thenReturn(mock_child);

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);

        assertEquals(test_request.pickCount, 1, "Expected an intersection");

        if(sortType == PickRequest.SORT_ALL || sortType == PickRequest.SORT_ORDERED)
            assertTrue(test_request.foundPaths instanceof List, "Bulk sort should return a list of paths");
        else
            assertTrue(test_request.foundPaths instanceof SceneGraphPath, "Single sort should return a path");

        // Can't test this as the mock does not implement both PickTarget and Node, so the
        // check in the SceneGraphPath update() method will ignore the mocked object.
//        SceneGraphPath result_path = (SceneGraphPath)test_request.foundPaths;
//        assertEquals(result_path.getNodeCount(), 1, "Wrong number of nodes in path");
//        assertEquals(result_path.getNode(0), mock_target, "Target not in the path");
    }


    // ------- Point Picking Tests -------------------------------------------

    @Test(groups = "unit")
    public void testPointSinglePickNoChildren() throws Exception
    {
        PickRequest test_request = new PickRequest();
        test_request.pickGeometryType = PickRequest.PICK_POINT;
        test_request.pickSortType = PickRequest.SORT_ANY;
        test_request.pickType = PickRequest.FIND_ALL;

        PickTarget mock_target = mock(SinglePickTarget.class);
        when(mock_target.checkPickMask(PickRequest.FIND_ALL)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.SINGLE_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);

        assertEquals(test_request.pickCount, 0, "Should not have an intersection");
    }

    @Test(groups = "unit", dataProvider = "pick target types")
    public void testPointPickNoIntersection(Class<PickTarget> pickClass, int targetType) throws Exception
    {
        PickRequest test_request = new PickRequest();
        test_request.pickGeometryType = PickRequest.PICK_POINT;
        test_request.pickSortType = PickRequest.SORT_ANY;
        test_request.pickType = PickRequest.FIND_ALL;
        test_request.origin[0] = 3.0f;  // outside the +/- 1.0 bounding box

        PickTarget mock_target = mock(pickClass);
        when(mock_target.checkPickMask(PickRequest.FIND_ALL)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(targetType);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);

        assertEquals(test_request.pickCount, 0, "Should not have found intersection");
    }

    @Test(groups = "unit")
    public void testPointLeafPick() throws Exception
    {
        PickRequest test_request = new PickRequest();
        test_request.pickGeometryType = PickRequest.PICK_POINT;
        test_request.pickSortType = PickRequest.SORT_ANY;
        test_request.pickType = PickRequest.FIND_ALL;

        PickTarget mock_target = mock(LeafPickTarget.class);
        when(mock_target.checkPickMask(PickRequest.FIND_ALL)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.LEAF_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);

        assertEquals(test_request.pickCount, 1, "Expected an intersection");
        assertTrue(test_request.foundPaths instanceof SceneGraphPath, "Not instance of scene graph path");


        // Can't test this as the mock does not implement both PickTarget and Node, so the
        // check in the SceneGraphPath update() method will ignore the mocked object.
//        SceneGraphPath result_path = (SceneGraphPath)test_request.foundPaths;
//        assertEquals(result_path.getNodeCount(), 1, "Wrong number of nodes in path");
//        assertEquals(result_path.getNode(0), mock_target, "Target not in the path");
    }

    @Test(groups = "unit", dataProvider = "single child pick options")
    public void testPointCustomPick(int sortType, int pickType) throws Exception
    {
        // Tests single level custom picker. Need separate test for nested custom pick
        // handling.

        PickRequest test_request = new PickRequest();
        test_request.pickGeometryType = PickRequest.PICK_POINT;
        test_request.pickSortType = sortType;
        test_request.pickType = pickType;

        CustomPickTarget mock_target = mock(CustomPickTarget.class);
        when(mock_target.checkPickMask(pickType)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.CUSTOM_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);

        verify(mock_target, times(1)).pickChildren(any(PickInstructions.class), any(Matrix4d.class), eq(test_request));
    }

    @Test(groups = "unit")
    public void testPointPickInvalidSortType() throws Exception
    {
        PickRequest test_request = new PickRequest();
        test_request.pickGeometryType = PickRequest.PICK_POINT;
        test_request.pickSortType = -300;
        test_request.pickType = PickRequest.FIND_ALL;

        PickTarget mock_target = mock(LeafPickTarget.class);
        when(mock_target.checkPickMask(PickRequest.FIND_ALL)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.LEAF_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());

        ErrorReporter mock_reporter = mock(ErrorReporter.class);

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.setErrorReporter(mock_reporter);
        class_under_test.pickSingle(mock_target, test_request);

        assertEquals(test_request.pickCount, 0, "Should not have found intersection");
        verify(mock_reporter, atLeast(1)).warningReport(anyString(), any(Throwable.class));
    }


    // ------  Ray Picking Tests ---------------------------------------------

    @Test(groups = "unit")
    public void testRayPickNoIntersection() throws Exception
    {
        PickRequest test_request = new PickRequest();
        test_request.pickGeometryType = PickRequest.PICK_RAY;
        test_request.pickSortType = PickRequest.SORT_ANY;
        test_request.pickType = PickRequest.FIND_ALL;
        test_request.origin[0] = 3.0f;  // outside the +/- 1.0 bounding box
        test_request.destination[1] = 1.0f;

        PickTarget mock_target = mock(LeafPickTarget.class);
        when(mock_target.checkPickMask(PickRequest.FIND_ALL)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.LEAF_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);

        assertEquals(test_request.pickCount, 0, "Should not have found intersection");
    }

    @Test(groups = "unit")
    public void testRaySinglePickNoChildren() throws Exception
    {
        PickRequest test_request = new PickRequest();
        test_request.pickGeometryType = PickRequest.PICK_RAY;
        test_request.pickSortType = PickRequest.SORT_ANY;
        test_request.pickType = PickRequest.FIND_ALL;
        test_request.destination[1] = 1.0f;

        PickTarget mock_target = mock(SinglePickTarget.class);
        when(mock_target.checkPickMask(PickRequest.FIND_ALL)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.SINGLE_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);

        assertEquals(test_request.pickCount, 0, "Should not have an intersection");
    }

    @Test(groups = "unit", dataProvider = "single child pick options")
    public void testRayCustomPick(int sortType, int pickType) throws Exception
    {
        // Tests single level custom picker. Need separate test for nested custom pick
        // handling.

        PickRequest test_request = new PickRequest();
        test_request.pickGeometryType = PickRequest.PICK_RAY;
        test_request.pickSortType = sortType;
        test_request.pickType = pickType;
        test_request.destination[1] = 1.0f;

        CustomPickTarget mock_target = mock(CustomPickTarget.class);
        when(mock_target.checkPickMask(pickType)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.CUSTOM_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);

        verify(mock_target, times(1)).pickChildren(any(PickInstructions.class), any(Matrix4d.class), eq(test_request));
    }

    @Test(groups = "unit")
    public void testRayPickInvalidSortType() throws Exception
    {
        PickRequest test_request = new PickRequest();
        test_request.pickGeometryType = PickRequest.PICK_RAY;
        test_request.pickSortType = -300;
        test_request.pickType = PickRequest.FIND_ALL;
        test_request.destination[1] = 1.0f;

        PickTarget mock_target = mock(LeafPickTarget.class);
        when(mock_target.checkPickMask(PickRequest.FIND_ALL)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.LEAF_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());

        ErrorReporter mock_reporter = mock(ErrorReporter.class);

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.setErrorReporter(mock_reporter);
        class_under_test.pickSingle(mock_target, test_request);

        assertEquals(test_request.pickCount, 0, "Should not have found intersection");
        verify(mock_reporter, atLeast(1)).warningReport(anyString(), any(Throwable.class));
    }

    // ------  Line Segment Picking Tests ------------------------------------

    @Test(groups = "unit")
    public void testLineSegmentPickNoIntersection() throws Exception
    {
        PickRequest test_request = new PickRequest();
        test_request.pickGeometryType = PickRequest.PICK_LINE_SEGMENT;
        test_request.pickSortType = PickRequest.SORT_ANY;
        test_request.pickType = PickRequest.FIND_ALL;
        test_request.origin[0] = 3.0f;  // outside the +/- 1.0 bounding box
        test_request.destination[1] = 1.0f;

        PickTarget mock_target = mock(LeafPickTarget.class);
        when(mock_target.checkPickMask(PickRequest.FIND_ALL)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.LEAF_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);

        assertEquals(test_request.pickCount, 0, "Should not have found intersection");
    }

    @Test(groups = "unit")
    public void testLineSegmentSinglePickNoChildren() throws Exception
    {
        PickRequest test_request = new PickRequest();
        test_request.pickGeometryType = PickRequest.PICK_LINE_SEGMENT;
        test_request.pickSortType = PickRequest.SORT_ANY;
        test_request.pickType = PickRequest.FIND_ALL;
        test_request.destination[1] = 1.0f;

        PickTarget mock_target = mock(SinglePickTarget.class);
        when(mock_target.checkPickMask(PickRequest.FIND_ALL)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.SINGLE_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);

        assertEquals(test_request.pickCount, 0, "Should not have an intersection");
    }

    @Test(groups = "unit", dataProvider = "single child pick options")
    public void testLineSegmentCustomPick(int sortType, int pickType) throws Exception
    {
        // Tests single level custom picker. Need separate test for nested custom pick
        // handling.

        PickRequest test_request = new PickRequest();
        test_request.pickGeometryType = PickRequest.PICK_LINE_SEGMENT;
        test_request.pickSortType = sortType;
        test_request.pickType = pickType;
        test_request.destination[1] = 1.0f;

        CustomPickTarget mock_target = mock(CustomPickTarget.class);
        when(mock_target.checkPickMask(pickType)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.CUSTOM_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);

        verify(mock_target, times(1)).pickChildren(any(PickInstructions.class), any(Matrix4d.class), eq(test_request));
    }

    @Test(groups = "unit")
    public void testLineSegmentPickInvalidSortType() throws Exception
    {
        PickRequest test_request = new PickRequest();
        test_request.pickGeometryType = PickRequest.PICK_LINE_SEGMENT;
        test_request.pickSortType = -300;
        test_request.pickType = PickRequest.FIND_ALL;
        test_request.destination[1] = 1.0f;

        PickTarget mock_target = mock(LeafPickTarget.class);
        when(mock_target.checkPickMask(PickRequest.FIND_ALL)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.LEAF_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());

        ErrorReporter mock_reporter = mock(ErrorReporter.class);

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.setErrorReporter(mock_reporter);
        class_under_test.pickSingle(mock_target, test_request);

        assertEquals(test_request.pickCount, 0, "Should not have found intersection");
        verify(mock_reporter, atLeast(1)).warningReport(anyString(), any(Throwable.class));
    }

    @DataProvider(name = "single child pick options")
    public Object[][] generateSingleChildPickOptions()
    {
        return new Object[][]
        {
            { PickRequest.SORT_ALL, PickRequest.FIND_ALL },
            { PickRequest.SORT_ANY, PickRequest.FIND_ALL },
            { PickRequest.SORT_CLOSEST, PickRequest.FIND_ALL },
            { PickRequest.SORT_ORDERED, PickRequest.FIND_ALL },

            { PickRequest.SORT_ALL, PickRequest.FIND_GENERAL },
            { PickRequest.SORT_ANY, PickRequest.FIND_GENERAL },
            { PickRequest.SORT_CLOSEST, PickRequest.FIND_GENERAL },
            { PickRequest.SORT_ORDERED, PickRequest.FIND_GENERAL },
        };
    }

    @DataProvider(name = "pick types")
    public Object[][] generatePickTypeOptions()
    {
        return new Object[][]
        {
            { PickRequest.PICK_BOX },
            { PickRequest.PICK_CONE },
            { PickRequest.PICK_CONE_SEGMENT },
            { PickRequest.PICK_CYLINDER },
            { PickRequest.PICK_FRUSTUM },
            { PickRequest.PICK_LINE_SEGMENT },
            { PickRequest.PICK_POINT },
            { PickRequest.PICK_RAY },
            { PickRequest.PICK_SPHERE }
        };
    }

    @DataProvider(name = "pick target types")
    public Object[][] generatePickTargetTypes()
    {
        return new Object[][]
        {
            { LeafPickTarget.class, PickTarget.LEAF_PICK_TYPE },
            { GroupPickTarget.class, PickTarget.GROUP_PICK_TYPE },
            { CustomPickTarget.class, PickTarget.CUSTOM_PICK_TYPE },
            { SinglePickTarget.class, PickTarget.SINGLE_PICK_TYPE }
        };
    }

    @DataProvider(name = "pick options")
    public Object[][] generatePickComboOptions()
    {
        float[] direction_up = { 0, 1, 0 };
        return new Object[][]
        {
            { PickRequest.PICK_POINT, null, null, 0, PickRequest.SORT_ALL, PickRequest.FIND_ALL },
            { PickRequest.PICK_POINT, null, null, 0, PickRequest.SORT_ANY, PickRequest.FIND_ALL },
            { PickRequest.PICK_POINT, null, null, 0, PickRequest.SORT_CLOSEST, PickRequest.FIND_ALL },
            { PickRequest.PICK_POINT, null, null, 0, PickRequest.SORT_ORDERED, PickRequest.FIND_ALL },

            { PickRequest.PICK_POINT, null, null, 0, PickRequest.SORT_ALL, PickRequest.FIND_GENERAL },
            { PickRequest.PICK_POINT, null, null, 0, PickRequest.SORT_ANY, PickRequest.FIND_GENERAL },
            { PickRequest.PICK_POINT, null, null, 0, PickRequest.SORT_CLOSEST, PickRequest.FIND_GENERAL },
            { PickRequest.PICK_POINT, null, null, 0, PickRequest.SORT_ORDERED, PickRequest.FIND_GENERAL },

            { PickRequest.PICK_LINE_SEGMENT, null, direction_up, 0, PickRequest.SORT_ALL, PickRequest.FIND_ALL },
            { PickRequest.PICK_LINE_SEGMENT, null, direction_up, 0, PickRequest.SORT_ANY, PickRequest.FIND_ALL },
            { PickRequest.PICK_LINE_SEGMENT, null, direction_up, 0, PickRequest.SORT_CLOSEST, PickRequest.FIND_ALL },
            { PickRequest.PICK_LINE_SEGMENT, null, direction_up, 0, PickRequest.SORT_ORDERED, PickRequest.FIND_ALL },

            { PickRequest.PICK_LINE_SEGMENT, null, direction_up, 0, PickRequest.SORT_ALL, PickRequest.FIND_GENERAL },
            { PickRequest.PICK_LINE_SEGMENT, null, direction_up, 0, PickRequest.SORT_ANY, PickRequest.FIND_GENERAL },
            { PickRequest.PICK_LINE_SEGMENT, null, direction_up, 0, PickRequest.SORT_CLOSEST, PickRequest.FIND_GENERAL },
            { PickRequest.PICK_LINE_SEGMENT, null, direction_up, 0, PickRequest.SORT_ORDERED, PickRequest.FIND_GENERAL },

            { PickRequest.PICK_RAY, null, direction_up, 0, PickRequest.SORT_ALL, PickRequest.FIND_ALL },
            { PickRequest.PICK_RAY, null, direction_up, 0, PickRequest.SORT_ANY, PickRequest.FIND_ALL },
            { PickRequest.PICK_RAY, null, direction_up, 0, PickRequest.SORT_CLOSEST, PickRequest.FIND_ALL },
            { PickRequest.PICK_RAY, null, direction_up, 0, PickRequest.SORT_ORDERED, PickRequest.FIND_ALL },

            { PickRequest.PICK_RAY, null, direction_up, 0, PickRequest.SORT_ALL, PickRequest.FIND_GENERAL },
            { PickRequest.PICK_RAY, null, direction_up, 0, PickRequest.SORT_ANY, PickRequest.FIND_GENERAL },
            { PickRequest.PICK_RAY, null, direction_up, 0, PickRequest.SORT_CLOSEST, PickRequest.FIND_GENERAL },
            { PickRequest.PICK_RAY, null, direction_up, 0, PickRequest.SORT_ORDERED, PickRequest.FIND_GENERAL },

// JC: Disabled because BoundingSphere and BoundingBox don't implement the checkIntersectionCylinder yet
//            { PickRequest.PICK_CYLINDER, null, direction_up, 1, PickRequest.SORT_ALL, PickRequest.FIND_ALL },
//            { PickRequest.PICK_CYLINDER, null, direction_up, 1, PickRequest.SORT_ANY, PickRequest.FIND_ALL },
//            { PickRequest.PICK_CYLINDER, null, direction_up, 1, PickRequest.SORT_CLOSEST, PickRequest.FIND_ALL },
//            { PickRequest.PICK_CYLINDER, null, direction_up, 1, PickRequest.SORT_ORDERED, PickRequest.FIND_ALL },
//
//            { PickRequest.PICK_CYLINDER, null, direction_up, 1, PickRequest.SORT_ALL, PickRequest.FIND_GENERAL },
//            { PickRequest.PICK_CYLINDER, null, direction_up, 1, PickRequest.SORT_ANY, PickRequest.FIND_GENERAL },
//            { PickRequest.PICK_CYLINDER, null, direction_up, 1, PickRequest.SORT_CLOSEST, PickRequest.FIND_GENERAL },
//            { PickRequest.PICK_CYLINDER, null, direction_up, 1, PickRequest.SORT_ORDERED, PickRequest.FIND_GENERAL },

//            { PickRequest.PICK_CONE, null, direction_up, 1, PickRequest.SORT_ALL, PickRequest.FIND_ALL },
//            { PickRequest.PICK_CONE, null, direction_up, 1, PickRequest.SORT_ANY, PickRequest.FIND_ALL },
//            { PickRequest.PICK_CONE, null, direction_up, 1, PickRequest.SORT_CLOSEST, PickRequest.FIND_ALL },
//            { PickRequest.PICK_CONE, null, direction_up, 1, PickRequest.SORT_ORDERED, PickRequest.FIND_ALL },
//
//            { PickRequest.PICK_CONE, null, direction_up, 1, PickRequest.SORT_ALL, PickRequest.FIND_GENERAL },
//            { PickRequest.PICK_CONE, null, direction_up, 1, PickRequest.SORT_ANY, PickRequest.FIND_GENERAL },
//            { PickRequest.PICK_CONE, null, direction_up, 1, PickRequest.SORT_CLOSEST, PickRequest.FIND_GENERAL },
//            { PickRequest.PICK_CONE, null, direction_up, 1, PickRequest.SORT_ORDERED, PickRequest.FIND_GENERAL },

            { PickRequest.PICK_BOX, null, direction_up, 0, PickRequest.SORT_ALL, PickRequest.FIND_ALL },
            { PickRequest.PICK_BOX, null, direction_up, 0, PickRequest.SORT_ANY, PickRequest.FIND_ALL },
            { PickRequest.PICK_BOX, null, direction_up, 0, PickRequest.SORT_CLOSEST, PickRequest.FIND_ALL },
            { PickRequest.PICK_BOX, null, direction_up, 0, PickRequest.SORT_ORDERED, PickRequest.FIND_ALL },

            { PickRequest.PICK_BOX, null, direction_up, 0, PickRequest.SORT_ALL, PickRequest.FIND_GENERAL },
            { PickRequest.PICK_BOX, null, direction_up, 0, PickRequest.SORT_ANY, PickRequest.FIND_GENERAL },
            { PickRequest.PICK_BOX, null, direction_up, 0, PickRequest.SORT_CLOSEST, PickRequest.FIND_GENERAL },
            { PickRequest.PICK_BOX, null, direction_up, 0, PickRequest.SORT_ORDERED, PickRequest.FIND_GENERAL },

            { PickRequest.PICK_SPHERE, null, null, 0.5f, PickRequest.SORT_ALL, PickRequest.FIND_ALL },
            { PickRequest.PICK_SPHERE, null, null, 0.5f, PickRequest.SORT_ANY, PickRequest.FIND_ALL },
            { PickRequest.PICK_SPHERE, null, null, 0.5f, PickRequest.SORT_CLOSEST, PickRequest.FIND_ALL },
            { PickRequest.PICK_SPHERE, null, null, 0.5f, PickRequest.SORT_ORDERED, PickRequest.FIND_ALL },

            { PickRequest.PICK_SPHERE, null, null, 0.5f, PickRequest.SORT_ALL, PickRequest.FIND_GENERAL },
            { PickRequest.PICK_SPHERE, null, null, 0.5f, PickRequest.SORT_ANY, PickRequest.FIND_GENERAL },
            { PickRequest.PICK_SPHERE, null, null, 0.5f, PickRequest.SORT_CLOSEST, PickRequest.FIND_GENERAL },
            { PickRequest.PICK_SPHERE, null, null, 0.5f, PickRequest.SORT_ORDERED, PickRequest.FIND_GENERAL },
        };
    }

}
