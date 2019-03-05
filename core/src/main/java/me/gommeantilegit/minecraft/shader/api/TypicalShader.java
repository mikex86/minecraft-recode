package me.gommeantilegit.minecraft.shader.api;

import com.badlogic.gdx.math.Matrix4;

/**
 * Blueprint for a typical shader
 */
public interface TypicalShader {

    /**
     * Translates the transformation matrix by the x, y and z component of the specified vector
     * @param translateX x component
     * @param translateY y component
     * @param translateZ z component
     */
    void translate(float translateX, float translateY, float translateZ);

    /**
     * Scales the transformation matrix by the specified x, y and z component
     * @param x x component
     * @param y y component
     * @param z z component
     */
    void scale(float x, float y, float z);

    /**
     * Rotates the transformation matrix by the specified angle on the specified axis
     * @param axisX angle * axisX = rotationX
     * @param axisY angle * axisY = rotationY
     * @param axisZ angle * axisZ = rotationZ
     * @param angle the angle that the given axis should be multiplied with to compute the rotation value
     */
    void rotate(float axisX, float axisY, float axisZ, float angle);

    /**
     * Pushes a copy of the current transformation matrix on top of the matrix stack making it the currently used one.
     */
    void pushMatrix();

    /**
     * Removes the top element of the matrix stack
     */
    void popMatrix();

    /**
     * @return the current transformation matrix
     */
    Matrix4 getTransformationMatrix();

    /**
     * @return the view matrix
     */
    Matrix4 getViewMatrix();

    /**
     * @return the projection matrix
     */
    Matrix4 getProjectionMatrix();

}
