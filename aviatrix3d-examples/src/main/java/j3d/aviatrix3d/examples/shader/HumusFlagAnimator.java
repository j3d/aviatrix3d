/*****************************************************************************
 *                        Yumtech, Inc Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package j3d.aviatrix3d.examples.shader;

// External imports
import java.util.Random;

import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector3d;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.geom.spring.SpringEvaluatorCallback;
import org.j3d.geom.spring.SpringNode;
import org.j3d.geom.spring.SpringSystem;
import org.j3d.texture.procedural.PerlinNoiseGenerator;

public class HumusFlagAnimator
    implements ApplicationUpdateObserver,
               NodeUpdateListener,
               SpringEvaluatorCallback
{
    /** Maximum random number value */
    private static final float MAX_RAND = 0x7FFF;

    /** The number of vertices for the cloth in the X direction */
    static final int CLOTH_SIZE_X = 54;

    /** The number of vertices for the cloth in the T direction */
    static final int CLOTH_SIZE_Y = 42;

    /** Value of the real size of the cloth */
    static final float C_SIZE  = 320.0f / CLOTH_SIZE_X;

    /** Time between state changes in seconds */
    private static final int T_DIFF = 8;

    /** The texture type constant needed when updating texture coordinates */
    private static final int[] TEX_TYPES =
        { VertexGeometry.TEXTURE_COORDINATE_2 };

    /** The springs used to model the cloth */
    private SpringSystem springSystem;

    /** A sum value for determining when to change to a new cloth */
    private int sum;

    /** The current texture being rendered */
    private Texture2D currentTexture;

    /** List of available textures for the flag */
    private Texture2D[] flagTextures;

    /* Transform that holds the geometry to the "light" */
    private TransformGroup lightGroup;

    /* Transform that holds the geometry to the cloth */
    private TransformGroup clothGroup;

    /** The Geometry of the flag */
    private IndexedTriangleStripArray clothGeometry;

    /** Array to recalculate the cloth geometry vertex list */
    private float[] clothVertices;

    /** Array to recalculate the cloth geometry normal list */
    private float[] clothNormals;

    /** Array to recalculate the cloth geometry texture coordinate list*/
    private float[][] clothTexCoords;

    /** Arguments for the cloth shader */
    private ShaderArguments clothArgs;

    /** The texture unit for the flag so we can change the texture */
    private TextureUnit clothTextureUnit;


    /** Arguments for the arrow shaders */
    private ShaderArguments arrowArgs;

    /** Transforms holding each arrow */
    private TransformGroup arrowTransform;

    /** Array to recalculate the arrow geometry vertex list */
    private float[] arrowVertices;

    /** Geometry used for the arrow */
    private TriangleFanArray arrowGeometry;

    /** Arguments for the arrow shaders */
    private ShaderArguments poleArgs;

    /** Transforms holding arrows on each pole of the cloth */
    private TransformGroup poleTransforms;

    /** Shape objects containing each arrow */
    private Appearance poleAppearances;

    /** Flags indicating the visibility state of each arrow */
    private boolean[] poleVisibility;

    /** The evaluation time of the cloth intervals for the springs */
    private long nextTime;

    /** The current time of the cloth intervals for the springs */
    private long time;

    /** Time the last frame was called */
    private long lastFrameTime;

    /** The calculated light position every frame */
    private float[] lightPos;

    /** The angle the wind is currently coming from */
    private float angle;

    /** A radius value used for the pole */
    private float radius;

    /** Wind direction for this frame */
    private float[] windDirection;

    /** Wind location calculation temporary state */
    private float[] windPos;

    /** Array for passing attributes through to the spring callback handler */
    private float[] attribs;

    /** The current "gravity" direction for the flag */
    private float[] gravity;

    /** Perlin Noise generator for setting light and arrow positions */
    private PerlinNoiseGenerator perlinNoise;

    /** State that the flag rendering is currently in */
    private int flagState;

    /** A random number generator to use */
    private Random rgen;

    /** A matrix for settting transform values when needed */
    private Matrix4d tmpMatrix;

    /** A vertex for settting transform values when needed */
    private Vector3d tmpVertex;

    /** A temp array for setting uniform variable values  */
    private float[] tmpFloat;

ShaderObject[] objs;
ShaderProgram prog;

    /**
     *
     */
    public HumusFlagAnimator(Texture2D[] flags,
                             TransformGroup cloth,
                             TransformGroup light,
                             TransformGroup arrow,
                             TransformGroup pole)
    {
        arrowTransform = arrow;
        flagTextures = flags;
        lightGroup = light;
        clothGroup = cloth;

        tmpMatrix = new Matrix4d();
        tmpMatrix.setIdentity();

        tmpVertex = new Vector3d();
        tmpFloat = new float[4];

        // Pull apart the cloth structure for the bits we need
        Shape3D shape = (Shape3D)cloth.getChild(0);
        Appearance app = shape.getAppearance();
        GLSLangShader shader = (GLSLangShader)app.getShader();
        clothArgs = shader.getShaderArguments();
        clothGeometry = (IndexedTriangleStripArray)shape.getGeometry();

        TextureUnit[] tex_u = new TextureUnit[1];
        app.getTextureUnits(tex_u);
        clothTextureUnit = tex_u[0];

        clothVertices = new float[CLOTH_SIZE_Y * CLOTH_SIZE_X * 3];
        clothNormals = new float[CLOTH_SIZE_Y * CLOTH_SIZE_X * 3];
        clothTexCoords = new float[1][CLOTH_SIZE_Y * CLOTH_SIZE_X * 2];

        clothGeometry.getVertices(clothVertices);
        clothGeometry.getNormals(clothNormals);
        clothGeometry.getTextureCoordinates(clothTexCoords);

        attribs = new float[4];
        windDirection = new float[3];
        windPos = new float[3];
        gravity = new float[] { 0, -70, 0 };

        shape = (Shape3D)pole.getChild(0);
        app = shape.getAppearance();
        shader = (GLSLangShader)app.getShader();
        poleArgs = shader.getShaderArguments();

        prog = shader.getShaderProgram();
        prog.requestInfoLog();

        objs = new ShaderObject[2];
        prog.getShaderObjects(objs);
        objs[0].requestInfoLog();
        objs[1].requestInfoLog();


        lightPos = new float[3];

        // Initialise the constants, leave the variable bits to the updateSceneGraph
        arrowVertices = new float[21];
        arrowVertices[1] = C_SIZE * (CLOTH_SIZE_Y - 1) + 5;
        arrowVertices[2] = 0;
        arrowVertices[4] = C_SIZE * (CLOTH_SIZE_Y - 1) + 5;
        arrowVertices[5] = 30;
        arrowVertices[7] = C_SIZE * (CLOTH_SIZE_Y - 1) + 5;
        arrowVertices[8] = 10;
        arrowVertices[9] = 0;
        arrowVertices[10] = C_SIZE * (CLOTH_SIZE_Y - 1) + 5;
        arrowVertices[11] = 10;
        arrowVertices[12] = 0;
        arrowVertices[13] = C_SIZE * (CLOTH_SIZE_Y - 1) + 5;
        arrowVertices[14] = -10;
        arrowVertices[16] = C_SIZE * (CLOTH_SIZE_Y - 1) + 5;
        arrowVertices[17] = -10;
        arrowVertices[19] = C_SIZE * (CLOTH_SIZE_Y - 1) + 5;
        arrowVertices[20] = -30;

        shape = (Shape3D)arrow.getChild(0);
        app = shape.getAppearance();
        shader = (GLSLangShader)app.getShader();

        arrowArgs = shader.getShaderArguments();
        arrowGeometry = (TriangleFanArray)shape.getGeometry();


        springSystem = new SpringSystem();
        springSystem.setGravity(gravity);
        springSystem.setSpringConstant(350);
        springSystem.addRectField(CLOTH_SIZE_X,
                                  CLOTH_SIZE_Y,
                                  clothVertices,
                                  clothNormals);

        perlinNoise = new PerlinNoiseGenerator();
        rgen = new Random();
        nextTime = 0;
        time = 0;
        flagState = -1;

        int flag_idx = (int)(rgen.nextFloat() * MAX_RAND) % flagTextures.length;
        currentTexture = flagTextures[flag_idx];

        clothTextureUnit.setTexture(currentTexture);

        lastFrameTime = System.currentTimeMillis();

        for(int i = 0; i < CLOTH_SIZE_X * CLOTH_SIZE_Y; i += CLOTH_SIZE_X)
        {
            SpringNode n = springSystem.getNode(i);
            n.locked = true;
        }
    }

    //---------------------------------------------------------------
    // Methods defined by ApplicationUpdateObserver
    //---------------------------------------------------------------

    /**
     * Notification that now is a good time to update the scene graph.
     */
    public void updateSceneGraph()
    {
        long cur_time = System.currentTimeMillis();
        float frame_time = (cur_time - lastFrameTime) / 1000f;
        time += frame_time * 1000;
        lastFrameTime = cur_time;

        lightPos[0] = 3 * C_SIZE * CLOTH_SIZE_X * perlinNoise.noise1(0.5f * time / 1000f);
        lightPos[1] = 320 + 100 * perlinNoise.noise1(0.3d * time / 1000f + 23.37f);
        lightPos[2] = C_SIZE * CLOTH_SIZE_X * perlinNoise.noise1(12.31f - 0.5f * time / 1000f);

        if(time >= nextTime)
        {
            // Change the flag every 3 seconds.
            nextTime = time + T_DIFF * 1000;
            flagState++;

            switch(flagState)
            {
                case 0:
                case 1:
                case 2:
                case 3:
                    float off = 4 * (rgen.nextFloat() - 0.5f);
                    angle += off + (off >= 0 ? 1 : -1) * 0.5f;
                    radius = rgen.nextFloat() * 0.5f + 0.3d;


                    windDirection[0] = radius * (float)Math.cos(angle);
                    windDirection[1] = 0;
                    windDirection[2] = radius * (float)Math.sin(angle);

                    gravity[0] = 350 * windDirection[0];
                    gravity[1] = 350 * windDirection[1] - 70;
                    gravity[2] = 350 * windDirection[2];

                    springSystem.setGravity(gravity);

                    arrowVertices[0] = radius * 80 + 40;
                    arrowVertices[3] = radius * 80;
                    arrowVertices[6] = radius * 80;
                    arrowVertices[15] = radius * 80;
                    arrowVertices[18] = radius * 80;

                    arrowGeometry.boundsChanged(this);
                    arrowTransform.boundsChanged(this);
                    break;

                case 5:
                    // Select the flag texture to use
                    int flag_idx = (int)(rgen.nextFloat() * MAX_RAND) % flagTextures.length;
                    currentTexture = flagTextures[flag_idx];

                    clothTextureUnit.dataChanged(this);
                    break;

                case 6:
                    flagState = 0;
            }
        }

        // In state 5, lower the flag down the pole. In state 6, raise it back up again.
        if(flagState == 4)
        {
            float off = 750.0f * sCurve(sCurve(((T_DIFF - ((nextTime - time) / 1000f)) / T_DIFF)));

            for (int i = 0; i < CLOTH_SIZE_Y; i++)
            {
                SpringNode node = springSystem.getNode(i * CLOTH_SIZE_X);
                node.position[node.offset + 1] = C_SIZE * (CLOTH_SIZE_Y - 1 - i) - off;
            }
        }
        else if (flagState == 5)
        {
            float off = 750.0f * sCurve(sCurve((nextTime - time) / (1000f * T_DIFF)));

            for (int i = 0; i < CLOTH_SIZE_Y; i++)
            {
                SpringNode node = springSystem.getNode(i * CLOTH_SIZE_X);
                node.position[node.offset + 1] = C_SIZE * (CLOTH_SIZE_Y - 1 - i) - off;
            }
        }

        float d_time = Math.min(frame_time, 0.01f);
        windPos[0] -= 500 * d_time * windDirection[0];
        windPos[0] -= 500 * d_time * windDirection[0];
        windPos[0] -= 500 * d_time * windDirection[0];

        attribs[0] = d_time * radius;
        attribs[1] = windPos[0];
        attribs[2] = windPos[1];
        attribs[3] = windPos[2];

        springSystem.update(d_time, this, attribs);
        springSystem.evaluateNormals();

        clothArgs.dataChanged(this);
        arrowArgs.dataChanged(this);
        poleArgs.dataChanged(this);

        clothGeometry.boundsChanged(this);
        clothGeometry.dataChanged(this);

        lightGroup.boundsChanged(this);
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
        if(src == clothGeometry)
        {
            clothGeometry.setVertices(VertexGeometry.COORDINATE_3,
                                      clothVertices,
                                      CLOTH_SIZE_Y * CLOTH_SIZE_X);
        }
        else if(src == lightGroup)
        {
            tmpVertex.set(lightPos);
            tmpMatrix.setTranslation(tmpVertex);

            lightGroup.setTransform(tmpMatrix);
        }
        else if(src == arrowGeometry)
        {
            arrowGeometry.setVertices(VertexGeometry.COORDINATE_3,
                                      arrowVertices,
                                      7);
        }
        else if(src == arrowTransform)
        {
            tmpMatrix.setIdentity();
            tmpMatrix.rotY(angle * 180 / (float)Math.PI);
            arrowTransform.setTransform(tmpMatrix);
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
        if(src == clothGeometry)
        {
            clothGeometry.setNormals(clothNormals);
            clothGeometry.setTextureCoordinates(TEX_TYPES, clothTexCoords, 1);
        }
        else if(src instanceof ShaderArguments)
        {
            // Same code for all of them.
            ((ShaderArguments)src).setUniform("lightPos", 3, lightPos, 1);
        }
        else if(src == clothTextureUnit)
        {
            clothTextureUnit.setTexture(currentTexture);
        }
    }

    //----------------------------------------------------------
    // Methods defined by SpringEvaluatorCallback
    //----------------------------------------------------------

    /**
     * Process this node now.
     *
     * @param node The node instance to work with
     * @param attribs List of attributes to associate with the node
     */
    public void processSpringNode(SpringNode node, float[] attribs)
    {
        float x = attribs[1] + node.position[node.offset] * 0.00432f;
        float y = attribs[2] + node.position[node.offset + 1] * 0.00432f;
        float z = attribs[3] + node.position[node.offset + 2] * 0.00432f;

//System.out.println("checking spring node " + node + " pos " + x + " " + y + " " + z);

        node.dir[0] += 500 * attribs[0] * perlinNoise.noise3(x, y, z);
        node.dir[1] += 500 * attribs[0] * perlinNoise.noise3(x + 5.571f,
                                                             y - 9.842f,
                                                             z + 4.741f);
        node.dir[2] += 500 * attribs[0] * perlinNoise.noise3(x - 2.3142f,
                                                             y + 7.1423d,
                                                             z + 3.412f);
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * S-curve function for value distribution for Perlin-1 noise function.
     *
     * @param t A value to distribute
     * @return recalculated value for t
     */
    private float sCurve(float t)
    {
        return (t * t * (3 - 2 * t));
    }

}
