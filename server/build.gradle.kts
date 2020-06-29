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
    implementation("com.google.code.gson:gson:2.8.5")
    testImplementation("junit", "junit", "4.12")
    implementation("org.jetbrains:annotations:19.0.0")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

application {
    mainClassName = "me.gommeantilegit.minecraft.server.MinecraftServer"
}

//val fatJar = task("fatJar", type = org.gradle.jvm.tasks.Jar::class) {
//    baseName = "${project.name}-fat"
//    manifest {
//        attributes["Implementation-Title"] = "Gradle Jar File Example"
//        attributes["Implementation-Version"] = version
//        attributes["Main-Class"] = "me.gommeantilegit.minecraft.server.MinecraftServer"
//    }
//    from(configurations.runtimeClasspath.get().map({ if (it.isDirectory) it else zipTree(it) }))
//    with(tasks.jar.get() as CopySpec)
//}
//
//
//tasks {
//    "build" {
//        dependsOn(fatJar)
//    }
//}
