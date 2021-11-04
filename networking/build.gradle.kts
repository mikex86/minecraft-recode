plugins {
    `java-library`
    idea
}

group = "me.gommeantilegit.minecraft"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit", "junit", "4.12")
    implementation(project(":core"))
    implementation(project(":utils"))
    implementation("io.netty:netty-all:5.0.0.Alpha2")
    implementation("org.reflections:reflections:0.10.2")
    implementation("org.jetbrains:annotations:22.0.0")
}