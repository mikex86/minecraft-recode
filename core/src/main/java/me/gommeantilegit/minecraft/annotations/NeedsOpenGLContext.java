package me.gommeantilegit.minecraft.annotations;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface NeedsOpenGLContext {
}
