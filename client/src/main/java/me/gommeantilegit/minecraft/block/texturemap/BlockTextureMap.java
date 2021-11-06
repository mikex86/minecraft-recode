package me.gommeantilegit.minecraft.block.texturemap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import kotlin.Pair;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.Side;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.block.ClientBlockRendererTypeRegistry;
import me.gommeantilegit.minecraft.texture.TextureWrapper;
import me.gommeantilegit.minecraft.texture.custom.CustomTexture;
import me.gommeantilegit.minecraft.utils.Pointer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SideOnly(side = Side.CLIENT)
public class BlockTextureMap {

    private static final int TEXTURE_ATLAS_SIZE = 1024;

    /**
     * The Block renderer registry where every block has a parent renderer
     */
    @NotNull
    private final ClientBlockRendererTypeRegistry rendererTypeRegistry;

    /**
     * The width of the image.
     */
    private final int width = TEXTURE_ATLAS_SIZE;

    /**
     * The height of the image
     */
    private final int height = TEXTURE_ATLAS_SIZE;

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
     * Pointer storing the texture wrapper wrapped around the value of {@link #texturePointer}
     */
    @NotNull
    private final Pointer<TextureWrapper> textureWrapper = new Pointer<>();

    @NotNull
    private final Map<String, Vector2> resourcePositions = new HashMap<>();

    /**
     * List of pairs storing the texture resource paths with their parent uv position in the final texture map image
     */
    @NotNull
    private final ArrayList<Pair<Pixmap, Vector2>> textureResources = new ArrayList<>();

    /**
     * Parent minecraft instance
     */
    @NotNull
    private final ClientMinecraft mc;

    public BlockTextureMap(@NotNull ClientMinecraft mc, @NotNull ClientBlockRendererTypeRegistry rendererTypeRegistry) {
        this.mc = mc;
        this.rendererTypeRegistry = rendererTypeRegistry;
    }

    public void setupTextureMap() {
    }

    private int greatestImageHeightOfRow;


    /**
     * Adds the texture to the resources of the map
     *
     * @param textureResource the resource path to the texture to add
     * @return the top left corner where in the final texturePointer map the block added will be.
     */
    @NotNull
    public Vector2 addTexture(@NotNull String textureResource) {
        {
            Vector2 pos;
            if ((pos = this.resourcePositions.get(textureResource)) != null) {
                return pos; // return the position where this texture is already placed
            }
        }
        FileHandle fileHandle = Gdx.files.classpath(textureResource);
        Pixmap img = new Pixmap(fileHandle);
        Vector2 position = new Vector2(u, v);
        if (img.getHeight() > greatestImageHeightOfRow) {
            greatestImageHeightOfRow = img.getHeight();
        }
        if (u + img.getWidth() > width) {
            u = 0;
            v += greatestImageHeightOfRow;
            if (v > height) {
                throw new IllegalArgumentException("Texture atlas too small!");
            }
        } else {
            u += img.getWidth();
        }
        this.textureResources.add(new Pair<>(img, position));
        this.resourcePositions.put(textureResource, position);
        return position;
    }

    public void build() {
        Pixmap img = buildImage();
        PixmapIO.writePNG(Gdx.files.local("debug.png"), img);
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
        for (Pair<Pixmap, Vector2> entry : this.textureResources) {
            Pixmap pixmap = entry.getFirst();
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

    public int getHeight() {
        return height;
    }

}
