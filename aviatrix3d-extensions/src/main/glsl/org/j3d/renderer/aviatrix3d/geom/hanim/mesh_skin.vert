/*****************************************************************************
 *                       Yumetech Copyright (c) 2004-2005
 *                               GLSL Source
 *
 * This source is licensed under the modified BSD license.
 * Please read http://www.opensource.org/licenses/bsd-license.php for more 
 * information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

// This is the number of matrices in the skeleton. It is changed dynamically by
// the Java code to reflect the real humanoid, before compilation.
attribute vec4 weight;
attribute vec4 matrixIndex;

// skeleton matrices
uniform mat4 boneMatrix[50];

// Number of textures to apply to the object
uniform int numTextures;

// Number of texture coordinates to generate
uniform bool numTexCoords;  

// Should fog values be calculated
uniform int fogSourceType;

// Number of lights to calculate the final colour from
uniform bool lightingEnabled;

// Flag per light indicating if it's enabled or not
uniform bool enabledLights[8];

// Forward decls for the other shaders in use
void processNoLighting(in bool);
void processLighting(in vec3,
                     in vec3,
                     in bool[],
                     in bool,
                     in bool,
                     in bool);

void processFogDepth(in vec3, in bool);

void main()
{      
    ivec4 idx = ivec4(matrixIndex);

    vec4 position = weight.x * boneMatrix[idx.x] * gl_Vertex +
                    weight.y * boneMatrix[idx.y] * gl_Vertex +
                    weight.z * boneMatrix[idx.z] * gl_Vertex +
                    weight.w * boneMatrix[idx.w] * gl_Vertex;

    gl_Position = gl_ModelViewProjectionMatrix * position;

    vec4 norm = vec4(gl_Normal, 0.0);
    vec4 normal = weight.x * boneMatrix[idx.x] * norm +
                  weight.y * boneMatrix[idx.y] * norm +
                  weight.z * boneMatrix[idx.z] * norm +
                  weight.w * boneMatrix[idx.w] * norm;

    vec3 final_normal = normalize(gl_NormalMatrix * normal.xyz);

    if(!lightingEnabled) 
        processNoLighting(true);
    else
        processLighting(gl_Position.xyz, 
                        final_normal, 
                        enabledLights,
                        true,
                        false,
                        true);

    // Process fog coord handling, if needed
    if(fogSourceType == 1)
        processFogDepth(gl_Position.xyz, true);
    else if(fogSourceType == 2)
        processFogDepth(gl_Position.xyz, false);


    if(numTextures > 0)
        gl_TexCoord[0] = gl_TextureMatrix[0] * gl_MultiTexCoord0;

    if(numTextures > 1)
        gl_TexCoord[1] = gl_TextureMatrix[1] * gl_MultiTexCoord1;

    if(numTextures > 2)
        gl_TexCoord[2] = gl_TextureMatrix[2] * gl_MultiTexCoord2;

    if(numTextures > 3)
        gl_TexCoord[3] = gl_TextureMatrix[3] * gl_MultiTexCoord3;

    if(numTextures > 4)
        gl_TexCoord[4] = gl_TextureMatrix[4] * gl_MultiTexCoord4;

    if(numTextures > 5)
        gl_TexCoord[5] = gl_TextureMatrix[5] * gl_MultiTexCoord5;

    if(numTextures > 6)
        gl_TexCoord[6] = gl_TextureMatrix[6] * gl_MultiTexCoord6;

    if(numTextures > 7)
        gl_TexCoord[7] = gl_TextureMatrix[7] * gl_MultiTexCoord7;

}

