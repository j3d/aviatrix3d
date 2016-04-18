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

package j3d.aviatrix3d.examples.texture;

// External imports

import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;
import org.j3d.maths.vector.Point3d;

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
                   NodeUpdateListener
{

    /**
     * The amount to rotate the object each frame, in radians
     */
    private static final float ROTATION_INC = (float) (Math.PI / 200);

    /**
     * A utility matrix used for updating the transforms each frame
     */
    private Matrix4d matrix;

    /**
     * The current angle of object rotation
     */
    private float rotation;

    /**
     * TG from light's point of view
     */
    private TransformGroup lightPointofView;

    /**
     * TG from camera's point of view
     */
    private TransformGroup camerasPointofView;

    /**
     * A utility matrix used for updating light position
     */
    private Matrix4d lightMat;

    /**
     * Spotlight from light's point of view
     */
    private SpotLight lightViewSpotLight;

    /**
     * Spotlight form camera's viewpoint
     */
    private SpotLight cameraViewSpotLight;

    /**
     * Updated light position
     */
    private Point3d lightPos;

    /**
     * Updated light direction
     */
    private Vector3d lightDir;

    /**
     * Light lookat Position
     */
    private Point3d lightLookAtPos;

    /**
     * World up vector
     */
    private Vector3d worldUpVec;

    /**
     * Camera transfrom from light's point of view
     */
    private TransformGroup spotlightViewTransform;

    /**
     * TG that contains the geometry that represents light
     */
    private TransformGroup lightGeometryGroup;

    /**
     * Utility to calculate the matrix
     */
    private MatrixUtils matrixUtils;

    /**
     * Light's point of view projection matrix with bias multiplied
     */
    private Matrix4d lightProjWithBiasMtx;

    /**
     * Final texture projection matrix
     */
    private Matrix4d textureProjMatrix;

    /**
     * Coordinate generator
     */
    private TexCoordGeneration coordGen;

    /**
     * Current light geometry's spotlight transformation
     */
    private Matrix4d curSpotlightTransform;

    /**
     * Translation of the torus
     */
    private Vector3d curTorusTranslation;

    private float[] sRow = new float[4];
    private float[] tRow = new float[4];
    private float[] qRow = new float[4];
    private float[] rRow = new float[4];

    /**
     * Constructor
     */
    public ShadowMappingAnimator(TransformGroup torusLightPointofView,
                                 TransformGroup torusCamerasView,
                                 SpotLight lPovSpotLight,
                                 SpotLight cameraSpotLight,
                                 TransformGroup spotlightTransform,
                                 TransformGroup lightGeomGroup,
                                 Point3d lightPos,
                                 Point3d lookAtPos,
                                 Vector3d worldUp,
                                 Matrix4d lightProjWithBias,
                                 TexCoordGeneration coordGenerator)
    {
        matrixUtils = new MatrixUtils();

        lightMat = new Matrix4d();
        lightMat.setIdentity();
        coordGen = coordGenerator;

        curTorusTranslation = new Vector3d();
        curTorusTranslation.set(0, 1, 0);
        curSpotlightTransform = new Matrix4d();

        lightDir = new Vector3d();

        textureProjMatrix = new Matrix4d();
        textureProjMatrix.setIdentity();

        lightGeometryGroup = lightGeomGroup;
        lightProjWithBiasMtx = lightProjWithBias;
        this.lightPos = lightPos;
        lightLookAtPos = lookAtPos;
        worldUpVec = worldUp;
        matrix = new Matrix4d();
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
    public void updateSceneGraph()
    {

        rotation += ROTATION_INC;

        matrixUtils.rotateZ(rotation, matrix);
        matrix.setTranslation(curTorusTranslation);

        lightPos.x = (float) Math.sin((double) rotation) * 3;

        if (lightPointofView.isLive())
        {
            lightPointofView.boundsChanged(this);
        }
        else
        {
            updateNodeBoundsChanges(lightPointofView);
        }

        if (camerasPointofView.isLive())
        {
            camerasPointofView.boundsChanged(this);
        }
        else
        {
            updateNodeBoundsChanges(camerasPointofView);
        }

        if (lightViewSpotLight.isLive())
        {
            lightViewSpotLight.dataChanged(this);
        }
        else
        {
            updateNodeDataChanges(lightViewSpotLight);
        }

        if (cameraViewSpotLight.isLive())
        {
            cameraViewSpotLight.dataChanged(this);
        }
        else
        {
            updateNodeDataChanges(cameraViewSpotLight);
        }

        if (lightGeometryGroup.isLive())
        {
            lightGeometryGroup.boundsChanged(this);
        }
        else
        {
            updateNodeBoundsChanges(lightGeometryGroup);
        }

        if (spotlightViewTransform.isLive())
        {
            spotlightViewTransform.boundsChanged(this);
        }
        else
        {
            updateNodeBoundsChanges(spotlightViewTransform);
        }

        if (coordGen.isLive())
        {
            coordGen.dataChanged(this);
        }
        else
        {
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
    public void updateNodeBoundsChanges(Object src)
    {
        if (src instanceof TransformGroup)
        {
            if (src == lightPointofView || src == camerasPointofView)
            {
                ((TransformGroup) src).setTransform(matrix);
            }
            else if (src == lightGeometryGroup)
            {
                matrixUtils.lookAt(lightPos,
                                   lightLookAtPos,
                                   worldUpVec,
                                   curSpotlightTransform);

                matrixUtils.inverse(curSpotlightTransform, curSpotlightTransform);
                curSpotlightTransform.m10 = -curSpotlightTransform.m10;
                curSpotlightTransform.m11 = -curSpotlightTransform.m11;
                curSpotlightTransform.m12 = -curSpotlightTransform.m12;
                lightGeometryGroup.setTransform(curSpotlightTransform);
            }
            else if (src == spotlightViewTransform)
            {
                matrixUtils.lookAt(lightPos,
                                   lightLookAtPos,
                                   worldUpVec,
                                   curSpotlightTransform);
                textureProjMatrix.set(lightProjWithBiasMtx);
                textureProjMatrix.mul(textureProjMatrix, curSpotlightTransform);

                matrixUtils.inverse(curSpotlightTransform, curSpotlightTransform);

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
    public void updateNodeDataChanges(Object src)
    {
        if (src instanceof SpotLight)
        {

            if (src == lightViewSpotLight)
            {
                lightViewSpotLight.setPosition((float) lightPos.x, (float) lightPos.y, (float) lightPos.z);

                lightDir.x = lightLookAtPos.x - lightPos.x;
                lightDir.y = lightLookAtPos.y - lightPos.y;
                lightDir.z = lightLookAtPos.z - lightPos.z;
                lightDir.normalise();

                lightViewSpotLight.setDirection((float) lightDir.x, (float) lightDir.y, (float) lightDir.z);

            }
            else if (src == cameraViewSpotLight)
            {
                cameraViewSpotLight.setPosition((float) lightPos.x, (float) lightPos.y, (float) lightPos.z);

                lightDir.x = lightLookAtPos.x - lightPos.x;
                lightDir.y = lightLookAtPos.y - lightPos.y;
                lightDir.z = lightLookAtPos.z - lightPos.z;
                lightDir.normalise();

                cameraViewSpotLight.setDirection((float) lightDir.x, (float) lightDir.y, (float) lightDir.z);
            }
        }
        else if (src instanceof TexCoordGeneration)
        {
            sRow[0] = (float)textureProjMatrix.m00;
            sRow[1] = (float)textureProjMatrix.m01;
            sRow[2] = (float)textureProjMatrix.m02;
            sRow[3] = (float)textureProjMatrix.m03;

            tRow[0] = (float)textureProjMatrix.m10;
            tRow[1] = (float)textureProjMatrix.m11;
            tRow[2] = (float)textureProjMatrix.m12;
            tRow[3] = (float)textureProjMatrix.m13;

            rRow[0] = (float)textureProjMatrix.m20;
            rRow[1] = (float)textureProjMatrix.m21;
            rRow[2] = (float)textureProjMatrix.m22;
            rRow[3] = (float)textureProjMatrix.m23;

            qRow[0] = (float)textureProjMatrix.m30;
            qRow[1] = (float)textureProjMatrix.m31;
            qRow[2] = (float)textureProjMatrix.m32;
            qRow[3] = (float)textureProjMatrix.m33;

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
