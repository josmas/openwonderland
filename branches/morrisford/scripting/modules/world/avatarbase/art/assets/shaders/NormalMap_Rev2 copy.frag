varying vec3 ToLight; // Normalized vector to the light
varying vec3 position;

uniform sampler2D   diffuseMap;
uniform sampler2D   specularMap;
uniform sampler2D   normalMap;

void main(void)
{
    	// Multi tex coords are screwed up for now.... default to index 0
    	vec4 texColor      = texture2D(diffuseMap, gl_TexCoord[0].st);
    	vec4 specColor     = texture2D(specularMap, gl_TexCoord[0].st);
    	vec4 normalMapValue  = texture2D(normalMap, gl_TexCoord[0].st);


    	vec3 normal = normalize(texture2D(normalMap, gl_TexCoord[0].st).xyz * 2.0 - 1.0);
 
	vec3 lightVector = normalize(ToLight); 
         
  	float nxDir = max(0.0, dot(normal, lightVector));  
  	vec4 diffuse = gl_LightSource[0].diffuse * nxDir;

    	vec4 color = texColor * diffuse;

    	color = color * 0.85 + texColor * 0.15; // mix in 15% start color

    	//gl_FragColor = vec4(lightVector ,1.0);
    	gl_FragColor = color;
	//gl_FragColor = diffuse;
}