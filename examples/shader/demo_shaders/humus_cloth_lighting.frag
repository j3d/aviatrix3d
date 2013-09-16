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

	float diffuse = saturate(dot(lVec, norm));
	float specular = pow(saturate(dot(reflect(-vVec, norm), lVec)), 16.0);

	gl_FragColor = (0.8 * diffuse + 0.15) * base + 0.4 * specular;
}
