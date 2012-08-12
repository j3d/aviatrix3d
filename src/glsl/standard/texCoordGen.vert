/*****************************************************************************
 *                        Yumetech Copyright (c) 2004-2005
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

/*
 * Vertex shader for replicating the standard texture coordinate generation 
 * routines.
 *
 * Each routine only caters to having all dimensions of the texture coordinates
 * generated for a single vertex. It is not possible to mix and match 
 * generation routines 
 * 
 * Notes:
 * vertexPosition must be provided in eye-coordinate space. 
 * normal The normal of the vertex
 */

void processTexCoordGen(in vec3 vertexPosition, 
                        in vec3 normal,
			in int textureUnit
                        in bool useFogCoord)
{
}


vec2 texGenSphereMap(in vec3 vertexPosition, in vec3 normal)
{
    vec3 u = normalize(vertexPosition);
    vec3 r = reflect(u, normal);
    float m = 2.0 * sqrt(r.x * r.x + r.y * r.y + r.z * r.z);
    return vec2(r.x / m + 0.5, r.y / m + 0.5);
}

vec3 texGenReflectionMap(in vec3 vertexPosition, in vec3 normal)
{
    vec3 u = normalize(vertexPosition);
    return reflect(u, normal);
}

vec4 texGenNormalMap(int vec3 normal)
{
    return vec4(normal, 1.0);
}

vec4 texGenObjectLinear(in int texUnit)
{
    return vec4(dot(gl_Vertex, glObjectPlaneS[i]),
		dot(gl_Vertex, glObjectPlaneT[i]),
		dot(gl_Vertex, glObjectPlaneR[i]),
		dot(gl_Vertex, glObjectPlaneQ[i]));
}

vec4 texGenEyeLinear(in int texUnit)
{
    return vec4(dot(gl_Vertex, glEyePlaneS[i]),
		dot(gl_Vertex, glEyePlaneT[i]),
		dot(gl_Vertex, glEyePlaneR[i]),
		dot(gl_Vertex, glEyePlaneQ[i]));
}

