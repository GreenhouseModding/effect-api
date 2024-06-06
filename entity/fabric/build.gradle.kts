import dev.greenhouseteam.effectapi.gradle.Properties
import dev.greenhouseteam.effectapi.gradle.Versions

plugins {
    id("effectapi.loader")
    id("fabric-loom") version "1.6-SNAPSHOT"
}

dependencies {
    minecraft("com.mojang:minecraft:${Versions.INTERNAL_MINECRAFT}")
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:${Versions.FABRIC_LOADER}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${Versions.FABRIC_API}")
}

loom {
    val aw = project(":common").file("src/main/resources/${Properties.MOD_ID}-entity.accesswidener");
    if (aw.exists())
        accessWidenerPath.set(aw)
    mixin {
        defaultRefmapName.set("${Properties.MOD_ID}-entity.refmap.json")
    }
    runs {
    }
}