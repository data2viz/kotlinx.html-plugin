import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.2.1"
    id("org.jetbrains.kotlin.jvm") version "2.0.20"
}

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")

    intellijPlatform {
        intellijIdeaCommunity("2024.3.1.1")
        testFramework(TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        name = "HTML to kotlinx.html"
        group = "io.data2viz"
        version = createProjectVersion()
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

tasks {
    test {
        useJUnit()
    }
    patchPluginXml {
        sinceBuild = "243"
        untilBuild = provider { null } // unset for the latest IDE version, see: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-p   lugin-extension.html#intellijPlatform-pluginConfiguration-ideaVersion-untilBuild
    }

    publishPlugin {
        token.set(project.findProperty("intellijPublishToken") as String?)
    }
}


fun createProjectVersion(): String {
// get version from gradle.properties
    val versionMajor: String by project
    val versionMinor: String by project

    var projectVersion = "$versionMajor.$versionMinor-SNAPSHOT"

    // get variables from github action workflow run (CI)
    val githubRef = System.getenv("GITHUB_REF")
    val githubRunNumber = System.getenv("GITHUB_RUN_NUMBER")
    if (githubRef == "refs/heads/master" && githubRunNumber != null) {
        // if run on CI set the version to the github run number
        projectVersion = "$versionMajor.$versionMinor.$githubRunNumber"
    }
    return projectVersion
}