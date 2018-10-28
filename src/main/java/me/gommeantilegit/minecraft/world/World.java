package me.gommeantilegit.minecraft.world;

import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.block.Blocks;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.phys.AxisAlignedBB;
import me.gommeantilegit.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class World {

    private final int[][][] blocks;
    private final int width, height, depth;

    @NotNull
    private final ArrayList<Chunk> chunks;

    @NotNull
    public final ArrayList<Entity> entities = new ArrayList<>();

    public World(int width, int height, int depth) {
        this.blocks = new int[width][height][depth];
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.chunks = genChunks(this);
    }

    /**
     * Spawns the given entity into the world
     *
     * @param entity the given entity.
     */
    public void spawnEntityInWorld(Entity entity) {
        this.entities.add(entity);
    }

    /**
     * World onTick update
     */
    public void tick() {
        trackEntities();
    }

    /**
     * @param axisAlignedBB the boundingBox
     * @return if an entity is in the given bounding box
     */
    public boolean isFree(AxisAlignedBB axisAlignedBB) {
        int i = 0;
        while (i < this.entities.size()) {
            if (this.entities.get(i).boundingBox.intersects(axisAlignedBB)) {
                return false;
            }
            ++i;
        }
        return true;
    }

    /**
     * Called on entity collision with a bounding box
     *
     * @param entity        the entity which collided
     * @param axisAlignedBB the bounding box it collided with
     */
    public void collision(ArrayList<AxisAlignedBB> blockABBs, Entity entity, AxisAlignedBB axisAlignedBB) {
        for (AxisAlignedBB bb : blockABBs) {
            if (bb.x0 == axisAlignedBB.x0 && bb.y0 == axisAlignedBB.y0 &&
                    bb.z0 == axisAlignedBB.z0 && bb.x1 == axisAlignedBB.x1 &&
                    bb.y1 == axisAlignedBB.y1 && bb.z1 == axisAlignedBB.z1) {
                onBlockCollision(entity, axisAlignedBB);
            }
        }
    }

    /**
     * Called on block collision
     *
     * @param entity        the entity which collided
     * @param axisAlignedBB the boundingBox of the block it collided with
     */
    private void onBlockCollision(Entity entity, AxisAlignedBB axisAlignedBB) {
        int x = (int) axisAlignedBB.x0, y = (int) axisAlignedBB.y0, z = (int) axisAlignedBB.z0;
        Block block = Blocks.getBlockByID(getBlockID(x, y, z));
        assert block != null;
        block.onEntityCollide(entity, axisAlignedBB);
    }

    /**
     * @param boundingBox the bounding box
     * @return all bounding boxes of the blocks in the given bounding box.
     */
    public ArrayList<AxisAlignedBB> getCubes(AxisAlignedBB boundingBox) {
        ArrayList<AxisAlignedBB> boundingBoxes = new ArrayList<>();
        for (int x = (int) boundingBox.x0; x < boundingBox.x1; x++) {
            for (int y = (int) boundingBox.y0; y < boundingBox.y1; y++) {
                for (int z = (int) boundingBox.z0; z < boundingBox.z1; z++) {
                    Block block = getBlock(x, y, z);
                    if (block != null) {
                        boundingBoxes.add(boundingBox);
                    }
                }
            }
        }
        return boundingBoxes;
    }

    /**
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @return the block instance at the specified coordinates.
     */
    @Nullable
    private Block getBlock(int x, int y, int z) {
        return Blocks.getBlockByID(getBlockID(x, y, z));
    }

    /**
     * @param blockID the given blockID
     * @return true if the block with the given id is has transparency or is air.
     */
    public boolean canSeeThrough(int blockID) {
        Block block = Blocks.getBlockByID(blockID);
        return block == null || block.transparent;
    }

    /**
     * Removes dead entities and updates them.
     */
    public void trackEntities() {
        //Tracking loaded entities
        int i = 0;
        while (i < this.entities.size()) {
            this.entities.get(i).tick();
            if (this.entities.get(i).dead) {
                this.entities.remove(i--);
            }
            ++i;
        }
    }

    /**
     * Sets the block at the given position
     *
     * @param x       x position
     * @param y       y position
     * @param z       z position
     * @param blockID the new block id
     */
    public void setBlock(int x, int y, int z, int blockID) {
        blocks[x][y][z] = blockID;
        Chunk chunk = getChunkAt(x, z);
        assert chunk != null;
        chunk.setRebuild(true);
    }

    /**
     * @param x x position
     * @param z z position
     * @return the parent chunk of the given position.
     */
    private Chunk getChunkAt(int x, int z) {
        for (Chunk chunk : this.chunks) {
            if (chunk.contains(x, z)) {
                return chunk;
            }
        }
        return null;
    }

    /**
     * @param world the given world
     * @return a new list of chunks generated for the given world.
     */
    private ArrayList<Chunk> genChunks(World world) {
        ArrayList<Chunk> chunks = new ArrayList<>();
        for (int x = 0; x < world.getWidth(); x += Chunk.CHUNK_SIZE) {
            for (int z = 0; z < world.getDepth(); z += Chunk.CHUNK_SIZE) {
                Chunk chunk = new Chunk(world.getHeight(), x, z, world);
                chunks.add(chunk);
            }
        }
        return chunks;
    }


    public ArrayList<Chunk> genChunks() {
        return chunks;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getDepth() {
        return depth;
    }

    public int[][][] getBlocks() {
        return blocks;
    }

    /**
     * @param x x position
     * @param y y position
     * @param z z position
     * @return the id of the block at the given position
     */
    public int getBlockID(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0) return 0;
        if (x >= width || y >= height || z >= depth) return 0;
        return blocks[x][y][z];
    }
}
