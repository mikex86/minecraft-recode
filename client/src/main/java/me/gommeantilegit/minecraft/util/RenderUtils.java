package me.gommeantilegit.minecraft.util;

import me.gommeantilegit.minecraft.world.chunk.builder.OptimizedMeshBuilder;

public class RenderUtils {

    public static void rect(OptimizedMeshBuilder builder,
                            float x00, float y00, float z00,
                            float u00, float v00,
                            float x10, float y10, float z10,
                            float u10, float v10,
                            float x11, float y11, float z11,
                            float u11, float v11,
                            float x01, float y01, float z01,
                            float u01, float v01,
                            float normalX, float normalY, float normalZ, float r, float g, float b, float a) {

        builder.rect(
                builder.getVertTmp1().setPos(x00, y00, z00).setNor(normalX, normalY, normalZ).setUV(u00, v00).setCol(r, g, b, a),
                builder.getVertTmp2().setPos(x10, y10, z10).setNor(normalX, normalY, normalZ).setUV(u10, v10).setCol(r, g, b, a),
                builder.getVertTmp3().setPos(x11, y11, z11).setNor(normalX, normalY, normalZ).setUV(u11, v11).setCol(r, g, b, a),
                builder.getVertTmp4().setPos(x01, y01, z01).setNor(normalX, normalY, normalZ).setUV(u01, v01).setCol(r, g, b, a)
        );
    }

}
