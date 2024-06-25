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

fun getCapabilityNameExtension() : String {
    if (!hasProperty("effectapi.moduleName"))
        return ""
    val moduleName = properties["effectapi.moduleName"] as String
    return "-$moduleName"
}

configurations {
    register("commonJava") {
        isCanBeResolved = true
    }
    register("commonTestJava") {
        isCanBeResolved = true
    }
    register("commonResources") {
        isCanBeResolved = true
    }
    register("commonTestResources") {
        isCanBeResolved = true
    }
}

dependencies {
    compileOnly(project(getCommonProjectName())) {
        capabilities {
            requireCapability("${Properties.GROUP}:${Properties.MOD_ID}${getCapabilityNameExtension()}-common")
        }
    }
    testCompileOnly(project(getCommonProjectName())) {
        capabilities {
            requireCapability("${Properties.GROUP}:${Properties.MOD_ID}${getCapabilityNameExtension()}-common")
        }
    }
    "commonJava"(project(getCommonProjectName(), "commonJava"))
    "commonTestJava"(project(getCommonProjectName(), "commonTestJava"))
    "commonResources"(project(getCommonProjectName(), "commonResources"))
    "commonTestResources"(project(getCommonProjectName(), "commonTestResources"))
}

tasks {
    named<JavaCompile>("compileJava").configure {
        dependsOn(configurations.getByName("commonJava"))
        source(configurations.getByName("commonJava"))
    }
    named<JavaCompile>("compileTestJava").configure {
        dependsOn(configurations.getByName("commonTestJava"))
        source(configurations.getByName("commonTestJava"))
    }
    named<ProcessResources>("processResources").configure {
        dependsOn(configurations.getByName("commonResources"))
        from(configurations.getByName("commonResources"))
        from(configurations.getByName("commonResources"))
    }
    named<ProcessResources>("processTestResources").configure {
        dependsOn(configurations.getByName("commonTestResources"))
        from(configurations.getByName("commonTestResources"))
        from(configurations.getByName("commonTestResources"))
    }
    named<Javadoc>("javadoc").configure {
        dependsOn(configurations.getByName("commonJava"))
        source(configurations.getByName("commonJava"))
    }
    named<Jar>("sourcesJar").configure {
        dependsOn(configurations.getByName("commonJava"))
        from(configurations.getByName("commonJava"))
        dependsOn(configurations.getByName("commonResources"))
        from(configurations.getByName("commonResources"))
    }
}