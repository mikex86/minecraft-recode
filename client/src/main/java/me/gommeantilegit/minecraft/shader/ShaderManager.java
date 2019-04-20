package me.gommeantilegit.minecraft.shader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import me.gommeantilegit.minecraft.shader.programs.StdShader;
import org.jetbrains.annotations.NotNull;

public class ShaderManager {

    public StdShader stdShader;

    public void compileShaders() {
        stdShader = new StdShader();
    }

    @NotNull
    private ShaderProgram compile(@NotNull String vertexShaderResource, @NotNull String fragmentShaderResource) {
        ShaderProgram program = new ShaderProgram(Gdx.files.classpath(vertexShaderResource), Gdx.files.classpath(fragmentShaderResource));
        System.out.println(program.getLog());
        return program;
    }

}
