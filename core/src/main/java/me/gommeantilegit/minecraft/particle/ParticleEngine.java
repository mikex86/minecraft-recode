package me.gommeantilegit.minecraft.particle;

import com.badlogic.gdx.math.Vector2;
import me.gommeantilegit.minecraft.Minecraft;
import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.block.Blocks;
import me.gommeantilegit.minecraft.texture.TextureWrapper;
import me.gommeantilegit.minecraft.timer.api.OpenGLOperation;
import me.gommeantilegit.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

public class ParticleEngine implements OpenGLOperation {

    /**
     * The parent world that the particle engine spawns the new particles into.
     */
    private final World world;

    /**
     * Queue of particles to be spawned
     */
    @NotNull
    private final Queue<Particle> scheduledParticles = new LinkedList<>();

    /**
     * Current state of particles in queue
     */
    private boolean particlesToSpawn = false;

    /**
     * Default constructor for a particle engine
     *
     * @param world sets {@link #world}
     */
    public ParticleEngine(World world) {
        this.world = world;
    }

    /**
     * Spawns a few particles into the world with the texture of the block with prevBlockID as it's id
     *
     * @param blockX      x pos of destroyed block
     * @param blockY      y pos of destroyed block
     * @param blockZ      z pos of destroyed block
     * @param prevBlockID id of destroyed block
     */
    public void spawnBlockBreakingParticles(int blockX, int blockY, int blockZ, int prevBlockID) {
        if (prevBlockID == 0) throw new IllegalStateException("Cannot break air... wtf?");
        Block prevBlock = Blocks.getBlockByID(prevBlockID);
        assert prevBlock != null;
        Vector2 uv = prevBlock.getTextureUVs()[0];
        int SD = 4;
        int xx = 0;
        while (xx < SD) {
            int yy = 0;
            while (yy < SD) {
                int zz = 0;
                while (zz < SD) {
                    float xp = (float) blockX + ((float) xx + 0.5f) / (float) SD;
                    float yp = (float) blockY + ((float) yy + 0.5f) / (float) SD;
                    float zp = (float) blockZ + ((float) zz + 0.5f) / (float) SD;
                    scheduleParticle(new Particle(world, xp, yp, zp,
                            xp - (float) blockX - 0.5f,
                            yp - (float) blockY - 0.5f,
                            zp - (float) blockZ - 0.5f,
                            Minecraft.mc.textureManager.blockTextureMap.getTexture(),
                            new TextureWrapper.RenderData(
                                    new int[]{
                                            (int) uv.x, (int) uv.y
                                    },
                                    new int[]{
                                            16, 16
                                    })
                    ));
                    ++zz;
                }
                ++yy;
            }
            ++xx;
        }
    }

    /**
     * Schedules the spawn of the given particle
     *
     * @param particle the particle to be spawned
     */
    private void scheduleParticle(Particle particle) {
        this.scheduledParticles.add(particle);
        particlesToSpawn = true;
    }

    /**
     * Spawns the particles on OpenGL Context
     *
     * @param partialTicks timer partial ticks
     */
    @Override
    public void onOpenGLContext(float partialTicks) {
        if (particlesToSpawn) {
            while (!this.scheduledParticles.isEmpty()) {
                try {
                    Particle particle = this.scheduledParticles.remove();
                    particle.setupMesh();
                    this.world.spawnEntityInWorld(particle);
                } catch (NoSuchElementException ignored) {
                }
            }
            particlesToSpawn = false;
        }
    }
}
