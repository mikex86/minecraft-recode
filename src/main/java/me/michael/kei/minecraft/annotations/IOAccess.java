package me.michael.kei.minecraft.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Documentational Annotation indicating that the method annotated performs IO Operations
 */
@Target(ElementType.METHOD)
public @interface IOAccess {
}
