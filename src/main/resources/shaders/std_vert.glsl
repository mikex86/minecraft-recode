#version 420 core

in vec3 a_Position;
in vec3 a_Normal;
in vec4 a_Color;
in vec2 a_TextureCoord;

uniform vec3 lightDirection;
uniform mat4 matViewProj;

out vec3 lightingColor;
out vec4 fragColorAttr;
out vec2 fragTextureCoords;
out vec3 fragVertex;

/*
  Normal of the vertex passed to the fragment shader
*/
out vec3 fragNormal;

/*
 Calculates the brightness of the processed vertex using dot with per vertex diffused lighting.
 @param min the minimum value of brightiness the vertex can have
*/
float calculateDiffusedLighting(float min){
    return max(dot(a_Normal, lightDirection), min);
}

void main(){

    fragNormal = a_Normal;

    fragColorAttr = a_Color;

    fragTextureCoords = a_TextureCoord;

    float dotLighting = calculateDiffusedLighting(0);

    /**
        Setting the lighting color variable to be passed to the fragment shader to be linearly interpolated per pixel.
        Using dotLighting value as r, g, b, values.
    */
    lightingColor = vec3(dotLighting);

    gl_Position = matViewProj * vec4(a_Position, 1);
}