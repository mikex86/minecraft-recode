package me.gommeantilegit.minecraft.world.generation.generator.api.biome;

public enum Biome {

    /**
     * Ocean Biome
     */
    OCEAN((byte) 0);

    /**
     * The biome identifier number
     */
    private final byte id;


    Biome(byte id) {
        this.id = id;
    }

    public byte getId() {
        return id;
    }
}
