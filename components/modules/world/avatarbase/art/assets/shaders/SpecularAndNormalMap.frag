varying vec3 ToLight; // Normalized vector to the light
//varying vec3 ToCamera; // Normalized vector to the camera
varying vec3 Normal; // The Normal!

uniform sampler2D   DiffuseMapIndex;
uniform sampler2D   NormalMapIndex;
uniform sampler2D   SpecularMapIndex;

vec4 White = vec4(1.0,1.0,1.0, 1.0);

void main(void)
{
    vec4 texColor      = texture2D(DiffuseMapIndex, gl_TexCoord[0].st);
    //vec4 specular   = texture2D(specularMap, gl_TexCoord[0].st); //Used for spec mapping
    //vec4 normal     = texture2D(normalMap, gl_TexCoord[0].st);

    vec4 color = clamp(texColor * dot(Normal.xyz, ToLight.xyz), 0.0, 1.0);

    color = color * 0.75 + texColor * 0.25; // mix in 15% start color
    color.a = 1.0;
    gl_FragColor = color;
}