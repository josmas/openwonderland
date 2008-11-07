//
// Vertex shader for procedural bricks
//
// Authors: Dave Baldwin, Steve Koren, Randi Rost
//          based on a shader by Darwyn Peachey
//
// Copyright (c) 2002-2004 3Dlabs Inc. Ltd. 
//
// See 3Dlabs-License.txt for license information
//

//uniform vec3 LightPosition;

const float SpecularContribution = 0.5;
const float DiffuseContribution  = 1.0 - SpecularContribution;

varying float LightIntensity;
varying vec2  MCposition;
//attribute vec3 weight;
attribute vec3 boneIndices;
uniform mat4 pose[55];
varying vec3 vertex_color;
varying vec3 Position;
//varying vec2 vTexCoord; // show the texture

void main(void)
{
    // DAHLGREN - Added the line below to support a simple frag shader
    gl_TexCoord[0] = gl_MultiTexCoord0; // show the texture
    vec3 weight = vec4(gl_Color).rgb;
    vec3 LightPosition = normalize(gl_LightSource[0].position.xyz);
    vec4 normal4 = vec4(gl_Normal, 0.0);
    vec4 pos = vec4((gl_Vertex * pose[int(boneIndices.x)] ) * weight.x + 
                (gl_Vertex * pose[int(boneIndices.y)]) * weight.y + 
                (gl_Vertex * pose[int(boneIndices.z)]) * weight.z);
    vec3 normal1 = vec3((normal4 * pose[int(boneIndices.x)] ) * weight.x + 
                (normal4 * pose[int(boneIndices.y)]) * weight.y + 
                (normal4 * pose[int(boneIndices.z)]) * weight.z);

    gl_Position = gl_ModelViewProjectionMatrix * pos;

    vec3 ecPosition = vec3 (gl_Vertex.xyz);
    vec3 tnorm      = normalize( gl_NormalMatrix * normal1);


    vec3 lightVec   = normalize(gl_LightSource[0].position.xyz - ecPosition);
    float diffuse   = max(dot(lightVec, tnorm), 0.3);

    vec3 cameraPosition = vec3(gl_ModelViewMatrixInverse * vec4(0,0,0,1.0));
    vec3 cameraVec = normalize( cameraPosition - gl_Vertex.xyz);
    vec3 halfVector = normalize( lightVec + cameraVec);

    float nxHalf = max( 0.0, dot ( tnorm, halfVector ));
    float spec      = 0.0;

    if (diffuse > 0.0)
    {
        //spec = pow(nxHalf, 0.5);
    }

    LightIntensity  = max((DiffuseContribution * diffuse +
                      SpecularContribution * spec), 0.2);

    MCposition      = gl_Vertex.xy;
    Position = gl_Vertex.xyz;

    vertex_color = abs(gl_Normal) * LightIntensity ;
    //vertex_color = vec3( LightIntensity * 0.8, LightIntensity, LightIntensity );
}