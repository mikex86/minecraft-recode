package me.gommeantilegit.minecraft.block.sound;

import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Wrapper around a HashMap storing all blocks as keys and their parent sound types as values.
 */
public class BlockSounds {

    @NotNull
    public final BlockSoundType stone = new BlockSoundType("stone", 1.0F, 1.0F);

    /**
     * the wood sound type
     */
    @NotNull
    public final BlockSoundType wood = new BlockSoundType("wood", 1.0F, 1.0F);

    /**
     * the gravel sound type
     */
    public final BlockSoundType gravel = new BlockSoundType("gravel", 1.0F, 1.0F);

    @NotNull
    public final BlockSoundType grass = new BlockSoundType("grass", 1.0F, 1.0F);

    @NotNull
    public final BlockSoundType metal = new BlockSoundType("stone", 1.0F, 1.5F);

    @NotNull
    public final BlockSoundType glass = new BlockSoundType("glass", 1.0F, 1.0F) {

        @NotNull
        public String getBreakSoundResourceStartString() {
            return "sound/random/glass";
        }

        @NotNull
        public String getPlaceSoundResourceStartString() {
            return "sound/step/stone";
        }
    };

    public final BlockSoundType cloth = new BlockSoundType("cloth", 1.0F, 1.0F);

    @NotNull
    public final BlockSoundType sand = new BlockSoundType("sand", 1.0F, 1.0F);

    @NotNull
    public final BlockSoundType snow = new BlockSoundType("snow", 1.0F, 1.0F);

    @NotNull
    public final BlockSoundType ladder = new BlockSoundType("ladder", 1.0F, 1.0F) {

        @NotNull
        public String getBreakSoundResourceStartString() {
            return "sound/dig/wood";
        }
    };

    @NotNull
    public final BlockSoundType anvil = new BlockSoundType("anvil", 0.3F, 1.0F) {

        @NotNull
        public String getBreakSoundResourceStartString() {
            return "sound/dig/stone";
        }

        @NotNull
        public String getPlaceSoundResourceStartString() {
            return "sound/random/anvil_land";
        }
    };

    @NotNull
    public final BlockSoundType slime = new BlockSoundType("slime", 1.0F, 1.0F) {

        @NotNull
        public String getBreakSoundResourceStartString() {
            return "sound/mob/slime/big";
        }

        @NotNull
        public String getPlaceSoundResourceStartString() {
            return "sound/mob/slime/big";
        }

        @NotNull
        public String getStepSoundResourceStartString() {
            return "sound/mob/slime/small";
        }

    };

    /**
     * The map connecting the block instance with it's parent block sound type
     */
    @NotNull
    private final HashMap<Block, BlockSoundType> blockSoundsMap = new HashMap<>();

    /**
     * Initializes the block sounds map
     */
    public void init(@NotNull Blocks blocks) {
        List<BlockSoundType> blockSoundTypes = Arrays.asList(stone, wood, gravel, grass, metal, glass, cloth, sand, snow, ladder, anvil, slime);
        for (Block block : blocks.getBlocks()) {
            registerBlockSoundType(block, blockSoundTypes.stream().
                    filter(t -> t.getSoundName().equals(block.getSoundType())).
                    findFirst().
                    orElseThrow(() -> new IllegalStateException("Unknown block sound type: \"" + block.getSoundType() + "\"")));
        }
    }

    /**
     * Registers the block sound type in the block sounds map
     *
     * @param block          the block
     * @param blockSoundType the parent sound type of the block
     */
    private void registerBlockSoundType(@NotNull Block block, @NotNull BlockSoundType blockSoundType) {
        this.blockSoundsMap.put(block, blockSoundType);
    }

    /**
     * @param block the block instance
     * @return the parent block sound type for the specified block
     */
    @NotNull
    public BlockSoundType getSoundType(@NotNull Block block) {
        return Optional.ofNullable(this.blockSoundsMap.getOrDefault(block, null)).orElseThrow(() -> new IllegalStateException("Block " + block.getSoundType() + " does not have any parent sound type!"));
    }

}
