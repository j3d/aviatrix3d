#define saturate(x) clamp(x,0.0,1.0)

varying vec3 normal;
varying vec3 lightVec;
varying vec3 viewVec;

uniform vec4 color;

void main(){
	vec3 norm = normalize(normal);
	vec3 lVec = normalize(lightVec);
	vec3 vVec = normalize(viewVec);

	float diffuse = saturate(dot(lVec, norm));
	float specular = pow(saturate(dot(reflect(-vVec, norm), lVec)), 16.0);

	gl_FragColor = (diffuse + 0.15) * color + 0.7 * specular;
}
