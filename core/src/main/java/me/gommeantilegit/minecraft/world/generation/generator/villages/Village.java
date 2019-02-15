package me.gommeantilegit.minecraft.world.generation.generator.villages;

import com.badlogic.gdx.math.Vector2;
import me.gommeantilegit.minecraft.world.generation.generator.villages.file.VillageFile;
import org.jetbrains.annotations.NotNull;

public class Village {

    private final int posX, posZ;

    @NotNull
    private final VillageFile villageFile;

    public Village(int posX, int posZ, @NotNull VillageFile villageFile) {
        this.posX = posX;
        this.posZ = posZ;
        this.villageFile = villageFile;
    }

    /**
     * @return a new Vec2 instance storing {@link #posX} and {@link #posZ}. Note changes to this vector do not affect {@link #posX} and {@link #posZ}
     */
    @NotNull
    public Vector2 getVillagePosition(){
        return new Vector2(posX, posZ);
    }

    @NotNull
    public VillageFile getVillageFile() {
        return villageFile;
    }
}
