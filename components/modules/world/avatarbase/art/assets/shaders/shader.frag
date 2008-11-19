//
// Fragment shader for procedural bricks
//
// Authors: Dave Baldwin, Steve Koren, Randi Rost
//          based on a shader by Darwyn Peachey
//
// Copyright (c) 2002-2004 3Dlabs Inc. Ltd. 
//
// See 3Dlabs-License.txt for license information
//
uniform sampler2D baseMap;
uniform sampler2D normalMap;
uniform sampler2D specularMap;

varying vec3 viewDirection;
varying vec3 LightDir;
varying vec3 EyeDir;


uniform vec3  BrickColor, MortarColor;
uniform vec2  BrickSize;
uniform vec2  BrickPct;

varying vec2  MCposition;
varying float LightIntensity;

void main(void)
{
    vec3  color;
    vec2  position, useBrick;
    
    position = MCposition / BrickSize;

    if (fract(position.y * 0.5) > 0.5)
        position.x += 0.5;

    position = fract(position);

    useBrick = step(position, BrickPct);

    color  = mix(MortarColor, BrickColor, useBrick.x * useBrick.y);
//    color *= LightIntensity;

vec2 uv = MCposition * .10;
//    gl_FragColor = vec4 (color, 1.0);
//gl_FragColor = texture2D(tex, uv);

	/* Extract colors from baseMap and specularMap */
	vec4  baseColor      = texture2D( baseMap, uv );
vec3 color3 = vec3(baseColor.xyz);

        float lit = max(dot(-LightDir, color3), 0.0);
lit *= lit;
        float lit2 = max(dot(-EyeDir, color3), 0.0);
	vec4  specularColor  = texture2D( normalMap, uv );
	
	gl_FragColor =  lit * ( specularColor * (vec4 (color, 1.0))) + lit2 * lit2 *lit2 * specularColor;//*  baseColor ) ;
}