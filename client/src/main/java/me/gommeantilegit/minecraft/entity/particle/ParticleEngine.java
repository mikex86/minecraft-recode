package me.gommeantilegit.minecraft.entity.particle;

import com.badlogic.gdx.math.Vector2;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.annotations.ThreadSafe;
import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.block.BlockTypeRenderer;
import me.gommeantilegit.minecraft.rendering.GLContext;
import me.gommeantilegit.minecraft.texture.TextureWrapper;
import me.gommeantilegit.minecraft.timer.api.AsyncOperation;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParticleEngine implements AsyncOperation {

    /**
     * The parent world that the particle engine spawns the new particles into.
     */
    @NotNull
    private final ClientMinecraft mc;

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
                    spawnParticle(new Particle(mc.theWorld, xp, yp, zp,
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
    private void spawnParticle(@NotNull Particle particle) {
        this.executorService.submit(() -> {
            particle.setupMesh(); // Mesh building complete before it gets added to the queue
            GLContext.getGlContext().runOnGLContext(() -> {
                particle.finishMesh();
                this.mc.theWorld.spawnEntityInWorld(particle); // Mesh initialized before it gets spawned into the world and thus before it is rendered
            });
        });
    }

    @Override
    public void stopAsyncWork() {
        this.executorService.shutdown();
    }
}
