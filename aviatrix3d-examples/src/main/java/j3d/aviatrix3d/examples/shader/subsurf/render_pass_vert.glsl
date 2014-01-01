/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2008
 *                               GLSL Shader Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

/** light space projection */
uniform mat4 lightPosMatrix;

/** The real world position of the point being projected */
varying vec4 worldSpacePosition;

/** Normal of this vertex in world space */
varying vec3 worldNormal;

varying vec4 eyeVec;

varying vec4 lightVec; 

/** Projected vertex position in screen space */
varying vec4 projectedVertex; 


void main()
{
    worldSpacePosition = gl_ModelViewMatrix * gl_Vertex;

    worldNormal = gl_NormalMatrix * gl_Normal;  

    projectedVertex = gl_ModelViewProjectionMatrix * gl_Vertex;

    vec4 light_position = lightPosMatrix[3];
    lightVec = light_position - projectedVertex;  
    eyeVec = -projectedVertex;  

    gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_TexCoord[1] = gl_TextureMatrix[0] * worldSpacePosition;

    gl_Position = ftransform();
}
