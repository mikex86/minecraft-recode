#version 120

varying vec3 lightingColor;
varying vec4 fragColorAttr;
varying vec2 fragTextureCoords;

uniform sampler2D boundTexture;

/* Texture mapping uniforms */
uniform bool texture_mapping_bool;

uniform int texture_pix_u0;
uniform int texture_pix_v0;
uniform int texture_pix_u1;
uniform int texture_pix_v1;

/* Color control */
uniform bool override_color;
uniform vec4 forced_color;

///* A color vector added to the output color before the fog specified by the java code */
//uniform highp vec4 additionColor;

/*
    Fog variables
*/
uniform bool enableFog;
varying float fogVisibility;

uniform vec4 fogColor;

/* Java code controlled color multiplier */
uniform vec4 shaderColor;

void main() {
    vec4 color;

    if (override_color) {
        color = forced_color * shaderColor;
        if (enableFog) {
           color = mix(color, fogColor, fogVisibility);
        }
        gl_FragColor = color;
        return;
    }

    vec4 texColor;

    //Setting the interpolated value of lightingColor passed from the vertexShader to the frag color with an alpha value of 1.

    if(!texture_mapping_bool) {
        texColor = texture2D(boundTexture, fragTextureCoords);
    } else {
        float texture_u0 = float(texture_pix_u0);
        float texture_v0 = float(texture_pix_v0);
        float texture_u1 = float(texture_pix_u1);
        float texture_v1 = float(texture_pix_v1);

        float dif_u = texture_u1 - texture_u0;
        float dif_v = texture_v1 - texture_v0;

        float uo = fragTextureCoords.x * dif_u;
        float vo = fragTextureCoords.y * dif_v;

        vec2 calculatedCoords = vec2(texture_u0 + uo, texture_v0 + vo);

        texColor = texture2D(boundTexture, calculatedCoords);
    }

    color = (texColor * fragColorAttr * shaderColor * vec4(lightingColor, 1));

    if (color.a < 0.1F) {
        discard;
    }

    if (enableFog) {
        color = mix(color, fogColor, fogVisibility);
    }

    gl_FragColor = color;
}