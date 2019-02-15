import org.gradle.internal.impldep.org.codehaus.plexus.component.configurator.converters.basic.UriConverter
import org.gradle.internal.remote.internal.Connection
import org.gradle.wrapper.Download
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.2.70"
}

group = "me.gommeantilegit.minecraft"
version = "1.0-SNAPSHOT"

allprojects {

    apply {
        plugin("java")
    }

    repositories{
        mavenCentral()
    }

    dependencies {
        compile("com.badlogicgames.gdx:gdx:1.9.8")
        compile("com.badlogicgames.gdx:gdx-freetype:1.9.8")
        compile("com.badlogicgames.gdx:gdx-backend-android:1.9.8")
        testCompile("junit", "junit", "4.12")
        compile(kotlin("stdlib-jdk8"))
    }

}
configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}