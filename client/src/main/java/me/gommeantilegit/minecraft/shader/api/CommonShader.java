package me.gommeantilegit.minecraft.shader.api;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

/**
 * Blueprint for a typical shader
 */
public abstract class CommonShader extends ShaderProgram {

    public CommonShader(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    public CommonShader(FileHandle vertexShader, FileHandle fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    /**
     * Translates the transformation matrix by the x, y and z component of the specified vector
     *
     * @param translateX x component
     * @param translateY y component
     * @param translateZ z component
     */
    public abstract void translate(float translateX, float translateY, float translateZ);

    /**
     * Scales the transformation matrix by the specified x, y and z component
     *
     * @param x x component
     * @param y y component
     * @param z z component
     */
    public abstract void scale(float x, float y, float z);

    /**
     * Rotates the transformation matrix by the specified angle on the specified axis
     *
     * @param axisX angle * axisX = rotationX
     * @param axisY angle * axisY = rotationY
     * @param axisZ angle * axisZ = rotationZ
     * @param angle the angle that the given axis should be multiplied with to compute the rotation value
     */
    public abstract void rotate(float axisX, float axisY, float axisZ, float angle);

    /**
     * Pushes a copy of the current transformation matrix on top of the matrix stack making it the currently used one.
     */
    public abstract void pushMatrix();

    /**
     * Removes the top element of the matrix stack
     */
    public abstract void popMatrix();

    /**
     * @return the current transformation matrix
     */
    public abstract Matrix4 getTransformationMatrix();

    /**
     * @return the view matrix
     */
    public abstract Matrix4 getViewMatrix();

    /**
     * @return the projection matrix
     */
    public abstract Matrix4 getProjectionMatrix();

//    /**
//     * Changes a shader uniform that is added to the output color of the shader before the fog mixing
//     * @param r red component
//     * @param g green component
//     * @param b blue component
//     * @param a alpha component
//     */
//    public abstract void setColorAdd(float r, float g, float b, float a);

    /**
     * Changes a shader base output color multiplier to the specified color vector
     * @param r red component
     * @param g green component
     * @param b blue component
     * @param a alpha component
     */
    public abstract void setColor(float r, float g, float b, float a);
}
