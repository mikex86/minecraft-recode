#version 310 es

/* Default precisions */
precision highp float;
precision highp int;
precision lowp sampler2D;
precision lowp samplerCube;

in highp vec3 a_Position;
in highp vec3 a_Normal;
in highp vec4 a_Color;
in highp vec2 a_TextureCoord;

uniform highp vec3 lightDirection;

/* Matrices */
uniform highp mat4 transMat;
uniform highp mat4 viewMat;
uniform highp mat4 projectionMat;
/*
    Fog Uniform variables
*/
uniform bool enableFog;
uniform highp float fogDensity;
uniform highp float fogGradient;

out vec3 lightingColor;
out vec4 fragColorAttr;
out vec2 fragTextureCoords;
out highp float fogVisibility;

/*
 Calculates the brightness of the processed vertex using dot with per vertex diffused lighting.
 @param min the minimum value of brightness the vertex can have
*/
float calculateDiffusedLighting(float min){
    return max(dot(
        normalize(
        (
            transMat * vec4(a_Normal, 0)
        ).xyz
        ),
        lightDirection), min);
}

void main(){

    highp mat4 matViewProj = projectionMat * viewMat * transMat;

    fragColorAttr = a_Color;

    fragTextureCoords = a_TextureCoord;

    lowp float dotLighting = calculateDiffusedLighting(0.1f);

    /**
        Setting the lighting color variable to be passed to the fragment shader to be linearly interpolated per pixel.
        Using dotLighting value as r, g, b, values.
    */
    lightingColor = vec3(dotLighting);

    if (enableFog){
        lowp float distanceToCamera = length((viewMat * (transMat * vec4(a_Position, 1))).xyz);
        fogVisibility = clamp(exp(-pow((distanceToCamera * fogDensity), fogGradient)), 0.0, 1.0);
    }
    gl_Position = matViewProj * vec4(a_Position, 1);
}