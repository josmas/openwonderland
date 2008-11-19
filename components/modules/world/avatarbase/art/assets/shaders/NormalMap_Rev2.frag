varying vec3 ToLight; // Normalized vector to the light
varying vec3 ToCamera; // Normalized direction to the viewer
varying vec3 position;

uniform sampler2D   diffuseMap;
uniform sampler2D   normalMap;

void main(void)
{
    	// Multi tex coords are screwed up for now.... default to index 0
    	vec4 texColor      = texture2D(diffuseMap, gl_TexCoord[0].st);
    	vec4 specColor     = gl_FrontMaterial.specular;
    	vec4 normalMapValue  = texture2D(normalMap, gl_TexCoord[0].st);


    	vec3 normal = normalize(texture2D(normalMap, gl_TexCoord[0].st).xyz * 2.0 - 1.0);
 
	vec3 lightVector = normalize(ToLight); 

	vec3 camVector = normalize(ToCamera);

	vec3 reflection = normalize(reflect(lightVector * -1.0, normal));
         
  	float nxDir = max(0.0, dot(normal, lightVector));
  
  	vec4 diffuse = texColor * (gl_LightSource[0].diffuse * nxDir);

	vec4 specular = (gl_LightSource[0].specular * pow(max(dot(reflection, camVector),0.0), 8.0));
	specular *= ceil(nxDir);
	specular *=  specColor;

	// if nxDir <= 0, specular is 0
	//specular *= ceil(nxDir);

	vec4 color = diffuse * 0.85 + texColor * 0.15 + specular;

    	//gl_FragColor = vec4(camVector, 1.0);
	//gl_FragColor = specColor;
    	gl_FragColor = color;
	//gl_FragColor = diffuse;
}