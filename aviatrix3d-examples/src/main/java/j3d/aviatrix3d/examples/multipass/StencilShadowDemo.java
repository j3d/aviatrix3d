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

// External imports
import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;

import javax.media.opengl.GLCapabilities;

import javax.vecmath.*;

import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.pipeline.graphics.FrustumCullStage;
import org.j3d.aviatrix3d.output.graphics.SimpleAWTSurface;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsCullStage;
import org.j3d.aviatrix3d.pipeline.graphics.DefaultGraphicsPipeline;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;
import org.j3d.aviatrix3d.pipeline.graphics.StateAndTransparencyDepthSortStage;

import org.j3d.aviatrix3d.pipeline.graphics.GraphicsSortStage;
import org.j3d.aviatrix3d.management.SingleThreadRenderManager;
import org.j3d.aviatrix3d.management.SingleDisplayCollection;

import org.j3d.renderer.aviatrix3d.geom.Box;

// Local imports

/**
 * Main class that creates a hard shadowing scene using shadow volume technique.
 * <p>
 * 
 * This example code demonstrates how hard shadow effects can be implemented using 
 * aviatrix3d scene graph.  Essentially, multiple render passes are used for drawing
 * hard shadow effect.  The algorithm used in this demo code is z-fail a.k.a.
 * Carmack's reverse.
 *
 * @author Sang Park
 * @version $Revision: 1.2 $
 */
public class StencilShadowDemo extends Frame
							   implements WindowListener {
	
    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;
    
    /** Silhouette edge geometry */
    private SEdgeIndTriArray silhouetteEdgeGeom;
    
    /** 
     * Global transformation matrix of the silhouette edge
     * geometry and the shadow volume geometry.
     */
    private Matrix4f geomTransform;
    
    /** Global position of the light */
    private Vector4f lightPos;
    
    /**
     * Constructor
     */
	public StencilShadowDemo() {
		
        super("Stencil Shadow Demo");
        
        setLayout(new BorderLayout());
        addWindowListener(this);

        setupAviatrix();
        setupSceneGraph();

        setSize(500, 500);
        setLocation(40, 40);

        // Need to set visible first before starting the rendering thread due
        // to a bug in JOGL. See JOGL Issue #54 for more information on this.
        // http://jogl.dev.java.net
        setVisible(true);
	}

    /**
     * Setup the avaiatrix pipeline here
     */
    private void setupAviatrix()
    {
        // Assemble a simple single-threaded pipeline.
        GLCapabilities caps = new GLCapabilities();
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);
        caps.setStencilBits(8);

        GraphicsCullStage culler = new FrustumCullStage();
        culler.setOffscreenCheckEnabled(true);

        GraphicsSortStage sorter = new StateAndTransparencyDepthSortStage();
        surface = new SimpleAWTSurface(caps);
        DefaultGraphicsPipeline pipeline = new DefaultGraphicsPipeline();

        pipeline.setCuller(culler);
        pipeline.setSorter(sorter);
        pipeline.setGraphicsOutputDevice(surface);

        displayManager = new SingleDisplayCollection();
        displayManager.addPipeline(pipeline);

        // Render manager
        sceneManager = new SingleThreadRenderManager();
        sceneManager.addDisplay(displayManager);
        sceneManager.setMinimumFrameInterval(0);

        // Before putting the pipeline into run mode, put the canvas on
        // screen first.
        Component comp = (Component)surface.getSurfaceObject();
        add(comp, BorderLayout.CENTER);
    }

    /**
     * Setup the basic scene which consists of a quad and a viewpoint
     */
    private void setupSceneGraph()
    {
    	// Set all multipass scenes.
        MultipassScene mainScene = new MultipassScene();
        
        TransformGroup volumeGroupPass1 = new TransformGroup();
        TransformGroup volumeGroupPass2 = new TransformGroup();
        
        geomTransform = new Matrix4f();
        geomTransform.setIdentity();
        
        lightPos = new Vector4f(0.9f, 0.9f, 0.0f, 1.0f);
        
        ArrayList<SEdgeIndTriArray> sceneGeomList = 
        	new ArrayList<SEdgeIndTriArray>();
        
        silhouetteEdgeGeom = createSilhouetteEdgeGeom();
        
        sceneGeomList.add(silhouetteEdgeGeom);
        
        StencilShadowAnimator shadowAnim = 
        	new StencilShadowAnimator(geomTransform,
        							  geomTransform,
        							  lightPos,
        							  sceneGeomList,
        							  volumeGroupPass1,
        							  volumeGroupPass2);
        		
        mainScene.addRenderPass(createNoLightScenePass(silhouetteEdgeGeom, 
        											   geomTransform,
        											   lightPos));
        mainScene.addRenderPass(createVolumeCastPass1(lightPos,
        											  volumeGroupPass1));
        mainScene.addRenderPass(createVolumeCastPass2(lightPos,
        											  volumeGroupPass2));
        mainScene.addRenderPass(createWholeScenePass(silhouetteEdgeGeom,
        											 geomTransform,
        											 lightPos));
        
        MultipassViewport sceneViewport = new MultipassViewport();
        sceneViewport.setDimensions(0, 0, 500, 500);
    	sceneViewport.setScene(mainScene);
    	
    	// Set layer
    	SimpleLayer mainLayer = new SimpleLayer();
    	mainLayer.setViewport(sceneViewport);
    	
    	Layer[] layers = { mainLayer };
    	
    	// Add to the display manager
    	displayManager.setLayers(layers, 1);
    	
    	sceneManager.setApplicationObserver(shadowAnim);
    }
    
    /**
     * Creates an instance of silhouette edged geometry
     * 
     * @return Instance of silhouette edged geometry
     */
    private SEdgeIndTriArray createSilhouetteEdgeGeom() {
    	
		SEdgeIndTriArray indexTriAry = new SEdgeIndTriArray();
		indexTriAry.setVertices(IndexedTriangleArray.COORDINATE_3, new float[] {-0.2f, 0.2f, 0.2f,
																				-0.2f, -0.2f, 0.2f,
																				0.2f, 0.2f, 0.2f,
																				0.2f, -0.2f, 0.2f,
																				0.2f, 0.2f, -0.2f,
																				0.2f, -0.2f, -0.2f,
																				-0.2f, 0.2f, -0.2f,
																				-0.2f, -0.2f, -0.2f});
		
		indexTriAry.setIndices(new int[] {0, 1, 3,
										  0, 3, 2,
										  4, 5, 7,
										  4, 7, 6,
										  6, 7, 1,
										  6, 1, 0,
										  2, 3, 5,
										  2, 5, 4,
										  6, 0, 2,
										  6, 2, 4,
										  1, 7, 5,
										  1, 5, 3}, 
										  36);
		
		indexTriAry.setNormals(new float[] {-0.23570226f, 0.23570226f, 0.94280905f ,
											-0.57735026f, -0.57735026f, 0.57735026f ,
											0.57735026f, 0.57735026f, 0.57735026f ,
											0.23570226f, -0.23570226f, 0.94280905f ,
											0.23570226f, 0.23570226f, -0.94280905f ,
											0.57735026f, -0.57735026f, -0.57735026f ,
											-0.57735026f, 0.57735026f, -0.57735026f ,
											-0.23570226f, -0.23570226f, -0.94280905f});
    	return indexTriAry;
    }
    
    /**
     * Creates a room geometry which shadow will be casted onto.
     * 
     * @param roomBoxWidth Width of the room box
     * @param roomBoxHeight Height of the room box
     * @param boxthickness Thickness of the room box
     * @param polyAttrib Polygon attribute
     * @return TransformGroup containing a room geometry
     */
    private TransformGroup createRoom(float roomBoxWidth,
									  float roomBoxHeight,
									  float boxthickness,
									  PolygonAttributes polyAttrib,
									  BlendAttributes blendAttrib) {
    	
		Material material = new Material();
		material.setDiffuseColor(new float[] {0.5f, 0.5f, 1.0f, 1});
		material.setAmbientColor(new float[] {0.05f, 0.05f, 0.05f, 1});
		material.setSpecularColor(new float[] {0.05f, 0.05f, 0.05f, 1});
		material.setShininess(0.01f);
		
		Appearance roomApp = new Appearance();
		roomApp.setMaterial(material);
		roomApp.setPolygonAttributes(polyAttrib);
		roomApp.setBlendAttributes(blendAttrib);
		
		Box roomGeomLeft = new Box(boxthickness, roomBoxHeight, roomBoxWidth, roomApp);
		Box roomGeomRight = new Box(boxthickness, roomBoxHeight, roomBoxWidth, roomApp);
		Box roomGeomBack = new Box(roomBoxWidth, roomBoxHeight, boxthickness, roomApp);
		Box roomGeomBottom = new Box(roomBoxWidth, boxthickness, roomBoxHeight, roomApp);
		
		Matrix4f mat = new Matrix4f();

		mat.setIdentity();
		mat.setTranslation(new Vector3f(-roomBoxWidth / 2, 0, 0));
		TransformGroup roomLeft = new TransformGroup();
		roomLeft.setTransform(mat);
		roomLeft.addChild(roomGeomLeft);

		mat.setIdentity();
		mat.setTranslation(new Vector3f(roomBoxWidth / 2, 0, 0));
		TransformGroup roomRight = new TransformGroup();
		roomRight.setTransform(mat);
		roomRight.addChild(roomGeomRight);

		mat.setIdentity();
		mat.setTranslation(new Vector3f(0, 0, -roomBoxHeight / 2));
		TransformGroup roomBack = new TransformGroup();
		roomBack.setTransform(mat);
		roomBack.addChild(roomGeomBack);

		mat.setIdentity();
		mat.setTranslation(new Vector3f(0, -roomBoxHeight / 2, 0));
		TransformGroup roomBottom = new TransformGroup();
		roomBottom.setTransform(mat);
		roomBottom.addChild(roomGeomBottom);
		
		TransformGroup roomGroup = new TransformGroup();
		roomGroup.addChild(roomLeft);
		roomGroup.addChild(roomRight);
		roomGroup.addChild(roomBack);
		roomGroup.addChild(roomBottom);
		
		return roomGroup;
    }
    
    /**
     * Creates an object that a shadow will be casted from.
     * 
     * @param polyAttrib Polygon attribute of the geometry
     * 
     * @return TransfromGroup containing shadow-able geometry.
     */
    private TransformGroup createGeom(Geometry geom,
    								  PolygonAttributes polyAttrib,
    								  BlendAttributes blendAttrib,
    								  Matrix4f transform) {

    	float[] geomDiffuse = {0.0f, 0.0f, 0.75f, 1};
    	float[] geomAmbient = {0.25f, 0.25f, 0.25f, 1};
    	float[] geomSpecular = {0.65f, 0.65f, 0.65f, 1};
    	float shiniess = 0.01f;
    	
    	Material geomMat = new Material();
		geomMat.setDiffuseColor(geomDiffuse);
		geomMat.setAmbientColor(geomAmbient);
		geomMat.setSpecularColor(geomSpecular);
		geomMat.setShininess(shiniess);
		
		Appearance appNode = new Appearance();
		appNode.setMaterial(geomMat);
		appNode.setPolygonAttributes(polyAttrib);
		appNode.setBlendAttributes(blendAttrib);
		
		Shape3D shapeGeom = new Shape3D();
		shapeGeom.setGeometry(geom);
		shapeGeom.setAppearance(appNode);
		
		TransformGroup shadowGroup = new TransformGroup();
		
		shadowGroup.addChild(shapeGeom);
		//shadowGroup.setTransform(transform);
		
		return shadowGroup;
    }
    
    /**
     * Creates a whole scene with no lights
     * 
     * @return Single render pass
     */
    private RenderPass createNoLightScenePass(Geometry geom, Matrix4f geomTransform, Vector4f lightPos)
    {
    	RenderPass renderPass = new RenderPass();
    	
    	DepthBufferState depthState = new DepthBufferState();
		depthState.setClearBufferState(true);
		depthState.enableDepthTest(true);
		depthState.enableDepthWrite(true);
		depthState.setDepthFunction(DepthBufferState.FUNCTION_LESS);
		
		StencilBufferState stencilState = new StencilBufferState();
		stencilState.setClearBufferState(true);
		
		ColorBufferState colorState = new ColorBufferState();
		colorState.setClearBufferState(true);
		colorState.setClearColor(0, 0, 0, 0);
		colorState.setColorMask(true, true, true, true);
		
		PolygonAttributes polyAttrib = new PolygonAttributes();
		polyAttrib.setCulledFace(PolygonAttributes.CULL_NONE);
		
		BlendAttributes blendAttrib = new BlendAttributes();
		
		renderPass.setStencilBufferState(stencilState);
		renderPass.setDepthBufferState(depthState);
		renderPass.setColorBufferState(colorState);
		
		// Setup scene
        Viewpoint vp = new Viewpoint();
        vp.setHeadlightEnabled(false);
        
        Vector3f trans = new Vector3f(0.0f, 0.0f, 3f);
		
        Matrix4f mat = new Matrix4f();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);
        scene_root.addChild(createGeom(geom, polyAttrib, blendAttrib, geomTransform));
        scene_root.addChild(createRoom(2, 2, 0.01f, polyAttrib, blendAttrib));

		renderPass.setActiveView(vp);
		renderPass.setRenderedGeometry(scene_root);
		
    	return renderPass;
    }
    
    /**
     * Creates a shadow volume scene where all polygons
     * are front facing.
     * 
     * @return Single render pass
     */
    private RenderPass createVolumeCastPass1(Vector4f lightPos,
    										 TransformGroup group)
    {
    	RenderPass renderPass = new RenderPass();
    	
    	DepthBufferState depthState = new DepthBufferState();
		depthState.setClearBufferState(false);
		depthState.enableDepthWrite(false);
		depthState.enableDepthTest(false);
		depthState.setDepthFunction(DepthBufferState.FUNCTION_LESS);
		
		ColorBufferState colorState = new ColorBufferState();
		colorState.setColorMask(false, false, false, false);
		colorState.setClearBufferState(false);
		
		StencilBufferState stencilState = new StencilBufferState();
		stencilState.setClearBufferState(false);
		stencilState.setStencilFunction(StencilAttributes.FUNCTION_ALWAYS);
		stencilState.setFunctionReferenceValue(0x0);
		stencilState.setFunctionCompareMask(~0x0);
		stencilState.setStencilFailOperation(StencilAttributes.STENCIL_KEEP);
		stencilState.setDepthFailOperation(StencilAttributes.STENCIL_INCREMENT);
		stencilState.setDepthPassOperation(StencilAttributes.STENCIL_KEEP);
		
		renderPass.setStencilBufferState(stencilState);
		renderPass.setDepthBufferState(depthState);
		renderPass.setColorBufferState(colorState);
		
		// Setup scene        
		Viewpoint vp = new Viewpoint();
        vp.setHeadlightEnabled(false);
        
        Vector3f trans = new Vector3f(0.0f, 0.0f, 3f);
		
        Matrix4f mat = new Matrix4f();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);
        
        Group scene_root = new Group();
        scene_root.addChild(tx);

        scene_root.addChild(group);
		renderPass.setActiveView(vp);
		renderPass.setRenderedGeometry(scene_root);
		
    	return renderPass;
    }
    
    /**
     * Creates a shadow volume scene where all polygons
     * are back facing.
     * 
     * @return Single render pass
     */
    private RenderPass createVolumeCastPass2(Vector4f lightPos,
    										 TransformGroup group)
    {
    	RenderPass renderPass = new RenderPass();
    	
    	DepthBufferState depthState = new DepthBufferState();
		depthState.setClearBufferState(false);
		depthState.enableDepthWrite(false);
		depthState.enableDepthTest(false);
		depthState.setDepthFunction(DepthBufferState.FUNCTION_LESS);
		
		ColorBufferState colorState = new ColorBufferState();
		colorState.setColorMask(false, false, false, false);
		colorState.setClearBufferState(false);
		
		StencilBufferState stencilState = new StencilBufferState();
		stencilState.setClearBufferState(false);
		stencilState.setStencilFunction(StencilAttributes.FUNCTION_ALWAYS);
		stencilState.setFunctionReferenceValue(0x0);
		stencilState.setFunctionCompareMask(~0x0);
		stencilState.setStencilFailOperation(StencilAttributes.STENCIL_KEEP);
		stencilState.setDepthFailOperation(StencilAttributes.STENCIL_DECREMENT);
		stencilState.setDepthPassOperation(StencilAttributes.STENCIL_KEEP);
		
		renderPass.setStencilBufferState(stencilState);
		renderPass.setDepthBufferState(depthState);
		renderPass.setColorBufferState(colorState);
		
		// Setup scene        
		Viewpoint vp = new Viewpoint();
        vp.setHeadlightEnabled(false);
        
        Vector3f trans = new Vector3f(0.0f, 0.0f, 3f);
		
        Matrix4f mat = new Matrix4f();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);
        
        Group scene_root = new Group();
        scene_root.addChild(tx);

        scene_root.addChild(group);
		renderPass.setActiveView(vp);
		renderPass.setRenderedGeometry(scene_root);
		
    	return renderPass;
    }
    
    /**
     * Creates a whole scene with lights
     * 
     * @return Single render pass
     */
    private RenderPass createWholeScenePass(Geometry geom, Matrix4f geomTransform, Vector4f lightPos)
    {
    	RenderPass renderPass = new RenderPass();
    	
    	DepthBufferState depthState = new DepthBufferState();
    	depthState.setDepthFunction(DepthBufferState.FUNCTION_LESS_OR_EQUAL);
    	depthState.setClearBufferState(false);
    	depthState.enableDepthWrite(true);
    	depthState.enableDepthTest(true);
		
		StencilBufferState stencilState = new StencilBufferState();
		stencilState.setStencilFunction(StencilAttributes.FUNCTION_EQUAL);
		stencilState.setFunctionReferenceValue(0x0);
		stencilState.setFunctionCompareMask(~0x0);
		stencilState.setStencilFailOperation(StencilAttributes.STENCIL_KEEP);
		stencilState.setDepthFailOperation(StencilAttributes.STENCIL_KEEP);
		stencilState.setDepthPassOperation(StencilAttributes.STENCIL_KEEP);
		stencilState.setClearBufferState(false);
		
		ColorBufferState colorState = new ColorBufferState();
		colorState.setColorMask(true, true, true, true);
		colorState.setClearBufferState(false);
		
		PolygonAttributes polyAttrib = new PolygonAttributes();
		polyAttrib.setCulledFace(PolygonAttributes.CULL_BACK);
		
		BlendAttributes blendAttrib = new BlendAttributes();
		
		renderPass.setStencilBufferState(stencilState);
		renderPass.setDepthBufferState(depthState);
		renderPass.setColorBufferState(colorState);
		
		// Setup scene
        Viewpoint vp = new Viewpoint();
        vp.setHeadlightEnabled(false);
        
        Vector3f trans = new Vector3f(0.0f, 0.0f, 3f);
		
        Matrix4f mat = new Matrix4f();
        mat.setIdentity();
        mat.setTranslation(trans);

        TransformGroup tx = new TransformGroup();
        tx.addChild(vp);
        tx.setTransform(mat);

        Group scene_root = new Group();
        scene_root.addChild(tx);
        scene_root.addChild(createGeom(geom, polyAttrib, blendAttrib, geomTransform));
        scene_root.addChild(createRoom(2, 2, 0.01f, polyAttrib, blendAttrib));
		
    	float[] lightDiffuse = new float[] {1.0f, 1.0f, 1.0f, 1};
    	float[] lightAmbient = new float[] {1.0f, 1.0f, 1.0f, 1};
    	float[] lightSpecular = new float[] {1.0f, 1.0f, 1.0f, 1};
    	
        PointLight pointLight = new PointLight();
		pointLight.setDiffuseColor(lightDiffuse);
		pointLight.setAmbientColor(lightAmbient);
		pointLight.setSpecularColor(lightSpecular);
		pointLight.setGlobalOnly(true);
		pointLight.setEnabled(true);
		pointLight.setPosition(lightPos.x, lightPos.y, lightPos.z);
		
		scene_root.addChild(pointLight);
		
		renderPass.setActiveView(vp);
		renderPass.setRenderedGeometry(scene_root);
    	return renderPass;
    }
    
    //---------------------------------------------------------------
    // Methods defined by WindowListener
    //---------------------------------------------------------------

    /**
     * Ignored
     */
    public void windowActivated(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    public void windowClosed(WindowEvent evt)
    {
    }

    /**
     * Exit the application
     *
     * @param evt The event that caused this method to be called.
     */
    public void windowClosing(WindowEvent evt)
    {
        sceneManager.shutdown();
        System.exit(0);
    }

    /**
     * Ignored
     */
    public void windowDeactivated(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    public void windowDeiconified(WindowEvent evt)
    {
    }

    /**
     * Ignored
     */
    public void windowIconified(WindowEvent evt)
    {
    }

    /**
     * When the window is opened, start everything up.
     */
    public void windowOpened(WindowEvent evt)
    {
        sceneManager.setEnabled(true);
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

	/**
	 * Starts application's main process
	 */
	public static void main(String[] args) {
		StencilShadowDemo shadowDemo = new StencilShadowDemo();
	}
}
