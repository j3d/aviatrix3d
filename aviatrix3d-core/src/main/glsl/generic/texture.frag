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
 * Fragment shader for replicating the texture application.
 *
 * 
 * Notes:
 */
void textureReplace2D(inout vec4 color, in sampler2D texture, in int texUnit)
{
    color = texture2D(texture, gl_TexCoord[texUnit].xy);
}

void textureModulate2D(inout vec4 color, in sampler2D texture, in int texUnit)
{
    color *= texture2D(texture, gl_TexCoord[texUnit].xy);
}

void textureDecal2D(inout vec4 color, in sampler2D texture, in int texUnit)
{
    vec4 tex_color = texture2D(texture, gl_TexCoord[texUnit].xy);
    vec3 rgb_col = mix(color.rgb, tex_color.rgb, tex_color.a);

    color = vec4(rgb_col, color.a);
}


void textureBlend2D(inout vec4 color, in sampler2D texture, in int texUnit)
{
    vec4 tex_color = texture2D(texture, gl_TexCoord[texUnit].xy);
    vec3 rgb_col = mix(color.rgb, gl_TextureEnvColor[texUnit].rgb, tex_color.rgb);

    color = vec4(rgb_col, color.a * tex_color.a);
}


void textureAdd2D(inout vec4 color, in sampler2D texture, in int texUnit)
{
    vec4 tex_color = texture2D(texture, gl_TexCoord[texUnit].xy);
    color.rgb += tex_color.rgb;
    color.a *= tex_color.a;

    color = clamp(color, 0.0, 1.0);
}

void textureSub2D(inout vec4 color, in sampler2D texture, in int texUnit)
{
    vec4 tex_color = texture2D(texture, gl_TexCoord[texUnit].xy);
    color.rgb -= tex_color.rgb;
    color.a *= tex_color.a;

    color = clamp(color, 0.0, 1.0);
}
