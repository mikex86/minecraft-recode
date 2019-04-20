package me.gommeantilegit.minecraft.rendering;

import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import org.jetbrains.annotations.NotNull;

public interface Constants {

    @NotNull
    VertexAttributes STD_VERTEX_ATTRIBUTES = new VertexAttributes(

            new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_Position"),
            new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, "a_Color"),
            new VertexAttribute(VertexAttributes.Usage.Normal, 3, "a_Normal"),
            new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_TextureCoord")

    );

}
