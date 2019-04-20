package me.gommeantilegit.minecraft;

import org.jetbrains.annotations.NotNull;

/**
 * Side of the Minecraft implementation
 */
public enum Side {
    CLIENT, SERVER;

    @NotNull
    public static Side getSide(Class<? extends AbstractMinecraft> minecraftClass) {
        if (minecraftClass.getName().equals("me.gommeantilegit.minecraft.ClientMinecraft")) {
            return CLIENT;
        } else {
            return SERVER;
        }
    }

//    /**
//     * Checks if the side annotated in the {@link me.gommeantilegit.minecraft.annotations.SideOnly} annotation of the class that called the method is equal to the side of implementation of the minecraft instance
//     * Calls {@link #assertSide(Side)}
//     */
//    public static void checkSide() {
//        try {
//            assertSide(AbstractMinecraft.class.getClassLoader().loadClass(Thread.currentThread().getStackTrace()[1].getClassName()).getAnnotation(SideOnly.class).side());
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
}
