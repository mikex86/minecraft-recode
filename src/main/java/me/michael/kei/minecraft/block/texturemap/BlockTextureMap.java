package me.michael.kei.minecraft.block.texturemap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.sun.istack.internal.NotNull;
import me.michael.kei.minecraft.annotations.IOAccess;

import java.util.HashMap;
import java.util.Map;

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
     * GDX Texture with the final map.
     * Is null until {@link #build()} is called.
     */
    private Texture texture;

    @NotNull
    private final HashMap<Pixmap, Vector2> textureResources = new HashMap<>();

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
     * @param blockTexture the given classpath resource
     * @return the top left corner where in the final texture map the block added will be.
     */
    @IOAccess
    @NotNull
    public Vector2 addTexuture(@NotNull String blockTexture) {
        Pixmap img = new Pixmap(Gdx.files.classpath(blockTexture));

        Vector2 position = new Vector2(u, v);
        if (img.getHeight() > greatestImageHeightOfRow) {
            greatestImageHeightOfRow = img.getHeight();
        }
        if (u + img.getWidth() > maxWidth) {
            u = 0;
            v += greatestImageHeightOfRow;
        } else
            u += img.getWidth();
        this.textureResources.put(img, position);
        return position;
    }

    public void build() {
        Pixmap img = buildImage();
        this.width = img.getWidth();
        this.height = img.getHeight();
        this.texture = new Texture(img);
    }

    private Pixmap buildImage() {
        int imgHeight = this.v + greatestImageHeightOfRow;
        Pixmap img = new Pixmap(this.u, imgHeight, Pixmap.Format.RGBA8888);
        for (Map.Entry<Pixmap, Vector2> entry : this.textureResources.entrySet()) {
            Pixmap pixmap = entry.getKey();
            Vector2 pos = entry.getValue();
            img.drawPixmap(pixmap, (int) pos.x, (int) pos.y);
        }
        return img;
    }


    public Texture getTexture() {
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
