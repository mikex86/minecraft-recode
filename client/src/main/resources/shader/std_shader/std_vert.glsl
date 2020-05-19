#version 150

/* Default precisions */
//#ifdef GL_ES
precision highp float;
precision highp int;
precision lowp sampler2D;
precision lowp samplerCube;

//#else
//in vec3 a_Position;
//in vec3 a_Normal;
//in vec4 a_Color;
//in vec2 a_TextureCoord;
//
//uniform vec3 lightDirection;
//
///* Matrices */
//uniform mat4 transMat;
//uniform mat4 viewMat;
//uniform mat4 projectionMat;
//
//uniform bool enableLighting;
//
//uniform float minDiffuseLighting;
//
///*
//    Fog Uniform variables
//*/
//uniform bool enableFog;
//uniform float fogStart;
//uniform float fogEnd;
//
//out vec3 lightingColor;
//out vec4 fragColorAttr;
//out vec2 fragTextureCoords;
//out float fogVisibility;
//#endif

in highp vec3 a_Position;
in highp vec3 a_Normal;
in highp vec4 a_Color;
in highp vec2 a_TextureCoord;

uniform highp vec3 lightDirection;

/* Matrices */
uniform highp mat4 transMat;
uniform highp mat4 viewMat;
uniform highp mat4 projectionMat;

uniform bool enableLighting;

uniform highp float minDiffuseLighting;

/*
    Fog Uniform variables
*/
uniform bool enableFog;
uniform highp float fogStart;
uniform highp float fogEnd;

out highp vec3 lightingColor;
out highp vec4 fragColorAttr;
out highp vec2 fragTextureCoords;
out highp float fogVisibility;

/*
 Calculates the brightness of the processed vertex using dot with per vertex diffused lighting.
 @param min the minimum value of brightness the vertex can have
*/
float calculateDiffusedLighting(float min) {
    return max(dot(
    normalize(
    (
    transMat * vec4(a_Normal, 0)
    ).xyz
    ),
    lightDirection), min);
}

/**
 * Calculates the fog factor for the specified vertex distance
 * @param dst the specified vertex distance
 */
float getFogFactor(float dst) {
    if (dst >= fogEnd) return 1.0f;
    if (dst <= fogStart) return 0.0f;

    return 1.0f - (fogEnd - dst) / (fogEnd - fogStart);
}

void main() {

    highp mat4 matViewProj = projectionMat * (viewMat * transMat);

    fragColorAttr = a_Color;

    fragTextureCoords = a_TextureCoord;

    lowp float dotLighting;

    if (enableLighting)
    dotLighting = calculateDiffusedLighting(minDiffuseLighting);
    else dotLighting = 1.0f;

    /**
        Setting the lighting color variable to be passed to the fragment shader to be linearly interpolated per pixel.
        Using dotLighting value as r, g, b, values.
    */
    lightingColor = vec3(dotLighting);

    if (enableFog){
        lowp float distanceToCamera = length((viewMat * (transMat * vec4(a_Position, 1))).xyz);
        fogVisibility = getFogFactor(distanceToCamera);
    }
    gl_Position = matViewProj * vec4(a_Position, 1);
}