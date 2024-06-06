pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
        maven("https://maven.neoforged.net/releases") {
            name = "NeoForged"
        }
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        maven("https://repo.spongepowered.org/repository/maven-public/") {
            name = "Sponge Snapshots"
        }

    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

// This should match the folder name of the project, or else IDEA may complain (see https://youtrack.jetbrains.com/issue/IDEA-317606)
rootProject.name = "effectapi-entity"
include("common", "fabric")

includeBuild("../base") {
    dependencySubstitution {
        substitute(module("dev.greenhouseteam:effectapi-base-common")).using(project(":common"))
    }
    dependencySubstitution {
        substitute(module("dev.greenhouseteam:effectapi-base-fabric")).using(project(":fabric"))
    }
}