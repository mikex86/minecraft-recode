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
    implementation(kotlin("stdlib-jdk8"))
    testCompile("junit", "junit", "4.12")
    if (rootProject.name == "MinecraftLibGDX") {
        //LibGDX for android
        compile("com.badlogicgames.gdx:gdx-platform:1.9.8")
        compile("com.badlogicgames.gdx:gdx-backend-android:1.9.8")
        compile("com.badlogicgames.gdx:gdx:1.9.8")
        compile("com.badlogicgames.gdx:gdx-freetype:1.9.8")
        compile("com.badlogicgames.gdx:gdx-backend-android:1.9.8")
    } else {
        // LibGDX for PC
        compile("com.badlogicgames.gdx:gdx:1.9.8")
        compile("com.badlogicgames.gdx:gdx-freetype:1.9.8")
        compile("com.badlogicgames.gdx:gdx-backend-android:1.9.8")
    }
    compile(project(":core"))
    compile(project(":utils"))
    compile(project(":networking"))

}