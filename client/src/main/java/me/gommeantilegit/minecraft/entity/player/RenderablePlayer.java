package me.gommeantilegit.minecraft.entity.player;

import me.gommeantilegit.minecraft.entity.IRenderableEntity;
import me.gommeantilegit.minecraft.entity.player.base.PlayerBase;
import me.gommeantilegit.minecraft.entity.player.skin.ClientSkin;
import me.gommeantilegit.minecraft.entity.renderer.model.IEntityModel;
import me.gommeantilegit.minecraft.entity.renderer.model.impl.PlayerModel;
import me.gommeantilegit.minecraft.shader.api.CommonShader;
import me.gommeantilegit.minecraft.world.WorldBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RenderablePlayer extends PlayerBase implements IRenderableEntity<CommonShader, RenderablePlayer> {

    @NotNull
    private final IEntityModel<CommonShader, RenderablePlayer> model;

    public RenderablePlayer(@Nullable WorldBase world, int maxHealth, @NotNull String username, @NotNull ClientSkin skin) {
        super(world, maxHealth, username, skin);
        this.model = new PlayerModel(skin.getTexture());
    }

    @NotNull
    @Override
    public IEntityModel<CommonShader, RenderablePlayer> getModel() {
        return this.model;
    }
}
