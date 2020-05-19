package me.gommeantilegit.minecraft.rendering;

import com.badlogic.gdx.Gdx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class GLContext {

    @Nullable
    private static GLContext glContext = null;

    /**
     * The thread that the OpenGL context is bound to
     */
    @NotNull
    private final Thread openGLThread;

    public GLContext(@NotNull Thread openGLThread) {
        this.openGLThread = openGLThread;
    }

    public static void initGLContext() {
        glContext = new GLContext(Thread.currentThread());
    }

    /**
     * Executes the specified runnable on the OpenGL Thrad an waits until it was executed
     *
     * @param voidFutureTask the future task to be executed
     */
    public void runOnGLContextWait(@NotNull FutureTask<?> voidFutureTask) {
        if (isCallingFromOpenGLThread()) {
            voidFutureTask.run();
            return;
        }
        runOnGLContext(voidFutureTask);
        try {
            voidFutureTask.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes the specified runnable on the OpenGL Thread
     *
     * @param runnable the runnable to be executed
     */
    public void runOnGLContext(@NotNull FutureTask<?> runnable) {
        if (isCallingFromOpenGLThread()) {
            runnable.run();
            return;
        }
        Gdx.app.postRunnable(runnable);
    }

    /**
     * Executes the specified runnable on the OpenGL Thread
     *
     * @param runnable the runnable to be executed
     */
    public void runOnGLContext(@NotNull Runnable runnable) {
        runOnGLContext(new FutureTask<>((Callable<Void>) () -> {
            runnable.run();
            return null;
        }));
    }


    /**
     * @return true, if this method was executed on the OpenGL thread else false
     */
    public boolean isCallingFromOpenGLThread() {
        return Thread.currentThread() == this.openGLThread;
    }


    @NotNull
    public static GLContext getGlContext() {
        return Objects.requireNonNull(glContext, "GLContext has not yet been initialized!");
    }
}
