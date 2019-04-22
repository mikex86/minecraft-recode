package me.gommeantilegit.minecraft.utils;

import org.jetbrains.annotations.NotNull;

import static com.badlogic.gdx.graphics.GL20.*;

/**
 * Class with OpenGL related functions
 */
public class OpenGLUtils {

    /**
     * @param error the OpenGL error code
     * @return the OpenGL error name for the error code
     */
    @NotNull
    public static String getGLErrorName(int error) {
        switch (error) {
            case GL_INVALID_ENUM:
                return "GL_INVALID_ENUM";
            case GL_INVALID_VALUE:
                return "GL_INVALID_VALUE";
            case GL_INVALID_OPERATION:
                return "GL_INVALID_OPERATION";
            case 0x0503:
                return "GL_STACK_OVERFLOW";
            case 0x0504:
                return "GL_STACK_UNDERFLOW";
            case GL_OUT_OF_MEMORY:
                return "GL_OUT_OF_MEMORY";
            case GL_INVALID_FRAMEBUFFER_OPERATION:
                return "GL_INVALID_FRAMEBUFFER_OPERATION";
            default:
                return "Unknown GL error_ \"" + error + "\"";
        }
    }
}
