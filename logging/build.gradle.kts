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
    compile("org.jetbrains:annotations:13.0")
    compile("com.github.oshi:oshi-core:3.4.0")
    compile(project(":utils"))
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}