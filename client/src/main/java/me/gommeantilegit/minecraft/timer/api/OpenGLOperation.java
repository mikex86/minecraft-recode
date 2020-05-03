package me.gommeantilegit.minecraft.timer.api;

import me.gommeantilegit.minecraft.annotations.NeedsOpenGLContext;

/**
 * Interface for Objects performing operations on the OpenGL Context
 */
//TODO: REMOVE
public interface OpenGLOperation {

    /**
     * Called on OpenGL Context
     */
    @NeedsOpenGLContext
    void onOpenGLContext(float partialTicks);

}
