#version 420 core

#ifdef GL_ES
#precision mediump float;
#endif

in vec3 lightingColor;
in vec4 fragColorAttr;
in vec2 fragTextureCoords;

uniform sampler2D boundTexture;

out vec4 glFragColorOut;

void main(){

    //Setting the interpolated value of lightingColor passed from the vertexShader to the frag color with an alpha value of 1.
    vec4 color = texture(boundTexture, fragTextureCoords) * fragColorAttr; //* vec4(lightingColor, 1);
    if(color.a < 0.1)
        discard;
    glFragColorOut = color;
}