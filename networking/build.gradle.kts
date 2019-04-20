plugins {
    java
    idea
}

group = "me.gommeantilegit.minecraft"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testCompile("junit", "junit", "4.12")
    compile(project(":core"))
    compile(project(":utils"))
    compile("io.netty:netty-all:5.0.0.Alpha2")
    compile("org.reflections:reflections:0.9.11")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}