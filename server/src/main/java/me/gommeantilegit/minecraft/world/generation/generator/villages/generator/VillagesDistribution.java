package me.gommeantilegit.minecraft.world.generation.generator.villages.generator;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class VillagesDistribution {

    /**
     * Salt used to change seed for random creator
     */
    private final int salt;

    public VillagesDistribution(@NotNull Random random) {
        this.salt = random.nextInt();
    }

//    TODO: IMPLEMENT

    public int getSalt() {
        return salt;
    }
}
