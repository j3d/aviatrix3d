/*****************************************************************************
 *                  Yumetech, Inc Copyright (c) 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package j3d.aviatrix3d.examples.multipass;

// External imports
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;
import org.j3d.maths.vector.Vector4d;
import org.j3d.maths.vector.AxisAngle4d;

import org.j3d.aviatrix3d.*;

// Local imports

/**
 * Animator class that animates shadowable geometry
 * 
 * @author Sang Park
 * @version $Revision: 1.1 $
 */
public class StencilShadowAnimator implements 
	ApplicationUpdateObserver,
	NodeUpdateListener {

    /** The amount to rotate the object each frame, in radians */
    private static final float ROTATION_INC = (float)(Math.PI / 99999);
    
    /** Geometry list */
    private List<SEdgeIndTriArray> meshGeomList;
    
    /** Currently available shadow geometries */
    private List<Geometry> shadowGeomList;
    
    /** Updatable coordinate list */
    private List<float[]> coordList;
    
    /** Front facing shadow volume group */
    private TransformGroup frontShadowGroup;
    
    /** Back facing shadow volume group */
    private TransformGroup backShadowGroup;
    
    /** A utility matrix used for updating the transforms each frame */
    private Matrix4d updateMatrix;
    
    /** 
     * A utility matrix used for updating the transfroms of the viewpoint
     * in each of the render passes.
     */
    private Matrix4d sceneMatrix;
    
    /** Shared position of the light */
    private Vector4d lightPos;

    /** The current angle of object rotation */
    private float rotation;
    
    /** Vertex type to update */
    private int vertexType;
    
    /** Currently updated index */
    private int currentIndex;
    
    private PolygonAttributes frontCulling;
    private Appearance frontCullApp;
    
    private PolygonAttributes backCulling;
    private Appearance backCullApp;
    
	/**
	 * Constructor
	 */
	public StencilShadowAnimator(Matrix4d objMatrix,
								 Matrix4d sceneMatrix,
								 Vector4d lightPos,
								 List<SEdgeIndTriArray> meshGeomList,
								 TransformGroup volPass1,
								 TransformGroup volPass2) {
		this.rotation = 0.0f;
		this.lightPos = lightPos;
		this.updateMatrix = objMatrix;
		this.sceneMatrix = sceneMatrix;
		this.meshGeomList = meshGeomList;
		this.coordList = new ArrayList<float[]>();
		this.shadowGeomList = new ArrayList<Geometry>();
		this.frontShadowGroup = volPass1;
		this.backShadowGroup = volPass2;
		
		this.frontCulling = new PolygonAttributes();
		this.frontCulling.setCulledFace(PolygonAttributes.CULL_FRONT);
        
		this.frontCullApp = new Appearance();
		this.frontCullApp.setPolygonAttributes(frontCulling);

		this.backCulling = new PolygonAttributes();
		this.backCulling.setCulledFace(PolygonAttributes.CULL_BACK);
        
		this.backCullApp = new Appearance();
		this.backCullApp.setPolygonAttributes(backCulling);
    	
		for(int j = 0; j < meshGeomList.size(); j++) {

	        VertexGeometry vtxArray =
	        	(VertexGeometry)meshGeomList.get(j);
	        
	        vertexType = vtxArray.getVertexType();

			int loopIncrVal = 0;
			
			if(vertexType == VertexGeometry.COORDINATE_3) {
				loopIncrVal = 3;
			} else if(vertexType == VertexGeometry.COORDINATE_4) {
				loopIncrVal = 4;
			}
			
			float[] vertices = 
	        	new float[vtxArray.getValidVertexCount() * loopIncrVal];
	        vtxArray.getVertices(vertices);
	        
	        coordList.add(vertices);
		}
	}

    //---------------------------------------------------------------
    // Methods defined by ApplicationUpdateObserver
    //---------------------------------------------------------------

    /**
     * Notification that now is a good time to update the scene graph.
     */
    public void updateSceneGraph() {
    	
        rotation = (float)((rotation + ROTATION_INC) % (2 * Math.PI));
        updateMatrix.setIdentity();
        updateMatrix.set(new AxisAngle4d(0, 1, 1, rotation));
        
        for(int j = 0; j < coordList.size(); j++) {
        	SEdgeIndTriArray vtxArray =
	        	(SEdgeIndTriArray)meshGeomList.get(j);
	        
	        vertexType = vtxArray.getVertexType();
			
			int loopIncrVal = 0;
			
			if(vertexType == VertexGeometry.COORDINATE_3) {
				loopIncrVal = 3;
			} else if(vertexType == VertexGeometry.COORDINATE_4) {
				loopIncrVal = 4;
			}
			
			float[] geomVertexes = coordList.get(j);
	        
	        Vector3d inVert = new Vector3d();
			Vector3d outVert = new Vector3d();
			
			for(int i = 0; i < geomVertexes.length; i += loopIncrVal) {
	
				inVert.x = geomVertexes[i+0];
				inVert.y = geomVertexes[i+1];
				inVert.z = geomVertexes[i+2];
				
		        updateMatrix.transform(inVert, outVert);
		        
		        geomVertexes[i+0] = outVert.x;
		        geomVertexes[i+1] = outVert.y;
		        geomVertexes[i+2] = outVert.z;
			}
			
			if(vtxArray.isLive()) {
				currentIndex = j;
				vtxArray.boundsChanged(this);
			} else {
				vtxArray.setVertices(vertexType, geomVertexes);
			}
			
			shadowGeomList = 
				createShadowVolumeGeom(vtxArray, updateMatrix, lightPos);
	        
	        if(frontShadowGroup.isLive()) {
	        	frontShadowGroup.boundsChanged(this);
	        } else {
	        	frontShadowGroup.removeAllChildren();
	    		for(int i = 0; i < shadowGeomList.size(); i++) {
	    			Geometry geom = shadowGeomList.get(i);
	    			Shape3D shadowShape = new Shape3D();
	    			shadowShape.setGeometry(geom);
	    			shadowShape.setAppearance(frontCullApp);
	    			frontShadowGroup.addChild(shadowShape);
	    		}
	        }
	        
	        if(backShadowGroup.isLive()) {
	        	backShadowGroup.boundsChanged(this);
	        } else {
	        	backShadowGroup.removeAllChildren();
	    		for(int i = 0; i < shadowGeomList.size(); i++) {
	    			Geometry geom = shadowGeomList.get(i);
	    			Shape3D shadowShape = new Shape3D();
	    			shadowShape.setGeometry(geom);
	    			shadowShape.setAppearance(backCullApp);
	    			backShadowGroup.addChild(shadowShape);
	    		}
	        }
        }
    }

    /**
     * Notification that the AV3D internal shutdown handler has detected a
     * system-wide shutdown. The aviatrix code has already terminated rendering
     * at the point this method is called, only the user's system code needs to
     * terminate before exiting here.
     */
    public void appShutdown() {
        // do nothing
    }

    //---------------------------------------------------------------
    // Methods defined by NodeUpdateListener
    //---------------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src) {
    	
    	if(src instanceof SEdgeIndTriArray) {
        	SEdgeIndTriArray vtxArray =
	        	(SEdgeIndTriArray)meshGeomList.get(currentIndex);
			float[] geomVertexes = coordList.get(currentIndex);
			vtxArray.setVertices(vertexType, geomVertexes);
			vtxArray.resetPlaneEquations();
			

    	}

    	if(src == frontShadowGroup) {
        	frontShadowGroup.removeAllChildren();
    		for(int i = 0; i < shadowGeomList.size(); i++) {
    			Geometry geom = shadowGeomList.get(i);
    			Shape3D shadowShape = new Shape3D();
    			shadowShape.setGeometry(geom);
    			shadowShape.setAppearance(frontCullApp);
    			frontShadowGroup.addChild(shadowShape);
    		}
    	} else if(src == backShadowGroup) { 
    		backShadowGroup.removeAllChildren();
    		for(int i = 0; i < shadowGeomList.size(); i++) {
    			Geometry geom = shadowGeomList.get(i);
    			Shape3D shadowShape = new Shape3D();
    			shadowShape.setGeometry(geom);
    			shadowShape.setAppearance(backCullApp);
    			backShadowGroup.addChild(shadowShape);
    		}
    	}
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src) {
    }
    
    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------
    
    /**
     * Creates an instance of shadow volume geometry
     * 
     * @param silhouetteGeom
     * @param transform
     * @param lightPos
     * @return
     */
    private ArrayList<Geometry> createShadowVolumeGeom(SEdgeIndTriArray silhouetteGeom,
		    										   Matrix4d transform,
		    										   Vector4d lightPos) {
    	
    	ArrayList<Geometry> geomList = new ArrayList<Geometry>();

		float[] vertices = new float[silhouetteGeom.getValidVertexCount() * 3];
		silhouetteGeom.getVertices(vertices);
		Edge[] edges = silhouetteGeom.getSilhouetteEdge();
		Triangle[] triangle = silhouetteGeom.getSilhouetteTriangle();

		// For each triangles
		for(int i = 0; i < edges.length; i++) {
			
			float dot1 = 
				triangle[edges[i].triangleIndex[0]].plane.dot(lightPos);
			float dot2 = 
				triangle[edges[i].triangleIndex[1]].plane.dot(lightPos);
			
			if(dot2 >= 0) {
				if(dot1 < 0) {
					// Silhouette triangle
					
					// Create shadow volume geometry and add it into the scene graph
					float v1x = (vertices[edges[i].vertexIndex[0] + 0] - lightPos.x);
					float v1y = (vertices[edges[i].vertexIndex[0] + 1] - lightPos.y);
					float v1z = (vertices[edges[i].vertexIndex[0] + 2] - lightPos.z);
					
					float v2x = (vertices[edges[i].vertexIndex[1] + 0] - lightPos.x);
					float v2y = (vertices[edges[i].vertexIndex[1] + 1] - lightPos.y);
					float v2z = (vertices[edges[i].vertexIndex[1] + 2] - lightPos.z);
					
					float[] coord = new float[] {
							vertices[edges[i].vertexIndex[0] + 0],
							vertices[edges[i].vertexIndex[0] + 1],
							vertices[edges[i].vertexIndex[0] + 2],
							1,
							vertices[edges[i].vertexIndex[1] + 0],
							vertices[edges[i].vertexIndex[1] + 1],
							vertices[edges[i].vertexIndex[1] + 2],
							1,
							v2x,
							v2y,
							v2z,
							0,
							v1x,
							v1y,
							v1z,
							0};
					
					QuadArray triStrip = new QuadArray();
					
					triStrip.setVertices(QuadArray.COORDINATE_4, coord);
					
					Shape3D geomShape = new Shape3D();
					geomShape.setGeometry(triStrip);
					
					geomList.add(triStrip);
				}
			}
			
			if(dot1 >= 0) {
				if(dot2 < 0) {
					// Silhouette triangle
					
					// Create shadow volume geometry and add it into the scene graph
					float v1x = (vertices[edges[i].vertexIndex[0] + 0] - lightPos.x);
					float v1y = (vertices[edges[i].vertexIndex[0] + 1] - lightPos.y);
					float v1z = (vertices[edges[i].vertexIndex[0] + 2] - lightPos.z);
					
					float v2x = (vertices[edges[i].vertexIndex[1] + 0] - lightPos.x);
					float v2y = (vertices[edges[i].vertexIndex[1] + 1] - lightPos.y);
					float v2z = (vertices[edges[i].vertexIndex[1] + 2] - lightPos.z);
					
					float[] coord = new float[] {
							vertices[edges[i].vertexIndex[1] + 0],
							vertices[edges[i].vertexIndex[1] + 1],
							vertices[edges[i].vertexIndex[1] + 2],
							1,
							vertices[edges[i].vertexIndex[0] + 0],
							vertices[edges[i].vertexIndex[0] + 1],
							vertices[edges[i].vertexIndex[0] + 2],
							1,
							v1x,
							v1y,
							v1z,
							0,
							v2x,
							v2y,
							v2z,
							0};
			        
					QuadArray triStrip = new QuadArray();
					
					triStrip.setVertices(QuadArray.COORDINATE_4, coord);
					
					Shape3D geomShape = new Shape3D();
					geomShape.setGeometry(triStrip);
					
					geomList.add(triStrip);
				}
			}
		}
    	
    	return geomList;
    }
    
}
