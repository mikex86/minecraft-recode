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
    implementation("org.reflections:reflections:0.9.11")
    implementation("org.jetbrains:annotations:19.0.0")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}