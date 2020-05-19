plugins {
    `java-library`
}

group = "me.gommeantilegit.minecraft"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit", "junit", "4.12")
    implementation("io.netty:netty-all:5.0.0.Alpha2")
    implementation("org.jetbrains:annotations:19.0.0")
}

