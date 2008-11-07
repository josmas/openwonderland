// Standard TNL

varying float LightIntensity;

void main(void)
{
    gl_TexCoord[0] = gl_MultiTexCoord0; // show the texture
    gl_Position    = ftransform();

    vec3 lightVec = (gl_ModelViewMatrixInverse * gl_LightSource[0].position).xyz - gl_Vertex.xyz;
    lightVec = normalize(lightVec);

    LightIntensity = max(dot(lightVec, gl_Normal), 0.0);
}