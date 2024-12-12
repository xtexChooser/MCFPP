rootProject.name = "Example"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven("https://jitpack.io/")
        maven("https://libraries.minecraft.net")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}