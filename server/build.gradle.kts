plugins {
    java
}

group = "me.gommeantilegit.minecraft"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compile(project(":core"))
    compile(project(":utils"))
    compile(project(":networking"))
    compile("io.netty:netty-all:5.0.0.Alpha2")
    compile("com.google.code.gson:gson:2.8.5")
    testCompile("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}