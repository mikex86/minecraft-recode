plugins {
    `java-library`
    kotlin("jvm")
}

group = "me.gommeantilegit.minecraft"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation("junit", "junit", "4.12")
    if (rootProject.name == "MinecraftLibGDX") {
        //LibGDX for android
        implementation("com.badlogicgames.gdx:gdx-platform:1.9.8")
        implementation("com.badlogicgames.gdx:gdx-backend-android:1.9.8")
        implementation("com.badlogicgames.gdx:gdx:1.9.8")
        implementation("com.badlogicgames.gdx:gdx-freetype:1.9.8")
        implementation("com.badlogicgames.gdx:gdx-backend-android:1.9.8")
    } else {
        // LibGDX for PC
        implementation("com.badlogicgames.gdx:gdx:1.9.8")
        implementation("com.badlogicgames.gdx:gdx-freetype:1.9.8")
        implementation("com.badlogicgames.gdx:gdx-backend-android:1.9.8")
    }
    implementation(project(":core"))
    implementation(project(":logging"))
    implementation(project(":utils"))
    implementation(project(":networking"))
    implementation("org.jetbrains:annotations:19.0.0")
    implementation("io.netty:netty-all:5.0.0.Alpha2")
}