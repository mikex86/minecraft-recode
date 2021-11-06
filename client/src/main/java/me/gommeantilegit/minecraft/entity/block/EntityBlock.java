package me.gommeantilegit.minecraft.entity.block;

import com.badlogic.gdx.graphics.Mesh;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.block.BlockTypeRenderer;
import me.gommeantilegit.minecraft.block.ClientBlockRendererTypeRegistry;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.entity.IRenderableEntity;
import me.gommeantilegit.minecraft.entity.mesh.MeshBuildingEntity;
import me.gommeantilegit.minecraft.entity.renderer.model.IEntityModel;
import me.gommeantilegit.minecraft.shader.api.CommonShader;
import me.gommeantilegit.minecraft.texture.custom.CustomTexture;
import me.gommeantilegit.minecraft.world.WorldBase;
import me.gommeantilegit.minecraft.world.chunk.builder.OptimizedMeshBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class EntityBlock extends MeshBuildingEntity implements IRenderableEntity<CommonShader, EntityBlock> {

    @NotNull
    private static final IEntityModel<CommonShader, EntityBlock> ENTITY_BLOCK_MODEL = new EntityBlockModel();

    @NotNull
    private final BlockTypeRenderer blockRenderer;

    @NotNull
    private final IBlockState displayBlockState;

    /**
     * The meshBuilder used to create the mesh of the entity
     */
    @Nullable
    private OptimizedMeshBuilder meshBuilder = null;

    /**
     * The mesh of the block entity to render
     */
    @Nullable
    private Mesh mesh = null;

    @NotNull
    private final CustomTexture texture;

    /**
     * @param world sets {@link #world}
     * @param texture
     */
    public EntityBlock(@NotNull WorldBase world, @NotNull IBlockState displayBlockState, @NotNull CustomTexture texture) {
        super(world);
        this.displayBlockState = displayBlockState;
        this.texture = texture;
        ClientBlockRendererTypeRegistry renderRegistry = ((ClientMinecraft) world.mc).blockRendererRegistry;
        Block displayBlock = displayBlockState.getBlock();
        this.blockRenderer = Objects.requireNonNull(renderRegistry.getRenderer(displayBlock), "Failed to retrieve block renderer for block type: " + displayBlock.getUnlocalizedName());
    }

    @NotNull
    @Override
    public OptimizedMeshBuilder buildMesh() {
        OptimizedMeshBuilder meshBuilder = new OptimizedMeshBuilder();
        this.blockRenderer.render(meshBuilder, 0, 0, 0, null, this.displayBlockState);
        return meshBuilder;
    }

    @Override
    public void storeBuildMesh(@NotNull OptimizedMeshBuilder meshBuilder) {
        this.meshBuilder = meshBuilder;
    }

    @Override
    public void finishMesh(@NotNull OptimizedMeshBuilder meshBuilder) {
        this.mesh = meshBuilder.end();
    }

    @Override
    public void finishMesh() {
        assert this.meshBuilder != null;
        finishMesh(this.meshBuilder);
        this.meshBuilder = null;
    }

    @Override
    public @NotNull IEntityModel<CommonShader, EntityBlock> getModel() {
        return ENTITY_BLOCK_MODEL;
    }

    @NotNull
    public CustomTexture getTexture() {
        return texture;
    }

    @NotNull
    public Mesh getMesh() {
        return Objects.requireNonNull(this.mesh, "EntityBlock Mesh not yet initialized");
    }
}
