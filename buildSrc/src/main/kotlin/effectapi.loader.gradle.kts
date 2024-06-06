import dev.greenhouseteam.effectapi.gradle.Properties

plugins {
    id("effectapi.common")
}

fun getCommonProjectName() : String {
    if (!project.hasProperty("effectapi.moduleName"))
        return "common"
    val moduleName = project.property("effectapi.moduleName") as String
    if (moduleName.isEmpty())
        return "common"
    return moduleName + "Common"
}


fun getArchivesNameExtension() : String {
    if (!hasProperty("effectapi.moduleName"))
        return ""
    val moduleName = properties["effectapi.moduleName"] as String
    if (moduleName.isEmpty())
        return moduleName;
    return "-$moduleName"
}

configurations {
    register("${getCommonProjectName()}Java") {
        isCanBeResolved = true
    }
    register("${getCommonProjectName()}TestJava") {
        isCanBeResolved = true
    }
    register("${getCommonProjectName()}Resources") {
        isCanBeResolved = true
    }
    register("${getCommonProjectName()}TestResources") {
        isCanBeResolved = true
    }
}

dependencies {
    compileOnly(project(":${getCommonProjectName()}")) {
        capabilities {
            requireCapability("${group}:${Properties.MOD_ID}${getArchivesNameExtension()}")
        }
    }
    testCompileOnly(project(":${getCommonProjectName()}")) {
        capabilities {
            requireCapability("${group}:${Properties.MOD_ID}${getArchivesNameExtension()}")
        }
    }
    "${getCommonProjectName()}Java"(project(":${getCommonProjectName()}", "${getCommonProjectName()}Java"))
    "${getCommonProjectName()}TestJava"(project(":${getCommonProjectName()}", "${getCommonProjectName()}TestJava"))
    "${getCommonProjectName()}Resources"(project(":${getCommonProjectName()}", "${getCommonProjectName()}Resources"))
    "${getCommonProjectName()}TestResources"(project(":${getCommonProjectName()}", "${getCommonProjectName()}TestResources"))
}

tasks {
    named<JavaCompile>("compileJava").configure {
        dependsOn(configurations.getByName("${getCommonProjectName()}Java"))
        source(configurations.getByName("${getCommonProjectName()}Java"))
    }
    named<JavaCompile>("compileTestJava").configure {
        dependsOn(configurations.getByName("${getCommonProjectName()}TestJava"))
        source(configurations.getByName("${getCommonProjectName()}TestJava"))
    }
    named<ProcessResources>("processResources").configure {
        dependsOn(configurations.getByName("${getCommonProjectName()}Resources"))
        from(configurations.getByName("${getCommonProjectName()}Resources"))
        from(configurations.getByName("${getCommonProjectName()}Resources"))
    }
    named<ProcessResources>("processTestResources").configure {
        dependsOn(configurations.getByName("${getCommonProjectName()}TestResources"))
        from(configurations.getByName("${getCommonProjectName()}TestResources"))
        from(configurations.getByName("${getCommonProjectName()}TestResources"))
    }
    named<Javadoc>("javadoc").configure {
        dependsOn(configurations.getByName("${getCommonProjectName()}Java"))
        source(configurations.getByName("${getCommonProjectName()}Java"))
    }
    named<Jar>("sourcesJar").configure {
        dependsOn(configurations.getByName("${getCommonProjectName()}Java"))
        from(configurations.getByName("${getCommonProjectName()}Java"))
        dependsOn(configurations.getByName("${getCommonProjectName()}Resources"))
        from(configurations.getByName("${getCommonProjectName()}Resources"))
    }
}