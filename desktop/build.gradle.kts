plugins {
    java
    application
    kotlin("jvm")
}

group = "me.gommeantilegit.minecraft"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.badlogicgames.gdx:gdx-platform:1.9.8")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:1.9.8")
    runtime(files("lib/natives.jar"))
    implementation(project(":client"))
    implementation(project(":core"))
    implementation("org.jetbrains:annotations:19.0.0")
}


application {
    mainClassName = "DesktopLauncher"
}

val fatJar = task("fatJar", type = org.gradle.jvm.tasks.Jar::class) {
    baseName = "${project.name}-fat"
    manifest {
        attributes["Implementation-Title"] = "Gradle Jar File Example"
        attributes["Implementation-Version"] = version
        attributes["Main-Class"] = "DesktopLauncher"
    }
    from(configurations.runtimeClasspath.get().map({ if (it.isDirectory) it else zipTree(it) }))
    with(tasks.jar.get() as CopySpec)
}


tasks {
    "build" {
        dependsOn(fatJar)
    }
}
