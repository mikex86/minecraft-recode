package me.gommeantilegit.minecraft.util.math.vecmath.nbt;

import com.badlogic.gdx.math.Vector2;
import me.gommeantilegit.minecraft.nbt.NBTObject;
import me.gommeantilegit.minecraft.nbt.api.INBTConverter;
import me.gommeantilegit.minecraft.nbt.exception.NBTParsingException;
import me.gommeantilegit.minecraft.nbt.impl.NBTArray;
import me.gommeantilegit.minecraft.nbt.impl.NBTFloat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Vector2NBTConverter implements INBTConverter<NBTArray, Vector2> {

    @NotNull
    public static final Vector2NBTConverter INSTANCE = new Vector2NBTConverter();

    @NotNull
    @Override
    public NBTArray toNBTData(Vector2 object) {
        return new NBTArray(new NBTObject[]{
                new NBTFloat(object.x),
                new NBTFloat(object.y)
        });
    }

    @NotNull
    @Override
    public Vector2 fromNBTData(NBTArray object, Object... args) throws NBTParsingException {
        NBTObject[] objects = object.getValue();
        try {
            NBTFloat x = (NBTFloat) objects[0];
            NBTFloat y = (NBTFloat) objects[1];

            if (x == null)
                throw new NBTParsingException("Vector x component not present in NBTArray");
            if (y == null)
                throw new NBTParsingException("Vector y component not present in NBTArray");
            return new Vector2(x.getValue(), y.getValue());
        } catch (ClassCastException e) {
            throw new NBTParsingException("NBTArray has members of invalid type!");
        }
    }

}
