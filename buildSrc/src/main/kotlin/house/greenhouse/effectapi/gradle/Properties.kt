package house.greenhouse.effectapi.gradle

import java.io.File

object Properties {
    const val GROUP = "house.greenhouse"
    const val MOD_AUTHOR = "Greenhouse Modding Team"
    val MOD_CONTRIBUTORS = listOf("MerchantPug")
    const val LICENSE = "MPL-2.0"
    val RUN_DIR = File("../../run")

    val MODULES = mapOf(
        "core" to ModuleProperties("core", "Effect API", "effect_api", "A library for generic effects based on enchantment effects."),
        "entity" to ModuleProperties("entity", "Effect API (Entity)", "effect_api_entity", "A library for entity effects based on enchantment effects.")
    )

    val PLATFORMS = setOf(
        "common",
        "fabric",
        "neoforge"
    )

    const val HOMEPAGE = "https://modrinth.com/project/effect-api"
    const val CURSEFORGE_PROJECT_ID = "?"
    const val CURSEFORGE_PAGE = "https://www.curseforge.com/minecraft/mc-mods/effect-api"
    const val MODRINTH_PROJECT_ID = "djAVgOYf"
    const val GITHUB_REPO = "GreenhouseModding/effect-api"
    const val GITHUB_COMMITISH = Versions.MOD

    class ModuleProperties(val moduleName: String, val modName: String, val modId: String, val description: String)
}