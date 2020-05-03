package me.gommeantilegit.minecraft.annotations;

import java.lang.annotation.*;

/**
 * Indicates that a method is thread safe
 */
@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface ThreadSafe {
}
