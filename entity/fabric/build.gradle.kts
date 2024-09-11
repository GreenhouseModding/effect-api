import house.greenhouse.effectapi.gradle.Properties
import house.greenhouse.effectapi.gradle.Versions
import house.greenhouse.effectapi.gradle.props

plugins {
    id("effectapi.loader")
    id("fabric-loom")
}

val core = project(":core-common")

loom {
    val aw = core.file("src/main/resources/${props.modId}.accesswidener");
    if (aw.exists())
        accessWidenerPath.set(aw)
    mixin {
        this.defaultRefmapName.set("${props.modId}.refmap.json")
    }
    runs {
        named("client") {
            client()
            configName = "${props.modName} - Fabric Client"
            setSource(sourceSets["test"])
            ideConfigGenerated(true)
            vmArgs("-Dmixin.debug.verbose=true", "-Dmixin.debug.export=true")
            runDir = Properties.RUN_DIR.toString()
        }
        named("server") {
            server()
            configName = "${props.modName} - Fabric Server"
            setSource(sourceSets["test"])
            ideConfigGenerated(true)
            vmArgs("-Dmixin.debug.verbose=true", "-Dmixin.debug.export=true")
            runDir = Properties.RUN_DIR.toString()
        }
    }
    mods {
        register(props.modId) {
            sourceSet(sourceSets["main"])
        }
        register("${props.modId}_test") {
            sourceSet(sourceSets["test"])
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${Versions.MINECRAFT}")
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:${Versions.FABRIC_LOADER}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${Versions.FABRIC_API}")

    compileOnly(project(":core-common")) {
        capabilities {
            requireCapability("${Properties.GROUP}:${core.props.modId}-common")
        }
    }
    implementation(project(":core-fabric", "namedElements")) {
        isTransitive = false
    }
}