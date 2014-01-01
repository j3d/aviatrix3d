// mostly derived from examples at: 
// http://www.gamedev.net/reference/programming/features/glsllib/default.asp

varying vec3 normal;
varying vec3 vertex;

void main() {

	vertex = vec3(gl_ModelViewMatrix * gl_Vertex);       
	normal = normalize(gl_NormalMatrix * gl_Normal);

	gl_TexCoord[0] = gl_MultiTexCoord0;

	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}