plugins {
    `java-library`
    application
    kotlin("jvm")
}

group = "me.gommeantilegit.minecraft"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("DesktopLauncher")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.badlogicgames.gdx:gdx-platform:1.10.0")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:1.10.0")
    runtime(files("lib/natives.jar"))
    implementation(project(":client"))
    implementation(project(":core"))
    implementation("org.jetbrains:annotations:22.0.0")
}



val fatJar = task("fatJar", type = org.gradle.jvm.tasks.Jar::class) {
    archiveBaseName.set("${project.name}-fat")
    manifest {
        attributes["Implementation-Title"] = "Gradle Jar File Example"
        attributes["Implementation-Version"] = archiveVersion
        attributes["Main-Class"] = "DesktopLauncher"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}


tasks {
    "build" {
        dependsOn(fatJar)
    }
}
