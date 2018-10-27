#ifdef GL_ES
#precision mediump float;
#endif

in vec3 lightingColor;
in vec4 fragColorAttr;
in vec2 fragTextureCoords;

uniform vec3 lightDirection;
uniform sampler2D boundTexture;

void main(){
    //Setting the interpolated value of lightingColor passed from the vertexShader to the frag color with an alpha value of 1.
    gl_FragColor = texture(boundTexture, fragTextureCoords) * fragColorAttr; //* vec4(lightingColor, 1);
}