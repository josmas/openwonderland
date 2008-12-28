//  File: shaderDeform_3.vert
//  LastModified: Friday, August Fifteenth, Two-Thousand and Eight
//  Comments: This shader generates data for using a normal and specular map

attribute vec3 tangent;


varying vec3 ToLight; // Normalized vector to the light
varying vec3 ToCamera; // Normalized direction to the viewer
varying vec3 position;



void main(void)
{
    	gl_TexCoord[0] = gl_MultiTexCoord0; // Grab texture input (diffuse)
    	gl_TexCoord[1] = gl_MultiTexCoord1; // Grab texture input (normal)
    	gl_TexCoord[2] = gl_MultiTexCoord2; // Grab texture input (specular)

 
	
    	// Determine our vert position under this pose
    	position = gl_Vertex.xyz;
  
    	// Transform gl_Position into world space
    	gl_Position = ftransform();

	// calculate ToCamera vector
	ToCamera = gl_NormalMatrix * vec3(0,0,1);


 	vec3 binormal = normalize(cross(tangent, gl_Normal));
 
	mat3 TBNMatrix = mat3(tangent, binormal, gl_Normal); 

  	ToLight = (gl_ModelViewMatrixInverse * gl_LightSource[0].position).xyz - position;

  	ToLight *= TBNMatrix;  

	// Transform into texture space
	ToCamera *= TBNMatrix;

}