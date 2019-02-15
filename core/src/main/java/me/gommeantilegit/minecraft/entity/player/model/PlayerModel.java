package me.gommeantilegit.minecraft.entity.player.model;

import me.gommeantilegit.minecraft.entity.player.Player;
import me.gommeantilegit.minecraft.rendering.Cube;
import me.gommeantilegit.minecraft.shader.programs.StdShader;
import me.gommeantilegit.minecraft.texture.custom.CustomTexture;
import me.gommeantilegit.minecraft.util.MathHelper;

import static java.lang.Math.*;

public class PlayerModel {

    public Cube head;
    public Cube body;
    public Cube arm0;
    public Cube arm1;
    public Cube leg0;
    public Cube leg1;

    private static final float SIZE = 0.058333334f;

    public PlayerModel(CustomTexture texture) {
        this.head = new Cube(0, 0, -4.0f, -8.0f, -4.0f,
                8, 8, 8, 8, 8, 8, texture);
        this.body = new Cube(16, 16, -4.0f, 0.0f, -2.0f,
                8, 12, 4, 8, 12, 4, texture);
        this.arm0 = new Cube(40, 16, -3.0f, -2.0f, -2.0f,
                4, 12, 4, 4, 12, 4, texture);
        this.arm0.translate(-5.0f, 2.0f, 0.0f);
        this.arm1 = new Cube(40, 16, -1.0f, -2.0f, -2.0f,
                4, 12, 4, 4, 12, 4, texture);
        this.arm1.translate(5.0f, 2.0f, 0.0f);
        this.leg0 = new Cube(0, 16, -2.0f, 0.0f, -2.0f,
                4, 12, 4, 4, 12, 4, texture);
        this.leg0.translate(-2.0f, 12.0f, 0.0f);
        this.leg1 = new Cube(0, 16, -2.0f, 0.0f, -2.0f,
                4, 12, 4, 4, 12, 4, texture);
        this.leg1.translate(2.0f, 12.0f, 0.0f);
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

    public void render(float partialTicks, Player entity, StdShader shaderProgram) {

        float height = this.body.height + this.leg0.height;

        shaderProgram.pushMatrix();
        shaderProgram.translate(0, -height * SIZE, 0);

        setRotationAngles(partialTicks, entity);

        this.head.render(shaderProgram);

        float corpseTurn = interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);

        shaderProgram.rotate(0, 1.0F, 0.0F, corpseTurn);

        this.body.render(shaderProgram);
        this.arm0.render(shaderProgram);
        this.arm1.render(shaderProgram);
        this.leg0.render(shaderProgram);
        this.leg1.render(shaderProgram);

        shaderProgram.popMatrix();
    }

    private float interpolateRotation(float prevRenderYawOffset, float renderYawOffset, float partialTicks) {
        float offsetDif = renderYawOffset - prevRenderYawOffset;
        return MathHelper.wrapToAngle(prevRenderYawOffset + offsetDif * partialTicks);
    }

    private void setRotationAngles(float partialTicks, Player entity) {
        float intensity = (float) (PI * PI * PI) * 1.4f;
        float limbSwing = entity.prevLimbSwing + (entity.limbSwing - entity.prevLimbSwing) * partialTicks;
        float limbSwingAmount = (entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks);
        //Head rotation
        {
            this.head.yRot = 0;
            this.head.xRot = -entity.rotationPitch;
        }
        //Arm rotation
        {
            this.arm0.xRot = (float) (cos(
                    (
                            limbSwing
                    ) * 0.6662F) * 2f *
                    limbSwingAmount * 0.5F * intensity);

            this.arm1.xRot = (float) (cos(
                    (
                            limbSwing
                    ) * 0.6662F + PI) * 2f *
                    limbSwingAmount * 0.5F * intensity);

            this.arm0.zRot = 0;
            this.arm0.zRot = 0;
        }

        //Leg swing
        {
            this.leg0.xRot = (float) (cos(limbSwing * 0.6662F) * 1.4 * limbSwingAmount * intensity);
            this.leg1.xRot = (float) (cos(limbSwing * 0.6662F + PI) * 1.4 * limbSwingAmount * intensity);
        }

        float swingProgress = entity.getInterpolatedSwingProgress(partialTicks);

//        this.bipedRightLeg.yRot = 0.0F;
//        this.bipedLeftLeg.yRot = 0.0F;
//
//        if (this.isRiding)
//        {
//            this.arm0.xRot += -((float)Math.PI / 5F);
//            this.arm1.xRot += -((float)Math.PI / 5F);
//            this.bipedRightLeg.xRot = -((float)Math.PI * 2F / 5F);
//            this.bipedLeftLeg.xRot = -((float)Math.PI * 2F / 5F);
//            this.bipedRightLeg.yRot = ((float)Math.PI / 10F);
//            this.bipedLeftLeg.yRot = -((float)Math.PI / 10F);
//        }
//
//        if (this.heldItemLeft != 0)
//        {
//            this.arm1.xRot = this.arm1.xRot * 0.5F - ((float)Math.PI / 10F) * (float)this.heldItemLeft;
//        }
//
//        this.arm0.yRot = 0.0F;
//        this.arm0.zRot = 0.0F;
//
//        switch (this.heldItemRight)
//        {
//            case 0:
//            case 2:
//            default:
//                break;
//
//            case 1:
//                this.arm0.xRot = this.arm0.xRot * 0.5F - ((float)Math.PI / 10F) * (float)this.heldItemRight;
//                break;
//
//            case 3:
//                this.arm0.xRot = this.arm0.xRot * 0.5F - ((float)Math.PI / 10F) * (float)this.heldItemRight;
//                this.arm0.yRot = -0.5235988F;
//        }
//

        arm0.yRot = 0.0F;
        arm0.zRot = 0.0F;

        arm1.zRot = 0.0F;
        arm1.yRot = 0.0F;

        if (swingProgress > 0) {
            float f = swingProgress;
            this.body.yRot = (float) -(sin(sqrt(f) * (float) Math.PI * 2.0F) * 0.2F);
            this.arm0.zRot = (float) (sin(this.body.yRot) * 5.0F);
            this.arm0.xRot = (float) (-cos(this.body.yRot) * 5.0F);
            this.arm1.zRot = (float) (-sin(this.body.yRot) * 5.0F);
            this.arm1.xRot = (float) (cos(this.body.yRot) * 5.0F);
            this.arm0.yRot += this.body.yRot;
            this.arm1.yRot += this.body.yRot;
            //noinspection SuspiciousNameCombination
            this.arm1.xRot += this.body.yRot;
            f = 1.0F - swingProgress;
            f = f * f;
            f = f * f;
            f = 1.0F - f;
            float f1 = (float) sin(f * (float) Math.PI);
            float f2 = (float) (sin(swingProgress * (float) Math.PI) * 0.75F);
            this.arm0.xRot = f1 * -60 - 5f;
//            this.arm0.yRot -= this.body.yRot * 4.0F;
            this.arm0.zRot += toDegrees(sin(swingProgress * (float) Math.PI)) * -0.4F * 1.0f;

            //Negate values
            {
                this.arm0.xRot = -arm0.xRot;
                this.arm0.yRot = -arm0.yRot;
//                this.arm0.zRot = -arm0.zRot;

                this.arm1.xRot = -arm1.xRot;
                this.arm1.yRot = -arm1.yRot;
//                this.arm1.zRot = -arm1.zRot;
            }
        }
        //Breathing
        {
            this.arm0.zRot += toDegrees(cos(entity.ticksExisted * 0.09F)) * 0.05F + 0.05F;
            this.arm1.zRot -= toDegrees(cos(entity.ticksExisted * 0.09F)) * 0.05F + 0.05F;
            this.arm0.xRot += toDegrees(sin(entity.ticksExisted * 0.067F)) * 0.05F;
            this.arm1.xRot -= toDegrees(sin(entity.ticksExisted * 0.067F)) * 0.05F;
        }
//
//        if (this.isSneak)
//        {
//            this.body.xRot = 0.5F;
//            this.arm0.xRot += 0.4F;
//            this.arm1.xRot += 0.4F;
//            this.bipedRightLeg.zRot = 4.0F;
//            this.bipedLeftLeg.zRot = 4.0F;
//            this.bipedRightLeg.rotationPointY = 9.0F;
//            this.bipedLeftLeg.rotationPointY = 9.0F;
//            this.head.rotationPointY = 1.0F;
//        }
//        else
//        {
//            this.body.xRot = 0.0F;
//            this.bipedRightLeg.zRot = 0.1F;
//            this.bipedLeftLeg.zRot = 0.1F;
//            this.bipedRightLeg.rotationPointY = 12.0F;
//            this.bipedLeftLeg.rotationPointY = 12.0F;
//            this.head.rotationPointY = 0.0F;
//        }
//
//        this.arm0.zRot += cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
//        this.arm1.zRot -= cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
//        this.arm0.xRot += sin(ageInTicks * 0.067F) * 0.05F;
//        this.arm1.xRot -= sin(ageInTicks * 0.067F) * 0.05F;
//
//        if (this.aimedBow)
//        {
//            float f3 = 0.0F;
//            float f4 = 0.0F;
//            this.arm0.zRot = 0.0F;
//            this.arm1.zRot = 0.0F;
//            this.arm0.yRot = -(0.1F - f3 * 0.6F) + this.head.yRot;
//            this.arm1.yRot = 0.1F - f3 * 0.6F + this.head.yRot + 0.4F;
//            this.arm0.xRot = -((float)Math.PI / 2F) + this.head.xRot;
//            this.arm1.xRot = -((float)Math.PI / 2F) + this.head.xRot;
//            this.arm0.xRot -= f3 * 1.2F - f4 * 0.4F;
//            this.arm1.xRot -= f3 * 1.2F - f4 * 0.4F;
//            this.arm0.zRot += cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
//            this.arm1.zRot -= cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
//            this.arm0.xRot += sin(ageInTicks * 0.067F) * 0.05F;
//            this.arm1.xRot -= sin(ageInTicks * 0.067F) * 0.05F;
//        }
    }

}

