package me.michael.kei.minecraft.shader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.sun.istack.internal.NotNull;

public class ShaderManager {

    @NotNull
    public ShaderProgram stdShader;

    public void compileShaders(){
        stdShader = compile("shaders/std_vert.glsl", "shaders/std_frag.glsl");
    }

    @NotNull
    private ShaderProgram compile(String vertexShaderResource, String fragmentShaderResource){
        ShaderProgram program = new ShaderProgram(Gdx.files.classpath(vertexShaderResource), Gdx.files.classpath(fragmentShaderResource));
        System.out.println(program.getLog());
        return program;
    }
}
