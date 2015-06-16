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

import java.util.Collection;
import java.util.List;

import org.j3d.maths.vector.Matrix4d;
import org.j3d.util.ErrorReporter;
import org.j3d.util.I18nManager;
import org.j3d.util.MatrixUtils;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.picking.*;
import org.j3d.aviatrix3d.rendering.BoundingVolume;

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

    @Test(groups = "unit")
    public void testNullRequest() throws Exception
    {
        PickTarget mock_target = mock(PickTarget.class);
        when(mock_target.checkPickMask(anyInt())).thenReturn(false);

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, null);

        verifyZeroInteractions(mock_target);
    }

    @Test(groups = "unit")
    public void testNullRootTarget() throws Exception
    {
        PickRequest test_request = new PickRequest();

        // Doesn't matter what these are for this test
        test_request.pickGeometryType = PickRequest.PICK_POINT;
        test_request.pickSortType = PickRequest.SORT_ANY;
        test_request.pickType = PickRequest.FIND_ALL;

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(null, test_request);

        assertEquals(test_request.pickCount, 0, "Unexpected intersection");
    }

    @Test(groups = "unit")
    public void testInvalidPickGeometryType() throws Exception
    {
        PickRequest test_request = new PickRequest();

        // Doesn't matter what these are for this test
        test_request.pickGeometryType = -30465;
        test_request.pickSortType = PickRequest.SORT_ANY;
        test_request.pickType = PickRequest.FIND_ALL;

        PickTarget mock_target = mock(PickTarget.class);
        when(mock_target.checkPickMask(anyInt())).thenReturn(true);

        ErrorReporter mock_reporter = mock(ErrorReporter.class);

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.setErrorReporter(mock_reporter);
        class_under_test.pickSingle(mock_target, test_request);

        assertEquals(test_request.pickCount, 0, "Unexpected intersection");
        verify(mock_reporter, atLeast(1)).warningReport(anyString(), any(Throwable.class));
    }


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
    public void testAllLeafPick(int geometryType, float[] origin, float[] destination, float additionalData,
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

        PickTarget mock_target = mockPickTarget(LeafPickTarget.class);
        when(mock_target.checkPickMask(pickType)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.LEAF_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);

        assertEquals(test_request.pickCount, 1, "Expected an intersection");

        SceneGraphPath result_path = null;

        if(sortType == PickRequest.SORT_ALL || sortType == PickRequest.SORT_ORDERED)
        {
            assertTrue(test_request.foundPaths instanceof List, "Bulk sort should return a list of paths");
            result_path = (SceneGraphPath)((List)test_request.foundPaths).get(0);
        }
        else
        {
            assertTrue(test_request.foundPaths instanceof SceneGraphPath, "Single sort should return a path");
            result_path = (SceneGraphPath)test_request.foundPaths;
        }

        assertNotNull(result_path, "No scene graph path actually collected");
        assertEquals(result_path.getNodeCount(), 1, "Wrong number of nodes in path");
        assertEquals(result_path.getTerminalNode(), mock_target, "Target not in the path");

        Matrix4d result_matrix = new Matrix4d();

        result_path.getTransform(result_matrix);
        assertIdentityMatrix(result_matrix);

        result_path.getInverseTransform(result_matrix);
        assertIdentityMatrix(result_matrix);
    }

    @Test(groups = "unit", dataProvider = "pick options")
    public void testPickTransformHandling(int geometryType,
                                          float[] origin,
                                          float[] destination,
                                          float additionalData,
                                          int sortType,
                                          int pickType) throws Exception
    {
        PickRequest test_request = new PickRequest();
        test_request.pickGeometryType = geometryType;
        test_request.pickSortType = sortType;
        test_request.pickType = pickType;

        // Need this true so that the matrix is created properly
        test_request.generateVWorldMatrix = true;

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

        Matrix4d test_matrix = new Matrix4d();
        test_matrix.m00 = 1.0;
        test_matrix.m01 = 2.0;


        PickTarget mock_target = mockPickTransformTarget(LeafPickTarget.class, test_matrix);
        when(mock_target.checkPickMask(pickType)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.LEAF_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);

        assertEquals(test_request.pickCount, 1, "Expected an intersection");

        SceneGraphPath result_path = null;

        if(sortType == PickRequest.SORT_ALL || sortType == PickRequest.SORT_ORDERED)
        {
            assertTrue(test_request.foundPaths instanceof List, "Bulk sort should return a list of paths");
            result_path = (SceneGraphPath)((List)test_request.foundPaths).get(0);
        }
        else
        {
            assertTrue(test_request.foundPaths instanceof SceneGraphPath, "Single sort should return a path");
            result_path = (SceneGraphPath)test_request.foundPaths;
        }

        assertNotNull(result_path, "No scene graph path actually collected");
        assertEquals(result_path.getNodeCount(), 1, "Wrong number of nodes in path");
        assertEquals(result_path.getTerminalNode(), mock_target, "Target not in the path");

        Matrix4d result_matrix = new Matrix4d();

        result_path.getTransform(result_matrix);
        assertEqualsMatrix(result_matrix, test_matrix, "forward");

        Matrix4d inv_matrix = new Matrix4d();
        MatrixUtils utils = new MatrixUtils();
        utils.inverse(test_matrix, inv_matrix);

        result_path.getInverseTransform(result_matrix);
        assertEqualsMatrix(result_matrix, inv_matrix, "inverse");
    }

    @Test(groups = "unit", dataProvider = "pick options")
    public void testPickTransformHandlingNoMatrix(int geometryType,
                                                  float[] origin,
                                                  float[] destination,
                                                  float additionalData,
                                                  int sortType,
                                                  int pickType) throws Exception
    {
        PickRequest test_request = new PickRequest();
        test_request.pickGeometryType = geometryType;
        test_request.pickSortType = sortType;
        test_request.pickType = pickType;

        // Need this true so that the matrix is created properly
        test_request.generateVWorldMatrix = false;

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

        Matrix4d test_matrix = new Matrix4d();
        test_matrix.m00 = 1.0;
        test_matrix.m01 = 2.0;

        PickTarget mock_target = mockPickTransformTarget(LeafPickTarget.class, test_matrix);
        when(mock_target.checkPickMask(pickType)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.LEAF_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);

        assertEquals(test_request.pickCount, 1, "Expected an intersection");

        SceneGraphPath result_path = null;

        if(sortType == PickRequest.SORT_ALL || sortType == PickRequest.SORT_ORDERED)
        {
            assertTrue(test_request.foundPaths instanceof List, "Bulk sort should return a list of paths");
            result_path = (SceneGraphPath)((List)test_request.foundPaths).get(0);
        }
        else
        {
            assertTrue(test_request.foundPaths instanceof SceneGraphPath, "Single sort should return a path");
            result_path = (SceneGraphPath)test_request.foundPaths;
        }

        assertNotNull(result_path, "No scene graph path actually collected");
        assertEquals(result_path.getNodeCount(), 1, "Wrong number of nodes in path");
        assertEquals(result_path.getTerminalNode(), mock_target, "Target not in the path");

        // Pick should succeed, but without any matrix being generated.
        Matrix4d result_matrix = new Matrix4d();

        result_path.getTransform(result_matrix);
        assertIdentityMatrix(result_matrix);

        result_path.getInverseTransform(result_matrix);
        assertIdentityMatrix(result_matrix);
    }

    @Test(groups = "unit", dataProvider = "pick options")
    public void testAllSingleWithNoChildren(int geometryType, float[] origin, float[] destination, float additionalData,
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

        SinglePickTarget mock_target = mock(SinglePickTarget.class);
        when(mock_target.checkPickMask(pickType)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.SINGLE_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());
        when(mock_target.getPickableChild()).thenReturn(null);

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);

        assertEquals(test_request.pickCount, 0, "Should not have any picks found");
    }

    @Test(groups = "unit", dataProvider = "pick options")
    public void testAllSingleWithLeafChild(int geometryType, float[] origin, float[] destination, float additionalData,
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

    @Test(groups = "unit", dataProvider = "pick options")
    public void testAllSingleWithSingleChild(int geometryType,
                                             float[] origin,
                                             float[] destination,
                                             float additionalData,
                                             int sortType,
                                             int pickType) throws Exception
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

        SinglePickTarget mock_intermediate = mock(SinglePickTarget.class);
        when(mock_intermediate.checkPickMask(pickType)).thenReturn(true);
        when(mock_intermediate.getPickTargetType()).thenReturn(PickTarget.SINGLE_PICK_TYPE);
        when(mock_intermediate.getPickableBounds()).thenReturn(new BoundingBox());
        when(mock_intermediate.getPickableChild()).thenReturn(mock_child);

        SinglePickTarget mock_target = mock(SinglePickTarget.class);
        when(mock_target.checkPickMask(pickType)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.SINGLE_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());
        when(mock_target.getPickableChild()).thenReturn(mock_intermediate);

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

    @Test(groups = "unit", dataProvider = "pick options")
    public void testAllSingleWithGroupChild(int geometryType, float[] origin, float[] destination, float additionalData,
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

        // Always have child 0 as null to test skipping this as the output
        PickTarget[] group_children = { null, mock_child };
        GroupPickTarget mock_intermediate = mock(GroupPickTarget.class);
        when(mock_intermediate.checkPickMask(pickType)).thenReturn(true);
        when(mock_intermediate.getPickTargetType()).thenReturn(PickTarget.GROUP_PICK_TYPE);
        when(mock_intermediate.getPickableBounds()).thenReturn(new BoundingBox());
        when(mock_intermediate.getPickableChildren()).thenReturn(group_children);
        when(mock_intermediate.numPickableChildren()).thenReturn(group_children.length);

        SinglePickTarget mock_target = mock(SinglePickTarget.class);
        when(mock_target.checkPickMask(pickType)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.SINGLE_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());
        when(mock_target.getPickableChild()).thenReturn(mock_intermediate);

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

    @Test(groups = "unit", dataProvider = "pick options")
    public void testAllSingleWithCustomChild(int geometryType, float[] origin, float[] destination, float additionalData,
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

        final PickTarget mock_child = mock(LeafPickTarget.class);
        when(mock_child.checkPickMask(pickType)).thenReturn(true);
        when(mock_child.getPickTargetType()).thenReturn(PickTarget.LEAF_PICK_TYPE);
        when(mock_child.getPickableBounds()).thenReturn(new BoundingBox());

        CustomPickTarget mock_intermediate = mock(CustomPickTarget.class);
        when(mock_intermediate.checkPickMask(pickType)).thenReturn(true);
        when(mock_intermediate.getPickTargetType()).thenReturn(PickTarget.CUSTOM_PICK_TYPE);
        when(mock_intermediate.getPickableBounds()).thenReturn(new BoundingBox());

        doAnswer(
            new Answer()
            {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable
                {
                    Object[] args = invocation.getArguments();

                    PickInstructions instructions = (PickInstructions)args[0];
                    instructions.resizeChildren(4);
                    instructions.numChildren = 2;

                    // Always have child 0 as null to test skipping this as the output
                    instructions.children[0] = null;
                    instructions.children[1] = mock_child;
                    instructions.hasTransform = false;
                    return null;
                }
            }
        ).when(mock_intermediate).pickChildren(any(PickInstructions.class), any(Matrix4d.class), eq(test_request));


        SinglePickTarget mock_target = mock(SinglePickTarget.class);
        when(mock_target.checkPickMask(pickType)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.SINGLE_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());
        when(mock_target.getPickableChild()).thenReturn(mock_intermediate);

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

    @Test(groups = "unit", dataProvider = "pick options")
    public void testAllGroupWithNoChildren(int geometryType,
                                           float[] origin,
                                           float[] destination,
                                           float additionalData,
                                           int sortType,
                                           int pickType) throws Exception
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

        GroupPickTarget mock_target = mock(GroupPickTarget.class);
        when(mock_target.checkPickMask(pickType)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.GROUP_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());
        when(mock_target.getPickableChildren()).thenReturn(new PickTarget[0]);
        when(mock_target.numPickableChildren()).thenReturn(0);

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);

        assertEquals(test_request.pickCount, 0, "Should not have found any intersections");
    }

    @Test(groups = "unit", dataProvider = "pick options")
    public void testAllGroupWithNoChildIntersection(int geometryType,
                                                    float[] origin,
                                                    float[] destination,
                                                    float additionalData,
                                                    int sortType,
                                                    int pickType) throws Exception
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

        // Run these all in reverse order so that each path will pick up a different child
        // first for the purposes of checking all code paths.
        PickTarget mock_child = mock(LeafPickTarget.class);
        when(mock_child.checkPickMask(pickType)).thenReturn(true);
        when(mock_child.getPickTargetType()).thenReturn(PickTarget.LEAF_PICK_TYPE);
        when(mock_child.getPickableBounds()).thenReturn(new BoundingVoid());

        PickTarget[] test_group_children = { mock_child };

        GroupPickTarget mock_target = mock(GroupPickTarget.class);
        when(mock_target.checkPickMask(pickType)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.GROUP_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());
        when(mock_target.getPickableChildren()).thenReturn(test_group_children);
        when(mock_target.numPickableChildren()).thenReturn(test_group_children.length);

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);

        assertEquals(test_request.pickCount, 0, "Should not have found any intersections");
    }


    @Test(groups = "unit", dataProvider = "pick options")
    public void testAllGroupWithLeafChild(int geometryType, float[] origin, float[] destination, float additionalData,
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

        // Run these all in reverse order so that each path will pick up a different child
        // first for the purposes of checking all code paths.
        PickTarget mock_child = mock(LeafPickTarget.class);
        when(mock_child.checkPickMask(pickType)).thenReturn(true);
        when(mock_child.getPickTargetType()).thenReturn(PickTarget.LEAF_PICK_TYPE);
        when(mock_child.getPickableBounds()).thenReturn(new BoundingBox());

        PickTarget[] test_group_children = { mock_child };

        GroupPickTarget mock_target = mock(GroupPickTarget.class);
        when(mock_target.checkPickMask(pickType)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.GROUP_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());
        when(mock_target.getPickableChildren()).thenReturn(test_group_children);
        when(mock_target.numPickableChildren()).thenReturn(test_group_children.length);

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);


        if(sortType == PickRequest.SORT_ALL || sortType == PickRequest.SORT_ORDERED)
        {
            assertEquals(test_request.pickCount, 1, "Wrong number of intersections found");
            assertTrue(test_request.foundPaths instanceof List, "Bulk sort should return a list of paths");
        }
        else
        {
            assertEquals(test_request.pickCount, 1, "Expected an intersection");
            assertTrue(test_request.foundPaths instanceof SceneGraphPath, "Single sort should return a path");
        }

        // Can't test this as the mock does not implement both PickTarget and Node, so the
        // check in the SceneGraphPath update() method will ignore the mocked object.
//        SceneGraphPath result_path = (SceneGraphPath)test_request.foundPaths;
//        assertEquals(result_path.getNodeCount(), 1, "Wrong number of nodes in path");
//        assertEquals(result_path.getNode(0), mock_target, "Target not in the path");
    }

    @Test(groups = "unit", dataProvider = "pick options")
    public void testAllGroupWithSingleChild(int geometryType, float[] origin, float[] destination, float additionalData,
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

        // Run these all in reverse order so that each path will pick up a different child
        // first for the purposes of checking all code paths.
        PickTarget mock_child = mock(LeafPickTarget.class);
        when(mock_child.checkPickMask(pickType)).thenReturn(true);
        when(mock_child.getPickTargetType()).thenReturn(PickTarget.LEAF_PICK_TYPE);
        when(mock_child.getPickableBounds()).thenReturn(new BoundingBox());

        SinglePickTarget mock_intermediate = mock(SinglePickTarget.class);
        when(mock_intermediate.checkPickMask(pickType)).thenReturn(true);
        when(mock_intermediate.getPickTargetType()).thenReturn(PickTarget.SINGLE_PICK_TYPE);
        when(mock_intermediate.getPickableBounds()).thenReturn(new BoundingBox());
        when(mock_intermediate.getPickableChild()).thenReturn(mock_child);

        PickTarget[] test_group_children = { mock_intermediate };

        GroupPickTarget mock_target = mock(GroupPickTarget.class);
        when(mock_target.checkPickMask(pickType)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.GROUP_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());
        when(mock_target.getPickableChildren()).thenReturn(test_group_children);
        when(mock_target.numPickableChildren()).thenReturn(test_group_children.length);

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);


        if(sortType == PickRequest.SORT_ALL || sortType == PickRequest.SORT_ORDERED)
        {
            assertEquals(test_request.pickCount, 1, "Wrong number of intersections found");
            assertTrue(test_request.foundPaths instanceof List, "Bulk sort should return a list of paths");
        }
        else
        {
            assertEquals(test_request.pickCount, 1, "Expected an intersection");
            assertTrue(test_request.foundPaths instanceof SceneGraphPath, "Single sort should return a path");
        }

        // Can't test this as the mock does not implement both PickTarget and Node, so the
        // check in the SceneGraphPath update() method will ignore the mocked object.
//        SceneGraphPath result_path = (SceneGraphPath)test_request.foundPaths;
//        assertEquals(result_path.getNodeCount(), 1, "Wrong number of nodes in path");
//        assertEquals(result_path.getNode(0), mock_target, "Target not in the path");
    }

    @Test(groups = "unit", dataProvider = "pick options")
    public void testAllGroupWithGroupChild(int geometryType, float[] origin, float[] destination, float additionalData,
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

        // Run these all in reverse order so that each path will pick up a different child
        // first for the purposes of checking all code paths.
        PickTarget mock_child = mock(LeafPickTarget.class);
        when(mock_child.checkPickMask(pickType)).thenReturn(true);
        when(mock_child.getPickTargetType()).thenReturn(PickTarget.LEAF_PICK_TYPE);
        when(mock_child.getPickableBounds()).thenReturn(new BoundingBox());

        PickTarget[] test_intermediate_children = { mock_child };

        GroupPickTarget mock_intermediate = mock(GroupPickTarget.class);
        when(mock_intermediate.checkPickMask(pickType)).thenReturn(true);
        when(mock_intermediate.getPickTargetType()).thenReturn(PickTarget.GROUP_PICK_TYPE);
        when(mock_intermediate.getPickableBounds()).thenReturn(new BoundingBox());
        when(mock_intermediate.getPickableChildren()).thenReturn(test_intermediate_children);
        when(mock_intermediate.numPickableChildren()).thenReturn(test_intermediate_children.length);

        PickTarget[] test_group_children = { mock_intermediate };

        GroupPickTarget mock_target = mock(GroupPickTarget.class);
        when(mock_target.checkPickMask(pickType)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.GROUP_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());
        when(mock_target.getPickableChildren()).thenReturn(test_group_children);
        when(mock_target.numPickableChildren()).thenReturn(test_group_children.length);

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);


        if(sortType == PickRequest.SORT_ALL || sortType == PickRequest.SORT_ORDERED)
        {
            assertEquals(test_request.pickCount, 1, "Wrong number of intersections found");
            assertTrue(test_request.foundPaths instanceof List, "Bulk sort should return a list of paths");
        }
        else
        {
            assertEquals(test_request.pickCount, 1, "Expected an intersection");
            assertTrue(test_request.foundPaths instanceof SceneGraphPath, "Single sort should return a path");
        }

        // Can't test this as the mock does not implement both PickTarget and Node, so the
        // check in the SceneGraphPath update() method will ignore the mocked object.
//        SceneGraphPath result_path = (SceneGraphPath)test_request.foundPaths;
//        assertEquals(result_path.getNodeCount(), 1, "Wrong number of nodes in path");
//        assertEquals(result_path.getNode(0), mock_target, "Target not in the path");
    }

    @Test(groups = "unit", dataProvider = "pick options")
    public void testAllGroupWithCustomChild(int geometryType, float[] origin, float[] destination, float additionalData,
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

        // Run these all in reverse order so that each path will pick up a different child
        // first for the purposes of checking all code paths.
        final PickTarget mock_child = mock(LeafPickTarget.class);
        when(mock_child.checkPickMask(pickType)).thenReturn(true);
        when(mock_child.getPickTargetType()).thenReturn(PickTarget.LEAF_PICK_TYPE);
        when(mock_child.getPickableBounds()).thenReturn(new BoundingBox());

        CustomPickTarget mock_intermediate = mock(CustomPickTarget.class);
        when(mock_intermediate.checkPickMask(pickType)).thenReturn(true);
        when(mock_intermediate.getPickTargetType()).thenReturn(PickTarget.CUSTOM_PICK_TYPE);
        when(mock_intermediate.getPickableBounds()).thenReturn(new BoundingBox());

        doAnswer(
            new Answer()
            {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable
                {
                    Object[] args = invocation.getArguments();

                    PickInstructions instructions = (PickInstructions)args[0];
                    instructions.resizeChildren(4);
                    instructions.numChildren = 2;

                    // Always have child 0 as null to test skipping this as the output
                    instructions.children[0] = null;
                    instructions.children[1] = mock_child;
                    instructions.hasTransform = false;
                    return null;
                }
            }
        ).when(mock_intermediate).pickChildren(any(PickInstructions.class), any(Matrix4d.class), eq(test_request));

        PickTarget[] test_group_children = { mock_intermediate };

        GroupPickTarget mock_target = mock(GroupPickTarget.class);
        when(mock_target.checkPickMask(pickType)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.GROUP_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());
        when(mock_target.getPickableChildren()).thenReturn(test_group_children);
        when(mock_target.numPickableChildren()).thenReturn(test_group_children.length);

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);


        if(sortType == PickRequest.SORT_ALL || sortType == PickRequest.SORT_ORDERED)
        {
            assertEquals(test_request.pickCount, 1, "Wrong number of intersections found");
            assertTrue(test_request.foundPaths instanceof List, "Bulk sort should return a list of paths");
        }
        else
        {
            assertEquals(test_request.pickCount, 1, "Expected an intersection");
            assertTrue(test_request.foundPaths instanceof SceneGraphPath, "Single sort should return a path");
        }

        // Can't test this as the mock does not implement both PickTarget and Node, so the
        // check in the SceneGraphPath update() method will ignore the mocked object.
//        SceneGraphPath result_path = (SceneGraphPath)test_request.foundPaths;
//        assertEquals(result_path.getNodeCount(), 1, "Wrong number of nodes in path");
//        assertEquals(result_path.getNode(0), mock_target, "Target not in the path");
    }

    @Test(groups = "unit", dataProvider = "pick options")
    public void testAllCustomLeafChild(int geometryType, float[] origin, float[] destination, float additionalData,
                                       int sortType, int pickType) throws Exception
    {
        // Tests single level custom picker. Need separate test for nested custom pick
        // handling.

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

        CustomPickTarget mock_target = mockPickTarget(CustomPickTarget.class);
        when(mock_target.checkPickMask(pickType)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.CUSTOM_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());

        final PickTarget mock_child = mockPickTarget(LeafPickTarget.class);
        when(mock_child.checkPickMask(pickType)).thenReturn(true);
        when(mock_child.getPickTargetType()).thenReturn(PickTarget.LEAF_PICK_TYPE);
        when(mock_child.getPickableBounds()).thenReturn(new BoundingBox());

        final Matrix4d test_matrix = new Matrix4d();
        test_matrix.m00 = 1.0;
        test_matrix.m01 = 2.0;

        doAnswer(
            new Answer()
            {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable
                {
                    Object[] args = invocation.getArguments();

                    PickInstructions instructions = (PickInstructions)args[0];
                    instructions.resizeChildren(4);
                    instructions.numChildren = 2;

                    // Always have child 0 as null to test skipping this as the output
                    instructions.children[0] = null;
                    instructions.children[1] = mock_child;
                    instructions.hasTransform = true;
                    instructions.localTransform = test_matrix;
                    return null;
                }
            }
        ).when(mock_target).pickChildren(any(PickInstructions.class), any(Matrix4d.class), eq(test_request));

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);

        verify(mock_target, times(1)).pickChildren(any(PickInstructions.class), any(Matrix4d.class), eq(test_request));

        assertEquals(test_request.pickCount, 1, "Wrong number of intersections found");

        SceneGraphPath result_path;

        if(sortType == PickRequest.SORT_ALL || sortType == PickRequest.SORT_ORDERED)
        {
            assertTrue(test_request.foundPaths instanceof List, "Bulk sort should return a list of paths");
            result_path = (SceneGraphPath)((List)test_request.foundPaths).get(0);
        }
        else
        {
            assertTrue(test_request.foundPaths instanceof SceneGraphPath, "Single sort should return a path");
            result_path = (SceneGraphPath)test_request.foundPaths;
        }

        assertNotNull(result_path, "No scene graph path actually collected");
        assertEquals(result_path.getNodeCount(), 2, "Wrong number of nodes in path");
        assertEquals(result_path.getTerminalNode(), mock_child, "Target not in the path");

        Matrix4d result_matrix = new Matrix4d();

        result_path.getTransform(result_matrix);
        assertEqualsMatrix(result_matrix, test_matrix, "forward");

        Matrix4d inv_matrix = new Matrix4d();
        MatrixUtils utils = new MatrixUtils();
        utils.inverse(test_matrix, inv_matrix);

        result_path.getInverseTransform(result_matrix);
        assertEqualsMatrix(result_matrix, inv_matrix, "inverse");
    }

    @Test(groups = "unit", dataProvider = "pick options")
    public void testAllCustomSingleChild(int geometryType, float[] origin, float[] destination, float additionalData,
                                       int sortType, int pickType) throws Exception
    {
        // Tests single level custom picker. Need separate test for nested custom pick
        // handling.

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

        CustomPickTarget mock_target = mock(CustomPickTarget.class);
        when(mock_target.checkPickMask(pickType)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.CUSTOM_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());

        PickTarget mock_child = mock(LeafPickTarget.class);
        when(mock_child.checkPickMask(pickType)).thenReturn(true);
        when(mock_child.getPickTargetType()).thenReturn(PickTarget.LEAF_PICK_TYPE);
        when(mock_child.getPickableBounds()).thenReturn(new BoundingBox());

        final SinglePickTarget mock_intermediate = mock(SinglePickTarget.class);
        when(mock_intermediate.checkPickMask(pickType)).thenReturn(true);
        when(mock_intermediate.getPickTargetType()).thenReturn(PickTarget.SINGLE_PICK_TYPE);
        when(mock_intermediate.getPickableBounds()).thenReturn(new BoundingBox());
        when(mock_intermediate.getPickableChild()).thenReturn(mock_child);

        doAnswer(
            new Answer()
            {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable
                {
                    Object[] args = invocation.getArguments();

                    PickInstructions instructions = (PickInstructions)args[0];
                    instructions.resizeChildren(4);
                    instructions.numChildren = 2;

                    // Always have child 0 as null to test skipping this as the output
                    instructions.children[0] = null;
                    instructions.children[1] = mock_intermediate;
                    instructions.hasTransform = false;
                    return null;
                }
            }
                ).when(mock_target).pickChildren(any(PickInstructions.class), any(Matrix4d.class), eq(test_request));

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);

        verify(mock_target, times(1)).pickChildren(any(PickInstructions.class), any(Matrix4d.class), eq(test_request));
    }

    @Test(groups = "unit", dataProvider = "pick options")
    public void testAllCustomGroupChild(int geometryType, float[] origin, float[] destination, float additionalData,
                                         int sortType, int pickType) throws Exception
    {
        // Tests single level custom picker. Need separate test for nested custom pick
        // handling.

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

        CustomPickTarget mock_target = mock(CustomPickTarget.class);
        when(mock_target.checkPickMask(pickType)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.CUSTOM_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());

        PickTarget mock_child = mock(LeafPickTarget.class);
        when(mock_child.checkPickMask(pickType)).thenReturn(true);
        when(mock_child.getPickTargetType()).thenReturn(PickTarget.LEAF_PICK_TYPE);
        when(mock_child.getPickableBounds()).thenReturn(new BoundingBox());

        PickTarget[] test_group_children = { mock_child };
        final GroupPickTarget mock_intermediate = mock(GroupPickTarget.class);
        when(mock_intermediate.checkPickMask(pickType)).thenReturn(true);
        when(mock_intermediate.getPickTargetType()).thenReturn(PickTarget.GROUP_PICK_TYPE);
        when(mock_intermediate.getPickableBounds()).thenReturn(new BoundingBox());
        when(mock_intermediate.getPickableChildren()).thenReturn(test_group_children);
        when(mock_intermediate.numPickableChildren()).thenReturn(test_group_children.length);

        doAnswer(
            new Answer()
            {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable
                {
                    Object[] args = invocation.getArguments();

                    PickInstructions instructions = (PickInstructions)args[0];
                    instructions.resizeChildren(4);
                    instructions.numChildren = 2;

                    // Always have child 0 as null to test skipping this as the output
                    instructions.children[0] = null;
                    instructions.children[1] = mock_intermediate;
                    instructions.hasTransform = false;
                    return null;
                }
            }
                ).when(mock_target).pickChildren(any(PickInstructions.class), any(Matrix4d.class), eq(test_request));

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);

        verify(mock_target, times(1)).pickChildren(any(PickInstructions.class), any(Matrix4d.class), eq(test_request));
    }

    @Test(groups = "unit", dataProvider = "pick options")
    public void testAllCustomCustomChild(int geometryType, float[] origin, float[] destination, float additionalData,
                                        int sortType, int pickType) throws Exception
    {
        // Tests single level custom picker. Need separate test for nested custom pick
        // handling.

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

        CustomPickTarget mock_target = mock(CustomPickTarget.class);
        when(mock_target.checkPickMask(pickType)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.CUSTOM_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());

        final PickTarget mock_child = mock(LeafPickTarget.class);
        when(mock_child.checkPickMask(pickType)).thenReturn(true);
        when(mock_child.getPickTargetType()).thenReturn(PickTarget.LEAF_PICK_TYPE);
        when(mock_child.getPickableBounds()).thenReturn(new BoundingBox());

        final CustomPickTarget mock_intermediate = mock(CustomPickTarget.class);
        when(mock_intermediate.checkPickMask(pickType)).thenReturn(true);
        when(mock_intermediate.getPickTargetType()).thenReturn(PickTarget.CUSTOM_PICK_TYPE);
        when(mock_intermediate.getPickableBounds()).thenReturn(new BoundingBox());

        doAnswer(
            new Answer()
            {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable
                {
                    Object[] args = invocation.getArguments();

                    PickInstructions instructions = (PickInstructions)args[0];
                    instructions.resizeChildren(4);
                    instructions.numChildren = 2;

                    // Always have child 0 as null to test skipping this as the output
                    instructions.children[0] = null;
                    instructions.children[1] = mock_intermediate;
                    instructions.hasTransform = false;
                    return null;
                }
            }
        ).when(mock_target).pickChildren(any(PickInstructions.class), any(Matrix4d.class), eq(test_request));

        doAnswer(
            new Answer()
            {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable
                {
                    Object[] args = invocation.getArguments();

                    PickInstructions instructions = (PickInstructions)args[0];
                    instructions.resizeChildren(4);
                    instructions.numChildren = 2;

                    // Always have child 0 as null to test skipping this as the output
                    instructions.children[0] = null;
                    instructions.children[1] = mock_child;
                    instructions.hasTransform = false;
                    return null;
                }
            }
        ).when(mock_intermediate).pickChildren(any(PickInstructions.class), any(Matrix4d.class), eq(test_request));

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);

        verify(mock_target, times(1)).pickChildren(any(PickInstructions.class), any(Matrix4d.class), eq(test_request));
    }

    @Test(groups = "unit", dataProvider = "pick options")
    public void testAllCustomNochildren(int geometryType, float[] origin, float[] destination, float additionalData,
                                        int sortType, int pickType) throws Exception
    {
        // Tests single level custom picker. Need separate test for nested custom pick
        // handling.

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

        CustomPickTarget mock_target = mock(CustomPickTarget.class);
        when(mock_target.checkPickMask(pickType)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.CUSTOM_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());

        doAnswer(
            new Answer()
            {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable
                {
                    Object[] args = invocation.getArguments();

                    PickInstructions instructions = (PickInstructions)args[0];
                    instructions.resizeChildren(1);
                    instructions.numChildren = 0;
                    instructions.hasTransform = false;
                    return null;
                }
            }
            ).when(mock_target).pickChildren(any(PickInstructions.class), any(Matrix4d.class), eq(test_request));

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);

        verify(mock_target, times(1)).pickChildren(any(PickInstructions.class), any(Matrix4d.class), eq(test_request));
        assertEquals(test_request.pickCount, 0, "Should not have an intersection");
    }


    @Test(groups = "unit", dataProvider = "pick options")
    public void testGroupChildSkipping(int geometryType, float[] origin, float[] destination, float additionalData,
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

        // Run these all in reverse order so that each path will pick up a different child
        // first for the purposes of checking all code paths.
        PickTarget mock_pickable_child = mock(LeafPickTarget.class);
        when(mock_pickable_child.checkPickMask(pickType)).thenReturn(true);
        when(mock_pickable_child.getPickTargetType()).thenReturn(PickTarget.LEAF_PICK_TYPE);
        when(mock_pickable_child.getPickableBounds()).thenReturn(new BoundingBox());

        // Child with a bounding VOID as the bounds so that it can never have an
        // intersection
        PickTarget mock_void_child = mock(LeafPickTarget.class);
        when(mock_void_child.checkPickMask(pickType)).thenReturn(true);
        when(mock_void_child.getPickTargetType()).thenReturn(PickTarget.LEAF_PICK_TYPE);
        when(mock_void_child.getPickableBounds()).thenReturn(new BoundingVoid());

        // Child with a pick mask not matching
        // intersection
        PickTarget mock_masked_child = mock(LeafPickTarget.class);
        when(mock_masked_child.checkPickMask(anyInt())).thenReturn(false);
        when(mock_masked_child.getPickTargetType()).thenReturn(PickTarget.LEAF_PICK_TYPE);
        when(mock_masked_child.getPickableBounds()).thenReturn(new BoundingBox());

        PickTarget mock_unbounded_child = mock(LeafPickTarget.class);
        when(mock_unbounded_child.checkPickMask(pickType)).thenReturn(true);
        when(mock_unbounded_child.getPickTargetType()).thenReturn(PickTarget.LEAF_PICK_TYPE);
        when(mock_unbounded_child.getPickableBounds()).thenReturn(null);

        // Include null as well in the child list. Pickable child is last so that we can cover
        // all code paths that reject it before finding it, for the single selection case.
        PickTarget[] test_group_children =
            { null, mock_masked_child, mock_void_child, mock_unbounded_child, mock_pickable_child  };

        GroupPickTarget mock_target = mock(GroupPickTarget.class);
        when(mock_target.checkPickMask(pickType)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.GROUP_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());
        when(mock_target.getPickableChildren()).thenReturn(test_group_children);
        when(mock_target.numPickableChildren()).thenReturn(test_group_children.length);

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);

        assertEquals(test_request.pickCount, 1, "Wrong number of intersections found");

        if(sortType == PickRequest.SORT_ALL || sortType == PickRequest.SORT_ORDERED)
        {
            assertTrue(test_request.foundPaths instanceof List, "Bulk sort should return a list of paths");
        }
        else
        {
            assertTrue(test_request.foundPaths instanceof SceneGraphPath, "Single sort should return a path");
        }

        // Can't test this as the mock does not implement both PickTarget and Node, so the
        // check in the SceneGraphPath update() method will ignore the mocked object.
//        SceneGraphPath result_path = (SceneGraphPath)test_request.foundPaths;
//        assertEquals(result_path.getNodeCount(), 1, "Wrong number of nodes in path");
//        assertEquals(result_path.getNode(0), mock_target, "Target not in the path");
    }

    @Test(groups = "unit", dataProvider = "pick types")
    public void testPickInvalidSortType(int geometryType) throws Exception
    {
        PickRequest test_request = new PickRequest();
        test_request.pickGeometryType = geometryType;
        test_request.pickSortType = -300;
        test_request.pickType = PickRequest.FIND_ALL;
        test_request.destination[1] = 1.0f;
        test_request.additionalData = 0.5f;

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

    @Test(groups = "unit", dataProvider = "pick types")
    public void testPickReturnSinglePath(int geometryType) throws Exception
    {
        PickRequest test_request = new PickRequest();
        test_request.pickGeometryType = geometryType;
        test_request.pickSortType = PickRequest.SORT_ANY;
        test_request.pickType = PickRequest.FIND_ALL;
        test_request.destination[1] = 1.0f;
        test_request.additionalData = 0.5f;
        test_request.foundPaths = new Object();

        PickTarget mock_target = mock(LeafPickTarget.class);
        when(mock_target.checkPickMask(PickRequest.FIND_ALL)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.LEAF_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);

        assertTrue(test_request.foundPaths instanceof SceneGraphPath, "Found path should be single scene graph path");
    }

    @Test(groups = "unit", dataProvider = "pick types")
    public void testPickReturnMultiplePaths(int geometryType) throws Exception
    {
        PickRequest test_request = new PickRequest();
        test_request.pickGeometryType = geometryType;
        test_request.pickSortType = PickRequest.SORT_ALL;
        test_request.pickType = PickRequest.FIND_ALL;
        test_request.destination[1] = 1.0f;
        test_request.additionalData = 0.5f;
        test_request.foundPaths = new Object();

        PickTarget mock_target = mock(LeafPickTarget.class);
        when(mock_target.checkPickMask(PickRequest.FIND_ALL)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.LEAF_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(new BoundingBox());

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);

        assertTrue(test_request.foundPaths instanceof List, "Found path should be a collection");
    }

    @Test(groups = "unit", dataProvider = "pick options")
    public void testBoundingGeometryPick(int geometryType,
                                         float[] origin,
                                         float[] destination,
                                         float additionalData,
                                         int sortType,
                                         int pickType) throws Exception
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

        BoundingVolume test_proxy_bounds = new BoundingBox();
        PickTarget mock_proxy = mockPickTarget(LeafPickTarget.class);

        when(mock_proxy.checkPickMask(pickType)).thenReturn(true);
        when(mock_proxy.getPickTargetType()).thenReturn(PickTarget.LEAF_PICK_TYPE);
        when(mock_proxy.getPickableBounds()).thenReturn(test_proxy_bounds);
        when(((Node)mock_proxy).getBounds()).thenReturn(test_proxy_bounds);

        BoundingGeometry test_bounds = new BoundingGeometry();
        test_bounds.setProxyGeometry((Node)mock_proxy);

        PickTarget mock_target = mockPickTarget(LeafPickTarget.class);
        when(mock_target.checkPickMask(pickType)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.LEAF_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(test_bounds);

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);

        assertEquals(test_request.pickCount, 1, "Expected an intersection");

        SceneGraphPath result_path = null;

        if(sortType == PickRequest.SORT_ALL || sortType == PickRequest.SORT_ORDERED)
        {
            assertTrue(test_request.foundPaths instanceof List, "Bulk sort should return a list of paths");
            result_path = (SceneGraphPath)((List)test_request.foundPaths).get(0);
        }
        else
        {
            assertTrue(test_request.foundPaths instanceof SceneGraphPath, "Single sort should return a path");
            result_path = (SceneGraphPath)test_request.foundPaths;
        }

        assertNotNull(result_path, "No scene graph path actually collected");
        assertEquals(result_path.getNodeCount(), 1, "Wrong number of nodes in path");
        assertEquals(result_path.getTerminalNode(), mock_proxy, "Target not in the path");

        Matrix4d result_matrix = new Matrix4d();

        result_path.getTransform(result_matrix);
        assertIdentityMatrix(result_matrix);

        result_path.getInverseTransform(result_matrix);
        assertIdentityMatrix(result_matrix);
    }

    @Test(groups = "unit", dataProvider = "pick options")
    public void testBoundingGeometryPickGeometryNotPickTarget(int geometryType,
                                                              float[] origin,
                                                              float[] destination,
                                                              float additionalData,
                                                              int sortType,
                                                              int pickType) throws Exception
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

        BoundingVolume test_proxy_bounds = new BoundingBox();
        Node mock_proxy = mock(Node.class);
        when((mock_proxy).getBounds()).thenReturn(test_proxy_bounds);

        BoundingGeometry test_bounds = new BoundingGeometry();
        test_bounds.setProxyGeometry(mock_proxy);

        PickTarget mock_target = mockPickTarget(LeafPickTarget.class);
        when(mock_target.checkPickMask(pickType)).thenReturn(true);
        when(mock_target.getPickTargetType()).thenReturn(PickTarget.LEAF_PICK_TYPE);
        when(mock_target.getPickableBounds()).thenReturn(test_bounds);

        DefaultPickingHandler class_under_test = new DefaultPickingHandler();
        class_under_test.pickSingle(mock_target, test_request);

        assertEquals(test_request.pickCount, 0, "Should not have found any intersections");
    }

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

    // ------- Point Picking Tests -------------------------------------------

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


    @DataProvider(name = "pick types")
    public Object[][] generatePickTypeOptions()
    {
        return new Object[][]
        {
            { PickRequest.PICK_BOX },
            { PickRequest.PICK_CONE },
            { PickRequest.PICK_CONE_SEGMENT },
            { PickRequest.PICK_CYLINDER },
//            { PickRequest.PICK_FRUSTUM },
            { PickRequest.PICK_LINE_SEGMENT },
            { PickRequest.PICK_POINT },
            { PickRequest.PICK_RAY },
            { PickRequest.PICK_SPHERE }
        };
    }

    @DataProvider(name = "pick options")
    public Object[][] generatePickComboOptions()
    {
        float[] direction_up = { 0, 1, 0 };
        float[] box_top = { 1, 1, 1};

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

            { PickRequest.PICK_BOX, null, box_top, 0, PickRequest.SORT_ALL, PickRequest.FIND_ALL },
            { PickRequest.PICK_BOX, null, box_top, 0, PickRequest.SORT_ANY, PickRequest.FIND_ALL },
            { PickRequest.PICK_BOX, null, box_top, 0, PickRequest.SORT_CLOSEST, PickRequest.FIND_ALL },
            { PickRequest.PICK_BOX, null, box_top, 0, PickRequest.SORT_ORDERED, PickRequest.FIND_ALL },

            { PickRequest.PICK_BOX, null, box_top, 0, PickRequest.SORT_ALL, PickRequest.FIND_GENERAL },
            { PickRequest.PICK_BOX, null, box_top, 0, PickRequest.SORT_ANY, PickRequest.FIND_GENERAL },
            { PickRequest.PICK_BOX, null, box_top, 0, PickRequest.SORT_CLOSEST, PickRequest.FIND_GENERAL },
            { PickRequest.PICK_BOX, null, box_top, 0, PickRequest.SORT_ORDERED, PickRequest.FIND_GENERAL },

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

    /**
     * Convenience method to check the given matrix is an identity matrix.
     *
     * @param matrix The matrix object to test
     */
    private void assertIdentityMatrix(Matrix4d matrix)
    {
        assertEquals(matrix.m00, 1.0, 0.00001, "[0][0] is not one in the identity matrix");
        assertEquals(matrix.m01, 0.0, 0.00001, "[0][1] is not zero in the identity matrix");
        assertEquals(matrix.m02, 0.0, 0.00001, "[0][2] is not zero in the identity matrix");
        assertEquals(matrix.m03, 0.0, 0.00001, "[0][3] is not zero in the identity matrix");

        assertEquals(matrix.m10, 0.0, 0.00001, "[1][0] is not zero in the identity matrix");
        assertEquals(matrix.m11, 1.0, 0.00001, "[1][1] is not one in the identity matrix");
        assertEquals(matrix.m12, 0.0, 0.00001, "[1][2] is not zero in the identity matrix");
        assertEquals(matrix.m13, 0.0, 0.00001, "[1][3] is not zero in the identity matrix");

        assertEquals(matrix.m20, 0.0, 0.00001, "[2][0] is not zero in the identity matrix");
        assertEquals(matrix.m21, 0.0, 0.00001, "[2][1] is not zero in the identity matrix");
        assertEquals(matrix.m22, 1.0, 0.00001, "[2][2] is not one in the identity matrix");
        assertEquals(matrix.m23, 0.0, 0.00001, "[2][3] is not zero in the identity matrix");

        assertEquals(matrix.m30, 0.0, 0.00001, "[3][0] is not zero in the identity matrix");
        assertEquals(matrix.m31, 0.0, 0.00001, "[3][1] is not zero in the identity matrix");
        assertEquals(matrix.m32, 0.0, 0.00001, "[3][2] is not zero in the identity matrix");
        assertEquals(matrix.m33, 1.0, 0.00001, "[3][3] is not one in the identity matrix");
    }

    /**
     * Convenience method to check the given matrix is an identity matrix.
     *
     * @param result The matrix object to test
     */
    private void assertEqualsMatrix(Matrix4d result, Matrix4d expected, String type)
    {
        assertEquals(result.m00, expected.m00, 0.00001, "[0][0] is incorrect in the " + type + " matrix");
        assertEquals(result.m01, expected.m01, 0.00001, "[0][1] is incorrect in the " + type + " matrix");
        assertEquals(result.m02, expected.m02, 0.00001, "[0][2] is incorrect in the " + type + " matrix");
        assertEquals(result.m03, expected.m03, 0.00001, "[0][3] is incorrect in the " + type + " matrix");

        assertEquals(result.m10, expected.m10, 0.00001, "[1][0] is incorrect in the " + type + " matrix");
        assertEquals(result.m11, expected.m11, 0.00001, "[1][1] is incorrect in the " + type + " matrix");
        assertEquals(result.m12, expected.m12, 0.00001, "[1][2] is incorrect in the " + type + " matrix");
        assertEquals(result.m13, expected.m13, 0.00001, "[1][3] is incorrect in the " + type + " matrix");

        assertEquals(result.m20, expected.m20, 0.00001, "[2][0] is incorrect in the " + type + " matrix");
        assertEquals(result.m21, expected.m21, 0.00001, "[2][1] is incorrect in the " + type + " matrix");
        assertEquals(result.m22, expected.m22, 0.00001, "[2][2] is incorrect in the " + type + " matrix");
        assertEquals(result.m23, expected.m23, 0.00001, "[2][3] is incorrect in the " + type + " matrix");

        assertEquals(result.m30, expected.m30, 0.00001, "[3][0] is incorrect in the " + type + " matrix");
        assertEquals(result.m31, expected.m31, 0.00001, "[3][1] is incorrect in the " + type + " matrix");
        assertEquals(result.m32, expected.m32, 0.00001, "[3][2] is incorrect in the " + type + " matrix");
        assertEquals(result.m33, expected.m33, 0.00001, "[3][3] is incorrect in the " + type + " matrix");
    }

    private <T> T mockPickTarget(Class<T> cls)
    {
        return (T) mock(Node.class, withSettings().extraInterfaces(cls));
    }

    private <T extends PickTarget> T mockPickTransformTarget(Class<T> cls, final Matrix4d matrix)
    {
        T target = (T) mock(Node.class, withSettings().extraInterfaces(cls, TransformPickTarget.class));

        doAnswer(
            new Answer()
            {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable
                {
                    Object[] args = invocation.getArguments();

                    Matrix4d matrixArg = (Matrix4d)args[0];
                    matrixArg.set(matrix);
                    return null;
                }
            }
        ).when((TransformPickTarget) target).getTransform(any(Matrix4d.class));

        final Matrix4d inv_matrix = new Matrix4d();
        MatrixUtils matrix_utils = new MatrixUtils();
        matrix_utils.inverse(matrix, inv_matrix);


        doAnswer(
            new Answer()
            {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable
                {
                    Object[] args = invocation.getArguments();

                    Matrix4d matrixArg = (Matrix4d) args[0];
                    matrixArg.set(inv_matrix);
                    return null;
                }
            }
                ).when((TransformPickTarget)target).getInverseTransform(any(Matrix4d.class));

        return target;
    }
}
