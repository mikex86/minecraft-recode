plugins {
    java
}

group = "me.gommeantilegit.minecraft"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testCompile("junit", "junit", "4.12")
    compile("io.netty:netty-all:5.0.0.Alpha2")
    compile(project(":core"))
    compile("org.jetbrains:annotations:13.0")
}