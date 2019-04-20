package me.gommeantilegit.minecraft.entity.player.base.skin;

/**
 * Represents a pixel with R, G, B, A values
 */
public class SkinPixel {

    /**
     * 8 bit color depth R, G, B, A values.
     * Treated as unsigned bytes
     */
    private final byte r, g, b, a;

    public SkinPixel(byte r, byte g, byte b, byte a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public byte getR() {
        return r;
    }

    public byte getG() {
        return g;
    }

    public byte getB() {
        return b;
    }

    public byte getA() {
        return a;
    }
}
