package me.gommeantilegit.minecraft.shader.programs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import org.jetbrains.annotations.NotNull;

import java.util.Stack;

public class StdShader extends ShaderProgram {

    private static final String TRANSFORMATION_MATRIX_UNIFORM_NAME = "transMat";
    private static final String VIEW_MATRIX_UNIFORM_NAME = "viewMat";
    private static final String PROJECTION_MATRIX_UNIFORM_NAME = "projectionMat";

    /* FOG UNIFORM NAMES */
    private static final String ENABLE_FOG_BOOLEAN_UNIFORM_NAME = "enableFog";
    private static final String FOG_COLOR_UNIFORM_NAME = "fogColor";
    private static final String FOG_DENSITY_UNIFORM_NAME = "fogDensity";
    private static final String FOG_GRADIENT_UNIFORM_NAME = "fogGradient";

    private Camera camera;

    private final Stack<Matrix4> matrixStack = new Stack<>();

    public StdShader() {
        super(Gdx.files.classpath("shaders/std_vert.glsl"), Gdx.files.classpath("shaders/std_frag.glsl"));
        System.err.println(getLog());
        this.matrixStack.push(new Matrix4());
    }

    public StdShader setCamera(Camera camera) {
        this.camera = camera;
        return this;
    }

    public void translate(float translateX, float translateY, float translateZ) {
        this.getTransformationMatrix().translate(translateX, translateY, translateZ);
        updateMatrix();
    }

    public void overrideColor(boolean state) {
        setUniformBoolean("override_color", state);
    }

    public void forceColor(float r, float g, float b, float a) {
        overrideColor(true);
        setUniformf("forced_color", r, g, b, a);
    }

    public void resetForcedColor() {
        forceColor(1, 1, 1, 1);
        overrideColor(false);
    }

    public void enableTextureMapping() {
        setUniformBoolean("texture_mapping_bool", true);
    }

    public void setPixelU0(int pu0) {
        setUniformi("texture_pix_u0", pu0);
    }

    public void setPixelV0(int pv0) {
        setUniformi("texture_pix_v0", pv0);
    }

    public void setPixelU1(int pu1) {
        setUniformi("texture_pix_u1", pu1);
    }

    public void setPixelV1(int pv1) {
        setUniformi("texture_pix_v1", pv1);
    }

    public void disableTextureMapping() {
        setUniformBoolean("texture_mapping_bool", false);
        setPixelU0(0);
        setPixelV0(0);
        setPixelU1(0);
        setPixelV1(0);
    }

    public void scale(float x, float y, float z) {
        getTransformationMatrix().scale(x, y, z);
        updateMatrix();
    }


    public void rotate(float axisX, float axisY, float axisZ, float angle) {
        this.getTransformationMatrix().rotate(axisX, axisY, axisZ, angle);
        updateMatrix();
    }

    public void newFrame() {
//        resetTranslation();
    }

    public void setUniformBoolean(@NotNull String uniformName, boolean state) {
        setUniformi(uniformName, state ? 1 : 0);
    }

    public void updateMatrix() {
        this.setUniformMatrix(TRANSFORMATION_MATRIX_UNIFORM_NAME, this.getTransformationMatrix());
        this.setUniformMatrix(VIEW_MATRIX_UNIFORM_NAME, this.camera.view);
        this.setUniformMatrix(PROJECTION_MATRIX_UNIFORM_NAME, this.camera.projection);
    }

    public void pushMatrix() {
        this.matrixStack.push(getTransformationMatrix().cpy());
        updateMatrix();
    }

    public void popMatrix() {
        this.matrixStack.pop();
        updateMatrix();
    }

    public void enableFog(boolean fog) {
        setUniformBoolean(ENABLE_FOG_BOOLEAN_UNIFORM_NAME, fog);
    }

    public void setFogGradient(float gradient) {
        setUniformf(FOG_GRADIENT_UNIFORM_NAME, gradient);
    }

    public void setFogDensity(float density) {
        setUniformf(FOG_DENSITY_UNIFORM_NAME, density);
    }

    public void setFogColor(Color color) {
        setUniformf(FOG_COLOR_UNIFORM_NAME, color);
    }

    public Matrix4 getTransformationMatrix() {
        return this.matrixStack.peek();
    }

    public Matrix4 getViewMatrix() {
        return this.camera.view;
    }

    public Matrix4 getProjectionMatrix() {
        return this.camera.projection;
    }

    public Camera getCamera() {
        return camera;
    }
}
