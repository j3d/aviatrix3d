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

import org.j3d.geom.spring.SpringNode;
import org.j3d.geom.spring.SpringSystem;
import org.j3d.texture.procedural.PerlinNoiseGenerator;

public class HumusClothAnimator
    implements ApplicationUpdateObserver, NodeUpdateListener
{
    /** Maximum random number value */
    private static final float MAX_RAND = 0x7FFF;

    /** The number of vertices for the cloth in the X direction */
    private static final int CLOTH_SIZE_X = 54;

    /** The number of vertices for the cloth in the T direction */
    private static final int CLOTH_SIZE_Y = 42;

    /** Value of the real size of the cloth */
    private static final float C_SIZE  = 320.0f / CLOTH_SIZE_X;

    /** Index values for the corners of the cloth for the ITSA */
    private static int cornerInds[] =
    {
        0,
        CLOTH_SIZE_X - 1,
        CLOTH_SIZE_X * CLOTH_SIZE_Y - 1,
        (CLOTH_SIZE_Y - 1) * CLOTH_SIZE_X
    };

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
    private float[] vertices;

    /** Array to recalculate the cloth geometry normal list */
    private float[] normals;

    /** Array to recalculate the cloth geometry texture coordinate list*/
    private float[][] texCoords;

    /** Arguments for the cloth shader */
    private ShaderArguments clothArgs;

    /** The texture unit for the flag so we can change the texture */
    private TextureUnit clothTextureUnit;

    /** Arguments for the sphere shaders */
    private ShaderArguments[] sphereArgs;

    /** Transforms holding each sphere */
    private TransformGroup[] sphereTransforms;

    /** Shape objects containing each sphere */
    private Appearance[] sphereAppearances;

    /** Flags indicating the visibility state of each sphere */
    private boolean[] sphereVisibility;

    /** Lists of sphere positions to match each transform */
    private float[][] spherePos;

    /** Lists of sphere radius values to match each transform */
    private float[] sphereSize;

    /** Lists of sphere colour to match each transform */
    private float[][] sphereColor;


    /** Arguments for the sphere shaders */
    private ShaderArguments[] cornerArgs;

    /** Transforms holding spheres on each corner of the cloth */
    private TransformGroup[] cornerTransforms;

    /** Shape objects containing each sphere */
    private Appearance[] cornerAppearances;

    /** Flags indicating the visibility state of each sphere */
    private boolean[] cornerVisibility;

    /** The evaluation time of the cloth intervals for the springs */
    private long nextTime;

    /** The current time of the cloth intervals for the springs */
    private long time;

    /** Time the last frame was called */
    private long lastFrameTime;

    /** The calculated light position every frame */
    private float[] lightPos;

    /** Perlin Noise generator for setting light and sphere positions */
    private PerlinNoiseGenerator perlinNoise;

    /** A random number generator to use */
    private Random rgen;

    /** A matrix for settting transform values when needed */
    private Matrix4d tmpMatrix;

    /** A vertex for settting transform values when needed */
    private Vector3d tmpVertex;

    /** A temp array for setting uniform variable values  */
    private float[] tmpFloat;

    /** The number of valid spheres that are currently shown */
    private int numValidSpheres;

    /**
     *
     */
    public HumusClothAnimator(Texture2D[] flags,
                              TransformGroup cloth,
                              TransformGroup light,
                              TransformGroup[] spheres,
                              TransformGroup[] corners)
    {
        sphereTransforms = spheres;
        cornerTransforms = corners;
        flagTextures = flags;
        lightGroup = light;
        clothGroup = cloth;

        tmpMatrix = new Matrix4d();
        tmpMatrix.setIdentity();

        tmpVertex = new Vector3d();
        tmpFloat = new float[4];

        // Pull apart the cloth structure for the bits we need
        Shape3D c_shape = (Shape3D)cloth.getChild(0);
        Appearance c_app = c_shape.getAppearance();
        GLSLangShader shader = (GLSLangShader)c_app.getShader();
        clothArgs = shader.getShaderArguments();
        clothGeometry = (IndexedTriangleStripArray)c_shape.getGeometry();

        TextureUnit[] tex_u = new TextureUnit[1];
        c_app.getTextureUnits(tex_u);
        clothTextureUnit = tex_u[0];

        // Generate the vertex setup for the cloth.
        vertices = new float[CLOTH_SIZE_Y * CLOTH_SIZE_X * 3];
        normals = new float[CLOTH_SIZE_Y * CLOTH_SIZE_X * 3];
        texCoords = new float[1][CLOTH_SIZE_Y * CLOTH_SIZE_X * 2];

        int num_index = (CLOTH_SIZE_Y - 1) * CLOTH_SIZE_X * 2;
        int[] c_indicies = new int[num_index];
        int[] strip_len = new int[CLOTH_SIZE_Y - 1];

        int idx = 0;
        for(int i = 0; i < CLOTH_SIZE_Y - 1; i++)
        {
            int i_offset = i * CLOTH_SIZE_X * 2;
            int x_offset = i * CLOTH_SIZE_X;

            for(int j = 0; j < CLOTH_SIZE_X; j++)
            {
                c_indicies[idx++] = x_offset + CLOTH_SIZE_X + j;
                c_indicies[idx++] = x_offset + j;
            }
        }

        for(int i = 0; i < CLOTH_SIZE_Y - 1; i++)
            strip_len[i] = CLOTH_SIZE_X * 2;

        clothGeometry.setVertices(VertexGeometry.COORDINATE_3,
                                  vertices,
                                  CLOTH_SIZE_Y * CLOTH_SIZE_X);
        clothGeometry.setIndices(c_indicies, num_index);
        clothGeometry.setStripCount(strip_len, CLOTH_SIZE_Y - 1);
        clothGeometry.setNormals(normals);
        clothGeometry.setTextureCoordinates(TEX_TYPES, texCoords, 1);

        // Rip apart the others
        // Start with the corner spheres
        cornerArgs = new ShaderArguments[4];
        cornerVisibility = new boolean[4];
        cornerAppearances = new Appearance[4];

        for(int i = 0; i < 4; i++)
        {
            Shape3D shape = (Shape3D)corners[i].getChild(0);
            Appearance app = shape.getAppearance();
            GLSLangShader shad = (GLSLangShader)app.getShader();
            ShaderArguments args = shad.getShaderArguments();

            cornerAppearances[i] = app;
            cornerArgs[i] = args;
            cornerVisibility[i] = false;
        }

        int max_spheres = spheres.length;

        lightPos = new float[3];
        spherePos = new float[max_spheres][3];
        sphereSize = new float[max_spheres];
        sphereColor = new float[max_spheres][3];

        sphereArgs = new ShaderArguments[max_spheres];
        sphereVisibility = new boolean[max_spheres];
        sphereAppearances = new Appearance[max_spheres];

        for(int i = 0; i < max_spheres; i++)
        {
            Shape3D shape = (Shape3D)spheres[i].getChild(0);
            Appearance app = shape.getAppearance();
            GLSLangShader shad = (GLSLangShader)app.getShader();
            ShaderArguments args = shad.getShaderArguments();

            sphereAppearances[i] = app;
            sphereArgs[i] = args;
            sphereVisibility[i] = false;
        }

        springSystem = new SpringSystem();
        springSystem.setGravity(new float[] { 0, -70, 0 });
        springSystem.setSpringConstant(350);
        springSystem.addRectField(CLOTH_SIZE_X,
                                  CLOTH_SIZE_Y,
                                  vertices,
                                  normals);

        perlinNoise = new PerlinNoiseGenerator();
        rgen = new Random();
        nextTime = 0;
        time = 0;

        lastFrameTime = System.currentTimeMillis();
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

        lightPos[0] = C_SIZE * CLOTH_SIZE_X * perlinNoise.noise1(0.5f * time / 1000f);
        lightPos[1] = 80 + 80 * perlinNoise.noise1(0.3f * time / 1000f + 23.37f);
        lightPos[2] = C_SIZE * CLOTH_SIZE_X * perlinNoise.noise1(12.31f - 0.5f * time / 1000f);

        if(time >= nextTime || ((nextTime - time < 4500) && sum < 5) || sum > 9800)
        {
            // Change the flag every 10 seconds.
            nextTime = time + 10000;

            resetClothPoints();
            springSystem.resetNaturalLengths();

            // choose how many spheres we want. Between 0 and 3.
            numValidSpheres = (int)(rgen.nextFloat() * MAX_RAND) & 3;

            for(int i = 0; i < numValidSpheres; i++)
            {
                if(!sphereVisibility[i])
                {
                    sphereVisibility[i] = true;
                    sphereAppearances[i].dataChanged(this);
                }

                sphereSize[i] = 60 + 40 * rgen.nextFloat();
                spherePos[i][0] = 0.8f * C_SIZE * CLOTH_SIZE_X * (rgen.nextFloat() - 0.5f);
                spherePos[i][1] = -sphereSize[i] - 50 * rgen.nextFloat();
                spherePos[i][2] = 0.8f * C_SIZE * CLOTH_SIZE_Y * (rgen.nextFloat() - 0.5f);

                sphereColor[i][0] = rgen.nextFloat();
                sphereColor[i][1] = rgen.nextFloat();
                sphereColor[i][2] = rgen.nextFloat();
            }

            // Turn off any remaining spheres.
            for(int i = numValidSpheres; i < sphereTransforms.length; i++)
            {
                if(sphereVisibility[i])
                {
                    sphereVisibility[i] = false;
                    sphereAppearances[i].dataChanged(this);
                }
            }

            // Unlock the four current corner points
            for(int i = 0; i < 4; i++)
            {
                SpringNode node = springSystem.getNode(cornerInds[i]);
                node.locked = false;
            }

            // If there are no spheres, or on some other random guess (about
            // 50% weighted), lock some of the four corners of the cloth so
            // that they hang.

            if(numValidSpheres == 0 || (rgen.nextFloat() * MAX_RAND > MAX_RAND / 2))
            {
                int pos = (int)(rgen.nextFloat() * MAX_RAND) & 3;
                int n = 2 + ((int)(rgen.nextFloat() * MAX_RAND) % 3);

                for(int i = 0; i < n; i++)
                {
                    SpringNode node = springSystem.getNode(cornerInds[(pos + i) & 3]);
                    node.locked = true;
                }
            }

            // Select the flag texture to use
            int flag_idx = (int)(rgen.nextFloat() * MAX_RAND) % flagTextures.length;
            currentTexture = flagTextures[flag_idx];

            clothTextureUnit.dataChanged(this);
        }

        springSystem.update(Math.min(frame_time, 0.0125f));

        sum = 0;
        int num_nodes = springSystem.getNodeCount();
        boolean include;

        for(int i = 0; i < num_nodes; i++)
        {
            SpringNode node = springSystem.getNode(i);

            include = true;

            for(int j = 0; j < numValidSpheres; j++)
            {
                float v_x = node.position[node.offset] - spherePos[j][0];
                float v_y = node.position[node.offset + 1] - spherePos[j][1];
                float v_z = node.position[node.offset + 2] - spherePos[j][2];

                float d_squared = v_x * v_x + v_y * v_y + v_z * v_z;
                float s_squared = sphereSize[j] * sphereSize[j];

                // If the cloth position has intersected the sphere, that looks
                // wrong. We want the cloth to drape over the sphere, so
                // readjust the location of the node's position to be on the
                // circumference of the sphere.
                if(d_squared < s_squared)
                {
                    // Normalise the vector
                    d_squared = 1 / (float)Math.sqrt(d_squared);
                    v_x *= d_squared;
                    v_y *= d_squared;
                    v_z *= d_squared;

                    node.position[node.offset] = spherePos[j][0] +
                                                 sphereSize[j] * v_x;
                    node.position[node.offset + 1] = spherePos[j][1] +
                                                     sphereSize[j] * v_y;
                    node.position[node.offset + 2] = spherePos[j][2] +
                                                      sphereSize[j] * v_z;

                    float time_mult = (float)Math.pow(0.015f, frame_time);
                    node.dir[0] *= time_mult;
                    node.dir[1] *= time_mult;
                    node.dir[2] *= time_mult;

                    include = false;
                }
            }

            if(include)
            {
                float d = node.dir[0] * node.dir[0] +
                          node.dir[1] * node.dir[1] +
                          node.dir[2] * node.dir[2];
                sum += d;
            }
        }

        sum /= (CLOTH_SIZE_X * CLOTH_SIZE_Y);

        springSystem.evaluateNormals();

        for(int i = 0; i < numValidSpheres; i++)
            sphereArgs[i].dataChanged(this);

        for(int i = 0; i < 4; i++)
        {
            SpringNode node = springSystem.getNode(cornerInds[i]);

            if(node.locked)
            {
                if(!cornerVisibility[i])
                {
                    cornerVisibility[i] = true;
                    cornerAppearances[i].dataChanged(this);
                    cornerArgs[i].dataChanged(this);
                }
            }
            else
            {
                if(cornerVisibility[i])
                {
                    cornerVisibility[i] = false;
                    cornerAppearances[i].dataChanged(this);
                }
            }
        }

        clothArgs.dataChanged(this);
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
    // Methods required by the UpdateListener interface.
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
                                      vertices,
                                      CLOTH_SIZE_Y * CLOTH_SIZE_X);
        }
        else if(src == lightGroup)
        {
            tmpVertex.set(lightPos[0], lightPos[1], lightPos[2]);
            tmpMatrix.setTranslation(tmpVertex);

            lightGroup.setTransform(tmpMatrix);
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
            clothGeometry.setNormals(normals);
            clothGeometry.setTextureCoordinates(TEX_TYPES, texCoords, 1);
        }
        if(src == clothArgs)
        {
            clothArgs.setUniform("lightPos", 3, lightPos, 1);
        }
        else if(src instanceof Appearance)
        {
            // but which appearance?
            for(int i = 0; i < 4; i++)
            {
                if(src == cornerAppearances[i])
                {
                    cornerAppearances[i].setVisible(cornerVisibility[i]);
                    return;
                }
            }

            // So it wasn't one of the corners, must be a sphere
            for(int i = 0; i < sphereAppearances.length; i++)
            {
                if(src == sphereAppearances[i])
                {
                    sphereAppearances[i].setVisible(sphereVisibility[i]);
                    return;
                }
            }
        }
        else if(src instanceof ShaderArguments)
        {
            // which arguments?
            for(int i = 0; i < 4; i++)
            {
                if(src == cornerArgs[i])
                {
                    SpringNode node = springSystem.getNode(cornerInds[i]);

                    tmpFloat[0] = node.position[node.offset];
                    tmpFloat[1] = node.position[node.offset + 1];
                    tmpFloat[2] = node.position[node.offset + 2];

                    cornerArgs[i].setUniform("spherePos", 3, tmpFloat, 1);
                    cornerArgs[i].setUniform("lightPos", 3, lightPos, 1);
                    return;
                }
            }

            // So it wasn't one of the corners, must be a sphere
            for(int i = 0; i < sphereArgs.length; i++)
            {
                if(src == sphereArgs[i])
                {
                    sphereArgs[i].setUniform("lightPos", 3, lightPos, 1);
                    sphereArgs[i].setUniform("spherePos", 3, spherePos[i], 1);

                    sphereArgs[i].setUniform("color", 3, sphereColor[i], 1);

                    tmpFloat[0] = sphereSize[i] - 1;
                    sphereArgs[i].setUniform("sphereSize", 1, tmpFloat, 1);
                    return;
                }
            }
        }
        else if(src == clothTextureUnit)
        {
            clothTextureUnit.setTexture(currentTexture);
        }
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Reset the vertex list back to the initial values from before the springs
     * get hold of them.
     */
    private void resetClothPoints()
    {
        int vtx_pos = 0;
        int tex_pos = 0;

        for(int i = 0; i < CLOTH_SIZE_Y; i++)
        {
            for(int j = 0; j < CLOTH_SIZE_X; j++)
            {
                vertices[vtx_pos++] = C_SIZE * (j - 0.5f * (CLOTH_SIZE_X - 1));
                vertices[vtx_pos++] = 0;
                vertices[vtx_pos++] = C_SIZE * (0.5f * (CLOTH_SIZE_Y - 1) - i);

                texCoords[0][tex_pos++] = (float)j / (CLOTH_SIZE_X - 1);
                texCoords[0][tex_pos++] = (float)i / (CLOTH_SIZE_Y - 1);
            }
        }
    }
}
