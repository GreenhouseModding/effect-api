import dev.greenhouseteam.effectapi.gradle.Properties

plugins {
    id("effectapi.common")
}

fun getCommonProjectName() : String {
    if (!project.hasProperty("effectapi.moduleName"))
        return ":common"
    val moduleName = project.property("effectapi.moduleName") as String
    if (moduleName.isEmpty())
        return ":common"
    return getProjectNameExtension() + getProjectNameExtension() + "-common"
}

fun getProjectNameExtension() : String {
    if (!hasProperty("effectapi.moduleName"))
        return ""
    val moduleName = properties["effectapi.moduleName"] as String
    if (moduleName.isEmpty())
        return moduleName
    return ":$moduleName"
}

fun getConfigurationsNameExtension() : String {
    if (!project.hasProperty("effectapi.moduleName"))
        return "common"
    val moduleName = project.property("effectapi.moduleName") as String
    if (moduleName.isEmpty())
        return "common"
    return "${moduleName}Common"
}

fun getCapabilityNameExtension() : String {
    if (!hasProperty("effectapi.moduleName"))
        return ""
    val moduleName = properties["effectapi.moduleName"] as String
    if (moduleName.isEmpty())
        return moduleName
    return "-$moduleName"
}

configurations {
    register("${getConfigurationsNameExtension()}Java") {
        isCanBeResolved = true
    }
    register("${getConfigurationsNameExtension()}TestJava") {
        isCanBeResolved = true
    }
    register("${getConfigurationsNameExtension()}Resources") {
        isCanBeResolved = true
    }
    register("${getConfigurationsNameExtension()}TestResources") {
        isCanBeResolved = true
    }
}

dependencies {
    compileOnly(project(getCommonProjectName())) {
        capabilities {
            requireCapability("${group}:${Properties.MOD_ID}${getCapabilityNameExtension()}")
        }
    }
    testCompileOnly(project(getCommonProjectName())) {
        capabilities {
            requireCapability("${group}:${Properties.MOD_ID}${getCapabilityNameExtension()}")
        }
    }
    "${getConfigurationsNameExtension()}Java"(project(getCommonProjectName(), "${getConfigurationsNameExtension()}Java"))
    "${getConfigurationsNameExtension()}TestJava"(project(getCommonProjectName(), "${getConfigurationsNameExtension()}TestJava"))
    "${getConfigurationsNameExtension()}Resources"(project(getCommonProjectName(), "${getConfigurationsNameExtension()}Resources"))
    "${getConfigurationsNameExtension()}TestResources"(project(getCommonProjectName(), "${getConfigurationsNameExtension()}TestResources"))
}

tasks {
    named<JavaCompile>("compileJava").configure {
        dependsOn(configurations.getByName("${getConfigurationsNameExtension()}Java"))
        source(configurations.getByName("${getConfigurationsNameExtension()}Java"))
    }
    named<JavaCompile>("compileTestJava").configure {
        dependsOn(configurations.getByName("${getConfigurationsNameExtension()}TestJava"))
        source(configurations.getByName("${getConfigurationsNameExtension()}TestJava"))
    }
    named<ProcessResources>("processResources").configure {
        dependsOn(configurations.getByName("${getConfigurationsNameExtension()}Resources"))
        from(configurations.getByName("${getConfigurationsNameExtension()}Resources"))
        from(configurations.getByName("${getConfigurationsNameExtension()}Resources"))
    }
    named<ProcessResources>("processTestResources").configure {
        dependsOn(configurations.getByName("${getConfigurationsNameExtension()}TestResources"))
        from(configurations.getByName("${getConfigurationsNameExtension()}TestResources"))
        from(configurations.getByName("${getConfigurationsNameExtension()}TestResources"))
    }
    named<Javadoc>("javadoc").configure {
        dependsOn(configurations.getByName("${getConfigurationsNameExtension()}Java"))
        source(configurations.getByName("${getConfigurationsNameExtension()}Java"))
    }
    named<Jar>("sourcesJar").configure {
        dependsOn(configurations.getByName("${getConfigurationsNameExtension()}Java"))
        from(configurations.getByName("${getConfigurationsNameExtension()}Java"))
        dependsOn(configurations.getByName("${getConfigurationsNameExtension()}Resources"))
        from(configurations.getByName("${getConfigurationsNameExtension()}Resources"))
    }
}