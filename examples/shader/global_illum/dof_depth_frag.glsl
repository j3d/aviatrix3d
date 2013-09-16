/** Demo of using gbuffer rendering pass. Fragment shader */

#version 110

uniform sampler2D colorMap;

void main(){
    // look up the colour map
    vec4 colour = texture2D(colorMap, gl_TexCoord[0].xy);

    // Finally fill out the buffer passes
//    gl_FragData[0] = vec4((colour * gl_FrontMaterial.diffuse).rgb, 1.0);
    gl_FragColor = vec4((colour * gl_FrontMaterial.diffuse).rgb, 1.0);
//    gl_FragColor = colour;
}
