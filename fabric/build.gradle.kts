import dev.greenhouseteam.effectapi.gradle.Properties
import dev.greenhouseteam.effectapi.gradle.Versions

plugins {
    id("effectapi.loader")
    id("fabric-loom")
}

repositories {
    maven {
        name = "TerraformersMC"
        url = uri("https://maven.terraformersmc.com/")
    }
}

val effectModules = setOf("base", "entity")

dependencies {
    minecraft("com.mojang:minecraft:${Versions.INTERNAL_MINECRAFT}")
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:${Versions.FABRIC_LOADER}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${Versions.FABRIC_API}")
    modLocalRuntime("com.terraformersmc:modmenu:${Versions.MOD_MENU}")

    testCompileOnly(project(":common", "commonTestJava"))

    effectModules.forEach {
        implementation(project(":${it}:${it}-common")) {
            capabilities {
                requireCapability("${Properties.GROUP}:${Properties.MOD_ID}-$it-common")
            }
        }
        implementation(project(":${it}:${it}-fabric"))
        include(project(":${it}:${it}-fabric"))
    }
}

loom {
    mods {
        register(Properties.MOD_ID) {
            sourceSet(sourceSets["main"])
        }
        register(Properties.MOD_ID + "_test") {
            sourceSet(sourceSets["test"])
        }
        register(Properties.MOD_ID + "_base") {
            sourceSet(project(":base:base-fabric").sourceSets["main"])
        }
        register(Properties.MOD_ID + "_entity") {
            sourceSet(project(":entity:entity-fabric").sourceSets["main"])
        }
    }
    runs {
        named("client") {
            client()
            configName = "Fabric Client"
            setSource(sourceSets["test"])
            ideConfigGenerated(true)
            vmArgs("-Dmixin.debug.verbose=true", "-Dmixin.debug.export=true")
        }
        named("server") {
            server()
            configName = "Fabric Server"
            setSource(sourceSets["test"])
            ideConfigGenerated(true)
            vmArgs("-Dmixin.debug.verbose=true", "-Dmixin.debug.export=true")
        }
    }
}