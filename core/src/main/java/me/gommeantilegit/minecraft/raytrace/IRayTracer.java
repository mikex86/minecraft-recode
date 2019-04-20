package me.gommeantilegit.minecraft.raytrace;

import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.util.block.facing.EnumFacing;
import me.gommeantilegit.minecraft.util.block.position.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IRayTracer {

    /**
     * Updates the RayTracer
     */
    void update();

    @Nullable
    IRayTracer.RayTraceResult getRayTraceResult();

    class RayTraceResult {

        /**
         * The position where the rayCast hit an object
         */
        public final Vector3 hitVec;

        /**
         * The faced block aligned position. Nullable
         */
        @Nullable
        private final BlockPos blockPos;

        /**
         * Represents the type of result. eg. if a block is faced or if its a miss. Later on an entity can be also faced at.
         */
        public enum EnumResultType {
            BLOCK, MISS, ENTITY
        }

        /**
         * The type of the result.
         *
         * @see EnumResultType
         */
        @NotNull
        public final EnumResultType type;

        /**
         * State if the moving object position is final and will not be changed this frame.
         */
        public boolean valid;

        /**
         * Facing, how the player is facing the block
         */
        @Nullable
        public final EnumFacing hitSide;

        /**
         * Entity faced. Null, if type is {@link RayTraceResult.EnumResultType#MISS} or {@link RayTraceResult.EnumResultType#BLOCK}
         */
        @Nullable
        private final Entity entity;

        /**
         * @param hitVec   sets {@link #hitVec}
         * @param blockPos sets {@link #blockPos}
         * @param type     sets {@link #type}
         */
        public RayTraceResult(@Nullable Vector3 hitVec, @Nullable BlockPos blockPos, @NotNull EnumResultType type, @Nullable EnumFacing hitSide) {
            this.hitVec = hitVec;
            this.blockPos = blockPos;
            this.type = type;
            this.hitSide = hitSide;
            this.entity = null;
        }

        public RayTraceResult(@Nullable Entity entityHitIn, @NotNull Vector3 hitVecIn) {
            this.type = EnumResultType.ENTITY;
            this.entity = entityHitIn;
            this.hitVec = hitVecIn;
            this.hitSide = null;
            this.blockPos = new BlockPos(hitVec);
        }

        @Nullable
        public BlockPos getBlockPos() {
            return blockPos;
        }

        @Nullable
        public Entity getEntity() {
            return entity;
        }

        @Nullable
        public EnumFacing getHitSide() {
            return hitSide;
        }

        @NotNull
        public EnumResultType getType() {
            return type;
        }
    }
}
