package me.gommeantilegit.minecraft.util.block.facing;

import me.gommeantilegit.minecraft.util.math.vecmath.intvectors.Vec3i;
import org.jetbrains.annotations.NotNull;

import static java.lang.Math.abs;
import static java.lang.Math.floor;

public enum EnumFacing {

    DOWN(new Vec3i(0, -1, 0)),
    UP(new Vec3i(0, 1, 0)),
    NORTH(new Vec3i(0, 0, -1)),
    SOUTH(new Vec3i(0, 0, 1)),
    WEST(new Vec3i(-1, 0, 0)),
    EAST(new Vec3i(1, 0, 0));

    /**
     * All Facings with horizontal axis in order S-W-N-E
     */
    public static final EnumFacing[] HORIZONTALS = new EnumFacing[4];

    /**
     * Direction vector the EnumFacing instance is facing.
     */
    private final Vec3i offset;

    /**
     * State if the facing is horizontal
     */
    private final boolean horizontal;

    EnumFacing(Vec3i offset) {
        this.offset = offset;
        this.horizontal = this.offset.getY() == 0;
    }

    static {
        int index = 0;
        for (EnumFacing facing : values()) {
            if (facing.horizontal)
                HORIZONTALS[index++] = facing;
        }
    }

    /**
     * Get the Facing corresponding to the given angle (0-360). An angle of 0 is SOUTH, an angle of 90 would be WEST.
     */
    @NotNull
    public static EnumFacing fromAngle(double angle) {
        return getHorizontal((int) floor(angle / 90.0D + 0.5D) & 3);
    }

    /**
     * Get a Facing by it's horizontal index (0-3). The order is S-W-N-E.
     */
    public static EnumFacing getHorizontal(int index) {
        return HORIZONTALS[abs(index % HORIZONTALS.length)];
    }

    public Vec3i getOffset() {
        return offset;
    }

}
