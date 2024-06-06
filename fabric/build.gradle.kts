import dev.greenhouseteam.effectapi.gradle.Properties
import dev.greenhouseteam.effectapi.gradle.Versions

plugins {
    id("effectapi.loader")
    id("fabric-loom") version "1.6-SNAPSHOT"
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
        compileOnly(project(":${it}:${it}-common")) {
            capabilities {
                requireCapability("${Properties.GROUP}:${Properties.MOD_ID}-$it")
            }
        }
        implementation(project(":${it}:${it}-fabric", "namedElements"))
    }
}

loom {
    mods {
        register(Properties.MOD_ID) {
            sourceSet(sourceSets["main"])
        }
        register(Properties.MOD_ID + "test") {
            sourceSet(sourceSets["test"])
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