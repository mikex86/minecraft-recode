plugins {
    `java-library`
    kotlin("jvm")
}

group = "me.gommeantilegit.minecraft"
version = "1.0-SNAPSHOT"

val libDir = "lib"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.eclipse.collections:eclipse-collections:10.4.0")
    testImplementation("junit", "junit", "4.12")
    implementation("org.jetbrains:annotations:22.0.0")
    implementation(project(":logging"))
    implementation(project(":utils"))
    implementation("com.badlogicgames.gdx:gdx:1.11.0")
}