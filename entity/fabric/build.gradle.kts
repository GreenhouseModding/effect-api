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
        defaultRefmapName.set("${props.modId}.refmap.json")
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

repositories {
    maven {
        url = uri("https://maven.terraformersmc.com/")
    }
    maven {
        name = "ParchmentMC"
        url = uri("https://maven.parchmentmc.org")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${Versions.MINECRAFT}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${Versions.PARCHMENT_MINECRAFT}:${Versions.PARCHMENT}@zip")
    })

    modImplementation("net.fabricmc:fabric-loader:${Versions.FABRIC_LOADER}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${Versions.FABRIC_API}")

    modImplementation("com.terraformersmc:modmenu:${Versions.MOD_MENU}") {
        exclude("net.fabricmc")
    }

    compileOnly(project(":core-common")) {
        capabilities {
            requireCapability("${Properties.GROUP}:${core.props.modId}-common")
        }
    }
    testCompileOnly(project(":core-common")) {
        capabilities {
            requireCapability("${Properties.GROUP}:${core.props.modId}-common")
        }
    }
    implementation(project(":core-fabric", "namedElements")) {
        isTransitive = false
    }
    include(project(":core-fabric"))
}