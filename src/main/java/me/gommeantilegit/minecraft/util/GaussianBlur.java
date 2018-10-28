package me.gommeantilegit.minecraft.util;

public class GaussianBlur {
    /**
     * Blurs the given data using Gaussian Blur
     *
     * @param data       the given input data which should be blurred
     * @param blurRadius number of pixels also using element n of the data for blurring. Increase for better results but hits CPU hard
     * @return the blurred output.
     */
    public int[][] blurData(int[][] data, double blurRadius) {
        int[][] output = new int[data.length][data[0].length];
        int width = data.length;
        int height = data[0].length;
        double rs = Math.ceil(blurRadius * 2.57);     // significant radius
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++) {
                double val = 0, wsum = 0;
                for (double iy = i - rs; iy < i + rs + 1; iy++)
                    for (double ix = j - rs; ix < j + rs + 1; ix++) {
                        double x = Math.min(width - 1, Math.max(0, ix));
                        double y = Math.min(height - 1, Math.max(0, iy));
                        double dsq = (ix - j) * (ix - j) + (iy - i) * (iy - i);
                        double wght = Math.exp(-dsq / (2 * blurRadius * blurRadius)) / (Math.PI * 2 * blurRadius * blurRadius);
                        val += data[(int) x][(int) y] * wght;
                        wsum += wght;
                    }
                output[j][i] = Math.round((float) val / (float) wsum);
            }
        return output;
    }
}
