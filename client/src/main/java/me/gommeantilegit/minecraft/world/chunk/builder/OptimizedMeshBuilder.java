package me.gommeantilegit.minecraft.world.chunk.builder;

import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.math.Vector3;
import org.jetbrains.annotations.NotNull;

public class OptimizedMeshBuilder extends MeshBuilder {

    @NotNull
    private final VertexInfo vertTmp1 = new VertexInfo(), vertTmp2 = new VertexInfo(), vertTmp3 = new VertexInfo(), vertTmp4 = new VertexInfo();

    public OptimizedMeshBuilder() {
    }

    @NotNull
    public VertexInfo getVertTmp4() {
        return vertTmp4;
    }

    @NotNull
    public VertexInfo getVertTmp3() {
        return vertTmp3;
    }

    @NotNull
    public VertexInfo getVertTmp2() {
        return vertTmp2;
    }

    @NotNull
    public VertexInfo getVertTmp1() {
        return vertTmp1;
    }
}