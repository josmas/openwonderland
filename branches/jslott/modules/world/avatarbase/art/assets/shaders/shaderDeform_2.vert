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

const float SpecularContribution = 0.3;
const float DiffuseContribution  = 1.0 - SpecularContribution;

varying float LightIntensity;
varying vec2  MCposition;
attribute vec3 weight;
attribute vec3 boneIndices;
uniform mat4 pose[28];

void main(void)
{
    vec3 LightPosition = normalize(vec3(gl_LightSource[0].position));

    vec4 normal4 = vec4(gl_Normal, 0.0);
    vec4 pos = vec4((gl_Vertex * pose[int(boneIndices.x)] ) * weight.x + 
                (gl_Vertex * pose[int(boneIndices.y)]) * weight.y + 
                (gl_Vertex * pose[int(boneIndices.z)]) * weight.z);
    vec3 normal1 = vec3((normal4 * pose[int(boneIndices.x)] ) * weight.x + 
                (normal4 * pose[int(boneIndices.y)]) * weight.y + 
                (normal4 * pose[int(boneIndices.z)]) * weight.z);

    vec3 ecPosition = vec3 (gl_ModelViewMatrix * pos);
    vec3 tnorm      = normalize(gl_NormalMatrix * normal1);


    vec3 lightVec   = normalize(LightPosition - ecPosition);
    vec3 reflectVec = reflect(-lightVec, tnorm);
    vec3 viewVec    = normalize(-ecPosition);
    float diffuse   = max(dot(lightVec, tnorm), 0.0);
    float spec      = 0.0;

    if (diffuse > 0.0)
    {
        spec = max(dot(reflectVec, viewVec), 0.0);
        spec = pow(spec, 16.0);
    }

    LightIntensity  = DiffuseContribution * diffuse +
                      SpecularContribution * spec;

    MCposition      = gl_Vertex.xy;

    gl_Position = gl_ModelViewProjectionMatrix * pos; 
}