package me.gommeantilegit.minecraft.block.texturemap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;

import kotlin.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.gommeantilegit.minecraft.annotations.IOAccess;
import me.gommeantilegit.minecraft.texture.custom.CustomTexture;

public class BlockTextureMap {


    /**
     * Maximal width of a row in pixel
     */
    private final int maxWidth;

    /**
     * The actual width of the image.
     */
    private int width;

    /**
     * The actual height of the image
     */
    private int height;

    /**
     * Counter UV values
     *
     * @see #addTexuture(String)
     */
    private int u, v;

    /**
     * GDX CustomTexture with the final map.
     * Is null until {@link #build()} is called.
     */
    private CustomTexture texture;

    @NotNull
    private final ArrayList<Pair<String, Vector2>> textureResources = new ArrayList<>();

    /**
     * @param maxWidth sets {@link #maxWidth}
     */
    public BlockTextureMap(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    private int greatestImageHeightOfRow;

    /**
     * adds the resource path to the resources of the map
     *
     * @param blockCustomTexture the given classpath resource
     * @return the top left corner where in the final texture map the block added will be.
     */
    @IOAccess
    @NotNull
    public Vector2 addTexuture(@NotNull String blockCustomTexture) {
        Pixmap img = new Pixmap(Gdx.files.classpath(blockCustomTexture));

        Vector2 position = new Vector2(u, v);
        if (img.getHeight() > greatestImageHeightOfRow) {
            greatestImageHeightOfRow = img.getHeight();
        }
        if (u + img.getWidth() > maxWidth) {
            u = 0;
            v += greatestImageHeightOfRow;
        } else
            u += img.getWidth();
        this.textureResources.add(new Pair<>(blockCustomTexture, position));
        return position;
    }

    public void build() {
        Pixmap img = buildImage();
        this.width = img.getWidth();
        this.height = img.getHeight();
        this.texture = new CustomTexture(img);
    }


    /**
     * Generates a new PixMap containing all block textures in an atlas
     *
     * @return the generated pixmap
     */
    private Pixmap buildImage() {
        int imgHeight = this.v + greatestImageHeightOfRow;
        Pixmap img = new Pixmap(this.u, imgHeight, Pixmap.Format.RGBA8888);
        for (Pair<String, Vector2> entry : this.textureResources) {
            Pixmap pixmap = new Pixmap(Gdx.files.classpath(entry.getFirst()));
            Vector2 pos = entry.getSecond();
            img.drawPixmap(pixmap, (int) pos.x, (int) pos.y);
        }
        return img;
    }

    /**
     * Re-uploads the OpenGL texture without regenerating the atlas
     */
    public void reload() {
        this.texture = new CustomTexture(buildImage());
    }

    public CustomTexture getTexture() {
        return texture;
    }

    public int getWidth() {
        return width;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public int getHeight() {
        return height;
    }

}
