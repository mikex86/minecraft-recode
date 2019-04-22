package me.gommeantilegit.minecraft.entity.particle;

import com.badlogic.gdx.math.Vector2;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.annotations.NeedsOpenGLContext;
import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.block.BlockTypeRenderer;
import me.gommeantilegit.minecraft.texture.TextureWrapper;
import me.gommeantilegit.minecraft.timer.api.OpenGLOperation;
import me.gommeantilegit.minecraft.timer.api.AbstractAsyncOperation;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

public class ParticleEngine extends AbstractAsyncOperation implements OpenGLOperation {

    /**
     * The parent world that the particle engine spawns the new particles into.
     */
    @NotNull
    private final ClientMinecraft mc;

    /**
     * Queue of particles to be spawned
     */
    @NotNull
    private final Queue<Particle> scheduledParticles = new LinkedList<>();

    /**
     * Current state of any particles being in the spawn queue
     */
    private boolean particlesToSpawn = false;

    /**
     * Queue of particles whose meshes need to be built
     */
    @NotNull
    private final Queue<Particle> toBuild = new LinkedList<>();

    /**
     * Current state of any being particles in the build queue
     */
    private boolean particlesToBuild = false;

    /**
     * Default constructor for a particle engine
     *
     * @param mc sets {@link #mc}
     */
    public ParticleEngine(@NotNull ClientMinecraft mc) {
        super("ParticleEngine-Async-Thread", 20);
        this.mc = mc;
    }

    /**
     * Spawns a few particles into the world with the texture of the block with prevBlock as it's id
     *
     * @param blockX      x pos of destroyed block
     * @param blockY      y pos of destroyed block
     * @param blockZ      z pos of destroyed block
     * @param prevBlock id of destroyed block
     */
    public void spawnBlockBreakingParticles(int blockX, int blockY, int blockZ, @NotNull Block prevBlock) {
        BlockTypeRenderer typeRenderer = mc.blockRendererRegistry.getRenderer(prevBlock);
        assert typeRenderer != null;
        Vector2 uv = typeRenderer.getTextureUVs()[0];
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
                    scheduleParticle(new Particle(mc.theWorld, xp, yp, zp,
                            xp - (float) blockX - 0.5f,
                            yp - (float) blockY - 0.5f,
                            zp - (float) blockZ - 0.5f,
                            mc.textureManager.blockTextureMap.getTexture(),
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
    private void scheduleParticle(@NotNull Particle particle) {
        synchronized (this.toBuild) {
            this.toBuild.add(particle);
            particlesToBuild = true;
        }
    }

    /**
     * Spawns the particles on OpenGL Context
     *
     * @param partialTicks timer partial ticks
     */
    @NeedsOpenGLContext
    @Override
    public void onOpenGLContext(float partialTicks) {
        if (particlesToSpawn) {
            synchronized (scheduledParticles) {
                while (!this.scheduledParticles.isEmpty()) {
                    try {
                        Particle particle = this.scheduledParticles.remove();
                        particle.finishMesh();
                        this.mc.theWorld.spawnEntityInWorld(particle);
                    } catch (NoSuchElementException ignored) {
                        this.scheduledParticles.clear();
                    }
                }
                particlesToSpawn = false;
            }
        }
    }

    @Override
    public void onAsyncThread() {
        if (particlesToBuild) {
            synchronized (toBuild) {
                while (!this.toBuild.isEmpty()) {
                    try {
                        Particle particle = this.toBuild.remove();
                        particle.setupMesh();
                        particlesToSpawn = true;
                        scheduledParticles.add(particle);
                    } catch (NoSuchElementException ignored) {
                    }
                }
                particlesToBuild = false;
            }
        }
    }
}
