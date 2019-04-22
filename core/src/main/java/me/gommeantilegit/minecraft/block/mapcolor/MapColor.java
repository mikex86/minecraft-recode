package me.gommeantilegit.minecraft.block.mapcolor;

import org.jetbrains.annotations.NotNull;

public class MapColor {

    /**
     * Holds all the 16 colors used on maps
     */
    @NotNull
    public static final MapColor[] mapColorArray = new MapColor[64];

    @NotNull
    public static final MapColor airColor = new MapColor(0, 0x0);

    @NotNull
    public static final MapColor grassColor = new MapColor(1, 0x7fb238);

    @NotNull
    public static final MapColor sandColor = new MapColor(2, 0xf7e9a3);

    @NotNull
    public static final MapColor clothColor = new MapColor(3, 0xc7c7c7);

    @NotNull
    public static final MapColor tntColor = new MapColor(4, 0xff0000);

    @NotNull
    public static final MapColor iceColor = new MapColor(5, 0xa0a0ff);

    @NotNull
    public static final MapColor ironColor = new MapColor(6, 0xa7a7a7);

    @NotNull
    public static final MapColor foliageColor = new MapColor(7, 0x7c00);

    @NotNull
    public static final MapColor snowColor = new MapColor(8, 0xffffff);

    @NotNull
    public static final MapColor clayColor = new MapColor(9, 0xa4a8b8);

    @NotNull
    public static final MapColor dirtColor = new MapColor(10, 0x976d4d);

    @NotNull
    public static final MapColor stoneColor = new MapColor(11, 0x707070);

    @NotNull
    public static final MapColor waterColor = new MapColor(12, 0x4040ff);

    @NotNull
    public static final MapColor woodColor = new MapColor(13, 0x8f7748);

    @NotNull
    public static final MapColor quartzColor = new MapColor(14, 0xfffcf5);

    @NotNull
    public static final MapColor adobeColor = new MapColor(15, 0xd87f33);

    @NotNull
    public static final MapColor magentaColor = new MapColor(16, 0xb24cd8);

    @NotNull
    public static final MapColor lightBlueColor = new MapColor(17, 0x6699d8);

    @NotNull
    public static final MapColor yellowColor = new MapColor(18, 0xe5e533);

    @NotNull
    public static final MapColor limeColor = new MapColor(19, 0x7fcc19);

    @NotNull
    public static final MapColor pinkColor = new MapColor(20, 0xf27fa5);

    @NotNull
    public static final MapColor grayColor = new MapColor(21, 0x4c4c4c);

    @NotNull
    public static final MapColor silverColor = new MapColor(22, 0x999999);

    @NotNull
    public static final MapColor cyanColor = new MapColor(23, 0x4c7f99);

    @NotNull
    public static final MapColor purpleColor = new MapColor(24, 0x7f3fb2);

    @NotNull
    public static final MapColor blueColor = new MapColor(25, 0x334cb2);

    @NotNull
    public static final MapColor brownColor = new MapColor(26, 0x664c33);

    @NotNull
    public static final MapColor greenColor = new MapColor(27, 0x667f33);

    @NotNull
    public static final MapColor redColor = new MapColor(28, 0x993333);

    @NotNull
    public static final MapColor blackColor = new MapColor(29, 0x191919);

    @NotNull
    public static final MapColor goldColor = new MapColor(30, 0xfaee4d);

    @NotNull
    public static final MapColor diamondColor = new MapColor(31, 0x5cdbd5);

    @NotNull
    public static final MapColor lapisColor = new MapColor(32, 0x4a80ff);

    @NotNull
    public static final MapColor emeraldColor = new MapColor(33, 0xd93a);

    @NotNull
    public static final MapColor obsidianColor = new MapColor(34, 0x815631);

    @NotNull
    public static final MapColor netherrackColor = new MapColor(35, 0x700200);

    /**
     * Holds the color in RGB value that will be rendered on maps.
     */
    public int colorValue;

    /**
     * Holds the index of the color used on map.
     */
    public final int colorIndex;

    public MapColor(int mapColorID, int color) {
        if (mapColorID >= 0 && mapColorID <= 63) {
            this.colorIndex = mapColorID;
            this.colorValue = color;
            mapColorArray[mapColorID] = this;
        } else {
            throw new IndexOutOfBoundsException("Map colour ID must be between 0 and 63 (inclusive)");
        }
    }

    /**
     * @param brightnessIndex a brightness index to offset the brightness of the color returned
     * @return the color for the material adjusted in brightness to this brightness index
     */
    public int getMapColor(int brightnessIndex) {
        short brightness = 220;
        switch (brightnessIndex) {
            case 3:
                brightness = 135;
                break;
            case 2:
                brightness = 255;
                break;
            case 0:
                brightness = 180;
                break;
        }
        int i = (this.colorValue >> 16 & 255) * brightness / 255;
        int j = (this.colorValue >> 8 & 255) * brightness / 255;
        int k = (this.colorValue & 255) * brightness / 255;
        return -16777216 | i << 16 | j << 8 | k;
    }
}
