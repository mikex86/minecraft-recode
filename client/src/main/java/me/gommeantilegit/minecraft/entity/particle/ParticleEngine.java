package me.gommeantilegit.minecraft.entity.particle;

import com.badlogic.gdx.math.Vector2;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.annotations.NeedsOpenGLContext;
import me.gommeantilegit.minecraft.annotations.ThreadSafe;
import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.block.BlockTypeRenderer;
import me.gommeantilegit.minecraft.texture.TextureWrapper;
import me.gommeantilegit.minecraft.timer.api.AsyncOperation;
import me.gommeantilegit.minecraft.timer.api.OpenGLOperation;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

public class ParticleEngine implements AsyncOperation, OpenGLOperation {

    /**
     * The parent world that the particle engine spawns the new particles into.
     */
    @NotNull
    private final ClientMinecraft mc;

    /**
     * Queue of particles to be spawned
     */
    @NotNull
    private final Queue<Particle> scheduledParticles = new LinkedBlockingDeque<>();

    /**
     * Current state of any particles being in the spawn queue
     */
    private boolean particlesToSpawn = false;

    /**
     * ExecutorService to build particle meshes
     */
    @NotNull
    private final ExecutorService executorService = Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() / 3), r -> {
        Thread thread = new Thread(r, "ParticleEngine-PoolThread");
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
        return thread;
    });

    /**
     * Default constructor for a particle engine
     *
     * @param mc sets {@link #mc}
     */
    public ParticleEngine(@NotNull ClientMinecraft mc) {
        this.mc = mc;
    }

    /**
     * Spawns a few particles into the world with the texture of the block with prevBlock as it's id
     *
     * @param blockX    x pos of destroyed block
     * @param blockY    y pos of destroyed block
     * @param blockZ    z pos of destroyed block
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
    @ThreadSafe
    private void scheduleParticle(@NotNull Particle particle) {
        this.executorService.submit(() -> {
            particle.setupMesh();
            particlesToSpawn = true;
            scheduledParticles.add(particle);
        });
    }

    /**
     * Spawns the particles on OpenGL Context
     *
     * @param partialTicks timer partial ticks
     */
    @Override
    @NeedsOpenGLContext
    public void onOpenGLContext(float partialTicks) {
        if (particlesToSpawn) {
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

    @Override
    public void stopAsyncWork() {
        this.executorService.shutdown();
    }
}
