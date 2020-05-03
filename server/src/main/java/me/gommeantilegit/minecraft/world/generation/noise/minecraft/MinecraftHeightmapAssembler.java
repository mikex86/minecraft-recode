package me.gommeantilegit.minecraft.world.generation.noise.minecraft;

public class MinecraftHeightmapAssembler {

    public static double[] assembleHeightmap(int width, int height, int depth, double[] temperature, double[] humidity, double[] noise1, double[] noise2, double[] noise3, double[] noise4, double[] noise5) {
        double[] heightmapFlat = new double[width * height * depth];
        int bufferIndex1 = 0;
        int bufferIndex2 = 0;
        int xChunkUnit = 16 / width;
        for (int xOffset = 0; xOffset < width; xOffset++) {
            int xPos = xOffset * xChunkUnit + xChunkUnit / 2;
            for (int zOffset = 0; zOffset < depth; zOffset++) {
                int zPos = zOffset * xChunkUnit + xChunkUnit / 2;
                double temp = temperature[xPos * 16 + zPos];
                double humitityVal = humidity[xPos * 16 + zPos] * temp;
                double invHumidityVal = 1.0D - humitityVal;
                invHumidityVal *= invHumidityVal;
                invHumidityVal *= invHumidityVal;
                invHumidityVal = 1.0D - invHumidityVal;
                double noisedHumidity = (noise1[bufferIndex2] + 256D) / 512D;
                noisedHumidity *= invHumidityVal;
                if (noisedHumidity > 1.0D) {
                    noisedHumidity = 1.0D;
                }
                double noise = noise2[bufferIndex2] / 8000D;
                if (noise < 0.0D) {
                    noise = -noise * 0.29999999999999999D;
                }
                noise = noise * 3D - 2D;
                if (noise < 0.0D) {
                    noise /= 2D;
                    if (noise < -1D) {
                        noise = -1D;
                    }
                    noise /= 1.3999999999999999D;
                    noise /= 2D;
                    noisedHumidity = 0.0D;
                } else {
                    if (noise > 1.0D) {
                        noise = 1.0D;
                    }
                    noise /= 8D;
                }
                if (noisedHumidity < 0.0D) {
                    noisedHumidity = 0.0D;
                }
                noisedHumidity += 0.5D;
                noise = (noise * (double) height) / 16D;
                double yNoise = (double) height / 2D + noise * 4D;
                bufferIndex2++;
                for (int yOffset = 0; yOffset < height; yOffset++) {
                    double yFinalNoise = 0.0D;
                    double yPos = (((double) yOffset - yNoise) * 12D) / noisedHumidity;
                    if (yPos < 0.0D) {
                        yPos *= 4D;
                    }
                    double noise1Val = noise4[bufferIndex1] / 512D;
                    double noise2Val = noise5[bufferIndex1] / 512D;
                    double noise3Val = (noise3[bufferIndex1] / 10D + 1.0D) / 2D;
                    if (noise3Val < 0.0D) {
                        yFinalNoise = noise1Val;
                    } else if (noise3Val > 1.0D) {
                        yFinalNoise = noise2Val;
                    } else {
                        yFinalNoise = noise1Val + (noise2Val - noise1Val) * noise3Val;
                    }
                    yFinalNoise -= yPos;
                    if (yOffset > height - 4) {
                        double d13 = (float) (yOffset - (height - 4)) / 3F;
                        yFinalNoise = yFinalNoise * (1.0D - d13) + -10D * d13;
                    }
                    heightmapFlat[bufferIndex1] = yFinalNoise;
                    bufferIndex1++;
                }
            }
        }
        return heightmapFlat;
    }

}
