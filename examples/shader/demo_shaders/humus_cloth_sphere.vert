uniform vec3 lightPos;
uniform vec3 camPos;
uniform vec3 spherePos;
uniform float sphereSize;

varying vec3 normal;
varying vec3 lightVec;
varying vec3 viewVec;

void main(){
	vec3 pos = gl_Vertex.xyz * sphereSize + spherePos;

	gl_Position = gl_ModelViewProjectionMatrix * vec4(pos, 1.0);

	normal = gl_Vertex.xyz;
	lightVec = lightPos - pos;
	viewVec = camPos - pos;
}
