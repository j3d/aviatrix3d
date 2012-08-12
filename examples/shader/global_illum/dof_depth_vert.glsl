/** Demo of using gbuffer rendering pass. Vertex shader */

#version 110

void main(){
    // Vertex position in object space
    gl_Position = ftransform();

    // Texture coordinates are just copied
    gl_TexCoord[0] = gl_MultiTexCoord0;
}
