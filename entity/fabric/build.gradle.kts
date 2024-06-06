import dev.greenhouseteam.effectapi.gradle.Properties
import dev.greenhouseteam.effectapi.gradle.Versions

plugins {
    id("effectapi.loader")
    id("fabric-loom") version "1.6-SNAPSHOT" apply true
}

apply(plugin = "fabric-loom")

loom {
    val aw = project(":entityCommon").file("src/main/resources/${Properties.MOD_ID}-entity.accesswidener");
    if (aw.exists())
        accessWidenerPath.set(aw)
    mixin {
        defaultRefmapName.set("${Properties.MOD_ID}-entity.refmap.json")
    }
    runs {
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${Versions.INTERNAL_MINECRAFT}")
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:${Versions.FABRIC_LOADER}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${Versions.FABRIC_API}")

    compileOnly(project(":baseCommon")) {
        capabilities {
            requireCapability("${Properties.GROUP}:${Properties.MOD_ID}-base")
        }
    }
    implementation(project(":baseFabric", "namedElements"))
    include(project(":baseFabric", "namedElements"))
}