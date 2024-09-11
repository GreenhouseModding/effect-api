import house.greenhouse.effectapi.gradle.Properties
import house.greenhouse.effectapi.gradle.Versions
import house.greenhouse.effectapi.gradle.props
import org.apache.tools.ant.filters.LineContains

plugins {
    id("effectapi.loader")
    id("net.neoforged.moddev")
}

neoForge {
    version = Versions.NEOFORGE
    parchment {
        minecraftVersion = Versions.PARCHMENT_MINECRAFT
        mappingsVersion = Versions.PARCHMENT
    }

    val at = project(":core-common").file("src/main/resources/${props.modId}.cfg")
    if (at.exists())
        setAccessTransformers(at)
    runs {
        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            systemProperty("forge.logging.console.level", "debug")
            systemProperty("neoforge.enabledGameTestNamespaces", props.modId)
            gameDirectory.set(Properties.RUN_DIR)
        }
        create("client") {
            client()
            ideName = "${props.modName} - NeoForge Client"
            sourceSet = sourceSets["test"]
        }
        create("server") {
            server()
            ideName = "${props.modName} - NeoForge Server"
            programArgument("--nogui")
            sourceSet = sourceSets["test"]
        }
    }

    mods {
        register(props.modId) {
            sourceSet(sourceSets["main"])
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