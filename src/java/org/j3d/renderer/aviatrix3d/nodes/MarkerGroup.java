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

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

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
    private Matrix4f pathMatrix;

    /** Temp var used to fetch the scene graph path */
    private ArrayList<TransformGroup> pathList;

    /** An array used to fetch the nodes from pathNodes */
    private Node[] pathNodes;

    /** A matrix used when calculating the target transform */
    private Matrix4f targetMatrix;

    /** The target position */
    private Vector3f targetPosition;
    
    /** The view position */
    private Vector3f viewPosition;
    
    /** A matrix used when calculating the view transform */
    private Matrix4f viewMatrix;

    /** MatrixUtils for gc free inversion */
    private MatrixUtils matrixUtils;

    /** Flag indicating the marker matrix has changed */
    private boolean matrixChanged;
    
    /** The matrix describing the marker transform */
    private Matrix4f markerMatrix;

    /** The marker position */
    private Vector3f markerPosition;
    
    /** The view rotation matrix */
    private Matrix3f rotMatrix;
	
	/** Enabled flag */
	private boolean enabled;

    /**
     * Constructor
     */
    public MarkerGroup()
    {
        matrixUtils = new MatrixUtils();
        
        markerPosition = new Vector3f();
        markerMatrix = new Matrix4f();
        
        rotMatrix = new Matrix3f();
        
        viewPosition = new Vector3f();
        viewMatrix = new Matrix4f();
        
        targetPosition = new Vector3f();
        targetMatrix = new Matrix4f();
        
        pathList = new ArrayList<TransformGroup>();
        pathNodes = new Node[20];  // arbitrary initial value
        pathMatrix = new Matrix4f();

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
                             Matrix4f vworldTx,
                             Matrix4f viewTransform,
                             Vector4f[] frustumPlanes,
                             float angularRes)
    {
        if (enabled && (target != null))
        {
            getLocalToVworld(target, targetMatrix);
            targetMatrix.get(targetPosition);
            
            viewMatrix.set(viewTransform);
            viewMatrix.get(viewPosition);
            
            viewMatrix.get(rotMatrix);
			
            markerPosition.set(
                targetPosition.x - viewPosition.x,
                targetPosition.y - viewPosition.y,
                targetPosition.z - viewPosition.z);
            markerPosition.normalize();
            markerPosition.scale(2);
            markerPosition.add(viewPosition);
            
            // account for the position of this node in the scenegraph
            vworldTx.get(viewPosition);
            markerPosition.sub(viewPosition);
			
            markerMatrix.setIdentity();
            markerMatrix.setRotation(rotMatrix);
            markerMatrix.setTranslation(markerPosition);
			
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
    private void getLocalToVworld(Node node, Matrix4f mat) 
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

            mat.mul(pathMatrix);
        }
    }
}
