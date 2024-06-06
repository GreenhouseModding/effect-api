pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
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
rootProject.name = "effect-api"
include(
    "common", "fabric",
    ":baseCommon", ":baseFabric",
    ":entityCommon", ":entityFabric"
)
project(":baseCommon").projectDir = file("./base/common")
project(":baseFabric").projectDir = file("./base/fabric")
project(":entityCommon").projectDir = file("./entity/common")
project(":entityFabric").projectDir = file("./entity/fabric")