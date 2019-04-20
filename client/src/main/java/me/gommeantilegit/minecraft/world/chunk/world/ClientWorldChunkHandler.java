package me.gommeantilegit.minecraft.world.chunk.world;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.Side;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.utils.collections.SortedArrayList;
import me.gommeantilegit.minecraft.world.ClientWorld;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.chunk.ClientChunk;
import me.gommeantilegit.minecraft.world.chunk.builder.ChunkRebuilder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;

@SideOnly(side = Side.CLIENT)
public class ClientWorldChunkHandler extends WorldChunkHandlerBase {

    @NotNull
    public final ChunkRebuilder chunkRebuilder;

    /**
     * Stores all chunks that are closer to the player than the worlds "nearChunkDistance"
     */
    @NotNull
    private final ArrayList<ClientChunk> nearChunks;

    public ClientWorldChunkHandler(@NotNull ClientWorld clientWorld) {
        super(new SortedArrayList<>(
                        // Keeping the chunks list stored for distance prioritized chunk loading
                        new ChunkSorter(clientWorld)),
                new ArrayList<>(),
                new ArrayList<>());
        this.chunkRebuilder = new ChunkRebuilder(clientWorld);
        this.nearChunks = new ArrayList<>();
    }

    public void addNearChunk(@NotNull ClientChunk clientChunk) {
        this.nearChunks.add(clientChunk);
    }

    public void removeNearChunk(@NotNull ClientChunk clientChunk) {
        this.nearChunks.remove(clientChunk);
    }

    @NotNull
    public ChunkRebuilder getChunkRebuilder() {
        return chunkRebuilder;
    }

    @NotNull
    public ArrayList<ClientChunk> getNearChunks() {
        return nearChunks;
    }

    private static class ChunkSorter implements Comparator<ChunkBase> {

        /**
         * Parent client world instance
         */
        @NotNull
        private final ClientWorld world;

        private ChunkSorter(@NotNull ClientWorld world) {
            this.world = world;
        }

        @Override
        public int compare(ChunkBase chunk1, ChunkBase chunk2) {
            return Double.compare(dst(chunk1), dst(chunk2));
        }

        /**
         * @param chunk the chunk that the distance should be measured to
         * @return the distance from the viewer viewing the world (as specified in {@link ClientWorld#viewer}) to the given chunk origin in blocks
         */
        private double dst(ChunkBase chunk) {
            Vector3 positionVector = world.viewer.getPositionVector();
            Vector2 pos2D = new Vector2(positionVector.x, positionVector.z);
            return pos2D.dst(chunk.getChunkOrigin().asLibGDXVec2D().add(ChunkBase.CHUNK_SIZE / 2f, ChunkBase.CHUNK_SIZE / 2f));
        }

    }
}
