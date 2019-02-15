package me.gommeantilegit.minecraft.texture.custom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;

import java.util.ArrayList;

public class CustomTexture extends Texture {

    private static final ArrayList<CustomTexture> TEXTURES = new ArrayList<>();

    public CustomTexture(String internalPath) {
        super(internalPath);
    }

    public CustomTexture(FileHandle file) {
        super(file);
    }

    public CustomTexture(FileHandle file, boolean useMipMaps) {
        super(file, useMipMaps);
    }

    public CustomTexture(FileHandle file, Pixmap.Format format, boolean useMipMaps) {
        super(file, format, useMipMaps);
    }

    public CustomTexture(Pixmap pixmap) {
        super(pixmap);
    }

    public CustomTexture(Pixmap pixmap, boolean useMipMaps) {
        super(pixmap, useMipMaps);
    }

    public CustomTexture(Pixmap pixmap, Pixmap.Format format, boolean useMipMaps) {
        super(pixmap, format, useMipMaps);
    }

    public CustomTexture(int width, int height, Pixmap.Format format) {
        super(width, height, format);
    }

    public CustomTexture(TextureData data) {
        super(data);
    }

    protected CustomTexture(int glTarget, int glHandle, TextureData data) {
        super(glTarget, glHandle, data);
    }

    public static void recreateTextures() {
        for (CustomTexture texture : TEXTURES) {
            texture.recreate();
        }
    }

    @Override
    public void load(TextureData data) {
        super.load(data);
        if (!TEXTURES.contains(this))
            TEXTURES.add(this);
    }

    @Override
    public void bind(int unit) {
        super.bind(unit);
    }

    @Override
    public void bind() {
        super.bind();
    }

    public void recreate() {
        this.glHandle = Gdx.gl.glGenTexture();
        this.load(getTextureData());
    }

}
