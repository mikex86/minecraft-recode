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
    compile(project(":logging"))
    compile(kotlin("stdlib-jdk8"))
    compile("org.eclipse.collections:eclipse-collections:10.0.0.M2")
    testCompile("junit", "junit", "4.12")
}