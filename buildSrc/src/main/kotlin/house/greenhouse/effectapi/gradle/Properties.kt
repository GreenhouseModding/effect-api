package house.greenhouse.effectapi.gradle

import java.io.File

object Properties {
    const val GROUP = "house.greenhouse"
    const val MOD_ID = "effect_api"
    const val MOD_NAME = "Effect API"
    const val MOD_DESCRIPTION = "A library for generic entity effects based on enchantment effects."
    const val MOD_AUTHOR = "Greenhouse Modding Team"
    val MOD_CONTRIBUTORS = listOf("MerchantPug")
    const val LICENSE = "MPL-2.0"
    val RUN_DIR = File("../../run")

    const val HOMEPAGE = "https://modrinth.com/project/effect-api"
    const val CURSEFORGE_PROJECT_ID = "?"
    const val CURSEFORGE_PAGE = "https://www.curseforge.com/minecraft/mc-mods/effect-api"
    const val MODRINTH_PROJECT_ID = "djAVgOYf"
    const val GITHUB_REPO = "GreenhouseModding/effect-api"
    const val GITHUB_COMMITISH = Versions.MOD
}