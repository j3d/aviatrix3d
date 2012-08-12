/** Demo of using the light pass in a deferred shading system. Fragment shader */

#version 110

uniform sampler2D colorMap;
uniform sampler2D depthMap;
uniform vec2 planes;
uniform float focalDistance;
uniform float dofNear;
uniform float dofFar;
uniform float blurAmount;

float saturate(float x)
{
    return max(0.0, min(1.0, x));
}

void main()
{
    float depth = texture2D(depthMap, gl_TexCoord[0].st).r;
    float dist = planes.x / (depth - planes.y);

    float f;
    if(dist < focalDistance)
    {
        f = (dist - focalDistance) / (focalDistance - dofNear);
    }
    else
    {
        f = (dist - focalDistance) / (dofFar - focalDistance);
    }

    float blur = clamp(abs(f), 0.0, blurAmount);
    gl_FragColor = vec4(blur, blur, blur, 1.0);

//  gl_FragColor = vec4(dist, dist, dist, 1.0);
}
