#version 310 es

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

/*
    Fog variables
*/
uniform bool enableFog;
in highp float fogVisibility;

uniform highp vec4 fogColor;
out highp vec4 glFragColorOut;

void main(){

    if (override_color) {
        glFragColorOut = forced_color;
        return;
    }

    //Setting the interpolated value of lightingColor passed from the vertexShader to the frag color with an alpha value of 1.

    highp vec4 texColor;
    if(!texture_mapping_bool) {
        texColor = texture(boundTexture, fragTextureCoords);
    } else {
        lowp ivec2 texSize = textureSize(boundTexture, 0);

        highp float texture_u0 = float(texture_pix_u0) / float(texSize.x);
        highp float texture_v0 = float(texture_pix_v0) / float(texSize.y);
        highp float texture_u1 = float(texture_pix_u1) / float(texSize.x);
        highp float texture_v1 = float(texture_pix_v1) / float(texSize.y);

        highp float dif_u = texture_u1 - texture_u0;
        highp float dif_v = texture_v1 - texture_v0;

        highp float uo = fragTextureCoords.x * dif_u;
        highp float vo = fragTextureCoords.y * dif_v;

        mediump vec2 calculatedCoords = vec2(texture_u0 + uo, texture_v0 + vo);

        texColor = texture(boundTexture, calculatedCoords);
//        texColor = vec4(1, 1, 1, 1);
    }

    mediump vec4 color = texColor * fragColorAttr * vec4(lightingColor, 1);

    if (texColor.a < 0.001){
        discard;
    }

    if (enableFog){
        color = mix(fogColor, color, fogVisibility);
    }

    glFragColorOut = color;
}