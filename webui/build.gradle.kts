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
    implementation("com.sparkjava:spark-kotlin:1.0.0-alpha")
    implementation("com.google.code.gson:gson:2.8.5")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}