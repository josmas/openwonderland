//  File: shaderDeform_3.vert
//  LastModified: Friday, August Fifteenth, Two-Thousand and Eight
//  Comments: This shader generates data for using a normal and specular map... but not yet. Just deformation and texture lookup currently

attribute vec4 boneIndices;

uniform mat4 pose[55];

varying vec3 ToLight; // Normalized vector to the light
//varying vec3 ToCamera; // Normalized vector to the camera - used for specular
varying vec3 Normal;

void main(void)
{

    gl_TexCoord[0] = gl_MultiTexCoord0; // Grab texture input (TEXTURE UNIT 0)

    vec3 weight = vec4(gl_Color).rgb; // Grab the vertex weight from the vert color
	// Calculate the fourth weight
    float weight4 = 1.0 - ( weight.x + weight.y + weight.z);

    // OMG math hax - This is a more optimized way of blending the matrices together. It uses fewer multiplies
    mat4 poseBlend = (      (pose[int(boneIndices.x)]) * weight.x + 
                            (pose[int(boneIndices.y)]) * weight.y + 
                            (pose[int(boneIndices.z)]) * weight.z +
                            (pose[int(boneIndices.w)]) * weight4     );
	// Determine our vert position under this pose
    vec4 pos = gl_Vertex * poseBlend;
	
	// Perform a manual matrix multiply of the upper 3x3 in the transform matrix
    Normal.x = dot (gl_Normal, poseBlend[0].xyz);
    Normal.y = dot (gl_Normal, poseBlend[1].xyz);
    Normal.z = dot (gl_Normal, poseBlend[1].xyz);
   
    //Normal = gl_Normal.xyz;
    // Transform the position 
    gl_Position = gl_ModelViewProjectionMatrix * pos;
    vec3 v = gl_ModelViewMatrix * pos;

    // the untransformed position of this vertex
    //vec3 ecPosition = vec3 (gl_Vertex.xyz);

    // Calculate the vector from us to the light
    ToLight   = vec3(0,1,0);
    //ToLight   = vec3(gl_ModelViewMatrixInverse * vec4(v,1.0));

    //vec3 cameraPosition = vec3(gl_ModelViewMatrixInverse * vec4(0,0,0,1.0));

    // Vector to the camera
    //ToCamera = normalize( cameraPosition - gl_Vertex.xyz);
}