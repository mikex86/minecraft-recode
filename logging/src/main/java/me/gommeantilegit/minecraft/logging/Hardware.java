package me.gommeantilegit.minecraft.logging;

import org.jetbrains.annotations.NotNull;
import oshi.SystemInfo;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Hardware util
 */
public class Hardware {

    /**
     * The GPU Vendor constant
     */
    @NotNull
    public static final String GPU_VENDOR = retrieveGPUVendor();

    /**
     * The GPU name
     */
    @NotNull
    public static final String GPU_NAME = retrieveGPUName();

    /**
     * System info instance
     */
    @NotNull
    private static final SystemInfo systemInfo = new SystemInfo();

    static {
        systemInfo.getHardware();
        systemInfo.getOperatingSystem();
    }

    /**
     * @return the GPU's name (accesses OpenGL)
     */
    @NotNull
    private static String retrieveGPUName() {
        return glGetString("GL_RENDERER");
    }

    /**
     * @return the GPU's vendor (accesses OpenGL)
     */
    @NotNull
    private static String retrieveGPUVendor() {
        return glGetString("GL_VENDOR");
    }

    @NotNull
    private static String glGetString(@NotNull String openGLCap) {
        try {
            Class<?> gdxClass = Hardware.class.getClassLoader().loadClass("com.badlogic.gdx.Gdx");
            try {
                Field gl = gdxClass.getField("gl");
                try {
                    Object ogl = gl.get(null);
                    if (ogl == null)
                        return "Cannot retrieve. Cannot access OpenGL! com.badlogic.gdx.Gdx#gl is null meaning Gdx has not yet been initialized!";
                    try {
                        Class<?> gl20Class = Hardware.class.getClassLoader().loadClass("com.badlogic.gdx.graphics.GL20");
                        try {
                            Field glVendorField = gl20Class.getField(openGLCap);
                            try {
                                try {
                                    Integer glVendorOpenGLCap = (Integer) glVendorField.get(ogl);
                                    try {
                                        Method glGetString = gl20Class.getDeclaredMethod("glGetString", int.class);
                                        try {
                                            try {
                                                try {
                                                    try {
                                                        return ((String) glGetString.invoke(ogl, glVendorOpenGLCap));
                                                    } catch (ClassCastException e) {
                                                        return ("Cannot retrieve. Cannot access OpenGL! Method com.badlogic.gdx.graphics.GL20#glGetString(int) didn't return a string! ") + (e.getLocalizedMessage()) + ("\n");
                                                    }
                                                } catch (IllegalAccessException e) {
                                                    return ("Cannot retrieve. Cannot access OpenGL! Method com.badlogic.gdx.graphics.GL20#glGetString(int) could not be accessed!\n");
                                                }
                                            } catch (IllegalArgumentException e) {
                                                return ("Cannot retrieve. Cannot access OpenGL! Method com.badlogic.gdx.graphics.GL20#glGetString(int) was supplied with illegal arguments!\n");
                                            }
                                        } catch (InvocationTargetException e) {
                                            return ("Cannot retrieve. Cannot access OpenGL! Method com.badlogic.gdx.graphics.GL20#glGetString(int) could not be invoked! Exception: ") + (e.getTargetException().getClass().getName()) + (": Message: \n") + (e.getTargetException().getLocalizedMessage()) + ("\n");
                                        }
                                    } catch (NoSuchMethodException e) {
                                        return ("Cannot retrieve. Cannot access OpenGL! Method com.badlogic.gdx.graphics.GL20#glGetString(int) not found!\n");
                                    }
                                } catch (ClassCastException e) {
                                    return ("Cannot retrieve. Cannot access OpenGL! Method com.badlogic.gdx.graphics.GL20#glGetString(int) didn't return an Integer. ") + (e.getLocalizedMessage()) + ("\n");
                                }
                            } catch (IllegalAccessException e) {
                                return ("Cannot retrieve. Cannot access OpenGL! Field com.badlogic.gdx.graphics.GL20#" + openGLCap + " not accessible!\n");
                            }
                        } catch (NoSuchFieldException e) {
                            return ("Cannot retrieve. Cannot access OpenGL! Field com.badlogic.gdx.graphics.GL20#" + openGLCap + " not found!\n");
                        }
                    } catch (ClassNotFoundException e) {
                        return ("Cannot retrieve. Cannot access OpenGL! Class com.badlogic.gdx.graphics.GL20 not found!\n");
                    }
                } catch (IllegalAccessException e) {
                    return ("Cannot retrieve. Cannot access OpenGL! Field com.badlogic.gdx.Gdx#gl not accessible!\n");
                }
            } catch (NoSuchFieldException e) {
                return ("Cannot retrieve. Cannot access OpenGL! Field com.badlogic.gdx.Gdx#gl not found!\n");
            }
        } catch (ClassNotFoundException ignored) {
            return ("Cannot retrieve. Cannot access OpenGL! Class com.badlogic.gdx.Gdx not found!\n");
        }
    }

    /**
     * Initializing the Hardware constants
     */
    public static void init() {
    }

    @NotNull
    public static SystemInfo getSystemInfo() {
        return systemInfo;
    }
}
