// mostly derived from examples at: 
// http://www.gamedev.net/reference/programming/features/glsllib/default.asp

varying vec3 normal;
varying vec3 vertex;

uniform bool use_headlight;
uniform bool has_texture;
uniform sampler2D tex_unit;

const vec4 AMBIENT_BLACK = vec4(0.0, 0.0, 0.0, 1.0);
const vec4 DEFAULT_BLACK = vec4(0.0, 0.0, 0.0, 0.0);

bool isLightEnabled(in int i) {

    // A separate variable is used to get
    // rid of a linker error.
    bool enabled = true;
   
    // If all the colors of the Light are set
    // to BLACK then we know we don't need to bother
    // doing a lighting calculation on it.
    if ((gl_LightSource[i].ambient  == AMBIENT_BLACK) &&
        (gl_LightSource[i].diffuse  == DEFAULT_BLACK) &&
        (gl_LightSource[i].specular == DEFAULT_BLACK)) {
        enabled = false;
	}
       
    return(enabled);
}

float getAttenuation(in int i, in float dist) {
    return(1.0 / (gl_LightSource[i].constantAttenuation +
                  gl_LightSource[i].linearAttenuation * dist +
                  gl_LightSource[i].quadraticAttenuation * dist * dist));
}

void directionalLight(in int i, in vec3 N, in float shininess,
                      inout vec4 ambient, inout vec4 diffuse, inout vec4 specular) {

    vec3 L = normalize(gl_LightSource[i].position.xyz);
   
    float nDotL = dot(N, L);
   
    if (nDotL > 0.0) {   
        vec3 H = gl_LightSource[i].halfVector.xyz;
       
        float pf = pow(max(dot(N,H), 0.0), shininess);

        diffuse  += gl_LightSource[i].diffuse  * nDotL;
        specular += gl_LightSource[i].specular * pf;
    }
   
    ambient  += gl_LightSource[i].ambient;
}

void pointLight(in int i, in vec3 N, in vec3 V, in float shininess,
                inout vec4 ambient, inout vec4 diffuse, inout vec4 specular) {

    vec3 D = gl_LightSource[i].position.xyz - V;
    vec3 L = normalize(D);

    float dist = length(D);
    float attenuation = getAttenuation(i, dist);

    float nDotL = dot(N,L);

    if (nDotL > 0.0) {   
        vec3 E = normalize(-V);
        vec3 R = reflect(-L, N);
       
        float pf = pow(max(dot(R,E), 0.0), shininess);

        diffuse  += gl_LightSource[i].diffuse  * attenuation * nDotL;
        specular += gl_LightSource[i].specular * attenuation * pf;
    }
   
    ambient  += gl_LightSource[i].ambient * attenuation;
}

void spotLight(in int i, in vec3 N, in vec3 V, in float shininess,
               inout vec4 ambient, inout vec4 diffuse, inout vec4 specular) {

    vec3 D = gl_LightSource[i].position.xyz - V;
    vec3 L = normalize(D);

    float dist = length(D);
    float attenuation = getAttenuation(i, dist);

    float nDotL = dot(N,L);

    if (nDotL > 0.0) {   

        float spotEffect = dot(normalize(gl_LightSource[i].spotDirection), -L);
       
        if (spotEffect > gl_LightSource[i].spotCosCutoff) {

            attenuation *=  pow(spotEffect, gl_LightSource[i].spotExponent);

            vec3 E = normalize(-V);
            vec3 R = reflect(-L, N);
       
            float pf = pow(max(dot(R,E), 0.0), shininess);

            diffuse  += gl_LightSource[i].diffuse  * attenuation * nDotL;
            specular += gl_LightSource[i].specular * attenuation * pf;
        }
    }
   
    ambient  += gl_LightSource[i].ambient * attenuation;
}

void calculateLighting(in bool headlight, in int numLights, in vec3 N, in vec3 V, in float shininess,
                       inout vec4 ambient, inout vec4 diffuse, inout vec4 specular) {

    // loop through each light, determine it's type and sum up the contributions
	int i = 0;
	if (!headlight) {
		// note: aviatrix reserves light 0 for the headlight,
		// so start our loop from light 1
		i = 1;
	}
	for (; i < numLights; i++) {

		if (gl_LightSource[i].position.w == 0.0) {

			directionalLight(i, N, shininess, ambient, diffuse, specular);

		} else if (gl_LightSource[i].spotCutoff == 180.0) {

			pointLight(i, N, V, shininess, ambient, diffuse, specular);

		} else {

			spotLight(i, N, V, shininess, ambient, diffuse, specular);
		}
    }
}

void main() {  

	vec4 color;

	if (gl_FrontMaterial.diffuse.a == 0.0) {
		// totally transparent, don't bother...
		color = vec4(0.0);
		discard;

	} else {
		vec4 ambient  = vec4(0.0);
	    vec4 diffuse  = vec4(0.0);
	    vec4 specular = vec4(0.0);
	
	    calculateLighting(use_headlight, gl_MaxLights, normal, vertex, gl_FrontMaterial.shininess,
	                      ambient, diffuse, specular);
	   
	    color = gl_FrontLightModelProduct.sceneColor  +
	            (ambient  * gl_FrontMaterial.ambient) +
	            (diffuse  * gl_FrontMaterial.diffuse) +
	            (specular * gl_FrontMaterial.specular);

		//////////////////////////////////////////////////////////////////////////
		// skip two sided lighting for now...
		/*
	    // Re-initialize for a back pass
	    ambient  = vec4(0.0);
	    diffuse  = vec4(0.0);
	    specular = vec4(0.0);
	          
	    // calculate the back contribution.
	    calculateLighting(num_lights, -normal, vertex, gl_BackMaterial.shininess,
	                      ambient, diffuse, specular);
	
		color += gl_BackLightModelProduct.sceneColor  +
	             (ambient  * gl_BackMaterial.ambient) +
	             (diffuse  * gl_BackMaterial.diffuse) +
	             (specular * gl_BackMaterial.specular);
		*/
		//////////////////////////////////////////////////////////////////////////

	    color = clamp(color, 0.0, 1.0);
		
		if (has_texture) {
			vec4 texture = texture2D(tex_unit, gl_TexCoord[0].st);
			color *= texture;
		}
		color.a = gl_FrontMaterial.diffuse.a;
	}
	gl_FragColor = color;
}

