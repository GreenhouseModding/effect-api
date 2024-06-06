import dev.greenhouseteam.effectapi.gradle.Properties
import dev.greenhouseteam.effectapi.gradle.Versions

plugins {
    id("effectapi.common")
    id("org.spongepowered.gradle.vanilla") version "0.2.1-SNAPSHOT"
}

sourceSets {
    create("generated") {
        resources {
            srcDir("src/generated/resources")
        }
    }
}

minecraft {
    version(Versions.INTERNAL_MINECRAFT)
    val aw = file("src/main/resources/${Properties.MOD_ID}-entity.accesswidener")
    if (aw.exists())
        accessWideners(aw)
}

dependencies {
    compileOnly("io.github.llamalad7:mixinextras-common:${Versions.MIXIN_EXTRAS}")
    annotationProcessor("io.github.llamalad7:mixinextras-common:${Versions.MIXIN_EXTRAS}")
    compileOnly("net.fabricmc:sponge-mixin:${Versions.FABRIC_MIXIN}")

    compileOnly(project(":base:base-common")) {
        capabilities {
            requireCapability("${Properties.GROUP}:${Properties.MOD_ID}-base")
        }
    }
}

configurations {
    register("entityCommonJava") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
    register("entityCommonTestJava") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
    register("entityCommonResources") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
    register("entityCommonTestResources") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
}

artifacts {
    add("entityCommonJava", sourceSets["main"].java.sourceDirectories.singleFile)
    add("entityCommonTestJava", sourceSets["test"].java.sourceDirectories.singleFile)
    add("entityCommonResources", sourceSets["main"].resources.sourceDirectories.singleFile)
    add("entityCommonTestResources", sourceSets["test"].resources.sourceDirectories.singleFile)
}