package me.gommeantilegit.minecraft.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Documentational Annotation indicating that the method annotated is only used by android.
 */
@Documented
@Target(ElementType.METHOD)
public @interface AndroidOnly {
}
