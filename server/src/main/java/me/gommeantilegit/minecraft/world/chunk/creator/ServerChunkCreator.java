package me.gommeantilegit.minecraft.world.chunk.creator;

import me.gommeantilegit.minecraft.annotations.Unsafe;
import me.gommeantilegit.minecraft.block.state.palette.IBlockStatePalette;
import me.gommeantilegit.minecraft.util.math.vecmath.intvectors.Vec2i;
import me.gommeantilegit.minecraft.world.ServerWorld;
import me.gommeantilegit.minecraft.world.WorldBase;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.chunk.ServerChunk;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ServerChunkCreator extends ChunkCreatorBase {

    public ServerChunkCreator(@NotNull ServerWorld world) {
        super(world);
    }

    /**
     * @param chunk the chunk to be added
     * @see OnChunkCreationListener
     */
    @Unsafe
    protected void createChunk(@NotNull ChunkBase chunk) {
        if (!this.world.getOnChunkCreationListeners().isEmpty()) {
            this.world.getOnChunkCreationListeners().forEach(l -> l.onChunkCreated(chunk));
        }
        super.createChunk(chunk);
    }

    @NotNull
    @Override
    public ServerChunk tryCreateChunkFor(@NotNull Vec2i position) {
        ServerChunk prev, chunk;
        synchronized (this) { // synchronizing to prevent other threads from racing to create a "NON EXISTING" chunk multiple times
            Vec2i origin = world.getChunkOrigin(position.getX(), position.getY());
            prev = ((ServerWorld) world).getWorldChunkHandler().getChunkAt(origin.getX(), origin.getY());
            if (prev == null) {
                chunk = createChunk(origin);
            } else {
                chunk = prev;
            }
        }
        if (prev == null) {
            ((ServerWorld) world).getWorldGenerator().onChunkCreated(chunk); // Invoking the terrain generation
            chunk.setWorldGenerationFinished(true);
        }
        return chunk;
    }

    @NotNull
    @Override
    public synchronized ServerChunk createChunk(@NotNull Vec2i origin) {
        return (ServerChunk) super.createChunk(origin);
    }

    @NotNull
    @Override
    protected ServerChunk newChunkFor(int height, int chunkX, int chunkZ, WorldBase world, IBlockStatePalette blockStatePalette) {
        return new ServerChunk(height, chunkX, chunkZ, world, blockStatePalette);
    }
}
