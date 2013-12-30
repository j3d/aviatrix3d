/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.renderer.aviatrix3d.nodes;

// External imports
import java.util.ArrayList;

import org.j3d.maths.vector.Matrix3d;
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;
import org.j3d.maths.vector.Vector4d;
import org.j3d.util.MatrixUtils;

// Local imports
import org.j3d.aviatrix3d.BaseGroup;
import org.j3d.aviatrix3d.Node;
import org.j3d.aviatrix3d.SharedGroup;
import org.j3d.aviatrix3d.SharedNode;
import org.j3d.aviatrix3d.TransformGroup;

import org.j3d.aviatrix3d.rendering.BoundingVolume;
import org.j3d.aviatrix3d.rendering.Cullable;
import org.j3d.aviatrix3d.rendering.CullInstructions;
import org.j3d.aviatrix3d.rendering.CustomCullable;

/**
 * A grouping node that places it's children in the line of site between a 
 * target node and the viewpoint, oriented towards the viewpoint.
 * <p>
 * In order to use this node effectively, you will need to use the
 * {@link org.j3d.aviatrix3d.pipeline.graphics.FrustumCullStage}  to process the
 * children. It uses custom culling routines internally and that is the only cull
 * stage that will do something useful with ths node.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public class MarkerGroup extends BaseGroup implements CustomCullable
{
    /** The node that is being tracked */
    private Node target;
    
    /** A matrix used when calculating the scene graph path to the target */
    private Matrix4d pathMatrix;

    /** Temp var used to fetch the scene graph path */
    private ArrayList<TransformGroup> pathList;

    /** An array used to fetch the nodes from pathNodes */
    private Node[] pathNodes;

    /** A matrix used when calculating the target transform */
    private Matrix4d targetMatrix;

    /** The target position */
    private Vector3d targetPosition;
    
    /** The view position */
    private Vector3d viewPosition;
    
    /** A matrix used when calculating the view transform */
    private Matrix4d viewMatrix;

    /** MatrixUtils for gc free inversion */
    private MatrixUtils matrixUtils;

    /** Flag indicating the marker matrix has changed */
    private boolean matrixChanged;
    
    /** The matrix describing the marker transform */
    private Matrix4d markerMatrix;

    /** The marker position */
    private Vector3d markerPosition;
    
    /** The view rotation matrix */
    private Matrix3d rotMatrix;
	
	/** Enabled flag */
	private boolean enabled;

    /**
     * Constructor
     */
    public MarkerGroup()
    {
        matrixUtils = new MatrixUtils();
        
        markerPosition = new Vector3d();
        markerMatrix = new Matrix4d();
        
        rotMatrix = new Matrix3d();
        
        viewPosition = new Vector3d();
        viewMatrix = new Matrix4d();
        
        targetPosition = new Vector3d();
        targetMatrix = new Matrix4d();
        
        pathList = new ArrayList<TransformGroup>();
        pathNodes = new Node[20];  // arbitrary initial value
        pathMatrix = new Matrix4d();

		enabled = true;
    }

    //-------------------------------------------------
    // Methods defined by CustomCullable
    //-------------------------------------------------

    /**
     * Check this node for children to traverse. The angular resolution is
     * defined as Field Of View (in radians) / viewport width in pixels.
     *
     * @param output Fill in the child information here
     * @param vworldTx The transformation from the root of the scene to
     *    this node according to the current traversal path
     * @param viewTransform The transformation from the root of the scene
     *    graph to the active viewpoint
     * @param frustumPlanes Listing of frustum planes in the order: right,
     *    left, bottom, top, far, near
     * @param angularRes Angular resolution of the screen, or 0 if not
     *    calculable from the available data.
     */
    public void cullChildren(CullInstructions output,
                             Matrix4d vworldTx,
                             Matrix4d viewTransform,
                             Vector4d[] frustumPlanes,
                             float angularRes)
    {
        if (enabled && (target != null))
        {
            getLocalToVworld(target, targetMatrix);

            targetPosition.set(targetMatrix.m03, targetMatrix.m13, targetMatrix.m23);
            viewMatrix.set(viewTransform);

            viewPosition.set(viewMatrix.m03, viewMatrix.m13, viewMatrix.m23);
            rotMatrix.m00 = viewMatrix.m00;
            rotMatrix.m01 = viewMatrix.m01;
            rotMatrix.m02 = viewMatrix.m02;

            rotMatrix.m10 = viewMatrix.m10;
            rotMatrix.m11 = viewMatrix.m11;
            rotMatrix.m12 = viewMatrix.m12;

            rotMatrix.m20 = viewMatrix.m20;
            rotMatrix.m21 = viewMatrix.m21;
            rotMatrix.m22 = viewMatrix.m22;

            markerPosition.set(
                targetPosition.x - viewPosition.x,
                targetPosition.y - viewPosition.y,
                targetPosition.z - viewPosition.z);
            markerPosition.normalise();
            markerPosition.scale(2);
            markerPosition.add(markerPosition, viewPosition);
            
            // account for the position of this node in the scenegraph
            viewPosition.set(vworldTx.m03, vworldTx.m13, vworldTx.m23);
            markerPosition.sub(markerPosition, viewPosition);
			
            markerMatrix.setIdentity();
            markerMatrix.set(rotMatrix);
            markerMatrix.m03 = markerPosition.x;
            markerMatrix.m13 = markerPosition.z;
            markerMatrix.m23 = markerPosition.y;

			if(BoundingVolume.FRUSTUM_ALLOUT ==
				bounds.checkIntersectionFrustum(frustumPlanes, markerMatrix))
			{
				output.hasTransform = false;
				output.numChildren = 0;
			}
			else
			{
				output.localTransform.set(markerMatrix);
				output.hasTransform = true;
				if (output.children.length < lastChild) 
				{
					output.resizeChildren(lastChild);
				}
				output.numChildren = 0;
				for (int i = 0; i < lastChild; ++i)
				{
					if(childList[i] instanceof Cullable)
					{
						output.children[output.numChildren++] = (Cullable)childList[i];
					}
				}
			}
        }
        else
        {
            output.hasTransform = false;
            output.numChildren = 0;
        }
    }

    //-------------------------------------------------------------------------
    // Local Methods
    //-------------------------------------------------------------------------

	/**
	 * Set the enabled state
	 *
	 * @param enabled The enabled state
	 */
    public void setEnabled(boolean enabled) 
	{
        this.enabled = enabled;
    }
    
	/**
	 * Set the node to track
	 *
	 * @param target The Node to track
	 */
    public void setTarget(Node target) 
	{
        this.target = target;
    }
    
    /**
     * Walk to the root of the scene and calculate the
     * root to virtual world coordinate location of the given node. If a
     * sharedGroup is found, then take the first parent listed always.
     *
     * @param node The end node to calculate from
     * @param mat The matrix to put the final result into
     */
    private void getLocalToVworld(Node node, Matrix4d mat) 
	{

        pathList.clear();
        
        // if the node is a transform, include it into the path....
        if (node instanceof SharedNode) 
		{
            Node child = ((SharedNode)node).getChild();
            if (child instanceof TransformGroup) 
			{
                pathList.add((TransformGroup)child);
            }
        }
        
        Node parent = node.getParent();
        while (parent != null) 
		{
            if (parent instanceof SharedGroup) 
			{
                SharedGroup sg = (SharedGroup)parent;

                int num_parents = sg.numParents();

                if (num_parents == 0) 
				{
                    break;
                } 
				else if (num_parents > pathNodes.length) 
				{
                    pathNodes = new Node[num_parents];
                }
                sg.getParents(pathNodes);
                parent = pathNodes[0];
            } 
			else 
			{
                if (parent instanceof TransformGroup) 
				{
                    pathList.add((TransformGroup)parent);
                }
                parent = parent.getParent();
            }
        }

        int num_nodes = pathList.size();
        mat.setIdentity();
        pathMatrix.setIdentity();

        for (int i = num_nodes - 1; i >= 0; i--) 
		{
            TransformGroup tg = pathList.get(i);
            tg.getTransform(pathMatrix);

            mat.mul(mat, pathMatrix);
        }
    }
}
