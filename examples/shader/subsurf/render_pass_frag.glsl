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
uniform sampler2D lightDepthTexture;

/** The depth texture generated in a previous render pass */
uniform sampler2D viewerDepthTexture;

/** Colour of the incoming light */
uniform vec4 lightColor;

/** Base colour of the object */
uniform vec4 objectBaseColor;

/** Specular light colour for highlights */
uniform vec4 objectSpecularColor;

/** Specular power coefficient */
uniform float specularCoefficient;

/** Will show as X Y and Z ports in QC, but actually represent RGB values. */
uniform vec3 extinctionCoefficient; 

/** Multiplier for something or other */
uniform float rimScalar;

/** light space projection */
uniform mat4 lightPosMatrix;

/* light to texture space projection */
uniform mat4 lightProjMatrix;

/** Falloff factor for the lighting */
uniform float sigma;

/** The depth scale amount from the texture */
uniform float depthScale;

/** Number of pixels in the depth texture size */
uniform vec2 viewDepthTextureSize;

/** 
 * The maximum depth range at which the object is no longer transparent. If the
 * view depth is greater than this amount ([0,1] range) then the alpha will be
 * clamped to 1.0
 */
uniform float maxTransparentDepth;

/** The real world position of the point being projected */
varying vec4 worldSpacePosition;

/** Normal of this vertex in world space */
varying vec3 worldNormal;

varying vec4 eyeVec;

varying vec4 lightVec; 

/** Projected vertex position in screen space */
varying vec4 projectedVertex; 

/**
 * Find the depth of object that the light has to pass through
 */     
float traceLightDepth()
{
    // transform point into projected light texture space
    vec4 tex_space_ccord = lightProjMatrix * vec4(worldSpacePosition.xyz, 1.0);

    // get distance from light at entry point
    float d_i = length(texture2DProj(lightDepthTexture, tex_space_ccord.xyw)) * depthScale;

    // transform position to light space
    vec4 l_point = lightPosMatrix * vec4(worldSpacePosition.xyz, 1.0);

    // distance of this pixel from light (exit)
    float d_o = length(l_point);

    // calculate depth
    return (d_o - d_i);
}

/**
 * Find the depth of object that the camera view has to pass through to see
 * the background
 */     
float traceViewDepth()
{
    // get distance from light at entry point
    vec2 tex_coord = vec2(gl_FragCoord.x / viewDepthTextureSize.x,
                          gl_FragCoord.y / viewDepthTextureSize.y);

    float d_i = texture2D(viewerDepthTexture, tex_coord).r;

    return (d_i - gl_FragCoord.z) * 100.0;
}


/**
 * Half lambert term calculation for shading phong lighting
 */
float halfLambert(vec3 v1, vec3 v2)
{
    return dot(v1,v2) * 0.5 + 0.5;
}
   
/**
 * Calculate the specular component using phong shading
 */
float blinnPhongSpecular(vec3 normalVec, vec3 lightVec)
{
    vec3 halfAngle = normalize(normalVec + lightVec);
    return pow(clamp(0.0,1.0,dot(normalVec,halfAngle)), specularCoefficient);
}


vec4 makeMeAColor() 
{
    float light_material_thickness = traceLightDepth();
    float view_material_thickness = traceViewDepth();

    vec4 light_position =  lightPosMatrix[3];

    float light_attenuation = 10.0 * (1.0 / distance(light_position, projectedVertex));

    vec3 eVec = normalize(eyeVec).yxz;
    vec3 lVec = normalize(lightVec).yxz;
    vec3 wNorm = normalize(worldNormal).yxz;

    vec4 dotLN = vec4(halfLambert(lVec, wNorm) * light_attenuation);
    dotLN = max(vec4(0.0), dotLN);
    dotLN *= objectBaseColor;

    vec3 indirect_light = vec3(light_material_thickness * max(0.0,dot(-wNorm,lVec)));
    indirect_light += light_material_thickness * halfLambert(-eVec,lVec);
    indirect_light *= light_attenuation;
    indirect_light.r *= extinctionCoefficient.r;
    indirect_light.g *= extinctionCoefficient.g;
    indirect_light.b *= extinctionCoefficient.b;

    vec3 rim = vec3(1.0 - max(0.0,dot(wNorm,eVec)));
    rim *= rim;
    rim *= max(0.0,dot(wNorm, lVec)) * objectSpecularColor.rgb;

    vec4 final_color;
    
    if(dotLN == 0.0) 
        final_color = vec4(0, 0, 0, 1);
    else 
    {
        final_color = dotLN + vec4(indirect_light, 1.0);
        final_color.rgb += (rim * rimScalar * light_attenuation * final_color.a);
        final_color.rgb += vec3(blinnPhongSpecular(wNorm, lVec) * light_attenuation * objectSpecularColor * final_color.a * 0.05);
    }

    final_color += exp(-light_material_thickness * sigma) * lightColor;
    final_color.a = view_material_thickness * 1000.0;

//    float d = min(maxTransparentDepth, view_material_thickness);

//    final_color = vec4(1, 1, 1, view_material_thickness);
//    final_color.a = min(maxTransparentDepth, view_material_thickness) * 10.0;

    return final_color;
}

void main()
{
    gl_FragColor = makeMeAColor();
//    float si = traceLightDepth();
//    gl_FragColor = exp(-si * sigma) * lightColor;
}
