#version 120

attribute vec3 a_Position;
attribute vec3 a_Normal;
attribute vec4 a_Color;
attribute vec2 a_TextureCoord;

uniform vec3 lightDirection;

/* Matrices */
uniform mat4 transMat;
uniform mat4 viewMat;
uniform mat4 projectionMat;

uniform bool enableLighting;

uniform float minDiffuseLighting;

/*
    Fog Uniform variables
*/
uniform bool enableFog;
uniform float fogStart;
uniform float fogEnd;

varying vec3 lightingColor;
varying vec4 fragColorAttr;
varying vec2 fragTextureCoords;
varying float fogVisibility;

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

    mat4 matViewProj = projectionMat * (viewMat * transMat);

    fragColorAttr = a_Color;

    fragTextureCoords = a_TextureCoord;

    float dotLighting;

    if (enableLighting)
    dotLighting = calculateDiffusedLighting(minDiffuseLighting);
    else dotLighting = 1.0f;

    /**
        Setting the lighting color variable to be passed to the fragment shader to be linearly interpolated per pixel.
        Using dotLighting value as r, g, b, values.
    */
    lightingColor = vec3(dotLighting);

    if (enableFog){
        float distanceToCamera = length((viewMat * (transMat * vec4(a_Position, 1))).xyz);
        fogVisibility = getFogFactor(distanceToCamera);
    }
    gl_Position = matViewProj * vec4(a_Position, 1);
}