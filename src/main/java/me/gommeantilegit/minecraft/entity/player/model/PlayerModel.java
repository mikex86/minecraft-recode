package me.gommeantilegit.minecraft.entity.player.model;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.rendering.Cube;

import static java.lang.Math.hypot;

public class PlayerModel {

    public Cube head;
    public Cube body;
    public Cube arm0;
    public Cube arm1;
    public Cube leg0;
    public Cube leg1;

    private static final float SIZE = 0.058333334f;

    public PlayerModel(Texture texture) {
        this.head = new Cube(0, 0, -4.0f, -8.0f, -4.0f, 8, 8, 8, 8f, 8f, 8, texture);

        this.body = new Cube(16, 16, -4.0f, 0.0f, -2.0f, 8, 12, 4, 8, 12, 4, texture);
        this.body.translate(0, -(this.head.height + this.body.height), 0);

        this.arm0 = new Cube(40, 16, -3.0f, -2.0f, -2.0f, 4, 12, 4, 4, 12, 4, texture);
        this.arm0.translate(-5.0f, -(this.head.height + this.arm0.height - 2.0f), 0.0f);

        this.arm1 = new Cube(40, 16, -1.0f, -2.0f, -2.0f, 4, 12, 4, 4, 12, 4, texture);
        this.arm1.translate(5.0f, -(this.head.height + this.arm1.height - 2.0f), 0.0f);

        this.leg0 = new Cube(0, 16, -2.0f, 0.0f, -2.0f, 4, 12, 4, 4, 12, 4, texture);
        this.leg0.translate(-2.0f, -(this.head.height + this.body.height + 12.0f), 0.0f);

        this.leg1 = new Cube(0, 16, -2.0f, 0.0f, -2.0f, 4, 12, 4, 4, 12, 4, texture);
        this.leg1.translate(2.0f, -(this.head.height + this.body.height + 12.0f), 0.0f);

        scale(SIZE);
    }

    private void scale(float size) {
        this.head.scale(size, size, size);
        this.body.scale(size, size, size);
        this.arm0.scale(size, size, size);
        this.arm1.scale(size, size, size);
        this.leg0.scale(size, size, size);
        this.leg1.scale(size, size, size);
    }

    public void render(float partialTicks, Entity entity, ShaderProgram shaderProgram, Camera camera) {

        this.head.yRot = entity.rotationYaw;
        this.head.xRot = entity.rotationPitch;

        this.arm0.xRot = (float) ((float)Math.sin((double)partialTicks * 0.6662 + 3.141592653589793) * hypot(entity.motionX, entity.motionZ)) * 7;
        this.arm0.zRot = (float)(Math.sin((double)partialTicks * 0.2312) + 1.0) * 1.0f;
//
        this.arm1.xRot = (float) ((float)Math.sin((double)partialTicks * 0.6662) * hypot(entity.motionX, entity.motionZ)) * 7;
        this.arm1.zRot = (float)(Math.sin((double)partialTicks * 0.2812) - 1.0) * 1.0f;

//        this.leg0.xRot = (float)Math.sin((double)time * 0.6662) * 1.4f;
//        this.leg1.xRot = (float)Math.sin((double)time * 0.6662 + 3.141592653589793) * 1.4f;
        this.head.render(camera, shaderProgram);
        this.body.render(camera, shaderProgram);
        this.arm0.render(camera, shaderProgram);
        this.arm1.render(camera, shaderProgram);
        this.leg0.render(camera, shaderProgram);
        this.leg1.render(camera, shaderProgram);
    }


}

