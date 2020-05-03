package me.gommeantilegit.minecraft.ui.screen.impl;

import me.gommeantilegit.minecraft.hud.scaling.DPI;
import me.gommeantilegit.minecraft.ui.button.GuiButton;
import me.gommeantilegit.minecraft.ui.button.GuiTextField;
import me.gommeantilegit.minecraft.ui.screen.GuiScreen;
import org.jetbrains.annotations.NotNull;

public class GuiMultiplayer extends GuiScreen {

    /**
     * The title string ("Play Multiplayer")
     */
    @NotNull
    private final String titleString = "Play Multiplayer";

    /**
     * Multiplayer info strings
     */
    @NotNull
    private final String[] infoStrings = {
            "Minecraft Multiplayer is currently not finished, but there",
            "is some buggy early testing going on.",
            "Enter the IP of a server to connect to it:"
    };

    /**
     * Font String width of {@link #titleString}
     */
    private int titleStringWidth;

    /**
     * The GuiMainMenu instance that was the previous guiScreen
     */
    @NotNull
    private final GuiMainMenu mainMenu;

    public GuiMultiplayer(@NotNull GuiMainMenu mainMenu) {
        this.mainMenu = mainMenu;
    }

    @Override
    public void initGui(int scaledWidth, int scaledHeight) {
        super.initGui(scaledWidth, scaledHeight);
        GuiButton connectButton = new GuiButton("Connect", scaledWidth / 2 - 100, scaledHeight / 4 + 96 + 12, mc);
        GuiTextField serverIPTextField = new GuiTextField("localhost", 200, 20, scaledWidth / 2 - 100, (scaledHeight / 4 - 10) + 50 + 18, mc) {
            @Override
            public boolean keyTyped(char character) {
                boolean state = super.keyTyped(character);
                connectButton.setDisabled(this.stringBuilder.length() == 0);
                return state;
            }
        }.setMaxLength(32);
        connectButton.setOnMouseDownListener(p -> mc.uiManager.displayGuiScreen(new GuiConnecting(serverIPTextField.getTypedText().split(":"))));
        this.buttons.add(connectButton);
        this.buttons.add(new GuiButton("Cancel", scaledWidth / 2 - 100, scaledHeight / 4 + 120 + 12, mc).setOnMouseDownListener(pointer -> mc.uiManager.displayGuiScreen(mainMenu)));
        this.buttons.add(serverIPTextField);
        this.titleStringWidth = mc.uiManager.fontRenderer.getStringWidth(titleString);
    }

    @Override
    public void render() {
        drawDefaultBackground();
        for (int i = 0; i < this.infoStrings.length; i++) {
            mc.uiManager.fontRenderer.drawStringWithShadow(this.infoStrings[i], DPI.scaledWidthi / 2f - 140, (DPI.scaledHeighti / 4 - 60) + 60 + (i != 2 ? i * 9 : 36), 0xa0a0a0);
        }
        //noinspection IntegerDivisionInFloatingPointContext
        mc.uiManager.fontRenderer.drawStringWithShadow(titleString, DPI.scaledWidthi / 2 - titleStringWidth / 2, DPI.scaledHeighti / 4 - 60 + 20, 0xffffff);
        super.render();
    }
}
