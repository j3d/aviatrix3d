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

/** The depth texture generated in a previous render pass */
uniform sampler2D viewerDepthTexture;

/** The depth scale amount from the texture */
uniform float depthScale;

vec4 makeMeAColor() 
{
    float d = texture2D(viewerDepthTexture, gl_TexCoord[0].st).r;

    return vec4(d, 0, 0, 0.5);
}

void main()
{
    gl_FragColor = makeMeAColor();
}
