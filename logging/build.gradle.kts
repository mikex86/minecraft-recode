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
    implementation("org.jetbrains:annotations:23.0.0")
    implementation("com.github.oshi:oshi-core:6.3.0")
    implementation("org.jetbrains:annotations:23.0.0")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}