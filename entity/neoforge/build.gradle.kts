import house.greenhouse.effectapi.gradle.Properties
import house.greenhouse.effectapi.gradle.Versions
import house.greenhouse.effectapi.gradle.props
import org.apache.tools.ant.filters.LineContains

plugins {
    id("effectapi.loader")
    id("net.neoforged.moddev")
}

val core = project(":core-common")

dependencies {
    compileOnly(project(":core-common")) {
        capabilities {
            requireCapability("${Properties.GROUP}:${core.props.modId}-common")
        }
    }
    implementation(project(":core-neoforge")) {
        capabilities {
            requireCapability("${Properties.GROUP}:${core.props.modId}-neoforge")
        }
    }.let {
        jarJar(it) {
            capabilities {
                requireCapability("${Properties.GROUP}:${core.props.modId}-neoforge")
            }
        }
    }
    testImplementation(project(":core-neoforge")) {
        capabilities {
            requireCapability("${Properties.GROUP}:${core.props.modId}-neoforge")
        }
    }
}

neoForge {
    version = Versions.NEOFORGE
    parchment {
        minecraftVersion = Versions.PARCHMENT_MINECRAFT
        mappingsVersion = Versions.PARCHMENT
    }
    addModdingDependenciesTo(sourceSets["test"])

    val at = project(":entity-common").file("src/main/resources/${props.modId}.cfg")
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
        register("${props.modId}_test") {
            sourceSet(sourceSets["test"])
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