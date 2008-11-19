//  File: shaderDeform_3.vert
//  LastModified: Friday, August Fifteenth, Two-Thousand and Eight
//  Comments: This shader generates data for using a normal and specular map

attribute vec4 boneIndices;
attribute vec3 tangent;

uniform mat4 pose[55];

varying vec3 ToLight; // Normalized vector to the light
varying vec3 ToCamera; // Normalized direction to the viewer
varying vec3 position;



void main(void)
{
    	gl_TexCoord[0] = gl_MultiTexCoord0; // Grab texture input (diffuse)
    	gl_TexCoord[1] = gl_MultiTexCoord1; // Grab texture input (normal)
    	gl_TexCoord[2] = gl_MultiTexCoord2; // Grab texture input (specular)

    	vec3 weight = vec4(gl_Color).rgb; // Grab the vertex weight from the vert color

    	// Calculate the fourth weight
    	float weight4 = 1.0 - ( weight.x + weight.y + weight.z);

    	// OMG math hax - This is a more optimized way of blending the matrices together. It uses fewer multiplies
    	mat4 poseBlend = (  (pose[int(boneIndices.x)]) * weight.x + 
                            (pose[int(boneIndices.y)]) * weight.y + 
                            (pose[int(boneIndices.z)]) * weight.z +
                            (pose[int(boneIndices.w)]) * weight4     );
	
    	// Determine our vert position under this pose
    	vec4 pos = gl_Vertex * poseBlend;

    	position = gl_Vertex.xyz;
  
    	// Transform gl_Position into world space
    	gl_Position = gl_ModelViewProjectionMatrix * pos;

	// calculate ToCamera vector
	ToCamera = (gl_ModelViewProjectionMatrix * vec4(0,0,1,1)).xyz;

	// Transform normal
	vec3 Normal;
	Normal.x = dot (gl_Normal, poseBlend[0].xyz);
    	Normal.y = dot (gl_Normal, poseBlend[1].xyz);
    	Normal.z = dot (gl_Normal, poseBlend[2].xyz);

	vec3 TangentVec;
	TangentVec.x = dot (tangent, poseBlend[0].xyz);
    	TangentVec.y = dot (tangent, poseBlend[1].xyz);
    	TangentVec.z = dot (tangent, poseBlend[2].xyz);

 	vec3 binormal = normalize(cross(TangentVec, Normal));
 
	mat3 TBNMatrix = mat3(TangentVec, binormal, Normal); 
  	ToLight = (gl_ModelViewMatrixInverse * gl_LightSource[0].position).xyz - position;
  	ToLight *= TBNMatrix;  
	// Transform into texture space
	ToCamera *= TBNMatrix;

}