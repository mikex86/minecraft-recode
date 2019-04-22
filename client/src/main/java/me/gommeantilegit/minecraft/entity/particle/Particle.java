package me.gommeantilegit.minecraft.entity.particle;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.Side;
import me.gommeantilegit.minecraft.annotations.NeedsOpenGLContext;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.rendering.mesh.MeshBuilding;
import me.gommeantilegit.minecraft.texture.TextureWrapper;
import me.gommeantilegit.minecraft.texture.custom.CustomTexture;
import me.gommeantilegit.minecraft.world.ClientWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;
import static me.gommeantilegit.minecraft.rendering.Constants.STD_VERTEX_ATTRIBUTES;
import static me.gommeantilegit.minecraft.util.RenderUtils.rect;

@SideOnly(side = Side.CLIENT)
public class Particle extends Entity implements MeshBuilding {

    /**
     * Particle motion values
     */
    private float xSpeed, ySpeed, zSpeed;

    /**
     * The texture applied to the particle rectangle.
     */
    @NotNull
    public final CustomTexture texture;

    /**
     * The region of the texture that should be applied to the particle.
     */
    @NotNull
    private final TextureWrapper.RenderData textureRegion;

    /**
     * The age of the particle in ticks.
     */
    private int age;

    /**
     * The lifetime of the particle in ticks
     */
    private int lifetime;

    /**
     * The size of the particle
     */
    private float size;

    /**
     * The particles mesh
     */
    @Nullable
    public Mesh mesh;

    /**
     * Meshbuilder used to build the particles mesh
     */
    @Nullable
    public MeshBuilder meshbuilder;

    /**
     * Default constructor creating a particle
     *
     * @param world  the world the particle should be spawned in
     * @param posX   the x position of the origin where the particle was emitted.
     * @param posY   the z position of the origin where the particle was emitted.
     * @param posZ   the y position of the origin where the particle was emitted.
     * @param xSpeed the x speed of the particle at spawn time.
     * @param ySpeed the y speed of the particle at spawn time.
     * @param zSpeed the z speed of the particle at spawn time.
     *               param textureWrapper sets textureWrapper
     *               param textureRegion  sets textureRegion
     */
    public Particle(ClientWorld world, float posX, float posY, float posZ, float xSpeed, float ySpeed, float zSpeed, @NotNull CustomTexture texture,
                    @NotNull TextureWrapper.RenderData textureRegion) {
        super(world);
        this.texture = texture;
        this.textureRegion = textureRegion;

        //Initializing particle SOUND_RESOURCES
        {
            this.setSize(0.2f, 0.2f);
            this.heightOffset = this.bbHeight / 2.0f;
            this.setPosition(posX, posY, posZ);
            this.xSpeed = xSpeed + (float) (Math.random() * 2.0 - 1.0) * 0.4f;
            this.ySpeed = ySpeed + (float) (Math.random() * 2.0 - 1.0) * 0.4f;
            this.zSpeed = zSpeed + (float) (Math.random() * 2.0 - 1.0) * 0.4f;
            float speed = (float) (Math.random() + Math.random() + 1.0) * 0.15f;
            float totalSpeedVector = (float) Math.sqrt(this.xSpeed * this.xSpeed + this.ySpeed * this.ySpeed + this.zSpeed * this.zSpeed);
            this.xSpeed = this.xSpeed / totalSpeedVector * speed * 0.4f;
            this.ySpeed = this.ySpeed / totalSpeedVector * speed * 0.4f + 0.1f;
            this.zSpeed = this.zSpeed / totalSpeedVector * speed * 0.4f;
            this.size = (float) (Math.random() * 0.5 + 0.5);
            this.lifetime = (int) (4.0 / (Math.random() * 0.9 + 0.1));
        }
    }

    @NotNull
    @Override
    public MeshBuilder buildMesh() {
        /*
         * Randomly generated SOUND_RESOURCES used as u and v offset to the textureRegion, whose size is reduced when rendering the particle. The those SOUND_RESOURCES
         * are a random offset moving the actually rendered region around inside of the specified region textureRegion
         */
        float uOffset = (float) Math.random() * 3.0f, vOffset = (float) Math.random() * 3.0f;

        //Setting up mesh
        MeshBuilder builder = new MeshBuilder();
        builder.begin(STD_VERTEX_ATTRIBUTES, GL_TRIANGLES);
        int x0 = 0, y0 = 0, z0 = 0;
        float u0 = textureRegion.getPixelPositions()[0], v0 = textureRegion.getPixelPositions()[1];
        float uWidth = textureRegion.getPixelSizes()[0] * 0.3f, vHeight = textureRegion.getPixelSizes()[1] * 0.3f;
        u0 += uOffset;
        v0 += vOffset;

        {
            u0 /= texture.getWidth();
            uWidth /= texture.getWidth();
            v0 /= texture.getHeight();
            vHeight /= texture.getHeight();
        }

        float u1 = u0 + uWidth, v1 = v0 + vHeight;
        float x1 = size * 0.3f, y1 = size * 0.3f;

        rect(builder,
                x0, y0, z0,
                u1, v1,
                x1, y0, z0,
                u0, v1,
                x1, y1, z0,
                u0, v0,
                x0, y1, z0,
                u1, v0,
                0, 0, 1,
                1, 1, 1, 1
        );
        return builder;
    }

    @Override
    public void storeBuildMesh(@NotNull MeshBuilder meshBuilder) {
        meshbuilder = meshBuilder;
    }

    @NeedsOpenGLContext
    @Override
    public void finishMesh(@NotNull MeshBuilder meshBuilder) {
        mesh = meshBuilder.end();
    }

    @NeedsOpenGLContext
    @Override
    public void finishMesh() {
        assert meshbuilder != null;
        finishMesh(this.meshbuilder);
        meshbuilder = null;
    }

    /**
     * Updating the particle
     */
    public void tick() {
        this.lastPosX = this.posX;
        this.lastPosY = this.posY;
        this.lastPosZ = this.posZ;
        if (this.age++ >= this.lifetime) {
            this.setDead();
        }
        this.ySpeed = (float) ((double) this.ySpeed - 0.04);
        this.moveEntity(this.xSpeed, this.ySpeed, this.zSpeed);
        this.xSpeed *= 0.98f;
        this.ySpeed *= 0.98f;
        this.zSpeed *= 0.98f;
        if (this.onGround) {
            this.xSpeed *= 0.7f;
            this.zSpeed *= 0.7f;
        }
    }
}

