#version 150

/* Default precisions */
precision mediump int;
precision lowp sampler2D;
precision lowp samplerCube;

in highp vec3 lightingColor;
in highp vec4 fragColorAttr;
in highp vec2 fragTextureCoords;

uniform sampler2D boundTexture;

/* Texture mapping uniforms */
uniform bool texture_mapping_bool;

uniform int texture_pix_u0;
uniform int texture_pix_v0;
uniform int texture_pix_u1;
uniform int texture_pix_v1;

/* Color control */
uniform bool override_color;
uniform highp vec4 forced_color;

///* A color vector added to the output color before the fog specified by the java code */
//uniform highp vec4 additionColor;

/*
    Fog variables
*/
uniform bool enableFog;
in highp float fogVisibility;

uniform highp vec4 fogColor;
out highp vec4 glFragColorOut;

/* Java code controlled color multiplier */
uniform highp vec4 shaderColor;

void main() {
    mediump vec4 color;

    if (override_color) {
        color = forced_color * shaderColor;
        if (enableFog) {
           color = mix(color, fogColor, fogVisibility);
        }
        glFragColorOut = color;
        return;
    }

    highp vec4 texColor;

    //Setting the interpolated value of lightingColor passed from the vertexShader to the frag color with an alpha value of 1.

    if(!texture_mapping_bool) {
        texColor = texture(boundTexture, fragTextureCoords);
    } else {
        highp ivec2 texSize = textureSize(boundTexture, 0);

        highp float texture_u0 = float(texture_pix_u0) / float(texSize.x);
        highp float texture_v0 = float(texture_pix_v0) / float(texSize.y);
        highp float texture_u1 = float(texture_pix_u1) / float(texSize.x);
        highp float texture_v1 = float(texture_pix_v1) / float(texSize.y);

        highp float dif_u = texture_u1 - texture_u0;
        highp float dif_v = texture_v1 - texture_v0;

        highp float uo = fragTextureCoords.x * dif_u;
        highp float vo = fragTextureCoords.y * dif_v;

        highp vec2 calculatedCoords = vec2(texture_u0 + uo, texture_v0 + vo);

        texColor = texture(boundTexture, calculatedCoords);
    }

    color = (texColor * fragColorAttr * shaderColor * vec4(lightingColor, 1));

    if (color.a < 0.1F) {
        discard;
    }

    if (enableFog) {
        color = mix(color, fogColor, fogVisibility);
    }

    glFragColorOut = color;
}