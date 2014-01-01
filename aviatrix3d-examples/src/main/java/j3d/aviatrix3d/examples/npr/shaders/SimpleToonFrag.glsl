uniform int selectedLight;
uniform float outlineWidth;
uniform vec4 outlineColour;

varying vec3 viewpos;
varying vec3 normal;
varying vec4 colour; 

void main()
{
    float gradient;
    vec3 n = normalize(normal);
    vec3 l = normalize(gl_LightSource[selectedLight].position.xyz);

    float intensity = dot(l, n);
    vec4 out_colour;

    if(dot(n, viewpos) < outlineWidth) 
    {
        out_colour = outlineColour;
    }
    else 
    {
        if (intensity > 0.95)
	    gradient = 1.0;
        else if (intensity > 0.5)
	    gradient = 0.5;
        else if (intensity > 0.25)
	    gradient = 0.25;
        else
    	gradient = 0.1;

        out_colour = colour * vec4(gradient, gradient, gradient, 1);
    }

    gl_FragColor = out_colour;
} 
