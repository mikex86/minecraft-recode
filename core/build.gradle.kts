plugins {
    java
    kotlin("jvm")
}

group = "me.gommeantilegit.minecraft"
version = "1.0-SNAPSHOT"

val libDir = "lib"

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    testCompile("junit", "junit", "4.12")

    if (rootProject.name == "MinecraftLibGDX") {
        //LibGDX
        compile("com.badlogicgames.gdx:gdx-platform:1.9.8")
        compile("com.badlogicgames.gdx:gdx-backend-android:1.9.8")
        compile("com.badlogicgames.gdx:gdx:1.9.8")
        compile("com.badlogicgames.gdx:gdx-freetype:1.9.8")
        compile("com.badlogicgames.gdx:gdx-backend-android:1.9.8")
    }
}