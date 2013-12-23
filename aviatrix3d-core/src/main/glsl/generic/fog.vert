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
 * Vertex shader for replicating the standard fog equations.
 *
 * Notes:
 * vertexPosition must be provided in eye-coordinate space. 
 */

void fogFragCoord() 
{
    gl_FogFragCoord = gl_FogCoord;
}

void fogStandard(in vec3 vertexPosition) 
{
    gl_FogFragCoord = abs(vertexPosition.z);
}

void processFogDepth(in vec3 vertexPosition, in bool useFogCoord)
{
    if(useFogCoord)
        fogFragCoord();
    else 
        fogStandard(vertexPosition);
}
