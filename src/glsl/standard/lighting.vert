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

/*
 * Vertex shader for replicating the standard light sources of directional, 
 * point and spot. Separate functions are provided for each that can be called
 * directly. Alternatively, if you want to make use of all lights and have them
 * directly output to gl_FrontColor etc, call the processLighting() function.
 *
 * Notes:
 * vertexPosition must be provided in eye-coordinate space. 
 * normal The normal of the vertex
 * ambientColor The initial ambient colour that the light modifies
 * diffuseColor The initial diffuse colour that the light modifies
 * specularColor The initial specular colour that the light modifies
 */

void processNoLighting(in bool useSecondary) 
{
    if(useSecondary)
        gl_FrontSecondaryColor = gl_SecondaryColor;

    gl_FrontColor = gl_Color;
}


void processDirectionalLight(in int lightIndex,
                             in vec3 normal,
                             inout vec4 ambientColor,
                             inout vec4 diffuseColor,
                             inout vec4 specularColor)
{
    float nDotVP = max(0.0, dot(normal, vec3(gl_LightSource[lightIndex].position)));
    float nDotHP = max(0.0, dot(normal, vec3(gl_LightSource[lightIndex].halfVector)));
    float power;

    if(nDotVP == 0.0)
        power = 0.0;
    else
        power = pow(nDotHP, gl_FrontMaterial.shininess);

    ambientColor += gl_LightSource[lightIndex].ambient;
    diffuseColor += gl_LightSource[lightIndex].diffuse * nDotVP;
    specularColor += gl_LightSource[lightIndex].specular * power;
}

void processPointLight(in int lightIndex,
                       in vec3 eyePosition,
                       in vec3 vertexPosition,
                       in vec3 normal,
                       inout vec4 ambientColor,
                       inout vec4 diffuseColor,
                       inout vec4 specularColor)
{
    // Vector from surface to light position.
    vec3 vp = vec3(gl_LightSource[lightIndex].position) - vertexPosition;
    float dist = length(vp);
    vp = normalize(vp);

    // Distance attenuation factor
    float attn = 1.0 / (gl_LightSource[lightIndex].constantAttenuation +
                        gl_LightSource[lightIndex].linearAttenuation * dist +
                        gl_LightSource[lightIndex].quadraticAttenuation * dist * dist);


    float nDotVP = max(0.0, dot(normal, vp));
    float nDotHV = max(0.0, dot(normal, normalize(vp + eyePosition)));

    float power;

    if(nDotVP == 0.0)
        power = 0.0;
    else
        power = pow(nDotHV, gl_FrontMaterial.shininess);

    ambientColor += gl_LightSource[lightIndex].ambient;
    diffuseColor += gl_LightSource[lightIndex].diffuse * nDotVP * attn;
    specularColor += gl_LightSource[lightIndex].specular * power * attn;
}


void processSpotLight(in int lightIndex,
                       in vec3 eyePosition,
                       in vec3 vertexPosition,
                       in vec3 normal,
                       inout vec4 ambientColor,
                       inout vec4 diffuseColor,
                       inout vec4 specularColor)
{

    vec3 vp = vec3(gl_LightSource[lightIndex].position) - vertexPosition;
    float dist = length(vp);
    vp = normalize(vp);

    // Distance attenuation factor
    float attn = 1.0 / (gl_LightSource[lightIndex].constantAttenuation +
                        gl_LightSource[lightIndex].linearAttenuation * dist +
                        gl_LightSource[lightIndex].quadraticAttenuation * dist * dist);

    // If we're inside the cutoff angle for the dropoff range, turn don't 
    // apply any attenuation factor from the spotlight. 
    float spotAngle = dot(-vp, gl_LightSource[lightIndex].spotDirection);

    if(spotAngle >= gl_LightSource[lightIndex].spotCosCutoff)
        attn *= pow(spotAngle, gl_LightSource[lightIndex].spotExponent);
    else
        attn = 0.0;

    float nDotVP = max(0.0, dot(normal, vp));
    float nDotHV = max(0.0, dot(normal, normalize(vp + eyePosition)));

    float power = 0.0;

    if(nDotVP == 0.0)
        power = 0.0;
    else
        power = pow(nDotHV, gl_FrontMaterial.shininess);

    ambientColor += gl_LightSource[lightIndex].ambient;
    diffuseColor += gl_LightSource[lightIndex].diffuse * nDotVP * attn;
    specularColor += gl_LightSource[lightIndex].specular * power * attn;
}

void processLights(in vec3 vertexPosition,
                   in vec3 normal,
                   in bool enabledLights[],
                   in bool useLocalViewer,
                   inout vec4 ambientColor,
                   inout vec4 diffuseColor,
                   inout vec4 specularColor)
{
    vec3 eye;

    if(useLocalViewer)
        eye = -normalize(vertexPosition);
    else
        eye = vec3(0.0, 0.0, 1.0);

    for(int i = 0; i < 8; i++)
    {
        if(enabledLights[i])
        {
/*
            if(gl_LightSource[i].position.w == 0.0)
                processDirectionalLight(i,
                                        normal, 
                                        ambientColor, 
                                        diffuseColor, 
                                        specularColor);
            else if(gl_LightSource[i].spotCutoff == 180.0)
                 processPointLight(i, 
                                   eye, 
                                   vertexPosition, 
                                   normal,
                                   ambientColor, 
                                   diffuseColor, 
                                   specularColor);
            else
*/
                 processSpotLight(i, 
                                  eye, 
                                  vertexPosition, 
                                  normal,
                                  ambientColor, 
                                  diffuseColor, 
                                  specularColor);
        }
    }

}

void processLighting(in vec3 vertexPosition,
                     in vec3 normal,
                     in bool enabledLights[],
                     in bool useLocalViewer,
                     in bool useTwoSided,
                     in bool useSeparateSpecular)
{
    vec4 ambient = vec4(gl_FrontMaterial.ambient);
    vec4 diffuse = vec4(gl_FrontMaterial.diffuse);
    vec4 specular = vec4(gl_FrontMaterial.specular);

    processLights(vertexPosition, 
                  normal, 
                  enabledLights,
                  useLocalViewer, 
                  ambient, 
                  diffuse, 
                  specular);

    if(useSeparateSpecular)
    {
        gl_FrontSecondaryColor = specular;
        gl_FrontColor = gl_FrontMaterial.emission + 
                        ambient * gl_LightModel.ambient;
    }
    else
        gl_FrontColor = specular + 
                    gl_FrontMaterial.emission + 
                    ambient * gl_LightModel.ambient;

/*
    if(useTwoSided)
    {
        ambient = vec4(gl_BackMaterial.ambient);
        diffuse = vec4(gl_BackMaterial.diffuse);
        specular = vec4(gl_BackMaterial.specular);
        normal = -normal;

        processLights(vertexPosition, 
                      normal,
              enabledLights,
                      useLocalViewer, 
                      ambient, 
                      diffuse, 
                      specular);

        if(useSeparateSpecular)
        {
            gl_BackSecondaryColor = specular;
            gl_BackColor = gl_BackMaterial.emission + 
                            ambient * gl_LightModel.ambient;
        }
        else
            gl_BackColor = specular + 
                        gl_BackMaterial.emission + 
                        ambient * gl_LightModel.ambient;
    }
*/
}
