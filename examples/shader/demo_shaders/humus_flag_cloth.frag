#define saturate(x) clamp(x,0.0,1.0)
#define lerp mix

uniform sampler2D Base;

varying vec2 texCoord;
varying vec3 normal;
varying vec3 lightVec;
varying vec3 viewVec;

void main(){
	vec4 base = texture2D(Base, texCoord);

	vec3 norm = normalize(normal);
	vec3 lVec = normalize(lightVec);
	vec3 vVec = normalize(viewVec);

	float diffuse = abs(dot(lVec, norm));
	//float specular = pow(abs(dot(reflect(-vVec, norm), lVec)), 16.0);
	
	float cosView = dot(vVec, normal);
	float shine = pow(1.0 - cosView * cosView, 5.0);


	gl_FragColor = saturate(0.7 * diffuse + 0.15 + 0.6 * shine) * base;
}
