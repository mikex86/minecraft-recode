plugins {
    `java-library`
    application
}

group = "me.gommeantilegit.minecraft"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation(project(":utils"))
    implementation(project(":networking"))
    implementation(project(":logging"))
    implementation(project(":webui"))
    implementation("io.netty:netty-all:5.0.0.Alpha2")
    implementation("com.google.code.gson:gson:2.8.9")
    testImplementation("junit", "junit", "4.12")
    implementation("org.jetbrains:annotations:22.0.0")
}

val fatJar = task("fatJar", type = org.gradle.jvm.tasks.Jar::class) {
    archiveBaseName.set("${project.name}-fat")
    manifest {
        attributes["Implementation-Title"] = "Gradle Jar File Example"
        attributes["Implementation-Version"] = archiveVersion
        attributes["Main-Class"] = "me.gommeantilegit.minecraft.server.MinecraftServer"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}