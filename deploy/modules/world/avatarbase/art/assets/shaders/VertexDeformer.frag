varying vec3 ToLight; // Normalized vector to the light
varying vec3 Normal; // The Normal!

uniform sampler2D   diffuseMap;

vec4 White = vec4(1.0,1.0,1.0, 1.0);

void main(void)
{
    vec4 texColor      = texture2D(diffuseMap, gl_TexCoord[0].st);

    vec4 color = clamp(texColor * dot(Normal.xyz, ToLight.xyz), 0.0, 1.0);

    color = color * 0.80 + texColor * 0.20; // mix in 20% start color

    gl_FragColor = color;
}