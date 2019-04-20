package me.gommeantilegit.minecraft.texture.texturewrappers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import org.jetbrains.annotations.NotNull;

import me.gommeantilegit.minecraft.texture.TextureWrapper;

/**
 * Texture wrapper for "textures/gui/gui.png"
 */
public class GuiTextures extends TextureWrapper {

    @NotNull
    public final RenderData mobileForwardButton = new TextureWrapper.RenderData(
            new int[]{
                    0, 108
            },
            new int[]{
                    27, 27
            }
    );

    @NotNull
    public final RenderData mobileLeftButton = new TextureWrapper.RenderData(
            new int[]{
                    25, 108
            },
            new int[]{
                    27, 27
            }
    );

    @NotNull
    public final RenderData mobileBackwardsButton = new TextureWrapper.RenderData(
            new int[]{
                    50, 108
            },
            new int[]{
                    27, 27
            }
    );

    @NotNull
    public final RenderData mobileRightButton = new TextureWrapper.RenderData(
            new int[]{
                    76, 108
            },
            new int[]{
                    27, 27
            }
    );

    @NotNull
    public final RenderData mobileJumpButton = new TextureWrapper.RenderData(
            new int[]{
                    101, 133
            },
            new int[]{
                    27, 27
            }
    );

//    /**
//     * The texture render data that is used for the gui button when it is disabled.
//     * The button is grayed out.
//     * @see me.gommeantilegit.minecraft.ui.button.GuiButton#disabled
//     */
//    @NotNull
//    public final RenderData guiButtonDisabled = new TextureWrapper.RenderData(
//            new int[]{
//                    0, 46
//            },
//            new int[]{
//                    200, 20
//            }
//    );
//
//    /**
//     * The texture render data that is used for the gui button when it is not hovered by the cursor.
//     */
//    @NotNull
//    public final RenderData guiButtonNormal = new TextureWrapper.RenderData(
//            new int[]{
//                    0, 46 + 20
//            },
//            new int[]{
//                    200, 20
//            }
//    );
//
//    /**
//     * The texture render data that is used for the gui button when it is hovered by the cursor.
//     */
//    @NotNull
//    public final RenderData guiButtonHovered = new TextureWrapper.RenderData(
//            new int[]{
//                    0, 46 + 40
//            },
//            new int[]{
//                    200, 20
//            }
//    );

    public GuiTextures(@NotNull SpriteBatch spriteBatch) {
        super("textures/gui/gui.png", spriteBatch);
    }

}
