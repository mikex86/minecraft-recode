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
    implementation("org.jetbrains:annotations:13.0")
    implementation("com.github.oshi:oshi-core:3.4.0")
    implementation("org.jetbrains:annotations:19.0.0")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}