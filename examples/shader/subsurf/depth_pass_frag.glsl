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

// The scale factor to bring depth in to the [0,1] range
uniform float depthScaleFactor;

varying float distance;

// Given a point in object space, lookup into depth textures
// returns depth
void main()
{

/*
    float d = distance * 0.1;
    if(d > 1.0)
       gl_FragColor = vec4(1, 0, 0, 0);
    else if(d > 0.5)
       gl_FragColor = vec4(0, 1, 0, 0);
    else if(d > 0.1)
       gl_FragColor = vec4(1, 1, 0, 0);
    else
       gl_FragColor = vec4(1, 0, 1, 0);
*/     
    gl_FragColor = vec4(distance * depthScaleFactor, 0, 0, 0);

    gl_FragDepth = distance  * depthScaleFactor;
}
