package me.gommeantilegit.minecraft.shader.programs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.shader.api.CommonShader;
import org.jetbrains.annotations.NotNull;

import java.util.Stack;

/**
 * Standard universal shader program
 * Designed to keep the camera at zero all the time!
 */
public class StdShader extends CommonShader {

    /**
     * Uniform variable name of transformation matrix
     */
    private static final String TRANSFORMATION_MATRIX_UNIFORM_NAME = "transMat";

    /**
     * Uniform variable name of view matrix
     */
    private static final String VIEW_MATRIX_UNIFORM_NAME = "viewMat";

    /**
     * Uniform variable name of projection matrix
     */
    private static final String PROJECTION_MATRIX_UNIFORM_NAME = "projectionMat";

    /* FOG UNIFORM NAMES */
    /**
     * Uniform variable name of the boolean enableFog storing the state whether or not fog should be enabled
     */
    private static final String ENABLE_FOG_BOOLEAN_UNIFORM_NAME = "enableFog";

    /**
     * Uniform variable name of the fog color used
     */
    private static final String FOG_COLOR_UNIFORM_NAME = "fogColor";

    /**
     * Uniform variable name of the fog end value
     */
    private static final String FOG_END_UNIFORM_NAME = "fogEnd";

    /**
     * Uniform variable name of the fog start value
     */
    private static final String FOG_START_UNIFORM_NAME = "fogStart";

    /**
     * Uniform variable name of the boolean state variable that indicates whether or not lighting is currently enabled
     */
    private static final String ENABLE_LIGHTING_UNIFORM_NAME = "enableLighting";

//    /**
//     * Uniform variable name of the color vec4 that is added to the output color of the fragment shader before the mix of the fog color
//     */
//    private static final String COLOR_ADD_VECTOR_UNIFORM_NAME = "additionColor";

    /**
     * Uniform variable name of a color multiplier that the output color of the fragment shader is multiplied with before the fog color
     */
    private static final String COLOR_MULTIPLIER_VECTOR_UNIFORM_NAME = "shaderColor";

    /**
     * Uniform variable name of the minimum diffused lighting factor that is allowed
     */
    private static final String MIN_DIFFUSED_LIGHTING_UNIFORM_NAME = "minDiffuseLighting";

    /**
     * Camera object
     */
    private Camera camera;

    /**
     * Transformation matrix stack
     */
    private final Stack<Matrix4> matrixStack = new Stack<>();

    /**
     * Virtual camera position for internal computations
     */
    @NotNull
    private final Vector3 virtualCameraPos = new Vector3();

    public StdShader() {
        super(Gdx.files.classpath("shader/std_shader/std_vert.glsl"), Gdx.files.classpath("shader/std_shader/std_frag.glsl"));
        System.err.println(getLog());
        this.matrixStack.push(new Matrix4());
    }

    @Override
    public void begin() {
        super.begin();
        setColor(1, 1, 1, 1);
    }

    /**
     * Specifies a virtual camera position.
     * Nothing changes for the user, but the shader internally keeps the camera at zero
     *
     * @param x x coord
     * @param y y coord
     * @param z z coord
     */
    public void setVirtualCameraPos(float x, float y, float z) {
        this.virtualCameraPos.set(x, y, z);
    }

    public void renderStart() {
        pushMatrix();
        translate(-virtualCameraPos.x, -virtualCameraPos.y, -virtualCameraPos.z);
    }

    public void renderEnd() {
        popMatrix();
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

    public void setUniformBoolean(@NotNull String uniformName, boolean state) {
        setUniformi(uniformName, state ? 1 : 0);
    }

    public void updateMatrix() {
        this.setUniformMatrix(TRANSFORMATION_MATRIX_UNIFORM_NAME, getTransformationMatrix());
        this.setUniformMatrix(VIEW_MATRIX_UNIFORM_NAME, this.camera.view);
        this.setUniformMatrix(PROJECTION_MATRIX_UNIFORM_NAME, this.camera.projection);
    }

    @Override
    public void setColor(float r, float g, float b, float a) {
        this.setUniformf(COLOR_MULTIPLIER_VECTOR_UNIFORM_NAME, r, g, b, a);
    }

    /**
     * Disables shader based lighting
     */
    public void disableLighting() {
        setUniformBoolean(ENABLE_LIGHTING_UNIFORM_NAME, false);
    }

    /**
     * Enables shader based lighting
     */
    public void enableLighting() {
        setUniformBoolean(ENABLE_LIGHTING_UNIFORM_NAME, true);
    }

    public void pushMatrix() {
        this.matrixStack.push(getTransformationMatrix().cpy());
    }

    public void popMatrix() {
        this.matrixStack.pop();
    }

    public void enableFog(boolean fog) {
        setUniformBoolean(ENABLE_FOG_BOOLEAN_UNIFORM_NAME, fog);
    }

    public void setFogStart(float fogStart) {
        setUniformf(FOG_START_UNIFORM_NAME, fogStart);
    }

    public void setMinDiffusedLighting(float minDiffusedLighting){
        this.setUniformf(MIN_DIFFUSED_LIGHTING_UNIFORM_NAME, minDiffusedLighting);
    }

    public void setFogEnd(float fogEnd) {
        setUniformf(FOG_END_UNIFORM_NAME, fogEnd);
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
