/*****************************************************************************
 *                  Yumetech, Inc Copyright (c) 2009
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

// External imports
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Point3f;

import org.j3d.aviatrix3d.*;
import org.j3d.util.MatrixUtils;

// Internal imports

/**
 * Class responsible for updating the orientation of the torus in real-time.
 * 
 * @author Sang Park
 * @version $Revision: 1.4 $
 */
public class ShadowMappingAnimator
	implements ApplicationUpdateObserver,
			   NodeUpdateListener {

    /** The amount to rotate the object each frame, in radians */
    private static final float ROTATION_INC = (float)(Math.PI / 200);
    
    /** A utility matrix used for updating the transforms each frame */
    private Matrix4f matrix;

    /** The current angle of object rotation */
    private float rotation;
    
    /** TG from light's point of view */
    private TransformGroup lightPointofView;
    
    /** TG from camera's point of view */
    private TransformGroup camerasPointofView;
    
    /** A utility matrix used for updating light position */
    private Matrix4f lightMat;

    /** Spotlight from light's point of view */
    private SpotLight lightViewSpotLight;
    
    /** Spotlight form camera's viewpoint */
    private SpotLight cameraViewSpotLight;
    
    /** Updated light position */
    private Point3f lightPos;
    
    /** Updated light direction */
    private Vector3f lightDir;
    
    /** Light lookat Position */
    private Point3f lightLookAtPos;
    
    /** World up vector */
    private Vector3f worldUpVec;
    
    /** Camera transfrom from light's point of view */
    private TransformGroup spotlightViewTransform;

    /** TG that contains the geometry that represents light */
    private TransformGroup lightGeometryGroup;
    
    /** Utility to calculate the matrix */
	private MatrixUtils matrixUtil = new MatrixUtils();
	
	/** Light's point of view projection matrix with bias multiplied */
	private Matrix4f lightProjWithBiasMtx;
	
	/** Final texture projection matrix */
	private Matrix4f textureProjMatrix;
	
	/** Coordinate generator */
	private TexCoordGeneration coordGen;
	
	/** Current light geometry's spotlight transformation */
	private Matrix4f curSpotlightTransform;
	
	/** Translation of the torus */
	private Vector3f curTorusTranslation;
	
	private float []sRow = new float[4];
	private float []tRow = new float[4];
	private float []qRow = new float[4];
	private float []rRow = new float[4];
	
    /**
     * Constructor
     */
	public ShadowMappingAnimator(TransformGroup torusLightPointofView,
								 TransformGroup torusCamerasView,
								 SpotLight lPovSpotLight,
								 SpotLight cameraSpotLight,
								 TransformGroup spotlightTransform,
								 TransformGroup lightGeomGroup,
								 Point3f lightPos,
								 Point3f lookAtPos,
								 Vector3f worldUp,
								 Matrix4f lightProjWithBias,
								 TexCoordGeneration coordGenerator) {
		lightMat = new Matrix4f();
		lightMat.setIdentity();
		coordGen = coordGenerator;
		
		curTorusTranslation = new Vector3f(0, 1, 0);
		curSpotlightTransform = new Matrix4f();
		
		lightDir = new Vector3f();
		
		textureProjMatrix = new Matrix4f();
		textureProjMatrix.setIdentity();
		
		lightGeometryGroup = lightGeomGroup;
		lightProjWithBiasMtx = lightProjWithBias;
		this.lightPos = lightPos;
		lightLookAtPos = lookAtPos;
		worldUpVec = worldUp;
		matrix = new Matrix4f();
		matrix.setIdentity();
		rotation = 0.0f;
		
		spotlightViewTransform = spotlightTransform;
		
		lightViewSpotLight = lPovSpotLight;
		cameraViewSpotLight = cameraSpotLight;
		
		lightPointofView = torusLightPointofView;
		camerasPointofView = torusCamerasView;
	}
	
    //---------------------------------------------------------------
    // Methods defined by ApplicationUpdateObserver
    //---------------------------------------------------------------

    /**
     * Notification that now is a good time to update the scene graph.
     */
    public void updateSceneGraph() {
    	
    	rotation += ROTATION_INC;

        matrix.rotZ(rotation);
        matrix.setTranslation(curTorusTranslation);
        
        lightPos.x = (float)Math.sin((double)rotation) * 3;

        if(lightPointofView.isLive()) {
        	lightPointofView.boundsChanged(this);
        } else {
        	updateNodeBoundsChanges(lightPointofView);
        }

        if(camerasPointofView.isLive()) {
        	camerasPointofView.boundsChanged(this);
        } else {
        	updateNodeBoundsChanges(camerasPointofView);
        }

        if(lightViewSpotLight.isLive()) {
        	lightViewSpotLight.dataChanged(this);
        } else {
        	updateNodeDataChanges(lightViewSpotLight);
        }

        if(cameraViewSpotLight.isLive()) {
        	cameraViewSpotLight.dataChanged(this);
        } else {
        	updateNodeDataChanges(cameraViewSpotLight);
        }
        
        if(lightGeometryGroup.isLive()) {
        	lightGeometryGroup.boundsChanged(this);
        } else {
        	updateNodeBoundsChanges(lightGeometryGroup);
        }
        
        if(spotlightViewTransform.isLive()) {
        	spotlightViewTransform.boundsChanged(this);
        } else {
        	updateNodeBoundsChanges(spotlightViewTransform);
        }
        
        if(coordGen.isLive()) {
        	coordGen.dataChanged(this);
        } else {
        	updateNodeDataChanges(coordGen);
        }
    }

    /**
     * Notification that the AV3D internal shutdown handler has detected a
     * system-wide shutdown. The aviatrix code has already terminated rendering
     * at the point this method is called, only the user's system code needs to
     * terminate before exiting here.
     */
    public void appShutdown()
    {
        // do nothing
    }
    
    //----------------------------------------------------------
    // Methods defined by NodeUpdateListener
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src) {
        if(src instanceof TransformGroup) {
        	if(src == lightPointofView || src == camerasPointofView) {
            	((TransformGroup)src).setTransform(matrix);
        	} else if(src == lightGeometryGroup) {
            	matrixUtil.lookAt(lightPos,
            			lightLookAtPos,
            			worldUpVec,
            			curSpotlightTransform);
            	curSpotlightTransform.invert();
            	curSpotlightTransform.m10 = -curSpotlightTransform.m10;
            	curSpotlightTransform.m11 = -curSpotlightTransform.m11;
            	curSpotlightTransform.m12 = -curSpotlightTransform.m12;
        		lightGeometryGroup.setTransform(curSpotlightTransform);
        	} else if(src == spotlightViewTransform) {
            	matrixUtil.lookAt(lightPos,
            			lightLookAtPos,
            			worldUpVec,
            			curSpotlightTransform);
            	textureProjMatrix.set(lightProjWithBiasMtx);
            	textureProjMatrix.mul(curSpotlightTransform);
            	
            	curSpotlightTransform.invert();

        		spotlightViewTransform.setTransform(curSpotlightTransform);
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
    	if (src instanceof SpotLight) {
        	
        	if(src == lightViewSpotLight) {
        		lightViewSpotLight.setPosition(lightPos.x,
        									   lightPos.y,
    									   	   lightPos.z);
	        	
	        	lightDir.x = lightLookAtPos.x - lightPos.x;
	        	lightDir.y = lightLookAtPos.y - lightPos.y;
	        	lightDir.z = lightLookAtPos.z - lightPos.z;
	        	lightDir.normalize();
	        	
	        	lightViewSpotLight.setDirection(lightDir.x,
	        									lightDir.y,
        										lightDir.z);

        	} else if(src == cameraViewSpotLight) {
        		cameraViewSpotLight.setPosition(lightPos.x,
											    lightPos.y,
										   	    lightPos.z);
	        	
	        	lightDir.x = lightLookAtPos.x - lightPos.x;
	        	lightDir.y = lightLookAtPos.y - lightPos.y;
	        	lightDir.z = lightLookAtPos.z - lightPos.z;
	        	lightDir.normalize();
				
				cameraViewSpotLight.setDirection(lightDir.x,
												 lightDir.y,
												 lightDir.z);
        	}
        } else if(src instanceof TexCoordGeneration) {

        	textureProjMatrix.getRow(0, sRow);
        	textureProjMatrix.getRow(1, tRow);
        	textureProjMatrix.getRow(2, rRow);
        	textureProjMatrix.getRow(3, qRow);
        	
        	coordGen.setParameter(TexCoordGeneration.TEXTURE_S,
			                      TexCoordGeneration.MODE_GENERIC,
			                      TexCoordGeneration.MAP_EYE_LINEAR,
			                      TexCoordGeneration.MODE_EYE_PLANE,
			                      sRow);
						
        	coordGen.setParameter(TexCoordGeneration.TEXTURE_T,
			                      TexCoordGeneration.MODE_GENERIC,
			                      TexCoordGeneration.MAP_EYE_LINEAR,
			                      TexCoordGeneration.MODE_EYE_PLANE,
			                      tRow);
			
        	coordGen.setParameter(TexCoordGeneration.TEXTURE_R,
			                      TexCoordGeneration.MODE_GENERIC,
			                      TexCoordGeneration.MAP_EYE_LINEAR,
			                      TexCoordGeneration.MODE_EYE_PLANE,
			                      rRow);
			
        	coordGen.setParameter(TexCoordGeneration.TEXTURE_Q,
				                  TexCoordGeneration.MODE_GENERIC,
				                  TexCoordGeneration.MAP_EYE_LINEAR,
			                      TexCoordGeneration.MODE_EYE_PLANE,
			                      qRow);
        }
    }
}
