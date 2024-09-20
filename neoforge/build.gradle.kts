import house.greenhouse.effectapi.gradle.Properties
import house.greenhouse.effectapi.gradle.Versions
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
    addModdingDependenciesTo(sourceSets["test"])


    val at = project(":common").file("src/main/resources/${Properties.MOD_ID}.cfg")
    if (at.exists())
        setAccessTransformers(at)
    runs {
        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            systemProperty("forge.logging.console.level", "debug")
            systemProperty("neoforge.enabledGameTestNamespaces", Properties.MOD_ID)
            gameDirectory.set(Properties.RUN_DIR)
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
        register(Properties.MOD_ID) {
            sourceSet(sourceSets["main"])
        }
        register(Properties.MOD_ID + "_test") {
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