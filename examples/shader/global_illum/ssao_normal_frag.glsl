/** Demo of using gbuffer rendering pass. Fragment shader */

#version 110

// stuff for ssao in eye space coords
varying vec3 esNormal;
varying float esDepth; 

void main(){
    gl_FragColor = vec4(normalize(esNormal), esDepth);
    gl_FragDepth = esDepth;
}
