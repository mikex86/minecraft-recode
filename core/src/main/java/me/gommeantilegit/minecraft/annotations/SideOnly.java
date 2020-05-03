package me.gommeantilegit.minecraft.annotations;

import me.gommeantilegit.minecraft.Side;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that a given piece of code is only for a specified side of the minecraft implementation
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
public @interface SideOnly {
    /**
     * The side that the annotated code is exclusive for
     */
    Side side();
}
