import dev.greenhouseteam.effectapi.gradle.Properties
import dev.greenhouseteam.effectapi.gradle.Versions

plugins {
    id("effectapi.loader")
    id("fabric-loom")
}

apply(plugin = "fabric-loom")

loom {
    val aw = project(":entity:entity-common").file("src/main/resources/${Properties.MOD_ID}_entity.accesswidener");
    if (aw.exists())
        accessWidenerPath.set(aw)
    mixin {
        this.defaultRefmapName.set("${Properties.MOD_ID}_entity.refmap.json")
        useLegacyMixinAp = false
    }
    runs {
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${Versions.INTERNAL_MINECRAFT}")
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:${Versions.FABRIC_LOADER}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${Versions.FABRIC_API}")

    compileOnly(project(":base:base-common")) {
        capabilities {
            requireCapability("${Properties.GROUP}:${Properties.MOD_ID}-base-common")
        }
    }
    compileOnly(project(":base:base-fabric", "namedElements"))
}