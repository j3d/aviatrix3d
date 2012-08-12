varying vec3 viewpos;
varying vec3 normal;
varying vec4 colour; 

void main()
{
    normal = gl_NormalMatrix * gl_Normal;
    colour = gl_FrontMaterial.diffuse;
    viewpos = -vec3(gl_ModelViewMatrix*gl_Vertex);
    gl_Position = ftransform();   
} 
