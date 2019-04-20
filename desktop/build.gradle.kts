plugins {
    java
    kotlin("jvm")
}

group = "me.gommeantilegit.minecraft"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compile("com.badlogicgames.gdx:gdx-platform:1.9.8")
    compile("com.badlogicgames.gdx:gdx-backend-lwjgl3:1.9.8")
    compile(files("lib/natives.jar"))
    compile(project(":client"))
}
