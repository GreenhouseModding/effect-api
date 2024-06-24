import dev.greenhouseteam.effectapi.gradle.Properties
import dev.greenhouseteam.effectapi.gradle.Versions
import org.apache.tools.ant.filters.LineContains

plugins {
    id("effectapi.loader")
    id("net.neoforged.moddev")
}

neoForge {
    version = Versions.NEOFORGE
    addModdingDependenciesTo(sourceSets["test"])

    val at = project(":common").file("src/main/resources/${Properties.MOD_ID}.cfg")
    if (at.exists())
        accessTransformers.add(at.absolutePath)

    runs {
        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            systemProperty("forge.logging.console.level", "debug")
            systemProperty("neoforge.enabledGameTestNamespaces", Properties.MOD_ID)
        }
        create("client") {
            client()
            sourceSet = sourceSets["test"]
        }
        create("server") {
            server()
            programArgument("--nogui")
            sourceSet = sourceSets["test"]
        }
    }

    mods {
        register("effectapi") {
            sourceSet(sourceSets["main"])
        }
        register("effectapi_test") {
            sourceSet(sourceSets["test"])
        }
        register("effectapi_base") {
            dependency(project(":base:base-neoforge"))
        }
        register("effectapi_entity") {
            dependency(project(":entity:entity-neoforge"))
        }
    }
}

val effectModules = setOf("base", "entity")

dependencies {
    testCompileOnly(project(":common", "commonTestJava"))

    effectModules.forEach {
        implementation(project(":${it}:${it}-common")) {
            capabilities {
                requireCapability("${Properties.GROUP}:${Properties.MOD_ID}-$it")
            }
        }
        jarJar(project(":${it}:${it}-neoforge")) {
            capabilities {
                requireCapability("${Properties.GROUP}:${Properties.MOD_ID}-$it")
            }
        }
    }
}

tasks {
    named<ProcessResources>("processResources").configure {
        filesMatching("*.mixins.json") {
            filter<LineContains>("negate" to true, "contains" to setOf("refmap"))
        }
    }
}