uniform vec3 lightPos;
uniform vec3 camPos;

varying vec2 texCoord;
varying vec3 normal;
varying vec3 lightVec;
varying vec3 viewVec;

void main(){
	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;

	texCoord = gl_MultiTexCoord0.xy;
	normal = gl_Normal;
	lightVec = lightPos - gl_Vertex.xyz;
	viewVec = camPos - gl_Vertex.xyz;
}
