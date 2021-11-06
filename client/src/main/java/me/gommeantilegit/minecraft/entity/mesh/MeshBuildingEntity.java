package me.gommeantilegit.minecraft.entity.mesh;

import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.rendering.mesh.IMeshBuilding;
import me.gommeantilegit.minecraft.world.WorldBase;
import org.jetbrains.annotations.Nullable;

public abstract class MeshBuildingEntity extends Entity implements IMeshBuilding {

    /**
     * @param world sets {@link #world}
     */
    public MeshBuildingEntity(@Nullable WorldBase world) {
        super(world);
    }

}
