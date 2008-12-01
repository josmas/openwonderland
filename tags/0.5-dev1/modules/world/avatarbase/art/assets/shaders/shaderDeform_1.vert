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

varying vec3 Position;
varying float LightIntensity;
varying vec2  MCposition;
uniform float waveTime;
uniform float waveWidth;
uniform float waveHeight;
uniform mat4 joint1;
uniform mat4 joint2;
uniform mat4 joint3;
attribute vec3 weight;
uniform mat4 pose[];

varying vec3 LightDir;
varying vec3 EyeDir;
varying vec3 Normal;

const float Scale = 1.0;


void main(void)
{
vec3 LightPosition = normalize(vec3(gl_LightSource[0].position));

	mat3 rotMat1 = mat3(
joint1[0].xyz,
joint1[1].xyz,
joint1[2].xyz
);
	mat3 rotMat2 = mat3(
joint2[0].xyz,
joint2[1].xyz,
joint2[2].xyz
);
	mat3 rotMat3 = mat3(
joint3[0].xyz,
joint3[1].xyz,
joint3[2].xyz
);

vec4 pos = vec4((gl_Vertex * joint1) * weight.x + (gl_Vertex * joint2) * weight.y + (gl_Vertex * joint3) * weight.z);
vec3 normal1 = vec3((gl_Normal * rotMat1) * weight.x + (gl_Normal * rotMat2) * weight.y + (gl_Normal * rotMat3) * weight.z);

    vec3 ecPosition = vec3 (gl_ModelViewMatrix * pos);
    vec3 tnorm      = normalize(gl_NormalMatrix * normal1);
//normal1 = vec3(pos);
//    vec3 tnorm      = normalize(normal1);

    vec3 lightVec   = normalize(LightPosition - ecPosition);
    vec3 reflectVec = reflect(-lightVec, tnorm);
    vec3 viewVec    = normalize(-ecPosition);
    float diffuse   = max(dot(lightVec, tnorm), 0.0) + 0.2;
    float spec      = 0.0;

    if (diffuse > 0.0)
    {
        spec = max(dot(reflectVec, viewVec), 0.0);
        spec = pow(spec, 16.0);
    }

    LightIntensity  = max( (DiffuseContribution * diffuse +
                      SpecularContribution * spec), 0.4);

    MCposition      = gl_Vertex.xy;
//    gl_Position     = ftransform();
//    gl_Position = gl_ModelViewProjectionMatrix * v;
//    gl_Position = gl_ModelViewMatrix * gl_Vertex;
//    gl_Position = v;

gl_Position = gl_ModelViewProjectionMatrix * pos; 


	vec4 pos2 = gl_ModelViewMatrix * gl_Vertex;
    //gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    vec3 eyeDir = vec3(pos2);
//    gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_TexCoord[0] = gl_Vertex;
    gl_FrontColor = gl_Color;
	
    vec3 n = tnorm; //normalize(gl_NormalMatrix * gl_Normal);
    vec3 t = normalize(cross(vec3(1.141, 2.78, 3.14), n));
    vec3 b = cross(n, t);

    vec3 v;
    v.x = dot(LightPosition, t);
    v.y = dot(LightPosition, b);
    v.z = dot(LightPosition, n);
    LightDir = normalize(v);

    v.x = dot(eyeDir, t);
    v.y = dot(eyeDir, b);
    v.z = dot(eyeDir, n);
    EyeDir = normalize(v);

Position = vec3(gl_Vertex) * Scale;
//gl_TexCoord[0] = MCposition;
}
