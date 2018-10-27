package me.michael.kei.minecraft.world;

import me.michael.kei.minecraft.world.chunk.Chunk;

import java.util.ArrayList;

public class World {

    private final int[][][] blocks;
    private final int width, height, depth;
    private final ArrayList<Chunk> chunks;

    public World(int width, int height, int depth) {
        this.blocks = new int[width][height][depth];
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.chunks = getChunks(this);
    }

    public int[][][] getBlocks() {
        return blocks;
    }

    public void setBlock(int x, int y, int z, int blockID) {
        blocks[x][y][z] = blockID;
        Chunk chunk = getChunkAt(x, z);
        assert chunk != null;
        chunk.setRebuild(true);
    }

    private Chunk getChunkAt(int x, int z) {
        for (Chunk chunk : this.chunks) {
            if (chunk.contains(x, z)) {
                return chunk;
            }
        }
        return null;
    }

    private ArrayList<Chunk> getChunks(World world) {
        ArrayList<Chunk> chunks = new ArrayList<>();
        for (int x = 0; x < world.getWidth(); x += Chunk.CHUNK_SIZE) {
            for (int z = 0; z < world.getDepth(); z += Chunk.CHUNK_SIZE) {
                Chunk chunk = new Chunk(world.getHeight(), x, z, world);
                chunks.add(chunk);
            }
        }
        return chunks;
    }


    public ArrayList<Chunk> getChunks() {
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

    public int getBlockID(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0) return 0;
        if (x >= width || y >= height || z >= depth) return 0;
        return blocks[x][y][z];
    }

}
