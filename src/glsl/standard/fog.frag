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
 * Fragment shader for replicating the standard fog equations.
 *
 * All the equations return a fog scaling value that can be used to mix the 
 * fog colour with the object's colour for this pixel. The fog component will 
 * already be clamped for [0-1] range. For example:
 * 
 * output_colour = mix (vec3(gl_Fog.color), input_colour, fogLinear());
 * 
 * Notes:
 */
const float LOG2E = 1.442695;   // 1 / log(2)

float fogLinear();
float fogExponential();
float fogExponential2();

float processFogColor(in bool useLinear, in bool useExponential)
{
    if(useLinear)
        return fogLinear();
    else if(useExponential)
        return fogExponential();
    else
        return fogExponential2();
}

float fogLinear() 
{
    float val = (gl_Fog.end - gl_FogFragCoord) * gl_Fog.scale;
    return clamp(val, 0.0, 1.0);
}

float fogExponential() 
{
    float val = exp2(-gl_Fog.density * gl_FogFragCoord * LOG2E);
    return clamp(val, 0.0, 1.0);
}

float fogExponential2() 
{
    float val = exp2(-gl_Fog.density * gl_Fog.density * LOG2E *
                      gl_FogFragCoord * gl_FogFragCoord);
    return clamp(val, 0.0, 1.0);
}
