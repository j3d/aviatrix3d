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

// Amount to grow the base object size by to avoid aliasing artifacts
uniform float grow;

varying float distance;

void main()
{
  vec4 pos = gl_Vertex;
  pos.xyz += gl_Normal * grow;  // scale vertex along normal
  gl_Position = gl_ModelViewProjectionMatrix * pos;
  distance = length(gl_ModelViewMatrix * gl_Vertex);
  gl_TexCoord[0].x = distance * 0.1;
}
