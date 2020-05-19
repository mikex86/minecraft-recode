package me.gommeantilegit.minecraft.block.state.palette;

import me.gommeantilegit.minecraft.annotations.ThreadSafe;
import me.gommeantilegit.minecraft.block.Blocks;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages and caches access to palette version specific block state lists
 */
public class PaletteVersionManager {

    @NotNull
    private final Blocks blocks;

    @NotNull
    private final Map<Integer, SoftReference<List<IBlockState>>> cache = new HashMap<>();

    public PaletteVersionManager(@NotNull Blocks blocks) {
        this.blocks = blocks;
    }

    @NotNull
    @ThreadSafe
    public synchronized List<IBlockState> get(int paletteVersion) {
        SoftReference<List<IBlockState>> reference = this.cache.get(paletteVersion);
        List<IBlockState> blockStates = reference == null ? null : reference.get();
        if (blockStates == null) {
            blockStates = searchPossibleBlockStates(paletteVersion);
            this.cache.putIfAbsent(paletteVersion, new SoftReference<>(blockStates));
        }
        return blockStates;
    }

    @NotNull
    private List<IBlockState> searchPossibleBlockStates(int paletteVersion) {
        List<IBlockState> possibleBlockStates = this.blocks.getPossibleBlockStates();
        List<IBlockState> filtered = new ArrayList<>((int) (possibleBlockStates.size() * 0.75f));
        for (IBlockState blockState : possibleBlockStates) {
            if (blockState.getBlock().getInitialVersion() <= paletteVersion)
                filtered.add(blockState);
        }
        return filtered;
    }
}
