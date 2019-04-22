package me.gommeantilegit.minecraft.block.texturemap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import kotlin.Pair;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.Side;
import me.gommeantilegit.minecraft.annotations.IOAccess;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.block.BlockTypeRenderer;
import me.gommeantilegit.minecraft.block.ClientBlockRendererTypeRegistry;
import me.gommeantilegit.minecraft.texture.TextureWrapper;
import me.gommeantilegit.minecraft.texture.custom.CustomTexture;
import me.gommeantilegit.minecraft.utils.Pointer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@SideOnly(side = Side.CLIENT)
public class BlockTextureMap {

    /**
     * Maximal width of a row in pixel
     */
    private final int maxWidth;

    /**
     * The Block renderer registry where every block has a parent renderer
     */
    @NotNull
    private final ClientBlockRendererTypeRegistry rendererTypeRegistry;

    /**
     * The actual width of the image.
     */
    private int width;

    /**
     * The actual height of the image
     */
    private int height;

    /**
     * Counter UV SOUND_RESOURCES
     *
     * @see #addTexture(String)
     */
    private int u, v;

    /**
     * GDX CustomTexture pointer with the final map.
     * Stores null until {@link #build()} is called.
     */
    @NotNull
    private final Pointer<CustomTexture> texturePointer = new Pointer<>();

    /**
     * Pointer storing the the texture wrapper wrapped around the value of {@link #texturePointer}
     */
    @NotNull
    private final Pointer<TextureWrapper> textureWrapper = new Pointer<>();

    /**
     * List of pairs storing the texture resource paths with their parent uv position in the final texture map image
     */
    @NotNull
    private final ArrayList<Pair<String, Vector2>> textureResources = new ArrayList<>();

    /**
     * Parent minecraft instance
     */
    @NotNull
    private final ClientMinecraft mc;

    /**
     * @param maxWidth sets {@link #maxWidth}
     */
    public BlockTextureMap(int maxWidth, @NotNull ClientMinecraft mc, @NotNull ClientBlockRendererTypeRegistry rendererTypeRegistry) {
        this.maxWidth = maxWidth;
        this.mc = mc;
        this.rendererTypeRegistry = rendererTypeRegistry;
    }

    /**
     * Sets up the texture map by placing every block texture on the texture map
     */
    public void setupTextureMap() {
        for (BlockTypeRenderer value : rendererTypeRegistry.getRendererRegistry().values()) {
            for (String textureResource : value.getTextureResources()) {
                addTexture("textures/blocks/" + textureResource + ".png");
            }
        }
    }

    private int greatestImageHeightOfRow;

    /**
     * adds the resource path to the resources of the map
     *
     * @param blockCustomTexture the given classpath resource
     * @return the top left corner where in the final texturePointer map the block added will be.
     */
    @IOAccess
    @NotNull
    public Vector2 addTexture(@NotNull String blockCustomTexture) {
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
        this.texturePointer.value = new CustomTexture(img);
        this.texturePointer.value.setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.MirroredRepeat); // Repeating texture mirrored to prevent texture floating point rounding errors leading to white pixels
        this.textureWrapper.value = new TextureWrapper(this.texturePointer.value, this.mc.spriteBatch);
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
     * Re-uploads the OpenGL texturePointer without regenerating the atlas
     */
    public void reload() {
        this.texturePointer.value = new CustomTexture(buildImage());
    }

    public CustomTexture getTexture() {
        return texturePointer.value;
    }

    public TextureWrapper getTextureWrapper() {
        return this.textureWrapper.value;
    }

    public Pointer<CustomTexture> getTexturePointer() {
        return texturePointer;
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
