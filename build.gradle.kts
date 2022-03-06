plugins {
    `java-library`
    kotlin("jvm") version "1.6.10"
}

group = "me.gommeantilegit.minecraft"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit", "junit", "4.12")
    implementation("org.jetbrains:annotations:22.0.0")
}