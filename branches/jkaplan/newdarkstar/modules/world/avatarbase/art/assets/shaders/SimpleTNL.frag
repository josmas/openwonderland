varying float LightIntensity;
uniform sampler2D baseMap;

void main(void)
{
    vec4 color = texture2D(baseMap, gl_TexCoord[0].st);
    color = clamp(color * LightIntensity, 0.0, 1.0); 

    gl_FragColor = color;
}