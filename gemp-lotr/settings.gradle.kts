/*
 * This file was generated by the Gradle 'init' task.
 *
 * The settings file is used to specify which projects to include in your build.
 * For more detailed information on multi-project builds, please refer to https://docs.gradle.org/8.8/userguide/multi_project_builds.html in the Gradle documentation.
 */

rootProject.name = "gemp-lotr"
include(
    "gempukku:gempukku-chat-server",
    "gempukku:gempukku-context",
    "gempukku:gempukku-netty-server",
    "gempukku:gempukku-server",
    "gemp-core:gemp-collection",
    "gemp-core:gemp-common",
    "gemp-core:gemp-db",
    "gemp-core:gemp-deck",
    "gemp-core:gemp-game",
    "gemp-core:gemp-json",
    "gemp-core:gemp-player",
    "gemp-core:gemp-tournament",
    "gemp-core:gemp-transfer",
    "gemp-lotr-assembly",
    "gemp-lotr-async",
    "gemp-lotr-cards",
    "gemp-lotr-common",
    "gemp-lotr-images",
    "gemp-lotr-logic",
    "gemp-lotr-server",
)

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
