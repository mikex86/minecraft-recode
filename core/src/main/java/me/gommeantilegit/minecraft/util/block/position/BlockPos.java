package me.gommeantilegit.minecraft.util.block.position;

import com.badlogic.gdx.math.Vector3;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;

import me.gommeantilegit.minecraft.util.block.facing.EnumFacing;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import static com.badlogic.gdx.math.MathUtils.floor;

/**
 * Helper class for representing a block position
 */
public class BlockPos {

    public static final BlockPos ORIGIN = new BlockPos(0, 0, 0);

    /**
     * X, Y, Z Components of the position vector
     */
    private final int x, y, z;

    /**
     * @param x sets {@link #x}
     * @param y sets {@link #y}
     * @param z sets {@link #z}
     */
    public BlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockPos(@NotNull Vector3 vector) {
        this(floor(vector.x), floor(vector.y), floor(vector.z));
    }

    @NotNull
    public BlockPos offset(@NotNull EnumFacing facing) {
        return new BlockPos(x + facing.getOffset().getX(), y + facing.getOffset().getY(), z + facing.getOffset().getZ());
    }

    @NotNull
    public BlockPos clone() {
        return new BlockPos(x, y, z);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @NotNull
    public static EnumFacing getFacing(Vector3 intersection, BlockPos facedBlock) {
        EnumFacing facing = EnumFacing.UP;
        if (intersection.y == (int) intersection.y) {
            float dif = intersection.y - facedBlock.y;
            if (dif == 0) facing = EnumFacing.DOWN;
        } else if (intersection.x == (int) intersection.x) {
            float dif = intersection.x - facedBlock.x;
            if (dif == 1) facing = EnumFacing.EAST;
            else if (dif == 0) facing = EnumFacing.WEST;
        } else if (intersection.z == (int) intersection.z) {
            float dif = intersection.z - facedBlock.z;
            if (dif == 1) facing = EnumFacing.SOUTH;
            else if (dif == 0) facing = EnumFacing.NORTH;
        }
        return facing;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BlockPos) {
            BlockPos pos = (BlockPos) obj;
            return x == pos.x && y == pos.y && z == pos.z;
        } else {
            return false;
        }
    }

    @NotNull
    public Vector3 asVector() {
        return new Vector3(x, y, z);
    }

    @NotNull
    public Collection<BlockPos> getNeighbors() {
        return Arrays.asList(clone(), add(1, 0, 0), add(0, 0, 1), add(0, 1, 1), add(0, -1, 0));
    }

    /**
     * @param x x addition
     * @param y y addition
     * @param z z addition
     * @return a new BlockPos the the value of this BlockPos + the given IntVector
     */
    private BlockPos add(int x, int y, int z) {
        return new BlockPos(this.x + x, this.y + y, this.z + z);
    }
}
