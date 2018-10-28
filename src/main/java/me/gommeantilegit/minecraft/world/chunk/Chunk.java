package me.gommeantilegit.minecraft.world.chunk;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import me.gommeantilegit.minecraft.Minecraft;
import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.block.Blocks;
import me.gommeantilegit.minecraft.world.World;

import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;

public class Chunk {

    public static final int CHUNK_SIZE = 16;
    private final int height;
    private final int x, z;
    private final World world;
    private Mesh mesh;
    private boolean rebuild = true;

    public Chunk(int height, int x, int z, World world) {
        this.height = height;
        this.x = x;
        this.z = z;
        this.world = world;
    }

    private void rebuild() {
        MeshBuilder builder = new MeshBuilder();
        builder.begin(Minecraft.mc.vertexAttributes, GL_TRIANGLES);
        for (int x = this.x; x < this.x + CHUNK_SIZE; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = this.z; z < this.z + CHUNK_SIZE; z++) {
                    int blockID = this.world.getBlocks()[x][y][z];
                    Block block = Blocks.getBlockByID(blockID);
                    if (block != null)
                        block.render(builder, x, y, z, world, false);
                }
            }
        }
        this.mesh = builder.end();
    }

    public void render() {
        if (rebuild) {
            rebuild();
            rebuild = false;
        }
        this.mesh.render(Minecraft.mc.shaderManager.stdShader, GL_TRIANGLES);
    }

    public Chunk setRebuild(boolean rebuild) {
        this.rebuild = rebuild;
        return this;
    }

    public boolean needsRebuild() {
        return rebuild;
    }

    public boolean contains(int x, int z) {
        return x >= this.x && z >= this.z && x < this.x + CHUNK_SIZE && z < this.z + CHUNK_SIZE;
    }
}
