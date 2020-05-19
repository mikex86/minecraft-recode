package me.gommeantilegit.minecraft.gmcdata;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field to be serialized by the {@link GMC} serializer if an object of the containing class is serialized
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SerializedProperty {

    @NotNull
    String value();

}
